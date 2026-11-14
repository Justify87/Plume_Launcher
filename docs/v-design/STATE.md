# V-Design — kanonischer State (M06)

> **M06 ist implementiert auf Draft-PR #39, noch nicht gemergt.** Der darunter erhaltene M05-Block ist ein historischer Stand, dessen Aussage „PR #38 offen“ durch den verifizierten Merge überholt ist. Dies ist der einzige kanonische Statusort.

```yaml
schema: plume-v-design-handoff/v1
checked_on: "2026-11-14 (Europe/Berlin)"
repository: Justify87/Plume_Launcher
target_application_id: dev.plume.advanced
milestone: M06
status: code_committed_ci_followup_running_hardware_pending
baseline_main_sha: de9bb87eef0ee13cbdcc9a884363839b17c2e602
predecessor_m05_pr: 38
predecessor_m05_merged: true
predecessor_m05_merge_sha: de9bb87eef0ee13cbdcc9a884363839b17c2e602
nuvio_dev_sha: be4b84a84e004a2d36405601683df000d8dc446a
work_branch: feature/vdesign-m06-remove-legacy-home
code_commit_1: 8e1ec2728f3a4035d5e1efb34bd33f27201d1598
code_commit_2: e48abd7c7d5554a5b5e2482df173ba38ebe76d0a
code_commit_3: d5d88e502ea960419756625523553d596f29de2d
code_commit_4: cabf0a76705759254f69f8c8754591fbbfff3c3f
draft_pr: 39
draft_pr_merged: false
first_code_ci_run: 43533466727
first_code_ci_check_job: 132256619151
first_code_ci_result: completed_failure_compile_two_legacy_symbols
second_code_ci_run: 43533837289
second_code_ci_check_job: 132257870564
second_code_ci_result: completed_failure_legacy_youtube_test_ref
third_code_ci_run: 43534292196
third_code_ci_result: completed_failure_android_json_stub_in_new_test
fourth_code_ci_run: 43534832233
fourth_code_ci_result_at_documentation: in_progress
mullvad_native_job: skipped_branch_filter
reference_path: docs/v-design/REFERENCE.png
reference_git_blob: d1b14c00a6478cb3b49fcbbd637e0aa71db5df18
reference_sha256: 95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25
reference_dimensions: "1536x864"
reference_format: jpeg_jfif_misnamed_png
local_android_sdk_build: not_run_no_checkout_or_sdk
shield_pro_2019: not_run_hardware_pending
emulator: not_run
next_milestone: M07
blockers:
  - latest M06 CI job result must be observed and compilation/test failures fixed
  - Shield HOME, D-Pad, Direct Boot, provider availability, All Apps, VPN and wallpaper need physical testing
  - hidden clock/topbar compatibility host can be consolidated after behavioral validation; it is not a Home alternative
```

## M06 tatsächliche Änderungen und Grenzen

