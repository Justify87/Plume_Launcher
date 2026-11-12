# Plume × Mullvad — verbindlicher Übergabestand

```yaml
schema: plume-mullvad-handoff/v1
milestone: M05
status: partial
checked_on: "2026-11-13"
main_head_at_start: 81017e4de5f24660fdfa45ef2c4734914bb0a796
predecessor_milestone: M04
predecessor_pr: 19
predecessor_merged_into_main: true
base_branch: main
feature_branch: feature/m05-mullvad-accounts-tv
upstream_tag: android/2026.12
upstream_sha: aa47f606b1a159214c7279344128119fa91be5fd
upstream_gitlink_unchanged: true
single_apk_application_id: dev.plume.advanced
m04_integrated_ci: observed_ci_pass
m04_ci_run: 43328844657
m05_source: implemented
m05_ci: not_run
m05_shield_test: not_run
m05_live_account_test: not_run
m05_pr: pending
```

## M05 — tatsächliche Basis

- Der GitHub-Stand von `main` beim Start ist `81017e4de5f24660fdfa45ef2c4734914bb0a796`, Merge von PR #19 inklusive M02–M04. Keine offenen PRs bei der initialen GitHub-Abfrage; M05 baut direkt darauf.
- Letzter stabiler Mullvad-Android-Release am 13.11.2026 laut GitHub Releases: `android/2026.12` (30.09.2026). `android/2026.13-beta1` (10.11.2026) ist prerelease; Submodul-Pin bleibt ausdrücklich `aa47f606b1a159214c7279344128119fa91be5fd`.
- M04: originale `MullvadApplication`/`MullvadVpnService`/`ManagementService`/`ConnectionProxy` im generierten Host, native `VpnActivity`, sichtbarer VPN-Zugang in Classic und Overview; [CI 43328844657](https://github.com/Justify87/Plume_Launcher/actions/runs/43328844657) erfolgreich. Echte Shield-/Tunnel-/Fokusprüfungen weiterhin **nicht ausgeführt**.

## M05 — Implementierung und Grenzen

- `tools/mullvad/VpnAccountUi.kt`: eigene 10-Fuß-Kontoansicht mit Mullvads originalen `AccountRepository`, `DeviceRepository`, `VoucherRepository` und `NewDeviceRepository`. Kontoerstellung, Anmeldung, Konto-/Ablaufanzeige, Aktualisieren, Abmeldung, Geräteübersicht/-entfernung inkl. Gerätelimit, Gutschein und bestätigte endgültige Kontolöschung. Fehler werden auf verständliche Zustände abgebildet; keine Testkonten, Mock-Daten oder neu erfundenen REST-Endpunkte.
- `tools/mullvad/VpnActivity.kt`: Einbindung in vorhandene Konto-Seite und laufende Original-Flows (`ManagementService.deviceState`, `AccountRepository.accountData`); dynamischer D-Pad-Fokus auf Anmeldung oder Kontoaktionen; Rückkehr/Neustart holt Live-Daten.
- `tools/mullvad/prepare.py`: kopiert neue Konto-View gemeinsam mit `VpnActivity` in das **temporäre** `ossProd`-Host-SourceSet. Gitlink, originale Mullvad-Dateien, Dienst und JNI bleiben unverändert.
- Zeit hinzufügen: der originale `AccountRepository.getWebsiteAuthToken()` plus `createAccountUri()` öffnet `https://mullvad.net/account` mit Mullvads bestehendem Mechanismus; ohne Browser verständlicher Hinweis. Keine Play-Billing-Implementierung im OSS-Build.
- Gerät umbenennen: im gepinnten Android-`DeviceRepository`/`ManagementService` und den Login-/ManageDevices-ViewModels **kein** solcher Repository-Vorgang; deshalb bewusst nicht als funktionierend ausgegeben. Andere Dritt-App-private Kontodaten werden nicht gelesen.
- Tests: source/staging Unit-Tests ergänzt, integrierte Kotlin-/Rust-APK-CI und echte Shield-/Online-Kontotests müssen nach PR-Anlage erfolgen. Der Arbeitscontainer hat keinen GitHub-Clone-DNS-Zugang und kein Android SDK/NDK/ADB; **kein lokaler Gradle-/Shield-Pass behauptet**.

## Reproduktion

```bash
git checkout feature/m05-mullvad-accounts-tv
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
./gradlew testDebugUnitTest lintDebug
bash tools/mullvad/build-integrated.sh
python3 tools/mullvad/check_apk.py <APK>
adb devices -l
adb install -r <APK>
```

## Handoff M06

M06 übernimmt genau diese `feature/m05-mullvad-accounts-tv`-Implementierung nach Merge (oder als korrekt gestapelten Branch), behält den Upstream-Pin und verbindet `Standorte` mit originaler Mullvad-Relay-Auswahl. Die `Konto`-Seite darf nicht durch eine zweite Auth-/Device-Logik ersetzt werden. Vor vollständiger Parität zuerst den realen Kontologin, Kontoablauf, VPN-Freigabe, Tunnel sowie Fernbedienung in Classic/Overview auf NVIDIA Shield prüfen. Genaue Hardwarefälle in `TEST_MATRIX.md`.
