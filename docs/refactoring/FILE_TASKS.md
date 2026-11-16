# M0 – FILE_TASKS

**Baseline:** `main` `e3c1a3eb78c85bb471d9012dd700b6efd81b44ce`; **Inventar aus Git-Tree:** `1807972e170566bcadd7606c7a93de6ce7dcdfe9` auf `refactor/m0-inventory-androidtest-20261116`.

**Wichtige Statusregel:** `OPEN / NOT_YET_FULL_REVIEWED` heißt ohne vollständige semantische Prüfung keine Freigabe. Ein kompletter technischer GitHub-Dateiabruf wird später gesondert als `FULL_FETCH / SEMANTIC_OPEN` vermerkt, niemals automatisch als vollständig fachlich geprüft. `KEEP` ist eine konservative Entscheidung und wird erst nach Prüfung abgeschlossen.

**Selektionsregel:** jede Kotlin-, Gradle-Kotlin-, Python-, Shell-/Wrapper-, ProGuard-/Build-Properties-Datei, aktive CI-Workflows, Manifest, Hauptlayout, XML-Systemkonfiguration und Gitmodules-Pin. Reine Ressourcen, Binärdateien, Issue-Templates und Dokumentation stehen im separaten Asset-Inventar. Fremdes Submodul ist ausgenommen.

