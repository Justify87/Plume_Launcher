# V-Design — M00–M08 Roadmap

**Regel:** Jeder Schritt beginnt am **tatsächlich gemergten** Vorgänger oder, solange dessen PR ungemergt ist, auf dessen **echtem PR-HEAD** (Stacking). Kanonischer Status in `STATE.md`, Tests in `TEST_MATRIX.md`; keine `passed`-Erfindungen.

| Schritt | Abhängigkeit | Konkretes Deliverable | Stand 2026-11-14 | Branch / PR | Review |
| --- | --- | --- | --- | --- | --- |
| M00 | Plume baseline; #32 + #33 merged | Originalreferenz, Verträge, Baseline, Inventar | **source/reference+CI passed; hardware not applicable** | [#32](https://github.com/Justify87/Plume_Launcher/pull/32) + [#33](https://github.com/Justify87/Plume_Launcher/pull/33) merged; [CI success 43509990732](https://github.com/Justify87/Plume_Launcher/actions/runs/43509990732) | `REVIEWS.md#M00` |
| M01 | M00 merged `59c7988` | Exakte Nuvio-Provider-/Paket-/Opt-in-Zuordnung, separate ungerenderte Daten | **merged into main `3d4abbdf`; final CI success, hardware pending** | [#34](https://github.com/Justify87/Plume_Launcher/pull/34) merged; [run 43514084610](https://github.com/Justify87/Plume_Launcher/actions/runs/43514084610) success | `REVIEWS.md#M01` |
| M02 | M01 merged `3d4abbdf` | Drei Datenkategorien, Episode-Dedupe, Prozent vs. Dauer, civil date/Countdown | **merged `main@3f77ba55`; code CI success, Shield pending** | [#35](https://github.com/Justify87/Plume_Launcher/pull/35) merged; [run 43517654027](https://github.com/Justify87/Plume_Launcher/actions/runs/43517654027) success on code HEAD | `REVIEWS.md#M02` |
| M03 | M02 merged `3f77ba55` | Ein sichtbarer V-Home-Host, 14 neutrale Geometrieplätze, fünf Sidebar-Ziele und echter Nuvio-Count | **merged `main@763869b8`; code+docs CI success on `8160f2bf`; Shield pending** | [#36](https://github.com/Justify87/Plume_Launcher/pull/36) merged | `REVIEWS.md#M03` |
| M04 | M03 merged `763869b8` | 14 echte Nuvio-Hero-/Standard-/Compact-Cards, Poster/Progress/Datum und Intents | **merged `main@fdd10f3`; final CI passed; Shield pending** | [#37](https://github.com/Justify87/Plume_Launcher/pull/37) merged, [CI 43524952681](https://github.com/Justify87/Plume_Launcher/actions/runs/43524952681) success | `REVIEWS.md#M04` |
| M05 | M04 merged `fdd10f3` | D-Pad, All Apps, Settings, HDMI, Back/HOME und Details-/Playback-Ziele | **code+JVM-tests committed; PR CI in progress; Shield pending** | [Draft #38](https://github.com/Justify87/Plume_Launcher/pull/38), `feature/vdesign-m05-navigation-interaction` | `REVIEWS.md#M05` |
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

 
**M02 CI-Nachtrag:** [Run 43517654027](https://github.com/Justify87/Plume_Launcher/actions/runs/43517654027) ist **completed/success** auf `97ed765829d8e75dde9868925bd3cd7b607fff2b`; `testDebugUnitTest lintDebug assembleDebug assembleRelease` erfolgreich, Release-APK +3192 B. Der Evidence-Docs-Commit danach erzeugt einen neuen HEAD; dessen CI muss separat beobachtet werden. M03 bleibt exakt nächster Meilenstein, Shield-Abnahme offen.


## M03-Stand (2026-11-13) — vor Hardware- und finaler PR-CI-Abnahme

- M02 ist nachweislich in `main@3f77ba552b47152349d28aae15b02a865b367649` gemergt; alte M02-Draft-Angaben oben sind historisch.
- [Draft PR #36](https://github.com/Justify87/Plume_Launcher/pull/36) fügt das einzige sichtbare Referenzraster hinzu; alte Classic/Overview-Views verbleiben rein technisch bis M06, nicht als zweite sichtbare Seite.
- M03 bindet die **vollen M02-Counts**; die 14 Kartenplätze sind bis M04 neutral/nicht fokussierbar. Noch keine echte Content- oder Bild-Abnahme.
- Fehlende App bzw. VPN-Activity wird aus den Sidebar-Targets ausgeschlossen. `Alle Apps` und HDMI/Sources bleiben über langes OK auf Übersicht bzw. Einstellungen vorläufig erreichbar; D-Pad/HOME/Back umfassend erst M05/Shield testen.
- **Exakt nächster Meilenstein: M04** — reale Nuvio-Kacheln/Artworks/Progress/Release-Hinweise, sichere Player-/Detail-Intents und DPAD-Fokus.

**M03 nachträglicher CI-Abschluss:** [Run 43521292243](https://github.com/Justify87/Plume_Launcher/actions/runs/43521292243), [check 132215945300](https://github.com/Justify87/Plume_Launcher/actions/runs/43521292243/job/132215945300), Code+Docs-SHA `8160f2bf05df335b52c9735c675b12a8dbda57b0`: `completed/success`. Unit-Tests, Lint, Debug+Release, Größenvergleich bestanden, Release-APK **535462 → 540870 Bytes (+5408)**. Dieser Evidence-Commit erzeugt einen neuen SHA, dessen CI nicht vorweggenommen wird. Shield/visuelle Abnahme bleibt offen.


## M04 (aktueller Schritt, 2026-11-14 Europe/Berlin)

- M03 ist über #36 tatsächlich gemergt, nicht mehr Draft. Verbindliche Quelle für den aktuellen M04-Status: [STATE.md](STATE.md), keine alten M03-Handoff-Zwischenstände.
- 14 Nuvio-Karten nach Originalgeometrie gebunden; echte Provider-Counts statt Montageslots; SmartTube nur Sidebar. Keine neuen Berechtigungen, kein Mullvad-Submodul-Diff.
- Quelle der Daten M01/M02; M04 übernimmt ausdrücklich keine Erfindung eines `Neu`-Flags oder synthetischer Minuten.
- JVM-Testquellen committed; [PR #37](https://github.com/Justify87/Plume_Launcher/pull/37) CI-Status nur mit geprüftem Run; Emulator/Shield/Pixel-Overlay offen.
- **M05 ist exakt als Nächstes dran**: D-Pad-Übergänge zwischen Sidebar/Hero/Standard/Compact, Back/HOME, Alle Apps, Settings, HDMI, VPN, Intent-Laufzeit und Fokusrestauration.


**M04 Korrektur vor CI-Abnahme:** [13988ae](https://github.com/Justify87/Plume_Launcher/commit/13988ae216feecd33c39f5e3b491294b9fff6c22) verbessert Fokus-Fallback und URI-Authority-Validierung mit Tests; finaler Docs+Code-HEAD-CI ist weiter nicht als bestanden gewertet. M05 unverändert der nächste Meilenstein.


**M04 Testbarkeit:** Der gezielte JVM-Binder `VDesignCardSlots` (Commit `3516fb`) prüft 3+3+8-Slot-Zuordnung und Upcoming-Paging ohne Android-UI-Simulator. Reale Layout-/Bildprüfung bleibt M07/M08. Erster finaler CI-Gate ist weiterhin abzuwarten; M05 folgt.


## M05-Übergabe 2026-11-14

M04 PR #37 ist real gemergt auf `fdd10f3394463bd5d4142c9978834d78464905cc`; M05 Draft [#38](https://github.com/Justify87/Plume_Launcher/pull/38) verwendet ausschließlich diese Basis, [Code-HEAD d80aa6f](https://github.com/Justify87/Plume_Launcher/commit/d80aa6fdc5e4d55d2a5b003ee7ce4638ec59296d) vor Dokumentation. Keine zweite visuelle Home-Seite, keine neue Permission, keine Änderung am Mullvad-Gitlink. Die neuen räumlichen Focus- und Intent-Unit-Tests sind eingecheckt; Gradle-Lauf [43528175980](https://github.com/Justify87/Plume_Launcher/actions/runs/43528175980) zu diesem Zeitpunkt noch nicht final bestätigt. Physische Shield-, VPN- und Nuvio-Runtime-Gates bleiben `hardware_pending`. Nächster Schritt **M06**, erst nach Review des finalen PR-HEAD und ohne implizite Mergefreigabe.
