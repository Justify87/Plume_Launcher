package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LauncherWorkspaceNavigationTest {
    @Test fun sidebarKeysRoundTripThroughSingleNavigationDestinationPolicy() {
        val nav = LauncherWorkspaceNavigation
        val routes = listOf(
            VDesignHomeView.OVERVIEW to nav.Destination.HOME,
            VDesignHomeView.NUVIO to nav.Destination.NUVIO,
            VDesignHomeView.SMARTTUBE to nav.Destination.SMARTTUBE,
            VDesignHomeView.SPOTIFY to nav.Destination.SPOTIFY,
            VDesignHomeView.ALL_APPS to nav.Destination.ALL_APPS,
            VDesignHomeView.SETTINGS to nav.Destination.SETTINGS,
            VDesignHomeView.VPN to nav.Destination.VPN,
        )
        for ((key, destination) in routes) {
            assertEquals(destination, nav.destinationForKey(key))
            assertEquals(key, nav.keyForDestination(destination))
        }
        assertEquals(nav.Destination.HOME, nav.destinationForKey("unknown"))
    }

    @Test fun railFocusImmediatelySelectsContentWithoutOk() {
        val before = LauncherWorkspaceNavigation.State()
        val selected = LauncherWorkspaceNavigation.focusRail(before,
            LauncherWorkspaceNavigation.Destination.ALL_APPS)
        assertEquals(LauncherWorkspaceNavigation.Destination.ALL_APPS, selected.destination)
        assertEquals(LauncherWorkspaceNavigation.Action.SHOW_CONTENT,
            LauncherWorkspaceNavigation.focusAction(before,
                LauncherWorkspaceNavigation.Destination.ALL_APPS))
        assertEquals(LauncherWorkspaceNavigation.Action.NO_OP,
            LauncherWorkspaceNavigation.focusAction(selected,
                LauncherWorkspaceNavigation.Destination.ALL_APPS))
    }

    @Test fun okLaunchesAppsButEntersSettingsAndVpnContent() {
        val actions = LauncherWorkspaceNavigation
        assertEquals(LauncherWorkspaceNavigation.Action.LAUNCH_APP,
            actions.actionOnOk(LauncherWorkspaceNavigation.Destination.NUVIO))
        assertEquals(LauncherWorkspaceNavigation.Action.LAUNCH_APP,
            actions.actionOnOk(LauncherWorkspaceNavigation.Destination.SMARTTUBE))
        assertEquals(LauncherWorkspaceNavigation.Action.FOCUS_CONTENT,
            actions.actionOnOk(LauncherWorkspaceNavigation.Destination.SETTINGS))
        assertEquals(LauncherWorkspaceNavigation.Action.FOCUS_CONTENT,
            actions.actionOnOk(LauncherWorkspaceNavigation.Destination.VPN))
        assertEquals(LauncherWorkspaceNavigation.Action.FOCUS_CONTENT,
            actions.actionOnOk(LauncherWorkspaceNavigation.Destination.ALL_APPS))
    }

    @Test fun backFirstReturnsToRailThenHome() {
        val selected = LauncherWorkspaceNavigation.focusRail(
            LauncherWorkspaceNavigation.State(), LauncherWorkspaceNavigation.Destination.VPN)
        val entered = LauncherWorkspaceNavigation.right(selected, true)
        assertEquals(false, entered.railFocused)
        assertEquals(selected, LauncherWorkspaceNavigation.back(entered))
        assertEquals(LauncherWorkspaceNavigation.State(),
            LauncherWorkspaceNavigation.back(selected))
    }

    @Test fun emptyContentKeepsRailFocused() {
        val selected = LauncherWorkspaceNavigation.focusRail(
            LauncherWorkspaceNavigation.State(), LauncherWorkspaceNavigation.Destination.ALL_APPS)
        assertEquals(selected, LauncherWorkspaceNavigation.right(selected, false))
    }

    @Test fun realNuvioChannelOwnerWinsOverDifferentInstalledVersion() {
        val installed = listOf("com.nuvio.tv", "com.justify87.nuviotv.wireguard")
        assertEquals("com.justify87.nuviotv.wireguard",
            NuvioInstallationPolicy.preferredPackage(installed, "com.justify87.nuviotv.wireguard"))
        assertNull(NuvioInstallationPolicy.preferredPackage(installed, null))
        assertEquals("com.nuvio.tv",
            NuvioInstallationPolicy.preferredPackage(listOf("com.nuvio.tv"), null))
    }
    @Test fun appPreviewNeverTargetsASecondPackageOrUnsafeData() {
        val policy = WorkspaceMediaLaunchPolicy
        assertEquals(true, policy.valid("com.spotify.tv.android",
            "com.spotify.tv.android", null, false, "https"))
        assertEquals(true, policy.valid("com.spotify.tv.android",
            null, "com.spotify.tv.android", false, null))
        assertEquals(false, policy.valid("com.spotify.tv.android",
            "com.nuvio.tv", null, false, null))
        assertEquals(false, policy.valid("com.spotify.tv.android",
            null, null, false, "https"))
        assertEquals(false, policy.valid("com.spotify.tv.android",
            "com.spotify.tv.android", null, true, null))
        assertEquals(false, policy.valid("com.spotify.tv.android",
            "com.spotify.tv.android", null, false, "file"))
        assertEquals(false, policy.valid("com.spotify.tv.android",
            "com.spotify.tv.android", null, false, "content"))
    }


    @Test fun backFromContentRestoresRailBeforeResettingDestination() {
        val focused = LauncherWorkspaceNavigation.State(
            destination = LauncherWorkspaceNavigation.Destination.SETTINGS,
            railFocused = false,
            contentFocusKey = "settings/theme",
        )
        val onRail = LauncherWorkspaceNavigation.back(focused)
        assertEquals(true, onRail.railFocused)
        assertEquals(LauncherWorkspaceNavigation.Destination.SETTINGS, onRail.destination)
        assertEquals("settings/theme", onRail.contentFocusKey)
        assertEquals(LauncherWorkspaceNavigation.State(), LauncherWorkspaceNavigation.back(onRail))
    }

    @Test fun railDestinationChangeClearsPreviousContentFocusWithoutEnteringEmptyPane() {
        val previous = LauncherWorkspaceNavigation.State(
            destination = LauncherWorkspaceNavigation.Destination.ALL_APPS,
            railFocused = false,
            contentFocusKey = "app.example",
        )
        val selected = LauncherWorkspaceNavigation.focusRail(
            previous, LauncherWorkspaceNavigation.Destination.VPN)
        assertEquals(true, selected.railFocused)
        assertEquals(LauncherWorkspaceNavigation.Destination.VPN, selected.destination)
        assertNull(selected.contentFocusKey)
        assertEquals(LauncherWorkspaceNavigation.Action.SHOW_CONTENT,
            LauncherWorkspaceNavigation.focusAction(previous,
                LauncherWorkspaceNavigation.Destination.VPN))
        assertEquals(selected, LauncherWorkspaceNavigation.right(selected, false))
    }
}
