# Plume Launcher Advanced — manuelle Releases

Dieses Hobby-Fork veröffentlicht **eine** kombinierte Plume+Mullvad ARM64-APK. Die Referenz
ist Mullvads `android/scripts/prepare-release.sh` (Version und Tag), `android/scripts/release/github-release`
(GitHub Release) und der gepinnte Container-Build. Mullvads eigener CDN, Release-Metadatenserver,
YubiKey-Signer, F-Droid-Pipeline und seine `android/20xx.xx`-Tags sind **nicht** Teil unseres Forks.

## Einmalige Signierung einrichten

Der vorhandene `.github/workflows/debug-apk.yml` liefert Debug-APKs und unsignierte
R8-Releases. Eine Debug-APK aus GitHub Actions ist **keine** dauerhaft updatefähige
Release-APK. Die neue `.github/workflows/release.yml` bricht ab, wenn kein dauerhafter
Release-Schlüssel eingerichtet wurde. Dies war die exakte Ursache des fehlgeschlagenen
ersten `v0.1.0`-Release-Laufs [#43644701229](https://github.com/Justify87/Plume_Launcher/actions/runs/43644701229):
**alle vier Signing-Secrets und die Zertifikats-Variable waren im Job leer.**
Die Veröffentlichung darf in diesem Fall nicht auf eine wechselnde Debug-Signatur
zurückfallen. Ein neuer Vorabtest benennt fehlende Einstellungen einzeln und
überprüft danach die Keystore-Datei, beide Passwörter, den Alias und das
erwartete Zertifikat **vor dem teuren Mullvad-Container-Build**.

Erzeuge einen neuen Schlüssel lokal und sichere die Datei und Passwörter **außerhalb
des Repositories** (Keystore und Kennwörter niemals committen oder im Chat versenden):

```bash
keytool -genkeypair -storetype PKCS12 -keystore plume-release.jks \
  -alias plume-release -keyalg RSA -keysize 3072 -validity 10000
keytool -list -v -keystore plume-release.jks -alias plume-release
```

Die Ausgabe enthält den **SHA256-Fingerabdruck des Zertifikats**. In GitHub
**Settings → Secrets and variables → Actions** anlegen:

| Art | Name | Inhalt |
| --- | --- | --- |
| Repository secret | `PLUME_RELEASE_KEYSTORE_BASE64` | base64-kodierte *gesamte* Keystore-Datei, eine Zeile |
| Repository secret | `PLUME_RELEASE_KEYSTORE_PASSWORD` | Keystore-Passwort |
| Repository secret | `PLUME_RELEASE_KEY_ALIAS` | Alias, z. B. `plume-release` |
| Repository secret | `PLUME_RELEASE_KEY_PASSWORD` | Passwort des Schlüssel-Eintrags |
| Repository variable | `PLUME_RELEASE_CERT_SHA256` | SHA256 des Zertifikats aus `keytool -list -v`; Doppelpunkte sind erlaubt |

**PKCS12-Hinweis:** Beim oben empfohlenen `-storetype PKCS12` müssen
`PLUME_RELEASE_KEYSTORE_PASSWORD` und `PLUME_RELEASE_KEY_PASSWORD`
denselben Wert enthalten. Java-`keytool` ignoriert bei PKCS12 teils
ein separat angegebenes falsches Key-Passwort. Der neue Vorabtest
erkennt diese Konstellation explizit. Bei bestehenden JKS-Dateien
können die beiden Passwörter unterschiedlich sein.

Unter Linux: `base64 -w0 plume-release.jks`. Unter macOS:
`base64 < plume-release.jks | tr -d '\n'`. Den Wert nur im GitHub-Secrets-Dialog eintragen.

**Einrichtung für deinen ersten Release (einmalig):**

1. Öffne [Settings → Actions Secrets](https://github.com/Justify87/Plume_Launcher/settings/secrets/actions).
   Trage die vier obigen Namen jeweils unter **Repository secrets** ein.
2. Unter **Variables** am gleichen Ort `PLUME_RELEASE_CERT_SHA256` anlegen.
   Den Fingerabdruck aus `keytool -list -v` verwenden; der Fingerabdruck
   ist öffentlich, der Keystore und die Passwörter sind geheim.
3. **Kein** geheimer Wert gehört in einen Issue, PR, Chat oder Git-Commit.
   Der Keystore muss für alle zukünftigen Updates sicher aufbewahrt werden.
4. Erst dann den Release-Workflow von `main` mit `version=0.1.0`
   starten; beim ersten Test empfiehlt sich `publish=false`, damit ein
   privater **Draft** entsteht, den du vor der Veröffentlichung prüfen kannst.

Der Workflow führt vor dem Build `keytool -exportcert` und `keytool -certreq`
aus und vergleicht den Fingerabdruck mit der GitHub-Variable. Es wird dabei
kein privater Schlüssel ausgegeben oder gespeichert. Der spätere Schritt
`apksigner verify --print-certs` vergleicht die Signatur der fertigen APK
ebenfalls mit demselben Fingerabdruck.

**Wichtig:** Falls bereits eine mit einem anderen Release-Schlüssel signierte
`dev.plume.advanced`-APK installiert wurde, ist ein Daten-erhaltendes Update
nur mit demselben Schlüssel möglich. Ein neu erzeugter Schlüssel ersetzt den
alten nicht. Debug-APKs aus unterschiedlichen GitHub-Runnern können ebenfalls
unterschiedliche Signaturen besitzen.

## Neues Release manuell starten

1. **Erstes Advanced-Release:** Die Version `0.1.0` ist bereits im Sourcecode
   und im überarbeiteten `CHANGELOG.md` vorbereitet. Die Release-Notizen im
   Abschnitt `## 0.1.0` vor der Freigabe prüfen. Frühere Plume-Notizen sind
   unter `docs/archive/legacy-features/pre-advanced-changelog.md` archiviert.
   **Spätere Releases:** Änderungen unter `## Unreleased` dokumentieren;
   der Release-Assistent verschiebt sie in den neuen Versionsabschnitt.
2. GitHub → **Actions → Release Plume Launcher Advanced → Run workflow**.
   Branch: **main**. Für das erste Release Version `0.1.0` (ohne `v`).
3. Häkchen bei `reviewed_changelog` setzen. `publish=false` belässt das
   fertige GitHub Release zunächst als **Draft**; `publish=true` macht es
   bei erfolgreichem Build öffentlich.
4. Die Actions-Ausführung kontrollieren. Das Release enthält die **signierte**
   `Plume-Launcher-Advanced.apk`, `SHA256SUMS` und den
   `SIGNER_SHA256.txt`-Zertifikatsfingerabdruck.

**Eigenständiger Versionsbeginn:** `versionName=0.1.0` und
`versionCode=11`. Die sichtbare SemVer-Version wurde bewusst zurückgesetzt.
Der technische Android-`versionCode` bleibt monoton über dem alten Wert 10,
um ein Downgrade bei gleicher App-ID zu vermeiden. Der erste Release-Workflow
`version=0.1.0` nutzt diesen bereits vorbereiteten Stand und erhöht die
Version **nicht nochmals**. Der nächste `0.1.1`-Release erhält `versionCode=12`
(bei vorbereitetem Unreleased-Changelog). `-beta1` und `-rc1`
sind erlaubt und werden als GitHub Pre-release eingeordnet. GitHubs
`releases/latest` zeigt nur stabile Veröffentlichungen.

**Update-Hinweis für bestehende Installationen:** Der im Fork übernommene
Updater vergleicht sichtbare Versionen. Eine vorhandene `1.7.1` wird daher
`0.1.0` nicht als neu erkennen. Außerdem muss eine Update-APK mit demselben
Zertifikat signiert sein. Vor einem Umstieg Einstellungen sichern und bei
abweichenden Signaturen eine Neuinstallation einplanen. Nach `0.1.0` sind
normale aufsteigende Updates (`0.1.1`, `0.2.0`, ...) vorgesehen.

## Versionsnummern, Tags und Sicherheit

- **Tag:** `v<version>`, beginnend mit `v0.1.0`. Der Release-Assistent erzeugt
  den Tag über GitHub erst **nach** erfolgreichen Unit-Tests, Container-Build,
  Manifest-/APK- und Signaturprüfungen. Bereits vorhandene Tags oder Releases
  werden abgewiesen. GitHub erzeugt hierbei einen Tag am Release-Commit,
  **keinen GPG-signierten Mullvad-Tag**.
- **Versionsquellcode:** Ein neuer Release-Commit übernimmt die freigegebenen
  Unreleased-Notizen in `## <version>`, setzt `## Unreleased` leer und
  hebt `versionName`/`versionCode` an. Wenn `main` unverändert ist,
  kann der Workflow diesen Commit selbst pushen. Er überschreibt niemals
  einen inzwischen geänderten `main`-HEAD.
- **Geschützter main-Branch:** Wenn eine Branch-Protection direkte Bot-Pushes
  blockiert, Version vorher in einem normalen PR vorbereiten. Der Release-Workflow
  erkennt diesen Fall nun bereits vor dem Mullvad-Build und gibt einen
  handlungsfähigen Fehler aus:
  `python3 tools/release/prepare.py --version 0.1.1` auf einem
  Feature-Branch ausführen, die Änderungen an `CHANGELOG.md` und
  `app/build.gradle.kts` prüfen, dann über PR + CI mergen.
  Danach den Release-Workflow mit **derselben** Version starten. Er erkennt
  vorbereitete Versionen und erstellt keinen neuen Commit.
- **Vor einem öffentlichen Release:** die signierte APK und Updatefähigkeit
  mit der bereits installierten Version auf der Shield testen. CI ersetzt
  keinen VPN-Verbindungs-, D-Pad- oder Boot-Test auf echter Hardware.
- **Wiederanlauf:** Bei einem Fehler *nach* einem erfolgreichen Versions-Push,
  aber *vor* der GitHub-Release-Erstellung kann der Workflow mit derselben
  Version erneut gestartet werden. Ein bereits existierender Tag wird
  nicht automatisch gelöscht oder überschrieben.

Der eingebaute Launcher-Updater erwartet genau
`https://github.com/Justify87/Plume_Launcher/releases/download/v<version>/Plume-Launcher-Advanced.apk`.
Deshalb **nicht** den Tag oder den APK-Dateinamen verändern. Ein Draft erscheint
noch nicht als Update über GitHubs `releases/latest`.

## Abweichung vom Mullvad-Original

Mullvad nutzt das Versionsformat `android/2026.12`, separat berechnete
Android-VersionCodes, signierte Git-Tags, einen YubiKey und einen eigenen
Downloadserver mit signierten Artefakten. Dieser persönliche Fork übernimmt
die wichtigen Prinzipien (getrennte Release-Identität, feste Toolchain,
eindeutige Version, Signatur- und Artefaktprüfung), verwendet aber eine
einfachere GitHub-Releases-Pipeline passend zum vorhandenen Plume-Updater.
