package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaFeedRepositoryTest {
    private fun item(
        id: String, pkg: String? = "app.a", title: String = id,
        category: MediaCategory = MediaCategory.CONTINUE, source: String = "tv",
        intent: String? = null, contentId: String? = id, time: Long? = null,
        order: Int = 0,
    ) = MediaItem(
        stableId = MediaFeedRules.stableId(source, pkg, contentId, intent, order),
        sourceId = source, sourcePackage = pkg, title = title, category = category,
        contentId = contentId, intentUri = intent, engagedAtMillis = time, sourceOrder = order,
    )

    @Test fun classificationDoesNotInventContinueOrNew() {
        assertEquals(MediaCategory.CONTINUE, MediaFeedRules.classify(0))
        assertEquals(MediaCategory.NEW, MediaFeedRules.classify(2))
        assertEquals(MediaCategory.RECOMMENDATION, MediaFeedRules.classify(1))
        assertEquals(MediaCategory.RECOMMENDATION, MediaFeedRules.classify(3))
        assertEquals(MediaCategory.UNKNOWN, MediaFeedRules.classify(null))
        assertEquals(MediaCategory.UNKNOWN, MediaFeedRules.classify(null, 999))
        assertEquals(MediaCategory.CONTINUE, MediaFeedRules.classify(null, 1000))
        assertEquals(MediaCategory.CONTINUE, MediaFeedRules.classify(null, continueChannel = true))
        assertEquals(MediaCategory.UNKNOWN, MediaFeedRules.classify(99))
    }

    @Test fun absentValuesStayAbsentAndProgressIsValidated() {
        val bare = MediaItem("id", "tv", null, "Untitled source item")
        assertEquals(null, bare.subtitle)
        assertEquals(null, bare.progressPercent)
        assertEquals(null, bare.durationMillis)
        assertEquals(null, bare.engagedAtMillis)
        assertEquals(null, bare.intentUri)
        assertEquals(MediaCategory.UNKNOWN, bare.category)
        try {
            bare.copy(progressPercent = 101)
            throw AssertionError("Out-of-range progress was accepted")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun repeatedSourcesAndIdenticalIntentsDedupeWithinAnApp() {
        val repo = MediaFeedRepository()
        val one = item("1", intent = "intent:#Intent;end")
        val repeat = item("2", intent = "intent:#Intent;end")
        repo.replaceSource("tv", listOf(one, repeat))
        assertEquals(1, repo.snapshot().size)
        repo.replaceSource("tv", listOf(one))
        assertEquals(listOf(one), repo.snapshot())
    }

    @Test fun identicalTitlesFromDifferentAppsOrSeriesStaySeparate() {
        val repo = MediaFeedRepository()
        repo.replaceSource("tv", listOf(
            item("season-1", title = "Pilot"),
            item("season-2", title = "Pilot"),
            item("season-1", pkg = "app.b", title = "Pilot"),
        ))
        assertEquals(3, repo.snapshot().size)
    }

    @Test fun missingDeepLinksKeepIndependentRowsAndStableFallbacks() {
        val a = MediaFeedRules.stableId("tv:channel/1", "app.a", null, null, 0)
        val b = MediaFeedRules.stableId("tv:channel/1", "app.a", null, null, 1)
        assertFalse(a == b)
        assertEquals(a, MediaFeedRules.stableId("tv:channel/1", "app.a", null, null, 0))
        val repo = MediaFeedRepository()
        repo.replaceSource("tv", listOf(
            item("1", title = "Episode", contentId = null, order = 0),
            item("2", title = "Episode", contentId = null, order = 1),
        ))
        assertEquals(2, repo.snapshot().size)
    }

    @Test fun appAndCategoryFiltersAreIndependentAndExact() {
        val repo = MediaFeedRepository()
        repo.replaceSource("tv", listOf(item("continue"), item("new", category = MediaCategory.NEW)))
        repo.replaceSource("demo", listOf(item("demo", pkg = "app.b", source = "demo")))
        assertEquals(2, repo.snapshot(sourceId = "tv").size)
        assertEquals(1, repo.snapshot(sourceId = "tv", category = MediaCategory.NEW).size)
        assertEquals(1, repo.snapshot(packageName = "app.b").size)
        assertTrue(repo.snapshot(packageName = "app").isEmpty())
    }

    @Test fun aBusyAppCannotPushAQuietAppBelowItsFirstThree() {
        val repo = MediaFeedRepository()
        repo.replaceSource("tv",
            (1..8).map { item("a/$it", time = 1000L - it, order = it) } +
                item("b/1", pkg = "app.b", time = 900L))
        val ids = repo.snapshot().map { it.contentId }
        assertEquals(listOf("a/1", "a/2", "a/3", "b/1"), ids.take(4))
        assertEquals(9, ids.size)
    }

    @Test fun sortingIsDeterministicAndKeepsCategoriesSeparate() {
        val items = listOf(
            item("new", category = MediaCategory.NEW, time = 9999),
            item("continued", time = 10),
            item("untimed", category = MediaCategory.UNKNOWN),
        )
        val sorted = MediaFeedRules.sort(items)
        assertEquals(listOf("continued", "new", "untimed"), sorted.map { it.contentId })
        assertEquals(sorted, MediaFeedRules.sort(items.reversed()))
    }

    @Test fun emptyOrInactiveSourcesDisappearAndSnapshotsCannotBeMutated() {
        val repo = MediaFeedRepository()
        repo.replaceSource("tv", listOf(item("a")))
        val old = repo.snapshot()
        repo.replaceSource("tv", emptyList())
        assertTrue(repo.snapshot().isEmpty())
        assertEquals(1, old.size)
        try {
            (old as MutableList).clear()
            throw AssertionError("Published snapshot is mutable")
        } catch (_: UnsupportedOperationException) { }
    }

    @Test fun sourcePackageAliasesFilterWithoutDuplicatingOverview() {
        val repo = MediaFeedRepository()
        val media = item("plex", pkg = "com.plex.tv").copy(packageAliases = setOf("com.plex.phone", "com.plex.tv"))
        repo.replaceSource("tv", listOf(media))
        assertEquals(1, repo.snapshot().size)
        assertEquals(1, repo.snapshot(packageName = "com.plex.phone").size)
    }
}
