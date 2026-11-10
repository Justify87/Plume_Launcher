package dev.plume

import java.io.ByteArrayInputStream
import java.time.Instant
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

/** YouTube data is opt-in, public and independent of SmartTube account access. */
internal object YouTubeSubscriptions {
    const val SOURCE_ID = "youtube:rss"
    const val MAX_CHANNELS = 12
    const val MAX_FEED_BYTES = 262144
    const val MAX_AGE_MS = 30L * 86400000
    const val CACHE_MS = 72L * 3600000
    val packages = listOf("org.smarttube.beta", "org.smarttube.stable",
        "com.liskovsoft.smarttubetv.beta", "com.teamsmart.videomanager.tv", "app.smarttube", "app.smarttube.fdroid")
    private val channel = Regex("UC[A-Za-z0-9_-]{22}")
    private val video = Regex("[A-Za-z0-9_-]{11}")
    private val url = Regex("(?:[?&]v=|youtu\\.be/|/shorts/|/live/)([A-Za-z0-9_-]{11})(?:[^A-Za-z0-9_-]|$)")
    fun isSmartTube(pkg: String?) = pkg in packages
    fun isSubscriptions(name: String?) = name?.trim()?.lowercase() in
        setOf("subscriptions", "abonnements", "abos")
    fun channelIds(value: String): List<String>? {
        val ids = value.split(Regex("[,;\\s]+")).filter { it.isNotBlank() }
        if (ids.any { !channel.matches(it) }) return null
        return ids.distinct().takeIf { it.size <= MAX_CHANNELS }
    }
    fun videoId(value: String?): String? {
        val raw = value?.trim() ?: return null
        return if (video.matches(raw)) raw else url.find(raw)?.groupValues?.get(1)
    }
    data class Video(val channelId: String, val id: String, val title: String, val author: String, val published: Long)
    fun parse(xml: ByteArray, requested: String, now: Long): List<Video> {
        if (!channel.matches(requested) || xml.size > MAX_FEED_BYTES) return emptyList()
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            isXIncludeAware = false
            isExpandEntityReferences = false
        }
        val entries = factory.newDocumentBuilder().parse(ByteArrayInputStream(xml))
            .getElementsByTagNameNS("http://www.w3.org/2005/Atom", "entry")
        val atom = "http://www.w3.org/2005/Atom"
        val yt = "http://www.youtube.com/xml/schemas/2015"
        fun field(e: org.w3c.dom.Element, ns: String, key: String) =
            e.getElementsByTagNameNS(ns, key).item(0)?.textContent?.trim()?.takeIf { it.isNotBlank() }
        return (0 until minOf(entries.length, 30)).mapNotNull { index ->
            val e = entries.item(index) as? org.w3c.dom.Element ?: return@mapNotNull null
            val id = field(e, yt, "videoId")?.takeIf(video::matches) ?: return@mapNotNull null
            if (field(e, yt, "channelId") != requested) return@mapNotNull null
            val title = field(e, atom, "title")?.take(180) ?: return@mapNotNull null
            val author = field(e, atom, "name")?.take(100) ?: requested
            val published = try { Instant.parse(field(e, atom, "published") ?: return@mapNotNull null).toEpochMilli() }
                catch (_: Exception) { return@mapNotNull null }
            if (published > now || now - published > MAX_AGE_MS) return@mapNotNull null
            Video(requested, id, title, author, published)
        }.distinctBy { it.id }.sortedByDescending { it.published }
    }
    fun unique(videos: List<Video>) = videos.distinctBy { it.id }
        .sortedWith(compareByDescending<Video> { it.published }.thenBy { it.id }).take(80)
    fun dedupe(items: List<MediaItem>): List<MediaItem> {
        val seen = HashSet<String>()
        return items.filter { it.contentId?.takeIf { id -> id.startsWith("yt:") }?.let(seen::add) ?: true }
    }
    fun media(videos: List<Video>, pkg: String?, staleHours: Long? = null) =
        unique(videos).mapIndexed { i, v -> MediaItem(
            stableId = "yt:${v.id}", sourceId = SOURCE_ID, sourcePackage = pkg,
            title = v.title, subtitle = v.author + (staleHours?.let { " · Feed ${it}h alt" } ?: ""),
            imageUri = "https://i.ytimg.com/vi/${v.id}/hqdefault.jpg",
            publishedAtMillis = v.published, category = MediaCategory.NEW,
            intentUri = "https://www.youtube.com/watch?v=${v.id}",
            contentId = "yt:${v.id}", sourceLabel = "YouTube", kind = MediaKind.VIDEO, sourceOrder = i,
        ) }
}
