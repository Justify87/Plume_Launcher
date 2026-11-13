# Plume × Mullvad — maßgeblicher Übergabestand

```yaml
schema: plume-mullvad-handoff/v1
milestone: M11
status: partial
checked_on: "2026-11-13"
main_head_at_start: 056b1c5245982ee8fddfe581ee859735511aae14
predecessor_milestone: M10
predecessor_pr: 27
predecessor_merged_into_main: true
base_branch: main
feature_branch: feature/m11-mullvad-diagnostics-tv
upstream_tag: android/2026.12
upstream_sha: aa47f606b1a159214c7279344128119fa91be5fd
upstream_gitlink_unchanged: true
single_apk_application_id: dev.plume.advanced
m10_ci: observed_ci_pass
m10_ci_run: 43423552657
m11_ci: pending_new_head
m11_shield_test: hardware_pending
m11_local_android_build: not_run
m11_original_submodule_diff: none
next_milestone: M12
```

## Abgeschlossene Vorgänger / konkrete Referenzen

- M01–M03: Originalquelle angepinnt und ein integrierter Host mit Mullvad-Rust/JNI/gRPC; M02 CI 43314187927, M03 CI 43323559922. Keine Endgeräte-Funktionstests.
- M04–M06: Produktive VPN-Activity, Mullvad-Konto/Devices/Voucher und Relaylisten; CI M06 43339177759.
- M07: DNS, LAN, Start/Boot, WireGuard-Basiswerte: PR #22, CI 43343996225.
- M08: Split Tunneling mit Android-TV-App-Liste: PR #23, CI 43402989685.
- M09: Multihop, DAITA, PQ: PR #24; nachträglicher Review PR #26, CI 43412061826.
- M10: Verschleierung/API-Zugang/Server-IP: PR #27 in `main` `056b1c5245982ee8fddfe581ee859735511aae14`; CI 43423552657 `success`, ARM64-Debug-APK mit Rust/JNI/Relays. Hardware weiterhin nicht getestet.

## M11 Codepfade

- `tools/mullvad/VpnDiagnosticsUi.kt`: native Original-`ProblemReportRepository`-Logs/Export/Support, In-App-Notification-Controller, Version/Changelog, gespeicherte Notifications-Option, OS-App-Benachrichtigungsstatus und Sprache (API 33+; gemeinsame APK).
- `tools/mullvad/VpnActivity.kt`: achte VPN-Seite „Diagnose“ produktiv verdrahtet, Live-Account-/Daemon-Events, D-Pad-Fokus, M11-Lifecycle, originale Benachrichtigungs-/VPN-Zustimmungs-Aktion.
- `tools/mullvad/prepare.py`: M11-UI-Staging; **einziger notwendiger Originalkopie-Diff** `MAIN_ACTIVITY_CLASS` → `VpnActivity` in generiertem `lib/common/.../ClassNames.kt`. Original-Gitlink, Service, DAITA, Rust/JNI, gRPC unverändert. Dadurch öffnen QS- und Push-PendingIntents die native TV-Activity.
- `tools/test_mullvad_prepare.py`: bindungs-/stagingbezogene M11-Assertions, reproduzierbarer Pure-Python-Generatorcheck.

## Nachweis, Risiken, M12-Handoff

- `FEATURE_MATRIX.md`: alle F01–F48 mit Originalpfad, TV-Pfad, Status `implemented`/`hardware_pending`/`not_applicable` und Einzelevidenz; reine Phone-UI-/Play-Billing-Ausnahmen begründet.
- `TEST_MATRIX.md`: Kommandos und Fälle M11-01–12; aktueller Stand ist **keine M11-APK-Kompilation/keine Hardware**, bis ein entsprechender CI-Run tatsächlich verifiziert ist.
- **M12:** zuerst neuen M11-PR-CI-Status und Artefakt prüfen, alle Compile-/Stagingfehler beheben. Dann Shield 2019 Android-TV-ADB mit gültigem Mullvad-Konto: VPN nach Home/Back, F34–F37/42/45, QS-Tile/Notification-PendingIntents, API-33-Sprachverhalten und vollständige D-Pad-Navigation tatsächlich abnehmen. M01–M10 Hardwarelücken in `TEST_MATRIX.md` ebenfalls schließen. Kein `tested` vor gemessener Geräte-/Build-Evidenz.
