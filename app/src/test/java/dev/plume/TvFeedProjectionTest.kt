package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TvFeedProjectionTest {
    private val nuvio = "com.nuvio.tv"

    private fun program(
        pkg: String = nuvio,
        origin: String = "tv:watchnext",
        channelName: String? = null,
        channelId: Long? = null,
        channelProviderId: String? = null,
        internalProviderId: String? = "series_s1e2",
        position: Long? = null,
        duration: Long? = null,
        watchNextType: Long? = null,
    ) = Prog(
        title = "Series", sub = "", progress = 0, image = null,
        intent = "intent:#Intent;package=$pkg;end", pkg = pkg,
        origin = origin, contentId = "42", channelName = channelName,
        channelId = channelId, channelProviderId = channelProviderId,
        internalProviderId = internalProviderId, watchNextType = watchNextType,
        playbackPositionMillis = position, durationMillis = duration,
        seasonNumber = "1", episodeNumber = "2",
    )

    private fun project(
        discovered: List<TvPreviewChannel> = emptyList(),
        feed: List<Prog> = emptyList(),
        categorized: List<Prog> = emptyList(),
        off: Set<String> = emptySet(),
        youtubeEnabled: Boolean = false,
    ) = TvFeedProjection.project(discovered, feed, categorized, off, youtubeEnabled)

    @Test fun categorizedNuvioUsesExactBrowsableChannelAndPublisher() {
        val key = "$nuvio:upcoming"
        val channel = TvPreviewChannel(77L, nuvio, "Upcoming", key, true)
        val entry = program(origin = "tv:ch77", channelId = 77L, channelProviderId = key)
        val result = project(discovered = listOf(channel), categorized = listOf(entry))
        assertEquals(1, result.providerFeeds.upcoming.size)
        assertEquals(nuvio, result.dashboardFeed.upcoming.single().sourcePackage)
        assertTrue(result.mediaItems.isEmpty())

        val wrongOwner = project(discovered = listOf(channel),
            categorized = listOf(program(pkg = "com.nuvio.app", origin = "tv:ch77",
                channelId = 77L, channelProviderId = key)))
        assertTrue(wrongOwner.providerFeeds.upcoming.isEmpty())
        assertTrue(wrongOwner.dashboardFeed.upcoming.isEmpty())
    }

    @Test fun hiddenPublisherDoesNotLeakIntoNuvioCategories() {
        val key = "$nuvio:available"
        val channel = TvPreviewChannel(9L, nuvio, "Available", key, true)
        val entry = program(origin = "tv:ch9", channelId = 9L, channelProviderId = key)
        val result = project(discovered = listOf(channel),
            categorized = listOf(entry), off = setOf(nuvio))
        assertTrue(result.providerFeeds.available.isEmpty())
        assertTrue(result.dashboardFeed.available.isEmpty())
    }

    @Test fun unknownDurationStaysUnknownForStartedWatchNext() {
        val entry = program(position = 20_000L, watchNextType = 0L)
        val result = project(feed = listOf(entry))
        val item = result.mediaItems.single()
        assertEquals(nuvio, item.sourcePackage)
        assertEquals(MediaCategory.CONTINUE, item.category)
        assertNull(item.durationMillis)
        assertNull(item.progressPercent)
        assertEquals(entry.intent, item.intentUri)
    }

    @Test fun smartTubeNewItemsRespectExistingPreferenceGate() {
        val video = program(pkg = "org.smarttube.beta", origin = "tv:ch15",
            channelName = "Subscriptions", internalProviderId = "AbCdEfGhI12")
        assertTrue(project(feed = listOf(video), youtubeEnabled = false).mediaItems.isEmpty())
        val enabled = project(feed = listOf(video), youtubeEnabled = true).mediaItems
        assertEquals(1, enabled.size)
        assertEquals("yt:AbCdEfGhI12", enabled.single().stableId)
        assertEquals(MediaCategory.NEW, enabled.single().category)
    }
}
