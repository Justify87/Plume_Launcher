# M0 – ARCHITECTURE_MAP

**Baseline:** `main` @ `e3c1a3eb78c85bb471d9012dd700b6efd81b44ce`. **Gültigkeit:** aktuelle aktive Verträge und produktiver Code, keine neuen Feature-Aufträge. **Status:** M0 vollständig inventarisiert und je Datei fachlich bewertet (123/123). M1–M6-Extraktionen bleiben gesperrt und benötigen vor jedem Move konkrete Ownership-/Thread-Verträge und Tests.

## Komponenten / aktuelle Integrationsgrenzen

| Bereich | Aktive Dateien / Ownership | Wichtige abhängige Tasks |
|---|---|---|
| Launcher / Apps / Settings | `MainActivity.kt` koordiniert `loadApps → refreshApps → refreshVisibleApps → refreshVDesignNavigation`, `renderAllAppsPane`, Settings-Pane, PIN und gespeicherte Präferenzen. App-Bilder sind in `AppPictures.kt`, Backup in `SettingsTransfer.kt`. | F026, F017, F034 |
| Workspace / Fokus | `LauncherWorkspaceNavigation.State` modelliert den Rail-Zielzustand; `VDesignHomeView` hält und verwendet ihn, zeigt rechte Pane und Fokus. `MainActivity` liefert Callback- und Datenbindung. `WorkspaceAppTileView` repräsentiert tatsächlich installierte Apps. | F025, F044, F050–F052 |
| Medien | `AndroidTvMediaSource` interpretiert tatsächlich sichtbare `TvContract`-Programme/-Kanäle; `NuvioDashboardFeed` projiziert Kategorien/Datum; `MediaFeedRepository` verwaltet Quellen und Snapshots. Activity bindet Views, Poster und Öffnen-Aktionen. | F015, F027–F029, F052–F054 |
| Wallpaper / System | `MainActivity` bindet bestehende `Gallery`-, `VideoWallpaper`-, `WallpaperUpload`-, `HomeOverride`-, `Remap`-, HDMI- und Wetterfunktionen. Surface-/Receiver-/Upload-Owner dürfen nicht durch Blindextraktion wechseln. | F020, F024, F031–F034, F046–F049 |
| Mullvad-TV / Staging | `MainActivity.installEmbeddedVpnPane` bindet via Reflection `MullvadEmbeddedPanel.create(host)` im kombinierten APK; separate `VpnActivity` ist ein eigener Activity-Lifecycle. `tools/mullvad/prepare.py` erzeugt den integrierten Host; Original-VPN-/Service-Quellen bleiben im unveränderten Submodul. | F099–F111 |
| Regression / CI | Unit- und AndroidTest-Quellen, Python-/Shell-Tests und `.github/workflows/ci-integrated.yml` sind die Baseline-Gates. `AppPicturesCheck` testet bisher ältere All-Apps-Ansicht; M0 aktualisiert nur diesen Testpfad. | F001, F008–F012, F059–F091, F120–F123 |

## Architektur-Tasks (getrennt von den Datei-Tasks)

### A01 – Komponenten- und Abhängigkeitskarte (M0, M2–M7)
- Git-Baum ist mit einem Task je eigener Code-/Konfigurationsdatei in `FILE_TASKS.md` inventarisiert; Ressourcen und Assets bleiben einzeln aufgeführt.
- Vor Extraktionen echte Aufrufer, Ressourcen-IDs und gespeicherte Keys aus dem vollständigen Code bestimmen. Bloße Import-/Namensscans aus M0 sind nur Vorarbeit.
- **Status M0:** PASS – 123/123 Datei-Tasks mit vollständiger Inhalts-/Verantwortungsprüfung, Querverweisen und dokumentierten KEEP-/EXTRACT-/REPLACE-Entscheidungen.

