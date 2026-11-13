package net.mullvad.mullvadvpn.app

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.VpnService
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.mullvad.mullvadvpn.di.paymentModule
import net.mullvad.mullvadvpn.di.uiModule
import net.mullvad.mullvadvpn.lib.grpc.GrpcConnectivityState
import net.mullvad.mullvadvpn.lib.grpc.ManagementService
import net.mullvad.mullvadvpn.lib.model.ActionAfterDisconnect
import net.mullvad.mullvadvpn.lib.model.DeviceState
import net.mullvad.mullvadvpn.lib.model.DisconnectReason
import net.mullvad.mullvadvpn.lib.model.GeoIpLocation
import net.mullvad.mullvadvpn.lib.model.MultihopMode
import net.mullvad.mullvadvpn.lib.model.RelayHopType
import net.mullvad.mullvadvpn.lib.model.RelayListType
import net.mullvad.mullvadvpn.lib.model.TunnelState
import net.mullvad.mullvadvpn.lib.repository.AutoStartAndConnectOnBootRepository
import net.mullvad.mullvadvpn.lib.repository.WireguardConstraintsRepository
import net.mullvad.mullvadvpn.lib.usecase.DeleteCustomDnsUseCase
import net.mullvad.mullvadvpn.lib.usecase.SystemVpnSettingsAvailableUseCase
import net.mullvad.mullvadvpn.lib.repository.ApiAccessRepository
import net.mullvad.mullvadvpn.lib.repository.RelayOverridesRepository
import net.mullvad.mullvadvpn.lib.repository.AccountRepository
import net.mullvad.mullvadvpn.lib.repository.DeviceRepository
import net.mullvad.mullvadvpn.lib.repository.NewDeviceRepository
import net.mullvad.mullvadvpn.lib.repository.VoucherRepository
import net.mullvad.mullvadvpn.lib.repository.ConnectionProxy
import net.mullvad.mullvadvpn.lib.repository.CustomListsRepository
import net.mullvad.mullvadvpn.lib.repository.RelayListFilterRepository
import net.mullvad.mullvadvpn.lib.repository.RelayListRepository
import net.mullvad.mullvadvpn.lib.repository.MultihopMigrationRepository
import net.mullvad.mullvadvpn.lib.repository.SettingsRepository
import net.mullvad.mullvadvpn.lib.usecase.FilteredRelayListUseCase
import net.mullvad.mullvadvpn.lib.usecase.ProviderToOwnershipsUseCase
import net.mullvad.mullvadvpn.lib.usecase.RecentsUseCase
import net.mullvad.mullvadvpn.lib.usecase.SelectSinglehopUseCase
import net.mullvad.mullvadvpn.lib.usecase.ModifyMultihopUseCase
import net.mullvad.mullvadvpn.lib.usecase.HopSelectionUseCase
import net.mullvad.mullvadvpn.lib.usecase.customlists.CustomListsRelayItemUseCase
import net.mullvad.mullvadvpn.lib.repository.UserPreferencesRepository
import net.mullvad.mullvadvpn.lib.repository.SplitTunnelingRepository
import net.mullvad.mullvadvpn.feature.splittunneling.impl.applist.ApplicationsProvider
import net.mullvad.mullvadvpn.serviceconnection.ServiceConnectionManager
import org.koin.android.ext.android.getKoin
import org.koin.core.context.loadKoinModules

/**
 * Plume's production ten-foot VPN surface. All tunnel and account data comes from
 * Mullvad's original management streams; this Activity never implements a tunnel.
 * Navigation surfaces are incrementally bound to the original upstream features.
 */
