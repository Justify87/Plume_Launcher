package dev.plume

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Small self-contained right-hand preview for an installed source app.
 * Artwork, text and actions come exclusively from currently published feeds.
 * No second app-specific home page, invented recommendation or background poller.
 */
internal class VDesignAppSurface(
    context: Context,
    title: String,
    icon: Drawable?,
    media: List<MediaItem>,
    nuvio: List<NuvioMediaItem>,
    artwork: (ImageView, String?) -> Unit,
    launchApp: () -> Unit,
    openMedia: (MediaItem) -> Unit,
    openNuvio: (NuvioMediaItem) -> Unit,
) : ScrollView(context) {
    private companion object {
        const val SURFACE = 0xFF22242A
        const val INK = 0xFFF7F7F8.toInt()
        const val SOFT = 0xFFB9BBC2.toInt()
        const val FOCUS = 0xFFE7FF80.toInt()
        const val DARK = 0xFF111217.toInt()
        const val MAX_CARDS = 12
    }
    private val density = resources.displayMetrics.density
    private fun dp(v: Int) = (density * v).toInt().coerceAtLeast(1)
    private fun surface(color: Int, radius: Int = 14) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }
    private fun label(value: String, sp: Float, strong: Boolean = false): TextView =
        TextView(context).apply {
            text = value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
            setTextColor(INK)
            typeface = Typeface.create("sans-serif",
                if (strong) Typeface.BOLD else Typeface.NORMAL)
            gravity = Gravity.CENTER_VERTICAL
            includeFontPadding = false
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

    private fun interactive(view: View, headline: TextView, detail: TextView? = null) {
        view.isFocusable = true
        view.isClickable = true
        view.setOnFocusChangeListener { focusedView, focused ->
            focusedView.background = surface(if (focused) FOCUS else SURFACE)
            headline.setTextColor(if (focused) DARK else INK)
            detail?.setTextColor(if (focused) DARK else SOFT)
            focusedView.animate().cancel()
            if (ValueAnimator.areAnimatorsEnabled()) {
                focusedView.animate()
                    .scaleX(if (focused) 1.012f else 1f)
                    .scaleY(if (focused) 1.012f else 1f)
                    .setDuration(145L).start()
            } else {
                focusedView.scaleX = 1f
                focusedView.scaleY = 1f
            }
        }
    }

    init {
        isFillViewport = true
        isVerticalScrollBarEnabled = false
        clipToPadding = false
        setBackgroundColor(0xFF121317.toInt())
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            setPadding(dp(18), dp(8), dp(18), dp(22))
        }
        addView(column, LayoutParams(-1, -2))
        val heading = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(18))
        }
        icon?.let { drawable ->
            heading.addView(ImageView(context).apply {
                setImageDrawable(drawable.constantState?.newDrawable(resources)?.mutate() ?: drawable)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }, LinearLayout.LayoutParams(dp(54), dp(54)).apply { marginEnd = dp(16) })
        }
        heading.addView(label(title, 28f, true), LinearLayout.LayoutParams(0, -2, 1f))
        val openLabel = label("App öffnen  ›", 16f, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(11), dp(16), dp(11))
        }
        interactive(openLabel, openLabel)
        openLabel.setOnClickListener { launchApp() }
        heading.addView(openLabel)
        column.addView(heading)

        if (media.isEmpty() && nuvio.isEmpty()) {
            column.addView(label(
                "Diese App veröffentlicht derzeit keine Vorschaukacheln. Mit OK öffnest du die App direkt.",
                18f).apply {
                setTextColor(SOFT)
                setPadding(0, dp(18), 0, 0)
            })
        } else {
            val grid = GridLayout(context).apply {
                columnCount = 4
                clipToPadding = false
                clipChildren = false
            }
            column.addView(grid)
            data class Entry(val title: String, val subtitle: String?,
                val uri: String?, val onOpen: () -> Unit)
            val entries = mutableListOf<Entry>()
            for (item in nuvio.take(MAX_CARDS)) {
                entries += Entry(item.title, VDesignCardText.episode(item),
                    VDesignCardText.artwork(item), { openNuvio(item) })
            }
            for (item in media.take(MAX_CARDS - entries.size)) {
                entries += Entry(item.title, item.subtitle, item.imageUri,
                    { openMedia(item) })
            }
            entries.forEachIndexed { index, entry ->
                val tile = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(8), dp(8), dp(8), dp(9))
                    clipChildren = false
                    background = surface(SURFACE)
                    contentDescription = listOfNotNull(entry.title, entry.subtitle)
                        .joinToString(", ")
                }
                val art = ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = surface(0xFF34363B.toInt(), 11)
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(v: View, outline: Outline) {
                            outline.setRoundRect(0, 0, v.width, v.height,
                                dp(11).toFloat())
                        }
                    }
                    clipToOutline = true
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }
                tile.addView(art, LinearLayout.LayoutParams(-1, dp(137)))
                entry.uri?.let { artwork(art, it) }
                val headline = label(entry.title, 17f, true).apply {
                    setPadding(dp(3), dp(8), dp(3), 0)
                }
                tile.addView(headline)
                val detail = entry.subtitle?.takeIf { it.isNotBlank() }?.let {
                    label(it, 13f).apply {
                        setTextColor(SOFT)
                        setPadding(dp(3), dp(3), dp(3), 0)
                    }.also(tile::addView)
                }
                interactive(tile, headline, detail)
                tile.setOnClickListener { entry.onOpen() }
                grid.addView(tile, GridLayout.LayoutParams(
                    GridLayout.spec(index / 4),
                    GridLayout.spec(index % 4, 1f)
                ).apply {
                    width = 0
                    height = dp(217)
                    setMargins(dp(4), 0, dp(9), dp(12))
                })
            }
        }
    }
}
