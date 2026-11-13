package net.mullvad.mullvadvpn.app

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.mullvad.mullvadvpn.lib.model.Constraint
import net.mullvad.mullvadvpn.lib.model.HopSelection
import net.mullvad.mullvadvpn.lib.model.MultihopMode
import net.mullvad.mullvadvpn.lib.model.QuantumResistantState
import net.mullvad.mullvadvpn.lib.model.Scenario
import net.mullvad.mullvadvpn.lib.model.Settings
import net.mullvad.mullvadvpn.lib.model.SplitFilterMigration
import net.mullvad.mullvadvpn.lib.model.TunnelState
import net.mullvad.mullvadvpn.lib.model.WireguardConstraints
import net.mullvad.mullvadvpn.lib.repository.MultihopMigrationRepository
import net.mullvad.mullvadvpn.lib.repository.SettingsRepository
import net.mullvad.mullvadvpn.lib.repository.UserPreferencesRepository
import net.mullvad.mullvadvpn.lib.repository.WireguardConstraintsRepository
import net.mullvad.mullvadvpn.lib.usecase.HopSelectionUseCase

/**
 * Advanced tunnel options are an Android-TV View over Mullvad's original daemon repositories.
 * No custom relay pairing, persisted UI shadow state, or WireGuard implementation.
 */
