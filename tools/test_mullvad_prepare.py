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

    def test_m05_account_ui_uses_original_repositories(self):
        vpn = (prepare.ROOT / "tools/mullvad/VpnActivity.kt").read_text()
        account = (prepare.ROOT / "tools/mullvad/VpnAccountUi.kt").read_text()
        self.assertIn("VpnAccountUi(", vpn)
        self.assertIn("accounts.accountData.collect", vpn)
        self.assertIn("accountUi.onDeviceState(state)", vpn)
        # uiModule owns VoucherRepository/NewDeviceRepository; smartphone MainActivity
        # is not launched in Plume. The original host modules must still be loaded.
        self.assertIn("loadKoinModules(listOf(uiModule, paymentModule))", vpn)
        for api in (
            "accounts.login(", "accounts.logout()", "accounts.createAccount()",
            "accounts.refreshAccountData()", "accounts.deleteAccount()",
            "devices.deviceList(", "devices.removeDevice(", "vouchers.submitVoucher(",
            "newDevices.newDeviceCreated()", "createAccountUri(",
        ):
            self.assertIn(api, account)
        self.assertNotIn("SharedPreferences", account)
        self.assertNotIn("HttpURLConnection", account)
        self.assertNotIn("renameDevice(", account)
        self.assertIn("private var deviceState: DeviceState? = null", account)
        self.assertIn("Mullvad-Kontostatus wird geladen", account)
        self.assertIn("A fresh expiry", account)

    def test_m06_location_ui_uses_upstream_repositories_and_search(self):
        activity = (prepare.ROOT / "tools/mullvad/VpnActivity.kt").read_text()
        browser = (prepare.ROOT / "tools/mullvad/VpnLocationUi.kt").read_text()
        self.assertIn("addLocationPanel(detail, locationUi.build())", activity)
        self.assertIn("locationUi.start()", activity)
        self.assertIn("locationUi.onDaemonReady(daemonReady)", activity)
        self.assertIn("locationUi.requestPrimaryFocus()", activity)
        for api in (
            "filteredRelays(type)", "newFilterOnSearch(query)",
            "chooseSinglehop(item)", "relays.refreshRelayList()",
            "filters.updateSelectedOwnership(", "filters.updateSelectedProviders(",
            "lists.createCustomList(", "lists.updateCustomListName(",
            "lists.updateCustomListLocations(", "lists.deleteCustomList(",
            "recents(type)", "settings.setRecentsEnabled(",
        ):
            self.assertIn(api, browser)
        self.assertNotIn("HttpURLConnection", browser)
        self.assertNotIn("sharedPreferences", browser)
        self.assertNotIn("connection.reconnect(", browser)
        self.assertIn("twoColumns.addView(leftScroll", browser)
        self.assertIn("twoColumns.addView(rightScroll", browser)

    def test_m07_settings_bind_original_mullvad_state_and_android_system(self):
        vpn = (prepare.ROOT / "tools/mullvad/VpnActivity.kt").read_text()
        ui = (prepare.ROOT / "tools/mullvad/VpnSettingsUi.kt").read_text()
        self.assertIn("settingsUi.start()", vpn)
        self.assertIn("settingsUi.onDaemonReady(daemonReady)", vpn)
        self.assertIn("settingsUi.handleBack()", vpn)
        self.assertIn("settingsUi.requestPrimaryFocus()", vpn)
        self.assertIn("VpnSettingsUi(", vpn)
        for contract in (
            "settings.settingsUpdates.collect",
            "wireguard.wireguardConstraints.collect",
            "boot.autoStartAndConnectOnBoot.collect",
            "settings.updateContentBlockers(",
            "settings.setDnsState(",
            "settings.setCustomDns(",
            "settings.addCustomDns(",
            "deleteDns(index)",
            "InetAddressValidator.getInstance()",
            "settings.setLocalNetworkSharing(",
            "settings.setIpv6Enabled(",
            "settings.setWireguardQuantumResistant(",
            "wireguard.setDeviceIpVersion(",
            "Mtu.fromString(",
            "settings.setWireguardMtu(",
            "settings.resetWireguardMtu()",
            "boot.setAutoStartAndConnectOnBoot(",
            'Intent("android.net.vpn.SETTINGS")',
            "systemSettingsAvailable()",
        ):
            self.assertIn(contract, ui)
        self.assertNotIn("SharedPreferences", ui)
        self.assertNotIn("HttpURLConnection", ui)
        self.assertNotIn("setCustomWireguardPort", ui)  # Obfuscation belongs to M10
        self.assertIn('with_name("VpnSettingsUi.kt")', (prepare.ROOT / "tools/mullvad/prepare.py").read_text())

    def test_m08_original_split_tunneling_and_tv_package_list(self):
        activity = (prepare.ROOT / "tools/mullvad/VpnActivity.kt").read_text()
        ui = (prepare.ROOT / "tools/mullvad/VpnSplitTunnelingUi.kt").read_text()
        for expression in (
            '"Split Tunneling"', "splitTunnelingUi.start()",
            "splitTunnelingUi.onDaemonReady(daemonReady)",
            "splitTunnelingUi.onResume()", "splitTunnelingUi.onPause()",
        ):
            self.assertIn(expression, activity)
        for expression in (
            "applications.apps()", "repository.splitTunnelingEnabled.collect",
            "repository.excludedApps.collect", "repository.enableSplitTunneling(",
            "repository.excludeApp(", "repository.includeApp(",
            "preferences.showSystemAppsSplitTunneling().collect",
            "preferences.setShowSystemAppsSplitTunneling(",
            "Intent.ACTION_PACKAGE_ADDED", "Intent.ACTION_PACKAGE_REMOVED",
            "Intent.ACTION_PACKAGE_CHANGED", "Intent.ACTION_PACKAGE_REPLACED",
            "ListView(activity)", "setOnItemClickListener", "EditText(activity)",
            "getApplicationIcon(", "entry.missing",
        ):
            self.assertIn(expression, ui)
        self.assertNotIn("SharedPreferences", ui)
        self.assertNotIn("addDisallowedApplication(", ui)
        self.assertIn('with_name("VpnSplitTunnelingUi.kt")',
                      (prepare.ROOT / "tools/mullvad/prepare.py").read_text())

    def test_m09_advanced_modes_reuse_original_repositories_and_m06_browser(self):
        vpn = (prepare.ROOT / "tools/mullvad/VpnActivity.kt").read_text()
        location = (prepare.ROOT / "tools/mullvad/VpnLocationUi.kt").read_text()
        advanced = (prepare.ROOT / "tools/mullvad/VpnAdvancedUi.kt").read_text()
        settings = (prepare.ROOT / "tools/mullvad/VpnSettingsUi.kt").read_text()
        for name in ('"Erweitert"', "advancedUi.start()", "advancedUi.onDaemonReady(daemonReady)",
                     "advancedUi.onTunnelState(", "openAdvancedLocation(",
                     "RelayListType.Multihop(RelayHopType.ENTRY)",
                     "RelayListType.Multihop(RelayHopType.EXIT)",
                     "locationUi.showFor(RelayListType.Single)"):
            self.assertIn(name, vpn)
        for api in ("ModifyMultihopUseCase", "MultihopChange.Entry(Constraint.Any)",
                    "MultihopChange.Entry(Constraint.Only(item))",
                    "MultihopChange.Exit(item)", "filteredRelays(type)",
                    "recents(type)", "selectedEntryOwnership.collect",
                    "selectedExitOwnership.collect", "hopType()).fold("):
            self.assertIn(api, location)
        for api in ("wireguard.wireguardConstraints.collect", "settings.settingsUpdates.collect",
                    "hops().collect", "migration.multihopMigrationState.collect",
                    "wireguard.setMultihop(mode)", "settings.setDaitaEnabled(!current)",
                    "migration.clearMultihopMigrationState()", "preferences.setHasSeenMultihopMigrationGuide()",
                    "QuantumResistantState.On", "QuantumResistantState.Off",
                    "TunnelState.Connected", "entryHostname", "openQuantum()"):
            self.assertIn(api, advanced)
        self.assertIn("fun focusQuantum()", settings)
        self.assertIn("if (index != 1 && returnFromAdvanced)", vpn)
        self.assertIn("advancedUi.onTunnelState(if (daemonReady) latestTunnel else null)", vpn)
        self.assertIn("val mode = if (daemonReady) constraints?.multihop else null", advanced)
        self.assertIn('with_name("VpnAdvancedUi.kt")',
                      (prepare.ROOT / "tools/mullvad/prepare.py").read_text())
        self.assertNotIn("SharedPreferences", advanced)
        self.assertNotIn("setWireguardQuantumResistant(", advanced)  # M07 owns the original control
        self.assertNotIn("connection.reconnect(", location)  # matching upstream selector behavior

    def test_m10_network_uses_original_api_and_relay_repositories(self):
        activity = (prepare.ROOT / "tools/mullvad/VpnActivity.kt").read_text()
        ui = (prepare.ROOT / "tools/mullvad/VpnNetworkUi.kt").read_text()
        for text in ('"Netzwerk"', "networkUi.start()", "networkUi.onDaemonReady(daemonReady)",
                     "networkUi.handleBack()", "VpnNetworkUi("):
            self.assertIn(text, activity)
        for contract in ("settings.settingsUpdates.collect", "wireguard.wireguardConstraints.collect",
                         "relays.portRanges.collect", "relays.shadowsocksPortRanges.collect",
                         "settings.setObfuscation(mode)", "settings.setCustomWireguardPort(port)",
                         "settings.setCustomUdp2TcpObfuscationPort(port)",
                         "settings.setCustomShadowsocksObfuscationPort(port)",
                         "settings.setCustomLwoObfuscationPort(port)",
                         "api.accessMethods.collect", "api.currentAccessMethod.collect",
                         "api.getShadowsocksCiphers()", "api.testCustomApiAccessMethod(proxy)",
                         "api.testApiAccessMethodById(item.id)",
                         "api.setCurrentApiAccessMethod(item.id)",
                         "api.setEnabledApiAccessMethod(item.id, !item.enabled)",
                         "api.addApiAccessMethod(", "api.updateApiAccessMethod(",
                         "api.removeApiAccessMethod(item.id)",
                         "overrides.relayOverrides.collect", "overrides.applySettingsPatch(json)",
                         "overrides.clearAllOverrides()", "ActivityResultContracts.OpenDocument()",
                         "InetAddressValidator.getInstance().isValid(address)"):
            self.assertIn(contract, ui)
        for forbidden in ("HttpURLConnection", "SharedPreferences", "VpnService.Builder",
                          "addDisallowedApplication(", "Socket("):
            self.assertNotIn(forbidden, ui)
        self.assertIn('tabs[restoredPage].requestFocus()', activity)
        self.assertIn("val railScroll = ScrollView(this)", activity)
        self.assertIn("addView(rail, FrameLayout.LayoutParams(-1, -2))", activity)
        self.assertNotIn("ScrollView.LayoutParams", activity)
        self.assertIn("columns.addView(railScroll", activity)
        self.assertIn('with_name("VpnNetworkUi.kt")',
                      (prepare.ROOT / "tools/mullvad/prepare.py").read_text())

    def test_m11_diagnostics_bind_original_backend_and_notification_targets(self):
        activity = (prepare.ROOT / "tools/mullvad/VpnActivity.kt").read_text()
        ui = (prepare.ROOT / "tools/mullvad/VpnDiagnosticsUi.kt").read_text()
        stage = (prepare.ROOT / "tools/mullvad/prepare.py").read_text()
        for contract in ("VpnDiagnosticsUi(", "diagnosticsUi.start()",
                         "diagnosticsUi.onDaemonReady(daemonReady)",
                         "diagnosticsUi.onDeviceState(state)",
                         "diagnosticsUi.onAccountData(it)", "diagnosticsUi.dispose()",
                         "KEY_REQUEST_VPN_PROFILE"):
            self.assertIn(contract, activity)
        for contract in ("reports.readLogs()", "reports.sendReport(report, includeAccountId)",
                         "reports.deleteLogs()", "notifications.notifications.collect",
                         "preferences.preferencesFlow().collect",
                         "preferences.setLocationInNotificationEnabled(",
                         "version.versionInfo.collect", "changelog.getLastVersionChanges()",
                         "changelog.setDismissNewChangelogNotification()",
                         'ActivityResultContracts.CreateDocument("text/plain")',
                         "Exportdialog nicht verfügbar:",
                         "NotificationManagerCompat.from(activity).areNotificationsEnabled()",
                         "Supportstatus unbekannt (Dienst nicht erreichbar)",
                         "renderVersion()"):
            self.assertIn(contract, ui)
        self.assertNotIn("HttpURLConnection", ui)
        self.assertNotIn("VpnService.Builder", ui)
        self.assertIn('with_name("VpnDiagnosticsUi.kt")', stage)
        original = 'const val MAIN_ACTIVITY_CLASS = "$MULLVAD_PACKAGE_NAME.app.MainActivity"'
        updated = prepare.route_original_notifications_to_tv(original)
        self.assertIn('.app.VpnActivity"', updated)
        self.assertIn('.app.MainActivity"', original)
        self.assertNotEqual(original, updated)
        with self.assertRaises(RuntimeError):
            prepare.route_original_notifications_to_tv(updated)

    def test_prepare_isolation_and_single_application(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            upstream = root / "upstream"
            (upstream / "android/app").mkdir(parents=True)
            (upstream / "android/settings.gradle.kts").write_text('include(":app")\n')
            (upstream / "android/build.gradle.kts").write_text(
                'this.skipDirtyCheck.set(preflightSkipDirtyCheck)\n'
            )
            (upstream / "android/app/build.gradle.kts").write_text(
                'android { defaultConfig { applicationId = "net.mullvad.mullvadvpn" } }\n'
            )
            (upstream / "android/app/src/main").mkdir(parents=True)
            (root / "app/src/main/java").mkdir(parents=True)
            (root / "app/src/main/res").mkdir(parents=True)
            (root / "app/src/main/AndroidManifest.xml").write_text(
                (prepare.ROOT / "app/src/main/AndroidManifest.xml").read_text()
            )
            (root / "app/build.gradle.kts").write_text(
                'android { defaultConfig {\n'
                '        versionName = "1.7.1"\n'
                '        versionCode = 10\n'
                '} }\n'
            )
            (root / "CHANGELOG.md").write_text("## 1.7.1\n- Test\n")
            class_names = (upstream / "android/lib/common/src/main/kotlin/net/mullvad/"
                           "mullvadvpn/lib/common/constant/ClassNames.kt")
            class_names.parent.mkdir(parents=True, exist_ok=True)
            class_names.write_text(
                'const val MAIN_ACTIVITY_CLASS = "$MULLVAD_PACKAGE_NAME.app.MainActivity"\n'
            )
            with patch.object(prepare, "ROOT", root), patch.object(prepare, "UPSTREAM", upstream), \
                    patch.object(prepare, "verify_upstream"):
                generated = prepare.prepare(root / "out")
                self.assertIn('include(":plume-ui")', (generated / "settings.gradle.kts").read_text())
                host = (generated / "app/build.gradle.kts").read_text()
                self.assertIn("dev.plume.advanced", host)
                self.assertIn('abiFilters += "arm64-v8a"', host)
                self.assertNotIn("abiFilters", (upstream / "android/app/build.gradle.kts").read_text())
                self.assertIn(
                    "this.skipDirtyCheck.set(true)",
                    (generated / "build.gradle.kts").read_text()
                )
                self.assertIn(
                    "this.skipDirtyCheck.set(preflightSkipDirtyCheck)",
                    (upstream / "android/build.gradle.kts").read_text()
                )
                self.assertIn('implementation(project(":plume-ui"))', host)
                self.assertIn('net.mullvad.mullvadvpn', (upstream / "android/app/build.gradle.kts").read_text())
                self.assertIn('val version = "1.7.1"',
                              (generated / "plume-ui/build.gradle.kts").read_text())
                self.assertTrue((generated / "plume-ui/src/main/AndroidManifest.xml").exists())
                self.assertTrue((generated / "app/src/ossProdDebug/AndroidManifest.xml").exists())
                self.assertTrue((generated / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnActivity.kt").exists())
                self.assertTrue((generated / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnAccountUi.kt").exists())
                self.assertTrue((generated / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnLocationUi.kt").exists())
                self.assertTrue((generated / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnSettingsUi.kt").exists())
                self.assertTrue((generated / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnSplitTunnelingUi.kt").exists())
                self.assertTrue((generated / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnAdvancedUi.kt").exists())
                self.assertTrue((generated / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnNetworkUi.kt").exists())
                self.assertTrue((generated / "app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnDiagnosticsUi.kt").exists())
                self.assertIn('.app.VpnActivity"',
                              (generated / "lib/common/src/main/kotlin/net/mullvad/mullvadvpn/"
                               "lib/common/constant/ClassNames.kt").read_text())
                self.assertIn('.app.MainActivity"', class_names.read_text())
                self.assertFalse((generated / "app/src/ossProdDebug/kotlin/net/mullvad/mullvadvpn/app/PlumeVpnDebugActivity.kt").exists())
                self.assertIn("VpnActivity", (generated / "app/src/ossProd/AndroidManifest.xml").read_text())
                staged_manifest = (generated / "plume-ui/src/main/AndroidManifest.xml").read_text()
                self.assertIn("android.intent.category.HOME", staged_manifest)
                self.assertNotIn("android:theme=\"@android:style/Theme.DeviceDefault.NoActionBar\"\n", staged_manifest.split("<application", 1)[1].split(">", 1)[0])


if __name__ == "__main__":
    unittest.main()
