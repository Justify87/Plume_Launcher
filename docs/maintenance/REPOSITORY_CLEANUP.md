# Repository cleanup — audit, decisions and milestone reviews

**Repository:** `Justify87/Plume_Launcher` · **target:** `main` · **audit baseline:** `ded8972f02f003a0f8c820d98438a79d286b2d1e` · **date:** 2026-11-15.

This document records what was actually verified during repository maintenance. It is not a product design specification. [The active workspace contract](../v-design/DESIGN_CONTRACT.md), [release instructions](../RELEASING.md), and live source/tests remain authoritative.

## Scope and guardrails

- One maintenance branch and one PR; the main branch is not modified directly.
- Preserve all working launcher, Nuvio, SmartTube, Spotify, wallpaper, screensaver, upload, HDMI, Home, and Mullvad integration paths.
- The Mullvad submodule `third_party/mullvadvpn-app` stays at `aa47f606b1a159214c7279344128119fa91be5fd`; do not change the generated integrated build, application ID, version, signing, or release workflow.
- Git tracks removed history in prior commits: do not relocate old milestones into a new archive directory.
- Do not claim physical Shield or end-to-end VPN testing without observed results.

## M01 — dependency inventory (review)

**Input:** GitHub recursive tree was complete (`truncated=false`): 381 ordinary blobs, 54 directory entries, one Mullvad gitlink, 33,619,092 bytes in ordinary blobs. The tree's root SHA is distinct from the commit SHA; `main` is `ded8972f02f003a0f8c820d98438a79d286b2d1e`.

**Findings and decisions:**

| Candidate | Count | Decision | Rationale / protection |
| --- | ---: | --- | --- |
| `docs/archive/legacy-features/**` | 16 | Remove after M02 | Explicit history; current product doesn't load markdown files |
| `docs/v-design/{DATA_CONTRACT,DECISIONS,HANDOFF,INVENTORY,REVIEWS,ROADMAP,STATE,TEST_MATRIX}.md` | 8 | Remove after M02 | Every document labels itself non-authoritative for the new UI; preserve actual media safety invariants |
| `docs/v-design/REFERENCE.png`, `REFERENCE_SHA256.txt` | 2 | Remove only with reference caveat | Previous M00–M08 image is *not* the latest user reference |
| `docs/readme/**` | 6 | Remove | Old upstream README screenshots; current README uses `ic_launcher_advanced.webp` instead |
| `docs/home-alpine-light-frosted.png`, `docs/home-forest-dark-frosted.png`, `docs/home-moonlit-dark-clear.png` | 3 | Remove | Historical theme screenshots; no found references in active markdown/site |
| `app/src/main/java/dev/plume/HomeFeedStatus.kt` | 1 | Remove after reference check | No production or unit-test reference outside its own file found in previous source audit |
| Selected 16 `res/drawable` entries | 16 | Verify before removal | Static search indicates unused; dynamic Material Symbol `*_fill` lookup in `MainActivity.kt` requires care |
| `docs/index.html`, `privacy.html`, `terms.html`, site CSS/JS/assets | 8 | **Keep for now** | GitHub Pages publication status not verified; pages contain old upstream download URLs |
| `screensaver/*.jpg` | 7 | **Keep** | Valid source material for seven gallery paintings; runtime currently downloads from upstream `gallery-1` |
| `wallpapers/*.webp`, current `docs/v-design/DESIGN_CONTRACT.md`, `docs/workspace/REVIEWS.md` | — | **Keep** | Live asset delivery, design authority and recent workspace implementation reviews |
| `tools/mullvad/**`, `.github/workflows/{pr,ci-integrated,debug-apk,release}.yml` | — | **Keep** | Combined application, PR gate, manual debug, signed release |

**Risks discovered:** (1) the **latest** 1536×864 Expressive reference original is not present as a Git binary; old `REFERENCE.png` must never be substituted; (2) `docs/index.html` advertises the original upstream and downloader code, not this fork; (3) `Gallery.kt` downloads paintings from upstream `gallery-1`, although local `screensaver/` copies exist; (4) `MainActivity.kt` dynamically resolves Material Symbol `_fill` variants.

