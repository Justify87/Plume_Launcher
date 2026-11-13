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
    @Test fun partialOrEmptySectionsNeverReceiveDummiesOrCrossFeedContent() {
        val feed = NuvioDashboardFeed(
            inProgress = listOf(entry("started", NuvioSection.IN_PROGRESS)),
            available = emptyList(),
            upcoming = listOf(entry("soon", NuvioSection.UPCOMING)),
        )
        assertEquals("started", VDesignCardSlots.media(feed, NuvioSection.IN_PROGRESS, 0)?.title)
        assertNull(VDesignCardSlots.media(feed, NuvioSection.IN_PROGRESS, 1))
        assertNull(VDesignCardSlots.media(feed, NuvioSection.AVAILABLE, 0))
        assertEquals("soon", VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 0)?.title)
        assertNull(VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 1))
        assertNull(VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 8))
        assertNull(VDesignCardSlots.media(feed, NuvioSection.IN_PROGRESS, -1))
    }

    @Test fun eightVisibleUpcomingCardsAreAWindowAndAllCountsSurvive() {
        val feed = NuvioDashboardFeed(
            emptyList(), emptyList(), (0 until 13).map {
                entry("upcoming-$it", NuvioSection.UPCOMING)
            },
        )
        assertEquals(13, feed.upcoming.size)
        assertEquals("upcoming-0", VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 0)?.title)
        assertEquals("upcoming-7", VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 7)?.title)
        assertEquals("upcoming-5", VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 0, 5)?.title)
        assertEquals("upcoming-12", VDesignCardSlots.media(feed, NuvioSection.UPCOMING, 7, 5)?.title)
    }

    @Test fun pagingNeverSkipsTheLastEntryOrEscapesTheSection() {
        assertEquals(0, VDesignCardSlots.nextUpcomingOffset(8, 0, 7, +1))
        assertEquals(4, VDesignCardSlots.nextUpcomingOffset(13, 0, 7, +1))
        assertEquals(5, VDesignCardSlots.nextUpcomingOffset(13, 4, 7, +1))
        assertEquals(1, VDesignCardSlots.nextUpcomingOffset(13, 5, 1, -1))
        assertEquals(0, VDesignCardSlots.nextUpcomingOffset(13, 1, 0, -1))
        assertEquals(0, VDesignCardSlots.nextUpcomingOffset(13, 0, 2, +1))
    }
}