### A02 – State, Lifecycle, Concurrency (M1 als Pflicht vor jedem Move)
- **Apps**: `MainActivity.refreshApps` lädt auf `io.execute`, schreibt `apps/APPS/sources/SOURCES` nach `ui.post`; `refreshVisibleApps` aktualisiert Sidebar und sichtbares Grid. Keine neue App-State-Kopie in einer weiteren View einführen. Kandidat M2: App-Lesen/Sortierpolicy hinter begrenzte eigene Fachklasse, Activity bleibt Verteilung/Owner bis expliziter Übergabe.
- **Workspace**: `VDesignHomeView.navigation` ist `LauncherWorkspaceNavigation.State`. Außerdem bestehen Fokus-Wiederherstellungsmarker und zuletzt gewählte Pane. Bei M5 nur EIN aktiver Besitzer, keine zweite Navigation-State-Quelle.
- **Medien/Poster**: Activity und Repository/Views haben getrennte Laufzeit-Lebenszyklen; bei M3 jeden Callback/Post/Cache-Owner mit Cancel-/Dispose-Reihenfolge erfassen. Null bei Dauer/Datum bleibt unbekannt, nicht null-zu-null-Mapping verlieren.
- **Wallpaper/System**: Receiver und ContentObserver werden bei Activity-Lifecycle verwaltet; Wallpaper-/Video-Surfaces und Upload-Server dürfen bei Move nicht länger gehalten werden als bisher.
- **Mullvad**: `MullvadEmbeddedPanel` registriert das `ActivityResult` am `ComponentActivity`-Host und bindet Host-Lifecycle-Observer. `VpnActivity` registriert ihr eigenes Result und ihren eigenen Service-Binding-Lifecycle. **Nicht** durch einen neuen Singleton oder gleichgesetztes `onResume` zusammenführen.
- **Status:** OPEN für präzise Thread-/Exception-/Callback-Tabelle jedes geplanten Moves; erst M1 darf diese Grenzen freigeben.

### A03 – Navigation / D-Pad / Apps / Settings (M1, M2, M5)
- Nachgewiesene Kette: `MainActivity.refreshVDesignNavigation → VDesignHomeView.bindNavigation`; Sidebar-Fokus erhält Preview-Callback; `MainActivity.renderAllAppsPane` erzeugt `WorkspaceAppTileView`-Elemente im `R.id.allAppsGrid`. `openAllApps` fokussiert Pane-Content.
- `VDesignHomeView.showWorkspace / restoreFocus / handleDpad` bewahrt Rail-/Content-/Home-/Back-Verhalten. Vor jeder Änderung Mapping `LauncherWorkspaceNavigation.State` ↔ View-Fokus mit Tests erfassen.
- Tests: `LauncherWorkspaceNavigationTest`, `VDesignFocusNavigatorTest`, `WorkspaceGridPolicyTest`; AndroidTest-Verhalten nur bei echtem Gerät/Emulator als PASS.
- **M0-Ist**: `AppPicturesCheck` referenzierte `R.id.appRow`, `renderApps` und einen früheren Banner-Listenpunkt. Produktiv existieren `R.id.allAppsGrid`, `renderAllAppsPane(Boolean)` und die aktuelle Icon/Reset-UI. Test-Anpassung betrifft nur Testcode.
- **Status:** PARTIAL, UI-/Fokuscharakterisierung im Emulator offen.

### A04 – Medienquelle, Paketzuordnung, Daten-/Startvertrag (M3)
- Source-/Publisher-Grenze: `AndroidTvMediaSource → NuvioDashboardFeed → VDesignHomeView / WorkspaceMediaPane`; generische `MediaFeedRepository`-Snapshots → `WorkspaceGenericMediaPane`.
- Öffnen erfolgt auf der Activity-Seite in `openVDesignMedia` und `openWorkspaceMediaItem` über vorhandene Intent-/Paket-Policies; deren bestehendes Verhalten **nicht** ändern. Nuvio wird über Publisher-/Paketidentität zugeordnet, nicht über angezeigten App-Namen.
- `TvContract.Channels.COLUMN_INTERNAL_PROVIDER_ID`: `pkg`, `pkg:in-progress`, `pkg:available`, `pkg:upcoming`. Progress, Veröffentlichungsdatum und ID gehören zur Quelle; uneindeutige Infos bleiben leer.
- Kandidat M3: Provider-Zugriff, Feed-Projektion, Artwork-Laden und UI-Callbacks als getrennte Grenzen beschreiben. **Keine Extraktion ohne gezielte Grenzfalltests und M1-Besitzvertrag.**
- **Status:** OPEN; kein Mediencode in M0 geändert.

