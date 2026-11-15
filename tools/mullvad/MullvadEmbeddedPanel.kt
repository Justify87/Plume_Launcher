package net.mullvad.mullvadvpn.app

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Typeface
import android.net.VpnService
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.mullvad.mullvadvpn.di.paymentModule
import net.mullvad.mullvadvpn.di.uiModule
import net.mullvad.mullvadvpn.feature.home.impl.connect.notificationbanner.InAppNotificationController
import net.mullvad.mullvadvpn.feature.splittunneling.impl.applist.ApplicationsProvider
import net.mullvad.mullvadvpn.lib.grpc.GrpcConnectivityState
import net.mullvad.mullvadvpn.lib.grpc.ManagementService
import net.mullvad.mullvadvpn.lib.model.ActionAfterDisconnect
import net.mullvad.mullvadvpn.lib.model.DeviceState
import net.mullvad.mullvadvpn.lib.model.DisconnectReason
import net.mullvad.mullvadvpn.lib.model.MultihopMode
import net.mullvad.mullvadvpn.lib.model.RelayHopType
import net.mullvad.mullvadvpn.lib.model.RelayListType
import net.mullvad.mullvadvpn.lib.model.TunnelState
import net.mullvad.mullvadvpn.lib.repository.*
import net.mullvad.mullvadvpn.lib.usecase.*
import net.mullvad.mullvadvpn.lib.usecase.customlists.CustomListsRelayItemUseCase
import net.mullvad.mullvadvpn.serviceconnection.ServiceConnectionManager
import org.koin.android.ext.android.getKoin
import org.koin.core.context.loadKoinModules

/**
 * Embedded Mullvad TV shell: the same original daemon/repositories and eight native
 * pages as VpnActivity, but hosted by Plume's existing Activity and permanent sidebar.
 * Unlike Activity embedding, it does not create a second window or a fake VPN state.
 */
internal class MullvadEmbeddedPanel private constructor(private val host: ComponentActivity) {
    companion object {
        /** Called reflectively by standalone Plume; must be created in onCreate, before STARTED. */
        @JvmStatic fun create(host: ComponentActivity): View {
            loadKoinModules(listOf(uiModule, paymentModule))
            return MullvadEmbeddedPanel(host).build()
        }
    }

    private val management by lazy { host.getKoin().get<ManagementService>() }
    private val connection by lazy { host.getKoin().get<ConnectionProxy>() }
    private val accounts by lazy { host.getKoin().get<AccountRepository>() }
    private val preferences by lazy { host.getKoin().get<UserPreferencesRepository>() }
    private val serviceBinding by lazy { ServiceConnectionManager(host.applicationContext) }
    private val tabs = mutableListOf<Button>()
    private val panels = mutableListOf<View>()
    private val focusTargets = mutableListOf<View>()
    private val sections = listOf("Status", "Standorte", "Konto", "VPN-Einstellungen",
        "Split Tunneling", "Erweitert", "Netzwerk", "Diagnose")
    private var selected = 0
    private var activated = false
    private var bound = false
    private var daemonReady = false
    private var returnFromAdvanced = false
    private var awaitingPermission = false
    private var latestTunnel: TunnelState? = null
    private val ink = VpnTvStyle.INK
    private val secondary = VpnTvStyle.MUTED
    private val surface = VpnTvStyle.SURFACE
    private val accent = VpnTvStyle.LIME