| ID | Datei | Blob-SHA | Lesestatus | Verantwortung | Aufrufer / Abhängigkeiten | State / Lifecycle | Befund | Entscheidung | M | Maßnahme / KEEP-Grund | Testnachweis | Task-Status |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| F001 | `.github/workflows/ci-integrated.yml` | `ddd88a2e757a` | OPEN / NOT_YET_FULL_REVIEWED | CI: ci-integrated | OPEN | OPEN | AndroidTest-Kompilation fehlte in CI | **REPLACE** | M0/M7 | AndroidTest-Kompilation zu bestehendem CI-Gate ergänzt | PR-CI: PENDING | CHANGED; CI_PENDING |
| F002 | `.github/workflows/debug-apk.yml` | `bff1db205b98` | OPEN / NOT_YET_FULL_REVIEWED | CI: debug-apk | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | PR-CI: PENDING | OPEN |
| F003 | `.github/workflows/pr.yml` | `1e1772802de3` | OPEN / NOT_YET_FULL_REVIEWED | CI: pr | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | PR-CI: PENDING | OPEN |
| F004 | `.github/workflows/release.yml` | `651975b32e04` | OPEN / NOT_YET_FULL_REVIEWED | CI: release | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | PR-CI: PENDING | OPEN |
| F005 | `.gitmodules` | `508ec73b4cdc` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag:  | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F006 | `app/build.gradle.kts` | `4860f81bd897` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag: build.gradle | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F007 | `app/proguard-rules.pro` | `e520c6c382b4` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag: proguard-rules | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F008 | `app/src/androidTest/java/dev/plume/ActivityTest.kt` | `ecb480bdedf6` | OPEN / NOT_YET_FULL_REVIEWED | Instrumentierung: ActivityTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | compileDebugAndroidTestKotlin, Gerät NOT_TESTED: PENDING | OPEN |
| F009 | `app/src/androidTest/java/dev/plume/AppPicturesCheck.kt` | `85514be2fe15` | OPEN / NOT_YET_FULL_REVIEWED | Instrumentierung: AppPicturesCheck | OPEN | OPEN | veraltete UI-Testbezüge | **REPLACE** | M0 | R.id.appRow/renderApps gegen All-Apps-Grid aktualisiert | compileDebugAndroidTestKotlin, Gerät NOT_TESTED: PENDING | CHANGED; CI_PENDING |
| F010 | `app/src/androidTest/java/dev/plume/GalleryPreviewTest.kt` | `9fe793a17f6c` | OPEN / NOT_YET_FULL_REVIEWED | Instrumentierung: GalleryPreviewTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | compileDebugAndroidTestKotlin, Gerät NOT_TESTED: PENDING | OPEN |
| F011 | `app/src/androidTest/java/dev/plume/MuteCheck.kt` | `bbce260ca504` | OPEN / NOT_YET_FULL_REVIEWED | Instrumentierung: MuteCheck | OPEN | OPEN | noch zu prüfen | **KEEP** | M0 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | compileDebugAndroidTestKotlin, Gerät NOT_TESTED: PENDING | OPEN |
| F012 | `app/src/androidTest/java/dev/plume/SettingsTransferCheck.kt` | `87982b826266` | OPEN / NOT_YET_FULL_REVIEWED | Instrumentierung: SettingsTransferCheck | OPEN | OPEN | noch zu prüfen | **KEEP** | M0 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | compileDebugAndroidTestKotlin, Gerät NOT_TESTED: PENDING | OPEN |
| F013 | `app/src/main/AndroidManifest.xml` | `be547d278e3f` | OPEN / NOT_YET_FULL_REVIEWED | Android Layout/Manifest/Integration: AndroidManifest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F014 | `app/src/main/java/dev/plume/AerialViews.kt` | `c56ff90b3604` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: AerialViews | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F015 | `app/src/main/java/dev/plume/AndroidTvMediaSource.kt` | `7ca4b0eb186a` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: AndroidTvMediaSource | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F016 | `app/src/main/java/dev/plume/ApkProvider.kt` | `d7aaa92178c2` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: ApkProvider | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F017 | `app/src/main/java/dev/plume/AppPictures.kt` | `fecc5c1ab11c` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: AppPictures | OPEN | OPEN | noch zu prüfen | **KEEP** | M2 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F018 | `app/src/main/java/dev/plume/Bing.kt` | `788d24572c40` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: Bing | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F019 | `app/src/main/java/dev/plume/FittingLabel.kt` | `b9669be8dacd` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: FittingLabel | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F020 | `app/src/main/java/dev/plume/Gallery.kt` | `205c95da24af` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: Gallery | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F021 | `app/src/main/java/dev/plume/Glass.kt` | `4d29f2e74dae` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: Glass | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F022 | `app/src/main/java/dev/plume/HomeOverride.kt` | `166987fef8ea` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: HomeOverride | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F023 | `app/src/main/java/dev/plume/HomeWatch.kt` | `08386fcf9c03` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: HomeWatch | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F024 | `app/src/main/java/dev/plume/InputActivity.kt` | `c72075522d42` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: InputActivity | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F025 | `app/src/main/java/dev/plume/LauncherWorkspaceNavigation.kt` | `33e78e2515b2` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: LauncherWorkspaceNavigation | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F026 | `app/src/main/java/dev/plume/MainActivity.kt` | `d3de3735a2ee` | OPEN / NOT_YET_FULL_REVIEWED | Launcher, Apps, Settings, Medien, System und Lifecycle | OPEN | OPEN | noch zu prüfen | **EXTRACT** | M1–M5 | M1 State-/Thread-Vertrag; erst M2–M5 gezielt extrahieren | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F027 | `app/src/main/java/dev/plume/MediaFeedRepository.kt` | `d2fbec616eab` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: MediaFeedRepository | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F028 | `app/src/main/java/dev/plume/MediaItem.kt` | `9c92afed42c0` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: MediaItem | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F029 | `app/src/main/java/dev/plume/NuvioDashboardFeed.kt` | `c89ebee84ea9` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: NuvioDashboardFeed | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F030 | `app/src/main/java/dev/plume/OptionArt.kt` | `791d3ef68c3a` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: OptionArt | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F031 | `app/src/main/java/dev/plume/Qr.kt` | `1d426c27add2` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: Qr | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F032 | `app/src/main/java/dev/plume/Remap.kt` | `31529a2163ac` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: Remap | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F033 | `app/src/main/java/dev/plume/ScreensaverTiming.kt` | `aef6aea78080` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: ScreensaverTiming | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F034 | `app/src/main/java/dev/plume/SettingsTransfer.kt` | `a76e20e065a1` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: SettingsTransfer | OPEN | OPEN | noch zu prüfen | **KEEP** | M2 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F035 | `app/src/main/java/dev/plume/ShowsWall.kt` | `592ec1819518` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: ShowsWall | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F036 | `app/src/main/java/dev/plume/SnapScrollView.kt` | `9440bb25adf1` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: SnapScrollView | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F037 | `app/src/main/java/dev/plume/Sun.kt` | `2ca34344f724` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: Sun | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F038 | `app/src/main/java/dev/plume/TopBar.kt` | `cb233b4e70ca` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: TopBar | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F039 | `app/src/main/java/dev/plume/VDesignCanvas.kt` | `7dfb61d06089` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VDesignCanvas | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F040 | `app/src/main/java/dev/plume/VDesignCardSlots.kt` | `6235d30a2bdd` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VDesignCardSlots | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F041 | `app/src/main/java/dev/plume/VDesignCardText.kt` | `c38bbb98f969` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VDesignCardText | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F042 | `app/src/main/java/dev/plume/VDesignCardView.kt` | `f3ff6461f08c` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VDesignCardView | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F043 | `app/src/main/java/dev/plume/VDesignFocusNavigator.kt` | `e1ab95e69e5c` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VDesignFocusNavigator | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F044 | `app/src/main/java/dev/plume/VDesignHomeView.kt` | `4b2a2c37252f` | OPEN / NOT_YET_FULL_REVIEWED | Rail/Panes/Fokus und Medienkarten | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F045 | `app/src/main/java/dev/plume/VDesignLaunchPolicy.kt` | `cdd9edf061d2` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VDesignLaunchPolicy | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F046 | `app/src/main/java/dev/plume/VDesignPosterDecodePolicy.kt` | `42cd5dfe51c8` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VDesignPosterDecodePolicy | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F047 | `app/src/main/java/dev/plume/VideoWallpaper.kt` | `2be4d32dd9da` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VideoWallpaper | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F048 | `app/src/main/java/dev/plume/WallpaperPack.kt` | `6e75f60c31eb` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: WallpaperPack | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F049 | `app/src/main/java/dev/plume/WallpaperUpload.kt` | `b0f174a8a538` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: WallpaperUpload | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F050 | `app/src/main/java/dev/plume/WorkspaceAppTileView.kt` | `25d95cdfd6d8` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: WorkspaceAppTileView | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F051 | `app/src/main/java/dev/plume/WorkspaceGenericMediaPane.kt` | `d29c3e6ffd78` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: WorkspaceGenericMediaPane | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F052 | `app/src/main/java/dev/plume/WorkspaceGridPolicy.kt` | `283dc58bb3d5` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: WorkspaceGridPolicy | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F053 | `app/src/main/java/dev/plume/WorkspaceMediaPane.kt` | `e498c31595ab` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: WorkspaceMediaPane | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F054 | `app/src/main/java/dev/plume/YouTubeRssSource.kt` | `59818b031f89` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: YouTubeRssSource | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F055 | `app/src/main/java/dev/plume/YouTubeSubscriptions.kt` | `39652daacebe` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: YouTubeSubscriptions | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F056 | `app/src/main/res/layout/activity_main.xml` | `56260c62feda` | OPEN / NOT_YET_FULL_REVIEWED | Android Layout/Manifest/Integration: activity_main | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F057 | `app/src/main/res/xml/home_override.xml` | `4d2fbaf313e5` | OPEN / NOT_YET_FULL_REVIEWED | Android Layout/Manifest/Integration: home_override | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F058 | `app/src/main/res/xml/network_security_config.xml` | `e3673af093fe` | OPEN / NOT_YET_FULL_REVIEWED | Android Layout/Manifest/Integration: network_security_config | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F059 | `app/src/test/java/dev/plume/AndroidTvMediaSourceTest.kt` | `8725c5a062d5` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: AndroidTvMediaSourceTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F060 | `app/src/test/java/dev/plume/ContinueWatchingTest.kt` | `f917be32d5bb` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: ContinueWatchingTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F061 | `app/src/test/java/dev/plume/FittingLabelTest.kt` | `e7c429df6e32` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: FittingLabelTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F062 | `app/src/test/java/dev/plume/GlassTest.kt` | `a748aba8638c` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: GlassTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F063 | `app/src/test/java/dev/plume/HdmiInputsTest.kt` | `9baaaf1960b8` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: HdmiInputsTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F064 | `app/src/test/java/dev/plume/HomeWatchTest.kt` | `be30b5a74606` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: HomeWatchTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F065 | `app/src/test/java/dev/plume/IconStyleTest.kt` | `6e295fef606a` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: IconStyleTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F066 | `app/src/test/java/dev/plume/InkTest.kt` | `6c3ed6f0ddd2` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: InkTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F067 | `app/src/test/java/dev/plume/LauncherAppsTest.kt` | `d88376c7ee46` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: LauncherAppsTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F068 | `app/src/test/java/dev/plume/LauncherWorkspaceNavigationTest.kt` | `13c6cb55a65f` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: LauncherWorkspaceNavigationTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F069 | `app/src/test/java/dev/plume/MediaFeedRepositoryTest.kt` | `016303c90bf5` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: MediaFeedRepositoryTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F070 | `app/src/test/java/dev/plume/NuvioDashboardFeedTest.kt` | `c066e9c0d1e8` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: NuvioDashboardFeedTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F071 | `app/src/test/java/dev/plume/NuvioProviderTest.kt` | `acd9ab2c6978` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: NuvioProviderTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F072 | `app/src/test/java/dev/plume/PinTest.kt` | `05ff3d80a9c8` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: PinTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F073 | `app/src/test/java/dev/plume/QrTest.kt` | `21142bffece5` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: QrTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F074 | `app/src/test/java/dev/plume/RebootSetupAndWallpaperTest.kt` | `036352a83e31` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: RebootSetupAndWallpaperTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F075 | `app/src/test/java/dev/plume/RemapTest.kt` | `0d81279ef71f` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: RemapTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F076 | `app/src/test/java/dev/plume/ScreensaverTimingTest.kt` | `b438912d109a` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: ScreensaverTimingTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F077 | `app/src/test/java/dev/plume/SettingsTransferTest.kt` | `cbfeb665fbf6` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: SettingsTransferTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M2 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F078 | `app/src/test/java/dev/plume/ShowsWallTest.kt` | `8180dc8c67f3` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: ShowsWallTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F079 | `app/src/test/java/dev/plume/SunTest.kt` | `3b37c78847c3` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: SunTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F080 | `app/src/test/java/dev/plume/TopBarTest.kt` | `e0a1a84aa6f1` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: TopBarTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F081 | `app/src/test/java/dev/plume/VDesignCanvasTest.kt` | `769a7d17ef95` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: VDesignCanvasTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F082 | `app/src/test/java/dev/plume/VDesignCardSlotsTest.kt` | `747cc80d94cc` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: VDesignCardSlotsTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F083 | `app/src/test/java/dev/plume/VDesignCardTextTest.kt` | `e7a88092b067` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: VDesignCardTextTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F084 | `app/src/test/java/dev/plume/VDesignFocusNavigatorTest.kt` | `c9e706bc92e4` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: VDesignFocusNavigatorTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F085 | `app/src/test/java/dev/plume/VDesignLaunchPolicyTest.kt` | `ecad94a09da9` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: VDesignLaunchPolicyTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F086 | `app/src/test/java/dev/plume/VDesignPosterDecodePolicyTest.kt` | `e3805a4c907e` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: VDesignPosterDecodePolicyTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F087 | `app/src/test/java/dev/plume/VersionTest.kt` | `19c134e7bd72` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: VersionTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F088 | `app/src/test/java/dev/plume/VideoScenesTest.kt` | `c19ed8678644` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: VideoScenesTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F089 | `app/src/test/java/dev/plume/WallpaperPackTest.kt` | `9767ecf7b5d0` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: WallpaperPackTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M4 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F090 | `app/src/test/java/dev/plume/WorkspaceGridPolicyTest.kt` | `a1fb5f56121e` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: WorkspaceGridPolicyTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M5 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F091 | `app/src/test/java/dev/plume/YouTubeSubscriptionsTest.kt` | `b91c75903793` | OPEN / NOT_YET_FULL_REVIEWED | JVM-Regression: YouTubeSubscriptionsTest | OPEN | OPEN | noch zu prüfen | **KEEP** | M3 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest: PENDING | OPEN |
| F092 | `build.gradle.kts` | `fd4b88199ff5` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag: build.gradle | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F093 | `gradle.properties` | `2ddb7266bd6e` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag: gradle | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F094 | `gradle/wrapper/gradle-wrapper.properties` | `c61a118f7ddb` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag: gradle-wrapper | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F095 | `gradlew` | `ef07e0162b18` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag: gradlew | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F096 | `gradlew.bat` | `db3a6ac207e5` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag: gradlew | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F097 | `settings.gradle.kts` | `fcc6bc875745` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag: settings.gradle | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F098 | `tools/lang.py` | `bdf7aec2d969` | OPEN / NOT_YET_FULL_REVIEWED | Python-Test-/Hilfsskript: lang | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F099 | `tools/mullvad/MullvadEmbeddedPanel.kt` | `7730cf833644` | OPEN / NOT_YET_FULL_REVIEWED | Embedded VPN UI/Consent/Host-Lifecycle | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F100 | `tools/mullvad/VpnAccountUi.kt` | `0ca6bc1960e9` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VpnAccountUi | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F101 | `tools/mullvad/VpnActivity.kt` | `c6af67e94700` | OPEN / NOT_YET_FULL_REVIEWED | VPN-Einstiegsactivity und eigener Lifecycle | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F102 | `tools/mullvad/VpnAdvancedUi.kt` | `b45e884245ac` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VpnAdvancedUi | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F103 | `tools/mullvad/VpnDiagnosticsUi.kt` | `788b1d8467e0` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VpnDiagnosticsUi | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F104 | `tools/mullvad/VpnLocationUi.kt` | `34549bf0d2a7` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VpnLocationUi | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F105 | `tools/mullvad/VpnNetworkUi.kt` | `689cb0e1c6b6` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VpnNetworkUi | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F106 | `tools/mullvad/VpnSettingsUi.kt` | `8a889571a348` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VpnSettingsUi | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F107 | `tools/mullvad/VpnSplitTunnelingUi.kt` | `0f394ecf772d` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VpnSplitTunnelingUi | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F108 | `tools/mullvad/VpnTvStyle.kt` | `0fcd6ba4854f` | OPEN / NOT_YET_FULL_REVIEWED | Launcher-/Mullvad-Code: VpnTvStyle | OPEN | OPEN | noch zu prüfen | **KEEP** | M6 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F109 | `tools/mullvad/build-integrated.sh` | `9a485c2e7a5a` | OPEN / NOT_YET_FULL_REVIEWED | Shell-Build-/Diagnose: build-integrated | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F110 | `tools/mullvad/check_apk.py` | `c1060f838731` | OPEN / NOT_YET_FULL_REVIEWED | Python-Test-/Hilfsskript: check_apk | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F111 | `tools/mullvad/prepare.py` | `629380a04857` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Staging-Vorbereitung | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | Staging-Tests + integrierte CI: PENDING | OPEN |
| F112 | `tools/plume-firetv-probe.sh` | `ba8fdbb66d39` | OPEN / NOT_YET_FULL_REVIEWED | Shell-Build-/Diagnose: plume-firetv-probe | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F113 | `tools/plume-install-test/adb` | `d7309b232dbb` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Wrapper-/Konfigurationsvertrag: adb | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F114 | `tools/plume-install-test/run.sh` | `9b1cc581f743` | OPEN / NOT_YET_FULL_REVIEWED | Shell-Build-/Diagnose: run | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F115 | `tools/plume-install.sh` | `454a0d5931e6` | OPEN / NOT_YET_FULL_REVIEWED | Shell-Build-/Diagnose: plume-install | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F116 | `tools/pr-rules.sh` | `3c8d3e38aba2` | OPEN / NOT_YET_FULL_REVIEWED | Shell-Build-/Diagnose: pr-rules | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F117 | `tools/release/prepare.py` | `d4ebe89848e1` | OPEN / NOT_YET_FULL_REVIEWED | Build-/Staging-Vorbereitung | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F118 | `tools/release/signing_preflight.py` | `d79b8641668d` | OPEN / NOT_YET_FULL_REVIEWED | Python-Test-/Hilfsskript: signing_preflight | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F119 | `tools/spotify_podcast_probe.py` | `aee712510d5b` | OPEN / NOT_YET_FULL_REVIEWED | Python-Test-/Hilfsskript: spotify_podcast_probe | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F120 | `tools/test_embedded_mullvad_consolidation.py` | `dd03234a0040` | OPEN / NOT_YET_FULL_REVIEWED | Python-Test-/Hilfsskript: test_embedded_mullvad_consolidation | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F121 | `tools/test_mullvad_apk.py` | `3b7df77cde39` | OPEN / NOT_YET_FULL_REVIEWED | Python-Test-/Hilfsskript: test_mullvad_apk | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F122 | `tools/test_mullvad_prepare.py` | `a747c1767b6f` | OPEN / NOT_YET_FULL_REVIEWED | Python-Test-/Hilfsskript: test_mullvad_prepare | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |
| F123 | `tools/test_spotify_podcast_probe.py` | `edf10d19debb` | OPEN / NOT_YET_FULL_REVIEWED | Python-Test-/Hilfsskript: test_spotify_podcast_probe | OPEN | OPEN | noch zu prüfen | **KEEP** | M0/M7 | Kein Refactoring ohne fachlich belegte Grenze; KEEP vorläufig | testDebugUnitTest/lintDebug/assembleDebug + betroffene Checks: PENDING | OPEN |

