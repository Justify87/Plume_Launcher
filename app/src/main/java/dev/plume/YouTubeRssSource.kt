package dev.plume

import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Called only on MainActivity's serial io executor; no background service or polling. */
internal class YouTubeRssSource(private val prefs: SharedPreferences) {
    private fun cached(ids: List<String>, now: Long): Pair<List<YouTubeSubscriptions.Video>, Long>? {
        if (prefs.getString("yt_cache_ids", null) != ids.joinToString(",")) return null
        val at = prefs.getLong("yt_cache_at", 0)
        if (at <= 0 || now - at !in 0L..YouTubeSubscriptions.CACHE_MS) return null
        return try {
            val data = JSONArray(prefs.getString("yt_cache", "[]"))
            (0 until minOf(data.length(), 80)).mapNotNull { index ->
                val v = data.getJSONObject(index)
                val channel = v.getString("channel")
                val id = v.getString("id")
                val published = v.getLong("published")
                if (channel !in ids || YouTubeSubscriptions.videoId(id) != id ||
                    published !in (now - YouTubeSubscriptions.MAX_AGE_MS)..now) null
                else YouTubeSubscriptions.Video(channel, id, v.getString("title"),
                    v.getString("author"), published)
            } to at
        } catch (e: Exception) { Log.w("Plume", "YouTube cache invalid", e); null }
    }
    private fun fetch(id: String, now: Long): List<YouTubeSubscriptions.Video> {
        val conn = URL("https://www.youtube.com/feeds/videos.xml?channel_id=$id")
            .openConnection() as HttpURLConnection
        conn.connectTimeout = 2500
        conn.readTimeout = 2500
        conn.instanceFollowRedirects = false
        try {
            if (conn.responseCode != 200) throw java.io.IOException("RSS HTTP ${conn.responseCode}")
            val bytes = conn.inputStream.use { stream ->
                ByteArrayOutputStream().use { output ->
                    val buffer = ByteArray(8192)
                    while (output.size() <= YouTubeSubscriptions.MAX_FEED_BYTES) {
                        val n = stream.read(buffer)
                        if (n < 0) break
                        output.write(buffer, 0, n)
                    }
                    if (output.size() > YouTubeSubscriptions.MAX_FEED_BYTES)
                        throw java.io.IOException("RSS too large")
                    output.toByteArray()
                }
            }
            return YouTubeSubscriptions.parse(bytes, id, now)
        } finally { conn.disconnect() }
    }
    fun read(force: Boolean, now: Long = System.currentTimeMillis()): Pair<List<YouTubeSubscriptions.Video>, Long?> {
        if (!prefs.getBoolean("yt_new_on", false)) return emptyList<YouTubeSubscriptions.Video>() to null
        val ids = YouTubeSubscriptions.channelIds(prefs.getString("yt_channel_ids", "").orEmpty())
            ?: return emptyList<YouTubeSubscriptions.Video>() to null
        if (ids.isEmpty()) return emptyList<YouTubeSubscriptions.Video>() to null
        val old = cached(ids, now)
        val attempt = prefs.getLong("yt_attempt", 0)
        val successAt = prefs.getLong("yt_cache_at", 0)
        val cooldown = if (successAt >= attempt) 30 * 60000L else 15 * 60000L
        if (!force && attempt > 0 && now - attempt < cooldown)
            return (old?.first.orEmpty()) to old?.second
        prefs.edit().putLong("yt_attempt", now).apply()
        val fresh = mutableListOf<YouTubeSubscriptions.Video>()
        var succeeded = false
        for (id in ids) {
            try {
                fresh += fetch(id, now)
                succeeded = true
            } catch (e: Exception) {
                Log.w("Plume", "YouTube RSS unavailable for $id", e)
                fresh += old?.first.orEmpty().filter { it.channelId == id }
            }
        }
        if (!succeeded) return old?.first.orEmpty() to old?.second
        val items = YouTubeSubscriptions.unique(fresh)
        val arr = JSONArray()
        items.forEach { v -> arr.put(JSONObject().put("channel", v.channelId)
            .put("id", v.id).put("title", v.title).put("author", v.author).put("published", v.published)) }
        prefs.edit().putString("yt_cache", arr.toString()).putString("yt_cache_ids", ids.joinToString(","))
            .putLong("yt_cache_at", now).apply()
        return items to null
    }
}
