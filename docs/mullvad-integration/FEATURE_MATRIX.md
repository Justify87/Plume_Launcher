# Feature-Matrix — Inventar des gepinnten Mullvad Android 2026.12

Quelle für **alle** Zeilen: `third_party/mullvadvpn-app/` bei SHA `aa47f606b1a159214c7279344128119fa91be5fd`. Pfade in Tabelle relativ zu `android/`. `lib/feature/<name>/impl` meint die originale Featureimplementierung; `lib/repository`, `lib/usecase` und `app` sind kein neu zu erfindender Stack. `TV` benennt die **geplante**, nicht vorhandene Plume-Ansicht.

**Status für jede Funktion in M01: `not_started`**. Repo-Pfadinspektion belegt den vorhandenen Codebereich, **nicht** Gerätefunktion oder UI-Feature-Parität. Spätere Meilensteine setzen nach Implementierung und Test `partial` bzw. `pass` und hinterlegen konkrete Evidenz in `TEST_MATRIX.md`. Endpunkt-Funktionen je gewähltem OSS/Prod-Flavour prüfen; Play-in-App-Abrechnung ist kein Bestandteil von OSS/Prod.

| ID | Upstream-Nutzfunktion / Originalpfad oder Original-API | Vorgesehene Plume-TV-Anbindung | Status |
| --- | --- | --- | --- |
| F01 | VPN verbinden, trennen, erneut verbinden; `app/service/MullvadVpnService.kt`, `lib/repository/ConnectionProxy.kt` | VPN-Startseite | partial |
| F02 | Echtzeit-Tunnelstatus, Fehlzustände, Reconnect; `lib/grpc/ManagementService.kt`, `lib/model` | VPN-Startseite/Status | partial |
| F03 | VPN-Systemfreigabe / Rechte-Dialog; `app/MainActivity.kt`, `app/service/MullvadVpnService.kt` | VPN-Ersteinrichtung | partial |
| F04 | Hintergrundbetrieb, Foreground-Notification, System-Revoke; `app/service/MullvadVpnService.kt`, `app/service/notifications` | OS + Status | partial |
| F05 | Rust-Daemon/JNI/Talpid/gRPC-/Management-IPC; `../mullvad-jni`, `../mullvad-daemon`, `../mullvad-management-interface`, `lib/talpid`, `lib/grpc` | interne Originalruntime | partial |
| F06 | Relay-Land/Stadt/Serverauswahl und Suche; `lib/feature/location/impl`, `lib/repository/RelayListRepository.kt` | Serverauswahl | partial |
| F07 | Relay-Filter, Anbieter, Besitztyp; `lib/feature/filter/impl`, `lib/repository/RelayListFilterRepository.kt`, `lib/usecase/ProviderToOwnershipsUseCase.kt` | Filterdialog | not_started |
| F08 | Zuletzt verwendete Standorte/Server; `lib/usecase/RecentsUseCase.kt` | Serverauswahl | not_started |
| F09 | Benutzerdefinierte Standortlisten bearbeiten/löschen; `lib/feature/customlist/impl`, `lib/repository/CustomListsRepository.kt` | Standortlisten | not_started |
| F10 | Multihop/Entry/Exit; `lib/feature/multihop/impl`, `lib/usecase/HopSelectionUseCase.kt` | VPN-Erweitert | not_started |
| F11 | Multihop-Migration/Assistent; `lib/feature/multihopmigration/impl`, `lib/repository/MultihopMigrationRepository.kt` | Erweitert/Migration | not_started |
| F12 | DAITA; `lib/feature/daita/impl` | VPN-Erweitert | not_started |
| F13 | WireGuard-Tunnel-Parameter/Constraints; `lib/feature/vpnsettings/impl`, `lib/repository/WireguardConstraintsRepository.kt` | VPN-Einstellungen | not_started |
| F14 | MTU-Einstellung; `lib/feature/vpnsettings/impl` (MtuDialogViewModel) | VPN-Erweitert | not_started |
| F15 | Anti-Zensur/Tunnel-Obfuskation/Ports; `lib/feature/anticensorship/impl`, `../tunnel-obfuscation` | VPN-Erweitert | not_started |
| F16 | API-Zugangsmethoden / Proxy-Zugang; `lib/feature/apiaccess/impl`, `lib/repository/ApiAccessRepository.kt` | Verbindungseinstellungen | not_started |
| F17 | Server-IP-Overrides; `lib/feature/serveripoverride/impl`, `lib/repository/RelayOverridesRepository.kt` | Verbindungseinstellungen | not_started |
| F18 | IPv6 und sonstige VPN-Einstellungen; `lib/feature/vpnsettings/impl`, `lib/repository/SettingsRepository.kt` | VPN-Einstellungen | not_started |
| F19 | Benutzerdefinierte DNS-Server; `lib/feature/dns/impl`, `lib/usecase/DeleteCustomDnsUseCase.kt` | DNS-Einstellungen | not_started |
| F20 | DNS-Inhaltsfilter (Werbung/Tracker/Malware etc. nach Upstream); `lib/feature/dns/impl`, `lib/feature/filter/impl` | DNS-Einstellungen | not_started |
| F21 | Lokales Netzwerk freigeben/LAN sharing; `lib/feature/lansharing/impl` | VPN-Einstellungen | not_started |
| F22 | Split-Tunneling: Apps ausschließen/einbeziehen; `lib/feature/splittunneling/impl`, `lib/repository/SplitTunnelingRepository.kt` | App-Auswahl | not_started |
| F23 | Auto-Connect/Verbindung bei Start/Boot; `lib/feature/autoconnect/impl`, `lib/repository/AutoStartAndConnectOnBootRepository.kt` | VPN-Einstellungen | not_started |
| F24 | Android Always-On/Lockdown/System-VPN-Konfiguration; `lib/feature/autoconnect/impl`, `lib/usecase/SystemVpnSettingsAvailableUseCase.kt` | Systemhinweise | not_started |
| F25 | Quick-Settings-VPN-Tile; `app/tile/MullvadTileService.kt` | OS-Tile, falls verfügbar | not_started |
| F26 | Login mit Kontonummer/Logout; `lib/feature/login/impl`, `lib/repository/AccountRepository.kt` | Konto/Ersteinrichtung | not_started |
| F27 | Kontodaten, Restlaufzeit, Verlängerung; `lib/feature/account/impl`, `lib/repository/AccountRepository.kt` | Konto | not_started |
| F28 | Zeit hinzufügen / Bezahl-Weiterleitung im OSS-Build; `lib/feature/addtime/impl`, `lib/payment` | Konto/Zeit hinzufügen | not_started |
| F29 | Voucher einlösen; `lib/feature/redeemvoucher/impl`, `lib/repository/VoucherRepository.kt` | Konto/Gutschein | not_started |
| F30 | Geräte anzeigen/verwalten; `lib/feature/managedevices/impl`, `lib/repository/DeviceRepository.kt` | Konto/Geräte | not_started |
| F31 | Gerätelimit/neue Geräte; `lib/feature/login/impl`, `lib/repository/NewDeviceRepository.kt` | Ersteinrichtung/Geräte | not_started |
| F32 | Account löschen; `lib/feature/deleteaccount/impl` | Konto/Löschen | not_started |
| F33 | Konto abgelaufen / keine Zeit / gesperrtes Gerät; `lib/feature/home/impl`, `lib/usecase/OutOfTimeUseCase.kt` | VPN-Status/Konto | not_started |
| F34 | Problembericht/Kontakt Support; `lib/feature/problemreport/impl`, `lib/repository/ProblemReportRepository.kt` | Diagnose/Support | not_started |
| F35 | Protokolle ansehen/teilen; `lib/feature/problemreport/impl` (ViewLogsViewModel) | Diagnose/Logs | not_started |
| F36 | Verbindungs-/Ablauf-Benachrichtigungen; `lib/feature/notification/impl`, `lib/push-notification` | VPN-Status/OS | not_started |
| F37 | Benachrichtigungseinstellungen / Accountwarnungen; `lib/feature/notification/impl`, `lib/usecase/AccountExpiryNotificationActionUseCase.kt` | VPN-Einstellungen | not_started |
| F38 | Allgemeine Einstellungen / persistente Präferenzen; `lib/feature/settings/impl`, `lib/repository/UserPreferencesRepository.kt` | VPN-Einstellungen | not_started |
| F39 | Sprache/Locale; `lib/feature/language/impl`, `lib/repository/LocaleRepository.kt` | VPN-Einstellungen | not_started |
| F40 | Erscheinungsbild/Theme; `lib/feature/appearance/impl` | Plume-Design/optionale Präferenz | not_started |
| F41 | App-Icon / Tarnungsicon; `lib/feature/appicon/impl` | Funktionsprüfung / ggf. TV-Äquivalent | not_started |
| F42 | App-Info, Version, Changelog; `lib/feature/appinfo/impl`, `lib/repository/ChangelogRepository.kt` | Über VPN | not_started |
| F43 | Aktuelle Standort-/Exit-IP-Anzeige; `lib/feature/home/impl`, `lib/usecase/LastKnownLocationUseCase.kt` | VPN-Startseite | not_started |
| F44 | App-Listen/System-App-Erkennung; `lib/feature/applisting/impl` | Split-Tunneling | not_started |
| F45 | Upstream-Updatehinweise inkl. Android-16-Hinweis; `lib/usecase/inappnotification`, `app/src/main/AndroidManifest.xml` | VPN-Hinweise | not_started |
| F46 | Netzwerkverfügbarkeit/Verbindungsweg; `lib/usecase/InternetAvailableUseCase.kt`, `lib/usecase/ConnectionPathUseCase.kt` | VPN-Status | not_started |
| F47 | Relay-Assets/Übersetzungen/Sortierung; `../dist-assets/relays/relays.json`, `lib/repository/RelayLocationTranslationRepository.kt` | Standortauswahl | not_started |
| F48 | Navigationszustand/Erststart/Welcome; `lib/feature/home/impl`, `lib/feature/login/impl`, `lib/navigation` | eigenständige TV-Navigation | not_started |

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
