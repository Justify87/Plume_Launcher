# Contributing to Plume Launcher Advanced

This is **Justify87's independent hobby fork**, not the original Plume project's
contribution policy. Its goal is a visually precise, expressive and usable
Android TV launcher with integrated Mullvad VPN.

## Active product and design rules

1. **The current user-supplied 1536 × 864 screenshot is the source of truth for
   the Home layout**, not the old M00–M07 V-Design mockup, upstream Plume styles,
   screenshots in the historical README, or old archival milestones. See
   [the active design contract](docs/v-design/DESIGN_CONTRACT.md).
2. **Prefer visual fidelity and remote usability over legacy implementation
   choices.** Refactor or replace existing Views/XML/layout architecture when
   necessary. No previous grid, focus policy, animation, theme, panel width,
   color, corner radius or legacy visual component is protected merely because
   it already exists.
3. **Modern libraries are allowed.** AndroidX, Material Components for Android,
   animation, image-loading or layout libraries, and other maintained,
   reasonably sized dependencies are permitted when they materially improve
   the result. Kotlin/Views/XML remain convenient, **not mandatory**; Compose
   is an option if its Android-TV performance, support and integration costs
   are acceptable. A rewrite for its own sake is not required.
4. **Budget is a constraint, not an ideology.** Favor simple, readable,
   maintainable code; avoid heavyweight frameworks, duplicated VPN
   implementations and expensive background activity without clear benefit.
   No fixed APK-size/RAM ceilings or categorical dependency bans apply.
5. **Optimize for a ten-foot interface.** Distinct directional focus,
   predictable Back/Home behavior, compact and legible settings, proper
   scrolling and stable focus restore matter more than phone-style UI
   conventions. Animations should be short, smooth and respect accessibility
   settings.
6. **Real content only.** Poster art, metadata, progress, dates and VPN
   connectivity/status originate from actual sources. Do not embed the
   example series/illustrations from the reference image or invent account,
   episode or network status.

## Scope and architecture

A cohesive redesign **may span all UI files and menus in one pull request**.
A feature issue and small isolated PRs are recommendations, not gates: a
single cohesive change is easier to assess when it is reviewed and tested as
one unit. Existing documentation, theme/Glass code, layout conventions and
original Plume PR guidelines may be superseded.

Do not delete working features merely to simplify the redesign: All Apps,
Nuvio preview channels, external apps, HDMI sources, wallpaper, launcher
role/accessibility setup, PIN and backups, and real Mullvad account/VPN
functions must remain reachable. Update tests, screenshots, instructions and
documentation to describe the **current** UI.

The TV launcher and Mullvad are integrated into one APK. The original Mullvad
repositories/services/daemon remain authoritative for tunnel, account,
relay, DNS and split-tunneling state. UI refactors must not silently alter
VPN routing, disable kill-switch protections or misrepresent actual status.
Do not change the pinned upstream submodule or Rust/JNI/VpnService behavior
without a separate technical justification, test evidence and explicit review.

## Safety and privacy rules that remain binding

- No ads, telemetry, analytics or secret account-data collection.
- Never commit passwords, API tokens, signing keys, account numbers or
  private VPN connection material.
- Review new Android permissions, networking, native binaries and build
  scripts for security and privacy impact.
- Keep the approved integrated Mullvad CI, signing/release gates and VPN
  regression checks; do not bypass failures just to make a design PR green.
- No fictitious emulator/Shield acceptance. State clearly what ran and what
  remains to be verified on physical devices.

## Review, testing and contribution process

For UI changes, describe the reference screenshot used, which screens
changed, focus/Back behavior, notable dependency/performance tradeoffs,
and show a screenshot or video from a working emulator/TV when possible.
Run the existing JVM, Android lint and combined Plume+Mullvad checks.
Report failures honestly. The maintainer decides when to merge; a successful
compile does not establish pixel-exact appearance on a Shield.

Third-party dependencies are reviewed for maintenance, licensing, size and
supply chain; their presence is **not** a reason to reject a PR.

Please keep the existing GPL-3.0 licensing and upstream credits.
For translations, edit `app/src/main/res/values-<code>/strings.xml`,
preserve placeholders (e.g. `%1$s`, `%d`) and use
`python3 tools/lang.py check` to catch mismatches.
For setup and diagnostics, see `tools/plume-install.sh` and
`tools/plume-firetv-probe.sh`; when filing bugs include TV model, Android
version, launcher version and reproduction steps.

**Precedence for new UI development:** this file and
`docs/v-design/DESIGN_CONTRACT.md` supersede contradictory legacy notes in
`docs/v-design/{ROADMAP,STATE,HANDOFF,REVIEWS,DECISIONS,INVENTORY,TEST_MATRIX,DATA_CONTRACT}.md`
and archived upstream contribution text. Historical evidence stays available
for traceability but is not an active design restriction.
