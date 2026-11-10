package dev.plume

import dev.plume.HomeFocusNavigator.Direction as RailDirection
import dev.plume.HomeFocusNavigator.Target as RailTarget
import dev.plume.HomeFocusNavigator.Zone
import dev.plume.OverviewRowsNavigator.Direction as CardDirection
import dev.plume.OverviewRowsNavigator.Target as CardTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * M04: pure focus-contract checks. Real focus, scrolling and remote events still require a TV.
 */
class OverviewUxRegressionTest {
    @Test fun railTraversalNeverProducesMissingEntriesForSmallAndLargeAppLists() {
        // Sidebar also contains Overview and All apps: no apps, one app, many apps.
        for (count in listOf(2, 3, 72)) {
            for (index in -3..(count + 3)) {
                for (direction in RailDirection.values()) {
                    val target = HomeFocusNavigator.next(
                        Zone.SIDEBAR, direction, index, count, contentAvailable = true,
                    )
                    if (target is RailTarget.Sidebar) {
                        assertTrue("Out-of-bounds rail focus: $target", target.index in 0 until count)
                    }
                }
                val fromTop = HomeFocusNavigator.next(
                    Zone.TOP, RailDirection.DOWN, index, count, contentAvailable = false,
                )
                assertTrue(fromTop is RailTarget.Sidebar)
                assertTrue((fromTop as RailTarget.Sidebar).index in 0 until count)
            }
            assertEquals(
                RailTarget.Top,
                HomeFocusNavigator.next(Zone.SIDEBAR, RailDirection.UP, 0, count, true),
            )
            assertEquals(
                RailTarget.Stay,
                HomeFocusNavigator.next(Zone.SIDEBAR, RailDirection.DOWN, count - 1, count, true),
            )
        }
    }

    @Test fun rightNeverEntersNonexistentContentAndLeftEscapesFirstCard() {
        assertEquals(
            RailTarget.Stay,
            HomeFocusNavigator.next(Zone.SIDEBAR, RailDirection.RIGHT, 0, 2, false),
        )
        assertEquals(
            RailTarget.Content,
            HomeFocusNavigator.next(Zone.SIDEBAR, RailDirection.RIGHT, 0, 2, true),
        )
        assertEquals(
            CardTarget.Sidebar,
            OverviewRowsNavigator.next(0, 0, listOf(6, 2), CardDirection.LEFT),
        )
        assertEquals(
            CardTarget.Stay,
            OverviewRowsNavigator.next(1, 1, listOf(6, 2), CardDirection.RIGHT),
        )
    }

    @Test fun verticalCardNavigationSkipsEmptyRowsAndNeverInventsCards() {
        assertEquals(
            CardTarget.Card(2, 0),
            OverviewRowsNavigator.next(0, 0, listOf(1, 0, 1), CardDirection.DOWN, 30),
        )
        assertEquals(
            CardTarget.Top,
            OverviewRowsNavigator.next(0, 0, listOf(1, 0), CardDirection.UP),
        )
        assertEquals(
            CardTarget.Sidebar,
            OverviewRowsNavigator.next(0, 0, listOf(0, 0), CardDirection.RIGHT),
        )
        assertEquals(
            CardTarget.Stay,
            OverviewRowsNavigator.next(0, 0, listOf(1, 0), CardDirection.DOWN),
        )
    }

    @Test fun visibleWindowRemainsBoundedAtViewportEdgesAndMissingData() {
        for (count in listOf(0, 1, 3, 40)) {
            for (offset in listOf(-500, 0, 50, 500, 100000)) {
                val window = DemoMedia.visibleWindow(count, offset, 480, 180)
                if (count == 0) assertTrue(window.isEmpty())
                else {
                    assertTrue(window.first >= 0)
                    assertTrue(window.last < count)
                }
            }
        }
        assertTrue(DemoMedia.rowsFor("invalid.package").flatten().isEmpty())
    }
}
