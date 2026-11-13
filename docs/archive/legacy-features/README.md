# Archiv früherer Plume-Feature-Meilensteine

> **Historische Dokumentation – keine aktuelle UI-Spezifikation.**  
> Archiviert am 2026-11-13 zur Vorbereitung des geplanten, mockupgetreuen V-Design-Redesigns.

Diese Dokumente halten frühere Implementierungsentscheidungen, Tests und offene Hardware-Prüfungen fest.
Sie wurden aus dem aktiven `docs/`-Root verschoben, **nicht** gelöscht oder als abgeschlossen nachgetestet.
Die Originalinhalte bleiben unverändert; Pfadangaben in alten Texten beziehen sich teilweise auf den früheren Ablageort bzw. damalige Commits.

| Feature-Ordner | Historische Dokumentation |
| --- | --- |
| [overview-ui/](overview-ui/) | M00 Ausgangsbaseline der Classic-Home, M04 Overview-UX-Abnahme, M11 Overview-Release-Abnahme |
| [android-tv-feed/](android-tv-feed/) | M06 TV-Provider-Feed und Shield-Tests |
| [app-pages/](app-pages/) | M07 App-Vorschauseiten und D-Pad-Abnahme |
| [nuvio-tv-channels/](nuvio-tv-channels/) | M08 frühere Nuvio-Next-Up-Kanalintegration, vor der neuen Vier-Kanal-Architektur |
| [smarttube-youtube/](smarttube-youtube/) | M09 SmartTube-Preview-/YouTube-RSS-Integration |
| [spotify-podcasts/](spotify-podcasts/) | M10 Spotify-Podcast-Machbarkeitsprüfung |

## Abgrenzung

- **Archiviert wird die bisherige Feature- und UI-Dokumentation, nicht ihre produktive Implementierung.** Existierende Features und Einstellungen werden mit dieser reinen Dokumentationsänderung nicht entfernt.
- Die historischen Anleitungen, die den alten Classic-/Overview-Layoutwechsel verlangen, sind **keine Vorgabe** für das geplante V-Design. Bis dessen Umsetzung kann der derzeitige Launcher allerdings noch das bisherige UI zeigen.
- `docs/mullvad-integration/` bleibt aktiv: VPN-Architektur, Release-/Testmatrix und Status gelten weiterhin unabhängig vom UI-Redesign. Ebenso bleiben allgemeine Projektunterlagen und aktuell referenzierte Wallpaper-/Webseiten-Assets bestehen.
- Ein neuer V-Design-Meilenstein soll seine **eigene** aktuelle Spezifikation, Fortschritts- und Testdateien in einem getrennten Verzeichnis verwalten. Es darf nicht behauptet werden, dass das neue Design durch diese Archivierung bereits implementiert sei.

## Umzugstabelle

- `docs/home-overview-baseline.md` → [`docs/archive/legacy-features/overview-ui/home-overview-baseline.md`](overview-ui/home-overview-baseline.md)
- `docs/m04-shield-ux-acceptance.md` → [`docs/archive/legacy-features/overview-ui/m04-shield-ux-acceptance.md`](overview-ui/m04-shield-ux-acceptance.md)
- `docs/m11-release-acceptance.md` → [`docs/archive/legacy-features/overview-ui/m11-release-acceptance.md`](overview-ui/m11-release-acceptance.md)
- `docs/m06-shield-tv-feed-acceptance.md` → [`docs/archive/legacy-features/android-tv-feed/m06-shield-tv-feed-acceptance.md`](android-tv-feed/m06-shield-tv-feed-acceptance.md)
- `docs/m07-shield-app-pages-acceptance.md` → [`docs/archive/legacy-features/app-pages/m07-shield-app-pages-acceptance.md`](app-pages/m07-shield-app-pages-acceptance.md)
- `docs/m08-nuvio-tv-acceptance.md` → [`docs/archive/legacy-features/nuvio-tv-channels/m08-nuvio-tv-acceptance.md`](nuvio-tv-channels/m08-nuvio-tv-acceptance.md)
- `docs/smarttube-m09.md` → [`docs/archive/legacy-features/smarttube-youtube/smarttube-m09.md`](smarttube-youtube/smarttube-m09.md)
- `docs/spotify-m10-feasibility.md` → [`docs/archive/legacy-features/spotify-podcasts/spotify-m10-feasibility.md`](spotify-podcasts/spotify-m10-feasibility.md)

Bei Verweisen aus anderen Dateien bitte den neuen Pfad verwenden. Die acht historischen Dokumentdateien wurden als unveränderte Git-Blobs verschoben.
