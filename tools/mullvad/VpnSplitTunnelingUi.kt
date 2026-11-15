package net.mullvad.mullvadvpn.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mullvad.mullvadvpn.feature.splittunneling.impl.applist.AppData
import net.mullvad.mullvadvpn.feature.splittunneling.impl.applist.ApplicationsProvider
import net.mullvad.mullvadvpn.lib.model.PackageName
import net.mullvad.mullvadvpn.lib.repository.SplitTunnelingRepository
import net.mullvad.mullvadvpn.lib.repository.UserPreferencesRepository

/** TV selector backed exclusively by Mullvad's daemon settings and package provider. */
internal class VpnSplitTunnelingUi(
    private val activity: ComponentActivity,
    private val repository: SplitTunnelingRepository,
    private val applications: ApplicationsProvider,
    private val preferences: UserPreferencesRepository,
    private val withDaemon: (String, suspend () -> Unit) -> Unit,
    private val focusRail: () -> Unit,
    private val ink: Int,
    private val secondary: Int,
    private val surface: Int,
    private val accent: Int,
) {
    private data class Entry(val app: AppData, val missing: Boolean = false)
    private var daemonReady = false
    private var enabled = false
    private var showSystem = false
    private var loaded = false
    private var searchText = ""
    private var sequence = 0
    private var registered = false
    private var installed: List<AppData> = emptyList()
    private var excluded: Set<PackageName> = emptySet()
    private var shown: List<Entry> = emptyList()
    private lateinit var state: TextView
    private lateinit var feedback: TextView
    private lateinit var count: TextView
    private lateinit var toggle: Button
    private lateinit var system: Button
    private lateinit var search: EditText
    private lateinit var list: ListView

    val primaryFocus: View get() = toggle
    fun requestPrimaryFocus() { toggle.requestFocus() }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { refreshApps() }
    }

    fun build(): View = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), dp(7), dp(12), dp(8))
        addView(label("Split Tunneling · Mullvad", 25f, true))
        state = label("Warte auf Mullvad-Dienst", 15f)
        feedback = label("OK auf einer App ändert ihre VPN-Ausnahme.", 14f)
        addView(state)
        addView(feedback)
        toggle = button("Split Tunneling") { toggleEnabled() }
        addView(toggle, params())
        system = button("System-/Hintergrundapps anzeigen") { toggleSystem() }
        addView(system, params())
        val searchRow = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        search = EditText(activity).apply {
            isSingleLine = true
            textSize = 17f
            hint = "App oder Paket suchen"
            contentDescription = "Apps durchsuchen"
            setTextColor(ink)
            setHintTextColor(secondary)
            setOnFocusChangeListener { _, focused ->
                background = shape(surface, if (focused) accent else secondary, if (focused) 3 else 1)
            }
            setPadding(dp(12), 0, dp(12), 0)
            VpnTvStyle.paint(this, false)
        setOnFocusChangeListener { view, focused -> VpnTvStyle.paint(view as Button, focused) }
        setOnClickListener { onClick() }
        setOnKeyListener { _, key, event ->
            if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_LEFT) {
                focusRail()
                true
            } else false
        }
    }
    private fun label(value: String, size: Float, bold: Boolean = false) = TextView(activity).apply {
        text = value
        textSize = size
        setTextColor(if (bold) ink else secondary)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(5), 0, dp(5))
    }
    private fun shape(fill: Int, outline: Int, width: Int) = GradientDrawable().apply {
        cornerRadius = dp(11).toFloat()
        setColor(fill)
        setStroke(dp(width), outline)
    }
    private fun params() = LinearLayout.LayoutParams(-1, dp(46)).apply { bottomMargin = dp(4) }
    private fun notice(value: String) { feedback.text = value }
    private fun dp(px: Int) = (px * activity.resources.displayMetrics.density).toInt()
}
