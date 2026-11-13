# V-Design — echte Milestone-Reviews

## M00 — Bootstrap-Vertrag — 2026-11-13

- **Reale Git-Basis:** Plume `main@fbf2473711c50d03eb76afe8fd0a722af1faf136`, Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a`; kein V-Design-Vorgänger-PR. Work branch `feature/vdesign-m00-bootstrap-contract`. Implementierungscommit `c8b02415e8aa8a79bd9428db6e0b2737942d1c47`, [Draft-PR #32](https://github.com/Justify87/Plume_Launcher/pull/32) offen/unmerged; CI [Run 43505553469](https://github.com/Justify87/Plume_Launcher/actions/runs/43505553469) zunächst `in_progress`/ohne Conclusion.
- **Diff-Review:** GitHub Compare bestätigt 1 Commit, 0 behind und genau zehn neu angelegte Textdateien `docs/v-design/*`, kein Binärbild und keine produktiven Dateien. **Scope:** Nur `docs/v-design/` – Design-, Datenvertrag, Inventar, Tests, Entscheidungen und Übergabe. Keine Änderung an Nuvio, Kotlin/XML/Gradle, Mullvad-Submodul oder Berechtigungen. Referenz-PNG noch **nicht** im Git-Diff; binärer GitHub-Upload blockiert.
- **Quellprüfung:** Nuvio `TvChannelKind`, `TvChannelFeedBuilder`, `AndroidTvChannelManager`, Android-TV-README; Plume `MainActivity`, XML, Media-/Sidebar-/Focus-Klassen; aktueller archivierter Mullvad-M12-STATE; GitHub CI-Workflow/CONTRIBUTING/PR-Template.
- **Referenztreue:** Original `REFERENCE.png` aus hochgeladenem ZIP lokal (249663 Bytes, 1536×864 RGB) visuell gelesen und SHA gegen `REFERENCE_SHA256.txt` geprüft: `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`. Geometrie dokumentiert, **keine echte UI-Bildprüfung**. Visueller Konflikt SmartTube-Karten versus ausdrücklicher Normtext in D-004 entschieden.
- **Provider-Korrektheit:** Vier echte Nuvio-Kanäle, Legacy als eigenständiger Mixed Feed, neue Kanäle optional; `internal_provider_id` nicht lokalisierte Namen. Risiko synthetische 100-s-Progressdauer und uneinheitliche Release-Zeitsemantik dokumentiert. Keine Testaussage über aktiven Provider auf Shield.
- **UX-Schutz:** D-Pad, Focus, All Apps, Uhr/Topbar, HDMI, Wallpaper, Setup, Direct Boot, Long-OK, HOME/Back, Settings, VPN, Bildcache als Pflichtpfade inventarisiert; keine Löschung in M00.
- **Tests:** Python ZIP/SHA/PIL tatsächliche lokale Prüfungen erfolgreich; GitHub Source/Tree/PR/Workflow tatsächlich geprüft. Android `./gradlew testDebugUnitTest lintDebug`: `not_run` (keine lokale Netzwerk-/SDK-/Projekt-Buildumgebung). Hardware: `hardware_pending`, realer Screenshotvergleich: `not_run`.
- **Vor-M00-CI:** Archiv-PR-Lauf [43503999928](https://github.com/Justify87/Plume_Launcher/actions/runs/43503999928) zum Beobachtungszeitpunkt `in_progress`; **kein** M00-CI-Pass. Pull-Request-CI nach Erstellung gesondert prüfen.
- **Befunde:** Originalbild zeigt Demo-SmartTube-Karten trotz Sidebar-only-Vorgabe; Nuvio-Base-Package `com.nuvio.tv` vs. Plume bekannte Aliase; Mullvad-Unterlagen bereits archiviert; kein GitHub-Binärupload. M01 darf nicht mit ungeprüfter PNG-Referenz starten.
- **Entscheidung:** **partial**. Dokumentations-Bootstrap erstellt, aber M00-Abnahmekriterium „byteidentisches Referenzbild im Repository“ ist noch nicht erfüllt. Kein behauptetes UI-Redesign, kein Test-False-Positive.
- **Übergabe:** `HANDOFF.md` – zuerst Originalbild auf Branch ergänzen + SHA/CI/Review/PR nachziehen, danach M01.

### Review-Pflicht für M01–M08

Für jeden Meilenstein Datum, tatsächlichen Base-/Work-Commit und PR, Scope und Dateidiff, Datenquelle, Null-/Security-/Intent-Verhalten, Referenzvergleich (nur mit realen Screenshots), D-Pad/HOME/Back/Shield, ausgeführte Tests, exakte CI-Run/Job-URLs und beobachteten Endstatus, systematische Folgefehler, Entscheidung (`passed/partial/blocked`) und Handoff als eigenen Abschnitt ergänzen. **Nie** `passed` ohne Evidenz.
