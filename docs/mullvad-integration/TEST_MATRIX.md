# Testmatrix und reproduzierbare Buildnachweise — M01

**Stand:** 2026-11-13 (M03-CI nachgeführt; ältere M01/M02-Tabellen behalten ihren historischen Kontext). Externe/historische CI-Ergebnisse sind als `observed_ci_pass` markiert, **nicht** als eigener lokaler Test. `not_run` bedeutet, dass es **keinen** Exitcode gibt; es wird **nicht** als 0 interpretiert.

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
| Python-Staging-Tests | python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v | `observed_ci_pass`: M03 Run 43323559922, Schritt „Test source staging generator“; kein lokaler Test |
| Plume M02-Baseline | ./gradlew :app:assembleDebug testDebugUnitTest lintDebug assembleRelease | `partial`: Unit-Tests und Lint in M03 Run 43323559922 erfolgreich; vollständiger genannter Baseline-Befehl nicht separat ausgeführt |
| Original Mullvad | cd third_party/mullvadvpn-app/android && ./gradlew assembleOssProdDebug | `not_run` |
| Integrierter Host | bash tools/mullvad/build-integrated.sh | `observed_ci_pass`: M02 Run 43314187927 und M03 Run 43323559922 mit originalem Container; lokal weiterhin nicht ausgeführt |
| APK / IDs / native Assets | python3 tools/mullvad/check_apk.py APK; aapt dump badging APK | `observed_ci_pass`: M03 Run 43323559922, inklusive ABI/JNI/Relay/Manifest/Package |
| Ein Launcher / zwei Application-Klassen | merged Manifest, DEX und aapt prüfen | `observed_ci_pass`: CI-Manifest-/DEX-Prüfungen, ein Plume-HOME-Einstieg und originaler Mullvad-Dienst |
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

