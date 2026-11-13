package net.mullvad.mullvadvpn.app

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import java.net.Inet6Address
import java.net.InetAddress
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.mullvad.mullvadvpn.lib.model.Constraint
import net.mullvad.mullvadvpn.lib.model.DefaultDnsOptions
import net.mullvad.mullvadvpn.lib.model.DnsState
import net.mullvad.mullvadvpn.lib.model.IpVersion
import net.mullvad.mullvadvpn.lib.model.Mtu
import net.mullvad.mullvadvpn.lib.model.QuantumResistantState
import net.mullvad.mullvadvpn.lib.model.Settings
import net.mullvad.mullvadvpn.lib.model.WireguardConstraints
import net.mullvad.mullvadvpn.lib.repository.AutoStartAndConnectOnBootRepository
import net.mullvad.mullvadvpn.lib.repository.SettingsRepository
import net.mullvad.mullvadvpn.lib.repository.WireguardConstraintsRepository
import net.mullvad.mullvadvpn.lib.usecase.DeleteCustomDnsUseCase
import net.mullvad.mullvadvpn.lib.usecase.SystemVpnSettingsAvailableUseCase
import org.apache.commons.validator.routines.InetAddressValidator

/**
 * Native, D-pad-friendly views over Mullvad's existing persisted daemon settings.
 * No local copy of VPN preferences: every label is rendered from upstream StateFlows.
 */
