package net.mullvad.mullvadvpn.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.VpnService
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.mullvad.mullvadvpn.lib.grpc.GrpcConnectivityState
import net.mullvad.mullvadvpn.lib.grpc.ManagementService
import net.mullvad.mullvadvpn.lib.model.AccountNumber
import net.mullvad.mullvadvpn.lib.model.DeviceState
import net.mullvad.mullvadvpn.lib.model.DisconnectReason
import net.mullvad.mullvadvpn.lib.model.TunnelState
import net.mullvad.mullvadvpn.lib.repository.AccountRepository
import net.mullvad.mullvadvpn.lib.repository.ConnectionProxy
import net.mullvad.mullvadvpn.lib.repository.UserPreferencesRepository
import net.mullvad.mullvadvpn.serviceconnection.ServiceConnectionManager
import org.koin.android.ext.android.getKoin

/**
 * M03-only TV harness. This class is copied into the ossProdDebug source set, never
 * into the release APK. It uses the original Mullvad daemon, gRPC and repositories.
 * Replace with Plume's production VPN screen in M04/M05.
 */
class PlumeVpnDebugActivity : ComponentActivity() {
    private val management by lazy { getKoin().get<ManagementService>() }
    private val accounts by lazy { getKoin().get<AccountRepository>() }
    private val connection by lazy { getKoin().get<ConnectionProxy>() }
    private val preferences by lazy { getKoin().get<UserPreferencesRepository>() }
    private val serviceBinding by lazy { ServiceConnectionManager(applicationContext) }

    private lateinit var daemonStatus: TextView
    private lateinit var tunnelStatus: TextView
    private lateinit var accountStatus: TextView
    private lateinit var feedback: TextView
    private lateinit var accountInput: EditText
    private lateinit var consentButton: Button
    private lateinit var connectButton: Button

    private var bound = false

