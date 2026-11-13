# M00 – Baseline der klassischen Startseite

Stand: 2026-11-10 · Repository: `Justify87/Plume_Launcher` · Basisbranch: `main`  
Geprüfter HEAD: `86616b7cb7ae4242769fc561a78169fe64dd505f` (Plume 1.7.1). Dieser Commit ist die *gelesene* Baseline, keine Anweisung zum Reset.

## Scope und Prüfstatus

Diese Notiz dokumentiert **bestehendes** Verhalten vor einer optionalen zweiten Home-Ansicht. M00 verändert weder Produktionscode noch XML, Ressourcen, Berechtigungen oder Einstellungen. Die Regeln unten sind anhand des Quellcodes geprüft; TV-Verhalten ist bis zu einem manuellen Test **nicht empirisch bestätigt**.

Gelesen: `README.md`, `CONTRIBUTING.md`/PR-Regeln, `app/build.gradle.kts`, Gradle-Wrapper, `app/src/main/AndroidManifest.xml`, `app/src/main/res/layout/activity_main.xml`, `MainActivity.kt`, `SnapScrollView.kt`, `Glass.kt` (GlassLayer), `HomeOverride.kt`, `TopBar.kt`, `SettingsTransfer.kt`, `AppPictures.kt`, `ContinueWatchingTest.kt`, `TopBarTest.kt`, `IconStyleTest.kt`, `HomeWatchTest.kt`, `ActivityTest.kt`, `.github/workflows/pr.yml`, `.github/workflows/translations.yml` und `tools/pr-rules.sh`.

## UI- und Navigationsvertrag (Ist-Zustand)

- **Struktur:** `activity_main.xml` enthält unter `root` Video-Wallpaper, Bild-Wallpaper und `content` mit Uhr/Top-Bar, `appScroll/appRow`, `watchScroll/watchRow`. `allApps` und `settingsScrim` sind separat darüberliegende Overlays. `SnapScrollView.computeScrollDeltaToGetChildRectOnScreen()` scrollt nur so weit, dass Kacheln vollständig sichtbar werden.
- **Fokus:** `MainActivity.homeNav()` in `dispatchKeyEvent()` ordnet die drei Bereiche bewusst an. `lastTop`, `lastApp`, `lastCard` merken die zuletzt fokussierten Views; bei nicht mehr angehängten App-Views wird auf die erste App zurückgegriffen. Normale Links/Rechts-Schritte innerhalb einer Reihe überlässt `homeNav()` Android.
  
  | Startbereich | Links / Rechts | Hoch | Runter |
  | --- | --- | --- | --- |
  | Top-Bar | Innerhalb der Reihe Android-Fokus; an beiden äußeren Grenzen **stehen bleiben**, kein Wrap | Auf aktuellem Icon bleiben | Letzte/erste App; wenn keine App: erste Watch-Karte; sonst bleiben |
  | App-Reihe | Links auf der ersten Kachel bleiben; Rechts auf der **letzten zur ersten App** springen | Letztes Top-Icon; sonst `launcherSettings` | Letzte/erste Watch-Karte, **nur falls `watchScroll` sichtbar**; sonst bleiben |
  | Watch-Reihe | Innerhalb Android-Fokus; an beiden Grenzen bleiben, kein Wrap | Letzte/erste App; wenn keine: Top-Bar | Auf aktueller Karte bleiben |

