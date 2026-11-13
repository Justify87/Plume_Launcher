# V-Design — kanonischer State (M03)

> Dieser Abschnitt ist die **aktuelle Wahrheit**. Ältere M00–M02-Zwischenstände stehen als zeitgebundene Historie in `REVIEWS.md` / `TEST_MATRIX.md`. Sie sind weder aktuelle PR- noch CI-Statusmeldungen.

```yaml
schema: plume-v-design-handoff/v1
checked_on: "2026-11-13"
repository: Justify87/Plume_Launcher
target_application_id: dev.plume.advanced
baseline_branch: main
baseline_sha_observed: 3f77ba552b47152349d28aae15b02a865b367649
predecessor_milestone: M02
predecessor_pr: 35
predecessor_merged: true
predecessor_merge_sha: 3f77ba552b47152349d28aae15b02a865b367649
nuvio_branch: dev
nuvio_sha_observed: be4b84a84e004a2d36405601683df000d8dc446a
milestone: M03
status: source_implemented_ci_pending_emulator_not_run_shield_pending
work_branch: feature/vdesign-m03-layout-sidebar
work_code_head_pre_docs: 71f5e80c09d004185cb09f7226d9ac66e66eeae1
draft_pr: 36
draft_pr_merged: false
source_diff_files: 6
reference_path: docs/v-design/REFERENCE.png
reference_blob: d1b14c00a6478cb3b49fcbbd637e0aa71db5df18
reference_sha256: 95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25
reference_bytes: 249663
reference_dimensions: "1536x864"
reference_format: jpeg_jfif_misnamed_png
reference_verified_in_m03: true
ci_first_code_run: 43520147382
ci_first_code_result: cancelled_after_new_commit
ci_second_code_run: 43520288284
ci_second_code_result: cancelled_after_new_commit
ci_current_source_run: 43520674555
ci_current_source_result_at_docs: in_progress_not_success
ci_current_source_check_job: 132213947427
ci_mullvad_native: skipped_branch_filter
m03_local_gradle: not_run_no_android_sdk_checkout
m03_emulator_screenshot: not_run
m03_shield_pro_2019: hardware_pending
m03_actual_pixel_comparison: not_run
next_milestone: M04_real_nuvio_media_cards
blockers:
  - Final PR-head CI needs independent observation after documentation commit
  - Real Shield/Android-TV provider permissions, focus, and intents not exercised
  - M04 must bind actual Nuvio media and real card actions without fake data
  - M05 must complete D-pad/Back/HOME/All-Apps/HDMI regression before M06 removal
```

## Nachweisbarer M03-Stand

