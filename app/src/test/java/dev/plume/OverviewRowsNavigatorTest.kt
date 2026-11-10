package dev.plume

import dev.plume.OverviewRowsNavigator.Direction
import dev.plume.OverviewRowsNavigator.Target
import org.junit.Assert.assertEquals
import org.junit.Test

class OverviewRowsNavigatorTest {
    @Test fun leftAndRightStayInRowAndLeftEdgeReturnsToRail() {
        assertEquals(Target.Sidebar, OverviewRowsNavigator.next(0, 0, listOf(3, 1), Direction.LEFT))
        assertEquals(Target.Card(0, 1), OverviewRowsNavigator.next(0, 0, listOf(3, 1), Direction.RIGHT))
        assertEquals(Target.Card(0, 1), OverviewRowsNavigator.next(0, 2, listOf(3, 1), Direction.LEFT))
        assertEquals(Target.Stay, OverviewRowsNavigator.next(0, 2, listOf(3, 1), Direction.RIGHT))
    }

    @Test fun upAndDownCrossOnlyAvailableRowsAndRestoreValidIndices() {
        assertEquals(Target.Card(1, 0), OverviewRowsNavigator.next(0, 2, listOf(3, 1), Direction.DOWN))
        assertEquals(Target.Card(0, 2), OverviewRowsNavigator.next(1, 0, listOf(3, 1), Direction.UP, 2))
        assertEquals(Target.Top, OverviewRowsNavigator.next(0, 0, listOf(3, 1), Direction.UP))
        assertEquals(Target.Stay, OverviewRowsNavigator.next(1, 0, listOf(3, 1), Direction.DOWN))
        assertEquals(Target.Card(2, 0), OverviewRowsNavigator.next(0, 0, listOf(1, 0, 3), Direction.DOWN))
    }

    @Test fun emptyMissingAndManyItemsNeverYieldInvalidPositions() {
        assertEquals(Target.Sidebar, OverviewRowsNavigator.next(0, 0, emptyList(), Direction.RIGHT))
        assertEquals(Target.Sidebar, OverviewRowsNavigator.next(0, 0, listOf(0, 0), Direction.DOWN))
        assertEquals(Target.Sidebar, OverviewRowsNavigator.next(0, 1, listOf(1), Direction.RIGHT))
        assertEquals(Target.Card(0, 19), OverviewRowsNavigator.next(0, 18, listOf(20), Direction.RIGHT))
        assertEquals(Target.Stay, OverviewRowsNavigator.next(0, 19, listOf(20), Direction.RIGHT))
        assertEquals(Target.Card(1, 2), OverviewRowsNavigator.next(0, 0, listOf(1, 3), Direction.DOWN, 100))
    }
}
