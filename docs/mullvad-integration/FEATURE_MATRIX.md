# Feature-Matrix — Inventar des gepinnten Mullvad Android 2026.12

Quelle für **alle** Zeilen: `third_party/mullvadvpn-app/` bei SHA `aa47f606b1a159214c7279344128119fa91be5fd`. Pfade in Tabelle relativ zu `android/`. `lib/feature/<name>/impl` meint die originale Featureimplementierung; `lib/repository`, `lib/usecase` und `app` sind kein neu zu erfindender Stack. `TV` benennt die **tatsächliche M11-Ansicht** (Original-M01-Spalte historisch als Planung angelegt).

**Aktueller Stand M11:** Die folgende Haupttabelle ist der verbindliche M11-Quell-/UI-Abgleich. `implemented` bedeutet ausschließlich Backend- und TV-Codepfad, **nicht** Hardwaretest. `hardware_pending` kennzeichnet zusätzlich noch geräteabhängige Funktion/Anzeige. `not_applicable` gilt nur für im M11-Anhang konkret begründete reine Smartphone-Präsentation. `tested` erst nach nachgewiesenen Tests vergeben. Die späteren M01–M10-Abschnitte bleiben **historische Meilenstein-Snapshots**; ihre früheren `not_started`/`partial`-Angaben sind keine aktuellen Funktionszustände. OSS/Prod enthält keine Play-In-App-Käufe.

