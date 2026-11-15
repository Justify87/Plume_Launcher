# Plume Launcher Advanced — verbindlicher Shell- und Designvertrag

**Stand: 15.11.2026 · Shell V2 · PR #47.** Die vom Nutzer bereitgestellte
1536×864-Bildvorlage gilt für die Home-Oberfläche. Der Nutzer hat das
Navigationsmodell anschließend verbindlich erweitert: Die linke Sidebar
ist eine **permanente globale Navigation**; nur der rechte Inhaltsbereich
wechselt, und zwar bereits beim **Fokussieren** eines Sidebar-Icons.

## Invarianten

1. **Eine sichtbare Shell:** Home, Apps, App-Vorschauen, Launcher-Einstellungen und
   Mullvad-TV-Ansichten teilen dieselbe Activity und dieselbe schlanke Icon-Leiste
   (Referenzbreite 98/1536). Das frühere Fullscreen-All-Apps und das Settings-Scrim-
   Overlay sind keine produktiven Navigationspfade mehr. Sie werden als rechte
   Inhalte in `VDesignHomeView` eingehängt.
2. **Fokus ist Vorschau, OK ist Aktion:** Hoch/Runter in der linken Leiste tauscht
   ohne Klick die rechte Ansicht. Rechts wechselt in die Inhaltsbedienung. OK auf
   einem App-Icon startet exakt die ausgewählte installierte App; OK auf Alle Apps
   oder Einstellungen bringt den Fokus in die rechte Ansicht. Links/Back führt
   sicher in die Navigation zurück. Keine leeren fokussierbaren Dummykacheln.
3. **Mullvad eingebettet:** `MullvadEmbeddedPanel` wird von der existierenden
   `MainActivity` als echte native rechte Ansicht gehostet. Alle acht Mullvad-
   TV-Bereiche nutzen weiterhin die Original-Repositories und den Originaldienst
   samt Android-VPN-Rechteprüfung. Die separate `VpnActivity` bleibt ausschließlich
   als kompatibler externer Einsprung für Android-Benachrichtigungen oder als
   Fallback erhalten. Sie ist *nicht* der normale Sidebar-Navigationspfad.
4. **Nuvio-Paketwahl:** Voll- und Debug-/Play-Store-Pakete sind verschiedene
   Android-Apps mit getrennten Anmeldedaten. Niemals unsortiert das erste Paket
   starten. Priorität für produktive Pakete; ausdrückliche Nutzerwahl unter
   Einstellungen → Anwendungen → Nuvio-App wählen und über langes OK am
   Sidebar-Icon, wenn mehrere Installationen vorhanden sind.
5. **Icons:** Systemziele verwenden ein einheitliches, gut erkennbares
   Outlineset mit vergleichbarer Strichstärke. Apps zeigen standardmäßig
   ihre **echten** farbigen Paket-Icons. In Einstellungen → Anwendungen →
   App-Icons in der Sidebar ist die einfarbige Ansicht auswählbar;
   langes OK auf App-/Alle-Apps-Icon wechselt die Darstellung ebenfalls.
6. **Wahre Daten:** Nuvio Preview-Kanäle und vorhandene Android-TV-/SmartTube-
   Publikationen sind die einzigen Medienquellen. Kein statisches Artwork
   oder fingiertes »Neu«, keine erfundenen Empfehlungen oder VPN-Zustände.
   `TRANSPORT_VPN` darf als Android-VPN-Transport, aber nicht als bestätigtes
   »Mullvad verbunden« dargestellt werden. Der eingebettete VPN-Bereich verwendet
   Mullvads originale Live-`TunnelState`-Flows.
7. **Keine stillen Funktionsverluste:** App-Start/Deep-Links, TV-Input/HDMI,
   Benutzer-PIN, TV-Listings, Wallpapers, Home-/Back-Einrichtung, Einstellungen,
   Original-Mullvad-Login/Server/DNS/Netzwerk/Split-Tunneling, Nutzerdaten,
   VPN-Sperre und Signierung bleiben erhalten.

