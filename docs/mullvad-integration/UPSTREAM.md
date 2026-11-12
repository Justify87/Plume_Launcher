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