    private val accountUi by lazy {
        VpnAccountUi(host, accounts, host.getKoin().get<DeviceRepository>(),
            host.getKoin().get<VoucherRepository>(), host.getKoin().get<NewDeviceRepository>(),
            ::withDaemon, { tabs.getOrNull(2)?.requestFocus() }, true, ink, secondary, surface, accent)
    }
    private val locationUi by lazy {
        VpnLocationUi(host, host.getKoin().get<RelayListRepository>(),
            host.getKoin().get<FilteredRelayListUseCase>(),
            host.getKoin().get<RelayListFilterRepository>(),
            host.getKoin().get<CustomListsRepository>(),
            host.getKoin().get<CustomListsRelayItemUseCase>(),
            host.getKoin().get<RecentsUseCase>(),
            host.getKoin().get<SettingsRepository>(),
            host.getKoin().get<SelectSinglehopUseCase>(),
            host.getKoin().get<ModifyMultihopUseCase>(),
            host.getKoin().get<ProviderToOwnershipsUseCase>(),
            ::withDaemon, { tabs.getOrNull(1)?.requestFocus() },
            true, ink, secondary, surface, accent)
    }
    private val settingsUi by lazy {
        VpnSettingsUi(host, host.getKoin().get<SettingsRepository>(),
            host.getKoin().get<WireguardConstraintsRepository>(),
            host.getKoin().get<AutoStartAndConnectOnBootRepository>(),
            host.getKoin().get<DeleteCustomDnsUseCase>(),
            host.getKoin().get<SystemVpnSettingsAvailableUseCase>(),
            ::withDaemon, { tabs.getOrNull(3)?.requestFocus() },
            true, ink, secondary, surface, accent)
    }
    private val splitUi by lazy {
        VpnSplitTunnelingUi(host, host.getKoin().get<SplitTunnelingRepository>(),
            host.getKoin().get<ApplicationsProvider>(),
            host.getKoin().get<UserPreferencesRepository>(), ::withDaemon,
            { tabs.getOrNull(4)?.requestFocus() }, ink, secondary, surface, accent)
    }
    private val advancedUi by lazy {
        VpnAdvancedUi(host, host.getKoin().get<SettingsRepository>(),
            host.getKoin().get<WireguardConstraintsRepository>(),
            host.getKoin().get<HopSelectionUseCase>(),
            host.getKoin().get<MultihopMigrationRepository>(),
            host.getKoin().get<UserPreferencesRepository>(),
            ::withDaemon, { tabs.getOrNull(5)?.requestFocus() },
            { openAdvancedLocation(RelayListType.Multihop(RelayHopType.ENTRY)) },
            {
                val always = host.getKoin().get<WireguardConstraintsRepository>()
                    .wireguardConstraints.value?.multihop == MultihopMode.ALWAYS
                openAdvancedLocation(if (always) RelayListType.Multihop(RelayHopType.EXIT)
                    else RelayListType.Single)
            },
            { showPage(3); settingsUi.focusQuantum() },
            {
                withDaemon("Neu verbinden") {
                    connection.reconnect().fold(
                        { message("Erneutes Verbinden fehlgeschlagen: " + it) },
                        { message("Verbindungsaufbau angefordert. Mullvad-Status folgt.") })
                }
            }, ink, secondary, surface, accent)
    }
    private val networkUi by lazy {
        VpnNetworkUi(host, host.getKoin().get<SettingsRepository>(),
            host.getKoin().get<RelayListRepository>(),
            host.getKoin().get<WireguardConstraintsRepository>(),
            host.getKoin().get<ApiAccessRepository>(),
            host.getKoin().get<RelayOverridesRepository>(),
            ::withDaemon, { tabs.getOrNull(6)?.requestFocus() },
            ink, secondary, surface, accent)
    }
    private val diagnosticsUi by lazy {
        VpnDiagnosticsUi(host, host.getKoin().get<ProblemReportRepository>(),
            host.getKoin().get<UserPreferencesRepository>(),
            host.getKoin().get<InAppNotificationController>(),
            host.getKoin().get<AppVersionInfoRepository>(),
            host.getKoin().get<ChangelogRepository>(),
            { tabs.getOrNull(7)?.requestFocus() },
            ink, secondary, surface, accent)
    }

    private lateinit var root: LinearLayout
    private lateinit var headline: TextView
    private lateinit var tunnelLine: TextView
    private lateinit var deviceLine: TextView
    private lateinit var locationLine: TextView
    private lateinit var serviceLine: TextView
    private lateinit var notice: TextView
    private lateinit var consent: Button

