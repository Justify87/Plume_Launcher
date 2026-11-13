# V-Design — M00–M08 Roadmap

**Regel:** Jeder Schritt beginnt am **tatsächlich gemergten** Vorgänger oder, solange dessen PR ungemergt ist, auf dessen **echtem PR-HEAD** (Stacking). Kanonischer Status in `STATE.md`, Tests in `TEST_MATRIX.md`; keine `passed`-Erfindungen.

| Schritt | Abhängigkeit | Konkretes Deliverable | Stand 2026-11-13 | Branch / PR | Review |
| --- | --- | --- | --- | --- | --- |
| M00 | Plume `main@fbf2473` | Originalreferenz, Verträge, echte Baseline und Migration-Inventar | **partial — Referenzbinärdatei noch nicht in PR** | [`feature/vdesign-m00-bootstrap-contract` / Draft #32](https://github.com/Justify87/Plume_Launcher/pull/32) | `REVIEWS.md#M00` |
| M01 | M00 **passed**, Referenz im Git | Exakte Nuvio-ID-Provider-Zuordnung, Permission/Empty-State, Legacy isoliert | not_started | — | pending |
| M02 | M01 | Drei Datenkategorien, Dedupe, Prozent vs. Dauer, Release/Countdown | not_started | — | pending |
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

**Aktuelle Grenze:** M00 enthält nur Dokumentation, kein UI-Wechsel. Nächster M01-Start ist solange blockiert, wie `REFERENCE.png` nicht im Repository verifiziert wurde.
