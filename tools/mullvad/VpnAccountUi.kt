package net.mullvad.mullvadvpn.app

import android.content.ActivityNotFoundException
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.collect
import net.mullvad.mullvadvpn.lib.common.util.createAccountUri
import net.mullvad.mullvadvpn.lib.model.AccountData
import net.mullvad.mullvadvpn.lib.model.AccountNumber
import net.mullvad.mullvadvpn.lib.model.CreateAccountError
import net.mullvad.mullvadvpn.lib.model.DeleteAccountError
import net.mullvad.mullvadvpn.lib.model.Device
import net.mullvad.mullvadvpn.lib.model.DeviceState
import net.mullvad.mullvadvpn.lib.model.LoginAccountError
import net.mullvad.mullvadvpn.lib.model.ParseVoucherCodeError
import net.mullvad.mullvadvpn.lib.model.RedeemVoucherError
import net.mullvad.mullvadvpn.lib.model.VoucherCode
import net.mullvad.mullvadvpn.lib.repository.AccountRepository
import net.mullvad.mullvadvpn.lib.repository.DeviceRepository
import net.mullvad.mullvadvpn.lib.repository.NewDeviceRepository
import net.mullvad.mullvadvpn.lib.repository.VoucherRepository

/**
 * Native TV account screen on Mullvad's original account/device/voucher repositories.
 * No parallel account storage, HTTP client or account cache is written by Plume.
 */
