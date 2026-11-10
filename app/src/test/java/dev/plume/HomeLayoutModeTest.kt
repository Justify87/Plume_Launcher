package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutModeTest {
    @Test fun missingValueKeepsClassicDefault() {
        assertEquals(HomeLayoutMode.CLASSIC, HomeLayoutMode.fromPreference(null))
        assertEquals(HomeLayoutMode.CLASSIC, HomeLayoutMode.fromPreference(""))
    }

    @Test fun storedModesRoundTrip() {
        for (mode in HomeLayoutMode.values()) {
            assertEquals(mode, HomeLayoutMode.fromPreference(mode.key))
        }
    }

    @Test fun unknownValueFallsBackWithoutThrowing() {
        assertEquals(HomeLayoutMode.CLASSIC, HomeLayoutMode.fromPreference("experimental"))
        assertEquals(HomeLayoutMode.CLASSIC, HomeLayoutMode.fromPreference("OVERVIEW"))
    }
}
