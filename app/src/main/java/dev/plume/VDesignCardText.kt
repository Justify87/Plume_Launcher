package dev.plume

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

/** M04 presentation values: no invented duration, episode number, release time or "new" claim. */
internal object VDesignCardText {
    fun episode(item: NuvioMediaItem): String? {
        val parts = listOfNotNull(item.seasonNumber?.let { "S$it" },
            item.episodeNumber?.let { "F$it" })
        val number = parts.joinToString(" · ").takeIf { it.isNotEmpty() }
        val name = item.episodeTitle?.trim()?.takeIf { it.isNotEmpty() }
        return listOfNotNull(number, name).joinToString("  ").takeIf { it.isNotEmpty() }
    }

    fun remaining(item: NuvioMediaItem): String? {
        val millis = item.remainingMillis?.takeIf { it > 0L } ?: return null
        if (item.durationMillis == null) return null
        val mins = ceil(millis / 60_000.0).toLong()
        return "$mins min verbleibend"
    }

    fun date(item: NuvioMediaItem, locale: Locale): String? =
        item.releaseDate?.format(DateTimeFormatter.ofPattern("EEE, d. MMM", locale))

    fun countdown(item: NuvioMediaItem, now: Instant, zone: ZoneId): String? {
        val days = item.daysUntilRelease(now, zone) ?: return null
        return when (days) {
            0L -> "Heute"
            1L -> "in 1 Tag"
            else -> "in $days Tagen"
        }
    }

    /** Only valid content:// or HTTP(S) authorities, never arbitrary file URLs or URI userinfo. */
    fun supportedArtwork(uri: String?): String? = uri?.trim()?.takeIf { value ->
        val parsed = runCatching { java.net.URI(value) }.getOrNull()
        when (parsed?.scheme?.lowercase(Locale.ROOT)) {
            "http", "https" -> !parsed.host.isNullOrBlank() && parsed.userInfo == null
            "content" -> !parsed.authority.isNullOrBlank() && parsed.userInfo == null
            else -> false
        }
    }

    fun artwork(item: NuvioMediaItem): String? =
        supportedArtwork(item.artworkUri) ?: supportedArtwork(item.posterUri)
            ?: supportedArtwork(item.logoUri)
}
