# V-Design — Entscheidungen (ADR-light)

## D-001 — Ein Home-Design
**2026-11-13.** Nur das V-Design aus `REFERENCE.png`; Classic und alte Overview samt Modusumschalter nach M06 tatsächlich entfernen. Hintergrund: verbindlicher Nutzerauftrag. Keine unbegründete Entfernung von All Apps, HDMI, Wallpaper, Uhr-Sonderfunktionen, Setup, Mullvad.

## D-002 — Provider statt private Nuvio-API
**2026-11-13.** Die drei Nuvio-Preview-Channels anhand Channel-`internal_provider_id` und Laufzeitpaket lesen. Legacy bleibt in Nuvio eigenständiger Mischkanal. Keine Nuvio-Codeänderungen und kein zusätzliches IPC/Netzwerk. Kanalberechtigungen und ggf. fehlende Metadaten offen behandeln.

## D-003 — Bild als unveränderte Norm
**2026-11-13, am selben Tag verifiziert.** Das ZIP lieferte ein `REFERENCE.png` mit 1536×864 und SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`. Die identischen 249663 Bytes wurden später als `Plume_V_Design_REFERENCE_original.png` hochgeladen, Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18`. Die JPEG/JFIF-Signatur `FF D8 FF E0` erklärt, dass der ursprüngliche Dateiname eine irreführende `.png`-Endung trägt; **keine Neukodierung erlaubt**. Der Nachtrag legt denselben Git-Blob unter `docs/v-design/REFERENCE.png` ab und entfernt den Alias. Normbild ist Referenz, kein Live-Wallpaper und kein Testartefakt einer realen App-UI.

## D-004 — Normtext schlägt SmartTube-Mockupbeispiel
**2026-11-13.** Das Referenzbild selbst zeigt exemplarische SmartTube-Videokarten, obwohl die schriftliche Pflicht eindeutig „SmartTube **nur** als App-Verknüpfung in Sidebar, **nicht** als Content-Kachel“ lautet. Entscheidung: Bildgeometrie/-design normativ, Inhalte sind nicht normativ. Kein SmartTube-Datenimport und keine Fake-Nuvio-Kacheln; das ist eine **bewusst dokumentierte inhaltliche Abweichung**. Für M01/M04/M07 berücksichtigen.

## D-005 — Archivierte Mullvad-Docs nicht duplizieren
**2026-11-13.** Die zuletzt gemergte Archivierung `fbf2473` verschob `docs/mullvad-integration/*` nach `docs/archive/legacy-features/mullvad-vpn/*`. Historische M12-Abnahme bleibt als Archiv erhalten; neue V-Design-Dokumente referenzieren den echten Pfad. Submodul unberührt.

## D-006 — Synthetische Position ist keine Dauer
**2026-11-13.** Nuvio publiziert aus Fortschrittsprozent mitunter synthetische `100000ms` Duration; ohne zusätzliche Gewissheit darf Plume daraus **keine 100-s-Laufzeit oder Restminuten** zeigen. Prozent kann zulässig sein. M02 implementiert Testfälle.

