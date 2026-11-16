package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherSettingsSectionsTest {
    @Test fun standardTvKeepsExistingSectionOrderAndSettingsRows() {
        val layout = LauncherSettingsSections.layout(googleTv = false, fireTv = false)
        assertEquals(listOf(R.string.section_style, R.string.section_applications,
            R.string.section_tv_media, R.string.section_clock_weather,
            R.string.section_screensaver, R.string.section_remote_home), layout.map { it.key })
        assertEquals(listOf(R.string.sort_apps, R.string.icon_style, R.string.sidebar_color_icons),
            layout.first { it.key == R.string.section_applications }.rows)
        assertEquals(listOf(R.string.home_button, R.string.default_launcher, R.string.smooth_volume,
            R.string.section_keys, R.string.setup_home),
            layout.last().rows)
    }

    @Test fun googleTvAndFireTvHideOnlyPreviouslySuppressedSystemActions() {
        val google = LauncherSettingsSections.layout(googleTv = true, fireTv = false).last().rows
        assertFalse(R.string.default_launcher in google)
        assertTrue(R.string.smooth_volume in google)
        val fire = LauncherSettingsSections.layout(googleTv = false, fireTv = true).last().rows
        assertFalse(R.string.default_launcher in fire)
        assertFalse(R.string.smooth_volume in fire)
        assertEquals(listOf(R.string.home_button, R.string.section_keys, R.string.setup_home), fire)
    }
}
