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
adb shell am start -n dev.plume.advanced/net.mullvad.mullvadvpn.app.VpnActivity
adb logcat -d -s AndroidRuntime MullvadVpnService MullvadDaemon
```

**Abnahme:** Quelle/Integration bis CI = `partial`; Singlehop-Exit-Any-Reset = `partial` wegen fehlender Upstream-Mutations-API; echte VPN-/D-Pad-/Relay-/Filter-/Listen-Prüfung = `not_run`. Der Stand ist erst dann `pass`, wenn APK-CI **und** physische Shield-Prüfung gegen Mullvads Originaldienst protokolliert sind.