- **Leere/ausgeblendete Watch-Reihe:** Keine Daten oder keine `READ_TV_LISTINGS`-Berechtigung => `showWatchNext(emptyList())`; aus der App-Reihe führt Runter dann nicht in eine leere Reihe. `row_show=never` unterbindet Watch-Fokus. `row_show=down` belässt vorhandene Karten in einer sichtbaren Scroll-View, blendet sie zunächst per Alpha/Translation aus und zeigt sie bei Fokus; dies ist **nicht** dasselbe wie `View.GONE`. `row_drop` steuert, ob die App-Reihe den freien Raum nutzt. Ein unsichtbarer Platzhalter hält ggf. die Höhe.
- **OK / App-Start:** App-Tile ruft `launch()` auf, protokolliert Nutzung (`n_<package>`, `t_<package>`), startet den explizit aufgelösten Leanback-/Launcher-Intent (Fallback) über `startApp()` und merkt `returnTo` und ggf. `openedFrom`. HDMI-Tiles werden über `Remap.openInput()` geöffnet. Watch-Karte verwendet `Intent.parseUri(..., URI_INTENT_SCHEME)`; bei Fehler wird eine Leanback-App als Fallback geöffnet. **Eine eigene zusätzliche Deep-Link-Validierung ist derzeit nicht ersichtlich** – beim Ausbau ausdrücklich berücksichtigen, nicht stillschweigend ändern.
- **Rückkehr:** Mit Zurück aus einer externen App bleibt der vorhandene Fokus grundsätzlich erhalten; `onResume()` setzt die Zeile bei `openedFrom` optisch zurück und baut Apps nur bei Bedarf neu. HOME auf der bestehenden `singleTask`-`MainActivity` führt über `onNewIntent()`: Editing beenden, Overlays schließen, ursprüngliche App/Watch-Karte via `returnTo` wenn verfügbar erneut fokussieren, andernfalls Watch-Scroll zurücksetzen und erste App fokussieren. `onResume()` leert danach die Rücksprungmarkierung. HOME aus einer fremden App, die Plume **nicht** gestartet hat, beginnt also bei der ersten App.
- **Zurück innerhalb von Plume:** `onBackPressed()` schließt Einstellungen (ggf. Unterseite) bzw. Alle Apps; die Home-Seite selbst wird nicht beendet. Ein offenes Popup reagiert über das Android-`PopupWindow`; im App-/Top-Verschiebemodus fängt `dispatchKeyEvent()` Zurück gesondert ab. `allApps` wird bei Close ausgeblendet, der Home-Fokus reaktiviert und `allAppsButton` fokussiert.
- **Langes OK:** `dispatchKeyEvent()` erkennt erste OK-Autowiederholung, `View.OnLongClickListener` dient als Fallback. Top-Bar: verschieben/verbergen/hinzufügen; App-Reihe: verschieben, aus Reihe entfernen, App-Info/ggf. Bild/Deinstallation; Alle Apps: App vorne anpinnen; Watch-Reihe: Eintrag lokal ausblenden oder App öffnen. PIN-Sperre wird beachtet. Während des Verschiebens verlässt der D-Pad-Fokus die betreffende Reihe nicht.

## Persistenz und bisherige Einstellungen

Speicher: `getSharedPreferences("home", MODE_PRIVATE)` in `MainActivity`; `SettingsTransfer` exportiert nur freigegebene portable Werte, nicht jeden lokalen Zustand. Kein neuer Preference-Key in M00.

| Schlüssel | Ist-Standard / Werte | Wirkung / Implementierung |
| --- | --- | --- |
| `row_show` | `always` / `always,down,never` | `rowShow()`, `placeRow()`, globaler Fokuslistener |
| `row_drop` | `false` / boolesch | `rowDrops()`, `placeRow()`: App-Zeile bei unsichtbarer Watch-Zeile verschieben |
| `row_align` | `left` / `left,centre,right` | `rowAlign()`, `alignRow()`: nur kurze Zeilen dürfen mittig/rechts ausgerichtet werden |
| `row_size` | `standard` / `standard,large,largest` | `rowSize()`, `rowTiles()`, `renderApps()`: Tile-Anzahl/-Größe |
| `names` | `focus` / `focus,always,never` | `rowNames()`, `showNames()`, `showRowLabel()` |
| `icon_style` | nicht gesetzt => `mono` oder altes `colour_icons` / `mono,colour,banner,bannermono` | `iconStyleOf()`, `iconStyle()`: Legacy-Fallback erhalten |
| `sort`, `order` | `manual`, leere Reihenfolge | `sortedApps()`, `saveOrder()`; optional `usage` nach `n_<package>`, Default-Reihenfolge/Label als Tie-Break |
| `row_hidden` | leere String-Menge | Aus Apps-Reihe verbergen, in Alle Apps behalten; `inputs_pinned` für HDMI separat |
| `cw_off`, `hidden_watch`, `ch_flip` | leere Mengen | Quellen-/Titel-Filter und Preview-Kanal-Umschaltung; `row_name` optionaler Titel |
| `top_bar` | Default gemäß `TopBar.parse()`, `plume` zwingend | Top-Bar-Icons, Reihenfolge, Ausblenden; `sources` nur bei vorhandener Quelle |
| `glass_style`, `glass`, `theme` | vorhandene Theme-/Glass-Einstellungen | Unverändert weiterverwenden |

