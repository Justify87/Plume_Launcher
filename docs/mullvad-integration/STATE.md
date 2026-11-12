# Plume × Mullvad — verbindlicher Übergabestand

```yaml
schema: plume-mullvad-handoff/v1
milestone: M01
status: partial
checked_on: "2026-11-12"
plume_repository: Justify87/Plume_Launcher
plume_base_branch: main
plume_base_sha: ab9bcc6c5188789c952722974834b49fe7ebae05
feature_branch: feature/m01-mullvad-upstream-handoff
pull_request: pending
upstream_repository: mullvad/mullvadvpn-app
upstream_tag: android/2026.12
upstream_sha: aa47f606b1a159214c7279344128119fa91be5fd
upstream_path: third_party/mullvadvpn-app
upstream_tree_modified: false
production_vpn_implemented: false
plume_original_ci: pass_observed
plume_local_build: not_run
mullvad_original_build: not_run
shield_original_vpn_test: not_run
m02_ready_for_build_spike: true
m01_acceptance_complete: false
```

## Bestätigter Stand

- `main` hat den oben genannten SHA; zum Prüfzeitpunkt **keine offenen PRs**. Keine Vorgänger-Meilensteine und vor M01 kein `docs/mullvad-integration/` oder `AGENTS.md`.
- Mullvad `android/2026.12` ist der zuletzt **stabile** Android-Release; `android/2026.13-beta1` ist ausdrücklich nicht gewählt. Git-Tagobjekt `6d040b17e22ea445d83dd7e126e1f173ceb4b27b` zeigt auf den eingefrorenen **Commit** oben.
- Das vollständige ursprüngliche Repository ist unverändert als Git-Submodul referenziert. Die `.gitmodules`-Datei und der Gitlink `160000` sind Bestandteile von M01. **Keine** Mullvad-Runtime, VPN-UI oder Plume-App-Datei wurde in M01 geändert.
- Ausgewählte Architektur: im späteren **abgeleiteten Build** bleibt Mullvads `android/app` das **einzige Android-Application-Modul** mit `applicationId=dev.plume.advanced`; Plumes bestehender View-Code wird als Android-Library mit eigenem `namespace=dev.plume` eingebunden. Unveränderter Originalstand bleibt für Upstream-Referenzbuilds erhalten. Details / Risiken: `DECISIONS.md`.
- Tatsächliche ältere Plume-CI: `main`-Debug-Lauf 43100442410 erfolgreich; PR-Lauf 43052315531 (Direktvorgänger des Merges) erfolgreich, Release-APK dort 528066 Bytes. **Kein** eigener M01-Lokalbuild, kein Mullvad-Originalbuild, kein Shield-ADB. Details: `TEST_MATRIX.md`.
- Lokale Toolchain-Vorbedingung fehlt: im ausführenden Container keine Android SDK/NDK, kein Rust/Cargo und keine ADB-Verbindung; GitHub nicht via CLI/DNS erreichbar (`git ls-remote` Exit 128). GitHub-Repository-Aktionen und Dateiansicht waren verfügbar. Es handelt sich um eine **Umgebungsblockade für Buildnachweise**, nicht um einen festgestellten Quellcodefehler.

## Nächster Schritt — **M02** (keine stillen Upstream-Upgrades)

1. Zuerst `UPSTREAM.md` / `DECISIONS.md` / `FEATURE_MATRIX.md` / `TEST_MATRIX.md` lesen, `git submodule update --init --recursive` und Gitlink-SHA mit `git -C third_party/mullvadvpn-app rev-parse HEAD` abgleichen.
2. Mit JDK 21, SDK 37, NDK 27.3.13750724, Rust 1.97.1 und Mullvad-Buildcontainer `third_party/mullvadvpn-app/android` **unverändert** als `ossProdDebug` bauen. Erfolgs-/Fehlerlogs und resultierende APK-Größe dokumentieren. Auf Shield Konto + Verbindung nur wenn erreichbar.
3. Einen kleinen **aus Upstream abgeleiteten Arbeitsbaum** für den kombinierten Build herstellen; Original-Submodul clean halten. Mullvad `android/app` als einziges APK beibehalten, Plume als `:plume-ui`-Library anbinden, `applicationId` gezielt überschreiben; alte Mullvad-MainActivity nicht als Benutzer-UI starten. Die Machbarkeit zuerst an `assemble` und Manifest-Merge prüfen, bevor andere Features hinzukommen.
4. Vorhandene `tools/pr-rules.sh`-Sperren für neue Dependencies / Manifest-Permissions **nur integrationsspezifisch** lösen, nicht pauschal deaktivieren. Bestehende Plume-Baseline weiterhin separat baubar lassen.
5. Änderungen + echte Testbefunde **in diesem Satz von fünf Dateien** in demselben M02-PR nachführen. Keine Userfunktionen als fertig markieren, bevor reale Runtime und TV-UI verdrahtet und getestet sind.

**Offen:** Original-Mullvad-Compile, Integrations-Compile, genaue Overlays/Gradle-Compatibility, Geräteprüfung, APK-Größe integriert, Erststart/Konto, D-Pad/Back, Reboot- und Hintergrund-VPN. M01 bleibt daher `partial`; für das Gesamtprojekt besteht **keine** Feature-Parität.