### A05 – Mullvad-TV, Originalservice, Reflection und Staging (M6)
- Pinned Gitlink: `third_party/mullvadvpn-app` @ `aa47f606b1a159214c7279344128119fa91be5fd`; package `dev.plume.advanced` und ARM64-/R8-Gates unverändert.
- `tools/mullvad/prepare.py` kopiert ausgewählte Kotlin-Dateien und erzeugt Host-/Keep-Regeln. Jede spätere Move-Datei benötigt gleichzeitige Prüfung der expliziten Copy-Liste, Reflection-Einstiege und R8-Erreichbarkeit.
- `MullvadEmbeddedPanel.create` muss während Launcher-`onCreate` ausgeführt werden, bevor die Activity den STARTED-Zustand erreicht; `VpnActivity` ist ein unabhängiger Einstieg. Echte Tunnel-/Dienststatusquelle bleibt das Originalservice.
- Kandidaten nur bei identischer Semantik: TV-UI-Primitive aus `VpnTvStyle`; Controller-Code erst nach Lifecycle-Vergleich. **Keine zweite VPN-Implementierung.**
- **Status:** KEEP für Backend/Submodul, UI-Controllerkonsolidierung offen; Hardware-VPN = NOT_TESTED.

### A06 – App-Prefs/Backup/Fokus/Format (M1–M4)
- `MainActivity` verwendet `getSharedPreferences("home", MODE_PRIVATE)`; `icon_style`, `sidebar_color_icons`, `lock_pin`, `setup_shown` und andere Bestandskeys nicht ändern.
- `SettingsTransfer` und `AppPictures` behalten Serialisierung, App-Bildpfade, Löschsemantik und bestehende Tests unverändert. M0 passt Test-Selektoren an, nicht Formate oder Dateipfade.
- **Status:** OPEN für vollständige Key-/Restore-Callsite-Matrix.

