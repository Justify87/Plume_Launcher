# Architekturentscheidungen — M01

**Status (13.11.2026):** M01-Zielarchitektur beschlossen; integrierte Build-Machbarkeit in M02 und M03 per [CI Run 43323559922](https://github.com/Justify87/Plume_Launcher/actions/runs/43323559922) nachgewiesen. Originalcode unverändert; Shield-/Tunnel-Gerätetests ausstehend.

## A01 — Vollständiger, gepinnter Git-Submoduleintrag (angenommen)

`third_party/mullvadvpn-app` zeigt direkt auf den peeled Commit von `android/2026.12`. Der komplette Repository-Root bleibt erhalten, da Cargo-Workspace, `mullvad-jni`, `mullvad-daemon`, `mullvad-management-interface`, Android Gradle Build-Logic und Relay-Assets relativ zueinander referenziert werden. Kein Kopieren einzelner `android/lib`-Verzeichnisse. `UPSTREAM.md` ist die Quelle der Wahrheit für den SHA. Vorteil: nachvollziehbarer unveränderter Original-Referenzbuild / geringe Drift. Kosten: Submodul-Checkout im Integrations-CI und umfangreiche Rust/Android-Toolchain.

## A02 — Einzige installierbare App, Mullvad als Build-Host (angenommen; M02-Kompilationsgate)

**Nicht:** zwei Android-`application`-Module zusammenpacken oder Mullvad-`:app` als AAR behandeln.

- **Einziger Host:** das ursprüngliche Mullvad-`android/app` bleibt das **einzige** gebaute `com.android.application`-Modul, weil es Rust/JNI, Module, Resources, Protobuf, Produkt-Flavors und Service-Manifest bereits korrekt zusammenführt.
- **Identität:** Der abgeleitete Host-Build überschreibt `applicationId` **nur** für die kombinierte APK auf `dev.plume.advanced`; ursprünglicher Package-/Namespace `net.mullvad.mullvadvpn` und JNI-Klassennamen bleiben bestehen. Plumes bestehende Package-Namen `dev.plume` bleiben unverändert. Es gibt nur **eine** `Application`-Klasse: Mullvads App-Bootstrap (oder sehr dünne Subklasse bei tatsächlichem Bedarf).
- **Plume als eingebundene TV-Library:** Original-Plume-Views/Kotlin/Ressourcen in einem neu definierten `:plume-ui` Android-Library-Modul mit `namespace=dev.plume` und **ohne** zweite Application-/Launcher-Instanz. Android-Manifeste in einem Plume-kontrollierten Merge-Overlay auf HOME/LEANBACK und Mullvads `MullvadVpnService` abstimmen. Benutzer-Einstieg ist Plumes bisherige `dev.plume.MainActivity`, nicht Mullvads Smartphone-Compose-`MainActivity`. Originalmodule dürfen Compose als transitive Build-Abhängigkeit behalten.
- **Quellendifferenz:** keine Dateien im Git-Submodul ändern. Ein Build-Harness unter Plume erzeugt in M02 einen **temporären abgeleiteten** Mullvad-Arbeitsbaum und appliziert schmale, versionierte Patches/Overlays auf Gradle-Host/Manifest/Bootstrap; originale Quelle bleibt für Referenzbuilds jederzeit intakt. Das ist **eine** APK, keine doppelte Installation. Die M02-Spike muss das Kompilieren von `:plume-ui`, Manifest-Merge und `assembleOssProdDebug` nachweisen, bevor Featurecode beginnt.
- **Launcher-Aliasse:** Zusätzlich zu Mullvads nicht exportierter `app/MainActivity.kt` registriert `android/lib/feature/appicon/impl/src/main/AndroidManifest.xml` eine **exportierte** `MainActivityAltDefault`-Alias sowie alternative Icon-Aliasse mit `LAUNCHER`/`LEANBACK_LAUNCHER`. Im kombinierten Manifest müssen die Mullvad-Launcher-Aliasse gezielt deaktiviert/entfernt werden, sonst erscheinen zusätzliche Mullvad-App-Einträge neben Plume. Original-Aliasse bei Referenzbuilds unverändert lassen; App-Icon-Feature-Parität auf TV separat prüfen.
- **Service:** `MullvadVpnService` mit `android.permission.BIND_VPN_SERVICE`/Foreground-Service und Mullvads bestehender `MullvadDaemon`/gRPC-Verbindung bleibt unabhängig von der sichtbaren Home-Activity. Keine neue WireGuard-/VPN-Implementierung. Konto/Status/Settings über Original-Repositories und Use-Cases statt parallelem Datenmodell.
- **UI-Grenze:** eigene Plume-TV-`VpnActivity` mit D-Pad/OK/Back und sichtbarem Launcher-Einstieg **für Classic und Overview**. Benutzerbezogene Mullvad-Compose-Screens werden nicht als UI verwendet, bleiben aber zunächst als Source/Dependencies erhalten, soweit Build/Runtimemodule sie erwarten. Kein größerer Herausrefaktor in M02.

**M02-Kompatibilitätsgate:** Sollten Host-Build plus `:plume-ui` mit minimalen Overlays nicht reproduzierbar bauen (z. B. Plugin-/R-Namespace-Konflikte), die **konkreten Fehlermeldungen** dokumentieren und eine alternative SourceSet-/Composite-Methode gegen die gleiche unveränderte Quelle belegen, bevor A02 revidiert wird. Keine freie Architektur-Rotation.

## A03 — Android-/Gradle-Versionen (angenommen)

- **minSdk 28** im integrierten Build (Mullvad-Original benötigt 28; Plume bisher 26): bewusster Ausschluss Android TV 8.0/8.1. Bestehenden Plume-Baseline-Build **vorerst mit minSdk 26** separat erhalten; erst der tatsächlich integrierte Host-Build setzt 28.
- Original **compileSdk / targetSdk 37**, AGP **9.3.1**, Kotlin **2.4.10**, JDK **21**, NDK **27.3.13750724**, Rust **1.97.1** als *Ausgangspunkt* des kombinierten Host-Builds. Original-Plume: AGP 9.2.1, compile/target 36, Java 17 CI. Keine simple Änderung von Plumes Gradle-Root auf Mullvads Pluginversion ohne Proof.
- API-37-Systemverhalten und Android-TV-Gerätekompatibilität sind **offen**. Plume-UI auch auf API 28/30/36 testen; Shield-Test bevor Freigabe. `applicationId`-Wechsel in Mullvad-Host bedeutet frische eigene App-Daten unter `dev.plume.advanced` und darf Plumes gespeicherte Einstellungen beim Update nicht löschen (Upgrade-Szenario auf Gerät prüfen).
- Native Targets Mullvad standardmäßig `arm,arm64,x86,x86_64`; für Endgerät Shield mindestens `arm64-v8a` / zutreffende Device-ABI nachweisen. Keine voreilige Target-Reduktion im M01.

## A04 — Regeln, CI und Größe (angenommen)

Plumes `CONTRIBUTING.md` widerspricht neuen Bibliotheken/Hintergrunddiensten sowie Berechtigungen; `tools/pr-rules.sh` blockiert solche PRs. Der VPN-Integrationsauftrag ist eine **bewusste projektbezogene Ausnahme**, nicht unbemerkt kompatibel mit den Regeln. In M01 **keine** Regeln aufgeweicht, da nur Docs/Submodul. M02 soll erlaubte Mullvad-spezifische Änderungen eng und prüfbar isolieren; vorhandene Prüfungen für normale Launcher-Änderungen weiter erzwingen.

Der ursprüngliche Plume-Prüfworkflow nutzt JDK 17 und kein Submodul. Integration benötigt eigenständiges JDK-21/NDK/Rust-CI und wahrscheinlich mehr Buildzeit. Ursprüngliche APK-Größe ~0.53 MB versus offizielles Mullvad-APK 39.0 MB; **erhebliche Größe und Laufzeitkomplexität gewollt** und nicht als regressionsfreie Nulländerung darstellen. OSS/Prod als Basis, kein Play-Billing/Google-Play-Flavour. Keine Download-Binaries in Git.

## A05 — Nicht-Ziele M01

Keine App-Runtime, keine VPN-Activity, kein Login, kein Runtime-Service-Start, kein Mock-Client; keine zusätzlichen Netzwerkanalysen, Protection-Audits oder Custom-Protokolle. Buildstatus in `TEST_MATRIX.md` ehrlich als `not_run` behandeln. Vor M02 keine Feature-Parität behaupten.


## M02 — A06: Host/Library als Staging implementiert (Build-Gate durch CI bestätigt)

Die in A02/A03 gewählte Architektur ist in tools/mullvad/prepare.py umgesetzt: Mullvad `android/app` ist der **einzige** Application-Host; Plumes View-Quellen werden unter `:plume-ui` als Android-Library eingebunden. Die Staging-Kopie erhält Mullvads ursprüngliches `MullvadApplication`, `MullvadVpnService`, Talpid, JNI/gRPC, Koin und die originalen Featuremodule. Nur ein temporärer Build-Diff ist notwendig; damit kann der M01-Gitlink unverändert bleiben. Originale Smartphone-Compose-Screens werden für M02 nicht als TV-UI exponiert, aber nicht aus dem Runtime-Graph entfernt.

Das `ossProd`-Variant-Overlay entfernt nur die sechs Mullvad-Smartphone-Launcher-Aliasse und belässt den systemgebundenen VPN-Service. Plumes bestehendes Manifest mit HOME-/LEANBACK-Einstieg, Screensaver und Service-Registrierungen wird als Library gemergt; bei widersprüchlichem Merge hat der Host im Staging die höhere Priorität. **Das ursprünglich offene Manifest-Gate ist mit M02-CI und M03-CI nachgewiesen**; die echte Gerätefunktion ist weiterhin nicht geprüft.

## M02 — A07: Getrennte CI und bewusster Toolchain-Kontrast

Die ursprünglichen Plume-Regeln, Java-17-Tests/Lint/Release und der APK-Größenvergleich bleiben erhalten. Der zusätzlich definierte Mullvad-CI-Job richtet SDK 37, NDK 27, Rust 1.97.1, JDK 21 und protobuf ein, erzeugt den Staging-Build und prüft die Native-SO/Relay-Datei im finalen Debug-APK. Der schwere Build wird auf Mullvad-Branches eingeschränkt. Das ist die eng gefasste Ausnahme für das VPN-Hobbyprojekt; **keine** globale Deaktivierung von Checks oder allgemeines Hardening-Projekt.

Native ABI zunächst ARM64, minSdk des Integrationshosts 28. Bestehende Plume-Versionierung wird im Host weitergereicht. Fehlendes Upgrade-/Manifest-/Boot-/API-Level-/Shield-Testing bleibt offen; ebenso Release-Minification/Signierung und die tatsächliche APK-Größenzunahme. Das M02-Build-Gate war beim ersten Entwurf `blocked`, ist aber mit [Run 43314187927](https://github.com/Justify87/Plume_Launcher/actions/runs/43314187927) und [Run 43323559922](https://github.com/Justify87/Plume_Launcher/actions/runs/43323559922) **bestanden**. Runtime-/Shield-Funktion bleibt `not_run`.

## A08 M02: single reproducible APK workflow backed by upstream container

Decision: use the Mullvad Android container image pinned by the selected upstream commit instead of reinstalling and maintaining a parallel JDK/SDK/NDK/Rust toolchain in Plume CI. This follows upstream's official android-app.yml build approach while keeping the actual app as one Mullvad-hosted application with the Plume library.

Manual workflow_dispatch and PR workflow_call both invoke the integrated job in .github/workflows/debug-apk.yml. Its integrated default produces the combined dev.plume.advanced ARM64 debug APK, checks JNI/relay/manifest/launcher registration and uploads an artifact if successful; the launcher-only alternative retains legacy Plume behavior. Existing quick PR tests and baseline release size check still run.

Adapted upstream: pinned build image, original android/gradlew, :app:assembleOssProdDebug and Cargo/Gradle volume layout. Omitted upstream: self-hosted device tests, Firebase, mockapi, stagemole/play, automated scheduled testing and unrelated multi-flavor matrix; none prove M02's single APK packaging. Noninteractive Docker replaces upstream's podman -it for GitHub-hosted Actions. No app feature scope change.

## A-M03 — Originaldienst und nur Debug-TV-Harness (12.11.2026)

**Angenommen.** Mullvads bereits vorhandene `MullvadApplication`, `MullvadVpnService`, gRPC-/Rust-/JNI- und Boot-/Notification-Komponenten bleiben unverändert. Eine zusätzliche `Application` oder zweite VPN-Engine ist ausgeschlossen. Für M03 wird eine nur in `ossProdDebug` generierte einfache Android-View-Activity im bestehenden `:app`-Host verwendet.

Die Steuerung ist strikt an `ServiceConnectionManager` und `ConnectionProxy` sowie `AccountRepository` gebunden, der angezeigte Tunnelzustand kommt aus `ManagementService.tunnelState`. `VpnService.prepare()` übernimmt den echten Systemdialog. Das Launcher-Top-Bar-Element fragt lediglich die Existenz der Activity ab; dadurch hat die unabhängige Plume-Baseline keine Mullvad-Klassenabhängigkeit.

**Lebenszyklus:** Plumes Home-Activity verwaltet **keine** VPN-Bindung. Der Debug-Harness bindet den Service, löst bei `onStop` keinen Disconnect aus und unbindet erst bei `onDestroy`; Androids ursprüngliche System-VPN-Bindung muss auf der Shield real nachgewiesen werden. Kein eigener Boot-Autostart in M03. Kein künstlicher Status `Connected`.

## M03-Code-Review: D-Pad-Grenze bei zusätzlichem Debug-Zugang (12.11.2026)

Im ersten M03-Entwurf war der VPN-Eintrag als zusätzliche View außerhalb von `TOP_KEYS` angehängt worden. Die beiden bestehenden Navigationsmodi hielten jedoch den letzten alten `TOP_KEYS`-Button für die rechte Fokusgrenze. Entscheidung: Keine neue konfigurierbare Top-Bar-Architektur und keine zusätzliche Persistenz für den **temporären** Debug-Harness. Stattdessen nutzt `atRightTopBarEdge` den aktuell sichtbaren Debug-VPN-Button als rechten Rand und fällt sonst auf den historischen Rand zurück. Existierende dynamische VPN-View wird nicht bei jeder Top-Bar-Aktualisierung ab-/wieder angehängt. Das minimiert Seiteneffekte in Classic/Overview.

## M04 — A08: TV-Activity im vorhandenen Original-Host

- Die zuvor debug-only registrierte Harness-Activity wird durch `net.mullvad.mullvadvpn.app.VpnActivity` im **`ossProd`-SourceSet** ersetzt. Das Plume-:plume-ui-Modul könnte die originalen Mullvad-Koin-/gRPC-Abhängigkeiten nicht ohne zusätzliche API-Abstraktion ansprechen; die direkte View-Activity im abgeleiteten Host ist konservativer und hält den Upstream unverändert.
- Das Plume-Toppanel erkennt die vorhandene Host-Activity nur über `PackageManager` und öffnet sie explizit im eigenen Paket. Ein zusätzliches Icon rechts vom vorhandenen, frei sortierbaren `TOP_KEYS`-Set ist gewollt: **keine** Migration oder Löschung gespeicherter `top_bar`-Werte; gleicher Einstieg in Classic/Overview.
- Der UI-Status ist ausschließlich die letzte In-Memory-Ausgabe der Original-`ManagementService`-Flows. Bei nicht bereitem Daemon wird kein alter Tunnelstatus als live dargestellt. Die Activity darf den System-VPN-Zustimmungsdialog starten, aber den Tunnel bei Back/Home nicht beenden.
- Navigations-Shell für spätere Meilensteine: `Status`, `Standorte`, `Konto`, `VPN-Einstellungen`, `Diagnose`. Noch fehlende Funktionsflächen tragen einen klaren Hinweis; keine Demo-Daten oder Umleitungen auf die Mullvad-Smartphone-Compose-UI.


## A-M05 — Native Konto-Seite am Original-Repository (13.11.2026)

- **Angenommen:** Unter der existierenden Konto-Destination von `VpnActivity` wird eine kleine zusätzliche View-Klasse `VpnAccountUi` in das *abgeleitete* `ossProd`-App-Host-SourceSet kopiert. Keine globale Navigation, keine neue Application, keine zweite Datenhaltung; Kontozustand nur aus Original-`deviceState` und `accountData`.
- **Original-APIs statt neuer HTTP-Clients:** Konto (create/login/logout/refresh/delete), Voucher (submit), Devices (deviceList/remove) und NewDeviceNotification aus den durch `UiModule`/`AppModule` eingebundenen Mullvad-Repositories. Nach Anmeldung und erneuter Activity-Anzeige wird aktualisiert; die Mullvad-Servicebindung gehört weiterhin der Host-Activity und wird beim Schließen ohne Disconnect aufgehoben.
- **UI:** Native EditText mit TV-Tastatur, sichtbare Button-Fokusrahmen, D-Pad/OK/Back, bestätigte Löschaktionen, Kontoablauf und echte Fehlermeldungen. Konto-/Gutschein-Credentials werden weder in Plume-Preferences noch in eigene Logs geschrieben; das ist Funktionskorrektheit der Original-Implementierung, kein separates Hardening-Projekt.
- **OSS-Zahlung:** Webseite ausschließlich über den vorhandenen Upstream-Token-URL-Helfer, Browser optional. Kein Play-Billing-Rewrite. **Gerät umbenennen** wird bewusst nicht angeboten: die an der gepinnten Android-Version nachgewiesenen Repository-/ViewModel-Methoden unterstützen nur Anzeigen und Entfernen.
- **Risiko:** Weder Netzwerk-/Gültigkonto- noch Shield-/Keyboard-Funktion ist vor dem CI-/Gerätetest bewiesen. Keine Änderung an Rust/JNI, `VpnService`, Plume-Launcher oder integrierter Build-Toolchain nötig.


### A-M05-Review — erforderlicher originaler UI-Module-Load

Der Quellreview des **gepinnten** Upstreams ergab: `MullvadApplication.onCreate` lädt nur `appModule`; `Mullvad MainActivity.onCreate` lädt separat `uiModule` und `paymentModule`. Die Plume-`VpnActivity` ersetzt den Smartphone-Entry-Point, deshalb muss sie das gleiche originale `loadKoinModules(listOf(uiModule, paymentModule))` vor dem Aufbau von `VpnAccountUi` ausführen. `VoucherRepository` und `NewDeviceRepository` wären sonst nicht registriert. Bewusster minimaler Host-Lifecycle-Overlay, **keine** Änderung des Original-`AppModule`/`UiModule`, kein neuer DI-Container.

## A-M06 — Standortansicht ohne Relay-Stack-Abzweig (13.11.2026)

- Native View `VpnLocationUi` im `ossProd`-Host statt eigenes Relaymodell, Parser oder Compose-Navigation. `FilteredRelayListUseCase(RelayListType.Single)` respektiert die echten Constraints, einschließlich Mullvads Relay-Partitioning; Original-`newFilterOnSearch` liefert die Server-/Städte-/Ländersuche. Der TV-Renderer zeigt die hierarchischen Original-`RelayItem`-Werte.
- `SelectSinglehopUseCase` als einzige Exit-Setter-Schnittstelle; analog Original-`SelectLocationViewModel` wird **nicht** zusätzlich `ConnectionProxy.reconnect()` ausgelöst. `ManagementService.tunnelState` bleibt einzige Quelle für den echten Exit, der in M04 dargestellt wird.
- Anbieter und Eigentümer werden in `RelayListFilterRepository` als echte `Constraint`-Werte gespeichert; Filterziele in M06 ausschließlich `RelayHopType.EXIT`. Multihop-Entry-Constraints, DAITA usw. nicht vor M09 umstellen.
- Original-`CustomListsRepository`, `CustomListsRelayItemUseCase`, `RecentsUseCase`, `SettingsRepository` übernehmen Persistenz; „Favoriten“ sind Custom Lists, keine eigene lokale Tabelle. Der Upstream hat keine `setSinglehopExitAutomatic`-Operation: `Constraint.Any` als bestehende Auswahl darstellen, aktives Reset ausdrücklich offen lassen und nicht als pass melden.
- In `VpnActivity` ist `addLocationPanel` ausdrücklich **kein** weiterer `ScrollView`-Wrapper: nur zwei innere Scrollflächen bieten einen definierten Höhen-/Fokusbereich für lange Länder-/Serverlisten. Unberührte M04-/M05-/Launcher-Navigation und Submodul-Quellen behalten.

**M06 UI-Review-Korrektur:** Links gewählte Länder/Listen behalten nach OK den linken Fokus; erst D-Pad-Rechts wechselt in die Detailspalte. Das vermeidet konkurrierende `post`-Fokusrestaurierung nach Neuzeichnen. Der CI-Kotlin-Fehler aus der falschen Position des `onClick`-Parameters wurde in der Plume-only-View behoben; originaler Mullvad-Code weiterhin unverändert.

## A-M07 — VPN-Basiswerte als eine originale Settings-Oberfläche (13.11.2026)

- **Ansicht:** statt Compose-Smartphone-Settings eine Plume-eigene `VpnSettingsUi` in der bereits vorhandenen VPN-Activity, mit klarer DNS-Unteransicht und D-Pad-Back/Fokus. Kein separater Launcher-Settings-Menüpunkt und keine Activity mit eigenem VPN-Lifecycle.
- **Wahrheit:** die originalen Koin-`SettingsRepository`/`WireguardConstraintsRepository`/`AutoStartAndConnectOnBootRepository`-Streams werden laufend gelesen; Writes laufen über deren Methoden, ohne Preferences-Doppelbuchhaltung. Android-Komponentenstatus ist der originale Boot-Repo-StateFlow.
- **DNS:** sechs originale `DefaultDnsOptions`-Flags; numerische IPv4-/IPv6-Adressen mit derselben `InetAddressValidator`-Bibliothek und `InetAddress`-Duplikatprüfung wie im Original-`CustomDnsDialogViewModel`. Löschen via `DeleteCustomDnsUseCase`; bei letztem DNS-Eintrag Rückkehr zu Default, keine leere Custom-Konfiguration behaupten. Lokale/IPv6-Erreichbarkeit als Hinweis statt heimliche Parameteränderung.
- **System:** die Android-VPN-Settings-Oberfläche via `SystemVpnSettingsAvailableUseCase`/Intent. Android stellt der regulären App keinen zuverlässigen öffentlichen Always-on-/Lockdown-Schalterstatus zur Verfügung; deshalb nicht aus eigenen Variablen ableiten. Mullvads realen Boot-Receiver via dessen Original-Repository schalten.
- **M10-Trennung:** `setCustomWireguardPort` schreibt `obfuscationSettings.wireguardPort`, keinen generischen WireGuard-Tunnelport. Diese Portsteuerung bei Anti-Zensur in M10 belassen; Quantum-Resistenz, IP-Version und MTU sind einfache Basiseinstellungen für M07. Nicht gegen frühere Entscheidungen oder Rust/JNI refaktorieren.
