# V-Design — aktuelle Übergabe nach M01 (2026-11-13)

## Verifizierter Einstieg

- Plume `main@59c7988d4b019c12647550095e24f8cf62aece33`; M00 [PR #32](https://github.com/Justify87/Plume_Launcher/pull/32) und [#33](https://github.com/Justify87/Plume_Launcher/pull/33) tatsächlich gemergt. Letzte M00-[CI 43509990732](https://github.com/Justify87/Plume_Launcher/actions/runs/43509990732): `completed/success`, `check=success`, `mullvad-apk=skipped`.
- Aktiver **M01** [Draft-PR #34](https://github.com/Justify87/Plume_Launcher/pull/34), Branch `feature/vdesign-m01-nuvio-provider`, dokumentierter Code-HEAD `30084ee9fd630c805e799e4c2bec20416310bb05`; nach diesem Dokumentationscommit muss die tatsächliche Branch-HEAD-SHA erneut ermittelt werden. Nicht gemergt ohne neue Nutzerfreigabe.
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

## Nachgetragene CI-Abnahme des M01-Code-HEAD

- [Erster PR-Run 43512433898](https://github.com/Justify87/Plume_Launcher/actions/runs/43512433898) **failure**, 2/213 JVM-Tests scheiterten (Serienidentität und Legacy-Fallback-Duplikat). Ursachen in [Code-Commit 30084ee9](https://github.com/Justify87/Plume_Launcher/commit/30084ee9fd630c805e799e4c2bec20416310bb05) behoben.
- [Korrigierter Run 43513105647](https://github.com/Justify87/Plume_Launcher/actions/runs/43513105647) **completed/success**; [check-Job 132188191911](https://github.com/Justify87/Plume_Launcher/actions/runs/43513105647/job/132188191911) Unit-Tests+Lint, Debug+Release-APK und Größenvergleich erfolgreich. APK **529422 → 532270 Bytes (+2848)**. Mullvad-APK-Job absichtlich `skipped`; Shield `hardware_pending`.
- Für nachfolgende M02-Arbeit **den HEAD des Draft-PR #34 nach diesem Dokumentationscommit erneut abrufen** und aktuelle finale CI beobachten. Kein M01-Merge ohne explizite Freigabe, keine Verwechslung mit tatsächlicher Shield-Abnahme.
