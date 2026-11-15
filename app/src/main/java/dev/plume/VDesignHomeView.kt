package dev.plume

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.Locale

/** Reference-anchored sole productive V-Design home host; no Classic/Overview layout switch. */
internal class VDesignHomeView(context: Context) : FrameLayout(context) {
    companion object {
        const val OVERVIEW = "vdesign:overview"
        const val NUVIO = "vdesign:nuvio"
        const val SMARTTUBE = "vdesign:smarttube"
        const val SETTINGS = "vdesign:settings"
        const val VPN = "vdesign:vpn"
        const val SPOTIFY = "vdesign:spotify"
        const val ALL_APPS = "vdesign:allapps"
    }

    data class Navigation(val key: String, val label: String, val icon: Drawable?)

    private object Palette {
        const val INK = 0xFFF7F7F8.toInt()
        const val SOFT = 0xFFC6C7CB.toInt()
        const val LIME = 0xFFE7FF80.toInt()
        const val LAVENDER = 0xFFDCB7F7.toInt()
        const val INACTIVE = 0xFF2B2D33.toInt()
    }

    private val anchored = mutableListOf<Pair<View, VDesignCanvas.Box>>()
    private val menu = mutableListOf<View>()
    private val cardBoxes = mutableMapOf<VDesignCardView, VDesignCanvas.Box>()
    private var lastSidebarKey = OVERVIEW
    private var lastSection: NuvioSection? = null
    private var preferMediaOnReturn = false
    private val lastMediaKey = mutableMapOf<NuvioSection, String>()
    private val titleProgress: TextView
    private val titleAvailable: TextView
    private val availableBadge: TextView
    private val dateHeader: TextView
    private val clockHeader: TextView
    private val vpnHeader: TextView
    private val titleUpcoming: TextView
    private var open: (String) -> Unit = {}
    private var allApps: () -> Unit = {}
    private var systemActions: (View) -> Unit = {}
    private var onMediaOpen: (NuvioMediaItem) -> Unit = {}
    private var artworkLoader: (ImageView, String?) -> Unit = { _, _ -> }
    private var mediaSourceIcon: (String) -> Drawable? = { null }
    private val progressCards = mutableListOf<VDesignCardView>()
    private val availableCards = mutableListOf<VDesignCardView>()
    private val upcomingCards = mutableListOf<VDesignCardView>()
    private var latestFeed = NuvioDashboardFeed.EMPTY
    private var lastNow: Instant = Instant.EPOCH
    private var lastZone: ZoneId = ZoneId.systemDefault()
    private var lastLocale: Locale = Locale.getDefault()
    private var upcomingOffset = 0

