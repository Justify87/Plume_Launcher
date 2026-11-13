# V-Design — kanonischer Handoff-State

```yaml
schema: plume-v-design-handoff/v1
checked_on: "2026-11-13"
repository: Justify87/Plume_Launcher
base_branch: main
main_head_observed: 3d4abbdf4f3dbfb481e0a8a6bf95aea815a172bd
nuvio_branch: dev
nuvio_head_observed: be4b84a84e004a2d36405601683df000d8dc446a
active_milestone: M02
active_status: code_and_ci_passed_hardware_pending
predecessor_milestone: M01
predecessor_pr: 34
predecessor_merged: true
predecessor_merge_sha: 3d4abbdf4f3dbfb481e0a8a6bf95aea815a172bd
m01_final_ci_run: 43514084610
m01_final_ci_result: completed_success_check_success_mullvad_job_skipped
work_branch: feature/vdesign-m02-media-model
work_head_sha_at_documentation: 97ed765829d8e75dde9868925bd3cd7b607fff2b
current_pr: 35
current_pr_state: open_draft_not_merged
m02_local_unit_test: not_run_no_checkout_or_android_sdk
m02_android_build: ci_debug_release_success_run_43517654027_local_not_run
m02_ci_run_first: 43516551245
m02_ci_job_first: 132199847040
m02_ci_observed: success_run_43517654027_on_pre_evidence_head_97ed7658
m02_mullvad_apk_job: skipped_by_workflow_branch_condition
m02_final_code_ci_run: 43517654027
m02_final_code_ci_job: 132203743576
m02_final_code_ci_head: 97ed765829d8e75dde9868925bd3cd7b607fff2b
m02_final_code_ci_result: completed_success
m02_release_apk_before_bytes: 532270
m02_release_apk_after_bytes: 535462
m02_release_apk_delta_bytes: 3192
m02_docs_evidence_head_ci: pending_new_run
m02_shield: hardware_pending
m02_visual_comparison: not_run_no_ui_in_m02
reference_sha256: 95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25
reference_git_blob_sha: d1b14c00a6478cb3b49fcbbd637e0aa71db5df18
reference_dimensions: "1536x864"
reference_bytes: 249663
reference_format: JPEG_JFIF_under_png_extension
reference_verified_in_m02: true
nuvio_feeds_snapshot: three_sorted_deduplicated_not_rendered
next_milestone: M03_single_v_design_host_after_review
blockers:
  - "Hardware/TV provider visibility and launcher content-intents not actually tested"
  - "Latest evidence-only documentation commit requires independent follow-up CI verification"
  - "Physical Shield TV provider/browsability, content intents and VPN runtime not tested"
  - "M03 must bind civil-day countdown refresh on resume/rebind/day boundary and replace old UI"
```

## Baseline / tatsächliche Repository-Geschichte

