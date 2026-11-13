# M04 → M05: operativer Handoff

**Kanonischer Status:** [STATE.md](STATE.md). **Feature-Branch:** `feature/vdesign-m04-card-styling`, [Draft #37](https://github.com/Justify87/Plume_Launcher/pull/37). **Baseline:** Plume `main@763869b8bf672f5955b1d0682a643f0565bca5fb` (M03 #36 merged); Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a` nur lesen.

## M04-Änderungen / konkrete Einstiegspunkte

1. `VDesignHomeView.kt` erstellt exakt 14 echte `VDesignCardView`-Slots an den M03-Referenzankern. `bindMedia` verbindet Artwork, Quellensymbol, Klicks; `bindFeed` übernimmt echte IN_PROGRESS/AVAILABLE/UPCOMING-Listen und vollständige Counts. Acht UPCOMING-Zellen zeigen jeweils einen Viererschritt-Ausschnitt; up/down kann weiterblättern. Keine synthetischen Titel.
2. `VDesignCardView.kt` rendert Varianten HERO, STANDARD, AVAILABLE und COMPACT: echter visueller Fokus-Rand ohne Transform, statischer Gradient, Bild, Quelle, Titel/Staffel/Folge, Fortschritt/Restzeit bei echten Werten, ISO-LocalDate als lokaler Text/Countdown. `VDesignCardText.kt` ist reine JVM-Formatlogik, `VDesignCardTextTest.kt` ihr Regressionstest.
3. `MainActivity.setupVDesign()` verdrahtet den bestehenden `loadPoster` und `sourceIcon`. `loadPoster` verteilt das Ergebnis auch an die anderen zeitgleich wartenden Karten mit identischer URL. `openVDesignMedia` lässt nur explizit an die echte Nuvio-MainActivity adressierte Intents durch und erzwingt Upcoming-Details statt Playback.
4. Alte Android-Views bleiben bis M06 nur unsichtbar als Migrationsbasis; `GlassLayer`/Wallpaper/Video, Mullvad-Rust/JNI/VpnService/gitlink wurden nicht umgebaut. Bestehende Nuvio-App-Channel-Quelle und klassischer Mischkanal unangetastet.
5. Original `REFERENCE.png`: 1536×864 JPEG/JFIF unter PNG-Suffix, **SHA-256 geprüft** `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`. Bild real angesehen, **kein realer Launcher-Screenshot** gemessen.

## M05 Pflicht / offene Risiken

- D-Pad: eindeutige Navigation zwischen Sidebar, Hero, Standard, verfügbaren und 4×2 Upcoming-Karten; bei >8 Einträgen Pages, diagonale Geometrie, Fokusrestauration bei Bind/Resume, Intent-Return/Back/HOME. M04 besitzt nur die schlanke vertikale UPCOMING-Paginierung.
- System-Pfade: Long-OK Übersicht=Alle Apps, Settings/Sources/HDMI, VPN, Direct Boot, Overlay-Fokus, Wallpaper und app-row Legacy-Migration. Keine feste sechste Sidebar-Aktion.
- Laufzeit: reale Nuvio-Categorized-Provider-Opt-in, mehrere Episoden, opt-out/Permission-denied, Bilder/Cache, Offline, langsame Downloads, fehlende Logos und Metadaten, korrektes Details-Opening für UPCOMING, ausbleibende Serien/Folgen-IDs. Verhindern, dass D-Pad oder Overlay hinter die Settings rutscht.
- Hardware/Pixel: mit Android-TV-Emulator und NVIDIA Shield Pro 2019 16:9 Screenshots inklusive Focus; Referenz-Overlay und GPU/Bitmap-Profilergebnisse wirklich messen oder `not_run` lassen.
- CI: PR [#37](https://github.com/Justify87/Plume_Launcher/pull/37), ersten Run [43523928137](https://github.com/Justify87/Plume_Launcher/actions/runs/43523928137) zum Beginn der Dokumentation `in_progress`. **Nach finalem Docs-Commit auf finalem SHA neu prüfen**, Ergebnisse nur auf zugehörigem SHA dokumentieren. Native Mullvad-APK-Job `skipped`; keine Runtime-Aussage.

**Kein Merge ohne Nutzerfreigabe. Nächster Meilenstein ausschließlich M05.**

## Historischer M03 → M04 Handoff (nur als Rückblick)

# M03 → M04: operativer V-Design-Handoff

**Stand:** 2026-11-13 · **Quelle des aktuellen Zustands:** [STATE.md](STATE.md). Frühere M02-Handoff-Absätze sind historisch und werden nicht als aktuelle Abhängigkeit verwendet.

## Verifizierte Baseline und echte Arbeit

- Plume `main@3f77ba552b47152349d28aae15b02a865b367649` enthält M02 über [Merge #35](https://github.com/Justify87/Plume_Launcher/pull/35). Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a` ist read-only.
- M03 Branch `feature/vdesign-m03-layout-sidebar`, [Draft-PR #36](https://github.com/Justify87/Plume_Launcher/pull/36), Quellcode-/View-Review HEAD **vor Docs-Commit** `71f5e80c09d004185cb09f7226d9ac66e66eeae1`. Nachfolgearbeiten auf tatsächlichem finalem PR-HEAD stapeln, nicht auf veraltetem `main`. Kein Merge.
- Originalbild `REFERENCE.png` 1536×864, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25` wirklich aus GitHub-Bytes geprüft und visuell angesehen; JPEG/JFIF trotz `.png`.
- Native Mullvad-Quellen und Submodul unverändert, historische Infos: `docs/archive/legacy-features/mullvad-vpn/`.

## Die tatsächlich verfügbare M04-Eingangsschnittstelle

- `MainActivity.nuvioDashboardFeed` enthält atomar den M02-Projektionsstand der drei bestätigten Nuvio-Channels; synchronisierte UI-Bindung läuft nach Query-Post und bei `onResume`. `VDesignHomeView.bindFeed(feed)` setzt **echte vollständige** Counts für Available und Upcoming, nicht montierte Slots.
- `VDesignHomeView` wird über `setupVDesign()` als sichtbarer Host in `root` **hinter `allApps` und `settingsScrim`**, aber **vor `content` und dem bereits vorhandenen `GlassLayer`** eingefügt. In Direct Boot wird `setupVDesign` wegen frühem Guard nicht aufgerufen. Alte `classicHome`, `overviewHome` und `HomeSidebarView` bleiben im XML nur als temporäre Migrationsbasis bis M06 erhalten; keine zweite sichtbare Home-Seite.
- `VDesignCanvas.place()` transformiert feste Referenzanker proportional; Renderer darf auf kleineren Viewports/bei realen Insets nicht still dp-gleich annehmen. 14 neutrale nicht anklickbare Slots: 1 Hero + 2 daneben, 3 breite, 8 kompakte. Header jeweils subtil ohne globale Titelleiste.
- Die Sidebar hat konkrete Schlüssel `OVERVIEW/NUVIO/SMARTTUBE/SETTINGS/VPN`, verwendet Original-App-Icons, startet nur tatsächlich vorhandene Apps und nur tatsächlich auflösbare interne Mullvad-Activity; Übersicht langes OK öffnet `openAllApps()`, Settings langes OK öffnet `showSources()`. Keine sechste sichtbare feste Option.
- Ein DST-sicherer lokaler Tageswechseltermin läuft nur während `resumed`, bei `onPause` gecancelt. M04 muss die realen UPCOMING-Texte beim tatsächlichen Bind neu aus `daysUntilRelease(now, zone)` berechnen; bei fehlendem Datum keinen Countdown anzeigen. Kein periodisches Background-Polling.

## M04 konkrete Arbeit / Prüffragen

1. Neutrale Slots in echte Kartenansichten umwandeln: tatsächlich verfügbare `NuvioMediaItem`-Einträge, Hero-Gewichtung und Artwork aus `artworkUri/posterUri/logoUri` via vorhandenem Poster-/Cache-Loader. Keine Hardcodes, keine beispielhaften Serien; bei weniger Datensätzen Restplatz zurückhaltend neutral/leer lassen.
2. Progressive Metadaten-Overlays nur aus belegbaren Werten: S/E, `progressPercent`, Restzeit ausschließlich aus `remainingMillis`, Upcoming-ISO-LocalDate, ehrliche volle Counts, **nie** `Neu` ohne strukturiertes Flag; 100000ms dürfen keine Minuten anzeigen.
3. M03-Platzhalter sind absichtlich nicht fokussierbar; M04 ergänzt echte D-Pad-Fokusziele, Lavendel-Fokusring ohne Geometriesprung, Fokusrestauration nach Feed-Refresh, korrekte Scrollregeln sowie sichere TV-Intent-Prüfung. **UPCOMING öffnet Details, nie ungeprüft Playback**.
4. Gezielte Regression: Missing-Permission, null/empty Provider, mehrere Serien mit gleichnamigen Episoden, keine SmartTube-Kacheln, falsch deklarierte Paket-/Intent-IDs, zurückkehrender Fokus nach Player/VPN/All-Apps/Settings. M05 übernimmt den vollständigen systematischen D-Pad-/HOME-/Back-/HDMI-Abnahmeteil.
5. `GlassLayer`/Wallpaper-Root-Reihenfolge nach Artworks nochmals reviewen. View-Inflation via echtem Android-Build/Emulator-Screenshot und Shield Pro 2019 tatsächlich testen oder offen als `not_run`/`hardware_pending` eintragen. CI auf dem **letzten** PR-SHA verifizieren.

## CI- und Merge-Grenze

M03-Code-Run [43520674555](https://github.com/Justify87/Plume_Launcher/actions/runs/43520674555) wurde beim Handoff **in_progress**, nicht successful beobachtet (Check-Job [132213947427](https://github.com/Justify87/Plume_Launcher/actions/runs/43520674555/job/132213947427)). Vorgänger-Runs [43520147382](https://github.com/Justify87/Plume_Launcher/actions/runs/43520147382) und [43520288284](https://github.com/Justify87/Plume_Launcher/actions/runs/43520288284) durch Folgecommits **cancelled**; Mullvad-APK-Job skipped. Dieses Dokument erzeugt einen neuen SHA: finalen Code-und-Dokumentations-Run gesondert beobachten. Lokale SDK-, Emulator- und Shield-Tests nicht durchgeführt. **M04 ist exakt der nächste Meilenstein. Kein Merge ohne explizite Freigabe.**

## M03 CI-Nachtrag – bestätigter Quellcode-/Dokumentationscheck

[Run 43521292243](https://github.com/Justify87/Plume_Launcher/actions/runs/43521292243) auf `8160f2bf05df335b52c9735c675b12a8dbda57b0` **completed/success**; [check-Job 132215945300](https://github.com/Justify87/Plume_Launcher/actions/runs/43521292243/job/132215945300) **success**. Echte Gradle-Unit-/Lint-Checks, Debug-/Release-APK, Größenvergleich: Release `535462→540870` (+5408 Bytes); 44 Installer-Checks, 10 Spotify-, 12 Mullvad-Staging-, 6 Mullvad-APK-Validator-Offline-Tests erfolgreich. Native Mullvad-APK `skipped`; lokale SDK-/Emulator-/Shield-Tests `not_run`. Ein neuer Dokumentations-Evidence-Commit erzeugt einen anderen PR-HEAD, dessen CI wiederum nachzuweisen ist; dieser Erfolg wird **nur** dem konkreten SHA zugeordnet. M04 bleibt exakt nächster Schritt, kein Merge erteilt.
