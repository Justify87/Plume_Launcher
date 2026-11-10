package dev.plume

import dev.plume.HomeFocusNavigator.Direction
import dev.plume.HomeFocusNavigator.Target
import dev.plume.HomeFocusNavigator.Zone
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFocusNavigatorTest {
    private fun move(zone: Zone, direction: Direction, index: Int = 0, count: Int = 4, content: Boolean = false) =
        HomeFocusNavigator.next(zone, direction, index, count, content)

    @Test fun topDownReturnsSelectedSidebarEntry() {
        assertEquals(Target.Sidebar(2), move(Zone.TOP, Direction.DOWN, index = 2))
        assertEquals(Target.Stay, move(Zone.TOP, Direction.UP))
        assertEquals(Target.Framework, move(Zone.TOP, Direction.LEFT))
    }

    @Test fun sidebarHasStableVerticalEdges() {
        assertEquals(Target.Top, move(Zone.SIDEBAR, Direction.UP))
        assertEquals(Target.Sidebar(0), move(Zone.SIDEBAR, Direction.UP, index = 1))
        assertEquals(Target.Sidebar(1), move(Zone.SIDEBAR, Direction.DOWN))
        assertEquals(Target.Stay, move(Zone.SIDEBAR, Direction.DOWN, index = 3))
        assertEquals(Target.Stay, move(Zone.SIDEBAR, Direction.LEFT))
    }

    @Test fun rightRequiresAnActualFocusableContentAction() {
        assertEquals(Target.Stay, move(Zone.SIDEBAR, Direction.RIGHT))
        assertEquals(Target.Content, move(Zone.SIDEBAR, Direction.RIGHT, content = true))
        assertEquals(Target.Sidebar(2), move(Zone.CONTENT, Direction.LEFT, index = 2))
        assertEquals(Target.Top, move(Zone.CONTENT, Direction.UP))
        assertEquals(Target.Stay, move(Zone.CONTENT, Direction.RIGHT))
    }

    @Test fun noAppsAndOutOfRangeSelectionCannotTrapFocus() {
        assertEquals(Target.Stay, move(Zone.TOP, Direction.DOWN, count = 0))
        assertEquals(Target.Stay, move(Zone.SIDEBAR, Direction.RIGHT, count = 0, content = true))
        assertEquals(Target.Stay, move(Zone.CONTENT, Direction.LEFT, count = 0))
        assertEquals(Target.Sidebar(0), move(Zone.TOP, Direction.DOWN, index = -10, count = 1))
        assertEquals(Target.Sidebar(2), move(Zone.TOP, Direction.DOWN, index = 100, count = 3))
    }
}
