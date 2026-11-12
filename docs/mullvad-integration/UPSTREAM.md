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