internal class VpnAdvancedUi(
    private val activity: ComponentActivity,
    private val settings: SettingsRepository,
    private val wireguard: WireguardConstraintsRepository,
    private val hops: HopSelectionUseCase,
    private val migration: MultihopMigrationRepository,
    private val preferences: UserPreferencesRepository,
    private val withDaemon: (String, suspend () -> Unit) -> Unit,
    private val focusRail: () -> Unit,
    private val openEntry: () -> Unit,
    private val openExit: () -> Unit,
    private val openQuantum: () -> Unit,
    private val reconnect: () -> Unit,
    private val ink: Int,
    private val secondary: Int,
    private val surface: Int,
    private val accent: Int,
) {
    private var daemonReady = false
    private var latest: Settings? = null
    private var constraints: WireguardConstraints? = null
    private var selectedHops: HopSelection? = null
    private var migrationState: SplitFilterMigration? = null
    private var tunnel: TunnelState? = null

    private lateinit var serviceLine: TextView
    private lateinit var summary: TextView
    private lateinit var activePath: TextView
    private lateinit var feedback: TextView
    private lateinit var migrationNote: TextView
    private lateinit var migrationButton: Button
    private lateinit var neverButton: Button
    private lateinit var whenNeededButton: Button
    private lateinit var alwaysButton: Button
    private lateinit var entryButton: Button
    private lateinit var exitButton: Button
    private lateinit var daitaButton: Button
    private lateinit var quantumButton: Button
    private lateinit var reconnectButton: Button

    val primaryFocus: View get() = whenNeededButton
    fun requestPrimaryFocus() { whenNeededButton.requestFocus() }

    fun build(): LinearLayout = column().apply {
        setPadding(dp(24), dp(18), dp(24), dp(25))
        addView(label("Erweiterte Tunnelmodi · Mullvad", 25f, true))
        serviceLine = label("Warte auf Mullvad-Einstellungen", 14f)
        summary = label("Auswahl wird geladen", 16f)
        activePath = label("Aktiver Verbindungsweg wird geladen", 15f)
        feedback = label("Änderungen erscheinen erst nach Bestätigung durch Mullvad als aktiv.", 14f)
        addView(serviceLine)
        addView(summary)
        addView(activePath)
        addView(feedback)
        addView(label("Multihop-Modus", 20f, true))
        whenNeededButton = action("Bei Bedarf") { setMode(MultihopMode.WHEN_NEEDED) }
        alwaysButton = action("Immer") { setMode(MultihopMode.ALWAYS) }
        neverButton = action("Nie") { setMode(MultihopMode.NEVER) }
        addView(whenNeededButton, rowParams())
        addView(alwaysButton, rowParams())
        addView(neverButton, rowParams())
        addView(label(
            "Bei Bedarf wählt Mullvad selbst einen zusätzlichen Eingang, wenn der " +
                "Tunnel ihn benötigt. Nur bei „Immer“ ist ein eigener Eingang auswählbar.", 14f))
        entryButton = action("Eingang wählen") { openEntry() }
        exitButton = action("Ausgang wählen") { openExit() }
        addView(entryButton, rowParams())
        addView(exitButton, rowParams())
        addView(label("DAITA und Post-Quantum", 20f, true))
        daitaButton = action("DAITA") { toggleDaita() }
        addView(daitaButton, rowParams())
        addView(label(
            "DAITA kann die verfügbaren Eingangsserver einschränken. Die Kompatibilität " +
                "berechnet der originale Mullvad-Relayfilter.", 14f))
        quantumButton = action("Post-Quantum konfigurieren") { openQuantum() }
        addView(quantumButton, rowParams())
        addView(label(
            "Der originale Post-Quantum-Schalter liegt in VPN-Einstellungen; " +
                "dieses Release kennt nur An und Aus.", 14f))
        reconnectButton = action("Tunnel erneut verbinden") { reconnect() }
        addView(reconnectButton, rowParams())
        migrationNote = label("", 14f)
        migrationButton = action("Originalen Migrationshinweis bestätigen") { acknowledgeMigration() }
        addView(migrationNote)
        addView(migrationButton, rowParams())
        render()
    }

    fun start() {
        activity.lifecycleScope.launch {
            settings.settingsUpdates.collect { latest = it; render() }
        }
        activity.lifecycleScope.launch {
            wireguard.wireguardConstraints.collect { constraints = it; render() }
        }
        activity.lifecycleScope.launch {
            hops().collect { selectedHops = it; render() }
        }
        activity.lifecycleScope.launch {
            migration.multihopMigrationState.collect { migrationState = it; render() }
        }
    }

    fun onDaemonReady(ready: Boolean) {
        daemonReady = ready
        if (!ready) tunnel = null
        render()
    }

    fun onTunnelState(state: TunnelState?) {
        tunnel = state
        render()
    }

    private fun render() {
        if (!::serviceLine.isInitialized) return
        val mode = constraints?.multihop
        val s = latest
        val enabled = daemonReady && mode != null && s != null
        serviceLine.text = if (!daemonReady) "Mullvad-Dienst nicht bereit"
            else if (!enabled) "Warte auf bestätigte Einstellungen"
            else "Aktuelle Einstellungen aus Mullvads Originaldienst"

        fun mark(value: MultihopMode, title: String) =
            title + if (mode == value) "  ✓ Aktiv" else ""
        whenNeededButton.text = mark(MultihopMode.WHEN_NEEDED, "Bei Bedarf")
        alwaysButton.text = mark(MultihopMode.ALWAYS, "Immer")
        neverButton.text = mark(MultihopMode.NEVER, "Nie")
        listOf(whenNeededButton, alwaysButton, neverButton).forEach { it.isEnabled = enabled }

        val entry = (selectedHops as? HopSelection.Multi)?.entry?.getOrNull()?.name
            ?: constraints?.entryLocation?.let(::locationText)
            ?: "Wird geladen"
        val exit = selectedHops?.exit()?.getOrNull()?.name
            ?: s?.relaySettings?.relayConstraints?.location?.let(::locationText)
            ?: "Wird geladen"
        summary.text = "Konfiguration: Eingang " + entry + "  ·  Ausgang " + exit
        entryButton.text = "Eingang auswählen: " + entry
        exitButton.text = "Ausgang auswählen: " + exit
        entryButton.isEnabled = enabled && mode == MultihopMode.ALWAYS
        exitButton.isEnabled = enabled

        val connected = (tunnel as? TunnelState.Connected)?.location
        activePath.text = if (daemonReady && connected != null) {
            val inHost = connected.entryHostname
            val outHost = connected.hostname
            "Tatsächlicher Tunnel: " +
                (inHost?.let { "Eingang $it → " } ?: "Einzelhop / kein bestätigter Eingang → ") +
                (outHost ?: "Ausgang nicht gemeldet")
        } else {
            "Tatsächlicher Tunnelweg: keine aktive Verbindung bestätigt"
        }

        val daita = s?.tunnelOptions?.daitaSettings?.enabled
        daitaButton.text = "DAITA: " + onOff(daita)
        daitaButton.isEnabled = enabled
        val pq = s?.tunnelOptions?.quantumResistant
        quantumButton.text = "Post-Quantum: " + when (pq) {
            QuantumResistantState.On -> "An · VPN-Einstellungen öffnen"
            QuantumResistantState.Off -> "Aus · VPN-Einstellungen öffnen"
            null -> "Wird geladen"
        }
        quantumButton.isEnabled = enabled
        reconnectButton.isEnabled = daemonReady

        val pending = migrationState?.scenario?.let { it != Scenario.ONE_A } == true
        migrationNote.visibility = if (pending) View.VISIBLE else View.GONE
        migrationButton.visibility = if (pending) View.VISIBLE else View.GONE
        migrationButton.isEnabled = daemonReady
        if (pending) {
            migrationNote.text = "Mullvad hat ältere Multihop-Einstellungen migriert (" +
                migrationState?.scenario + "). Bitte Modus, Eingang und Ausgang überprüfen."
        }
    }

    private fun locationText(location: Constraint<net.mullvad.mullvadvpn.lib.model.RelayItemId>): String =
        when (location) {
            Constraint.Any -> "Automatisch"
            is Constraint.Only -> location.value.toString()
        }

    private fun setMode(mode: MultihopMode) = withDaemon("Multihop") {
        wireguard.setMultihop(mode).fold(
            { feedback.text = "Multihop konnte nicht gesetzt werden: " + it },
            { feedback.text = "Modus angefordert; Bestätigung aus Mullvads Einstellungen folgt." },
        )
    }

    private fun toggleDaita() {
        val current = latest?.tunnelOptions?.daitaSettings?.enabled ?: return
        withDaemon("DAITA") {
            settings.setDaitaEnabled(!current).fold(
                { feedback.text = "DAITA konnte nicht gesetzt werden: " + it },
                { feedback.text = "DAITA-Änderung angefordert; Mullvads Status folgt." },
            )
        }
    }

    private fun acknowledgeMigration() = withDaemon("Multihop-Migration") {
        migration.clearMultihopMigrationState().fold(
            { feedback.text = "Migrationshinweis konnte nicht bestätigt werden: " + it },
            {
                preferences.setHasSeenMultihopMigrationGuide()
                feedback.text = "Originaler Migrationshinweis bestätigt."
            },
        )
    }

    private fun onOff(value: Boolean?): String = when (value) {
        true -> "An"
        false -> "Aus"
        null -> "Wird geladen"
    }

    private fun action(title: String, onClick: () -> Unit): Button =
        Button(activity).apply {
            text = title
            textSize = 16f
            isAllCaps = false
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            setTextColor(ink)
            setPadding(dp(14), dp(3), dp(14), dp(3))
            val paint: (Boolean) -> Unit = { focused ->
                setTextColor(if (focused) Color.WHITE else ink)
                background = GradientDrawable().apply {
                    cornerRadius = dp(10).toFloat()
                    setColor(if (focused) accent else surface)
                    setStroke(dp(if (focused) 3 else 1), if (focused) Color.WHITE else secondary)
                }
            }
            paint(false)
            setOnFocusChangeListener { _, focused -> paint(focused) }
            setOnClickListener { onClick() }
            setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_LEFT) {
                    focusRail()
                    true
                } else false
            }
        }

    private fun rowParams() = LinearLayout.LayoutParams(-1, dp(53)).apply {
        bottomMargin = dp(7)
    }
    private fun column() = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
    private fun label(value: String, size: Float, bold: Boolean = false) =
        TextView(activity).apply {
            text = value
            textSize = size
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(if (bold) ink else secondary)
            setPadding(dp(3), dp(7), dp(3), dp(7))
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }
    private fun dp(value: Int) = (value * activity.resources.displayMetrics.density).toInt()
}
