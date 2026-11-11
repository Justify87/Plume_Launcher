# M11 – Release-Härtung und Shield-Endabnahme

**Stand 11.11.2026 · Ziel:** optionales, alltagstaugliches Plume Overview ohne Regressionen im Classic-Modus.  
**Ausgangs-HEAD `main`:** `0f3aaade5f4624711b7d7544d977dc65285d6c7a` (nicht der alte M00-Referenzcommit).  
**Branch:** `feature/m11-overview-release-hardening`. **Kein Merge freigegeben.**

## Was tatsächlich implementiert und geprüft wurde

- Das Layout ist schon per `home_layout_mode` (Default `classic`) umschaltbar; die neue Übersicht benutzt `HomeSidebarView`, `OverviewHomeView`, `HomePageContent` und das gemeinsame `MediaFeedRepository`. Style, Wallpaper, Home-Service und die Classic-Row bleiben eigenständig.
- `MainActivity` fragt Android-TV-Programme auf dem vorhandenen seriellen `io`-Executor ab. Einzelne App-/Kanalseiten lesen nur den vorhandenen Snapshot. Die öffentliche YouTube-RSS-Quelle nutzt denselben Executor, einen bestehenden Cache, Abrufintervalle und Timeouts. Keine neue Dauerschleife, kein Dienst.
- `OverviewHomeView` mountet nur sichtbare und benachbarte Cover-Karten und erhält Slots für korrektes TV-Scrolling. **M11 verhindert den vollständigen Rebuild bei identischer Seiten-/Daten-/Text-Signatur.** Nach Änderungen an Icon-/Glass-Stil wird die Signatur bewusst invalidiert. Keine neuen Einstellungen nötig.
- M11 unterscheidet fehlende TV-Berechtigung, einen tatsächlich fehlgeschlagenen TV-Providerzugriff, einen bei aktivierter YouTube-RSS-Nutzung fehlenden Netzwerkzugang und eine leere Quelle. Nur dann wird eine Erklärung gezeigt, wenn noch keine Items vorhanden sind.
- Bestehende Cover-`LruCache`-Einträge werden an weitere wartende Classic-Karten und die aktuell gemounteten Overview-Karten verteilt. Ein fehlender oder ungültiger Deep Link löst die bisherige Nicht-verfügbar-Meldung aus, mit vorhandenem App-Launch-Fallback.
- Lifecycle-Review: `onPause` stoppt die periodischen UI-Ticks; `onDestroy` meldet Broadcast-, Netzwerk-, TV-Input- und Foto-Observer ab, beendet Poster-/Feed-Executors und entfernt Handler-Callbacks. Direct Boot rendert nur die initiale Layout-View und rekreiert nach Entsperrung. M11 verhindert zusätzlich späte Netzwerkverlust-UI-Updates nach Activity-Zerstörung.
- Bereits bestehende Einstellungen reichen: **Home layout**, **From apps**, **Channels**, optional **SmartTube / YouTube**. Keine Sidebar-Toggles oder Reihenfolge-Editoren ohne Gerätebefund.

**Wichtig:** Das ist eine Codeprüfung, **keine** Profiler- oder Hardwaremessung. `M00` dokumentiert UI-Verhalten, aber keine vergleichbare instrumentierte Millisekunden-/RAM-Serie. Weder schnellere Kaltstarts noch weniger Speicher werden als gemessen behauptet.

## Tatsächliche Integrationsgrenzen

| Quelle | Darstellung | Voraussetzung / Grenze |
| --- | --- | --- |
| Android TV Watch Next und App-Preview-Kanäle | „Weitermachen“ bei glaubwürdigen Fortschrittsdaten; nachweisbare neue Einträge nach Klassifikation | `READ_TV_LISTINGS`, sichtbare und vom Provider befüllte Kanäle. Nicht jede App unterstützt das |
| Nuvio TV | Gestartete Inhalte; in einem separaten Next-Up-Kanal die nächste bereits ausgestrahlte Episode | Passender Fork/Version, Kanalfreigabe. „Next Up“ bedeutet nicht automatisch *neu erschienen* oder *abspielbar* |
| SmartTube / YouTube | Optional SmartTube-Subscriptions-Preview-Kanal, ergänzend öffentliche Atom-Feeds | Ohne SmartTube-Veröffentlichung keine privaten Abos. RSS ist keine persönliche „ungesehen“-Liste |
| Spotify | **Nicht in der Android-App implementiert** | M10 enthält nur ein isoliertes API-Prüfwerkzeug. Echter Developer-Zugang, Episode und Position auf Shield müssen erst belegt werden |

## Automatisierbare Abnahme

Auf einer verfügbaren Build-Umgebung vom Feature-Branch ausführen (oder den **tatsächlichen PR-CI-Run** prüfen):

```bash
sh tools/pr-rules.sh "$(git merge-base HEAD origin/main)"
sh tools/plume-install-test/run.sh
python3 -m unittest discover -s tools -p 'test_spotify_podcast_probe.py' -v
./gradlew testDebugUnitTest assembleDebug lintDebug assembleRelease
```

**Status bei Erstellung dieses Dokuments:** Android-SDK/Gradle-/GitHub-Netzwerkzugriff steht in der lokalen Ausführungsumgebung nicht zur Verfügung. Daher noch **kein lokaler Android-Build, kein Lint-Resultat und kein Shield-Nachweis** für diesen Branch. Die GitHub-PR-Checks können die tatsächlichen Gradle-Ergebnisse nach PR-Erstellung liefern. Keinen Test als erfolgreich eintragen, bevor der entsprechende Lauf sichtbar bestanden hat.

