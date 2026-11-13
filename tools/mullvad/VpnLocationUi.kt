package net.mullvad.mullvadvpn.app

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.text.InputFilter
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.mullvad.mullvadvpn.lib.common.util.relaylist.newFilterOnSearch
import net.mullvad.mullvadvpn.lib.model.Constraint
import net.mullvad.mullvadvpn.lib.model.CustomList
import net.mullvad.mullvadvpn.lib.model.CustomListId
import net.mullvad.mullvadvpn.lib.model.CustomListName
import net.mullvad.mullvadvpn.lib.model.GeoLocationId
import net.mullvad.mullvadvpn.lib.model.Ownership
import net.mullvad.mullvadvpn.lib.model.ProviderId
import net.mullvad.mullvadvpn.lib.model.Providers
import net.mullvad.mullvadvpn.lib.model.RecentItem
import net.mullvad.mullvadvpn.lib.model.Recents
import net.mullvad.mullvadvpn.lib.model.RelayHopType
import net.mullvad.mullvadvpn.lib.model.RelayItem
import net.mullvad.mullvadvpn.lib.model.RelayItemId
import net.mullvad.mullvadvpn.lib.model.RelayListType
import net.mullvad.mullvadvpn.lib.repository.CustomListsRepository
import net.mullvad.mullvadvpn.lib.repository.RelayListFilterRepository
import net.mullvad.mullvadvpn.lib.repository.RelayListRepository
import net.mullvad.mullvadvpn.lib.repository.SettingsRepository
import net.mullvad.mullvadvpn.lib.usecase.FilteredCountries
import net.mullvad.mullvadvpn.lib.usecase.FilteredRelayListUseCase
import net.mullvad.mullvadvpn.lib.usecase.ProviderToOwnershipsUseCase
import net.mullvad.mullvadvpn.lib.usecase.RecentsUseCase
import net.mullvad.mullvadvpn.lib.usecase.SelectSinglehopUseCase
import net.mullvad.mullvadvpn.lib.usecase.customlists.CustomListsRelayItemUseCase

/**
 * Original Mullvad relay repositories and use-cases presented as native ten-foot Views.
 * No relay downloads, local relay parser, parallel settings or tunnel implementation.
 * This screen chooses a SINGLE-HOP exit; M09 will reuse its row/navigation conventions.
 */
