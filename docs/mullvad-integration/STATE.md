# Plume × Mullvad — verbindlicher Übergabestand

```yaml
schema: plume-mullvad-handoff/v1
milestone: M04
status: partial
checked_on: "2026-11-13"
main_head_at_start: 0daf26b271c94396ea4930a73fc7db2d3ee81ab9
base_branch: feature/m01-mullvad-upstream-handoff
base_sha: 6c857cecd3f022a524c62f17ffddb94d0233b202
predecessor_pr: 18
predecessor_merge_sha: 6c857cecd3f022a524c62f17ffddb94d0233b202
feature_branch: feature/m04-mullvad-tv-vpn-home
upstream_tag: android/2026.12
upstream_sha: aa47f606b1a159214c7279344128119fa91be5fd
upstream_gitlink_unchanged: true
production_vpn_ui: implemented_source
m04_integrated_ci: observed_ci_pass
m04_ci_run: 43328844657
m04_ci_source_sha: 389fe3a12e639fed2d4590583c53b7b0654f620a
m04_apk_artifact: Plume-Mullvad-ARM64-Debug-63
m04_pr: 19
m04_shield_test: not_run
m04_live_vpn_test: not_run
```

## Grundlage

- `main` enthält zum M04-Start nur M01. M02 (#17, Merge `fdc982a`) und M03 (#18, Merge `6c857ce`) wurden in `feature/m01-mullvad-upstream-handoff` gestapelt; **M04 basiert auf diesem Branch**, nicht auf altem `main`.
- M03: CI [43323559922](https://github.com/Justify87/Plume_Launcher/actions/runs/43323559922) **observed_ci_pass** (APK inklusive original Mullvad-Rust/JNI, DEX, Manifest, Plume-Tests). Kein Gerätetest oder bestätigter VPN-Tunnel.
- Upstream-Submodul unverändert `aa47f606b1a159214c7279344128119fa91be5fd`; die einzige Anwendung bleibt `dev.plume.advanced`.

## M04-Implementierung

- `tools/mullvad/VpnActivity.kt`: eigene native Android-TV-Activity im abgeleiteten Mullvad-OSS/Prod-Host, für Debug **und** Release. Direkte Koin-/ManagementService-Flows, `ConnectionProxy`, `AccountRepository`, echte `VpnService.prepare`-Freigabe; niemals ein alternativer Tunnelstack oder persistierter VPN-Status.
- D-Pad-Navigation: linke Fünfer-Navigation, rechte Inhaltsfläche, Fokusrahmen, OK, Back (Unterseite → Status → Launcher), SavedInstanceState für geöffnete Seite; Kontoanmeldung aus M03 übernommen. Standorte/VPN-Einstellungen und nicht integrierte Diagnosefunktionen explizit markiert.
- Plumes gemeinsame obere Leiste öffnet VPN in Classic und Overview. Eigenes Symbol am rechten Ende, ohne `TOP_KEYS`-/`top_bar`-Präferenzen zu verändern; Fokus-Rückgabe bei Back. Standalone Plume blendet den Einstieg bei fehlender Komponente aus.
- Der temporäre Debug-Harness ist aus der Staging-APK entfernt (alte Datei im Repository gelöscht). Ursprünglicher Mullvad-VPN-Service bleibt autonom; Activity-`onDestroy` unbindet ohne Disconnect.
- Mullvad-Originaldateien: **keine geändert**; ausschließlich generierte Overlay-/Staging-Ergänzungen, siehe `UPSTREAM.md`.

## Ausführung / nachgewiesene Grenzen

- Der Arbeitscontainer hat keinen DNS-Zugriff zu GitHub für Git-Clone und keinen vollständigen Android SDK/NDK/Rust-/ADB-Gerätezugang. Build, Kotlin-Compile und Shield-Tunnel können hier nicht lokal behauptet werden.
- Prüfen: `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v`, `./gradlew testDebugUnitTest lintDebug`, `bash tools/mullvad/build-integrated.sh`, `python3 tools/mullvad/check_apk.py <apk>`; CI-Ausgang steht in `TEST_MATRIX.md`.
- **M04 CI erfolgreich:** [Run 43328844657](https://github.com/Justify87/Plume_Launcher/actions/runs/43328844657) auf Code-/Workflow-SHA `389fe3a12e639fed2d4590583c53b7b0654f620a` hat `check` und `mullvad-apk` beide als `success` beendet. Der Workflow hat den kombinierten ARM64-`ossProdDebug`-Build, das Paket `dev.plume.advanced`, den produktiven `VpnActivity`-Manifest-/DEX-Eintrag, die Abwesenheit des M03-Harness, den originalen VPN-Dienst, JNI/Relays, Plume-Unit/Lint und die Staging-Tests geprüft. Artifact `Plume-Mullvad-ARM64-Debug-63` hat **120901828 Bytes als Upload**, nicht rohe APK-Größe.
- M04 bleibt `partial` bis Connected-/Reconnect-/Permission- und tatsächliche Shield-D-Pad-/Exit-IP-Prüfung erfolgreich sind. Original-Mullvad-Standalone-Releasebuild nicht ausgeführt; kein physisches Gerät vorhanden. Ein nachgeschalteter Dokumentationscommit enthält keine Buildcodeänderung.

## Handoff M05

Nächster Meilenstein: **Standortauswahl aus den originalen Mullvad-Relay-Repositories** in die bereits vorhandene `Standorte`-Detailseite einbinden. Keine Fake-Relays, keine neue VPN-Engine; F06–F08 auf gepinnten Original-APIs umsetzen. Fokus, Back, bestehende Verbindung und D-Pad auf Shield erneut testen. `TEST_MATRIX.md` mit echtem Gerätebeleg ergänzen.