internal class VpnSettingsUi(
    private val activity: ComponentActivity,
    private val settings: SettingsRepository,
    private val wireguard: WireguardConstraintsRepository,
    private val boot: AutoStartAndConnectOnBootRepository,
    private val deleteDns: DeleteCustomDnsUseCase,
    private val systemSettingsAvailable: SystemVpnSettingsAvailableUseCase,
    private val withDaemon: (String, suspend () -> Unit) -> Unit,
    private val focusRail: () -> Unit,
    private val dark: Boolean,
    private val ink: Int,
    private val secondary: Int,
    private val surface: Int,
    private val accent: Int,
) {
    private var latest: Settings? = null
    private var latestWireguard: WireguardConstraints? = null
    private var bootEnabled = false
    private var daemonReady = false
    private var dnsOpen = false
    private var dnsAddresses: List<String> = emptyList()
    private val filterControls = mutableListOf<FilterControl>()

    private lateinit var general: LinearLayout
    private lateinit var dns: LinearLayout
    private lateinit var feedback: TextView
    private lateinit var stateLine: TextView
    private lateinit var generalFirst: Button
    private lateinit var dnsFirst: Button
    private lateinit var lanButton: Button
    private lateinit var ipv6Button: Button
    private lateinit var quantumButton: Button
    private lateinit var ipVersionButton: Button
    private lateinit var mtuButton: Button
    private lateinit var bootButton: Button
    private lateinit var dnsModeButton: Button
    private lateinit var allFiltersButton: Button
    private lateinit var addDnsButton: Button
    private lateinit var dnsWarnings: TextView
    private lateinit var dnsList: LinearLayout

    val primaryFocus: View get() = generalFirst

    fun requestPrimaryFocus() {
        showDns(false)
        generalFirst.requestFocus()
    }

    fun handleBack(): Boolean {
        if (!dnsOpen) return false
        showDns(false)
        generalFirst.requestFocus()
        return true
    }

    fun build(): LinearLayout = column().apply {
        setPadding(dp(25), dp(15), dp(25), dp(25))
        addView(label("VPN-Einstellungen · Original Mullvad", 25f, true))
        stateLine = label("Einstellungen werden geladen …", 14f)
        feedback = label("Alle Änderungen werden vom Mullvad-Dienst gespeichert.", 14f)
        addView(stateLine)
        addView(feedback)

        general = column()
        generalFirst = action("DNS-Einstellungen öffnen") { showDns(true); dnsFirst.requestFocus() }
        general.addView(generalFirst, rowParams())
        general.addView(label("Mullvad DNS: Inhaltsfilter und eigene DNS-Server", 14f))
        lanButton = action("LAN-Freigabe") { toggleLan() }
        general.addView(lanButton, rowParams())
        ipv6Button = action("IPv6") { toggleIpv6() }
        general.addView(ipv6Button, rowParams())
        quantumButton = action("Post-Quanten-Schlüsselaustausch") { toggleQuantum() }
        general.addView(quantumButton, rowParams())
        ipVersionButton = action("WireGuard IP-Version") { selectIpVersion() }
        general.addView(ipVersionButton, rowParams())
        mtuButton = action("WireGuard MTU") { editMtu() }
        general.addView(mtuButton, rowParams())
        general.addView(label("Port-Obfuskation und Anti-Zensur folgen unter Erweitert (M10).", 13f))
        bootButton = action("Bei Geräte-Neustart verbinden") { toggleBoot() }
        general.addView(bootButton, rowParams())
        general.addView(label(
            "Mullvads Boot-Empfänger startet den VPN-Dienst nur bei erteilter Android-VPN-Freigabe. " +
                "Ein App-Start allein löst hier keinen zusätzlichen Autoconnect aus.", 13f))
        general.addView(action("Android Always-on / VPN-Sperrmodus öffnen") { openSystemVpnSettings() },
            rowParams())
        general.addView(label(
            "Always-on und ‚Verbindungen ohne VPN blockieren‘ gehören Android. " +
                "Ihr tatsächlicher Schalterstatus wird in den Systemeinstellungen angezeigt, " +
                "nicht von Plume erfunden. Die Verfügbarkeit hängt vom TV-Gerät ab.", 13f))
        addView(general)

        dns = column()
        dns.visibility = View.GONE
        dnsFirst = action("Zurück zu VPN-Einstellungen") { handleBack() }
        dns.addView(dnsFirst, rowParams())
        dns.addView(label("DNS und Inhaltsfilter", 23f, true))
        dnsModeButton = action("Eigene DNS-Server") { toggleCustomDns() }
        dns.addView(dnsModeButton, rowParams())
        dns.addView(label(
            "Im Standard-DNS-Modus können Inhaltsfilter aktiv sein. " +
                "Bei eigenen DNS-Servern sind Mullvads Inhaltsfilter nicht aktiv.", 14f))
        addFilter("Werbung", { it.blockAds }, { it.copy(blockAds = !it.blockAds) })
        addFilter("Tracker", { it.blockTrackers }, { it.copy(blockTrackers = !it.blockTrackers) })
        addFilter("Schadsoftware", { it.blockMalware }, { it.copy(blockMalware = !it.blockMalware) })
        addFilter("Inhalte für Erwachsene", { it.blockAdultContent },
            { it.copy(blockAdultContent = !it.blockAdultContent) })
        addFilter("Glücksspiel", { it.blockGambling },
            { it.copy(blockGambling = !it.blockGambling) })
        addFilter("Soziale Netzwerke", { it.blockSocialMedia },
            { it.copy(blockSocialMedia = !it.blockSocialMedia) })
        allFiltersButton = action("Alle Filter umschalten") { toggleAllFilters() }
        dns.addView(allFiltersButton, rowParams())
        dns.addView(label("Eigene DNS-Server (IPv4 oder IPv6)", 20f, true))
        dnsWarnings = label("", 14f)
        dns.addView(dnsWarnings)
        addDnsButton = action("DNS-Server hinzufügen") { editDns(null) }
        dns.addView(addDnsButton, rowParams())
        dnsList = column()
        dns.addView(dnsList)
        addView(dns)
    }

    fun start() {
        activity.lifecycleScope.launch {
            settings.settingsUpdates.collect {
                latest = it
                render()
            }
        }
        activity.lifecycleScope.launch {
            wireguard.wireguardConstraints.collect {
                latestWireguard = it
                render()
            }
        }
        activity.lifecycleScope.launch {
            boot.autoStartAndConnectOnBoot.collect {
                bootEnabled = it
                render()
            }
        }
    }

    fun onDaemonReady(ready: Boolean) {
        daemonReady = ready
        render()
    }

    fun onResume() { render() }

    private fun showDns(open: Boolean) {
        dnsOpen = open
        general.visibility = if (open) View.GONE else View.VISIBLE
        dns.visibility = if (open) View.VISIBLE else View.GONE
        render()
    }

    private fun render() {
        if (!::stateLine.isInitialized) return
        stateLine.text = when {
            !daemonReady -> "Mullvad-Dienst nicht bereit · Einstellungen derzeit nicht änderbar"
            latest == null -> "Warte auf bestätigte Original-Einstellungen"
            else -> "Mullvad-Einstellungen live · Werte werden im Originaldienst persistiert"
        }
        val s = latest
        val enabled = s != null && daemonReady
        lanButton.text = "Lokales Netzwerk erlauben: " + onOff(s?.allowLan)
        ipv6Button.text = "IPv6 im Tunnel: " + onOff(s?.tunnelOptions?.enableIpv6)
        quantumButton.text = "Post-Quanten-Schlüsselaustausch: " +
            onOff(s?.let { it.tunnelOptions.quantumResistant == QuantumResistantState.On })
        ipVersionButton.text = "WireGuard IP-Version: " + when (val value = latestWireguard?.ipVersion) {
            Constraint.Any -> "Automatisch"
            is Constraint.Only -> if (value.value == IpVersion.IPV4) "IPv4" else "IPv6"
            null -> "Wird geladen"
        }
        mtuButton.text = "WireGuard MTU: " +
            (s?.tunnelOptions?.mtu?.value?.toString() ?: "Automatisch")
        bootButton.text = "Bei Geräte-Neustart verbinden: " + onOff(bootEnabled)
        dnsModeButton.text = "Eigene DNS-Server verwenden: " +
            onOff(s?.let { it.tunnelOptions.dnsOptions.state == DnsState.Custom })
        listOf(lanButton, ipv6Button, quantumButton, mtuButton, dnsModeButton).forEach {
            it.isEnabled = enabled
        }
        ipVersionButton.isEnabled = enabled && latestWireguard != null
        bootButton.isEnabled = true  // Android component state, not a daemon RPC
        addDnsButton.isEnabled = enabled

        val options = s?.tunnelOptions?.dnsOptions
        val default = options?.defaultOptions ?: DefaultDnsOptions()
        val customMode = options?.state == DnsState.Custom
        allFiltersButton.isEnabled = enabled && !customMode
        filterControls.forEach { control ->
            control.button.text = control.title + ": " + onOff(control.isOn(default))
            control.button.isEnabled = enabled && !customMode
        }
        val addresses = options?.customOptions?.addresses.orEmpty()
        dnsWarnings.text = buildString {
            if (customMode && addresses.isEmpty()) append("Kein DNS-Server eingetragen. ")
            if (addresses.any { it.isLinkLocalAddress || it.isSiteLocalAddress } && s?.allowLan == false)
                append("Lokale DNS-Adressen sind ohne LAN-Freigabe möglicherweise unerreichbar. ")
            if (addresses.any { it is Inet6Address } && s?.tunnelOptions?.enableIpv6 == false)
                append("IPv6-DNS-Server sind bei deaktiviertem Tunnel-IPv6 möglicherweise unerreichbar.")
        }
        val listed = addresses.map { it.hostAddress ?: it.toString() }
        if (listed != dnsAddresses) {
            dnsAddresses = listed
            val focus = activity.currentFocus?.tag
            dnsList.removeAllViews()
            listed.forEachIndexed { index, address ->
                val item = action("DNS " + (index + 1) + ": " + address) { editDns(index) }
                item.tag = "dns-$address"
                item.isEnabled = enabled
                dnsList.addView(item, rowParams())
            }
            if (focus is String && focus.startsWith("dns-")) {
                dnsList.post {
                    (0 until dnsList.childCount).map { dnsList.getChildAt(it) }
                        .firstOrNull { it.tag == focus }?.requestFocus()
                        ?: dnsList.getChildAt(0)?.requestFocus()
                }
            }
        }
    }

    private fun toggleLan() {
        val next = latest?.allowLan?.not() ?: return
        execute("LAN-Freigabe") {
            settings.setLocalNetworkSharing(next).fold(
                { fail("LAN-Freigabe", it) }, { note("LAN-Freigabe gespeichert") })
        }
    }

    private fun toggleIpv6() {
        val next = latest?.tunnelOptions?.enableIpv6?.not() ?: return
        execute("IPv6") {
            settings.setIpv6Enabled(next).fold(
                { fail("IPv6", it) }, { note("IPv6-Einstellung gespeichert") })
        }
    }

    private fun toggleQuantum() {
        val next = if (latest?.tunnelOptions?.quantumResistant == QuantumResistantState.On)
            QuantumResistantState.Off else QuantumResistantState.On
        execute("Post-Quanten-Schlüsselaustausch") {
            settings.setWireguardQuantumResistant(next).fold(
                { fail("Post-Quanten-Schlüsselaustausch", it) }, { note("Einstellung gespeichert") })
        }
    }

    private fun selectIpVersion() {
        val choices = arrayOf("Automatisch", "IPv4", "IPv6")
        val selected = when (latestWireguard?.ipVersion) {
            Constraint.Any -> 0
            Constraint.Only(IpVersion.IPV4) -> 1
            Constraint.Only(IpVersion.IPV6) -> 2
            else -> 0
        }
        AlertDialog.Builder(activity).setTitle("WireGuard IP-Version")
            .setSingleChoiceItems(choices, selected) { dialog, position ->
                dialog.dismiss()
                val choice: Constraint<IpVersion> = when (position) {
                    1 -> Constraint.Only(IpVersion.IPV4)
                    2 -> Constraint.Only(IpVersion.IPV6)
                    else -> Constraint.Any
                }
                execute("IP-Version") {
                    wireguard.setDeviceIpVersion(choice).fold(
                        { fail("IP-Version", it) }, { note("IP-Version gespeichert") })
                }
            }.setNegativeButton("Abbrechen", null).show()
    }

    private fun editMtu() {
        val input = input(latest?.tunnelOptions?.mtu?.value?.toString().orEmpty())
        AlertDialog.Builder(activity).setTitle("WireGuard MTU (1280–1420)")
            .setMessage("Leeres Feld bedeutet nicht automatisch. Für Standard explizit zurücksetzen.")
            .setView(input)
            .setPositiveButton("Speichern", null)
            .setNeutralButton("Standard") { _, _ ->
                execute("MTU zurücksetzen") {
                    settings.resetWireguardMtu().fold(
                        { fail("MTU", it) }, { note("MTU auf Automatisch gesetzt") })
                }
            }
            .setNegativeButton("Abbrechen", null)
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        Mtu.fromString(input.text.toString().trim()).fold(
                            { fail("MTU", it) },
                            { mtu ->
                                dialog.dismiss()
                                execute("MTU") {
                                    settings.setWireguardMtu(mtu).fold(
                                        { fail("MTU", it) }, { note("MTU gespeichert") })
                                }
                            }
                        )
                    }
                }
                dialog.show()
            }
    }

    private fun toggleBoot() {
        try {
            boot.setAutoStartAndConnectOnBoot(!bootEnabled)
            note("Boot-Autoconnect über Mullvads originalen Android-Empfänger eingestellt")
        } catch (error: Exception) {
            fail("Boot-Autoconnect", error)
        }
    }

    private fun openSystemVpnSettings() {
        if (!systemSettingsAvailable()) {
            note("Dieses Android-TV-Gerät bietet keinen aufrufbaren System-VPN-Einstellungsbildschirm.")
            return
        }
        try {
            activity.startActivity(Intent("android.net.vpn.SETTINGS"))
        } catch (error: Exception) {
            fail("Android-VPN-Einstellungen", error)
        }
    }

    private fun addFilter(
        title: String,
        isOn: (DefaultDnsOptions) -> Boolean,
        toggle: (DefaultDnsOptions) -> DefaultDnsOptions,
    ) {
        val button = action(title) {
            execute("DNS-Filter $title") {
                settings.updateContentBlockers(toggle).fold(
                    { fail("DNS-Filter $title", it) }, { note("Filter gespeichert") })
            }
        }
        filterControls += FilterControl(title, button, isOn)
        dns.addView(button, rowParams())
    }

    private fun toggleAllFilters() {
        val allOn = latest?.tunnelOptions?.dnsOptions?.defaultOptions?.isAllBlockersEnabled ?: return
        execute("DNS-Filter") {
            settings.updateContentBlockers {
                DefaultDnsOptions(
                    blockAds = !allOn, blockTrackers = !allOn,
                    blockMalware = !allOn, blockAdultContent = !allOn,
                    blockGambling = !allOn, blockSocialMedia = !allOn,
                )
            }.fold({ fail("DNS-Filter", it) }, { note("Alle Filter gespeichert") })
        }
    }

    private fun toggleCustomDns() {
        val options = latest?.tunnelOptions?.dnsOptions ?: return
        if (options.state == DnsState.Custom) {
            execute("Standard-DNS") {
                settings.setDnsState(DnsState.Default).fold(
                    { fail("DNS-Modus", it) }, { note("Mullvad-Standard-DNS wieder aktiv") })
            }
        } else if (options.customOptions.addresses.isEmpty()) {
            editDns(null, enableAfterAdd = true)
        } else {
            execute("Eigene DNS") {
                settings.setDnsState(DnsState.Custom).fold(
                    { fail("DNS-Modus", it) }, { note("Eigene DNS-Server aktiviert") })
            }
        }
    }

    private fun editDns(index: Int?, enableAfterAdd: Boolean = false) {
        val addresses = latest?.tunnelOptions?.dnsOptions?.customOptions?.addresses ?: return
        val value = index?.let { addresses.getOrNull(it)?.hostAddress }.orEmpty()
        val edit = input(value)
        val dialog = AlertDialog.Builder(activity)
            .setTitle(if (index == null) "DNS-Server hinzufügen" else "DNS-Server bearbeiten")
            .setMessage("Nur numerische IPv4-/IPv6-Adressen; keine Hostnamen.")
            .setView(edit)
            .setPositiveButton("Speichern", null)
            .setNegativeButton("Abbrechen", null)
            .apply {
                if (index != null) setNeutralButton("Entfernen") { _, _ -> removeDns(index) }
            }.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val raw = edit.text.toString().trim()
                val validator = InetAddressValidator.getInstance()
                if (raw.isBlank() || !validator.isValid(raw)) {
                    edit.error = "Bitte eine gültige numerische IPv4-/IPv6-Adresse eingeben."
                    return@setOnClickListener
                }
                val parsed = InetAddress.getByName(raw) // parsed only after upstream validator passes
                if (addresses.withIndex().any { it.index != index && it.value == parsed }) {
                    edit.error = "Diese DNS-Adresse ist bereits eingetragen."
                    return@setOnClickListener
                }
                dialog.dismiss()
                execute("Eigene DNS") {
                    val result = if (index == null) settings.addCustomDns(parsed)
                        else settings.setCustomDns(index, parsed)
                    result.fold(
                        { fail("DNS-Server", it) },
                        {
                            if (enableAfterAdd) {
                                settings.setDnsState(DnsState.Custom).fold(
                                    { fail("DNS-Modus", it) },
                                    { note("DNS-Server hinzugefügt und aktiviert") },
                                )
                            } else note("DNS-Server gespeichert")
                        },
                    )
                }
            }
        }
        dialog.show()
    }

    private fun removeDns(index: Int) {
        execute("DNS-Server entfernen") {
            deleteDns(index).fold(
                { fail("DNS-Server entfernen", it) },
                { count ->
                    if (count == 0 && latest?.tunnelOptions?.dnsOptions?.state == DnsState.Custom) {
                        settings.setDnsState(DnsState.Default).fold(
                            { fail("DNS-Modus", it) },
                            { note("Letzter DNS-Server gelöscht · Standard-DNS aktiv") },
                        )
                    } else note("DNS-Server gelöscht")
                },
            )
        }
    }

    private fun execute(name: String, block: suspend () -> Unit) {
        if (!daemonReady || latest == null) {
            note("$name: Mullvad-Dienst oder Einstellungen noch nicht bereit.")
            return
        }
        withDaemon(name) {
            try { block() } catch (error: Exception) { fail(name, error) }
        }
    }

    private fun note(value: String) { feedback.text = value }
    private fun fail(action: String, cause: Any?) {
        feedback.text = "$action fehlgeschlagen: $cause"
    }

    private fun onOff(value: Boolean?): String = when (value) {
        true -> "Ein"
        false -> "Aus"
        null -> "Wird geladen"
    }

    private fun input(value: String) = EditText(activity).apply {
        setText(value)
        setSingleLine(true)
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        setSelectAllOnFocus(true)
        setPadding(dp(18), dp(12), dp(18), dp(12))
    }

    private fun column() = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
    private fun rowParams() = LinearLayout.LayoutParams(-1, dp(52)).apply {
        bottomMargin = dp(7)
    }
    private fun label(value: String, size: Float, strong: Boolean = false) =
        TextView(activity).apply {
            text = value
            textSize = size
            setTextColor(if (strong) ink else secondary)
            if (strong) setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(8), dp(4), dp(8))
        }

    private fun action(value: String, click: () -> Unit) = Button(activity).apply {
        text = value
        textSize = 16f
        isAllCaps = false
        isFocusable = true
        setTextColor(ink)
        background = buttonBackground(false)
        setOnFocusChangeListener { view, focused ->
            (view as Button).setTextColor(if (focused) Color.WHITE else ink)
            view.background = buttonBackground(focused)
        }
        setOnKeyListener { _, key, event ->
            if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_LEFT) {
                focusRail()
                true
            } else false
        }
        setOnClickListener { click() }
    }

    private fun buttonBackground(focused: Boolean) = GradientDrawable().apply {
        cornerRadius = dp(10).toFloat()
        setColor(if (focused) accent else surface)
        setStroke(dp(if (focused) 3 else 1),
            if (focused) Color.WHITE else if (dark) 0xFF63768C.toInt() else 0xFFABBCCB.toInt())
    }

    private fun dp(value: Int) = (value * activity.resources.displayMetrics.density).toInt()

    private data class FilterControl(
        val title: String,
        val button: Button,
        val isOn: (DefaultDnsOptions) -> Boolean,
    )
}
