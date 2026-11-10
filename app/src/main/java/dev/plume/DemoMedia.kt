package dev.plume

/**
 * Strictly offline examples for M03. These are not Watch Next entries, viewing history or
 * suggestions from a provider. Never merge them into HomeWatch / TvContract or persist them.
 */
internal object DemoMedia {
    enum class Section { CONTINUE, NEW }
    enum class Kind { EPISODE, MOVIE, VIDEO, PODCAST }

    data class Card(
        val id: String,
        val section: Section,
        val kind: Kind,
        val title: String,
        val subtitle: String,
        val source: String,
        val packages: Set<String>,
        val progress: Int? = null,
    ) {
        init { require(progress == null || progress in 0..100) }
    }

    private val plex = setOf("com.plexapp.android", "com.plexapp.android.tv")
    private val youtube = setOf("com.google.android.youtube.tv")
    private val spotify = setOf("com.spotify.tv.android")
    private val jellyfin = setOf("org.jellyfin.androidtv")

    val examples: List<Card> = listOf(
        Card("episode", Section.CONTINUE, Kind.EPISODE, "The Last Signal", "Season 2 · Episode 4", "Plex", plex, 64),
        Card("movie", Section.CONTINUE, Kind.MOVIE, "A Journey Beyond the Stars", "Film · 2025", "Jellyfin", jellyfin, 32),
        Card("video", Section.CONTINUE, Kind.VIDEO, "How telescopes see the universe", "Video · 18 min", "YouTube", youtube, 43),
        Card("podcast", Section.CONTINUE, Kind.PODCAST, "The science of dreaming", "Podcast · Episode 12", "Spotify", spotify, 71),
        Card("episode-2", Section.CONTINUE, Kind.EPISODE, "The Northern Lights", "Season 1 · Episode 8", "Plex", plex, 19),
        Card("video-2", Section.CONTINUE, Kind.VIDEO, "Photography with natural light", "Video · 24 min", "YouTube", youtube, 52),
        Card("new-video", Section.NEW, Kind.VIDEO, "A new lens in the field", "Video · Just added", "YouTube", youtube),
        Card("new-podcast", Section.NEW, Kind.PODCAST, "Science Dispatch", "Podcast · New episode", "Spotify", spotify),
        Card("new-movie", Section.NEW, Kind.MOVIE, "Across the Ocean", "Film · New arrival", "Plex", plex),
        Card("new-video-2", Section.NEW, Kind.VIDEO, "Why the night sky changes", "Video · Just added", "YouTube", youtube),
    )

    /** The prototype reads the same MediaItem shape as future live sources. No provider queries. */
    fun asMediaItems(): List<MediaItem> = examples.mapIndexed { index, card ->
        val pkg = card.packages.firstOrNull()
        MediaItem(
            stableId = MediaFeedRules.stableId("demo", pkg, card.id, null, index),
            sourceId = "demo", sourcePackage = pkg, packageAliases = card.packages,
            contentId = card.id, title = card.title, subtitle = card.subtitle,
            progressPercent = card.progress,
            category = when (card.section) {
                Section.CONTINUE -> MediaCategory.CONTINUE
                Section.NEW -> MediaCategory.NEW
            },
            kind = when (card.kind) {
                Kind.EPISODE -> MediaKind.EPISODE
                Kind.MOVIE -> MediaKind.MOVIE
                Kind.VIDEO -> MediaKind.VIDEO
                Kind.PODCAST -> MediaKind.PODCAST
            },
            sourceLabel = card.source, sourceOrder = index,
        )
    }

    /** Null means the common Overview. A package shows only its own example cards. */
    fun rowsFor(packageName: String?): List<List<Card>> {
        val selected = examples.filter { packageName == null || packageName in it.packages }
        return Section.values().map { section -> selected.filter { it.section == section } }
    }

    /** Visible slots plus one on each side, independent of Android/View state. */
    fun visibleWindow(count: Int, scrollX: Int, viewportWidth: Int, pitch: Int): IntRange {
        if (count <= 0) return IntRange.EMPTY
        if (pitch <= 0 || viewportWidth <= 0) return 0..minOf(1, count - 1)
        val first = (scrollX.coerceAtLeast(0) / pitch).coerceIn(0, count - 1)
        val last = ((scrollX.coerceAtLeast(0) + viewportWidth - 1) / pitch).coerceIn(0, count - 1)
        return (first - 1).coerceAtLeast(0)..(last + 1).coerceAtMost(count - 1)
    }
}