- **Produktiver Start:** `MainActivity` erzeugt nur `VDesignHomeView` als Home; `activity_main.xml` enthält weder `classicHome` noch `overviewHome` und keine alternativen App-/Watch-Row-Bäume mehr. Die alte Auswahl `home_layout_mode` wird nirgendwo gelesen.
- **Entfernt:** `HomeLayoutMode`, `HomeSidebarView`, `OverviewHomeView`, `HomeFocusNavigator`, `OverviewRowsNavigator`, `HomePageState` samt reinem Alt-Projektor, `OverviewRenderSignature`, Demo-Medienquelle und zugehörige Tests. V-spezifische Navigations- und Datenklassen bleiben.
- **Erhalten:** installierte Apps, All Apps, TV-Eingänge, TV-/Nuvio-Channel-Queries, Wallpaper, Foto-/Screensaverfunktionen, Systemaktionen/Launcher-Setup, relevante Settings, Original-Mullvad. Alte Preferences werden **nicht** global gelöscht; alte Importfelder werden ignoriert.
- **Direktstart:** Bei gesperrtem Benutzer wird ein Daten-freies `VDesignHomeView` mit Overview-Sidebar gezeigt. Keine Credential-Preferences oder TV-Listings vor Entsperren.
- **Kompatibilität:** Versteckte Clock-/Topbar-Views sind als interne System- und Styling-Helfer weiterhin im `content`-Container, aber `visibility=gone`; das ist **kein auswählbarer zweiter Home-Renderer**. Das ist gezielt belassene technische Schuld und zu testen.
- **Nicht angefasst:** Nuvio-`CONTINUE`-Mischkanal, SmartTube als V-Content-Kachel, Mullvad-Rust/JNI/Submodule, Android-Manifest-Berechtigungen. Normbild wurde angesehen und als identischer Git-Blob beibehalten.
- **Abnahme:** statische Sichtbarkeits-/Referenzsuche durchgeführt, erster CI-Compilerlauf fehlgeschlagen; zweiter Lauf kompilierte produktiven Code, scheiterte aber an entfernten Overview-Referenzen im YouTube-Unit-Test; Test auf businesslogische RSS-Deduplizierung migriert; dritter Run kompilierte 218 Tests und lieferte 1 Fehlschlag im neu eingeführten JSON-Importtest wegen Android-`org.json`-JVM-Stub. [cabf0a7](https://github.com/Justify87/Plume_Launcher/commit/cabf0a76705759254f69f8c8754591fbbfff3c3f) prüft stattdessen die reine `SettingsTransfer.validate`-Migrationslogik. Vierter CI-Run [43534832233](https://github.com/Justify87/Plume_Launcher/actions/runs/43534832233) war beim Dokumentieren `in_progress`; vollständiger Dateiimport bleibt als Android-Runtime-Test offen. Keine realen Shield-Bilder, Performance-/HOME-/VPN-Runtime-Erfolge.

---

# V-Design — kanonischer State (M05)

> **Aktueller Stand** (14.11.2026, Europe/Berlin). Frühere M00–M04-Statusangaben im nachfolgenden Text sind historische Momentaufnahmen, insbesondere der bereits gemergte M04-PR #37.

```yaml
schema: plume-v-design-handoff/v1
checked_on: "2026-11-14 (Europe/Berlin)"
repository: Justify87/Plume_Launcher
target_application_id: dev.plume.advanced
baseline_branch: main
baseline_sha_observed: fdd10f3394463bd5d4142c9978834d78464905cc
predecessor_milestone: M04
predecessor_pr: 37
predecessor_merged: true
predecessor_merge_sha: fdd10f3394463bd5d4142c9978834d78464905cc
nuvio_branch: dev
nuvio_sha_observed: be4b84a84e004a2d36405601683df000d8dc446a
milestone: M05
status: code_docs_ci_success_evidence_followup_pending_hardware_pending
work_branch: feature/vdesign-m05-navigation-interaction
work_code_head_before_docs: d80aa6fdc5e4d55d2a5b003ee7ce4638ec59296d
draft_pr: 38
draft_pr_merged: false
reference_path: docs/v-design/REFERENCE.png
reference_blob: d1b14c00a6478cb3b49fcbbd637e0aa71db5df18
reference_sha256: 95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25
reference_bytes: 249663
reference_dimensions: "1536x864"
reference_format: jpeg_jfif_misnamed_png
reference_verified_in_m05: true
m04_final_predecessor_ci_run: 43524952681
m04_final_predecessor_ci_result: completed_success
m05_code_ci_run: 43528175980
m05_code_ci_status_at_documentation: cancelled_on_subsequent_docs_commit
m05_code_docs_ci_run: 43528616572
m05_code_docs_ci_check_job: 132240568044
m05_code_docs_ci_sha: 1e8a7cb5e7d2c823a32e4069cfa96e1a71a11b0e
m05_code_docs_ci_result: completed_success
m05_release_apk_before_bytes: 545714
m05_release_apk_after_bytes: 550334
m05_release_apk_delta_bytes: 4620
m05_docs_head_ci: pending_evidence_followup_run
m05_native_mullvad_job: skipped_branch_filter
m05_local_gradle: not_run_no_android_sdk_checkout
m05_emulator: not_run
m05_shield_pro_2019: hardware_pending
m05_real_home_screenshot: not_run
next_milestone: M06_remove_legacy_home_and_mode
blockers:
  - CI of the evidence-only documentation follow-up must be independently inspected
  - Android TV/Shield HOME, DPAD, overlay, real Nuvio episode playback, HDMI and VPN runtime still untested
  - M06 removes legacy hidden home views and mode after preserving all reachable functions
```

## Tatsächlich implementiertes M05

- **Baseline:** [M04 PR #37](https://github.com/Justify87/Plume_Launcher/pull/37) am `main@fdd10f3394463bd5d4142c9978834d78464905cc` gemergt, CI [Run 43524952681](https://github.com/Justify87/Plume_Launcher/actions/runs/43524952681) auf M04-Code-und-Docs-HEAD tatsächlich `completed/success`. Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a` ausschließlich gelesen. Historische Mullvad-Docs liegen korrekt in `docs/archive/legacy-features/mullvad-vpn/`; Originalgitlink `third_party/mullvadvpn-app` blieb unberührt.
- **Referenz erneut geprüft:** Originalbytes von `REFERENCE.png` aus GitHub-Base64 gelesen, JPEG/JFIF-Header sowie SOF0-Maße 1536×864 eigenständig geprüft, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25` für 249663 Bytes unabhängig neu berechnet. Kein Real-App-Screenshot/Pixelvergleich.
- **Fokus:** `VDesignFocusNavigator.kt` trifft reine, deterministische Entscheidungen anhand der echten 4 visuellen Reihen und Kartenbreiten. `VDesignHomeView.kt` führt D-Pad aus, überspringt leere Bereiche, führt aus der ersten erreichbaren Karte zurück zur Sidebar und merkt pro Kategorie den stabilen Medien-Schlüssel. Alle Fokusziele sind aktuelle, angehängte, sichtbare Views. UPCOMING >8 bleibt per Viererschritt-Paging und Identitätserhalt erreichbar; weder 14 leere Kachel-Dummies noch alte Home-Views sind fokussierbar.
- **Lebenszyklus:** `MainActivity.kt` verdrahtet `onResume/onNewIntent/onBackPressed`, All-Apps-/Settings-Schließung und das D-Pad vor `homeNav`; kein unerwarteter Alt-Layout-Fokus. HOME kehrt zum aktuellen V-Design zurück, Back zu Sidebar/Overlays. VPN-Rückkehr verwendet den alten, bestehenden integrierten Activity-Einstieg. `refreshVDesignNavigation()` entfernt nicht installierte Links; Menu-Fokus fällt auf tatsächlich sichtbare Sidebar-Einträge zurück.
- **Aktionen:** Sidebar Nuvio/SmartTube als echte installierte App-Ziele; `Übersicht` langes OK für vorhandenes All Apps; `Einstellungen` normales OK fürs existierende Settings-Overlay, langes OK für HDMI/Quellen, Android-/WLAN-Systemeinstellungen, Power und vorhandene Home-Einrichtung. Kein sechster sichtbarer Sidebarpunkt. Die kompakte Hilfe steht in `contentDescription`.
- **Nuvio-Intent:** `VDesignLaunchPolicy.kt` validiert exakte explizite Nuvio MainActivity, Publisher-Paket/Aktion, Daten-/Selector-Freiheit. Für MainActivity-Start entsteht ein frisches Intent, das nur die dokumentierten skalaren Nuvio-Felder kopiert; UPCOMING bekommt ausnahmslos `launchMode=detail` ohne `videoId`; nur nachgewiesene In-Progress-/Available-`stream`-Intents mit echter Video-ID starten Playback, sonst ehrlicher Detail-/Unavailable-Fallback. Nuvios eigener Classic-Mischkanal und SmartTube-Content bleiben unberührt.
- **Tests/CI:** Neue JVM-Klassen `VDesignFocusNavigatorTest` und `VDesignLaunchPolicyTest` committed. Code-PR [Draft #38](https://github.com/Justify87/Plume_Launcher/pull/38), erster korrigierter Code-HEAD-Lauf [43528175980](https://github.com/Justify87/Plume_Launcher/actions/runs/43528175980) bei Dokumentation `in_progress`; keine erfundene grüne Abnahme. Lokales SDK/TV-/Shield-Testgerät nicht verfügbar (`not_run/hardware_pending`). Nach dem Evidence-Docs-Commit dessen neuen finalen CI-HEAD separat prüfen; dieser Snapshot bleibt nur historisch für den vorherigen Quell-HEAD.
- **Review:** [REVIEWS.md](REVIEWS.md) M05, [TEST_MATRIX.md](TEST_MATRIX.md) M05; **genau nächster Meilenstein M06**. Ohne Nutzerfreigabe bleibt [PR #38](https://github.com/Justify87/Plume_Launcher/pull/38) Draft und ungemergt.

### M05 CI-Finalisierung nach dem ersten Code-und-Docs-Commit

- **Unveränderlicher Code-und-Docs-HEAD** [`1e8a7cb5`](https://github.com/Justify87/Plume_Launcher/commit/1e8a7cb5e7d2c823a32e4069cfa96e1a71a11b0e): [Run 43528616572](https://github.com/Justify87/Plume_Launcher/actions/runs/43528616572), [Job check 132240568044](https://github.com/Justify87/Plume_Launcher/actions/runs/43528616572/job/132240568044) **completed/success**. `./gradlew testDebugUnitTest lintDebug`, `assembleDebug assembleRelease`, Repo-Regeln, Installer-/Spotify-/Mullvad-Offline-Tests bestanden.
- Im Check-Log tatsächlich gemessen: Release `545714 → 550334` Bytes (**+4620**). Bestehende Lint-Baseline beibehalten, keine neuen Permissions. [Native Mullvad Job 132240569945](https://github.com/Justify87/Plume_Launcher/actions/runs/43528616572/job/132240569945) **skipped** durch Branchfilter (keine Rust/JNI-Runtime-Aussage).
- Frühere Runs [43528093899](https://github.com/Justify87/Plume_Launcher/actions/runs/43528093899) und [43528175980](https://github.com/Justify87/Plume_Launcher/actions/runs/43528175980) **cancelled** durch nachfolgende Commits, nicht als Success zählen. Dieser CI-Evidenznachtrag erzeugt erneut einen SHA; dessen CI ist beim Schreiben noch **pending** und darf nicht voreilig als passed markiert werden.
- Physische Shield Pro 2019, Emulator, Live-Nuvio, VPN-Tunnel und Referenz-Pixelvergleich bleiben **not_run/hardware_pending**; gesamter Meilenstein weiterhin **partial** bis Geräte-Abnahme.

---

## Historische Vorstände (M00–M04; keine aktuellen PR-/CI-Behauptungen)

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
work_code_head_pre_docs: 3516fb0463747a72b84604e3b6b9490b0ceb33f8
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
m04_binder_unit_tests: committed_pending_final_ci
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


**M04 Binder-Test-Nachtrag vor finalem Docs-Commit:** `VDesignCardSlots.kt` bindet die 3/3/8 Plätze als reine, Android-freie Projektion, begrenzt Upcoming-Paging auf die tatsächliche Listenlänge und erhält volle Counts. `VDesignHomeView` nutzt die geprüfte Projektion. `VDesignCardSlotsTest.kt` deckt 1/0/1 echte Elemente, >8, letzte Position und Paginierungsgrenzen ab. [Code-Commit 3516fb](https://github.com/Justify87/Plume_Launcher/commit/3516fb0463747a72b84604e3b6b9490b0ceb33f8). **Testquellen hinzugefügt, Ausführung erst durch finalen CI-Run nachzuweisen.**