**M01 review:** Proceed with documentation consolidation first. Static source/manifest/tests/reference searches can establish strong candidates, but an actual CI build is required to accept Kotlin/resource deletions. Do not remove the website, screensaver paintings, tooling, existing V-Design-named runtime classes, or the Mullvad submodule in this PR.

## M02 — active contracts and documentation (review)

**Result:** Added `docs/architecture/ACTIVE_INTEGRATIONS.md` with source-backed Nuvio channel IDs, source-package and intent safeguards, media honesty, pinned Mullvad single-APK architecture, safety/CI invariants and hardware-test boundaries. Replaced README wholesale to reflect merged PR #48 and the actual combined ARM64 debug workflow (rather than a closed PR as a draft). Updated CONTRIBUTING precedence, CHANGELOG/RELEASING archive references and the active design contract's missing-image caveat.

**Review:** Reconciled Nuvio provider keys/package variants with `AndroidTvMediaSource.kt` and `LauncherWorkspaceNavigation.kt`; kept device tests unclaimed and Mullvad source pin unchanged. Current visual reference *cannot* be reconstructed from the old binary or substituted. Obsolete milestone-specific status assertions were deliberately excluded from new active docs. **Pass for documentation consolidation; screenshot artifact remains an explicit follow-up.**

## M03 — retired docs and screenshots (review)

**Removed from the current Git tree:** 16 archived feature documents under `docs/archive/legacy-features/`, 8 old `docs/v-design/` milestone markdown files, two old reference image/checksum files, six old `docs/readme/` images and three unreferenced historical Home screenshots. **Total 35 tracked files, 11,495,343 bytes.** Historical copies remain in Git commit history, not in a replacement archive directory.

**Review and dependency checks:** Up-to-date `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md` and `docs/RELEASING.md` were updated first, and the valid source/package/CI contracts were consolidated into `docs/architecture/ACTIVE_INTEGRATIONS.md`. We intentionally did not remove the old site's `home-dark.jpg`/`home-light.jpg`, site code, ten downloadable wallpapers, gallery paintings, live VDesign-named UI classes, or `VISUAL_REVIEW_2026-11.md`. The latest user screenshot is still **not** in Git, and a device screenshot comparison is **not** asserted. **Source-level deletion review: pass; CI pending until PR.**

## M04 — unused code and resources (review)

Removed `app/src/main/java/dev/plume/HomeFeedStatus.kt` plus these 16 unreferenced drawables: `ic_auto`, `ic_clock`, `ic_launcher`, `ic_lock`, `ic_moon`, `ic_profile`, `ic_remote`, `ic_screensaver`, `ic_shows`, `ic_sun`, `ic_update`, `ic_wallpaper`, `ms_video_file`, `ms_video_file_fill`, `vdesign_calendar`, `vdesign_sparkle`. **17 files, 11,472 bytes.**

**Reference review:** Checked all 43 Plume production Kotlin sources and all 38 unit/instrumentation test sources in the original audit, plus the 10 Mullvad TV Kotlin files and relevant main layout, manifest and string/XML resources: no external references to `HomeFeedStatus`/`HomeFeedEmptyState` or the listed drawable resource names. `MainActivity.kt` dynamically resolves *used* Material Symbol `*_fill` counterparts via `resources.getIdentifier()`: these were **not** deleted. The unused `ms_video_file` icon and its fill twin were assessed together. `ic_launcher_advanced.webp` remains the manifest icon; `banner.xml` remains the TV banner.

**Review:** Removed only a self-contained dead helper and resource-only candidates, with no modifications to active UI, startup, VPN service or migration logic. **Source-level pass; Android resource-link/compilation and R8 review pending final PR CI.**

## M05 — optional legacy decisions (review)