internal class VpnAccountUi(
    private val activity: ComponentActivity,
    private val accounts: AccountRepository,
    private val devices: DeviceRepository,
    private val vouchers: VoucherRepository,
    private val newDevices: NewDeviceRepository,
    private val withDaemon: (String, suspend () -> Unit) -> Unit,
    private val focusRail: () -> Unit,
    private val dark: Boolean,
    private val ink: Int,
    private val secondary: Int,
    private val surface: Int,
    private val accent: Int,
) {
    private var deviceState: DeviceState = DeviceState.LoggedOut
    private var accountData: AccountData? = null
    private var limitAccount: AccountNumber? = null
    private var listedAccount: AccountNumber? = null
    private var listedDevices: List<Device> = emptyList()
    private var loadingDevices = false
    private var deviceError: String? = null

    private lateinit var loginGroup: LinearLayout
    private lateinit var loggedInGroup: LinearLayout
    private lateinit var devicesGroup: LinearLayout
    private lateinit var accountStatus: TextView
    private lateinit var accountNumberText: TextView
    private lateinit var expiryText: TextView
    private lateinit var accountFeedback: TextView
    private lateinit var deviceHeading: TextView
    private lateinit var deviceRows: LinearLayout
    private lateinit var loginInput: EditText
    private lateinit var voucherInput: EditText
    private lateinit var refreshButton: Button

    fun build(): LinearLayout = column().apply {
        addView(label("Konto", 25f, true))
        accountStatus = label("Warte auf Mullvad-Kontostatus", 17f)
        addView(accountStatus)
        accountFeedback = label("Kontoaktionen verwenden Mullvads Originaldienst.", 15f)
        addView(accountFeedback)

        loginGroup = column().apply {
            addView(label("Mit bestehender 16-stelliger Mullvad-Kontonummer anmelden", 16f))
            loginInput = edit("Kontonummer (16 Ziffern)", true)
            loginInput.filters = arrayOf(InputFilter.LengthFilter(16))
            addView(loginInput, LinearLayout.LayoutParams(dp(420), dp(58)))
            addView(action("Anmelden") { login() })
            addView(action("Neues Mullvad-Konto erstellen") {
                AlertDialog.Builder(activity)
                    .setTitle("Neues Konto erstellen?")
                    .setMessage("Mullvad erstellt eine neue Kontonummer. Notiere sie, bevor du fortfährst. Ein bestehendes Konto wird dadurch nicht übernommen.")
                    .setNegativeButton("Abbrechen", null)
                    .setPositiveButton("Erstellen") { _, _ -> createAccount() }
                    .show()
            })
        }
        addView(loginGroup)

        loggedInGroup = column().apply {
            accountNumberText = label("", 18f, true)
            expiryText = label("", 17f)
            addView(accountNumberText)
            addView(expiryText)
            refreshButton = action("Konto und Geräte aktualisieren") { refresh() }
            addView(refreshButton)
            addView(action("Laufzeit verlängern (Mullvad-Webseite)") { openAccountWebsite() })
            addView(label("Gutschein einlösen", 17f, true))
            voucherInput = edit("Gutscheincode", false)
            addView(voucherInput, LinearLayout.LayoutParams(dp(420), dp(58)))
            addView(action("Gutschein prüfen und einlösen") { redeemVoucher() })
            addView(action("Abmelden") { confirmLogout() })
            addView(action("Konto endgültig löschen") { confirmDeleteAccount() })
        }
        addView(loggedInGroup)

        devicesGroup = column().apply {
            deviceHeading = label("Geräte", 18f, true)
            addView(deviceHeading)
            addView(action("Geräteliste aktualisieren") {
                val number = (deviceState as? DeviceState.LoggedIn)?.accountNumber ?: limitAccount
                if (number != null) loadDevices(number)
            })
            deviceRows = column()
            addView(deviceRows)
            addView(label(
                "Umbenennen wird von den Konto-/Geräte-Repositories des festgelegten Android-Upstreams nicht angeboten.",
                13f,
            ))
        }
        addView(devicesGroup)
        render()
    }

    val primaryFocus: View get() = loginInput

    fun requestPrimaryFocus() {
        if (deviceState is DeviceState.LoggedIn) refreshButton.requestFocus()
        else loginInput.requestFocus()
    }

    fun onDeviceState(state: DeviceState) {
        val previous = (deviceState as? DeviceState.LoggedIn)?.accountNumber
        val current = (state as? DeviceState.LoggedIn)?.accountNumber
        deviceState = state
        if (previous != current) {
            accountData = null
            listedAccount = null
            listedDevices = emptyList()
            deviceError = null
            limitAccount = null
        }
        render()
        if (current != null && previous != current) refresh()
    }

    fun onAccountData(data: AccountData?) {
        accountData = data
        render()
    }

    /** Also called on return from a browser; never uses a locally persisted session. */
    fun refresh() {
        val number = (deviceState as? DeviceState.LoggedIn)?.accountNumber ?: return
        withDaemon("Kontodaten aktualisieren") {
            accounts.refreshAccountData()
            loadDevicesFromDaemon(number)
        }
    }

    private fun login() {
        val number = loginInput.text.toString().filterNot(Char::isWhitespace)
        if (!number.matches(Regex("\\d{16}"))) {
            notice("Bitte genau 16 Ziffern eingeben.")
            return
        }
        withDaemon("Anmelden") {
            accounts.login(AccountNumber(number)).fold(
                { error ->
                    when (error) {
                        is LoginAccountError.MaxDevicesReached -> {
                            limitAccount = error.accountNumber
                            notice("Gerätelimit erreicht. Entferne ein altes Gerät und melde dich erneut an.")
                            render()
                            loadDevicesFromDaemon(error.accountNumber)
                        }
                        else -> notice(loginError(error))
                    }
                },
                {
                    newDevices.newDeviceCreated()
                    loginInput.text.clear()
                    notice("Anmeldung bestätigt. Warte auf den Kontostatus des Mullvad-Dienstes.")
                },
            )
        }
    }

    private fun createAccount() = withDaemon("Konto erstellen") {
        accounts.createAccount().fold(
            { notice(createError(it)) },
            { number ->
                // The account number is deliberately never written to preferences or logs.
                AlertDialog.Builder(activity)
                    .setTitle("Neue Mullvad-Kontonummer")
                    .setMessage("Diese Nummer jetzt sicher notieren:\n\n" +
                        number.value.chunked(4).joinToString(" ") +
                        "\n\nOhne sie kannst du dich später nicht wieder anmelden.")
                    .setCancelable(false)
                    .setPositiveButton("Zur Anmeldung übernehmen") { _, _ ->
                        loginInput.setText(number.value)
                        loginInput.requestFocus()
                    }
                    .show()
            },
        )
    }

    private fun confirmLogout() {
        AlertDialog.Builder(activity)
            .setTitle("Bei Mullvad abmelden?")
            .setMessage("Dieses Gerät wird abgemeldet. Eine laufende VPN-Verbindung kann dadurch beendet werden.")
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Abmelden") { _, _ ->
                withDaemon("Abmelden") {
                    accounts.logout().fold(
                        { notice("Abmeldung fehlgeschlagen. Bitte Verbindung prüfen und erneut versuchen.") },
                        { notice("Abmeldung bestätigt. Kontostatus wird vom Mullvad-Dienst aktualisiert.") },
                    )
                }
            }
            .show()
    }

    private fun confirmDeleteAccount() {
        val current = deviceState as? DeviceState.LoggedIn ?: return
        val confirmInput = edit("16-stellige Kontonummer bestätigen", true).apply {
            filters = arrayOf(InputFilter.LengthFilter(16))
        }
        val expiry = accountData?.takeIf { it.accountNumber == current.accountNumber }?.expiryDate
        val remainingTime = if (expiry != null && expiry.isAfter(ZonedDateTime.now())) {
            "\nDas Konto besitzt noch bezahlte Restlaufzeit."
        } else ""
        AlertDialog.Builder(activity)
            .setTitle("Mullvad-Konto unwiderruflich löschen?")
            .setMessage("Alle Geräte und verbleibende Kontolaufzeit gehen verloren." +
                remainingTime + "\nZum Bestätigen die vollständige Kontonummer eingeben.")
            .setView(confirmInput)
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Endgültig löschen") { _, _ ->
                if (confirmInput.text.toString() != current.accountNumber.value ||
                    (deviceState as? DeviceState.LoggedIn)?.accountNumber != current.accountNumber
                ) {
                    notice("Kontonummer stimmt nicht überein. Keine Löschung durchgeführt.")
                } else {
                    withDaemon("Konto löschen") {
                        accounts.deleteAccount().fold(
                            { error ->
                                val explanation = when (error) {
                                    DeleteAccountError.AccountNumberDoesNotMatch -> "Kontonummer stimmt nicht überein."
                                    is DeleteAccountError.UnableToReachApi -> "Mullvad-API nicht erreichbar."
                                    is DeleteAccountError.Unknown -> "Unbekannter API-Fehler."
                                }
                                notice("Konto nicht gelöscht: " + explanation)
                            },
                            { notice("Mullvad hat die Kontolöschung bestätigt.") },
                        )
                    }
                }
            }
            .show()
    }

    private fun redeemVoucher() {
        if (deviceState !is DeviceState.LoggedIn) return
        val voucher = VoucherCode.fromString(voucherInput.text.toString())
        voucher.fold(
            { error ->
                notice(when (error) {
                    is ParseVoucherCodeError.AllDigit -> "Das sieht wie eine Kontonummer aus, kein Gutschein."
                    is ParseVoucherCodeError.TooShort -> "Gutscheincode zu kurz."
                })
            },
            { code ->
                withDaemon("Gutschein einlösen") {
                    vouchers.submitVoucher(code).fold(
                        { notice(voucherError(it)) },
                        {
                            voucherInput.text.clear()
                            notice("Gutschein erfolgreich eingelöst. Laufzeit wird aktualisiert.")
                            accounts.refreshAccountData()
                        },
                    )
                }
            },
        )
    }

    private fun openAccountWebsite() {
        if (deviceState !is DeviceState.LoggedIn) return
        withDaemon("Mullvad-Webseite") {
            val uri = createAccountUri("https://mullvad.net/account", accounts.getWebsiteAuthToken())
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: ActivityNotFoundException) {
                notice("Kein Browser installiert. Öffne https://mullvad.net/account auf einem anderen Gerät.")
            }
        }
    }

    private fun loadDevices(number: AccountNumber) {
        withDaemon("Geräte laden") { loadDevicesFromDaemon(number) }
    }

    private suspend fun loadDevicesFromDaemon(number: AccountNumber) {
        loadingDevices = true
        deviceError = null
        listedAccount = number
        renderDevices()
        devices.deviceList(number).fold(
            {
                if (listedAccount == number) {
                    deviceError = "Geräteliste derzeit nicht erreichbar. Bitte erneut versuchen."
                }
            },
            {
                if (listedAccount == number) listedDevices = it.sortedBy { device -> device.creationDate }
            },
        )
        if (listedAccount == number) {
            loadingDevices = false
            renderDevices()
        }
    }

    private fun confirmRemoveDevice(device: Device) {
        val number = (deviceState as? DeviceState.LoggedIn)?.accountNumber ?: limitAccount ?: return
        if (listedAccount != number || device !in listedDevices) return
        val ownDevice = (deviceState as? DeviceState.LoggedIn)?.device?.id == device.id
        AlertDialog.Builder(activity)
            .setTitle("Gerät entfernen?")
            .setMessage(device.displayName() +
                if (ownDevice) "\nDas ist dieses Gerät. Die VPN-Anmeldung wird dadurch ungültig."
                else "\nDas entfernte Gerät verliert seine Mullvad-Berechtigung.")
            .setNegativeButton("Abbrechen", null)
            .setPositiveButton("Gerät entfernen") { _, _ ->
                withDaemon("Gerät entfernen") {
                    devices.removeDevice(number, device.id).fold(
                        { notice("Gerät konnte nicht entfernt werden. Bitte Verbindung prüfen.") },
                        {
                            notice("Mullvad hat das Gerät entfernt.")
                            loadDevicesFromDaemon(number)
                        },
                    )
                }
            }
            .show()
    }

    private fun render() {
        if (!::loginGroup.isInitialized) return
        val loggedIn = deviceState as? DeviceState.LoggedIn
        loginGroup.visibility = if (loggedIn == null) View.VISIBLE else View.GONE
        loggedInGroup.visibility = if (loggedIn != null) View.VISIBLE else View.GONE
        devicesGroup.visibility =
            if (loggedIn != null || limitAccount != null) View.VISIBLE else View.GONE
        accountStatus.text = when (deviceState) {
            is DeviceState.LoggedIn -> "Angemeldet bei Mullvad"
            DeviceState.LoggedOut -> if (limitAccount != null) "Anmeldung: Gerätelimit erreicht"
                else "Nicht angemeldet"
            DeviceState.Revoked -> "Geräteberechtigung widerrufen – erneut anmelden"
        }
        if (loggedIn != null) {
            accountNumberText.text = "Kontonummer: " +
                loggedIn.accountNumber.value.chunked(4).joinToString(" ")
            val expiry = accountData?.takeIf { it.accountNumber == loggedIn.accountNumber }?.expiryDate
            expiryText.text = when {
                expiry == null -> "Restlaufzeit: wird vom Mullvad-Konto geladen"
                !expiry.isAfter(ZonedDateTime.now()) -> "Konto abgelaufen am " + date(expiry)
                else -> "Gültig bis " + date(expiry)
            }
        }
        renderDevices()
    }

    private fun renderDevices() {
        if (!::deviceRows.isInitialized) return
        val number = (deviceState as? DeviceState.LoggedIn)?.accountNumber ?: limitAccount
        deviceHeading.text = if (limitAccount != null && deviceState !is DeviceState.LoggedIn) {
            "Gerätelimit – entferne ein Gerät für die Anmeldung"
        } else "Geräte dieses Mullvad-Kontos"
        deviceRows.removeAllViews()
        if (number == null || listedAccount != number) {
            deviceRows.addView(label("Geräteliste noch nicht geladen.", 15f))
            return
        }
        if (loadingDevices) {
            deviceRows.addView(label("Geräte werden von Mullvad geladen …", 15f))
            return
        }
        if (deviceError != null) {
            deviceRows.addView(label(deviceError ?: "", 15f))
            return
        }
        if (listedDevices.isEmpty()) {
            deviceRows.addView(label("Keine Geräte gemeldet.", 15f))
            return
        }
        listedDevices.forEach { device ->
            val own = (deviceState as? DeviceState.LoggedIn)?.device?.id == device.id
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(label(device.displayName() + if (own) " · Dieses Gerät" else "", 16f),
                    LinearLayout.LayoutParams(0, dp(58), 1f))
                addView(action("Entfernen") { confirmRemoveDevice(device) },
                    LinearLayout.LayoutParams(dp(170), dp(54)))
            }
            deviceRows.addView(row)
        }
    }

    private fun loginError(error: LoginAccountError): String = when (error) {
        LoginAccountError.InvalidAccount -> "Kontonummer nicht gefunden."
        is LoginAccountError.InvalidInput -> "Ungültige Kontonummer."
        is LoginAccountError.MaxDevicesReached -> "Gerätelimit erreicht."
        LoginAccountError.TooManyAttempts -> "Zu viele Versuche. Bitte später erneut anmelden."
        LoginAccountError.Timeout -> "Zeitüberschreitung bei der Mullvad-Anmeldung."
        LoginAccountError.ApiUnreachable -> "Mullvad-API nicht erreichbar. Internetverbindung prüfen."
        is LoginAccountError.Unknown -> "Anmeldung fehlgeschlagen (unbekannter Fehler)."
    }

    private fun createError(error: CreateAccountError): String = when (error) {
        CreateAccountError.TooManyAttempts -> "Zu viele Kontoerstellungen. Bitte später erneut versuchen."
        CreateAccountError.ApiUnreachable -> "Mullvad-API nicht erreichbar."
        CreateAccountError.TimeOut -> "Zeitüberschreitung beim Erstellen des Kontos."
        is CreateAccountError.Unknown -> "Konto konnte nicht erstellt werden."
    }

    private fun voucherError(error: RedeemVoucherError): String = when (error) {
        RedeemVoucherError.InvalidVoucher -> "Ungültiger Gutschein."
        RedeemVoucherError.VoucherAlreadyUsed -> "Dieser Gutschein wurde bereits eingelöst."
        RedeemVoucherError.TooShortVoucher -> "Gutscheincode zu kurz."
        RedeemVoucherError.EnteredAccountNumber -> "Das ist eine Kontonummer, kein Gutschein."
        RedeemVoucherError.ApiUnreachable -> "Mullvad-API nicht erreichbar."
        is RedeemVoucherError.Unknown -> "Gutschein konnte nicht eingelöst werden."
    }

    private fun date(value: ZonedDateTime): String =
        value.withZoneSameInstant(ZoneId.systemDefault()).format(
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        )

    private fun notice(value: String) {
        accountFeedback.text = value
        // No user credential, voucher, token or backend Throwable is logged.
    }

    private fun column() = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(0), dp(8), dp(0), dp(9))
    }

    private fun label(value: String, size: Float, strong: Boolean = false) =
        TextView(activity).apply {
            text = value
            textSize = size
            setTextColor(if (strong) ink else secondary)
            gravity = Gravity.CENTER_VERTICAL
            if (strong) setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(7), 0, dp(7))
        }

    private fun edit(hintText: String, numeric: Boolean) = EditText(activity).apply {
        hint = hintText
        inputType = if (numeric) {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        }
        isSingleLine = true
        textSize = 18f
        setTextColor(ink)
        setHintTextColor(secondary)
        setBackgroundColor(if (dark) 0xFF314257.toInt() else Color.WHITE)
        setPadding(dp(14), dp(7), dp(14), dp(7))
        setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                focusRail()
                true
            } else false
        }
    }

    private fun action(title: String, onClick: () -> Unit) = Button(activity).apply {
        text = title
        isAllCaps = false
        textSize = 16f
        isFocusable = true
        setTextColor(ink)
        setPadding(dp(10), dp(5), dp(10), dp(5))
        val repaint: (Boolean) -> Unit = { focused ->
            setTextColor(if (focused) Color.WHITE else ink)
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(if (focused) accent else surface)
                setStroke(dp(if (focused) 3 else 1),
                    if (focused) Color.WHITE else secondary)
            }
        }
        repaint(false)
        setOnFocusChangeListener { _, focused -> repaint(focused) }
        setOnClickListener { onClick() }
        setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                focusRail()
                true
            } else false
        }
    }

    private fun dp(value: Int) =
        (value * activity.resources.displayMetrics.density).toInt()
}
