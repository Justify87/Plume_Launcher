# V-Design — kanonischer Handoff-State

```yaml
schema: plume-v-design-handoff/v1
checked_on: "2026-11-13"
repository: Justify87/Plume_Launcher
base_branch: main
main_head_observed: fbf2473711c50d03eb76afe8fd0a722af1faf136
nuvio_branch: dev
nuvio_head_observed: be4b84a84e004a2d36405601683df000d8dc446a
active_milestone: M00
active_status: partial
predecessor_milestone: null
predecessor_pr: null
predecessor_merged: not_applicable
predecessor_head_sha: null
work_branch: feature/vdesign-m00-bootstrap-contract
work_head_sha_observed: c8b02415e8aa8a79bd9428db6e0b2737942d1c47
work_head_note: "Last confirmed implementation commit before metadata commit; live HEAD always check PR #32"
current_pr: 32
current_pr_state: open_draft
local_tests: passed_local_zip_sha_image_source_review_only
android_build: not_run_no_checkout_sdk_network
ci: in_progress_observed_not_success
ci_run_url: https://github.com/Justify87/Plume_Launcher/actions/runs/43505553469
shield: hardware_pending
visual_comparison: not_run_no_app_screen
reference_sha256: 95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25
reference_present_in_repo: false
next_milestone: M00_reference_binary_completion_then_M01
blockers:
  - "Original REFERENCE.png is readable from user ZIP locally but cannot be uploaded as binary by available GitHub connector"
  - "No local cloned Android project / Android SDK / physical Shield evidence"
```

## Baseline und Abhängigkeiten

- **Plume main:** `fbf2473711c50d03eb76afe8fd0a722af1faf136` — letzter Merge archivierte Mullvad-Docs; kein vorhandener V-Design-Vorgänger-PR, keine offenen Plume-PRs bei Abfrage.
- **Nuvio dev:** `be4b84a84e004a2d36405601683df000d8dc446a` — #23 neue drei optionale Preview-Kanäle gemergt, klassischer `CONTINUE` bleibt.
- Mullvad M12 PR #29 ist gemergt (`9efbfc1e...`), echte Submodul-Originalintegration und Plume-Paket `dev.plume.advanced` bewahren. Historischer Status: `docs/archive/legacy-features/mullvad-vpn/STATE.md`, nach Archivierung **nicht** `docs/mullvad-integration/STATE.md`.
- M00 verändert **keine** Kotlin-/XML-/Rust-/VPN-/Gradle-Datei. Keine produktive UI-Arbeit vor M01.

## M00 Ergebnis und nächster Zugriff

**Teilweise umgesetzt:** Contracts/Source-Inventur/Review/Testmatrix im PR-Zweig. Originalbinärbild SHA lokal korrekt, jedoch **nicht im Git-Commit**; M00-Gate bleibt offen. Nach echtem Commit/PR CI-Daten ergänzen, niemals `passed` durch Annahme. M01 beginnt erst nach verifiziertem SHA-gleiches `REFERENCE.png` und Freigabe des gemergten/gestapelten M00-Standes.

## PR / CI Evidence nach GitHub-Write

- Dokumentations-Implementierungscommit: `c8b02415e8aa8a79bd9428db6e0b2737942d1c47` (GitHub `feature/vdesign-m00-bootstrap-contract`). GitHub-Vergleich gegen Baseline: 1 Commit, 0 behind, **genau zehn neue `docs/v-design/*`-Textdateien**, keine Source-/Mullvad-Diffs.
- **Draft-PR:** [#32](https://github.com/Justify87/Plume_Launcher/pull/32), offen, ungemergt; GitHub meldet mergeable, **keine Mergefreigabe**.
- **PR-CI:** [Run 43505553469](https://github.com/Justify87/Plume_Launcher/actions/runs/43505553469), beim ersten Abruf `in_progress`, `conclusion=null`; nicht als grün markiert. Nach Abschluss explizit nachprüfen.
- Wegen des Git-Selbstbezugs bildet `work_head_sha_observed` den *vorherigen bestätigten* Dokumentationscommit ab; dieser reine Metadaten-Nachtrag hat einen zusätzlichen SHA, der über [PR #32](https://github.com/Justify87/Plume_Launcher/pull/32/commits) überprüfbar ist.
