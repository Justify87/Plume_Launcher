package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoMediaTest {
    @Test fun defaultOverviewHasExactlyTwoOfflineDemoSections() {
        val rows = DemoMedia.rowsFor(null)
        assertEquals(2, rows.size)
        assertTrue(rows[0].isNotEmpty())
        assertTrue(rows[1].isNotEmpty())
        assertEquals(DemoMedia.Section.CONTINUE, rows[0].first().section)
        assertEquals(DemoMedia.Section.NEW, rows[1].first().section)
        assertTrue(rows.flatten().any { it.kind == DemoMedia.Kind.EPISODE && it.progress != null })
        assertTrue(rows.flatten().any { it.kind == DemoMedia.Kind.MOVIE })
        assertTrue(rows.flatten().any { it.kind == DemoMedia.Kind.VIDEO })
        assertTrue(rows.flatten().any { it.kind == DemoMedia.Kind.PODCAST && it.progress != null })
        assertTrue(rows[1].any { it.progress == null })
    }

    @Test fun appPreviewFiltersByExactPackageAndUnknownAppGetsEmptyState() {
        val onlyYoutube = DemoMedia.rowsFor("com.google.android.youtube.tv")
        assertEquals(2, onlyYoutube.size)
        assertTrue(onlyYoutube.flatten().isNotEmpty())
        assertTrue(onlyYoutube.flatten().all { it.source == "YouTube" })
        assertTrue(DemoMedia.rowsFor("com.example.nonexistent").flatten().isEmpty())
        assertTrue(DemoMedia.rowsFor("").flatten().isEmpty())
    }

    @Test fun zeroOneThreeAndManyCardsOnlyMountVisibleAndNextSlots() {
        assertTrue(DemoMedia.visibleWindow(0, 0, 300, 100).isEmpty())
        assertEquals(0..0, DemoMedia.visibleWindow(1, 0, 300, 100))
        assertEquals(0..2, DemoMedia.visibleWindow(3, 0, 100, 100))
        assertEquals(0..3, DemoMedia.visibleWindow(30, 100, 200, 100))
        assertEquals(27..29, DemoMedia.visibleWindow(30, 2800, 200, 100))
        assertEquals(0..1, DemoMedia.visibleWindow(30, 0, 0, 100))
        assertFalse(DemoMedia.visibleWindow(30, 2800, 200, 100).contains(0))
    }

    @Test fun progressIsOptionalAndWithinRange() {
        DemoMedia.Card("ok", DemoMedia.Section.NEW, DemoMedia.Kind.VIDEO, "Title", "", "Demo", emptySet(), 0)
        DemoMedia.Card("ok2", DemoMedia.Section.NEW, DemoMedia.Kind.VIDEO, "Title", "", "Demo", emptySet(), 100)
        try {
            DemoMedia.Card("bad", DemoMedia.Section.NEW, DemoMedia.Kind.VIDEO, "Title", "", "Demo", emptySet(), 101)
            throw AssertionError("Expected invalid progress to be rejected")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }
}
