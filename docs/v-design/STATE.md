# V-Design — kanonischer State (M04)

> Ausschließlich dieser erste Block beschreibt den aktuellen Stand. M00–M03 unten sind **historische** Reviewstände, keine aktuellen offenen PRs.

```yaml
schema: plume-v-design-handoff/v1
checked_on: "2026-11-14 (Europe/Berlin)"
repository: Justify87/Plume_Launcher
target_application_id: dev.plume.advanced
baseline_branch: main
baseline_sha_observed: 763869b8bf672f5955b1d0682a643f0565bca5fb
predecessor_milestone: M03
predecessor_pr: 36
predecessor_merged: true
predecessor_merge_sha: 763869b8bf672f5955b1d0682a643f0565bca5fb
nuvio_branch: dev
nuvio_sha_observed: be4b84a84e004a2d36405601683df000d8dc446a
milestone: M04
status: implementation_committed_ci_in_progress_hardware_pending
work_branch: feature/vdesign-m04-card-styling
work_code_head_pre_docs: 13988ae216feecd33c39f5e3b491294b9fff6c22
draft_pr: 37
draft_pr_merged: false
reference_path: docs/v-design/REFERENCE.png
reference_blob: d1b14c00a6478cb3b49fcbbd637e0aa71db5df18
reference_sha256: 95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25
reference_bytes: 249663
reference_dimensions: "1536x864"
reference_format: jpeg_jfif_misnamed_png
reference_verified_in_m04: true
m04_initial_ci_run: 43523928137
m04_initial_ci_check_job: 132224852062
m04_initial_ci_status_at_docs: cancelled_after_docs_update
m04_mullvad_native_job_at_docs: skipped
m04_second_ci_run: 43524298952
m04_second_ci_status_before_fix: in_progress
m04_followup_ci: pending_on_final_docs_head
m04_local_gradle: not_run_no_android_sdk_checkout
m04_emulator_screenshot: not_run
m04_shield_pro_2019: hardware_pending
m04_pixel_comparison: not_run
next_milestone: M05_dpad_and_navigation
blockers:
  - Latest PR-head CI must be inspected after this docs commit and any fix
  - Real provider permissions, source images, D-pad and Nuvio intents need Shield/TV testing
  - True pixel/screenshot comparison requires actual launcher rendering
```

## Tatsächlich implementiertes M04

