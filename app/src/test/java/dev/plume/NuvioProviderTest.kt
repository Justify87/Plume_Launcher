package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure JVM tests for explicit TV channel identity. Device/provider access needs Shield testing. */
class NuvioProviderTest {
    private fun channel(pkg: String = "com.nuvio.tv", id: Long = 41L,
                        suffix: String? = ":in-progress", browsable: Boolean? = true,
                        name: String = "Localized name") =
        TvPreviewChannel(id, pkg, name, suffix?.let { pkg + it }, browsable)

    private fun record(channel: TvPreviewChannel?, id: String = "entry",
                       pkg: String = "com.nuvio.tv", position: Long? = null,
                       watchNext: Boolean = false, type: Long? = null) =
        AndroidTvProgram(
            item = MediaItem(stableId = "tv:$id", sourceId = "tv", sourcePackage = pkg,
                title = "Program", playbackPositionMillis = position, contentId = id),
            watchNext = watchNext, watchNextType = type,
            channelName = channel?.displayName, channelId = channel?.id,
            channelProviderId = channel?.channelProviderId,
            internalProviderId = "program:$id", seasonNumber = "2",
            episodeNumber = "4", releaseDate = "2026-12-01",
        )

    @Test fun exactKnownBuildIdsAndNoNameImpersonation() {
        listOf("com.nuvio.tv", "com.nuviodebug.com", "com.nuvio.app",
            "com.nuvio.appdebug", "com.nuvio.tv.debug", "com.nuvio.app.debug",
            "com.justify87.nuviotv.wireguard", "com.justify87.nuviotv.wireguard.debug",
            "com.justify87.nuviotv.wireguard.playstore",
            "com.justify87.nuviotv.wireguard.playstore.debug")
            .forEach { assertTrue(it, AndroidTvMediaSource.isNuvioPackage(it)) }
        listOf("com.nuvio.tv.fake", "com.nuvio.tv2", "com.nuvio.fake",
            "org.smarttube.beta", "com.justify87.nuviotv.wireguard.fake")
            .forEach { assertFalse(it, AndroidTvMediaSource.isNuvioPackage(it)) }
    }

    @Test fun localizedNamesCannotChangeProviderIdClassification() {
        val channels = listOf(
            channel(id = 1L, suffix = ":in-progress", name = "Fortsetzen"),
            channel(id = 2L, suffix = ":available", name = "Up next later"),
            channel(id = 3L, suffix = ":upcoming", name = "Continue Watching"),
        )
        assertEquals(listOf(NuvioChannelKind.IN_PROGRESS, NuvioChannelKind.AVAILABLE,
            NuvioChannelKind.UPCOMING), channels.map { AndroidTvMediaSource.nuvioKind(it) })
        val feeds = AndroidTvMediaSource.selectNuvio(channels,
            channels.map { record(it, id = "episode:${it.id}") }, emptyList(), true)
        assertEquals(1, feeds.inProgress.size)
        assertEquals(1, feeds.available.size)
        assertEquals(1, feeds.upcoming.size)
        assertEquals("2026-12-01", feeds.upcoming.single().program.releaseDate)
        assertEquals("program:episode:3", feeds.upcoming.single().program.internalProviderId)
        assertFalse(feeds.upcoming.single().legacyFallback)
    }

    @Test fun classicMixedEntriesDoNotBecomeAvailableOrUpcoming() {
        val classic = channel(id = 7L, suffix = "", name = "Weitersehen")
        assertEquals(NuvioChannelKind.LEGACY, AndroidTvMediaSource.nuvioKind(classic))
        val legacy = listOf(record(classic, "started", position = 30_000L),
            record(classic, "not-started", position = null))
        val feeds = AndroidTvMediaSource.selectNuvio(listOf(classic),
            emptyList(), legacy, true)
        assertEquals(listOf("started"), feeds.inProgress.map { it.program.item.contentId })
        assertTrue(feeds.inProgress.single().legacyFallback) // Stable classic provider ID, not label.
        val watchNext = listOf(record(null, "started", position = 30_000L,
            watchNext = true, type = 0L))
        val fallback = AndroidTvMediaSource.selectNuvio(listOf(classic),
            emptyList(), legacy + watchNext, true)
        assertEquals(listOf("started"), fallback.inProgress.map { it.program.item.contentId })
        assertTrue(fallback.inProgress.single().legacyFallback)
        assertTrue(fallback.available.isEmpty())
        assertTrue(fallback.upcoming.isEmpty())
    }