internal class VpnLocationUi(
    private val activity: ComponentActivity,
    private val relays: RelayListRepository,
    private val filteredRelays: FilteredRelayListUseCase,
    private val filters: RelayListFilterRepository,
    private val lists: CustomListsRepository,
    private val listItems: CustomListsRelayItemUseCase,
    private val recents: RecentsUseCase,
    private val settings: SettingsRepository,
    private val chooseSinglehop: SelectSinglehopUseCase,
    private val providerOwnership: ProviderToOwnershipsUseCase,
    private val withDaemon: (String, suspend () -> Unit) -> Unit,
    private val focusRail: () -> Unit,
    private val dark: Boolean,
    private val ink: Int,
    private val secondary: Int,
    private val surface: Int,
    private val accent: Int,
) {
    private var daemonReady = false
    private var originalCountries: List<RelayItem.Location.Country> = emptyList()
    private var filteredCountries = FilteredCountries()
    private var customLists: List<CustomList>? = null
    private var customItems: List<RelayItem.CustomList> = emptyList()
    private var recentItems: List<RecentItem>? = null
    private var recentsEnabled = false
    private var ownership: Constraint<Ownership> = Constraint.Any
    private var providers: Constraint<Providers> = Constraint.Any
    private var availableProviders: List<ProviderId> = emptyList()
    private var selected: Constraint<RelayItemId> = Constraint.Any
    private var chosenCountry: GeoLocationId.Country? = null
    private var chosenCity: GeoLocationId.City? = null
    private var chosenList: CustomListId? = null
    private var query = ""
    private var previousModel: Int? = null
    private var leftFocusKey: String? = null
    private var rightFocusKey: String? = null
    private var firstRight: View? = null
    private var lastLeft: View? = null

    private lateinit var status: TextView
    private lateinit var selectedLine: TextView
    private lateinit var feedback: TextView
    private lateinit var search: EditText
    private lateinit var leftScroll: ScrollView
    private lateinit var rightScroll: ScrollView
    private lateinit var leftRows: LinearLayout
    private lateinit var rightRows: LinearLayout

    val primaryFocus: View get() = search
    fun requestPrimaryFocus() { search.requestFocus() }

    fun build(): LinearLayout = column().apply {
        setPadding(dp(22), dp(13), dp(22), dp(14))
        addView(label("Serverstandorte · Singlehop-Ausgang", 24f, true))
        status = label("Mullvad-Relayliste wird geladen …", 14f)
        selectedLine = label("Aktuelle Auswahl: wird geladen", 15f, true)
        feedback = label("Relay-Auswahl und Filter werden direkt an Mullvad übermittelt.", 13f)
        addView(status)
        addView(selectedLine)
        addView(feedback)

        search = EditText(activity).apply {
            hint = "Land, Stadt oder Server suchen"
            isSingleLine = true
            textSize = 16f
            setTextColor(ink)
            setHintTextColor(secondary)
            setBackgroundColor(if (dark) 0xFF314257.toInt() else Color.WHITE)
            setPadding(dp(13), dp(6), dp(13), dp(6))
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    query = s?.toString().orEmpty()
                    chosenCountry = null
                    chosenCity = null
                    chosenList = null
                    repaint(true)
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_LEFT) {
                    focusRail()
                    true
                } else false
            }
        }
        addView(search, LinearLayout.LayoutParams(-1, dp(47)))

        val actions = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(action("Aktualisieren", "refresh") { refresh() }, flexible())
            addView(action("Eigentümer", "ownership") { chooseOwnership() }, flexible())
            addView(action("Anbieter", "providers") { chooseProviders() }, flexible())
            addView(action("Zuletzt an/aus", "recentsToggle") { toggleRecents() }, flexible())
            addView(action("+ Liste", "newList") { nameDialog(null) }, flexible())
        }
        addView(actions)
        addView(label("Links: Länder, zuletzt verwendet, Listen   ·   Rechts: Städte, Server, Listeninhalt", 12f))

        val twoColumns = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        leftScroll = ScrollView(activity).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = true
        }
        rightScroll = ScrollView(activity).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = true
        }
        leftRows = column()
        rightRows = column()
        leftScroll.addView(leftRows)
        rightScroll.addView(rightRows)
        twoColumns.addView(leftScroll, LinearLayout.LayoutParams(0, -1, 1f).apply {
            marginEnd = dp(12)
        })
        twoColumns.addView(rightScroll, LinearLayout.LayoutParams(0, -1, 1.15f))
        addView(twoColumns, LinearLayout.LayoutParams(-1, 0, 1f))
        repaint(true)
    }

    /** Observe upstream flows only while the VPN Activity exists; no launcher-owned backend. */
    fun start() {
        activity.lifecycleScope.launch {
            relays.relayList.collect { originalCountries = it; repaint() }
        }
        activity.lifecycleScope.launch {
            filteredRelays(RelayListType.Single).collect { filteredCountries = it; repaint() }
        }
        activity.lifecycleScope.launch {
            relays.selectedLocation.collect { selected = it; repaint() }
        }
        activity.lifecycleScope.launch {
            lists.customLists.collect { customLists = it; repaint() }
        }
        activity.lifecycleScope.launch {
            listItems().collect { customItems = it; repaint() }
        }
        activity.lifecycleScope.launch {
            recents(RelayListType.Single).collect { recentItems = it; repaint() }
        }
        activity.lifecycleScope.launch {
            settings.settingsUpdates.collect {
                recentsEnabled = it?.recents is Recents.Enabled
                repaint()
            }
        }
        activity.lifecycleScope.launch {
            filters.selectedExitOwnership.collect { ownership = it; repaint() }
        }
        activity.lifecycleScope.launch {
            filters.selectedExitProviders.collect { providers = it; repaint() }
        }
        activity.lifecycleScope.launch {
            providerOwnership().collect {
                availableProviders = it.keys.sorted()
                repaint()
            }
        }
    }

    fun onDaemonReady(ready: Boolean) {
        daemonReady = ready
        repaint()
    }

    private fun countries(): List<RelayItem.Location.Country> {
        val all = filteredCountries.countries
        return if (query.isBlank()) all else all.newFilterOnSearch(query).matchedCountries
    }

    private fun repaint(force: Boolean = false) {
        if (!::status.isInitialized) return
        status.text = when {
            !daemonReady -> "Mullvad-Dienst nicht bereit / offline. Keine Live-Standortauswahl."
            originalCountries.isEmpty() -> "Relayliste noch nicht verfügbar. Aktualisieren zum erneuten Laden."
            filteredCountries.countries.isEmpty() -> "Keine mit den gewählten Mullvad-Constraints kompatiblen Relays."
            countries().isEmpty() && query.isNotBlank() -> "Keine Treffer für diese Suche."
            else -> originalCountries.size.toString() + " Länder geladen · " +
                countries().size + " sichtbar · " +
                (if (ownership == Constraint.Any) "alle Eigentümer" else "Eigentümerfilter aktiv") +
                " · " + (if (providers == Constraint.Any) "alle Anbieter" else "Anbieterfilter aktiv")
        }
        selectedLine.text = "Gewählter Ausgang: " + when (val value = selected) {
            Constraint.Any -> "Automatisch (originale Einstellung)"
            is Constraint.Only -> displayName(value.value)
        }
        val countryRows = countries()
        if (chosenList == null && countryRows.none { it.id == chosenCountry }) {
            chosenCountry = countryRows.firstOrNull()?.id
            chosenCity = null
        }
        val signature = listOf(
            countryRows, customLists, customItems, recentItems, recentsEnabled,
            selected, chosenCountry, chosenCity, chosenList, daemonReady, query
        ).hashCode()
        if (!force && signature == previousModel) return
        previousModel = signature

        val lastFocused = activity.currentFocus
        val leftFocused = contains(leftRows, lastFocused)
        val rightFocused = contains(rightRows, lastFocused)
        val wasLeftKey = if (leftFocused) lastFocused?.tag as? String else null
        val wasRightKey = if (rightFocused) lastFocused?.tag as? String else null
        val leftY = leftScroll.scrollY
        val rightY = rightScroll.scrollY
        leftRows.removeAllViews()
        rightRows.removeAllViews()
        firstRight = null
        lastLeft = null

        if (recentItems != null) {
            leftRows.addView(label("Zuletzt verwendet", 16f, true))
            if (recentItems.isNullOrEmpty()) {
                leftRows.addView(label("Noch keine Standorte", 13f))
            }
            recentItems.orEmpty().take(5).forEachIndexed { index, recent ->
                if (recent is RecentItem.Relay) {
                    addLeft("recent:" + index, "↺ " + recent.item.name) { select(recent.item) }
                }
            }
        } else if (!recentsEnabled) {
            leftRows.addView(label("Zuletzt verwendet: deaktiviert", 13f))
        }
        leftRows.addView(label("Benutzerdefinierte Listen", 16f, true))
        if (customLists == null) leftRows.addView(label("Listen werden geladen …", 13f))
        customLists.orEmpty().forEach { item ->
            addLeft("list:" + item.id.value, "★ " + item.name.value +
                " (" + item.locations.size + ")") {
                chosenList = item.id
                chosenCity = null
                repaint(true)
                firstRight?.requestFocus()
            }
        }
        leftRows.addView(label("Länder", 16f, true))
        countryRows.forEach { country ->
            addLeft("country:" + country.id.code, country.name +
                if (!country.active) " · inaktiv" else "") {
                chosenList = null
                chosenCountry = country.id
                chosenCity = null
                repaint(true)
                firstRight?.requestFocus()
            }
        }
        if (countryRows.isEmpty()) leftRows.addView(label("Keine Länder in der Auswahl.", 14f))

        val activeList = customLists.orEmpty().firstOrNull { it.id == chosenList }
        if (activeList != null) {
            renderList(activeList)
        } else {
            val country = countryRows.firstOrNull { it.id == chosenCountry }
            if (country != null) renderCountry(country)
            else rightRows.addView(label("Links einen Standort oder eine Liste auswählen.", 16f))
        }

        leftScroll.post {
            leftScroll.scrollTo(0, leftY)
            if (leftFocused) findKey(leftRows, wasLeftKey)?.requestFocus()
        }
        rightScroll.post {
            rightScroll.scrollTo(0, rightY)
            if (rightFocused) findKey(rightRows, wasRightKey)?.requestFocus()
        }
    }

    private fun renderCountry(country: RelayItem.Location.Country) {
        addRight("select:country", "✓ Land wählen: " + country.name, country.active) {
            select(country)
        }
        addRight("add:country", "+ Land zu Liste hinzufügen", true) { chooseListFor(country) }
        val activeCity = country.cities.firstOrNull { it.id == chosenCity }
        if (activeCity == null) {
            rightRows.addView(label("Städte in " + country.name, 17f, true))
            country.cities.forEach { city ->
                addRight("city:" + city.id.code, city.name +
                    " (" + city.relays.size + " Server)" +
                    if (!city.active) " · inaktiv" else "", true) {
                    chosenCity = city.id
                    repaint(true)
                    firstRight?.requestFocus()
                }
            }
            if (country.cities.isEmpty()) rightRows.addView(label("Keine Städte verfügbar.", 14f))
        } else {
            addRight("back:cities", "← Alle Städte", true) {
                chosenCity = null
                repaint(true)
            }
            addRight("select:city", "✓ Stadt wählen: " + activeCity.name, activeCity.active) {
                select(activeCity)
            }
            addRight("add:city", "+ Stadt zu Liste hinzufügen", true) {
                chooseListFor(activeCity)
            }
            rightRows.addView(label("Server in " + activeCity.name, 17f, true))
            activeCity.relays.forEach { relay ->
                addRight("server:" + relay.id.code,
                    relay.name + " · " + relay.provider.value +
                        (if (relay.ownership == Ownership.MullvadOwned) " · Mullvad" else " · gemietet") +
                        if (!relay.active) " · inaktiv" else "", relay.active) {
                    select(relay)
                }
                // Server assignment stays reachable without treating a click as a VPN selection.
                addRight("add:" + relay.id.code, "  + " + relay.name + " zu Liste", true) {
                    chooseListFor(relay)
                }
            }
            if (activeCity.relays.isEmpty()) {
                rightRows.addView(label("Keine kompatiblen Server in dieser Stadt.", 14f))
            }
        }
    }

    private fun renderList(list: CustomList) {
        rightRows.addView(label("Liste: " + list.name.value, 20f, true))
        addRight("list:choose", "✓ Als VPN-Standort wählen",
            customItems.firstOrNull { it.id == list.id }?.active == true) {
            customItems.firstOrNull { it.id == list.id }?.let(::select)
        }
        addRight("list:rename", "Liste umbenennen", true) { nameDialog(list) }
        addRight("list:delete", "Liste löschen", true) { confirmDelete(list) }
        rightRows.addView(label("Standorte hinzufügen: links ein Land wählen, dann \"+ … zu Liste\".", 13f))
        if (list.locations.isEmpty()) rightRows.addView(label("Diese Liste ist leer.", 14f))
        val matched = customItems.firstOrNull { it.id == list.id }?.locations.orEmpty()
        list.locations.forEachIndexed { index, geo ->
            val name = matched.firstOrNull { it.id == geo }?.name ?: displayName(geo)
            addRight("list:remove:" + index, "− " + name + " entfernen", true) {
                updateLocations(list, list.locations.filterNot { it == geo })
            }
        }
    }

    private fun select(item: RelayItem) {
        if (!item.active) return notice("Dieser Mullvad-Standort ist inaktiv.")
        withDaemon("Standort wählen") {
            chooseSinglehop(item).fold(
                { notice("Standort nicht übernommen: " + it) },
                { notice("Auswahl an Mullvad übergeben. Tunnelstatus folgt vom Originaldienst.") }
            )
            // Like the upstream SelectLocationViewModel, do NOT force a reconnect here.
        }
    }

    private fun refresh() = withDaemon("Relayliste aktualisieren") {
        relays.refreshRelayList().fold(
            { notice("Mullvad konnte die Relayliste nicht aktualisieren: " + it) },
            { notice("Mullvad hat das Aktualisieren der Relayliste angenommen.") }
        )
    }

    private fun toggleRecents() = withDaemon("Zuletzt verwendete Standorte") {
        settings.setRecentsEnabled(!recentsEnabled).fold(
            { notice("Einstellung konnte nicht übernommen werden: " + it) },
            { notice("Zuletzt verwendet wird aus Mullvads Einstellungen aktualisiert.") }
        )
    }

    private fun chooseOwnership() {
        val choices = arrayOf("Alle", "Eigene Mullvad-Server", "Gemietete Server")
        val current = when (ownership) {
            Constraint.Any -> 0
            Constraint.Only(Ownership.MullvadOwned) -> 1
            else -> 2
        }
        AlertDialog.Builder(activity).setTitle("Eigentümerfilter (Ausgang)")
            .setSingleChoiceItems(choices, current) { dialog, index ->
                val value: Constraint<Ownership> = when (index) {
                    1 -> Constraint.Only(Ownership.MullvadOwned)
                    2 -> Constraint.Only(Ownership.Rented)
                    else -> Constraint.Any
                }
                dialog.dismiss()
                withDaemon("Eigentümerfilter") {
                    filters.updateSelectedOwnership(value, RelayHopType.EXIT).fold(
                        { notice("Eigentümerfilter fehlgeschlagen: " + it) },
                        { notice("Eigentümerfilter an Mullvad übergeben.") }
                    )
                }
            }.setNegativeButton("Abbrechen", null).show()
    }

    private fun chooseProviders() {
        if (availableProviders.isEmpty()) {
            notice("Anbieterliste noch nicht von Mullvad geladen.")
            return
        }
        val names = availableProviders.map { it.value }.toTypedArray()
        val selectedSet = (providers as? Constraint.Only<Providers>)?.value
        val chosen = BooleanArray(names.size) {
            selectedSet == null || availableProviders[it] in selectedSet
        }
        AlertDialog.Builder(activity).setTitle("Anbieterfilter (Ausgang)")
            .setMultiChoiceItems(names, chosen) { _, index, checked ->
                chosen[index] = checked
            }
            .setNeutralButton("Alle") { _, _ ->
                withDaemon("Anbieterfilter") {
                    filters.updateSelectedProviders(Constraint.Any, RelayHopType.EXIT).fold(
                        { notice("Filter fehlgeschlagen: " + it) },
                        { notice("Alle Mullvad-Anbieter zugelassen.") }
                    )
                }
            }
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Übernehmen") { _, _ ->
                val chosenSet = availableProviders.filterIndexed { index, _ -> chosen[index] }.toSet()
                val value: Constraint<Providers> = if (chosenSet.size == availableProviders.size) {
                    Constraint.Any
                } else Constraint.Only(chosenSet)
                withDaemon("Anbieterfilter") {
                    filters.updateSelectedProviders(value, RelayHopType.EXIT).fold(
                        { notice("Filter fehlgeschlagen: " + it) },
                        { notice("Anbieterfilter an Mullvad übergeben.") }
                    )
                }
            }.show()
    }

    private fun nameDialog(old: CustomList?) {
        val edit = EditText(activity).apply {
            setSingleLine(true)
            filters = arrayOf(InputFilter.LengthFilter(CustomListName.MAX_LENGTH))
            setText(old?.name?.value.orEmpty())
            hint = "Listenname"
            setSelectAllOnFocus(true)
        }
        AlertDialog.Builder(activity)
            .setTitle(if (old == null) "Mullvad-Liste erstellen" else "Liste umbenennen")
            .setView(edit)
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Speichern") { _, _ ->
                val name = CustomListName.fromString(edit.text.toString())
                if (name.value.isBlank()) return@setPositiveButton notice("Listenname darf nicht leer sein.")
                withDaemon("Benutzerdefinierte Liste") {
                    if (old == null) {
                        lists.createCustomList(name).fold(
                            { notice("Liste konnte nicht erstellt werden: " + it) },
                            { notice("Liste in Mullvad angelegt.") }
                        )
                    } else {
                        lists.updateCustomListName(old.id, name).fold(
                            { notice("Liste konnte nicht umbenannt werden: " + it) },
                            { notice("Listenname in Mullvad geändert.") }
                        )
                    }
                }
            }.show()
    }

    private fun confirmDelete(item: CustomList) {
        AlertDialog.Builder(activity).setTitle("Liste löschen?")
            .setMessage(item.name.value + " wird aus dem Mullvad-Konto entfernt.")
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Löschen") { _, _ ->
                withDaemon("Liste löschen") {
                    lists.deleteCustomList(item.id).fold(
                        { notice("Liste konnte nicht gelöscht werden: " + it) },
                        {
                            chosenList = null
                            notice("Liste von Mullvad gelöscht.")
                        }
                    )
                }
            }.show()
    }

    private fun chooseListFor(location: RelayItem.Location) {
        val options = customLists.orEmpty()
        if (options.isEmpty()) {
            notice("Zuerst über \"+ Liste\" eine Mullvad-Liste erstellen.")
            return
        }
        AlertDialog.Builder(activity).setTitle(location.name + " – Liste auswählen")
            .setItems(options.map { it.name.value }.toTypedArray()) { _, index ->
                val list = options[index]
                val exists = location.id in list.locations
                val next = if (exists) {
                    list.locations.filterNot { it == location.id }
                } else list.locations + location.id
                updateLocations(list, next)
            }
            .setNegativeButton("Abbrechen", null).show()
    }

    private fun updateLocations(list: CustomList, locations: List<GeoLocationId>) =
        withDaemon("Listenstandorte") {
            lists.updateCustomListLocations(list.id, locations).fold(
                { notice("Liste konnte nicht aktualisiert werden: " + it) },
                { notice("Standorte der Liste " + list.name.value + " in Mullvad gespeichert.") }
            )
        }

    private fun displayName(id: RelayItemId): String = when (id) {
        is CustomListId -> customLists.orEmpty().firstOrNull { it.id == id }?.name?.value ?: id.value
        is GeoLocationId.Country ->
            originalCountries.firstOrNull { it.id == id }?.name ?: id.code
        is GeoLocationId.City -> originalCountries
            .firstOrNull { it.id == id.country }?.cities
            ?.firstOrNull { it.id == id }?.name ?: id.code
        is GeoLocationId.Hostname -> id.code
    }

    private fun addLeft(key: String, text: String, onClick: () -> Unit) {
        val control = action(text, key, onClick, true)
        leftRows.addView(control)
        if (lastLeft == null) lastLeft = control
    }

    private fun addRight(key: String, text: String, enabled: Boolean, onClick: () -> Unit) {
        val control = action(text, key, onClick, false).apply {
            isEnabled = enabled && daemonReady
            alpha = if (isEnabled) 1f else 0.52f
        }
        rightRows.addView(control)
        if (firstRight == null && control.isEnabled) firstRight = control
    }

    private fun action(
        title: String, key: String, onClick: () -> Unit, left: Boolean = false
    ) = Button(activity).apply {
        text = title
        tag = key
        isAllCaps = false
        textSize = 14f
        gravity = Gravity.CENTER_VERTICAL or Gravity.START
        setPadding(dp(12), dp(3), dp(12), dp(3))
        setTextColor(ink)
        val paint: (Boolean) -> Unit = { focused ->
            setTextColor(if (focused) Color.WHITE else ink)
            background = GradientDrawable().apply {
                cornerRadius = dp(9).toFloat()
                setColor(if (focused) accent else surface)
                setStroke(dp(if (focused) 3 else 1),
                    if (focused) Color.WHITE else secondary)
            }
        }
        paint(false)
        setOnFocusChangeListener { _, focused ->
            paint(focused)
            if (focused) {
                if (left) leftFocusKey = key else rightFocusKey = key
            }
        }
        setOnClickListener { onClick() }
        setOnKeyListener { _, code, event ->
            if (event.action != KeyEvent.ACTION_DOWN) false
            else if (left && code == KeyEvent.KEYCODE_DPAD_RIGHT) {
                firstRight?.requestFocus() == true
            } else if (code == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (left) { focusRail(); true }
                else findKey(leftRows, leftFocusKey)?.requestFocus() ?: (lastLeft?.requestFocus() == true)
            } else false
        }
    }.also {
        it.layoutParams = LinearLayout.LayoutParams(-1, dp(48)).apply {
            bottomMargin = dp(5)
        }
    }

    private fun contains(root: View, view: View?): Boolean {
        if (view == null) return false
        if (root == view) return true
        if (root !is android.view.ViewGroup) return false
        return (0 until root.childCount).any { contains(root.getChildAt(it), view) }
    }

    private fun findKey(rows: LinearLayout, key: String?): View? =
        (0 until rows.childCount)
            .asSequence().map { rows.getChildAt(it) }
            .firstOrNull { it.tag == key && it.isFocusable && it.isEnabled }

    private fun flexible() = LinearLayout.LayoutParams(0, dp(45), 1f).apply {
        marginEnd = dp(7)
    }

    private fun column() = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
    }

    private fun label(text: String, size: Float, bold: Boolean = false) =
        TextView(activity).apply {
            this.text = text
            textSize = size
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(if (bold) ink else secondary)
            setPadding(dp(2), dp(5), dp(2), dp(5))
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun notice(message: String) { feedback.text = message }
    private fun dp(pixels: Int) =
        (pixels * activity.resources.displayMetrics.density).toInt()
}
