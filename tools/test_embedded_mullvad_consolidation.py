#!/usr/bin/env python3
"""Checks for PR47 -> PR48 Mullvad embedding; no Android SDK needed.

These are structural contract tests, not substitutes for the combined Kotlin
compile or a real tunnel test on NVIDIA Shield.
"""
import importlib.util
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = ROOT / "tools/mullvad/prepare.py"
spec = importlib.util.spec_from_file_location("plume_mullvad_prepare", source)
prepare = importlib.util.module_from_spec(spec)
spec.loader.exec_module(prepare)


class EmbeddedMullvadConsolidationTest(unittest.TestCase):
    def test_standalone_and_combined_use_same_component_activity_dependency(self):
        standalone = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        combined = prepare.PLUME_LIBRARY_BUILD
        for script in (standalone, combined):
            self.assertIn('implementation("androidx.activity:activity:1.10.1")', script)
            # The pinned Mullvad dependency-verification lockfile contains the
            # core-ktx 1.16.0 AAR hash but not the 1.13.0 AAR hash.
            self.assertIn('implementation("androidx.core:core-ktx:1.16.0")', script)
            self.assertNotIn('implementation("androidx.core:core-ktx:1.13.0")', script)

    def test_original_backend_and_all_eight_tv_pages_are_retained(self):
        panel = (ROOT / "tools/mullvad/MullvadEmbeddedPanel.kt").read_text()
        for symbol in (
            'management.tunnelState.collect', 'management.connectionState.collect',
            'ServiceConnectionManager(host.applicationContext)',
            'VpnService.prepare(host)', 'serviceBinding.bind()',
            'connection.connect()', 'connection.disconnect(', 'connection.reconnect()',
            'VpnAccountUi(', 'VpnLocationUi(', 'VpnSettingsUi(',
            'VpnSplitTunnelingUi(', 'VpnAdvancedUi(', 'VpnNetworkUi(',
            'VpnDiagnosticsUi(', 'fun handleBack(): Boolean',
        ):
            with self.subTest(symbol=symbol):
                self.assertIn(symbol, panel)
        self.assertNotIn('SharedPreferences', panel)
        self.assertNotIn('HttpURLConnection', panel)

    def test_panel_registers_consent_before_start_and_activates_only_when_visible(self):
        host = (ROOT / "app/src/main/java/dev/plume/MainActivity.kt").read_text()
        panel = (ROOT / "tools/mullvad/MullvadEmbeddedPanel.kt").read_text()
        self.assertIn('class MainActivity : ComponentActivity()', host)
        self.assertIn('super.onActivityResult(code, result, data)', host)
        self.assertIn('super.onRequestPermissionsResult(code, perms, results)', host)
        self.assertIn('installEmbeddedVpnPane()', host)
        self.assertIn('registerForActivityResult(', panel)
        self.assertIn('onVisibilityAggregated(isVisible: Boolean)', panel)
        self.assertIn('if (isVisible) activate()', panel)
        self.assertNotIn('onViewAttachedToWindow(v: View) { activate() }', panel)

    def test_in_launcher_uses_embedded_view_not_legacy_vpn_activity(self):
        host = (ROOT / "app/src/main/java/dev/plume/MainActivity.kt").read_text()
        self.assertIn('vDesign.installWorkspacePane(VDesignHomeView.VPN, content)', host)
        self.assertIn('Class.forName("net.mullvad.mullvadvpn.app.MullvadEmbeddedPanel")', host)
        self.assertIn('getMethod("handleBack")', host)
        self.assertNotIn('WorkspaceVpnEntryPane(', host)
        self.assertFalse((ROOT / "app/src/main/java/dev/plume/WorkspaceVpnEntryPane.kt").exists())

    def test_derived_host_stages_panel_and_preserves_r8_reflection(self):
        staging = (ROOT / "tools/mullvad/prepare.py").read_text()
        self.assertIn('with_name("MullvadEmbeddedPanel.kt")', staging)
        self.assertIn('consumerProguardFiles("consumer-rules.pro")', staging)
        self.assertIn('public static android.view.View create(androidx.activity.ComponentActivity)', staging)
        self.assertIn('public boolean handleBack()', staging)
        self.assertIn('class MullvadEmbeddedPanel private constructor', (
            ROOT / "tools/mullvad/MullvadEmbeddedPanel.kt").read_text())


if __name__ == "__main__":
    unittest.main()