**M02-Abnahmestatus (aktualisiert 13.11.2026):** Build- und APK-Strukturgate **`observed_ci_pass`** durch [Run 43314187927](https://github.com/Justify87/Plume_Launcher/actions/runs/43314187927), erneut in [M03 Run 43323559922](https://github.com/Justify87/Plume_Launcher/actions/runs/43323559922) bestätigt. Ein einzelnes Paket `dev.plume.advanced`, HOME/LEANBACK-Manifest, ARM64-JNI, Relay-Assets und originaler Mullvad-VpnService wurden geprüft. **Nicht blockiert für M03/M04-Weiterentwicklung**; jedoch Service-Laufzeit, Shield und echter Tunnel `not_run`.

## M02 workflow consolidation evidence (12 November 2026)

| Check | Evidence | Result |
| --- | --- | --- |
| Mullvad upstream Android build configuration | Pinned android-app.yml, building/container-run.sh and android/docker/Dockerfile | pass_observed for source inspection |
| Immutable original Mullvad commit and image digest | M01 SHA aa47f606... and image SHA256 ca22c11f... checked by reusable job | pass_observed; no submodule source changes |
| Shared APK build definition | debug-apk.yml workflow_dispatch + workflow_call; pr.yml delegates integrated job | implemented |
| Android build image pull | Actions run 43310027867 | pass_observed; reached original image-based Gradle build |
| Original Plume regression path | Separate PR check job, tests/lint/release/size | retained; current job was still running at observation |
| Combined installed APK / original JNI / manifest / package ID | Shared integrated job, check_apk.py, aapt badging and manifest, SHA256SUMS | `observed_ci_pass`: M02 Run 43314187927 und M03 Run 43323559922, Artifact vorhanden; Installation auf Shield `not_run` |
| Shield ABI, install, D-Pad, VPN account, tunnel and reboot | ADB on physical Shield | not_run |

CI build evidence is now documented above and below; actual device installation and tunnel remain outstanding. The manual debug workflow exposes integrated (default) and launcher-only; GitHub Actions manual dispatch UI requires the workflow to be present on the default branch.

## M03 — Build- und Geräteabnahme (12.11.2026)

### Verifizierte M02-Voraussetzung

- GitHub PR [#17](https://github.com/Justify87/Plume_Launcher/pull/17) wurde am 12.11.2026 in `feature/m01-mullvad-upstream-handoff` gemergt; Merge-SHA `fdc982a9a36053a3b6c5a3d190ae90d65ab6536c`.
- [CI Run 43314187927](https://github.com/Justify87/Plume_Launcher/actions/runs/43314187927), Commit `29869d381f35a5405fe5c3d10fabd29094d1233d`: Plume-Regeln/Unit Tests/Lint **success**, kombinierter Container-APK-Build und Paket-/VpnService-/JNI-/Asset-/Manifest-Prüfung **success**, Artifact `Plume-Mullvad-ARM64-Debug-54` (Artifact-Größe 120842117 Bytes; **nicht** als rohe APK-Größe interpretieren).
- M02 auf Hardware weiterhin `not_run`; ein erfolgreicher APK-Build beweist keine funktionsfähige VPN-Verbindung.

### M03-Pflichtprüfungen

| ID | Prüfschritt | Status bei Implementierung |
| --- | --- | --- |
| M03-01 | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` | `observed_ci_pass`; Run 43323559922, Staging-Tests |
| M03-02 | `./gradlew testDebugUnitTest lintDebug` (Plume-Baseline) | `observed_ci_pass`; Run 43323559922, Plume Unit Tests/Lint |
| M03-03 | `bash tools/mullvad/build-integrated.sh --no-configuration-cache --max-workers=2` | `observed_ci_pass`; Run 43323559922, Android-/Rust-/JNI-Containerbuild |
| M03-04 | `python3 tools/mullvad/check_apk.py <apk>`, `aapt dump badging`, `aapt dump xmltree` (eine `dev.plume.advanced` APK, Originaldienst und Debug-Harness) | `observed_ci_pass`; Run 43323559922, APK/Manifest/DEX/ABI/Assets |
| M03-05 | SDK-/ADB-Gerätetest: VPN-Dienst/JNI geladen, Konto, Erlaubnis, Tunnel und Exit-IP | `not_run`: kein angeschlossenes Shield-/Mullvad-Konto |
| M03-06 | System-VPN-Freigabe verweigern; kein Connect, Fehlermeldung | `not_run`: Shield |
| M03-07 | Wechsel in andere App, Netzwerk trennen/wiederherstellen; Tunnel-/Fehlerstatus korrekt | `not_run`: Shield |
| M03-08 | Classic und Overview: VPN-Knopf, D-Pad, OK, Back, reguläre Launcher-Funktionen | `not_run`: Shield |
| M03-09 | Release-Build enthält **keine** `PlumeVpnDebugActivity`, keine zusätzliche Application, kein zweites Launcher-Icon | `partial`: Debug-Harness im kombinierten APK verifiziert; separater Release-APK-Negativtest `not_run` |

### ADB-Checkliste (auf einer echten Shield, Android/ABI vorher feststellen)

```bash
adb devices -l
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abilist
adb install -r <Plume-Launcher-Advanced-Mullvad-arm64-debug.apk>
adb shell am start -n dev.plume.advanced/dev.plume.MainActivity
# VPN-Zugang über Top-Bar (Classic und Overview) mit D-Pad/OK öffnen;
# alternativ gezielter Activity-Start für Testzwecke:
adb shell am start -n dev.plume.advanced/net.mullvad.mullvadvpn.app.PlumeVpnDebugActivity
adb logcat -c
# eigenes gültiges Mullvad-Konto eingeben, Datenschutz bestätigen,
# VPN-Systemberechtigung zulassen, Connect abwarten:
adb logcat -d -s MullvadVpnService MullvadDaemon PlumeM03 AndroidRuntime
adb shell dumpsys connectivity
adb shell dumpsys activity services dev.plume.advanced
# Exit-IP und DNS/Tunnel-Funktion in einer tatsächlich über VPN laufenden App prüfen.
# Dann Android-Einstellungen/andere App öffnen und Status erneut kontrollieren:
adb shell am start -a android.settings.SETTINGS
# Zurück zu Plume, VPN öffnen, trennen und reconnect prüfen.
# Negativtest: Systemberechtigung auf frisch zurückgesetztem VPN-Profil ablehnen;
# Netz kurz deaktivieren; erwartete Status-/Fehlerevents protokollieren.
```

Die Debug-Test-Activity ist `exported=false`; ADB-Shell-Direktstarts können je nach Android-Version für nicht exportierte Komponenten abgelehnt werden. In dem Fall ausschließlich den sichtbaren Plume-VPN-Einstieg verwenden. Keine Testdaten oder Kontonummer in Logs/Dokumentation übernehmen.

### M03-Kompilierfehler erkannt und korrigiert

- M03 Run [43319030884](https://github.com/Justify87/Plume_Launcher/actions/runs/43319030884) auf `e1097a0`: `check` **pass_observed**, integrierter `:app:compileOssProdDebugKotlin` **fail** wegen zwei Deprecation-Compilerdiagnosen in `PlumeVpnDebugActivity.kt:70` (`systemUiVisibility` / `SYSTEM_UI_FLAG_LAYOUT_STABLE`). Dies betrifft **nicht** den originalen Dienst oder JNI.
- Behebung: unnötige System-UI-Zeile in `4599ad1148dc6747f5004511023cf2f7cfa73829` entfernt. Zur Absicherung prüft `debug-apk.yml` bei vorhandenem M03-Harness zusätzlich Manifest-Registrierung und kompilierten Activity-Namen in den APK-DEX-Dateien.
- Resultat des erneuten M03-Builds: [Run 43320664681](https://github.com/Justify87/Plume_Launcher/actions/runs/43320664681) **success**, ebenso [finaler Run 43323559922](https://github.com/Justify87/Plume_Launcher/actions/runs/43323559922) nach D-Pad-Fix. Build-Erfolg ist kein Shield- oder VPN-Laufzeittest.

### Review nach bestandenem CI und D-Pad-Folgefehler

- [M03 CI Run 43320664681](https://github.com/Justify87/Plume_Launcher/actions/runs/43320664681) am 12.11.2026: `check` **pass**, `Combined Plume + Mullvad ARM64 Debug APK` **pass**; originaler Gradle-/Rust-/JNI-Build `BUILD SUCCESSFUL in 12m 40s`, Paketprüfung meldet `APK structure OK (838 entries; Rust/JNI and relay assets present)`. GitHub-Artifact `Plume-Mullvad-ARM64-Debug-58`, Artifact-Größe **120883975 Bytes** (ZIP/Artifact, keine gemessene rohe APK-Dateigröße). Die CI prüfte auch die neue Debug-Activity im Manifest und DEX.
- Statischer Folgecheck: D-Pad-Rechtsgrenze war in `homeNav` (Classic) und `overviewNav` weiterhin an `shownTop().last()` gebunden und konnte den nachträglich hinzugefügten VPN-Knopf aussperren. `refreshVpnDebugEntry` entfernte/reinsertierte die View wiederholt, was den Fokus verlieren lassen konnte.
- Gezielter Fix `0ceae7e15e1cf4a90808218a1eec91354d0b4ebc`: rechter Rand wird aus dem tatsächlich sichtbaren VPN-Eintrag oder dem letzten originalen Symbol ermittelt; der VPN-Eintrag bleibt bei Top-Bar-Refresh angehängt und markiert `lastTop` beim Fokus.
- Kotlin-Warnungen im erfolgreichen Log: `Gallery.kt:259/260` nutzen expression body und return ohne expliziten Typ; laut Compiler **erst in Kotlin 2.5** ein Fehler. Der eingefrorene Upstream nutzt Kotlin 2.4.10; kein Anlass für ungefragte Migration/Refactoring in M03. Weitere harmlose Plume-Warnungen in `MainActivity.kt` 3269, 4453, 4561. **Kein** JVM-/Kotlin-weiter systematischer Ausfall im aktuellen Build nachgewiesen.
- CI nach Fokus-Fix: [Run 43323559922](https://github.com/Justify87/Plume_Launcher/actions/runs/43323559922), Commit `00eddf11f84c5418f911d9d61caacbd141881fdf`, **success** für `check` und `mullvad-apk`; Staging-, Gradle-, Rust/JNI-, Manifest- und DEX-Prüfung erfolgreich. Artifact `Plume-Mullvad-ARM64-Debug-60`, **120884259 Bytes** Artifact-Upload (nicht rohe APK). Physischer Classic-/Overview-D-Pad-Test, Systemdialog, Konto, Exit-IP und Tunnelstatus weiterhin `not_run`.

## M04 — neue Tests / Abnahme (13.11.2026)

| ID | Prüfung | Ergebnis / Beleg |
| --- | --- | --- |
| M04-01 | Quell- und Branchprüfung: M03 in `feature/m01-mullvad-upstream-handoff` bei `6c857ce`, Original-Gitlink gleich `aa47f606` | `pass`: GitHub API, PR #17/#18 merged in gestapelten Branch; `main` noch bei `0daf26b` |
| M04-02 | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` | `observed_ci_pass`: [Run 43328844657](https://github.com/Justify87/Plume_Launcher/actions/runs/43328844657), Staging-Tests; lokal `not_run` |
| M04-03 | `./gradlew testDebugUnitTest lintDebug`, `sh tools/plume-install-test/run.sh` | `observed_ci_pass`: Run 43328844657, `check` success (Plume Unit/Lint, Installer-, Spotify-, Repository-Regeln); lokal `not_run` |
| M04-04 | `bash tools/mullvad/build-integrated.sh` / Original-Container, `check_apk.py` | `observed_ci_pass`: Run 43328844657 `mullvad-apk`, ARM64 OSS/Prod Debug, Container, Kotlin/Rust/JNI, strukturelle APK-Prüfung, Artifact `Plume-Mullvad-ARM64-Debug-63` |
| M04-05 | APK-Manifest: `VpnActivity` vorhanden, `PlumeVpnDebugActivity` abwesend, ein HOME-/Leanback-Einstieg, MullvadVpnService und `libmullvad_jni.so` | `observed_ci_pass`: Run 43328844657, `aapt` Badging/Manifest, DEX-Nachweis, Python-APK-Archivcheck; keine Gerätemessung |
| M04-06 | Classic & Overview: D-Pad zum VPN-Icon/OK, Status, fünf Navigationseinträge, Back, Fokus-Rückgabe und Activity-Recreation | `not_run`, keine Shield-/Emulator-Verbindung |
| M04-07 | echte Konto-/Android-Permission-Abfrage, verweigerte Freigabe, Connect/Disconnect/Reconnect/Fehler | `not_run`, kein angeschlossenes Shield und keine Kontodaten |
| M04-08 | Exit-IP/Standort laut Mullvad und extern prüfen, App-Wechsel/Home und Resume ohne Tunneltrennung, Reboot | `not_run`, echte Hardware erforderlich |

**Geräteabnahme (erst nach erfolgreichem M04-CI):**
```bash
git checkout feature/m04-mullvad-tv-vpn-home
git submodule update --init --recursive
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
bash tools/mullvad/build-integrated.sh
adb devices -l
adb shell getprop ro.product.cpu.abilist
adb install -r <integrated-debug.apk>
adb shell am start -n dev.plume.advanced/dev.plume.MainActivity
# VPN-Icon per D-Pad in Classic und Overview öffnen. Konto/Permission/Connect auf TV.
adb logcat -d -s AndroidRuntime MullvadVpnService MullvadDaemon
adb shell dumpsys connectivity
# Zusätzlich in einer Browser-App auf dem TV tatsächliche Exit-IP prüfen,
# Back/Home/Appwechsel, Disconnect/Reconnect, gesperrte Permission, Neustart.
```

**Abnahme derzeit `partial`**: M04-Quellimplementierung **und CI-Build-/APK-Strukturnachweis `observed_ci_pass`** auf Code-Commit `389fe3a`. Shield-/Tunnel-/Exit-IP- und reale D-Pad-/Fokus-Hardwareevidenz sind weiterhin `not_run`; ohne Ausführung kein vollständiges `pass`. Artifact-Uploadgröße **120901828 Bytes** ist kein APK-Rohwert. Finaler Dokumentationscommit ändert keinen Buildcode.


## M05 — Konto, Geräte, Gutschein, Kontoaktionen (2026-11-13)

| ID | Prüfung | Aktuelles Ergebnis | Beleg/Grenze |
| --- | --- | --- | --- |
| M05-01 | `main`-HEAD, keine offenen PRs, M04 #19 gemergt, Mullvad-Stable erneut geprüft | `pass_observed` | GitHub API: `main` = `81017e4de5f24660fdfa45ef2c4734914bb0a796`; Stable `android/2026.12` |
| M05-02 | Staging-Unit-Tests inkl. neuer Account-View und Null-Originaldiff | `not_run` | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v`; GitHub CI nach PR |
| M05-03 | Plume-Regressionschecks | `not_run` | `./gradlew testDebugUnitTest lintDebug` und `sh tools/plume-install-test/run.sh`; GitHub CI nach PR |
| M05-04 | ARM64 OSS/Prod Kotlin/Rust/JNI-Integrationsbuild | `not_run` | `bash tools/mullvad/build-integrated.sh`; GitHub CI nach PR |
| M05-05 | APK-Paket, ein Launcher, Originalservice, JNI, Relay-Assets und neue Konto-UI im DEX | `not_run` | `python3 tools/mullvad/check_apk.py <apk>`; `aapt dump xmltree`, `dexdump`; Hardware noch nicht geprüft |
| M05-06 | D-Pad/OK/Back, TV-Tastatur in Classic/Overview, Fokus auf Login bzw. Konto und AlertDialog | `not_run` | Kein Android-TV-Gerät verbunden |
| M05-07 | Echte Anmeldung/Abmeldung, Kontoerstellung und Laufzeit, Prozessneustart | `not_run` | Kein Mullvad-Testkonto/Shield vorhanden; nie echte Kontonummer in Logs/PR |
| M05-08 | API offline/ungültige Kontonummer, Fehlertext und Wiederholung | `not_run` | Shield/Emulator; keine Credentials loggen |
| M05-09 | Gerätelimit, Gerät anzeigen, Entfernen, Widerruf und erneutes Login | `not_run` | Benötigt autorisierte Konten mit Geräten; Gerät umbenennen laut Original-API nicht verfügbar |
| M05-10 | Gutscheincode und Konto löschen inkl. doppelte Bestätigung, offizieller Web-Zeitkauf | `not_run` | Nur mit eigens freigegebenem Testgutschein/Testkonto; keine produktiven Konten löschen |

**Reproduktion (CI/SDK-Host):**

```bash
git checkout feature/m05-mullvad-accounts-tv
git submodule update --init --recursive
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
./gradlew testDebugUnitTest lintDebug
sh tools/plume-install-test/run.sh
bash tools/mullvad/build-integrated.sh
python3 tools/mullvad/check_apk.py <generated-ossProdDebug.apk>
```

**NVIDIA Shield Pro (2019) / Fernbedienung, sobald verfügbar:**

```bash
adb devices -l
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abilist
adb install -r <generated-ossProdDebug.apk>
adb shell am start -n dev.plume.advanced/dev.plume.MainActivity
# Classic und Overview: VPN-Icon → Konto → Konto-ID eingeben → Anmelden.
# Originalzustand/Gültig-bis anzeigen; neu starten und Status erneut aufrufen.
# Geräteliste laden, eigenes Gerät markieren; nur kontrollierte Testgeräte entfernen.
# Gutschein/Account-Delete nur über freigegebene TEST-Zugänge, nie produktive Konten.
# Offline-Modus, ungültige ID, Back/OK/DPAD-LEFT, TV-Keyboard und AlertDialogs prüfen.
# Anschließend VPN-Connect/Disconnect/Exit-IP testen; Home/Back darf Tunnel nicht stoppen.
adb logcat -d -s AndroidRuntime MullvadVpnService MullvadDaemon
adb shell dumpsys connectivity
```

M05 bleibt `partial` bis CI **und** Live-/Shield-Kontoabnahme tatsächlich nachgewiesen sind. Ein erfolgreiches Compiler-Gate wird später nur als `observed_ci_pass` ergänzt, nie als Ersatz für Geräteevidenz.


**M05 DI-Folgefehlerprüfung:** Source-Audit am gepinnten Upstream: `MullvadApplication` lädt nur `appModule`; originales `MainActivity` lädt `uiModule`/`paymentModule`. M05-VPN-Activity-Overlay lädt diese beiden Module vor `buildScreen()`. Test `test_m05_account_ui_uses_original_repositories` prüft den Modulaufruf; tatsächliche Runtime-/Koin-Auflösung bleibt `not_run` bis zum Geräte- oder Instrumentationstest.

## M06 — Build, Regression und Shield-Abnahme (13.11.2026)

Basis `main` `2c518bc3427c99d72820559deee5d7258791cee9`, Feature-Branch `feature/m06-mullvad-locations-tv`. M05 CI [43334096218](https://github.com/Justify87/Plume_Launcher/actions/runs/43334096218) zuvor `success` (Beobachtung, kein M06-Lauf).

| Prüfung | Kommando / Szenario | Ergebnis bei Codeerstellung |
| --- | --- | --- |
| Source-Staging-Assertion | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` | `not_run` (vor GitHub-CI) |
| Plume-Regression | `./gradlew testDebugUnitTest lintDebug assembleDebug` | `not_run` (kein SDK/Repo im lokalen Runtime-Container) |
| Integrierte ARM64-APK | `bash tools/mullvad/build-integrated.sh` | `not_run` (GitHub-CI nach PR-Anlage) |
| APK Manifest/JNI/Relays | `python3 tools/mullvad/check_apk.py <APK>` | `not_run` |
| Shield TV: Liste & Auswahl | `adb install -r <APK>`; VPN öffnen → Standorte → Land → Stadt → Server → Auswahl; Exit-IP über Original-Tunnel/externes IP-Check bestätigen | `not_run` |
| Suche/Offline | Suche nach Land/Stadt/Server, 0 Treffer, aktiv/inaktiv; Netzwerk trennen und Relayliste aktualisieren; Bildschirmtexte/kein Crash | `not_run` |
| TV D-Pad/Scroll | Classic + Overview VPN öffnen; D-Pad Links/Rechts/OK/Back; Länder-/Serverlisten jeweils 50+ Schritte; Fokus/Scrollposition bei Flow-Update prüfen | `not_run` |
| Originalfilter | Eigentümer Mullvad/gemietet/Alle; Anbieter einzeln/alle/keine, UI-Relays mit tatsächlichem Tunnel-Constraint abgleichen | `not_run` |
| Custom Lists | Liste erstellen, umbenennen, Land/Stadt/Hostname hinzufügen und entfernen, Liste als Standort wählen, löschen; Neustart/Neuverbinden | `not_run` |
| Recents | Zuletzt an/aus, mehrfach Standort wechseln, zuletzt verwendete Ziele auswählen, Neustart | `not_run` |
| Standortänderung verbunden | Erst VPN verbinden, Standort wählen, Status/Exit-IP und eventuelle originale Wiederverbindung beobachten (kein manuelles Reconnect aus M06) | `not_run` |

```bash
git checkout feature/m06-mullvad-locations-tv
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
./gradlew testDebugUnitTest lintDebug assembleDebug
bash tools/mullvad/build-integrated.sh
python3 tools/mullvad/check_apk.py <APK>
adb devices -l
adb install -r <APK>
adb shell monkey -p dev.plume.advanced -c android.intent.category.LEANBACK_LAUNCHER 1
# Anschließend per D-Pad in Plume den sichtbaren VPN-Zugang öffnen; die VPN-Activity ist absichtlich nicht exportiert.
adb logcat -d -s AndroidRuntime MullvadVpnService MullvadDaemon
```

**Abnahme:** Quelle/Integration bis CI = `partial`; Singlehop-Exit-Any-Reset = `partial` wegen fehlender Upstream-Mutations-API; echte VPN-/D-Pad-/Relay-/Filter-/Listen-Prüfung = `not_run`. Der Stand ist erst dann `pass`, wenn APK-CI **und** physische Shield-Prüfung gegen Mullvads Originaldienst protokolliert sind.

### M06 CI-Review: erste Korrektur

- [Run 43338169709](https://github.com/Justify87/Plume_Launcher/actions/runs/43338169709): `check=success` (Plume-Regeln, Installer, Spotify-Probe, Mullvad-Prepare-Python-Tests, Plume-Unit/Lint); `mullvad-apk=fail`, fünf Kotlin-Compilerfehler `VpnLocationUi.kt:145–149`, fehlendes `onClick` und Boolean/Lambda-Typkonflikt. Ursache ist allein die Parameterreihenfolge der `action`-View-Fabrik mit Trailing Lambda; Signatur/Argumente korrigiert, erneuter Build nötig. Kein Rust-/gRPC-/Mullvad-Sourcefehler beobachtet.
- ADB-Abnahmekommando auf exportierte Plume-Leanback-Start-Activity geändert; direkte `am start`-Anforderung an die nicht exportierte `VpnActivity` war unzulässig.

## M07 — DNS, LAN, Autoconnect, WireGuard-Basisoptionen (13.11.2026)

Ausgang: `main` `73f50f59751f27021737bdf8a4c1b325eba9e146`, M06 PR #21 gemergt. [M06-CI 43339177759](https://github.com/Justify87/Plume_Launcher/actions/runs/43339177759) **success** für M06-Code; **keine** M07-/Shield-Testergebnisse daraus ableiten.

| Testfall | Konkreter Nachweis | Aktueller Status |
| --- | --- | --- |
| M07-01 Staging/Quellassertions | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` (M07-Original-API und generierte View) | `observed_ci_pass` in Run 43343996225 |
| M07-02 Plume-Regression | `./gradlew testDebugUnitTest lintDebug`; `sh tools/plume-install-test/run.sh`. Separates `assembleDebug` lokal nicht ausgeführt | `observed_ci_pass` (Unit/Lint/Installer) in Run 43343996225 |
| M07-03 Kombinierte APK | `bash tools/mullvad/build-integrated.sh`, anschließend `python3 tools/mullvad/check_apk.py <APK>` | `observed_ci_pass` (839 APK-Einträge, JNI/Relays, Artifact) in Run 43343996225 |
| M07-04 TV-Fokus | Classic/Overview: sichtbares VPN → VPN-Einstellungen → DNS-Unteransicht, links/rechts/OK/Back, EditText mit Bildschirmtastatur, lange DNS-Liste scrollen | `not_run` |
| M07-05 DNS-Filter | sechs Filter einzeln + alle setzen und zurücksetzen; VPN-DNS-Abfragen/Blockverhalten; Wechsel zu Custom DNS und zurück | `not_run` |
| M07-06 DNS-Adressen | IPv4/IPv6 neu/ändern/löschen; fehlerhafte Eingabe und Duplikat; letzte Adresse entfernen; App/Service neustarten und persistierten Zustand abgleichen | `not_run` |
| M07-07 LAN/IPv6 | LAN-Freigabe Ein/Aus mit tatsächlichem lokalen Ziel; IPv6 Ein/Aus; Warnung bei lokalen/IPv6-DNS-Servern prüfen | `not_run` |
| M07-08 MTU/WireGuard | MTU 1280/1420, außerhalb des Bereichs abweisen, Standard; IPv4/IPv6/Auto; Post-Quanten-Ein/Aus, Fehlermeldungen bei widersprüchlichen Constraints | `not_run` |
| M07-09 Boot & Android OS | Boot-Toggle Ein/Aus, echtes Gerät neustarten; bei erteilter/nicht erteilter VPN-Freigabe; Always-on/Lockdown in Android-OS-UI prüfen, nie falschen Toggle-Wert anzeigen | `not_run` |
| M07-10 Netzwerk/Service-Rebind | Netzwerk wechseln, fremde App öffnen, Home/Back, VPN-Service-Rebind; Einstellungen und Tunnelstatus erneut lesen | `not_run` |

```bash
git switch feature/m07-mullvad-vpn-settings-tv
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
./gradlew testDebugUnitTest lintDebug assembleDebug
sh tools/plume-install-test/run.sh
bash tools/mullvad/build-integrated.sh
python3 tools/mullvad/check_apk.py <APK>
adb devices -l
adb shell getprop ro.product.cpu.abilist
adb install -r <APK>
adb shell monkey -p dev.plume.advanced -c android.intent.category.LEANBACK_LAUNCHER 1
# D-Pad in Classic + Overview: VPN → VPN-Einstellungen / DNS
# Alle Testfälle M07-04..10 mit Testkonto und echten Netzwerkzielen durchführen.
adb logcat -d -s AndroidRuntime MullvadVpnService MullvadDaemon
adb shell dumpsys connectivity
```

**CI-Nachweis:** [GitHub Run 43343996225](https://github.com/Justify87/Plume_Launcher/actions/runs/43343996225), Commit `f0409b2d34bee5267afe19d25b121543f98658d4`, `check=success`, `mullvad-apk=success`, `BUILD SUCCESSFUL in 10m 10s`, `APK structure OK (839 entries; Rust/JNI and relay assets present)`. Die CI prüft Quellgenerator/Regression und Single-APK-Artefakt, **keine** realen DNS-Anfragen, Tunnel-Exit-IP, System-Permission, Boot/Always-on oder D-Pad. M07-04–10 bleiben ohne angeschlossenes Shield `not_run`, vollständige Geräteabnahme `partial`.

**Review:** Erstes Layoutreview erkannte DNS-Button-Initialisierung/Offline-Einschränkung und beseitigte sie (`d2855c8`); zweites Review korrigierte versehentlich eingefügte zusätzliche `dns.apply`-Verschachtelung (`f0409b2`), deren CI vollständig erfolgreich war. Keine Original-Mullvad-Quelldatei verändert. Für den Handoff an M08 verbleibt der Original-Split-Tunneling-/App-Listen-Bereich; Boot-/DNS-Settings weiterverwenden.


## M08 — Split Tunneling (13.11.2026)

| Test | Nachweis | Ergebnis bei Implementierung |
| --- | --- | --- |
| M08-01 Generator | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` | `observed_ci_pass`: [Run 43402989685](https://github.com/Justify87/Plume_Launcher/actions/runs/43402989685) |
| M08-02 Plume-Regression | CI-Plume-Unit/Lint und Installer-Regression | `observed_ci_pass`: Run 43402989685; vollständiges separates `assembleDebug` lokal `not_run` |
| M08-03 ARM64-Mullvad-APK | Original-Container-Integrationsbuild, `check_apk.py`, Manifest/Checksum | `observed_ci_pass`: Run 43402989685; `BUILD SUCCESSFUL in 12m 34s`, 839 APK-Einträge, Artifact `Plume-Mullvad-ARM64-Debug-74` |
| M08-04 TV D-Pad | Classic/Overview VPN → Split Tunneling; OK/Back/Links, Tastatur-Suche, lange App-Liste | `hardware_pending` |
| M08-05 Persistenz | Zwei **tatsächlich installierte** TV-Apps auswählen, Ein/Aus, Prozessneustart, Regeln gleich | `hardware_pending` |
| M08-06 Routing | Mit und ohne VPN dieselbe Test-IP über **ausgeschlossene** und **eingeschlossene** App prüfen | `hardware_pending` |
| M08-07 Android-Lockdown | Always-on/Sperrmodus an/aus, gleiche Apps mit echter Netzverbindung prüfen | `hardware_pending` |
| M08-08 Paketänderung | TV-/Sideload-App install/uninstall, Refresh im offenen VPN-Menü und bei Resume; fehlende Regel entfernbar | `hardware_pending` |
| M08-09 UI-Vollständigkeit | Systemapps Ein/Aus, Suche ohne Treffer, Icons, >100 Apps Scroll und Fokus | `hardware_pending` |

```bash
git switch feature/m08-mullvad-split-tunneling-tv
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
./gradlew testDebugUnitTest lintDebug assembleDebug
sh tools/plume-install-test/run.sh
bash tools/mullvad/build-integrated.sh
python3 tools/mullvad/check_apk.py <APK>
adb devices -l
adb shell pm list packages
adb install -r <APK>
adb shell monkey -p dev.plume.advanced -c android.intent.category.LEANBACK_LAUNCHER 1
# Nur auf dem Gerät vorhandene Pakete auswählen; externe Exit-IP in beiden Apps vergleichen.
# Android-VPN-Sperrmodus separat an/aus; Prozessneustart, Install/Uninstall und D-Pad prüfen.
adb logcat -d -s AndroidRuntime MullvadVpnService MullvadDaemon
```

Geräteumgebung nicht verbunden. Keine Shield-/Routing-/Persistenz-Passes ohne Logs, Netzwerkbeleg und D-Pad-Prüfung.

**Nachtrag:** Review korrigiert M08-Handoff-Markdown und ergänzt sichtbaren EditText-Fokusrahmen. Für den finalen PR-HEAD muss der erneut gestartete CI-Lauf getrennt geprüft werden. Shield-Fälle M08-04–09 bleiben unverändert `hardware_pending`.

## M09 — Multihop, DAITA, PQ, Review und Hardwareabnahme (13.11.2026)

**Ausgang:** `main` `560ee47cffb1640af7ac9dd889b6441f78efc824`, M08 PR #23 gemergt, keine offenen PRs. Upstream-SHA `aa47f606b1a159214c7279344128119fa91be5fd` unverändert. Neue Plume-Views benötigen die originale Kotlin-/Rust-APK-CI; lokale Android-Toolchain und erreichbare Shield stehen dem GitHub-Connector-Kontext nicht zur Verfügung. Keine nicht ausgeführten Tests als `pass` ausgeben.

| M09-Test | Reproduktion/Nachweis | Stand |
| --- | --- | --- |
| Ursprungs-Pin und Gitlink | `git ls-tree HEAD third_party/mullvadvpn-app`; `git -C third_party/mullvadvpn-app rev-parse HEAD` | `partial`: GitHub-Quelle geprüft, kein kompletter lokaler Checkout |
| Original-API-Klassen und UI-Anbindung | Quellen gegen festen Tag `android/2026.12`, `VpnAdvancedUi`, `VpnLocationUi`, `VpnActivity`, `prepare.py` | `partial`: Quellprüfung, noch kein Gerätebeleg |
| Staging / Duplikatfreiheit | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` | `not_run` bis CI/neuer Checkout |
| Plume-Regression | `./gradlew testDebugUnitTest assembleDebug lintDebug assembleRelease`; `sh tools/plume-install-test/run.sh` | `not_run` bis CI/neuer Checkout |
| Ein-Plume-APK inkl. Rust/JNI/Relay-Assets | `bash tools/mullvad/build-integrated.sh`, `python3 tools/mullvad/check_apk.py <apk>` | `not_run` bis CI/neuer Checkout |
| Live: Multihop NEVER → WHEN_NEEDED → ALWAYS → NEVER | Android-TV-VPN-UI + bestätigte `wireguardConstraints.multihop`-States, jeweils wirklicher Tunnelstatus | `hardware_pending` |
| Live: Entry automatisch, Länder/Städte/Hosts, Exit, Filter | Standorte in „Erweitert“ wählen; gültig/ungültig/same host; DAITA-Constraints; keine erzwungene Neuverbindung | `hardware_pending` |
| Live: DAITA aus/an und wieder aus | Original-Relayfilter, Persistenz, Tunnel/Fehlerstatus nach Umschalten | `hardware_pending` |
| Live: Post-Quantum an/aus und wieder Standard | M09→M07 Navigation; `QuantumResistantState.On/Off` vom Daemon; reconnect/Tunnel | `hardware_pending` |
| Live: Migration und Netzwerkfehler | Bei tatsächlich vorhandener Upstream-Migrationsmeldung Originalstatus/Bestätigung; Offline-/Daemon-Ausfall | `hardware_pending` |
| Live: Classic/Overview D-Pad/OK/Back/Home/Neustart | Fokus/Rückkehr zu „Erweitert“, Unabhängigkeit der VPN-Verbindung von Plumes Home-Activity | `hardware_pending` |

**Reproduzierbarer Build-/Hardware-Block:**
```bash
git checkout feature/m09-mullvad-multihop-daita-tv
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
./gradlew testDebugUnitTest assembleDebug lintDebug assembleRelease
sh tools/plume-install-test/run.sh
bash tools/mullvad/build-integrated.sh
python3 tools/mullvad/check_apk.py <integrated-debug.apk>
adb devices -l
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abilist
adb install -r <integrated-debug.apk>
adb logcat -c
# Danach in Classic und Overview „VPN“ → „Erweitert“ per D-Pad öffnen,
# Original-Konto anmelden, Systemfreigabe bestätigen und alle Matrixfälle prüfen.
adb logcat -d -s MullvadVpnService MullvadDaemon AndroidRuntime
adb shell dumpsys connectivity
```

**Review-Entscheidung:** keine originalen Mullvad-Dateien oder VPN-Krypto verändert, ein einziges APK; originaler Relay-Filter und `ModifyMultihopUseCase` statt eigener Server-Paarung; M07-PQ-Writer wiederverwendet; keine Fake-Effektivzustände. CI-/Shield-Ergebnisse erst nach realen Runs ergänzen. M09 `partial` bis vollständiger Abnahme, M10 kann auf derselben Original-Runtime aufbauen.

### M09 zusätzlicher Build- und Review-Nachweis — nach Merge von PR #24

- Erste vollständige CI auf `0b3af65b302398fc175284fccad65e0f1eee6315`: [Run 43409181838](https://github.com/Justify87/Plume_Launcher/actions/runs/43409181838), `check=success`, `mullvad-apk=success`, `Legacy Plume-only debug APK=skipped` (workflowabhängig). Python-Staging, Plume-Regressionen, originale Kotlin/Rust/JNI-Apk und APK-Struktur damit **`observed_ci_pass`** für diesen Commit.
- Nachreview: D-Pad-Rail wechselt vom temporären Entry-/Exit-Browser zur normalen Singlehop-Ansicht; Daemon-Reconnect übernimmt aktuellen echten Tunnelstatus; Offline-Labels für Multihop/DAITA/PQ zeigen ohne laufenden Originaldienst keine bestätigte Aktivierung. Tests der Staging-Regeln um diese Bedingungen erweitert.
- **Neuer Review-Commit:** In Folgeprüfung den frischen CI-Status separat auswerten; `Run 43409181838` ist kein Buildnachweis für spätere Änderungen. Shield-Tests bleiben unabhängig davon `hardware_pending`.

**Post-Merge-Situation:** PR #24 wurde während der M09-Review-Arbeit nach `main` gemergt, bevor die nachträglichen UI-Korrekturen Teil des PRs waren. Deshalb werden diese isoliert auf `main` `0df3ead5749ad5a3f465029308c104e9babe67d1` als `feature/m09-mullvad-postmerge-ui-review` erneut getestet. `Run 43409181838` ist grün für den M09-Merge, **kein** finaler Buildbeleg für das Folge-Review.

**CI-Branchkonvention:** `.github/workflows/pr.yml` startet den integrierten `mullvad-apk`-Job nur bei PR-Head-Refs mit `mullvad` im Namen. Der erste Post-Merge-Review-PR #25 wurde deshalb vor der finalen Abnahme durch einen identischen Review-Branch mit `mullvad` im Namen ersetzt; **der in PR #25 übersprungene APK-Job ist kein Testpass**. Der neu eröffnete Review-PR wird vollständig geprüft.

## M10 — Netzwerkoptionen / nachzuweisende TV-Abnahme (13.11.2026)

Basis `main` `0df3ead5749ad5a3f465029308c104e9babe67d1`, gepinnter Mullvad-SHA `aa47f606b1a159214c7279344128119fa91be5fd`. M09-PR #24 CI [43409181838](https://github.com/Justify87/Plume_Launcher/actions/runs/43409181838) abgeschlossen mit `success`. M10-Quellstand `f938114fb80b3ae38ea068dc1904ca67b216ce1f` hat nach Korrektur [CI 43423552657](https://github.com/Justify87/Plume_Launcher/actions/runs/43423552657) mit `success` bestanden. Geräteabnahme bleibt `hardware_pending`.

| Testfall | Prüfschritte / Originalzustand | Stand |
| --- | --- | --- |
| Source-/Staging-Gates | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v`; Host enthält VpnNetworkUi, keine Submodulmutation | `observed_ci_pass`: Run 43423552657 |
| Plume Regressionen | `./gradlew testDebugUnitTest lintDebug`; `sh tools/plume-install-test/run.sh` | `observed_ci_pass`: Run 43423552657; eigenständige Launcher-assemble*-Jobs im Mullvad-PR bewusst übersprungen |
| Integrierter APK-Build | `bash tools/mullvad/build-integrated.sh --no-configuration-cache --max-workers=2`; `python3 tools/mullvad/check_apk.py <APK>` | `observed_ci_pass`: Run 43423552657, Kotlin/Rust/JNI, APK-Struktur mit 839 Einträgen |
| Obfuskation | Auf Shield alle sieben Modi Auto/Off/UDP→TCP/Shadowsocks/QUIC/LWO/WireguardPort schalten; `settingsUpdates`, Offline, Reconnect, Restart, Tunnelstatus/Fehler | `hardware_pending` |
| Ports | Automatisch, 80/443/5001, 51820/53, zulässige/ungültige eigene Ports, Relayportbereiche, Persistenz, Verbindungswechsel | `hardware_pending` |
| DAITA/Multihop-Konflikte | M09 ALWAYS/WHEN_NEEDED/NEVER, DAITA on/off kombinieren, Mullvad-Fehler/Relay-Effekt statt künstlichem Umschalten prüfen | `hardware_pending` |
| API-Zugang | Direkt/Brücken/EncryptedDNS; aktive/inaktive Methode, Testen, erfolgreich/fehlerhaft Wechseln, Offline-Neustart | `hardware_pending` |
| Eigene API-Methode | SOCKS5 mit/ohne Auth, Shadowsocks unterstützte Cipher, Add/Edit/Test/Enable/Disable/Delete; Name/IP/Port invalid, letzter aktiver Zugang | `hardware_pending` |
| Server-IP | Gültiger/ungültiger JSON-Text- und Dateiimport, Liste, Neustart, Reset/Abbruch, tatsächlicher Relay-Effekt | `hardware_pending` |
| TV/Launcher | Classic und Overview: sichtbarer VPN-Einstieg, Netzwerk-Unterseiten per D-Pad/OK/Back, Tastatur/Dateipicker, Home ohne Tunnelabbruch | `hardware_pending` |

**Reproduzierbare Abnahme (NVIDIA Shield):**
```bash
git checkout feature/m10-mullvad-network-access-tv
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
./gradlew testDebugUnitTest assembleDebug lintDebug assembleRelease
sh tools/plume-install-test/run.sh
bash tools/mullvad/build-integrated.sh
python3 tools/mullvad/check_apk.py <integrated-debug.apk>
adb devices -l
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abilist
adb install -r <integrated-debug.apk>
adb shell am start -n dev.plume.advanced/dev.plume.MainActivity
adb logcat -c
# In Classic und Overview VPN > Netzwerk; Originalkonto/VPN-Zustimmung.
# Alle obigen Szenarien durchspielen, reale IP/Relay-Wirkung separat belegen.
adb logcat -d -s AndroidRuntime MullvadVpnService MullvadDaemon
adb shell dumpsys connectivity
```

**Review:** Original Rust/JNI/Service unverändert, keine zweite Anwendung/Proxyengine, echte Repositories; kein reales Shield/ADB im Arbeitskontext. Ein grüner Build ohne Geräte-/Tunnelabnahme bleibt `partial`.

### M10 CI-Nachprüfung nach dem letzten Fehler

| Nachweis | Ergebnis / Ursache |
| --- | --- |
| [CI 43414769371](https://github.com/Justify87/Plume_Launcher/actions/runs/43414769371) auf `234d01739284` | `check=success`, `mullvad-apk=fail`; in `VpnActivity.kt:432` steht `ScrollView.LayoutParams` statt Androids `FrameLayout.LayoutParams` |
| CI-Workflow `.github/workflows/pr.yml` / `debug-apk.yml` | Source-Staging erfolgreich, Originalimage und Rust-/Kotlin-Module bis Hostkompilierung erfolgreich; Workflow selbst korrekt fail-fast und deshalb für diesen Fehler unverändert |
| M09-Basis seit PR #26 | Die auf `main` nachträglich gemergten D-Pad-/Offline-Korrekturen wurden mit M10 gemergt; M09-Runtimefunktion muss auf Shield trotzdem geprüft werden |
| M10 Re-Run / ARM64-APK / Shield | [Run 43423552657](https://github.com/Justify87/Plume_Launcher/actions/runs/43423552657) `success`, Artifact `Plume-Mullvad-ARM64-Debug-84`; Shield und echter Tunnel `hardware_pending` |

**Aktuelles CI-Gate:** [Run 43423552657](https://github.com/Justify87/Plume_Launcher/actions/runs/43423552657) für Commit `f938114fb80b3ae38ea068dc1904ca67b216ce1f`: Plume `check=success`, integriertes `mullvad-apk=success`, originaler Android-Build `BUILD SUCCESSFUL in 11m 42s`, APK-Prüfer `APK structure OK (839 entries; Rust/JNI and relay assets present)`; Artifact `Plume-Mullvad-ARM64-Debug-84`. Der finale Handoff-Dokumentationscommit ändert keine ausführbaren Dateien; diese CI-Evidenz gilt für den geprüften Codecommit. Shield bleibt ausdrücklich `hardware_pending`.

## M11 — Diagnose, Notifications, Abdeckung (13.11.2026)

Basis: `main` = `056b1c5245982ee8fddfe581ee859735511aae14` (M10 PR #27 gemergt), keine offenen PRs zu Beginn. Pin `aa47f606b1a159214c7279344128119fa91be5fd`. **Keine ADB-/Shield-Verbindung und kein lokaler vollständiger SDK/Rust-Checkout**. Die **M11-Code-Kompilation ist jetzt durch [CI 43431639278](https://github.com/Justify87/Plume_Launcher/actions/runs/43431639278) am Commit `48eee9561ff19338b61e066014e9d632b5f268a5` bewiesen**. Das ist keine Hardware-/Runtime-Abnahme.

| Test | Reproduktion | Status |
| --- | --- | --- |
| M11-01 Original-Repository-Verträge | `ProblemReportRepository`, `InAppNotificationController`, `UserPreferencesRepository`, `AppVersionInfoRepository`, `ChangelogRepository` und gepinntes `ClassNames.kt` gegen M11-Overlay | `reviewed_source` (kein Compilerpass) |
| M11-02 Generator/Quelltest | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` | `observed_ci_pass` in Jobs `check` und `mullvad-apk`, Run 43431639278 |
| M11-03 Launcherregression | `./gradlew testDebugUnitTest lintDebug`, `sh tools/plume-install-test/run.sh`, Spotify-Offline-Probe | `observed_ci_pass`: alle Schritte im `check`-Job von Run 43431639278; Legacy-Standalone-APK bewusst skipped |
| M11-04 ARM64-APK / 1 App | `bash tools/mullvad/build-integrated.sh`; `python3 tools/mullvad/check_apk.py <APK>` | `observed_ci_pass`: Run 43431639278, `BUILD SUCCESSFUL in 9m 30s`, 839 APK-Einträge, Original-Rust/JNI und Relay-Assets, Manifest/Paket/SHA256, Artifact `Plume-Mullvad-ARM64-Debug-89` (14039849900, 121357240 Byte ZIP) |
| M11-05 TV D-Pad und Back | Classic und Overview → VPN → alle 8 Seiten, Fokus, Back, lange Logs/Listen und Tastatur | `hardware_pending` |
| M11-06 Original-Notifications | Echt ausgelöster Tunnel-Offline-Fehler, Reconnect, ablaufendes Konto, Device-Revoked/NewDevice, Bannerreihenfolge, Systemmeldungen + Tap → native TV-Activity | `hardware_pending` |
| M11-07 Location-Notification | Toggle Ein/Aus; gespeicherter `UserPreferencesRepository`-Status nach Neustart; Android-System-Notifications erlaubt/verweigert | `hardware_pending` |
| M11-08 Logs + Export | Original-`collectLogs/readLogs`; Preview 160 letzte Zeilen; SAF-Export vollständig, Abbruch, fehlender DocumentProvider, Logging offline, keine Automatik | `hardware_pending` |
| M11-09 Problembericht | Mit/ohne E-Mail, mit/ohne Konto-ID, leere Beschreibung, Abbruch, Offline-Fehler, erfolgreiche Sendung nur mit Testkonto und Freigabe | `hardware_pending` |
| M11-10 Version/Changelog/Sprache | Upstream-Version, unsupported/Changelog, gelesen, Android-11-vs-13-Locale, gemeinsamer Plume-Kontext | `hardware_pending` |
| M11-11 Quick Settings | Falls Shield QS-Tile besitzt: originaler Service, Toggle, ohne Berechtigung direkte Plume-VpnActivity statt Compose-Screen | `hardware_pending` / Feature OS-abhängig |
| M11-12 Kernregressionen | VPN über Home/Back bestehen lassen, Boot, Split-Tunneling, Standort, DNS, DAITA, Obfuscation, API-Zugang, Offline, Revoke | `hardware_pending` |

```bash
git switch feature/m11-mullvad-diagnostics-tv
git submodule update --init --recursive
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
./gradlew testDebugUnitTest lintDebug
sh tools/plume-install-test/run.sh
python3 -m unittest discover -s tools -p test_spotify_podcast_probe.py -v
bash tools/mullvad/build-integrated.sh --no-configuration-cache --max-workers=2
python3 tools/mullvad/check_apk.py <integrated-debug.apk>
adb devices -l
adb shell getprop ro.build.version.sdk
adb install -r <integrated-debug.apk>
adb shell am start -n dev.plume.advanced/net.mullvad.mullvadvpn.app.VpnActivity
adb logcat -c
# VPN-Status, Konto, Meldungen, Sprache, Logs, Export und Report kontrolliert abnehmen.
adb logcat -d -s AndroidRuntime MullvadVpnService MullvadDaemon
adb shell dumpsys connectivity
```

**Review M11:** neue Diagnose ausschließlich Original-Repositories; höchstens 160 Logzeilen im TV-Text, vollständiger Export nur explizit; Original-PendingIntents im generierten Host zur TV-Activity. `check` und integrierter `mullvad-apk`-Job **beide success** am M11-Code-Commit. Integrations-/Gerätescreenshots, echtes Senden, OS-Tile-Abnahme nicht vorhanden. Deshalb M11 als Meilenstein **`partial` trotz CI `pass`**; keine Runtime-/Geräte-`pass`.

**M11 Navigationsreview-Fix:** Android restauriert dynamisch erzeugte Fokusziele ohne View-ID nicht automatisch. `VpnActivity.onCreate(savedInstanceState)` setzt deshalb den Menüfokus nach `showPage(restoredPage)` explizit auf `tabs[restoredPage]`; `test_mullvad_prepare.py` verlangt diese Quellverdrahtung. Physisches D-Pad-Fokusrestore bleibt `hardware_pending`.

**M11 Review-Fix Versionszustand:** `AppVersionInfoRepository.versionInfo` initialisiert `isSupported=true`, auch ohne erreichbaren Originaldienst. Die TV-UI zeigt solange `Supportstatus unbekannt (Dienst nicht erreichbar)` statt eines falschen Erfolgs. Der ursprüngliche Repositorywert bleibt unverändert; erst bei `GrpcConnectivityState.Ready` wird sein Supportflag ausgegeben. Source-Assertion ergänzt; CI und Shield-Abnahme noch erforderlich.

**M11 TV-Export-Fallback:** Manche Android-TV-Firmwares besitzen keinen SAF-Dateidialog. `CreateDocument.launch` fängt in der Diagnose-UI einen fehlenden/fehlerhaften Picker ab und zeigt statt eines Activity-Crashs eine Fehlermeldung; erfolgreiche Picker-Auswahl schreibt weiterhin alle Original-Mullvad-Logzeilen. Shield-Test bleibt `hardware_pending`.