- **Quelle:** M02 [#35](https://github.com/Justify87/Plume_Launcher/pull/35) tatsächlich gemergt; alter dokumentierter M02-Draft-Status war nur damals richtig. [M02-Code-CI 43517654027](https://github.com/Justify87/Plume_Launcher/actions/runs/43517654027) success auf `97ed7658...`. Nuvio `dev@be4b84a...` wurde ausschließlich gelesen. Die historische Mullvad-Dokumentation liegt nach D-005 unter `docs/archive/legacy-features/mullvad-vpn/STATE.md`, nicht unter dem alten Pfad.
- **Referenz tatsächlich gelesen:** GitHub-Datei Base64/JPEG-Dimensionsheader und eigenständig SHA-256 über alle Bytes geprüft. 249663 Bytes, 1536×864, SHA `95af66ad...`; Originalbild außerdem visuell betrachtet. Kein Screenshot der tatsächlich laufenden App.
- **Implementierungs-Commits vor Docs:** [a0ed6251](https://github.com/Justify87/Plume_Launcher/commit/a0ed62512c860bafb83f37d3250e5ee0f8ffe19a) Grundshell, [b0299f38](https://github.com/Justify87/Plume_Launcher/commit/b0299f38d1093d28ce9101ac6e722e17474efe57) Drawer-/HDMI-Korrektur, [71f5e80c](https://github.com/Justify87/Plume_Launcher/commit/71f5e80c09d004185cb09f7226d9ac66e66eeae1) visuelles Anchor-Review. Branch ohne Reset, Fremdcommit oder Merge.
- **Sichtbar:** Ein programmatischer, klassischer Android-View-Host über den alten Home-Views, unter All-Apps-/Settings-Overlays; tiefer statischer Farbverlauf; 14 unfokussierbare Leer-Slots (Hero+2, 3, 4×2). Rechte Kachelgeometrie folgt `DESIGN_CONTRACT.md` und der Referenz; 5 feste Sidebar-Schlüssel, Ziele nach Installation gefiltert, VPN nur bei resolvierbarer interner Activity. Kein App-/Fake-Serienmaterial, keine SmartTube-Kachel.
- **Daten/Lebenszyklus:** M02-`NuvioDashboardFeed` bindet die vollständigen deduplizierten Zähler; Count-Refresh nach TV-Query/Resume und nur ein lokaler, DST-sicherer Tageswechsel-Callback während Resume. Release-Daten-Kacheltexte entstehen erst M04. Bestehender `GlassLayer` bleibt auf Root-Index 2, Wallpaper/Video unberührt. Direct Boot beendet `onCreate` vor Initialisierung des neuen Hosts/Persistenzzugriff.
- **Temporäre Navigation:** Übersicht langes OK → vorhandenes All-Apps; Einstellungen → vorhandenes Settings-Panel, langes OK → bestehende Sources/HDMI-Auswahl; VPN → originale integrierte Activity. Altes `home_layout_mode` wird in M03 bewusst **nicht** gelöscht, aber als sichtbarer Startmodus von der neuen Shell überlagert; vollständige Einstellungs-/Pref-Migration gehört M06.
- **Prüfgrenze:** JVM-Koordinatentests sind committed; Android SDK/Emulator/Shield in dieser Ausführung nicht verfügbar. Die bisher beobachteten CI-Run-IDs sind keine endgültigen Success-Belege; aktuelle PR-HEAD-Checks nach diesem Dokumentationscommit gesondert nachlesen. Mullvad-native-Job branchbedingt skipped.
- **Review:** `REVIEWS.md#M03`; Teststatus `TEST_MATRIX.md#M03`. Ergebnis: **partial_pending_final_ci_and_hardware**. Keine Mergefreigabe.

## Canvas- und Insets-Abbildung (M03)

Die **1536×864 Referenzbildpixel** werden nicht als Android-dp interpretiert: `scale=min(hostWidth/1536,hostHeight/864)`, Zentrierungsversatz für nicht-16:9-Inset-Flächen, `x=(offsetX+referenceX*scale)`, `y=(offsetY+referenceY*scale)`; Android liefert die tatsächlich vorhandene Host-Fläche nach Fensterdekor. Bei 960×540 Host sind die Sidebar rund 136px, der Hero `x159,y46,w407,h164`. Dynamische Schrift berührt die Geometrie nicht (Font-Scale in einem begrenzten Bereich). Overscan-/Font-Scale-/Emulator-/Shield-Screenshots **nicht gemessen**, nicht als pixelgenau behauptet.

## Verantwortlicher nächster Einstieg

**M04** startet vom tatsächlichen gemergten M03-Commit bzw. vom **echten Draft-PR-#36-HEAD**, falls noch ungemergt. Echt bindbare `NuvioDashboardFeed.sections()`, echte Artwork-, Status- und Deep-Link-Implementierungen für 14 Slots plus D-Pad-Navigation; bei leeren/unbewilligten Kanälen Empty-State statt synthetischer Inhalte. `NuvioMediaItem.daysUntilRelease` beim Bind/Resume/Tageswechsel für UPCOMING anzeigen (nie Uhrzeit erfinden), `showNewBadge` respektieren, 100000ms ohne Restminuten; UPCOMING nur Details. M05 folgt mit vollständigen Apps-/Einstellungen-/HDMI-/Back-/HOME-Fokuspfaden, M06 entfernt Alt-Hosts/Mode. Bis dahin **kein Merge ohne ausdrückliche Nutzerfreigabe**.
