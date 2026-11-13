# Plume × Mullvad — maßgeblicher Übergabestand

```yaml
schema: plume-mullvad-handoff/v1
milestone: M12
status: partial
checked_on: '2026-11-13'
main_head_at_start: 2efdcbe7b9bfbba6a1c3918025cd413053fdbd46
predecessor_milestone: M11
predecessor_pr: 28
predecessor_merged_into_main: true
base_branch: main
feature_branch: feature/m12-mullvad-regression-release
upstream_tag: android/2026.12
upstream_sha: aa47f606b1a159214c7279344128119fa91be5fd
upstream_gitlink_unchanged: true
single_apk_application_id: dev.plume.advanced
integrated_min_sdk: 28
supported_abi: arm64-v8a
m11_ci: observed_ci_pass
m11_ci_run: 43433569189
m12_ci: in_progress_not_yet_confirmed
m12_release_gate: pending_ci
m12_shield: hardware_pending
m12_local_android_build: not_run_no_sdk_or_checkout
m12_original_submodule_diff: none
m12_release_signing: unsigned_upstream_by_design
m12_pr: 29
next_step: validate_pr_ci_then_physical_shield
```

## Tatsächliche Vorgänger und Architektur
- M01–M03 Original-Mullvad-Commit als Submodul, originaler Rust/JNI/gRPC/Daemon/VpnService in **einer** APK.
- M04–M06 eigenständige D-Pad-VPN-Activity in Plume Classic/Overview, Konto, Relaylisten und benutzerdefinierte Listen.
- M07–M10 DNS/LAN/Boot, Split Tunneling, Multihop/DAITA/PQ, Obfuskation/API-Zugang/Overrides.
- M11 Diagnose, Notifications, Version/Locale; PR #28 in main gemergt. Letzter [M11-CI 43433569189](https://github.com/Justify87/Plume_Launcher/actions/runs/43433569189) `check=success`, `mullvad-apk=success`. KEIN echter Shield-/Tunneltest.
- Keine neue Änderung im Mullvad-Gitlink/Original. Weiterhin ausschließlich regenerierbare `ossProd`-TV-Overlays und die M11-`ClassNames.kt`-PendingIntent-Umleitung.

## M12-Gates (vor PR-CI)
| Gate | Ehrlicher Status |
| --- | --- |
| Plume-Regressionschecks, Tests, lint, Debug/Release, Größenbaseline | not_run; GitHub-PR-Job vorgesehen |
| Native Original-Runtime ARM64: Debug und R8-Release + Manifest/JNI/Relays | not_run; GitHub-PR-Job vorgesehen |
| APK-Validator + Offline-Negativtests | not_run; CI vorbereitet |
| F01–F48 Funktionszuordnung / Quellreview | reviewed, Hardware `hardware_pending` |
| Release-Key/Update | Upstream-Release **unsigned**, gleiches Plume-Signaturzertifikat nötig |
| Reale Shield, VPN-Lifecycle, D-Pad, Konto, OS und Boot | hardware_pending |
| Roh-APK-Größe, Launcher-Startzeit, RAM VPN aus/verbunden | not_measured |

## Handoff / Pfade
- `.github/workflows/pr.yml`, `.github/workflows/debug-apk.yml`: M12-Plume-Builds und native Debug+Release-Gates; `tools/mullvad/check_apk.py` und `tools/test_mullvad_apk.py`: ARM64-ELF/Relay-JSON plus Offline-Tests.
- `UPSTREAM.md` dokumentiert die Originaldifferenz, `FEATURE_MATRIX.md` F01–F48 und offene Tests; `TEST_MATRIX.md` enthält die vollständige Shield- und Metrik-Abnahme; `RELEASE.md` Installation/Update.
- PR-CI auswerten und beobachtete Resultate/Größen nachtragen, Fehler korrigieren; danach physische Shield Pro 2019 mit gültigem Konto. M12 bleibt **partial** bis echter Geräteabnahme.

## M12 ABI-Binärreview und nachgemessene Baseline

- M11-Debug aus CI 43433569189 / Artifact 14043766791: **121321933 Bytes**, SHA256 `8bedd39977a829a255eec333ef08aea9faae8d3bcc2ee497dcc3596977c8f2f5`, 839 Einträge, ARM64-ELF-JNI (e_machine 183) und gültige Relay-JSON.
- Systematischer Fehler: zusätzliche AndroidX-Shared-Libraries für ARM32/x86/x86_64 ohne Mullvad-JNI; `aapt` bewarb deshalb falsche CPU-Kompatibilität. M12 behebt dies **nur im generierten Build** mittels `abiFilters += "arm64-v8a"` und prüft es für Debug+Release; Originalquellen weiterhin unverändert.
- Aktueller PR: [#29](https://github.com/Justify87/Plume_Launcher/pull/29). Ergebnisse der nachfolgenden CI-Neustarts erst bei beobachtetem Abschluss als pass eintragen. Bis Shield-/Live-Tunneltest weiterhin `partial`.
