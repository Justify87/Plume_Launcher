# V-Design — nächste ausführbare Übergabe

## M00 — tatsächlicher Stand 2026-11-13

- Ausgangscommit Plume `main`: `fbf2473711c50d03eb76afe8fd0a722af1faf136`.
- Nuvio `dev`: `be4b84a84e004a2d36405601683df000d8dc446a`.
- M00-Branch: `feature/vdesign-m00-bootstrap-contract`; [Draft-PR #32](https://github.com/Justify87/Plume_Launcher/pull/32) offen; Implementierungscommit `c8b02415e8aa8a79bd9428db6e0b2737942d1c47`; aktuellen Branch-HEAD stets neu prüfen, nie blind auf alten main wechseln.
- Quellprüfung: Nuvio 4-Kanalvertrag; Plume alte Classic/Overview/Home-Settings; Mullvad-M12-Status unter `docs/archive/legacy-features/mullvad-vpn/`.
- ZIP-Original `REFERENCE.png` wirklich gelesen, 1536×864, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`; **Datei fehlt in GitHub**, GitHub-Connector hat keinen lokalen Binär-Datei-Upload. M00 `partial`.
- Keine UI-Codeänderungen; keine realen Gradle-, Emulator- oder Shield-Tests. M00-PR-CI [Run 43505553469](https://github.com/Justify87/Plume_Launcher/actions/runs/43505553469) war beim ersten Abruf `in_progress` und ist kein Pass.

## Pflicht vor M01

1. **Zuerst M00 vervollständigen:** Aus Benutzer-ZIP exakt `repo-seed/docs/v-design/REFERENCE.png` nach `docs/v-design/REFERENCE.png` auf **dem M00-Branch** legen (nicht Bild neu generieren), binären SHA gegen `REFERENCE_SHA256.txt` prüfen, committen und M00-PR aktualisieren. Solange das fehlt: **M01 nicht auf Verdacht ausführen**.
2. Realen PR-/Head-/CI-Status und ggf. Review nachziehen; M00 erst bei bestandenem Referenz-Gate auf `passed` stellen. Kein Merge ohne Nutzerfreigabe.
3. Anschließend M01 anhand `DATA_CONTRACT.md`: Nuvio-`IN_PROGRESS/AVAILABLE/UPCOMING` nach echter Channel-`internal_provider_id` anbinden; Legacy-Mischkanal und SmartTube-Inhalte nicht auf drei V-Reihen verteilen. Tatsächliche Nuvio-Paketnamen, Sichtbarkeits- und TV-Permissions prüfen.
4. Vor Änderung `STATE.md`, `ROADMAP.md`, `DESIGN_CONTRACT.md`, `DATA_CONTRACT.md`, `DECISIONS.md`, `INVENTORY.md`, `REVIEWS.md`, `TEST_MATRIX.md` und archivierten Mullvad-M12-Stand lesen. Fokus, Settings, HOME/Back, All Apps, Mullvad und Originalsubmodul erhalten.

## Offene systematische Risiken

Synthetische `duration=100000ms`-Progressdaten erzeugen sonst falsche Restzeit; Kanalnamen in Plume lokalisiert; ältere Nuvio-Paket-Aliase können an `com.nuvio.tv` vorbeigehen; alte Topbar enthält essentielle Aktionen und darf in M06 nicht blind gelöscht werden; Originalbild hat ausdrücklich nicht normative SmartTube-Demo-Kacheln.
