# V-Design — aktuelle Übergabe nach M01 (2026-11-13)

## Verifizierter Einstieg

- Plume `main@59c7988d4b019c12647550095e24f8cf62aece33`; M00 [PR #32](https://github.com/Justify87/Plume_Launcher/pull/32) und [#33](https://github.com/Justify87/Plume_Launcher/pull/33) tatsächlich gemergt. Letzte M00-[CI 43509990732](https://github.com/Justify87/Plume_Launcher/actions/runs/43509990732): `completed/success`, `check=success`, `mullvad-apk=skipped`.
- Aktiver **M01** [Draft-PR #34](https://github.com/Justify87/Plume_Launcher/pull/34), Branch `feature/vdesign-m01-nuvio-provider`, dokumentierter Code-HEAD `9480a9417ec62e95909547372510151d8f3c29dc`; nach diesem Dokumentationscommit muss die tatsächliche Branch-HEAD-SHA erneut ermittelt werden. Nicht gemergt ohne neue Nutzerfreigabe.
- Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a` nur gelesen, Originalkanal nicht verändert. Native Mullvad-Dokumente liegen unter `docs/archive/legacy-features/mullvad-vpn/`, originales Submodul unangetastet.
- Normative Datei `docs/v-design/REFERENCE.png`: 249663 Bytes, 1536×864, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`; historisch JPEG-Bytes bei PNG-Endung. M01 erneut aus Original-Blob geprüft.

## Tatsächliche technische Übergabe

`TvPreviewChannel` erhält ID, Package, Display-Label, `channelProviderId` und `browsable` (tri-state). `AndroidTvMediaSource.nuvioKind` prüft exakt Paket + Provider-Suffix statt lokalisiertem Namen. In `MainActivity.queryWatchNext()` werden optional genehmigte Nuvio-Kanäle `:in-progress`, `:available`, `:upcoming` in derselben IO-Abfrage gelesen und als `nuvioProviderFeeds` gespeichert — noch **nicht** in alte Overview-/Classic-Tiles gerendert. Program-Provider-ID und Channel-Provider-ID sind getrennt. Release-Date wird unverändert mitgeführt; M02 übernimmt Datums-/Zeitinterpretation.

Classic-Auswahl und Watch Next behalten ihre Vorfilter und vorhandene SmartTube-Logik; Nuvio-kategorisierte Preview-Kanäle gelangen nicht in die klassische Mischzeile. Wenn ein expliziter Nuvio-In-Progress-Kanal fehlt, können echte gestartete Legacy-/Watch-Next-Einträge ihn ersetzen, niemals mit falschen Available-/Upcoming-Episoden. Fehlt `browsable`, werden Kategorien nicht ungeprüft angezeigt; für echtes Android-TV-11-Opt-in besteht eine Hardwareprüfung.

## Exakt nächster Schritt M02 — Datenqualität, Sortierung, Verknüpfung

1. Erst den **aktuellen** Branch/PR-HEAD und das finale PR-CI kontrollieren. M01 nicht als bestanden ohne CI/Review; Shield `hardware_pending`.
2. Aus `NuvioProviderFeeds` echte V-Design-Medienmodelle ableiten: In-Progress-Hero-Validierung, Available-Releasebeleg, Upcoming-Datum und lokale Zeitzonengrenzen; Episode-ID/Staffel/Folge deduplizieren ohne `_id`-Kollaps.
3. Nuvios synthetische 100s-Progress-Dauer niemals als echte Restzeit ausgeben. Kein SmartTube im Drei-Feed-Modell. Provider-Empty-States und Error-/Permission-Fälle explizit darstellen.
4. M03 übernimmt erst anschließend die visuelle V-Design-Umstellung; keine hartcodierten Demodaten. VPN-/D-Pad-/HDMI-/Settings-Funktionen behalten.

Alle tatsächlichen Fakten in `STATE.md`, `ROADMAP.md`, `REVIEWS.md`, `TEST_MATRIX.md` dokumentieren. **Kein Merge ohne explizite Freigabe.**