**Decision on old project website:** The live GitHub repository metadata returned `has_pages: false`, `homepage: ""` and there is no `gh-pages` branch. The site still linked to the **original** Plume download, releases and old Downloader code, and its privacy/terms pages described the old product. Therefore deleted its **8** static HTML/CSS/JS/assets files (`docs/index.html`, `docs/privacy.html`, `docs/terms.html`, `docs/style.css`, `docs/theme.js`, `docs/plume-icon.png`, `docs/home-dark.jpg`, `docs/home-light.jpg`; 743,181 bytes). Kept the separate **current fork icon source** `docs/plume-advanced-icon.svg`.

**Protected optional assets:** The seven `screensaver/*.jpg` public-domain painting source files stay; the current `Gallery.kt` downloads `gallery-1` from the old upstream but changing that is a separate functional network migration requiring a verified replacement source. Ten downloadable `wallpapers/*.webp` plus their manifest README stay. Fire TV/installer diagnostic tools are still referenced and remain available.

**M05 review:** The website removal is backed by repository `has_pages=false`, branch inventory and internal reference checks, but cannot prove absence of unknown external bookmarks. Website deletion does not affect Android compilation. No untested gallery/CDN behavior was changed. **Pass, with known upstream-gallery technical debt deferred to a dedicated functional PR.**

## M06 — final verification and release decision (review)

**Final source tree and diff review:** Compared the cleanup branch against `main@ded8972f02f003a0f8c820d98438a79d286b2d1e`. The reviewed candidate set removed **60 tracked files totaling 12,249,996 raw bytes** (M03 35; M04 17; M05 8), and added two active documents (this audit + `docs/architecture/ACTIVE_INTEGRATIONS.md`). No feature code, `AndroidManifest.xml`, `app/build.gradle.kts`, `.gitmodules`, `third_party/mullvadvpn-app` gitlink, `tools/mullvad/**`, or GitHub Actions workflow files were modified.

**Static checks actually performed:**
- Retrieved the branch's complete recursive Git tree and verified archive/site/obsolete assets are absent; the Mullvad gitlink stays pinned to `aa47f606b1a159214c7279344128119fa91be5fd`.
- Scanned all **12** remaining Markdown files for ordinary local relative link targets: **23 local links, 0 broken**. Plain text back-references in this audit intentionally describe removed files.
- Checked the Kotlin/test sources and Mullvad TV UI sources for deleted symbols and drawable names, plus central Android Manifest/layout/values resources. No discovered live reference; the dynamically resolved Material Symbol fill siblings that are actually used remain.
- Confirmed `docs/v-design/DESIGN_CONTRACT.md`, `docs/v-design/VISUAL_REVIEW_2026-11.md`, `docs/workspace/REVIEWS.md`, debug/release playbooks, licensed sources, wallpaper assets, public-domain paintings and app icons remain.
- GitHub metadata explicitly reports Pages disabled. The old website was removed instead of preserving misleading upstream download and privacy pages.
- Kept the `CHANGELOG.md` version `0.1.0` release notes so `app/build.gradle.kts` still has an extractable version section.

**What this review does *not* establish:** No actual `./gradlew testDebugUnitTest lintDebug assembleDebug`, Android resource packaging, R8 combined Mullvad release, or on-device Shield/VPN test was executed locally: the connected GitHub interface is a source/repository interface, not a local Android/NDK/Rust build environment. The GitHub PR's integrated CI is the required executable check; its final HEAD-specific status must be inspected before merge.

**Release decision:** Source and documentation cleanup meets its static objective. **Do not merge until the PR's combined CI is green.** Physical Shield UI, Nuvio installed-variant login, VPN consent/tunnel/kill-switch, artwork, and screenshot visual QA remain separate hardware acceptance items. The latest visual reference original is still missing from Git, and the Gallery painting download uses the inherited upstream URL; both are explicitly deferred, not silently fixed.


## Remaining known limitations

A real NVIDIA Shield test, GitHub Pages configuration confirmation, and the byte-identical latest user reference file cannot be inferred from existing files. These are distinct from compile/test evidence. Nothing in this maintenance PR should be represented as having resolved them without supporting evidence.
