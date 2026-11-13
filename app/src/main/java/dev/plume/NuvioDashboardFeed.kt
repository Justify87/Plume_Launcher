package dev.plume

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * M02's Android-free V-Design model. This is deliberately separate from Classic's
 * MediaCategory/MediaFeedRepository: their mixed Watch Next and SmartTube rules survive
 * until the single V-Design screen replaces the old renderers in later milestones.
 */
internal enum class NuvioSection { IN_PROGRESS, AVAILABLE, UPCOMING }

internal data class NuvioMediaItem(
    val stableId: String,
    val section: NuvioSection,
    val sourcePackage: String,
    val title: String,
    val seriesId: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val episodeTitle: String?,
    val shortDescription: String?,
    val longDescription: String?,
    val releaseDate: LocalDate?,
    val artworkUri: String?,
    val posterUri: String?,
    val logoUri: String?,
    val intentUri: String?,
    val progressPercent: Int?,
    /** Null for missing or potentially synthetic 100-second publisher data. */
    val durationMillis: Long?,
    val playbackPositionMillis: Long?,
    val remainingMillis: Long?,
    val lastWatchedMillis: Long?,
    /** Source position after TvContract's descending weight ordering. */
    val providerOrder: Int,
    val legacyFallback: Boolean,
) {
    /**
     * No "new" claim is possible: Nuvio publishes only a localized description,
     * not its isReleaseAlert boolean. Date alone does not prove a release alert.
     */
    val showNewBadge: Boolean get() = false

    /** A yyyy-MM-dd is a civil date, never a UTC-midnight instant. */
    fun daysUntilRelease(now: Instant, zone: ZoneId): Long? {
        val date = releaseDate ?: return null
        val days = ChronoUnit.DAYS.between(now.atZone(zone).toLocalDate(), date)
        return days.takeIf { it >= 0L }
    }
}

internal data class NuvioDashboardSection(val section: NuvioSection, val items: List<NuvioMediaItem>) {
    val count: Int get() = items.size // Full filtered list, not the mounted subset.
}

internal data class NuvioDashboardFeed(
    val inProgress: List<NuvioMediaItem>,
    val available: List<NuvioMediaItem>,
    val upcoming: List<NuvioMediaItem>,
) {
    /** Fixed visual hierarchy, including empty sections for honest empty states. */
    fun sections(): List<NuvioDashboardSection> = listOf(
        NuvioDashboardSection(NuvioSection.IN_PROGRESS, inProgress),
        NuvioDashboardSection(NuvioSection.AVAILABLE, available),
        NuvioDashboardSection(NuvioSection.UPCOMING, upcoming),
    )

    companion object { val EMPTY = NuvioDashboardFeed(emptyList(), emptyList(), emptyList()) }
}

/** Pure projection of the existing approved, independently read M01 provider channels. */
internal object NuvioMediaProjector {
    private fun part(text: String) = "${text.length}:$text"
    private fun nonblank(value: String?) = value?.trim()?.takeIf { it.isNotEmpty() }
    private fun episodeNumber(value: String?): Int? =
        nonblank(value)?.toIntOrNull()?.takeIf { it >= 0 }

    /**
     * Nuvio's actual program key is contentId_s{season}e{episode}. Watch Next uses
     * wn_ as a prefix. A fallback intent may contain S.contentId, but a generic
     * app-launch intent and TV table _id are NEVER cross-channel identity.
     */
    private fun identity(program: AndroidTvProgram): String? {
        val pkg = nonblank(program.item.sourcePackage) ?: return null
        val season = episodeNumber(program.seasonNumber)
        val episode = episodeNumber(program.episodeNumber)
        val rawProvider = nonblank(program.internalProviderId)?.let {
            if (AndroidTvMediaSource.isNuvioPackage(pkg)) it.removePrefix("wn_") else it
        }
        val intentContent = nonblank(program.item.intentUri)?.let {
            Regex("(?:^|;)S\\.contentId=([^;]+)").find(it)?.groupValues?.get(1)
        }?.takeIf { it.isNotBlank() }
        val series = nonblank(program.seriesId)
        val content = rawProvider?.let { key ->
            if (season != null && episode != null)
                key.removeSuffix("_s${season}e$episode") else key
        } ?: intentContent
        // Do not merge by title, generic intent, or episode number without a series.
        if (content == null && (series == null || season == null || episode == null)) return null
        val base = content?.let { "content:${part(it)}" } ?: "series:${part(series!!)}"
        val suffix = if (season != null && episode != null) ":s$season:e$episode" else ""
        return "${part(pkg)}:$base$suffix"
    }

