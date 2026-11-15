# Persistent Workspace – Meilenstein-Reviews

**PR:** [#48](https://github.com/Justify87/Plume_Launcher/pull/48)
**Basis:** `main@6e733da7f2ad29c4f77ea15ca583afbb55355b24` (PR #46 bereits gemergt)
**Norm:** [aktiver Designvertrag](../v-design/DESIGN_CONTRACT.md)
**Gate:** *Kein Merge ohne kombinierten Build und gerätebezogene Abnahme.*

## M1 – Vertrag, Zustandsmodell und Auswahl der Nuvio-Version

- **Änderungen:** Komplett neues `LauncherWorkspaceNavigation.kt`, korrespondierende reine Unit-Tests, Designvertrag komplett ersetzt.
- **Ziel:** Fokus auf linkem Ziel wählt sofort Content; OK startet App oder betritt eine Inhaltsansicht; Back wechselt vom Inhalt zur Rail und dann Home.
- **Nuvio:** `NuvioInstallationPolicy` verwendet zuerst die nachweislich sendende TV-Channel-Paketkennung; mehrere Installationen ohne eindeutigen Provider dürfen nicht stillschweigend auf eine andere App zeigen.
- **Review:** Modell und UI-Semantik isoliert testbar, keine Änderung an vorhandenen VPN-Klassen. Der erste CI-Lauf [43723082014](https://github.com/Justify87/Plume_Launcher/actions/runs/43723082014) scheiterte an einer ungültigen Test-Referenz auf Kotlin-Nested-Typen; Testdatei wurde **komplett ersetzt**. Endgültiger CI-Pass muss auf dem neuen HEAD bestätigt werden.
- **Entscheidung:** `partial_ci_pending`.

## M2 – Die permanente Shell

- **Änderungen:** `VDesignHomeView` übernimmt alle rechten Inhaltsansichten als echte Kinder, zeigt nur den aktiven Content an, lässt die Rail darüber sichtbar und verwendet den getesteten Navigationszustand. Änderungen an `MainActivity` verlagern All Apps/Settings aus den ehemaligen Fullscreen-Overlay-Routen.
- **Korrekturen:** Aktive Home-Karten sind nur in Home sichtbar. Das neue D-Pad verlässt den rechten Inhaltsbereich erst am tatsächlichen linken Rand, nicht bei jeder Linksbewegung. Das ehemalige `returnTo`-Fokusfeld wurde nach einem CI-Compilerfehler wiederhergestellt.
- **Review:** Source-Kontrolle bestätigt identische View-Hierarchie statt zweiter Launcher Activity. Funktionale Shield-D-Pad-Abnahme ist noch offen.
- **Entscheidung:** `partial_ci_and_shield_pending`.

## M3 – Apps, Settings, Icon-Design

- **Änderungen:** Neue `WorkspaceAppTileView` ersetzt die alte All-Apps-Kacheloptik. Kompaktes `GridLayout` mit fünf Spalten ist in der rechten Shell gehostet. Settings XML als Vollbreiten-Inhaltsbereich anstatt Scrim-Overlay umgeschrieben; alte Aktionen, PIN und Pages bleiben erhalten.
- **Farbicons:** App-Glyphen werden unverfälscht aus installierten Packages geladen. Neue unabhängige, lokalisierte Preference `sidebar_color_icons` schaltet Farbe/monochrom in der Rail um.
- **Review:** Zustandswechsel und Fokusregeln quellseitig geprüft; App-Start und Settings-Navigation müssen auf Shield 2019 Pro abgenommen werden.
- **Entscheidung:** `partial_ci_and_shield_pending`.

## M4 – Medien, Bildmaske, Motion

- **Änderungen:** Hero-Kurve verkleinert (ca. 60–70 % auf ca. 44–52 % Farbfläche je Höhe); Textbreite und Titelgröße entsprechend verkleinert. Nuvio bekommt eigene, echte Kanalansicht; SmartTube/Spotify zeigen ausschließlich im `MediaFeedRepository` vorhandene Medien, ohne Mockups. Medienlinks dürfen nur im publizierenden Paket öffnen.
- **Performance:** Karten bleiben beim bloßen Wiederfokussieren ohne Datenänderung erhalten. Einfache 145–165 ms Transformationsanimationen, `ValueAnimator.areAnimatorsEnabled()` und keine Blur/Full-Layout-Animation.
- **Quellen:** Android-TV-D-Pad und Sichtbarkeit: https://developer.android.com/develop/adaptive-apps/guides/tv/build-adaptive-apps-for-tv ; TV-fokussierte Komponenten: https://developer.android.com/training/tv/playback/compose
- **Review:** Datenwahrheit und Intent-Grenzen in Kotlin-Unit-Tests spezifiziert; pixelgenauer Bildvergleich nicht ausgeführt.
- **Entscheidung:** `partial_ci_and_visual_qa_pending`.

## M5 – Mullvad innerhalb derselben Shell

- **Status:** **NICHT erfüllt.** Die Mullvad-Management-UI ist weiterhin an `VpnActivity : ComponentActivity`, deren `lifecycleScope`/ActivityResult-Registrierungen und ihre acht Unterseiten gebunden. Die momentan im rechten Bereich eingehängte `WorkspaceVpnEntryPane` ist ein **transparenter Übergangseinstieg**, kein nativer Mullvad-Inline-Controller. OK darauf öffnet noch die bestehende Mullvad Activity.
- **Ursächliche nächste Arbeit:** Controller von der Activity-Lebensdauer ablösen; originalen Repository-/Tunnelstatus über einen gemeinsamen Lifecycle-Owner an eine im Launcher gehostete View binden. Staging und standalone-Launcher-Variante testen. Erst danach die getrennte Activity aus regulärer Navigation entfernen.
- **Review:** Keine fiktive Mullvad-Verbindung oder neuer Tunnelcode erzeugt. Kein Nutzerauftrag zum Embedded-VPN als bestanden markieren.
- **Entscheidung:** `blocked_unimplemented`.

## Gesamtgate

1. Durchlauf `testDebugUnitTest lintDebug assembleDebug`, gepinnter kombinierter Mullvad ARM64/R8 CI-Build auf **letztem** PR-HEAD.
2. Auf Shield: Sidebar D-Pad, Fokus Home/App-Karte, App-Galerie 5 Spalten, Settings-Blättern/Back/PIN und tatsächliche Nuvio-Paketidentität.
3. Echte Screenshots gegen neue Nutzerreferenz, auch bei 720p/1080p; Frames und Memory/Startdauer prüfen.
4. Live-Mullvad-Status und Split-Tunneling ohne neue Security-/Privacy-Regressionsfehler.

**Schlussentscheidung bis zur Prüfung:** Draft / NO-GO. Quelländerungen und CI sind keine Geräte- oder Pixelabnahme.

## M3b – Responsive All Apps (Fortsetzung 15.11.2026)

- **Ursache:** Die erste neue App-Übersicht verwendete fünf feste 145-dp-Spalten, obwohl die Sidebar stets Platz beansprucht. Auf kleineren TV-Viewports und bei Overscan/abweichender Dichte konnte die fünfte Spalte abgeschnitten werden.
- **Lösung:** Den gesamten `renderAllAppsPane`-Renderer ersetzt. Die neue reine `WorkspaceGridPolicy` berechnet 1–5 Spalten und Kachelbreiten aus der realen Breite des rechten Viewports einschließlich 48-dp-Innenabstand. Grid-Signatur enthält die berechnete Geometrie; ein erneuter Render erhält den fokussierten Paketnamen.
- **Review:** Isolierte Grenzwerttests für 1100, 580, 90 dp und exakte Fünfspalten-Grenze erstellt. Keine Änderung an App-Launch-Intents oder VPN-Service. Der vorige PR-HEAD `0f94c71a` bestand Combined CI [43727194921](https://github.com/Justify87/Plume_Launcher/actions/runs/43727194921); der neue HEAD `4b3bddd9` hat zum Zeitpunkt dieser Dokumentation **noch keinen bestätigten CI-Lauf**.
- **Entscheidung:** `code_review_pass_ci_pending`. Ohne Screenshot-/Shield-Review kein Abnahme-PASS.

## Offene harte Zielabweichung

Die `WorkspaceVpnEntryPane` ist weiterhin **nur** ein Einstieg in die separate Mullvad Activity. Die verbindliche Anforderung „Mullvad vollständig rechts in derselben Shell“ ist deshalb **nicht erfüllt**. Die Mullvad-Repositories und Tunnelsteuerung dürfen nicht durch eine kosmetische Scheinimplementierung ersetzt werden. Eine echte Lifecycle-Entkopplung der Activity bleibt der nächste eigenständige Meilenstein.
