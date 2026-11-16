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
