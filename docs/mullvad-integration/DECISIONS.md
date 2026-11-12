# Architekturentscheidungen — M01

**Status:** technische Zielarchitektur entschieden; Build-Machbarkeit muss in M02 nachgewiesen werden. Originalcode ist in M01 unangetastet.

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


## M02 — A06: Host/Library tatsächlich als Staging implementiert (Build-Gate offen)

Die in A02/A03 gewählte Architektur ist in tools/mullvad/prepare.py umgesetzt: Mullvad `android/app` ist der **einzige** Application-Host; Plumes View-Quellen werden unter `:plume-ui` als Android-Library eingebunden. Die Staging-Kopie erhält Mullvads ursprüngliches `MullvadApplication`, `MullvadVpnService`, Talpid, JNI/gRPC, Koin und die originalen Featuremodule. Nur ein temporärer Build-Diff ist notwendig; damit kann der M01-Gitlink unverändert bleiben. Originale Smartphone-Compose-Screens werden für M02 nicht als TV-UI exponiert, aber nicht aus dem Runtime-Graph entfernt.

Das `ossProd`-Variant-Overlay entfernt nur die sechs Mullvad-Smartphone-Launcher-Aliasse und belässt den systemgebundenen VPN-Service. Plumes bestehendes Manifest mit HOME-/LEANBACK-Einstieg, Screensaver und Service-Registrierungen wird als Library gemergt; bei widersprüchlichem Merge hat der Host im Staging die höhere Priorität. **Bis zu einem kompilierten Manifest wurde das nicht bewiesen**.

## M02 — A07: Getrennte CI und bewusster Toolchain-Kontrast

Die ursprünglichen Plume-Regeln, Java-17-Tests/Lint/Release und der APK-Größenvergleich bleiben erhalten. Der zusätzlich definierte Mullvad-CI-Job richtet SDK 37, NDK 27, Rust 1.97.1, JDK 21 und protobuf ein, erzeugt den Staging-Build und prüft die Native-SO/Relay-Datei im finalen Debug-APK. Der schwere Build wird auf Mullvad-Branches eingeschränkt. Das ist die eng gefasste Ausnahme für das VPN-Hobbyprojekt; **keine** globale Deaktivierung von Checks oder allgemeines Hardening-Projekt.

Native ABI zunächst ARM64, minSdk des Integrationshosts 28. Bestehende Plume-Versionierung wird im Host weitergereicht. Fehlendes Upgrade-/Manifest-/Boot-/API-Level-/Shield-Testing bleibt offen; ebenso Release-Minification/Signierung und die tatsächliche APK-Größenzunahme. M02 ist bis zu Build-Evidenz `blocked`.

## A08 M02: single reproducible APK workflow backed by upstream container

Decision: use the Mullvad Android container image pinned by the selected upstream commit instead of reinstalling and maintaining a parallel JDK/SDK/NDK/Rust toolchain in Plume CI. This follows upstream's official android-app.yml build approach while keeping the actual app as one Mullvad-hosted application with the Plume library.

Manual workflow_dispatch and PR workflow_call both invoke the integrated job in .github/workflows/debug-apk.yml. Its integrated default produces the combined dev.plume.advanced ARM64 debug APK, checks JNI/relay/manifest/launcher registration and uploads an artifact if successful; the launcher-only alternative retains legacy Plume behavior. Existing quick PR tests and baseline release size check still run.

Adapted upstream: pinned build image, original android/gradlew, :app:assembleOssProdDebug and Cargo/Gradle volume layout. Omitted upstream: self-hosted device tests, Firebase, mockapi, stagemole/play, automated scheduled testing and unrelated multi-flavor matrix; none prove M02's single APK packaging. Noninteractive Docker replaces upstream's podman -it for GitHub-hosted Actions. No app feature scope change.

## A-M03 — Originaldienst und nur Debug-TV-Harness (12.11.2026)

**Angenommen.** Mullvads bereits vorhandene `MullvadApplication`, `MullvadVpnService`, gRPC-/Rust-/JNI- und Boot-/Notification-Komponenten bleiben unverändert. Eine zusätzliche `Application` oder zweite VPN-Engine ist ausgeschlossen. Für M03 wird eine nur in `ossProdDebug` generierte einfache Android-View-Activity im bestehenden `:app`-Host verwendet.

Die Steuerung ist strikt an `ServiceConnectionManager` und `ConnectionProxy` sowie `AccountRepository` gebunden, der angezeigte Tunnelzustand kommt aus `ManagementService.tunnelState`. `VpnService.prepare()` übernimmt den echten Systemdialog. Das Launcher-Top-Bar-Element fragt lediglich die Existenz der Activity ab; dadurch hat die unabhängige Plume-Baseline keine Mullvad-Klassenabhängigkeit.

**Lebenszyklus:** Plumes Home-Activity verwaltet **keine** VPN-Bindung. Der Debug-Harness bindet den Service, löst bei `onStop` keinen Disconnect aus und unbindet erst bei `onDestroy`; Androids ursprüngliche System-VPN-Bindung muss auf der Shield real nachgewiesen werden. Kein eigener Boot-Autostart in M03. Kein künstlicher Status `Connected`.