- Start: [M03 #36](https://github.com/Justify87/Plume_Launcher/pull/36) **merged** auf `763869b8bf672f5955b1d0682a643f0565bca5fb`; Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a` nur gelesen. [Draft-PR #37](https://github.com/Justify87/Plume_Launcher/pull/37) auf Branch `feature/vdesign-m04-card-styling`; **kein Merge autorisiert**.
- Referenz: GitHub-Datei `REFERENCE.png` tatsächlich visuell betrachtet, Base64-JPEG/JFIF neu dekodiert, **SHA-256 = 95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25** nachgerechnet, **249663 Bytes**, Git-Blob `d1b14c00...`. Kein Screenshot der laufenden App.
- Code: `VDesignCardView.kt` rendert 1 Hero, 2 Standard, 3 verfügbare Karten und 8 kompakte Upcoming-Slots mit Full-Bleed-Artwork, statischem dunklem Gradient, Nuvio-Quellenmarke, abgestufter Typografie und Lavender-Fokusring ohne Scale-/Layoutverschiebung. Nuvio `showNewBadge=false` wird respektiert; keine Hardcodes oder SmartTube-Karten. `VDesignCardText.kt` formatiert echtes S/F, reale Restzeit, lokale Tagesdaten; bei fehlender Evidenz keine Aussage.
- `VDesignHomeView.kt`: 14 wiederverwendbare View-Slots statt leeren M03-Platzhaltern; Bindung an die drei kompletten M02-Listen und Counts. UPCOMING >8 wird auf D-Pad-Abwärts-/Aufwärtsnavigation in Viererschritten umgebunden; M05 übernimmt vollständige Navigation und Focus-Cross-Row-Regression.
- `MainActivity.kt`: Weiterleitung auf existierenden asynchronen Poster-LruCache-Loader, in-flight Artwork an alle gebundenen passenden V-Karten verteilen; Quelle/Komponente/Action für Nuvio-Intents prüfen. UPCOMING setzt `launchMode=detail`, entfernt `videoId`, bekommt keinen generischen App-Fallback. Mischkanal/SmartTube/Mullvad-Gitlink bleiben unverändert.
- Tests: `VDesignCardTextTest.kt` mit fehlenden S/F, realer Dauer, lokaler Mitternacht, unsicheren URI-Schemen, Fallback-Art und ungültigen Hosts/Userinfo. Fix-Commit `13988ae` stellt bei Feed-Eviction Fokus auf die nächste echte Karte/Sidebar wieder her und prüft Bild-URI-Authority mit `java.net.URI`. **Eingecheckt, im lokalen Container nicht ausgeführt** (keine Android-SDK-/GitHub-Checkout-Umgebung). CI [43523928137](https://github.com/Justify87/Plume_Launcher/actions/runs/43523928137), [Job 132224852062](https://github.com/Justify87/Plume_Launcher/actions/runs/43523928137/job/132224852062) war zum Dokumentationszeitpunkt `in_progress`, kein Success-Claim. Mullvad native `skipped`, keine physische Abnahme.
- Review: [REVIEWS.md](REVIEWS.md) M04; Matrix [TEST_MATRIX.md](TEST_MATRIX.md). **Exakt nächster Meilenstein: M05**.

## Historischer Stand M03 (nicht aktuell)

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
status: code_docs_ci_success_hardware_pending
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
ci_current_source_result_at_docs: cancelled_on_next_docs_commit
ci_current_source_check_job: 132213947427
m03_code_docs_ci_run: 43521292243
m03_code_docs_ci_job: 132215945300
m03_code_docs_ci_sha: 8160f2bf05df335b52c9735c675b12a8dbda57b0
m03_code_docs_ci_result: completed_success
m03_release_apk_before_bytes: 535462
m03_release_apk_after_bytes: 540870
m03_release_apk_delta_bytes: 5408
m03_followup_evidence_docs_ci: pending_new_run
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
- **CI nachträglich tatsächlich abgeschlossen:** [Run 43521292243](https://github.com/Justify87/Plume_Launcher/actions/runs/43521292243) auf exakt `8160f2bf05df335b52c9735c675b12a8dbda57b0` ist **completed/success**; [check 132215945300](https://github.com/Justify87/Plume_Launcher/actions/runs/43521292243/job/132215945300) **success**. `./gradlew testDebugUnitTest lintDebug` (BUILD SUCCESSFUL) und `./gradlew assembleDebug assembleRelease` (BUILD SUCCESSFUL), 44/0 Installerchecks, 10/12/6 Offline-Python-Tests erfolgreich. Release-APK `535462 → 540870` Bytes (**+5408 Bytes**) für neue View-Shell/2 Vector-Icons und deren Klassen/Styles. `lintDebug` meldete 114 Warnungen und vorhandene Baseline-Funde, Build erfolgreich, Lint-Baseline unverändert. Mullvad-native-Job branchbedingt skipped, kein VPN-Runtime-Nachweis. Lokales Gradle/Android SDK/Emulator/Shield weiterhin nicht verfügbar. Dieser zusätzliche CI-Evidence-Nachtrag erzeugt einen neuen Docs-HEAD; sein CI muss gesondert geprüft werden.
- **Review:** `REVIEWS.md#M03`; Teststatus `TEST_MATRIX.md#M03`. Ergebnis: **partial_hardware_pending** (Code-/Docs-CI auf `8160f2bf` nachweislich erfolgreich; Evidence-Docs-Follow-up-CI noch abzuwarten). Keine Mergefreigabe.

## Canvas- und Insets-Abbildung (M03)

Die **1536×864 Referenzbildpixel** werden nicht als Android-dp interpretiert: `scale=min(hostWidth/1536,hostHeight/864)`, Zentrierungsversatz für nicht-16:9-Inset-Flächen, `x=(offsetX+referenceX*scale)`, `y=(offsetY+referenceY*scale)`; Android liefert die tatsächlich vorhandene Host-Fläche nach Fensterdekor. Bei 960×540 Host sind die Sidebar rund 136px, der Hero `x159,y46,w407,h164`. Dynamische Schrift berührt die Geometrie nicht (Font-Scale in einem begrenzten Bereich). Overscan-/Font-Scale-/Emulator-/Shield-Screenshots **nicht gemessen**, nicht als pixelgenau behauptet.

## Verantwortlicher nächster Einstieg

**M04** startet vom tatsächlichen gemergten M03-Commit bzw. vom **echten Draft-PR-#36-HEAD**, falls noch ungemergt. Echt bindbare `NuvioDashboardFeed.sections()`, echte Artwork-, Status- und Deep-Link-Implementierungen für 14 Slots plus D-Pad-Navigation; bei leeren/unbewilligten Kanälen Empty-State statt synthetischer Inhalte. `NuvioMediaItem.daysUntilRelease` beim Bind/Resume/Tageswechsel für UPCOMING anzeigen (nie Uhrzeit erfinden), `showNewBadge` respektieren, 100000ms ohne Restminuten; UPCOMING nur Details. M05 folgt mit vollständigen Apps-/Einstellungen-/HDMI-/Back-/HOME-Fokuspfaden, M06 entfernt Alt-Hosts/Mode. Bis dahin **kein Merge ohne ausdrückliche Nutzerfreigabe**.


**M04 Folge-Fix vor Abschluss:** [13988ae](https://github.com/Justify87/Plume_Launcher/commit/13988ae216feecd33c39f5e3b491294b9fff6c22) (Card-Fokus-Eviction und URI Host/Userinfo). Der erste [Run 43523928137](https://github.com/Justify87/Plume_Launcher/actions/runs/43523928137) wurde durch Folge-Commit **cancelled**, obwohl Unit/Lint schon erfolgreich durchliefen. Folge-Run [43524298952](https://github.com/Justify87/Plume_Launcher/actions/runs/43524298952) war zuvor `in_progress`; letzter HEAD-Run gesondert beobachten, nicht als Erfolg behaupten.