Änderungen erfolgen durch bestehende Einstellungen/Long-OK-Funktionen; `SettingsTransfer` validiert portable Werte und stellt bei Import fehlende unterstützte Werte auf Defaults zurück. Ein künftiges `home_mode` muss **neu und default `classic`** sein; Migration, Import/Export und Rückfall nach ungültigem Wert sind gesondert zu spezifizieren, bevor es produktiv wird. Es darf `row_*`, `names`, `icon_style` usw. nicht stillschweigend überschreiben.

## TV-Inhalte, Cover und Nebenläufigkeit

- `refreshWatchNext()` prüft `READ_TV_LISTINGS`, ruft `queryWatchNext()` im Single-Thread-`io` auf und rendert über `ui.post` auf dem UI-Thread; Exceptions werden als leere Liste behandelt.
- `queryWatchNext()` kombiniert `TvContract.WatchNextPrograms.CONTENT_URI` und aktivierte `TvContract.Channels`-/Preview-Programme. Kanäle mit Namen „Continue Watching“ sind standardmäßig an, andere aus. Reihenfolge: Watch-Next-Typ/Zeit, Channel-Zeit/Gewicht und `newestPerApp(..., PER_APP)`; Deduplizierung und Anzeige bis zu 20 Titel. „Neu für dich“ existiert hier **noch nicht als separate Datenquelle**.
- `showWatchNext()` behält unveränderte Karten/Fokus, erzeugt die ersten vier Karten direkt, weitere gestaffelt bzw. beim Fokusvorrücken via `addCardsTo()`; fehlende Poster werden erneut versucht. `readPrograms()` behandelt einen fehlenden Provider-Cursor ohne Absturz.
- `loadPoster()` nutzt 3 Poster-Threads, `loading` als Inflight-Schutz, einen Bitmap-`LruCache` von 6 MiB, `decode()` mit Downsampling/RGB_565, `retryPosters()` bei Verbindungsrückkehr. `openStream()` öffnet HTTP(S) oder Content-URIs mit 5-s-Timeout für Netzwerkverbindungen. Daneben bestehen App-/Icon-/Wallpaper-Caches (`APPS`, `SOURCES`, `GLYPHS`, `ACCENTS`, `thumbCache`).
- `GlassLayer` wird in `onCreate()` ausdrücklich als **Kindindex 2** in `root` eingefügt: über Video-/Bild-Wallpaper, unter den Inhalts-/Overlay-Ebenen. `glass.add/remove/kick`, `style()`, `applyGlass()`, `VideoWallpaper` und Wallpaper-Rendering hängen von dieser Reihenfolge und der tatsächlichen View-Position ab. Nicht im Zuge eines Home-Modus-Refactorings beiläufig umsortieren.

## Android-TV-Berechtigungen, Start und Direct Boot

