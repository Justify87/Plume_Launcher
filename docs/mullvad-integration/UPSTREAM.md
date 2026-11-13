# Mullvad Android — eingefrorener Originalstand und Übernahme

## Unveränderliche Quelle für M02–M12

| Feld | Wert |
| --- | --- |
| Repository | https://github.com/mullvad/mullvadvpn-app |
| Gewählter stabiler Tag | `android/2026.12` |
| Release | 2026-09-30 (stable, nicht prerelease) |
| Annotiertes Tagobjekt | `6d040b17e22ea445d83dd7e126e1f173ceb4b27b` |
| **Peeled Commit-SHA** | **`aa47f606b1a159214c7279344128119fa91be5fd`** |
| Nicht gewählt | `android/2026.13-beta1` (2026-11-10, prerelease) |
| Submodul | `third_party/mullvadvpn-app` |
| Einbindung | `.gitmodules` + Gitlink Mode `160000`; **vollständiges** Repo samt Root-Cargo-Workspace |
| Mullvad-Originaldateien in M01 geändert | **keine** |
| Lokale Mullvad-Patches in M01 | **keine** |
| Status Originalbuild | `not_run`, Umgebung ohne SDK/NDK/Rust/Clone-Netzzugang |

Tagprüfung: `https://api.github.com/repos/mullvad/mullvadvpn-app/git/refs/tags/android/2026.12` verweist auf das **Tagobjekt**, dessen `object.sha` den obigen Commit fixiert; **nicht** Tagobjekt-SHA mit Quellcommit verwechseln. Letzter stabiler Release geprüft anhand https://github.com/mullvad/mullvadvpn-app/releases und https://mullvad.net/en/download/vpn/android (12.11.2026). Ein neuerer Tag darf **nicht** automatisch nachgezogen werden.

## Checkout / reproduzierbarer Pin

```bash
git clone --recurse-submodules https://github.com/Justify87/Plume_Launcher.git
cd Plume_Launcher
git checkout feature/m01-mullvad-upstream-handoff # oder den später gemergten Hauptbranch
git submodule update --init --recursive
git -C third_party/mullvadvpn-app rev-parse HEAD
# MUSS aa47f606b1a159214c7279344128119fa91be5fd sein:
git ls-tree HEAD third_party/mullvadvpn-app
git -C third_party/mullvadvpn-app status --porcelain
```

Nach jedem neuen Checkout bei CI **`actions/checkout@v5` mit `submodules: recursive`** nutzen, sofern der Integrationsbuild das Submodul benötigt. Das aktuelle Plume-PR-CI checkt Submodule noch nicht aus; M01 benötigt dies wegen rein dokumentarischer Änderungen nicht. Keine automatischen `git submodule update --remote`-Aufrufe.

## Originalstruktur: Pfade nicht verflachen

- `third_party/mullvadvpn-app/Cargo.toml`, `Cargo.lock`, `rust-toolchain.toml` (Rust **1.97.1**), `mullvad-jni/`, `mullvad-daemon/`, `mullvad-management-interface/`, `talpid-*/`: zusammenhängender Rust-Workspace. `mullvad-jni` = `cdylib` / `libmullvad_jni.so`; Rust-Daemon samt Core-Tunnel-Verwaltung und gRPC/Protobuf werden unverändert referenziert.
- `android/settings.gradle.kts`: Mullvad-Composite-Build `gradle/build-logic` plus die `:lib:...`-Module und Feature-`api/impl`-Module; **nicht** nur `:app` isoliert importieren.
- `android/app/build.gradle.kts`: **Application-Plugin**, `oss` + `prod`-Flavors, Rust-Gradle-Plugin, Relay- und Release-Asset-Verzeichnisse, `jniLibs`; aktuell `applicationId=net.mullvad.mullvadvpn`. Keine konsumierbare AAR.
- `android/gradle/libs.versions.toml`: AGP **9.3.1**, Kotlin **2.4.10**, minSdk **28**, compileSdk **37**, targetSdk **37**, NDK **27.3.13750724**.
- `android/lib/feature/appicon/impl/src/main/AndroidManifest.xml`: exportierte Activity-Aliasse für Default/Icon-Tarnung inkl. LEANBACK-LAUNCHER. Für Originaltest den Alias, **nicht** die `exported=false` Mullvad-`MainActivity`, als ADB-Einstieg verwenden; beim kombinierten Build keinen zweiten Launcher-Eintrag ausliefern.
- `android/app/src/main/kotlin/net/mullvad/mullvadvpn/app/MullvadApplication.kt`: Koin-App-Start/Benachrichtigungen. `.../app/service/MullvadVpnService.kt` + `MullvadDaemon.kt`: VPN-Lifecycle, JNI-Start, gRPC-Management; `.../di/AppModule.kt`, `UiModule.kt`, `VpnServiceModule.kt`: DI-Grenzen. `android/lib/{grpc,repository,model,talpid,usecase,feature,...}` bleiben Original.
- `dist-assets/relays/relays.json`: statisches Relay-Asset (Buildsource). Ressourcen/Protobuf/JSon/DI und native Bibliotheken sind im kombinierten APK zu berücksichtigen.