| ID | Upstream-Nutzfunktion / Originalpfad oder Original-API | Plume-TV-Anbindung | M11-Status | Konkrete Evidenz / Grenze |
| --- | --- | --- | --- | --- |
| F01 | VPN verbinden, trennen, erneut verbinden; `app/service/MullvadVpnService.kt`, `lib/repository/ConnectionProxy.kt` | VPN-Startseite | `implemented` | VpnActivity: ConnectionProxy connect/disconnect/reconnect; Tunnel auf Shield nicht belegt |
| F02 | Echtzeit-Tunnelstatus, Fehlzustände, Reconnect; `lib/grpc/ManagementService.kt`, `lib/model` | VPN-Startseite/Status | `implemented` | VpnActivity: ManagementService.tunnelState und Fehlerstate; Shield offen |
| F03 | VPN-Systemfreigabe / Rechte-Dialog; `app/MainActivity.kt`, `app/service/MullvadVpnService.kt` | VPN-Ersteinrichtung | `implemented` | VpnActivity: VpnService.prepare; OS-Zustimmung auf Shield offen |
| F04 | Hintergrundbetrieb, Foreground-Notification, System-Revoke; `app/service/MullvadVpnService.kt`, `app/service/notifications` | OS + Status | `implemented` | Originaler MullvadVpnService/NotificationManager; Home/Boot/Lockdown auf Shield offen |
| F05 | Rust-Daemon/JNI/Talpid/gRPC-/Management-IPC; `../mullvad-jni`, `../mullvad-daemon`, `../mullvad-management-interface`, `lib/talpid`, `lib/grpc` | interne Originalruntime | `implemented` | Gepinnter Rust/JNI/gRPC im integrierten M10-APK; Laufzeittunnel offen |
| F06 | Relay-Land/Stadt/Serverauswahl und Suche; `lib/feature/location/impl`, `lib/repository/RelayListRepository.kt` | Serverauswahl | `implemented` | VpnLocationUi: Original RelayListRepository, Suche, Ortswahl; Shield offen |
| F07 | Relay-Filter, Anbieter, Besitztyp; `lib/feature/filter/impl`, `lib/repository/RelayListFilterRepository.kt`, `lib/usecase/ProviderToOwnershipsUseCase.kt` | Filterdialog | `implemented` | VpnLocationUi: RelayListFilterRepository + ProviderToOwnerships; Shield offen |
| F08 | Zuletzt verwendete Standorte/Server; `lib/usecase/RecentsUseCase.kt` | Serverauswahl | `implemented` | VpnLocationUi: RecentsUseCase + Präferenz; Shield offen |
| F09 | Benutzerdefinierte Standortlisten bearbeiten/löschen; `lib/feature/customlist/impl`, `lib/repository/CustomListsRepository.kt` | Standortlisten | `implemented` | VpnLocationUi: CustomListsRepository CRUD; Shield offen |
| F10 | Multihop/Entry/Exit; `lib/feature/multihop/impl`, `lib/usecase/HopSelectionUseCase.kt` | VPN-Erweitert | `implemented` | VpnAdvancedUi + VpnLocationUi: originale Entry-/Exit-Constraints; Shield offen |
| F11 | Multihop-Migration/Assistent; `lib/feature/multihopmigration/impl`, `lib/repository/MultihopMigrationRepository.kt` | Erweitert/Migration | `implemented` | VpnAdvancedUi: MultihopMigrationRepository/Geführte Hinweise; Shield offen |
| F12 | DAITA; `lib/feature/daita/impl` | VPN-Erweitert | `implemented` | VpnAdvancedUi: SettingsRepository.setDaitaEnabled; Shield offen |
| F13 | WireGuard-Tunnel-Parameter/Constraints; `lib/feature/vpnsettings/impl`, `lib/repository/WireguardConstraintsRepository.kt` | VPN-Einstellungen | `implemented` | VpnSettingsUi: WireguardConstraintsRepository; Shield offen |
| F14 | MTU-Einstellung; `lib/feature/vpnsettings/impl` (MtuDialogViewModel) | VPN-Erweitert | `implemented` | VpnSettingsUi: originale MTU-Setter/Reset; Shield offen |
| F15 | Anti-Zensur/Tunnel-Obfuskation/Ports; `lib/feature/anticensorship/impl`, `../tunnel-obfuscation` | VPN-Erweitert | `implemented` | VpnNetworkUi: Obfuscation-Setter + dynamische Ports; Shield offen |
| F16 | API-Zugangsmethoden / Proxy-Zugang; `lib/feature/apiaccess/impl`, `lib/repository/ApiAccessRepository.kt` | Verbindungseinstellungen | `implemented` | VpnNetworkUi: ApiAccessRepository prüfen/wechseln/CRUD; Shield offen |
| F17 | Server-IP-Overrides; `lib/feature/serveripoverride/impl`, `lib/repository/RelayOverridesRepository.kt` | Verbindungseinstellungen | `implemented` | VpnNetworkUi: RelayOverridesRepository JSON-Patch/Reset; Shield offen |
| F18 | IPv6 und sonstige VPN-Einstellungen; `lib/feature/vpnsettings/impl`, `lib/repository/SettingsRepository.kt` | VPN-Einstellungen | `implemented` | VpnSettingsUi: originale IPv6/IP-Version Settings; Shield offen |
| F19 | Benutzerdefinierte DNS-Server; `lib/feature/dns/impl`, `lib/usecase/DeleteCustomDnsUseCase.kt` | DNS-Einstellungen | `implemented` | VpnSettingsUi: Custom DNS CRUD über Originalrepository; Shield offen |
| F20 | DNS-Inhaltsfilter (Werbung/Tracker/Malware etc. nach Upstream); `lib/feature/dns/impl`, `lib/feature/filter/impl` | DNS-Einstellungen | `implemented` | VpnSettingsUi: DNS-Blocker/Filter über Originalrepository; Shield offen |
| F21 | Lokales Netzwerk freigeben/LAN sharing; `lib/feature/lansharing/impl` | VPN-Einstellungen | `implemented` | VpnSettingsUi: setLocalNetworkSharing; Shield offen |
| F22 | Split-Tunneling: Apps ausschließen/einbeziehen; `lib/feature/splittunneling/impl`, `lib/repository/SplitTunnelingRepository.kt` | App-Auswahl | `implemented` | VpnSplitTunnelingUi: originale exclude/include-App-APIs; Shield offen |
| F23 | Auto-Connect/Verbindung bei Start/Boot; `lib/feature/autoconnect/impl`, `lib/repository/AutoStartAndConnectOnBootRepository.kt` | VPN-Einstellungen | `implemented` | VpnSettingsUi: AutoStartAndConnectOnBootRepository; Shield offen |
| F24 | Android Always-On/Lockdown/System-VPN-Konfiguration; `lib/feature/autoconnect/impl`, `lib/usecase/SystemVpnSettingsAvailableUseCase.kt` | Systemhinweise | `implemented` | VpnSettingsUi: Android-VPN-Systemeinstellungen, keine erfundenen Toggles; Shield offen |
| F25 | Quick-Settings-VPN-Tile; `app/tile/MullvadTileService.kt` | OS-Tile, falls verfügbar | `hardware_pending` | Original-MullvadTileService, QS-Fallback via staged ClassNames-Redirect; TV-QS-Funktion offen |
| F26 | Login mit Kontonummer/Logout; `lib/feature/login/impl`, `lib/repository/AccountRepository.kt` | Konto/Ersteinrichtung | `implemented` | VpnAccountUi: AccountRepository login/logout; Shield offen |
| F27 | Kontodaten, Restlaufzeit, Verlängerung; `lib/feature/account/impl`, `lib/repository/AccountRepository.kt` | Konto | `implemented` | VpnAccountUi: accountData Ablauf, Mullvad-Webkonto; Shield offen |
| F28 | Zeit hinzufügen / Bezahl-Weiterleitung im OSS-Build; `lib/feature/addtime/impl`, `lib/payment` | Konto/Zeit hinzufügen | `implemented` | VpnAccountUi: Original-OSS-Weblink, Play Billing nicht im OSS; Shield offen |
| F29 | Voucher einlösen; `lib/feature/redeemvoucher/impl`, `lib/repository/VoucherRepository.kt` | Konto/Gutschein | `implemented` | VpnAccountUi: VoucherRepository.submitVoucher; Shield offen |
| F30 | Geräte anzeigen/verwalten; `lib/feature/managedevices/impl`, `lib/repository/DeviceRepository.kt` | Konto/Geräte | `implemented` | VpnAccountUi: DeviceRepository list/remove; Shield offen |
| F31 | Gerätelimit/neue Geräte; `lib/feature/login/impl`, `lib/repository/NewDeviceRepository.kt` | Ersteinrichtung/Geräte | `implemented` | VpnAccountUi: NewDeviceRepository/Gerätelimit; Shield offen |
| F32 | Account löschen; `lib/feature/deleteaccount/impl` | Konto/Löschen | `implemented` | VpnAccountUi: AccountRepository.deleteAccount mit Bestätigung; Shield offen |
| F33 | Konto abgelaufen / keine Zeit / gesperrtes Gerät; `lib/feature/home/impl`, `lib/usecase/OutOfTimeUseCase.kt` | VPN-Status/Konto | `implemented` | VpnAccountUi + VpnDiagnosticsUi: Revoked/Expired/LoggedOut, echte accountData; Shield offen |
| F34 | Problembericht/Kontakt Support; `lib/feature/problemreport/impl`, `lib/repository/ProblemReportRepository.kt` | Diagnose/Support | `implemented` | VpnDiagnosticsUi: ProblemReportRepository.collectLogs/sendReport, explizite Bestätigung; CI/Shield offen |
| F35 | Protokolle ansehen/teilen; `lib/feature/problemreport/impl` (ViewLogsViewModel) | Diagnose/Logs | `implemented` | VpnDiagnosticsUi: ProblemReportRepository.readLogs, Android-SAF Export; CI/Shield offen |
| F36 | Verbindungs-/Ablauf-Benachrichtigungen; `lib/feature/notification/impl`, `lib/push-notification` | VPN-Status/OS | `implemented` | Original NotificationManager + InAppNotificationController in Diagnose; Shield offen |
| F37 | Benachrichtigungseinstellungen / Accountwarnungen; `lib/feature/notification/impl`, `lib/usecase/AccountExpiryNotificationActionUseCase.kt` | VPN-Einstellungen | `implemented` | VpnDiagnosticsUi: UserPreferencesRepository.setLocationInNotificationEnabled, OS-Settings; Shield offen |
| F38 | Allgemeine Einstellungen / persistente Präferenzen; `lib/feature/settings/impl`, `lib/repository/UserPreferencesRepository.kt` | VPN-Einstellungen | `implemented` | VpnSettingsUi + VpnDiagnosticsUi: Original-Settings und UserPreferences; Shield offen |
| F39 | Sprache/Locale; `lib/feature/language/impl`, `lib/repository/LocaleRepository.kt` | VPN-Einstellungen | `hardware_pending` | VpnDiagnosticsUi: Upstream LanguageRepository auf API 33+, gesamte APK; Android-TV API 28-32 Systemsprache |
| F40 | Erscheinungsbild/Theme; `lib/feature/appearance/impl` | Plume-Design/optionale Präferenz | `not_applicable` | Eine einzige Plume-APK: Plume-Theme steuert native VPN-Views; separate Mullvad-Compose-Theme ohne VPN-Wirkung |
| F41 | App-Icon / Tarnungsicon; `lib/feature/appicon/impl` | Funktionsprüfung / ggf. TV-Äquivalent | `not_applicable` | Sechs Smartphone-Aktivitäts-Aliase für Tarnungsicons werden nur im Plume-Manifest entfernt; kein Tunnelmerkmal |
| F42 | App-Info, Version, Changelog; `lib/feature/appinfo/impl`, `lib/repository/ChangelogRepository.kt` | Über VPN | `implemented` | VpnDiagnosticsUi: AppVersionInfoRepository + ChangelogRepository inkl. Dismiss; Shield offen |
| F43 | Aktuelle Standort-/Exit-IP-Anzeige; `lib/feature/home/impl`, `lib/usecase/LastKnownLocationUseCase.kt` | VPN-Startseite | `implemented` | VpnActivity: TunnelState.location() zeigt echte Exit-IP/Host, kein IP-Lookup; Shield offen |
| F44 | App-Listen/System-App-Erkennung; `lib/feature/applisting/impl` | Split-Tunneling | `implemented` | VpnSplitTunnelingUi: ApplicationsProvider incl. System-/nicht installierte Apps; Shield offen |
| F45 | Upstream-Updatehinweise inkl. Android-16-Hinweis; `lib/usecase/inappnotification`, `app/src/main/AndroidManifest.xml` | VPN-Hinweise | `implemented` | VpnDiagnosticsUi: originale InAppNotificationController inkl. Android-16-Upgrade und NewChangelog; Shield offen |
| F46 | Netzwerkverfügbarkeit/Verbindungsweg; `lib/usecase/InternetAvailableUseCase.kt`, `lib/usecase/ConnectionPathUseCase.kt` | VPN-Status | `implemented` | VpnActivity + VpnAdvancedUi: gRPC Connectivity/Entry-/Exit-Werte; Geokarten-UI entfällt |
| F47 | Relay-Assets/Übersetzungen/Sortierung; `../dist-assets/relays/relays.json`, `lib/repository/RelayLocationTranslationRepository.kt` | Standortauswahl | `implemented` | VpnLocationUi: gepinnte Relay-Assets und Übersetzung im Originaldaemon; Shield offen |
| F48 | Navigationszustand/Erststart/Welcome; `lib/feature/home/impl`, `lib/feature/login/impl`, `lib/navigation` | eigenständige TV-Navigation | `hardware_pending` | VpnActivity: native 8-seitige TV-Navigation, Classic/Overview Einstieg; DPAD/Back/Restore Shield offen |

