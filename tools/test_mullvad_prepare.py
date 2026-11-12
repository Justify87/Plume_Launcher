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
    def test_overlay_only_removes_six_mullvad_aliases(self):
        import xml.etree.ElementTree as ET
        xml = prepare.overlay_manifest()
        app = ET.fromstring(xml).find("application")
        self.assertEqual(len(app.findall("activity-alias")), 6)
        self.assertEqual(len(app.findall("activity")), 1)
        self.assertEqual(app.find("activity").get("{http://schemas.android.com/apk/res/android}name"),
                         "net.mullvad.mullvadvpn.app.VpnActivity")
        self.assertIn('tools:node="remove"', xml)

    def test_plume_library_manifest_keeps_home_but_not_app_theme(self):
        import xml.etree.ElementTree as ET
        original = (prepare.ROOT / "app/src/main/AndroidManifest.xml").read_text()
        result = prepare.plume_library_manifest(original)
        app = ET.fromstring(result).find("application")
        android = "{http://schemas.android.com/apk/res/android}"
        self.assertNotIn(android + "theme", app.attrib)
        self.assertNotIn(android + "networkSecurityConfig", app.attrib)
        self.assertNotIn(android + "label", app.attrib)
        main = next(entry for entry in app.findall("activity")
                    if entry.get(android + "name") == ".MainActivity")
        alias = next(entry for entry in app.findall("activity-alias")
                     if entry.get(android + "name") == ".LauncherEntry")
        self.assertEqual(main.get(android + "theme"),
                         "@android:style/Theme.DeviceDefault.NoActionBar")
        self.assertEqual(main.get(android + "label"), "@string/plume_integration_name")
        self.assertEqual(alias.get(android + "label"), "@string/plume_integration_name")
        self.assertIn("android.intent.category.HOME", result)
        self.assertIn("android.intent.category.LEANBACK_LAUNCHER", result)
        self.assertEqual(len(app.findall("service")), 2)
        self.assertEqual(len(app.findall("provider")), 1)

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
        self.assertIsNone(app.find("activity"))  # no duplicate debug-only entry
        replaced = set(app.get(tools + "replace").split(","))
        self.assertEqual(replaced, {
            "android:label", "android:icon", "android:roundIcon", "android:banner"
        })

    def test_tv_navigation_is_explicit_and_original_service_is_reused(self):
        kotlin = (prepare.ROOT / "tools/mullvad/VpnActivity.kt").read_text()
        for name in ("Status", "Standorte", "Konto", "VPN-Einstellungen", "Diagnose"):
            self.assertIn('"' + name + '"', kotlin)
        for api in ("management.tunnelState.collect", "management.connectionState.collect",
                    "connection.connect()", "connection.disconnect(", "connection.reconnect()",
                    "VpnService.prepare(this)", "serviceBinding.bind()"):
            self.assertIn(api, kotlin)
        self.assertNotIn("PlumeVpnDebugActivity", kotlin)
        main = (prepare.ROOT / "app/src/main/java/dev/plume/MainActivity.kt").read_text()
        self.assertIn("net.mullvad.mullvadvpn.app.VpnActivity", main)
        self.assertIn("refreshVpnEntry()", main)

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
            (root / "app/src/main/AndroidManifest.xml").write_text(
                (prepare.ROOT / "app/src/main/AndroidManifest.xml").read_text()
            )
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
                self.assertTrue((generated / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnActivity.kt").exists())
                self.assertFalse((generated / "app/src/ossProdDebug/kotlin/net/mullvad/mullvadvpn/app/PlumeVpnDebugActivity.kt").exists())
                self.assertIn("VpnActivity", (generated / "app/src/ossProd/AndroidManifest.xml").read_text())
                staged_manifest = (generated / "plume-ui/src/main/AndroidManifest.xml").read_text()
                self.assertIn("android.intent.category.HOME", staged_manifest)
                self.assertNotIn("android:theme=\"@android:style/Theme.DeviceDefault.NoActionBar\"\n", staged_manifest.split("<application", 1)[1].split(">", 1)[0])


if __name__ == "__main__":
    unittest.main()
