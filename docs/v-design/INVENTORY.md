# V-Design — Legacy-Funktionsinventar und Migrationspflicht

Quellenstand Plume `main@fbf2473711c50d03eb76afe8fd0a722af1faf136`: `MainActivity.kt` (6627 Zeilen), `activity_main.xml` (489 Zeilen), `HomeLayoutMode.kt`, `HomeSidebarView.kt`, `OverviewHomeView.kt`, `HomeFocusNavigator.kt`, `OverviewRowsNavigator.kt`, `AndroidTvMediaSource.kt`, `MediaItem.kt`, `MediaFeedRepository.kt`, Manifest, Gradle/Workflows. Dies ist eine **statische Inventur**, kein ausgeführter TV-Test.

| Bestehendes Feature / Beleg | Aktuelle Verankerung | V-Design-Pflicht / Verantwortlicher Schritt |
| --- | --- | --- |
| Classic und Overview Auswahl | `HomeLayoutMode.CLASSIC/OVERVIEW`, Pref `home_layout_mode`, `MainActivity.applyHomeLayoutMode()`, XML `classicHome/overviewHome` | ein Startbild M03; **beide alte Home-Modi samt Auswahl und Migration in M06 entfernen**, nicht früh blind |
| Sidebar/App-Seiten | `HomeSidebarView`, `HomePageState`, `OverviewHomeView` | fünf Referenzaktionen M03/M05, nur verfügbare App-Targets |
| Apps/Alle Apps | `appRow`, `allAppsButton`, `allAppsGrid`, `openAllApps()` | dauerhafter D-Pad-Zugang (z. B. Long-OK Übersicht) M03/M05, kein verstecktes Feature |
| System- & Launcher-Settings | `systemSettings`, `launcherSettings`, `settingsPanel`, `settingsScrim`, `openLauncherSettings()` | Sidebar „Einstellungen“ und Settings-Overlay erreichbar M03/M05 |
| Wallpaper/Video/Glass/Blur | `videoWallpaper`, `wallpaper`, `GlassLayer`, `loadWallpaper()` | Einstellmöglichkeiten + robustes Fallback erhalten, keine zweite Home-Theme-Vorschau M04/M06 |
| Uhr/Datum/Wetter/Featured | XML `clock/date/weather/featured`, `applyTopBar()` | künftig Settings/optionales Overlay oder expliziter Rückbau mit Entscheidung M06; keine Header im Normscreen |
| Netzwerk-/Topbar-Shortcuts | XML `wifi/sourcesButton/allAppsButton/systemSettings/launcherSettings`, Long-OK `fireHold()` | funktionale Ziele in Sidebar/Settings/Overlay oder dokumentierter TV Shortcut M05/M06 |
| HDMI/Sources | `sourcesButton`, `hdmiIcon()`, Sources-Listen | aus Einstellungen/Sidebar-Untermenü erreichbar, Interaktion mit HDMI prüfen M05/M06 |
| D-Pad-Reihen/Fokus/Scroll | `HomeFocusNavigator`, `OverviewRowsNavigator`, Fokus-Bookmarks in `OverviewHomeView` | stabile Quer-/Längsfokuswege, Refresh, Rückkehr, Ränder M05 |
| HOME/Back & Rückkehr | `onBackPressed()`, `onNewIntent()`, `HOME`-Kategorielogik | Rückkehr auf letzten sicheren Fokus statt Home-Falle; All-Apps/Settings Back M05/M06 |
| Launcher-Setup / Rolle / Home-Override | `ensureHomeService()`, `openHomeServiceSettings()`, `setupStep` | funktional erhalten, inkl. fehlender Android-TV-Settings M06–M08 |
| Direct Boot / Start | bestehendes Manifest + Setup / Gradle | keine neuen Berechtigungen, Start vor Unlock und nach Boot prüfen M06–M08 |
| Long-OK / kontextbezogene Aktionen | `fireHold()`, bestehende LongClicks | Einstellungen/Neuanordnung oder Ersatzverhalten erhalten, Konflikte mit All Apps prüfen M05/M06 |
| Bildcache/Poster-Refresh | `OverviewHomeView.retryArtwork()`, bestehender Artwork Loader | Cachegrenzen, Verlust eines Poster-URI, Fokus nicht resetten M04/M07 |
| Medien-Repository / TV-Permission | `AndroidTvMediaSource`, `MediaFeedRepository`, TV-Query in `MainActivity` | realer Provider und Fehlerfälle M01/M02/M07, kein zusätzlicher Poller |
| Mullvad VPN | `dev.plume.advanced`, integrierte VPN-Activity/Original-Gitlink, archiviertes `docs/archive/legacy-features/mullvad-vpn/STATE.md` | Sidebar VPN öffnet originale integrierte Oberfläche; Rust/JNI/VpnService/Gitlink unberührt M03/M05/M08 |

## Kritische Querverbindungen / Folgefehler

- Wechsel von `home_layout_mode` muss Pref-Legacy und vorheriges Fokusziel sicher migrieren; Einstellungen können weiterhin auf alte Layoutnamen verweisen. Der M06-Rückbau benötigt symbol-/usage-basiertes Aufräumen statt großem `MainActivity`-Rewrite.
- Der alte Topbar-Host ist im XML außerhalb von Classic/Overview; bloß die Layoutcontainer zu entfernen würde Sources, Systemsettings, All Apps, Uhr, Wetter, Home-Rückkehr und Hold-Aktionen gefährden.
- Externe App-Intents, deep links, Android TV Query Permissions und Mullvad-Entry müssen vor Launcher-Rollout auf realer Shield geprüft werden.
- Original-Mullvad-Dokumentation liegt seit der Archivierung nicht mehr in `docs/mullvad-integration`; **keine Wiederherstellung des alten Pfads**. Verwende den archivierten M12-Stand als historischen Verweis, nicht als Aussage zur aktuellen Hardwareabnahme.
