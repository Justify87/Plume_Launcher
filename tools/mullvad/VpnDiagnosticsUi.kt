package net.mullvad.mullvadvpn.app

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import java.time.ZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mullvad.mullvadvpn.feature.home.impl.connect.notificationbanner.InAppNotificationController
import net.mullvad.mullvadvpn.lib.model.AccountData
import net.mullvad.mullvadvpn.lib.model.DeviceState
import net.mullvad.mullvadvpn.lib.model.InAppNotification
import net.mullvad.mullvadvpn.lib.model.UserReport
import net.mullvad.mullvadvpn.lib.model.VersionInfo
import net.mullvad.mullvadvpn.lib.repository.AppVersionInfoRepository
import net.mullvad.mullvadvpn.lib.repository.ChangelogRepository
import net.mullvad.mullvadvpn.lib.repository.ProblemReportRepository
import net.mullvad.mullvadvpn.lib.repository.SendProblemReportResult
import net.mullvad.mullvadvpn.lib.repository.UserPreferencesRepository

/**
 * TV-only presentation of the original Mullvad report, notification and version repositories.
 * It never collects its own logs, implements analytics, or writes a second diagnostic backend.
 */
internal class VpnDiagnosticsUi(
    private val activity: ComponentActivity,
    private val reports: ProblemReportRepository,
    private val preferences: UserPreferencesRepository,
    private val notifications: InAppNotificationController,
    private val version: AppVersionInfoRepository,
    private val changelog: ChangelogRepository,
    private val backToRail: () -> Unit,
    private val ink: Int,
    private val secondary: Int,
    private val surface: Int,
    private val accent: Int,
) {
    lateinit var primaryFocus: Button
        private set

    private lateinit var backendLine: TextView
    private lateinit var notificationLine: TextView
    private lateinit var systemNotificationLine: TextView
    private lateinit var locationButton: Button
    private lateinit var versionLine: TextView
    private lateinit var expiryLine: TextView
    private lateinit var logsPreview: TextView
    private lateinit var feedback: TextView
    private lateinit var emailInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var includeAccount: CheckBox

    private var showLocation: Boolean? = null
    private var latestDevice: DeviceState? = null
    private var latestAccount: AccountData? = null
    private var daemonReady = false
    private var latestVersionInfo: VersionInfo? = null
    private var logBusy = false
    private var sendBusy = false

    // Registered during Activity.onCreate, before lifecycle STARTED.
    private val exportDocument =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                activity.lifecycleScope.launch {
                    try {
                        val lines = withContext(Dispatchers.IO) { reports.readLogs() }
                        withContext(Dispatchers.IO) {
                            val stream = activity.contentResolver.openOutputStream(uri)
                                ?: error("Datei konnte nicht geöffnet werden")
                            stream.bufferedWriter(Charsets.UTF_8).use { out ->
                                lines.forEach { out.appendLine(it) }
                            }
                        }
                        notice("Original-Mullvad-Protokoll exportiert.")
                    } catch (error: Exception) {
                        notice("Export fehlgeschlagen: " + error.safeMessage())
                    }
                }
            }
        }

    fun build(): LinearLayout = column().apply {
        addView(label("Diagnose und Meldungen", 25f, true))
        backendLine = label("Mullvad-Dienst: wird geprüft", 16f)
        addView(backendLine)
        notificationLine = label("Original-Mullvad-Hinweise: werden geladen", 16f)
        addView(notificationLine)
        expiryLine = label("Konto: wird geladen", 16f)
        addView(expiryLine)

        addView(label("Android-Benachrichtigungen", 20f, true))
        systemNotificationLine = label("Systemberechtigung: wird geprüft", 15f)
        addView(systemNotificationLine)
        locationButton = action("Standort in VPN-Systemmeldung: wird geladen") { toggleLocation() }
        addView(locationButton)
        addView(action("Android-Benachrichtigungseinstellungen") { openNotificationSettings() })
        addView(label("Mullvads Originaldienst verwaltet Verbindungs- und Ablaufmeldungen. " +
            "Auf Android TV sind Benachrichtigungskanäle geräteabhängig.", 13f))

        addView(label("Version und Änderungen", 20f, true))
        versionLine = label("Mullvad-Version: wird geladen", 16f)
        addView(versionLine)
        addView(action("Änderungen dieser Version anzeigen") { showChangelog() })
        addView(action("App-Sprache (gesamte Plume-APK)") { changeLanguage() })

        addView(label("Originalprotokolle", 20f, true))
        primaryFocus = action("Mullvad-Logs sammeln und ansehen") { showLogs() }
        addView(primaryFocus)
        addView(action("Vollständige Mullvad-Logs exportieren") {
            try {
                exportDocument.launch("mullvad-problem-report.txt")
            } catch (error: Exception) {
                // Some TV firmware ships without a document picker or usable SAF provider.
                notice("Exportdialog nicht verfügbar: " + error.safeMessage())
            }
        })
        logsPreview = label("Noch keine Logs gelesen. Es wird nichts automatisch exportiert.", 13f)
        logsPreview.setTextIsSelectable(true)
        addView(logsPreview)

        addView(label("Problem an Mullvad melden", 20f, true))
        addView(label("Der Versand erfolgt ausschließlich über Mullvads Original-Repository.", 14f))
        emailInput = EditText(activity).apply {
            hint = "E-Mail (optional)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setSingleLine(true)
            setTextColor(ink)
            setHintTextColor(secondary)
        }
        addView(emailInput, LinearLayout.LayoutParams(-1, dp(55)))
        descriptionInput = EditText(activity).apply {
            hint = "Problembeschreibung (erforderlich)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            minLines = 3
            maxLines = 6
            setTextColor(ink)
            setHintTextColor(secondary)
            gravity = Gravity.TOP
        }
        addView(descriptionInput, LinearLayout.LayoutParams(-1, dp(130)))
        includeAccount = CheckBox(activity).apply {
            text = "Mullvad-Konto-ID im Bericht einschließen (optional)"
            isChecked = false
            isEnabled = false
            setTextColor(ink)
        }
        addView(includeAccount)
        addView(action("Bericht an Mullvad senden") { confirmReport() })
        feedback = label("Keine Diagnose oder Datenübertragung ohne deine Aktion.", 14f)
        addView(feedback)
    }

    fun start() {
        activity.lifecycleScope.launch {
            preferences.preferencesFlow().collect { prefs ->
                showLocation = prefs.showLocationInSystemNotification
                locationButton.text = "Standort in VPN-Systemmeldung: " +
                    if (prefs.showLocationInSystemNotification) "Ein" else "Aus"
            }
        }
        activity.lifecycleScope.launch {
            notifications.notifications.collect { items ->
                notificationLine.text = if (items.isEmpty()) {
                    "Original-Mullvad-Hinweise: aktuell keine"
                } else {
                    "Original-Mullvad-Hinweise:\n" + items.joinToString("\n") {
                        "• " + it.statusLevel + ": " + describe(it)
                    }
                }
            }
        }
        activity.lifecycleScope.launch {
            version.versionInfo.collect { info ->
                latestVersionInfo = info
                renderVersion()
            }
        }
        refreshSystemNotifications()
    }

    fun onDaemonReady(ready: Boolean) {
        daemonReady = ready
        backendLine.text = if (ready) "Mullvad-Daemon: bereit"
            else "Mullvad-Daemon: nicht erreichbar – keine bestätigten Live-Werte"
        renderVersion()
    }

    private fun renderVersion() {
        if (!::versionLine.isInitialized) return
        val info = latestVersionInfo
        versionLine.text = when {
            info == null -> "Mullvad-Version: wird geladen"
            !daemonReady -> "Mullvad " + info.currentVersion +
                " · Supportstatus unbekannt (Dienst nicht erreichbar)"
            info.isSupported -> "Mullvad " + info.currentVersion + " · vom Dienst unterstützt"
            else -> "Mullvad " + info.currentVersion +
                " · Version laut Mullvad nicht mehr unterstützt"
        }
    }

    fun onDeviceState(state: DeviceState) {
        latestDevice = state
        renderAccount()
    }

    fun onAccountData(data: AccountData?) {
        latestAccount = data
        renderAccount()
    }

    fun onResume() = refreshSystemNotifications()

    fun requestPrimaryFocus() { primaryFocus.requestFocus() }

    fun dispose() {
        // Mirrors upstream ReportProblemViewModel.onCleared(); do not retain report logs.
        reports.deleteLogs()
    }

    private fun renderAccount() {
        val device = latestDevice
        includeAccount.isEnabled = device is DeviceState.LoggedIn &&
            latestAccount?.accountNumber == device.accountNumber
        if (!includeAccount.isEnabled) includeAccount.isChecked = false
        expiryLine.text = when (device) {
            DeviceState.Revoked -> "Mullvad-Gerät widerrufen: im Konto neu anmelden"
            DeviceState.LoggedOut -> "Mullvad-Konto: abgemeldet"
            is DeviceState.LoggedIn -> {
                val expiry = latestAccount?.takeIf { it.accountNumber == device.accountNumber }
                    ?.expiryDate
                when {
                    expiry == null -> "Mullvad-Konto: Ablauf wird geladen"
                    !expiry.isAfter(ZonedDateTime.now()) -> "Mullvad-Konto abgelaufen: $expiry"
                    else -> "Mullvad-Konto gültig bis $expiry"
                }
            }
            null -> "Mullvad-Konto: wird geladen"
        }
    }

    private fun describe(notification: InAppNotification): String = when (notification) {
        is InAppNotification.TunnelStateError -> "Tunnel: " + notification.error.cause
        InAppNotification.TunnelStateBlocked -> "Internet wird durch den Tunnel gesperrt"
        is InAppNotification.AccountExpiry -> {
            if (notification.expiry.isNegative || notification.expiry.isZero) "Konto abgelaufen"
            else "Konto läuft in " + notification.expiry.toDays() + " Tagen ab"
        }
        is InAppNotification.NewDevice -> "Neues Gerät: " + notification.deviceName
        is InAppNotification.UnsupportedVersion -> "Mullvad-Version nicht unterstützt: " +
            notification.versionInfo.currentVersion
        InAppNotification.NewVersionChangelog -> "Neue Mullvad-Versionshinweise"
        InAppNotification.Android16UpgradeWarning -> "Android-16-VPN-Updatehinweis"
        is InAppNotification.MultihopMigration -> "Multihop-Einstellungen prüfen"
        is InAppNotification.MultihopMigrationBlocked -> "Multihop-Konfiguration blockiert"
    }

    private fun toggleLocation() {
        val enabled = showLocation ?: return notice("Benachrichtigungseinstellung wird geladen.")
        activity.lifecycleScope.launch {
            try {
                preferences.setLocationInNotificationEnabled(!enabled)
            } catch (error: Exception) {
                notice("Speichern fehlgeschlagen: " + error.safeMessage())
            }
        }
    }

    private fun refreshSystemNotifications() {
        if (!::systemNotificationLine.isInitialized) return
        systemNotificationLine.text = "Android erlaubt Plume-VPN-Benachrichtigungen: " +
            if (NotificationManagerCompat.from(activity).areNotificationsEnabled()) "Ja" else "Nein"
    }

    private fun openNotificationSettings() {
        try {
            activity.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
            })
        } catch (_: ActivityNotFoundException) {
            notice("Dieses Android TV bietet keine App-Benachrichtigungseinstellungen an.")
        } catch (error: Exception) {
            notice("Systemeinstellungen nicht verfügbar: " + error.safeMessage())
        }
    }

    private fun changeLanguage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return notice("App-spezifische Sprachen benötigen Android 13; " +
                "die System-Sprache gilt auf diesem Android TV.")
        }
        try {
            val languageRepository =
                net.mullvad.mullvadvpn.feature.language.impl.LanguageRepository(activity)
            val locales = languageRepository.getSupportedLocales()
            val labels = arrayOf("Systemsprache (gesamte App)") +
                locales.map { it.getDisplayName(it) }.toTypedArray()
            var chosen = (locales.indexOf(languageRepository.getAppLocale()) + 1)
                .coerceAtLeast(0)
            AlertDialog.Builder(activity)
                .setTitle("App-Sprache – betrifft auch Plume")
                .setSingleChoiceItems(labels, chosen) { _, which -> chosen = which }
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Übernehmen") { _, _ ->
                    languageRepository.setAppLocale(locales.getOrNull(chosen - 1))
                }
                .show()
        } catch (error: Exception) {
            notice("Sprachauswahl nicht verfügbar: " + error.safeMessage())
        }
    }

    private fun showChangelog() {
        try {
            val changes = changelog.getLastVersionChanges()
            val text = if (changes.isEmpty()) "Keine Versionshinweise in diesem Build."
                else changes.joinToString("\n\n") { "• " + it }
            AlertDialog.Builder(activity)
                .setTitle("Mullvad – Versionsänderungen")
                .setMessage(text)
                .setPositiveButton("Schließen") { _, _ ->
                    activity.lifecycleScope.launch {
                        changelog.setDismissNewChangelogNotification()
                    }
                }
                .show()
        } catch (error: Exception) {
            notice("Versionshinweise nicht verfügbar: " + error.safeMessage())
        }
    }

    private fun showLogs() {
        if (logBusy) return
        logBusy = true
        notice("Original-Mullvad-Protokolle werden gelesen.")
        activity.lifecycleScope.launch {
            try {
                val lines = withContext(Dispatchers.IO) { reports.readLogs() }
                // Keep the full original report only in Mullvad's file; limit TV rendering.
                val recent = lines.takeLast(160).joinToString("\n").takeLast(16000)
                logsPreview.text = "Letzte " + minOf(lines.size, 160) + " von " +
                    lines.size + " Logzeilen:\n" + recent
                notice("Original-Mullvad-Logs geladen. Für alle Zeilen Export verwenden.")
            } catch (error: Exception) {
                notice("Logs nicht verfügbar: " + error.safeMessage())
            } finally {
                logBusy = false
            }
        }
    }

    private fun confirmReport() {
        if (sendBusy) return
        val description = descriptionInput.text.toString().trim()
        if (description.isBlank()) return notice("Bitte eine Problembeschreibung eingeben.")
        val email = emailInput.text.toString().trim().takeIf { it.isNotEmpty() }
        AlertDialog.Builder(activity)
            .setTitle("Problembericht versenden?")
            .setMessage(
                "Mullvad erhält den Text und die von Mullvad gesammelten Logs." +
                    if (includeAccount.isChecked) " Deine Konto-ID wird mitgesendet."
                    else " Die Konto-ID wird nicht mitgesendet."
            )
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("An Mullvad senden") { _, _ ->
                sendReport(UserReport(email, description), includeAccount.isChecked)
            }
            .show()
    }

    private fun sendReport(report: UserReport, includeAccountId: Boolean) {
        sendBusy = true
        notice("Bericht wird über Mullvads Originalfunktion übertragen.")
        activity.lifecycleScope.launch {
            try {
                reports.setEmail(report.email ?: "")
                reports.setDescription(report.description)
                when (reports.sendReport(report, includeAccountId)) {
                    SendProblemReportResult.Success -> {
                        reports.setEmail("")
                        reports.setDescription("")
                        emailInput.text.clear()
                        descriptionInput.text.clear()
                        logsPreview.text = "Original-Bericht versendet; lokale Bericht-Logs gelöscht."
                        notice("Bericht an Mullvad gesendet.")
                    }
                    SendProblemReportResult.Error.CollectLog ->
                        notice("Mullvad konnte die Original-Logs nicht sammeln.")
                    SendProblemReportResult.Error.SendReport ->
                        notice("Mullvad konnte den Bericht nicht senden (z. B. offline).")
                }
            } catch (error: Exception) {
                notice("Bericht fehlgeschlagen: " + error.safeMessage())
            } finally {
                sendBusy = false
            }
        }
    }

    private fun notice(message: String) { feedback.text = message }
    private fun Throwable.safeMessage() = message ?: javaClass.simpleName

    private fun column() = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(28), dp(16), dp(28), dp(24))
    }

    private fun label(value: String, size: Float, bold: Boolean = false) =
        TextView(activity).apply {
            text = value
            textSize = size
            setTextColor(if (bold) ink else secondary)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(9), 0, dp(9))
        }

    private fun action(title: String, click: () -> Unit) = Button(activity).apply {
        text = title
        textSize = 16f
        isAllCaps = false
        setTextColor(ink)
        isFocusable = true
        setOnClickListener { click() }
        setOnFocusChangeListener { _, hasFocus ->
            setTextColor(if (hasFocus) Color.WHITE else ink)
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(if (hasFocus) accent else surface)
                setStroke(dp(if (hasFocus) 3 else 1), if (hasFocus) Color.WHITE else secondary)
            }
        }
        background = GradientDrawable().apply {
            cornerRadius = dp(10).toFloat()
            setColor(surface)
            setStroke(dp(1), secondary)
        }
        setOnKeyListener { _, key, event ->
            if (event.action == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_LEFT) {
                backToRail()
                true
            } else false
        }
    }

    private fun dp(value: Int) = (value * activity.resources.displayMetrics.density).toInt()
}
