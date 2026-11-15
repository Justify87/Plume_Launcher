#!/usr/bin/env python3
"""Create a disposable Mullvad-hosted Plume build without editing the pinned submodule.

The source-of-truth remains third_party/mullvadvpn-app at the M01 SHA.
Everything modified by this script lives below .build/ and is regenerable.
"""
import argparse
import re
import shutil
import xml.etree.ElementTree as ET
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
        consumerProguardFiles("consumer-rules.pro")

        // Preserve the original Plume generated resource without modifying its Kotlin.
        val changelog = file("CHANGELOG.md").readText().replace("\r", "")
        val section = { v: String -> changelog.substringAfter("\n## $v\n", "").substringBefore("\n## ") }
        val version = "__PLUME_VERSION_NAME__"
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
    implementation("androidx.activity:activity:1.10.1")
    // Pinned Mullvad verification-metadata already trusts the 1.16.0 AAR.
    // Activity pulls 1.13.0, whose AAR is not in the upstream lockfile.
    implementation("androidx.core:core-ktx:1.16.0")
    testImplementation(libs.junit)
}
'''

ALIASES = (
    "Default", "Game", "Weather", "Notes", "Ninja", "Browser"
)



# The pinned upstream verification file already authenticates these module
# resolves their JAR counterparts. These exact JAR SHA-256 values are cross-
#   - org.jetbrains.kotlinx ... 1.6.4: Briar witness.gradle and rules_kotlin lock
# Never disable verification or write unreviewed downloaded hashes to a lock.
# Only the temporary generated host changes; the pinned submodule stays intact.
WITNESSED_COMPILER_JARS = (
    ("kotlinx-coroutines-android",
     "68516559e6f84a621b9783cd892a64630ccd7875843588ddb3f0501425e33f15",
     "3fdc0eed5bc4b83ee9622774520a2db25470370eacd1581cac1e37704f095b00"),
    ("kotlinx-coroutines-core-jvm",
     "0d94c8a41483e7c2707ebd693e1b1357a84152998ce85550ebbc54ca4321a3a7",
     "c24c8bb27bb320c4a93871501a7e5e0c61607638907b197aef675513d4c820be"),
)

# (group, name, version, pinned upstream module SHA-256, independently witnessed JAR SHA-256)
WITNESSED_RUNTIME_JARS = (
    ("androidx.collection", "collection-jvm", "1.4.2",
     "aad6b3536c0f0e570aa733d5141d70fa6b9b3add370f73a311ca4ca5dee25448",
     "984ce9bd780055eafb8a105aca3f514686bf7b1b6272ec7c645a98ce40fc7db4"),
    ("org.jetbrains.kotlinx", "kotlinx-coroutines-android", "1.7.3",
     "48dfd8139edee5481bcc8b25e24d7586aca615f0d1ed2beb242dc7478ed4cee0",
     "59fffb26bee12c32dadcfa5d420c2a7db85d3253518128b170efda726613256d"),
)


def staged_verification_artifacts():
    """Explicit set of artifacts the generated single-APK host adds."""
    for name, module_sha, jar_sha in WITNESSED_COMPILER_JARS:
        yield ("org.jetbrains.kotlinx", name, "1.6.4", module_sha, jar_sha)
    yield from WITNESSED_RUNTIME_JARS


def stage_witnessed_compiler_checksums(path: Path) -> None:
    """Prepare a disposable lockfile with independently witnessed JAR hashes.

    Verify the exact pinned upstream .module SHA-256 before adding its matching
    JAR SHA-256. A missing/changed component or duplicate entry fails closed.
    All checks happen before writing the complete replacement file.
    """
    original = path.read_text(encoding="utf-8")
    updated = original
    for group, name, version, module_sha, jar_sha in staged_verification_artifacts():
        opening = (
            f'      <component group="{group}" name="{name}" version="{version}">\n'
        )
        if updated.count(opening) != 1:
            raise RuntimeError(
                f"Unexpected pinned Gradle metadata for {group}:{name}:{version}"
            )
        begin = updated.index(opening)
        end = updated.find("      </component>", begin)
        if end < 0:
            raise RuntimeError(
                f"Incomplete Gradle verification component: {group}:{name}:{version}"
            )
        block = updated[begin:end]
        module = (
            f'         <artifact name="{name}-{version}.module">\n'
            f'            <sha256 value="{module_sha}"'
        )
        if module not in block:
            raise RuntimeError(
                f"Unrecognized upstream module SHA256 for {group}:{name}:{version}"
            )
        if f'<artifact name="{name}-{version}.jar">' in block:
            raise RuntimeError(f"JAR is already pinned: {group}:{name}:{version}")
        addition = (
            f'         <artifact name="{name}-{version}.jar">\n'
            f'            <sha256 value="{jar_sha}" origin="Independent lockfile witness"/>\n'
            f'         </artifact>\n'
        )
        updated = (
            updated[:begin]
            + block.replace(module, addition + module, 1)
            + updated[end:]
        )
    path.write_text(updated, encoding="utf-8")


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
        android:usesCleartextTraffic="true"
        android:networkSecurityConfig="@xml/network_security_config"
        tools:replace="android:label,android:icon,android:roundIcon,android:banner">
        <!-- HOME/LEANBACK and all Plume components come from the library manifest.
             Only the host owns the application-wide attributes. -->
        <!-- Remove the six Mullvad smartphone launcher entries only in this variant. -->
{removals}
        <!-- Original Mullvad backend, dedicated Plume TV UI. -->
        <activity android:name="net.mullvad.mullvadvpn.app.VpnActivity"
            android:exported="false"
            android:screenOrientation="landscape"
            android:theme="@android:style/Theme.DeviceDefault.NoActionBar" />
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
        tools:replace="android:label,android:icon,android:roundIcon,android:banner">
        <!-- Production VpnActivity lives in the ossProd manifest for every build type. -->
    </application>
</manifest>
'''