### A07 – Regression und Gate-Nachweise (M0–M7)
- M0: `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `compileDebugAndroidTestKotlin` im bestehenden PR-CI-Workflow; keines der bestehenden Gates entfernt.
- Ferner: `tools/plume-install-test/run.sh`, Python-Unittests für Spotify/Mullvad-Staging und integrierter ARM64-Debug-/R8-Release-Check über bestehende CI.
- Ohne Android SDK/Projektcheckout vor Ort können diese Gradle- und Python-Gates **hier** nicht lokal ausgeführt werden. Die PR-CI ist der vorgesehene reale Nachweis. Ein abgeschlossener früherer Debug-Workflow eines anderen SHA zählt nicht.
- Geräte-/Tunnel-/D-Pad-Abnahme: **NOT_TESTED**.
- **Status M0:** CI PASS für letzten Code-/Quellreview-HEAD `ffd18b0b5c9279fc228f4c4465357ccc084a652c` ([Run](https://github.com/Justify87/Plume_Launcher/actions/runs/43881462010)). Abschlussdokumentation wird auf neuem PR-HEAD erneut geprüft; Geräte/VPN `NOT_TESTED`.

### A08 – Finaler Code-/Import-/Altrouten-Review (M7)
- Löschen nur, wenn ein konkreter Refactoring-Task einen alten Pfad nach belegtem Ersatz obsolet gemacht hat.
- Kein generelles Aufräumen von Kommentaren, Ressourcen, Legacy-Dokumentation, Dependencies oder Security-Themen.
- **Status:** NOT_STARTED (M7).

## Vorgesehene Move-Verträge: vor Umsetzung zu vervollständigen

| Kandidat / Herkunft | Zielverantwortung | Call- und Thread-Grenze | Zu erhaltendes Verhalten | Risiko / passende Checks | Phase |
|---|---|---|---|---|---|
| `MainActivity.loadApps/refreshApps` (F026) | App-Katalog/Quellermittlung; Activity nur UI-Delivery | Worker-`io.execute` → UI-`ui.post` | tatsächlich installierte Variante, Sortierung, Icon, Fokus | stale async result, App-Install/Remove; `LauncherAppsTest`, `LauncherWorkspaceNavigationTest`, AndroidTest | M1→M2 |
| `MainActivity` Settings-/Pane-Bindung (F026) | Kohärenter Settings-/Pane-Controller, unveränderte Key-/PIN-Stellen | UI-Thread, Seitenrückkehr/Fokus | `home`-Prefs, Backup/PIN/Upload, Rail sichtbar | Callback-Reihenfolge und `pageExit`; `SettingsTransferTest`, `AppPicturesCheck` | M1→M2 |
| `MainActivity` TV-Abfragen/Poster/Medienstart (F026) | Provider-Gateway / Feed-Projektion / Artwork-Bindung, keine doppelte Datenhaltung | IO/Provider → UI-Callbacks; Owner in M1 klären | Channel-Owner, Sortierung, Progress, Zeit, Deep-Link | Quellvermischung, Null-/Zeitsemantik; `AndroidTvMediaSourceTest`, `NuvioDashboardFeedTest` | M1→M3 |
| `MainActivity` Wallpaper/System-Koordination (F026) | begrenzte Feature-Koordination, kleine reine Helfer KEEP | Receiver/Callback/Surface/Upload-Lifetime | Bildschirm, Galerie, Wallpaper und Home/HDMI | Leaks, stale surfaces, falsche Stop-Reihenfolge; vorhandene Tests + AndroidTest | M1→M4 |
| `VDesignHomeView` und Activity-Navigationsbindung (F044,F026) | Single-Owner Navigation, fokussierte Pane-Rendering-Grenze | UI-Thread, View attach/detach, Fokusrestore | D-Pad/Back/Home, App-OK, Scroll | neue doppelte State-Quelle; `VDesignFocusNavigatorTest` und Emulator | M1→M5 |
| `MullvadEmbeddedPanel` vs. `VpnActivity` (F099,F101) | nur semantisch gemeinsame UI-Primitiven, Controller ggf. KEEP | Host-Lifecycle vs Activity-Lifecycle; Result vor STARTED | Originalservice, Consent, acht Funktionsbereiche | Timing-/Staging-/R8-Abweichung; `test_mullvad_prepare`, CI ARM64/R8, Hardware NOT_TESTED | M1→M6 |

**Freigaberegel:** Solange Ziel-Owner, Thread-/Lifecycle-Vertrag, charakterisierender Test und Gegenreview nicht nachgewiesen sind, keinen Move beginnen. `KEEP` ist ein korrekter Abschluss ohne zusätzliche Abstraktion.

## M0-Schlussabgleich

- 323 reguläre Dateien im Ausgangsbaum, 123 eigene Code-/Konfigurationsdateien; 200 Ressourcen/Assets/sonstige Dateien vollständig inventarisiert; 3 Audit-Dokumente ausschließlich im PR neu. Der Gitlink `third_party/mullvadvpn-app` bleibt unverändert.
- Quelllektüre: 123/123 `FULL_REVIEW` einschließlich 21 lückenloser Abschnitte der `MainActivity.kt`. Zu jedem Pfad eigener Befund, Aufrufer-/Abhängigkeitshinweise, State/Lifecycle, M-Task, Entscheidung und Prüfungen im Register. Für **M1–M6** sind genaue Move-Schnittstellen bewusst noch zu verifizieren, nicht als bereits implementiert behauptet.
- Änderungen nur F001 (AndroidTest-Kompilation im bestehenden CI) sowie F008/F009/F010/F012 (real obsolete AndroidTest-Annahmen). Keine UI-/Produktions-/VPN-/Dependency-/Signing-/Versionsänderung. Eigener kritischer Zweitreview: drei nach der Teständerung veraltete SHA-Einträge und irreführende `KEEP`-Labels korrigiert.
- [CI auf geprüftem Code-HEAD](https://github.com/Justify87/Plume_Launcher/actions/runs/43881462010): Unit, Lint, Debug, AndroidTest-Kompilation, Shell/Python-Staging und kombinierter ARM64-Debug/R8-Release/APK-Check **PASS**; Hardware, echter Tunnel und AndroidTest-Ausführung auf Gerät **NOT_TESTED**. Zweitreview nicht extern unabhängig.
- M0-Code-/Quellen-/CI-Gate besteht; finaler PR-HEAD nach dieser Dokumentationskorrektur separat gegen CI prüfen. M1 erst nach Maintainer-Merge.

---

## M1 – Verbindlicher State- und Lifecycle-Vertrag

**Baseline:** main @ a438073d3a6150d16efbfbc017df4910fb5f516f (M0 #51 gemergt). **Umsetzung:** keine produktive Extraktion in M1. Die folgenden Target-Owner sind künftige *Kandidaten*, keine bereits implementierten Klassen. Originalverhalten wird nur charakterisiert. M1-Tests F068 und F069 sind die gezielten Ergänzungen.

### A02 – State-/Lifecycle-Besitz nach aktuellem Quellcode

| State / Herkunft | Aktueller Owner und Thread-/Callback-Kante | Move-Grenze und zwingende Invariante |
|---|---|---|
| Apps, HDMI, Paketbilder, appsDirty | **F026 MainActivity** besitzt apps/sources und statische volatile Cache-Listen APPS/SOURCES. packageReceiver markiert dirty und plant 300-ms-Runnable; refreshApps liest loadApps/hdmiInputs auf *einzelnem* io-Executor, veröffentlicht per ui.post auf dem Hauptthread, aktualisiert Navigation/Grid. | **M2-Kandidat:** nur stateless App-Katalog-Abfrage extrahieren. Activity bleibt bis gesonderter Übergabe der **eine** mutable Katalog-Owner; keine zweite Liste als konkurrierender State. LEANBACK-LAUNCHER-Priorität, konkrete Start-Komponente, Paketvariante und Fokus identisch. |
| Preferences, Settings, PIN, Backup | **F026** hält lazy SharedPreferences **home**, pages/pageBack/pageExit, unlockedUntil und Settings-Pane. **F034 SettingsTransfer** hält bestehende Backup-/Restore-Formatierung; **F017 AppPictures** besitzt AtomicFile-Icon-/Banner-Bilder. | **M2:** nur nachgewiesene Settings-Aktions-/Rendering-Grenze verschieben; Keys, Datentypen, Unlock, PIN, App-Bildpfade, Callback-Reihenfolge unverändert. F017/F034 vorerst KEEP. |
| Rail, Pane, Fokus | **F044 VDesignHomeView** ist tatsächlicher mutable Owner von LauncherWorkspaceNavigation.State, lastSidebarKey, Focus-WeakReference und Medienkartenfokus. **F025 LauncherWorkspaceNavigation** ist nur pure Transition-Policy. **F026** liefert Daten/Callbacks und appsPaneSignature. | **M5:** Navigation bleibt ausschließlich in F044. Keinen zweiten Activity-/ViewModel-State anlegen. FocusRail, Right, Back/Home und Restore müssen gleich bleiben; alte detached Views dürfen nicht gehalten werden. |
| TvContract, Nuvio, RSS und Medienfeed | **F026** besitzt mediaFeed (F027: einziger quellgebundener, synchronisierter Snapshot-Owner), @Volatile nuvioProviderFeeds/nuvioDashboardFeed/tvFeedQueryFailed. **F015** wählt Programme/Kanäle, **F029** projiziert Nuvio-Kategorien. | **M3-Kandidat:** stateless TV-Provider-Abfrage statt Vermischung mit UI; Query auf bestehendem io, Bridge-Status + mediaFeed.replaceSource auf Worker, bindFeed/refreshVDesignNavigation über ui.post nur bei !isDestroyed. Keine neue Feed-Kopie, keine geänderte Publisher-, Datums-, Null-, Intent- oder Progress-Semantik. |
| Poster und Thumbnails | **F026** besitzt posterIo (3 Threads), synchronisiertes loading-URI-Set, posters (6 MiB LruCache), separates thumbCache (8 MiB). loadPoster setzt View-URI-tag, decodiert auf posterIo, räumt loading im finally auf, cached und schickt UI-Callback. | **M3-Kandidat:** nur als zusammengehörigen Loader mit Executor + Inflight-Set + Poster-Cache bewegen; ThumbCache nicht blind vereinheitlichen. UI darf nach Rebind nur bei tag==uri aktualisiert werden. onTrimMemory evictAll, onDestroy Handler-Posts vor Worker-Shutdown entfernen. |
| Wallpaper, Galerie, Upload, System | **F026** besitzt wallpaperLoad-Generation, io-Decode und ui-Apply, Observer/Receiver/Callback-Registrierung. **F047 VideoWallpaper** besitzt SurfaceHolder/MediaPlayer, **F049 UploadServer** Socket/Tempfile. | **M4:** wenn sinnvoll nur reinen Decode-/System-Teil bewegen, wallpaperLoad-Token und UI-Apply zusammenhalten; F047/F049 KEEP. Kein zusätzlicher Player-/Upload-Owner, keine Änderung am Release auf Pause/Destroy. |
| Mullvad-TV und Originalservice | **F099 MullvadEmbeddedPanel** wird in MainActivity.onCreate via Reflection angelegt, registriert Consent auf dem Launcher-ComponentActivity-Host vor STARTED, aktiviert Controller bei Sichtbarkeit und besitzt Host-Lifecycle-Observer. **F101 VpnActivity** registriert eigenen ActivityResult-Launcher und besitzt eigenes Binding/OnDestroy. Originalservice bleibt Quelle; F111 prepare.py steuert Staging/R8. | **M6:** F099 und F101 KEEP als *verschiedene* Lifecycle-Owner. Nur wirklich identische View-Primitiven in vorhandenem F108 VpnTvStyle zusammenfassen; kein gemeinsamer Consent-/Service-Singleton und kein zweites VPN-Backend. |

### A02 – Start/Stop/Callbacks: exakte Reihenfolgen, die spätere Moves nicht ändern dürfen

1. **onCreate / Direct Boot:** F026 registriert unlockReceiver, bevor der gesperrte Zweig ohne Credential-Prefs zurückkehrt. Im entsperrten Zweig folgen Workspace/Wallpaper-Initialisierung, setupVDesign samt *sofortiger* installEmbeddedVpnPane-Erstellung (Consent-Registrierung vor STARTED), dann Photo-Observer sowie Paket-, Netzwerk- und TV-Input-Callbacks.
2. **onResume:** resumed=true; Focus/VPN-Rückkehr prüfen; Apps ggf. über io aktualisieren, Feed und Navigation auf aktuellem UI-Thread binden, alte Poster bei Rückkehr neu anfragen; Wetter-/Theme-/Wallpaper-/Midnight-Callbacks planen und Focusrestore per ui.post.
3. **onPause:** Focus in F044 merken; resumed=false; VideoWallpaper.pause; refreshIfDirty, weatherTick, vDesignDayTick, themeTick und wallpaperTick entfernen. **onStop ist anders:** offenes Upload-UI schließen/Server stoppen, unlockedUntil=0, Remap.learn=null. Keine zusammengelegte Pause/Stop-Semantik (Systemdialoge).
4. **onDestroy:** unlockReceiver und nach ready packageReceiver, Connectivity-Callback, TV-Input-Callback sowie PhotoObserver abmelden; VideoWallpaper.clear, Upload/pageExit stoppen. ui.removeCallbacksAndMessages(null) erfolgt **vor** io.shutdownNow und posterIo.shutdownNow. Keine späteren MainActivity-UI-Posts erlauben.
5. **F044 View-Attach:** headerTicker wird onAttachedToWindow gestartet und onDetachedFromWindow entfernt; er gehört zur View, nicht zum Activity-Handler. Focusrestore nutzt WeakReference und überprüft attached/shown/focusable.
6. **Wallpaper async:** wallpaperLoad++ vor Decode auf io, UI-Apply überprüft !isDestroyed und gleiche Generation, verspäteter Fade-Callback erneut. F047 Surface wird separat pausiert/clear.
7. **Poster async:** loading.add → posterIo.decode → loading.remove (finally) → posters.put → ui.post (tag- und Destroy-Check); Avatar-/Poster-Callbacks an VDesign- und Workspace-Panes bleiben Hauptthread-gebunden.
8. **Mullvad:** Embedded-Result-Registration vor STARTED, Build ist nicht activate (erst onVisibilityAggregated); F099 Host onDestroy unbind/dispose. Separate F101-Activity hält eigenen Consent-Launcher, onPause-Split-Callback und onDestroy-Unbind; keine Controller-Fusion.

### A03/A04/A05/A06/A07 – Gate je geplanter M2–M6-Änderung

| Phase / Datei-Tasks | Source → Zielverantwortung; Thread-Grenze | Zu bewahrendes Verhalten und Risiko | Testnachweis / noch offene Geräteprüfung |
|---|---|---|---|
| **M2 F026/F017/F034/F050** | loadApps aus F026 → kleiner *zustandsloser* App-Katalogadapter; **bestehender io → ui.post** verbleibt bei F026. Settings nur als zusammenhängendes Rendering/Aktionsteil, Prefs/Seiten-Owner bleibt Activity. | Installation, richtiger App-Intent, Sidebar-Paket/Icons, PIN, Settings, Backup und Fokus; kein doppelter Katalog-State. | LauncherAppsTest, IconStyleTest, PinTest, SettingsTransferTest, AppPicturesCheck; App-Install/Remove und System-UI **NOT_TESTED**. |
| **M3 F026/F015/F027/F029/F045/F053/F054** | TvContract-Abfrage F026 → stateless Provideradapter auf io; F027 bleibt Snapshot-Owner, UI-Delivery über bestehenden Handler. Poster-Cache/Executor nur komplett verschieben. | Channel-/Publisher-Owner, Stable IDs, Progress, Termin, Null-Unknown, sortierte Feed-Sources, Intent/Ziel und View-Tag/Cache. | AndroidTvMediaSourceTest, NuvioDashboardFeedTest, MediaFeedRepositoryTest **inklusive M1-Snapshot-/Quellen-Isolation**; Live Provider **NOT_TESTED**. |
| **M4 F026/F020/F022/F024/F047/F049** | Ggf. nur Decode-Helfer aus MainActivity; wallpaperLoad/Handler/Surface/Upload-Owner bleiben jeweils eindeutig. | Direct Boot, Home/HDMI, Receiver/Video/Wallpaper/Galerie/Screensaver/Upload; Generation und Dispose nicht verändern. | RebootSetupAndWallpaperTest, VideoScenesTest, ScreensaverTimingTest, GalleryPreviewTest, SettingsTransferCheck; Surface/Hardware **NOT_TESTED**. |
| **M5 F025/F026/F044/F050–F054** | F026 als Callback-/Datenprovider, F044 als *einziger* Navigationsstate; F025 reine Policy **KEEP**. | Persistenter Rail, sofortige Pane-Vorschau, D-Pad/Back/Home, Scroll und Fokusrestore; keine doppelte Zustandsquelle. | LauncherWorkspaceNavigationTest **inklusive M1 Back/Focus-Transitions**, VDesignFocusNavigatorTest, WorkspaceGridPolicyTest; TV-Fokuslauf **NOT_TESTED**. |
| **M6 F099–F111** | Keine Controller-Verschiebung: F099 (Launcher-Host) und F101 (eigene Activity) bleiben getrennt; F108 Shared Style bereits vorhanden; F111 Staging-Owner. | Acht Bereiche, Originalservice, Consent vor STARTED, echtes VPN-State, Reflection/Keep/R8/ARM64. | test_embedded_mullvad_consolidation.py, test_mullvad_prepare.py, integrierter ARM64-/R8-CI-Build; echter Tunnel **NOT_TESTED**. |

**Charakterisierung M1:** F068 ergänzt Back-Content→Rail→Home und Wechsel von Pane mit altem Focus-Key; F069 ergänzt defensive Producer-Listen/Alias-Snapshot-Kopie, stabile alte Snapshots sowie unabhängiges Löschen einer Quelle. Diese JVM-Tests behaupten **keine** Activity-/Service-/TV-Hardware-Integration.

**Zweitreview:** Alle empfohlenen neuen Fachklassen sind nur optional und müssen vor M2–M6 anhand echten Diffs begründet sein. KEEP ist vollwertig. Keine Sicherheits-, Dependency-, UI- oder Build-Neugestaltung.
