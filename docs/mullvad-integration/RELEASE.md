# Plume × Mullvad — Release- und Pflegeanleitung

**M12 / 13.11.2026.** Hobbyprojekt-Testversion, reale Shield-2019-Abnahme offen.

## Herkunft, Identität und Lizenzen
- Ziel: https://github.com/Justify87/Plume_Launcher (Fork von https://github.com/aradhyas8/Plume_Launcher; `LICENSE` GPL-3.0). Original: https://github.com/mullvad/mullvadvpn-app.
- Stabile Mullvad-Quelle **`android/2026.12`**, **`aa47f606b1a159214c7279344128119fa91be5fd`**; Lizenzen/Fremdkomponenten gemäß gepinnten Original-Dateien erhalten. Keine offizielle Mullvad-App.
- **Eine** App-ID `dev.plume.advanced`: originale Mullvad-`MullvadApplication`, `MullvadVpnService`, Rust/JNI, gRPC/Daemon, Repositories/Use Cases. TV-Oberfläche als eigene native Android-Views-`VpnActivity` aus Plume Classic/Overview statt zweiter Smartphone-Compose-App.
- Integriert **Android 9/API 28+**, ABI **arm64-v8a** für Shield 2019. Plume-only-Minimum API 26 gilt ausdrücklich nicht für das integrierte Paket.

## Bauen und prüfen
```bash
git clone --recurse-submodules https://github.com/Justify87/Plume_Launcher.git
cd Plume_Launcher
test "$(git -C third_party/mullvadvpn-app rev-parse HEAD)" = aa47f606b1a159214c7279344128119fa91be5fd
sh tools/plume-install-test/run.sh
python3 -m unittest discover -s tools -p test_spotify_podcast_probe.py -v
python3 -m unittest discover -s tools -p test_mullvad_prepare.py -v
python3 -m unittest discover -s tools -p test_mullvad_apk.py -v
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
bash tools/mullvad/build-integrated.sh --no-configuration-cache --max-workers=2 :app:assembleOssProdRelease
```
- Plume-only: JDK17 und Android-SDK. Integrierter Build: gepinntes Mullvad-Android-Containerimage (Docker/Container-Runtime), Original-NDK/Rust und das Submodul; keine selbst geschriebene VPN-Runtime.
- APKs: `.build/plume-mullvad/mullvadvpn-app/android/app/build/outputs/apk/ossProd/{debug,release}/`. Beide über `tools/mullvad/check_apk.py`, `aapt dump badging/xmltree` sowie SHA256 prüfen.
- GitHub Actions → **Build Plume Launcher Advanced Debug APK** → `variant=integrated` liefert die **Debug-APK** sowie eine **unsignierte R8-Release-APK**. Beide Varianten haben dieselbe App-ID, sie müssen nicht zusammen installiert werden.
- Mullvads Original `android/app/build.gradle.kts` hat ausdrücklich `release.signingConfig = null`: ein `assembleRelease`-Pass ist **noch keine signierte/installierbare Release-Ausgabe**. Zum echten Release die APK außerhalb von Git mit dem dauerhaften Plume-Update-Schlüssel signieren und `apksigner verify` ausführen; keine Schlüssel/Passwörter committen.

## Installation / Erstlogin / Optionen
1. Auf Shield Android-Version/ABI und vorhandene Plume-Installation prüfen; Artifact-ZIP entpacken und Debug-APK-SHA256 mit `SHA256SUMS` vergleichen.
2. `adb install -r Plume-Launcher-Advanced-Mullvad-arm64-debug.apk` nur bei **gleichem Signierschlüssel**; GitHub-Actions-Debug-Schlüssel wechseln möglicherweise zwischen Runs. Vor einem nötigen Uninstall unbedingt Plume-Einstellungen sichern (App-Deinstallation entfernt App-Daten).
3. Plume über HOME/Leanback-Kachel öffnen. Classic **oder** Overview → sichtbarer **VPN**-Zugang. Die VPN-Activity ist mit `exported=false` absichtlich nicht extern per `adb am start` direkt startbar.
4. Im VPN-Reiter **Konto** Mullvad-Kontonummer eingeben, Konto-/Gerätestatus prüfen. Für die Verbindung den **Android-VPN-Zustimmungsdialog** akzeptieren; tatsächliche Exit-IP/Status prüfen. Kein separates Mullvad-APK nötig.
5. Standorte/Listen, DNS/Filter, LAN/IPv6/MTU, Boot-Autoconnect, Split-Tunneling, Multihop/DAITA/Post-Quantum, Verschleierung, API-Zugriff und Server-IP über native VPN-Unterseiten konfigurieren; Always-on/Lockdown gehört in Androids VPN-Systemeinstellungen.
6. Logs, Diagnose und Supportberichte nur auf ausdrückliche Aktion ausgeben. Quick-Settings, SAF-Dateipicker, Locale/API33 und System-Permissionen können firmwareabhängig fehlen.

