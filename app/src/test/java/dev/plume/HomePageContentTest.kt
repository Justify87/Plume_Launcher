package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure M07 checks. Actual D-pad focus and app launches still need Shield testing. */
class HomePageContentTest {
    private fun item(
        id: String,
        pkg: String,
        category: MediaCategory = MediaCategory.CONTINUE,
        order: Int = 0,
    ) = MediaItem(
        stableId = MediaFeedRules.stableId("tv", pkg, id, null, order),
        sourceId = "tv",
        sourcePackage = pkg,
        title = id,
        contentId = id,
        category = category,
        sourceOrder = order,
    )

    @Test fun overviewAndAppPagesProjectTheSameFeedWithoutCrossAppLeakage() {
        val repo = MediaFeedRepository()
        val watchNuvio = item("watch-nuvio", "app.nuvio")
        val watchTube = item("watch-tube", "app.tube")
        val newTube = item("new-tube", "app.tube", MediaCategory.NEW)
        val recommendation = item("suggested", "app.nuvio", MediaCategory.RECOMMENDATION)
        val unknown = item("unknown", "app.tube", MediaCategory.UNKNOWN)
        repo.replaceSource("tv", listOf(watchNuvio, watchTube, newTube, recommendation, unknown))

        val overview = HomePageContent.sections(repo, HomePageState.Overview)
        assertEquals(listOf(MediaCategory.CONTINUE, MediaCategory.NEW,
            MediaCategory.RECOMMENDATION, MediaCategory.UNKNOWN), overview.map { it.category })
        assertEquals(setOf("watch-nuvio", "watch-tube"),
            overview.first().items.map { it.title }.toSet())

        val nuvio = HomePageContent.sections(repo, HomePageState.App("app.nuvio"))
        assertEquals(listOf(MediaCategory.CONTINUE, MediaCategory.RECOMMENDATION),
            nuvio.map { it.category })
        assertEquals(setOf("watch-nuvio", "suggested"), nuvio.flatMap { it.items }.map { it.title }.toSet())
        assertEquals(5, overview.sumOf { it.items.size })
        assertEquals(5, repo.snapshot(sourceId = "tv").size)
    }

    @Test fun emptyAppHasNoFakeRowsAndAllAppsHasNoMedia() {
        val repo = MediaFeedRepository()
        repo.replaceSource("tv", listOf(item("episode", "app.other")))
        assertTrue(HomePageContent.sections(repo, HomePageState.App("app.empty")).isEmpty())
        assertTrue(HomePageContent.sections(repo, HomePageState.AllApps).isEmpty())
        val overview = HomePageContent.sections(MediaFeedRepository(), HomePageState.Overview)
        assertTrue(overview.isEmpty())
    }

    @Test fun unknownContentNeverPretendsToBeNewOrRecommended() {
        val repo = MediaFeedRepository()
        repo.replaceSource("tv", listOf(item("unknown", "app.a", MediaCategory.UNKNOWN)))
        val sections = HomePageContent.sections(repo, HomePageState.App("app.a"))
        assertEquals(listOf(MediaCategory.UNKNOWN), sections.map { it.category })
    }

    @Test fun sidebarPreviewBackAndRemovedAppUseOverviewSafely() {
        val packages = setOf("app.a", "app.b")
        val app = HomePageState.fromSidebar("app.a", packages)
        assertEquals(HomePageState.App("app.a"), app)
        assertEquals(HomePageState.Overview, app.backTarget())
        assertNull(HomePageState.Overview.backTarget())
        assertNull(HomePageState.AllApps.backTarget())
        assertEquals(HomePageState.AllApps,
            HomePageState.fromSidebar(HomeSidebarView.ALL_APPS, packages))
        assertEquals(HomePageState.Overview,
            HomePageState.fromSidebar("app.a", setOf("app.b")))
        assertEquals(HomePageState.Overview,
            HomePageState.fromSidebar("app.unknown", packages))
    }

    @Test fun focusRestoresStableCardAcrossRefreshAndFallsBackWithinCategory() {
        val old = item("old", "app.a", order = 0)
        val retained = item("retained", "app.a", order = 1)
        val new = item("new", "app.a", order = 2)
        val rows = listOf(
            HomeMediaSection(MediaCategory.CONTINUE, listOf(new, retained, old)),
            HomeMediaSection(MediaCategory.NEW, listOf(item("fresh", "app.a", MediaCategory.NEW))),
        )
        assertEquals(HomePageContent.FocusTarget(0, 1),
            HomePageContent.restoreFocus(rows, MediaCategory.CONTINUE, retained.stableId, 0))
        assertEquals(HomePageContent.FocusTarget(0, 2),
            HomePageContent.restoreFocus(rows, MediaCategory.CONTINUE, "deleted", 100))
        assertEquals(HomePageContent.FocusTarget(0, 0),
            HomePageContent.restoreFocus(rows.drop(1), MediaCategory.CONTINUE, "deleted", 2))
    }

    @Test fun removingLastCardLeavesNoInvalidFocusTarget() {
        val empty = listOf(
            HomeMediaSection(MediaCategory.CONTINUE, emptyList()),
            HomeMediaSection(MediaCategory.NEW, emptyList()),
        )
        assertNull(HomePageContent.restoreFocus(empty, MediaCategory.CONTINUE, "missing", 2))
    }
}
