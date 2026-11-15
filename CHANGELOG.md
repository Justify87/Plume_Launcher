# Plume Launcher Advanced changelog

This is the release history of **Plume Launcher Advanced**, maintained separately
from the original Plume Launcher. The older Plume and Classic/Overview notes are
preserved in `docs/archive/legacy-features/pre-advanced-changelog.md`.

The launcher displays release notes from the section matching its `versionName`.
Keep the most important six changes first and use one `- ` bullet per change.

## Unreleased

- **Unified navigation.** Keep a compact icon rail visible while Home, app previews, All Apps, Settings and Mullvad pages change on the right as soon as you focus an icon.
- **Correct Nuvio launch.** Prefer production Nuvio packages and let you select an installed variant explicitly instead of opening a differently signed debug login.
- **Recognizable app icons.** Switch between full-color installed app icons and a monochrome sidebar; use one consistent set of built-in system symbols.
- **More visible episode artwork.** Reduce the pale curved Hero panel to the left half, with smaller readable controls.
- **Embedded Mullvad controls.** Use original Mullvad service, repositories and eight compact TV subpages inside the launcher without switching Activities in the combined build.
- **Modern TV interactions.** Reuse genuine provider artwork, compact in-place Settings and All Apps, D-Pad focus states, coalesced poster loading and reduced-motion-safe property transitions.

## 0.1.0

- **First Advanced release.** Start independent versioning for Plume Launcher Advanced.
- **V-Design Home.** Navigate one modern Android TV home screen with the remote.
- **Nuvio TV.** Show in-progress, newly available and upcoming episodes from supported TV channels.
- **Real media data.** Hide unavailable sections instead of showing sample content.
- **Integrated Mullvad VPN.** Open the TV VPN interface in the same ARM64 Android app.
- **Shield TV support.** Use Android TV Home navigation, app shortcuts and system access.
- **Pinned toolchain.** Build and validate the combined Plume-Mullvad APK.
- **GitHub updates.** Get future stable releases from this fork.
