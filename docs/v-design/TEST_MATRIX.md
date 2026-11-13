# V-Design — Test- und Evidenzmatrix

Die folgenden Statuswerte sind **beobachtete** Ergebnisse oder `not_run`; spätere Jobs dürfen nicht als bereits ausgeführt dargestellt werden.

| Prüfung | Evidenz / Ausführung | Ergebnis | Geplant |
| --- | --- | --- | --- |
| ZIP Seed-Datei vorhanden und tatsächlich lesbar | Python `zipfile` auf hochgeladenem ZIP; Datei 249663 Bytes | **passed_local** | M00 |
| Referenz-SHA256 stimmt | Python `hashlib.sha256` `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25` = `REFERENCE_SHA256.txt` | **passed_local** | M00 |
| Referenz 1536×864 | PIL `Image.open`, RGB 1536×864; tatsächlich visuell angesehen | **passed_local** | M00 |
| Byteidentische `docs/v-design/REFERENCE.png` im Git-PR | Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18` exakt unter neuem Pfad, Source-Blob identisch; 249663 Bytes, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25` | **passed_binary_identity** | M00 |
| Source Baseline Plume/Nuvio | GitHub API main `fbf2473711c50d03eb76afe8fd0a722af1faf136`; dev `be4b84a84e004a2d36405601683df000d8dc446a` | **passed_source_review** | M00 |
| Nuvio Channel IDs/Publisher-Felder | `TvChannelKind`, `TvChannelFeedBuilder`, `AndroidTvChannelManager` gelesen | **passed_source_review** | M00 |
| Veraltete Mullvad-Doc-Pfade identifiziert | Repo-Baum, `docs/archive/legacy-features/mullvad-vpn/STATE.md` | **passed_source_review** | M00 |
| UI-Featureinventar statt Blind-Löschung | `MainActivity`, XML, Media-/Focus-Klassen, Manifest/Gradle | **passed_source_review** | M00 |
| Dokumentationsdiff / Dateirechte / Binärdiff | GitHub Compare `fbf2473...c8b0241`: genau 10 neue Textdateien `docs/v-design/*`, 0 andere Änderungen | **passed_source_review** (binary PNG weiterhin fehlt) | M00 |
| Android/Gradle lokal | `./gradlew testDebugUnitTest lintDebug` | **not_run — Container ohne Netz/Repo-Checkout/Android SDK** | M00 |
| Plume M00-CI | PR #32: früher [Run 43505553469](https://github.com/Justify87/Plume_Launcher/actions/runs/43505553469) `cancelled`, späterer [Run 43507886182](https://github.com/Justify87/Plume_Launcher/actions/runs/43507886182) `check=success`; Referenz fehlte damals. PR #33 letzter Run 43509990732 completed/success, check=success | **old_pr_ci_passed / new_pr_ci_pending — kein vollständiges M00-Pass** | M00 |
| Exakte drei Nuvio Channel Types | Provider-ID-Adaptertests | not_run | M01 |
| Legacy-Kanal nicht als Available mischen | Mixed-Feed-Unit-Tests | not_run | M01 |
| TV-Permission denied / Nuvio fehlt | Provider-/Emulator-/Shield-Test | not_run | M01–M07 |
| Real vs synthetischer Fortschritt / Restzeit | M02-Unit-Tests | not_run | M02 |
| „Neu“ nur bei nachgewiesenem Alert | Release-Tests | not_run | M02 |
| Upcoming Release und lokale Zeitzone/Tageswechsel | M02-Unit-Tests | not_run | M02 |
| Episodensicheres Dedupe, Anzahl und Sortierung | M02-Unit-Tests | not_run | M02 |
| Sidebar, 3 Reihen, Hero ≈52 % | echte UI-Screenshots plus Bildoverlay | not_run | M03–M07 |
| echte/missing Poster, Cache | Emulator/Unit-Tests | not_run | M04 |
| D-Pad/Back/HOME/Long-OK/HDMI/Settings/Alle Apps/VPN | Emulator/Shield | not_run | M05 |
| Nuvio Deep Link und Upcoming Detail | echter TV-Integrationstest | not_run | M05 |
| keine alten Home-Modi/Settings | Code-Suche und Migrationstests | not_run | M06 |
| JVM-Tests, lint, Debug/Release und echte CI | CI-Lauf-/Job-URLs | not_run | M07–M08 |
| Mullvad original JNI/VpnService/Gitlink | Git-Diff + Integrations-CI | not_run | M07–M08 |
| Shield Pro 2019 (Android TV 11), VPN & Performance | physisches Messprotokoll | **hardware_pending** | M08 |

