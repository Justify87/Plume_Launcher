package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherAppCatalogTest {
    @Test fun tvVariantWinsAndDuplicatePackagesAreDropped() {
        val tv = listOf("tv.a" to "a", "tv.b" to "b")
        val regular = listOf("phone.a" to "a", "phone.c" to "c")
        val result = LauncherAppCatalog.select(tv, regular, "self", "settings") { it.second }
        assertEquals(listOf("tv.a", "tv.b", "phone.c"), result.map { it.first })
    }

    @Test fun excludesOnlyLauncherAndSettingsPackages() {
        val apps = listOf("self", "settings", "third.party")
        assertEquals(listOf("third.party"), LauncherAppCatalog.select(apps, emptyList(), "self", "settings") { it })
    }
}
