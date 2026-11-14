# Plume Launcher Advanced — manuelle Releases

Dieses Hobby-Fork veröffentlicht **eine** kombinierte Plume+Mullvad ARM64-APK. Die Referenz
ist Mullvads `android/scripts/prepare-release.sh` (Version und Tag), `android/scripts/release/github-release`
(GitHub Release) und der gepinnte Container-Build. Mullvads eigener CDN, Release-Metadatenserver,
YubiKey-Signer, F-Droid-Pipeline und seine `android/20xx.xx`-Tags sind **nicht** Teil unseres Forks.

## Einmalige Signierung einrichten

Der vorhandene `.github/workflows/debug-apk.yml` liefert Debug-APKs und unsignierte
R8-Releases. Eine Debug-APK aus GitHub Actions ist **keine** dauerhaft updatefähige
Release-APK. Die neue `.github/workflows/release.yml` bricht ab, wenn kein dauerhafter
Release-Schlüssel eingerichtet wurde.

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

Unter Linux: `base64 -w0 plume-release.jks`. Unter macOS:
`base64 < plume-release.jks | tr -d '\n'`. Den Wert nur im GitHub-Secrets-Dialog eintragen.

**Wichtig:** Falls bereits eine mit einem anderen Release-Schlüssel signierte
`dev.plume.advanced`-APK installiert wurde, ist ein Daten-erhaltendes Update
nur mit demselben Schlüssel möglich. Ein neu erzeugter Schlüssel ersetzt den
alten nicht. Debug-APKs aus unterschiedlichen GitHub-Runnern können ebenfalls
unterschiedliche Signaturen besitzen.

## Neues Release manuell starten

1. Die Einträge unter `## Unreleased` in `CHANGELOG.md` **inhaltlich prüfen**.
   Der derzeitige Bestand enthält auch alte Classic-/Overview-Beschreibungen; vor dem
   ersten Release korrigieren. Der Workflow übernimmt diese Einträge 1:1 als
   Notes und leert danach die Unreleased-Sektion.
2. GitHub → **Actions → Release Plume Launcher Advanced → Run workflow**.
   Branch: **main**. Version etwa `1.8.0` (ohne `v`).
3. Häkchen bei `reviewed_changelog` setzen. `publish=false` belässt das
   fertige GitHub Release zunächst als **Draft**; `publish=true` macht es
   bei erfolgreichem Build öffentlich.
4. Die Actions-Ausführung kontrollieren. Das Release enthält die **signierte**
   `Plume-Launcher-Advanced.apk`, `SHA256SUMS` und den
   `SIGNER_SHA256.txt`-Zertifikatsfingerabdruck.

Die Android-APK-Version in `app/build.gradle.kts` beginnt derzeit bei
`versionName=1.7.1`, `versionCode=10`. Für `1.8.0` wird daraus
`versionName=1.8.0`, `versionCode=11`. Danach erhöht jede weitere
Vorbereitung den `versionCode` um eins. Auch `-beta1` und `-rc1`
sind erlaubt und werden als GitHub Pre-release eingeordnet. GitHubs
`releases/latest` bleibt auf stabile Veröffentlichungen beschränkt.

## Versionsnummern, Tags und Sicherheit

- **Tag:** `v<version>`, z. B. `v1.8.0`. Der Release-Assistent erzeugt
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
  blockiert, Version vorher in einem normalen PR vorbereiten:
  `python3 tools/release/prepare.py --version 1.8.0` auf einem
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
