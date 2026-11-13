# V-Design — kanonischer Handoff-State

```yaml
schema: plume-v-design-handoff/v1
checked_on: "2026-11-13"
repository: Justify87/Plume_Launcher
base_branch: main
main_head_observed: 59c7988d4b019c12647550095e24f8cf62aece33
nuvio_branch: dev
nuvio_head_observed: be4b84a84e004a2d36405601683df000d8dc446a
active_milestone: M01
active_status: partial_pending_pr_ci_and_shield
predecessor_milestone: M00
predecessor_pr: 33
predecessor_merged: true
predecessor_merge_sha: 59c7988d4b019c12647550095e24f8cf62aece33
m00_final_ci_run: 43509990732
m00_final_ci_result: check_success_mullvad_apk_skipped
work_branch: feature/vdesign-m01-nuvio-provider
work_head_sha_at_documentation: 9480a9417ec62e95909547372510151d8f3c29dc
current_pr: 34
current_pr_state: open_draft_not_merged
m01_local_unit_test: not_run_no_checkout_or_android_sdk
m01_test_code: added_pending_ci_execution
m01_android_build: not_run_local
m01_ci: pending_final_docs_head_checks
m01_shield: hardware_pending
reference_sha256: 95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25
reference_git_blob_sha: d1b14c00a6478cb3b49fcbbd637e0aa71db5df18
reference_dimensions: "1536x864"
reference_bytes: 249663
reference_format: JPEG_JFIF_under_png_extension
reference_verified_in_m01: true
nuvio_feeds_snapshot: isolated_not_rendered
next_milestone: M02_after_M01_review_and_CI
blockers:
  - "M01 PR #34 final CI outcome not yet observed"
  - "Physical Shield TV provider/approval/permission integration not tested"
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
