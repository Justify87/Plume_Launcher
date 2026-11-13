# M02 finaler CI-Nachtrag — 2026-11-13

**Maßgeblicher beobachteter Check:** [Run 43517654027](https://github.com/Justify87/Plume_Launcher/actions/runs/43517654027) `completed/success` auf `97ed765829d8e75dde9868925bd3cd7b607fff2b`; [check 132203743576](https://github.com/Justify87/Plume_Launcher/actions/runs/43517654027/job/132203743576) JVM-Tests/Lint, Debug+Release, 44 Installer-Checks sowie 10/12/6 Offline-Python-Tests bestanden, APK `532270→535462` (+3192 B). Nachfolgender Evidence-Docs-Commit braucht neuen separaten CI-Nachweis; native Mullvad-Job skipped, Shield hardware_pending. PR [#35](https://github.com/Justify87/Plume_Launcher/pull/35) **offen, ungemergt**. Genau nächster Meilenstein M03.

---

# V-Design — operative Übergabe nach M02 (2026-11-13)

## Vertrauenswürdiger Einstieg

- Plume letzter vor Beginn von M02 geprüfter **gemergter Main-HEAD**: `3d4abbdf4f3dbfb481e0a8a6bf95aea815a172bd`. M00 #32/#33 und M01 [#34](https://github.com/Justify87/Plume_Launcher/pull/34) **sind gemergt**. [M01-finaler CI-Run 43514084610](https://github.com/Justify87/Plume_Launcher/actions/runs/43514084610) `completed/success`.
- **M02** Branch `feature/vdesign-m02-media-model`, [Draft-PR #35](https://github.com/Justify87/Plume_Launcher/pull/35), Code-HEAD bei Dokumentation `1a057219e8afb355fb2e1c6f7cda9c0227e587a0`; nach Docs-Commit tatsächlichen aktuellen Branch/PR-HEAD und dessen CI erneut prüfen. **Nicht gemergt**.
- Nuvio `Justify87/NuvioTV dev@be4b84a84e004a2d36405601683df000d8dc446a` nur gelesen. Dokumente der abgeschlossenen Mullvad-Funktion befinden sich unter `docs/archive/legacy-features/mullvad-vpn/`; kein Submodul/JNI/Rust/VpnService geändert.
- Normative `docs/v-design/REFERENCE.png`: aus Original-GitHub-Base64 erneut SHA-256-bestätigt `95af66ad04f462867b5d52825c7e1946ac68b5e40bed52c07f4e74c085218f25`, 249663 Bytes, **1536×864**. JPEG/JFIF-Bytes unter historischer PNG-Endung bleiben unverändert.

## M02: tatsächlich implementierte Schnittstelle für M03

- `MainActivity.queryWatchNext()` füllt `nuvioProviderFeeds` aus M01 und danach `nuvioDashboardFeed = NuvioMediaProjector.project(nuvioProviderFeeds)`. Bei fehlender TV-Listings-Berechtigung/Providerfehler werden **beide** Snapshots geleert; der einzelne bestehende TV-IO-Executor bleibt. Das Modell ist bislang nicht in alten Classic-/Overview-Kacheln gerendert.
- `NuvioDashboardFeed.sections()` liefert **immer genau** `IN_PROGRESS`, `AVAILABLE`, `UPCOMING` inklusive leerer Listen; `NuvioDashboardSection.count` ist die vollständige Anzahl nach Cross-Channel-Dedupe, **nicht** die 3/8 tatsächlich montierten Cards.
- `NuvioMediaItem` trägt reale Staffel/Folge/Episodentitel, Serien-ID (falls Provider liefert), Kurz-/Langbeschreibung, Release-LocalDate, Artwork-/Poster-/Logo-URLs, Intent, sortierte Provider-Position, belastbaren Prozent-/Zeitstatus. Identität: programmbezogene Nuvio-Provider-ID `contentId_sXeY` bzw. `wn_`-Variante, erkannte `S.contentId`-Intent-Extra als Fallback; Staffel/Folge unterscheiden Episoden. **Keine Dedupe per bloßem Titel oder TV-`_id`**; bei völlig fehlender Identität absichtlich kein Cross-Channel-Zusammenwurf.
- Reihenfolge: In-Progress `lastWatched` absteigend, Available originaler Kanal-Weight (Index der bereits sortierten Query), Upcoming echte ISO-`yyyy-MM-dd` aufsteigend; unbekanntes Datum **zuletzt**. Identische Episode in mehreren Kategorien erhält Vorrang `IN_PROGRESS > AVAILABLE > UPCOMING`. Nuvios vorgelagerte `distinctBy(contentId)`-Serienunterdrückung kann Plume **nicht** rückgängig machen.
- Nuvio synthetisiert für Prozent-only manchmal 100000 ms und eine dazu passende Position: solche Werte dürfen eine Fortschrittsanzeige begründen, aber **keine** verbleibenden Minuten. Auch eine theoretisch reale Episode von exakt 100 Sekunden wird aus Sicherheitsgründen ohne Restzeit angezeigt. Fehlendes Bild oder Datum darf eine echte Episode nicht löschen.
- `NuvioMediaItem.showNewBadge` ist bewusst immer `false`: Nuvio überträgt `isReleaseAlert` nicht strukturiert, `short_description` ist lokalisierte Anzeige, keine boolesche Evidenz. `daysUntilRelease(now, zone)` vergleicht **lokale Kalendertage**; `NuvioMediaProjector.nextLocalDayBoundary(now, zone)` liefert den DST-korrekten nächsten einmaligen Rebind-Zeitpunkt. **Noch kein Polling und keine UI-Timer**.

## Nachprüfung und exakt M03

1. Vor M03 tatsächlichen M02-PR-HEAD und **finalen** CI-Run auf diesem SHA lesen. Erster [Run 43516551245](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245), [check 132199847040](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245/job/132199847040) war bei letzter M02-Quellprüfung `in_progress`, Mullvad-APK-Job `skipped`; finalen Status erst nach realem Abruf eintragen. Lokaler Build `not_run`, Shield `hardware_pending`.
2. **M03: einziges V-Design-Home plus Sidebar nach Normbild** aus `NuvioDashboardFeed.sections()` bauen, keine Demo-SmartTube-Tiles. Bei Empty States die drei Bereiche behalten; die alten Layouts erst gemäß Roadmap ersetzen.
3. Day-count beim Bind/Resume neu aus `daysUntilRelease(Instant.now(), ZoneId.systemDefault())` auswerten und **einmalig** an `nextLocalDayBoundary` invalidieren; Callback bei Pause/Destroy bzw. geänderter Zeitzone sauber erneuern. Dies ist UI-Lebenszyklusarbeit für M03, kein M02-Polling.
4. Sichere Deep-Link-Grenze `MainActivity` weiter beachten; UPCOMING immer Details. Testen auf Emulator/Shield erst wenn wirklich möglich; kein Screenshot-/VPN-Hardware-Pass behaupten.

**Kein Merge ohne explizite Freigabe.** Historische M01-Draft-Angaben in älteren Review-Abschnitten sind durch den verifizierten Merge #34 überholt.

 
## Nachtrag CI-Review vor finalem Docs-Commit

Erster [Run 43516551245](https://github.com/Justify87/Plume_Launcher/actions/runs/43516551245) endete `failure` trotz ausgeführter JVM-Tests: `lintDebug` fand neue fehlerhafte Einrückung im M02-Reset-Catch. Fehler kausal in [834ae544](https://github.com/Justify87/Plume_Launcher/commit/834ae544b3d24a8963842e19a9562d6ce6af07f5) behoben, Lint nicht deaktiviert. Zusätzlich [6b740628](https://github.com/Justify87/Plume_Launcher/commit/6b740628d10e05d92f9c065ff38f42e6371c2cef) und [1a057219](https://github.com/Justify87/Plume_Launcher/commit/1a057219e8afb355fb2e1c6f7cda9c0227e587a0): unmögliche `position > duration` darf nicht als 100%-Progress erscheinen; Test nachgezogen. Zwischen-Runs wurden durch neue Commits teils `cancelled`. **Letzten CI-Run ausschließlich für den endgültigen Branch-/PR-HEAD prüfen**; bei fehlendem Abschluss keinen Pass eintragen.