class VpnActivity : ComponentActivity() {
    private val management by lazy { getKoin().get<ManagementService>() }
    private val connection by lazy { getKoin().get<ConnectionProxy>() }
    private val accounts by lazy { getKoin().get<AccountRepository>() }
    private val devices by lazy { getKoin().get<DeviceRepository>() }
    private val vouchers by lazy { getKoin().get<VoucherRepository>() }
    private val newDevices by lazy { getKoin().get<NewDeviceRepository>() }
    private val accountUi by lazy {
        VpnAccountUi(this, accounts, devices, vouchers, newDevices, ::withDaemon,
            { tabs.getOrNull(2)?.requestFocus() }, dark, ink, secondary, surface, accent)
    }
    private val locationUi by lazy {
        VpnLocationUi(
            this,
            getKoin().get<RelayListRepository>(),
            getKoin().get<FilteredRelayListUseCase>(),
            getKoin().get<RelayListFilterRepository>(),
            getKoin().get<CustomListsRepository>(),
            getKoin().get<CustomListsRelayItemUseCase>(),
            getKoin().get<RecentsUseCase>(),
            getKoin().get<SettingsRepository>(),
            getKoin().get<SelectSinglehopUseCase>(),
            getKoin().get<ModifyMultihopUseCase>(),
            getKoin().get<ProviderToOwnershipsUseCase>(),
            ::withDaemon,
            { tabs.getOrNull(1)?.requestFocus() },
            dark, ink, secondary, surface, accent,
        )
    }
    private val settingsUi by lazy {
        VpnSettingsUi(
            this,
            getKoin().get<SettingsRepository>(),
            getKoin().get<WireguardConstraintsRepository>(),
            getKoin().get<AutoStartAndConnectOnBootRepository>(),
            getKoin().get<DeleteCustomDnsUseCase>(),
            getKoin().get<SystemVpnSettingsAvailableUseCase>(),
            ::withDaemon,
            { tabs.getOrNull(3)?.requestFocus() },
            dark, ink, secondary, surface, accent,
        )
    }
    private val splitTunnelingUi by lazy {
        VpnSplitTunnelingUi(
            this,
            getKoin().get<SplitTunnelingRepository>(),
            getKoin().get<ApplicationsProvider>(),
            getKoin().get<UserPreferencesRepository>(),
            ::withDaemon,
            { tabs.getOrNull(4)?.requestFocus() },
            ink, secondary, surface, accent,
        )
    }
    private val advancedUi by lazy {
        VpnAdvancedUi(
            this,
            getKoin().get<SettingsRepository>(),
            getKoin().get<WireguardConstraintsRepository>(),
            getKoin().get<HopSelectionUseCase>(),
            getKoin().get<MultihopMigrationRepository>(),
            getKoin().get<UserPreferencesRepository>(),
            ::withDaemon,
            { tabs.getOrNull(5)?.requestFocus() },
            { openAdvancedLocation(RelayListType.Multihop(RelayHopType.ENTRY)) },
            {
                val always = getKoin().get<WireguardConstraintsRepository>()
                    .wireguardConstraints.value?.multihop == MultihopMode.ALWAYS
                openAdvancedLocation(
                    if (always) RelayListType.Multihop(RelayHopType.EXIT)
                    else RelayListType.Single,
                )
            },
            { showPage(3); settingsUi.focusQuantum() },
            {
                withDaemon("Neu verbinden") {
                    connection.reconnect().fold(
                        { message("Neu verbinden fehlgeschlagen: " + it) },
                        { message("Neu verbinden angefordert. Status folgt vom Originaldienst.") },
                    )
                }
            },
            ink, secondary, surface, accent,
        )
    }
    private val networkUi by lazy {
        VpnNetworkUi(
            this,
            getKoin().get<SettingsRepository>(),
            getKoin().get<RelayListRepository>(),
            getKoin().get<WireguardConstraintsRepository>(),
            getKoin().get<ApiAccessRepository>(),
            getKoin().get<RelayOverridesRepository>(),
            ::withDaemon,
            { tabs.getOrNull(6)?.requestFocus() },
            ink, secondary, surface, accent,
        )
    }
    private val preferences by lazy { getKoin().get<UserPreferencesRepository>() }
    private val serviceBinding by lazy { ServiceConnectionManager(applicationContext) }

    private val sections = listOf("Status", "Standorte", "Konto", "VPN-Einstellungen", "Split Tunneling", "Erweitert", "Netzwerk", "Diagnose")
    private val tabs = mutableListOf<Button>()
    private val panels = mutableListOf<View>()
    private val focusTargets = mutableListOf<View>()
    private var page = 0
    private var returnFromAdvanced = false
    private var bound = false
    private var daemonReady = false
    // View state only: overwritten by original flows, never persisted or used as a VPN source.
    private var latestTunnel: TunnelState? = null
    private var latestDevice: DeviceState? = null
    private var awaitingSystemPermission = false

