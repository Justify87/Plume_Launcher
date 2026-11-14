# M06 → M07: operativer Handoff

**Ergebnis:** Quellcode-Migration auf einzelnem V-Home; Code-CI auf `3e8a135` [completed/success](https://github.com/Justify87/Plume_Launcher/actions/runs/43583732764); Draft-PR [#39](https://github.com/Justify87/Plume_Launcher/pull/39) **offen, kein Merge**. Grundlage tatsächlich gemergter M05 PR #38 (`main@de9bb87eef0ee13cbdcc9a884363839b17c2e602`).

- M06 Branch `feature/vdesign-m06-remove-legacy-home`, Code-HEAD vor Docs `3e8a1351285a72c99fa4f2c6526873845693eadc`. Bei neuem HEAD nicht alte CI-Status übertragen.
- `MainActivity.setupVDesign` und `VDesignHomeView` sind einzige Home-Renderer; alte XML `classicHome`/`overviewHome` und Home-Mode-Klassen gelöscht. Long-OK `Übersicht` → Alle Apps; `Einstellungen` → System/HDMI/Power. All Apps Long-OK zeigt App-Aktionen statt unsichtbarem Home-Row-Pinning.
- `MainActivity.refreshWatchNext` bleibt für echte Nuvio-TV-Provider-Snapshots. V-Design bindet `NuvioDashboardFeed` für `:in-progress`, `:available`, `:upcoming` und verwendet `VDesignLaunchPolicy`. Nuvio-`CONTINUE` ist weiterhin **ein eigenständiger Publisher-Mischkanal**.
- Ältere SettingsTransfer-Version-1-Dateien mit `home_layout_mode` / `row_size` / `row_align` / `row_show` / `row_drop` sind zulässig; diese abgeschalteten Keys werden beim Import ignoriert. Keine komplette Settings-Löschung. `row_hidden` und `order` bleiben für Bestandsdaten portabel.
- Direkter Boot vor Unlock rendert die **V-Shell mit Empty State**, ohne Credential-Preferences/TV-Provider. Physische Shield-Abnahme dieses Pfads steht aus. Versteckte Clock-/Topbar-Hilfsviews im `content`-Host sind kein Home-Layout und wurden für Wallpaper-/Systemaktionen-Kompatibilität nicht blind gelöscht.
- **Tests:** erster CI-Lauf [43533466727](https://github.com/Justify87/Plume_Launcher/actions/runs/43533466727) fehlgeschlagen wegen `rowNames`/`cwColour`. Folgekorrigierter Code [e48abd7](https://github.com/Justify87/Plume_Launcher/commit/e48abd7c7d5554a5b5e2482df173ba38ebe76d0a), [Folge-CI 43533837289](https://github.com/Justify87/Plume_Launcher/actions/runs/43533837289) **failed** an altem YouTube-Overview-Unit-Test. Mit [d5d88e5](https://github.com/Justify87/Plume_Launcher/commit/d5d88e502ea960419756625523553d596f29de2d) auf `YouTubeSubscriptions.dedupe` migriert; [dritter Run 43534292196](https://github.com/Justify87/Plume_Launcher/actions/runs/43534292196) **completed/failure** wegen eines Android-JSON-JVM-Stubs im neuen SettingsTransfer-Importtest. Auf reine `validate`-Migration korrigiert in [cabf0a7](https://github.com/Justify87/Plume_Launcher/commit/cabf0a76705759254f69f8c8754591fbbfff3c3f); [vierter Run 43534832233](https://github.com/Justify87/Plume_Launcher/actions/runs/43534832233) `in_progress` zum Schreibzeitpunkt. Echter JSON-Dateiimport bleibt **Android-Device-Pflicht**. Kein lokales Gradle, kein Android SDK, kein Emulator, keine Shield-Testbehauptung.

## Exakt nächster Meilenstein — M07

Auf dem **wirklich geprüften M06 PR-HEAD** bzw. dessen freigegebenen Merge-Stamm arbeiten. Zuerst CI, dann Shield Pro 2019: HOME-Rolle, Device Protected Direct Boot, D-Pad/Back/Focus Return, All Apps install/deinstall, HDMI/CEC, Wallpaper/Theme/Settings, Mullvad-VPN-Start/JNI und echte Nuvio-Preview-Kanäle. Mit echten Screenshots gegen Originalbild prüfen; offene visuelle Änderungen nicht still in M06 reintroduzieren. Keine alten Home-Modi wiederherstellen. Alte Mockup-Beispielmedien sind keine gültige Datenquelle.

---

# M05 → M06: operativer Handoff

**CI-Nachtrag (nach Code+Docs):** [Run 43528616572](https://github.com/Justify87/Plume_Launcher/actions/runs/43528616572), [Check 132240568044](https://github.com/Justify87/Plume_Launcher/actions/runs/43528616572/job/132240568044) für `1e8a7cb5e7d2c823a32e4069cfa96e1a71a11b0e`: **completed/success**, `testDebugUnitTest lintDebug assembleDebug assembleRelease` erfolgreich; Release `545714 → 550334` B (**+4620**). Mullvad-native skipped, Hardware pending. Dieser Evidence-Docs-Commit ist ein neuer SHA; sein Run bleibt separat zu prüfen.

**Kanonischer State:** [STATE.md](STATE.md), **Branch:** `feature/vdesign-m05-navigation-interaction`, **Draft-PR:** [#38](https://github.com/Justify87/Plume_Launcher/pull/38), **Basis:** der echte M04-Merge `main@fdd10f3394463bd5d4142c9978834d78464905cc` ([#37](https://github.com/Justify87/Plume_Launcher/pull/37) merged). Vor M06-Start den **realen HEAD/PR-Merge und letzten CI-Lauf** erneut prüfen. Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a` read-only.

## M05-Quellen und Invarianten für M06

1. `VDesignFocusNavigator.kt` ist die reine Fokusmatrix: 1 Hero + 2 Standard / 3 Available / 4×2 Upcoming, räumlich nach x-Überlappung, keine Wrap-Sprünge. `VDesignHomeView.handleDpad()` verwendet sie in `MainActivity.dispatchKeyEvent()` **vor** alter `homeNav`; bei leerem Feed bleibt Sidebar fokussierbar. `VDesignCardView.onFocused` erfasst die tatsächliche Medien-ID, nicht die Karten-View als langfristigen Schlüssel.
2. `VDesignHomeView.restoreFocus()` erinnert Sidebar-Key, Section und Medien-`stableId` auch über Provider-Rebind und Upcoming-Paging; nur sichtbare, attached und focusable Views dürfen gewählt werden. `MainActivity.onResume/onNewIntent/onBackPressed` sowie All-Apps-/Settings-Schließpfade sind verbunden. Nach echten App-Rückkehr-/HOME-/Overlay-/Deinstallationsfällen auf Shield prüfen; nicht aus Source-Review als erledigt behaupten.
3. `VDesignLaunchPolicy.kt` trennt STREAM/DETAIL/UNAVAILABLE; `openVDesignMedia()` startet nur neu konstruierten, eingeschränkten Nuvio-MainActivity-Intent. InProgress/Available dürfen ausschließlich mit gültigem Publisher-Video-ID-/Mode streamen; UPCOMING nie. Nuvio `CONTINUE` bleibt im Publisher unverändert, SmartTube nur Sidebar-App.
4. `Übersicht` langes OK → `openAllApps`; `Einstellungen` OK → `openLauncherSettings`; langes OK → `showVDesignSystemActions` mit erreichbarem HDMI/Quellen, OS-/WLAN-Settings, Power, Home-Einrichtung. VPN öffnet `net.mullvad.mullvadvpn.app.VpnActivity` aus dem **integrierten** APK, falls real installiert. Keine sechste Standard-Sidebar-Kachel.
5. `MainActivity` und `activity_main.xml` enthalten weiter unsichtbare Classic-/Overview-/Topbar-Hosts und `home_layout_mode`-Einstellungen. **Genau M06:** diese strukturiert entfernen, alle Settings-/App-/TV-Provider-/Wallpaper-/Power-/Setup-/Sources-Funktionen zuvor gegen ihre neuen Pfade inventarisieren, Pref-Migration behandeln und Regressionstests anpassen. Original `third_party/mullvadvpn-app` und generierte originale Mullvad-Rust/JNI/VpnService nicht verändern. `GlassLayer`-Root-Index 2 und Direct-Boot-Guard vor Preference-/Provider-Zugriff bewahren.
6. CI-Beleg bis Dokumentationscommit: [PR #38](https://github.com/Justify87/Plume_Launcher/pull/38), [Run 43528175980](https://github.com/Justify87/Plume_Launcher/actions/runs/43528175980) zunächst `in_progress`. Finalen Docs-HEAD/Run nachlesen und bei Fehlern real korrigieren; nativer Mullvad-Build im V-Design-PR branchbedingt `skipped`. Kein Android-SDK-/Emulator-/Shield-Lauf lokal vorhanden: `not_run/hardware_pending`; kein Referenz-Pixel-Claim.

**Normreferenz:** Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18`, 249663 Bytes, JPEG/JFIF 1536×864 unter `REFERENCE.png`; unabhängig geprüfter SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`.

**Nächster Meilenstein ausschließlich M06. Kein Merge ohne explizite Freigabe.**

---

## Historischer M04 → M05 Handoff (nicht aktueller Stand)

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


### Korrektur nach Code-Review
[Commit 13988ae](https://github.com/Justify87/Plume_Launcher/commit/13988ae216feecd33c39f5e3b491294b9fff6c22) behandelt entferntes fokussiertes Medium mit Fallback auf die nächste echte Karte/Sidebar; validiert `java.net.URI`-Authorities und lehnt URL-Userinfo ab. Zusätzliche JVM-Assertions committed. Finaler CI-Run für dokumentierten HEAD bleibt offen; kein Emulator-/Hardwarepass behauptet.


### M04 Binder-/Paging-Regression ergänzend
`VDesignCardSlots.media(feed, section, slot, offset)` und `nextUpcomingOffset(total,current,slot,direction)` sind die kleine reine Binding-Schnittstelle. [3516fb](https://github.com/Justify87/Plume_Launcher/commit/3516fb0463747a72b84604e3b6b9490b0ceb33f8) ersetzt die lokalen manuellen Indizes in `VDesignHomeView`, Unit-Tests für 3+3+8, echte Empty States und Scroll-Grenzen ergänzt. CI auf diesem Commit plus finalem Docs-HEAD separat auswerten. **Nächster Schritt M05.**


### M06 finale technische Übergabe

Geprüfter Code-HEAD: [`3e8a135`](https://github.com/Justify87/Plume_Launcher/commit/3e8a1351285a72c99fa4f2c6526873845693eadc). CI [Run 43583732764](https://github.com/Justify87/Plume_Launcher/actions/runs/43583732764) / [Check 132413775845](https://github.com/Justify87/Plume_Launcher/actions/runs/43583732764/job/132413775845) **success** (JVM, Lint, Debug/Release-APK, Repo-/Offline-Regressionen). Release APK `550334 → 509570 B (-40764 B)`. Native Mullvad-APK-Job `skipped`; **keine** Tunnel-/JNI-Laufzeitbestätigung und keine echte Shield-Bedienbestätigung.

Neu im M06-Code-Review: `onNewIntent` schließt erneut Einstellungen/All Apps/Topbar-Move auf HOME; `refreshVisibleApps` restauriert möglichst den fokussierten All-Apps-Paket-Key; langes OK im All-Apps-Grid respektiert die bestehende PIN-Sperre; bereits vorhandene eigene App-Icon-Dateien werden sowohl in Sidebar als auch All Apps gelesen. Nicht genutzte Bannerdateien bleiben gespeichert, der obsolete Bannereditor wird nicht mehr angeboten. Weiterhin nur ein V-Home-Renderer und kein Home-Mode-Schalter.

**M07-Startpunkt:** M06 [PR #39](https://github.com/Justify87/Plume_Launcher/pull/39), wirklich aktuellen HEAD und CI nach dem Docs-Commit prüfen. Vor einer Shield-Abnahme weder globale Preferences löschen noch die verbliebenen unsichtbaren Clock-/Topbar-Systemhelfer blind entfernen. HOME/Back/All Apps Long-OK/PIN/TV-Provider/Upgrade-Prefs/SettingsTransfer.decode/Direct Boot/Wallpaper/Mullvad echte Hardware-Pflicht. Kein Merge ohne ausdrückliche Nutzerfreigabe.

**Letzte verifizierte Code+Docs-Baseline:** [`ba52e586`](https://github.com/Justify87/Plume_Launcher/commit/ba52e586ea9929783d359b9bc29aab7016b09416), [CI-Run 43584273457](https://github.com/Justify87/Plume_Launcher/actions/runs/43584273457) **completed/success**. Keine automatische Merge-Freigabe. Ein neuer Doku-Evidenz-Commit ist separat zu prüfen; für M07 den wirklichen PR-HEAD als Basis verwenden.
