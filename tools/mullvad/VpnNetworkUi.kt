package net.mullvad.mullvadvpn.app

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import java.net.InetAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mullvad.mullvadvpn.lib.model.ApiAccessMethod
import net.mullvad.mullvadvpn.lib.model.ApiAccessMethodSetting
import net.mullvad.mullvadvpn.lib.model.ApiAccessMethodName
import net.mullvad.mullvadvpn.lib.model.Cipher
import net.mullvad.mullvadvpn.lib.model.Constraint
import net.mullvad.mullvadvpn.lib.model.NewAccessMethodSetting
import net.mullvad.mullvadvpn.lib.model.ObfuscationMode
import net.mullvad.mullvadvpn.lib.model.Port
import net.mullvad.mullvadvpn.lib.model.PortRange
import net.mullvad.mullvadvpn.lib.model.RelayOverride
import net.mullvad.mullvadvpn.lib.model.Settings
import net.mullvad.mullvadvpn.lib.model.SocksAuth
import net.mullvad.mullvadvpn.lib.model.WireguardConstraints
import net.mullvad.mullvadvpn.lib.repository.ApiAccessRepository
import net.mullvad.mullvadvpn.lib.repository.RelayListRepository
import net.mullvad.mullvadvpn.lib.repository.RelayOverridesRepository
import net.mullvad.mullvadvpn.lib.repository.SettingsRepository
import net.mullvad.mullvadvpn.lib.repository.WireguardConstraintsRepository
import org.apache.commons.validator.routines.InetAddressValidator

/**
 * M10 TV-only surface. The pinned Mullvad repositories own every setting, validation
 * response, API test, selected method and relay override. No local VPN/proxy service.
 */
