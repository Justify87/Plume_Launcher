# V-Design — kanonischer Handoff-State

```yaml
schema: plume-v-design-handoff/v1
checked_on: "2026-11-13"
repository: Justify87/Plume_Launcher
base_branch: main
main_head_observed: 78666326d97c5692eaa49f652da77c9a16e5433c
nuvio_branch: dev
nuvio_head_observed: be4b84a84e004a2d36405601683df000d8dc446a
active_milestone: M00
active_status: partial_pending_pr_ci
predecessor_milestone: null
predecessor_pr: null
predecessor_merged: not_applicable
completed_bootstrap_pr: 32
completed_bootstrap_merged: true
completed_bootstrap_merge_sha: 78666326d97c5692eaa49f652da77c9a16e5433c
work_branch: feature/vdesign-m00-reference-completion
reference_commit_observed: e1c13b2783aa632c2cbf391b10eb923426f8fc81
work_head_sha_observed: 28a2acc8aefb5b1e9610a49ea12b62baf970bc35
work_head_note: "Last observed documentation commit before PR metadata update; actual live HEAD is in PR #33"
current_pr: 33
current_pr_state: open_draft
local_tests: passed_sha256_header_dimensions_and_git_blob_identity
android_build: not_run_no_local_android_sdk_or_checkout
ci: pending_final_pr_head_checks
ci_first_run_id: 43509467207
ci_first_run_initial_status: in_progress_not_success
ci_first_run_url: https://github.com/Justify87/Plume_Launcher/actions/runs/43509467207
ci_previous_m00_first_run: "43505553469 cancelled"
ci_previous_m00_final_run: "43507886182 success (check=success, mullvad-apk=skipped; image gate absent then)"
shield: hardware_pending
visual_comparison: not_run_no_launcher_screenshot
reference_sha256: 95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25
reference_git_blob_sha: d1b14c00a6478cb3b49fcbbd637e0aa71db5df18
reference_present_in_repo: true
reference_path: docs/v-design/REFERENCE.png
reference_encoding: JPEG_JFIF_under_original_png_filename
next_milestone: M01_after_M00_CI_review
blockers:
  - "PR #33 CI on final commit not yet confirmed successful"
  - "No local Android SDK/checkout or Shield runtime evidence; Shield acceptance belongs to M08"
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
