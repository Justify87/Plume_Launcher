# Plume Launcher Advanced – Refactoring PROGRESS

**Aktueller Status:** M0, M1 und M2 gemergt (#51, #52, #53). **M3** im eigenen Branch und PR #55: Source-/Projection-/Poster-Extraktionen umgesetzt, eigene JVM-Grenzfalltests ergänzt; **CI PENDING**, Geräteabnahme **NOT_TESTED**. M4–M7 NOT_STARTED. Ältere Meilensteinabschnitte darunter sind historische Prüfstände.

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


---

## M2 – Apps / Launcher-Einstellungen (PR #53)

**Basis:** `main` @ `b02d08ec2e8583074fff087f9e7d148abcb5de1c` (M1-Merge PR #52). **Branch:** `refactor/m2-app-catalog-settings-20261116`. **PR:** https://github.com/Justify87/Plume_Launcher/pull/53.

- **F026 → F124:** App-Klassenmodell mit schwachem Icon-Reload und stateless `PackageManager`-TV-/Phone-Ermittlung sowie manuelle/usage/Default/HDMI-Sortierung aus `MainActivity` ausgelagert. Activity hält unverändert Apps/HDMI-Sources als Single State Owner, `appsDirty`, `io→ui`-Delivery, Start-/Fallback-Intent, Launch-Stats, Fokus und UI.
- **F026 → F125:** feste Settings-Struktur (6 Sektionen, TV-/Fire-abhängige Zeilen) extrahiert; alle `renderSettings`-Beschriftungen, `RowStyle`, `showPage`, `pageExit`, PIN-/Backup-Callbacks und Preferences im bestehenden Activity-Lifecycle. **F017 AppPictures, F034 SettingsTransfer, PIN, Icon-UI, tatsächlicher App-Start: KEEP**; Verschieben würde ohne Mehrwert den State-/Callback-Besitz aufteilen.
- **F067 und F126:** drei zusätzliche Tests für Package-Sortierung/Variante sowie zwei neue Tests für Settings-Zeilen/Section-Order. Vorhandene Tests unverändert belassen.
- **Task-Register:** Historisches M0-Inventar 123/123, M2-Neudateien F124–F126 = **126 Task-Dateien**. F026/F067 aktualisiert; alle anderen Original-Tasks bleiben unverändert. Submodul, Build/CI-Konfiguration, Android-API, Signing, Layout, Features und Release unverändert.
- **Vorläufiges Gate:** **PARTIAL bis PR-HEAD-CI und kritischer Diff-Zweitreview erfolgreich**. `testDebugUnitTest lintDebug assembleDebug compileDebugAndroidTestKotlin` und bestehende ADB-/Python-/Mullvad-ARM64-/R8-/APK-Checks: **PENDING**. AndroidTest-Lauf, Shield Pro, D-Pad/Focus, App-Paketwechsel, echter VPN-Tunnel: **NOT_TESTED**.
- **Scope-Review:** Keine neue State-Kopie oder Executor. Die enge Extraktion lässt die API-/Prefs-/Intent-/Render-Verträge in ihren ursprünglichen Lebenszyklen; eine Komplettverschiebung von `launch` oder `renderSettings` wäre eine künstliche Callback-/Activity-Wrapper-Schicht. UI und Funktion sollen unverändert bleiben.

---

## M3 – Medienabfrage / Content-Pipeline (PR #55)

**Basis:** `main` @ `2e05a0abddcaaf61137255cc3905c06cd2ace6b0` (M2 gemergt); **Branch:** `refactor/m3-media-pipeline-20261117`; **PR:** https://github.com/Justify87/Plume_Launcher/pull/55. M3 **PARTIAL bis finale CI und Review**.

### Geprüfte Datei-/Architektur-Tasks

- F026 → F127 `TvContractMediaReader.kt`: `readPrograms` und `appChannels` inklusive `Prog`, Fehler-/Null-/Sortier-/Publisher-Vertrag aus Activity extrahiert. F015 `AndroidTvMediaSource` weiterhin KEEP als unabhängige Channel-/Programm-Policy.
- F026 → F128 `TvFeedProjection.kt`: `AndroidTvProgram`-Mapping, `selectNuvio`, `NuvioMediaProjector.project`, generische Medienklassifikation und zwei getrennte 20er-Limits. F027 `MediaFeedRepository` unveränderter Source-Snapshot-Owner, F029 `NuvioDashboardFeed` unverändert KEEP.
- F026 → F129 `MediaPosterLoader.kt`: 3 Worker, Inflight-Set, 6-MiB-LruCache, Poster-Byte-/Sample-Decoding, UI-Handler/URI-Tag und `onTrimMemory`/`onDestroy`-Lifecycle als kohärente Einheit. Wallpaper-Thumbnailcache `thumbCache` bleibt bewusst separat in Activity.
- F130 `TvFeedProjectionTest.kt`: vier JVM-Regressionsfälle für exakten Nuvio-Kanal/Publisher (inkl. fremdem Paket), `cw_off`, unbekannte Dauer/Prozent und `yt_new_on`-Gate. Bestands-Tests F059/F069/F070/F071/F085 sowie PR-CI weiterverwenden.
- `FILE_TASKS`: historisch 123 + M2 3 + M3 4 = **130 eigenständige Tasks**, keine doppelten Pfade; tatsächliche F015/F026/F127–F130-Blobs und Zuständigkeiten ergänzt. A02, A04, A07 im `ARCHITECTURE_MAP` auf Ist-Zustand angepasst.

### Scope / Verhaltensinvarianz

Keine Änderung an `refreshWatchNext`-Trigger, `io.execute → ui.post`, Activity-eigenem Feed-/Failure-State, App-Start-/Intent-Policies (`openVDesignMedia`, `openWorkspaceMediaItem`), `YouTubeRssSource`, `YouTubeSubscriptions`, `MediaItem`, `MediaFeedRepository`, `NuvioMediaProjector`, UI-Layout/Fokus, Pref-Keys/Backup, Mullvad/Submodul, Versionsnummern, Signing oder CI-Konfiguration. Keine Security-/Feature-Nebenarbeit.

### Test-/Review-Gate

| Prüfung | Ergebnis |
|---|---|
| `./gradlew testDebugUnitTest lintDebug assembleDebug compileDebugAndroidTestKotlin` | **PENDING** – PR-HEAD-CI |
| `sh tools/plume-install-test/run.sh`, Spotify-/Mullvad-Python-Tests | **PENDING** – PR-HEAD-CI |
| Kombinierter Mullvad ARM64-Debug + unsigned R8-Release und APK-/Manifest-Checks | **PENDING** – PR-HEAD-CI |
| AndroidTest-Ausführung, echte TvContract-Provider, Nuvio/SmartTube auf Shield Pro, Poster-/Fokus-/VPN-Hardware | **NOT_TESTED** |

**Kritischer Zweitreview (selbst, nicht extern unabhängig):** Transfer auf Quelltext-/Diff-Ebene verglichen: einzelne Reader-Methoden ohne zusätzlichen Worker/State; Projektion erzeugt dieselben `AndroidTvProgram`-Metadaten mit denselben `mapIndexed`-Indizes und 20er-Grenzen; Poster-Cache und Shutdown gemeinschaftlich verlagert, `tag==uri` geschützt. Der bisherige `MainActivity`-OpenStream bleibt von anderen Featurebereichen nutzbar. Noch **kein** Hardware- oder CI-PASS behauptet.

**Nächster Task:** finale PR-CI auf demselben HEAD abwarten und Fehler ursachenbezogen im selben PR beheben; nach gesamtem PASS PR reviewbereit markieren, **nicht mergen**. Kein M4 vor M3-Merge.

---

## M4 – Wallpaper, Screensaver und Systemintegration (PR-HEAD-CI PENDING)

**Basis:** `main` @ `9c9fb89c94b381dca27ee5d79990605afba26e39` (M3 erfolgreich gemergt; CI PASS). **Branch:** `refactor/m4-wallpaper-system-coordination-20261117`.

### Tatsächlich umgesetzte Tasks

- **F026 → F131 `VideoWallpaperArtwork.kt`:** Video-Poster-Cache/Erstframe und zeitlich abgetastete Videoframes aus `MainActivity` extrahiert. Die vorhandenen `io`-Worker, `wallpaperLoad`-Generation, UI-Apply, `VideoWallpaper`-Surface, Pause/Resume/Clear bleiben unberührt.
- **F026 → F132 `WallpaperVideoFiles.kt`:** persönliche mp4-Dateien, Katalog-/Aerial-Ausschlüsse und Bereinigung bei `video_keep_one` ausgelagert. Keine zweite Dateiliste und kein langlebiger State.
- **F026 → F133 `DreamSettingsGateway.kt`:** Android-Dream-Komponente und Secure/System-Werte inkl. unveränderter Write-Reihenfolge gekapselt. Activity behält `WRITE_SECURE`-Prüfung, Gallery-Download, Dream-Page, Return-/Prefs-Flow.
- **F026 → F134 `WeatherDataSource.kt`:** Wetter-/Geo-JSON und City-Suche auf unverändertem Activity-`io`-Executor zusammengezogen. Gleiche `geo_*`/`wx_*`-Werte, 25m/6h-Fenster, fehlende Geo-Infos, URI-Parameter. Activity bleibt Owner für `weatherTick`, UI-Handler und Stale-Result-Check.
- **F135/F136:** neue JVM-Charakterisierung: private vs. Katalog-/Aerial-Videos, Light-/Dark-Auswahl und Cleanup; Wetter-/Geocoding-URL-Vertrag. Bestehende `ScreensaverTimingTest`, `VideoScenesTest`, `WallpaperPackTest`, `RebootSetupAndWallpaperTest`, `HomeWatchTest` und AndroidTest-Kompilation bleiben CI-Gates.
- **FILE_TASKS:** M0 123 + M2 3 + M3 4 + M4 6 = **136 Datei-Tasks**. F026 aktualisiert; bestehende Wallpaper/System-Dateien bewusst KEEP.

### Scope-Review und Verifikation

**KEEP:** `Bing`, `GalleryDream`, `AerialShow`, `UploadServer`, `WallpaperPack`, `VideoWallpaper`, HomeOverride/HomeWatch, HDMI/Remap, QR, ScreensaverTiming, Sun, Wetter-/Update-UI/PackageInstaller, alle Receiver/ContentObserver und Upload-PageExit. Keine neuen Hintergrund-Worker, keine veränderte Start-/Focus-/Statusbar- oder VPN-Logik.

| Gate | M4-Code-Review-Status |
|---|---|
| Semantischer Source-Diff, Ownership, Fokus-/Prefs-/Callback-Order | Quellvergleich erledigt, erneuter kritischer PR-Review offen |
| `testDebugUnitTest lintDebug assembleDebug compileDebugAndroidTestKotlin` | **PENDING** auf endgültigem PR-HEAD |
| Installer-Shell, Spotify-/Mullvad-Python-/Staging-Tests | **PENDING** |
| Integrierter ARM64-Debug, unsigned R8, APK-/Manifestchecks | **PENDING** |
| TV-Emulator/Shield: Surface, Galerie, echte Screensaver-Settings, Wetter-, Upload-/Systemaktionen | **NOT_TESTED** |
| VPN-Tunnel/echte Dienste | **NOT_TESTED**, Produktkern unverändert |

**Vorläufige Entscheidung:** `M4 PARTIAL` bis finaler CI-PASS und kritischer Zweitreview. Keinen automatischen Merge und keinen Beginn von M5 vor Nutzer-Merge.
