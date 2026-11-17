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
import java.lang.ref.WeakReference

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

    data class Navigation(val key: String, val label: String, val icon: Drawable?, val appIcon: Boolean = false)

    private object Palette {
        const val INK = 0xFFF7F7F8.toInt()
        const val SOFT = 0xFFC6C7CB.toInt()
        const val LIME = 0xFFE7FF80.toInt()
        const val LAVENDER = 0xFFDCB7F7.toInt()
        const val INACTIVE = 0xFF2B2D33.toInt()
    }

    private val anchored = mutableListOf<Pair<View, VDesignCanvas.Box>>()
    private val menu = mutableListOf<View>()
    private var renderedSidebarSlots = emptyList<VDesignSidebarLayout.Slot>()
    private val homeContent = mutableListOf<View>()
    private val workspacePanels = linkedMapOf<String, View>()
    private var navigation = LauncherWorkspaceNavigation.State()
    private fun destination(key: String): LauncherWorkspaceNavigation.Destination = when (key) {
        NUVIO -> LauncherWorkspaceNavigation.Destination.NUVIO
        SMARTTUBE -> LauncherWorkspaceNavigation.Destination.SMARTTUBE
        SPOTIFY -> LauncherWorkspaceNavigation.Destination.SPOTIFY
        ALL_APPS -> LauncherWorkspaceNavigation.Destination.ALL_APPS
        SETTINGS -> LauncherWorkspaceNavigation.Destination.SETTINGS
        VPN -> LauncherWorkspaceNavigation.Destination.VPN
        else -> LauncherWorkspaceNavigation.Destination.HOME
    }
    private val currentWorkspace: String get() = when (navigation.destination) {
        LauncherWorkspaceNavigation.Destination.HOME -> OVERVIEW
        LauncherWorkspaceNavigation.Destination.NUVIO -> NUVIO
        LauncherWorkspaceNavigation.Destination.SMARTTUBE -> SMARTTUBE
        LauncherWorkspaceNavigation.Destination.SPOTIFY -> SPOTIFY
        LauncherWorkspaceNavigation.Destination.ALL_APPS -> ALL_APPS
        LauncherWorkspaceNavigation.Destination.SETTINGS -> SETTINGS
        LauncherWorkspaceNavigation.Destination.VPN -> VPN
    }
    private var preview: (String) -> Unit = {}
    private var lastContentFocus: WeakReference<View>? = null
    private var lastContentDestination: String? = null
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
        refreshHeader()
        homeContent += (0 until childCount).map { getChildAt(it) }
    }

    fun installWorkspacePane(key: String, content: View) {
        if (key == OVERVIEW) return
        workspacePanels.remove(key)?.let { old ->
            removeView(old)
            anchored.removeAll { it.first === old }
        }
        (content.parent as? ViewGroup)?.removeView(content)
        content.visibility = View.GONE
        workspacePanels[key] = content
        mount(content, VDesignCanvas.Box(103, 0, 1433, 864))
        menu.forEach { bringChildToFront(it) }
        if (currentWorkspace == key) showWorkspace(key, force = true)
    }

    fun showWorkspace(key: String, force: Boolean = false) {
        val alreadyVisible = !force && key == currentWorkspace &&
            (key == OVERVIEW || workspacePanels[key]?.visibility == View.VISIBLE)
        navigation = LauncherWorkspaceNavigation.focusRail(navigation, destination(key))
        if (alreadyVisible) return
        homeContent.forEach { it.visibility = if (key == OVERVIEW) View.VISIBLE else View.GONE }
        workspacePanels.forEach { (destination, view) ->
            val selected = destination == key
            view.animate().cancel()
            view.alpha = 1f
            view.translationX = 0f
            view.visibility = if (selected) View.VISIBLE else View.GONE
            if (selected && android.animation.ValueAnimator.areAnimatorsEnabled()) {
                view.alpha = 0f
                view.translationX = 10f * scale()
                view.animate().alpha(1f).translationX(0f).setDuration(165L).start()
            }
        }
    }

    fun selectedWorkspace(): String = currentWorkspace

    private fun containsChild(root: View, child: View?): Boolean {
        var node = child
        while (node != null) {
            if (node === root) return true
            node = node.parent as? View
        }
        return false
    }

    fun isContentFocused(focused: View?): Boolean =
        focused != null && workspacePanels[currentWorkspace]?.let { containsChild(it, focused) } == true

    fun isRailFocused(focused: View?): Boolean = focused != null && menu.any { it === focused }

    fun focusWorkspaceContent(): Boolean {
        if (currentWorkspace == OVERVIEW) return rememberedCard()?.requestFocus() == true
        val content = workspacePanels[currentWorkspace] ?: return false
        fun findFirst(v: View): View? {
            if (!v.isShown) return null
            if (v is ViewGroup) for (i in 0 until v.childCount) {
                findFirst(v.getChildAt(i))?.let { return it }
            }
            return if (v.isFocusable && v.isEnabled) v else null
        }
        val moved = findFirst(content)?.requestFocus() == true
        if (moved) navigation = LauncherWorkspaceNavigation.right(navigation, true)
        return moved
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

    fun refreshStatus() = refreshHeader()

    private fun refreshHeader() {
        val now = ZonedDateTime.now()
        dateHeader.text = now.format(DateTimeFormatter.ofPattern("EEE, d. MMM yyyy", Locale.GERMAN))
        clockHeader.text = now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.GERMAN))
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
        progressCards.forEach { it.artworkReady(uri, bitmap) }
        availableCards.forEach { it.artworkReady(uri, bitmap) }
        upcomingCards.forEach { it.artworkReady(uri, bitmap) }
    }

    fun retryMissingArtwork() {
        progressCards.forEach { it.retryMissingArtwork() }
        availableCards.forEach { it.retryMissingArtwork() }
        upcomingCards.forEach { it.retryMissingArtwork() }
    }

    private fun sidebarDescription(item: Navigation): String = when (item.key) {
        OVERVIEW -> item.label + ". Langes OK: Alle Apps"
        SETTINGS -> item.label + ". Langes OK: System, HDMI und Power"
        else -> item.label
    }

    private fun sidebarIcon(icon: Drawable?): Drawable? =
        icon?.constantState?.newDrawable(resources)?.mutate() ?: icon

    fun bindNavigation(items: List<Navigation>, onOpen: (String) -> Unit,
        onPreview: (String) -> Unit = {}, showFullColorAppIcons: Boolean = true,
        onAllApps: () -> Unit, onSystemActions: (View) -> Unit) {
        open = onOpen
        preview = onPreview
        allApps = onAllApps
        systemActions = onSystemActions
        val previousFocus = menu.firstOrNull { it.isFocused }?.tag as? String
        if (previousFocus != null) lastSidebarKey = previousFocus
        val slots = VDesignSidebarLayout.slots(
            items.map { it.key to (it.appIcon && showFullColorAppIcons) })
        if (slots == renderedSidebarSlots && menu.size == items.size &&
            menu.all { it.parent === this }) {
            // Refresh app icons and accessibility labels without destroying focused rail views.
            items.forEachIndexed { index, item ->
                val bar = menu[index] as FrameLayout
                bar.contentDescription = sidebarDescription(item)
                (bar.getChildAt(0) as ImageView).setImageDrawable(sidebarIcon(item.icon))
            }
            return
        }
        for (old in menu) {
            removeView(old)
            anchored.removeAll { it.first === old }
        }
        menu.clear()
        for ((item, slot) in items.zip(slots)) {
            val bar = FrameLayout(context).apply {
                tag = item.key
                contentDescription = sidebarDescription(item)
                isFocusable = true
                isClickable = true
            }
            val mark = ImageView(context).apply {
                setImageDrawable(sidebarIcon(item.icon))
                scaleType = ImageView.ScaleType.FIT_CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            bar.addView(mark, FrameLayout.LayoutParams(
                (36 * scale()).toInt().coerceAtLeast(1),
                (36 * scale()).toInt().coerceAtLeast(1), Gravity.CENTER))
            val originalColors = slot.fullColorIcon
            fun style(focused: Boolean) {
                if (originalColors) mark.clearColorFilter()
                else mark.setColorFilter(if (focused) 0xFF121317.toInt() else Palette.INK)
                bar.background = GradientDrawable().apply {
                    cornerRadius = 18f * scale()
                    setColor(if (focused && !originalColors) Palette.LIME
                        else if (focused) 0xFF282B2D.toInt() else Color.TRANSPARENT)
                    if (focused && originalColors)
                        setStroke((2 * scale()).toInt().coerceAtLeast(1), Palette.LIME)
                }
                bar.animate().cancel()
                if (android.animation.ValueAnimator.areAnimatorsEnabled()) {
                    bar.animate().scaleX(if (focused) 1.045f else 1f)
                        .scaleY(if (focused) 1.045f else 1f).setDuration(150L).start()
                } else {
                    bar.scaleX = 1f
                    bar.scaleY = 1f
                }
            }
            style(false)
            bar.setOnFocusChangeListener { _, focused ->
                style(focused)
                if (focused) {
                    lastSidebarKey = item.key
                    preferMediaOnReturn = false
                    showWorkspace(item.key)
                    preview(item.key)
                }
            }
            bar.setOnClickListener { open(item.key) }
            if (item.key == OVERVIEW) bar.setOnLongClickListener { allApps(); true }
            if (item.key == SETTINGS) bar.setOnLongClickListener { systemActions(bar); true }
            menu += bar
            mount(bar, VDesignCanvas.Box(17, slot.y, 66, 66))
        }
        renderedSidebarSlots = slots
        menu.forEach { bringChildToFront(it) }
        if (previousFocus != null && isShown)
            (menu.firstOrNull { it.tag == previousFocus } ?: menu.firstOrNull())?.requestFocus()
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
    fun focusAllApps(): Boolean = focusSidebar(ALL_APPS)
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

    /**
     * Preserve an existing in-pane focus target when another Activity opens.
     * Weak references cannot keep detached app tiles/settings rows alive after a rebind.
     */
    fun rememberWorkspaceFocus(view: View?) {
        when {
            isContentFocused(view) -> {
                lastContentFocus = WeakReference(view)
                lastContentDestination = currentWorkspace
            }
            isRailFocused(view) -> {
                lastContentFocus = null
                lastContentDestination = null
            }
        }
    }

    fun restoreFocus(): Boolean {
        if (!isShown) return false
        if (currentWorkspace != OVERVIEW) {
            val target = lastContentFocus?.get()
            if (lastContentDestination == currentWorkspace && target != null &&
                target.isAttachedToWindow && target.isShown && target.isFocusable &&
                target.requestFocus()) return true
            return focusRememberedSidebar()
        }
        if (preferMediaOnReturn && rememberedCard()?.requestFocus() == true) return true
        return focusRememberedSidebar() || rememberedCard()?.requestFocus() == true
    }

    fun handleDpad(focused: View?, keyCode: Int): Boolean {
        if (!isShown || focused == null) return false
        if (isContentFocused(focused)) {
            if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT) return false
            val next = focused.focusSearch(View.FOCUS_LEFT)
            return if (next != null && next !== focused && isContentFocused(next))
                false else focusRememberedSidebar()
        }
        if (isRailFocused(focused) && currentWorkspace != OVERVIEW &&
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) return focusWorkspaceContent()
        val direction = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> VDesignFocusNavigator.Direction.UP
            KeyEvent.KEYCODE_DPAD_DOWN -> VDesignFocusNavigator.Direction.DOWN
            KeyEvent.KEYCODE_DPAD_LEFT -> VDesignFocusNavigator.Direction.LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT -> VDesignFocusNavigator.Direction.RIGHT
            else -> return false
        }
        val liveCards = if (currentWorkspace == OVERVIEW) focusableCards() else emptyList()
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
