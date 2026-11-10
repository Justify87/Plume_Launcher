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
)

internal object AndroidTvMediaSource {
    /** Actual application IDs of this Nuvio fork, including its debug and Play Store variants. */
    fun isNuvioPackage(pkg: String?): Boolean = pkg in setOf(
        "com.justify87.nuviotv.wireguard",
        "com.justify87.nuviotv.wireguard.debug",
        "com.justify87.nuviotv.wireguard.playstore",
        "com.justify87.nuviotv.wireguard.playstore.debug",
    )

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
                program.watchNext && program.watchNextType == 0L ->
                    if (!nuvio || started) MediaCategory.CONTINUE else null
                !program.watchNext && program.channelName.equals("Continue Watching", ignoreCase = true) ->
                    if (!nuvio || started) MediaCategory.CONTINUE else null
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
                program.item.copy(category = category)
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
        val seriesNames = listOfNotNull(
            program.seriesId?.trim()?.takeIf { it.isNotEmpty() },
            program.seriesTitle?.trim()?.takeIf { it.isNotEmpty() },
            // Some providers put the series name in title and the episode in episode_title.
            program.item.title.takeIf {
                !program.episodeTitle.isNullOrBlank() && it != program.episodeTitle
            },
        ).distinct()
        val season = program.seasonNumber?.trim()?.takeIf { it.isNotEmpty() }
        val episode = program.episodeNumber?.trim()?.takeIf { it.isNotEmpty() }
        program.internalProviderId?.trim()?.takeIf { it.isNotEmpty() }?.let { id ->
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