## Home-Referenz in 1536×864 Designkoordinaten

| Element | Geometrie (x,y,w,h) |
|---|---|
| Permanente Icon-Leiste | 0,0,98,864 |
| Weiterschauen-Titel | 131,94,365,37 |
| Hero | 108,145,655,365 |
| Fortsetzen rechts (2) | 781,142,303,170 / 781,330,303,177 |
| Verfügbare Folgen (3) | 1132,147/266/386,380,103 |
| Demnächst (6) | x=133/379/611/842/1074/1306; y=605; h=218 |
| Uhr, Datum, ehrlicher VPN-Transport | obere rechte Ecke |

Die ursprüngliche Hero-Maske verdeckte bis zu 70 % des Posters. Die neue
sacht geschwungene Akzentfläche beansprucht maximal 50 %, sodass wenigstens
die rechte Posterhälfte sichtbar bleibt. Der kleinere Titelblock ist
dementsprechend einzupassen, nicht über das Foto zu legen.

## Komponenten und Animationen

- Matte Anthrazit-Oberflächen `#121317`, lesbares Weiß `#F7F7F8`,
  graue Hilfstexte, limegrüner Fokus `#E7FF80`; Lavendel/Pfirsich
  nur für kleine, sinnvolle Akzente.
- Aktueller Fokus: klare Hintergrundfläche, leichte Skalierung
  (maximal ca. 1,03×), keine flackernden Ringe, keine Glas-/Unschärfepanels.
- Navigations-/Seitenübergänge: ca. 135–165 ms, Opacity/Translation nur
  auf dem sichtbaren Panel; keine aufwendigen Per-Frame-Blurs.
  Abgebrochene Animationen müssen auf `alpha=1`, `translationX=0`
  zurückgesetzt werden. `ValueAnimator.areAnimatorsEnabled()` beachten.
- Einstellungen und Mullvad: kompakte, fernbedienungstaugliche Zeilen
  mit lesbarer Zustandsanzeige, scrollbaren Listen und sichtbarem
  D-Pad-Fokus. Innerhalb von Mullvad ist eine schmale zweite
  Funktionsliste erlaubt, weil acht getrennte Fachbereiche vorhanden sind.
- Bibliotheken nur, wenn notwendig. `androidx.activity` ist für die
  direkte Integration originaler lifecycle- und ActivityResult-gebundener
  Mullvad-Komponenten ausdrücklich gerechtfertigt. Kein zweiter VPN-Client,
  kein Material-Komponenten-Framework nur der Optik wegen.

## Abnahmeregeln

**Code**: JVM/Gradle, Android Lint, Shell-/Nuvio-Unit-Tests, Python-Stage-Tests,
kombinierte Mullvad-ARM64-Debug/R8-Release-CI. **Hardware**:
NVIDIA Shield TV Pro 2019, Home-Rolle, reale Nuvio-Varianten und Nutzerlogin,
D-Pad-Fokus beim bloßen Hervorheben, App-Grid, Settings-Unterseiten,
Mullvad-Konto/Standort/VPN-Connect/Disconnect, Split Tunneling, Aufwachen/Back,
720p/1080p, Overscan und GPU-Framestats.

**Pixeltreue** wird nur nach Vergleich eines echten Screenshots des laufenden
Launchers mit der neuen Bildvorlage behauptet. Die Vorlagengrafik im Chat
ist als Originalbild sichtbar, aber kein Git-Binärasset auf dem Branch.
Insbesondere dürfen CI-/Source-Checks nicht als Shield-Abnahme ausgegeben werden.

Historische Dokumente `docs/v-design/{ROADMAP,STATE,REVIEWS,DECISIONS,...}.md`
aus M00–M08 und die alte `REFERENCE.png` dokumentieren nur den
alten Entwicklungsstand und **überschreiben diesen Vertrag nicht**.
