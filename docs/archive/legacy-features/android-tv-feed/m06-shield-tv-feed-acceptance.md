# M06 — Android TV provider feed: NVIDIA Shield TV Pro 2019 acceptance

M06 only connects the existing TvContract query (Watch Next + explicitly enabled app channels)
to the Overview's shared MediaFeedRepository. It does **not** use a second crawler.
Classic still renders its original Prog list and retains existing sort, hide, app-off
and channel-flip settings. No services or recurrent polling were added.

## Prerequisites

- Install **Plume Launcher Advanced** beside the original Plume app.
- Have a current NuvioTV installation and SmartTube on the Shield, signed in or
  configured with real media when applicable. Their publication of Android TV
  Watch Next or Preview channels depends on their own builds and settings.
- Enable **Overview** under the launcher's Home layout preference.
- Grant Android TV listings access when the TV requests it; if needed and allowed by
  the device's permission model, use
  `adb shell pm grant dev.plume.advanced android.permission.READ_TV_LISTINGS`.
  Do not assume that this command succeeds on every firmware version.
- In Plume's Continue Watching channel settings, inspect the actual names published
  on the Shield. A channel named *Continue Watching* is enabled by default; other
  channels can be enabled for **Classic**, but they are not automatically tagged
  resumable in Overview.

## Manual checks (device-only, not run by the coding agent)

1. **Watch Next** — Start a film/episode in an app that actually publishes a
   Watch Next program with type CONTINUE. Return Home. Confirm Overview >
   Continue Watching shows the title, poster, source mark, and known episode/progress
   details. Press OK: the correct source app should open.
2. **NuvioTV** — With real Nuvio items in Watch Next and/or its *Continue Watching*
   Preview channel, return Home, compare Overview and Classic. When one episode
   appears in both and there is matching provider or series/episode identity,
   Overview should show one card; different episodes must remain independent.
3. **SmartTube** — Check which Preview channels/Watch Next entries this actual
   SmartTube build publishes. A generic *Recommended* or subscriptions channel
   must **not** populate Overview's Continue row. If SmartTube publishes a valid
   resumable entry, test OK; if it publishes none, record that rather than
   reporting the source as broken.
4. **New for you** — No unverified release is labelled new. With only Watch Next
   type NEW or generic Preview cards, the row must show its honest empty message.
5. **Per-app page** — Focus NuvioTV or SmartTube in the sidebar. Only that app's
   resumable cards should be shown. The Open app action remains available and
   the All apps overlay must still open.
6. **Existing filters** — Hide an entry using Classic's long-OK action; check it
   is gone from both Classic and Overview. Turn an app off in `cw_off`, toggle
   Watch Next or channels via `ch_flip`, and verify both layouts respect the
   preferences. Re-enable them to verify restoration. Preserve user app order.
7. **Missing permission or provider** — Deny/revoke `READ_TV_LISTINGS` or
   test with apps/channels absent. Overview must show a useful empty/permission
   message; no crash. Verify the view again after granting access.
8. **Stale/bad deep link** — Use a provider entry whose intent no longer resolves.
   OK may open the publishing app's TV launch activity instead; otherwise the
   launcher displays an unavailable-item message, never a crash or cross-app launch.
9. **Focus/regression** — Exercise D-pad left/right/up/down across both rows and
   sidebar after returning from a video. Switch back to Classic and check app-row
   order, source icons, poster cache, long-OK hide, and Watch Next row visibility.

## Build/CI verification

Run `./gradlew testDebugUnitTest assembleDebug` in a checked-out branch.
Also review the repository's `.github/workflows/pr.yml` checks (unit tests,
lint, release APK, installation regression tests and APK-size comparison).
Report build and device results separately; CI does not replace Shield testing.

## Source and privacy limitations

Android TV providers can return null cursors, throw permission errors or expose
no channels at all; this is not evidence that an app has no viewing history.
The `internal_provider_id` field may be series-scoped or missing; duplicate
suppression errs on the side of leaving ambiguous items distinct rather than
merging different shows. Android TV Watch Next type NEW is not treated as proof
of a verified new release, so M06 intentionally does not publish it to that row.
No online metadata lookup or new permission is introduced.
