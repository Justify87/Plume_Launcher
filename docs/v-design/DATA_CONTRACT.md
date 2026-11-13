# V-Design — geprüfter Android-TV/Nuvio-Datenvertrag

**Quellreview:** `Justify87/NuvioTV@be4b84a84e004a2d36405601683df000d8dc446a` (`dev`): `docs/android-tv-channels/README.md`, `TvChannelKind.kt`, `TvChannelFeedBuilder.kt`, `AndroidTvChannelManager.kt`. **Plume-Baseline:** `Justify87/Plume_Launcher@fbf2473711c50d03eb76afe8fd0a722af1faf136` (`main`), `AndroidTvMediaSource.kt`, `MediaItem.kt`, `MediaFeedRepository.kt`, `MainActivity.kt`. Nur gelesen, kein Nuvio-Eingriff.

## Provider-Kanalvertrag (nicht nach lokalisiertem Displaynamen filtern)

| Nuvio `TvChannelKind` | `TvContract.Channels.COLUMN_INTERNAL_PROVIDER_ID` | Eigenschaft | V-Design |
| --- | --- | --- | --- |
| `CONTINUE` | `context.packageName` | Klassischer **gemischter** CW/Next-Up-Feed | Unverändert in Nuvio; **nicht aufsplitten** |
| `IN_PROGRESS` | `context.packageName + ":in-progress"` | Unfertige Filme/Episoden | Weiterschauen |
| `AVAILABLE` | `context.packageName + ":available"` | Bereits verfügbare Next-Up-Folgen, nicht nur jüngste | Neue Folgen verfügbar |
| `UPCOMING` | `context.packageName + ":upcoming"` | Zukünftige Next-Up-Folgen | Demnächst |

Paketnamen der echten Installation anhand der `package_name`-Spalte prüfen und gegen zulässige Nuvio-App-IDs validieren. Plume `AndroidTvMediaSource.isNuvioPackage()` kennt gegenwärtig `com.justify87.nuviotv.wireguard` sowie `.debug`, `.playstore`, `.playstore.debug`; Nuvios `app/build.gradle.kts` setzt als Basis `com.nuvio.tv`. **M01 muss real installierte Varianten gegenprüfen** und darf die Erkennung nicht auf überholte Namen reduzieren. Channelnamen sind lokalisiert, **nie Klassifikationsgrundlage**. Vier statt maximal drei Kanäle ist unter Android TV erlaubt; zusätzliche Kanäle sind opt-in im Standardlauncher. Kanal-Freigabe/Browsable/Provider-Zugriff auf Android TV 11 real testen; niemals Listing-Rechte umgehen.

## Tatsächlich im Nuvio-Publisher belegte Felder

`channel_id`, `type`, `title`, `intent_uri`, Programm-`internal_provider_id` (stabile programmspezifische ID), `weight` (absteigend mit Reihenfolge), `poster_art_uri`, `poster_art_aspect_ratio`, `logo_uri`, `season_display_number`, `episode_display_number`, `episode_title`, `short_description` (nur bei Kategorien mit Beschreibung), `long_description`, `release_date` (`yyyy-MM-dd`), `last_engagement_time_utc_millis`. `duration_millis` und `last_playback_position_millis` bei Progress, ggf. **synthetisch**. Channel-`package_name` und Channel-`internal_provider_id` separat lesen; Program-`internal_provider_id` **nicht mit Channel-ID verwechseln**. Null bzw. Providervarianten zulassen.

Die Nuvio-Channelmanager-Implementierung kodiert einen expliziten `Intent(context, MainActivity::class.java)` als `intent_uri`, inkl. `contentId`, `contentType`, `videoId`, S/E, `launchMode`. Der reale Intent muss beim Öffnen auf erwartetes Nuvio-Paket und sichere Intent-Komponenten/Extras geprüft werden.

## Fachliche Regeln