    private val vpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                connect()
            } else {
                message("VPN-Systemfreigabe abgelehnt. Keine Verbindung gestartet.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        setContentView(createContent())

        // All status comes from the original daemon and management gRPC stream.
        lifecycleScope.launch {
            management.connectionState.collect { state ->
                daemonStatus.text = "Mullvad-Daemon / gRPC: $state"
            }
        }
        lifecycleScope.launch {
            management.tunnelState.collect { state ->
                tunnelStatus.text = when (state) {
                    is TunnelState.Connected -> "Tunnel: verbunden (Mullvad-Daemon)"
                    is TunnelState.Connecting -> "Tunnel: verbindet ..."
                    is TunnelState.Disconnecting -> "Tunnel: trennt ..."
                    is TunnelState.Disconnected -> "Tunnel: getrennt"
                    is TunnelState.Error -> "Tunnel-Fehler: ${state.errorState}"
                }
            }
        }
        lifecycleScope.launch {
            management.deviceState.collect { device ->
                accountStatus.text = when (device) {
                    is DeviceState.LoggedIn -> "Konto: angemeldet · ${device.device.displayName()}"
                    DeviceState.LoggedOut -> "Konto: nicht angemeldet"
                    DeviceState.Revoked -> "Konto: Geräteberechtigung widerrufen"
                }
            }
        }
        lifecycleScope.launch {
            try {
                if (preferences.preferences().isPrivacyDisclosureAccepted) {
                    bindOriginalService()
                } else {
                    consentButton.visibility = View.VISIBLE
                    consentButton.requestFocus()
                    message("Vor der Initialisierung den Mullvad-Datenschutzhinweis bestätigen.")
                }
            } catch (error: Exception) {
                message("Initialisierung fehlgeschlagen: ${error.message}")
            }
        }
    }

    private fun bindOriginalService() {
        if (bound) return
        try {
            serviceBinding.bind()
            bound = true
            consentButton.visibility = View.GONE
            connectButton.requestFocus()
            message("Originaldienst gestartet. Warte auf Mullvad-Daemon und Kontostatus.")
        } catch (error: Exception) {
            message("Mullvad-Dienst konnte nicht gebunden werden: ${error.message}")
        }
    }

    private fun connectWithPermission() {
        if (!bound) {
            message("Mullvad-Dienst ist noch nicht initialisiert.")
            return
        }
        try {
            val request = VpnService.prepare(this)
            if (request == null) connect() else vpnPermission.launch(request)
        } catch (error: Exception) {
            message("VPN-Systemdialog nicht verfügbar: ${error.message}")
        }
    }

    private fun connect() = runWithDaemon("Verbinden") {
        // connect() repeats Mullvad's original permission and conflict checks.
        connection.connect().fold(
            { message("Verbinden fehlgeschlagen: $it") },
            { message("Verbinden angefordert; tatsächlichen Tunnelstatus abwarten.") },
        )
    }

    private fun runWithDaemon(action: String, block: suspend () -> Unit) {
        if (!bound) {
            message("Mullvad-Dienst ist noch nicht initialisiert.")
            return
        }
        lifecycleScope.launch {
            try {
                val ready = withTimeoutOrNull(20_000L) {
                    management.connectionState.first { it == GrpcConnectivityState.Ready }
                } != null
                if (!ready) {
                    message("$action: Mullvad-Daemon antwortet nicht (20 s).")
                    return@launch
                }
                block()
            } catch (error: Exception) {
                message("$action fehlgeschlagen: ${error.message ?: error.javaClass.simpleName}")
            }
        }
    }

    private fun createContent(): View {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(48), dp(28), dp(48), dp(32))
        }
        column.addView(label("Mullvad VPN", 28f, true))
        column.addView(label("M03 · Debug-Testoberfläche · originaler Mullvad-Dienst", 14f))
        daemonStatus = label("Mullvad-Daemon / gRPC: noch nicht bereit", 17f)
        tunnelStatus = label("Tunnel: noch kein Originalstatus", 20f, true)
        accountStatus = label("Konto: wird geladen", 17f)
        column.addView(daemonStatus)
        column.addView(tunnelStatus)
        column.addView(accountStatus)

        column.addView(label(
            "Mullvad benötigt ein eigenes gültiges Konto und die VPN-Freigabe von Android. " +
                "Datenschutzinformationen: mullvad.net/help/privacy-policy", 15f
        ))
        consentButton = button("Hinweis bestätigen und Dienst starten") {
            lifecycleScope.launch {
                try {
                    preferences.setPrivacyDisclosureAccepted()
                    bindOriginalService()
                } catch (error: Exception) {
                    message("Datenschutz-Einrichtung fehlgeschlagen: ${error.message}")
                }
            }
        }.also {
            it.visibility = View.GONE
            column.addView(it)
        }

        accountInput = EditText(this).apply {
            hint = "16-stellige Mullvad-Kontonummer"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            isSingleLine = true
            textSize = 18f
            setTextColor(Color.WHITE)
            setHintTextColor(0xFFB1BBC8.toInt())
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }
        column.addView(accountInput, LinearLayout.LayoutParams(dp(360), dp(56)))
        column.addView(button("Mit bestehendem Konto anmelden") {
            val number = accountInput.text.toString().trim().replace(" ", "")
            if (!number.matches(Regex("\\d{16}"))) {
                message("Bitte eine gültige 16-stellige Kontonummer eingeben.")
            } else {
                runWithDaemon("Anmelden") {
                    accounts.login(AccountNumber(number)).fold(
                        { message("Anmeldung fehlgeschlagen: $it") },
                        {
                            accountInput.text.clear()
                            message("Anmeldung vom Mullvad-Daemon bestätigt.")
                        },
                    )
                }
            }
        })

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
        }
        connectButton = button("Verbinden") { connectWithPermission() }
        actions.addView(connectButton)
        actions.addView(button("Trennen") {
            runWithDaemon("Trennen") {
                connection.disconnect(DisconnectReason.USER_INITIATED_DISCONNECT_BUTTON).fold(
                    { message("Trennen fehlgeschlagen: $it") },
                    { message("Trennen angefordert; Tunnelstatus abwarten.") },
                )
            }
        })
        actions.addView(button("Neu verbinden") {
            runWithDaemon("Neu verbinden") {
                connection.reconnect().fold(
                    { message("Reconnect fehlgeschlagen: $it") },
                    { message("Reconnect angefordert; Tunnelstatus abwarten.") },
                )
            }
        })
        column.addView(actions)
        feedback = label("Dienst wird initialisiert ...", 16f)
        column.addView(feedback)
        column.addView(label(
            "BACK kehrt zu Plume zurück. Der VPN-Tunnel hängt nicht von der Home-Activity ab.", 14f
        ))

        return ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(0xFF101A28.toInt())
            addView(column, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }
    }

    private fun label(value: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.WHITE)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, dp(8), 0, dp(8))
    }

    private fun button(value: String, onClick: () -> Unit) = Button(this).apply {
        text = value
        textSize = 15f
        isAllCaps = false
        isFocusable = true
        setPadding(dp(12), dp(8), dp(12), dp(8))
        setTextColor(Color.WHITE)
        funShape(false)
        setOnFocusChangeListener { _, focused ->
            funShape(focused)
        }
        setOnClickListener { onClick() }
    }

    private fun Button.funShape(focused: Boolean) {
        background = GradientDrawable().apply {
            setColor(if (focused) 0xFF266B89.toInt() else 0xFF24344A.toInt())
            cornerRadius = dp(12).toFloat()
            setStroke(dp(2), if (focused) Color.WHITE else 0xFF3B5067.toInt())
        }
    }

    private fun message(value: String) {
        feedback.text = value
        android.util.Log.i("PlumeM03", value)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        if (bound) {
            // Original Mullvad service remains alive on Android's VPN binding while connected.
            // No disconnect(), stopService() or daemon shutdown from launcher/activity lifecycle.
            serviceBinding.unbind()
            bound = false
        }
        super.onDestroy()
    }
}
