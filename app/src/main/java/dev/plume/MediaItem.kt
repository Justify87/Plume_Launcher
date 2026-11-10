package dev.plume

/** Only categories supported by the source data are assigned. Unknown is not a recommendation. */
internal enum class MediaCategory { CONTINUE, NEW, RECOMMENDATION, UNKNOWN }
internal enum class MediaKind { EPISODE, MOVIE, VIDEO, PODCAST, UNKNOWN }

/**
 * Read-only media record shared by the Overview and app previews.
 *
 * [stableId] identifies a source record, not its title. [contentId] and [intentUri] are optional
 * identifiers from the provider. An intent URI is untrusted data, not an already validated launch
 * target; parse and validate it only at the existing launch boundary.
 *
 * Missing timestamps, duration, position, artwork, intent, and progress stay null. [sourceOrder]
 * records provider order, not a fabricated date. [packageAliases] is currently used only by the
 * offline demo for TV/phone package variants of one app.
 */
internal data class MediaItem(
    val stableId: String,
    val sourceId: String,
    val sourcePackage: String?,
    val title: String,
    val subtitle: String? = null,
    val imageUri: String? = null,
    val progressPercent: Int? = null,
    val durationMillis: Long? = null,
    val playbackPositionMillis: Long? = null,
    val engagedAtMillis: Long? = null,
    val publishedAtMillis: Long? = null,
    val category: MediaCategory = MediaCategory.UNKNOWN,
    val intentUri: String? = null,
    val contentId: String? = null,
    val sourceLabel: String? = null,
    val kind: MediaKind = MediaKind.UNKNOWN,
    val packageAliases: Set<String> = emptySet(),
    val sourceOrder: Int = 0,
) {
    init {
        require(stableId.isNotBlank() && sourceId.isNotBlank())
        require(progressPercent == null || progressPercent in 0..100)
        require(durationMillis == null || durationMillis >= 0)
        require(playbackPositionMillis == null || playbackPositionMillis >= 0)
    }
}
