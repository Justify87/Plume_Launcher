package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class ContinueWatchingTest {
    @Test fun newestFirstAndThreePerAppAhead() {
        val netflix = (1..10).map { 100L - it to "netflix/$it" }
        val stremio = (1..4).map { 95L to "stremio/$it" }
        val nuvio = (1..2).map { 0L to "nuvio/$it" } // never opened from Plume
        val shown = newestPerApp(netflix + stremio + nuvio, 3) { it.substringBefore('/') }
        assertEquals(
            listOf("netflix/1", "netflix/2", "netflix/3", "stremio/1", "stremio/2", "stremio/3", "nuvio/1", "nuvio/2",
                "netflix/4", "netflix/5", "stremio/4") + (6..10).map { "netflix/$it" },
            shown,
        )
    }

    @Test fun oneAppIsNotCapped() {
        val stremio = (1..10).map { 100L - it to "stremio/$it" }
        assertEquals(stremio.map { it.second }, newestPerApp(stremio, 3) { it.substringBefore('/') })
    }

    @Test fun busyAppCantPushOthersBack() {
        val items = (1..4).map { 100L - it to "stremio/$it" } + (90L to "netflix/1")
        assertEquals(listOf("stremio/1", "stremio/2", "stremio/3", "netflix/1", "stremio/4"), newestPerApp(items, 3) { it.substringBefore('/') })
    }

    @Test fun whatYouStartedComesBeforeSuggestionsWhateverTheUnit() {
        val rows = listOf(
            watchSortKey(1, 1_792_576_332_167) to "wholphin/suggestion-a",
            watchSortKey(1, 1_792_576_332_167) to "wholphin/suggestion-b",
            watchSortKey(0, 1_792_581_271) to "wholphin/ncis",
            watchSortKey(0, 1_791_758_381) to "wholphin/descendants",
            watchSortKey(0, 1_791_242_669) to "wholphin/coquin",
            watchSortKey(0, 1_791_237_906_401) to "moonfin/clochette", 
        )
        val shown = newestPerApp(rows, 3) { it.substringBefore('/') }
        assertEquals(listOf("wholphin/ncis", "wholphin/descendants", "wholphin/coquin", "moonfin/clochette",
            "wholphin/suggestion-a", "wholphin/suggestion-b"), shown)
    }

    @Test fun suggestionsStillShowWhenAnAppHasNothingStarted() {
        val rows = listOf(watchSortKey(1, 1_792_576_332_167) to "netflix/next", watchSortKey(0, 1_792_581_271) to "wholphin/ncis")
        assertEquals(listOf("wholphin/ncis", "netflix/next"), newestPerApp(rows, 3) { it.substringBefore('/') })
    }

    @Test fun channelCardsNewestFirstNotInsertOrder() {
        val nuvio = byWeight(listOf(
            Triple(Int.MAX_VALUE - 2L, channelSortKey(1_000, 0), "nuvio/A"),
            Triple(Int.MAX_VALUE - 1L, channelSortKey(2_000, 0), "nuvio/B"),
            Triple(Int.MAX_VALUE - 0L, channelSortKey(3_000, 0), "nuvio/C"),
        ))
        val shown = newestPerApp(nuvio + (watchSortKey(0, 2_500) to "netflix/1"), 3) { it.substringBefore('/') }
        assertEquals(listOf("nuvio/C", "netflix/1", "nuvio/B", "nuvio/A"), shown)
    }

    @Test fun channelCardsWithNoTimeFollowWeightThenAppOrder() {
        val weighted = byWeight(listOf(Triple(1L, channelSortKey(0, 500), "a"), Triple(3L, channelSortKey(0, 500), "c"),
            Triple(2L, channelSortKey(0, 500), "b")))
        assertEquals(listOf("c", "b", "a"), newestPerApp(weighted, 3) { "app" })
        val plain = byWeight(listOf(Triple(0L, channelSortKey(0, 500), "x"), Triple(0L, channelSortKey(0, 500), "y")))
        assertEquals(listOf("x", "y"), newestPerApp(plain, 3) { "app" })
    }

    @Test fun onlyReadableSeasonAndEpisodeNumbersShow() {
        assertEquals("5", displayNumber("5"))
        assertEquals("12", displayNumber(" 12 "))
        assertEquals("1A", displayNumber("1A"))
        assertEquals(null, displayNumber("e"))
        assertEquals(null, displayNumber("eyJzIjoidHYiLCJrIjoidHUgaGkgcmUgZGlsIG1laW4iLCJ0IjoidHUgaGkgcmUgZGlsIG1laW4ifQ"))
        assertEquals(null, displayNumber(""))
        assertEquals(null, displayNumber(null))
    }

    @Test fun continueWatchingChannelsStartOnOthersOff() {
        assertEquals(true, channelOn("Continue Watching", "stremio/Continue Watching", emptySet()))
        assertEquals(false, channelOn("On Deck", "plex/On Deck", emptySet()))
        assertEquals(true, channelOn("On Deck", "plex/On Deck", setOf("plex/On Deck")))
        assertEquals(false, channelOn("Continue watching", "stremio/Continue watching", setOf("stremio/Continue watching")))
    }
}