def route_original_notifications_to_tv(source: str) -> str:
    """Only the staged host: reuse original PendingIntent / QS Tile, open TV Activity.

    The upstream constant targets Mullvad MainActivity (phone Compose UI). This
    single redirect preserves all original notification and VPN service logic.
    """
    original = 'const val MAIN_ACTIVITY_CLASS = "$MULLVAD_PACKAGE_NAME.app.MainActivity"'
    replacement = 'const val MAIN_ACTIVITY_CLASS = "$MULLVAD_PACKAGE_NAME.app.VpnActivity"'
    if source.count(original) != 1:
        raise RuntimeError("Pinned Mullvad MAIN_ACTIVITY_CLASS changed; review the routing")
    return source.replace(original, replacement)


ANDROID_NS = "{http://schemas.android.com/apk/res/android}"


def plume_library_manifest(original: str) -> str:
    """Keep Plume components and intent filters, but let Mullvad own the app.

    The original Plume manifest is an APPLICATION manifest. An Android library
    must not impose its application theme or branding on the Mullvad host.
    Scope Plume's classic View theme and label to its launcher entries.
    """
    ET.register_namespace("android", "http://schemas.android.com/apk/res/android")
    ET.register_namespace("tools", "http://schemas.android.com/tools")
    root = ET.fromstring(original)
    app = root.find("application")
    if app is None:
        raise RuntimeError("Plume manifest is missing <application>")
    for attribute in (
        "label", "icon", "roundIcon", "banner",
        "usesCleartextTraffic", "networkSecurityConfig", "theme",
    ):
        app.attrib.pop(ANDROID_NS + attribute, None)

    for tag, name in (("activity", ".MainActivity"), ("activity-alias", ".LauncherEntry")):
        component = next(
            (entry for entry in app.findall(tag) if entry.get(ANDROID_NS + "name") == name),
            None,
        )
        if component is None:
            raise RuntimeError(f"Plume manifest is missing {tag} {name}")
        component.set(ANDROID_NS + "label", "@string/plume_integration_name")
        if tag == "activity":
            component.set(ANDROID_NS + "theme", "@android:style/Theme.DeviceDefault.NoActionBar")

    return ET.tostring(root, encoding="unicode", xml_declaration=True) + "\n"


