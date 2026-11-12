# Feature-Matrix — Inventar des gepinnten Mullvad Android 2026.12

Quelle für **alle** Zeilen: `third_party/mullvadvpn-app/` bei SHA `aa47f606b1a159214c7279344128119fa91be5fd`. Pfade in Tabelle relativ zu `android/`. `lib/feature/<name>/impl` meint die originale Featureimplementierung; `lib/repository`, `lib/usecase` und `app` sind kein neu zu erfindender Stack. `TV` benennt die **geplante**, nicht vorhandene Plume-Ansicht.

**Status für jede Funktion in M01: `not_started`**. Repo-Pfadinspektion belegt den vorhandenen Codebereich, **nicht** Gerätefunktion oder UI-Feature-Parität. Spätere Meilensteine setzen nach Implementierung und Test `partial` bzw. `pass` und hinterlegen konkrete Evidenz in `TEST_MATRIX.md`. Endpunkt-Funktionen je gewähltem OSS/Prod-Flavour prüfen; Play-in-App-Abrechnung ist kein Bestandteil von OSS/Prod.

| ID | Upstream-Nutzfunktion / Originalpfad oder Original-API | Vorgesehene Plume-TV-Anbindung | Status |
| --- | --- | --- | --- |
| F01 | VPN verbinden, trennen, erneut verbinden; `app/service/MullvadVpnService.kt`, `lib/repository/ConnectionProxy.kt` | VPN-Startseite | not_started |
| F02 | Echtzeit-Tunnelstatus, Fehlzustände, Reconnect; `lib/grpc/ManagementService.kt`, `lib/model` | VPN-Startseite/Status | not_started |
| F03 | VPN-Systemfreigabe / Rechte-Dialog; `app/MainActivity.kt`, `app/service/MullvadVpnService.kt` | VPN-Ersteinrichtung | not_started |
| F04 | Hintergrundbetrieb, Foreground-Notification, System-Revoke; `app/service/MullvadVpnService.kt`, `app/service/notifications` | OS + Status | not_started |
| F05 | Rust-Daemon/JNI/Talpid/gRPC-/Management-IPC; `../mullvad-jni`, `../mullvad-daemon`, `../mullvad-management-interface`, `lib/talpid`, `lib/grpc` | interne Originalruntime | not_started |
| F06 | Relay-Land/Stadt/Serverauswahl und Suche; `lib/feature/location/impl`, `lib/repository/RelayListRepository.kt` | Serverauswahl | not_started |
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

Der **Codeeinbindungspfad** für F05 (Rust/JNI/gRPC/Tunnelverwaltung), F04 (VpnService), F47 (Relay-Assets) und F48 (Plume-HOME-Einstieg) wurde im Staging beschrieben und als Build-Generator implementiert. Ohne erfolgreiches APK/Manifest/ABI/JNI-Tests gibt es **keine** nachgewiesene funktionierende Runtime, keinen Tunnel und keine Benutzeroberfläche. Alle 48 Funktionen behalten daher bewusst `not_started`. Buildquelle vorhanden ist nicht gleich Feature-Parität. M03 muss den blockierten Compile-Nachweis aus M02 zunächst berücksichtigen.

## M02 build workflow change (12 November 2026)

A shared manual/PR integrated APK build now uses the official pinned Mullvad Android toolchain image. This does not prove functional VPN parity: F01-F48 remain not_started pending an actual packaged APK and subsequent runtime/TV/Shield tests. Do not mark any feature as pass because source modules merely compile.
