# Testmatrix und reproduzierbare Buildnachweise — M01

**Stand:** 2026-11-12. Externe/historische CI-Ergebnisse sind als `observed_ci_pass` markiert, **nicht** als eigener lokaler Test. `not_run` bedeutet, dass es **keinen** Exitcode gibt; es wird **nicht** als 0 interpretiert.

| ID | Test / konkreter Befehl | Umgebung / Beleg | Ergebnis | Exitcode / Größe |
| --- | --- | --- | --- | --- |
| T01 | Prüfen `main`, PRs, `README.md`, `CONTRIBUTING.md`, `AGENTS.md`, CI, Regeln | GitHub API, `main` SHA `ab9bcc6c5188789c952722974834b49fe7ebae05`; 0 offene PRs; AGENTS.md nicht vorhanden | pass | API erfolgreich |
| T02 | Historischer `./gradlew testDebugUnitTest lintDebug assembleDebug` | `debug-apk.yml`, GitHub Actions [Run 43100442410](https://github.com/Justify87/Plume_Launcher/actions/runs/43100442410), **genau main-SHA**; Job `Build and upload Advanced Debug APK`, erfolgreich | observed_ci_pass | Job success; APK-Größe nicht exakt aus Jobprotokoll ermittelt (Artifact-Upload ZIP 1,688,824 Bytes ist **nicht** rohe APK) |
| T03 | Historischer `./gradlew testDebugUnitTest assembleDebug lintDebug assembleRelease` + Plume-Regeln, Installer-Test, Spotify-Probe | `pr.yml`, [Run 43052315531](https://github.com/Justify87/Plume_Launcher/actions/runs/43052315531) auf `c51b78e98746310d9ed99f7c07ae32148d13bfda` (direkter Merge-Elterncommit) | observed_ci_pass | Job success; Release-APK **528066 Bytes**; Base **526382 Bytes**, +1684 (aus Joblog) |
| T04 | M01 neu auf Plume: `./gradlew testDebugUnitTest assembleDebug lintDebug assembleRelease` | Arbeitscontainer enthält **keinen** vollständigen Plume-Checkout und **kein** Android SDK; GitHub-DNS nicht erreichbar | not_run | **kein Exitcode**; nicht als M01-Lokalpass melden |
| T05 | Original Mullvad: `cd third_party/mullvadvpn-app/android && ../building/containerized-build.sh android` | Container hat keine Android SDK/NDK, keinen Rust/Cargo, keinen Podman-Buildcontainer/Clone | not_run | **kein Exitcode**, keine erzeugte Original-APK |
| T06 | Original Mullvad alternativ: `cd third_party/mullvadvpn-app/android && ./gradlew assembleOssProdDebug` | gleiche Toolchain-/Checkout-Blockade | not_run | **kein Exitcode**, Größe unbekannt |
| T07 | Original Mullvad auf Shield: Konto, System-VPN-Zustimmung, Login, Connect, DNS/Exit-IP, Reboot, Disconnect | kein mit ADB erreichbares Shield-Gerät / kein Konto im ausführenden Umfeld | not_run | **kein Exitcode** |
| T08 | Kombinierte Plume-Mullvad-APK (Build/Service/UI) | M01 enthält absichtlich **keine** Runtime | not_run | nicht Teil M01 |
| T09 | `git ls-remote https://github.com/mullvad/mullvadvpn-app.git refs/tags/android/2026.12` | Arbeitscontainer, 12.11.2026 | blocked_environment | **128**, `Could not resolve host: github.com`; Tag/Commit stattdessen GitHub-API geprüft |
| T10 | Java-/Android-/Rust-/ADB-Probe: `java -version`, `command -v cargo`, `command -v adb`, `echo $ANDROID_HOME` | Arbeitscontainer | partial | JDK **21.0.12.1** vorhanden; `ANDROID_HOME` unset; Cargo/ADB nicht vorhanden |
| T11 | M01-Submodul: `git ls-tree HEAD third_party/mullvadvpn-app`, `git -C ... rev-parse HEAD` | nach Feature-Branch-Commit und Clone lokal/CI nachzuholen; GitHub-Tree in M01 geprüft | not_run | CI/Checkout-Nachweis offen |
| T12 | M01-Dokumentation: fünf Dateien + `.gitmodules`, Gitlink SHA | GitHub Commit-Tree des Commits `580bccb...` und [PR #16](https://github.com/Justify87/Plume_Launcher/pull/16) geprüft; 7 hinzugefügte Einträge, Gitlink Mode 160000 mit erwarteter SHA | pass | GitHub API; PR-CI separat prüfen |

## Eigentliche Build-Baselines (wenn Umgebung verfügbar)

**Plume, unverändertes `main`** (nicht in vorhandenen Branch zurückresetten; frischer Checkout/Worktree):
```bash
git clone --branch main https://github.com/Justify87/Plume_Launcher.git plume-baseline
cd plume-baseline
git rev-parse HEAD
./gradlew testDebugUnitTest assembleDebug lintDebug assembleRelease
stat -c '%s' app/build/outputs/apk/{debug/app-debug.apk,release/app-release.apk}
sh tools/plume-install-test/run.sh
python3 -m unittest discover -s tools -p 'test_spotify_podcast_probe.py' -v
```
Voraussetzungen: Plume-README-Buildsetup / Java 17 entsprechend aktueller CI, SDK 36, Internet. Exitcode und Größe pro Artefakt erfassen. Die historischen CI-Logs sind **Vergleich**, kein Ersatz für neuen Build.

**Mullvad-Original, separat und unmodifiziert:**
```bash
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = "aa47f606b1a159214c7279344128119fa91be5fd"
cd third_party/mullvadvpn-app/android
../building/containerized-build.sh android
# optional ohne Container (JDK 21 + Rust/NDK/SDK 37):
./gradlew assembleOssProdDebug
```
Kommandos in genau getrennten Umgebungen ausführen; `dist-assets/relays/relays.json`, `Cargo.lock`, native ABI-`.so` und APK-Größe nachvollziehen. Offizielles GitHub-Release-Asset `MullvadVPN-2026.12.apk` hat **39,008,494 Bytes**, SHA-256 `0ad70b89b2b48bb0b1ae29115eee57d75277ffeccf95a0d3db98945d740a6d9e`. **Das ist kein in M01 gebauter APK.**

## Shield-Abnahme: M02+ (ADB nur auf verbundenem Gerät)

```bash
adb devices -l
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abilist
adb install -r <original-mullvad-debug.apk>
adb shell am start -n net.mullvad.mullvadvpn/net.mullvad.mullvadvpn.feature.appearance.impl.obfuscation.MainActivityAltDefault
adb logcat -c
# Konto/VPN-Zustimmung/Connect manuell auf TV vornehmen:
adb logcat -d -s MullvadVpnService MullvadDaemon AndroidRuntime
adb shell dumpsys connectivity
# Nach Freigabe später kombinierte Plume-APK separat installieren und
# Classic, Overview, Home, VPN/Back, Service trotz Activity-Wechsel, Neustart testen.
```

Erster Originaltest benötigt das **originale** `net.mullvad.mullvadvpn`-Paket und **eigenes gültiges** Mullvad-Konto, Test-IP/Exit über Netzwerk (nicht bloß `dumpsys`) sowie tatsächliche Fernbedienung. Das Original bietet eine exportierte Launcher-/Leanback-Activity-**Alias** im Modul `lib/feature/appicon/impl`; `app.MainActivity` selbst hat `exported=false` und darf daher **nicht** von ADB-Shell direkt als externe Komponente gestartet werden. Beim kombinierten APK diese Mullvad-Launcher-Aliasse per Manifest-Overlay entfernen/deaktivieren, damit Plume das einzige Launcher-Symbol bleibt. **Keine** tatsächlichen Shield-Tests in M01; alle `not_run`.

## Review und Ergebnisse

- Original-Mullvad in M01 unverändert; Gitlink fixiert und keine zwei APKs angelegt.
- Backend/UI/Lifecycle-Pfade konkret erfasst; **keine** TV-Fokusprüfung in M01, da kein TV-Code erstellt.
- Plume wird in M01 nicht verändert; bestehender Launcher-Build durch reine Handoff-Dateien/Submodul nicht bewusst verändert. Dies ist eine *Change-Scope*-Aussage, kein Regressionstest.
- Abnahme: Tag/SHA **pass**; Handoff/Architektur **pass** nach Commitprüfung; Plume-CI **observed_ci_pass**; neuer Plume-Lokalbuild **not_run**; echter Mullvad-Originalbuild **not_run**; Shield **not_run**; M01 gesamt **partial**.

## Folgeschritte

M02 hat zuerst Originalbuild/Plume-Baseline und integrierte Build-Pipeline nachzuweisen, danach nur minimale lokale Patches. Jede nachgeholte Messung mit Datum, Gerätename, Betriebssystem/API-Level, ABI, Befehl, Exitcode und Artifact-Größe eintragen. Alle anderen Featurezeilen in `FEATURE_MATRIX.md` bleiben `not_started`.


## M02 — Nachweise und offene Prüfungen (12.11.2026)

| Test / Ziel | Nachweis | Ergebnis |
| --- | --- | --- |
| M01-Gitlink / PR | PR #16, cbe1f46bd38ac6dfee5918b255cb4f26315b3e6d | `pass_observed`: offener gestapelter Vorgänger, SHA laut M01; keine neue lokale Cloneprüfung |
| Staging-/Host-Code | GitHub-Commit 9b1e630247696fea7b5164bca83b850bdcfe0bf5 | `partial`: echte Dateien/CI-Definition; nicht gebaut |
| SDK/NDK/Rust/ADB lokal | command -v rustc cargo sdkmanager adb | `blocked`: alle fehlen |
| Original Git CLI Netzwerk | git ls-remote https://github.com/Justify87/Plume_Launcher.git HEAD | `blocked`: Exit 128 / Could not resolve host: github.com |
| Python-Staging-Tests | python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v | `not_run`: CI vorgesehen |
| Plume M02-Baseline | ./gradlew :app:assembleDebug testDebugUnitTest lintDebug assembleRelease | `not_run`: nur historische M01-CI hatte Erfolg |
| Original Mullvad | cd third_party/mullvadvpn-app/android && ./gradlew assembleOssProdDebug | `not_run` |
| Integrierter Host | bash tools/mullvad/build-integrated.sh | `not_run`: lokales SDK/NDK/Rust fehlt; M02-PR-CI offen |
| APK / IDs / native Assets | python3 tools/mullvad/check_apk.py APK; aapt dump badging APK | `not_run`: keine APK vorhanden |
| Ein Launcher / zwei Application-Klassen | merged Manifest, DEX und aapt prüfen | `not_run` |
| Shield ABI, Installation | adb shell getprop ro.product.cpu.abilist; adb install -r APK | `not_run` |
| Plume Classic/Overview/D-Pad/Back, Home, Updates, Screensaver | Gerätetest | `not_run` |
| Dienst/Konto/VPN-Bindung | Gerätetest ab M03 | `not_run`; kein UI-Start/Mock in M02 |

**Reproduktion mit Toolchain (noch auszuführen):**

```bash
git checkout feature/m02-mullvad-apk-integration
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
bash tools/mullvad/build-integrated.sh
find .build/plume-mullvad/mullvadvpn-app/android/app/build/outputs/apk -name '*.apk'
python3 tools/mullvad/check_apk.py <integrated-debug.apk>
aapt dump badging <integrated-debug.apk> # applicationId dev.plume.advanced prüfen
adb devices -l
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abilist
adb install -r <integrated-debug.apk>
adb shell am start -n dev.plume.advanced/dev.plume.MainActivity
adb logcat -d -s AndroidRuntime MullvadVpnService MullvadDaemon
```

**M02-Abnahmestatus:** `blocked` bis erfolgreicher Build einschließlich tatsächlicher originaler Mullvad-Runtime, genau einer APK mit appId dev.plume.advanced, korrektem HOME/LEANBACK-Manifest, ARM64-SO, Relaydatei und vollständiger Build-/APK-Evidenz. Bis dahin keine Freigabe für M03.
