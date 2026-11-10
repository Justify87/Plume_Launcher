package dev.plume

/** Persisted separately from the existing classic row preferences. */
internal enum class HomeLayoutMode(val key: String) {
    CLASSIC("classic"),
    OVERVIEW("overview");

    companion object {
        const val PREF_KEY = "home_layout_mode"

        fun fromPreference(stored: String?): HomeLayoutMode =
            values().firstOrNull { it.key == stored } ?: CLASSIC
    }
}