def plume_library_build() -> str:
    """Keep the staged Plume library's version/What's new in sync with app Gradle."""
    build = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
    names = re.findall(r'(?m)^\s*versionName\s*=\s*"([^"]+)"', build)
    if len(names) != 1 or not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+(?:-(?:beta|rc)[0-9]+)?", names[0]):
        raise RuntimeError("Exactly one valid Plume versionName is required")
    original = 'val version = "__PLUME_VERSION_NAME__"'
    if PLUME_LIBRARY_BUILD.count(original) != 1:
        raise RuntimeError("Staged Plume version template changed unexpectedly")
    return PLUME_LIBRARY_BUILD.replace(original, f'val version = "{names[0]}"', 1)


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
    stage_witnessed_compiler_checksums(android / "gradle/verification-metadata.xml")
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
    # Rust/JNI is built for ARM64 only. Without a packaging filter, native
    # Change only the disposable host; never patch pinned Mullvad sources.
    content = content.replace(old, '''applicationId = "dev.plume.advanced"
        ndk {
            abiFilters += "arm64-v8a"
        }''', 1)
    content += '\n// Plume integration: TV views are a library, not a second APK.\ndependencies { implementation(project(":plume-ui")) }\n'
    host_build.write_text(content, encoding="utf-8")

    # Mullvad's releasePreflight executes "git status --porcelain", but the
    # this generator. Keep releasePreflight itself (including build header),
    root_build = android / "build.gradle.kts"
    root_contents = root_build.read_text(encoding="utf-8")
    original_preflight = "this.skipDirtyCheck.set(preflightSkipDirtyCheck)"
    if root_contents.count(original_preflight) != 1:
        raise RuntimeError("Unexpected pinned Mullvad releasePreflight contract")
    root_build.write_text(
        root_contents.replace(
            original_preflight,
            "this.skipDirtyCheck.set(true) // Disposable Plume host has no .git",
            1,
        ),
        encoding="utf-8",
    )

    library = android / "plume-ui"
    source = ROOT / "app/src"
    for sub in ("main/java", "main/res", "test"):
        original = source / sub
        if original.is_dir():
            shutil.copytree(original, library / "src" / sub, dirs_exist_ok=True)
    (library / "src/main/AndroidManifest.xml").write_text(
        plume_library_manifest((source / "main/AndroidManifest.xml").read_text(encoding="utf-8")),
        encoding="utf-8",
    )
    shutil.copy2(ROOT / "CHANGELOG.md", library / "CHANGELOG.md")
    (library / "build.gradle.kts").write_text(plume_library_build(), encoding="utf-8")
    (library / "consumer-rules.pro").write_text(
        "-keep class net.mullvad.mullvadvpn.app.MullvadEmbeddedPanel { " +
        "public static android.view.View create(androidx.activity.ComponentActivity); " +
        "public boolean handleBack(); }\n",
        encoding="utf-8",
    )

    overlay = android / "app/src/ossProd/AndroidManifest.xml"
    overlay.parent.mkdir(parents=True, exist_ok=True)
    overlay.write_text(overlay_manifest(), encoding="utf-8")
    debug_overlay = android / "app/src/ossProdDebug/AndroidManifest.xml"
    debug_overlay.parent.mkdir(parents=True, exist_ok=True)
    debug_overlay.write_text(debug_variant_manifest(), encoding="utf-8")
    vpn_activity = android / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnActivity.kt"
    vpn_activity.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(Path(__file__).with_name("VpnActivity.kt"), vpn_activity)
    shutil.copy2(Path(__file__).with_name("VpnAccountUi.kt"), vpn_activity.with_name("VpnAccountUi.kt"))
    shutil.copy2(Path(__file__).with_name("VpnLocationUi.kt"), vpn_activity.with_name("VpnLocationUi.kt"))
    shutil.copy2(Path(__file__).with_name("VpnSettingsUi.kt"), vpn_activity.with_name("VpnSettingsUi.kt"))
    shutil.copy2(Path(__file__).with_name("VpnSplitTunnelingUi.kt"), vpn_activity.with_name("VpnSplitTunnelingUi.kt"))
    shutil.copy2(Path(__file__).with_name("VpnAdvancedUi.kt"), vpn_activity.with_name("VpnAdvancedUi.kt"))
    shutil.copy2(Path(__file__).with_name("VpnNetworkUi.kt"), vpn_activity.with_name("VpnNetworkUi.kt"))
    shutil.copy2(Path(__file__).with_name("VpnDiagnosticsUi.kt"), vpn_activity.with_name("VpnDiagnosticsUi.kt"))
    shutil.copy2(Path(__file__).with_name("VpnTvStyle.kt"), vpn_activity.with_name("VpnTvStyle.kt"))
    shutil.copy2(Path(__file__).with_name("MullvadEmbeddedPanel.kt"), vpn_activity.with_name("MullvadEmbeddedPanel.kt"))
    # *disposable staging tree only*, never edit the pinned original submodule.
    class_names = android / ("lib/common/src/main/kotlin/net/mullvad/mullvadvpn/"
        "lib/common/constant/ClassNames.kt")
    if not class_names.is_file():
        raise RuntimeError("Pinned Mullvad ClassNames.kt not found in staged host")
    class_names.write_text(
        route_original_notifications_to_tv(class_names.read_text(encoding="utf-8")),
        encoding="utf-8",
    )
    name_res = android / "app/src/ossProd/res/values/plume_integration.xml"
    name_res.parent.mkdir(parents=True, exist_ok=True)
    name_res.write_text(
        '<resources><string name="plume_integration_name">Plume Launcher Advanced</string></resources>\n',
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
