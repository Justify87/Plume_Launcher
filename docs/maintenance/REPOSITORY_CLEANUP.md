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

## M02 — active contracts and documentation

**Status:** pending.

## M03 — retired docs and screenshots

**Status:** pending.

## M04 — unused code and resources

**Status:** pending.

## M05 — optional legacy decisions

**Status:** pending.

## M06 — final verification

**Status:** pending.

## Remaining known limitations

A real NVIDIA Shield test, GitHub Pages configuration confirmation, and the byte-identical latest user reference file cannot be inferred from existing files. These are distinct from compile/test evidence. Nothing in this maintenance PR should be represented as having resolved them without supporting evidence.