- **IN_PROGRESS:** sortiert nach `lastWatched` absteigend; Hero = erstes gültiges Element. Fortschritt nur aus belegtem Verhältnis oder vom Publisher gelieferten Prozentwerten. **Achtung:** Nuvio setzt, wenn nur `progressPercent` verfügbar ist, **synthetisch** `durationMillis=100000` und `positionMillis=percent*1000`; diese 100 Sekunden sind **keine tatsächliche Laufzeit**. In diesem Fall darf Plume **keine Minuten/restliche Dauer** behaupten. M02 muss reale Dauer gegenüber Prozent-Fallback sicher unterscheiden oder Restzeit weglassen.
- **AVAILABLE:** Bereits ausgestrahlte Next-Up-Episoden unabhängig von Aktualität. `short_description` mit `New Episode/New Season/Next Up` ist lokalisierter **Text**, kein persistentes maschinenlesbares Alert-Flag. `Neu`-Badge nur bei explizit belegbarem Release Alert; andernfalls weglassen.
- **UPCOMING:** `release_date` ist ISO-Kalendertag; wenn Uhrzeit fehlt, keine Uhrzeit erfinden. Der Nuvio-Builder interpretiert date-only Release-Zeit beim Klassifizieren als UTC-Mitternacht, stellt zur UI ggf. eine lokale Datumsausgabe bereit; **Tag- und Zeitzonengrenzen können divergieren**. M02 muss lokale Anzeige/Tageswechsel reproduzierbar testen. Bei fehlendem Datum kein erfundener Countdown. Deep Link öffnet Details; **nie** ungeprüft starten.
- **Dedupe:** Prüfe Serien-/Content-ID, Staffel und Episode sowie kanalspezifische Herkunft. Programm-`_id` ist Datenbank-Row-ID, **keine übergreifende Episodenidentität**. Unterschiedliche Episoden der gleichen Serie dürfen nicht kollabieren. Ein begonnener Titel hat unter den neuen Nuvio-Kanälen Vorrang vor Next Up, Legacy bleibt absichtlich unabhängig.
- **Counts & Leerstand:** Headeranzahl aus tatsächlich validierten/duplikatbereinigten Einträgen, nicht montierten Views. Nicht installierte App, fehlender Zugriff, leerer/unsichtbarer Provider: Empty-State, keine erfundenen Daten.
- **Aktualisierung:** Vorhandenen Plume-Worker/Repository-Snapshot nutzen, kein neuer Hintergrund-Poller oder Netzwerkdienst; Nuvios Quelle bleibt eigener Cache. Kein Zugriff auf Nuvios interne Datenbank/Dateien, keine TMDB/Trakt-Hilfsabfragen.

## Bestehende Plume-Lücken (M01/M02, jetzt ausdrücklich **nicht** behoben)

1. `AndroidTvMediaSource.select()` nutzt lokalisierte Kanalnamen `Continue Watching` und `Next Up` und mischt SmartTube-Subscriptionen in `MediaCategory.NEW`; kein Mapping der drei Nuvio-Channel-IDs.
2. `MediaCategory` kennt nur `CONTINUE/NEW/RECOMMENDATION/UNKNOWN` und nicht drei definierte Reihen.
3. `MediaItem` hat `engagedAtMillis`, `durationMillis`, `playbackPositionMillis`, aber keine ausdrückliche Release-Date-/S/E-/Belegt-Flag-Metadatenstruktur für V-Design.
4. `MediaFeedRepository` sortiert derzeit generisch mit per-App-Quoten; nicht ohne Anpassung für den strikt Nuvio-basierten Hero und korrekten Upcoming-Termin geeignet.
5. Providerberechtigung, Sichtbarkeit, Android-11-Kanalregistrierung sowie echte Nuvio-Intent-Ausführung sind **noch nicht** auf Shield verifiziert.


## M01 — implementierter Provider-Vertrag und reale Builds (13.11.2026)

**Nuvio-Quell-HEAD (read-only)** `be4b84a84e004a2d36405601683df000d8dc446a`: `TvChannelKind.kt` Git-Blob `76a7cdeaaafab1a22189a1cfe3107a99261cd75c`, `AndroidTvChannelManager.kt` `13a1ab9c22cf7641fea319af2b3f2c17cad1cce1`, `TvChannelFeedBuilder.kt` `b8aecf52e40529bbefe02bd92f683f58e96c1ab0`, `app/build.gradle.kts` `8bc01a1b06c1674c443470c05a627395087dca71`; `docs/android-tv-channels/README.md` tatsächlich gelesen. Nuvio ändert sich durch M01 **nicht**.

**Installierte Package-Identitäten (Gradle validiert):** FullRelease `com.nuvio.tv`, FullDebug `com.nuviodebug.com`, PlayStoreRelease `com.nuvio.app`, PlayStoreDebug `com.nuvio.appdebug`. Benchmark über `applicationIdSuffix = .debug`: `com.nuvio.tv.debug`, `com.nuvio.app.debug`; diese optionalen Varianten nur als Paketidentität, nicht als auf Shield beobachtete Installation. Historische Plume-Unterstützung `com.justify87.nuviotv.wireguard`, `.debug`, `.playstore`, `.playstore.debug` bleibt für Altinstallationen. Jede ID strikt vollständig statt über Paketpräfix abgleichen. Die obige M00-Aussage, Plume **kenne nur** historische IDs, bezeichnet den **alten** Source-Stand.