    private val dark by lazy {
        val home = getSharedPreferences("home", MODE_PRIVATE)
        when (home.getString("theme", "light")) {
            "dark" -> true
            "auto" -> resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
            "wallpaper" -> {
                val hue = home.getInt("glass_hue", 0xFF777777.toInt())
                (hue shr 16 and 255) * 299 + (hue shr 8 and 255) * 587 +
                    (hue and 255) * 114 < 128000
            }
            else -> false
        }
    }
    private val ink get() = if (dark) Color.WHITE else 0xFF192535.toInt()
    private val secondary get() = if (dark) 0xFFBCC9D8.toInt() else 0xFF536277.toInt()
    private val surface get() = if (dark) 0xFF1B293B.toInt() else 0xFFE7EDF4.toInt()
    private val accent = 0xFF247F98.toInt()

    private lateinit var grpcLine: TextView
    private lateinit var headline: TextView
    private lateinit var tunnelLine: TextView
    private lateinit var locationLine: TextView
    private lateinit var deviceLine: TextView
    private lateinit var diagnosticLine: TextView
    private lateinit var feedback: TextView
    private lateinit var consentButton: Button

    private val vpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            awaitingSystemPermission = false
            if (result.resultCode == Activity.RESULT_OK) connect()
            else message("Android-VPN-Freigabe abgelehnt. Keine Verbindung gestartet.")
            renderTunnel()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Mullvad's smartphone MainActivity normally loads these modules. Plume does not
        // launch that Activity, so its original account/voucher DI must be loaded here.
        loadKoinModules(listOf(uiModule, paymentModule))
        super.onCreate(savedInstanceState)
        setContentView(buildScreen())
        locationUi.start()
        settingsUi.start()
        splitTunnelingUi.start()
        advancedUi.start()
        networkUi.start()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (page == 3 && settingsUi.handleBack()) return
                if (page == 6 && networkUi.handleBack()) return
                if (page == 1 && returnFromAdvanced) {
                    returnFromAdvanced = false
                    locationUi.showFor(RelayListType.Single)
                    showPage(5)
                    advancedUi.requestPrimaryFocus()
                    return
                }
                if (page != 0) {
                    showPage(0)
                    tabs[0].requestFocus()
                } else finish()
            }
        })
        // StateFlow/gRPC are authoritative, including across activity recreation.
        lifecycleScope.launch {
            management.connectionState.collect { state ->
                daemonReady = state == GrpcConnectivityState.Ready
                locationUi.onDaemonReady(daemonReady)
                settingsUi.onDaemonReady(daemonReady)
                splitTunnelingUi.onDaemonReady(daemonReady)
                advancedUi.onDaemonReady(daemonReady)
                advancedUi.onTunnelState(if (daemonReady) latestTunnel else null)
                networkUi.onDaemonReady(daemonReady)
                grpcLine.text = "Mullvad-Dienst: " + if (daemonReady) "bereit" else state.toString()
                diagnosticLine.text = grpcLine.text
                renderTunnel()
                if (daemonReady && bound) accountUi.refresh()
            }
        }
        lifecycleScope.launch {
            management.tunnelState.collect { state ->
                latestTunnel = state
                advancedUi.onTunnelState(if (daemonReady) state else null)
                renderTunnel()
            }
        }
        lifecycleScope.launch {
            management.deviceState.collect { state ->
                latestDevice = state
                val value = when (state) {
                    is DeviceState.LoggedIn -> "Angemeldet · " + state.device.displayName()
                    DeviceState.LoggedOut -> "Nicht angemeldet"
                    DeviceState.Revoked -> "Geräteberechtigung widerrufen"
                }
                deviceLine.text = "Konto: " + value
                accountUi.onDeviceState(state)
            }
        }
        lifecycleScope.launch {
            accounts.accountData.collect { accountUi.onAccountData(it) }
        }
        lifecycleScope.launch {
            try {
                if (preferences.preferences().isPrivacyDisclosureAccepted) bindOriginalService()
                else {
                    consentButton.visibility = View.VISIBLE
                    message("Vor der ersten Mullvad-Initialisierung Datenschutzhinweis bestätigen.")
                    showPage(0)
                    consentButton.requestFocus()
                }
            } catch (error: Exception) {
                message("Initialisierung fehlgeschlagen: " + error.safeMessage())
            }
        }
        if (savedInstanceState != null) showPage(savedInstanceState.getInt("vpnPage", 0).coerceIn(0, 7))
        else tabs[0].requestFocus()
    }

    override fun onResume() {
        super.onResume()
        if (bound && daemonReady) accountUi.refresh()
        settingsUi.onResume()
        splitTunnelingUi.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("vpnPage", page)
        super.onSaveInstanceState(outState)
    }

    private fun openAdvancedLocation(type: RelayListType) {
        returnFromAdvanced = true
        locationUi.showFor(type)
        showPage(1)
        locationUi.requestPrimaryFocus()
    }

    private fun bindOriginalService() {
        if (bound) return
        try {
            serviceBinding.bind()
            bound = true
            consentButton.visibility = View.GONE
            accountUi.refresh()
            message("Originaldienst wird initialisiert. Tunnelstatus kommt direkt von Mullvad.")
        } catch (error: Exception) {
            message("Mullvad-Dienst konnte nicht gestartet werden: " + error.safeMessage())
        }
    }

    private fun connectWithPermission() {
        if (!bound) return message("Mullvad ist noch nicht initialisiert.")
        try {
            val request = VpnService.prepare(this)
            if (request == null) connect()
            else {
                awaitingSystemPermission = true
                renderTunnel()
                vpnPermission.launch(request)
            }
        } catch (error: Exception) {
            awaitingSystemPermission = false
            message("VPN-Systemfreigabe nicht verfügbar: " + error.safeMessage())
            renderTunnel()
        }
    }

    private fun connect() = withDaemon("Verbinden") {
        connection.connect().fold(
            { message("Verbinden fehlgeschlagen: " + it) },
            { message("Verbindungsaufbau angefordert. Status folgt vom Originaldienst.") }
        )
    }

    private fun withDaemon(action: String, block: suspend () -> Unit) {
        if (!bound) return message("Mullvad ist noch nicht initialisiert.")
        lifecycleScope.launch {
            try {
                if (withTimeoutOrNull(20000L) {
                    management.connectionState.first { it == GrpcConnectivityState.Ready }
                } == null) return@launch message(action + ": Mullvad-Daemon antwortet nicht.")
                block()
            } catch (error: Exception) {
                message(action + " fehlgeschlagen: " + error.safeMessage())
            }
        }
    }

    private fun renderTunnel() {
        if (!::headline.isInitialized) return
        val state = latestTunnel
        val title = when {
            awaitingSystemPermission -> "Android-VPN-Freigabe"
            !daemonReady -> "Dienst nicht erreichbar"
            state == null -> "Warte auf Tunnelstatus"
            state is TunnelState.Connected -> "VPN verbunden"
            state is TunnelState.Connecting -> "VPN verbindet"
            state is TunnelState.Disconnecting &&
                state.actionAfterDisconnect == ActionAfterDisconnect.Reconnect -> "VPN verbindet erneut"
            state is TunnelState.Disconnecting -> "VPN trennt"
            state is TunnelState.Error -> "VPN-Fehler"
            else -> "VPN getrennt"
        }
        headline.text = title
        tunnelLine.text = when {
            awaitingSystemPermission -> "Bitte den Android-Systemdialog bestätigen."
            !daemonReady -> "Keine bestätigten Live-Daten des Mullvad-Daemons."
            state is TunnelState.Error -> "Mullvad meldet: " + state.errorState
            state == null -> "Verbindung wird initialisiert."
            else -> "Originaler Tunnelzustand: " + state.javaClass.simpleName
        }
        val location = if (daemonReady) state?.location() else null
        val place = location?.let { listOfNotNull(it.city, it.country).joinToString(", ") }
        val ip = location?.ipv4?.hostAddress ?: location?.ipv6?.hostAddress
        val prefix = if (state is TunnelState.Connected) "VPN-Ausgang" else "Standort/IP laut Mullvad"
        locationLine.text = if (location != null) prefix + ": " +
            listOfNotNull(place, ip, location.hostname).filter { it.isNotBlank() }.joinToString(" · ")
        else "Standort und IP: keine bestätigten aktuellen Daten"
    }

    private fun buildScreen(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(36), dp(26), dp(36), dp(22))
            setBackgroundColor(if (dark) 0xFF101B29.toInt() else 0xFFF5F8FB.toInt())
        }
        root.addView(text("Mullvad VPN", 31f, true), LinearLayout.LayoutParams(-1, dp(55)))
        grpcLine = text("Mullvad-Dienst: initialisiert", 15f)
        root.addView(grpcLine)
        val columns = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val rail = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(18), dp(20), 0)
        }
        val detail = FrameLayout(this).apply {
            setBackgroundColor(surface)
            clipToPadding = false
            clipChildren = false
        }
        // Eight VPN pages exceed the available height on several 720p TV densities.
        // Let Android scroll focused rail items into view when navigating via D-Pad.
        val railScroll = ScrollView(this).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = false
            clipToPadding = false
            addView(rail, FrameLayout.LayoutParams(-1, -2))
        }
        columns.addView(railScroll, LinearLayout.LayoutParams(dp(230), -1))
        columns.addView(detail, LinearLayout.LayoutParams(0, -1, 1f))
        root.addView(columns, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(text("D-Pad: auswählen · OK: öffnen · Back: zur Übersicht / Plume", 13f))

        val status = panel()
        headline = text("Warte auf Originaldienst", 26f, true)
        tunnelLine = text("Noch kein bestätigter Status", 17f)
        locationLine = text("Standort und IP: keine bestätigten aktuellen Daten", 16f)
        deviceLine = text("Konto: wird geladen", 16f)
        listOf(headline, tunnelLine, locationLine, deviceLine).forEach { status.addView(it) }
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val connectButton = button("Verbinden") { connectWithPermission() }
        actions.addView(connectButton, actionParams())
        actions.addView(button("Trennen") {
            withDaemon("Trennen") {
                connection.disconnect(DisconnectReason.USER_INITIATED_DISCONNECT_BUTTON).fold(
                    { message("Trennen fehlgeschlagen: " + it) },
                    { message("Trennung angefordert. Warte auf den Originalstatus.") }
                )
            }
        }, actionParams())
        actions.addView(button("Neu verbinden") {
            withDaemon("Neu verbinden") {
                connection.reconnect().fold(
                    { message("Erneutes Verbinden fehlgeschlagen: " + it) },
                    { message("Neu verbinden angefordert. Warte auf den Originalstatus.") }
                )
            }
        }, actionParams())
        status.addView(actions)
        consentButton = button("Datenschutzhinweis bestätigen und Dienst starten") {
            lifecycleScope.launch {
                try {
                    preferences.setPrivacyDisclosureAccepted()
                    bindOriginalService()
                } catch (error: Exception) {
                    message("Datenschutz-Einrichtung fehlgeschlagen: " + error.safeMessage())
                }
            }
        }.apply { visibility = View.GONE }
        status.addView(consentButton)
        status.addView(text("Datenschutzinformationen: mullvad.net/help/privacy-policy", 13f))
        feedback = text("Mullvad wird initialisiert.", 16f)
        status.addView(feedback)
        addPanel(detail, status)
        focusTargets += connectButton

        addLocationPanel(detail, locationUi.build())
        focusTargets += locationUi.primaryFocus

        val account = panel()
        account.addView(accountUi.build())
        addPanel(detail, account)
        focusTargets += accountUi.primaryFocus

        val vpnSettings = panel()
        vpnSettings.addView(settingsUi.build())
        addPanel(detail, vpnSettings)
        focusTargets += settingsUi.primaryFocus

        addLocationPanel(detail, splitTunnelingUi.build())
        focusTargets += splitTunnelingUi.primaryFocus

        val advanced = panel()
        advanced.addView(advancedUi.build())
        addPanel(detail, advanced)
        focusTargets += advancedUi.primaryFocus

        addLocationPanel(detail, networkUi.build())
        focusTargets += networkUi.primaryFocus

        val diagnosis = panel()
        diagnosis.addView(text("Diagnose", 25f, true))
        diagnosticLine = text("Mullvad-Dienst: wird geprüft", 17f)
        diagnosis.addView(diagnosticLine)
        diagnosis.addView(text("Loganzeige und Supportbericht: noch nicht integriert (M11).", 15f))
        val overviewButton = button("Zur Statusübersicht") { showPage(0); tabs[0].requestFocus() }
        diagnosis.addView(overviewButton, LinearLayout.LayoutParams(dp(245), dp(56)))
        addPanel(detail, diagnosis)
        focusTargets += overviewButton

        sections.forEachIndexed { index, name ->
            val tab = button(name) {
                showPage(index)
                if (index == 2) accountUi.requestPrimaryFocus()
                else if (index == 1) {
                    returnFromAdvanced = false
                    locationUi.showFor(RelayListType.Single)
                    locationUi.requestPrimaryFocus()
                }
                else if (index == 3) settingsUi.requestPrimaryFocus()
                else if (index == 4) splitTunnelingUi.requestPrimaryFocus()
                else if (index == 5) advancedUi.requestPrimaryFocus()
                else if (index == 6) networkUi.requestPrimaryFocus()
                else focusTargets[index].requestFocus()
            }
            tab.setOnFocusChangeListener { _, focused ->
                paint(tab, focused, page == index)
                if (focused) {
                    // Switching away from the temporary entry/exit browser restores
                    // normal single-hop mode, including D-Pad focus-only tab changes.
                    if (index != 1 && returnFromAdvanced) {
                        returnFromAdvanced = false
                        locationUi.showFor(RelayListType.Single)
                    } else if (index == 1 && !returnFromAdvanced) {
                        locationUi.showFor(RelayListType.Single)
                    }
                    showPage(index)
                }
            }
            tab.setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    showPage(index)
                    if (index == 2) accountUi.requestPrimaryFocus()
                    else if (index == 1) {
                        returnFromAdvanced = false
                        locationUi.showFor(RelayListType.Single)
                        locationUi.requestPrimaryFocus()
                    }
                    else if (index == 3) settingsUi.requestPrimaryFocus()
                    else if (index == 4) splitTunnelingUi.requestPrimaryFocus()
                    else if (index == 5) advancedUi.requestPrimaryFocus()
                else if (index == 6) networkUi.requestPrimaryFocus()
                    else focusTargets[index].requestFocus()
                    true
                } else false
            }
            tabs += tab
            rail.addView(tab, LinearLayout.LayoutParams(-1, dp(60)).apply { bottomMargin = dp(9) })
        }
        showPage(0)
        return root
    }

    private fun addLocationPanel(parent: FrameLayout, content: View) {
        // The location browser owns two independently scrolling D-Pad columns.
        // Do not nest it in a full-screen ScrollView.
        parent.addView(content, FrameLayout.LayoutParams(-1, -1))
        panels += content
    }

    private fun addPanel(parent: FrameLayout, content: LinearLayout) {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = false
            addView(content)
        }
        parent.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        panels += scroll
    }

    private fun panel() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(28), dp(18), dp(28), dp(22))
    }

    private fun showPage(next: Int) {
        page = next
        panels.forEachIndexed { i, view -> view.visibility = if (i == next) View.VISIBLE else View.GONE }
        tabs.forEachIndexed { i, button -> paint(button, button.isFocused, i == next) }
    }

    private fun button(title: String, click: () -> Unit) = Button(this).apply {
        text = title
        isAllCaps = false
        textSize = 16f
        isFocusable = true
        setTextColor(ink)
        paint(this, false)
        setOnFocusChangeListener { view, focused -> paint(view as Button, focused) }
        setOnClickListener { click() }
        setOnKeyListener { _, key, event ->
            if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_LEFT) {
                tabs.getOrNull(page)?.requestFocus() == true
            } else false
        }
    }

    private fun paint(view: Button, focused: Boolean, selected: Boolean = false) {
        view.setTextColor(if (focused) Color.WHITE else ink)
        view.background = GradientDrawable().apply {
            cornerRadius = dp(12).toFloat()
            setColor(if (focused) accent else if (selected) {
                if (dark) 0xFF34495F.toInt() else 0xFFD6E4EF.toInt()
            } else surface)
            setStroke(dp(if (focused) 3 else 1), if (focused) Color.WHITE else secondary)
        }
    }

    private fun text(value: String, size: Float, strong: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(if (strong) ink else secondary)
        if (strong) setTypeface(typeface, Typeface.BOLD)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(7), 0, dp(7))
    }

    private fun actionParams() = LinearLayout.LayoutParams(0, dp(58), 1f).apply {
        marginEnd = dp(10)
    }

    private fun message(value: String) {
        feedback.text = value
        // Do not log account numbers or other Mullvad account responses.
    }

    private fun Throwable.safeMessage() = message ?: javaClass.simpleName
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onPause() {
        splitTunnelingUi.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (bound) {
            serviceBinding.unbind() // never disconnect or stop the original VPN on Back/Home
            bound = false
        }
        super.onDestroy()
    }
}
