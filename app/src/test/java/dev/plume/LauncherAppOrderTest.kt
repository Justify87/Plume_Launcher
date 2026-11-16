package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherAppOrderTest {
    private data class Item(val pkg: String, val label: String, val uses: Int = 0)

    private fun sort(
        items: List<Item>,
        mode: String? = "manual",
        order: List<String> = emptyList(),
        defaults: List<String> = emptyList(),
    ) = LauncherAppOrder.sort(
        items, mode, order, defaults, "hdmi:",
        packageName = { it.pkg }, label = { it.label }, usageCount = { it.uses },
    ).map { it.pkg }

    @Test fun manualOrderKeepsHdmiBeforeUnlistedApps() {
        val items = listOf(Item("z", "Zulu"), Item("hdmi:1", "HDMI"), Item("a", "Alpha"))
        assertEquals(listOf("hdmi:1", "z", "a"), sort(items, order = listOf("z")))
    }

    @Test fun usageModeUsesCountThenDefaultAndLabel() {
        val items = listOf(Item("b", "Beta", 4), Item("a", "Alpha", 4), Item("c", "Charlie", 7))
        assertEquals(listOf("c", "b", "a"), sort(items, mode = "usage", defaults = listOf("b")))
    }

    @Test fun unknownModeFallsBackToManualAndDefaultOrder() {
        val items = listOf(Item("b", "Beta"), Item("a", "Alpha"))
        assertEquals(listOf("a", "b"), sort(items, mode = "unknown", defaults = listOf("a")))
    }

    @Test fun emptyManualOrderUsesAlphabeticalFallback() {
        val items = listOf(Item("z", "zeta"), Item("a", "Alpha"))
        assertEquals(listOf("a", "z"), sort(items))
    }
}