## Mechanischer Inventarabgleich

- Git-Tree nicht abgeschnitten: **323** reguläre Dateien, **123** eigene Code-/Konfigurations-Tasks, **200** sonstige Dateien.
- Eindeutige Pfade / Datei-Tasks: **123/123**. Fehlend oder doppelt laut Selektionsregel: **0**.
- **M0-Freigabe: OFFEN.** Alle OPEN-Zeilen benötigen lückenlose Quelllektüre, Befund/Abhängigkeiten/State, Tests und erneuten Abgleich.

## Inventar der Nicht-Code-Dateien

Diese Dateien sind bewusst keine Refactoring-Tasks; jede ist mit Pfad/Blob erfasst. `ms_*_fill` kann dynamisch referenziert werden.

| Pfad | Blob-SHA | Einordnung |
|---|---|---|
| `.gitattributes` | `b0ec781fbe8d` | INVENTORIED / nur bei Refactoring-Bezug |
| `.github/ISSUE_TEMPLATE/bug_report.yml` | `1b1089aeb0b3` | INVENTORIED / nur bei Refactoring-Bezug |
| `.github/ISSUE_TEMPLATE/config.yml` | `bc3e9e5cbb31` | INVENTORIED / nur bei Refactoring-Bezug |
| `.github/ISSUE_TEMPLATE/feature_request.yml` | `80660fd3514a` | INVENTORIED / nur bei Refactoring-Bezug |
| `.github/pull_request_template.md` | `8e747f1d2ae1` | INVENTORIED / nur bei Refactoring-Bezug |
| `.gitignore` | `2fc1b1497f24` | INVENTORIED / nur bei Refactoring-Bezug |
| `CHANGELOG.md` | `cf60c7c52416` | INVENTORIED / nur bei Refactoring-Bezug |
| `CONTRIBUTING.md` | `54ad45dff25f` | INVENTORIED / nur bei Refactoring-Bezug |
| `LICENSE` | `53d1f3d01864` | INVENTORIED / nur bei Refactoring-Bezug |
| `README.md` | `afdc607f0308` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/lint-baseline.xml` | `5b00bd55a78f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable-nodpi/ic_launcher_advanced.webp` | `767c9615835e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable-nodpi/wallpaper_aurora.webp` | `6f615f869d92` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/banner.xml` | `d4a1d488d5be` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_apps.xml` | `68d4c3d03c0d` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_ethernet.xml` | `7c3f8dcf7086` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_hdmi.xml` | `d3cccfd0db3e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_home.xml` | `84d3d9f6e082` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_playback.xml` | `bda354820208` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_power.xml` | `af673c2acdfc` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_settings.xml` | `87e5c07cfa88` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_settop.xml` | `082a1420e148` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_sources.xml` | `d67835bb2b10` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_tune.xml` | `91d3278cfff5` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_vpn.xml` | `a6462ebb050a` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ic_wifi.xml` | `2177623ae178` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_appletv.xml` | `7b4b337ada3e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_chromecast.xml` | `da2707374bd6` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_googleplay.xml` | `42f74e70308f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_netflix.xml` | `25f8a3f8acfa` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_nintendoswitch.xml` | `ae496b737411` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_nvidia.xml` | `2952317661d8` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_playstation.xml` | `bb2fec75679e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_primevideo.xml` | `660b8c3eea98` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_spotify.xml` | `3565128f2c7e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_stremio.xml` | `a5a725b16fb7` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_vlcmediaplayer.xml` | `66de4209dc3a` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_xbox.xml` | `8a9391b72d35` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_youtube.xml` | `cc498b7b4d4b` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/logo_youtubemusic.xml` | `4ff6931f91d2` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_add_photo_alternate.xml` | `2559c95e710f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_add_photo_alternate_fill.xml` | `c5822cc71055` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_apps.xml` | `c617e66ad963` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_apps_fill.xml` | `75222f75577f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_badge.xml` | `e0d44680b1d5` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_badge_fill.xml` | `4840c9053af4` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_bedtime.xml` | `1181eb315a15` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_bedtime_fill.xml` | `227dfbf468ad` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_blur_on.xml` | `56cde0b69ba5` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_blur_on_fill.xml` | `6a2aee8b4a8f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_check.xml` | `d9d9e2c2b0cc` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_check_fill.xml` | `d33a0660a85f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_checklist.xml` | `5708876b9724` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_checklist_fill.xml` | `30a9f46dcf5f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_chevron_left.xml` | `b10fc2f66147` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_chevron_left_fill.xml` | `72668c99b0b6` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_chevron_right.xml` | `3d52594035b3` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_chevron_right_fill.xml` | `8b2d9b2a44fa` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_cloud_fill.xml` | `051bef3efda2` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_code.xml` | `b9d1d8c50e50` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_code_fill.xml` | `5c999506fe30` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_contrast.xml` | `b7b599acfc38` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_contrast_fill.xml` | `222e35abe77c` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_dark_mode.xml` | `c1ee24925032` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_dark_mode_fill.xml` | `25022b51f745` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_delete.xml` | `a8dcd8a4fe4d` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_delete_fill.xml` | `479a4cc72eaa` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_edit.xml` | `1906ff92ee07` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_edit_fill.xml` | `6fe4687f18c5` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_filter_list.xml` | `48a2013f0c84` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_filter_list_fill.xml` | `11fffff25ef2` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_foggy_fill.xml` | `daf75dc48fb2` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_folder.xml` | `51017ab76350` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_folder_fill.xml` | `89f6ba1f1d64` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_forest.xml` | `101edee955d7` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_forest_fill.xml` | `84a382091675` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_format_align_left.xml` | `1adef7ad4987` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_format_align_left_fill.xml` | `58aca7cc50a7` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_format_size.xml` | `cb1acd07359a` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_format_size_fill.xml` | `33d2771cf53f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_home.xml` | `843f1c79d036` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_home_app_logo.xml` | `bfaee476f270` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_home_app_logo_fill.xml` | `b2bd0b332e2c` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_home_fill.xml` | `778a509d47c6` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_keyboard.xml` | `07576fd81ddf` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_keyboard_fill.xml` | `cecf45c89cdc` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_label.xml` | `e82964d4a2f0` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_label_fill.xml` | `4fd30b60df5a` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_landscape.xml` | `4cfae02bdda4` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_landscape_fill.xml` | `c29eca59d075` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_light_mode.xml` | `38c8c65d022e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_light_mode_fill.xml` | `4c8c752013e5` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_local_fire_department.xml` | `08b49def40ac` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_local_fire_department_fill.xml` | `e0be93aba64c` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_location_city.xml` | `10debda605b3` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_location_city_fill.xml` | `59f3a2452e5a` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_location_on.xml` | `7f60fa5b2cd8` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_location_on_fill.xml` | `193924a02c7e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_lock.xml` | `e60f9e8dd2d3` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_lock_fill.xml` | `01ebc66f6ff4` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_movie.xml` | `ce710a8456a4` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_movie_fill.xml` | `d6eae7ed2836` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_open_in_full.xml` | `34678350811e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_open_in_full_fill.xml` | `33a56e4578dd` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_open_in_new.xml` | `e76c115384c1` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_open_in_new_fill.xml` | `3245c74b7ba9` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_palette.xml` | `cecb20c6c32e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_palette_fill.xml` | `540ea703ee21` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_partly_cloudy_day.xml` | `3700f4671d3b` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_partly_cloudy_day_fill.xml` | `4f5b84e4ed26` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_partly_cloudy_night_fill.xml` | `5b115c3f0baa` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_photo_library.xml` | `076a8a496866` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_photo_library_fill.xml` | `44344fc358bf` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_play_circle.xml` | `4b914f914f48` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_play_circle_fill.xml` | `4209344c8930` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_podcasts.xml` | `071866c741f7` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_podcasts_fill.xml` | `e5e10412d23d` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_public.xml` | `fb3e9b850325` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_public_fill.xml` | `55d13db96a3d` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_rainy_fill.xml` | `f42a0edc5a7d` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_restart_alt.xml` | `fda09bb87390` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_restart_alt_fill.xml` | `104619d053ad` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_routine.xml` | `da54709cd0ed` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_routine_fill.xml` | `998cce674f0c` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_schedule.xml` | `01b054d80838` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_schedule_fill.xml` | `bcc7fdbbe56a` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_settings_remote.xml` | `0269d8b78e59` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_settings_remote_fill.xml` | `c96aae405a4a` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_shuffle.xml` | `e1bee8f9a733` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_shuffle_fill.xml` | `437865b0174a` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_sort.xml` | `88d094e64ec3` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_sort_fill.xml` | `3eef3d130ff0` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_swap_horiz.xml` | `a46c0032ba53` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_swap_horiz_fill.xml` | `0a33c3d11c58` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_system_update.xml` | `e84a16c63b91` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_system_update_fill.xml` | `6fbf94496005` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_texture.xml` | `45d9a1f65214` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_texture_fill.xml` | `b91adeda58d1` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_thermostat.xml` | `52a08861023f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_thermostat_fill.xml` | `1b4cbd8a71c5` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_thunderstorm_fill.xml` | `d288f86a1aeb` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_timer.xml` | `390e962969eb` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_timer_fill.xml` | `70540f52795b` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_vertical_align_bottom.xml` | `6c56cbd4e68d` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_vertical_align_bottom_fill.xml` | `3d61577f5c14` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_video_library.xml` | `d9bd6e679893` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_video_library_fill.xml` | `78013df671a9` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_view_carousel.xml` | `516e9f7eafa7` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_view_carousel_fill.xml` | `e91014acceee` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_visibility.xml` | `890c4181669a` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_visibility_fill.xml` | `867ddd524daf` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_visibility_off.xml` | `b20effa1b05d` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_visibility_off_fill.xml` | `117f60ddb2fd` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_volume_up.xml` | `378e2c0f7353` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_volume_up_fill.xml` | `3a714525c314` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_wallpaper.xml` | `7e2942e8bf5f` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_wallpaper_fill.xml` | `18214c556608` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_water.xml` | `cce44d2522e0` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_water_fill.xml` | `b4a2ac924923` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_weather_snowy_fill.xml` | `9ae4f583b440` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_widgets.xml` | `01f7381d1ba7` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/drawable/ms_widgets_fill.xml` | `39d3bc2f2f45` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/font/clock_serif.ttf` | `f76fe6ee7a80` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/raw/apple_root_ca.cer` | `8a9ff247419d` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/raw/video_scenes.gz` | `5db61d11bd24` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/values-ar/strings.xml` | `de68b92a66ce` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/values-de/strings.xml` | `ac166139443e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/values-es/strings.xml` | `7de9a53270e0` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/values-fr/strings.xml` | `a1c1d348add5` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/values-it/strings.xml` | `a8ba752fd652` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/values-pt-rBR/strings.xml` | `ed86344fd53e` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/values-tr/strings.xml` | `70aa96238092` | INVENTORIED / nur bei Refactoring-Bezug |
| `app/src/main/res/values/strings.xml` | `f12524ccb48e` | INVENTORIED / nur bei Refactoring-Bezug |
| `docs/DEBUG_BUILDS.md` | `8d1beca79f74` | INVENTORIED / nur bei Refactoring-Bezug |
| `docs/RELEASING.md` | `bc9a7d4c7e34` | INVENTORIED / nur bei Refactoring-Bezug |
| `docs/architecture/ACTIVE_INTEGRATIONS.md` | `c1e4ebec814f` | INVENTORIED / nur bei Refactoring-Bezug |
| `docs/maintenance/REPOSITORY_CLEANUP.md` | `27fa079b383c` | INVENTORIED / nur bei Refactoring-Bezug |
| `docs/plume-advanced-icon.svg` | `6a0304a09d29` | INVENTORIED / nur bei Refactoring-Bezug |
| `docs/v-design/DESIGN_CONTRACT.md` | `1baa2459b8d5` | INVENTORIED / nur bei Refactoring-Bezug |
| `docs/v-design/VISUAL_REVIEW_2026-11.md` | `6f4acb1c7f26` | INVENTORIED / nur bei Refactoring-Bezug |
| `docs/workspace/REVIEWS.md` | `1e4a9a194459` | INVENTORIED / nur bei Refactoring-Bezug |
| `gradle/wrapper/gradle-wrapper.jar` | `8bdaf60c75ab` | INVENTORIED / nur bei Refactoring-Bezug |
| `screensaver/andes.jpg` | `8f821dfa2d8a` | INVENTORIED / nur bei Refactoring-Bezug |
| `screensaver/chimborazo.jpg` | `cf7298282fd1` | INVENTORIED / nur bei Refactoring-Bezug |
| `screensaver/cotopaxi.jpg` | `531f92c3e842` | INVENTORIED / nur bei Refactoring-Bezug |
| `screensaver/hudson.jpg` | `102352b52e14` | INVENTORIED / nur bei Refactoring-Bezug |
| `screensaver/icebergs.jpg` | `a280ba2d8bbc` | INVENTORIED / nur bei Refactoring-Bezug |
| `screensaver/rye.jpg` | `56e270240759` | INVENTORIED / nur bei Refactoring-Bezug |
| `screensaver/thunderstorm.jpg` | `c80ed80fc636` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/01_Alpine_Dawn.webp` | `3061e98683e4` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/02_Forest_Mist.webp` | `8d4fe22bb817` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/03_Coastal_Dusk.webp` | `bbae41205924` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/04_Autumn_Canopy.webp` | `02c8d542f4a9` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/05_Arctic_Aurora.webp` | `fb288f54f213` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/06_Sand_Dunes.webp` | `ca045a3f6808` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/07_Aerial_River.webp` | `f69525772b46` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/08_Ocean_Reef.webp` | `ece24f4c47ae` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/09_Cherry_Blossom.webp` | `08ae8e7efd1c` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/10_Moonlit_Ridge.webp` | `c65514d5ce00` | INVENTORIED / nur bei Refactoring-Bezug |
| `wallpapers/README.md` | `6c8a303bdc34` | INVENTORIED / nur bei Refactoring-Bezug |

## Externes Submodul

- `third_party/mullvadvpn-app` @ `aa47f606b1a159214c7279344128119fa91be5fd` – keine eigene Codeänderung.
