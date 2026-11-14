# Manuelle Debug-APK (Plume Launcher Advanced)

Dieser Workflow baut **nur die kombinierte Plume+Mullvad ARM64 Debug-APK** zum
Testen auf der NVIDIA Shield TV Pro und anderen Android-TV-Geräten mit ARM64.
Er ersetzt nicht den vollständigen PR-Regressionstest und ist **kein Release**.

## Starten

1. Öffne [GitHub Actions](https://github.com/Justify87/Plume_Launcher/actions)
   und wähle **Build Plume Launcher Advanced Debug APK**.
2. Wähle **Run workflow**. Lass das Workflow-Dropdown auf **main**,
   damit immer der aktuelle Debug-Workflow ausgeführt wird. Gib im Feld
   **source_ref** die zu bauende Quellversion an: `main`, einen Feature-Branch
   oder einen Commit-SHA. Damit kannst du auch ältere Feature-Branches testen,
   ohne deren möglicherweise veraltete Workflow-Dateien auszuführen.
3. Nach grünem Build den Artefakt-Eintrag
   `Plume-Mullvad-ARM64-Debug-<run number>` herunterladen und entpacken.
   Die Datei für die Installation heißt
   `Plume-Launcher-Advanced-Mullvad-arm64-debug.apk`.

**Inhalt des Artefakts:** eine installierbare Debug-APK,
`SHA256SUMS`, `BUILD_INFO.txt`, Android-Paketinformationen und die
Signaturprüfung. Artefakte werden 14 Tage aufbewahrt. Der Workflow verwendet
`workflow_dispatch` und erscheint nach dem Merge auf `main` in GitHub Actions.

## Wodurch Debug und Release sich unterscheiden

| Manuelle Debug-APK | Reguläres GitHub-Release |
| --- | --- |
| Kompiliert ausschließlich `:app:assembleOssProdDebug` | Kompiliert zusätzlich den minimierten Release-Build |
| Mullvads festgepinnter Docker-Container und gepinnte Quellversion | Identische kompatible Buildgrundlage |
| Android-Debug-Signatur, **keine** Release-Secrets | Ein dauerhafter, vom Eigentümer verwalteter Release-Schlüssel |
| Dateien nur unter GitHub **Actions → Artifacts** | GitHub **Releases**, Tags und Release Notes |
| Belässt `versionName`, `versionCode` und `CHANGELOG.md` unverändert | Release-Prozess verwaltet Versionen |
| Keine Git-Pushes, Tags oder Veröffentlichungen | Separater manueller Release-Workflow |

### Installation und Update-Hinweis

Der Package-Name lautet `dev.plume.advanced`. Das ist **dieselbe App-ID**,
die auch der signierte Release-Workflow verwendet. Bei verschiedenen
Signaturzertifikaten erlaubt Android normalerweise kein Update über eine
vorhandene Installation. Daher eine schon anders signierte APK vorher
deinstallieren (zuvor Einstellungen sichern), oder einen passenden Debug-Key
verwenden. Der Standard-Debug-Key auf einem frischen GitHub-Runner ist
nicht dauerhaft identisch und wird **nicht** als Release-Key genutzt.

Der Workflow erzeugt nur ARM64. Ein APK-Build mit grüner CI garantiert
noch nicht, dass VPN-Tunnel, TV-Fokus und die Medienkanäle auf der echten Shield
funktionieren. Diese Funktionen müssen manuell getestet werden.

## CI-Aufteilung

- `.github/workflows/debug-apk.yml`: **manueller Debug-Build, eine APK**.
- `.github/workflows/pr.yml`: genau ein PR-Statuscheck.
- `.github/workflows/ci-integrated.yml`: dahinterliegende vollständige
  PR-Regressionssuite mit eigenständigen Unit-Tests, Lint, Mullvad-Container
  und Debug-/Release-Kompilierung.
- `.github/workflows/release.yml`: getrennte, manuelle Veröffentlichung
  einer mit dem persönlichen Keystore signierten APK.

Diese Trennung verhindert, dass Änderungen am manuellen Debug-Build
versehentlich die verpflichtenden PR-Regressionsprüfungen entfernen.