## Originalbuild: referenzierte upstream-Kommandos, **in M01 nicht ausgeführt**

Voraussetzungen Linux x86_64, Podman (alternativ `CONTAINER_RUNNER=docker`), Git-Clone, verfügbare Buildimages/Artefakte, Speicher/Netz; siehe `third_party/mullvadvpn-app/android/docs/BuildInstructions.md`.

```bash
cd third_party/mullvadvpn-app/android
../building/containerized-build.sh android
# alternativ bei installierter Toolchain: ./gradlew assembleOssProdDebug
# Artefakt bei letzterem: app/build/outputs/apk/ossProd/debug/
```

Für manuellen Build: JDK 21, SDK `platforms;android-37`, Build Tools 37.0.0, NDK 27.3.13750724, protobuf-compiler, GCC, Rust 1.97.1 inkl. Android-Rust-Targets. `android/gradle/verification-metadata.xml` und Build-/Cargo-Lockdateien nicht entfernen. Container-Build wird durch Mullvad selbst bevorzugt. M01 hat **keinen Exitcode** für diese Befehle; kein Originalbuild behauptet.

Offiziell veröffentlichte **fremdgebaute** APK `MullvadVPN-2026.12.apk`: **39,008,494 Bytes**, SHA256 `0ad70b89b2b48bb0b1ae29115eee57d75277ffeccf95a0d3db98945d740a6d9e` laut Release-Asset-Metadaten; **nicht** als selbst gebauter Nachweis.

## Integration / Drift-Protokoll

**M01:** Gitlink, `.gitmodules` und ausschließlich Handoff-Dateien. `git diff aa47f606...` innerhalb Submoduls soll **leer** bleiben. Zukünftige minimale Anpassungen **nur in Plume-kontrollierten Overlay-/Patch-Dateien** mit Pfad, Grund, Prüfkommando und Upstream-Diff notieren; zu Buildzwecken einen **abgeleiteten** Arbeitsbaum verwenden, den originalen Gitlink nicht verändern. Vor jeder Revision prüfen, dass der stabile SHA noch identisch ist. Kein Snapshot, kein Rewrite des VPN-Stacks.

## Lizenz / Attribution

Mullvad: Root `LICENSE.md`, `Cargo.toml` `GPL-3.0-or-later`. Plume: Root `LICENSE` GPL-3.0. Original Copyright-/Lizenzhinweise, Third-Party-NOTICE-Dateien und korrespondierenden Source bei Weitergabe der kombinierten APK verfügbar halten; später im TV zugänglich machen. Markenzeichen/Logos nicht als offizielle Mullvad-App ausgeben. Dies ist Dokumentation, kein separater Lizenzcheck.


## M02 — dokumentierte Staging-Patches (12.11.2026)

Mullvad-Gitlink und **sämtliche originalen Mullvad-Kotlin-/Rust-Dateien bleiben unverändert**. Die folgenden Änderungen existieren ausschließlich in dem von tools/mullvad/prepare.py erzeugten Arbeitsbaum unter .build/:

| Abgeleiteter Mullvad-Pfad | Patch im Staging | Begründung |
| --- | --- | --- |
| android/settings.gradle.kts | include(":plume-ui") | Original-Host um Plume-Library ergänzen |
| android/app/build.gradle.kts | nur defaultConfig.applicationId auf dev.plume.advanced; zusätzlich implementation(project(":plume-ui")) | ein einziges APK |
| android/plume-ui/build.gradle.kts (neu) | com.android.library aus Mullvads AGP-Katalog, namespace dev.plume, minSdk 28, compileSdk 37, Plume-ResValue | originale Plume-Views ohne neue Application |
| android/plume-ui/src/main und src/test (neu) | Kopie der Original-Plume-Dateien, inklusive Ressourcen/Manifest | originale Plume-Kotlin-Quellen beibehalten |
| android/app/src/ossProd/AndroidManifest.xml (neu) | Plume-Label/-Icons, Theme für Plume MainActivity, sechs Mullvad-Appicon-Aliasse mit tools:node=remove | ein sichtbarer TV-Launcher statt konkurrierender Mullvad-Start-Icons |
| android/app/src/ossProd/res/values/plume_integration.xml (neu) | eigener Name Plume Launcher | keine globale Umbenennung von Mullvad-Strings |

**Buildpfad:** `bash tools/mullvad/build-integrated.sh` baut im temporären Mullvad-Root `:app:assembleOssProdDebug`, bewahrt dessen Plugin-/Library-/Rust-Struktur, verwendet den **originalen Mullvad Gradle Wrapper 9.7.0**, AGP 9.3.1, Kotlin 2.4.10, SDK 37, JDK 21, NDK 27.3.13750724, Rust 1.97.1 und reicht Plumes versionCode/versionName weiter. Nur ARM64 ist bis zur Shield-ABI-Prüfung konfiguriert. Relay-Datei bleibt unter dist-assets/relays/relays.json; JNI-Output stammt wie upstream aus app/build/rustJniLibs/android und dem Cargo-Workspace target/.

Die separate Plume-Baseline bleibt auf AGP 9.2.1, Gradle 9.4.1, compile/targetSdk 36, minSdk 26 und Java 17; Integration erfordert minSdk 28. Es gibt **keine** Patch-Dateien im Original-Git-Submodul. Der generierte Source-Diff muss nach jedem prepare-Lauf reproduzierbar sein.