## D-007 — M00-Gate nach verfrühtem Merge schließen
**2026-11-13.** [PR #32](https://github.com/Justify87/Plume_Launcher/pull/32) wurde tatsächlich gemergt, obwohl die Bilddatei darin fehlte. Ein separater, nicht gemergter `feature/vdesign-m00-reference-completion`-PR behebt ausschließlich Bildpfad und Dokumentations-Evidenz auf dem aktuellen `main`-HEAD; kein Reset und keine implizite Mergefreigabe. Der erste CI-Run des alten PRs endete `cancelled`, **der spätere finale Run 43507886182 war `success`**. Dennoch war das Referenz-Gate noch offen; also kein vollständiges M00-`passed` vor Ergänzung und deren CI. M01 bleibt bis zum nachgeholten M00-Abnahmecheck zurückgestellt.

## Weitere Entscheidungen
Fortlaufende ID, Datum, Problem, verworfene Alternativen, Beschluss, Folgen/Abweichung und Testbeleg eintragen. Änderungen am Design nicht stillschweigend vornehmen.


## D-008 — Exakte Channel-Provider-Identität, 2026-11-13 (M01)
Ein Kanal gilt nur dann als Nuvio-Kategorie, wenn `package_name` aus der geprüften expliziten Installationsliste stammt und `channel.internal_provider_id` exakt `package + suffix` ist. `program.internal_provider_id` ist eine andere, programmspezifische ID. Lokalisierte Namen sind ungeeignet, ebenso Substring-Erkennung ähnlich benannter Apps. Moderne reale Nuvio-Pakete siehe `DATA_CONTRACT.md`. Bestehende historische Aliase bleiben explizit zugelassen, ohne fremde Apps zu erkennen.

## D-009 — Opt-in/Legacy-Fallback und Nicht-Rendering, 2026-11-13 (M01)
Neue Vorschau-Feeds werden nur gelesen, wenn `browsable == true` aus dem TV-Provider geliefert wird. Fehlende Browsable-Spalte gilt nicht automatisch als genehmigt. Keine `requestChannelBrowsable`-Aufrufe aus Plume, kein Overlay und keine eigene Nuvio-DB-Abfrage. Legacy/Watch Next liefert ausschließlich bei positiver Progress-Evidenz einen **In-Progress**-Fallback bei fehlendem neuen Kanal. Available/Upcoming werden niemals aus Misch- oder Namensdaten rekonstruiert. `NuvioProviderFeeds` bleibt bis M02/M03 nicht gerendert, damit alte UI/Classic ohne neues Kachellayout erhalten bleibt. M02 muss Quelle/Sortierung/Dedupe/Release-Datum weiter verifizieren.

 
## D-010 — Separate M02-V-Datenform statt Alt-Feed-Regeln, 2026-11-13

`NuvioDashboardFeed` erhält drei stets vorhandene Bereiche, `NuvioMediaItem` Episode/S-E, tatsächliche Zeitfelder, Beschreibungen, Artwork, Releases und Quellreihenfolge. Es wird ausschließlich aus bereits genehmigten M01-Kanälen projiziert. Bestehende `MediaCategory`/`MediaFeedRepository` mit Classic/SmartTube und per-App-Quoten bleiben bis zur UI-Migration erhalten, statt sie innerhalb M02 quer zu refactoren. Dedupe verwendet die Nuvio-programmspezifische Provider-ID mit Episoden-Suffix und optional erkanntes Intent-`S.contentId`, nie bloßen Titel, generisches Intent oder TV-`_id`. Bei unbekannter Identität keine unbewiesene Zusammenführung. Nuvios upstream `distinctBy(contentId)` kann vorab unterschiedliche Next-Ups derselben Serie unterdrücken; kein privater DB-Zugriff als Workaround.

## D-011 — Release-Evidenz, synthetische Dauer und lokaler Tag, 2026-11-13

Kategorisierter Available-Channel bedeutet „verfügbar“, nicht „gerade neu“. Lokalisierte `short_description` inklusive „New Episode“ beweist nicht das interne `isReleaseAlert`-Flag. Daher in M02 `showNewBadge=false`, bis der TV-Provider echte maschinenlesbare Evidenz bietet. Nuvios 100000-ms-Fallback ist nicht von echter 100-s-Laufzeit unterscheidbar: Prozent anzeigen, **nie** echte Laufzeit/Restzeit aus diesen Werten. `release_date` ist streng parsebares `LocalDate`; kein ableitbarer Termin aus `last_engagement_time` oder Quellgewichtung. Tageszählung via `Instant+ZoneId`, neue UI muss beim Rebind/Resume und einmaligen lokalen Tageswechsel frisch auswerten, kein Dauerpolling.

 
## D-012 — Widersprüchliche TV-Progress-Daten nicht als 100% darstellen, 2026-11-13

Der vorhandene Classic-`Prog`-Parser clampte rechnerisch ungültige `playbackPositionMillis > durationMillis` auf 100 Prozent. Ein solcher Wert darf im neuen V-Dashboard keine Fortschrittsevidenz sein. Die M02-Projektion verwirft in diesem Fall sogar bereits befülltes `progressPercent`; die originale Classic-Laufzeitlogik bleibt wegen Meilenstein-Scope unangetastet. Regression mit explizit 100%-geklemmtem Eingabewert ergänzt.


## D-013 — M03 als eingeblendete einzelne View-Shell, 2026-11-13

Der existierende `activity_main.xml`-Host ist stark mit Classic-/Overview-/Topbar-/All-Apps-/Settings- und Wallpaper-Lebenszyklus verwoben. Ein vollständiger XML-Austausch in M03 hätte unbegründete systematische Regressionen verursacht. Deshalb liegt die **eine sichtbare** V-Design-Shell als leichte, klassische `FrameLayout`-View vor dem unsichtbaren Legacy-Home, jedoch unter bestehenden Overlays. Das Original-`GlassLayer` bleibt bei Root-Index 2; Direct-Boot-Guard führt noch vor der Host-Initialisierung zurück. Dies ist keine auswählbare zweite Oberfläche. Alt-Hosts/Pref `home_layout_mode` bleiben rein intern bis M06 und werden dann strukturiert entfernt. Kein Live-Blur, keine neue Bibliothek, kein Submodul-Eingriff.

## D-014 — Referenzanker, Sidebar-Aktionsschutz und neutrale M03-Karten, 2026-11-13

Das tatsächliche 1536×864-JPEG-Mockup wurde angesehen und SHA-geprüft: Sidebarbreite 218 (~14,2 %), sichtbare Menüanker y94/161/223/285/347, drei Medienreihen 1+2/3/4×2, gerundete Matte-Gradienten, dezente Labels, keine globale Topbar. `VDesignCanvas` passt proportional an die gegebene Host-Fläche; Android-dp ist nicht identisch mit Bildpixeln. Die 14 M03-Karten sind **inert und ohne Serien-/Poster-Fallback**; erst M04 verbindet echte `NuvioDashboardFeed`-Medien und sichere Aktionen. SmartTube ist ausschließlich Sidebar-App. Fehlende App-/VPN-Aktivitäten erscheinen nicht als fokussierbare Ziele. `Alle Apps` über langes OK auf Übersicht, HDMI/Sources vorläufig über langes OK auf Einstellungen, ohne erfundenen sechsten Menüpunkt. Real-App-Screenshot und vollständiger D-Pad-/Shield-Test bleiben offen.


## M04 – D-014: echte Karte statt datenfalschem Referenzfoto (2026-11-14)
- Bewahrt die 1536×864 Geometrie aus `REFERENCE.png`, rendert nur `NuvioDashboardFeed`; Hero = erstes sortiertes IN_PROGRESS-Element, kein zweiter Home-Modus.
- `Neu` bleibt bis zur echten typisierten Publisher-Information deaktiviert. Bei fehlendem Datum/Progress/Bitmap nicht raten; S/F auch bei Film nicht erfinden.
- Verwendet bestehenden RGB_565 Poster-LruCache und UI-Handler statt einer zweiten Fetch-Engine; URI in `VDesignCardText.supportedArtwork` begrenzen.
- UPCOMING `launchMode=detail` und kein Playback-Fallback, auch bei malformed intent. Visueller Fokus ohne teure Blur-/Scale-Effekte.
- Acht UPCOMING-Slots werden für mehr Einträge versetzt gebunden; vollständige D-Pad-Navigation sowie Emulator-/Hardware-Abnahme sind explizit M05/M07/M08 vorbehalten.


## D-015 — Pure geometrische Fokusmatrix statt Framework-Heuristik (M05, 2026-11-14)

**Problem:** M04 brachte 14 unterschiedlich breite, wiederverwendbare Card-Slots; `HomeFocusNavigator` und `OverviewRowsNavigator` beziehen sich auf die vor M03 sichtbare Legacy-Hierarchie. Androids implizite Fokusfindung oder die alten Row-Indizes können beim V-Hero, Leersektionen, UPCOMING-Paging und View-Rebuild springen. **Entscheidung:** Kleine reine Kotlin-Projektion `VDesignFocusNavigator` arbeitet auf den tatsächlichen V-Design-Canvas-Koordinaten, horizontaler Breitenüberlappung und den vier visuellen Reihen. `VDesignHomeView` führt Zielwahl nur auf tatsächlich gebundenen/attached Fokus-Views aus und hält Sidebar-/Section-/Medien-Key statt View-Identität. `MainActivity.dispatchKeyEvent` priorisiert diese Shell vor dem Legacy-Navigator; Alt-Hosts bleiben bis M06 technisch bestehen. **Abweichung:** Keine neuen Animationen, keine Änderung an den Normankern. **Evidenz:** Neue JVM-Tests/PR #38, reale Shield-/Emulator-Fernbedienung bis M07/M08 pending.

## D-016 — Harte Publisher-Intent-Grenze und erreichbare versteckte Topbar-Aktionen (M05, 2026-11-14)

**Problem:** TV-`intent_uri` enthält fremdgesteuerte Intent-Parameter; bei UPCOMING darf niemals Playback vorab stattfinden. Die bislang unsichtbare Topbar versteckte Power, System-/WLAN-Settings und Home-Setup. **Entscheidung:** Nur explizite Nuvio-`com.nuvio.tv.MainActivity` mit passendem Runtime-Paket, korrekter VIEW-Aktion und ohne fremdes URI-Scheme/Selector wird akzeptiert. Ein *neuer* Intent kopiert ausschließlich vom Nuvio-Publisher belegte skalare Felder, `UPCOMING` setzt Detail ohne Video-ID, In-Progress/Available Stream nur bei belegtem `launchMode=stream` mit Video-ID und Titel. Unsichere/fehlende Daten ergeben Detail oder Unavailable-Meldung, nicht generischen Stream. `Einstellungen`-Long-OK öffnet ein vorhandene Systemeinstiege verwendendes Menü für Quellen/HDMI, OS-/WLAN-Settings, Power und Home-Einrichtung; `Übersicht`-Long-OK öffnet Alle Apps. Norm-Sidebar bleibt bei maximal fünf Einträgen. Keine zusätzlichen Berechtigungen, kein neues VPN-Modul. **Tests:** Quellreview gegen Nuvio-Channel-Publisher plus reine Launch-Unit-Tests; echte Shield-Intent-/HOME-Abnahme offen.
