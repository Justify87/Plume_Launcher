package dev.plume

/**
 * Adapts the records returned by MainActivity's existing TvContract query to the shared feed.
 * No provider access, worker, polling or Activity dependencies live here.
 */
internal data class AndroidTvProgram(
    val item: MediaItem,
    val watchNext: Boolean,
    val watchNextType: Long? = null,
    val channelName: String? = null,
    val internalProviderId: String? = null,
    val seriesId: String? = null,
    val seriesTitle: String? = null,
    val episodeTitle: String? = null,
    val seasonNumber: String? = null,
    val episodeNumber: String? = null,
    val channelId: Long? = null,
    val channelProviderId: String? = null,
    val releaseDate: String? = null,
)

/** Channel ID, real package, localized label, provider key, and approved visibility. */
internal data class TvPreviewChannel(
    val id: Long,
    val packageName: String,
    val displayName: String,
    val channelProviderId: String?,
    val browsable: Boolean?,
) {
    // Keep old Triple-style settings and wallpaper callers intact.
    val first: Long get() = id
    val second: String get() = packageName
    val third: String get() = displayName
}
internal enum class NuvioChannelKind { LEGACY, IN_PROGRESS, AVAILABLE, UPCOMING }
internal data class NuvioFeedEntry(
    val program: AndroidTvProgram,
    val kind: NuvioChannelKind,
    val legacyFallback: Boolean = false,
)
internal data class NuvioProviderFeeds(
    val inProgress: List<NuvioFeedEntry>,
    val available: List<NuvioFeedEntry>,
    val upcoming: List<NuvioFeedEntry>,
) {
    companion object { val EMPTY = NuvioProviderFeeds(emptyList(), emptyList(), emptyList()) }
}

internal object AndroidTvMediaSource {
    val categorizedNuvioKinds = setOf(NuvioChannelKind.IN_PROGRESS,
        NuvioChannelKind.AVAILABLE, NuvioChannelKind.UPCOMING)
    /** Actual application IDs of this Nuvio fork, including its debug and Play Store variants. */
    /** Current actual full/Play Store/debug/benchmark IDs plus historical fork installations. */
    fun isNuvioPackage(pkg: String?): Boolean = pkg in setOf(
        "com.nuvio.tv", "com.nuviodebug.com", "com.nuvio.app", "com.nuvio.appdebug",
        "com.nuvio.tv.debug", "com.nuvio.app.debug",
    ) || isLegacyNuvioPackage(pkg)

    fun isLegacyNuvioPackage(pkg: String?): Boolean = pkg in setOf(
        "com.justify87.nuviotv.wireguard",
        "com.justify87.nuviotv.wireguard.debug",
        "com.justify87.nuviotv.wireguard.playstore",
        "com.justify87.nuviotv.wireguard.playstore.debug",
    )

    /** Based solely on exact TvChannelKind provider key and the real owning package. */
    fun nuvioKind(channel: TvPreviewChannel): NuvioChannelKind? {
        val pkg = channel.packageName
        if (channel.id <= 0L || !isNuvioPackage(pkg)) return null
        return when (channel.channelProviderId) {
            pkg -> NuvioChannelKind.LEGACY
            "$pkg:in-progress" -> NuvioChannelKind.IN_PROGRESS
            "$pkg:available" -> NuvioChannelKind.AVAILABLE
            "$pkg:upcoming" -> NuvioChannelKind.UPCOMING
            else -> null
        }
    }

