# Plume Launcher Advanced – Refactoring PROGRESS

**Status:** M0 **PASS**, PR #51 von Nutzerauftrag aus gemergt als `a438073d3a6150d16efbfbc017df4910fb5f516f`; abschließende M0-CI [Run 43936635601](https://github.com/Justify87/Plume_Launcher/actions/runs/43936635601) **SUCCESS**. M1 im eigenen Branch in Bearbeitung, **CI PENDING**; M2–M7 NOT_STARTED.

**Baseline:** `main` @ `e3c1a3eb78c85bb471d9012dd700b6efd81b44ce`. **Branch:** `refactor/m0-inventory-androidtest-20261116`. **Pull Request:** [#51](https://github.com/Justify87/Plume_Launcher/pull/51). **Letzter erfolgreich getesteter Code- und Quellreview-HEAD vor Dokumentationsabschluss:** `ffd18b0b5c9279fc228f4c4465357ccc084a652c`. **CI:** [Run 43881462010](https://github.com/Justify87/Plume_Launcher/actions/runs/43881462010) = `completed / success`. Aktuellen HEAD und neueren Status stets in den [PR Checks](https://github.com/Justify87/Plume_Launcher/pull/51/checks) prüfen.

## M0 – Erreichte Aufgaben

- **FILE_TASKS:** 123/123 eigene Code-/Konfigurationsdateien vollständig geprüft und pro Datei eine eigene Entscheidung, Befund, Abhängigkeiten, State-/Lifecycle-Hinweise, Milestone und Tests dokumentiert. **15 Integrations-, 5 Instrumentierungs-, 66 Quellcode- und 37 Test-Reviews.** `MainActivity.kt` vollständig in 21 lückenlosen Abschnitten. **117 KEEP, 5 REPLACE, 1 EXTRACT (nur spätere Phasen).**
- **Tree-Check:** historischer `main`-Baum 323 reguläre Dateien = 123 Code-/Konfigurations-Tasks + 200 andere Dateien; 0 doppelte und 0 fehlende Einträge. Blob-SHAs mit tatsächlichem PR-Tree abgeglichen. Zusätzlich nur 3 neue Refactoring-Markdown-Dateien. Das externe Mullvad-Submodul bleibt auf `aa47f606b1a159214c7279344128119fa91be5fd`.
- **ARCHITECTURE_MAP:** A01–A08, aktuelle Zustandsbesitzer/Call-Chains, Thread-/Lifecycle-Risiken und für spätere Moves zu erfüllende Tests. Spätere Extraktionen erst nach M1-Owner-Gate, keine vorgezogene Umsetzung.
- **Geänderte Dateien:** F001 `.github/workflows/ci-integrated.yml` (AndroidTest-Kotlin-Kompilation zusätzlich zu bestehenden Gates); F008 `ActivityTest.kt` (korrekte isolierte Testzahl), F009 `AppPicturesCheck.kt` (aktuelles All-Apps-Grid/`renderAllAppsPane`, Icon/Reset und `ImageView.colorFilter`), F010 `GalleryPreviewTest.kt` (echte Paketkennung via ComponentName), F012 `SettingsTransferCheck.kt` (bestehende supported-Keys/Restore-Policy). Produktivcode, UI, Assets, Build-/Signing-/Releaseverträge und Mullvad-Service unverändert.

## Konkrete Tests auf geprüften Commit `ffd18b0b5c9279fc228f4c4465357ccc084a652c`

| Prüfkommando bzw. Workflow-Schritt | Ergebnis / Nachweis |
|---|---|
| Original-Mullvad-Pin / Buildimage, bestehende PR-Regeln | **PASS** |
| `sh tools/plume-install-test/run.sh` | **PASS** |
| `python3 -m unittest discover -s tools -p 'test_spotify_podcast_probe.py' -v` | **PASS** |
| `./gradlew testDebugUnitTest lintDebug assembleDebug compileDebugAndroidTestKotlin` | **PASS** |
| Mullvad-Staging-/Controller-/APK-Validator-Unittests | **PASS** |
| `bash tools/mullvad/build-integrated.sh --no-configuration-cache --max-workers=2 :app:assembleOssProdRelease` | **PASS**, ARM64 Debug plus unsigned Release/R8 |
| Integrierte APK-/Paket-/Dienst-/ABI-/Manifest-Verifikation | **PASS** |
| AndroidTest **ausführen** auf Emulator oder Shield | **NOT_TESTED** (nur kompiliert) |
| Echtes Shield Pro / VPN-/D-Pad-Hardwareverhalten | **NOT_TESTED** |

Alle **PASS**-Aussagen beziehen sich auf [den tatsächlich erfolgreichen CI-Lauf](https://github.com/Justify87/Plume_Launcher/actions/runs/43881462010) und **genau diesen SHA**. Ein CI-Lauf auf dem anschließenden reinen Dokumentations-Commit ist zusätzlich einzusehen, bevor der PR freigegeben wird.

## Kritischer eigener Zweitreview

1. PR-Änderungen begrenzt auf 4 AndroidTest-Dateien, bestehenden CI-Workflow und 3 M0-Dokumente. Kein Produktivcode, kein Security-/Feature-/UI-/Release-Workstream, keine neuen Abstraktionen und kein zweiter VPN-Pfad.
2. Veraltete `appRow`-/`renderApps`-/Banner-Menüannahmen ersetzt, ohne die real verbleibende Banner-Bildfit-/Uploadserver-Regression zu deaktivieren. `ActivityTest`, Galerie-Paket- und SettingsTransfer-Testannahmen gegen aktive Verträge korrigiert. Alle fünf Test-/CI-Dateien auf erfolgreichem SHA kompiliert/geprüft.
3. Mechanisch entdeckte veraltete Blob-SHAs für F008/F010/F012 und deren irreführende `KEEP`-Entscheidung berichtigt. Bestätigte `KEEP`-Quellen bleiben unverändert, F026 `EXTRACT` erst in M1–M5.
4. **Nicht getestet:** echte Geräteszenarien und VPN-Tunnel; `NOT_TESTED`, kein fingierter PASS. Der Zweitreview ist ein **eigener**, kein unabhängiger externer Review.

## Gate und nächster Schritt

**M0: PASS für nachgewiesene Quell-/Datei-Reviews und den grünen Code-/Integrations-CI-Lauf.** Nach diesem Dokumentations-Commit die CI auf dem finalen PR-HEAD erneut prüfen; bei Fehlschlag denselben PR korrigieren. **Nicht selbst mergen.** Erst *nach* Maintainer-Merge auf neuem `main` mit M1 State-/Lifecycle-Gate beginnen.

---

## M1 – State/Lifecycle/Interface, 16.11.2026

**Neue Baseline:** main @ `a438073d3a6150d16efbfbc017df4910fb5f516f` (M0 Merge #51), mit vollständigem M0-CI-PASS auf `00011adbf63b48f4e5479f1fa9a5dd04e0b65d19`. **Branch:** `refactor/m1-state-lifecycle-contracts-20261116`. **PR:** erst nach diesem überprüfbaren Commit erstellen. **Milestone-Gate:** CI und finaler Diff-Review auf PR-HEAD noch offen.

### Real erledigt und den Tasks zugeordnet

- **A02/A03/A04/A05/A06; F015/F017/F025/F026/F027/F029/F034/F044/F047/F049/F099/F101/F111:** in `ARCHITECTURE_MAP.md` konkrete aktuelle State-/Lifecycle-Besitzer, Kanten zwischen io-/posterIo-Worker und UI-Handler, onCreate/onResume/onPause/onStop/onDestroy-Reihenfolge sowie begrenzte M2–M6-Move-Kandidaten dokumentiert. **KEIN** neuer produktiver Owner/Thread/Service.
- **F068:** `LauncherWorkspaceNavigationTest.kt` erweitert um zwei deterministische Charakterisierungen zu Content→Rail→Home, Destination-Wechsel, Focus-Key-Verfall und leerer Content-Fokus-Anforderung.
- **F069:** `MediaFeedRepositoryTest.kt` erweitert um zwei deterministische Charakterisierungen zu defensiver Source-/Alias-Kopie, altem Snapshot und unabhängigen Feeds. Kein Produktionscodeänderung.
- **FILE_TASKS:** weiterhin **123/123** eigene Code-/Konfig-Datei-Tasks, 0 fehlende/duplizierte Pfade, 0 Blob-SHA-Differenzen beim Branch-Abgleich. Nach beiden testbezogenen Änderungen kumulativ **115 KEEP, 7 REPLACE, 1 EXTRACT** (EXTRACT für F026 bleibt für M2–M5 vorgemerkt; M1 verschiebt keinen Code).
- **Bewusst KEEP:** F025 und F044 als bereits einzelner Navigation-State-Owner, F027 als separater Source-Snapshot-Owner, F017/F034 als Persistenzgrenzen, F047/F049 als Surface-/Socket-Owner, F099/F101 als verschiedene Consent-/Lifecycle-Owner, F111 Staging.

### Konkrete Testmatrix für den M1-Branch

| Kommando / Workflow-Schritt | Aktueller Status |
|---|---|
| `./gradlew testDebugUnitTest lintDebug assembleDebug compileDebugAndroidTestKotlin` | **PENDING** – PR-CI auf endgültigem SHA |
| `sh tools/plume-install-test/run.sh` | **PENDING** – PR-CI |
| Spotify-Python-Unit-Tests, Mullvad-Staging/Controller/APK-Tests | **PENDING** – PR-CI |
| `bash tools/mullvad/build-integrated.sh --no-configuration-cache --max-workers=2 :app:assembleOssProdRelease`; ARM64-/R8-/APK-Check | **PENDING** – PR-CI |
| AndroidTest-Ausführung, Emulator/Shield Pro, echter VPN-Tunnel | **NOT_TESTED** |
| GitHub-Compare `main…M1-Branch` | **PASS** – nur zwei JVM-Test-Dateien und Audit-Dokumentation; keine Produktions-/Submodul-/Build-Änderung |

**Kritischer eigener Zweitreview (nicht extern unabhängig):** Tests prüfen vorhandene Policy-/Snapshot-Contracts und ändern keine UI-Pfade. M1 dokumentiert Eigentums-/Shutdown-Reihenfolgen statt neue State-Duplikate einzubauen. Die weitergehenden Geräte-/Callback-Integrationstests wurden **nicht** erfunden oder als PASS bezeichnet. Keine neuen Features, keine Security-Aufgaben, kein Dependency- oder Signing-Umbau. M2 bleibt bis M1-Gate und Maintainer-Merge gesperrt.

**Nächster eindeutiger Schritt:** M1-PR eröffnen, genau dessen HEAD gegen bestehenden CI-Gesamtlauf prüfen, bei Fehlschlägen denselben PR korrigieren; finalen CI-SHA/Run in PR und diesem Fortschrittsregister dokumentieren. M1 **PASS** erst nach grünem Gate. 

## M2 – Apps und Launcher-Einstellungen (laufend)

**Basis:** M1 in main gemergt, Merge-Commit `b02d08ec2e8583074fff087f9e7d148abcb5de1c`. **PR:** [#54](https://github.com/Justify87/Plume_Launcher/pull/54), zunächst Draft.

- App-Installationsermittlung/Leanback-Präferenz/Paketfilter aus `MainActivity.loadApps` nach `LauncherAppCatalog.query/select` verlagert. `MainActivity` behält `App`-Objekterzeugung und Launcher-Intents sowie den bestehenden IO→UI-Callback-Owner.
- Sortierung aus `MainActivity.sortedApps` nach `LauncherAppOrder.sort` extrahiert. Originale Preference-Keys `sort`, `order`, `n_<pkg>`, `inputs_pinned` sowie HDMI-Sonderfall bleiben erhalten. Keine konkurrierende App-State-Quelle.
- Vier neue JVM-Tests in `LauncherAppCatalogTest` und `LauncherAppOrderTest` für TV-Variantenpriorität, Paketfilter, manuelle/HDMI- und Nutzungs-Sortierung.
- `AppPictures`, `SettingsTransfer`, PIN, App-Start, Settings-Pane, Fokus, Bilddateien und Layout vorerst **KEEP**: die verbleibende Extraktion nur nach zielgerichtetem Vertragstest. Keine UI-/VPN-/Security-/Build-Änderung.

**Gate:** PARTIAL bis finaler CI-/Diff-Zweitreview und gegebenenfalls weiterer M2-Tranchen. AndroidTest-Ausführung/Shield/VPN-Tunnel: NOT_TESTED.
