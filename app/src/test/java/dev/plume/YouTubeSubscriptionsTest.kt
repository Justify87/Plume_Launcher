package dev.plume

import org.junit.Assert.*
import org.junit.Test

class YouTubeSubscriptionsTest {
    private val channel = "UC1234567890123456789012"
    private val now = java.time.Instant.parse("2026-11-11T00:00:00Z").toEpochMilli()
    private fun atom(date: String?) = """
      <feed xmlns="http://www.w3.org/2005/Atom" xmlns:yt="http://www.youtube.com/xml/schemas/2015">
      <entry><yt:videoId>abcDEF12345</yt:videoId><yt:channelId>$channel</yt:channelId>
      <title>Example</title><author><name>Author</name></author>
      ${date?.let { "<published>$it</published>" } ?: ""}</entry></feed>
    """.trimIndent().toByteArray()
    @Test fun emptyInvalidAndTooManyChannelIds() {
        assertEquals(emptyList<String>(), YouTubeSubscriptions.channelIds(""))
        assertNull(YouTubeSubscriptions.channelIds("not-a-channel"))
        assertNull(YouTubeSubscriptions.channelIds((1..13).joinToString(",") { "UC" + it.toString().padStart(22, '0') }))
        assertEquals(listOf(channel), YouTubeSubscriptions.channelIds("$channel;$channel"))
    }
    @Test fun genuineFeedHasStableMetadataAndMissingDateIsNotNew() {
        val result = YouTubeSubscriptions.parse(atom("2026-11-10T12:00:00Z"), channel, now).single()
        assertEquals("abcDEF12345", result.id)
        assertEquals("Author", result.author)
        assertEquals("yt:abcDEF12345", YouTubeSubscriptions.media(listOf(result), null).single().contentId)
        assertTrue(YouTubeSubscriptions.parse(atom(null), channel, now).isEmpty())
    }
    @Test fun oldFutureOrWrongChannelRejected() {
        assertTrue(YouTubeSubscriptions.parse(atom("2026-08-01T00:00:00Z"), channel, now).isEmpty())
        assertTrue(YouTubeSubscriptions.parse(atom("2026-12-01T00:00:00Z"), channel, now).isEmpty())
        assertTrue(YouTubeSubscriptions.parse(atom("2026-11-10T12:00:00Z"), "UCabcdefghijklmnopqrstuv", now).isEmpty())
        assertTrue(YouTubeSubscriptions.parse(ByteArray(262145), channel, now).isEmpty())
    }
    @Test fun linkValidationAndCrossSourceDedup() {
        assertEquals("abcDEF12345", YouTubeSubscriptions.videoId("https://www.youtube.com/watch?v=abcDEF12345"))
        assertNull(YouTubeSubscriptions.videoId("https://youtube.com/"))
        val item = YouTubeSubscriptions.media(YouTubeSubscriptions.parse(atom("2026-11-10T12:00:00Z"),channel,now), null).single()
        assertEquals(1, YouTubeSubscriptions.dedupe(listOf(item, item.copy(sourceId="tv"))).size)
    }
    @Test fun historyIsNotNewAndInstallIsNotAssumed() {
        assertTrue(YouTubeSubscriptions.isSubscriptions("Subscriptions"))
        assertFalse(YouTubeSubscriptions.isSubscriptions("History"))
        assertFalse(YouTubeSubscriptions.isSmartTube(null))
    }
    @Test fun rssMetadataWinsAcrossTvAndRssWithoutHidingTheSmartTubeAppPage() {
        val video = YouTubeSubscriptions.Video(channel, "abcDEF12345", "new", "Uploader", now - 60_000)
        val rss = YouTubeSubscriptions.media(listOf(video), null).single()
            .copy(packageAliases = setOf("org.smarttube.beta"))
        val tv = rss.copy(stableId = "tv-row", sourceId = "tv", sourcePackage = "org.smarttube.beta",
            publishedAtMillis = null, title = "TV title", packageAliases = emptySet())
        val repo = MediaFeedRepository()
        repo.replaceSource("tv", listOf(tv))
        repo.replaceSource(YouTubeSubscriptions.SOURCE_ID, listOf(rss))
        val overview = HomePageContent.sections(repo, HomePageState.Overview).flatMap { it.items }
        assertEquals(1, overview.size)
        assertEquals(YouTubeSubscriptions.SOURCE_ID, overview.single().sourceId)
        val smartTubePage = HomePageContent.sections(repo, HomePageState.App("org.smarttube.beta"))
            .flatMap { it.items }
        assertEquals(1, smartTubePage.size)
    }

    @Test fun rejectsDoctypeAndExternalEntities() {
        val xml = """<!DOCTYPE feed [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
            <feed xmlns="http://www.w3.org/2005/Atom"><entry><title>&xxe;</title></entry></feed>"""
        try {
            assertTrue(YouTubeSubscriptions.parse(xml.toByteArray(), channel, now).isEmpty())
        } catch (_: org.xml.sax.SAXException) {
            // Secure XML parser refuses the DTD entirely.
        }
    }
}
