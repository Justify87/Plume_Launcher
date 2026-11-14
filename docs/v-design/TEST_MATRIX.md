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


## M03 — Referenz-Shell und Navigationsprüfung (2026-11-13)

| Gate | Evidenz | Tatsächlicher Status |
| --- | --- | --- |
| M02 korrekt als Baseline | [Merged PR #35](https://github.com/Justify87/Plume_Launcher/pull/35), `main@3f77ba55...` | **verified_merged** |
| Referenz-Binär-/Format-Gate | Alle GitHub-Base64-Bytes gelesen, unabhängiger SHA-256 = `95af66ad...`, 1536×864 JPEG/JFIF; Bild visuell überprüft | **passed_source_verification** |
| Layout-Anker / mathematische Fit-Abbildung | `VDesignCanvasTest.referenceCanvasPreservesExactSidebarAndHeroAnchors`, `proportionalTvCanvasDoesNotAssumeDpEqualsReferencePixels`, `unusualInsetsOrAspectKeepCanvasUnstretched` | **tests_committed, execution only via PR CI** |
| Ein sichtbarer Host / Altseiten nur intern | `MainActivity.setupVDesign`, neue programmatische Root-Z-Reihenfolge; `content` unsichtbar | **source_review_only**; Emulator nicht gemessen |
| Fünf Sidebar-Targets / kein All-Apps-Permanentpunkt | `VDesignHomeView.bindNavigation` + install/resolvable package filtering | **source_review_only**, actual TV D-Pad pending |
| Nuvio-Counts und No-Fake-Content | `bindFeed(nuvioDashboardFeed)`, 14 unfokussierbare Empty-Slots, keine SmartTube-Programmquelle | **source_review_only**, reale Programme werden erst M04 gerendert |
| Lifecycle/Direct Boot/Glass-Layer | Early return on locked user, unveränderte `GlassLayer` root insertion at index 2; Callback onResume/onPause | **source_review_only**, runtime not_run |
| All Apps/Settings/HDMI/VPN / Focus | Lang-OK Übersicht/Settings, existierende Launcher-/VPN-Ziele, Restore-Fokus | **source_review_only; emulator_not_run/hardware_pending** |
| Erster PR-Run auf `a0ed625...` | [43520147382](https://github.com/Justify87/Plume_Launcher/actions/runs/43520147382) | **cancelled** durch späteren Commit |
| Zweiter PR-Run auf `b0299f...` | [43520288284](https://github.com/Justify87/Plume_Launcher/actions/runs/43520288284) | **cancelled** durch späteren Commit |
| Dritter Run auf Quell-HEAD `71f5e80...` | [43520674555](https://github.com/Justify87/Plume_Launcher/actions/runs/43520674555), [check 132213947427](https://github.com/Justify87/Plume_Launcher/actions/runs/43520674555/job/132213947427) | **in_progress at docs time**, no pass asserted |
| Native Mullvad/JNI/VpnService | Source-/Gitlink-Diff unverändert, branch-conditioned native job | **skipped; runtime pending**, keine integrierte APK-Abnahme |
| Lokale Builds/JVM/Emulator/Shield | Kein Android SDK/Checkout oder angeschlossenes Gerät | **not_run/hardware_pending** |
| Exakter endgültiger PR-HEAD nach Docs | Run nach Commit separat beobachten, ggf. PR-Kommentar mit immutable Evidence | **pending** |


### M03 – CI nach dem vollständigen Code- und Dokumentationscommit

| Test/Gate | Unveränderlicher Nachweis | Beobachtetes Ergebnis |
| --- | --- | --- |
| PR-CI auf HEAD `8160f2bf...` | [Run 43521292243](https://github.com/Justify87/Plume_Launcher/actions/runs/43521292243), [check 132215945300](https://github.com/Justify87/Plume_Launcher/actions/runs/43521292243/job/132215945300) | **completed/success** |
| JVM inkl. M03-Canvas-Tests und Android-Lint | `./gradlew testDebugUnitTest lintDebug` im Check-Log; BUILD SUCCESSFUL | **passed_ci**; 114 Warnungen, bekannte Baseline-Filter nicht geändert |
| Debug-/Release-APK-Builds | `./gradlew assembleDebug assembleRelease`; BUILD SUCCESSFUL | **passed_ci** |
| Release-APK-Differenz | Check-Log `APK: 535462 -> 540870 bytes (5408)` | **+5408 Bytes**, attributed to M03 shell/icons |
| Repo-Regeln/Installer/Offline-Regressionen | Check-Log: 44 Checks/0 fail; 10+12+6 Python unit tests | **passed_ci** |
| Native Original-Mullvad/Rust/JNI/echter Tunnel | `mullvad-apk` [Job 132215947446](https://github.com/Justify87/Plume_Launcher/actions/runs/43521292243/job/132215947446) | **skipped_branch_condition; runtime_hardware_pending** |
| Neuer Evidence-only-Docs-HEAD | nach diesem Commit neuen Run wirklich nachlesen | **pending_new_ci**, kein erfundener Pass |


## M04 — Prüfevidenz (2026-11-14; Quellenstand 66cf0da2843c334a9686f2d61499a13f09a9d762)

| Gate | Geprüfte Evidenz | Status |
| --- | --- | --- |
| M03 wirklicher Merge/Baseline | GitHub #36 merged, `main@763869b8bf672f5955b1d0682a643f0565bca5fb`; Nuvio `dev@be4b84a84e004a2d36405601683df000d8dc446a` | **passed_source_review** |
| SHA Referenz + visuell angesehen | GitHub-JFIF-Bytes 249663, 1536×864; SHA256 `95af66ad...`, Blob `d1b14c...` | **passed_binary_visual_reference**; kein Live-Screenshot |
| Hero/Standard/Available/Compact source-bound | `VDesignHomeView` 14 fixe Slots, `VDesignCardView` Focus/Progress/Art, `VDesignCardText` | **passed_source_review**, Emulator **not_run** |
| Long title/episode/films null | `VDesignCardTextTest.episodeLabelsOmitUnknownSeasonAndNeverInventFilmEpisodes`, TextView ellipse/maxLines | **test_committed_not_yet_run**; screenshot **not_run** |
| Echte vs synthetische Restdauer | `remainingIsShownOnlyWithRealDuration` + M02 null duration contract | **test_committed_not_yet_run** |
| Leere/falsche Bild-URI, stale recycling, gleiche URI in-flight | `artworkSelectionRejectsFileSchemesAndUsesPosterFallback`, ImageView.tag and shared LruCache callbacks | **test_committed_not_yet_run**, Runtime **not_run** |
| Lokalisierte ISO Daten, lokale Mitternacht, keine Fake-Countdowns | `civilReleaseDateAndMissingCountdownAreHonest` | **test_committed_not_yet_run** |
| Badge/SmartTube/Classic | `NuvioMediaItem.showNewBadge=false`; ausschließlich M02 drei Nuvio-Listen | **passed_source_review**, Device **not_run** |
| Fokusrahmen/Kachelüberlauf/Fonts/Blur-GPU | Kein Scale/Blur in VDesign-Card, referenzgebundene Positionen | **passed_source_review**, Pixel-/GPU-/Device **not_run** |
| Upcoming >8 / D-Pad / intent return | Viererschritte UP/DOWN, strict details mode; umfassende Navigation M05 | **passed_source_review**, Device **not_run** |
| JVM-Unit, lintDebug, assembleDebug/Release | [CI 43523928137](https://github.com/Justify87/Plume_Launcher/actions/runs/43523928137) / [check 132224852062](https://github.com/Justify87/Plume_Launcher/actions/runs/43523928137/job/132224852062) | **in_progress_at_docs**, result not inferred |
| Mullvad original Rust/JNI/VpnService/gitlink | M04-Dateidiff nur Plume UI + JVM tests/docs, keine Mullvad-/Gradle-/Manifest-Änderung | **passed_scope_review**; native workflow skipped |
| Lokaler Build/Emulator/Shield 2019/Screenshot/Physik/Leistung | Keine lokale Android SDK- und Git-Klon-Umgebung; kein Shield verbunden | **not_run/hardware_pending** |

**Wichtig:** Ein laufender Vorgänger-CI-Run beweist noch keine Buildqualität. Nach Docs-Commit finalen SHA und Workflow/Jobs prüfen; M05 ist nächster Schritt, niemals Merge ohne Freigabe.


### M04 Code-Review-Nachtrag vor finalem CI
| Gate | Prüfung | Stand |
| --- | --- | --- |
| Fokus nach entfernter Episode | `VDesignHomeView.bindFeed` Ersatzziel nach Stable-ID-Verlust in `13988ae` | source_review_only, emulator not_run |
| URL mit leerem Host / HTTP-Userinfo / content ohne Authority | zusätzliche Assertions in `VDesignCardTextTest.kt` plus `java.net.URI`-Validierung | test_added_not_run_at_commit |
| erster PR-Run `66cf0da` | [Run 43523928137](https://github.com/Justify87/Plume_Launcher/actions/runs/43523928137) | **completed/cancelled** nach neuerem Docs-Commit, Unit+lint success vor Abbruch |
| Docs-Run vor Source-Fix | [Run 43524298952](https://github.com/Justify87/Plume_Launcher/actions/runs/43524298952) | **in_progress** zuletzt beobachtet; durch Folge-Commit eventuell cancelled, kein Erfolg behauptet |
| finaler HEAD-Run | noch nach dieser Docs-Aktualisierung prüfen | pending |


### M04 zusätzliche bindbare Regressionstests
| Gate | Nachweis | Status vor finalem CI |
| --- | --- | --- |
| 3+3+8-Binder, fehlende Titel/Serien, keine Dummies/Cross-Kategorien | `VDesignCardSlotsTest.partialOrEmptySectionsNeverReceiveDummiesOrCrossFeedContent` | **committed_not_yet_run** |
| UPCOMING 13 Einträge, mehr als 8 reale Vorschauen | `eightVisibleUpcomingCardsAreAWindowAndAllCountsSurvive` | **committed_not_yet_run** |
| UP/DOWN-Paging, letzte unvollständige Reihe, Clamp | `pagingNeverSkipsTheLastEntryOrEscapesTheSection` | **committed_not_yet_run** |
| realer View-Renderer/GPU/Emulator/Shield | kein UI-/Hardware-Lauf im Container | **not_run/hardware_pending** |


## M05 — D-Pad, Overlay, Home und abgesicherte Nuvio-Starts (2026-11-14)

| Prüfung | Faktische Evidenz | Status zur Dokumentation |
| --- | --- | --- |
| M04-HEAD/PR real gemergt | [PR #37](https://github.com/Justify87/Plume_Launcher/pull/37), `main@fdd10f3394463bd5d4142c9978834d78464905cc` | **verified_merged** |
| M04 letzter Code-/Docs-Run | [43524952681](https://github.com/Justify87/Plume_Launcher/actions/runs/43524952681), Check 132228353581 | **completed_success** |
| Originalbild SHA/Größe/Format | GitHub blob `d1b14c00...`, SHA-256 `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`, 249663 Bytes JPEG/JFIF 1536×864 | **passed_original_binary_sha** (kein Launcher-Screenshot) |
| 1 Hero + 2 Standard, 3 Available, 4×2 Mini, kein Wrap und Empty-Row-Skip | `VDesignFocusNavigatorTest.heroAndTwoNeighborsRespectWideGeometry`, `availableThreeAndFourByTwoMinisNeverWrapOrStrand`, `emptyBandsSkipToActualLiveCardsAndNoDeadEnd` | **tests_committed_ci_pending** |
| Sidebar D-Pad sichtbare Targets, kein Invalid-Focus | `VDesignFocusNavigatorTest.sidebarVisibleItemsOnlyAndPreferredTarget` | **test_committed_ci_pending** |
| Nuvio Stream/Detail/UPCOMING never stream | `VDesignLaunchPolicyTest.episodePlaybackAndDetailFallback`, `upcomingNeverAutoplays` | **tests_committed_ci_pending** |
| Provider-intent package/component/action/scheme/selector | `VDesignLaunchPolicyTest.explicitComponentNoUntrustedSchemesOrSelectors`, Nuvio `AndroidTvChannelManager` Quell-HEAD gelesen | **test_committed_ci_pending/source_review** |
| App-/VPN-/Overlay-/HOME-/Back-/Return-Fokus und Deinstallation | `VDesignHomeView`/ `MainActivity` Code-Review und neue Fokusverdrahtung | **source_review_only**; TV-Integration **not_run** |
| Long-OK Alle Apps/Settings; HDMI, Systemsettings, Power, Home-Setup | `MainActivity.showVDesignSystemActions`, `onHold`, `onBackPressed` Quellenreview | **source_review_only**, Fernbedienung **not_run** |
| CI-Codebranch nach JVM-Testtyp-Korrektur | [Run 43528175980](https://github.com/Justify87/Plume_Launcher/actions/runs/43528175980), [Job 132239022875](https://github.com/Justify87/Plume_Launcher/actions/runs/43528175980/job/132239022875) auf `d80aa6fd...` | **in_progress at documentation**, kein Success angenommen |
| CI auf finalem Code+Docs-HEAD | Workflow läuft erneut beim Dokumentationscommit, finale URL/Status neu lesen | **pending_new_run** |
| Native Mullvad/JNI/VpnService Original | GitHub Compare nur 7 Plume-Code-/Testdateien, kein Submodul/Manifest/Gradle geändert; M05-Workflow Mullvad `skipped` | **scope_passed**, **native_not_run** |
| Lokale JVM/Android-SDK-Builds, Emulator, Shield Pro 2019, echte IPTV/VPN-/HDMI-Session, UI-Pixel und Memory/GPU | Kein Android-SDK/Repo-Checkout im Container, kein Gerät angebunden | **not_run / hardware_pending** |

**Regel:** Aktuelle `STATE.md`-YAML hat Vorrang vor älteren M04-/M03-Momentaufnahmen. Finale CI-Nachweise nur mit tatsächlicher Job-URL und zugehörigem commit SHA; keine physische Abnahme aus Unit-Tests ableiten.


### M05 — CI-Ergebnis auf dem vollständigen Code-und-Dokumentationsstand

| Gate | Beobachtete Evidenz | Status |
| --- | --- | --- |
| Full source+docs SHA | [`1e8a7cb5e7d2c823a32e4069cfa96e1a71a11b0e`](https://github.com/Justify87/Plume_Launcher/commit/1e8a7cb5e7d2c823a32e4069cfa96e1a71a11b0e), [Run 43528616572](https://github.com/Justify87/Plume_Launcher/actions/runs/43528616572) | **completed_success** |
| Check-Job, Repo/ADB/Offline + Kotlin/JVM und lint | [Job 132240568044](https://github.com/Justify87/Plume_Launcher/actions/runs/43528616572/job/132240568044): `./gradlew testDebugUnitTest lintDebug` BUILD SUCCESSFUL | **passed_ci** |
| Debug- und Release-APK | gleicher Job: `./gradlew assembleDebug assembleRelease` BUILD SUCCESSFUL | **passed_ci** |
| Release APK mit tatsächlicher Baseline | Joblog `APK: 545714 -> 550334 bytes (4620)` | **passed_measured_delta +4620 B** |
| Native Mullvad/ARM64/VPN | [Job 132240569945](https://github.com/Justify87/Plume_Launcher/actions/runs/43528616572/job/132240569945) | **skipped_branch_filter**, VPN/Shield hardware_pending |
| Alte M05-Workflow-Runs | [43528093899](https://github.com/Justify87/Plume_Launcher/actions/runs/43528093899) und [43528175980](https://github.com/Justify87/Plume_Launcher/actions/runs/43528175980) | **cancelled_by_new_commits**, kein Pass |
| Dieser CI-Evidence-Docs-Commit | Neuer HEAD und GitHub-Actions-Run erst nach Push auszuwerten | **pending_new_ci** |

Damit sind Unit- und Code-/Docs-CI-Gates nachgewiesen, aber **nicht** physische Device-/HOME-/VPN-/Nuvio-Playback-Tests oder Pixelvergleich.


## M06 — Home-Einzelvertrag, Migration, reale CI-Evidenz

| Gate | Beobachtete Evidenz | Stand |
| --- | --- | --- |
| Alt-View-Bäume entfernt | GitHub-Quellprüfung `activity_main.xml`: 0× `classicHome`, `overviewHome`, `appRow`, `watchRow`, `homeBodyHost`; `VDesignHomeView` verbleibt | **passed_static_source** |
| Alt-Modus und Renderer unerreichbar | `MainActivity.kt`: 0× `HomeLayoutMode`, `homeLayoutMode`, `HomeSidebarView`, `OverviewHomeView`, `HomePageState`, `R.id.appRow`, `R.id.watchRow`; Mode-Kotlin-Dateien gelöscht | **passed_static_source** |
| Altdaten-Import | `SettingsTransferTest.oldExportWithObsoleteHomeModeAndRowsIsSafe`, `restore` lässt abgeschaltete lokale Schlüssel in Ruhe | **test_committed_ci_pending** |
| Direct Boot V-only | gesperrter User erhält `VDesignHomeView` ohne Credential-Preferences und ohne TV-Provider | **passed_source_review; hardware_not_run** |
| Erster PR CI-Run (Compiler) | [43533466727](https://github.com/Justify87/Plume_Launcher/actions/runs/43533466727), [Check 132256619151](https://github.com/Justify87/Plume_Launcher/actions/runs/43533466727/job/132256619151) | **completed_failure — rowNames/cwColour** |
| Korrigierter Code nach Erst-Fehler | `e48abd7c7d5554a5b5e2482df173ba38ebe76d0a`, [Folge-Run 43533837289](https://github.com/Justify87/Plume_Launcher/actions/runs/43533837289) | **completed_failure — alter YouTube-Overview-Unit-Test** |
| YouTube-Dedupe-Unit-Test migriert | [d5d88e5](https://github.com/Justify87/Plume_Launcher/commit/d5d88e502ea960419756625523553d596f29de2d), [dritter Run 43534292196](https://github.com/Justify87/Plume_Launcher/actions/runs/43534292196) | **in_progress zum Doku-Schreibzeitpunkt** |
| JVM, lint, debug/release APK am letzten HEAD | realen **finalen** CI SHA/Job nach Docs-Commit neu prüfen | **not_yet_confirmed** |
| Gradle lokal | kein SDK/Checkout für lokalen Build | **not_run** |
| Shield Pro 2019, HOME, Long OK, VPN, Provider, Direct Boot, All Apps, Wallpaper | kein Gerät/Emulator im Ausführungsumfeld | **not_run_hardware_pending** |
| Original Mullvad JNI/Submodule, Nuvio dev, Permissions | keiner der betreffenden Pfade in Code-Commit geändert | **passed_diff_scope_only; runtime_not_run** |


| M06 Code+Docs, 3. CI-Anlauf | [43534378039](https://github.com/Justify87/Plume_Launcher/actions/runs/43534378039), [Check 132259774709](https://github.com/Justify87/Plume_Launcher/actions/runs/43534378039/job/132259774709): 218 Tests, 1× `SettingsTransferTest` `org.json`-RuntimeException | **completed_failure** |
| M06 pure Migration nach JVM-Stub-Korrektur | [cabf0a7](https://github.com/Justify87/Plume_Launcher/commit/cabf0a76705759254f69f8c8754591fbbfff3c3f), [Run 43534832233](https://github.com/Justify87/Plume_Launcher/actions/runs/43534832233) | **in_progress_at_documentation** |
| M06 vollständiger JSON-Dateiimport unter Android | nicht durch plain JVM `validate()` abgedeckt, Emulator/Shield erforderlich | **not_run** |
