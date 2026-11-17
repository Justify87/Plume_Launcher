package dev.plume

import android.app.Instrumentation
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

/** Device-side check for retaining rail views while updating icons and callbacks. */
class VDesignNavigationCheck(private val runner: Instrumentation) {
    fun run() = runner.runOnMainSync {
        val home = VDesignHomeView(runner.targetContext)
        var opened: String? = null
        fun items(color: Int) = listOf(
            VDesignHomeView.Navigation(VDesignHomeView.OVERVIEW, "Übersicht", ColorDrawable(Color.WHITE)),
            VDesignHomeView.Navigation(VDesignHomeView.NUVIO, "Nuvio", ColorDrawable(color), true),
            VDesignHomeView.Navigation(VDesignHomeView.ALL_APPS, "Alle Apps", ColorDrawable(Color.WHITE)),
        )
        fun bind(color: Int, callback: (String) -> Unit, fullColor: Boolean = true) {
            home.bindNavigation(items(color), onOpen = callback,
                showFullColorAppIcons = fullColor, onAllApps = {}, onSystemActions = {})
        }
        fun rail(key: String): View = (0 until home.childCount).map { home.getChildAt(it) }
            .first { it.tag == key }

        bind(Color.RED, { opened = "old:" + it })
        val first = rail(VDesignHomeView.NUVIO)
        bind(Color.BLUE, { opened = "new:" + it })
        val updated = rail(VDesignHomeView.NUVIO)
        check(first === updated) { "Unchanged rail was rebuilt" }
        val icon = (updated as FrameLayout).getChildAt(0) as ImageView
        check((icon.drawable as ColorDrawable).color == Color.BLUE) {
            "Reused rail did not receive the updated installed-app icon"
        }
        updated.performClick()
        check(opened == "new:" + VDesignHomeView.NUVIO) {
            "Reused rail retained an obsolete open callback"
        }
        bind(Color.BLUE, { opened = it }, fullColor = false)
        check(updated !== rail(VDesignHomeView.NUVIO)) {
            "Icon-color policy change must rebuild the rail focus style"
        }
        home.removeAllViews()
    }
}
