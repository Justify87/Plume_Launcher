package dev.plume

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * M03 single-screen host. The old XML hosts remain mounted but invisible through M06.
 * M04 owns real artwork, focusable media cards and verified Nuvio deep links: all M03
 * media cells are deliberately inert geometry, NEVER synthetic show records.
 */
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
    private val titleAvailable: TextView
    private val titleUpcoming: TextView
    private var open: (String) -> Unit = {}
    private var allApps: () -> Unit = {}

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

        // M03: 1 focused-sized hero + 2 standard; 3 equal wides; 4 x 2 compact.
        // No titles, 'Neu', countdown or fake artwork until M04 binds real provider items.
        cell(VDesignCanvas.Box(255, 74, 651, 262), hero = true)
        cell(VDesignCanvas.Box(919, 83, 287, 245))
        cell(VDesignCanvas.Box(1221, 83, 287, 245))
        for ((x, width) in listOf(258 to 402, 679 to 403, 1102 to 407))
            cell(VDesignCanvas.Box(x, 403, width, 140))
        for (y in listOf(612, 727)) for (x in listOf(258, 576, 893, 1210))
            cell(VDesignCanvas.Box(x, y, 302, 102), compact = true)
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

    private fun cell(box: VDesignCanvas.Box, hero: Boolean = false, compact: Boolean = false) {
        val placeholder = View(context).apply {
            tag = if (hero) "vdesign:hero-slot" else "vdesign:media-slot"
            isFocusable = false
            isClickable = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                if (hero) intArrayOf(0x9B292B42.toInt(), 0xB01A1B30.toInt())
                else intArrayOf(0x8C303148.toInt(), 0xB0181C30.toInt())).apply {
                cornerRadius = (if (compact) 15 else 19) * scale()
                setStroke((1 * scale()).toInt().coerceAtLeast(1), Palette.EMPTY_EDGE)
            }
        }
        mount(placeholder, box)
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

    /** Full provider counts from M02, not the 3/8 visible slots. */
    fun bindFeed(feed: NuvioDashboardFeed) {
        titleAvailable.text = "Neue Folgen verfügbar  ·  ${feed.available.size}"
        titleUpcoming.text = "Demnächst  ·  ${feed.upcoming.size}"
    }

    fun bindNavigation(items: List<Navigation>, onOpen: (String) -> Unit, onAllApps: () -> Unit) {
        open = onOpen
        allApps = onAllApps
        val focusedKey = menu.firstOrNull { it.isFocused }?.tag
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
                contentDescription = item.label
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
            bar.setOnFocusChangeListener { _, focused -> style(focused) }
            bar.setOnClickListener { open(item.key) }
            if (item.key == OVERVIEW) bar.setOnLongClickListener { allApps(); true }
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
        if (focusedKey != null) menu.firstOrNull { it.tag == focusedKey }?.requestFocus()
    }

    fun focusOverview() = menu.firstOrNull { it.tag == OVERVIEW }?.requestFocus() == true
    fun focusVpn() = menu.firstOrNull { it.tag == VPN }?.requestFocus() == true
}