    /**
     * A separate M01 snapshot, not rendered by the existing Classic/Overview.
     * Unknown/unapproved channels never contribute; fallback contains only started legacy
     * Watch Next or classic mixed-channel entries, never supposed 'available' episodes.
     */
    fun selectNuvio(
        channels: List<TvPreviewChannel>,
        programs: List<AndroidTvProgram>,
        legacyPrograms: List<AndroidTvProgram>,
        permissionGranted: Boolean,
    ): NuvioProviderFeeds {
        if (!permissionGranted) return NuvioProviderFeeds.EMPTY
        val active = channels.filter { it.browsable == true &&
            nuvioKind(it) in categorizedNuvioKinds }.associateBy { it.id }
        val entries = programs.mapNotNull { program ->
            val channel = active[program.channelId] ?: return@mapNotNull null
            val kind = nuvioKind(channel) ?: return@mapNotNull null
            if (program.watchNext || program.item.sourcePackage != channel.packageName ||
                program.channelProviderId != channel.channelProviderId) return@mapNotNull null
            NuvioFeedEntry(program, kind)
        }
        val progress = entries.filter { it.kind == NuvioChannelKind.IN_PROGRESS }.toMutableList()
        if (active.values.none { nuvioKind(it) == NuvioChannelKind.IN_PROGRESS }) {
            // Prefer a real Watch Next record when it duplicates a Classic preview. Only
            // identity + explicit episode numbers permit fallback dedupe; M02 owns full dedupe.
            val seenFallback = HashSet<String>()
            progress += legacyPrograms.filter { p ->
                isNuvioPackage(p.item.sourcePackage) &&
                    (p.item.playbackPositionMillis ?: 0L) >= 1_000L &&
                    ((p.watchNext && p.watchNextType == 0L) ||
                        (!p.watchNext && p.channelProviderId == p.item.sourcePackage) ||
                        (!p.watchNext && p.channelProviderId == null &&
                            p.channelName.equals("Continue Watching", ignoreCase = true)))
            }.sortedByDescending { it.watchNext }.filter { p ->
                val id = p.internalProviderId?.trim()?.removePrefix("wn_")?.takeIf { it.isNotEmpty() }
                val season = p.seasonNumber?.trim()?.takeIf { it.isNotEmpty() }
                val episode = p.episodeNumber?.trim()?.takeIf { it.isNotEmpty() }
                if (id == null || season == null || episode == null) true
                else seenFallback.add(listOf(p.item.sourcePackage, id, season, episode)
                    .joinToString("\u0000"))
            }.map { NuvioFeedEntry(it, NuvioChannelKind.IN_PROGRESS, legacyFallback = true) }
        }
        return NuvioProviderFeeds(progress.toList(),
            entries.filter { it.kind == NuvioChannelKind.AVAILABLE },
            entries.filter { it.kind == NuvioChannelKind.UPCOMING })
    }

    /**
     * Nuvio's legacy Watch Next and Continue Watching channel mix in unstarted Next Up
     * episodes, even though Watch Next marks all of them as CONTINUE. Only its entries
     * with actual playback progress are resumable. Newer Nuvio versions additionally
     * publish already-aired Next Up episodes in a distinct channel called "Next Up".
     *
     * Other apps retain the existing M06 rules. A generic Watch Next NEXT/NEW flag or
     * engagement timestamp is never treated as proof that an episode was released.
     */
    fun select(programs: List<AndroidTvProgram>, permissionGranted: Boolean): List<MediaItem> {
        if (!permissionGranted) return emptyList()
        val classified = programs.mapNotNull { program ->
            val nuvio = isNuvioPackage(program.item.sourcePackage)
            val started = (program.item.playbackPositionMillis ?: 0L) >= 1000L
            val category = when {
                !program.watchNext && nuvio && program.channelProviderId in setOf(
                    "${program.item.sourcePackage}:in-progress",
                    "${program.item.sourcePackage}:available",
                    "${program.item.sourcePackage}:upcoming",
                ) -> null
                !program.watchNext && nuvio &&
                    program.channelProviderId == program.item.sourcePackage ->
                    if (started) MediaCategory.CONTINUE else null
                program.watchNext && program.watchNextType == 0L ->
                    if (!nuvio || started) MediaCategory.CONTINUE else null
                !program.watchNext && program.channelName.equals("Continue Watching", ignoreCase = true) ->
                    if (!nuvio || started) MediaCategory.CONTINUE else null
                !program.watchNext && YouTubeSubscriptions.isSmartTube(program.item.sourcePackage) &&
                    YouTubeSubscriptions.isSubscriptions(program.channelName) &&
                    (YouTubeSubscriptions.videoId(program.internalProviderId)
                        ?: YouTubeSubscriptions.videoId(program.item.intentUri)) != null -> MediaCategory.NEW
                !program.watchNext && nuvio && program.channelName.equals("Next Up", ignoreCase = true) &&
                    (program.seasonNumber?.toIntOrNull() ?: -1) >= 0 &&
                    (program.episodeNumber?.toIntOrNull() ?: 0) > 0 && !started -> MediaCategory.NEW
                else -> null
            }
            if (category == null) null else program to category
        }
        // A started episode always wins if a provider publishes it in both channels.
        val seen = HashSet<String>()
        return classified.sortedBy { if (it.second == MediaCategory.CONTINUE) 0 else 1 }
            .mapNotNull { (program, category) ->
                val keys = duplicateKeys(program)
                if (keys.any { it in seen }) return@mapNotNull null
                seen.addAll(keys)
                if (category == MediaCategory.NEW && YouTubeSubscriptions.isSmartTube(program.item.sourcePackage)) {
                    val id = YouTubeSubscriptions.videoId(program.internalProviderId)
                        ?: YouTubeSubscriptions.videoId(program.item.intentUri)!!
                    program.item.copy(category = category, stableId = "yt:$id",
                        contentId = "yt:$id", kind = MediaKind.VIDEO)
                } else program.item.copy(category = category)
            }
    }

