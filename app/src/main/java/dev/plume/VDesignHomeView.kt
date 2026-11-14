package dev.plume

import android.content.Context
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
    }

    data class Navigation(val key: String, val label: String, val icon: Drawable?)

    private object Palette {
        const val INK = 0xFFF5F1FF.toInt()
        const val SOFT = 0xFFCBC5DF.toInt()
        const val LAVENDER = 0xFFB18AFF.toInt()
        const val EMPTY_EDGE = 0x334D5576
    }

    private val anchored = mutableListOf<Pair<View, VDesignCanvas.Box>>()
    private val menu = mutableListOf<View>()
    private val cardBoxes = mutableMapOf<VDesignCardView, VDesignCanvas.Box>()
    private var lastSidebarKey = OVERVIEW
    private var lastSection: NuvioSection? = null
    private var preferMediaOnReturn = false
    private val lastMediaKey = mutableMapOf<NuvioSection, String>()
    private val titleAvailable: TextView
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
        background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
            intArrayOf(0xEE111321.toInt(), 0xF0121525.toInt(), 0xF21F1A34.toInt()))
        val sidebarShade = View(context).apply {
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(0xF00D101D.toInt(), 0xEE171827.toInt()))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        mount(sidebarShade, VDesignCanvas.Box(0, 0, VDesignCanvas.SIDEBAR_WIDTH, 864))
        mount(View(context).apply { setBackgroundColor(0x55454866) },
            VDesignCanvas.Box(217, 0, 1, 864))

        heading("Weiterschauen", 263, 27, 25, R.drawable.ic_playback)
        titleAvailable = heading("Neue Folgen verfügbar", 263, 357, 23, R.drawable.vdesign_sparkle)
        titleUpcoming = heading("Demnächst", 263, 564, 23, R.drawable.vdesign_calendar)

        card(VDesignCanvas.Box(255, 74, 651, 262), VDesignCardView.Kind.HERO, progressCards)
        card(VDesignCanvas.Box(919, 83, 287, 245), VDesignCardView.Kind.STANDARD, progressCards)
        card(VDesignCanvas.Box(1221, 83, 287, 245), VDesignCardView.Kind.STANDARD, progressCards)
        for ((x, width) in listOf(258 to 402, 679 to 403, 1102 to 407))
            card(VDesignCanvas.Box(x, 403, width, 140), VDesignCardView.Kind.AVAILABLE, availableCards)
        for (y in listOf(612, 727)) for (x in listOf(258, 576, 893, 1210))
            card(VDesignCanvas.Box(x, y, 302, 102), VDesignCardView.Kind.COMPACT, upcomingCards)
    }

    private fun scale(): Float = kotlin.math.min(
        resources.displayMetrics.widthPixels / 1536f, resources.displayMetrics.heightPixels / 864f)
    private fun fontPixels(reference: Int): Float =
        reference * scale() * resources.configuration.fontScale.coerceIn(0.85f, 1.35f)

    private fun heading(label: String, x: Int, y: Int, textPx: Int, icon: Int): TextView {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = false
        }
        val mark = ImageView(context).apply {
            setImageResource(icon)
            setColorFilter(Palette.SOFT)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        row.addView(mark, LinearLayout.LayoutParams(
            (22 * scale()).toInt().coerceAtLeast(1), (22 * scale()).toInt().coerceAtLeast(1)))
        val text = TextView(context).apply {
            this.text = label
            setTextColor(Palette.INK)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontPixels(textPx))
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = false
            maxLines = 1
        }
        row.addView(text, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT).apply {
                marginStart = (11 * scale()).toInt()
            })
        mount(row, VDesignCanvas.Box(x, y, 1190, 37))
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
        titleAvailable.text = "Neue Folgen verfügbar  ·  ${feed.available.size}"
        titleUpcoming.text = "Demnächst  ·  ${feed.upcoming.size}"
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
            if (index >= 0 && index !in upcomingOffset until upcomingOffset + 8)
                upcomingOffset = (index / 4 * 4).coerceAtMost((feed.upcoming.size - 8).coerceAtLeast(0))
        }
        upcomingOffset = upcomingOffset.coerceAtMost((feed.upcoming.size - 8).coerceAtLeast(0))
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
        // Actual reference row tops: 94, 161, 223, 285 and 347 at 1536x864.
        for (item in items) {
            val bar = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((12 * scale()).toInt(), 0, (8 * scale()).toInt(), 0)
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
            }
            bar.addView(mark, LinearLayout.LayoutParams(
                (27 * scale()).toInt().coerceAtLeast(1), (27 * scale()).toInt().coerceAtLeast(1)))
            val label = TextView(context).apply {
                text = item.label
                setTextColor(Palette.SOFT)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, fontPixels(19))
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                gravity = Gravity.CENTER_VERTICAL
                isSingleLine = true
            }
            bar.addView(label, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                marginStart = (12 * scale()).toInt()
            })
            fun style(focused: Boolean) {
                label.setTextColor(if (focused) Color.WHITE else Palette.SOFT)
                bar.background = if (focused) GradientDrawable().apply {
                    cornerRadius = 15 * scale()
                    setColor(0x88505268.toInt())
                    setStroke((1 * scale()).toInt().coerceAtLeast(1), 0xFF77718E.toInt())
                } else GradientDrawable().apply {
                    cornerRadius = 15 * scale()
                    setColor(Color.TRANSPARENT)
                }
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
            // Slot indices preserve ordering if optional apps or VPN are unavailable.
            val y = when (item.key) {
                OVERVIEW -> 94
                NUVIO -> 161
                SMARTTUBE -> 223
                SETTINGS -> 285
                VPN -> 347
                else -> 94
            }
            mount(bar, VDesignCanvas.Box(12, y, 194, 58))
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
            if (index >= 0 && index !in upcomingOffset until upcomingOffset + 8) {
                upcomingOffset = (index / 4 * 4).coerceAtMost(
                    (latestFeed.upcoming.size - 8).coerceAtLeast(0))
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
                (direction == VDesignFocusNavigator.Direction.UP ||
                    direction == VDesignFocusNavigator.Direction.DOWN)) {
                val delta = if (direction == VDesignFocusNavigator.Direction.DOWN) 1 else -1
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