    init {
        isFocusable = false
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(0xFF121317.toInt())
        // The small icon rail has no permanent labels or decorative divider.
        titleProgress = heading("WEITERSCHAUEN", 131, 94, 23)
        titleAvailable = heading("NEUE FOLGEN", 1132, 94, 23)
        titleUpcoming = heading("DEMNÄCHST", 133, 547, 23)
        mount(View(context).apply { setBackgroundColor(0xFF45464B.toInt()) },
            VDesignCanvas.Box(1104, 94, 1, 413))
        availableBadge = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontPixels(17))
            setTextColor(0xFF121317.toInt())
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = 23 * scale()
                setColor(Palette.LAVENDER)
            }
        }
        mount(availableBadge, VDesignCanvas.Box(1424, 94, 87, 37))
        dateHeader = header("Datum", 18)
        clockHeader = header("00:00", 28).apply {
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        vpnHeader = header("VPN prüfen", 17).apply {
            gravity = Gravity.CENTER
            setTextColor(0xFF101216.toInt())
            background = GradientDrawable().apply {
                cornerRadius = 22 * scale()
                setColor(Palette.LIME)
            }
        }
        mount(dateHeader, VDesignCanvas.Box(1020, 16, 152, 35))
        mount(View(context).apply { setBackgroundColor(0xFF494A4F.toInt()) },
            VDesignCanvas.Box(1181, 19, 1, 29))
        mount(vpnHeader, VDesignCanvas.Box(1209, 17, 198, 37))
        mount(clockHeader, VDesignCanvas.Box(1429, 11, 87, 46))

        card(VDesignCanvas.Box(108, 145, 655, 365), VDesignCardView.Kind.HERO, progressCards)
        card(VDesignCanvas.Box(781, 142, 303, 170), VDesignCardView.Kind.STANDARD, progressCards)
        card(VDesignCanvas.Box(781, 330, 303, 177), VDesignCardView.Kind.STANDARD, progressCards)
        for (y in listOf(147, 266, 386))
            card(VDesignCanvas.Box(1132, y, 380, 103), VDesignCardView.Kind.AVAILABLE, availableCards)
        for ((x, width) in listOf(
            133 to 228, 379 to 214, 611 to 212,
            842 to 213, 1074 to 211, 1306 to 205
        )) card(VDesignCanvas.Box(x, 605, width, 218), VDesignCardView.Kind.COMPACT, upcomingCards)
        refreshHeader()
    }

    private fun header(value: String, size: Int) = TextView(context).apply {
        text = value
        setTextSize(TypedValue.COMPLEX_UNIT_PX, fontPixels(size))
        setTextColor(Palette.INK)
        gravity = Gravity.CENTER_VERTICAL
        includeFontPadding = false
        isFocusable = false
        maxLines = 1
    }

    private val headerTicker = object : Runnable {
        override fun run() {
            refreshHeader()
            if (isAttachedToWindow) postDelayed(this, 30_000L)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        removeCallbacks(headerTicker)
        headerTicker.run()
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(headerTicker)
        super.onDetachedFromWindow()
    }

    private fun refreshHeader() {
        val now = ZonedDateTime.now()
        dateHeader.text = now.format(DateTimeFormatter.ofPattern("EEE, d. MMM yyyy", Locale.GERMAN))
        clockHeader.text = now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.GERMAN))
        // Active VPN transport is verifiable. The mere presence of the Mullvad Activity is not.
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val active = connectivity?.getNetworkCapabilities(connectivity.activeNetwork)
        val vpn = active?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        vpnHeader.text = if (vpn) "●  VPN aktiv" else "●  VPN prüfen"
        vpnHeader.background = GradientDrawable().apply {
            cornerRadius = 22 * scale()
            setColor(if (vpn) Palette.LIME else Palette.INACTIVE)
        }
        vpnHeader.setTextColor(if (vpn) 0xFF121317.toInt() else Palette.SOFT)
    }

    private fun scale(): Float = kotlin.math.min(
        resources.displayMetrics.widthPixels / 1536f, resources.displayMetrics.heightPixels / 864f)
    private fun fontPixels(reference: Int): Float =
        reference * scale() * resources.configuration.fontScale.coerceIn(0.85f, 1.35f)

    private fun heading(label: String, x: Int, y: Int, textPx: Int): TextView {
        val text = TextView(context).apply {
            this.text = label
            setTextColor(Palette.INK)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontPixels(textPx))
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            letterSpacing = .19f
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = false
            maxLines = 1
        }
        mount(text, VDesignCanvas.Box(x, y, 365, 37))
        return text
    }

    private fun card(box: VDesignCanvas.Box, kind: VDesignCardView.Kind,
        into: MutableList<VDesignCardView>) {
        val view = VDesignCardView(context, kind,
            loadArtwork = { image, uri -> artworkLoader(image, uri) },
            sourceMark = { pkg -> mediaSourceIcon(pkg) })
        view.onSelect = { onMediaOpen(it) }
        view.onFocused = { card ->
            val item = card.media
            if (item != null) {
                lastSection = item.section
                lastMediaKey[item.section] = item.stableId
                preferMediaOnReturn = true
            }
        }
        cardBoxes[view] = box
        mount(view, box)
        into += view
    }

    private fun mount(view: View, box: VDesignCanvas.Box) {
        addView(view, LayoutParams(1, 1))
        anchored += view to box
        if (width > 0 && height > 0) position(view, box)
    }

    private fun position(view: View, box: VDesignCanvas.Box) {
        val p = VDesignCanvas.place(width, height, box)
        view.layoutParams = LayoutParams(p.width, p.height).apply {
            leftMargin = p.x
            topMargin = p.y
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        for ((view, box) in anchored) position(view, box)
    }

    /** Immutable M02 snapshot plus callbacks from the existing shared poster cache and TV launcher. */
    fun bindMedia(loadArtwork: (ImageView, String?) -> Unit,
        sourceIcon: (String) -> Drawable?, onSelect: (NuvioMediaItem) -> Unit) {
        artworkLoader = loadArtwork
        mediaSourceIcon = sourceIcon
        onMediaOpen = onSelect
    }

    /** Reuse the 14 slots across provider refreshes and local-midnight invalidation. */
    fun bindFeed(feed: NuvioDashboardFeed) {
        val focusedCard = cards().firstOrNull { it.isFocused }
        val focusedId = focusedCard?.media?.stableId
        val focusedSection = focusedCard?.media?.section
        latestFeed = feed
        lastNow = Instant.now()
        lastZone = ZoneId.systemDefault()
        lastLocale = resources.configuration.locales[0] ?: Locale.getDefault()
        titleProgress.text = "WEITERSCHAUEN  ·  ${feed.inProgress.size.toString().padStart(2, '0')}"
        titleAvailable.text = "NEUE FOLGEN"
        availableBadge.text = "${feed.available.size} NEU"
        titleUpcoming.text = "DEMNÄCHST"
        progressCards.forEachIndexed { index, card ->
            card.bind(VDesignCardSlots.media(feed, NuvioSection.IN_PROGRESS, index),
                lastNow, lastZone, lastLocale)
            card.showEmpty(if (index == 0 && feed.inProgress.isEmpty()) "Keine Einträge" else null)
        }
        availableCards.forEachIndexed { index, card ->
            card.bind(VDesignCardSlots.media(feed, NuvioSection.AVAILABLE, index),
                lastNow, lastZone, lastLocale)
            card.showEmpty(if (index == 0 && feed.available.isEmpty()) "Keine Einträge" else null)
        }
        // Retain the focused upcoming identity even if provider ordering changed.
        if (focusedId != null) {
            val index = feed.upcoming.indexOfFirst { it.stableId == focusedId }
            if (index >= 0 && index !in upcomingOffset until upcomingOffset + 6)
                upcomingOffset = (index / 6 * 6).coerceAtMost((feed.upcoming.size - 6).coerceAtLeast(0))
        }
        upcomingOffset = upcomingOffset.coerceAtMost((feed.upcoming.size - 6).coerceAtLeast(0))
        bindUpcoming()
        if (focusedId != null && isShown) {
            val restored = cards().firstOrNull {
                it.media?.stableId == focusedId && it.media?.section == focusedSection
            } ?: cards().firstOrNull { it.media?.section == focusedSection && it.isFocusable }
                ?: cards().firstOrNull { it.isFocusable }
            if (restored == null) focusRememberedSidebar() else restored.requestFocus()
        }
    }

    private fun bindUpcoming() {
        upcomingCards.forEachIndexed { index, card ->
            card.bind(VDesignCardSlots.media(latestFeed, NuvioSection.UPCOMING,
                index, upcomingOffset), lastNow, lastZone, lastLocale)
            card.showEmpty(if (index == 0 && latestFeed.upcoming.isEmpty()) "Keine Einträge" else null)
        }
        upcomingCards.firstOrNull { it.isFocused }?.media?.let {
            lastMediaKey[NuvioSection.UPCOMING] = it.stableId
            lastSection = NuvioSection.UPCOMING
        }
    }

    fun artworkReady(uri: String, bitmap: Bitmap) {
        // Avoid allocating a temporary concatenated card list for every decoded poster.
        progressCards.forEach { it.artworkReady(uri, bitmap) }
        availableCards.forEach { it.artworkReady(uri, bitmap) }
        upcomingCards.forEach { it.artworkReady(uri, bitmap) }
    }

    fun retryMissingArtwork() {
        progressCards.forEach { it.retryMissingArtwork() }
        availableCards.forEach { it.retryMissingArtwork() }
        upcomingCards.forEach { it.retryMissingArtwork() }
    }

    fun bindNavigation(items: List<Navigation>, onOpen: (String) -> Unit, onAllApps: () -> Unit,
        onSystemActions: (View) -> Unit) {
        open = onOpen
        allApps = onAllApps
        systemActions = onSystemActions
        val focusedKey = menu.firstOrNull { it.isFocused }?.tag as? String
        if (focusedKey != null) lastSidebarKey = focusedKey
        for (view in menu) {
            removeView(view)
            anchored.removeAll { it.first === view }
        }
        menu.clear()
        for (item in items) {
            val bar = FrameLayout(context).apply {
                tag = item.key
                contentDescription = when (item.key) {
                    OVERVIEW -> item.label + ". Langes OK: Alle Apps"
                    SETTINGS -> item.label + ". Langes OK: System, HDMI und Power"
                    else -> item.label
                }
                isClickable = true
                isFocusable = true
            }
            val mark = ImageView(context).apply {
                setImageDrawable(item.icon?.constantState?.newDrawable(resources)?.mutate() ?: item.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                setColorFilter(Color.WHITE)
            }
            bar.addView(mark, FrameLayout.LayoutParams((34 * scale()).toInt().coerceAtLeast(1),
                (34 * scale()).toInt().coerceAtLeast(1), Gravity.CENTER))
            fun style(focused: Boolean) {
                mark.setColorFilter(if (focused) 0xFF121317.toInt() else Palette.INK)
                bar.background = GradientDrawable().apply {
                    cornerRadius = 40 * scale()
                    setColor(if (focused) Palette.LIME else Color.TRANSPARENT)
                }
                bar.animate().cancel()
                bar.animate().scaleX(if (focused) 1.04f else 1f)
                    .scaleY(if (focused) 1.04f else 1f).setDuration(145).start()
            }
            style(false)
            bar.setOnFocusChangeListener { _, focused ->
                style(focused)
                if (focused) {
                    lastSidebarKey = item.key
                    preferMediaOnReturn = false
                }
            }
            bar.setOnClickListener { open(item.key) }
            if (item.key == OVERVIEW) bar.setOnLongClickListener { allApps(); true }
            if (item.key == SETTINGS) bar.setOnLongClickListener { systemActions(bar); true }
            menu += bar
            val y = when (item.key) {
                OVERVIEW -> 111
                NUVIO -> 197
                SMARTTUBE -> 280
                SPOTIFY -> 364
                ALL_APPS -> 447
                VPN -> 527
                SETTINGS -> 608
                else -> 197
            }
            mount(bar, VDesignCanvas.Box(18, y, 68, 68))
        }
        if (focusedKey != null && isShown) {
            (menu.firstOrNull { it.tag == focusedKey } ?: menu.firstOrNull())?.requestFocus()
        }
    }

    private fun cards(): List<VDesignCardView> = progressCards + availableCards + upcomingCards
    private fun focusableCards(): List<VDesignCardView> =
        cards().filter { it.media != null && it.isFocusable && it.isAttachedToWindow && it.isShown }

    private fun focusSidebar(key: String): Boolean {
        val target = menu.firstOrNull { it.tag == key && it.isFocusable && it.isAttachedToWindow && it.isShown }
            ?: menu.firstOrNull { it.isFocusable && it.isAttachedToWindow && it.isShown }
        return target?.requestFocus() == true
    }

    fun focusRememberedSidebar(): Boolean = focusSidebar(lastSidebarKey)
    fun focusOverview(): Boolean = focusSidebar(OVERVIEW)
    fun focusVpn(): Boolean = focusSidebar(VPN)

    private fun rememberedCard(): VDesignCardView? {
        val section = lastSection
        val id = section?.let { lastMediaKey[it] }
        if (section == NuvioSection.UPCOMING && id != null) {
            val index = latestFeed.upcoming.indexOfFirst { it.stableId == id }
            if (index >= 0 && index !in upcomingOffset until upcomingOffset + 6) {
                upcomingOffset = (index / 6 * 6).coerceAtMost(
                    (latestFeed.upcoming.size - 6).coerceAtLeast(0))
                bindUpcoming()
            }
        }
        val live = focusableCards()
        return live.firstOrNull { it.media?.section == section && it.media?.stableId == id }
            ?: live.firstOrNull { it.media?.section == section }
            ?: live.firstOrNull()
    }

    /** Never persist a View reference; restore only real, attached, currently bound cells. */
    fun restoreFocus(): Boolean {
        if (!isShown) return false
        if (preferMediaOnReturn && rememberedCard()?.requestFocus() == true) return true
        return focusRememberedSidebar() || rememberedCard()?.requestFocus() == true
    }

    /** Called before legacy homeNav: all visible V-Design D-pad moves are explicit. */
    fun handleDpad(focused: View?, keyCode: Int): Boolean {
        if (!isShown || focused == null) return false
        val direction = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> VDesignFocusNavigator.Direction.UP
            KeyEvent.KEYCODE_DPAD_DOWN -> VDesignFocusNavigator.Direction.DOWN
            KeyEvent.KEYCODE_DPAD_LEFT -> VDesignFocusNavigator.Direction.LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT -> VDesignFocusNavigator.Direction.RIGHT
            else -> return false
        }
        val liveCards = focusableCards()
        val cells = liveCards.mapNotNull { card ->
            cardBoxes[card]?.let { box ->
                card.media?.let { VDesignFocusNavigator.Cell(it.section, cardsForSectionSlot(card), box) }
            }
        }
        val menuKey = (focused.tag as? String)?.takeIf { key ->
            menu.any { it === focused && it.tag == key }
        }
        val target = if (menuKey != null) {
            val preferred = rememberedCard()?.let { card ->
                cells.firstOrNull { it.section == card.media?.section && it.slot == cardsForSectionSlot(card) }
            } ?: cells.firstOrNull()
            VDesignFocusNavigator.fromSidebar(menuKey, menu.mapNotNull { it.tag as? String },
                direction, preferred)
        } else {
            val card = focused as? VDesignCardView ?: return false
            val current = cells.firstOrNull {
                it.section == card.media?.section && it.slot == cardsForSectionSlot(card)
            } ?: return focusRememberedSidebar()
            if (current.section == NuvioSection.UPCOMING &&
                (direction == VDesignFocusNavigator.Direction.LEFT ||
                    direction == VDesignFocusNavigator.Direction.RIGHT)) {
                val delta = if (direction == VDesignFocusNavigator.Direction.RIGHT) 1 else -1
                val offset = VDesignCardSlots.nextUpcomingOffset(
                    latestFeed.upcoming.size, upcomingOffset, current.slot, delta)
                if (offset != upcomingOffset) {
                    upcomingOffset = offset
                    bindUpcoming()
                    if (!card.isFocusable) {
                        if (focusableCards().firstOrNull()?.requestFocus() != true) focusRememberedSidebar()
                    }
                    return true
                }
            }
            VDesignFocusNavigator.next(current, cells, direction, lastSidebarKey)
        }
        when (target) {
            VDesignFocusNavigator.Target.Stay -> {}
            is VDesignFocusNavigator.Target.Sidebar -> focusSidebar(target.key)
            is VDesignFocusNavigator.Target.Card -> liveCards.firstOrNull {
                it.media?.section == target.section && cardsForSectionSlot(it) == target.slot
            }?.requestFocus()
        }
        return true
    }

    private fun cardsForSectionSlot(card: VDesignCardView): Int = when {
        card in progressCards -> progressCards.indexOf(card)
        card in availableCards -> availableCards.indexOf(card)
        else -> upcomingCards.indexOf(card)
    }
}
