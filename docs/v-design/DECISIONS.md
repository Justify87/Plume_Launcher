# V-Design — Entscheidungen (ADR-light)

## D-001 — Ein Home-Design
**2026-11-13.** Nur das V-Design aus `REFERENCE.png`; Classic und alte Overview samt Modusumschalter nach M06 tatsächlich entfernen. Hintergrund: verbindlicher Nutzerauftrag. Keine unbegründete Entfernung von All Apps, HDMI, Wallpaper, Uhr-Sonderfunktionen, Setup, Mullvad.

## D-002 — Provider statt private Nuvio-API
**2026-11-13.** Die drei Nuvio-Preview-Channels anhand Channel-`internal_provider_id` und Laufzeitpaket lesen. Legacy bleibt in Nuvio eigenständiger Mischkanal. Keine Nuvio-Codeänderungen und kein zusätzliches IPC/Netzwerk. Kanalberechtigungen und ggf. fehlende Metadaten offen behandeln.

## D-003 — Bild als unveränderte Norm
**2026-11-13, am selben Tag verifiziert.** Das ZIP lieferte ein `REFERENCE.png` mit 1536×864 und SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`. Die identischen 249663 Bytes wurden später als `Plume_V_Design_REFERENCE_original.png` hochgeladen, Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18`. Die JPEG/JFIF-Signatur `FF D8 FF E0` erklärt, dass der ursprüngliche Dateiname eine irreführende `.png`-Endung trägt; **keine Neukodierung erlaubt**. Der Nachtrag legt denselben Git-Blob unter `docs/v-design/REFERENCE.png` ab und entfernt den Alias. Normbild ist Referenz, kein Live-Wallpaper und kein Testartefakt einer realen App-UI.

## D-004 — Normtext schlägt SmartTube-Mockupbeispiel
**2026-11-13.** Das Referenzbild selbst zeigt exemplarische SmartTube-Videokarten, obwohl die schriftliche Pflicht eindeutig „SmartTube **nur** als App-Verknüpfung in Sidebar, **nicht** als Content-Kachel“ lautet. Entscheidung: Bildgeometrie/-design normativ, Inhalte sind nicht normativ. Kein SmartTube-Datenimport und keine Fake-Nuvio-Kacheln; das ist eine **bewusst dokumentierte inhaltliche Abweichung**. Für M01/M04/M07 berücksichtigen.

## D-005 — Archivierte Mullvad-Docs nicht duplizieren
**2026-11-13.** Die zuletzt gemergte Archivierung `fbf2473` verschob `docs/mullvad-integration/*` nach `docs/archive/legacy-features/mullvad-vpn/*`. Historische M12-Abnahme bleibt als Archiv erhalten; neue V-Design-Dokumente referenzieren den echten Pfad. Submodul unberührt.

## D-006 — Synthetische Position ist keine Dauer
**2026-11-13.** Nuvio publiziert aus Fortschrittsprozent mitunter synthetische `100000ms` Duration; ohne zusätzliche Gewissheit darf Plume daraus **keine 100-s-Laufzeit oder Restminuten** zeigen. Prozent kann zulässig sein. M02 implementiert Testfälle.

## D-007 — M00-Gate nach verfrühtem Merge schließen
**2026-11-13.** [PR #32](https://github.com/Justify87/Plume_Launcher/pull/32) wurde tatsächlich gemergt, obwohl die Bilddatei darin fehlte. Ein separater, nicht gemergter `feature/vdesign-m00-reference-completion`-PR behebt ausschließlich Bildpfad und Dokumentations-Evidenz auf dem aktuellen `main`-HEAD; kein Reset und keine implizite Mergefreigabe. Der erste CI-Run des alten PRs endete `cancelled`, **der spätere finale Run 43507886182 war `success`**. Dennoch war das Referenz-Gate noch offen; also kein vollständiges M00-`passed` vor Ergänzung und deren CI. M01 bleibt bis zum nachgeholten M00-Abnahmecheck zurückgestellt.

## Weitere Entscheidungen
Fortlaufende ID, Datum, Problem, verworfene Alternativen, Beschluss, Folgen/Abweichung und Testbeleg eintragen. Änderungen am Design nicht stillschweigend vornehmen.


## D-008 — Exakte Channel-Provider-Identität, 2026-11-13 (M01)
Ein Kanal gilt nur dann als Nuvio-Kategorie, wenn `package_name` aus der geprüften expliziten Installationsliste stammt und `channel.internal_provider_id` exakt `package + suffix` ist. `program.internal_provider_id` ist eine andere, programmspezifische ID. Lokalisierte Namen sind ungeeignet, ebenso Substring-Erkennung ähnlich benannter Apps. Moderne reale Nuvio-Pakete siehe `DATA_CONTRACT.md`. Bestehende historische Aliase bleiben explizit zugelassen, ohne fremde Apps zu erkennen.

## D-009 — Opt-in/Legacy-Fallback und Nicht-Rendering, 2026-11-13 (M01)
Neue Vorschau-Feeds werden nur gelesen, wenn `browsable == true` aus dem TV-Provider geliefert wird. Fehlende Browsable-Spalte gilt nicht automatisch als genehmigt. Keine `requestChannelBrowsable`-Aufrufe aus Plume, kein Overlay und keine eigene Nuvio-DB-Abfrage. Legacy/Watch Next liefert ausschließlich bei positiver Progress-Evidenz einen **In-Progress**-Fallback bei fehlendem neuen Kanal. Available/Upcoming werden niemals aus Misch- oder Namensdaten rekonstruiert. `NuvioProviderFeeds` bleibt bis M02/M03 nicht gerendert, damit alte UI/Classic ohne neues Kachellayout erhalten bleibt. M02 muss Quelle/Sortierung/Dedupe/Release-Datum weiter verifizieren.
