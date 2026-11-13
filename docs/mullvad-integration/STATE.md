# Plume × Mullvad — verbindlicher Übergabestand

```yaml
schema: plume-mullvad-handoff/v1
milestone: M10
status: partial
checked_on: "2026-11-13"
main_head_at_start: 0df3ead5749ad5a3f465029308c104e9babe67d1
predecessor_milestone: M09
predecessor_pr: 24
predecessor_merged_into_main: true
base_branch: main
feature_branch: feature/m10-mullvad-network-access-tv
upstream_tag: android/2026.12
upstream_sha: aa47f606b1a159214c7279344128119fa91be5fd
upstream_gitlink_unchanged: true
single_apk_application_id: dev.plume.advanced
m08_pr: 23
m08_initial_commit: 0e64a56f35fdfa860e1a5e25f7fcb01af633562b
m08_initial_ci: observed_ci_pass
m08_initial_ci_run: 43402989685
m08_final_ci: not_verified_after_final_review_commits
m09_ci: observed_ci_pass
m09_ci_run: 43409181838
m09_pr: 24
m09_pr_merged_into_main: true
m09_merged_main_sha: 0df3ead5749ad5a3f465029308c104e9babe67d1
m09_postmerge_review_branch: feature/m09-mullvad-postmerge-ui-review
m09_postmerge_review_ci: pending
m09_initial_commit: 0b3af65b302398fc175284fccad65e0f1eee6315
m09_initial_ci: observed_ci_pass
m09_initial_ci_run: 43409181838
m09_final_ci: pending_after_review_fixes
m10_ci: previously_failed_compile
m10_initial_ci_run: 43414769371
m10_retest: triggered_by_merge_fix
m10_shield_test: hardware_pending
m09_shield_test: hardware_pending
m08_shield_test: hardware_pending
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

- Ausgangs-HEAD `main`: `03d0ef3462743c50d3f0e00bde229917141da8dd` (M07 PR #22 gemergt; keine offenen PRs). Branch `feature/m08-mullvad-split-tunneling-tv`.
- Status: `partial` bis GitHub-CI und Shield-Hardwareabnahme. Pinnung unverändert: Mullvad `android/2026.12` / `aa47f606b1a159214c7279344128119fa91be5fd`; originale Quellmodule unverändert.
- Neu: `tools/mullvad/VpnSplitTunnelingUi.kt` als `ossProd`-Staging-Overlay; sechster Rail-Tab in `VpnActivity`; `prepare.py` und Staging-Tests erweitert. Original `SplitTunnelingRepository`, `ApplicationsProvider`, `UserPreferencesRepository`; keine zweite Datenhaltung/VpnService-Filterung.
- Bedienung: suchbare, virtualisierte D-Pad-App-Liste mit Icons und Paketnamen, Split-Tunneling-Toggle, ausgeschlossene/eingeschlossene Apps, System-/Hintergrundapps, dynamisches Refresh bei Paketwechsel/Resume, fehlende gespeicherte Pakete erkennbar und entfernbar. Ausschlüsse bleiben bei deaktiviertem Split Tunneling sichtbar, aber als inaktiv markiert. Android-Lockdown-Wirkung wird ausdrücklich nicht behauptet.
- Lokale Kotlin-/Rust-/ADB-Tests mangels Repo-Clone-Netz/SDK nicht ausgeführt; PR-CI nach Erstellung bewerten. Shield-Persistenz, TV-Fokus und echte App-Routen: `hardware_pending`.
- Handoff M09: nach Merge auf M08-`main` (sonst korrekt gestapelt) mit Multihop/DAITA fortfahren, keine eigene Tunnel- oder App-Regellogik hinzufügen; Shield-Abnahme M08 nachholen.

### M08 CI und Review (PR #23)

- Erstes integriertes CI auf `0e64a56f35fdfa860e1a5e25f7fcb01af633562b`: [Run 43402989685](https://github.com/Justify87/Plume_Launcher/actions/runs/43402989685) **success**. `check`: Original-Plume-Regeln, Staging-Assertions, Installer-Regressionen, Unit/Lint erfolgreich. `mullvad-apk`: gepinnter Original-Container, Kotlin/Rust/JNI-Build `BUILD SUCCESSFUL in 12m 34s`, APK-Struktur `839` Einträge mit Native-/Relay-Assets, ein Paket `dev.plume.advanced`; ARM64-Debug-Artifact `Plume-Mullvad-ARM64-Debug-74`. Keine reale Shield-Verbindung geprüft.
- Review-Nachtrag: `STATE.md`-YAML-Metadaten auf M08 aktualisiert; Markdown-Backtick-Escapes in den fünf Handoff-Dateien korrigiert. Suchfeld-Fokus erhält expliziten Rahmen (native TV-Ansicht). Diese Nachkorrekturen benötigen eigenen finalen PR-CI-Lauf; bis dahin bleibt der *neue* HEAD nicht als erneut gebaut behauptet.
- M09 übernimmt M08 erst ab tatsächlichem gemergtem PR-/CI-Stand; keine externen Testgeräte oder Tunnelprüfungen vortäuschen.

## M09 — Multihop, DAITA, Post-Quantum (13.11.2026)

- **Basis:** tatsächlicher `main`-HEAD `560ee47cffb1640af7ac9dd889b6441f78efc824`, M08 PR #23 bereits gemergt, beim Start keine offenen PRs; direkter Branch `feature/m09-mullvad-multihop-daita-tv` von `main`. Kein alter Referenz-Reset.
- **Original:** unveränderter Gitlink `third_party/mullvadvpn-app` auf `android/2026.12`/`aa47f606b1a159214c7279344128119fa91be5fd`; **keine** originalen Kotlin-, Rust-, JNI-, gRPC-, VpnService- oder Daemon-Dateien modifiziert.
- **Host-Overlays:** neue `tools/mullvad/VpnAdvancedUi.kt`, M06-Browser `VpnLocationUi.kt` parametrisiert auf `RelayListType.Single`, `Multihop(ENTRY)` und `Multihop(EXIT)`; `VpnActivity.kt` erhält Rail „Erweitert“, Live-Tunnelpfad, D-Pad-/Back-Rückkehr. `VpnSettingsUi.focusQuantum()` führt zur bereits bestehenden echten M07-PQ-Einstellung. `prepare.py` kopiert neue View in temporären `ossProd`-Host.
- **Original APIs:** `WireguardConstraintsRepository.setMultihop` für `MultihopMode.NEVER/WHEN_NEEDED/ALWAYS`; `ModifyMultihopUseCase(MultihopChange.Entry/Exit)` und `HopSelectionUseCase` für Eingang/Ausgang, `Constraint.Any` für automatische Entry-Wahl. `FilteredRelayListUseCase` filtert Entry/Exit inkl. DAITA-/Provider-/Ownership-Konflikten; `SettingsRepository.setDaitaEnabled`; PQ weiterhin `SettingsRepository.setWireguardQuantumResistant(QuantumResistantState.On/Off)` aus M07. `MultihopMigrationRepository` liefert Original-Migrationsmeldung; `TunnelState.Connected.location` ist die einzig bestätigte Anzeige des tatsächlich benutzten Entry-/Exit-Hostnamens.
- **Review:** Kein neuer VPN-Stack, keine eigene Relay-Paarungsregel, kein zusätzlicher Speicher, keine Neuverbindung nur wegen Standortwahl; explizite Neuverbindung über vorhandenen `ConnectionProxy`-Pfad. Ein Modus ist erst als aktiv markiert, wenn Original-Flow ihn bestätigt. `WHEN_NEEDED` lässt keinen manuell gewählten Entry zu; `ALWAYS` erlaubt ihn.
- **Abnahmestand:** `partial` bis CI-Kotlin/Rust/JNI-/APK-Gate und echte TV-/Tunnelprüfung. Staging-Assertions erweitert; lokale Android-Toolchain/Shield nicht vorhanden. Den aktuellen CI-Lauf erst nach PR-Anlage als nachgewiesen eintragen. `hardware_pending`: D-Pad, Single/Multi/Auto, reale Entry-/Exit-Relays, DAITA-Filter, PQ-/Tunnelwirkung, Standardwerte, Neustart/Background.
- **M10-Handoff:** auf gemergtem M09-`main` oder gestapeltem Feature-Branch aufbauen; originale Anti-Zensur-/Obfuskations- und Netzwerkoptionen integrieren, `setCustomWireguardPort` als Obfuskationsport bezeichnen, nicht als generischen WireGuard-Port. Bestehende M06–M09-Repositories/Views erhalten; alle Hardwareprüfungen nachholen, bevor `pass` gesetzt wird.

### M09 CI / Review-Korrekturen (nach Merge von PR #24)

- **Erster Build:** [Actions 43409181838](https://github.com/Justify87/Plume_Launcher/actions/runs/43409181838) auf `0b3af65b302398fc175284fccad65e0f1eee6315` hat `check=success` und `mullvad-apk=success`, inklusive Quell-Staging, Kotlin/Rust/JNI-`ossProdDebug`-Build und APK-Manifest-/Assets-Gate. Das belegt **noch keine Shield-VPN-Wirkung**.
- **Review-Fix:** Bei Rückkehr aus Multihop-Entry-/Exit-Auswahl in die normale Standortnavigation den Browser wieder in `RelayListType.Single` setzen; die Bestätigungsmeldung eines Tunnels auch beim erneuten Erreichen des Daemons synchronisieren. Modus/PQ bei getrenntem Daemon nicht als aktuell aktiv markieren, obwohl ein älterer Flowwert im Speicher steht.
- **Finale CI:** Nach dieser Revision erneut auf **neuen** PR-HEAD prüfen und Ergebnis eintragen; der erste grüne Lauf gilt ausschließlich für den ersten Commit. M09 bis dahin `partial`, Shield `hardware_pending`.

**Änderung der PR-Basis während der Umsetzung:** PR #24 wurde am 13.11.2026 10:41:36 UTC extern nach `main` gemergt (Merge-Commit `0df3ead5749ad5a3f465029308c104e9babe67d1`). Die nachträglichen Review-Fixes liegen deshalb **nicht** im ersten M09-PR, sondern müssen auf einem eigenen Branch `feature/m09-mullvad-postmerge-ui-review` direkt vom genannten Merge-Commit in einem separaten PR geprüft werden. Der erste grüne M09-Build gilt nur für den bereits gemergten Commit `0b3af65b...`; neue Review-Fixes erst nach neuem CI als gebaut bewerten.

**CI-Branchkonvention:** `.github/workflows/pr.yml` startet den integrierten `mullvad-apk`-Job nur bei PR-Head-Refs mit `mullvad` im Namen. Der erste Post-Merge-Review-PR #25 wurde deshalb vor der finalen Abnahme durch einen identischen Review-Branch mit `mullvad` im Namen ersetzt; **der in PR #25 übersprungene APK-Job ist kein Testpass**. Der neu eröffnete Review-PR wird vollständig geprüft.

## M10 — Verschleierung, API-Zugriff und Server-IP (13.11.2026)

- **Basis:** `main` `0df3ead5749ad5a3f465029308c104e9babe67d1` (M09 PR #24 bereits gemergt), bei Start keine offenen PRs. M09-PR-CI [43409181838](https://github.com/Justify87/Plume_Launcher/actions/runs/43409181838) `success`; M09-/M10-Shield-Hardwareabnahmen weiterhin offen.
- **Feature-Branch:** `feature/m10-mullvad-network-access-tv`, direkte Base `main`, keine fremden PR-Merges. Gepinnter Original-Upstream: `android/2026.12` / `aa47f606b1a159214c7279344128119fa91be5fd`, Gitlink unverändert, keine Änderung am originalen Mullvad-Rust-/Kotlin-/JNI-/gRPC-/Servicecode.
- **Implementierung:** `tools/mullvad/VpnNetworkUi.kt` mit eigenem TV-Tab „Netzwerk“ und Unterseiten Verschleierung, API-Zugriff, Server-IP. `VpnActivity.kt` übernimmt D-Pad/Back, Statusbindung und Koin-Repositories; `prepare.py` kopiert die View in das abgeleitete `ossProd`-SourceSet. `test_mullvad_prepare.py` prüft die unveränderten Original-Schreibpfade.
- **Originalzustand:** `SettingsRepository` kontrolliert die sieben im Pin vorhandenen Obfuskationsmodi samt echten Port-Settern; `RelayListRepository.portRanges` stellt die erlaubten WireGuard-/LWO-Portbereiche bereit. `ApiAccessRepository` verwaltet aktuelle/eingeschaltete Methoden und eigene SOCKS5-/Shadowsocks-Proxys mit Original-Test/CRUD; `RelayOverridesRepository` importiert JSON-Patches (Text/Datei) oder setzt alle Einträge zurück. Backend-Flows rendern den Status, Offlinezustände deaktivieren Änderungen. Keine lokale VPN- oder Proxy-Konfiguration.
- **Grenzen:** Die originale Server-IP-UI bietet am Pin JSON-Datei-/Textimport und globalen Reset, aber keinen per-Host-Edit-Setter im Repository; deshalb keine erfundene Einzel-IP-Mutation. QUIC besitzt keine eigene Portauswahl. Android-TV-Quick-Settings-Tile, Tarnungsicons, Smartphone-Touch/Share-Flows werden M11 als getrennte OS-/Gerätefragen übergeben.
- **Belegstand:** `partial` — GitHub-CI des neuen M10-PRs und echte Shield-/VPN-/Offline-/Reconnect-Tests stehen aus. Keine lokale Android-SDK-/NDK-/Rust-/ADB-Umgebung verfügbar; die Staging-Tests sind ergänzt, aber noch nicht als `pass` gemeldet. Nach erfolgreichem CI bitte CI-Run/Artifact/Commit hier ergänzen.
- **M11-Handoff:** Auf PR/Branch M10 nach Merge auf `main` (sonst gestapelt) aufsetzen. Noch nicht abgenommene Originalbereiche gemäß `FEATURE_MATRIX.md`: M01–M10 geräteabhängige VPN-/Konto-/Relay-/DNS-/Split-/Multihop-/Verschleierungs-/API-/Override-Tests; F25 Quick Tile, F34–F35 Problembericht/Logs, F36–F37 Benachrichtigungen, F38–F42 allgemeine Präferenzen/Locale/Appearance/App-Info, F41 Tarnungsicons sowie weitere unverifizierte Funktionen. Nur `pass` vergeben, wenn tatsächlich auf TV und mit Originaldienst geprüft.

### M10 CI-Fehleranalyse / Merge-Fix (13.11.2026)

- [Run 43414769371](https://github.com/Justify87/Plume_Launcher/actions/runs/43414769371): `check=success` (Staging, Launcher-Regressionen, Unit/Lint), `mullvad-apk=fail` bei `:app:compileOssProdDebugKotlin`: `VpnActivity.kt:432:38 Unresolved reference 'LayoutParams'` durch irrtümliches `ScrollView.LayoutParams` im M10-Rail. **Codekorrektur:** `FrameLayout.LayoutParams`, da `ScrollView` von `FrameLayout` erbt. Keine Änderung am Mullvad-Kern oder am containerbasierten Workflow nötig.
- **Systematischer Folgefehler:** PR #26 (M09-Nachreview) wurde nach dem M10-Abzweig in `main` gemergt. Der ältere M10-Branch war dadurch auf GitHub `mergeable_state=dirty`. M09-Fokus-, Daemon-/Offline-Korrekturen und Testassertions in einen zweieltrigen Merge-Commit auf dem M10-Branch integriert, statt die Korrekturen still zu verlieren; Gitlink/Upstream unverändert.
- Source-/Staging-Tests prüfen künftig die korrekte `FrameLayout.LayoutParams`-Schreibweise und untersagen die konkrete nichtkompilierende `ScrollView.LayoutParams`-Variante. Textprüfungen beweisen weiterhin **keine** Kompilierung; der kombinierte Gradle-APK-Build bleibt Pflichtgate. Keine ungetestete Erfolgsbehauptung, Shield weiterhin `hardware_pending`.
