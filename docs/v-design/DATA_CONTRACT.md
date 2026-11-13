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