    /**
     * TV _id belongs to a particular table, not to the episode, so never use it for merging.
     * Cross-channel duplicates require app-scoped provider identity or a known series and episode.
     * Titles alone, and generic app-launch intents, cannot establish identity.
     */
    private fun duplicateKeys(program: AndroidTvProgram): List<String> {
        val pkg = program.item.sourcePackage?.trim()?.takeIf { it.isNotEmpty() } ?: return emptyList()
        fun part(value: String) = "${value.length}:$value"
        val prefix = part(pkg)
        val keys = ArrayList<String>()
        val season = program.seasonNumber?.trim()?.takeIf { it.isNotEmpty() }
        val episode = program.episodeNumber?.trim()?.takeIf { it.isNotEmpty() }
        val seriesNames = listOfNotNull(
            program.seriesId?.trim()?.takeIf { it.isNotEmpty() },
            program.seriesTitle?.trim()?.takeIf { it.isNotEmpty() },
            // Some providers put the series name in title and the episode in episode_title.
            program.item.title.takeIf {
                // When the publisher identifies a series explicitly, a shared episode title
                // ("Pilot") cannot be used as a second series key across unrelated shows.
                program.seriesId.isNullOrBlank() && program.seriesTitle.isNullOrBlank() &&
                    ((!program.episodeTitle.isNullOrBlank() && it != program.episodeTitle) ||
                        (isNuvioPackage(pkg) && season != null && episode != null && it.isNotBlank()))
            },
        ).distinct()
        program.internalProviderId?.trim()?.takeIf { it.isNotEmpty() }?.let { rawId ->
            // Nuvio prefixes Watch Next IDs with "wn_" but not Preview IDs.
            // Nuvio also scopes TV-episode keys to season/episode itself.
            val id = if (isNuvioPackage(pkg)) rawId.removePrefix("wn_") else rawId
            // Internal IDs can identify an entire series. Never merge other episodes or series.
            val suffix = when {
                season != null && episode != null -> ":${part(season)}:${part(episode)}"
                program.episodeTitle != null -> ":${part(program.episodeTitle)}"
                else -> ""
            }
            if (seriesNames.isEmpty()) keys += "provider:$prefix:${part(id)}$suffix"
            else for (series in seriesNames) keys += "provider:$prefix:${part(id)}:${part(series)}$suffix"
        }
        for (series in seriesNames) {
            if (season != null && episode != null) {
                keys += "episode:$prefix:${part(series)}:${part(season)}:${part(episode)}"
            }
            // Exact media deep links can disambiguate a series even without season/episode data.
            // Do not match plain launcher intents or match across unknown series.
            val uri = program.item.intentUri
            if (uri != null && uri.isNotBlank() && hasContentDeepLink(uri)) {
                val episodeSuffix = if (season != null && episode != null)
                    ":${part(season)}:${part(episode)}" else ""
                keys += "link:$prefix:${part(series)}$episodeSuffix:${part(uri)}"
            }
        }
        return keys
    }

    private fun hasContentDeepLink(uri: String): Boolean {
        // A generic intent:#Intent;package=...;end cannot identify an episode.
        val target = uri.substringBefore("#Intent;")
        return target.contains("://") && target.substringAfter("://").isNotBlank()
    }
}
