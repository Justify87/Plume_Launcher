# Plume Launcher Advanced — Persistent Workspace (active design contract)

**Supersedes:** the old single-screen V-Design implementation, overlay-based launcher
settings, separate in-launcher VPN page, old rail icon rules, and all M00–M08
layout contracts. The user's November 2026 reference image remains the visual
authority for the **Home content**, not for navigation behavior contradicted by
the user's newer explicit instructions.

## One persistent shell

The left rail is always visible on all in-launcher destinations. Its compact
icon buttons must be recognizable at TV viewing distance, show a clear focus
state, and never force a full-screen transition. The entire remaining width
is a single content viewport.

- **Rail focus immediately previews/selects** the right-hand destination.
  Neither All Apps, Settings nor Mullvad requires an initial OK to display.
- **Right** enters focusable content. **Left/Back** returns to the rail; Back
  from a non-Home rail selection returns to Home. Home stays reachable.
- **OK on an app rail icon** launches exactly that installed application
  variant; OK on a content destination enters its focusable controls.
- Home, Nuvio, SmartTube, Spotify, All Apps, Settings and Mullvad are content
  destinations, not different full-screen launcher activities.
- Nuvio/SmartTube/Spotify may show available, source-backed channel previews
  in the right pane. Missing channels show a useful empty state, not fake data.
- Keep selection, scroll position and focus key across preview switching and
  app return. Rebuild lists only when their data actually changes.

## Sidebar and icons

Use actual installed package icons for app entries, with an explicit setting
for **original full-color** versus **monochrome**. Never forcibly tint color
app icons. Home/All Apps/Settings/VPN use coherent vector symbols; they must
not be confused with each other. Scale icon art within a consistent hit target
rather than expanding the rail.

The correct Nuvio installation is the one owning the TV preview channel.
Never silently choose a different variant just because its package sorts first.
When multiple Nuvio variants exist and no provider is identifiable, require
explicit selection or show a disambiguation choice; do not open an unrelated
logged-out variant.

## Home composition

Keep the user's reference Home composition: dominant Continue Watching hero,
two supporting cards, three available episodes, and a single row of six
upcoming entries. The lime-colored hero panel must leave substantially more
of the episode poster visible than the previous implementation. Artwork is
dynamic; titles and progress are source-derived. No invented 'new' labels,
VPN identity, episode dates, or sample poster content.

## All Apps, Settings and Mullvad

All Apps is a native right-pane grid with an in-pane header, responsive columns,
clear focused app tiles and direct OK-to-launch. The left rail never disappears.

Launcher Settings is a right-pane two-level layout with compact categories,
focused settings and concise descriptions. Existing PIN, wallpaper, setup,
system actions and other settings remain reachable. Remove the old dimmed
full-screen scrim and floating panel *after* the replacement is functional.

Mullvad uses the same right pane and retains all original account, location,
DNS, advanced, diagnostics and split-tunneling behavior. Its original
service/repository layer remains authoritative. Do not launch a separate
`VpnActivity` for routine in-launcher navigation after its panel integration
is working; retain a compatibility entry only if necessary for external launch
intents. VPN consent and Android system dialogs may still appear outside the
shell, because Android owns them.

## Motion and performance

Use one small motion vocabulary: 120–180 ms focus transitions, 160–220 ms
content changes, no distracting continuous animation. Animate opacity and
translation of the content viewport, not full-screen relayouts. Avoid
unnecessary rebinds, heavyweight blur and offscreen rendering. Honor the
Android animator duration scale and disabled animations. Focus, click and
selection must remain distinguishable. Avoid adding libraries unless there is
a demonstrated benefit and acceptable Android TV cost.

## Implementation contract

**Replace obsolete UI modules completely instead of accumulating local
patches.** New components should be cohesive, small, independently testable,
and have one owner for navigation state. Do not rewrite working security,
account, networking, channel projection or media playback code just to
change presentation. Retire old overlays and duplicate navigation paths only
after new routes pass regression tests.

Milestones:
1. Navigation and package-owner policy with unit tests.
2. Complete persistent rail and content host, icon styles and focus behavior.
3. New All Apps, Settings and embedded Mullvad content; remove legacy overlays.
4. Home hero composition and source-backed app previews.
5. Motion/performance pass, combined Plume+Mullvad CI, emulator screenshots,
   real Shield D-Pad/VPN acceptance, final visual comparison.

After each milestone review behavior, regressions, source ownership and test
evidence. Do not merge until the integrated app compiles and core routes work.
Never claim 100% pixel fidelity without actual screenshots from the device.
