# M10 — Spotify-Podcasts: Machbarkeits-Gate und Shield-Testplan

**Dokumentationsprüfung: 11.11.2026. Ergebnis: Machbarkeit nur teilweise belegt, kein produktives Spotify-Feature aktiviert.**

Die Spotify-Web-API dokumentiert gespeicherte Podcasts, Episoden und ggf. deren resume_point. Ohne einen echten, autorisierten Development-Mode-Client und eine Prüfung auf der NVIDIA Shield TV Pro 2019 kann jedoch **weder der konkrete API-Zugang noch ein Episoden-/Positions-Deep-Link** nachgewiesen werden. Deshalb werden **keine** Spotify-Karten, kein vorgetäuschter Hörverlauf und kein ungetesteter OAuth-Flow in den Launcher integriert. Ein isolierter, nur lesender Prüfstand ist der Lieferumfang dieses Meilensteins. Die bisherige Startseite und alle bestehenden Einstellungen bleiben unverändert.

## Ausgangslage

- Prüf-Basis: main bei Beginn von M10 = **8873607c4e39ac63286906c228f080dd19b64fb0** (M09 bereits gemergt).
- Geprüft: MainActivity.kt, MediaItem.kt, MediaFeedRepository.kt, AndroidTvMediaSource.kt, YouTubeRssSource.kt, YouTubeSubscriptions.kt, MediaFeedRepositoryTest.kt, YouTubeSubscriptionsTest.kt, app/build.gradle.kts, CONTRIBUTING.md, tools/pr-rules.sh, PR- und Debug-APK-Workflows.
- Es gibt kein AGENTS.md im aktuellen Repository.
- Feed und App-Seiten verwenden bereits MediaItem und MediaFeedRepository. M09 demonstriert begrenzte, optionale RSS-Nutzung auf dem bestehenden IO-Executor. Keine weitere Launcher-Abhängigkeit, Permission, Compose-Komponente oder Hintergrundarbeit erforderlich.

## Offiziell dokumentierte API, Stand 11.11.2026

| Bedarf | Endpunkt / Scope | Belegbarkeit |
| --- | --- | --- |
| Gespeicherte Shows | GET /v1/me/shows, user-library-read | Dokumentiert; 200 mit eigenem Account **nicht geprüft** |
| Episoden einer Show | GET /v1/shows/{id}/episodes, user-read-playback-position für personenbezogenen Fortschritt | release_date und optional resume_point; **nicht** alle Episoden schon gehört |
| Gespeicherte Einzelfolgen | GET /v1/me/episodes, user-library-read und user-read-playback-position | Nur **gespeicherte**, nicht alle gehörten Folgen |
| Offene Folge | resume_point.fully_played = false UND resume_position_ms > 0 | Kann CONTINUE rechtfertigen, **wenn** aus echter Nutzerantwort vorhanden und Position nicht am Ende |
| Neue Folge | release_date mit echter Tagespräzision | Nur *neu veröffentlicht*, nicht „noch ungehört“; Monats-/Jahresangabe nicht ohne Weiteres als neu verwenden |
| Hörchronologie | GET /v1/me/player/recently-played | Laut Spotify **keine Podcast-Episoden**; kein Label „zuletzt gehört“ |
| Gerade abgespielte Folge | GET /v1/me/player/currently-playing?additional_types=episode | Nur aktueller Zustand, keine Historie; zusätzlicher Scope für dieses PoC **nicht** angefordert |
| Exakter TV-Deep-Link | spotify:episode:<ID> / https://open.spotify.com/episode/<ID> | Android Content Linking generell dokumentiert, **nicht** Ziel/Position der Spotify-TV-App auf Shield bestätigt |

**Wichtige Entwicklerbedingungen:** Spotify-Development-Mode verlangt ein Premium-Abo des **App-Inhabers**, höchstens **fünf** freigeschaltete Nutzer pro App und unterliegt Kontingenten. Die Februar-2026-Regeln gelten für neue Apps ab **11.02.2026**, für bestehende Development-Mode-Apps seit **09.03.2026**. Seit dem **Juli-2026-Update** dürfen Entwickler wieder bis zu **25 Client-IDs** anlegen; das API-Quota wird nun pro Entwicklerkonto statt pro Client gezählt. Die genannten Leseendpunkte stehen im Februar-2026-Changelog unter den verbliebenen Endpunkten, was noch **keine konkrete Freigabe** für unser Konto belegt. 401, 403 und 429 sind getrennt zu behandeln.

