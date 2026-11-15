<p align="center">
  <img src="app/src/main/res/drawable-nodpi/ic_launcher_advanced.webp" alt="Plume Launcher Advanced icon" width="128" height="128">
</p>

<h1 align="center">Plume Launcher Advanced</h1>

<p align="center">A personal, content-first Android TV launcher with an embedded Mullvad VPN interface.</p>

<p align="center">
  <a href="https://github.com/Justify87/Plume_Launcher/actions/workflows/pr.yml"><img alt="Pull request checks" src="https://github.com/Justify87/Plume_Launcher/actions/workflows/pr.yml/badge.svg"></a>
  <a href="LICENSE"><img alt="GPL-3.0 license" src="https://img.shields.io/badge/License-GPL--3.0-blue"></a>
</p>

> **Independent fork.** This project is maintained by [Justify87](https://github.com/Justify87), based on [Plume Launcher by aradhyas8](https://github.com/aradhyas8/Plume_Launcher). It is not an official successor or Mullvad distribution. Original author, third-party licenses, and project origins are acknowledged [below](#credits-and-license).

## Current product

The merged [persistent-workspace implementation (#48)](https://github.com/Justify87/Plume_Launcher/pull/48) replaced separate in-launcher fullscreen destinations with one shell. A compact sidebar remains visible while the right-hand panel changes. The sidebar has Home, supported app previews, All Apps, launcher settings, and Mullvad. Focusing a destination shows its content; OK on an installed app shortcut launches that app's actual package.

- **Home:** in-progress, available and upcoming Nuvio episodes, using real Android TV Preview channels; an editorial Continue Watching hero and a single upcoming row.
- **App previews:** Nuvio, SmartTube and Spotify surfaces display only published/accessible media; missing source data stays visibly unavailable rather than being invented.
- **App launcher:** installed-app icons and a compact grid, with explicit source-package and focus rules.
- **Mullvad:** original service, account, tunnel, locations, DNS, diagnostics and split-tunneling infrastructure, presented through an embedded Android TV panel.

The current product contract is [docs/v-design/DESIGN_CONTRACT.md](docs/v-design/DESIGN_CONTRACT.md). The integration invariants and their actual implementation paths are in [docs/architecture/ACTIVE_INTEGRATIONS.md](docs/architecture/ACTIVE_INTEGRATIONS.md). For provenance of the workspace integration see [PR #48](https://github.com/Justify87/Plume_Launcher/pull/48).

**Validation boundary:** A merged PR and successful CI are not evidence of complete physical Shield TV or real-tunnel verification. The latest user-supplied Expressive visual reference is described in [VISUAL_REVIEW_2026-11.md](docs/v-design/VISUAL_REVIEW_2026-11.md), but its original image bytes are not currently versioned in this repository.

## Build an on-demand debug APK

For the combined Plume + Mullvad ARM64 application:

1. Open [Build Plume Launcher Advanced Debug APK](https://github.com/Justify87/Plume_Launcher/actions/workflows/debug-apk.yml).
2. Choose **Run workflow** on `main`, with the required `source_ref` (normally `main`; a feature branch can also be built).
3. Download the workflow artifact from the successful run and use the **combined ARM64 debug APK** from the ZIP. This does not create a release, tag or production signing key.

See [Debug build instructions](docs/DEBUG_BUILDS.md) for the current artifact filename, SHA-256, installation and signing caveats. An APK from different debug runners may have a different certificate: back up settings before replacing a locally installed debug version.

The standalone Plume Android module remains useful for quick checks:

```bash
git clone --recurse-submodules https://github.com/Justify87/Plume_Launcher.git
cd Plume_Launcher
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The full combined ARM64 build requires the pinned Mullvad Android toolchain/container and original submodule; `tools/mullvad/build-integrated.sh` is the entry point. The combined package retains `dev.plume.advanced`, the original `dev.plume` Kotlin namespace, and Android TV/Mullvad components. See [release instructions](docs/RELEASING.md). The standalone module targets API 26+, while the integrated Mullvad build requires API 28+ and ARM64; do not infer integrated-device compatibility from standalone Gradle settings.

## Install and maintain

Original Plume and Plume Launcher Advanced can be installed side-by-side because their application IDs are different (`dev.plume` versus `dev.plume.advanced`). Only one should take over Home at a time. Some devices require Home-role or Accessibility setup; do not blindly disable the stock Google TV launcher. `tools/plume-install.sh` provides the original setup/diagnostics helper.

Settings backups are selective and do not include every image, PIN, VPN credential, remap or OS-level Home permission. An Android settings grant such as `WRITE_SECURE_SETTINGS` must be done knowingly and is not needed for a normal APK installation.

Manual **debug** builds are for testing; [releases](https://github.com/Justify87/Plume_Launcher/releases) are the separate, signed distribution path. A release update requires the same signing certificate as the installed app, and `0.1.0` is an independent semantic version lineage.

## Code orientation

| Component | Purpose |
| --- | --- |
| [`MainActivity.kt`](app/src/main/java/dev/plume/MainActivity.kt) | Launcher UI host, settings, Android TV integration |
| [`VDesignHomeView.kt`](app/src/main/java/dev/plume/VDesignHomeView.kt) | Current persistent sidebar and home panel (legacy class name remains) |
| [`LauncherWorkspaceNavigation.kt`](app/src/main/java/dev/plume/LauncherWorkspaceNavigation.kt) | Sidebar state, installed Nuvio package selection, launch safety |
| [`AndroidTvMediaSource.kt`](app/src/main/java/dev/plume/AndroidTvMediaSource.kt) | TV source ownership / channel-provider identities |
| [`NuvioDashboardFeed.kt`](app/src/main/java/dev/plume/NuvioDashboardFeed.kt) | Categorized real episode projection |
| [`tools/mullvad/prepare.py`](tools/mullvad/prepare.py) | Disposable integrated build overlay, original submodule intact |
| [`tools/mullvad/MullvadEmbeddedPanel.kt`](tools/mullvad/MullvadEmbeddedPanel.kt) | Embedded TV control panel |

Contributions follow [CONTRIBUTING.md](CONTRIBUTING.md). Maintenance decisions and removed paths are recorded in [docs/maintenance/REPOSITORY_CLEANUP.md](docs/maintenance/REPOSITORY_CLEANUP.md); historical material is recoverable from Git history instead of living in a second active documentation tree.

## Privacy and network access

The launcher does not require a central tracking account and should remain free of advertisements and analytics. Media entries are read only from supported Android TV/provider interfaces and optional feeds, never by pretending to access another app's private account history. Optional images, wallpaper, update checks, weather and public YouTube feeds involve network requests; VPN/account logic resides in the original Mullvad-backed implementation. Android TV listings, image-picker and Accessibility permissions are constrained by device and user configuration. Spotify-account data integration is not claimed to exist.

## Credits and license

Plume Launcher Advanced derives from [Plume Launcher](https://github.com/aradhyas8/Plume_Launcher) by **aradhyas8** and other upstream contributors. Their authorship and existing copyrights remain applicable; modifications and branding do not imply upstream endorsement. The embedded [Mullvad VPN](https://github.com/mullvad/mullvadvpn-app) Android source and its third-party licenses remain upstream-owned and are not an official Mullvad build. Media and icon assets have their own applicable authorship and licensing (including Material Symbols and Simple Icons).

This repository is distributed under [GPL-3.0](LICENSE), subject to the licenses and notices of included third-party works.
