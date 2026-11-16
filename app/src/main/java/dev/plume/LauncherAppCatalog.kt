package dev.plume

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

internal fun <T> preferTvApps(tv: List<T>, other: List<T>, pkg: (T) -> String) = (tv + other).distinctBy(pkg)

internal class App(val pkg: String, val label: String, val launch: Intent, icon: Drawable, val offline: Boolean = false, private val load: (() -> Drawable)? = null) {
    private val strong = if (load == null) icon else null
    private var weak = java.lang.ref.WeakReference(icon)
    val icon: Drawable get() = strong ?: weak.get() ?: load!!().also { weak = java.lang.ref.WeakReference(it) }
}

internal object LauncherAppCatalog {
    fun installed(pm: PackageManager, ownPackage: String, tvSettingsPackage: String): List<App> {
        fun query(category: String) = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0)
            .map { it.activityInfo to category }
        return preferTvApps(query(Intent.CATEGORY_LEANBACK_LAUNCHER), query(Intent.CATEGORY_LAUNCHER)) { it.first.packageName }
            .filter { (a, _) -> a.packageName != ownPackage && a.packageName != tvSettingsPackage }
            .map { (a, category) ->
                App(
                    a.packageName,
                    a.loadLabel(pm).toString(),
                    Intent(Intent.ACTION_MAIN).addCategory(category)
                        .setComponent(ComponentName(a.packageName, a.name)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    a.loadIcon(pm),
                    load = { a.loadIcon(pm) },
                )
            }
    }

    fun <T> sorted(entries: List<T>, pkg: (T) -> String, label: (T) -> String,
                   sort: String?, order: () -> List<String>, defaultOrder: List<String>,
                   hdmiPrefix: String, usage: (String) -> Int): List<T> {
        val byDefault = compareBy<T> { defaultOrder.indexOf(pkg(it)).let { i -> if (i < 0) Int.MAX_VALUE else i } }
            .thenBy { label(it).lowercase() }
        if (sort == "usage") {
            return entries.sortedWith(compareByDescending<T> { usage(pkg(it)) }.then(byDefault))
        }
        val manualOrder = order()
        return entries.sortedWith(compareBy<T> {
            manualOrder.indexOf(pkg(it)).let { i -> if (i >= 0) i else if (pkg(it).startsWith(hdmiPrefix)) -1 else Int.MAX_VALUE }
        }.then(byDefault))
    }
}
