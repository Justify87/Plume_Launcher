# V-Design — Test- und Evidenzmatrix

Die folgenden Statuswerte sind **beobachtete** Ergebnisse oder `not_run`; spätere Jobs dürfen nicht als bereits ausgeführt dargestellt werden.

| Prüfung | Evidenz / Ausführung | Ergebnis | Geplant |
| --- | --- | --- | --- |
| ZIP Seed-Datei vorhanden und tatsächlich lesbar | Python `zipfile` auf hochgeladenem ZIP; Datei 249663 Bytes | **passed_local** | M00 |
| Referenz-SHA256 stimmt | Python `hashlib.sha256` `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25` = `REFERENCE_SHA256.txt` | **passed_local** | M00 |
| Referenz 1536×864 | PIL `Image.open`, RGB 1536×864; tatsächlich visuell angesehen | **passed_local** | M00 |
| Byteidentische `docs/v-design/REFERENCE.png` im Git-PR | Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18` exakt unter neuem Pfad, Source-Blob identisch; 249663 Bytes, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25` | **passed_binary_identity** | M00 |
| Source Baseline Plume/Nuvio | GitHub API main `fbf2473711c50d03eb76afe8fd0a722af1faf136`; dev `be4b84a84e004a2d36405601683df000d8dc446a` | **passed_source_review** | M00 |
| Nuvio Channel IDs/Publisher-Felder | `TvChannelKind`, `TvChannelFeedBuilder`, `AndroidTvChannelManager` gelesen | **passed_source_review** | M00 |
| Veraltete Mullvad-Doc-Pfade identifiziert | Repo-Baum, `docs/archive/legacy-features/mullvad-vpn/STATE.md` | **passed_source_review** | M00 |
| UI-Featureinventar statt Blind-Löschung | `MainActivity`, XML, Media-/Focus-Klassen, Manifest/Gradle | **passed_source_review** | M00 |
| Dokumentationsdiff / Dateirechte / Binärdiff | GitHub Compare `fbf2473...c8b0241`: genau 10 neue Textdateien `docs/v-design/*`, 0 andere Änderungen | **passed_source_review** (binary PNG weiterhin fehlt) | M00 |
| Android/Gradle lokal | `./gradlew testDebugUnitTest lintDebug` | **not_run — Container ohne Netz/Repo-Checkout/Android SDK** | M00 |
| Plume M00-CI | Historischer [PR-#32-Run 43505553469](https://github.com/Justify87/Plume_Launcher/actions/runs/43505553469): `cancelled`; neuer Referenz-PR noch nicht gelaufen | **pending_new_pr_ci — kein Pass** | M00 |
| Exakte drei Nuvio Channel Types | Provider-ID-Adaptertests | not_run | M01 |
| Legacy-Kanal nicht als Available mischen | Mixed-Feed-Unit-Tests | not_run | M01 |
| TV-Permission denied / Nuvio fehlt | Provider-/Emulator-/Shield-Test | not_run | M01–M07 |
| Real vs synthetischer Fortschritt / Restzeit | M02-Unit-Tests | not_run | M02 |
| „Neu“ nur bei nachgewiesenem Alert | Release-Tests | not_run | M02 |
| Upcoming Release und lokale Zeitzone/Tageswechsel | M02-Unit-Tests | not_run | M02 |
| Episodensicheres Dedupe, Anzahl und Sortierung | M02-Unit-Tests | not_run | M02 |
| Sidebar, 3 Reihen, Hero ≈52 % | echte UI-Screenshots plus Bildoverlay | not_run | M03–M07 |
| echte/missing Poster, Cache | Emulator/Unit-Tests | not_run | M04 |
| D-Pad/Back/HOME/Long-OK/HDMI/Settings/Alle Apps/VPN | Emulator/Shield | not_run | M05 |
| Nuvio Deep Link und Upcoming Detail | echter TV-Integrationstest | not_run | M05 |
| keine alten Home-Modi/Settings | Code-Suche und Migrationstests | not_run | M06 |
| JVM-Tests, lint, Debug/Release und echte CI | CI-Lauf-/Job-URLs | not_run | M07–M08 |
| Mullvad original JNI/VpnService/Gitlink | Git-Diff + Integrations-CI | not_run | M07–M08 |
| Shield Pro 2019 (Android TV 11), VPN & Performance | physisches Messprotokoll | **hardware_pending** | M08 |

**CI-Baseline vor M00:** `https://github.com/Justify87/Plume_Launcher/actions/runs/43503999928` (vorheriger Mullvad-Docs-Archivierungs-PR), bei Abfrage `in_progress`. Andere ältere PR-Runs sind kein Nachweis für M00. `main`-Combined-Status-API lieferte `statuses: []` (kein Erfolgsbeleg).

**Review-Regel:** `passed_source_review` beweist ausschließlich gelesenen Quelltext, nicht Laufzeitkorrektheit. Weder physischer Test noch Bildoverlay des realen Launchers wurde durchgeführt.

## M00-Nachtragsprüfungen 2026-11-13

| Prüfung | Tatsächlich geprüfter Befund | Status |
| --- | --- | --- |
| Prüfsummenberechnung über echte GitHub-Originalbytes | Base64-Dekodierung des GitHub-Blobs, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`; Testvektoren `abc` und leerer String stimmen | passed |
| Containerkennung und Pixelgröße | JPEG/JFIF-Signatur `FF D8 FF E0`; SOF0-Header 1536×864 (die originale `.png`-Endung täuscht PNG vor) | passed_identified_mislabeled_jpeg |
| Binärkopie in kanonischen Gitpfad | Git-Tree referenziert alten unveränderten Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18` als `docs/v-design/REFERENCE.png` im Commit `e1c13b2783aa632c2cbf391b10eb923426f8fc81` | passed_blob_identity; nach PR-Commit nochmals read-back |
| Historische PR-#32-CI | [Run 43505553469](https://github.com/Justify87/Plume_Launcher/actions/runs/43505553469) abgeschlossen `cancelled` | cancelled, kein Erfolg |
| Neuer M00-Ergänzungs-PR-CI | Nach PR-Erstellung mit Run-/Job-URL und Endstatus nachtragen | pending |
| Android-Gradle lokal, Screenshot-Vergleich, Shield Pro 2019 | Keine lokale SDK-/Checkout-/Geräteumgebung; Bild ist Referenz und keine App-Screenshot-Evidenz | not_run / hardware_pending |