- `AndroidManifest.xml`: `READ_TV_LISTINGS` (zur Laufzeit in `MainActivity.onCreate()` angefragt), `com.android.providers.tv.permission.READ_EPG_DATA`, Sichtbarkeit von `android.media.tv` über `<queries>`, Leanback-Feature, `MAIN/HOME/COMMON_HOME`, `singleTask`. App-Erkennung über `LEANBACK_LAUNCHER` und `LAUNCHER`.
- Optional für Home-Service-/Screensaver-Einstellungen: `WRITE_SECURE_SETTINGS` (ADB-Grant, nicht als vorhanden annehmen); Home-Override via `HomeOverride`-Accessibility-Service/alternativ Default-Home-Rolle. Bildordner-Funktionen benötigen je nach OS `READ_MEDIA_IMAGES` bzw. `READ_EXTERNAL_STORAGE` (bis API 32). Keine neuen Berechtigungen für M00 oder das reine UI-Prototyping.
- **Direct Boot:** `MainActivity` ist `directBootAware`. `onCreate()` registriert zunächst `ACTION_USER_UNLOCKED`; wenn `UserManager.isUserUnlocked == false`, wird nur das XML-Layout gesetzt und **keine** Preference, Datei, Cache, GlassLayer-Initialisierung oder TV-Abfrage ausgeführt (`ready=false`). Bei Unlock `recreate()`. `onResume()`, `dispatchKeyEvent()`, `onBackPressed()` respektieren `ready`. Ein zweiter Home-Modus darf diesen Guard nicht umgehen.

## Geplante, noch **nicht umgesetzte** Eingriffspunkte

1. **Moduswahl nach Unlock:** bei der initialen Layout-/Content-Erzeugung in `onCreate()` und einer späteren Einstellungsaktion. Klassische Ansicht als Default/Fallback; für neue Ansicht ein eigener klarer View-Baum bzw. Layout-Teil ohne Veränderung der klassischen IDs/Fokusziele.
2. **Navigation trennen:** `dispatchKeyEvent()`/`homeNav()` sind für exakt drei klassische Reihen geschrieben. Neue Sidebar nur im aktivierten Modus mit eigener Fokusmatrix; `onNewIntent()`, `onResume()`, `onBackPressed()` und Overlay-Fokusgrenzen müssen beide Modi behandeln.
3. **Daten teilen statt erneut abfragen:** `refreshApps()/sortedApps()/renderApps()` und `refreshWatchNext()/queryWatchNext()/showWatchNext()` als bewährte App-/TV-Datenwege nutzen; eigene Übersichts-Darstellung darf keine zweiten permanenten Poller oder weiteren `TvContract`-Queries erzeugen. Trennung von Daten und klassischer UI erst in eigenem Scope.
4. **Darstellung/Fokus erhalten:** `GlassLayer`-Einfügeindex, Wallpapers, `SnapScrollView`, `placeRow()`, `style()`, `homeFocusable()`, Posterladen, bestehende Menüs, `last*` und `returnTo` schützen. Bei der neuen App-Seite alte Persistenz für Reihenfolge/Verbergen weiter nutzen.
5. **Inhaltsgrenze:** Watch Next/Continue-Channels liefern „Weitermachen“; ein Feed „Neu für dich“ braucht erst später eine eigene, opt-in und fehlertolerante Quellendefinition. Das ist **keine** M00-Funktion.

## Automatisierte Baseline / offene Tests

Bereits vorhandene kleine Tests: `TopBarTest` (Endgrenzen, Umordnen, Pflicht-Icon), `ContinueWatchingTest` (Quellensortierung, Fairness, Kanalgewicht), `IconStyleTest` (Legacy-Iconstil), `HomeWatchTest` (Home-Intercept), außerdem `ActivityTest` als Instrumentation für All-Apps/Home/Back/Theme-Wechsel und weitere Tests. Die Navigation in `homeNav()` benutzt konkrete `View`-/Fokus-/Visibility-Zustände und ist ohne größeres Umbauen **kein sinnvoller reiner JVM-Test**. Deshalb in M00 keine Produktionscode-Extraktion und keine künstlichen Unit-Tests.

