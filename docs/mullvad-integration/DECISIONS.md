# Architekturentscheidungen — M01

**Status:** technische Zielarchitektur entschieden; Build-Machbarkeit muss in M02 nachgewiesen werden. Originalcode ist in M01 unangetastet.

## A01 — Vollständiger, gepinnter Git-Submoduleintrag (angenommen)

`third_party/mullvadvpn-app` zeigt direkt auf den peeled Commit von `android/2026.12`. Der komplette Repository-Root bleibt erhalten, da Cargo-Workspace, `mullvad-jni`, `mullvad-daemon`, `mullvad-management-interface`, Android Gradle Build-Logic und Relay-Assets relativ zueinander referenziert werden. Kein Kopieren einzelner `android/lib`-Verzeichnisse. `UPSTREAM.md` ist die Quelle der Wahrheit für den SHA. Vorteil: nachvollziehbarer unveränderter Original-Referenzbuild / geringe Drift. Kosten: Submodul-Checkout im Integrations-CI und umfangreiche Rust/Android-Toolchain.

## A02 — Einzige installierbare App, Mullvad als Build-Host (angenommen; M02-Kompilationsgate)

**Nicht:** zwei Android-`application`-Module zusammenpacken oder Mullvad-`:app` als AAR behandeln.

- **Einziger Host:** das ursprüngliche Mullvad-`android/app` bleibt das **einzige** gebaute `com.android.application`-Modul, weil es Rust/JNI, Module, Resources, Protobuf, Produkt-Flavors und Service-Manifest bereits korrekt zusammenführt.
- **Identität:** Der abgeleitete Host-Build überschreibt `applicationId` **nur** für die kombinierte APK auf `dev.plume.advanced`; ursprünglicher Package-/Namespace `net.mullvad.mullvadvpn` und JNI-Klassennamen bleiben bestehen. Plumes bestehende Package-Namen `dev.plume` bleiben unverändert. Es gibt nur **eine** `Application`-Klasse: Mullvads App-Bootstrap (oder sehr dünne Subklasse bei tatsächlichem Bedarf).
- **Plume als eingebundene TV-Library:** Original-Plume-Views/Kotlin/Ressourcen in einem neu definierten `:plume-ui` Android-Library-Modul mit `namespace=dev.plume` und **ohne** zweite Application-/Launcher-Instanz. Android-Manifeste in einem Plume-kontrollierten Merge-Overlay auf HOME/LEANBACK und Mullvads `MullvadVpnService` abstimmen. Benutzer-Einstieg ist Plumes bisherige `dev.plume.MainActivity`, nicht Mullvads Smartphone-Compose-`MainActivity`. Originalmodule dürfen Compose als transitive Build-Abhängigkeit behalten.
- **Quellendifferenz:** keine Dateien im Git-Submodul ändern. Ein Build-Harness unter Plume erzeugt in M02 einen **temporären abgeleiteten** Mullvad-Arbeitsbaum und appliziert schmale, versionierte Patches/Overlays auf Gradle-Host/Manifest/Bootstrap; originale Quelle bleibt für Referenzbuilds jederzeit intakt. Das ist **eine** APK, keine doppelte Installation. Die M02-Spike muss das Kompilieren von `:plume-ui`, Manifest-Merge und `assembleOssProdDebug` nachweisen, bevor Featurecode beginnt.
- **Launcher-Aliasse:** Zusätzlich zu Mullvads nicht exportierter `app/MainActivity.kt` registriert `android/lib/feature/appicon/impl/src/main/AndroidManifest.xml` eine **exportierte** `MainActivityAltDefault`-Alias sowie alternative Icon-Aliasse mit `LAUNCHER`/`LEANBACK_LAUNCHER`. Im kombinierten Manifest müssen die Mullvad-Launcher-Aliasse gezielt deaktiviert/entfernt werden, sonst erscheinen zusätzliche Mullvad-App-Einträge neben Plume. Original-Aliasse bei Referenzbuilds unverändert lassen; App-Icon-Feature-Parität auf TV separat prüfen.
- **Service:** `MullvadVpnService` mit `android.permission.BIND_VPN_SERVICE`/Foreground-Service und Mullvads bestehender `MullvadDaemon`/gRPC-Verbindung bleibt unabhängig von der sichtbaren Home-Activity. Keine neue WireGuard-/VPN-Implementierung. Konto/Status/Settings über Original-Repositories und Use-Cases statt parallelem Datenmodell.
- **UI-Grenze:** eigene Plume-TV-`VpnActivity` mit D-Pad/OK/Back und sichtbarem Launcher-Einstieg **für Classic und Overview**. Benutzerbezogene Mullvad-Compose-Screens werden nicht als UI verwendet, bleiben aber zunächst als Source/Dependencies erhalten, soweit Build/Runtimemodule sie erwarten. Kein größerer Herausrefaktor in M02.

