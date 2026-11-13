# M08 – NVIDIA Shield TV Pro 2019 acceptance matrix

**Implementation source snapshots:** Plume `main` at `f55cb4a309cc751db2815d75a3a1455c6aa359b0`, NuvioTV `dev` at `c0f69ba329dd1484c36a7507e891950810faedfe` (both checked 2026-11-10). The separate NuvioTV change publishes the new `Next Up` preview channel; the existing `Continue Watching` channel and Watch Next feed remain compatible.

**Status:** The matrix below defines expected provider data and a reproducible procedure. **No real Shield query, TV screenshot, playback test or ADB inspection has been executed in the GitHub-connected editing environment.** Record actual provider rows and observed behaviour when testing on the Shield. Do not infer actual provider visibility from Kotlin builders or JVM tests.

## Required device checks

1. Install the latest Nuvio and Plume test APKs on Shield TV Pro 2019. Record package names with `adb shell pm list packages | grep -i nuvio` (also note debug versus release). Verify TV listings permission and app/channel source selection.
2. In Android TV channel settings, make **Nuvio – Continue Watching** and **Nuvio – Next Up** browsable. Channel creation only requests visibility; firmware and user approval decide whether it is actually visible to other apps.
3. With ADB, **if shell permissions allow it**, inspect `adb shell content query --uri content://android.media.tv/channel`. For program rows, use the concrete channel IDs returned on the device: `adb shell content query --uri 'content://android.media.tv/preview_program?channel_id=ID'`; some providers ignore query parameters or restrict shell reads. If access is denied, use an on-device diagnostic app with valid TV-listings permission instead.
4. Capture redacted examples (no tokens or personal video history) of `package_name`, `display_name`, `internal_provider_id`, `type`, `title`, `season_display_number`, `episode_display_number`, `episode_title`, `duration_millis`, `last_playback_position_millis`, `intent_uri`, `last_engagement_time_utc_millis` and `browsable`. Also inspect Nuvio's Watch Next entries. The `intent_uri` contains playback extras, so redact identifiable content IDs if sharing logs.
5. Switch Plume **Style → Home layout** between Classic and Overview. Verify D-pad Right/Left, Up/Down, OK, Back, app-page filtering, focus retention, loading without artwork and return from Nuvio. Confirm that Nuvio launches the intended season and episode, not just its home screen.
6. Disable Nuvio's app feed in Plume, deny TV listings permission, clear Nuvio history, remove the Next Up channel and repeat refresh/restart. The launcher must neither crash nor show stale/newly invented episodes.

## Data and behavior matrix

| Case | Source evidence to record | Expected Plume result | Shield observed |
|---|---|---|---|
| Old Nuvio, in-progress movie | Watch Next CONTINUE or Continue Watching preview, positive playback position | Weitermachen; deep link to movie | **Not tested** |
| Old Nuvio, Next Up item | Legacy CONTINUE but zero playback position; no Next Up channel | **Not** labeled resumed or NEW | **Not tested** |
| Updated Nuvio, next aired episode | `Next Up` preview channel, correct season and episode, no playback position; Nuvio's `hasAired` check | Neu für dich; opens exact episode | **Not tested** |
| Updated Nuvio, unaired episode | `hasAired=false` in Nuvio cache; excluded before publication | No item in NEW | **Not tested** |
| Updated Nuvio, in-progress episode and Next Up for same series | Distinct episode identifiers, per-item progress | Started episode in Weitermachen and different next available episode in NEW | **Not tested** |
| Duplicate same season/episode from Watch Next and preview | App package, series title, season and episode match | One entry; started playback wins | **Not tested** |
| Same episode title in different seasons | Different season numbers, same title | Both episodes remain distinct | **Not tested** |
| No current next episode or missing Next Up channel | No Next Up rows/channel | NEW empty; no invented timestamp | **Not tested** |
| Watch Next disabled; preview published | `ch_flip` state and package filtering | Classic respects previous toggles; Overview uses Nuvio-specific Next Up in shared feed | **Not tested** |
| Invalid or missing deep link | Provider intent absent or malformed | Safe fallback to Nuvio app (or unavailable message) | **Not tested** |
| No TV listings permission / Nuvio uninstalled | No readable provider data | Empty state; no crash | **Not tested** |
| Classic layout | Legacy Watch Next and user-enabled channels only | No added Next Up tiles unless user explicitly enabled that legacy channel | **Not tested** |

### Meaning of "new"

The Next Up channel is **not** a generic list of newly released programmes. It contains Nuvio's **next unwatched, already-aired episode** after applying the same days-cap, dismissed, and in-progress suppression rules as the existing channel. Some of these episodes may have aired long ago. `lastWatched`/`sortTimestamp` describes ranking, **not** a verified first air date. Publication of an aired episode also does not guarantee that a playable stream is present in an individual addon.

## Compatibility and dependency

- **Plume first / old Nuvio:** Old Nuvio has no `Next Up` channel. In-progress content with genuine playback position still appears; zero-progress legacy Next Up items are omitted. No private Nuvio files, databases, IPC APIs or permissions are used.
- **Nuvio first / old Plume:** The original Continue Watching and Watch Next publications are unchanged. Original Plume can ignore the additional channel or allow the user to enable it manually.
- **Both updated:** The distinct channel is consumed into M05's shared repository, deduplicated against started content and displayed only in Overview and app preview pages. The classic home row is still derived from its original `visible` set.

## Automated validation

Required commands when an Android SDK checkout is available:

```sh
# Plume repository
./gradlew testDebugUnitTest assembleDebug

# Nuvio repository (the project's full flavor)
./gradlew testFullDebugUnitTest assembleFullDebug
```

Also examine both pull requests' actual GitHub Actions runs. No successful local Gradle or physical Shield test is claimed by this document.
