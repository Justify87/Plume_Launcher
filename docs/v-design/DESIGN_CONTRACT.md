# Plume Launcher Advanced — Expressive TV Designvertrag

**Neuer verbindlicher Stand: 15.11.2026.** Das vom Nutzer in der Unterhaltung bereitgestellte
Bild (1536 × 864) ersetzt alle bisherigen V-Design-Mockups als **einzige visuelle Referenz**.
Die ältere Datei `docs/v-design/REFERENCE.png` bleibt ausschließlich als historischer
M00–M07-Nachweis erhalten; sie **darf nicht** als Vorlage für neue UI-Arbeit verwendet werden.
Die neue Nutzerbilddatei wurde nicht als Git-Binärdatei eingecheckt. Deshalb besteht **noch
kein Pixelvergleichsnachweis** für einen echten TV-Screenshot. Änderungen dürfen nicht als
„100 % pixelgenau“ deklariert werden, solange kein Referenzvergleich auf der Shield durchgeführt wurde.

## Home: exaktes Raster im Designraum

Koordinaten sind Referenzpixel für `VDesignCanvas` (1536 × 864), keine gerätespezifischen dp:

| Element | x | y | w | h |
|---|---:|---:|---:|---:|
| Sidebar (nur Icons) | 0 | 0 | 98 | 864 |
| Datumszeile | 1020 | 16 | 152 | 35 |
| VPN-Statuspill | 1209 | 17 | 198 | 37 |
| Uhr | 1429 | 11 | 87 | 46 |
| „Weiterschauen“ Titel | 131 | 94 | 365 | 37 |
| Hero | 108 | 145 | 655 | 365 |
| Fortschritt 2 | 781 | 142 | 303 | 170 |
| Fortschritt 3 | 781 | 330 | 303 | 177 |
| Trenner zu „Neue Folgen“ | 1104 | 94 | 1 | 413 |
| Neue-Folgen-Titel | 1132 | 94 | 365 | 37 |
| Neue-Folgen-Pill | 1424 | 94 | 87 | 37 |
| Neue Folge 1/2/3 | 1132 | 147/266/386 | 380 | 103 |
| „Demnächst“ Titel | 133 | 547 | 365 | 37 |
| Demnächst 1–6 | 133/379/611/842/1074/1306 | 605 | 228/214/212/213/211/205 | 218 |

## Verbindliche Bildsprache

- **Hintergrund** tiefes Anthrazit `#121317`, keine Gradienten, Frosting oder zusätzliche
  Launcher-Produktleiste.
- **Akzent** sanftes Limettengelb `#E7FF80`, für aktiven D-Pad-Fokus und echten Fortschritt.
  **Sekundär** Lavendel `#DCB7F7`; warme Pfirsichtöne für wechselnde Datums-Badges.
- Großes **Hero-Bild** rechts unter geometrisch geschwungener, hellgrüner Fläche links,
  schwarzer Titel und schwarze „Fortsetzen“-Pill. Die geschwungene Fläche wird als View
  gezeichnet; sie ist nicht Teil des Serienartworks.
- Nebenhelden: 2 kleine Full-Bleed-Bildkacheln übereinander mit dunkler Lesbarkeitsblende.
  Neue Folgen: 3 horizontale Zeilen mit Bild links, Text + „Neu“ rechts. Unten genau
  **eine Reihe aus 6** Bildkacheln mit Datums-Pill, Titel und Countdown darunter.
- **Kein Demo-Inhalt**: nur reale Nuvio-TV-Kanäle. Titel, Artwork, Folgennummern, Prozentwerte,
  Daten und Countdowns sind aus dem Provider abzuleiten. Fehlende Werte nicht erfinden.
- Datum/Uhr müssen lokal und fortlaufend aktualisiert werden. VPN-Transport darf nur als
  „VPN aktiv“ bezeichnet werden, wenn Android tatsächlich einen VPN-Netztransport bestätigt;
  der Netztransport allein beweist **nicht** eindeutig den Mullvad-Status.

## Interaktion und Unterseiten

