package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VDesignCardSlotsTest {
    private fun entry(id: String, section: NuvioSection) = NuvioMediaItem(
        stableId = id, section = section, sourcePackage = "com.nuvio.tv", title = id,
        seriesId = id, seasonNumber = null, episodeNumber = null, episodeTitle = null,
        shortDescription = null, longDescription = null, releaseDate = null,
        artworkUri = null, posterUri = null, logoUri = null, intentUri = null,
        progressPercent = null, durationMillis = null, playbackPositionMillis = null,
        remainingMillis = null, lastWatchedMillis = null, providerOrder = 0,
        legacyFallback = false,
    )
    @Test fun sectionsNeverReceiveFakeOrCrossFeedContent() {
        val feed = NuvioDashboardFeed(
            listOf(entry("started", NuvioSection.IN_PROGRESS)), emptyList(),
            listOf(entry("soon", NuvioSection.UPCOMING)),
        )
        assertEquals("started", VDesignCardSlots.media(feed, NuvioSection.IN_PROGRESS, 0)?.title)
        assertNull(VDesignCardSlots.media(feed, NuvioSection.IN_PROGRESS, 1))
        assertNull(VDesignCardSlots.media(feed, NuvioSection.AVAILABLE, 0))
        assertEquals("soon", VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 0)?.title)
        assertNull(VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 1))
        assertNull(VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 6))
        assertNull(VDesignCardSlots.media(feed, NuvioSection.IN_PROGRESS, -1))
    }
    @Test fun sixVisibleUpcomingCardsRemainWindowOverFullFeed() {
        val feed = NuvioDashboardFeed(emptyList(), emptyList(), (0 until 13).map {
            entry("upcoming-$it", NuvioSection.UPCOMING)
        })
        assertEquals(13, feed.upcoming.size)
        assertEquals("upcoming-5", VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 5)?.title)
        assertEquals("upcoming-11", VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 5, 6)?.title)
        assertEquals("upcoming-12", VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 5, 7)?.title)
    }
    @Test fun horizontalPagingReachesLastEntryWithoutCrossingBounds() {
        assertEquals(0, VDesignCardSlots.nextUpcomingOffset(6, 0, 5, +1))
        assertEquals(6, VDesignCardSlots.nextUpcomingOffset(13, 0, 5, +1))
        assertEquals(7, VDesignCardSlots.nextUpcomingOffset(13, 6, 5, +1))
        assertEquals(1, VDesignCardSlots.nextUpcomingOffset(13, 7, 0, -1))
        assertEquals(0, VDesignCardSlots.nextUpcomingOffset(13, 1, 0, -1))
        assertEquals(0, VDesignCardSlots.nextUpcomingOffset(13, 0, 3, +1))
    }
}