**Datentransport:** `TvPreviewChannel(id: Long, packageName: String, displayName: String, channelProviderId: String?, browsable: Boolean?)` liest `_id`, `package_name`, `display_name`, `internal_provider_id` und `browsable` aus `TvContract.Channels`. Kanal-ID != programmspezifische interne ID; `AndroidTvProgram` hat nun zusätzlich `channelId`, `channelProviderId` und unbearbeitetes `releaseDate`. Ohne Browsable-Freigabe oder passende Provider-ID wird eine neue Kategorie **nicht** geraten. `NuvioProviderFeeds(inProgress,available,upcoming)` hält vollständige `NuvioFeedEntry(program,kind,legacyFallback)` getrennt und wird bei fehlendem TV-Listings-Privilege geleert. Programm-ReleaseDate bleibt als Originalstring für M02; fehlende Felder sind `null`, nicht mit Testinhalten befüllt.

**Classic- und SmartTube-Pfad:** Bestehende TV-IO-Query, `hidden_watch`, `cw_off`, Classic-Watch-Next/Preview-Quellen und historische SmartTube-Subscriptions bleiben aktiv; neue Nuvio-Kanäle werden aus der Classic-Auswahl ausgeschlossen. Der frühere Nuvio-`Next Up`-Namenspfad bleibt als Legacy-Kompatibilität nur für bekannte alte Paket-Aliase außerhalb der drei neuen Feeds; dieser Legacy-Pfad ist **kein** Beleg für `:available`. Positiver Progress-Rückgriff auf Legacy-Mixed/Watch-Next nur für `inProgress` und nur ohne sichtbaren expliziten In-Progress-Kanal. Nicht installierte Nuvio-App, ungeklärte Approval, Provider-Exceptions und nicht vorliegende Zusatzkanäle erzeugen keine Kategorien. Keine neue Permission/Service/Netzwerkabfrage.

**Verbleibende M02-Aufgaben:** Quellprogramme in finale Datenmodelle/Präsentationskategorien überführen, Source-spezifisches Dedupe, Episoden-/Staffelidentität, Korrektheit von Release-/Datumssortierung, ISO date-only mit Zone, synthetische 100000-ms-Dauer vs. Prozent-Evidenz, sichere Deep-Link-Validierung. M01 enthält absichtlich **keine neue Kachel-Darstellung**.

 
## M02 — implementierter Projektionsvertrag (2026-11-13)

**Basis:** Plume `main@3d4abbdf4f3dbfb481e0a8a6bf95aea815a172bd` enthält gemergtes M01 #34. Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a` unverändert. Klassischer Nuvio-`CONTINUE`-Mischkanal ist kein Available-/Upcoming-Provider. Keine direkte Nuvio-App-DB/Netzwerkabfrage.

