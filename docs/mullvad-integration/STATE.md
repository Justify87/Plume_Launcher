# Plume × Mullvad — verbindlicher Übergabestand

```yaml
schema: plume-mullvad-handoff/v1
milestone: M02
status: blocked
checked_on: "2026-11-12"
plume_repository: Justify87/Plume_Launcher
plume_main_sha: ab9bcc6c5188789c952722974834b49fe7ebae05
predecessor_branch: feature/m01-mullvad-upstream-handoff
predecessor_pr: 16
predecessor_head: cbe1f46bd38ac6dfee5918b255cb4f26315b3e6d
predecessor_status: partial
feature_branch: feature/m02-mullvad-apk-integration
pull_request: 17
upstream_repository: mullvad/mullvadvpn-app
upstream_tag: android/2026.12
upstream_sha: aa47f606b1a159214c7279344128119fa91be5fd
upstream_path: third_party/mullvadvpn-app
upstream_tree_modified: false
integration_strategy: staged_mullvad_app_host_plus_plume_library
integration_prepare: tools/mullvad/prepare.py
integration_build: tools/mullvad/build-integrated.sh
integration_gradle_root: .build/plume-mullvad/mullvadvpn-app/android
integration_build_task: :app:assembleOssProdDebug
application_id: dev.plume.advanced
mullvad_application: net.mullvad.mullvadvpn.app.MullvadApplication
mullvad_service: net.mullvad.mullvadvpn.app.service.MullvadVpnService
plume_main_activity: dev.plume.MainActivity
native_abi_target: arm64-v8a
m02_local_build: not_run
m02_ci_build: queued
m02_apk_verified: false
m02_shield_test: not_run
m03_ready: false
```

## Ausgangsbasis und PRs

- main-HEAD zu Arbeitsbeginn: ab9bcc6c5188789c952722974834b49fe7ebae05.
- M01 ist **offen** als PR #16 und enthält das gepinnte, unveränderte Mullvad-Submodul und alle fünf Handoff-Dateien; M01-Gesamtstatus ist **partial** (ohne Android-/Rust-Build).
- M02 ist der gestapelte **Draft-PR #17** gegen den M01-Branch, nicht gegen altes main. Der M02-Codecommmit zu Beginn ist 9b1e630247696fea7b5164bca83b850bdcfe0bf5. Keine anderen PRs wurden gemergt.
- Das einmal gewählte Upstream-Tag android/2026.12 / SHA aa47f606b1a159214c7279344128119fa91be5fd bleibt unverändert.

## Tatsächliche Integration — noch ohne Compile-Nachweis

- tools/mullvad/prepare.py verifiziert den upstream HEAD, erzeugt eine **wegwerfbare Kopie** des vollständigen Mullvad-Repositorys unter .build/ (nicht committet), kopiert Plumes originale Views, Tests, Ressourcen und Manifest in die Android-Library :plume-ui und fügt sie als Dependency in Mullvads originales :app-Application-Modul ein.
- Nur im Staging wird die Application-ID in defaultConfig auf dev.plume.advanced geändert. Das ossProd-Manifest-Overlay macht Plumes MainActivity zum HOME-/Leanback-Einstieg und entfernt sechs Mullvad-Smartphone-Launcher-Aliasse. Mullvads MullvadApplication, MullvadVpnService, Rust/JNI, gRPC, Repository-/DI- und Relay-Dateien bleiben original.
- Ein einziger **geplanter** installierbarer Android-App-Host (Mullvad-:app, ossProdDebug). ARM64 ist ein vorläufiges ABI-Ziel, ohne Shield-Geräteabfrage. Plume-Paketname, Kotlin-Pakete und UI-Bestand bleiben erhalten.
- Der **ursprüngliche** Root-Build `./gradlew :app:assembleDebug` bleibt eine reine Plume-Baseline. Die **kombinierte** App wird mit `bash tools/mullvad/build-integrated.sh` beziehungsweise aus dem generierten Android-Root mit `./gradlew :app:assembleOssProdDebug` gebaut. Kein irreführender Anspruch, dass der alte Root-Befehl eine Mullvad-APK liefert.
- Plumes bisherige Java-17-PR-Checks (Regeln, Tests, Lint, Release, Größenvergleich) bleiben aktiv. Ein zusätzlicher 120-Minuten-JDK21-/SDK37-/NDK27-/Rust1.97.1-CI-Job für Mullvad-Branches wurde definiert; zum Dokumentationszeitpunkt **noch kein Resultat**.

## Grenzen und M03-Handoff

In dieser Ausführungsumgebung fehlen Android SDK/NDK, Rust/Cargo und ADB; GitHub ist über das Git-CLI wegen DNS nicht erreichbar. Deshalb sind weder Original-Mullvad noch die abgeleitete APK **lokal** gebaut oder auf der Shield getestet worden. Die CI-Ausführung, korrekter Manifest-Merge, JNI-Load, tatsächliche Paket-ID, Startfähigkeit und APK-Größe sind ebenfalls offen. Das Gate M02 ist **blocked**, nicht pass.

**Nächste konkrete Aufgabe (noch M02, bevor M03 beginnt):** GitHub-PR #17 Build-/CI-Logs prüfen; Compiler-/Manifest-/JNI-Fehler korrigieren; Original- und Integrationsbuild erfolgreich nachweisen; APK- und Manifest-Inhalt, Größe sowie Plume-Baseline dokumentieren. Erst dann M02 auf pass setzen und in M03 die originale Mullvad-VPN-Dienstlogik an eine fernbedienbare TV-VPN-Activity anbinden. Keine UI/Feature-Parität vor realen Tests.