## Isolierter Prüfstand

Dateien: tools/spotify_podcast_probe.py und tools/test_spotify_podcast_probe.py. Nur Python-Standardbibliothek; **nicht** Teil der Android-APK, kein Login-Server, kein Token-Cache, keine Spotify-App-Daten, keine Medienwiedergabe.

Auf dem eigenen Rechner zunächst eine Spotify-Developer-App mit zulässigem OAuth-Flow (für die spätere Android-Integration **Authorization Code with PKCE**), den zwei minimalen Scopes **user-library-read** und **user-read-playback-position**, einem erlaubten Testnutzer und geeigneter Redirect-URI konfigurieren. Erst nach freiwilliger Zustimmung des Kontoinhabers ein **kurzlebiges User-Access-Token** lokal bereitstellen.

    SPOTIFY_ACCESS_TOKEN='<temporäres, privat gehaltenes Token>' python3 tools/spotify_podcast_probe.py
    python3 -m unittest discover -s tools -p 'test_spotify_podcast_probe.py' -v

**Token nie in GitHub, einen Chat, Screenshots oder öffentliche Logs kopieren.** Der Prüfer druckt das Token und rohe API-Antworten nicht. Er fragt auf fest allowlisteten HTTPS-Pfaden maximal drei gespeicherte Shows, zehn gespeicherte Folgen und je zehn Folgen der Shows ab; keine Redirects mit Bearer-Token, sieben Sekunden Timeout und 256 KiB Antwortlimit. Der Ausschnitt ist bewusst klein und **nicht** als komplette Bibliothek zu interpretieren.

Ergebnisformen je Anfrage: **ok** (200 plus items-Array), **blocked** (401/403/429 oder sonstiges HTTP-Problem), **offline** oder **unsupported**. Die Ausgabe trennt echte unfertige positive resume_points von bloß frisch veröffentlichten Episoden. Fehlen gespeicherte Shows, wird der Show-Episoden-Endpunkt **nicht** geprüft; fehlen angefangen Folgen, kann der konkrete Fortschritt **nicht** bestätigt werden. Auch nach ok ist die TV-Wiedergabe **nicht** validiert. Der Prüfer liefert bei passenden Datensätzen Kandidaten für den **manuellen** Deep-Link-Test.

### Manuelle Abnahme auf der NVIDIA Shield TV Pro 2019 — offen

1. Testkonto auf Spotify autorisieren, Premium-/Allowlist-/Scopes und Status 200 für alle drei read-only Endpunkte überprüfen. Für einen vollständigen Fortschrittstest eine Folge **wirklich** anspielen und pausieren sowie eine andere als **vollständig gehört** markieren; Unterschiede in resume_point und fully_played kontrollieren. Eine fehlende Eigenschaft niemals erfinden.
2. Auf der Shield die tatsächliche installierte Spotify-TV-App öffnen, Wiedergabeposition merken. Folgende Befehle mit einer **echten** 22-stelligen Episode-ID aus dem lokalen Probe-Output einzeln ausführen:

       adb shell am start -W -a android.intent.action.VIEW -d 'spotify:episode:<ECHTE_ID>'
       adb shell am start -W -a android.intent.action.VIEW -d 'https://open.spotify.com/episode/<ECHTE_ID>'

3. **Am Fernseher** getrennt protokollieren: (a) startet die Spotify-TV-App statt Browser/Startseite, (b) wird exakt die richtige Episode gezeigt, (c) wird auch die gespeicherte **Zeitposition** verwendet. Ein erfolgreiches Android-Intent-Resultat allein bestätigt (b) und (c) **nicht**. Spotify-URIs und HTTPS-Links nur aus validierten Episoden-IDs ableiten.
4. Bei künftiger Integration zusätzlich D-Pad/Back/Home, Login-Abbruch, Trennen, leere Bibliothek, 401/403/429, abgelaufene Tokens, Offline, fehlendes resume_point, vollständig gehörte Episoden und nicht installierte Spotify-App prüfen. Unverbundene Konten müssen unsichtbare/leere Spotify-Snapshots statt toter Karten haben.

## Entscheidung nach dem Gate