**CI-Baseline vor M00:** `https://github.com/Justify87/Plume_Launcher/actions/runs/43503999928` (vorheriger Mullvad-Docs-Archivierungs-PR), bei Abfrage `in_progress`. Andere ältere PR-Runs sind kein Nachweis für M00. `main`-Combined-Status-API lieferte `statuses: []` (kein Erfolgsbeleg).

**Review-Regel:** `passed_source_review` beweist ausschließlich gelesenen Quelltext, nicht Laufzeitkorrektheit. Weder physischer Test noch Bildoverlay des realen Launchers wurde durchgeführt.

## M00-Nachtragsprüfungen 2026-11-13

| Prüfung | Tatsächlich geprüfter Befund | Status |
| --- | --- | --- |
| Prüfsummenberechnung über echte GitHub-Originalbytes | Base64-Dekodierung des GitHub-Blobs, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`; Testvektoren `abc` und leerer String stimmen | passed |
| Containerkennung und Pixelgröße | JPEG/JFIF-Signatur `FF D8 FF E0`; SOF0-Header 1536×864 (die originale `.png`-Endung täuscht PNG vor) | passed_identified_mislabeled_jpeg |
| Binärkopie in kanonischen Gitpfad | Git-Tree referenziert alten unveränderten Git-Blob `d1b14c00a6478cb3b49fcbbd637e0aa71db5df18` als `docs/v-design/REFERENCE.png` im Commit `e1c13b2783aa632c2cbf391b10eb923426f8fc81` | passed_blob_identity; nach PR-Commit nochmals read-back |
| Historische PR-#32-CI | [Run 43505553469](https://github.com/Justify87/Plume_Launcher/actions/runs/43505553469) zuerst `cancelled`; später [Run 43507886182](https://github.com/Justify87/Plume_Launcher/actions/runs/43507886182), `check=success`, `mullvad-apk=skipped` | letzter alter PR-Run `success`, damaliges Bild-Gate fehlte |
| Neuer M00-Ergänzungs-PR-CI | Nach PR-Erstellung mit Run-/Job-URL und Endstatus nachtragen | pending |
| Android-Gradle lokal, Screenshot-Vergleich, Shield Pro 2019 | Keine lokale SDK-/Checkout-/Geräteumgebung; Bild ist Referenz und keine App-Screenshot-Evidenz | not_run / hardware_pending |

**Erster PR-#33-CI-Beobachtungspunkt:** [Run 43509467207](https://github.com/Justify87/Plume_Launcher/actions/runs/43509467207), `check=in_progress`, `mullvad-apk=skipped` zum Abrufzeitpunkt. Dokumentations-Metadatencommit kann diesen Run gemäß Workflow-Concurrency abbrechen. Deshalb **für finale Abnahme unbedingt neuesten HEAD/Run erneut lesen**; kein `passed` aus diesem Zwischenstand.


## M01 — echte Test- und CI-Evidenz (2026-11-13)

| Gate | Nachweis | Status |
| --- | --- | --- |
| M00 finaler PR-CI-Gate | [Run 43509990732](https://github.com/Justify87/Plume_Launcher/actions/runs/43509990732), [check Job 132177567483](https://github.com/Justify87/Plume_Launcher/actions/runs/43509990732/job/132177567483) | **completed/success; check=success**, Mullvad-Job skipped (bedingtes Job-Gate) |
| Originalbild in M01 | GitHub `docs/v-design/REFERENCE.png` Git-Blob `d1b14c00...` über Base64 dekodiert, SHA-256 per JS berechnet, 249663 Bytes, FF D8 FF E0, 1536×864 | **passed_sha256_and_dimensions**, keine reale Launcher-Bildprüfung |
| Exakte Paketvarianten | Nuvio `app/build.gradle.kts@8bc01a1b...`: FullRelease `com.nuvio.tv`, FullDebug `com.nuviodebug.com`, PlayStoreRelease `com.nuvio.app`, PlayStoreDebug `com.nuvio.appdebug`; Benchmark-Varianten + historische Fork-Aliase im Code | **passed_source_review**; Unit-Tests hinzugefügt, Ausführung pending |
| Provider-ID statt Displayname | `NuvioProviderTest.localizedNamesCannotChangeProviderIdClassification` | **test_added_not_run** (PR-CI pending) |
| Legacy Mixed & positiver Fortschritt | `classicMixedEntriesDoNotBecomeAvailableOrUpcoming`, `watchNextNeedsCorrectTypeAndPositivePlayback` | **test_added_not_run** |
| Missing provider-ID/Spuffing / andere App | `missingWrongAndSpoofedProviderIdentityAreNotIncluded` | **test_added_not_run** |
| Permission, Browsable/Opt-in, fehlende App | `optInAndPermissionAndAbsentProviderDoNotInventItems` | **test_added_not_run** |
| Kein Classic-Channel-Polluting, separate Snapshots | `newCategoryWithMisleadingOldLabelNeverEntersClassicSelection`; `MainActivity` Review | **test_added_not_run / passed_source_review** |
| PR #34 Gradle, lint, Debug/Release | [Draft PR #34](https://github.com/Justify87/Plume_Launcher/pull/34), finalen Run/Job erst nach Ausführung eintragen | **pending**, kein angenommener Pass |
| Lokaler Android SDK-/Gradle-Build | `git ls-remote` in Container: DNS github.com nicht auflösbar; kein Android SDK | **not_run_environment** |
| Shield Pro 2019, Android-TV-Provider-Opt-in, Mullvad Runtime, Pixelreferenz | kein physisches Gerät oder echter UI-Screenshot erreichbar | **hardware_pending / not_run** |

Nur GitHub-Lauf mit zugehörigem finalen Commit nachträglich als CI-Pass werten. Der Launcher wird in M01 nicht neu gerendert; visuelle Abnahme M03–M08.


### M01 — tatsächliche GitHub-CI-Ergebnisse nach Code-Fix

| Gate | Exakte Evidenz | Ergebnis |
| --- | --- | --- |
| Früher PR-Check `9480a941...` | [Run 43512433898](https://github.com/Justify87/Plume_Launcher/actions/runs/43512433898), [Job 132185785148](https://github.com/Justify87/Plume_Launcher/actions/runs/43512433898/job/132185785148) | **failure**, 213 Unit-Tests, 2 Fehler (Serien-Dedupe / Legacy-Doppelung) |
| Fehlerkorrektur | [Commit 30084ee9](https://github.com/Justify87/Plume_Launcher/commit/30084ee9fd630c805e799e4c2bec20416310bb05), beide Fehlerursachen in `AndroidTvMediaSource.kt` korrigiert | **committed**, vorherige Tests beibehalten |
| Korrigierte Tests + Lint | [Run 43513105647](https://github.com/Justify87/Plume_Launcher/actions/runs/43513105647), [check 132188191911](https://github.com/Justify87/Plume_Launcher/actions/runs/43513105647/job/132188191911) auf `30084ee9...` | **passed**, Gradle `testDebugUnitTest lintDebug` |
| Debug + Release | gleicher Job, `assembleDebug assembleRelease` | **passed** |
| Release-APK gegen main | gleicher Job, Release APK: vorher 529422, nachher 532270 Bytes | **passed**, +2848 Bytes |
| M01-nativer Mullvad-APK-Job | Run 43513105647, Branch-Bedingung des Workflows | **skipped**, kein nativer Runtime-Pass |
| CI für nachgezogenen Dokumentations-HEAD | wird nach diesem Commit neu ausgelöst, anschließend realen URL/Status prüfen | **pending**, nicht vorweggenommen |
| Physische Shield 2019, echte TV-Channel-Opt-in/Permission-Laufzeit | kein Gerät angebunden | **hardware_pending**, kein Screenshot-Vergleich |

 
## M02 — aktuelle Test- und CI-Evidenz (2026-11-13)

| Prüfung | Konkreter Test/Nachweis | Beobachteter Status |
| --- | --- | --- |
| Baseline M01 merged | GitHub [PR #34](https://github.com/Justify87/Plume_Launcher/pull/34) merged, Main `3d4abbdf`; [CI 43514084610](https://github.com/Justify87/Plume_Launcher/actions/runs/43514084610) completed/success | **passed_source_and_ci_observed** |
| Normreferenz 249663 Bytes, JPEG/JFIF 1536×864 und SHA-256 | GitHub-BLOB `d1b14c00...` Base64 gelesen, SHA-256 `95af66ad...` neu berechnet | **passed_binary_verification** |
| Section-Reihenfolge/Empty und voller Count >8 | `NuvioDashboardFeedTest.fixedOrderAndHonestEmptySections`, `fullVisibleCountsNotMountedThreeOrEightCards` | **added; PR-CI pending** |
| Cross-Channel-Dedupe, Provider-S/E, Titeldoppler, Intent | `crossChannelDuplicateStartedWinsAndWatchNextPrefixNormalizes`, `distinctEpisodesAndShowsSurviveEvenWhenTitlesMatch`, `titleAndGenericIntentAndRowIdAloneCannotCrossDedupe`, `recognizedIntentContentIdIsCrossChannelEvidence` | **added; PR-CI pending** |
| Provider-Gewichtung, Engagement, chronologische Releases | `priorityIsWatchTimeThenProviderWeightThenUpcomingCivilDate` | **added; PR-CI pending** |
| Synthetische 100s, echte Zeiten, unbekannte/fehlerhafte Werte | `percentOnlyPublisherDurationNeverClaimsMinutes`, `realDurationAndPositionYieldValidProgressAndRemaining`, `unknownAndImpossibleTimingRemainsUnknown` | **added; PR-CI pending** |
| Keine falschen Neu-Badges oder Bilddaten | `descriptionIsNotAnAlertFlagAndMissingArtIsNotFilteredOut` | **added; PR-CI pending** |
| ISO LocalDate, Kalender/Timezone, 2028-02-29, DST | `strictCivilDateNeverFallsBackToEngagementTimestamp`, `todayAndTimezoneSwitchAtActualLocalMidnight`, `leapDayAndDstBoundaryDoNotAssume24HourDays` | **added; PR-CI pending** |
| Fremd-App-Spoofing aus V-Dashboard ausgeschlossen | `nonNuvioEntriesCannotEnterThreeFeeds` | **added; PR-CI pending** |
| Erste M02-CI | [Run 43516551245](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245), [check Job 132199847040](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245/job/132199847040) | **in_progress bei erster Beobachtung**, kein success claim |
| M02 Kotlin/lint/APKs lokal | Kein lokaler GitHub-Checkout/Android SDK; `git ls-remote` DNS nicht auflösbar | **not_run_environment** |
| Mullvad CI / Original-JNI-Runtime | CI `mullvad-apk` durch Branchfilter skipped, keine Originalsubmoduländerung | **skipped / hardware_pending** |
| UI-Screenshot/D-Pad/Shield Pro 2019/echte Provider-Permission | Keine Emulator-/physische Geräteverbindung | **not_run / hardware_pending** |

**Nach CI:** Jede spätere Testaussage muss exakt den neuesten M02-PR-Code-/Dokumentations-HEAD und den beobachteten Run/Job referenzieren. Alte M01-Tabellen sind historisch.

 
### M02 CI-Nachtrag: erster Fehl-Lint und Folgefehler-Fix

| Gate | Realer Nachweis | Befund |
| --- | --- | --- |
| Erster Code-HEAD `9003878...` | [Run 43516551245](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245), [check 132199847040](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245/job/132199847040) | **failed**: `testDebugUnitTest` ausgeführt, `lintDebug` SuspiciousIndentation M02-Reset-Catch, exit 1 |
| Fehlerbehebung | [834ae544](https://github.com/Justify87/Plume_Launcher/commit/834ae544b3d24a8963842e19a9562d6ce6af07f5) | **committed**, keine Disable-/Baseline-Änderung |
| Folgefehler in Datenvalidierung | [6b740628](https://github.com/Justify87/Plume_Launcher/commit/6b740628d10e05d92f9c065ff38f42e6371c2cef), [1a057219](https://github.com/Justify87/Plume_Launcher/commit/1a057219e8afb355fb2e1c6f7cda9c0227e587a0) | **fix + test committed**: `position > duration` trotz Parser-Clamp kein 100% |
| Weitere Workflow-Läufe | 43517228539, 43517300556, 43517409755 | **cancelled** wegen späterer Commits; kein Pass |
| Letzter Dokumentations-HEAD CI | nach finalem Dokumentationscommit URL/Status erneut lesen | **pending, nicht als bestanden gewertet** |

 
### M02 — tatsächlicher finaler Code-/Docs-CI-Erfolg

| Gate | Echte Ausführung | Ergebnis |
| --- | --- | --- |
| [Run 43517654027](https://github.com/Justify87/Plume_Launcher/actions/runs/43517654027), SHA `97ed765829d8e75dde9868925bd3cd7b607fff2b` | [check 132203743576](https://github.com/Justify87/Plume_Launcher/actions/runs/43517654027/job/132203743576) | **completed/success** |
| Android-Lint und JVM-Unit-Tests | `./gradlew testDebugUnitTest lintDebug`, BUILD SUCCESSFUL im Check-Log | **passed_ci** |
| Debug- und Release-APK | `./gradlew assembleDebug assembleRelease`, BUILD SUCCESSFUL | **passed_ci** |
| APK-Größenvergleich | Baseline `532270`, M02 `535462` Byte, Differenz `+3192` | **passed_ci** |
| Repo/Offline-Tests | 44 ADB Installer Checks/0 failed; 10 Spotify-, 12 Mullvad-Staging-, 6 Mullvad-APK-Validator-Python-Tests | **passed_ci** |
| Native Mullvad-APK | Job `mullvad-apk` in diesem V-Design-PR `skipped` | **not_run_branch_condition** |
| Neuer Evidence-Docs-HEAD | Nach diesem Nachtrag CI separat prüfen, keine Vorwegnahme | **pending_new_run** |
| Hardware-Shield, echte Opt-in/Intent-/VPN-Sessions | Kein Gerät oder Emulator angebunden | **hardware_pending** |

Der frühere failed-Lint-Run 43516551245 bleibt als historische Evidenz erhalten. Die Testfalllisten in diesem Dokument wurden im erfolgreichen Lauf mit ausgeführt; keine Test-Suppression.