- **Plume `main` beim Nachtrag:** `78666326d97c5692eaa49f652da77c9a16e5433c`. [PR #32](https://github.com/Justify87/Plume_Launcher/pull/32) wurde am 13.11.2026 gemergt, **obwohl sein Image-Gate offen war**. Historische Implementierungsbasis des PRs: `fbf2473711c50d03eb76afe8fd0a722af1faf136`.
- **Nuvio `dev`:** `be4b84a84e004a2d36405601683df000d8dc446a`, vier normale Preview Channels, klassischer `CONTINUE` bleibt Mixed-Feed.
- Mullvad-Dokumentation wurde nach `docs/archive/legacy-features/mullvad-vpn/` archiviert; `docs/mullvad-integration/STATE.md` existiert nicht mehr. Rust/JNI/VpnService und `dev.plume.advanced` bleiben unberührt.
- M00 ist Dokumentations- und Bildvertrag; **kein** Kotlin-/XML-/Mullvad-/Provider-Refactor und **kein** ausgeführter UI-Screenshotvergleich.

## Normatives Bild — jetzt direkt per Git-Blob identisch

- Der bereits vorhandene Upload `docs/v-design/Plume_V_Design_REFERENCE_original.png` wurde als **Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18`** gelesen; SHA-256 wurde unabhängig aus seinen über GitHub übertragenen Base64-Bytes berechnet: **`95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`**; Größe **249663 Bytes**; **1536×864**. Die SHA-Implementierung wurde gegen bekannte SHA-256-Testvektoren (`abc`, leerer String) geprüft.
- **Wichtig:** Die vom ZIP geerbte Dateiendung `.png` ist irreführend. Die unveränderten Originalbytes beginnen mit `FF D8 FF E0` (JPEG/JFIF). **Nicht konvertieren:** Das würde den SHA-Vertrag brechen. Dies ist eine Dokumentationsreferenz, kein Android-Drawable.
- Commit [`e1c13b2783aa632c2cbf391b10eb923426f8fc81`](https://github.com/Justify87/Plume_Launcher/commit/e1c13b2783aa632c2cbf391b10eb923426f8fc81) referenziert exakt **denselben** Git-Blob als `docs/v-design/REFERENCE.png` und entfernt nur den temporären Alias. Die Git-Binäridentität wird nach dem PR-Commit erneut geprüft.

## CI / Handoff

- **PR #32 CI korrekt eingeordnet:** Der [erste Run 43505553469](https://github.com/Justify87/Plume_Launcher/actions/runs/43505553469) wurde `cancelled`, aber der **[spätere Run 43507886182](https://github.com/Justify87/Plume_Launcher/actions/runs/43507886182)** auf dem PR-HEAD `b5d5cca75e250b32390b85ba7b30960973ea7b2d` ist `completed/success` (`check=success`, `mullvad-apk=skipped`). Das prüfte **nicht** die damals fehlende Referenz. Main Combined-Status lieferte `statuses: []`.
- **[Draft-PR #33](https://github.com/Justify87/Plume_Launcher/pull/33)** wurde tatsächlich erstellt (Basis `main@78666326…`, erster geprüfter PR-HEAD `28a2acc8…`); erster [PR-CI-Run 43509467207](https://github.com/Justify87/Plume_Launcher/actions/runs/43509467207) war beim Abruf `in_progress`, kein Pass. Der Metadaten-Nachtrag erzeugt einen neuen HEAD: finale CI für den tatsächlichen PR-HEAD erneut prüfen. Status bis dahin **`partial_pending_pr_ci`**.
- **M01**: Nuvio `IN_PROGRESS` / `AVAILABLE` / `UPCOMING` anhand Channel-`internal_provider_id` anbinden, Installationspaket/Provider-Berechtigungen real prüfen; keine SmartTube-Medien und keine Mischkanal-Aufteilung.
- Keine Merge-Aktion ohne neue ausdrückliche Nutzerfreigabe.


## Aktueller Meilenstein M01 — 13.11.2026

**M01 implementiert auf dem echten gemergten M00-main** `9480a9417ec6` als zuletzt geprüfter Code-HEAD (vor diesem Dokumentationscommit), Branch `feature/vdesign-m01-nuvio-provider`, [Draft-PR #34](https://github.com/Justify87/Plume_Launcher/pull/34); **kein Merge**. M00 #33 wurde am 13.11.2026 gemergt; M00-finaler [CI-Run 43509990732](https://github.com/Justify87/Plume_Launcher/actions/runs/43509990732) war `completed/success`, Job [check](https://github.com/Justify87/Plume_Launcher/actions/runs/43509990732/job/132177567483) `success`, Mullvad-Job `skipped` nach Branch-Bedingung. Frühere M00-Statuszeilen oben sind historische Erstbeobachtungen, **nicht** der aktuelle Gate-Stand.

Der reale Referenzblob wurde erneut aus GitHub-Base64 dekodiert, unabhängig SHA-256 berechnet (`95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25` = Referenzdatei), JPEG/JFIF-Signatur und 1536×864 verifiziert. Nuvio `dev@be4b84a` nur gelesen. Provider-IDs/Opt-in werden nun im vorhandenen TV-IO-Pfad getrennt gelesen; noch keine V-Design-Kacheln. `NuvioProviderFeeds` wird atomar auf die drei Gruppen aktualisiert, bei fehlender Permission geleert. Nur ausdrücklich browsable Channels werden gelesen. Für klassisches Watch Next bleibt ein In-Progress-Rückgriff mit positiver Position, **niemals** für Available/Upcoming. Alte Classic-/SmartTube-Anzeige ist in M01 weiter vorhanden und wird erst in späteren Meilensteinen visuell ersetzt.

**Noch nicht abgenommen:** CI auf dem endgültigen M01-PR-HEAD, Android TV 11 Provider-Opt-in in Laufzeit, Shield Pro 2019, visuelles Live-Screenshot- und Mullvad-Geräteverhalten. Keine realen Hardware-Erfolge behauptet. M02 darf nur auf dem realen M01-HEAD fortsetzen und übernimmt fachliche Datenqualität/Sortierung/Verknüpfung.


## M01 — finaler Code-CI-Nachtrag (13.11.2026)

- [PR #34](https://github.com/Justify87/Plume_Launcher/pull/34) (Draft, ungemergt). Erster [Run 43512433898](https://github.com/Justify87/Plume_Launcher/actions/runs/43512433898) auf `9480a941...` endete `failure` mit **2 fehlgeschlagenen Tests von 213**: `differentShowsWithSameEpisodeTitleAndGenericIntentRemainDistinct` und `classicMixedEntriesDoNotBecomeAvailableOrUpcoming`. Es handelte sich um echte M01-Dedupe-/Fallback-Regressionsbefunde, keine Gradle-Infrastrukturfehler. Code-Fix [30084ee9](https://github.com/Justify87/Plume_Launcher/commit/30084ee9fd630c805e799e4c2bec20416310bb05) vermeidet Serien-Kollision bei expliziten Serien-IDs und doppelte Legacy-Fallback-Sätze.
- [Korrigierter Run 43513105647](https://github.com/Justify87/Plume_Launcher/actions/runs/43513105647) auf Code-SHA `30084ee9fd630c805e799e4c2bec20416310bb05`: **completed/success**. [Job check 132188191911](https://github.com/Justify87/Plume_Launcher/actions/runs/43513105647/job/132188191911): `testDebugUnitTest lintDebug` **success**, `assembleDebug assembleRelease` **success**, APK-Größenvergleich **success**. Release-APK: **529422 → 532270 Bytes (+2848 Bytes)**. `mullvad-apk` in diesem PR nach Branch-Regel **skipped**, das ist **kein** ausgeführter Mullvad-Integrationstest. Lokale Gradle-/Hardware-Abnahme weiterhin `not_run`.
- Nach diesem Dokumentationscommit entsteht ein neuer PR-HEAD, dessen CI unabhängig zu prüfen ist; daher noch keine vorweggenommene finale CI-Behauptung. **M01 bleibt `partial_hardware_pending`** bis echter Android-TV-11-/Shield-Test des Package-/Provider-/Browsable-Pfads. Kein Merge erteilt. Nächster technischer Meilenstein **M02**.

 
## M02 — kanonischer Stand (2026-11-13)

M01 wurde tatsächlich als [PR #34](https://github.com/Justify87/Plume_Launcher/pull/34) am 13.11.2026 in `main@3d4abbdf4f3dbfb481e0a8a6bf95aea815a172bd` gemergt. Dessen finaler [Run 43514084610](https://github.com/Justify87/Plume_Launcher/actions/runs/43514084610) ist `completed/success`; die frühere Draft-/nicht-gemergt-Darstellung in historischen M01-Absätzen ist **überholt**. M02 beginnt exakt von diesem Main-HEAD. Nuvio `dev@be4b84a...` bleibt unverändert.

M02 implementiert `NuvioDashboardFeed.kt` (Android-freie, drei fixierte Reihen), `NuvioDashboardFeedTest.kt`, vier Metadatenfelder in `AndroidTvMediaSource.kt` und die tatsächliche Befüllung durch `MainActivity.queryWatchNext()`, ohne Classic-Darstellung/Deep-Links anzutasten. Exakte Priorität: engagierteste In-Progress-Episode → providergewichtete Available-Reihe → Upcoming nach realem lokalem Kalendertag. Episode-Key aus Nuvio-Provider-ID mit S/E; keine Deduplizierung per Name oder TV-`_id`. Kein Badging aus lokalisierten Texten, keine Restzeit bei Nuvios synthetischer 100-s-Dauer; unbekannte Werte bleiben null, echte Full-Feed-Counts bleiben erhalten.

Originaldatei `REFERENCE.png` wurde in M02 erneut als GitHub-Base64 gelesen und byteweise SHA-256-geprüft: **249663 Bytes, `95af66ad...`, JPEG/JFIF, 1536×864**, Ergebnis match. [Draft PR #35](https://github.com/Justify87/Plume_Launcher/pull/35), Code-HEAD zunächst `9003878bf5289d201bbf1027c9b08805292d766b`. Erster [CI-Run 43516551245](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245), [check-Job 132199847040](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245/job/132199847040), beim Review **in_progress**, kein CI-Pass behauptet. Lokale Gradle-/Android-Tests `not_run`, Shield `hardware_pending`. Die reale finale CI nach dem Dokumentationscommit separat abrufen. **Kein Merge erteilt.**

Historische M00-/M01-Abschnitte unten dokumentieren damalige Zwischenstände und sind nicht der aktuelle Handoff. **Exakt nächster Schritt M03**, erst nach Abschlussreview von M02.

 
### M02 erster echter CI-Fail und Follow-up

[Run 43516551245](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245) auf `9003878...` **completed/failure** ([check 132199847040](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245/job/132199847040)): `testDebugUnitTest` führte aus, `lintDebug` brach mit **1 neuem SuspiciousIndentation-Fehler in `MainActivity.kt:5773`** ab. Kein Problem durch ignorierte Tests oder geänderte Lint-Baseline versteckt. **Fix-Commit [834ae544](https://github.com/Justify87/Plume_Launcher/commit/834ae544b3d24a8963842e19a9562d6ce6af07f5)** rückt die Exception-Pfad-Reset-Zeile korrekt ein. Zusätzliches Logikreview identifizierte, dass die Classic-Parser-Prozentberechnung eine unmögliche `position > duration` auf 100 % klemmen kann; [Fix 6b740628](https://github.com/Justify87/Plume_Launcher/commit/6b740628d10e05d92f9c065ff38f42e6371c2cef) unterdrückt diesen Wert im V-Feed, [Test 1a057219](https://github.com/Justify87/Plume_Launcher/commit/1a057219e8afb355fb2e1c6f7cda9c0227e587a0) ergänzt den Regressionfall.

Die Folge-Runs `43517228539` und `43517300556` wurden durch das PR-Workflow-`cancel-in-progress` nach weiteren Commits **cancelled**, nicht als grün bewertet; der Run `43517409755` wurde ebenfalls durch den folgenden Testcommit abgebrochen. **CI für den endgültigen Commit nach diesem Dokumentationsnachtrag neu beobachten.** Gesamtstatus bleibt `partial_pending_final_ci_and_shield`.

 
### M02 — erfolgreicher CI-Abschluss auf dokumentiertem Implementierungsstand

Der [Run 43517654027](https://github.com/Justify87/Plume_Launcher/actions/runs/43517654027) auf exakt `97ed765829d8e75dde9868925bd3cd7b607fff2b` endete **completed/success**. [check Job 132203743576](https://github.com/Justify87/Plume_Launcher/actions/runs/43517654027/job/132203743576) führte Plume-Regeln, ADB Installer Regression (44 Checks, 0 Fehlchecks), Spotify (10 Python-Tests), Mullvad Staging (12 Python-Tests), Mullvad APK Validator (6 Python-Tests), Gradle `testDebugUnitTest lintDebug`, `assembleDebug assembleRelease` und APK-Vergleich tatsächlich aus; **alle erfolgreich**. Release-APK `532270 → 535462 Bytes`, **+3192 Bytes**. Native Mullvad-APK-Job `skipped` nach Workflow-Branchbedingung, keine VPN-Laufzeitabnahme. Lokale Gradle-Builds `not_run`; Shield Pro 2019 `hardware_pending`.

Dieser Dokumentationsnachtrag erzeugt einen **neuen Git-HEAD**, für den ein eigener CI-Lauf erst nach dem Push ausgewertet werden darf. Die beobachtete Erfolgsaussage bezieht sich ausdrücklich nur auf `97ed765...`. M02-Quellcode und vollständiger PR-Diff unverändert. Nächster Schritt M03, Merge nur nach Nutzerfreigabe.
