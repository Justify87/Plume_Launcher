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

    private val nuvio = "com.justify87.nuviotv.wireguard"

    @Test fun legacyNuvioZeroProgressIsNotPretendedToBeStarted() {
        val selected = AndroidTvMediaSource.select(listOf(
            record("old-next", app = nuvio, progress = null, season = "2", episode = "3"),
            record("old-preview", app = nuvio, watchNext = false,
                channel = "Continue Watching", progress = null, season = "2", episode = "3"),
            record("started", app = nuvio, progress = 32_000L, season = "1", episode = "1"),
        ), true)
        assertEquals(listOf("started"), selected.map { it.contentId })
        assertEquals(MediaCategory.CONTINUE, selected.single().category)
    }

    @Test fun newNuvioVersionSeparatesAiredNextUpFromResume() {
        val selected = AndroidTvMediaSource.select(listOf(
            record("resumed", app = nuvio, progress = 55_000L, season = "1", episode = "4"),
            record("next", app = nuvio, watchNext = false, type = null,
                channel = "Next Up", progress = null, series = "Example",
                season = "1", episode = "5", episodeTitle = "Next episode"),
        ), true)
        assertEquals(listOf(MediaCategory.CONTINUE, MediaCategory.NEW), selected.map { it.category })
        assertEquals(listOf("resumed", "next"), selected.map { it.contentId })
    }

    @Test fun newEpisodesNeedNuvioChannelAndEpisodeNumbers() {
        val selected = AndroidTvMediaSource.select(listOf(
            record("missing-episode", app = nuvio, watchNext = false,
                channel = "Next Up", type = null, progress = null, season = "1"),
            record("unknown-channel", app = nuvio, watchNext = false,
                channel = "Suggestions", type = null, progress = null, season = "1", episode = "2"),
            record("other-app", app = "org.example.player", watchNext = false,
                channel = "Next Up", type = null, progress = null, season = "1", episode = "2"),
            record("legacy-nuvio", app = nuvio, type = 1L, progress = null,
                season = "1", episode = "2"),
        ), true)
        assertTrue(selected.isEmpty())
    }

    @Test fun identicalEpisodeTitlesAcrossSeasonsRemainDistinct() {
        val selected = AndroidTvMediaSource.select(listOf(
            record("s1e1", app = nuvio, watchNext = false, channel = "Next Up",
                type = null, progress = null, title = "A Show", series = "A Show",
                season = "1", episode = "1", episodeTitle = "Pilot"),
            record("s2e1", app = nuvio, watchNext = false, channel = "Next Up",
                type = null, progress = null, title = "A Show", series = "A Show",
                season = "2", episode = "1", episodeTitle = "Pilot"),
        ), true)
        assertEquals(listOf("s1e1", "s2e1"), selected.map { it.contentId })
    }

    @Test fun startedEpisodeWinsOverNextUpDuplicateEvenWhenSecond() {
        val selected = AndroidTvMediaSource.select(listOf(
            record("next", app = nuvio, watchNext = false, channel = "Next Up", type = null,
                progress = null, title = "A Show", series = "A Show",
                season = "2", episode = "3", episodeTitle = "Third"),
            record("started", app = nuvio, progress = 45_000L,
                title = "A Show", series = "A Show", season = "2", episode = "3",
                episodeTitle = "Third"),
        ), true)
        assertEquals(listOf("started"), selected.map { it.contentId })
        assertEquals(MediaCategory.CONTINUE, selected.single().category)
    }

    @Test fun noNewChannelOrMissingPermissionMeansNoNewEpisodes() {
        assertTrue(AndroidTvMediaSource.select(emptyList(), true).isEmpty())
        val legacy = listOf(record("resume", app = nuvio, progress = 10_000L),
            record("old-next", app = nuvio, progress = null))
        assertTrue(AndroidTvMediaSource.select(legacy, true).none { it.category == MediaCategory.NEW })
        assertTrue(AndroidTvMediaSource.select(legacy, false).isEmpty())
    }

    @Test fun exactNuvioEpisodeDeepLinkIsPreserved() {
        val intent = "intent:#Intent;action=android.intent.action.VIEW;" +
            "component=com.justify87.nuviotv.wireguard/com.nuvio.tv.MainActivity;" +
            "S.contentId=series-3;i.season=1;i.episode=7;end"
        val selected = AndroidTvMediaSource.select(listOf(
            record("link", app = nuvio, watchNext = false, channel = "Next Up", type = null,
                progress = null, series = "Example", season = "1", episode = "7",
                intent = intent),
        ), true)
        assertEquals(intent, selected.single().intentUri)
    }

    @Test fun nuvioWatchNextAndPreviewHaveDifferentPrefixesButSameMovie() {
        val selected = AndroidTvMediaSource.select(listOf(
            record("watch-row", app = nuvio, title = "A Movie",
                providerId = "wn_movie-42", progress = 25_000L),
            record("preview-row", app = nuvio, title = "A Movie", watchNext = false,
                channel = "Continue Watching", type = null,
                providerId = "movie-42", progress = 25_000L),
        ), true)
        assertEquals(listOf("watch-row"), selected.map { it.contentId })
    }

    @Test fun nuvioEpisodesDedupeWithoutEpisodeTitleMetadata() {
        val selected = AndroidTvMediaSource.select(listOf(
            record("watch-row", app = nuvio, title = "Show",
                providerId = "wn_show-1_s2e8", season = "2", episode = "8",
                episodeTitle = null, progress = 11_000L),
            record("channel-row", app = nuvio, title = "Show", watchNext = false,
                channel = "Continue Watching", providerId = "show-1_s2e8",
                season = "2", episode = "8", episodeTitle = null, progress = 11_000L),
        ), true)
        assertEquals(1, selected.size)
    }

    @Test fun supportedNuvioBuildVariantsHaveExactPackageIdentity() {
        assertTrue(AndroidTvMediaSource.isNuvioPackage(nuvio))
        assertTrue(AndroidTvMediaSource.isNuvioPackage(nuvio + ".debug"))
        assertTrue(AndroidTvMediaSource.isNuvioPackage(nuvio + ".playstore"))
        assertTrue(!AndroidTvMediaSource.isNuvioPackage("com.nuvio.tv"))
        assertTrue(!AndroidTvMediaSource.isNuvioPackage("com.justify87.nuviotv.wireguard.fake"))
    }
}
