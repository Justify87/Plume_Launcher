package dev.plume

/**
 * Android-free binding policy for the 3+3+8 fixed reference cells.
 * The full immutable feed owns counts; these are only visible windows.
 */
internal object VDesignCardSlots {
    fun media(feed: NuvioDashboardFeed, section: NuvioSection, slot: Int,
        upcomingOffset: Int = 0): NuvioMediaItem? {
        if (slot < 0) return null
        return when (section) {
            NuvioSection.IN_PROGRESS -> feed.inProgress.getOrNull(slot.takeIf { it < 3 } ?: return null)
            NuvioSection.AVAILABLE -> feed.available.getOrNull(slot.takeIf { it < 3 } ?: return null)
            NuvioSection.UPCOMING -> feed.upcoming.getOrNull(
                slot.takeIf { it < 8 }?.plus(upcomingOffset.coerceAtLeast(0)) ?: return null)
        }
    }

    /** Only the bottom row can page down, only the top row can page back up. */
    fun nextUpcomingOffset(total: Int, current: Int, slot: Int, direction: Int): Int {
        val max = (total - 8).coerceAtLeast(0)
        val safe = current.coerceIn(0, max)
        return when {
            direction > 0 && slot in 4..7 -> (safe + 4).coerceAtMost(max)
            direction < 0 && slot in 0..3 -> (safe - 4).coerceAtLeast(0)
            else -> safe
        }
    }
}