## Weitere Inventarregeln

- Obige 48 **prüfbaren Funktionsgruppen** umfassen die identifizierten Android-`feature/*`-Module sowie DI, Notification, Service, App-Info, Boot, OS-Integration, Rust/JNI/gRPC. Untermenüs/Flags innerhalb der Gruppen in M03–M12 am **gepinten Original** gegen UI-Matrix abgleichen und separat ergänzen, falls im Detail abweichend; nicht aus aktuellem Mullvad-`main` übernehmen.
- Reine Smartphone-Darstellung (Map/Navigation, Compose-Hilfsbibliotheken, Animationen) wird **funktional** durch TV-Views ersetzt. Funktionen wie Android-TV-Quick-Tile oder App-Icon-Tarnung können OS-bedingt fehlen; in dem Fall `unsupported_on_device` mit Gerätebeleg statt `pass` dokumentieren.
- `play`-Billing ist im gewählten **ossProd**-Flavour nicht verfügbar. Keine Play-Abrechnung oder andere nicht vorhandenen Flavour-Funktionen als erforderliche OSS-Parität ausgeben. Zahlung nur über im OSS-Original vorhandenen Pfad.
- Keine Statusauswertung aus Build allein: `pass` verlangt Backend + nutzbare D-Pad-TV-UI bzw. explizite OS-Funktion + reale Testevidenz.


