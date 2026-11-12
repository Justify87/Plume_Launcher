# Plume × Mullvad — verbindlicher Übergabestand

```yaml
schema: plume-mullvad-handoff/v1
milestone: M03
status: partial
checked_on: "2026-11-12"
target_repository: Justify87/Plume_Launcher
main_head_at_start: 0daf26b271c94396ea4930a73fc7db2d3ee81ab9
base_branch: feature/m01-mullvad-upstream-handoff
base_sha: fdc982a9a36053a3b6c5a3d190ae90d65ab6536c
predecessor_pr: 17
predecessor_merge_sha: fdc982a9a36053a3b6c5a3d190ae90d65ab6536c
feature_branch: feature/m03-mullvad-real-service
m03_source_commit: a5aea96b143d971a9096a04ecfc8411821c57470
m03_pr: 18
upstream_tag: android/2026.12
upstream_sha: aa47f606b1a159214c7279344128119fa91be5fd
upstream_gitlink_unchanged: true
m02_integrated_ci: observed_pass
m02_ci_run: 43314187927
m03_integrated_ci: pending
m03_runtime_test: not_run
m03_shield_test: not_run
production_vpn_ui: false
```

## Aktuelle Git-/Build-Fakten

- M01 wurde als PR #16 nach `main` gemergt; `main` war zu M03-Beginn `0daf26b`.
- M02 PR #17 wurde **in den noch vorhandenen M01-Feature-Branch** gemergt, nicht direkt nach `main`. Deshalb ist der tatsächliche M03-Ausgangspunkt `fdc982a` auf `feature/m01-mullvad-upstream-handoff`; kein Rebuild auf altem `main`.
- M02 CI [Run 43314187927](https://github.com/Justify87/Plume_Launcher/actions/runs/43314187927): Plume-Checks und kombinierter ARM64-APK-Build erfolgreich, einschließlich `check_apk.py`, `aapt`-Paket-/Manifestprüfung und Artifact-Upload. **Das ist ein struktureller APK-Nachweis, kein Gerätetunneltest.**
- Vollständiger Original-Mullvad-Submodulstand bleibt auf `aa47f606b1a159214c7279344128119fa91be5fd`; einzig generierte Dateien unter `.build/` werden ergänzt.

## M03-Code und reale Schnittstellen

- `tools/mullvad/prepare.py` kopiert `PlumeVpnDebugActivity.kt` nur nach `android/app/src/ossProdDebug/kotlin/...` und registriert sie nur im `ossProdDebug`-Manifest.
- Die Debug-Activity ist **eine native Android-View-Activity** im bestehenden Mullvad-`:app`-Host. Sie verwendet unverändert `MullvadApplication` und Koin-`appModule`, `ServiceConnectionManager`, `AccountRepository.login(AccountNumber)`, `ConnectionProxy.connect()/disconnect()/reconnect()` und `ManagementService.connectionState/tunnelState/deviceState`.
- Der sichtbare Plume-Top-Bar-Zugang **VPN** erscheint nur, wenn diese Debug-Activity im **gleichen Paket** installiert ist. Daher weder ein zweites Launcher-Icon noch eine zweite Android-Application; in standalone Plume / Release bleibt der Zugang unsichtbar. Gilt für Classic und Overview.
- Android `VpnService.prepare()` fragt über den **echten Systemdialog** die Berechtigung an. Ablehnung löst keinen Connect aus. `ConnectionProxy.connect()` führt zusätzlich Mullvads originale Prüfungen aus.
- Nach dem Verlassen der TV-VPN-Activity ruft Plumes `MainActivity.onStop()` weder VPN-Disconnect noch Daemon-Stop auf. Der Test-Harness unbindet erst bei eigener Zerstörung; der Originaldienst hat `stopWithTask=false` und verwaltet sein VPN-Systembinding selbst.
- Originale Mullvad-Kotlin-/Rust-/JNI-/gRPC-/Service-Dateien **nicht verändert**. Die ursprünglichen Mullvad-Boot- und Notifications-Komponenten bleiben im Hostmanifest. Kein Konto hardcodiert; kein eigener Tunnel-Stack.

## Evidenz und Grenzen

- M03-Änderungen: Quellcode implementiert, aber CI/Runtime/Shield noch **nicht** nachgewiesen; daher `partial` und kein grüner Tunnelstatus. Der neue CI-PR-Build ist die nächste reproduzierbare Prüfung.
- Android SDK/NDK, Rust/Cargo und ADB stehen in der ausführenden Arbeitsumgebung nicht vollständig zur Verfügung. Keine angeschlossene Shield und keine Mullvad-Kontodaten.
- Original Compose-MainActivity bleibt im Host enthalten, aber ohne Smartphone-Launcher-Alias. M04/M05 müssen Debug-Harness durch produktive TV-Oberflächen ersetzen.

## M04-Handoff

Nach grünem M03-Compile und Shield-Test die Debug-Activity durch eine produktive Plume-VPN-Activity ersetzen; Top-Bar-Einstieg dauerhaft machen. **Nicht** die UI-Harness-Klasse als VPN-Engine übernehmen. Wiederverwendbare APIs: `AccountRepository.login`, `ConnectionProxy.connect/disconnect/reconnect`, `ManagementService`-Flows, `ServiceConnectionManager.bind/unbind` sowie `VpnService.prepare`. Die Service-Bindung ist Activity-unabhängig zu halten. Testkommandos und offene Fälle stehen in `TEST_MATRIX.md`.
