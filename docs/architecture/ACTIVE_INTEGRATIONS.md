# Active integration invariants (source-oriented)

This document consolidates the still-valid **technical** rules from historical Plume/Mullvad/Nuvio milestone notes. It is deliberately **not** a replacement UI layout contract: [the persistent-workspace design contract](../v-design/DESIGN_CONTRACT.md) takes precedence for visual/navigation decisions. Implementation and tests take precedence over historical status claims.

## Android TV and Nuvio media channels

Canonical Kotlin: `app/src/main/java/dev/plume/AndroidTvMediaSource.kt`, `NuvioDashboardFeed.kt`, `VDesignLaunchPolicy.kt`, `LauncherWorkspaceNavigation.kt`, `MainActivity.kt`.

Nuvio publishes an optional legacy mixed continue feed plus three categorised Preview channels. They are distinguished **only** by the owning package and `TvContract.Channels.COLUMN_INTERNAL_PROVIDER_ID`:

| Provider key relative to actual installed package `pkg` | Category | Treatment |
| --- | --- | --- |
| `pkg` | Legacy mixed/continue | Retain existing fallback rules; never treat all entries as released episodes |
| `pkg:in-progress` | In progress | Only evidence-backed watch progress; no fake durations |
| `pkg:available` | Available next episodes | Release availability must be publisher-backed |
| `pkg:upcoming` | Upcoming | Preserve civil `yyyy-MM-dd` in user's local timezone |

Safety and data contract:
- Query channel entries only with the necessary Android TV listings/provider permissions and the platform's visibility rules; require a legitimate, browsable channel and exact provider key.
- A channel's `package_name` is its content publisher. Do **not** infer package identity from a localized channel title, and do not mix a program's internal ID with the channel provider ID.
- Support real Nuvio installation variants, including `com.nuvio.tv`, `com.nuviodebug.com`, `com.nuvio.app`, `com.nuvio.appdebug`, `com.nuvio.tv.debug`, `com.nuvio.app.debug` and the historical `com.justify87.nuviotv.wireguard*` variants currently whitelisted in source.
- When more than one variant is installed, **prefer the package that published the channel**; never open a different, logged-out app based on alphabetical order. If ambiguous, choose explicitly or show a truthful no-selection state (`NuvioInstallationPolicy`).
- Do not promote `watchNextType`, engagement time, or a localized title into proof of a newly released episode. The mixed legacy channel is fallback only and a real playback position is necessary for resuming.
- TV provider metadata can be incomplete or contain synthetic playback durations. Null means unknown, not zero; do not show fabricated time remaining, a fake `new` badge, unsupported quality labels or placeholder artwork as real content.
- Use stable source identity + real episode IDs for deduplication. Publisher sort weights, release dates and watch position are separate concepts. Missing/provider-permission error states must be honest.
- A media click intent must resolve exclusively to the publishing app package; reject cross-package components, malicious selectors, and unsafe `file:`/`content:` overrides.
- SmartTube/public YouTube feeds are bounded by accessible source data; Spotify account/podcast history is not claimed without a supported provider.

Important: the former M00–M08 specification's static grid, screenshot hashes, legacy feature-gate dates and stale CI statuses have **not** been copied here.

## Mullvad single-APK architecture

Source pin: `third_party/mullvadvpn-app` = `aa47f606b1a159214c7279344128119fa91be5fd` (Mullvad `android/2026.12`). The `.gitmodules` entry and mode-`160000` gitlink are essential. Upstream Rust, JNI, service/daemon, gRPC, repository, account/relay, DNS and split-tunneling behavior remain authoritative. Do not fork or copy a second VPN runtime into the launcher.

The integrated application is built through a disposable generated host under `.build/plume-mullvad/`: `tools/mullvad/prepare.py` stages Plume's Android UI as a library and overlays the original Mullvad Android host while preserving the pinned source. `tools/mullvad/build-integrated.sh` invokes the pinned Mullvad Android container. The single installable combined APK is `dev.plume.advanced` with the original `MullvadApplication`, `MullvadVpnService`, native libraries and the launcher HOME/LEANBACK entry point. Keep the ARM64 ABI and integrated Android 9/API 28+ constraints; standalone Plume has its own API 26 minimum.

The embedded TV Mullvad panel is source-owned by `tools/mullvad/MullvadEmbeddedPanel.kt` and its `Vpn*Ui.kt` components. It must preserve: VPN consent and revoke, live tunnel state, account/device state, relay locations, DNS, LAN/auto-connect, per-app split tunneling, advanced tunnel choices, obfuscation, diagnostics and notifications. Android OS dialogs may open outside the launcher workspace. VPN status must not be simulated, and account secrets/keys must never enter source or logs.

The pinned original checkout is verified by CI and the build container's digest. Never silently upgrade it or change checksums to accommodate an unreviewed dependency. Production signing must never fall back to a random debug key.

## Build and review gates

- `.github/workflows/pr.yml` delegates to `ci-integrated.yml` for a single PR gate: standalone JVM tests, Android lint, installer regression, original-submodule check, combined ARM64 debug build, R8 release-shrinker check, manifest/native-library assertions and artifact checks.
- `.github/workflows/debug-apk.yml` is **manual**, builds a sideloadable debug artifact only and is never a substitute for a signed release.
- `.github/workflows/release.yml` independently handles guarded release signing, tags and publication. See [release playbook](../RELEASING.md).
- Unit/CI evidence must be from an actual run on the final PR HEAD. Report shield-device D-pad, Home, VPN tunnel, split-tunneling, reconnect and poster comparisons as **not tested** until hardware verification has actually happened.

## Active UI and older media features

`docs/v-design/DESIGN_CONTRACT.md` and `CONTRIBUTING.md` govern one persistent rail, right-pane content, smaller Hero mask, dynamic real media and installed-app icons. Existing wallpaper, Aerial Views, gallery, camera/photo upload, app picture, HDMI, Home override, PIN, backup and remote functionality remain within product scope even when inherited class names contain `Glass` or `VDesign`. Do not delete live code solely because the name sounds old.

## Historical traceability

Old Classic/Overview, M00–M08, M01–M12 and upstream release decisions remain in the Git history before the repository cleanup. Use historical commits for forensic evidence, not as active requirements. The **latest user visual reference image is not stored in this Git repository** as of this audit; its SHA-256, limitations and approximate visual measurements are recorded in [VISUAL_REVIEW_2026-11.md](../v-design/VISUAL_REVIEW_2026-11.md). Reacquire the **actual** reference bytes before asserting pixel-perfect comparison.
