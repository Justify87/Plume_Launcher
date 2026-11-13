# V-Design — Entscheidungen (ADR-light)

## D-001 — Ein Home-Design
**2026-11-13.** Nur das V-Design aus `REFERENCE.png`; Classic und alte Overview samt Modusumschalter nach M06 tatsächlich entfernen. Hintergrund: verbindlicher Nutzerauftrag. Keine unbegründete Entfernung von All Apps, HDMI, Wallpaper, Uhr-Sonderfunktionen, Setup, Mullvad.

## D-002 — Provider statt private Nuvio-API
**2026-11-13.** Die drei Nuvio-Preview-Channels anhand Channel-`internal_provider_id` und Laufzeitpaket lesen. Legacy bleibt in Nuvio eigenständiger Mischkanal. Keine Nuvio-Codeänderungen und kein zusätzliches IPC/Netzwerk. Kanalberechtigungen und ggf. fehlende Metadaten offen behandeln.

## D-003 — Bild als unveränderte Norm
**2026-11-13.** Das ZIP enthält `REFERENCE.png` 1536×864, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`. Weder neu generieren noch als UI-Hintergrund verwenden. Die GitHub-Schnittstelle dieser Sitzung kann die lokal verfügbare Binärdatei nicht übertragen. M00 nur **partial**, bis exakt diese Datei hinzugefügt und SHA am Commit nachgeprüft ist.

## D-004 — Normtext schlägt SmartTube-Mockupbeispiel
**2026-11-13.** Das Referenzbild selbst zeigt exemplarische SmartTube-Videokarten, obwohl die schriftliche Pflicht eindeutig „SmartTube **nur** als App-Verknüpfung in Sidebar, **nicht** als Content-Kachel“ lautet. Entscheidung: Bildgeometrie/-design normativ, Inhalte sind nicht normativ. Kein SmartTube-Datenimport und keine Fake-Nuvio-Kacheln; das ist eine **bewusst dokumentierte inhaltliche Abweichung**. Für M01/M04/M07 berücksichtigen.

## D-005 — Archivierte Mullvad-Docs nicht duplizieren
**2026-11-13.** Die zuletzt gemergte Archivierung `fbf2473` verschob `docs/mullvad-integration/*` nach `docs/archive/legacy-features/mullvad-vpn/*`. Historische M12-Abnahme bleibt als Archiv erhalten; neue V-Design-Dokumente referenzieren den echten Pfad. Submodul unberührt.

## D-006 — Synthetische Position ist keine Dauer
**2026-11-13.** Nuvio publiziert aus Fortschrittsprozent mitunter synthetische `100000ms` Duration; ohne zusätzliche Gewissheit darf Plume daraus **keine 100-s-Laufzeit oder Restminuten** zeigen. Prozent kann zulässig sein. M02 implementiert Testfälle.

## Weitere Entscheidungen
Fortlaufende ID, Datum, Problem, verworfene Alternativen, Beschluss, Folgen/Abweichung und Testbeleg eintragen. Änderungen am Design nicht stillschweigend vornehmen.
