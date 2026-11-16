# Plume Launcher Advanced – Refactoring PROGRESS

**Status:** M0 fachlich abgeschlossen; PR erst nach grüner CI auf dem *letzten* HEAD reviewbereit. **M1–M7: NOT_STARTED.**

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