## M02 — Funktionsstatus bleibt konservativ

Der **Codeeinbindungspfad** für F05 (Rust/JNI/gRPC/Tunnelverwaltung), F04 (VpnService), F47 (Relay-Assets) und F48 (Plume-HOME-Einstieg) wurde im Staging beschrieben und als Build-Generator implementiert. Dieser Absatz beschrieb ursprünglich das noch blockierte M02-Build-Gate. **Aktuell bestanden:** [M02 CI Run 43314187927](https://github.com/Justify87/Plume_Launcher/actions/runs/43314187927) und [M03 CI Run 43323559922](https://github.com/Justify87/Plume_Launcher/actions/runs/43323559922) bestätigen ARM64-APK, Manifest, Rust/JNI und Relay-Assets. Ein echter Tunnel und D-Pad-Gerätetests sind **nicht** nachgewiesen. Die M01-Inventarzeilen bleiben historische `not_started`-Baselines; die M03-Codeanbindungen werden unten als `partial` geführt.

## M02 build workflow change (12 November 2026)

A shared manual/PR integrated APK build now uses the official pinned Mullvad Android toolchain image. The integrated APK is now packaged and CI-validated (M02 Run 43314187927; M03 Run 43323559922), but this does **not** prove functional VPN parity. The original M01 inventory statuses are historical; M03 code hookups below are `partial`, and runtime/TV/Shield tests remain `not_run`. Do not mark features as `pass` because source modules compile.

## M03 — implementierte Anbindung, noch keine vollständige Funktionsabnahme

| Funktionsgruppe | M03-Codeanbindung | Evidenzstatus |
| --- | --- | --- |
| F01, F02 | `ConnectionProxy.connect/disconnect/reconnect`, `ManagementService.tunnelState` im Debug-Harness | `partial`: Code, kein Laufzeittest |
| F03 | `VpnService.prepare()` und Activity-Result inkl. Ablehnung, Mullvad-Prüfung in `ConnectionProxy.connect` | `partial`: kein OS-Dialog auf Shield getestet |
| F04, F05 | Original-`MullvadVpnService`, `MullvadApplication`, Rust/JNI/gRPC aus M02 unverändert | `partial`: M02 APK-Struktur geprüft, echter Tunnel nicht getestet |
| F26 | `AccountRepository.login(AccountNumber)` mit manueller Eingabe | `partial`: keine gültige Kontoeingabe getestet |
| F48 | Native Android-View-Activity, sichtbarer VPN-Knopf in Classic und Overview (Debug-Variante) | `partial`: D-Pad und Fokus nicht am TV getestet |

**Wichtig:** Die bestehenden Zeilen F01–F48 bleiben bis zur echten Geräte- und UI-Prüfung unverändert konservativ. M03 implementiert einen Testzugang, **keine** produktive Feature-Parität.

## M04-Statuspräzisierung (2026-11-13)

- **F01–F03 `partial`:** Produktive TV-Statusanzeige mit Original-`ConnectionProxy`, `ManagementService` und `VpnService.prepare` als Quellimplementierung; CI-/Shield-Tunnel noch offen, **nicht `pass`**.
- **F04/F05 `partial`:** unabhängiger Original-`MullvadVpnService` mit originalem Rust/JNI/gRPC strukturell im Vorgänger-CI geprüft; Prozess- und Tunnel-Laufzeit nach Home/Back auf Shield **nicht** geprüft.
- **F26 `partial`:** bestehende Kontonummer-Anmeldung über originales `AccountRepository` in eigener Konto-Ansicht; Gerätelogin/Anmeldefehler noch ohne Endgerät-Prüfung.
- **F06–F25/F27–F48** soweit nicht separat dokumentiert bleiben `not_started`; `Standorte`, `VPN-Einstellungen` und große Teile der `Diagnose` sind ausdrücklich Navigations-Shells. Kompilieren und UI-Shell allein begründen keine vollständige Parität.


## M05 — Konto-/Geräte-Feature-Matrix (Codeanbindung ≠ Hardware-Pass)

| ID | Originalpfad / reale Plume-UI | Stand und offener Nachweis |
| --- | --- | --- |
| F26 | `AccountRepository.login/logout/createAccount`, `LoginAccountError` → native Konto-Seite mit Android-TV-EditText und Bestätigungen | `partial`: Quellimplementierung; echter Login/Logout und Keyboard auf Shield `not_run` |
| F27 | `AccountRepository.accountData/refreshAccountData` → Kontoübersicht und Ablauf in Ortszeit | `partial`: Live-Flow angebunden, echter Ablauf-/Neustarttest `not_run` |
| F28 | `AccountRepository.getWebsiteAuthToken` + originaler `createAccountUri` → offizieller Mullvad-Kontolink | `partial`: OSS ohne Play-Billing; TV-Browser/Weiterleitung `not_run` |
| F29 | `VoucherCode.fromString` / `VoucherRepository.submitVoucher` → Gutscheinformular | `partial`: realer Gutschein/Fehlermeldung `not_run` |
| F30 | `DeviceRepository.deviceList/removeDevice` → Liste, eigenes Gerät markiert, Entfernung mit Bestätigung | `partial`: Login-, Remove- und Gerätewiderruf-Test `not_run`; Umbenennung **nicht im gepinnten Android-Repository verfügbar**, daher `not_applicable_upstream_api` |
| F31 | `LoginAccountError.MaxDevicesReached` / `NewDeviceRepository.newDeviceCreated` → Limit-Geräteliste, Entfernen und Wiederanmeldung | `partial`: Gerätelimit-Szenario `not_run` |
| F32 | `AccountRepository.deleteAccount` → zweite Kontonummernbestätigung und Backend-Resultat | `partial`: echter Löschtest ohne freigegebenes Testkonto `not_run` |
| F33 | `AccountRepository.accountData.expiryDate`, `DeviceState.Revoked/LoggedOut` → abgelaufen/widerrufen angezeigt | `partial`: Ablauf offline/live `not_run`; keine synthetischen Zustände |

**Abnahme:** Keine Zeile `pass` ohne Originalfunktion **und** benutzbare TV-UI **und** tatsächliche Testevidenz. Original-Smartphone-Compose-UI wird nicht als Plume-Kontooberfläche geöffnet. Ein getrennt installiertes Mullvad-Paket gibt keine privaten Accounts an `dev.plume.advanced` weiter.


**M05 Bootstrap-Review:** `VoucherRepository` und `NewDeviceRepository` sind im originalen `uiModule`, nicht im bei Application-Start geladenen `appModule`; die Plume-`VpnActivity` lädt daher wie Mullvads MainActivity `uiModule` und `paymentModule`. Ohne diesen Schritt wären F29/F31 auf Gerät nicht auflösbar. Staging-Assertion ergänzt; Gerätepass bleibt offen.

## M06 — Standort-/Listen-Feature-Matrix (Quellanbindung ohne Shield-Abnahme)

| ID | TV-Implementierung und originaler Quellpfad | Status |
| --- | --- | --- |
| F06 | `VpnLocationUi`: Länder → Städte → Server, originale `RelayListRepository.relayList`, `FilteredRelayListUseCase(Single)`, `newFilterOnSearch`, `SelectSinglehopUseCase`; Auswahlzustand original `selectedLocation` | `partial`: UI und Repositories angebunden; Live-Tunnel/Shield/Fokus `not_run` |
| F07 | Eigentümer `MullvadOwned`/`Rented`/`Any`, original Provider-Liste (`ProviderToOwnershipsUseCase`), `RelayListFilterRepository.updateSelectedOwnership/updateSelectedProviders` für `EXIT` | `partial`: echte API-Anbindung; Laufzeittest `not_run` |
| F08 | `RecentsUseCase(RelayListType.Single)`, `SettingsRepository.setRecentsEnabled` und `RecentItem.Relay` | `partial`: keine eigene Recents-Datenbank, Shield `not_run` |
| F09 | `CustomListsRepository.createCustomList/updateCustomListName/updateCustomListLocations/deleteCustomList`, `CustomListsRelayItemUseCase`, Singlehop-Selektion einer Liste | `partial`: API + TV-Dialoge; CRUD auf echtem Gerät `not_run` |
| F47 | Original-Daemon-Relaystream `ManagementService.relayCountries` über Übersetzung/Sortierung des `RelayListRepository`; kein separater Launcher-Download | `partial`: Integrationspfad; APK/Endgerät `not_run` |
| M06-Gap | Original `ManagementService.setRelayLocation(RelayItemId)` schreibt nur `Constraint.Only`. Initial-`Constraint.Any` wird angezeigt, ein Singlehop-Exit-Reset ist nicht über öffentliches Upstream-Repository verfügbar; Multihop-Entry-Any erst M09 | `partial` für expliziten Auto-Reset, **nicht** als geliefert darstellen |

Alle `partial`-Angaben bedeuten Source-/UI-Einbindung und **nicht** erfüllte Endgeräte-Feature-Parität. Keine Pingwerte, keinen künstlichen Standort und keinen manuellen VPN-Reconnect eingeführt.

## M07 — Original-Einstellungswerte in nativer TV-UI (ohne Shield-Funktionspass)

| Gruppe | Reale M07-Anbindung | Evidenzstatus |
| --- | --- | --- |
| F13 | `WireguardConstraintsRepository.wireguardConstraints` + `setDeviceIpVersion`; WireGuard-Obfuskationsport ausdrücklich M10 | `partial`: Quellcode, Runtime-Test `not_run` |
| F14 | `SettingsRepository.setWireguardMtu/resetWireguardMtu` und `Mtu.fromString` (1280–1420) | `partial`: Quellcode, Runtime-Test `not_run` |
| F18 | IPv6 `settingsUpdates.tunnelOptions.enableIpv6`, `setIpv6Enabled`; einfache Quantumresistenz `setWireguardQuantumResistant` | `partial` |
| F19 | `DnsOptions.Custom` / `SettingsRepository.setCustomDns/addCustomDns/setDnsState` + `DeleteCustomDnsUseCase`, Original-`InetAddressValidator` und Duplikatprüfung | `partial` |
| F20 | Sechs `DefaultDnsOptions`-Blocker über `updateContentBlockers`, Original-DNS-Modus live | `partial` |
| F21 | `settingsUpdates.allowLan` / `setLocalNetworkSharing` | `partial` |
| F23 | `AutoStartAndConnectOnBootRepository.autoStartAndConnectOnBoot` / `setAutoStartAndConnectOnBoot` + originaler Receiver | `partial` |
| F24 | `SystemVpnSettingsAvailableUseCase`, `android.net.vpn.SETTINGS`; echter Android-Always-on-/Lockdown-Wert nur in OS-UI | `partial` (OS-Unterseite, keine vorgetäuschte App-API) |

M07 liefert keine Anti-Zensur-`setCustomWireguardPort`-Funktion (M10), keine Split-Tunneling-App-Auswahl (M08), keine neue VPN-Engine. `pass` erfordert kompiliertes APK, D-Pad/UI und realen Gerätespeicher-/Tunnel-Nachweis gemäß `TEST_MATRIX.md`.


## M08 — Split-Tunneling / App-Listing

| Gruppe | Original-Anbindung und Plume-TV-UI | Status |
| --- | --- | --- |
| F22 | `SplitTunnelingRepository.splitTunnelingEnabled/excludedApps`, `enableSplitTunneling/excludeApp/includeApp` über den originalen Daemon; 6. Rail-Tab, D-Pad/OK/Back und sichtbare aktive/inaktive Regeln | `partial`: Quelle/UI umgesetzt; Routing/Persistenz auf Shield `hardware_pending` |
| F22 App-Liste | `ApplicationsProvider.apps()`, `QUERY_ALL_PACKAGES` des Host-Manifests, Label/Paket-Suche, Icon, virtualisierte ListView, sortierte Ausschlüsse, Systemapp-Originalpräferenz | `partial`: Fokus/Großliste/Keyboard auf Shield `hardware_pending` |
| F22 Paketwechsel | onResume + Android-Paketbroadcasts bei offener Activity, fehlende gespeicherte Ausnahmen über Original-Repository entfernbar | `partial`: Install-/Uninstall-Test `hardware_pending` |
| F24 Sperrmodus | ehrlicher Warntext: Android-Lockdown kann Ausnahmen blockieren; kein unbewiesener Routing-/Sperrmodusstatus | `partial`: Lockdown-Netztest `hardware_pending` |

Original `MigrateSplitTunneling` nicht verändert; die Device-Abnahme aus M01–M07 bleibt separat offen.

## M09 — fortgeschrittene Tunnelmodi, aktueller Implementierungsstand

Die ursprüngliche Inventartabelle oben beschreibt weiterhin die historische M01-Baseline. Für diese M09-Ausbaustufe gelten nachfolgende **aktuelle** Zustände; `partial` heißt Quellanbindung und TV-UI vorhanden, aber ohne abgeschlossene reale Shield-/Tunnelabnahme.

| ID | Original-API am Pin `aa47f606...` | Plume-TV-Integration | M09-Status |
| --- | --- | --- | --- |
| F10 Multihop/Entry/Exit | `WireguardConstraintsRepository.setMultihop`, `ModifyMultihopUseCase`/`MultihopChange`, `HopSelectionUseCase`, `FilteredRelayListUseCase(RelayListType.Multihop)`, `RelayListFilterRepository` | „Erweitert“: Nie/Bei Bedarf/Immer; M06-Browser als Entry/Exit, automatische Entry-Wahl, getrennte Originalfilter; aktiver Hostname nur aus Tunnel | `partial`, `hardware_pending` |
| F11 Migration | `MultihopMigrationRepository.multihopMigrationState/clearMultihopMigrationState`, `UserPreferencesRepository.setHasSeenMultihopMigrationGuide` | Migrierte Einstellung anzeigen, geprüftes Originalereignis bestätigen; keine eigene Migration | `partial`, `hardware_pending` |
| F12 DAITA | `SettingsRepository.setDaitaEnabled`, `Settings.tunnelOptions.daitaSettings.enabled`; `FilteredRelayListUseCase` | Aktivieren/Deaktivieren, Original-StateFlow, DAITA-abhängige Relayliste | `partial`, `hardware_pending` |
| F13 Post-Quantum | `SettingsRepository.setWireguardQuantumResistant`, `QuantumResistantState.On/Off`, `settingsUpdates` | M07-Schalter bleibt der einzige Writer; M09 zeigt Backendstatus und springt dorthin | `partial`, `hardware_pending` |
| F46 Verbindungspfad | `TunnelState.Connected.location.entryHostname/hostname`, `HopSelectionUseCase`; `ConnectionPathUseCase` ist geo-/kartenorientiert | getrennte Anzeige von konfigurierten Hops und tatsächlich bestätigter Entry-/Exit-Verbindung | `partial`, `hardware_pending` |

**Keine Parität durch statische Ansicht allein:** `pass` erst nach GitHub-Kotlin/Rust/JNI-/APK-Build **und** realem TV-/VPN-Test. `WHEN_NEEDED` und DAITA können Entry-Verfügbarkeit einschränken; von Mullvad gelieferte Optionen nicht mit Plume-eigenen Regeln überschreiben. Nur zwei PQ-Zustände; keine zusätzliche Sicherheitseinstellung im Release.

## M10 — Anti-Zensur, API-Zugang und Server-IP, tatsächlicher Ausbaustand

Die historische Inventartabelle bleibt unverändert; diese Fortschreibung ist für F15–F17 maßgeblich. `partial` heißt Original-Repositories plus native TV-View sind angebunden; **kein** Geräte-`pass`.

| ID | Gepinnte Originalschnittstelle | TV-Umsetzung / Originalgrenze | Status |
| --- | --- | --- | --- |
| F15 Obfuskation | `ObfuscationMode.Auto/Off/Udp2Tcp/Shadowsocks/Quic/Lwo/WireguardPort`, `SettingsRepository.setObfuscation`, `settingsUpdates` | Netzwerk → Verschleierung, sieben Modi, echter Status inkl. DAITA/Multihop | `partial`; Shield/Tunnel offen |
| F15 Ports | `setCustomWireguardPort` (Obfuskationsport!), `setCustomUdp2TcpObfuscationPort`, `setCustomShadowsocksObfuscationPort`, `setCustomLwoObfuscationPort`; `RelayListRepository.portRanges` | Automatisch/Original-Presets, eigener zulässiger Port, backendbestätigte Werte; QUIC ohne Portoption | `partial`; CI/Gerät offen |
| F16 API-Methoden | `ApiAccessRepository.accessMethods/currentAccessMethod`, `testApiAccessMethodById`, `setCurrentApiAccessMethod`, `setEnabledApiAccessMethod` | Liste mit aktivem Status, Test/Wechsel, Aktivierung/Deaktivierung, API-Fehler | `partial`; API-Erreichbarkeit offen |
| F16 Eigene Zugänge | `addApiAccessMethod`, `updateApiAccessMethod`, `removeApiAccessMethod`, `testCustomApiAccessMethod`, `getShadowsocksCiphers` | SOCKS5-Remote/optional Auth und Shadowsocks, Textfelder, IP-/Port-/Cipher-Validierung, Löschbestätigung | `partial`; echte Proxyprüfung offen |
| F17 Relay-Overrides | `RelayOverridesRepository.relayOverrides/applySettingsPatch/clearAllOverrides` | bestätigte Einträge, JSON-Text oder Dokument importieren und global zurücksetzen | `partial`; gültige/ungültige Imports offen |
| F17 Einzelhost-Editor | Gepinnte Android-Originaloberfläche bietet Import/Reset, aber keinen direkten Einzelhost-Setter im Repository | Keine erfundene Einzel-IP-CRUD-UI; Änderungen nur durch Original-JSON-Patch | `not_applicable` als eigenständige Aktion |
| F25 Quick Settings / Tarnungs-Icon | Android-OS-/Launcher-abhängig, nicht Teil der F15–F17-Original-Netzwerk-Repositories | Keine vorgetäuschten TV-Systemaktionen | `not_started`, M11 Geräteprüfung |

**Weiter offen für M11:** F01–F24, F26–F33 und F43+ soweit laut vorigen Meilensteinen `partial`; F25, F34–F42 einschließlich Benachrichtigungen, Diagnose/Problembericht, App-Info, Locale, TV-Quick-Tile/-App-Icon-Äquivalent. Aktuelle Hardware-/CI-Lücken stehen in `TEST_MATRIX.md`.

## M11 — Funktionsabgleich und Scope-Entscheidungen (13.11.2026)

- **F34/F35:** `VpnDiagnosticsUi` nutzt genau `ProblemReportRepository.collectLogs/readLogs/sendReport/deleteLogs`; Vorschau ist auf die letzten 160 Zeilen begrenzt, Export schreibt alle Originalzeilen ausschließlich nach ausdrücklicher Wahl eines Android-SAF-Zieles. Kein eigener Logger, Telemetriesystem oder Hintergrundsammler. Absenden mit optionaler E-Mail und separater Einwilligung zur Konto-ID; leerer Beschreibungstext nicht sendbar. Offline-/Sammelfehler sichtbar. Auf Shield noch ungetestet.
- **F36/F37/F45:** Systemtunnel-/Ablaufbenachrichtigungen bleiben originale `NotificationManager`/`AccountExpiryNotificationActionUseCase`; In-App-Meldungen stammen direkt aus `InAppNotificationController.notifications` (Tunnel-/Offline-Fehler, Kontoablauf, neues Gerät, Multihop, Version, Android-16-Hinweis). Der TV-Toggle schreibt ausschließlich `UserPreferencesRepository.setLocationInNotificationEnabled`. `NotificationManagerCompat.areNotificationsEnabled()` ist nur eine OS-Abfrage, kein selbst gebauter Push-Service. Die originale `MAIN_ACTIVITY_CLASS` wird **nur in der generierten Host-Kopie** auf `VpnActivity` umgebogen, damit Tile und Original-PendingIntents keine Smartphone-Compose-Seite öffnen; Quick-Settings-Eignung der Shield bleibt `hardware_pending`.
- **F38/F39/F42:** Version/Support über `AppVersionInfoRepository.versionInfo`, Changelog inkl. Gelesen-Markierung über `ChangelogRepository`, Sprache via vorhandener `LanguageRepository` nur auf Android >= 13 und **für die gemeinsame APK**; auf Android <= 12 folgt die gesamte APK der OS-Sprache. Die übrigen VPN-Präferenzen liegen in `VpnSettingsUi`/`VpnAdvancedUi` und `UserPreferencesRepository`. Die Sprachliste darf nicht so etikettiert werden, als wäre die VPN-UI eine zweite App.
- **F40 – not_applicable:** Mullvads Compose-Farb-/Darstellungspräferenz ist reine Phone-UI-Präsentation. Die nativen VPN-Views folgen bereits Plumes gewähltem Design (Classic/Overview). Kein Verlust einer VPN-Tunnel- oder Account-Funktion.
- **F41 – not_applicable:** Smartphone-App-Icon-Umbenennung per `MainActivityAlt*`-Alias ist in der gemeinsamen Launcher-APK irreführend (würde den HOME-Launcher tarnen oder doppelte App-Icons erzeugen). Im Staging-Manifest werden diese sechs ursprünglichen Aliase entfernt, **keine** Tunnel-/VPN-Funktion entfällt.
- **F28 / Store:** OSS/Prod nutzt Mullvads reguläre Konto-/Bezahl-Webseite in `VpnAccountUi`; Google-Play-Billing ist kein Bestandteil des gepinnten OSS/Prod-Flavours. Keine leere TV-Kauffunktion.
- **F46:** Originaler Connection-Path-UseCase für die Smartphone-Kartenvisualisierung ist keine separate Tunnel-Einstellfunktion. `VpnActivity`/`VpnAdvancedUi` zeigen bestätigte Entry-/Exit-Werte und gRPC-/Offline-Zustände als Text, keine ungeprüften Kartendaten.
- **M11 Evidenzstand:** Quell-APIs am Tag `android/2026.12` / `aa47f606b1a159214c7279344128119fa91be5fd` geprüft. [CI-Run 43431639278](https://github.com/Justify87/Plume_Launcher/actions/runs/43431639278) an M11-Codecommit `48eee9561ff19338b61e066014e9d632b5f268a5`: `check=success`, `mullvad-apk=success`, 839 APK-Einträge mit Rust/JNI/Relay-Assets. Alle **48** Inventarpositionen sind einzeln klassifiziert. Kein Shield-D-Pad-, Live-VPN-, QS-Tile- oder Mail-Sende-Pass. Reine App-Store-Listing-Aktionen aus Mullvads Smartphone-App sind bei der neuen gemeinsamen `dev.plume.advanced`-Launcher-APK ohne Play-Store-Eintrag **nicht anwendbar**; Kontoverlängerung erfolgt via regulärem Mullvad-Weblink (F28).

## M12 — gepinntes Gesamtinventar und Grenzen (13.11.2026)

- Die verbindliche M11-Tabelle F01–F48 wurde erneut als **48 eindeutige** Gruppen auf Pin `aa47f606b1a159214c7279344128119fa91be5fd` gezählt und mit TV-Quellankern gesichtet; kein neues bestätigtes Original-Feature erfunden.
- `implemented` heißt vorhandene Backend-/View-Bindung, **nicht** Endgerät-Parität. Alle entsprechenden Funktionsgruppen bleiben für Shield/VPN-D-Pad **partial/hardware_pending**, kein unverdientes `tested`/`pass` durch CI.
- Echte Hardwarepflicht: F01–F05 Tunnel/VpnService; F06–F12 Relays/Multihop/DAITA; F13–F24 DNS/LAN/Split/Boot; F25 Tile; F26–F33 Konto; F34–F39 Diagnose/Locale; F42–F48 Status/Navigationspfade.
- F40/F41 bleiben `not_applicable` für Smartphone-Compose-Theme/Tarnungsicon, F25/F39 nur mit Gerätetest `unsupported_on_device`; `ossProd` ohne Play-Billing. Original-M06-Singlehop-Any-Reset weiter nicht exponiert.
- M12-APK-Prüfungen messen Binär-/Build-Eigenschaften, **keine** VPN-Tunnelwirkung, App-Routing oder D-Pad-Erreichbarkeit. Geräteprotokoll siehe `TEST_MATRIX.md`.

