# Plume × Mullvad — verbindlicher Übergabestand

```yaml
schema: plume-mullvad-handoff/v1
milestone: M07
status: partial
checked_on: "2026-11-13"
main_head_at_start: 73f50f59751f27021737bdf8a4c1b325eba9e146
predecessor_milestone: M06
predecessor_pr: 21
predecessor_merged_into_main: true
base_branch: main
feature_branch: feature/m07-mullvad-vpn-settings-tv
upstream_tag: android/2026.12
upstream_sha: aa47f606b1a159214c7279344128119fa91be5fd
upstream_gitlink_unchanged: true
single_apk_application_id: dev.plume.advanced
m06_ci: observed_ci_pass
m06_ci_run: 43339177759
m06_shield_test: not_run
m07_code: implemented_build_verified
m07_pr: 22
m07_commit: f0409b2d34bee5267afe19d25b121543f98658d4
m07_ci: observed_ci_pass
m07_ci_run: 43343996225
m07_shield_test: not_run
m07_status_reason: source_build_and_apk_verified_hardware_tests_pending
```

## M05 — tatsächliche Basis

- Der GitHub-Stand von `main` beim Start ist `81017e4de5f24660fdfa45ef2c4734914bb0a796`, Merge von PR #19 inklusive M02–M04. Keine offenen PRs bei der initialen GitHub-Abfrage; M05 baut direkt darauf.
- Letzter stabiler Mullvad-Android-Release am 13.11.2026 laut GitHub Releases: `android/2026.12` (30.09.2026). `android/2026.13-beta1` (10.11.2026) ist prerelease; Submodul-Pin bleibt ausdrücklich `aa47f606b1a159214c7279344128119fa91be5fd`.
- M04: originale `MullvadApplication`/`MullvadVpnService`/`ManagementService`/`ConnectionProxy` im generierten Host, native `VpnActivity`, sichtbarer VPN-Zugang in Classic und Overview; [CI 43328844657](https://github.com/Justify87/Plume_Launcher/actions/runs/43328844657) erfolgreich. Echte Shield-/Tunnel-/Fokusprüfungen weiterhin **nicht ausgeführt**.

## M05 — Implementierung und Grenzen

- `tools/mullvad/VpnAccountUi.kt`: eigene 10-Fuß-Kontoansicht mit Mullvads originalen `AccountRepository`, `DeviceRepository`, `VoucherRepository` und `NewDeviceRepository`. Kontoerstellung, Anmeldung, Konto-/Ablaufanzeige, Aktualisieren, Abmeldung, Geräteübersicht/-entfernung inkl. Gerätelimit, Gutschein und bestätigte endgültige Kontolöschung. Fehler werden auf verständliche Zustände abgebildet; keine Testkonten, Mock-Daten oder neu erfundenen REST-Endpunkte.
- `tools/mullvad/VpnActivity.kt`: Einbindung in vorhandene Konto-Seite und laufende Original-Flows (`ManagementService.deviceState`, `AccountRepository.accountData`); dynamischer D-Pad-Fokus auf Anmeldung oder Kontoaktionen; Rückkehr/Neustart holt Live-Daten.
- `tools/mullvad/prepare.py`: kopiert neue Konto-View gemeinsam mit `VpnActivity` in das **temporäre** `ossProd`-Host-SourceSet. Gitlink, originale Mullvad-Dateien, Dienst und JNI bleiben unverändert.
- Zeit hinzufügen: der originale `AccountRepository.getWebsiteAuthToken()` plus `createAccountUri()` öffnet `https://mullvad.net/account` mit Mullvads bestehendem Mechanismus; ohne Browser verständlicher Hinweis. Keine Play-Billing-Implementierung im OSS-Build.
- Gerät umbenennen: im gepinnten Android-`DeviceRepository`/`ManagementService` und den Login-/ManageDevices-ViewModels **kein** solcher Repository-Vorgang; deshalb bewusst nicht als funktionierend ausgegeben. Andere Dritt-App-private Kontodaten werden nicht gelesen.
- Tests: source/staging Unit-Tests ergänzt, integrierte Kotlin-/Rust-APK-CI und echte Shield-/Online-Kontotests müssen nach PR-Anlage erfolgen. Der Arbeitscontainer hat keinen GitHub-Clone-DNS-Zugang und kein Android SDK/NDK/ADB; **kein lokaler Gradle-/Shield-Pass behauptet**.

## Reproduktion

```bash
git checkout feature/m05-mullvad-accounts-tv
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
./gradlew testDebugUnitTest lintDebug
bash tools/mullvad/build-integrated.sh
python3 tools/mullvad/check_apk.py <APK>
adb devices -l
adb install -r <APK>
```

## Handoff M06

M06 übernimmt genau diese `feature/m05-mullvad-accounts-tv`-Implementierung nach Merge (oder als korrekt gestapelten Branch), behält den Upstream-Pin und verbindet `Standorte` mit originaler Mullvad-Relay-Auswahl. Die `Konto`-Seite darf nicht durch eine zweite Auth-/Device-Logik ersetzt werden. Vor vollständiger Parität zuerst den realen Kontologin, Kontoablauf, VPN-Freigabe, Tunnel sowie Fernbedienung in Classic/Overview auf NVIDIA Shield prüfen. Genaue Hardwarefälle in `TEST_MATRIX.md`.

### Review-Nachtrag: originaler UI-DI-Bootstrap

Der Originalcode registriert `VoucherRepository` und `NewDeviceRepository` in `uiModule`, nicht in `appModule`; die Mullvad-Smartphone-`MainActivity` lädt `uiModule`/`paymentModule` in `onCreate`. Da Plume diese Activity nie startet, übernimmt die native `VpnActivity` exakt `loadKoinModules(listOf(uiModule, paymentModule))`, **vor** ihrer View-Erstellung. Ohne diese Anbindung würden Koin-Auflösungen der Original-Repositories erst zur Laufzeit fehlschlagen. Der neue Source-Staging-Test verlangt diese Modulinitialisierung; das Submodul bleibt unverändert.

## M06 — aktiver Stand / Übergabe

- **Basis:** tatsächlicher `main`-HEAD `2c518bc3427c99d72820559deee5d7258791cee9` (M05 PR #20 gemergt); keine offenen PRs beim Start. M05-Workflow [43334096218](https://github.com/Justify87/Plume_Launcher/actions/runs/43334096218) hat `success`, seine Shield-Kontoabnahme bleibt `not_run`.
- **Pin:** `android/2026.12` / `aa47f606b1a159214c7279344128119fa91be5fd`, Gitlink unverändert. Ausgangs- und Relayauswahl im separaten `VpnLocationUi.kt` im temporären `ossProd`-Host; originale Kotlin/Rust/JNI-Dateien **nicht geändert**.
- **Original-Datenfluss:** `RelayListRepository.relayList/selectedLocation/refreshRelayList`, `FilteredRelayListUseCase(RelayListType.Single)`, Mullvad `newFilterOnSearch`, `RelayListFilterRepository`, `ProviderToOwnershipsUseCase`, `RecentsUseCase`, `SettingsRepository.setRecentsEnabled`, `CustomListsRepository` und `CustomListsRelayItemUseCase`. Auswahl über `SelectSinglehopUseCase`; kein erzwungener Reconnect entgegen dem Android-Upstream.
- **TV:** Zwei unabhängig scrollbar/fokussierbare Spalten für Länder und ihre Städte/Server, Suche, Lade-/Offline-/Leermeldungen, Eigentümer-/Anbieter-Dialog, zuletzt verwendete Ziele, Custom-List-Erstellen/Umbenennen/Löschen/Standorte hinzufügen/entfernen und Auswahl einer Liste als Ziel. D-Pad links zurück zum Rail, rechts in Details; bei Flow-Updates Fokus und Scrollposition anhand stabiler Schlüssel wiederherstellen.
- **Grenze:** Mullvad Android 2026.12 bietet in `RelayListRepository.updateSelectedRelayLocation` / `ManagementService.setRelayLocation` nur `RelayItemId` → `Constraint.Only`, **keine** öffentliche Singlehop-Ausgangswahl-zurück-auf-`Constraint.Any`-Operation. Der vorhandene `Any`-Zustand wird angezeigt; kein erfundener Reset-Button. Automatische Multihop-Einstiegswahl gehört zu M09; diese API nicht mit Exit verwechseln. „Favoriten“ werden über benutzerdefinierte Listen organisiert, da kein separates Favorites-Repository besteht.
- **Offen für M07:** Nach CI den echten Shield-Test (Login, Relayliste/Offline, Suche/Fokus, Filter, Create/Rename/Delete, Änderung während laufendem Tunnel und Exit-IP-Validierung) durchführen; zuvor keine vollständige M06-Parität behaupten. M07 implementiert anschließend die VPN-Einstellungen mit denselben Original-Repositories, ohne die M06-Tunnel-/Filterlogik nachzubauen.

### M06 CI-Korrektur 1 (PR #21)

- [CI 43338169709](https://github.com/Justify87/Plume_Launcher/actions/runs/43338169709): `check=success` einschließlich Staging-Tests/Plume-Unit/Lint; `mullvad-apk=fail` auf `:app:compileOssProdDebugKotlin` (fünf Fehler aus Toolbar-`action`-Aufrufen mit nachfolgendem Boolean-Parameter). Hilfsfunktionssignatur korrigiert (Click-Lambda als letzter Parameter), Renderer überprüft und D-Pad-Fokuskonflikt beim Auswählen eines linken Eintrags behoben. Nächster CI-Lauf muss den Fix nachweisen, nicht vorher `pass` setzen.

## M07 — aktuelle Übergabe (13.11.2026)

- **Ausgang:** `main` `73f50f59751f27021737bdf8a4c1b325eba9e146`, M06 PR #21 am 13.11.2026 gemergt. M06-PR-CI [43339177759](https://github.com/Justify87/Plume_Launcher/actions/runs/43339177759) `success`; Shield-Funktionstests von M06 weiterhin `not_run`. Kein gestapelter PR nötig.
- **Upstream:** stabiler Tag `android/2026.12` erneut verifiziert; Gitlink `aa47f606b1a159214c7279344128119fa91be5fd` eingefroren. Alle neuen Änderungen sind ausschließlich Plume-eigene Staging-Overlays. Original-Quellen/Daemon/JNI werden nicht geändert.
- **M07-Code:** `tools/mullvad/VpnSettingsUi.kt` wird als native View in die originale Mullvad-Host-APK gestaged. `VpnActivity` zeigt `VPN-Einstellungen` mit DNS-Unterseite statt Platzhalter und bindet originale `SettingsRepository`, `WireguardConstraintsRepository`, `AutoStartAndConnectOnBootRepository`, `DeleteCustomDnsUseCase`, `SystemVpnSettingsAvailableUseCase` (Koin `uiModule` bereits M05).
- **API & Persistenz:** `settingsUpdates` und `wireguardConstraints` sowie Boot-Komponenten-StateFlow sind die einzige Wahrheit. DNS-Filter, eigene IPv4/IPv6-DNS-Server mit Original-Validierungsbibliothek, LAN, IPv6, Quantumschutz, Geräte-IP-Version, MTU/Reset und Boot-Autoconnect sind mit realen Settern verdrahtet. Kein künstliches Konfigurations-Cache.
- **Android-Grenze:** `android.net.vpn.SETTINGS` öffnet bei Verfügbarkeit den OS-Bildschirm; Always-on/Lockdown werden **nicht** als von Plume ausles- oder setzbare Toggles vorgetäuscht. Mullvads Boot-Receiver benötigt vorhandene System-VPN-Erlaubnis und Android-Startbedingungen. `setCustomWireguardPort` steuert den **Obfuskationsport** und bleibt M10, nicht falsch als allgemeiner WireGuard-Port beschriften.
- **Tests:** Quell-/Staging-Assertions geschrieben; bei Erstellung weder lokaler Android-/Rust-Build noch Shield/ADB möglich. CI erst nach PR prüfen. Kein `pass` für reale DNS-/LAN-/Boot-/Tunnel-/TV-Funktion ohne Geräteevidenz.
- **M08:** baut ab exakt diesem Feature-Branch nach Merge auf `main`, andernfalls als Stack. Original-Split-Tunneling-Repository und reale App-Liste verwenden; die M07-Settings-Seite erhalten, bestehende `SystemVpnSettingsAvailableUseCase`-Grenze respektieren.

### M07 Build-/Review-Nachtrag — PR #22

- **Code-HEAD vor Dokumentationsnachtrag:** `f0409b2d34bee5267afe19d25b121543f98658d4`. [PR #22](https://github.com/Justify87/Plume_Launcher/pull/22) basiert auf gemergtem M06-`main` `73f50f59751f27021737bdf8a4c1b325eba9e146`; kein Submodul-/Upstream-Diff.
- **CI:** [Run 43343996225](https://github.com/Justify87/Plume_Launcher/actions/runs/43343996225), vollständiger Status `success`. `check` hat Staging-Python-Tests, Plume-Unit-Tests/Lint, Launcher-Installer-Regressionen und Projektregeln bestanden. `mullvad-apk` hat den originalen Container-/Kotlin-/Rust-/JNI-Integrationsbuild bestanden (`BUILD SUCCESSFUL in 10m 10s`), die APK geprüft (`APK structure OK (839 entries; Rust/JNI and relay assets present)`) und das ARM64-Debug-Artifact hochgeladen. Dies ist **kein** echter VPN-/Hardware-Nachweis.
- **Review-Korrekturen im selben PR:** `d2855c8` trennt DNS-Feldverfügbarkeit von Offline-/Default-DNS-State und initialisiert die Filterknöpfe; `f0409b2` entfernt eine irrtümliche zusätzliche `dns.apply`-Layout-Verschachtelung vor erfolgreichem Kotlin-CI. Alt-Läufe `43343229823`/`43343427456` wurden wegen neuer PR-Commits automatisch abgebrochen; **nicht** als Buildfehler zählen.
- **Grenzen:** Tunnel-DNS-Effekt, D-Pad auf Shield, Persistenz nach Neustart, LAN-Effekt, Auto-Boot und Android-System-Always-on weiterhin `not_run` (keine Shield vorhanden). M07 bleibt **`partial`** und wird **nicht** zum Hardware-`pass` erklärt. M08 folgt erst diesem Branch/PR nach Merge oder korrekt gestapelt auf seinem HEAD.


## M08 — aktuelle Übergabe (13.11.2026)

- Ausgangs-HEAD \`main\`: \`03d0ef3462743c50d3f0e00bde229917141da8dd\` (M07 PR #22 gemergt; keine offenen PRs). Branch \`feature/m08-mullvad-split-tunneling-tv\`.
- Status: \`partial\` bis GitHub-CI und Shield-Hardwareabnahme. Pinnung unverändert: Mullvad \`android/2026.12\` / \`aa47f606b1a159214c7279344128119fa91be5fd\`; originale Quellmodule unverändert.
- Neu: \`tools/mullvad/VpnSplitTunnelingUi.kt\` als \`ossProd\`-Staging-Overlay; sechster Rail-Tab in \`VpnActivity\`; \`prepare.py\` und Staging-Tests erweitert. Original \`SplitTunnelingRepository\`, \`ApplicationsProvider\`, \`UserPreferencesRepository\`; keine zweite Datenhaltung/VpnService-Filterung.
- Bedienung: suchbare, virtualisierte D-Pad-App-Liste mit Icons und Paketnamen, Split-Tunneling-Toggle, ausgeschlossene/eingeschlossene Apps, System-/Hintergrundapps, dynamisches Refresh bei Paketwechsel/Resume, fehlende gespeicherte Pakete erkennbar und entfernbar. Ausschlüsse bleiben bei deaktiviertem Split Tunneling sichtbar, aber als inaktiv markiert. Android-Lockdown-Wirkung wird ausdrücklich nicht behauptet.
- Lokale Kotlin-/Rust-/ADB-Tests mangels Repo-Clone-Netz/SDK nicht ausgeführt; PR-CI nach Erstellung bewerten. Shield-Persistenz, TV-Fokus und echte App-Routen: \`hardware_pending\`.
- Handoff M09: nach Merge auf M08-\`main\` (sonst korrekt gestapelt) mit Multihop/DAITA fortfahren, keine eigene Tunnel- oder App-Regellogik hinzufügen; Shield-Abnahme M08 nachholen.
