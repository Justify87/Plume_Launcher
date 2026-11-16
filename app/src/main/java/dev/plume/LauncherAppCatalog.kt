package dev.plume

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager

/** Resolves installed launcher activities without owning Activity or UI state. */
internal object LauncherAppCatalog {
    fun query(pm: PackageManager, launcherPackage: String, settingsPackage: String): List<Pair<ActivityInfo, String>> {
        fun category(name: String) =
            pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(name), 0)
                .map { it.activityInfo to name }
        return select(
            category(Intent.CATEGORY_LEANBACK_LAUNCHER),
            category(Intent.CATEGORY_LAUNCHER),
            launcherPackage,
            settingsPackage,
        ) { it.first.packageName }
    }

    fun <T> select(
        tv: List<T>,
        other: List<T>,
        launcherPackage: String,
        settingsPackage: String,
        packageName: (T) -> String,
    ): List<T> = preferTvApps(tv, other, packageName)
        .filter { packageName(it) != launcherPackage && packageName(it) != settingsPackage }
}
