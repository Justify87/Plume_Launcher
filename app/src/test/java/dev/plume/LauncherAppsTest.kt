package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherAppsTest {
    @Test fun tvEntriesTakePrecedenceAndPhoneOnlyAppsRemain() {
        val tv = listOf("both" to "tv", "tv-only" to "tv")
        val other = listOf("both" to "phone", "phone-only" to "phone")
        assertEquals(tv + ("phone-only" to "phone"), preferTvApps(tv, other) { it.first })
    }

    @Test fun manualOrderPreservesHdmiPriorityAndDefaultTies() {
        val entries = listOf(
            "other.app" to "Other",
            "hdmi:port1" to "HDMI",
            "com.netflix.ninja" to "Netflix",
            "custom.app" to "Custom",
        )
        val sorted = LauncherAppCatalog.sorted(
            entries, { it.first }, { it.second }, "manual",
            { listOf("custom.app") }, listOf("com.netflix.ninja"), "hdmi:", { 0 },
        )
        assertEquals(listOf("hdmi:port1", "custom.app", "com.netflix.ninja", "other.app"),
            sorted.map { it.first })
    }

    @Test fun usageSortPrioritizesLaunchCountsWithStableDefaultTies() {
        val entries = listOf(
            "z.app" to "Zulu",
            "com.netflix.ninja" to "Netflix",
            "a.app" to "Alpha",
        )
        val sorted = LauncherAppCatalog.sorted(
            entries, { it.first }, { it.second }, "usage", { error("manual order must not be read in usage mode") },
            listOf("com.netflix.ninja"), "hdmi:",
            { if (it == "z.app") 5 else 2 },
        )
        assertEquals(listOf("z.app", "com.netflix.ninja", "a.app"), sorted.map { it.first })
    }

    @Test fun manualOrderingLeavesAlreadySpecifiedPackageIdentityUntouched() {
        val entries = listOf("com.nuvio.release" to "Nuvio", "com.nuvio.debug" to "Nuvio")
        val sorted = LauncherAppCatalog.sorted(entries, { it.first }, { it.second }, "manual",
            { listOf("com.nuvio.debug") }, emptyList(), "hdmi:", { 0 })
        assertEquals(listOf("com.nuvio.debug", "com.nuvio.release"), sorted.map { it.first })
    }
}