    private fun releaseDate(value: String?): LocalDate? =
        nonblank(value)?.let { runCatching {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrNull() }

    private fun convert(entry: NuvioFeedEntry, section: NuvioSection): Pair<String?, NuvioMediaItem>? {
        val p = entry.program
        val pkg = nonblank(p.item.sourcePackage)?.takeIf(AndroidTvMediaSource::isNuvioPackage)
            ?: return null
        val id = identity(p)
        val duration = p.item.durationMillis?.takeIf { it > 0L }
        val position = p.item.playbackPositionMillis?.takeIf { it > 0L }
        // Nuvio writes 100000/percent*1000 when it knows a percent but NO runtime.
        // 100000 can also be a real duration; hiding remaining time is conservative.
        val actualDuration = duration?.takeUnless { it == 100_000L }
        val actualPosition = position?.takeIf { actualDuration != null && it <= actualDuration }
        val percent = p.item.progressPercent?.takeIf { it in 1..100 }
            ?: if (duration != null && position != null && position <= duration)
                ((position.toDouble() * 100.0) / duration.toDouble()).toInt().coerceIn(0, 100)
                    .takeIf { it > 0 } else null
        val remaining = if (actualDuration != null && actualPosition != null &&
            actualPosition >= 1_000L) actualDuration - actualPosition else null
        val item = NuvioMediaItem(
            stableId = id?.let { "nuvio:${part(it)}" }
                ?: "nuvio:unknown:${section.name}:${part(pkg)}:${part(p.item.stableId)}",
            section = section,
            sourcePackage = pkg,
            title = p.item.title,
            seriesId = nonblank(p.seriesId),
            seasonNumber = episodeNumber(p.seasonNumber),
            episodeNumber = episodeNumber(p.episodeNumber),
            episodeTitle = nonblank(p.episodeTitle),
            shortDescription = nonblank(p.shortDescription),
            longDescription = nonblank(p.longDescription),
            releaseDate = releaseDate(p.releaseDate),
            artworkUri = nonblank(p.item.imageUri),
            posterUri = nonblank(p.posterUri),
            logoUri = nonblank(p.logoUri),
            intentUri = nonblank(p.item.intentUri),
            progressPercent = percent,
            durationMillis = actualDuration,
            playbackPositionMillis = actualPosition,
            remainingMillis = remaining,
            lastWatchedMillis = p.item.engagedAtMillis,
            providerOrder = p.item.sourceOrder,
            legacyFallback = entry.legacyFallback,
        )
        return id to item
    }

    fun project(raw: NuvioProviderFeeds): NuvioDashboardFeed {
        val started = raw.inProgress.mapNotNull { convert(it, NuvioSection.IN_PROGRESS) }
            .sortedWith(compareByDescending<Pair<String?, NuvioMediaItem>> {
                it.second.lastWatchedMillis ?: Long.MIN_VALUE
            }.thenBy { it.second.providerOrder }.thenBy { it.second.stableId })
        val available = raw.available.mapNotNull { convert(it, NuvioSection.AVAILABLE) }
            .sortedWith(compareBy<Pair<String?, NuvioMediaItem>> { it.second.providerOrder }
                .thenBy { it.second.stableId })
        val upcoming = raw.upcoming.mapNotNull { convert(it, NuvioSection.UPCOMING) }
            .sortedWith(compareBy<Pair<String?, NuvioMediaItem>> {
                it.second.releaseDate ?: LocalDate.MAX
            }.thenBy { it.second.providerOrder }.thenBy { it.second.stableId })

        // Earlier sections win an exact episode conflict. Unknown identity cannot
        // safely be deduplicated; preserve those records rather than lose episodes.
        val seen = HashSet<String>()
        fun unique(items: List<Pair<String?, NuvioMediaItem>>): List<NuvioMediaItem> =
            items.filter { (key, _) -> key == null || seen.add(key) }.map { it.second }
        return NuvioDashboardFeed(unique(started), unique(available), unique(upcoming))
    }

    /** DST-aware next refresh boundary for M03's resumed/rebound UI; no timer/poller here. */
    fun nextLocalDayBoundary(now: Instant, zone: ZoneId): Instant =
        now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()
}
