# V-Design — M00–M08 Roadmap

**Regel:** Jeder Schritt beginnt am **tatsächlich gemergten** Vorgänger oder, solange dessen PR ungemergt ist, auf dessen **echtem PR-HEAD** (Stacking). Kanonischer Status in `STATE.md`, Tests in `TEST_MATRIX.md`; keine `passed`-Erfindungen.

| Schritt | Abhängigkeit | Konkretes Deliverable | Stand 2026-11-13 | Branch / PR | Review |
| --- | --- | --- | --- | --- | --- |
| M00 | Plume baseline; #32 + #33 merged | Originalreferenz, Verträge, Baseline, Inventar | **source/reference+CI passed; hardware not applicable** | [#32](https://github.com/Justify87/Plume_Launcher/pull/32) + [#33](https://github.com/Justify87/Plume_Launcher/pull/33) merged; [CI success 43509990732](https://github.com/Justify87/Plume_Launcher/actions/runs/43509990732) | `REVIEWS.md#M00` |
| M01 | M00 merged `59c7988` | Exakte Nuvio-Provider-/Paket-/Opt-in-Zuordnung, separate ungerenderte Daten | **merged into main `3d4abbdf`; final CI success, hardware pending** | [#34](https://github.com/Justify87/Plume_Launcher/pull/34) merged; [run 43514084610](https://github.com/Justify87/Plume_Launcher/actions/runs/43514084610) success | `REVIEWS.md#M01` |
| M02 | M01 merged `3d4abbdf` | Drei Datenkategorien, Episode-Dedupe, Prozent vs. Dauer, civil date/Countdown | **implemented, CI pending, hardware pending** | [Draft #35](https://github.com/Justify87/Plume_Launcher/pull/35); [first run 43516551245](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245) in_progress at review | `REVIEWS.md#M02` |
| M03 | M02 | einziges V-Layout und Sidebar; sichere App-/VPN-Ziele | not_started | — | pending |
| M04 | M03 | echte Hero-/Standard-/Mini-Poster + typografische Overlays/Focus | not_started | — | pending |
| M05 | M04 | D-Pad, All Apps, Settings, HDMI, Back/HOME und Details-/Playback-Ziele | not_started | — | pending |
| M06 | M05 | Classic/Overview + `home_layout_mode` **wirklich entfernen**, Pref-Migration, alte Settings-Pfade sichern | not_started | — | pending |
| M07 | M06 | Screenshotvergleich 16:9, Ressourcennutzung, Missing-Permissions, Test-/CI-Fixes | not_started | — | pending |
| M08 | M07 | Gesamtabnahme, Shield Pro 2019 und Release-Handoff | not_started | — | pending |

## Finale Abnahmematrix / Definition of Done

| Gate | Evidenz | Abnahmebedingung |
| --- | --- | --- |
| Originalbild | SHA-256 + echte Repo-Datei, 1536×864 | **binary_equal** gegen ZIP, andernfalls fail |
| Single Home | Code-/Settings-/Manifest-Diff + TV-Test | keine Classic-/Overview-Umschalter und kein alter Home-Host |
| Drei Nuvio-Feeds | Provider-ID-Test + echte Appdaten | in-progress / available / upcoming strikt getrennt |
| Legacy & SmartTube | Regressionstest | klassischer Mischkanal in Nuvio unverändert; keine SmartTube-Kachel |
| Content-Truth | Null/Duration/Alert/Countdown/Dedupe-Tests | keine falschen Minuten, „Neu“, Daten oder Counts |
| Navigationsschutz | D-Pad-Emulator/Shield-Matrix | All Apps, Settings, HDMI, HOME/Back, Fokus, VPN erreichbar |
| Mullvad-Lebenszyklus | Diff + echte Integrationschecks | Originalsubmodul/Rust/JNI/VpnService bewahrt |
| Build/CI | konkrete Job-URLs + Status | erfolgreiche verpflichtende Jobs nachweisbar, nicht vermutet |
| Referenztreue | echte UI-Screenshots + Koordinatenvergleich | dokumentierte legitime Abweichungen; keine Mockup-Titel |
| Shield-Abnahme | Pro 2019 Testprotokoll | nur bei tatsächlicher Durchführung `passed`; sonst `hardware_pending` |

**Aktuelle Grenze:** M00 enthält nur Dokumentation und byteidentische Normreferenz, keinen UI-Wechsel. Das Referenz-Gate ist durch Git-Blob-/SHA-Identität erfüllt. M01 beginnt erst nach beobachteter M00-Ergänzungs-PR-CI, Review und regelgerechter Übergabe (gemergter oder gestapelter tatsächlicher HEAD). Die Originalbytes sind JPEG/JFIF mit historischer `.png`-Endung: nicht still in PNG transkodieren.


**M01-Gate, beobachtet 2026-11-13:** Source-Code und reine JVM-Tests vorhanden; PR-CI auf dem finalen Dokumentationscommit noch `pending`, echte Shield-/Android-TV-11-Channel-Browsability `hardware_pending`. Das M00-Referenz-Gate ist abgeschlossen; frühere M00-Notizen in historischen Absätzen dürfen nicht mit dem aktuellen M01-Status verwechselt werden. Kein Merge erteilt.

**Nachtrag M01:** [CI #43513105647](https://github.com/Justify87/Plume_Launcher/actions/runs/43513105647) für Code-HEAD `30084ee9` `completed/success`; der `check`-Job führte Unit-Tests, Lint, Debug-/Release-Builds und APK-Vergleich erfolgreich aus (+2848 Bytes). Vorheriger [Run #43512433898](https://github.com/Justify87/Plume_Launcher/actions/runs/43512433898) mit zwei Testfehlern wurde im Commit `30084ee9` ursächlich behoben. Neuer CI-Lauf für diesen Dokumentationscommit separat auswerten. Shield und realer TV-Provider bleiben `hardware_pending`.

 
**M02-Übergabe 2026-11-13:** M01 ist tatsächlich gemergt; historische Anmerkungen mit `M01 pending` sind nicht aktuell. M02 arbeitet auf [PR #35](https://github.com/Justify87/Plume_Launcher/pull/35) ohne UI-Refactor, nur Quellmodell/Parser/Tests. Countdown-Datum ist dynamisch bei erneuter Auswertung; **M03 muss das Render-Rebind/Resume und den einzelnen DST-sicheren Tagwechsel-Callback tatsächlich verdrahten**. Kein Merge ohne Freigabe.