    @Test fun exactClassicProviderIdPermitsPositiveProgressFallback() {
        val classic = channel(id = 8L, suffix = "", name = "Localised")
        val feeds = AndroidTvMediaSource.selectNuvio(listOf(classic), emptyList(),
            listOf(record(classic, "started", position = 9_000L),
                record(classic, "mixed-next", position = null)), true)
        assertEquals(listOf("started"), feeds.inProgress.map { it.program.item.contentId })
        assertTrue(feeds.inProgress.single().legacyFallback)
    }

    @Test fun missingWrongAndSpoofedProviderIdentityAreNotIncluded() {
        val missing = channel(suffix = null)
        val wrong = channel(id = 2L, suffix = ":available-guess")
        val otherApp = channel(id = 3L, pkg = "org.nuvio.tv", suffix = ":upcoming")
        listOf(missing, wrong, otherApp).forEach {
            assertEquals(null, AndroidTvMediaSource.nuvioKind(it))
        }
        val valid = channel(id = 4L, suffix = ":available")
        val result = AndroidTvMediaSource.selectNuvio(listOf(valid),
            listOf(record(valid, pkg = "org.smarttube.beta"),
                record(valid).copy(channelProviderId = "com.nuvio.tv:upcoming"),
                record(missing), record(wrong), record(otherApp, pkg = "org.nuvio.tv")),
            emptyList(), true)
        assertTrue(result.available.isEmpty())
        assertTrue(result.upcoming.isEmpty())
    }

    @Test fun optInAndPermissionAndAbsentProviderDoNotInventItems() {
        val hidden = channel(browsable = false)
        val unknownApproval = channel(id = 2L, browsable = null)
        val approved = channel(id = 3L)
        assertEquals(NuvioProviderFeeds.EMPTY, AndroidTvMediaSource.selectNuvio(
            listOf(hidden, unknownApproval), listOf(record(hidden), record(unknownApproval)),
            emptyList(), true))
        assertEquals(NuvioProviderFeeds.EMPTY, AndroidTvMediaSource.selectNuvio(
            listOf(approved), listOf(record(approved)), emptyList(), false))
        assertEquals(NuvioProviderFeeds.EMPTY,
            AndroidTvMediaSource.selectNuvio(emptyList(), emptyList(), emptyList(), true))
    }

    @Test fun approvedButEmptyInProgressDoesNotUseLegacyFallback() {
        val active = channel()
        val classic = channel(id = 9L, suffix = "")
        val feeds = AndroidTvMediaSource.selectNuvio(listOf(active, classic), emptyList(),
            listOf(record(classic, position = 10_000L)), true)
        assertTrue(feeds.inProgress.isEmpty())
    }

    @Test fun watchNextNeedsCorrectTypeAndPositivePlayback() {
        val good = record(null, "started", position = 2_000L, watchNext = true, type = 0L)
        val wrong = record(null, "next", position = 2_000L, watchNext = true, type = 1L)
        val tooShort = record(null, "unstarted", position = 999L, watchNext = true, type = 0L)
        val feeds = AndroidTvMediaSource.selectNuvio(emptyList(), emptyList(),
            listOf(good, wrong, tooShort), true)
        assertEquals(listOf("started"), feeds.inProgress.map { it.program.item.contentId })
        assertTrue(feeds.inProgress.single().legacyFallback)
    }

    @Test fun newCategoryWithMisleadingOldLabelNeverEntersClassicSelection() {
        val categorized = channel(id = 1L, suffix = ":available", name = "Continue Watching")
        val legacy = channel(id = 2L, suffix = "", name = "Localised")
        val selected = AndroidTvMediaSource.select(listOf(
            record(categorized, "false-classic", position = 4_000L),
            record(legacy, "classic", position = 5_000L),
        ), true)
        assertEquals(listOf("classic"), selected.map { it.contentId })
        assertTrue(selected.all { it.category == MediaCategory.CONTINUE })
    }
}
