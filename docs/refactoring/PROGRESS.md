# Refactoring-Fortschritt – Plume Launcher Advanced

**Arbeitsgrenze:** M0 allein, kein M1 ohne erfolgreiches Gate und anschließenden Merge. **Originalbasis:** `main` @ `e3c1a3eb78c85bb471d9012dd700b6efd81b44ce` (16.11.2026), Git-Tree 323 reguläre Dateien plus externes Mullvad-Submodul. **Branch:** `refactor/m0-inventory-androidtest-20261116`. **Letzter vor diesem Status geprüfter Branch-Commit:** `97b6bec075b72a3e163302bc2430ccfe828dff6f`. Der aktuelle HEAD ist über den Branch-Ref bzw. den PR abzurufen; eine Datei kann nicht ihren eigenen finalen Commit-SHA enthalten.

## M0 – Inventar / Baseline / AndroidTest

**Entscheidung:** **PARTIAL** – überprüfbare reale Änderungen und 100 % Git-Tree-/Datei-Task-Zuordnung, jedoch *kein* vollständig abgeschlossener fachlicher Review jeder Datei und noch keine bestätigten Pflicht-Gates.

### Bereits nachgewiesen
- `main` = historischer Audit-SHA `e3c1a3eb78c85bb471d9012dd700b6efd81b44ce`, bei Branch-Erstellung keine offenen PRs.
- **123 individuelle Code-/Konfigurationsdatei-Tasks** F001–F123, **200 Nicht-Code-Dateien**, ein externer Gitlink; Task-Pfade aus rekursivem, nicht abgeschnittenem Git-Tree abgeleitet (mechanisch 0 fehlende/0 doppelte Tasks).
- Für alle 123 Code-/Konfigurationsdateien wurde der **komplette GitHub-Quellinhalt** geholt, der Blob-SHA gegen den aktuellen Branch verglichen und ein struktureller Textscan ausgeführt. **Die fachliche End-to-End-Lektüre/Prüfung von Verhalten, Aufrufern, Thread- und Lifecycle-Besitz ist noch offen**: `FULL_FETCH / SEMANTIC_OPEN` statt unzutreffendem `FULL_REVIEW`. Tabelle siehe `FILE_TASKS.md`.
- `ARCHITECTURE_MAP.md` mit A01–A08, beobachteten Haupt-Aufrufketten und vorsichtig eingegrenzten Move-Verträgen; M1 muss offene Vertragsdetails vor Extraktion schließen.
- **Teständerung:** `app/src/androidTest/java/dev/plume/AppPicturesCheck.kt` referenzierte nicht mehr existierendes `R.id.appRow`, `renderApps()` und die frühere separate Banner-Menüzeile. Anpassung an `R.id.allAppsGrid`, `openAllApps()`, `renderAllAppsPane(Boolean)`, aktuelle Icon-/Reset-Menüpunkte. Banner-/Icon-Fit und der vorhandene Upload-Server-Test bleiben erhalten; keine Produktcodeänderung.
- **CI-Änderung:** `.github/workflows/ci-integrated.yml` ergänzt `compileDebugAndroidTestKotlin` zum bestehenden `./gradlew testDebugUnitTest lintDebug assembleDebug`; kein bestehender Test deaktiviert.

### Befund, Zuordnung, Invarianz
- **F009 / A03 / M0:** AndroidTest-Altpfad. Beobachtbare Invariante: All Apps bleibt rechte Workspace-Pane, Upload/Reset, Icon-Modus, Paketidentität und PIN/Focus/Server-Cleanup unverändert; nur die Instrumentierungsansteuerung folgt dem bereits bestehenden Code.
- **F001 / A07 / M0:** Die PR-CI baute bisher nur Launcher-Unit/Lint/Debug und integrierten Mullvad-Host. AndroidTest-Kotlin wurde nicht mitkompiliert; neue Prüfung deckt veraltete Compile-Referenzen auf.
- **F026 / A01–A04 / M1–M5:** `MainActivity` mit Mehrfachverantwortung bleibt unangetastet bis zum Ownership-Gate. **Kein** nicht begründeter Big-Bang-Umbau.
- **F099/F101/F111 / A05 / M6:** Mullvad-Originalservice, getrennte Host-Lifecycles, Staging und gepinnter Gitlink bleiben unverändert.

