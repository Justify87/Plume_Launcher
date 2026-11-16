package dev.plume

internal object LauncherSettingsSections {
    data class Section(val key: Int, val rows: List<Int>)

    fun layout(googleTv: Boolean, fireTv: Boolean): List<Section> = listOf(
        Section(R.string.section_style, listOf(R.string.theme, R.string.glass, R.string.app_animations)),
        Section(R.string.section_applications, listOf(R.string.sort_apps, R.string.icon_style, R.string.sidebar_color_icons)),
        Section(R.string.section_tv_media, listOf(R.string.cw_apps, R.string.cw_channels)),
        Section(R.string.section_clock_weather, listOf(R.string.weather, R.string.weather_units, R.string.weather_location)),
        Section(R.string.section_screensaver, listOf(R.string.screensaver, R.string.test_screensaver, R.string.screensaver_timing)),
        Section(R.string.section_remote_home, listOfNotNull(
            R.string.home_button,
            R.string.default_launcher.takeIf { !googleTv && !fireTv },
            R.string.smooth_volume.takeIf { !fireTv },
            R.string.section_keys,
            R.string.setup_home,
        )),
    )
}
