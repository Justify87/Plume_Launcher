package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class VDesignFocusNavigatorTest {
    private val n = VDesignFocusNavigator
    private fun cell(section: NuvioSection, slot: Int, x: Int, y: Int, w: Int, h: Int) =
        VDesignFocusNavigator.Cell(section, slot, VDesignCanvas.Box(x, y, w, h))
    private val started = listOf(
        cell(NuvioSection.IN_PROGRESS, 0, 108, 145, 655, 365),
        cell(NuvioSection.IN_PROGRESS, 1, 781, 142, 303, 170),
        cell(NuvioSection.IN_PROGRESS, 2, 781, 330, 303, 177),
    )
    private val available = listOf(147, 266, 386).mapIndexed { i, y ->
        cell(NuvioSection.AVAILABLE, i, 1132, y, 380, 103)
    }
    private val upcoming = listOf(133, 379, 611, 842, 1074, 1306)
        .mapIndexed { i, x -> cell(NuvioSection.UPCOMING, i, x, 605, 205, 218) }
    private fun at(section: NuvioSection, slot: Int) = VDesignFocusNavigator.Target.Card(section, slot)
    private fun move(from: VDesignFocusNavigator.Cell, live: List<VDesignFocusNavigator.Cell>,
        d: VDesignFocusNavigator.Direction) = n.next(from, live, d, "vdesign:overview")

    @Test fun progressPairAndEpisodeColumnFollowVisualCoordinates() {
        val all = started + available + upcoming
        assertEquals(at(NuvioSection.IN_PROGRESS, 2),
            move(started[0], all, VDesignFocusNavigator.Direction.RIGHT))
        assertEquals(at(NuvioSection.IN_PROGRESS, 0),
            move(started[1], all, VDesignFocusNavigator.Direction.LEFT))
        assertEquals(at(NuvioSection.IN_PROGRESS, 2),
            move(started[1], all, VDesignFocusNavigator.Direction.DOWN))
        assertEquals(at(NuvioSection.AVAILABLE, 2),
            move(started[2], all, VDesignFocusNavigator.Direction.RIGHT))
        assertEquals(at(NuvioSection.AVAILABLE, 1),
            move(available[2], all, VDesignFocusNavigator.Direction.UP))
    }

    @Test fun bottomStripIsOneRowAndStopsAtEdges() {
        val all = started + available + upcoming
        assertEquals(at(NuvioSection.UPCOMING, 1),
            move(upcoming[0], all, VDesignFocusNavigator.Direction.RIGHT))
        assertEquals(at(NuvioSection.UPCOMING, 4),
            move(upcoming[5], all, VDesignFocusNavigator.Direction.LEFT))
        assertEquals(VDesignFocusNavigator.Target.Stay,
            move(upcoming[5], all, VDesignFocusNavigator.Direction.DOWN))
        assertEquals(VDesignFocusNavigator.Target.Stay,
            move(upcoming[5], all, VDesignFocusNavigator.Direction.RIGHT))
        assertEquals(VDesignFocusNavigator.Target.Sidebar("vdesign:overview"),
            move(upcoming[0], all, VDesignFocusNavigator.Direction.LEFT))
    }

    @Test fun sparseContentStillUsesRealCandidates() {
        val all = listOf(started[0], upcoming[1])
        assertEquals(at(NuvioSection.UPCOMING, 1),
            move(started[0], all, VDesignFocusNavigator.Direction.DOWN))
        assertEquals(at(NuvioSection.IN_PROGRESS, 0),
            move(upcoming[1], all, VDesignFocusNavigator.Direction.UP))
        assertEquals(VDesignFocusNavigator.Target.Stay,
            move(started[0], listOf(started[0]), VDesignFocusNavigator.Direction.RIGHT))
    }

    @Test fun sidebarOnlyTargetsVisibleActions() {
        val keys = listOf("vdesign:overview", "vdesign:spotify", "vdesign:allapps")
        assertEquals(VDesignFocusNavigator.Target.Sidebar("vdesign:spotify"),
            n.fromSidebar(keys[0], keys, VDesignFocusNavigator.Direction.DOWN, started[0]))
        assertEquals(VDesignFocusNavigator.Target.Stay,
            n.fromSidebar(keys.last(), keys, VDesignFocusNavigator.Direction.DOWN, started[0]))
        assertEquals(at(NuvioSection.IN_PROGRESS, 0),
            n.fromSidebar(keys[0], keys, VDesignFocusNavigator.Direction.RIGHT, started[0]))
    }
}