**Belegstatus (aktualisiert 13.11.2026):** Integrierter Host-/ARM64-Rust-/JNI-Build und strukturelle APK-/Manifest-/Relay-Prüfung sind **`observed_ci_pass`**: [M02 Run 43314187927](https://github.com/Justify87/Plume_Launcher/actions/runs/43314187927) und [M03 Run 43323559922](https://github.com/Justify87/Plume_Launcher/actions/runs/43323559922). M03-Artifact `Plume-Mullvad-ARM64-Debug-60` vorhanden; dessen Upload-Größe 120884259 Bytes ist nicht die rohe APK-Größe. Der separate, unveränderte Mullvad-Originalbuild bleibt `not_run`; tatsächliche Shield-Installation und VPN-Laufzeit bleiben ebenfalls `not_run`.

## M02 CI original-container adoption (12 November 2026)

At the M01 pinned commit, Mullvad's .github/workflows/android-app.yml builds :app:assembleOssProdDebug via building/container-run.sh. The image is specified by digest in building/android-container-image.txt: ghcr.io/mullvad/mullvadvpn-app-build-android@sha256:ca22c11fb8a15321f897fa169c9c9d1f7d0d9edfeea16fa04af4de71ebcf4286.

This image contains JDK 21, Android SDK 37.0, build-tools 37.0.0, NDK 27.3.13750724 and Rust 1.97.1 with Android targets, according to upstream android/docker/Dockerfile. The shared Plume wrapper runs the original android/gradlew, mounts generated Mullvad source under /build plus /cargo-target, Cargo registry and Gradle cache volumes, and passes Plume app ID/version through staged Gradle and existing version properties. It removes upstream's interactive -it requirement, uses Docker on GitHub-hosted Ubuntu (Podman supported via CONTAINER_RUNNER), targets ARM64, and limits Gradle/Cargo concurrency to fit the runner.

No Mullvad repository source patch or upstream SHA change. Only CI orchestrator and staged Plume-integration overlays are modified. PR and manual APK generation share the same implementation in .github/workflows/debug-apk.yml. Previously documented host SDK/NDK/Rust manual setup is superseded for M02 integrated CI. The original standalone Mullvad build and actual Shield test remain unverified.

## M03 — temporäre Debug-Schnittstelle (12.11.2026)

Der Pin `android/2026.12` / `aa47f606b1a159214c7279344128119fa91be5fd` bleibt identisch. **Null Änderungen am originalen Mullvad-Submodul**, also auch keine Rust-/JNI-/Daemon-/VpnService-Patches. Der bestehende `MullvadApplication`- und `MullvadVpnService`-Manifestpfad aus M02 bleibt aktiv.

| Plume-verwaltete Datei | Staging-Änderung | Grund |
| --- | --- | --- |
| `tools/mullvad/prepare.py` | Kopiert die TV-Test-Activity nach `android/app/src/ossProdDebug/kotlin/...` und meldet sie im Debug-Overlay an | eine einzige App; Harness strikt debug-only |
| `tools/mullvad/PlumeVpnDebugActivity.kt` | Neue Android-View-Activity mit originalem Koin-/gRPC-/Konto-/VPN-Backend | Status und echte Steuerung während M03 ohne Compose-Screens |
| `app/src/main/java/dev/plume/MainActivity.kt` | Dynamischer Top-Bar-Zugang nur beim Vorhandensein der kombinierten Debug-Activity | Classic und Overview ohne neue Laufzeitdependency |
| `tools/test_mullvad_prepare.py` | Prüft Staging-Quelle und Debug-Manifest-Eintrag | isolierten Release-/Debug-Pfad sicherstellen |

M03 ändert die normalen Mullvad-Original-Quelldateien **nicht**. Die generierte Debug-Activity gehört zum existierenden Mullvad-Host und nutzt dessen Originalmodule, statt eigenen WireGuard-/gRPC-Code zu erzeugen. M04/M05 entfernen/ersetzen den Harness.

## M04 — produktive TV-Oberfläche ohne Upstream-Diff (13.11.2026)

| Plume-Datei | Staging-Pfad / Änderung | Grund |
| --- | --- | --- |
| `tools/mullvad/VpnActivity.kt` | `android/app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnActivity.kt` (neu) | native Plume-TV-UI nutzt die **originalen** ManagementService-/Repository-/VPN-Service-Schnittstellen |
| `tools/mullvad/prepare.py` | `android/app/src/ossProd/AndroidManifest.xml` registriert VpnActivity; Debug-Overlay behält nur Branding | dieselbe UI in kombinierten Debug/Release-Varianten |
| `app/src/main/java/dev/plume/MainActivity.kt` und `res/drawable/ic_vpn.xml` | als `:plume-ui` im Staging | dauerhafter Classic-/Overview-Einstieg, gespeicherte Topbar unberührt |
| `tools/mullvad/PlumeVpnDebugActivity.kt` | entfernt; keine Kopie mehr in `ossProdDebug` | M03-Testharness nicht als produktive UI ausliefern |

**Originaler Mullvad-SHA bleibt `aa47f606b1a159214c7279344128119fa91be5fd`**; Original-Kotlin, Gradle, Rust, JNI, gRPC, VpnService und DI im Submodul: **0 lokale Änderungen**. Nur die beim Prepare regenerierten `ossProd`-Dateien weichen vom gepinnten Original-Buildbaum ab. Kein separates VPN-Paket, kein Original-App-Compose-Einstieg als Benutzeroberfläche. Geräteprüfung weiterhin `not_run`.

**M04-Nachweis:** [GitHub Actions Run 43328844657](https://github.com/Justify87/Plume_Launcher/actions/runs/43328844657), Code-/Workflow-Commit `389fe3a12e639fed2d4590583c53b7b0654f620a`, `check=success`, `mullvad-apk=success` (Containerbuild + Manifest-/DEX-/JNI-/Relays-Checks). Artifact `Plume-Mullvad-ARM64-Debug-63`, Uploadgröße 120901828 Bytes (nicht APK-Rohgröße). Keine zusätzlichen Original-Mullvad-Quellpatches. Shield-/Exit-IP-/Tunnelabnahme weiterhin `not_run`.


## M05 — Konto-UI und Null-Diff zur Quelle (13.11.2026)

- Gepinnter Stable-Tag am 13.11.2026 erneut gegen Releases geprüft: `android/2026.12` bleibt jüngster Stable, `android/2026.13-beta1` ist Beta. **Kein** Upstream-Update; Gitlink `aa47f606b1a159214c7279344128119fa91be5fd` bleibt gleich.
- Original-Mullvad-Dateien: **0 geändert**. Kein Eingriff in `AccountRepository.kt`, `DeviceRepository.kt`, `NewDeviceRepository.kt`, `VoucherRepository.kt`, Mullvad-ViewModels, gRPC, Rust/JNI oder Dienst.
- Neu: `tools/mullvad/VpnAccountUi.kt`, generiert nach `android/app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnAccountUi.kt`. Temporärer Kopiervorgang in `tools/mullvad/prepare.py` neben `VpnActivity.kt`, ohne das Submodul zu patchen. `VpnActivity.kt` im Overlay bindet direkte Original-Flows an den neuen Bildschirm; Originaldatenbank/API unverändert.
- Originale Konto-Webweiterleitung: `AccountRepository.getWebsiteAuthToken()` und `net.mullvad.mullvadvpn.lib.common.util.createAccountUri` (ein unverändertes Upstream-Hilfsmodul). Im OSS-Build keine Google-Play-Billing-Komponenten implementiert.
- M05-Review: `AccountRepository` bietet create/login/logout/refresh/delete, `DeviceRepository` list/remove/update, `VoucherRepository.submitVoucher`; `NewDeviceRepository` verwaltet nur Neu-Gerät-Benachrichtigung. Umbenennen ist in dieser originalen Android-Repository-Schnittstelle nicht vorhanden; kein erfundener Remote-Call.
- Ergebnisstand beim Quellcommit: statische Integration vorbereitet, kompilierbarer CI- und Gerätestand noch nachzuholen; Nachweise in `TEST_MATRIX.md`.


**M05-Review-Korrektur:** Das originale `MullvadApplication` lädt nur `appModule`; `Mullvad MainActivity` lädt `uiModule` und `paymentModule` separat. Das Plume-Host-Overlay `VpnActivity.kt` lädt deshalb genau diese originalen Module vor der nativen Konto-View, damit `VoucherRepository`/`NewDeviceRepository` auflösbar sind. Originalquellen weiterhin **null Änderungen**.

## M06 — Standort-Overlay (13.11.2026)

Original-Pin bleibt `android/2026.12` / `aa47f606b1a159214c7279344128119fa91be5fd`, **keine** Änderung an `third_party/mullvadvpn-app`. Neue Plume-kontrollierte `tools/mullvad/VpnLocationUi.kt` wird in `prepare.py` ausschließlich nach `android/app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/` im temporär kopierten Host gestaged. Anpassung an `tools/mullvad/VpnActivity.kt` bindet den View und die bereits durch `uiModule` registrierten Original-Repositories/Use-Cases an. Alle Relaylisten kommen aus Mullvads `ManagementService` und `RelayListRepository`, nicht über einen Plume-Server oder HTTP-Parser. Test: `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` und integrierter `bash tools/mullvad/build-integrated.sh`. Einschränkung: offizieller Singlehop-Selector unterstützt keinen Rückstellaufruf auf `Constraint.Any` über die exponierte Repository-API; kein Originalpatch für M06.

## M07 — Settings-Overlay, weiterhin Null-Diff zum Original (13.11.2026)

- Ursprünglicher Stable-Tag `android/2026.12`, Commit `aa47f606b1a159214c7279344128119fa91be5fd`, Gitlink und Dateien `third_party/mullvadvpn-app/**` **unverändert**.
- `tools/mullvad/VpnSettingsUi.kt` ist nur ein zusätzliches Plume-verwaltetes Android-Views-Overlay unter `android/app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/` im generierten Build. `tools/mullvad/prepare.py` kopiert dieses zusammen mit den bisherigen M04–M06-Views. `VpnActivity.kt` verbindet Original-Koin-Repositories und die neue TV-Unterseite.
- Die originale `SettingsRepository.setCustomWireguardPort`-API verändert den **Obfuskationsport**, daher keine scheinbare allgemeine WireGuard-Port-Funktion in M07. Androids `android.net.vpn.SETTINGS` ist der originale Aufruf für Always-on/Lockdown; der Boot-Receiver `AutoStartVpnBootCompletedReceiver` verbleibt im Upstream.
- Die originalen DNS-/MTU-Validierungskriterien bleiben erhalten: `InetAddressValidator` + Duplikatsvergleich, `Mtu.fromString`, DNS-Status/Filter über vorhandenes Repository. Getestet wird das Overlay via `tools/test_mullvad_prepare.py`; der volle Kotlin-/JNI-Build über unveränderte CI-Prozedur.


## M08 — Originalquellen unverändert

Keine Änderungen an `third_party/mullvadvpn-app`, originalen `SplitTunnelingRepository`, `SplitTunnelingUseCase`, `ApplicationsProvider`, `MigrateSplitTunneling` oder Rust/JNI. Im regenerierbaren Host kommt nur `VpnSplitTunnelingUi.kt` hinzu (`tools/mullvad/prepare.py`); `VpnActivity.kt` erhält TV-Rail-Integration. Pin: `aa47f606b1a159214c7279344128119fa91be5fd`. Das originale `SplitTunnelingUseCase` leert die dargestellten Ausschlüsse bei deaktiviertem Feature; die TV-View liest zur Anzeige dauerhaft gespeicherter Werte stattdessen direkt den originalen Repository-Flow `excludedApps`. Kein neuer Speicher. Prüfen: `git diff --submodule=log -- third_party/mullvadvpn-app`.

## M09 — fortgeschrittene Tunnel-Views, Original vollständig unverändert

**Einziger Upstream:** `android/2026.12` auf `aa47f606b1a159214c7279344128119fa91be5fd`, Gitlink `third_party/mullvadvpn-app` unverändert. Änderungen an originalen Mullvad-Dateien: **0**. Rust/Daemon/JNI/gRPC/Protokoll, `android/app/src/main`, `android/lib/**` bleiben bitgleich.

| Plume-Datei | Abgeleiteter Staging-Pfad / Grund |
| --- | --- |
| `tools/mullvad/VpnAdvancedUi.kt` (neu) | `android/app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/VpnAdvancedUi.kt`: originale Multihop-/DAITA-/Migration-States und TV-Schalter |
| `tools/mullvad/VpnLocationUi.kt` | Bisheriger M06-Standortbrowser auch für `RelayListType.Multihop(ENTRY/EXIT)`, originale `ModifyMultihopUseCase`-Auswahl; keine zweite Relay-Datenbank |
| `tools/mullvad/VpnActivity.kt` | Rail-Einstieg „Erweitert“, Tunnel-Entry/Exit aus `TunnelState`, D-Pad-/Back-Integration |
| `tools/mullvad/VpnSettingsUi.kt` | Minimaler Fokus-Entry-Point zum bestehenden M07-`QuantumResistantState.On/Off`-Schalter |
| `tools/mullvad/prepare.py` | Kopiert die neue TV-View ausschließlich in die disposable `ossProd`-Kopie |
| `tools/test_mullvad_prepare.py` | Staging-/Architektur-Assertions, keine Android/Rust-Patches |

**Originalschnittstellen:** `WireguardConstraintsRepository`, `SettingsRepository`, `FilteredRelayListUseCase`, `ModifyMultihopUseCase`, `HopSelectionUseCase`, `MultihopMigrationRepository`, `RelayListFilterRepository`, `ConnectionProxy`. `ConnectionPathUseCase` liefert Geokoordinaten, keine verlässliche Hostname-Anzeige; dafür wird der bestehende `TunnelState.Connected.location` gelesen. Wiederherstellbarer Build: `python3 tools/mullvad/prepare.py`, `bash tools/mullvad/build-integrated.sh`. Kein Upgrade des Upstream-Pins und kein zusätzliches APK.

## M10 — unveränderter Upstream / lokales UI-Overlay (13.11.2026)

- Original-Tag `android/2026.12`, Gitlink `aa47f606b1a159214c7279344128119fa91be5fd`, kein Update, keine neuen Patches und **keine** geänderten Original-Mullvad-Dateien. Neuerer Android-Beta-Tag `2026.13-beta1` ist kein Ersatz für diesen Pin.
- Neu in Plume: `tools/mullvad/VpnNetworkUi.kt`; Änderungen ausschließlich in `tools/mullvad/VpnActivity.kt`, `tools/mullvad/prepare.py`, Source-Staging-Tests und Übergabedokumentation. `prepare.py` kopiert die neue View nur in die temporäre Host-APK, neben M04–M09-Overlays. Die originalen `android/lib/feature/anticensorship`, `apiaccess`, `serveripoverride`, `lib/repository` und `mullvad-daemon` bleiben 1:1.
- Original-Schnittstellen: `SettingsRepository` (Obfuscation-/Port-Setter), `RelayListRepository` (Portbereiche), `ApiAccessRepository` (API-Zugangsfluss, Test, CRUD) und `RelayOverridesRepository` (Settings-Patch/Reset). Änderungen werden durch den originalen ManagementService/gRPC verarbeitet; keine direkte Mutation von JSON-/Konfigurationsdateien.
- Diff-Reproduktion: `git -C third_party/mullvadvpn-app status --porcelain` muss leer sein, `git ls-tree HEAD third_party/mullvadvpn-app` muss den gepinnten SHA zeigen. Der einzige Hostpatch entsteht kontrolliert unter `.build/plume-mullvad/`.

## M11 — Diagnose und Notification-PendingIntents (13.11.2026)

- Mullvad-Tag/Gitlink bleibt unverändert: `android/2026.12` / `aa47f606b1a159214c7279344128119fa91be5fd`. **0 Dateien im Originalsubmodul geändert.**
- Neue ausschließlich lokale Overlaydatei: `tools/mullvad/VpnDiagnosticsUi.kt`; `prepare.py` kopiert sie nach `android/app/src/ossProd/kotlin/net/mullvad/mullvadvpn/app/` des generierten Hosts.
- **Einzige neue Original-Kopie-Abweichung (funktional notwendig):** In der regenerierten Datei `android/lib/common/src/main/kotlin/net/mullvad/mullvadvpn/lib/common/constant/ClassNames.kt` wird ausschließlich `MAIN_ACTIVITY_CLASS` von `"$MULLVAD_PACKAGE_NAME.app.MainActivity"` auf `"$MULLVAD_PACKAGE_NAME.app.VpnActivity"` umgestellt. Original `VPN_SERVICE_CLASS`, Original-Quick-Tile, Account-Expiry-/Tunnel-PendingIntents und Rust/JNI/Daemon/Repositories **unverändert**. Ohne dieses Routing öffnen Systemmeldungen/Tile beim Antippen den falschen nicht für TV vorgesehenen Smartphone-Screen.
- Der Patch ist exakt und fail-fast über `route_original_notifications_to_tv` geprüft, im Generator-Pytest nachgewiesen und nach jedem Build neu aus dem festgepinnten Original erzeugt. Neue Berechtigungen oder zweite Application: **keine**. Exakte kompilerte Varianten-/Shield-Evidenz siehe `TEST_MATRIX.md`.