**M02-Kompatibilitätsgate:** Sollten Host-Build plus `:plume-ui` mit minimalen Overlays nicht reproduzierbar bauen (z. B. Plugin-/R-Namespace-Konflikte), die **konkreten Fehlermeldungen** dokumentieren und eine alternative SourceSet-/Composite-Methode gegen die gleiche unveränderte Quelle belegen, bevor A02 revidiert wird. Keine freie Architektur-Rotation.

## A03 — Android-/Gradle-Versionen (angenommen)

- **minSdk 28** im integrierten Build (Mullvad-Original benötigt 28; Plume bisher 26): bewusster Ausschluss Android TV 8.0/8.1. Bestehenden Plume-Baseline-Build **vorerst mit minSdk 26** separat erhalten; erst der tatsächlich integrierte Host-Build setzt 28.
- Original **compileSdk / targetSdk 37**, AGP **9.3.1**, Kotlin **2.4.10**, JDK **21**, NDK **27.3.13750724**, Rust **1.97.1** als *Ausgangspunkt* des kombinierten Host-Builds. Original-Plume: AGP 9.2.1, compile/target 36, Java 17 CI. Keine simple Änderung von Plumes Gradle-Root auf Mullvads Pluginversion ohne Proof.
- API-37-Systemverhalten und Android-TV-Gerätekompatibilität sind **offen**. Plume-UI auch auf API 28/30/36 testen; Shield-Test bevor Freigabe. `applicationId`-Wechsel in Mullvad-Host bedeutet frische eigene App-Daten unter `dev.plume.advanced` und darf Plumes gespeicherte Einstellungen beim Update nicht löschen (Upgrade-Szenario auf Gerät prüfen).
- Native Targets Mullvad standardmäßig `arm,arm64,x86,x86_64`; für Endgerät Shield mindestens `arm64-v8a` / zutreffende Device-ABI nachweisen. Keine voreilige Target-Reduktion im M01.

## A04 — Regeln, CI und Größe (angenommen)

Plumes `CONTRIBUTING.md` widerspricht neuen Bibliotheken/Hintergrunddiensten sowie Berechtigungen; `tools/pr-rules.sh` blockiert solche PRs. Der VPN-Integrationsauftrag ist eine **bewusste projektbezogene Ausnahme**, nicht unbemerkt kompatibel mit den Regeln. In M01 **keine** Regeln aufgeweicht, da nur Docs/Submodul. M02 soll erlaubte Mullvad-spezifische Änderungen eng und prüfbar isolieren; vorhandene Prüfungen für normale Launcher-Änderungen weiter erzwingen.

Der ursprüngliche Plume-Prüfworkflow nutzt JDK 17 und kein Submodul. Integration benötigt eigenständiges JDK-21/NDK/Rust-CI und wahrscheinlich mehr Buildzeit. Ursprüngliche APK-Größe ~0.53 MB versus offizielles Mullvad-APK 39.0 MB; **erhebliche Größe und Laufzeitkomplexität gewollt** und nicht als regressionsfreie Nulländerung darstellen. OSS/Prod als Basis, kein Play-Billing/Google-Play-Flavour. Keine Download-Binaries in Git.

## A05 — Nicht-Ziele M01

Keine App-Runtime, keine VPN-Activity, kein Login, kein Runtime-Service-Start, kein Mock-Client; keine zusätzlichen Netzwerkanalysen, Protection-Audits oder Custom-Protokolle. Buildstatus in `TEST_MATRIX.md` ehrlich als `not_run` behandeln. Vor M02 keine Feature-Parität behaupten.