    private val permission = host.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        awaitingPermission = false
        if (result.resultCode == Activity.RESULT_OK) connect()
        else message("Android-VPN-Freigabe abgelehnt.")
        renderTunnel()
    }

    private fun openAdvancedLocation(type: RelayListType) {
        returnFromAdvanced = true
        locationUi.showFor(type)
        showPage(1)
        locationUi.requestPrimaryFocus()
    }

    private fun message(value: String) {
        if (::notice.isInitialized) notice.text = value
        // Never log account numbers, relay tokens or service responses.
    }
    private fun Throwable.safeMessage() = message ?: javaClass.simpleName
    private fun dp(value: Int) = (value * host.resources.displayMetrics.density).toInt()
    private fun text(value: String, size: Float, bold: Boolean = false) =
        TextView(host).apply {
            text = value
            textSize = size
            setTextColor(if (bold) ink else secondary)
            if (bold) typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(3), 0, dp(3))
        }
    private fun button(label: String, onClick: () -> Unit) = Button(host).apply {
        text = label
        textSize = 15f
        VpnTvStyle.paint(this, false)
        setOnFocusChangeListener { view, selected -> VpnTvStyle.paint(view as Button, selected) }
        setOnClickListener { onClick() }
    }
    private fun column() = LinearLayout(host).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(13), dp(7), dp(13), dp(12))
    }
    private fun scrolled(content: View) = ScrollView(host).apply {
        isFillViewport = true
        isVerticalScrollBarEnabled = false
        addView(content)
    }
    private fun addPage(parent: FrameLayout, content: View, scroll: Boolean) {
        val page = if (scroll) scrolled(content) else content
        parent.addView(page, FrameLayout.LayoutParams(-1, -1))
        panels += page
    }

    private fun build(): View {
        root = LinearLayout(host).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(VpnTvStyle.BACKGROUND)
            setPadding(dp(5), 0, dp(5), dp(5))
        }
        serviceLine = text("MULLVAD VPN · Dienst wird vorbereitet", 18f, true)
        root.addView(serviceLine, LinearLayout.LayoutParams(-1, dp(35)))
        val body = LinearLayout(host).apply { orientation = LinearLayout.HORIZONTAL }
        val rail = LinearLayout(host).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, dp(10), 0)
        }
        val menu = ScrollView(host).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = false
            addView(rail)
        }
        body.addView(menu, LinearLayout.LayoutParams(dp(161), -1))
        val detail = FrameLayout(host).apply {
            setBackgroundColor(VpnTvStyle.RAIL)
        }
        body.addView(detail, LinearLayout.LayoutParams(0, -1, 1f))
        root.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))

        val status = column()
        headline = text("VPN-Status unbekannt", 25f, true)
        tunnelLine = text("Warte auf bestätigten Originalstatus", 16f)
        locationLine = text("Standort und IP: noch keine Daten", 15f)
        deviceLine = text("Konto: wird geladen", 15f)
        for (v in listOf(headline, tunnelLine, locationLine, deviceLine)) status.addView(v)
        val actions = LinearLayout(host).apply { orientation = LinearLayout.HORIZONTAL }
        for ((name, click) in listOf<Pair<String, () -> Unit>>(
            "Verbinden" to { connectWithPermission() },
            "Trennen" to {
                withDaemon("Trennen") {
                    connection.disconnect(DisconnectReason.USER_INITIATED_DISCONNECT_BUTTON).fold(
                        { message("Trennen fehlgeschlagen: " + it) },
                        { message("Trennung angefordert.") })
                }
            },
            "Neu verbinden" to {
                withDaemon("Neu verbinden") {
                    connection.reconnect().fold(
                        { message("Neu verbinden fehlgeschlagen: " + it) },
                        { message("Neuverbindung angefordert.") })
                }
            },
        )) actions.addView(button(name, click), LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            marginEnd = dp(7)
        })
        status.addView(actions)
        consent = button("Datenschutzhinweis akzeptieren und Mullvad starten") {
            host.lifecycleScope.launch {
                try {
                    preferences.setPrivacyDisclosureAccepted()
                    bindOriginalService()
                } catch (error: Exception) {
                    message("Mullvad-Initialisierung fehlgeschlagen: " + error.safeMessage())
                }
            }
        }.apply { visibility = View.GONE }
        status.addView(consent)
        notice = text("", 14f)
        status.addView(notice)
        addPage(detail, status, scroll = true)
        focusTargets += actions.getChildAt(0)

        addPage(detail, locationUi.build(), scroll = false)
        focusTargets += locationUi.primaryFocus
        addPage(detail, column().apply { addView(accountUi.build()) }, scroll = true)
        focusTargets += accountUi.primaryFocus
        addPage(detail, column().apply { addView(settingsUi.build()) }, scroll = true)
        focusTargets += settingsUi.primaryFocus
        addPage(detail, splitUi.build(), scroll = false)
        focusTargets += splitUi.primaryFocus
        addPage(detail, column().apply { addView(advancedUi.build()) }, scroll = true)
        focusTargets += advancedUi.primaryFocus
        addPage(detail, networkUi.build(), scroll = false)
        focusTargets += networkUi.primaryFocus
        addPage(detail, diagnosticsUi.build(), scroll = true)
        focusTargets += diagnosticsUi.primaryFocus

        sections.forEachIndexed { index, name ->
            val tab = button(name) {
                showPage(index)
                focusTargets[index].requestFocus()
            }
            tab.setOnFocusChangeListener { view, focused ->
                VpnTvStyle.paint(view as Button, focused, selected == index)
                if (focused) {
                    if (index != 1 && returnFromAdvanced) {
                        returnFromAdvanced = false
                        locationUi.showFor(RelayListType.Single)
                    }
                    showPage(index)
                }
            }
            tab.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    showPage(index)
                    focusTargets[index].requestFocus()
                    true
                } else false
            }
            tabs += tab
            rail.addView(tab, LinearLayout.LayoutParams(-1, dp(46)).apply {
                bottomMargin = dp(5)
            })
        }
        showPage(0)
        root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) { activate() }
            override fun onViewDetachedFromWindow(v: View) {}
        })
        host.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                if (activated) {
                    if (bound && daemonReady) accountUi.refresh()
                    settingsUi.onResume()
                    splitUi.onResume()
                    diagnosticsUi.onResume()
                }
            }
            override fun onPause(owner: LifecycleOwner) {
                if (activated) splitUi.onPause()
            }
            override fun onDestroy(owner: LifecycleOwner) {
                if (activated) diagnosticsUi.dispose()
                if (bound) serviceBinding.unbind()
                bound = false
            }
        })
        return root
    }

    private fun showPage(next: Int) {
        val changed = selected != next
        selected = next
        panels.forEachIndexed { i, view ->
            view.animate().cancel()
            view.alpha = 1f
            view.translationX = 0f
            view.visibility = if (i == next) View.VISIBLE else View.GONE
            if (changed && i == next && ValueAnimator.areAnimatorsEnabled()) {
                view.alpha = .92f
                view.translationX = dp(9).toFloat()
                view.animate().alpha(1f).translationX(0f).setDuration(145).start()
            }
        }
        tabs.forEachIndexed { i, tab -> VpnTvStyle.paint(tab, tab.isFocused, i == next) }
    }

    private fun activate() {
        if (activated) return
        activated = true
        locationUi.start()
        settingsUi.start()
        splitUi.start()
        advancedUi.start()
        networkUi.start()
        diagnosticsUi.start()
        host.lifecycleScope.launch {
            management.connectionState.collect { state ->
                daemonReady = state == GrpcConnectivityState.Ready
                locationUi.onDaemonReady(daemonReady)
                settingsUi.onDaemonReady(daemonReady)
                splitUi.onDaemonReady(daemonReady)
                advancedUi.onDaemonReady(daemonReady)
                advancedUi.onTunnelState(if (daemonReady) latestTunnel else null)
                networkUi.onDaemonReady(daemonReady)
                diagnosticsUi.onDaemonReady(daemonReady)
                serviceLine.text = "MULLVAD VPN · " + if (daemonReady) "Dienst bereit" else "Dienst: " + state
                renderTunnel()
                if (daemonReady && bound) accountUi.refresh()
            }
        }
        host.lifecycleScope.launch {
            management.tunnelState.collect { state ->
                latestTunnel = state
                advancedUi.onTunnelState(if (daemonReady) state else null)
                renderTunnel()
            }
        }
        host.lifecycleScope.launch {
            management.deviceState.collect { state ->
                deviceLine.text = "Konto: " + when (state) {
                    is DeviceState.LoggedIn -> "Angemeldet · " + state.device.displayName()
                    DeviceState.LoggedOut -> "Nicht angemeldet"
                    DeviceState.Revoked -> "Geräteberechtigung widerrufen"
                }
                accountUi.onDeviceState(state)
                diagnosticsUi.onDeviceState(state)
            }
        }
        host.lifecycleScope.launch {
            accounts.accountData.collect {
                accountUi.onAccountData(it)
                diagnosticsUi.onAccountData(it)
            }
        }
        host.lifecycleScope.launch {
            try {
                if (preferences.preferences().isPrivacyDisclosureAccepted) bindOriginalService()
                else {
                    consent.visibility = View.VISIBLE
                    message("Mullvad benötigt die Bestätigung des Datenschutzhinweises.")
                }
            } catch (error: Exception) {
                message("Mullvad konnte nicht starten: " + error.safeMessage())
            }
        }
    }

    private fun bindOriginalService() {
        if (bound) return
        try {
            serviceBinding.bind()
            bound = true
            consent.visibility = View.GONE
            message("Originaler Mullvad-Dienst wird initialisiert.")
            accountUi.refresh()
        } catch (error: Exception) {
            message("Dienstbindung fehlgeschlagen: " + error.safeMessage())
        }
    }
    private fun withDaemon(action: String, block: suspend () -> Unit) {
        if (!bound) return message("Mullvad ist noch nicht initialisiert.")
        host.lifecycleScope.launch {
            try {
                if (withTimeoutOrNull(20_000L) {
                    management.connectionState.first { it == GrpcConnectivityState.Ready }
                } == null) return@launch message("$action: Dienst antwortet nicht.")
                block()
            } catch (error: Exception) { message("$action fehlgeschlagen: " + error.safeMessage()) }
        }
    }
    private fun connectWithPermission() {
        if (!bound) return message("Mullvad ist noch nicht initialisiert.")
        try {
            val request = VpnService.prepare(host)
            if (request == null) connect()
            else {
                awaitingPermission = true
                renderTunnel()
                permission.launch(request)
            }
        } catch (error: Exception) {
            awaitingPermission = false
            message("Android-VPN-Freigabe konnte nicht geöffnet werden: " + error.safeMessage())
            renderTunnel()
        }
    }
    private fun connect() = withDaemon("Verbinden") {
        connection.connect().fold(
            { message("Verbinden fehlgeschlagen: " + it) },
            { message("Verbindungsaufbau angefordert.") })
    }
    private fun renderTunnel() {
        if (!::headline.isInitialized) return
        val state = latestTunnel
        headline.text = when {
            awaitingPermission -> "Android-VPN-Freigabe"
            !daemonReady -> "Mullvad-Dienst nicht bereit"
            state == null -> "Warte auf VPN-Status"
            state is TunnelState.Connected -> "VPN verbunden"
            state is TunnelState.Connecting -> "VPN verbindet"
            state is TunnelState.Disconnecting &&
                state.actionAfterDisconnect == ActionAfterDisconnect.Reconnect -> "Verbindet erneut"
            state is TunnelState.Disconnecting -> "VPN trennt"
            state is TunnelState.Error -> "VPN-Fehler"
            else -> "VPN getrennt"
        }
        tunnelLine.text = when {
            awaitingPermission -> "Android-Systemfreigabe bestätigen"
            !daemonReady -> "Keine bestätigten Live-Daten vom Mullvad-Daemon"
            state is TunnelState.Error -> "Mullvad meldet: " + state.errorState
            state == null -> "Tunnelstatus wird initialisiert"
            else -> "Originaler Tunnelstatus: " + state.javaClass.simpleName
        }
        val location = if (daemonReady) state?.location() else null
        val place = location?.let { listOfNotNull(it.city, it.country).joinToString(", ") }
        val ip = location?.ipv4?.hostAddress ?: location?.ipv6?.hostAddress
        locationLine.text = if (location != null) "VPN-Ausgang: " +
            listOfNotNull(place, ip, location.hostname).filter { it.isNotBlank() }.joinToString(" · ")
        else "Standort und IP: nicht bestätigt"
    }

    /** A nested VPN page consumes Back first. Returns false at the top-level rail. */
    fun handleBack(): Boolean {
        if (selected == 3 && settingsUi.handleBack()) return true
        if (selected == 6 && networkUi.handleBack()) return true
        if (selected == 1 && returnFromAdvanced) {
            returnFromAdvanced = false
            locationUi.showFor(RelayListType.Single)
            showPage(5)
            advancedUi.requestPrimaryFocus()
            return true
        }
        if (selected != 0) {
            showPage(0)
            tabs[0].requestFocus()
            return true
        }
        return false
    }
}
