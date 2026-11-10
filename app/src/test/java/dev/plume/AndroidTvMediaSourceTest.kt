package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidTvMediaSourceTest {
    private fun record(
        rowId: String, app: String = "com.nuvio.tv", title: String = "Pilot",
        watchNext: Boolean = true, type: Long? = 0L, channel: String? = null,
        providerId: String? = null, series: String? = null, season: String? = null,
        episode: String? = null, episodeTitle: String? = null,
        intent: String? = "intent:#Intent;package=com.nuvio.tv;end",
        progress: Long? = 15_000L,
    ) = AndroidTvProgram(
        item = MediaItem(
            stableId = MediaFeedRules.stableId("tv:$rowId", app, rowId, intent, 0),
            sourceId = "tv", sourcePackage = app, title = title,
            contentId = rowId, intentUri = intent, playbackPositionMillis = progress,
        ),
        watchNext = watchNext, watchNextType = type, channelName = channel,
        internalProviderId = providerId, seriesTitle = series,
        seasonNumber = season, episodeNumber = episode, episodeTitle = episodeTitle,
    )

    @Test fun onlyExplicitContinueSourcesAreResumable() {
        val records = listOf(
            record("continue"),
            record("watch-next-next", type = 1),
            record("watch-next-new", type = 2),
            record("ordinary-preview", watchNext = false, channel = "Recommended"),
            record("preview-continue", watchNext = false, type = null, channel = "Continue Watching"),
        )
        val selected = AndroidTvMediaSource.select(records, permissionGranted = true)
        assertEquals(listOf("continue", "preview-continue"), selected.map { it.contentId })
        assertTrue(selected.all { it.category == MediaCategory.CONTINUE })
        assertTrue(selected.none { it.category == MediaCategory.NEW })
    }

    @Test fun differentShowsWithSameEpisodeTitleAndGenericIntentRemainDistinct() {
        val result = AndroidTvMediaSource.select(listOf(
            record("watch-row-a", series = "Show A", season = "1", episode = "1"),
            record("channel-row-b", watchNext = false, channel = "Continue Watching", series = "Show B", season = "1", episode = "1"),
        ), true)
        assertEquals(2, result.size)
    }

    @Test fun duplicateNuvioWatchNextAndPreviewKeepWatchNextRecord() {
        val records = listOf(
            record("watch", providerId = "nuvio-series-7-ep-3", series = "Series Seven", season = "1", episode = "3"),
            record("preview", watchNext = false, channel = "Continue Watching",
                providerId = "nuvio-series-7-ep-3", series = "Series Seven", season = "1", episode = "3"),
        )
        assertEquals(listOf("watch"), AndroidTvMediaSource.select(records, true).map { it.contentId })
    }

    @Test fun sharedSeriesDeepLinkKeepsDifferentEpisodes() {
        val sameSeriesLink = "https://provider.example/series/9"
        val records = listOf(
            record("one", series = "Series Nine", season = "1", episode = "1", intent = sameSeriesLink),
            record("two", watchNext = false, channel = "Continue Watching", series = "Series Nine",
                season = "1", episode = "2", intent = sameSeriesLink),
        )
        assertEquals(2, AndroidTvMediaSource.select(records, true).size)
    }

    @Test fun knownSeriesAndEpisodeDedupeEvenWithDifferentDatabaseIds() {
        val records = listOf(
            record("watch", series = "Series Seven", season = "2", episode = "9"),
            record("preview", watchNext = false, channel = "Continue Watching",
                series = "Series Seven", season = "2", episode = "9"),
            record("next-episode", watchNext = false, channel = "Continue Watching",
                series = "Series Seven", season = "2", episode = "10"),
        )
        assertEquals(listOf("watch", "next-episode"), AndroidTvMediaSource.select(records, true).map { it.contentId })
    }

    @Test fun sharedProviderSeriesIdDoesNotCollapseDifferentEpisodes() {
        val records = listOf(
            record("episode1", providerId = "series-id", series = "The Series", season = "1", episode = "1"),
            record("episode2", watchNext = false, channel = "Continue Watching",
                providerId = "series-id", series = "The Series", season = "1", episode = "2"),
        )
        assertEquals(2, AndroidTvMediaSource.select(records, true).size)
    }

    @Test fun scopedTvRowIdsDoNotCollapseUnrelatedProviderTables() {
        val repo = MediaFeedRepository()
        val first = record("1", series = "Show A", season = "1", episode = "1").item
            .copy(contentId = "tv:watchnext:1")
        val second = record("1", series = "Show B", season = "1", episode = "1").item
            .copy(contentId = "tv:ch42:1")
        repo.replaceSource("tv", listOf(first, second))
        assertEquals(2, repo.snapshot().size)
    }

    @Test fun sameProviderIdsInDifferentAppsAreNotDuplicates() {
        val records = listOf(
            record("nuvio", providerId = "same-id"),
            record("smarttube", app = "com.smarttube.tv", providerId = "same-id"),
        )
        assertEquals(2, AndroidTvMediaSource.select(records, true).size)
    }

    @Test fun missingIdentityDoesNotCollapseLegitimateEpisodes() {
        val records = listOf(record("first"), record("second"))
        assertEquals(2, AndroidTvMediaSource.select(records, true).size)
    }

    @Test fun deniedPermissionAndEmptyProvidersStayEmpty() {
        assertTrue(AndroidTvMediaSource.select(listOf(record("one")), false).isEmpty())
        assertTrue(AndroidTvMediaSource.select(emptyList(), true).isEmpty())
    }

    @Test fun noNewLabelWithoutPublicationEvidence() {
        val selected = AndroidTvMediaSource.select(listOf(
            record("suggested", type = 2L, progress = null),
            record("ordinary", watchNext = false, channel = "Featured", type = null),
        ), true)
        assertTrue(selected.isEmpty())
    }
}