Neue JVM-Tests: `OverviewReleaseRegressionTest` (unveränderter Snapshot, geänderte Texte/Seite/Items, Berechtigungs-/Fehler-/Offline-/Leerzustände, 0 und 80 Medienitems). Bestehende M04–M09-Regressionstests weiterhin ausführen.

## Manuelle Shield-TV-Pro-2019-Abnahme – OFFEN

Vor Beginn tatsächliche Geräte-/App-Daten aufnehmen:

```bash
git rev-parse HEAD
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell getprop ro.build.fingerprint
adb shell dumpsys package dev.plume.advanced | grep -E 'versionName|versionCode' | head
```

APK aus genau dem dokumentierten Commit installieren; Signaturkonflikte **nicht** durch unbedachtes Löschen von Plume beheben. Bestehende Einstellungen vorab über die Plume-Backup-Funktion sichern. Für Laufzeitvergleich denselben Wallpaper-/Glass-/Netzwerkzustand für den alten M00-/Classic-Stand und den neuen Branch benutzen, jeweils fünf frische Starts nach Warm-up sowie Rückkehr aus derselben App. Mit `adb shell am start -W -n dev.plume.advanced/dev.plume.MainActivity` Starts protokollieren, mit `adb shell dumpsys meminfo dev.plume.advanced` Speicher messen. **Nur gemessene Werte** mit Geräte- und Commitangabe vergleichen; der alte M00-Stand ist nie automatisch Testgegenstand.

| ID | Fall | Erwartung | Ergebnis |
| --- | --- | --- | --- |
| S01 | Frische Installation, keine `home_layout_mode`-Preference; Home-Setup; Home-Service | Classic bleibt Standard, Setup und Home-Taste funktionieren | **OFFEN** |
| S02 | Classic → Übersicht → Classic, Settings/Wallpaper/Glass/Sortierung ändern | Kein Verlust der Classic-Preference; Wallpaper und Fokus weiter korrekt | **OFFEN** |
| S03 | Sidebar ohne Apps, mit einer App und mit mindestens 50 Apps; lange App-Namen | D-Pad Up/Down/Right/Left sicher; scrollt und hält sichtbaren Fokus | **OFFEN** |
| S04 | Kein MediaItem, viele (>20) Items, mehrere unterschiedliche Medienreihen; lange Titel | Kein leerer Fokus, keine sichtbaren Halb-Karten, Texte schneiden sauber ab | **OFFEN** |
| S05 | Identische Providerdaten nach Rückkehr aus App, mehrfacher Seitenwechsel | Fokus und Scrollposition bleiben erhalten; keine unnötigen neuen Cover-Anfragen | **OFFEN** |
| S06 | App entfernen/aktualisieren, während eine App-Unterseite gewählt ist | Sicherer Rückfall auf Übersicht; keine alte App-Launch-Aktion | **OFFEN** |
| S07 | Fehlende/entzogene TV-Listings-Berechtigung; absichtlich leere Quellen | Verständlicher Leerzustand, Home und Einstellungen weiterhin bedienbar | **OFFEN** |
| S08 | Provider nicht erreichbar/Fehler; SmartTube nicht installiert | Keine Phantomdaten, kein Crash oder blockierter Launcher | **OFFEN** |
| S09 | YouTube-RSS an, keine Verbindung; dann wieder online | Offline-Hinweis; erneuter Abruf gemäß bestehendem Cache/Intervall | **OFFEN** |
| S10 | Nuvio ältere Version, Next-Up-Version, fehlende Kanalfreigabe | Nur echte gestartete Items fortsetzen; Next-Up nicht mit Verlauf verwechseln | **OFFEN** |
| S11 | SmartTube „Subscriptions“ vorhanden/nicht vorhanden, manueller UC-Kanal | Nur belegte Videos, korrekter App-Start oder Fehlermeldung | **OFFEN** |
| S12 | Spotify nicht installiert, leere Spotify-Bibliothek | Kein Spotify-Feed/keine toten Spotify-Karten: M10 liefert noch keine Android-Integration | **OFFEN** |
| S13 | Ungültiger Deep Link, App deinstalliert, erlaubter Link zur richtigen Folge | Kein Crash; App-Fallback oder Nicht-verfügbar-Meldung | **OFFEN** |
| S14 | Settings/All-apps-Overlays, direkter Home-Start vor Entsperrung | Fokus nicht hinter Overlay; Direct Boot und Unlock ohne Null-Zugriff | **OFFEN** |
| S15 | Deutsch/Englisch, hell/dunkel, Video-/Foto-Wallpaper, Clear/Frosted/Off | Lesbare Beschriftungen, erhaltene Fokusindikatoren, kein Karten-„Flackern“ | **OFFEN** |
| S16 | Kaltstart, App-Rückkehr, schneller D-Pad-Repeat, RAM, UI-Neuaufbau | Gegen M00 messbar regressionsarm; genaue Messwerte und Kommandos protokollieren | **OFFEN** |

**Freigabekriterium:** PR-CI grün **und** dokumentierte Shield-Protokolle mit Firmware/API, APK-/Commit-ID und tatsächlich beobachteten Fokus-, Performance- und Quellentests. Solange S01–S16 offen sind, ist M11 **code-seitig umgesetzt, aber nicht hardwareseitig endabgenommen**.
