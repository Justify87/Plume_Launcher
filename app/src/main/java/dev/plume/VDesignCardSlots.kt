package dev.plume

/**
 * Android-free binding policy for the 3+3+6 fixed reference cells.
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
                slot.takeIf { it < 6 }?.plus(upcomingOffset.coerceAtLeast(0)) ?: return null)
        }
    }

    /** A single row pages right from the last tile and left from the first tile. */
    fun nextUpcomingOffset(total: Int, current: Int, slot: Int, direction: Int): Int {
        val max = (total - 6).coerceAtLeast(0)
        val safe = current.coerceIn(0, max)
        return when {
            direction > 0 && slot == 5 -> (safe + 6).coerceAtMost(max)
            direction < 0 && slot == 0 -> (safe - 6).coerceAtLeast(0)
            else -> safe
        }
    }
}