### Tatsächliche Prüfungen / Beweisstatus
| Prüfung / Befehl | Commitbezug / Resultat |
|---|---|
| GitHub Branch-Read + rekursiver `git/trees/{sha}?recursive=1` | `e3c1a3eb78c85bb471d9012dd700b6efd81b44ce`, 323 reguläre Dateien, nicht abgeschnitten, Gitlink geprüft → **PASS** |
| GitHub `fetch_file` für 123 Code-/Konfigurationsdateien + Content-SHA-Vergleich | M0-Branch, 123/123 komplett abgerufen → **PASS (technischer Abruf; kein semantischer Review)** |
| Mechanischer Task- ↔ Git-Tree-Abgleich | M0-Branch-Baum vor Dokumentation, 123/123 Code-Task-Pfade, 0 doppelte/fehlende → **PASS** |
| `./gradlew testDebugUnitTest lintDebug assembleDebug compileDebugAndroidTestKotlin` | **NOT_RUN lokal**: kein Repository-Checkout/Android SDK in der aktiven Umgebung; PR-CI muss auf finalem HEAD laufen |
| `sh tools/plume-install-test/run.sh` | **NOT_RUN lokal**, Nachweis über PR-CI offen |
| `python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v` (und weitere bestehende Mullvad-/Spotify-Tests) | **NOT_RUN lokal**, PR-CI offen |
| `bash tools/mullvad/build-integrated.sh … :app:assembleOssProdRelease` / R8 / ARM64 / APK-Validierung | **NOT_RUN lokal**, bestehende PR-CI offen |
| TV-Emulator, reale Shield Pro, VPN-Tunnel | **NOT_TESTED** |

**CI-Nachweis:** bisherige grüne Debug-Läufe vom 15.11.2026 gelten ausdrücklich **nicht** als Beweis für diesen M0-Branch. Neuer M0-PR und aktuelle CI-URL nach PR-Erstellung nachtragen.

### Kritischer eigener Zweitreview (nicht extern unabhängig)
- Scope: ausschließlich Test-Fix, Erweiterung des bestehenden AndroidTest-Compile-Gates und Audit-Markdown; keine Produktions-/UI-/VPN-/Signing-/Dependency-Änderungen.
- Alte Banner-UI-Zeile existiert nicht mehr, aber der unveränderte Bild-Konvertierungs-/Upload-Test verarbeitet Banner weiterhin; nicht durch ein fiktives neues UI-Feature ersetzen.
- Aufrufer-, State- und Test-Semantik nicht durch Regex-Scans als geprüft bezeichnen. Das bleibt der explizite M0-Blocker.
- Nach Erhalt der PR-CI muss der **eigene Diff** erneut gegen Testpfad, Versions-/Signing-/Submodul-Invarianz und allen gemeldeten Fehlern gegengeprüft werden.

### Offene Punkte / Resume
1. PR für den M0-Branch eröffnen und CI auf dessen echtem HEAD prüfen; bei Fehlern denselben PR reparieren.
2. Pro Task fachliche Durchsicht samt echten Aufrufern/Thread-Grenzen nachtragen. Auch `KEEP` erst mit begründeter Prüfung schließen.
3. Fehlende Pflicht-Tests nachweislich ausführen. Nur bei allen M0-Gates `PASS`; andernfalls `PARTIAL` oder `BLOCKED`.
4. **M1 nicht starten**, solange M0 nicht bestanden und der M0-PR nicht durch den Maintainer gemergt ist.

## Status M1–M7
**NOT_STARTED** (Abhängigkeitsgates gesperrt). Kein neuer Feature-/Security-Workstream.
