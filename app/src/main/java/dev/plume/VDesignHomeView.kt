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
    private val homeElements = mutableListOf<View>()
    // Settings and All Apps were originally root children. Keep them attached to this
    // shell when offscreen so MainActivity.findViewById continues to work on every visit.
    private val parkedPages = FrameLayout(context).apply { visibility = View.GONE }

    private val rightPane = FrameLayout(context).apply {
        setBackgroundColor(0xFF121317.toInt())
        isFocusable = false
        visibility = View.GONE
    }
    private var visibleSection = OVERVIEW
    private var colouredAppIcons = true
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
        titleAvailable = heading("VERFÜGBARE FOLGEN", 1132, 94, 20, 285).apply {
            letterSpacing = .09f
        }
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
        // A single permanent shell: media cards and a reparentable right-hand content pane
        // share this View. Only the compact icon rail stays on top at every navigation level.
        addView(parkedPages, LayoutParams(0, 0))
        mount(rightPane, VDesignCanvas.Box(104, 64, 1418, 784))
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

    /** Refresh immediately after returning from VPN settings; timer covers live Home changes. */
    fun refreshStatus() = refreshHeader()

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

    private fun scale(): Float = VDesignCanvas.scale(width, height).takeIf { it > 0f }
        ?: VDesignCanvas.scale(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
    private fun fontPixels(reference: Int): Float =
        reference * scale() * resources.configuration.fontScale.coerceIn(0.85f, 1.35f)

    private fun heading(label: String, x: Int, y: Int, textPx: Int, textWidth: Int = 365): TextView {
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
        mount(text, VDesignCanvas.Box(x, y, textWidth, 37))
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
        if (view !== rightPane && view !in menu) {
            homeElements += view
            if (visibleSection != OVERVIEW) view.visibility = View.INVISIBLE
        }
        if (width > 0 && height > 0) position(view, box)
    }

    /**
     * One permanent shell: replace the page on the right, never the left rail.
     * Persistent settings/apps Views are parked offscreen rather than detached:
     * the existing launcher's many findViewById-based features still work.
     */
    fun showSection(key: String, content: View? = null) {
        if (key == visibleSection &&
            (key == OVERVIEW || rightPane.getChildAt(0) === content)) return
        visibleSection = key
        val isHome = key == OVERVIEW
        homeElements.forEach { it.visibility = if (isHome) View.VISIBLE else View.INVISIBLE }
        rightPane.animate().cancel()
        rightPane.alpha = 1f
        rightPane.translationX = 0f
        if (isHome) {
            rightPane.visibility = View.GONE
            return
        }
        rightPane.visibility = View.VISIBLE
        if (rightPane.getChildAt(0) !== content) {
            val previous = rightPane.getChildAt(0)
            if (previous != null) {
                rightPane.removeView(previous)
                if (previous.id == R.id.allApps || previous.id == R.id.settingsScrim) {
                    previous.visibility = View.GONE
                    parkedPages.addView(previous, FrameLayout.LayoutParams(-1, -1))
                }
            }
            if (content != null) {
                (content.parent as? ViewGroup)?.removeView(content)
                content.visibility = View.VISIBLE
                rightPane.addView(content, FrameLayout.LayoutParams(-1, -1))
            }
        }
        if (android.animation.ValueAnimator.areAnimatorsEnabled()) {
            rightPane.alpha = .90f
            rightPane.animate().alpha(1f).setDuration(135).start()
        }
    }

    fun selectedSection(): String = visibleSection

    /** Focus the first actionable tile in the right pane; never focus an empty placeholder. */
    fun focusFirstSectionAction(): Boolean {
        fun find(group: View): View? {
            if (group is ViewGroup) {
                for (i in 0 until group.childCount) {
                    val child = group.getChildAt(i)
                    if (child.visibility == View.VISIBLE) find(child)?.let { return it }
                }
            }
            return group.takeIf { it !== rightPane && it.isFocusable && it.isEnabled && it.visibility == View.VISIBLE }
        }
        return find(rightPane)?.requestFocus() == true
    }

    fun focusActiveRail(): Boolean = focusRememberedSidebar()

    fun setColouredAppIcons(enabled: Boolean) {
        if (colouredAppIcons == enabled) return
        colouredAppIcons = enabled
        for (entry in menu) {
            val key = entry.tag as? String ?: continue
            val img = (entry as? FrameLayout)?.getChildAt(0) as? ImageView ?: continue
            val app = key == NUVIO || key == SMARTTUBE || key == SPOTIFY
            if (app && colouredAppIcons) img.clearColorFilter()
            else img.setColorFilter(if (entry.isFocused) 0xFF121317.toInt() else Palette.INK)
        }
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
        val viewport = VDesignCanvas.scale(w, h)
        if (viewport > 0f) {
            val fontScale = resources.configuration.fontScale.coerceIn(.85f, 1.35f)
            fun setType(view: TextView, referencePx: Int) =
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, viewport * fontScale * referencePx)
            setType(titleProgress, 23)
            setType(titleAvailable, 20)
            setType(titleUpcoming, 23)
            setType(availableBadge, 17)
            setType(dateHeader, 18)
            setType(clockHeader, 28)
            setType(vpnHeader, 17)
            cards().forEach { it.updateViewportScale(viewport) }
            for (bar in menu) {
                val mark = (bar as? FrameLayout)?.getChildAt(0)
                mark?.layoutParams = FrameLayout.LayoutParams(
                    (34 * viewport).toInt().coerceAtLeast(1),
                    (34 * viewport).toInt().coerceAtLeast(1), Gravity.CENTER)
            }
        }
    }

    /** Immutable M02 snapshot plus callbacks from the existing shared poster cache and TV launcher. */
    fun bindMedia(loadArtwork: (ImageView, String?) -> Unit,
        sourceIcon: (String) -> Drawable?, onSelect: (NuvioMediaItem) -> Unit) {
        artworkLoader = loadArtwork
        mediaSourceIcon = sourceIcon
        onMediaOpen = onSelect
    }

    /** Reuse twelve reference slots across provider refreshes and local-midnight invalidation. */
    fun bindFeed(feed: NuvioDashboardFeed) {
        val focusedCard = cards().firstOrNull { it.isFocused }
        val focusedId = focusedCard?.media?.stableId
        val focusedSection = focusedCard?.media?.section
        latestFeed = feed
        lastNow = Instant.now()
        lastZone = ZoneId.systemDefault()
        lastLocale = resources.configuration.locales[0] ?: Locale.getDefault()
        titleProgress.text = "WEITERSCHAUEN  ·  ${feed.inProgress.size.toString().padStart(2, '0')}"
        titleAvailable.text = "VERFÜGBARE FOLGEN"
        availableBadge.text = feed.available.size.toString()
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
            card.setUpcomingAccent(index)
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

    fun bindNavigation(
        items: List<Navigation>,
        onOpen: (String) -> Unit,
        onAllApps: () -> Unit,
        onSystemActions: (View) -> Unit,
        onPreview: (String) -> Unit = {},
        onAppLongPress: (String, View) -> Unit = { _, _ -> },
    ) {
        open = onOpen
        allApps = onAllApps
        systemActions = onSystemActions
        val hadFocus = menu.firstOrNull { it.isFocused }?.tag as? String
        if (hadFocus != null) lastSidebarKey = hadFocus
        menu.forEach { removeView(it); anchored.removeAll { pair -> pair.first === it } }
        menu.clear()
        var appIndex = 0
        for (item in items) {
            val appShortcut = item.key == NUVIO || item.key == SMARTTUBE || item.key == SPOTIFY
            val bubble = FrameLayout(context).apply {
                tag = item.key
                isFocusable = true
                isClickable = true
                contentDescription = when (item.key) {
                    OVERVIEW -> "Startseite"
                    ALL_APPS -> "Alle Apps – Vorschau rechts, OK zum Öffnen"
                    SETTINGS -> "Einstellungen – rechts durchsuchen"
                    VPN -> "Mullvad VPN"
                    else -> item.label
                }
            }
            val icon = ImageView(context).apply {
                setImageDrawable(item.icon?.constantState?.newDrawable(resources)?.mutate() ?: item.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            bubble.addView(icon, FrameLayout.LayoutParams((37 * scale()).toInt().coerceAtLeast(1),
                (37 * scale()).toInt().coerceAtLeast(1), Gravity.CENTER))
            fun update(focused: Boolean) {
                if (appShortcut && colouredAppIcons) icon.clearColorFilter()
                else icon.setColorFilter(if (focused) 0xFF121317.toInt() else Palette.INK)
                bubble.background = GradientDrawable().apply {
                    cornerRadius = 35 * scale()
                    setColor(if (focused) Palette.LIME else Color.TRANSPARENT)
                }
                bubble.animate().cancel()
                if (android.animation.ValueAnimator.areAnimatorsEnabled()) {
                    bubble.animate().scaleX(if (focused) 1.03f else 1f)
                        .scaleY(if (focused) 1.03f else 1f).setDuration(145).start()
                } else { bubble.scaleX = 1f; bubble.scaleY = 1f }
            }
            update(false)
            bubble.setOnFocusChangeListener { _, focused ->
                update(focused)
                if (focused) {
                    lastSidebarKey = item.key
                    preferMediaOnReturn = false
                    onPreview(item.key)
                }
            }
            bubble.setOnClickListener { onOpen(item.key) }
            bubble.setOnLongClickListener {
                when (item.key) {
                    SETTINGS -> onSystemActions(bubble)
                    OVERVIEW -> onAllApps()
                    else -> if (appShortcut || item.key == ALL_APPS) onAppLongPress(item.key, bubble) else return@setOnLongClickListener false
                }
                true
            }
            menu += bubble
            val y = when (item.key) {
                OVERVIEW -> 111
                NUVIO, SMARTTUBE, SPOTIFY -> 197 + 83 * appIndex++
                ALL_APPS -> 197 + 83 * appIndex
                VPN -> 527
                SETTINGS -> 608
                else -> 197
            }
            mount(bubble, VDesignCanvas.Box(18, y, 68, 68))
        }
        if (hadFocus != null && isShown) {
            (menu.firstOrNull { it.tag == hadFocus } ?: menu.firstOrNull())?.requestFocus()
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

    /** D-Pad stays spatial on Home; the same rail switches right-side pages on focus. */
    fun handleDpad(focused: View?, keyCode: Int): Boolean {
        if (!isShown || focused == null) return false
        val direction = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> VDesignFocusNavigator.Direction.UP
            KeyEvent.KEYCODE_DPAD_DOWN -> VDesignFocusNavigator.Direction.DOWN
            KeyEvent.KEYCODE_DPAD_LEFT -> VDesignFocusNavigator.Direction.LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT -> VDesignFocusNavigator.Direction.RIGHT
            else -> return false
        }
        val railKey = (focused.tag as? String)?.takeIf { key ->
            menu.any { it === focused && it.tag == key }
        }
        if (railKey != null && visibleSection != OVERVIEW) {
            if (direction == VDesignFocusNavigator.Direction.RIGHT) {
                focusFirstSectionAction()
                return true
            }
            val next = VDesignFocusNavigator.fromSidebar(railKey,
                menu.mapNotNull { it.tag as? String }, direction, null)
            if (next is VDesignFocusNavigator.Target.Sidebar) focusSidebar(next.key)
            return true
        }
        if (visibleSection != OVERVIEW) return false
        val liveCards = focusableCards()
        val cells = liveCards.mapNotNull { card ->
            cardBoxes[card]?.let { box ->
                card.media?.let { VDesignFocusNavigator.Cell(it.section, cardsForSectionSlot(card), box) }
            }
        }
        val target = if (railKey != null) {
            val preferred = rememberedCard()?.let { card ->
                cells.firstOrNull { it.section == card.media?.section && it.slot == cardsForSectionSlot(card) }
            } ?: cells.firstOrNull()
            VDesignFocusNavigator.fromSidebar(railKey, menu.mapNotNull { it.tag as? String },
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
                    if (!card.isFocusable) focusRememberedSidebar()
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
