# Referenzbild-Review (15.11.2026)

## Quelle und Grenzen

Das direkt vom Nutzer hochgeladene **1536 × 864**-Bild wurde als JPEG-RGB
analysiert (trotz lokaler `.png`-Dateiendung). SHA-256 der Originalbytes
während dieser Arbeit:

`e47c60b36554957ef362aa61c72a89e765902dfac587f4f00c5547a2bf9e3ff7`

Es handelt sich um ein **Mockup mit Beispielmotiven**, nicht um einen
Screenshot des tatsächlich laufenden APKs. Die Datei konnte über die
GitHub-Textschnittstelle nicht als Binärdatei versioniert werden. Eine
pixelgenaue Abnahme bleibt ausdrücklich offen, bis ein **echter
Emulator-/Shield-Screenshot gegen dieses Referenzbild** überlagert wird.

## Gemessene Quellfarben (JPEG-Stichproben, ungefähre Werte)

| Fläche | Bildkoordinate | RGB/Hex |
|---|---|---|
| Basis-Hintergrund | (900, 80) | `#131419` |
| Basis-Hintergrund unten rechts | (1520, 800) | `#111216` |
| Hero-Oberfläche oben links | (127, 170) | `#E0E5BD` |
| Hero-Oberfläche unten links | (127, 465) | `#B9C579` |
| Limette, Datums-Pill | (150, 630) | `#E6FA7D` |
| Lavendel, „Neu“-Badge | (1440, 209) | `#D1B5EF` |
| Pfirsich, Datums-Pill | (1375, 629) | `#FFE1C9` |
| Bereichstrenner | (1104, 200) | `#3B3C41` |

Die Hero-Fläche weist einen deutlich erkennbaren weichen Helligkeitsverlauf
auf und ist **nicht** einfarbig. Die neue Canvas-Implementierung verwendet
eine einfache Shader-Tönung, statt eine große zusätzliche Bitmap auszuliefern.
Abweichungen durch JPEG-Kompression/Farbmanagement sind zu berücksichtigen.

## Implementierung gegen Referenz

- [x] Schmale Icon-Rail; tatsächliche Shortcuts werden ohne Lücken angeordnet
- [x] 1 großer + 2 kleine In-Progress-Karten; 3 Folgen rechts; 6 Upcoming-Karten in 1 Zeile
- [x] Hellgrüne gebogene Hero-Fläche mit weichem Farbverlauf; reale Poster bleiben dynamisch
- [x] Matte Background-Palette, lime-farbiger Fokus und Fortschritt, passende Daten-Pills
- [x] D-Pad-Seitenwechsel im Upcoming-Streifen verschiebt exakt einen Titel pro Schritt
- [x] Verfügbarkeitszahlen und „Neu“-Labels ohne erfundene Release-Alerts
- [x] Gleiche kompakte, matte Sprache für Launcher-/Mullvad-Settings und All Apps
- [ ] Originalbild byte-identisch im Repository versioniert
- [ ] Echter Emulator- oder Shield-Screenshot mit Overlay-/Geometrievergleich
- [ ] TV-Aktivität, Layout-Overscan, Kontrast/Schriftgrößen und Mullvad-Tunnel live geprüft

## Bewertungsmaßstab

Ein erfolgreicher Kotlin- oder Rust-Build bestätigt nur Programmier- bzw.
Integrationskonsistenz, **nicht** visuelle 100-%-Identität. Bei der echten
Abnahme zuerst die Boxen aus `DESIGN_CONTRACT.md` gegen
den 1536×864-Viewport vergleichen, dann Font, Bildbeschnitt, Verlauf und
Fokus mit denselben App-Inhalten manuell prüfen. Fehlende Verfügbarkeit und
VPN-Status werden niemals als „Neu“ bzw. „Mullvad verbunden“ erfunden.
