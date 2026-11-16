package dev.plume

import android.content.Context
import android.content.SharedPreferences
import android.media.tv.TvContract
import android.net.Uri

internal class Prog(val title: String, val sub: String, val progress: Int, val image: String?, val intent: String?, val pkg: String?,
                   val group: String? = pkg, val wide: Boolean = true, 
                   val contentId: String? = null, val origin: String = "tv:watchnext",
                   val category: MediaCategory = MediaCategory.UNKNOWN, val durationMillis: Long? = null,
                   val playbackPositionMillis: Long? = null, val engagedAtMillis: Long? = null,
                   val watchNextType: Long? = null, val channelName: String? = null,
                   val internalProviderId: String? = null, val seriesId: String? = null,
                   val seriesTitle: String? = null, val episodeTitle: String? = null,
                   val seasonNumber: String? = null, val episodeNumber: String? = null,
                   val channelId: Long? = null, val channelProviderId: String? = null,
                   val releaseDate: String? = null, val shortDescription: String? = null,
                   val longDescription: String? = null, val posterUri: String? = null,
                   val logoUri: String? = null)

internal fun hideKey(pkg: String?, title: String) = "$pkg/$title"

/** The TvContract read boundary; callers retain their existing worker and UI lifecycles. */
internal class TvContractMediaReader(
    private val context: Context,
    private val prefs: SharedPreferences,
) {
    fun appChannels(reportFailure: Boolean = false, onFailure: () -> Unit): List<TvPreviewChannel> = try {
        val out = ArrayList<TvPreviewChannel>()
        val cursor = context.contentResolver.query(TvContract.Channels.CONTENT_URI, null, null, null, null)
        if (cursor == null && reportFailure) android.util.Log.w("Plume", "TV channels provider returned null")
        cursor?.use { c ->
            val name = c.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)
            val pkg = c.getColumnIndex(TvContract.Channels.COLUMN_PACKAGE_NAME)
            val id = c.getColumnIndex(TvContract.Channels._ID)
            val provider = c.getColumnIndex(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_ID)
            val browsable = c.getColumnIndex(TvContract.Channels.COLUMN_BROWSABLE)
            if (name >= 0 && pkg >= 0 && id >= 0) {
                while (c.moveToNext()) {
                    val p = if (c.isNull(pkg)) null else c.getString(pkg)
                    val n = if (c.isNull(name)) null else c.getString(name)
                    if (p.isNullOrBlank() || n == null || p == context.packageName || c.isNull(id)) continue
                    out += TvPreviewChannel(c.getLong(id), p, n,
                        if (provider < 0 || c.isNull(provider)) null else c.getString(provider),
                        if (browsable < 0 || c.isNull(browsable)) null else c.getInt(browsable) == 1)
                }
            } else if (reportFailure) android.util.Log.w("Plume", "TV channels provider missing required columns")
        }
        out
    } catch (e: Exception) {
        if (reportFailure) {
            onFailure()
            android.util.Log.w("Plume", "TV channel discovery unavailable", e)
        }
        emptyList()
    }

    fun readPrograms(uri: Uri, hidden: Set<String>, group: String? = null,
                             channelName: String? = null, channelId: Long? = null,
                             channelProviderId: String? = null): List<Pair<Long, Prog>> {
        val out = ArrayList<Triple<Long, Long, Prog>>()
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor == null) android.util.Log.w("Plume", "no TV provider answer for $uri") // provider hidden from us or missing
        cursor?.use { c ->
            fun str(col: String) = c.getColumnIndex(col).let { if (it < 0 || c.isNull(it)) null else c.getString(it) }
            fun num(col: String) = c.getColumnIndex(col).let { if (it < 0 || c.isNull(it)) 0L else c.getLong(it) }
            fun optionalNum(col: String): Long? = c.getColumnIndex(col).let { if (it < 0 || c.isNull(it)) null else c.getLong(it) }
            while (c.moveToNext()) {
                if (str("browsable") == "0") continue
                val title = str("title") ?: continue
                if (hideKey(str("package_name"), title) in hidden) continue
                val season = displayNumber(str("season_display_number"))
                val episode = displayNumber(str("episode_display_number"))
                val duration = num("duration_millis")
                val position = num("last_playback_position_millis")
                val sub = listOfNotNull(
                    if (season != null && episode != null) context.getString(R.string.season_episode, season, episode) else null,
                    if (duration > 0 && position in 1000 until duration) timeLeft(duration - position) else null,
                ).joinToString(" - ")
                val progress = if (duration > 0 && position >= 1000) (position * 100 / duration).toInt().coerceIn(0, 100) else 0
                val pkg = str("package_name")
                val wide = ShowsWall.wide(str("thumbnail_uri") != null, num("poster_art_aspect_ratio").toInt())
                val engaged = num("last_engagement_time_utc_millis")
                val prog = Prog(
                    title, sub, progress, str("thumbnail_uri") ?: str("poster_art_uri"),
                    str("intent_uri"), pkg, group ?: pkg, wide,
                    contentId = str("_id"), origin = if (group == null) "tv:watchnext" else "tv:$group",
                    category = MediaFeedRules.classify(
                        if (group == null) optionalNum("watch_next_type") else null,
                        position.takeIf { it > 0 },
                        channelName.equals("Continue Watching", ignoreCase = true),
                    ),
                    durationMillis = duration.takeIf { it > 0 },
                    playbackPositionMillis = position.takeIf { it > 0 },
                    engagedAtMillis = engaged.takeIf { it > 0 },
                    watchNextType = if (group == null) optionalNum("watch_next_type") else null,
                    channelName = channelName, internalProviderId = str("internal_provider_id"),
                    seriesId = str("series_id"), seriesTitle = str("series_title"),
                    episodeTitle = str("episode_title"),
                    seasonNumber = season, episodeNumber = episode,
                    channelId = channelId, channelProviderId = channelProviderId,
                    releaseDate = str("release_date"),
                    shortDescription = str("short_description"),
                    longDescription = str("long_description"),
                    posterUri = str("poster_art_uri"), logoUri = str("logo_uri"),
                )
                out += Triple(num("weight"),
                    if (group == null) watchSortKey(num("watch_next_type"), engaged) else channelSortKey(engaged, prefs.getLong("t_$pkg", 0)), prog)
            }
        }
        return byWeight(out)
    }

    private fun timeLeft(ms: Long): String {
        val min = ms / 60_000
        return if (min >= 60) context.getString(R.string.time_left_hm, min / 60, min % 60) else context.getString(R.string.time_left_m, min)
    }
}
