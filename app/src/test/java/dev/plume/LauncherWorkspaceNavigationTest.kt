package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LauncherWorkspaceNavigationTest {
    private val nav = LauncherWorkspaceNavigation

    @Test fun focusingRailChangesContentWithoutClick() {
        val old = nav.State()
        val selected = nav.focusRail(old, nav.Destination.ALL_APPS)
        assertEquals(nav.Destination.ALL_APPS, selected.destination)
        assertEquals(nav.Action.SHOW_CONTENT,
            nav.focusAction(old, nav.Destination.ALL_APPS))
        assertEquals(nav.Action.NO_OP,
            nav.focusAction(selected, nav.Destination.ALL_APPS))
    }

    @Test fun okOnAppLaunchesButOkOnSettingsFocusesContent() {
        assertEquals(nav.Action.LAUNCH_APP, nav.actionOnOk(nav.Destination.NUVIO))
        assertEquals(nav.Action.LAUNCH_APP, nav.actionOnOk(nav.Destination.SMARTTUBE))
        assertEquals(nav.Action.FOCUS_CONTENT, nav.actionOnOk(nav.Destination.SETTINGS))
        assertEquals(nav.Action.FOCUS_CONTENT, nav.actionOnOk(nav.Destination.VPN))
        assertEquals(nav.Action.FOCUS_CONTENT, nav.actionOnOk(nav.Destination.ALL_APPS))
    }

    @Test fun backFirstReturnsToRailThenHome() {
        val selected = nav.focusRail(nav.State(), nav.Destination.VPN)
        val inContent = nav.right(selected, true)
        assertEquals(false, inContent.railFocused)
        assertEquals(selected, nav.back(inContent))
        assertEquals(nav.State(), nav.back(selected))
    }

    @Test fun noFocusableContentKeepsRailFocus() {
        val selected = nav.focusRail(nav.State(), nav.Destination.ALL_APPS)
        assertEquals(selected, nav.right(selected, false))
    }

    @Test fun nuvioPrefersActualFeedOwnerOverAlphabeticalInstalledApp() {
        val installed = listOf("com.nuvio.tv", "com.justify87.nuviotv.wireguard")
        assertEquals("com.justify87.nuviotv.wireguard",
            NuvioInstallationPolicy.preferredPackage(installed, "com.justify87.nuviotv.wireguard"))
        assertNull(NuvioInstallationPolicy.preferredPackage(installed, null))
        assertEquals("com.nuvio.tv",
            NuvioInstallationPolicy.preferredPackage(listOf("com.nuvio.tv"), null))
    }
}
