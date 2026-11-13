# M04 – Shield TV Pro 2019: Overview-UX-Abnahme

**Stand:** 10.11.2026. **Status: Geräteabnahme offen, keine Freigabe für Phase B.**

- Repository: `Justify87/Plume_Launcher`; Basis-`main` **`8768d224191356d8995622f1297a7f1b99acb0bd`** (M01–M03 und Advanced-Branding gemergt).
- Version am Basiscommit: **`1.7.1`**, `versionCode=10`, `applicationId=dev.plume.advanced` (`app/build.gradle.kts`).
- Verifizierter Basis-Build: [GitHub Actions #42899289988](https://github.com/Justify87/Plume_Launcher/actions/runs/42899289988), `workflow_dispatch`, Commit `8768d224`, **success**. Job führte `./gradlew testDebugUnitTest lintDebug assembleDebug` aus; APK-Badging und Upload erfolgreich.
- Artefakt: **`Plume-Launcher-Advanced-Debug-APK-2`**, enthält `Plume-Launcher-Advanced-debug.apk` und `SHA256SUMS`. Zu finden bei **Actions → Run #42899289988 → Artifacts** (Ablauf laut GitHub: 24.11.2026).
- Die M04-Änderungen **sind nicht** in diesem Basis-Build enthalten. Zur Abnahme **nach M04** den Debug-Workflow manuell auf dem M04-Feature-Branch ausführen und **dessen** Commit, APK-Prüfsumme und Run-Link eintragen. PR-CI baut `assembleRelease`, nicht `assembleDebug`.

## Nachweisgrenzen

**Bisher statisch geprüft:** `MainActivity.kt` (`dispatchKeyEvent`, `overviewNav`, `setupHomeSidebar`, `renderHomeSidebar`, `updateOverviewPreview`, Home/Back), `OverviewHomeView.kt`, `HomeSidebarView.kt`, `HomeFocusNavigator.kt`, `OverviewRowsNavigator.kt`, `DemoMedia.kt`, `activity_main.xml`, `AndroidManifest.xml`, `app/build.gradle.kts`, `docs/home-overview-baseline.md`, `CONTRIBUTING.md`, `tools/pr-rules.sh` und die beiden CI-Workflows.

**Nicht erfolgt:** Bedienung einer echten Shield; Emulatorstart; visuelle Screenshot-/Video-Bewertung; tatsächliche Fokus- oder Speicher-/Latenzmessung. „Statisch“ oder „Unit-Test“ **ersetzt keinen Gerätetest**.

**M04-Codekorrektur:** Beim Neuaufbau der Sidebar wird die Vorschau bereits aktualisiert. Wenn anschließend dieselbe Sidebar-Zeile wieder Fokus erhält, darf das Fokus-Callback die unveränderten Demoreihen nicht nochmals neu erstellen. `MainActivity.kt` aktualisiert die Vorschau deshalb bei diesem Callback nur nach einem **tatsächlichen Wechsel** der Auswahl. Damit entfällt unnötige Arbeit beim Wiederherstellen des Fokus; ein messbarer Geschwindigkeitsgewinn ist **nicht behauptet**. Der Classic-Pfad bleibt unberührt. `OverviewUxRegressionTest` prüft zusätzliche reine Navigations-/Daten-Grenzfälle.

## Vorbereitung auf Shield (Schritte in dieser Reihenfolge)

1. Unter **Settings → Device Preferences → About** Modell und Android-Version dokumentieren; Shield TV Pro **2019** vorausgesetzt. Installierte App-Version und Build-Commit protokollieren. Bestehende Launcher-/Wallpaper-Einstellungen möglichst vorab sichern. Die Fork hat eine **andere App-ID** als das Original; bei Updates derselben Fork muss die Signatur weiterhin passen.
2. Auf GitHub **Actions → Build Plume Launcher Advanced Debug APK → Run workflow** öffnen, für eine M04-Prüfung den Branch **`ux/m04-shield-prototype-validation`** auswählen. Erst wenn der Workflow **success** meldet, unter **Artifacts** das ZIP laden, entpacken und `SHA256SUMS` lokal mit der APK abgleichen.
3. ADB mit der Shield verbinden (oder APK per USB übertragen). Installieren: `adb install -r Plume-Launcher-Advanced-debug.apk`. Gibt Android einen **Signaturkonflikt** mit einer früheren Advanced-Installation aus, **nicht blind deinstallieren**: zuerst Einstellungen sichern. Installationsfehler vollständig protokollieren.
4. Launcher öffnen, z. B. `adb shell am start -n dev.plume.advanced/dev.plume.MainActivity`. In **Plume settings → Style → Home layout** zwischen **Classic** und **Overview** wechseln. Für die folgenden Fälle zuerst **Overview** wählen. Der erste Start bestehender Nutzer muss weiter **Classic** sein.
5. Pro Prüfung **bestanden / fehlgeschlagen / nicht testbar**, **Datum**, **Gerät/Android-Version**, **Git-Commit**, **Wallpaper/Theme/Glass**, und bei Fehler **Startfokus → Tastenfolge → beobachteter Fokus** notieren. Bei Fokusfehler ein kurzes Handyvideo oder `adb shell input keyevent`-Reproduktionsskript hinzufügen. Ein Screenshot allein belegt keine D-Pad-Navigation.

## Manuelle Abnahmematrix

**Legende:** Alle Felder **OFFEN** bedeuten ausdrücklich **nicht durchgeführt**. Bestehen erfordert die beobachteten Erwartungen auf dem Gerät, nicht nur einen erfolgreichen Build.

| ID | Aufgabe auf der Shield-Fernbedienung | Erwartetes Ergebnis | Status |
|---|---|---|---|
| U01 | Erster Start mit **nicht gesetzter** `home_layout_mode`-Einstellung; dann Overview aktivieren | Classic als Standard, Overview bewusst umschaltbar; Modus bleibt nach Neustart erhalten | OFFEN |
| U02 | Von Top-Bar **Runter** zur Sidebar, **Hoch** zurück, Top-Bar **Links/Rechts** bis an Enden | Immer sichtbarer Fokus; kein Wegspringen, keine Top-Bar-Falle | OFFEN |
| U03 | Sidebar: **Overview → erste App → letzte App → Alle Apps** per Hoch/Runter; auch zurück | Genau eine erkennbare Auswahl, lange Liste scrollt, am unteren Ende kein Fokusverlust | OFFEN |
| U04 | Sidebar-Eintrag **Overview**, dann **Rechts**, Medienreihe durch **Rechts/Links** bis zum Rand | Erste Demo-Karte erreichbar, Karten vollständig sichtbar, Links am ersten Cover führt zur Sidebar | OFFEN |
| U05 | Medienkarte **Runter** zur zweiten Reihe, **Hoch** zurück; zwischen verschiedenen Spalten wechseln | Kein Sprung in nicht vorhandene Karte; zuletzt ausgewählte Spalte wird sinnvoll wiederhergestellt | OFFEN |
| U06 | In Sidebar eine **App** nur fokussieren; **nicht OK drücken** | Vorschau aktualisiert sich, aber App startet **nicht** durch Fokuswechsel | OFFEN |
| U07 | Sidebar-App **Rechts → Open app → Runter → Demo-Karte → OK** | Die Demo-Karte meldet klar, dass sie nicht abspielbar ist; kein falscher Deep Link / App-Start | OFFEN |
| U08 | Sidebar-App **OK**, alternativ **Open app → OK**; danach **Back** bzw. **HOME** | Nur bewusster OK-Druck startet die gewählte App; Rückkehr ohne Fokusverlust | OFFEN |
| U09 | **Langes OK** auf App-Eintrag, Medienkarte und Open-app-Aktion testen | Keine versehentliche App-Öffnung oder doppelte Aktivierung durch OK-Autorepeat | OFFEN |
| U10 | In App-Vorschau **Back**, anschließend Alle Apps und Einstellungen öffnen/schließen | Back zur Übersicht; Overlays blockieren Home-Fokus, danach klar sichtbarer Fokus | OFFEN |
| U11 | Themes **hell/dunkel**, Glass **Off/Frosted/Clear**, mehrere **helle/dunkle/Video-Wallpaper** | Texte, Fokusrahmen, Fortschritt und Demo-Tag klar lesbar; Glass und Hintergrund korrekt | OFFEN |
| U12 | Sidebar mit vielen Apps (z. B. 40+), danach Apps ausblenden bis keine App in der Reihe bleibt | Kein festhängender Fokus; Overview und Alle Apps bleiben erreichbar; keine ungültige Auswahl | OFFEN |
| U13 | Lange App-Namen, lange Demo-Titel, abweichende Schriftgröße/Skalierung | Kein Abschneiden der Bedienelemente; Texte mit Ellipse bzw. sinnvollem Umbruch | OFFEN |
| U14 | Unbekannte App ohne Demo; keine Preview-Daten / keine Watch-Next-Berechtigung | Klare leere App-Vorschau; kein Kartenphantom, kein Absturz; Classic bleibt robust | OFFEN |
| U15 | Direkt nach Start **HOME**, App öffnen/HOME zurück, Hintergrund/Wallpaper wechseln | Kein sichtbares Hängenbleiben; Fokus und Scrollposition weiterhin sinnvoll | OFFEN |
| U16 | **Classic**: Top-Bar/App/Watch Next, Rechts-Wrap auf letzter App, App-Start und Back/Home | Ursprüngliches M00-Verhalten bleibt erhalten, keine neue Demo im echten Watch Next | OFFEN |
| U17 | Classic: App-Liste sortieren/ausblenden, Watch Next leer, langes OK, Einstellungen/Theme | Alte Einstellungen, Focus-/Overlay-Regeln und App-Caches funktionieren | OFFEN |
| U18 | Neustart vor Benutzerentsperrung, dann entsperren (falls testbar) | Kein Absturz durch Direct Boot; Launcher baut Views erst nach Unlock auf | OFFEN |
| U19 | Bildschirm-/UI-Skalierung wie unten beschrieben prüfen (1080p, 4K, ggf. Emulator) | Keine festen Mockup-Pixelannahmen, keine offscreen-Buttons oder unzugänglichen Inhalte | OFFEN |
| U20 | Reaktionszeit, View-Zahl und Speicher jeweils Classic/Overview vergleichen | Aufzeichnungen statt geschätzter Zahlen; relevante Verschlechterung dokumentiert | OFFEN |

**Praktischer Orientierungstest (mindestens fünf Wiederholungen):** In Overview von einer beliebigen Top-Bar-Position aus „angefangenen Podcast finden und Karte aktivieren“, danach „zuletzt gewählte App öffnen“; Schritte/Tastendrücke, Fehlversuche, Verwechslung zwischen **Demo** und echten Fortsetzen-Karten dokumentieren. *M04 ist ein Offline-Prototyp: OK auf einer Medienkarte darf **keine** Wiedergabe starten; echter Inhaltsstart wird erst in Phase B entwickelt.*

## DP-Canvas und Abmessungen

Die ursprüngliche XML-Bemerkung **960 × 540dp** ist ein Orientierungspunkt, keine garantierte Größe für **jede** 1080p- oder 4K-Ausgabe. Android-TV-Dichte, Font Scale, Fenstereinzüge und Overscan verändern nutzbare dp. Das Overview-Layout nutzt eine **240dp** breite Sidebar, **36dp** Startabstand rechts, einen vertikal scrollbaren Preview-Bereich und eine Media-Reihe mit von der **tatsächlich gemessenen Spaltenbreite** abgeleiteter Kachelbreite. Bei wenig Höhe müssen beide Demoreihen auch per D-Pad erreichbar bleiben.

Für beide Shield-Ausgabemodi (1080p/4K, sofern TV und Shield sie anbieten) notieren:

```bash
adb shell wm size
adb shell wm density
adb shell settings get system font_scale
adb shell dumpsys window displays
```

Als grobe Plausibilitätsrechnung gilt `dp ≈ px * 160 / densityDpi`; entscheidend ist aber die **tatsächliche nutzbare View-Fläche nach Inset**. Ein Android-TV-Emulator mit kleinen Layoutflächen bzw. großer Schrift ist ein sinnvoller Zusatztest, **kein Ersatz** für eine Shield. `wm size` und `wm density` auf der echten Shield **nicht unbedacht verstellen**. Falls am Emulator simuliert: nach jeder Änderung `adb shell wm size reset` und `adb shell wm density reset`.

## Performance-Messprotokoll statt erfundener Zahlen

Für **Classic** und **Overview** unter demselben Theme/Wallpaper, App-Bestand und denselben Einstellungen jeweils fünf Wiederholungen protokollieren:

1. **Kalt-/Warmstart:** `adb shell am start -W -n dev.plume.advanced/dev.plume.MainActivity`; Androids `TotalTime` und `WaitTime` aufschreiben, **zusätzlich** per Video/Stopuhr Zeit bis zur *ersten tatsächlich reagierenden D-Pad-Taste* ermitteln. `am start -W` allein beweist keine interaktive Bereitschaft. Für einen echten Kaltstart App-Prozess nur in kontrollierter Testumgebung beenden, nicht während Home-/Accessibility-Einrichtung.
2. **Rückkehr:** Von einer gestarteten App per HOME und Back zurückgehen und Zeit bis zur reagierenden D-Pad-Auswahl messen. Den Median je Modus und die Spannweite dokumentieren.
3. **Speicher:** Nach stabiler Startseite `adb shell dumpsys meminfo dev.plume.advanced` ausführen und insbesondere **TOTAL PSS** und Java-/Native-/Graphics-Werte notieren. Mehrfach messen; keine einzelne Momentaufnahme als dauerhaft darstellen.
4. **Ruckler:** `adb shell dumpsys gfxinfo dev.plume.advanced reset`, dann die Sidebar und beide Medienreihen mehrmals durchfahren; anschließend `adb shell dumpsys gfxinfo dev.plume.advanced` für Jank- und Frame-Informationen sichern.
5. **View-Zahl:** Mit Android Studio **Layout Inspector** im jeweiligen Modus die sichtbare View-Hierarchie nach Fokus- und Scrollbewegung zählen. `uiautomator dump` zeigt nur Accessibility-Knoten und ist **keine** vollständige View-Zählung. Besonders die dynamisch gemounteten Karten und lange Sidebar vergleichen.

**M00-Baseline:** `docs/home-overview-baseline.md` beschreibt Verhalten, aber **keine gemessenen** Startzeiten, PSS-Werte oder View-Zahlen. Eine numerische Aussage „M04 ist X % schneller/langsamer als M00“ wäre daher unzulässig. Bei fehlenden alten Logs den **aktuellen Classic-Modus auf demselben Gerät** als ausdrücklich so benannten Vergleich erfassen; Unterschiede zu M00 bleiben offen.

## UX-Review und Phase-B-Gate

- **Statische Einschätzung:** Die zwei klar getrennten Demo-Reihen und die Sidebar entsprechen dem M03-Ziel. Die Demo-Markierung und die bewusst nicht abspielbaren Beispielkarten verhindern eine vorgetäuschte echte Wiedergabe. Die App-Aktion bleibt separat.
- **Kleine M04-Korrektur:** überflüssiger Neuaufbau der Demo-Reihen beim Restore derselben Sidebar-Auswahl vermieden. Keine neue Datenquelle, kein eigener Dienst, keine neue Berechtigung, keine Änderung am klassischen Home-Navigator.
- **Offene Blocker:** reale D-Pad-Randfälle/Long-OK, Dichte/Überlauf bei 1080p und 4K, Lesbarkeit über Wallpapers, Start-/Rückkehrzeiten, PSS/View-Zahl und die vollständige Classic-Regression. Für die M04-Commits müssen außerdem PR-Unit-Tests/CI und eine Debug-APK aus genau dem M04-Commit erfolgreich durchlaufen.
- **Entscheidung:** **Phase B vorerst NICHT freigeben**. Freigabe erst nach abgeschlossener U01–U20-Matrix bzw. dokumentierten, ausdrücklich akzeptierten Restpunkten und ohne Fokusverlust, Fokusfalle, unerwarteten App-Start, unlesbaren Text oder Classic-Regression.

### Testprotokoll (vor Ort ausfüllen)

| Feld | Messung/Nachweis |
|---|---|
| Datum, Shield-Version, Android-Version | OFFEN |
| APK-Commit, Actions-Run, APK-SHA-256 | OFFEN (M04); Basis-Run #42899289988 vorhanden |
| Classic-Startzeit / Overview-Startzeit (Median, n=5) | OFFEN |
| Classic-Return / Overview-Return (Median, n=5) | OFFEN |
| Classic-PSS / Overview-PSS (mehrfach) | OFFEN |
| Classic-Views / Overview-Views (Layout Inspector) | OFFEN |
| Effektive dp-Fläche bei 1080p / 4K, Font Scale | OFFEN |
| U01–U20, Videos/Screenshots, Fehler | OFFEN |
| Entscheidung (Freigabe oder Blocker) | OFFEN |
