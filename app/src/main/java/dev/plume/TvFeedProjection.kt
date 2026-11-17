package dev.plume

/** Projects provider rows without owning feed state, performing queries, or opening apps. */
internal object TvFeedProjection {
    data class Result(
        val providerFeeds: NuvioProviderFeeds,
        val dashboardFeed: NuvioDashboardFeed,
        val mediaItems: List<MediaItem>,
    )

    fun project(
        discovered: List<TvPreviewChannel>,
        feedEntries: List<Prog>,
        nuvioCategorized: List<Prog>,
        off: Set<String>,
        youtubeEnabled: Boolean,
    ): Result {
        fun tvProgram(index: Int, p: Prog): AndroidTvProgram = AndroidTvProgram(
                item = MediaItem(
                    stableId = MediaFeedRules.stableId(p.origin, p.pkg, p.contentId, p.intent, index),
                    sourceId = "tv", sourcePackage = p.pkg, title = p.title,
                    subtitle = p.sub.takeIf { it.isNotBlank() }, imageUri = p.image,
                    progressPercent = p.progress.takeIf { p.durationMillis != null && (p.playbackPositionMillis ?: 0L) >= 1000L },
                    durationMillis = p.durationMillis, playbackPositionMillis = p.playbackPositionMillis,
                    engagedAtMillis = MediaFeedRules.timestampMillis(p.engagedAtMillis),
                    intentUri = p.intent?.takeIf { it.isNotBlank() },
                    contentId = p.contentId?.let { "${p.origin}:$it" },
                    sourceLabel = p.channelName, sourceOrder = index,
                ),
                watchNext = p.origin == "tv:watchnext", watchNextType = p.watchNextType,
                channelName = p.channelName, internalProviderId = p.internalProviderId,
                seriesId = p.seriesId, seriesTitle = p.seriesTitle,
                episodeTitle = p.episodeTitle, seasonNumber = p.seasonNumber,
                episodeNumber = p.episodeNumber, channelId = p.channelId,
                channelProviderId = p.channelProviderId, releaseDate = p.releaseDate,
                shortDescription = p.shortDescription, longDescription = p.longDescription,
                posterUri = p.posterUri, logoUri = p.logoUri,
            )
        val candidates = feedEntries.mapIndexed(::tvProgram)
        val providerFeeds = AndroidTvMediaSource.selectNuvio(
            channels = discovered,
            programs = nuvioCategorized.filter { it.pkg !in off }
                .mapIndexed(::tvProgram),
            legacyPrograms = candidates,
            permissionGranted = true,
        )
        val dashboardFeed = NuvioMediaProjector.project(providerFeeds)
        val classified = AndroidTvMediaSource.select(candidates, permissionGranted = true)
            .filter { youtubeEnabled ||
                !(YouTubeSubscriptions.isSmartTube(it.sourcePackage) && it.category == MediaCategory.NEW) }
        // Limit each section independently. A full Continue row must not hide all Next Up.
        val mediaItems =
            classified.filter { it.category == MediaCategory.CONTINUE }.take(20) +
                classified.filter { it.category == MediaCategory.NEW }.take(20)
        return Result(providerFeeds, dashboardFeed, mediaItems)
    }
}
