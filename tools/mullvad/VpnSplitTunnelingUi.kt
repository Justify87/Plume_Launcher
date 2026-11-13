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
        setPadding(dp(24), dp(14), dp(24), dp(14))
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
            background = shape(surface, secondary, 1)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchText = s?.toString().orEmpty()
                    renderList()
                }
                override fun afterTextChanged(s: Editable?) = Unit
            })
        }
        searchRow.addView(search, LinearLayout.LayoutParams(0, dp(52), 1f))
        searchRow.addView(button("Suche leeren") {
            search.setText("")
            search.requestFocus()
        }, LinearLayout.LayoutParams(dp(165), dp(52)).apply { marginStart = dp(12) })
        addView(searchRow)
        count = label("App-Liste wird geladen", 14f)
        addView(count)
        addView(label(
            "Androids „Verbindungen ohne VPN blockieren“ kann Ausnahmen verhindern. " +
                "Eine gespeicherte Mullvad-Ausnahme beweist keine ungetunnelte Route. " +
                "Sperrmodus in Androids VPN-Systemeinstellungen prüfen.", 13f))
        list = ListView(activity).apply {
            divider = ColorDrawable(Color.TRANSPARENT)
            dividerHeight = dp(5)
            isVerticalScrollBarEnabled = true
            selector = selection()
            adapter = rows
            setOnItemClickListener { _, _, position, _ ->
                shown.getOrNull(position)?.let { changeApp(it) }
            }
            setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_LEFT) {
                    focusRail()
                    true
                } else false
            }
        }
        addView(list, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(6) })
        renderState()
    }

    fun start() {
        activity.lifecycleScope.launch {
            repository.splitTunnelingEnabled.collect {
                enabled = it
                renderState()
                renderList()
            }
        }
        activity.lifecycleScope.launch {
            repository.excludedApps.collect {
                excluded = it
                renderState()
                renderList()
            }
        }
        activity.lifecycleScope.launch {
            preferences.showSystemAppsSplitTunneling().collect {
                showSystem = it
                renderState()
                renderList()
            }
        }
        refreshApps()
    }

    fun onDaemonReady(ready: Boolean) {
        daemonReady = ready
        renderState()
        renderList()
    }

    fun onResume() {
        if (!registered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            activity.registerReceiver(packageReceiver, filter)
            registered = true
        }
        refreshApps()
    }

    fun onPause() {
        if (registered) {
            activity.unregisterReceiver(packageReceiver)
            registered = false
        }
    }

    private fun refreshApps() {
        val request = ++sequence
        activity.lifecycleScope.launch {
            try {
                val next = withContext(Dispatchers.IO) { applications.apps() }
                if (request != sequence) return@launch
                installed = next
                loaded = true
                renderList()
            } catch (error: Exception) {
                if (request == sequence) notice("App-Liste nicht lesbar: " +
                    (error.message ?: error.javaClass.simpleName))
            }
        }
    }

    private fun toggleEnabled() {
        if (!daemonReady) return notice("Mullvad-Dienst noch nicht bereit.")
        withDaemon("Split Tunneling") {
            repository.enableSplitTunneling(!enabled).fold(
                { notice("Aktivierung fehlgeschlagen: " + it) },
                { notice("Änderung über Mullvad gesendet; Status wird aktualisiert.") },
            )
        }
    }

    private fun toggleSystem() {
        activity.lifecycleScope.launch {
            try {
                preferences.setShowSystemAppsSplitTunneling(!showSystem)
            } catch (error: Exception) {
                notice("App-Anzeige fehlgeschlagen: " + (error.message ?: "Unbekannt"))
            }
        }
    }

    private fun changeApp(entry: Entry) {
        if (!daemonReady) return notice("Mullvad-Dienst noch nicht bereit.")
        val name = entry.app.packageName
        val remove = name in excluded
        withDaemon("App-Ausnahme") {
            val result = if (remove) repository.includeApp(name) else repository.excludeApp(name)
            result.fold(
                { notice("App-Ausnahme fehlgeschlagen: " + it) },
                { notice("Änderung an Mullvad gesendet; gespeicherte Antwort folgt.") },
            )
        }
    }

    private fun renderState() {
        if (!::toggle.isInitialized) return
        state.text = when {
            !daemonReady -> "Dienst nicht bereit · keine Änderungen möglich"
            enabled -> "Split Tunneling aktiv · " + excluded.size + " gespeicherte Ausnahmen"
            else -> "Split Tunneling aus · " + excluded.size + " gespeicherte Ausnahmen inaktiv"
        }
        toggle.text = if (enabled) "Split Tunneling: AN · OK zum Ausschalten"
            else "Split Tunneling: AUS · OK zum Einschalten"
        toggle.isEnabled = daemonReady
        system.text = if (showSystem) "System-/Hintergrundapps: sichtbar"
            else "System-/Hintergrundapps: ausblenden"
    }

    private fun renderList() {
        if (!::list.isInitialized) return
        val selected = shown.getOrNull(list.selectedItemPosition)?.app?.packageName?.value
        val known = installed.map { it.packageName }.toHashSet()
        val missing = excluded.filterNot { it in known }.map {
            Entry(AppData(it, 0, it.value, true), true)
        }
        shown = (installed.asSequence()
            .filter { showSystem || !it.isSystemApp || it.packageName in excluded }
            .map { Entry(it) }.toList() + missing)
            .filter {
                searchText.isBlank() ||
                    it.app.name.contains(searchText.trim(), ignoreCase = true) ||
                    it.app.packageName.value.contains(searchText.trim(), ignoreCase = true)
            }
            .sortedWith(compareByDescending<Entry> { it.app.packageName in excluded }
                .thenBy { it.app.name.lowercase(Locale.getDefault()) }
                .thenBy { it.app.packageName.value })
        rows.notifyDataSetChanged()
        count.text = if (!loaded) "Apps werden geladen …" else
            shown.size.toString() + " von " + installed.size + " VPN-fähigen Apps sichtbar · " +
                excluded.size + " Ausnahmen" + if (enabled) "" else " (inaktiv)"
        if (selected != null && list.hasFocus()) {
            val index = shown.indexOfFirst { it.app.packageName.value == selected }
            if (index >= 0) list.setSelection(index)
        }
    }

    // ListView recycles rows: hundreds of packages do not allocate hundreds of buttons/icons.
    private val rows = object : BaseAdapter() {
        override fun getCount() = shown.size
        override fun getItem(position: Int): Any = shown[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = (convertView as? LinearLayout) ?: LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), dp(7), dp(14), dp(7))
                minimumHeight = dp(68)
                addView(ImageView(activity).apply { scaleType = ImageView.ScaleType.FIT_CENTER },
                    LinearLayout.LayoutParams(dp(46), dp(46)).apply { marginEnd = dp(15) })
                addView(LinearLayout(activity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(label("", 17f, true))
                    addView(label("", 13f))
                }, LinearLayout.LayoutParams(0, -2, 1f))
            }
            val entry = shown[position]
            (row.getChildAt(0) as ImageView).setImageDrawable(
                if (entry.missing) null else runCatching {
                    activity.packageManager.getApplicationIcon(entry.app.packageName.value)
                }.getOrNull())
            val labels = row.getChildAt(1) as LinearLayout
            (labels.getChildAt(0) as TextView).text = entry.app.name
            val isExcluded = entry.app.packageName in excluded
            (labels.getChildAt(1) as TextView).text = when {
                entry.missing -> "Nicht installiert / sichtbar · OK: Ausnahme entfernen"
                isExcluded && enabled -> "Ausgeschlossen (ohne VPN konfiguriert)"
                isExcluded -> "Ausnahme gespeichert, derzeit inaktiv"
                enabled -> "Im VPN eingeschlossen"
                else -> "Nicht ausgeschlossen · Split Tunneling aus"
            } + " · " + entry.app.packageName.value
            row.contentDescription = entry.app.name + ", " +
                (if (isExcluded) "ausgeschlossen" else "eingeschlossen") + ", OK zum Umschalten"
            return row
        }
    }

    private fun selection() = StateListDrawable().apply {
        val focused = shape(accent, Color.WHITE, 3)
        addState(intArrayOf(android.R.attr.state_pressed), focused)
        addState(intArrayOf(android.R.attr.state_selected), focused)
        addState(intArrayOf(android.R.attr.state_focused), focused)
        addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
    }
    private fun button(title: String, onClick: () -> Unit) = Button(activity).apply {
        text = title
        isAllCaps = false
        textSize = 16f
        setTextColor(ink)
        background = shape(surface, secondary, 1)
        setOnFocusChangeListener { _, focused ->
            background = shape(if (focused) accent else surface,
                if (focused) Color.WHITE else secondary, if (focused) 3 else 1)
            setTextColor(if (focused) Color.WHITE else ink)
        }
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
    private fun params() = LinearLayout.LayoutParams(-1, dp(53)).apply { bottomMargin = dp(7) }
    private fun notice(value: String) { feedback.text = value }
    private fun dp(px: Int) = (px * activity.resources.displayMetrics.density).toInt()
}
