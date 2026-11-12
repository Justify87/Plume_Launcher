#!/usr/bin/env python3
"""Create a disposable Mullvad-hosted Plume build without editing the pinned submodule.

The source-of-truth remains third_party/mullvadvpn-app at the M01 SHA.
Everything modified by this script lives below .build/ and is regenerable.
"""
import argparse
import re
import shutil
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PIN = "aa47f606b1a159214c7279344128119fa91be5fd"
UPSTREAM = ROOT / "third_party/mullvadvpn-app"
DEFAULT_OUTPUT = ROOT / ".build/plume-mullvad"
MARKER = ".plume-generated-mullvad-build"

PLUME_LIBRARY_BUILD = r'''plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.plume"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "dev.plume.ActivityTest"

        // Preserve the original Plume generated resource without modifying its Kotlin.
        val changelog = file("CHANGELOG.md").readText().replace("\r", "")
        val section = { v: String -> changelog.substringAfter("\n## $v\n", "").substringBefore("\n## ") }
        val version = "1.7.1"
        val minor = version.split('.').take(2).joinToString(".")
        val notes = (section(version) + if (minor != version) "\n" + section(minor) else "")
            .lines().filter { it.startsWith("- ") || it.startsWith("### ") }
            .map { if (it.startsWith("### ")) "#" + it.removePrefix("### ") else it.removePrefix("- ") }
        require(notes.isNotEmpty()) { "Plume CHANGELOG.md has no notes for $version" }
        resValue("string", "whats_new", notes.joinToString("\\n") { it.replace("'", "\\'").replace("\"", "\\\"") })
    }

    buildFeatures { resValues = true }
    lint {
        disable += setOf("MissingTranslation", "ExtraTranslation")
    }
}

dependencies {
    testImplementation(libs.junit)
}
'''

ALIASES = (
    "Default", "Game", "Weather", "Notes", "Ninja", "Browser"
)


def verify_upstream() -> None:
    if not UPSTREAM.is_dir():
        raise RuntimeError("M01 submodule absent: run git submodule update --init --recursive")
    try:
        sha = subprocess.check_output(
            ["git", "-C", str(UPSTREAM), "rev-parse", "HEAD"], text=True
        ).strip()
        dirty = subprocess.check_output(
            ["git", "-C", str(UPSTREAM), "status", "--porcelain", "--ignore-submodules=all"],
            text=True,
        ).strip()
    except (subprocess.CalledProcessError, FileNotFoundError) as exc:
        raise RuntimeError("Cannot verify Mullvad Git submodule") from exc
    if sha != PIN:
        raise RuntimeError(f"Mullvad HEAD mismatch: expected {PIN}, got {sha}")
    if dirty:
        raise RuntimeError("Mullvad submodule has local modifications; refusing integration")


def overlay_manifest() -> str:
    removals = "\n".join(
        '        <activity-alias android:name="net.mullvad.mullvadvpn.feature.appearance.'
        f'impl.obfuscation.MainActivityAlt{suffix}" tools:node="remove" />'
        for suffix in ALIASES
    )
    return f'''<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <application
        android:label="@string/plume_integration_name"
        android:icon="@drawable/ic_launcher_advanced"
        android:roundIcon="@drawable/ic_launcher_advanced"
        android:banner="@drawable/banner"
        tools:replace="android:label,android:icon,android:roundIcon,android:banner">
        <!-- Mullvad service / Application remain original. Plume is the HOME entry. -->
        <activity android:name="dev.plume.MainActivity"
            android:label="@string/plume_integration_name"
            android:theme="@android:style/Theme.DeviceDefault.NoActionBar" />
        <activity-alias android:name="dev.plume.LauncherEntry"
            android:label="@string/plume_integration_name" />
        <!-- Remove the six Mullvad smartphone launcher entries only in this variant. -->
{removals}
    </application>
</manifest>
'''



def debug_variant_manifest() -> str:
    """Variant-priority overlay beats Mullvad's src/debug app icon declarations.

    In Android source-set precedence, src/debug overrides src/ossProd.
    Put Plume's app branding on the ossProdDebug variant, above both.
    Mullvad's original debug manifest stays unmodified.
    """
    return '''<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <application
        android:label="@string/plume_integration_name"
        android:icon="@drawable/ic_launcher_advanced"
        android:roundIcon="@drawable/ic_launcher_advanced"
        android:banner="@drawable/banner"
        tools:replace="android:label,android:icon,android:roundIcon,android:banner" />
</manifest>
'''


def prepare(output: Path) -> Path:
    verify_upstream()
    if output.exists():
        if not (output / MARKER).is_file():
            raise RuntimeError(f"Refusing to overwrite an unmarked directory: {output}")
        shutil.rmtree(output)

    staged = output / "mullvadvpn-app"
    shutil.copytree(
        UPSTREAM, staged, symlinks=True,
        ignore=shutil.ignore_patterns(".git", ".gradle", "target"),
    )
    output.joinpath(MARKER).write_text(PIN + "\n", encoding="utf-8")

    android = staged / "android"
    settings = android / "settings.gradle.kts"
    if 'include(":plume-ui")' in settings.read_text(encoding="utf-8"):
        raise RuntimeError("Unexpected upstream settings: plume-ui already exists")
    with settings.open("a", encoding="utf-8") as stream:
        stream.write('\n// Plume integration: only the staged build includes this module.\ninclude(":plume-ui")\n')

    host_build = android / "app/build.gradle.kts"
    content = host_build.read_text(encoding="utf-8")
    old = 'applicationId = "net.mullvad.mullvadvpn"'
    if content.count(old) != 1:
        raise RuntimeError("Unexpected Mullvad applicationId declaration")
    content = content.replace(old, 'applicationId = "dev.plume.advanced"', 1)
    content += '\n// Plume integration: TV views are a library, not a second APK.\ndependencies { implementation(project(":plume-ui")) }\n'
    host_build.write_text(content, encoding="utf-8")

    library = android / "plume-ui"
    source = ROOT / "app/src"
    for sub in ("main/java", "main/res", "test"):
        original = source / sub
        if original.is_dir():
            shutil.copytree(original, library / "src" / sub, dirs_exist_ok=True)
    shutil.copy2(source / "main/AndroidManifest.xml", library / "src/main/AndroidManifest.xml")
    shutil.copy2(ROOT / "CHANGELOG.md", library / "CHANGELOG.md")
    (library / "build.gradle.kts").write_text(PLUME_LIBRARY_BUILD, encoding="utf-8")

    overlay = android / "app/src/ossProd/AndroidManifest.xml"
    overlay.parent.mkdir(parents=True, exist_ok=True)
    overlay.write_text(overlay_manifest(), encoding="utf-8")
    debug_overlay = android / "app/src/ossProdDebug/AndroidManifest.xml"
    debug_overlay.parent.mkdir(parents=True, exist_ok=True)
    debug_overlay.write_text(debug_variant_manifest(), encoding="utf-8")
    name_res = android / "app/src/ossProd/res/values/plume_integration.xml"
    name_res.parent.mkdir(parents=True, exist_ok=True)
    name_res.write_text(
        '<resources><string name="plume_integration_name">Plume Launcher</string></resources>\n',
        encoding="utf-8",
    )
    print(f"Prepared derived single-APK build at {android}")
    print("Unmodified source: " + str(UPSTREAM))
    return android


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    path = args.output.resolve()
    if path == ROOT or path == UPSTREAM or UPSTREAM in path.parents:
        parser.error("Output must not overwrite the source tree or submodule")
    prepare(path)


if __name__ == "__main__":
    main()