Die Sidebar ist ausschließlich Icon-basiert. Reihenfolge: Home, Nuvio (wenn installiert),
SmartTube (wenn installiert), Spotify (wenn installiert), Alle Apps, Mullvad (wenn vorhanden),
Einstellungen. Long-OK auf Home bietet weiterhin Alle Apps und Long-OK auf Einstellungen
die System-/HDMI-/Power-Aktionen. Unsichtbare Ziele sind nicht fokussierbar.

D-Pad-Ziele basieren auf den tatsächlichen Bildschirmkoordinaten. „Demnächst“ paginiert nur
am linken bzw. rechten Ende des 6er-Streifens, ohne künstliche Einträge. Position und Medienfokus
bleiben beim Datenrefresh und beim Zurückkehren erhalten.

Launcher-Einstellungen und Mullvad-TV-Untermenüs teilen den matten Dunkelmodus:
`#1B1C21` Fenster, `#202126` Zeilen, `#F7F7F8` Text, `#B8BAC1`
Sekundärtext und `#E7FF80` Fokus. Kompakte Zeilen (ca. 44–46 dp),
schmale Zwischenräume, Scroll nur bei Bedarf, klar lesbare Seitentitel. Kurze
Animationsübergänge (ca. 145–165 ms) **ohne** Fokusversatz. Maintained UI-/Animationsbibliotheken sind zulässig, wenn sie tatsächlich helfen.

Mullvads originaler Dienst, gRPC, Relaylisten, Konten, WireGuard, Rust/JNI, Split-Tunneling
und VPN-Rechte sind weiterhin alleinige Autorität. Design-Umbau darf keine zweite
„VPN-Implementierung“ installieren und niemals einen unbestätigten Status behaupten.

## Technologie- und Änderungsfreiheit

Frühere Vorgaben „Framework Views only“, „keine AndroidX-Bibliotheken“, „keine
Compose-UI“, „kleine PRs“, „keine zusätzlichen Designkomponenten“ und
„ursprüngliches Plume-Layout erhalten“ sind für Plume Launcher Advanced
**aufgehoben**. Der Nutzer kann die bestehende UI auch vollständig ersetzen
lassen. Ziel ist die bestmögliche Referenztreue, nicht die Beibehaltung alter
Technik.

Geeignete Material-Komponenten, AndroidX, ein Bildlader, Animationsbibliotheken,
Custom Views oder selektive Compose-Module sind erlaubt. Vor Einführung prüfen:
Maintainerstatus/Lizenz, sinnvolle Laufzeit-/RAM-/APK-Kosten, Android-TV-D-Pad,
Barrierefreiheit, Android 8+, integrierten Mullvad-Gradle/Rust-Build und Wartbarkeit.
**Keine Bibliothek nur der Mode wegen erzwingen.** Der vorhandene native View-Ansatz
ist für diesen Entwurf weiterhin ein pragmatischer Ausgangspunkt.

Die komplette Startseite, Launcher-Settings und Mullvad-TV-Oberflächen dürfen
in einem zusammenhängenden PR geändert werden. Keine alte visuelle Konvention
stellt eine Architektur- oder Dateigrenze dar. Datenechtheit, VPN-Sicherheit,
Privatsphäre und technische Regressionstests bleiben verbindlich.

## Abnahme

1. `testDebugUnitTest`, `lintDebug`, kombinierter gepinnter Mullvad-Docker/ARM64-Build
   einschließlich der vorhandenen Release/R8-Regressionsprüfungen.
2. Shield-TV 2019 Pro: Fokus, Fernbedienung, 1080p und Overscan; Wallpaper-/Apps-/Settings-
   Overlay, Back/Home, Unterseiten sowie reales Mullvad Verbinden/Trennen.
3. Vergleich von **echtem** Launcher-Screenshot mit der **neuen** Nutzerbildvorlage
   inklusive Typografie, Bildmasken, Abstände, Kartenanordnung und Animationen.

Punkte 2 und 3 benötigen reale Gerätetests und dürfen nicht als bestanden bezeichnet werden,
wenn lediglich JVM-Tests bzw. CI erfolgreich sind.