**Nur wenn die API-Zugriffe und die exakte Episoden-/Positionsnavigation auf der Shield nachgewiesen sind:** Eine kleine, standardmäßig deaktivierte SpotifyMediaSource an vorhandenen MediaFeedRepository/IO-Executor anschließen; kein Dienst, keine schweren SDKs. Ein OAuth-PKCE-Flow braucht klare Zustimmung, Abbrechen und Trennen, geschützte Token-Ablage (Android Keystore), lokale Token-/Cache-Löschung und legitime Spotify-Kontoseite zum Widerruf. Kein Client Secret in der APK; nicht auf die TV-App-Tokens zugreifen. Für CONTINUE nur nachweislich unfertige Fortschrittswerte; keine chronologische Hörfolge behaupten. Für NEW nur belegbare Veröffentlichungstermine, als **neu erschienen** kenntlich. Spotify-Cover unverändert, Markenhinweis/Quellenattribution berücksichtigen und Karte nur anbieten, wenn ein **auf Shield funktionierender** Ziel-Link existiert.

**Falls das Gate fehlschlägt:** RSS-Fallback als separater Umsetzungsmeilenstein für **öffentlich zugängliche Original-Podcast-Feeds des Herausgebers**, freiwillige HTTPS-URL-Eingabe, begrenzter sicherer Parser (Redirect-/Größen-/XXE-/Netzwerkschutz), Aktualisieren nur beim Öffnen/Refresh, echtes Veröffentlichungsdatum und „Neu erschienen“. **Kein** Spotify-Abspielstand über RSS, daher **kein** CONTINUE ohne eigenen kontrollierten Player und lokal erfasste Position. RSS-GUIDs sind **keine** Spotify-Episoden-IDs; keinen künstlichen Spotify-Deep-Link herstellen. Ein vom Herausgeber ausdrücklich publizierter Web-Link ist möglich.

## Kriterien und tatsächlicher Teststand

| Kriterium | Status |
| --- | --- |
| HEAD, Branch und relevante Quellen geprüft | erfüllt |
| API-Dokumentation, Zugangsbedingungen und Recently-Played-Grenze | erfüllt (Dokumentation) |
| Isolierter, read-only PoC und Offline-Unit-Tests | umgesetzt; **10** lokale PoC-Tests bestanden |
| Live-API mit eigener Entwickler-App und Nutzer-Token | **nicht ausgeführt** — keine autorisierte Zielumgebung |
| Fortschritt und gehörte/vollständige Folge im realen Spotify-Account | **offen** |
| Richtige Episode **und Position** per Shield-Deep-Link | **offen** — kein Gerätezugriff |
| Produktive Spotify-Karten, Login, Trennung, Refresh-Token | **absichtlich nicht implementiert** bis zur Freigabe durch das Gate |
| RSS-Alternative | Konzept dokumentiert, **nicht implementiert** |
| Regressionen an bestehender Android-Startseite | kein produktiver Android-Code geändert; Gradle-/Shield-Verifikation separat erforderlich |

Der fehlende Live-Test ist ein **echter Blocker**, nicht das Versprechen, dass Spotify grundsätzlich nicht funktioniert. Besonders die fehlende Podcast-Hörchronologie ist dagegen eine **harte API-Grenze**.

## Offizielle Belege

- Saved Shows: https://developer.spotify.com/documentation/web-api/reference/get-users-saved-shows
- Show Episodes: https://developer.spotify.com/documentation/web-api/reference/get-a-shows-episodes
- Saved Episodes: https://developer.spotify.com/documentation/web-api/reference/get-users-saved-episodes
- Recently Played (keine Podcasts): https://developer.spotify.com/documentation/web-api/reference/get-recently-played
- Currently Playing: https://developer.spotify.com/documentation/web-api/reference/get-the-users-currently-playing-track
- Quota Modes: https://developer.spotify.com/documentation/web-api/concepts/quota-modes
- Februar-2026-Migration: https://developer.spotify.com/documentation/web-api/tutorials/february-2026-migration-guide
- Februar-2026-Changelog: https://developer.spotify.com/documentation/web-api/references/changes/february-2026
- Juli-2026-Changelog: https://developer.spotify.com/documentation/web-api/references/changes/july-2026
- PKCE: https://developer.spotify.com/documentation/web-api/tutorials/code-pkce-flow
- Redirect-URIs: https://developer.spotify.com/documentation/web-api/concepts/redirect_uri
- Android Content Linking: https://developer.spotify.com/documentation/android/tutorials/content-linking
