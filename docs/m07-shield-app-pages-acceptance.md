# M07: Shield TV app-page acceptance

Branch: `feature/m07-app-content-pages`. Run on NVIDIA Shield TV Pro (2019) with a D-pad remote.
These are **manual steps, not automated results**.

## Preconditions

- Install the debug APK built from M07. Enable **Overview** in Plume settings.
- Grant TV listings access if prompted; otherwise verify the clear permission empty state.
- Use at least two installed TV apps. Prefer one that publishes Watch Next records and one that does not.
- Published new releases, recommendations, and other categories only appear if the provider supplies them. M06 currently recognizes reliable resume records; do not claim a new release from timestamps alone.

## Remote checks

1. **Overview:** Select Overview in the sidebar. Only genuine app-independent items appear, grouped by their verified category; empty rows do not take space.
2. **Preview:** Focus Nuvio TV, SmartTube, or another app with Up/Down in the sidebar. The right side changes without launching anything.
3. **Filter:** Check each app page; it must contain **only** records owned by that app. The Overview must include the union without duplicated records.
4. **Action:** Press Right on an app sidebar entry. **Open app** receives focus. Press OK and verify that the selected app launches.
5. **Sidebar OK:** Return to Plume; press OK directly on that app's sidebar entry. The app opens instead of entering the media page.
6. **Media OK:** Press Right to the app page, Down to a card, then OK. Verify that playback opens in the publishing app, or a controlled fallback appears for an invalid/unavailable deep link.
7. **Empty app:** Select an app with no published TV items. Verify its icon/name, concise message and reachable Open app action; no empty row or broken focus.
8. **Navigation:** Navigate several cards horizontally and between populated rows. Check left-edge return to the sidebar, Up to the top bar, and no half-card clipping.
9. **Back:** While an app page is visible, press Back. The Overview page and Overview sidebar selection return. Open All apps and confirm Back still closes its overlay normally.
10. **Refresh:** With a media card focused, return after playback and inspect focus and horizontal scroll. The same stable item should remain focused if still available; otherwise the nearest available card should receive focus.
11. **Removal:** Uninstall or hide the selected app while it is active. After the launcher refreshes, verify fallback to Overview without a stale app label, broken action, or inaccessible focus.
12. **Regression:** Switch to Classic. Check app launches, Watch Next, app order, settings, wallpaper, glass and Home/Back behavior. Switch back to Overview.

## Known source limits

- The Android TV provider controls which entries and deep links are available; SmartTube or other apps may publish no records.
- M07 reuses M06's in-memory TV snapshot. Moving through pages must not start new provider scans.
- Device testing, focus animation and TV permission behavior cannot be proven by JVM unit tests.
