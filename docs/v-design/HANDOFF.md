# V-Design — nächste ausführbare Übergabe

## M00, überprüfter Stand (13.11.2026)

Die ursprüngliche M00-Dokumentationsarbeit wurde mit [PR #32](https://github.com/Justify87/Plume_Launcher/pull/32) bereits in `main@78666326d97c5692eaa49f652da77c9a16e5433c` gemergt. **Der PR enthielt nicht die normative Referenzdatei.** Der Nachtrag beginnt daher korrekt auf diesem gemergten Commit und trägt den Branch `feature/vdesign-m00-reference-completion` (Image-Commit `e1c13b2783aa632c2cbf391b10eb923426f8fc81`, erster Dokumentationscommit `28a2acc8aefb5b1e9610a49ea12b62baf970bc35`, **[Draft-PR #33](https://github.com/Justify87/Plume_Launcher/pull/33)**).

Die ursprünglichen Bytes sind mittlerweile auf GitHub vorhanden gewesen, nur falsch benannt: `Plume_V_Design_REFERENCE_original.png`. Aus dem GitHub-Base64-Blob ausgelesen: 249663 Bytes, 1536×864, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`, Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18`. Das originale Datei-Encoding ist tatsächlich **JPEG/JFIF trotz `.png`-Dateiname**. Der Nachtrag ordnet **denselben Blob ohne Konvertierung** unter `docs/v-design/REFERENCE.png` zu und entfernt den Alias. Originalidentität bleibt erhalten.

Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a` und der alte `CONTINUE`-Kanal bleiben unverändert. Die letzte Mullvad-M12-Dokumentation liegt unter `docs/archive/legacy-features/mullvad-vpn/`, nicht mehr unter dem alten `docs/mullvad-integration/`-Pfad. Android-UI und VPN-Implementierung sind im M00 unangetastet.

## Erstes To-do für Folgeschritt

**Zunächst M00-Nachtrag abnehmen:** [PR #33](https://github.com/Justify87/Plume_Launcher/pull/33) und dessen *aktuelle* HEAD-SHA erfassen, `REFERENCE.png` erneut gegen `REFERENCE_SHA256.txt` und den Git-Blob vergleichen, echten CI-Run/Jobs prüfen, `STATE.md`, `REVIEWS.md`, `TEST_MATRIX.md` mit den finalen Ergebnissen nachziehen. PR **nicht** ohne Nutzerfreigabe mergen. Bis zur CI-/Review-Evidenz lautet M00 `partial_pending_pr_ci`.

**Exakt nächster Implementierungsmeilenstein: M01 — Nuvio-Kanalzuordnung.** Auf dem dann real gemergten M00-Stand (sonst auf dessen echten offenen PR-HEAD) beginnen; `TvContract.Channels.COLUMN_INTERNAL_PROVIDER_ID` mit den Suffixen `:in-progress`, `:available`, `:upcoming` verwenden. Nuvios tatsächlich installierte Package-IDs, Sichtbarkeit, Berechtigungen und Leerzustände prüfen. Den klassischen `CONTINUE`-Kanal nicht aufteilen, SmartTube nur als App in der Sidebar vorsehen. Original-Mullvad-JNI/VpnService/Gitlink beibehalten.

Lies vor M01: `STATE.md`, `ROADMAP.md`, `DESIGN_CONTRACT.md`, `DATA_CONTRACT.md`, `DECISIONS.md`, `REVIEWS.md`, `TEST_MATRIX.md`, `INVENTORY.md` und das **wirkliche** `REFERENCE.png`. Beachte die Folgefehler: synthetische Nuvio-100-s-Dauer, lokale Datumsgrenzen, bisherige Plume-Namensfilter, Altmodus-Prefs, D-Pad/HDMI/All Apps/Setup und Mullvad.
