package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class VDesignFocusNavigatorTest {
    private val n = VDesignFocusNavigator
    private fun cell(section: NuvioSection, slot: Int, x: Int, y: Int, w: Int, h: Int) =
        VDesignFocusNavigator.Cell(section, slot, VDesignCanvas.Box(x, y, w, h))
    private val started = listOf(
        cell(NuvioSection.IN_PROGRESS, 0, 255, 74, 651, 262),
        cell(NuvioSection.IN_PROGRESS, 1, 919, 83, 287, 245),
        cell(NuvioSection.IN_PROGRESS, 2, 1221, 83, 287, 245)
    )
    private val available = listOf(258, 679, 1102).mapIndexed { i, x ->
        cell(NuvioSection.AVAILABLE, i, x, 403, 402, 140)
    }
    private val mini = listOf(612, 727).flatMapIndexed { row, y ->
        listOf(258, 576, 893, 1210).mapIndexed { col, x ->
            cell(NuvioSection.UPCOMING, row * 4 + col, x, y, 302, 102)
        }
    }
    private fun at(section: NuvioSection, slot: Int) = VDesignFocusNavigator.Target.Card(section, slot)
    private fun move(from: VDesignFocusNavigator.Cell, live: List<VDesignFocusNavigator.Cell>,
        d: VDesignFocusNavigator.Direction) = n.next(from, live, d, "vdesign:overview")

    @Test fun heroAndTwoNeighborsRespectWideGeometry() {
        val all = started + available + mini
        assertEquals(at(NuvioSection.IN_PROGRESS, 1), move(started[0], all, n.Direction.RIGHT))
        assertEquals(at(NuvioSection.IN_PROGRESS, 0), move(started[1], all, n.Direction.LEFT))
        assertEquals(at(NuvioSection.AVAILABLE, 0), move(started[0], all, n.Direction.DOWN))
        assertEquals(at(NuvioSection.AVAILABLE, 2), move(started[2], all, n.Direction.DOWN))
        assertEquals(at(NuvioSection.IN_PROGRESS, 0), move(available[0], all, n.Direction.UP))
        assertEquals(n.Target.Stay, move(started[0], all, n.Direction.UP))
    }

    @Test fun availableThreeAndFourByTwoMinisNeverWrapOrStrand() {
        val all = started + available + mini
        assertEquals(at(NuvioSection.AVAILABLE, 1), move(available[0], all, n.Direction.RIGHT))
        assertEquals(at(NuvioSection.UPCOMING, 0), move(available[0], all, n.Direction.DOWN))
        assertEquals(at(NuvioSection.UPCOMING, 4), move(mini[0], all, n.Direction.DOWN))
        assertEquals(at(NuvioSection.UPCOMING, 3), move(mini[7], all, n.Direction.UP))
        assertEquals(n.Target.Stay, move(mini[7], all, n.Direction.DOWN))
        assertEquals(n.Target.Stay, move(mini[3], all, n.Direction.RIGHT))
        assertEquals(n.Target.Sidebar("vdesign:overview"), move(mini[4], all, n.Direction.LEFT))
    }

    @Test fun emptyBandsSkipToActualLiveCardsAndNoDeadEnd() {
        val all = listOf(started[0], mini[3], mini[4])
        assertEquals(at(NuvioSection.UPCOMING, 3), move(started[0], all, n.Direction.DOWN))
        assertEquals(at(NuvioSection.IN_PROGRESS, 0), move(mini[3], all, n.Direction.UP))
        assertEquals(n.Target.Sidebar("vdesign:overview"), move(started[0], listOf(started[0]), n.Direction.LEFT))
        assertEquals(n.Target.Stay, move(started[0], listOf(started[0]), n.Direction.RIGHT))
    }

    @Test fun sidebarVisibleItemsOnlyAndPreferredTarget() {
        val keys = listOf("vdesign:overview", "vdesign:settings", "vdesign:vpn")
        assertEquals(n.Target.Sidebar("vdesign:settings"), n.fromSidebar(keys[0], keys, n.Direction.DOWN, started[0]))
        assertEquals(n.Target.Stay, n.fromSidebar(keys.last(), keys, n.Direction.DOWN, started[0]))
        assertEquals(n.Target.Stay, n.fromSidebar(keys[0], keys, n.Direction.RIGHT, null))
        assertEquals(at(NuvioSection.IN_PROGRESS, 0), n.fromSidebar(keys[0], keys, n.Direction.RIGHT, started[0]))
        assertEquals(n.Target.Stay, n.fromSidebar(keys[0], keys, n.Direction.LEFT, started[0]))
    }
}