internal class VpnNetworkUi(
    private val activity: ComponentActivity,
    private val settings: SettingsRepository,
    private val relays: RelayListRepository,
    private val wireguard: WireguardConstraintsRepository,
    private val api: ApiAccessRepository,
    private val overrides: RelayOverridesRepository,
    private val withDaemon: (String, suspend () -> Unit) -> Unit,
    private val focusRail: () -> Unit,
    private val ink: Int,
    private val secondary: Int,
    private val surface: Int,
    private val accent: Int,
) {
    private var ready = false
    private var selectedPage = 0
    private var liveSettings: Settings? = null
    private var liveWireguard: WireguardConstraints? = null
    private var methods: List<ApiAccessMethodSetting>? = null
    private var currentMethod: ApiAccessMethodSetting? = null
    private var relayOverrides: List<RelayOverride>? = null
    private var wireguardRanges = emptyList<PortRange>()
    private var shadowsocksRanges = emptyList<PortRange>()
    private var ciphers = emptyList<Cipher>()

    private val nav = mutableListOf<Button>()
    private val pages = mutableListOf<View>()
    private val modeButtons = mutableMapOf<ObfuscationMode, Button>()
    private val portButtons = mutableMapOf<ObfuscationMode, Button>()
    private lateinit var stateLine: TextView
    private lateinit var modeInfo: TextView
    private lateinit var modeFeedback: TextView
    private lateinit var apiInfo: TextView
    private lateinit var apiFeedback: TextView
    private lateinit var apiRows: LinearLayout
    private lateinit var addMethod: Button
    private lateinit var overrideInfo: TextView
    private lateinit var overrideFeedback: TextView
    private lateinit var overrideRows: LinearLayout
    private lateinit var clearOverrides: Button
    private lateinit var importText: Button
    private lateinit var importFile: Button

    private val filePicker =
        activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                withDaemon("Server-IP-Datei importieren") {
                    try {
                        val json = withContext(Dispatchers.IO) {
                            activity.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)
                                ?.use { it.readText() }
                                ?: error("Datei konnte nicht gelesen werden")
                        }
                        applyOverrides(json)
                    } catch (error: Exception) {
                        overrideFeedback.text = "Datei konnte nicht importiert werden: " +
                            (error.message ?: error.javaClass.simpleName)
                    }
                }
            }
        }

    val primaryFocus: View get() = nav.first()
    fun requestPrimaryFocus() { nav.getOrNull(selectedPage)?.requestFocus() }

    fun build(): LinearLayout = column().apply {
        addView(label("Netzwerk und Zugang · Mullvad", 25f, true))
        stateLine = label("Mullvad-Dienst wird geladen", 14f)
        addView(stateLine)
        val selector = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, dp(10))
        }
        listOf("Verschleierung", "API-Zugriff", "Server-IP").forEachIndexed { i, name ->
            val button = action(name) { showPage(i, true) }
            button.setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_LEFT &&
                    i == 0) {
                    focusRail()
                    true
                } else false
            }
            selector.addView(button, LinearLayout.LayoutParams(0, dp(54), 1f))
            nav += button
        }
        addView(selector)
        val content = FrameLayout(activity)
        listOf(buildObfuscation(), buildApiAccess(), buildOverrides()).forEach { group ->
            val scroll = ScrollView(activity).apply {
                isFillViewport = true
                isVerticalScrollBarEnabled = false
                addView(group)
                visibility = View.GONE
            }
            content.addView(scroll, FrameLayout.LayoutParams(-1, -1))
            pages += scroll
        }
        addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        showPage(0, false)
        render()
    }

    fun start() {
        activity.lifecycleScope.launch {
            settings.settingsUpdates.collect { liveSettings = it; render() }
        }
        activity.lifecycleScope.launch {
            wireguard.wireguardConstraints.collect { liveWireguard = it; render() }
        }
        activity.lifecycleScope.launch {
            relays.portRanges.collect { wireguardRanges = it; render() }
        }
        activity.lifecycleScope.launch {
            relays.shadowsocksPortRanges.collect { shadowsocksRanges = it; render() }
        }
        activity.lifecycleScope.launch {
            api.accessMethods.collect { methods = it; render() }
        }
        activity.lifecycleScope.launch {
            api.currentAccessMethod.collect { currentMethod = it; render() }
        }
        activity.lifecycleScope.launch {
            overrides.relayOverrides.collect { relayOverrides = it; render() }
        }
    }

    private var ciphersRequested = false

    fun onDaemonReady(isReady: Boolean) {
        ready = isReady
        if (!ready) {
            ciphersRequested = false
            // Repositories can retain cached values: do not show them as effective while offline.
            stateLine.text = "Mullvad-Dienst offline · Änderungen gesperrt"
        } else if (!ciphersRequested) {
            // The UI starts before Plume binds the original service. Only request the
            // supported cipher list once gRPC is ready, never during Activity creation.
            ciphersRequested = true
            withDaemon("API-Zugriff: Chiffren laden") {
                api.getShadowsocksCiphers().fold(
                    {
                        ciphersRequested = false
                        apiFeedback.text = "Chiffren nicht geladen: " + it
                    },
                    { ciphers = it; render() },
                )
            }
        }
        render()
    }

    fun handleBack(): Boolean {
        if (selectedPage == 0) return false
        showPage(0, true)
        return true
    }

    private fun showPage(index: Int, focus: Boolean) {
        selectedPage = index
        pages.forEachIndexed { i, view -> view.visibility = if (i == index) View.VISIBLE else View.GONE }
        nav.forEachIndexed { i, button -> style(button, button.isFocused, i == index) }
        if (focus) nav[index].requestFocus()
    }

    private fun buildObfuscation() = column().apply {
        addView(label("Anti-Zensur und Verschleierung", 20f, true))
        modeInfo = label("Originale Tunneloptionen werden geladen", 15f)
        modeFeedback = label("Modus und Ports werden erst nach Bestätigung durch Mullvad aktiv.", 14f)
        addView(modeInfo)
        addView(modeFeedback)
        val modes = listOf(
            ObfuscationMode.Auto to "Automatisch",
            ObfuscationMode.WireguardPort to "WireGuard-Port (Verschleierung)",
            ObfuscationMode.Lwo to "LWO",
            ObfuscationMode.Quic to "QUIC",
            ObfuscationMode.Shadowsocks to "Shadowsocks",
            ObfuscationMode.Udp2Tcp to "WireGuard über TCP",
            ObfuscationMode.Off to "Aus",
        )
        modes.forEach { (mode, title) ->
            val button = action(title) { setMode(mode) }
            modeButtons[mode] = button
            addView(button, rowParams())
            if (mode in listOf(
                    ObfuscationMode.WireguardPort, ObfuscationMode.Lwo,
                    ObfuscationMode.Shadowsocks, ObfuscationMode.Udp2Tcp
                )) {
                val port = action("Port auswählen") { selectPort(mode) }
                portButtons[mode] = port
                addView(port, rowParams())
            }
        }
        addView(label(
            "Die zulässigen benutzerdefinierten Ports stammen aus Mullvads Relay-Daten. " +
                "QUIC hat im gewählten Originalrelease keine eigene Portauswahl.", 14f))
    }

    private fun buildApiAccess() = column().apply {
        addView(label("API-Zugangsmethoden", 20f, true))
        apiInfo = label("Warte auf Originalmethoden", 15f)
        apiFeedback = label("Vor dem Wechsel testet Mullvad die gewählte Methode.", 14f)
        addView(apiInfo)
        addView(apiFeedback)
        apiRows = column()
        addView(apiRows)
        addMethod = action("Eigene Zugangsmethode hinzufügen") { chooseProxyType(null) }
        addView(addMethod, rowParams())
        addView(label(
            "SOCKS5 und Shadowsocks werden ausschließlich über Mullvads originale " +
                "API-/Proxy-Verwaltung eingerichtet. Keine zusätzliche Proxy-App.", 14f))
    }

    private fun buildOverrides() = column().apply {
        addView(label("Server-IP-Overrides", 20f, true))
        overrideInfo = label("Warte auf Originalstatus", 15f)
        overrideFeedback = label("Import über Mullvads offizielle Settings-Patch-API.", 14f)
        addView(overrideInfo)
        addView(overrideFeedback)
        overrideRows = column()
        addView(overrideRows)
        importText = action("JSON-Override als Text importieren") { promptOverrideJson() }
        importFile = action("JSON-Datei auswählen") {
            filePicker.launch(arrayOf("application/json", "text/plain", "*/*"))
        }
        clearOverrides = action("Alle Overrides zurücksetzen") { confirmClearOverrides() }
        addView(importText, rowParams())
        addView(importFile, rowParams())
        addView(clearOverrides, rowParams())
        addView(label(
            "Importe und Rücksetzungen wirken auf Mullvads gespeicherte " +
                "Relay-Overrides. Ungültige Patches weist Mullvad zurück.", 14f))
    }

    private fun render() {
        if (!::stateLine.isInitialized) return
        val s = if (ready) liveSettings else null
        val mode = s?.obfuscationSettings?.selectedObfuscationMode
        stateLine.text = if (!ready) "Mullvad-Dienst offline · Änderungen gesperrt"
            else if (s == null) "Warte auf bestätigte Einstellungen"
            else "Originalzustand geladen · Änderungen wirken auf Mullvad"

        modeInfo.text = "Verschleierung: " + (mode?.name ?: "nicht verfügbar") +
            " · Multihop: " + ((if (ready) liveWireguard?.multihop?.name else null) ?: "unbekannt") +
            " · DAITA: " + when (s?.tunnelOptions?.daitaSettings?.enabled) {
                true -> "an"
                false -> "aus"
                null -> "unbekannt"
            } + " · Kombinationen prüft Mullvad."
        modeButtons.forEach { (key, button) ->
            button.text = modeTitle(key) + if (mode == key) "  ✓ Aktiv" else ""
            button.isEnabled = s != null
        }
        portButtons.forEach { (key, button) ->
            val constraint = s?.obfuscationSettings?.let { options ->
                when (key) {
                    ObfuscationMode.WireguardPort -> options.wireguardPort
                    ObfuscationMode.Udp2Tcp -> options.udp2tcp.port
                    ObfuscationMode.Shadowsocks -> options.shadowsocks.port
                    ObfuscationMode.Lwo -> options.lwo.port
                    else -> Constraint.Any
                }
            }
            button.text = "Port: " + when (constraint) {
                is Constraint.Only -> constraint.value.toString()
                Constraint.Any -> "Automatisch"
                null -> "nicht verfügbar"
            }
            button.isEnabled = s != null && mode == key
        }

        val list = if (ready) methods else null
        apiInfo.text = if (list == null) "API-Zugang: Dienststatus wird geladen"
            else "Aktuelle Methode: " + (currentMethod?.name?.value ?: "nicht gemeldet") +
                " · " + list.size + " Methoden"
        addMethod.isEnabled = list != null
        if (list != null) renderApiRows(list) else apiRows.removeAllViews()

        val entries = if (ready) relayOverrides else null
        overrideInfo.text = if (entries == null) "Server-IP-Overrides: nicht verfügbar"
            else entries.size.toString() + " aktive Override-Einträge"
        importText.isEnabled = ready
        importFile.isEnabled = ready
        clearOverrides.isEnabled = ready && !entries.isNullOrEmpty()
        overrideRows.removeAllViews()
        entries?.forEach { item ->
            overrideRows.addView(label(
                item.hostname + " · IPv4: " + (item.ipv4AddressIn?.hostAddress ?: "–") +
                    " · IPv6: " + (item.ipv6AddressIn?.hostAddress ?: "–"), 15f
            ))
        }
    }

    private fun modeTitle(mode: ObfuscationMode): String = when (mode) {
        ObfuscationMode.Auto -> "Automatisch"
        ObfuscationMode.WireguardPort -> "WireGuard-Port (Verschleierung)"
        ObfuscationMode.Lwo -> "LWO"
        ObfuscationMode.Quic -> "QUIC"
        ObfuscationMode.Shadowsocks -> "Shadowsocks"
        ObfuscationMode.Udp2Tcp -> "WireGuard über TCP"
        ObfuscationMode.Off -> "Aus"
    }

    private fun setMode(mode: ObfuscationMode) = withDaemon("Verschleierungsmodus") {
        settings.setObfuscation(mode).fold(
            { modeFeedback.text = "Mullvad konnte den Modus nicht setzen: " + it },
            { modeFeedback.text = "Änderung gesendet; Mullvads Originalstatus folgt." },
        )
    }

    private fun selectPort(mode: ObfuscationMode) {
        val presets = when (mode) {
            ObfuscationMode.Udp2Tcp -> listOf(80, 443, 5001)
            ObfuscationMode.WireguardPort, ObfuscationMode.Lwo -> listOf(51820, 53)
            else -> emptyList()
        }
        val choices = listOf("Automatisch") + presets.map { it.toString() } +
            if (mode == ObfuscationMode.Udp2Tcp) emptyList() else listOf("Benutzerdefiniert …")
        AlertDialog.Builder(activity)
            .setTitle(modeTitle(mode) + ": Port")
            .setItems(choices.toTypedArray()) { _, index ->
                when {
                    index == 0 -> updatePort(mode, Constraint.Any)
                    index <= presets.size -> updatePort(mode, Constraint.Only(Port(presets[index - 1])))
                    else -> promptCustomPort(mode)
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun promptCustomPort(mode: ObfuscationMode) {
        val field = EditText(activity).apply {
            hint = "Port (0–65535)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }
        AlertDialog.Builder(activity).setTitle(modeTitle(mode) + ": eigener Port")
            .setView(field)
            .setPositiveButton("Übernehmen", null)
            .setNegativeButton("Abbrechen", null)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        Port.fromString(field.text.toString()).fold(
                            { field.error = "Ungültiger Port: " + it },
                            { port ->
                                val ranges = when (mode) {
                                    ObfuscationMode.Shadowsocks ->
                                        listOf(PortRange(Port.MIN_VALUE..Port.MAX_VALUE))
                                    ObfuscationMode.Lwo, ObfuscationMode.WireguardPort ->
                                        wireguardRanges
                                    else -> emptyList()
                                }
                                if (ranges.none { port in it }) {
                                    field.error = "Nicht in Mullvads erlaubten Portbereichen"
                                } else {
                                    updatePort(mode, Constraint.Only(port))
                                    dialog.dismiss()
                                }
                            },
                        )
                    }
                }
                dialog.show()
            }
    }

    private fun updatePort(mode: ObfuscationMode, port: Constraint<Port>) =
        withDaemon("Verschleierungsport") {
            val result = when (mode) {
                ObfuscationMode.WireguardPort -> settings.setCustomWireguardPort(port)
                ObfuscationMode.Udp2Tcp -> settings.setCustomUdp2TcpObfuscationPort(port)
                ObfuscationMode.Shadowsocks -> settings.setCustomShadowsocksObfuscationPort(port)
                ObfuscationMode.Lwo -> settings.setCustomLwoObfuscationPort(port)
                else -> return@withDaemon
            }
            result.fold(
                { modeFeedback.text = "Port wurde abgelehnt: " + it },
                { modeFeedback.text = "Port angefordert; bestätigter Wert folgt." },
            )
        }

    private fun renderApiRows(list: List<ApiAccessMethodSetting>) {
        val focusId = (activity.currentFocus?.tag as? String)
        apiRows.removeAllViews()
        var toRestore: Button? = null
        list.forEach { item ->
            val isCurrent = currentMethod?.id == item.id
            val line = item.name.value + " · " + apiTypeName(item.apiAccessMethod) +
                (if (isCurrent) " · ✓ Aktuell" else "") +
                (if (item.enabled) "" else " · Deaktiviert")
            val button = action(line) { showMethodActions(item) }
            button.tag = item.id.toString()
            if (focusId == button.tag) toRestore = button
            apiRows.addView(button, rowParams())
        }
        if (toRestore != null) toRestore?.requestFocus()
    }

    private fun apiTypeName(method: ApiAccessMethod): String = when (method) {
        ApiAccessMethod.Direct -> "Direkt"
        ApiAccessMethod.Bridges -> "Brücken"
        ApiAccessMethod.EncryptedDns -> "Verschlüsseltes DNS"
        is ApiAccessMethod.CustomProxy.Socks5Remote -> "SOCKS5"
        is ApiAccessMethod.CustomProxy.Shadowsocks -> "Shadowsocks"
    }

    private fun showMethodActions(item: ApiAccessMethodSetting) {
        val custom = item.apiAccessMethod is ApiAccessMethod.CustomProxy
        val actions = mutableListOf("Testen", "Als aktuellen API-Zugang verwenden",
            if (item.enabled) "Deaktivieren" else "Aktivieren")
        if (custom) actions += listOf("Bearbeiten", "Löschen")
        AlertDialog.Builder(activity).setTitle(item.name.value)
            .setItems(actions.toTypedArray()) { _, choice ->
                when (choice) {
                    0 -> withDaemon("API-Methode testen") {
                        api.testApiAccessMethodById(item.id).fold(
                            { apiFeedback.text = "API-Test fehlgeschlagen: " + it },
                            { apiFeedback.text = "API-Test erfolgreich: " + item.name.value },
                        )
                    }
                    1 -> withDaemon("API-Zugang wechseln") {
                        api.testApiAccessMethodById(item.id).fold(
                            { apiFeedback.text = "Nicht gewechselt – API-Test fehlgeschlagen: " + it },
                            {
                                api.setCurrentApiAccessMethod(item.id).fold(
                                    { apiFeedback.text = "Wechsel fehlgeschlagen: " + it },
                                    { apiFeedback.text = "Wechsel angefordert; Live-Status folgt." },
                                )
                            },
                        )
                    }
                    2 -> {
                        if (item.enabled && methods?.count { it.enabled } == 1) {
                            apiFeedback.text = "Mindestens eine API-Zugangsmethode muss aktiv bleiben."
                        } else withDaemon("API-Methode umschalten") {
                            api.setEnabledApiAccessMethod(item.id, !item.enabled).fold(
                                { apiFeedback.text = "Aktivierung fehlgeschlagen: " + it },
                                { apiFeedback.text = "Änderung angefordert; Live-Status folgt." },
                            )
                        }
                    }
                    3 -> chooseProxyType(item)
                    4 -> confirmDelete(item)
                }
            }
            .setNegativeButton("Schließen", null)
            .show()
    }

    private fun confirmDelete(item: ApiAccessMethodSetting) {
        AlertDialog.Builder(activity).setTitle("API-Methode löschen?")
            .setMessage(item.name.value + " wird aus Mullvads Einstellungen entfernt.")
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Löschen") { _, _ ->
                withDaemon("API-Methode löschen") {
                    api.removeApiAccessMethod(item.id).fold(
                        { apiFeedback.text = "Löschen fehlgeschlagen: " + it },
                        { apiFeedback.text = "Entfernung angefordert; Live-Liste folgt." },
                    )
                }
            }.show()
    }

    private fun chooseProxyType(item: ApiAccessMethodSetting?) {
        val current = item?.apiAccessMethod
        val which = if (current is ApiAccessMethod.CustomProxy.Shadowsocks) 1 else 0
        AlertDialog.Builder(activity).setTitle("API-Zugangstyp")
            .setSingleChoiceItems(arrayOf("SOCKS5 Remote", "Shadowsocks"), which) { dialog, index ->
                dialog.dismiss()
                showProxyForm(item, index == 1)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showProxyForm(item: ApiAccessMethodSetting?, shadowsocks: Boolean) {
        val existing = item?.apiAccessMethod
        val socks = existing as? ApiAccessMethod.CustomProxy.Socks5Remote
        val shadow = existing as? ApiAccessMethod.CustomProxy.Shadowsocks
        val body = column()
        val name = edit("Name", item?.name?.value.orEmpty())
        val ip = edit("IP-Adresse", socks?.ip ?: shadow?.ip.orEmpty())
        val port = edit("Port", (socks?.port ?: shadow?.port)?.value?.toString().orEmpty(),
            InputType.TYPE_CLASS_NUMBER)
        body.addView(name)
        body.addView(ip)
        body.addView(port)
        val auth = CheckBox(activity).apply {
            text = "SOCKS5-Authentifizierung"
            isChecked = socks?.auth != null
        }
        val user = edit("Benutzername", socks?.auth?.username.orEmpty())
        val password = edit("Passwort", socks?.auth?.password ?: shadow?.password.orEmpty(),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val cipherLabels = ciphers.map { it.value }
        val cipher = Spinner(activity)
        if (shadowsocks) {
            val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, cipherLabels)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            cipher.adapter = adapter
            val currentIndex = ciphers.indexOf(shadow?.cipher)
            if (currentIndex >= 0) cipher.setSelection(currentIndex)
            body.addView(label("Shadowsocks-Chiffre (vom Mullvad-Dienst)", 14f))
            body.addView(cipher, rowParams())
            body.addView(password)
        } else {
            body.addView(auth)
            body.addView(user)
            body.addView(password)
            val update = {
                user.visibility = if (auth.isChecked) View.VISIBLE else View.GONE
                password.visibility = if (auth.isChecked) View.VISIBLE else View.GONE
            }
            auth.setOnCheckedChangeListener { _, _ -> update() }
            update()
        }
        val scroll = ScrollView(activity).apply {
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(body)
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(if (item == null) "API-Zugang hinzufügen" else "API-Zugang bearbeiten")
            .setView(scroll)
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Testen und speichern", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val methodName = name.text.toString()
                val address = ip.text.toString().trim()
                val parsedPort = Port.fromString(port.text.toString()).getOrNull()
                when {
                    methodName.isBlank() -> name.error = "Name erforderlich"
                    !InetAddressValidator.getInstance().isValid(address) ->
                        ip.error = "Gültige IPv4-/IPv6-Adresse erforderlich"
                    parsedPort == null -> port.error = "Port 0–65535 erforderlich"
                    shadowsocks && cipherLabels.isEmpty() ->
                        apiFeedback.text = "Mullvad hat keine unterstützten Shadowsocks-Chiffren geliefert."
                    !shadowsocks && auth.isChecked && user.text.isBlank() ->
                        user.error = "Benutzername erforderlich"
                    !shadowsocks && auth.isChecked && password.text.isBlank() ->
                        password.error = "Passwort erforderlich"
                    else -> {
                        val proxy: ApiAccessMethod.CustomProxy = if (shadowsocks) {
                            ApiAccessMethod.CustomProxy.Shadowsocks(
                                address, parsedPort!!,
                                password.text.toString().ifBlank { null },
                                ciphers[cipher.selectedItemPosition],
                            )
                        } else {
                            ApiAccessMethod.CustomProxy.Socks5Remote(
                                address, parsedPort!!,
                                if (auth.isChecked) SocksAuth(
                                    user.text.toString(), password.text.toString()
                                ) else null,
                            )
                        }
                        val safeName = ApiAccessMethodName.fromString(methodName)
                        withDaemon("API-Zugang testen") {
                            api.testCustomApiAccessMethod(proxy).fold(
                                { error ->
                                    apiFeedback.text = "API-Test fehlgeschlagen: " + error
                                    AlertDialog.Builder(activity)
                                        .setTitle("Mullvad-API-Test fehlgeschlagen")
                                        .setMessage(
                                            "Methode trotzdem speichern? Der Zugriff kann offline sein."
                                        )
                                        .setNegativeButton("Zurück", null)
                                        .setPositiveButton("Trotzdem speichern") { _, _ ->
                                            saveProxy(item, safeName, proxy)
                                            dialog.dismiss()
                                        }.show()
                                },
                                {
                                    saveProxy(item, safeName, proxy)
                                    dialog.dismiss()
                                },
                            )
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun saveProxy(
        item: ApiAccessMethodSetting?,
        name: ApiAccessMethodName,
        proxy: ApiAccessMethod.CustomProxy,
    ) = withDaemon("API-Zugang speichern") {
        val result = if (item == null) {
            api.addApiAccessMethod(NewAccessMethodSetting(name, true, proxy))
        } else {
            // API return types differ for add/update; fold in their own branches below.
            api.updateApiAccessMethod(item.id, name, proxy).fold(
                { apiFeedback.text = "Speichern fehlgeschlagen: " + it },
                { apiFeedback.text = "Änderung gesendet; Originalstatus folgt." },
            )
            return@withDaemon
        }
        result.fold(
            { apiFeedback.text = "Hinzufügen fehlgeschlagen: " + it },
            { apiFeedback.text = "Hinzufügen gesendet; Originalstatus folgt." },
        )
    }

    private fun promptOverrideJson() {
        val text = EditText(activity).apply {
            hint = "Mullvad-Settings-Patch (JSON)"
            minLines = 5
            maxLines = 12
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setHorizontallyScrolling(false)
        }
        AlertDialog.Builder(activity).setTitle("Server-IP-Overrides importieren")
            .setView(text)
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Importieren", null)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        val patch = text.text.toString()
                        if (patch.isBlank()) text.error = "JSON darf nicht leer sein"
                        else {
                            withDaemon("Server-IP-Overrides") { applyOverrides(patch) }
                            dialog.dismiss()
                        }
                    }
                }
                dialog.show()
            }
    }

    private suspend fun applyOverrides(json: String) {
        overrides.applySettingsPatch(json).fold(
            { overrideFeedback.text = "Mullvad hat den Import abgelehnt: " + it },
            { overrideFeedback.text = "Import akzeptiert; bestätigte Overrides folgen." },
        )
    }

    private fun confirmClearOverrides() {
        AlertDialog.Builder(activity).setTitle("Alle Server-IP-Overrides entfernen?")
            .setMessage("Mullvad setzt seine Relay-IP-Overrides auf Standard zurück.")
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Zurücksetzen") { _, _ ->
                withDaemon("Server-IP-Overrides zurücksetzen") {
                    overrides.clearAllOverrides().fold(
                        { overrideFeedback.text = "Zurücksetzen fehlgeschlagen: " + it },
                        { overrideFeedback.text = "Reset angefordert; Originalstatus folgt." },
                    )
                }
            }.show()
    }

    private fun action(title: String, onClick: () -> Unit) = Button(activity).apply {
        text = title
        isAllCaps = false
        textSize = 16f
        isFocusable = true
        gravity = Gravity.CENTER_VERTICAL or Gravity.START
        setPadding(dp(14), dp(3), dp(14), dp(3))
        style(this, false)
        setOnFocusChangeListener { view, focus -> style(view as Button, focus) }
        setOnClickListener { onClick() }
        setOnKeyListener { _, key, event ->
            if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_LEFT) {
                focusRail()
                true
            } else false
        }
    }

    private fun style(button: Button, focused: Boolean, active: Boolean = false) {
        button.setTextColor(if (focused) Color.WHITE else ink)
        button.background = GradientDrawable().apply {
            cornerRadius = dp(10).toFloat()
            setColor(if (focused) accent else if (active) 0xFFBCDDE5.toInt() else surface)
            setStroke(dp(if (focused) 3 else 1), if (focused) Color.WHITE else secondary)
        }
    }

    private fun edit(hintText: String, initial: String, input: Int = InputType.TYPE_CLASS_TEXT) =
        EditText(activity).apply {
            hint = hintText
            setText(initial)
            inputType = input
            setSingleLine(true)
            textSize = 16f
        }

    private fun label(value: String, size: Float, strong: Boolean = false) =
        TextView(activity).apply {
            text = value
            textSize = size
            setTextColor(if (strong) ink else secondary)
            if (strong) setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(7), 0, dp(7))
        }

    private fun column() = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(8), dp(20), dp(16))
    }

    private fun rowParams() = LinearLayout.LayoutParams(-1, dp(56)).apply {
        bottomMargin = dp(5)
    }

    private fun dp(size: Int): Int = (size * activity.resources.displayMetrics.density).toInt()
}
