package dev.plume

/** Preserves the launcher's manual, HDMI and usage-based app ordering. */
internal object LauncherAppOrder {
    fun <T> sort(
        apps: List<T>,
        mode: String?,
        manualOrder: List<String>,
        defaultOrder: List<String>,
        hdmiPrefix: String,
        packageName: (T) -> String,
        label: (T) -> String,
        usageCount: (T) -> Int,
    ): List<T> {
        val byDefault = compareBy<T> {
            defaultOrder.indexOf(packageName(it)).let { index ->
                if (index < 0) Int.MAX_VALUE else index
            }
        }.thenBy { label(it).lowercase() }
        if (mode == "usage") {
            return apps.sortedWith(compareByDescending<T> { usageCount(it) }.then(byDefault))
        }
        return apps.sortedWith(compareBy<T> {
            manualOrder.indexOf(packageName(it)).let { index ->
                if (index >= 0) index
                else if (packageName(it).startsWith(hdmiPrefix)) -1
                else Int.MAX_VALUE
            }
        }.then(byDefault))
    }
}
