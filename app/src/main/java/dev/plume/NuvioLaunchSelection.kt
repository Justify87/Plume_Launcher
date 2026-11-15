package dev.plume

/** Android isolates logins per package. Never pick a random debug/Play Store variant. */
internal object NuvioLaunchSelection {
    private val stableOrder = listOf(
        "com.nuvio.tv", "com.nuvio.app",
        "com.justify87.nuviotv.wireguard",
        "com.justify87.nuviotv.wireguard.playstore",
        "com.nuviodebug.com", "com.nuvio.appdebug",
        "com.nuvio.tv.debug", "com.nuvio.app.debug",
        "com.justify87.nuviotv.wireguard.debug",
        "com.justify87.nuviotv.wireguard.playstore.debug",
    )

    fun select(installed: List<String>, userSelected: String?): String? {
        val candidates = installed.filter(AndroidTvMediaSource::isNuvioPackage).toSet()
        if (userSelected in candidates) return userSelected
        return stableOrder.firstOrNull { it in candidates }
            ?: candidates.sorted().firstOrNull()
    }
}
