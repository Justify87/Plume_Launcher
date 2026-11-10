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
    /**
     * A Watch Next entry is resumable only if its type is CONTINUE (0).
     * A Preview entry is resumable only from an explicitly named Continue Watching channel.
     * The TV provider's "new" flag does not prove a new release date, so M06 publishes no NEW
     * items until a source supplies verifiable release metadata. Unknown rows stay out of both
     * Overview sections but remain available to Classic through its existing Prog path.
     */
    fun select(programs: List<AndroidTvProgram>, permissionGranted: Boolean): List<MediaItem> {
        if (!permissionGranted) return emptyList()
        val known = HashSet<String>()
        return programs.mapNotNull { program ->
            val resumable = if (program.watchNext) program.watchNextType == 0L
                else program.channelName.equals("Continue Watching", ignoreCase = true)
            if (!resumable) return@mapNotNull null
            val keys = duplicateKeys(program)
            if (keys.any { it in known }) return@mapNotNull null
            known.addAll(keys)
            program.item.copy(category = MediaCategory.CONTINUE)
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
