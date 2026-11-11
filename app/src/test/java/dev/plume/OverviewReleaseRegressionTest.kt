package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** M11: focus stability and honest zero-content messages without an emulator. */
class OverviewReleaseRegressionTest {
    private fun item(id: String) = MediaItem(
        stableId = id, sourceId = "tv", sourcePackage = "app.test",
        title = "A very long episode title for a TV card — $id",
        category = MediaCategory.CONTINUE,
    )

    @Test fun identicalFeedAndPageDoNotNeedAnotherRowRebuild() {
        val rows = listOf(HomeMediaSection(MediaCategory.CONTINUE, listOf(item("a"), item("b"))))
        val signature = OverviewRenderSignature("sidebar:overview", rows, listOf("Continue"), "")
        assertEquals(signature, OverviewRenderSignature("sidebar:overview",
            rows.map { it.copy(items = it.items.toList()) }, listOf("Continue"), ""))
        assertNotEquals(signature, signature.copy(pageKey = "app.test"))
        assertNotEquals(signature, signature.copy(labels = listOf("Weitermachen")))
        assertNotEquals(signature, signature.copy(sections = listOf(
            HomeMediaSection(MediaCategory.CONTINUE, listOf(item("a"))))))
    }

    @Test fun missingAndOfflineSourcesHaveDistinctEmptyStates() {
        assertEquals(HomeFeedEmptyState.PERMISSION, HomeFeedStatus.emptyState(
            false, false, false, false, true))
        assertEquals(HomeFeedEmptyState.PROVIDER_ERROR, HomeFeedStatus.emptyState(
            false, true, true, true, false))
        assertEquals(HomeFeedEmptyState.OFFLINE, HomeFeedStatus.emptyState(
            false, true, false, true, false))
        assertEquals(HomeFeedEmptyState.EMPTY, HomeFeedStatus.emptyState(
            false, true, false, false, false))
        assertEquals(HomeFeedEmptyState.NONE, HomeFeedStatus.emptyState(
            true, true, true, true, false))
    }

    @Test fun emptyAndBusyPagesNeverCreateUnfocusableMediaTargets() {
        val empty = HomePageContent.sections(MediaFeedRepository(), HomePageState.Overview)
        assertEquals(null, HomePageContent.restoreFocus(empty, null, null, 0))
        val feed = MediaFeedRepository()
        feed.replaceSource("tv", (0 until 80).map { item("episode-$it") })
        val sections = HomePageContent.sections(feed, HomePageState.Overview)
        val first = HomePageContent.restoreFocus(sections, null, null, 0)
        assertEquals(HomePageContent.FocusTarget(0, 0), first)
        val last = HomePageContent.restoreFocus(sections, MediaCategory.CONTINUE,
            "missing", 500)
        assertEquals(HomePageContent.FocusTarget(0, 79), last)
    }
}
