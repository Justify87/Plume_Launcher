#!/usr/bin/env python3
"""Pure-Python preparation tests; no Android SDK or upstream checkout needed."""
import importlib.util
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("prepare", Path(__file__).parent / "mullvad/prepare.py")
prepare = importlib.util.module_from_spec(spec)
spec.loader.exec_module(prepare)


class PreparationTests(unittest.TestCase):
    def test_overlay_has_six_alias_removals_and_plume_home(self):
        xml = prepare.overlay_manifest()
        import xml.etree.ElementTree as ET
        root = ET.fromstring(xml)
        app = root.find("application")
        self.assertEqual(len(app.findall("activity-alias")), 7)
        self.assertIn('dev.plume.MainActivity', xml)
        self.assertIn('tools:node="remove"', xml)

    def test_debug_variant_manifest_overrides_mullvad_debug_branding(self):
        import xml.etree.ElementTree as ET
        xml = prepare.debug_variant_manifest()
        root = ET.fromstring(xml)
        app = root.find("application")
        android = "{http://schemas.android.com/apk/res/android}"
        tools = "{http://schemas.android.com/tools}"
        self.assertEqual(app.get(android + "icon"), "@drawable/ic_launcher_advanced")
        self.assertEqual(app.get(android + "roundIcon"), "@drawable/ic_launcher_advanced")
        self.assertEqual(app.get(android + "banner"), "@drawable/banner")
        replaced = set(app.get(tools + "replace").split(","))
        self.assertEqual(replaced, {
            "android:label", "android:icon", "android:roundIcon", "android:banner"
        })

    def test_prepare_isolation_and_single_application(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            upstream = root / "upstream"
            (upstream / "android/app").mkdir(parents=True)
            (upstream / "android/settings.gradle.kts").write_text('include(":app")\n')
            (upstream / "android/app/build.gradle.kts").write_text(
                'android { defaultConfig { applicationId = "net.mullvad.mullvadvpn" } }\n'
            )
            (upstream / "android/app/src/main").mkdir(parents=True)
            (root / "app/src/main/java").mkdir(parents=True)
            (root / "app/src/main/res").mkdir(parents=True)
            (root / "app/src/main/AndroidManifest.xml").write_text("<manifest/>")
            (root / "CHANGELOG.md").write_text("## 1.7.1\n- Test\n")
            with patch.object(prepare, "ROOT", root), patch.object(prepare, "UPSTREAM", upstream), \
                    patch.object(prepare, "verify_upstream"):
                generated = prepare.prepare(root / "out")
                self.assertIn('include(":plume-ui")', (generated / "settings.gradle.kts").read_text())
                host = (generated / "app/build.gradle.kts").read_text()
                self.assertIn("dev.plume.advanced", host)
                self.assertIn('implementation(project(":plume-ui"))', host)
                self.assertIn('net.mullvad.mullvadvpn', (upstream / "android/app/build.gradle.kts").read_text())
                self.assertTrue((generated / "plume-ui/src/main/AndroidManifest.xml").exists())
                self.assertTrue((generated / "app/src/ossProdDebug/AndroidManifest.xml").exists())


if __name__ == "__main__":
    unittest.main()