Für **M02** als automatisierte Instrumentation-/Fokustests vormerken: (a) letzte App + Rechts => erste App; (b) Top-Bar-Enden stoppen; (c) Watch-Enden stoppen; (d) Hoch/Runter behalten letzten Fokus; (e) `row_show=down/never`, `row_drop` und leerer Feed; (f) alle Apps verborgen; (g) HOME von gestarteter/nicht gestarteter App und Zurück; (h) Overlay-/PIN-/Long-OK-/Reorder-Fokus; (i) Relaunch/Theme-/Direct-Boot-Wechsel; (j) ungültiger/fehlender Deep Link fällt ohne Crash zurück.

**CI:** `.github/workflows/pr.yml` prüft bei PR: `tools/pr-rules.sh`, `./gradlew testDebugUnitTest lintDebug assembleRelease`, APK-Größenvergleich zum Basiscommit. `translations.yml` läuft nur bei Übersetzungs-/`tools/lang.py`-Änderungen oder manuell. Der Meilenstein verlangt zusätzlich lokal `./gradlew testDebugUnitTest assembleDebug`; ohne vollständiges Repository und Android-SDK kann dieser lokale Lauf nicht behauptet werden.

## Manuelle Smoke-Tests: NVIDIA Shield TV Pro 2019 (noch auszuführen)

1. Klassisch starten, Uhr/Walls/Glass/Top-Bar prüfen; D-Pad-Fokus und Skalierung bei **hell/dunkel**, Glass Off/Clear/Frosted, Video-Wallpaper.
2. Top-Bar: Links/Rechts an Grenzen; Runter zu App; Hoch zurück. Optionalen Sources-/Power-Button und geänderte Top-Bar-Reihenfolge prüfen.
3. App-Reihe: ganz links Links bleibt; ganz rechts Rechts geht zur ersten App; Hoch/Runter und letzte Fokusposition; bei `row_size` Standard/Large/Largest, `row_align` links/mitte/rechts, `names` Fokus/immer/nie und Mono/Colour/Banner.
4. Watch-Karten: Links/Rechts-Grenzen, Hoch zu App, Runter bleibt; Scroll snappt ganze Karten, Progress/Poster, leere Reihe und fehlende Berechtigung; `row_show` always/down/never jeweils mit `row_drop` an/aus.
5. OK startet App/Watch-Episode; **Zurück** aus der App bewahrt Home-Fokus; **HOME** kehrt zum Ausgangs-Tile/-Cover zurück, bei anderem Start zur ersten App. Fehlende Ziel-App oder nicht mehr vorhandenes Cover ohne Crash.
6. Langes OK: App verschieben (OK bestätigen, Zurück beenden), verbergen/wieder aus Alle Apps hinzufügen; Watch-Card ausblenden; Top-Icon verschieben; PIN-Sperre beachten. HOME während eines Moves verwirft Top-Bar-Move bzw. beendet App-Edit.
7. Alle Apps und Einstellungen öffnen/schließen; Zurück kehrt ins korrekte UI zurück; Öffnen über Remote-Key, Home während eines Overlays; Theme-Wechsel und Rückkehr aus Android-Einstellungen.
8. App installieren/deinstallieren, Reihenfolge/Hidden-Set prüfen; Home-Service/Default-Launcher; Neustart vor Benutzer-Entsperrung, dann Unlock und populierter Start.
9. Einstellungen sichern/wiederherstellen und kontrollieren, dass `row_*`, `names`, `icon_style`, Sortierung und versteckte Apps erhalten bleiben.

**Review M00:** Keine sichtbaren Änderungen an der klassischen Startseite, keine neue Abhängigkeit/Berechtigung und keine Glass-/Fokus-Codeänderung. Offene Punkte sind die tatsächlichen lokalen Build-Ergebnisse und die manuelle Shield-Validierung; sie dürfen nicht als bestanden markiert werden.
