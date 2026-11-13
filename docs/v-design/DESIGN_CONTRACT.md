# V-Design — verbindlicher Designvertrag

**Stand 2026-11-13 (M00-Nachtrag, ursprüngliches ZIP und GitHub-Bytes geprüft):** Ein einziges V-Design ersetzt in M06 Classic und Overview. `docs/v-design/REFERENCE.png` ist die **normative, byteidentische Referenz** (1536×864, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`, 249663 Bytes). Die Dateiendung `.png` stammt aus dem Original, **der tatsächliche Dateicontainer ist JPEG/JFIF** (Magic `FF D8 FF E0`). Das Bild wird absichtlich nicht umkodiert, weil der Binär-SHA sonst nicht identisch wäre. Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18` wurde über GitHub-Base64 geprüft und im Image-Commit `e1c13b2783aa632c2cbf391b10eb923426f8fc81` am kanonischen Pfad referenziert. M00 bleibt nur bis zu echten neuen PR-/CI-Nachweisen `partial_pending_pr_ci`.

## Gemessene Layoutanker (Referenzpixel, linke obere Ecke = 0/0)

| Fläche | x | y | Breite / Höhe | Verhalten |
| --- | --- | --- | --- | --- |
| Referenzcanvas | 0 | 0 | 1536 × 864 | 16:9, keine px-zu-dp-1:1-Annahme |
| Sidebar + Trennkante | 0–218 | 0–864 | ~218 × 864 = 14,2 % | links, vertikale Aktionen |
| Hauptfläche | 254–1510 | 0–864 | ~1256 × 864 | ohne Launcher-Logo/zweiten Header |
| Weiterschauen-Titel | ~263 | ~29–54 | kompakt | Icon, Typografie ~23–25 Referenzpixel |
| Hero, fokussiert | 255–906 | 74–336 | ~651 × 262 | ≈52 % der Kartenzeilenbreite, Fokus ohne Sprung |
| Hero-Nachbar 1 | 919–1206 | 83–328 | ~287 × 245 | ≈23 % der Zeilenbreite |
| Hero-Nachbar 2 | 1221–1508 | 83–328 | ~287 × 245 | ≈23 % der Zeilenbreite |
| Neue-Folgen-Titel | ~263 | 355–387 | kompakt + Zähler | keine Beschreibung |
| Neue-Folgen-Karten | ~258–1510 | 403–543 | drei ~400 × 140 | gleichbreit innerhalb dieser Reihe |
| Demnächst-Titel | ~263 | 566–601 | kompakt + Zähler | keine Beschreibung |
| Demnächst-Mini-Karten | ~258–1510 | 612–830 | vier Spalten × zwei sichtbare Reihen | Rest via Scroll |

**Hinweis zu Messwerten:** Kontrastkanten und anti-aliasingbedingte Ränder sind Näherungen; die realen Bildpixels sind maßgeblich. Die UI muss proportional in die tatsächlich nutzbare TV-Fläche übersetzt werden (Systemleisten, Overscan/Safe Insets, TV-Dichte, Font-Scale berücksichtigen). Relative Hierarchie ist wichtiger als exakte dp-Zahlen. Visueller Real-App-Screenshot und Referenz-Overlay erst in M07, jetzt `not_run`.

## Visuelles System

- Tiefer schiefer-/blauvioletter, sehr weichgezeichneter Hintergrund; keine überladene Topbar oder doppelte `Übersicht`-Überschrift.
- Matte, gerundete Full-Bleed-Bildkarten (~16–20 Referenzpixel Radius), dunkler Gradientenfuß für weiße, abgestufte Overlayschrift; echte Poster/Backdrops, kein eingebautes Serienmaterial.
- Fokussierter Hero: schmaler Lavendelring (Richtwert `#B18AFF`) plus zurückhaltendes Leuchten **ohne** Layoutverschiebung oder erhebliche Skalierung.
- Sans-Serif aus Android-Systemschriften, keine neue Font-Bibliothek. Dezente Bereichsüberschriften; Counts aus **gefilterten wirklichen Daten**, nicht aus der Zahl montierter Karten.
- `Neu` und Countdown nur bei belegter Datenlage. Unbekannte Metadaten werden weggelassen. Keine Mockup-Bilder/-Titel als programmatische Fallbacks.

## Sidebar / Navigation

Sichtbare Reihenfolge: `Übersicht` → `Nuvio TV` → `SmartTube` → `Einstellungen` → `VPN`; nur **installierte/tatsächlich verfügbare** Ziele fokussierbar. `VPN` muss die vorhandene integrierte Mullvad-Activity öffnen, kein Fake-Intent. `Alle Apps` und weitere Apps werden über einen nachweislich D-Pad-zugänglichen Mechanismus erreicht, **keine sechste feste Position im Referenzbild erzwingen**. Option für M05: Long-OK auf Übersicht → Alle Apps, wenn konfliktfrei.

D-Pad rechts/links pro Zeile, oben/unten mit horizontal sinnvoller Zuordnung; links am Zeilenbeginn zur Sidebar, rechts der Sidebar zum Inhalt. Fokus und Scroll bleiben bei Refresh und Rückkehr erhalten. Home/Back, System-/Launcher-Einstellungen, HDMI-Quellen, Wallpaper, Setup, Direct Boot, Launcher-Rolle, Long-OK und Mullvad müssen nach Migration weiterhin erreichbar sein (siehe `INVENTORY.md`). Uhr, Wetter, aktuelle Topbar und sekundäre Aktionen: in Settings/Overlay bzw. explizit dokumentierte Alternative, nicht unbemerkt entfernen.

## Inhalt / Aktionen

| Reihenfolge | Kategorie | Exakte Nuvio-Channel-ID | Darstellung | Verhalten |
| --- | --- | --- | --- | --- |
| 1 | Weiterschauen | `<laufzeitpaket>:in-progress` | 1 Hero + 2 Neben-Karten, S/E, verlässlicher Progress, Restzeit nur bei realer Dauer | Continue oder geprüfter Details-Fallback |
| 2 | Neue Folgen verfügbar | `<laufzeitpaket>:available` | 3 breite Kacheln, reale Staffeln/Episoden | Playback/Details je Original-Intent |
| 3 | Demnächst | `<laufzeitpaket>:upcoming` | 4×2 Minis, Datum und nur belegter Countdown | **Immer Details** |

Leere Kategorien erhalten zurückhaltenden Empty-State, keine Nachfüllung mit fremden Inhalten. Der klassische Nuvio-Kanal `CONTINUE` bleibt in Nuvio unverändert; in Plume nicht stillschweigend auf die drei Kategorien aufteilen. **SmartTube nur App-Shortcut** in der Sidebar.

### Konflikt in der Originalbildvorlage (explizit aufgelöst)

Das Originalmockup zeigt in den Medienreihen **SmartTube-Kacheln**, obwohl der normative schriftliche Nutzervertrag ausdrücklich **nur Nuvio-Medien** und SmartTube **nur als Sidebar-Verknüpfung** verlangt. **D-004:** Die **Geometrie und Hierarchie** des Bilds bleiben verbindlich; SmartTube-Beispielkarten werden niemals als echte Daten in die drei Nuvio-Reihen übernommen. Wenn Nuvio weniger Inhalte liefert, Slots leer lassen statt mit SmartTube zu füllen. Keine Ersetzung durch hardcodierte Serienbilder.

## Abnahmegrenzen

M00 implementiert **kein UI**. Der spätere Referenzvergleich braucht echte 16:9 Screenshots vom Emulator/Shield, Fokuszustände und dokumentierte sichtbare Abweichungen. Kein `pixel-perfect`, keine Hardwarebestätigung ohne echte Prüfung. Das byteidentische `REFERENCE.png` ist inzwischen unter dem kanonischen Pfad im Nachtragsbranch enthalten; vor `passed` sind noch die **echten** PR-/CI-Ergebnisse und das Abschlussreview nachzutragen. Das ist kein visueller Implementierungstest.