## Release-Freigabe und Messung
- Die vollständige ADB-/D-Pad-/Konten-/Netzwerk-/Boot-/Standby-/Update-Matrix steht in `TEST_MATRIX.md`. Keine vollständige Parität oder Shield-Freigabe allein aus CI behaupten.
- M11- und M12-Roh-Debug-APK **in Bytes** vergleichen (nicht Artifact-ZIP-Größen); R8-Release separat. Auf **gleicher** Shield Launcher-Startzeit (`am start -W`), PSS/RSS (`dumpsys meminfo`) VPN aus und verbunden sowie UI-D-Pad-Verhalten messen.
- Ein signiertes Upgrade einer vorhandenen Plume-Installation muss dasselbe Zertifikat verwenden; Classic/Overview-Einstellungen und Account-/VPN-Verhalten nach `adb install -r` prüfen.

## Minimaler Upstream-Updateprozess
1. **Neuen stabilen** Mullvad-Android-Tag/SHA bewusst wählen, Pin/Gitlink und `UPSTREAM.md` dokumentiert ändern; nie ungefragt Beta oder `main`.
2. Diffs der Originalschnittstellen prüfen: `MullvadVpnService`, `MullvadApplication`, DI/Koin, Repositories/Use Cases, gRPC/JNI/Rust, Gradle/NDK/Min-SDK, Manifest, Relay-Assets, `ClassNames.kt`, Lizenzen.
3. Ausschließlich betroffene Staging-Adapter in `tools/mullvad/prepare.py` bzw. `Vpn*Ui.kt` anpassen, keine Geschäftsroutinen neu implementieren.
4. Sämtliche Plume-/Mullvad-Offline-Tests, Original-Debug- plus R8-Release-Build, APK-/Manifest-/ABI-/Signatur-/Größenchecks wiederholen.
5. Vollständige `TEST_MATRIX.md` auf physischer Shield 2019 einschließlich VPN, D-Pad, Boot, Appwechsel, Split/App-Routen, Konto und signiertem Plume-Update erneut durchführen. Nur belegte Fälle `pass`.

## Bekannte Grenzen
- Bis zur Shield-Abnahme **M12: partial**, Leistungsdaten `not_measured`, reale Netzfunktionen `hardware_pending`.
- Nur ARM64, keine Play-Billing-Flavour-Parität; Release derzeit unsigned; wechselnde CI-Debug-Signatur kann Daten-erhaltende Updates verhindern.
- Original-M06-Repository bietet keinen bestätigten Rückstellaufruf auf automatischen Singlehop-Exit. QS-Tile/Dateiauswahl/Android-Locale je nach TV-Firmware offen.

## M12 ABI-Packaging-Fix

Die tatsächlich extrahierte M11-Baseline enthielt AndroidX-`.so` für ARM32 und x86, aber `libmullvad_jni.so` ausschließlich für ARM64. Das konnte Androids Native-Code-Kompatibilitätsanzeige irreführen. Der neue regenerierte Host setzt `ndk.abiFilters` auf **arm64-v8a**; beide Buildvarianten dürfen nur diesen ABI-Eintrag ausgeben. Neue Nicht-ARM64-Ziele benötigen zuerst einen vollständigen Original-JNI-/Rust-Build und eigene Gerätereviews; **kein** bloßes Freischalten eines ABI-Labels.
