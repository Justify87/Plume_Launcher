package dev.plume

/** Stable sidebar geometry and render fingerprint; navigation state stays in VDesignHomeView. */
internal object VDesignSidebarLayout {
    data class Slot(val key: String, val y: Int, val fullColorIcon: Boolean)

    fun slots(items: List<Pair<String, Boolean>>): List<Slot> {
        var shortcuts = 0
        return items.map { (key, fullColorIcon) ->
            val y = when (key) {
                VDesignHomeView.OVERVIEW -> 88
                VDesignHomeView.NUVIO, VDesignHomeView.SMARTTUBE, VDesignHomeView.SPOTIFY ->
                    170 + 81 * shortcuts++
                VDesignHomeView.ALL_APPS -> 170 + 81 * shortcuts
                VDesignHomeView.VPN -> 674
                VDesignHomeView.SETTINGS -> 761
                else -> 170
            }
            Slot(key, y, fullColorIcon)
        }
    }
}