- **Quellparser:** `MainActivity.readPrograms()` liest nun zusätzlich nullbare Spalten `short_description`, `long_description`, `poster_art_uri`, `logo_uri`. Bereits verfügbar: reales Channel-`internal_provider_id`, programmspezifische `internal_provider_id`, `release_date`, S/E, echte Program-Weight-Reihenfolge und TV-Zeitwerte. `Prog`/`AndroidTvProgram` transportieren Metadaten nullsicher weiter; der Classic-Zweig ignoriert die neuen Felder.
- **Projizierte Struktur:** `NuvioDashboardFeed.sections()` ist **fest** `IN_PROGRESS`, `AVAILABLE`, `UPCOMING` (auch leer). `NuvioMediaItem` enthält mindestens `stableId/section/sourcePackage/title/seriesId/seasonNumber/episodeNumber/episodeTitle/shortDescription/longDescription/releaseDate: LocalDate?/artworkUri/posterUri/logoUri/intentUri/progressPercent/durationMillis/playbackPositionMillis/remainingMillis/lastWatchedMillis/providerOrder/legacyFallback`. Keine Fake-Daten oder SmartTube-Medien. `count` ist die Gesamtzahl gültiger eindeutiger Einträge, nicht der gerenderten Karten.
- **Episode/Dedupe:** Nuvio `progressKey` = `contentId_s{season}e{episode}`, in Watch Next ggf. `wn_`. Normalisierung entfernt nur bekannten Prefix und exakten S/E-Suffix, übernimmt explizite Staffel/Folge in eigenen Key; alternativ erkannter `S.contentId` aus serialisiertem Intent, falls interne ID fehlt. Ohne belastbare Serien-/Contentidentität keine Dedupe über TV-`_id`, Text oder generisches Launcher-Intent. **Cross-Channel-Priorität:** In-Progress vor Available vor Upcoming. Gleiche Serie, andere Episode, bleibt eigenständig (soweit Nuvio sie überhaupt in TvContract veröffentlicht).
- **Sortierung:** In-Progress `engagedAtMillis` absteigend (normalisiert), Tie `sourceOrder`; Available streng `sourceOrder` (= TV-Weight-Ranking), keine behauptete Release-Neuigkeit; Upcoming strikt ISO `yyyy-MM-dd` als **lokaler Kalendertag** aufsteigend, null/ungültig zuletzt. `lastWatched` oder `sortTimestamp` ist **keine Release-Zeit**.
- **Zeit/Alerts:** Prozent nur aus Providerwert oder plausiblem Pos/Dur-Verhältnis. Wenn Nuvio `duration=100000ms` veröffentlicht, reale Dauer/Position/Restlaufzeit aus Vorsicht null; Prozent darf bestehen. Sonst Restzeit nur bei `position>=1000ms`, gültiger echter Dauer und `position<=duration`. Beschreibung ist **kein** strukturiertes `isReleaseAlert`; Badge bleibt falsch. `daysUntilRelease(now, zone)` ist tagesbasiert mit `0=Heute`, `null=kein gültiger zukünftiger Termin`, `nextLocalDayBoundary` DST-sicher; Render-/Resume-/Midnight-Anbindung folgt in M03.
- **Geräte-/Sicherheitsgrenzen:** `intentUri` bleibt ungeprüfte Rohdatenquelle und darf nur über bisherige geprüfte Launchgrenze ausgeführt werden. Nuvio-`UPCOMING` in M03/M05 ausschließlich Details. Keine neuen Permissions oder Services. TV-11-/Shield-Browsability, tatsächliche Serienangebote und reale Deep Links `hardware_pending`.

**Beleg:** neue `NuvioDashboardFeedTest` reine JUnit-Testfälle; [M02 PR #35](https://github.com/Justify87/Plume_Launcher/pull/35) CI zum Nachtragszeitpunkt noch nicht vollständig abgeschlossen. `REFERENCE.png` unverändert 1536×864, SHA-256 bestätigt.

 
**M02 Datenvalidierungsnachtrag:** Nicht nur synthetische 100000-ms-Dauer, sondern auch `position > duration` ist unsichere Evidenz: selbst ein aus dem Classic-Parser übernommener geklemmter `progressPercent=100` wird im V-Feed zu `null` normalisiert (Commit `6b740628`, Test `1a057219`). Keine Änderung am alten Classic-UI vor M03–M06.


## M04 – Rendering-Vertrag auf M02-Typen (2026-11-14)

- `VDesignCardView` akzeptiert ausschließlich `NuvioMediaItem` aus `NuvioDashboardFeed`, getrennt nach `IN_PROGRESS/AVAILABLE/UPCOMING`. `VDesignHomeView` bindet 3+3+8 Slots, Counts sind komplette M02-Listen; fehlender Content = neutrale leere Zelle ohne SmartTube/Mockup-Dummy.
- Artistische Bildauswahl: `artworkUri` → `posterUri` → `logoUri` wenn URI `http/https/content` ist; Bitmap-Ladung über existierenden asynchronen `loadPoster` und LruCache. Caching und URL/Intent-Security über Android/Shield später integrieren und testen.
- Echte Restzeit = `remainingMillis` bei `durationMillis`; Fortschrittsprozent zeigt sich unabhängig von unbekannter Dauer; synthetische 100000ms werden im M02-Projektor vor M04 verworfen.
- `episodeTitle` nur bei Verfügbarkeit, `seasonNumber`/`episodeNumber` separat optional. Bei UPCOMING nur bestehendes `LocalDate` + dynamischer `daysUntilRelease`-Countdown.
- `showNewBadge` false, solange die Quell-App den erforderlichen strukturierten Indikator nicht veröffentlicht. Die vorhandene `shortDescription` ist kein maschinenlesbares Flag.
- Streng validierte TV-Intents zu expliziter Nuvio-MainActivity; UPCOMING nur `detail`. Nuvio-Classic/Legacy-Continue bleibt in Nuvio unverändert; Plume mischt ihn nicht unter neue Kategorien.
