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
| T12 | M01-Dokumentation: fünf Dateien + `.gitmodules`, Gitlink SHA | GitHub Commit-Tree und PR nach Erstellung prüfen | pending | PR/Commit folgt |

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
adb shell am start -n net.mullvad.mullvadvpn/.app.MainActivity
adb logcat -c
# Konto/VPN-Zustimmung/Connect manuell auf TV vornehmen:
adb logcat -d -s MullvadVpnService MullvadDaemon AndroidRuntime
adb shell dumpsys connectivity
# Nach Freigabe später kombinierte Plume-APK separat installieren und
# Classic, Overview, Home, VPN/Back, Service trotz Activity-Wechsel, Neustart testen.
```

Erster Originaltest benötigt das **originale** `net.mullvad.mullvadvpn`-Paket und **eigenes gültiges** Mullvad-Konto, Test-IP/Exit über Netzwerk (nicht bloß `dumpsys`) sowie tatsächliche Fernbedienung. Falls das Android-Original kein Leanback-Startsymbol anbietet, per ADB starten; nicht aus fehlendem Launcher-Eintrag auf VPN-Fehler schließen. **Keine** tatsächlichen Shield-Tests in M01; alle `not_run`.

## Review und Ergebnisse

- Original-Mullvad in M01 unverändert; Gitlink fixiert und keine zwei APKs angelegt.
- Backend/UI/Lifecycle-Pfade konkret erfasst; **keine** TV-Fokusprüfung in M01, da kein TV-Code erstellt.
- Plume wird in M01 nicht verändert; bestehender Launcher-Build durch reine Handoff-Dateien/Submodul nicht bewusst verändert. Dies ist eine *Change-Scope*-Aussage, kein Regressionstest.
- Abnahme: Tag/SHA **pass**; Handoff/Architektur **pass** nach Commitprüfung; Plume-CI **observed_ci_pass**; neuer Plume-Lokalbuild **not_run**; echter Mullvad-Originalbuild **not_run**; Shield **not_run**; M01 gesamt **partial**.

## Folgeschritte

M02 hat zuerst Originalbuild/Plume-Baseline und integrierte Build-Pipeline nachzuweisen, danach nur minimale lokale Patches. Jede nachgeholte Messung mit Datum, Gerätename, Betriebssystem/API-Level, ABI, Befehl, Exitcode und Artifact-Größe eintragen. Alle anderen Featurezeilen in `FEATURE_MATRIX.md` bleiben `not_started`.
