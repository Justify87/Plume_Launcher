package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceGridPolicyTest {
    @Test fun wideViewportHasFiveColumnsWithoutOverflow() {
        val grid = WorkspaceGridPolicy.calculate(1100)
        assertEquals(5, grid.columns)
        assertTrue(grid.tileWidth * grid.columns + grid.gap * (grid.columns - 1) <= 1100 - 48)
    }

    @Test fun narrowViewportReducesColumnsRatherThanClippingTiles() {
        val grid = WorkspaceGridPolicy.calculate(580)
        assertEquals(4, grid.columns)
        assertTrue(grid.tileWidth >= 112)
        assertTrue(grid.tileWidth * grid.columns + grid.gap * (grid.columns - 1) <= 580 - 48)
    }

    @Test fun tinyViewportStillProducesFocusableTile() {
        val grid = WorkspaceGridPolicy.calculate(90)
        assertEquals(1, grid.columns)
        assertTrue(grid.tileWidth > 0)
    }

    @Test fun exactBoundaryDoesNotOverflow() {
        val grid = WorkspaceGridPolicy.calculate(48 + 5 * 112 + 4 * 11)
        assertEquals(5, grid.columns)
        assertEquals(112, grid.tileWidth)
    }
}
