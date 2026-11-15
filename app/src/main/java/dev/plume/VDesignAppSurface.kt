package dev.plume

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.util.TypedValue

/**
 * A real app preview inside the launcher shell, never an app-specific fake home screen.
 * Shows only published media from the existing Nuvio/Android TV/SmartTube feeds.
 */
internal class VDesignAppSurface(
    context: Context,
    private val title: String,
    icon: Drawable?,
    media: List<MediaItem>,
    nuvio: List<NuvioMediaItem>,
    private val artwork: (ImageView, String?) -> Unit,
    private val launchApp: () -> Unit,
    private val openMedia: (MediaItem) -> Unit,
    private val openNuvio: (NuvioMediaItem) -> Unit,
) : ScrollView(context) {
    private val density = resources.displayMetrics.density
    private fun dp(n: Int) = (n * density).toInt()
    private fun shape(color: Int, r: Int = 18) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(r).toFloat()
    }
    private fun text(value: String, sp: Float, bold: Boolean = false) = TextView(context).apply {
        this.text = value
        setTextColor(0xFFF7F7F8.toInt())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        gravity = Gravity.CENTER_VERTICAL
        includeFontPadding = false
        maxLines = 2
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    private fun focus(v: View) {
        v.isFocusable = true
        v.isClickable = true
        v.setOnFocusChangeListener { view, focused ->
            view.background = shape(if (focused) 0xFFE7FF80.toInt() else 0xFF22242A.toInt())
            view.animate().cancel()
            if (ValueAnimator.areAnimatorsEnabled()) {
                view.animate().scaleX(if (focused) 1.012f else 1f)
                    .scaleY(if (focused) 1.012f else 1f).setDuration(145).start()
            } else { view.scaleX = 1f; view.scaleY = 1f }
        }
    }
    init {
        isFillViewport = true
        isVerticalScrollBarEnabled = false
        clipToPadding = false
        setBackgroundColor(0xFF121317.toInt())
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(5), dp(14), dp(26))
            clipChildren = false
        }
        addView(column)
        val header = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, dp(20))
        }
        icon?.let {
            header.addView(ImageView(context).apply {
                setImageDrawable(it.constantState?.newDrawable(resources)?.mutate() ?: it)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }, LinearLayout.LayoutParams(dp(56), dp(56)).apply { marginEnd = dp(18) })
        }
        header.addView(text(title, 29f, true), LinearLayout.LayoutParams(0, -2, 1f))
        val launch = text("App öffnen  ›", 17f, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(17), dp(13), dp(17), dp(13))
            background = shape(0xFF25272C.toInt())
            setOnClickListener { launchApp() }
        }
        focus(launch)
        launch.setOnFocusChangeListener { view, selected ->
            view.background = shape(if (selected) 0xFFE7FF80.toInt() else 0xFF25272C.toInt())
            launch.setTextColor(if (selected) 0xFF111217.toInt() else 0xFFF7F7F8.toInt())
        }
        header.addView(launch)
        column.addView(header)
        if (media.isEmpty() && nuvio.isEmpty()) {
            column.addView(text("Diese App veröffentlicht derzeit keine Vorschaukacheln. Mit OK kannst du sie direkt öffnen.", 19f).apply {
                setTextColor(0xFFB9BBC2.toInt())
                setPadding(0, dp(24), 0, 0)
            })
            return@init
        }
        val grid = GridLayout(context).apply {
            columnCount = 4
            useDefaultMargins = false
            clipChildren = false
            clipToPadding = false
        }
        column.addView(grid)
        val entries = mutableListOf<Triple<String, String?, Pair<String?, () -> Unit>>>()
        for (item in nuvio.take(16)) {
            entries += Triple(item.title, VDesignCardText.episode(item),
                VDesignCardText.artwork(item) to { openNuvio(item) })
        }
        for (item in media.take(16 - entries.size)) {
            entries += Triple(item.title, item.subtitle, item.imageUri to { openMedia(item) })
        }
        entries.forEachIndexed { index, (name, subtitle, image) ->
            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(8), dp(8), dp(8), dp(10))
                clipChildren = false
                background = shape(0xFF22242A.toInt())
                contentDescription = listOfNotNull(name, subtitle).joinToString(", ")
            }
            focus(card)
            card.setOnClickListener { image.second() }
            val poster = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = shape(0xFF34363B.toInt(), 12)
                clipToOutline = true
                outlineProvider = object : android.view.ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: android.graphics.Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, dp(12).toFloat())
                    }
                }
            }
            card.addView(poster, LinearLayout.LayoutParams(-1, dp(137)))
            image.first?.let { artwork(poster, it) }
            card.addView(text(name, 17f, true).apply { setPadding(dp(3), dp(9), dp(3), 0) })
            subtitle?.takeIf { it.isNotBlank() }?.let {
                card.addView(text(it, 13f).apply {
                    setTextColor(0xFFB9BBC2.toInt())
                    setPadding(dp(3), dp(3), dp(3), 0)
                })
            }
            grid.addView(card, GridLayout.LayoutParams(GridLayout.spec(index / 4),
                GridLayout.spec(index % 4, 1f)).apply {
                width = 0
                height = dp(218)
                setMargins(dp(5), 0, dp(9), dp(14))
            })
        }
    }
}
