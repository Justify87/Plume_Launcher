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

## M02 workflow consolidation evidence (12 November 2026)

| Check | Evidence | Result |
| --- | --- | --- |
| Mullvad upstream Android build configuration | Pinned android-app.yml, building/container-run.sh and android/docker/Dockerfile | pass_observed for source inspection |
| Immutable original Mullvad commit and image digest | M01 SHA aa47f606... and image SHA256 ca22c11f... checked by reusable job | pass_observed; no submodule source changes |
| Shared APK build definition | debug-apk.yml workflow_dispatch + workflow_call; pr.yml delegates integrated job | implemented |
| Android build image pull | Actions run 43310027867 | pass_observed; reached original image-based Gradle build |
| Original Plume regression path | Separate PR check job, tests/lint/release/size | retained; current job was still running at observation |
| Combined installed APK / original JNI / manifest / package ID | Shared integrated job, check_apk.py, aapt badging and manifest, SHA256SUMS | pending at observation; no confirmed artifact yet |
| Shield ABI, install, D-Pad, VPN account, tunnel and reboot | ADB on physical Shield | not_run |

When CI completes, record the real job conclusion, specific failure (if any), package ID, single APK/ARM64 JNI/relay manifest and actual artifact size. The manual debug workflow exposes integrated (default) and launcher-only; GitHub Actions manual dispatch UI requires the updated workflow to be present on default branch.

## M03 — Build- und Geräteabnahme (12.11.2026)

### Verifizierte M02-Voraussetzung

- GitHub PR [#17](https://github.com/Justify87/Plume_Launcher/pull/17) wurde am 12.11.2026 in `feature/m01-mullvad-upstream-handoff` gemergt; Merge-SHA `fdc982a9a36053a3b6c5a3d190ae90d65ab6536c`.
- [CI Run 43314187927](https://github.com/Justify87/Plume_Launcher/actions/runs/43314187927), Commit `29869d381f35a5405fe5c3d10fabd29094d1233d`: Plume-Regeln/Unit Tests/Lint **success**, kombinierter Container-APK-Build und Paket-/VpnService-/JNI-/Asset-/Manifest-Prüfung **success**, Artifact `Plume-Mullvad-ARM64-Debug-54` (Artifact-Größe 120842117 Bytes; **nicht** als rohe APK-Größe interpretieren).
- M02 auf Hardware weiterhin `not_run`; ein erfolgreicher APK-Build beweist keine funktionsfähige VPN-Verbindung.

### M03-Pflichtprüfungen

| ID | Prüfschritt | Status bei Implementierung |
| --- | --- | --- |
| M03-01 | `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` | `not_run`; PR-CI vorgesehen |
| M03-02 | `./gradlew testDebugUnitTest lintDebug` (Plume-Baseline) | `not_run`; PR-CI vorgesehen |
| M03-03 | `bash tools/mullvad/build-integrated.sh --no-configuration-cache --max-workers=2` | `not_run`; PR-CI vorgesehen |
| M03-04 | `python3 tools/mullvad/check_apk.py <apk>`, `aapt dump badging`, `aapt dump xmltree` (eine `dev.plume.advanced` APK, Originaldienst und Debug-Harness) | `not_run`; CI vorgesehen |
| M03-05 | SDK-/ADB-Gerätetest: VPN-Dienst/JNI geladen, Konto, Erlaubnis, Tunnel und Exit-IP | `not_run`: kein angeschlossenes Shield-/Mullvad-Konto |
| M03-06 | System-VPN-Freigabe verweigern; kein Connect, Fehlermeldung | `not_run`: Shield |
| M03-07 | Wechsel in andere App, Netzwerk trennen/wiederherstellen; Tunnel-/Fehlerstatus korrekt | `not_run`: Shield |
| M03-08 | Classic und Overview: VPN-Knopf, D-Pad, OK, Back, reguläre Launcher-Funktionen | `not_run`: Shield |
| M03-09 | Release-Build enthält **keine** `PlumeVpnDebugActivity`, keine zusätzliche Application, kein zweites Launcher-Icon | `not_run`: strukturell aus Quell-Staging abgeleitet, APK-Nachweis offen |

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
- Resultat des erneuten M03-Builds beim Anlegen dieser Dokumentation noch **pending**. Keine ARM64-/VPN-/Shield-Laufzeitfreigabe aus diesem Fehler oder der Reparatur ableiten.

### Review nach bestandenem CI und D-Pad-Folgefehler

- [M03 CI Run 43320664681](https://github.com/Justify87/Plume_Launcher/actions/runs/43320664681) am 12.11.2026: `check` **pass**, `Combined Plume + Mullvad ARM64 Debug APK` **pass**; originaler Gradle-/Rust-/JNI-Build `BUILD SUCCESSFUL in 12m 40s`, Paketprüfung meldet `APK structure OK (838 entries; Rust/JNI and relay assets present)`. GitHub-Artifact `Plume-Mullvad-ARM64-Debug-58`, Artifact-Größe **120883975 Bytes** (ZIP/Artifact, keine gemessene rohe APK-Dateigröße). Die CI prüfte auch die neue Debug-Activity im Manifest und DEX.
- Statischer Folgecheck: D-Pad-Rechtsgrenze war in `homeNav` (Classic) und `overviewNav` weiterhin an `shownTop().last()` gebunden und konnte den nachträglich hinzugefügten VPN-Knopf aussperren. `refreshVpnDebugEntry` entfernte/reinsertierte die View wiederholt, was den Fokus verlieren lassen konnte.
- Gezielter Fix `0ceae7e15e1cf4a90808218a1eec91354d0b4ebc`: rechter Rand wird aus dem tatsächlich sichtbaren VPN-Eintrag oder dem letzten originalen Symbol ermittelt; der VPN-Eintrag bleibt bei Top-Bar-Refresh angehängt und markiert `lastTop` beim Fokus.
- Kotlin-Warnungen im erfolgreichen Log: `Gallery.kt:259/260` nutzen expression body und return ohne expliziten Typ; laut Compiler **erst in Kotlin 2.5** ein Fehler. Der eingefrorene Upstream nutzt Kotlin 2.4.10; kein Anlass für ungefragte Migration/Refactoring in M03. Weitere harmlose Plume-Warnungen in `MainActivity.kt` 3269, 4453, 4561. **Kein** JVM-/Kotlin-weiter systematischer Ausfall im aktuellen Build nachgewiesen.
- Neuer CI-Lauf nach Fokus-Fix: zunächst `pending`. Physischer Classic-/Overview-D-Pad-Test, Kontodialog und Tunnelstatus weiterhin `not_run`.
