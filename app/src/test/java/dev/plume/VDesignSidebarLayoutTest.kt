package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class VDesignSidebarLayoutTest {
    private val layout = VDesignSidebarLayout

    @Test fun completeRailPreservesExistingCoordinates() {
        val keys = listOf(
            VDesignHomeView.OVERVIEW,
            VDesignHomeView.NUVIO,
            VDesignHomeView.SMARTTUBE,
            VDesignHomeView.SPOTIFY,
            VDesignHomeView.ALL_APPS,
            VDesignHomeView.VPN,
            VDesignHomeView.SETTINGS,
        )
        assertEquals(listOf(88, 170, 251, 332, 413, 674, 761),
            layout.slots(keys.map { it to false }).map { it.y })
    }

    @Test fun missingAppsDoNotLeaveEmptyShortcutPositions() {
        val keys = listOf(
            VDesignHomeView.OVERVIEW,
            VDesignHomeView.SPOTIFY,
            VDesignHomeView.ALL_APPS,
            VDesignHomeView.SETTINGS,
        )
        assertEquals(listOf(88, 170, 251, 761),
            layout.slots(keys.map { it to false }).map { it.y })
    }

    @Test fun sameStructureAllowsReuseButChangesRequireRebuild() {
        val keys = listOf(VDesignHomeView.OVERVIEW, VDesignHomeView.NUVIO,
            VDesignHomeView.ALL_APPS)
        val before = layout.slots(keys.map { it to false })
        assertEquals(before, layout.slots(keys.map { it to false }))
        assertNotEquals(before, layout.slots(listOf(
            VDesignHomeView.OVERVIEW to false,
            VDesignHomeView.NUVIO to true,
            VDesignHomeView.ALL_APPS to false,
        )))
        assertNotEquals(before, layout.slots(listOf(
            VDesignHomeView.OVERVIEW to false,
            VDesignHomeView.ALL_APPS to false,
        )))
    }
}
