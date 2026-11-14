package dev.plume

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Drawable
import android.graphics.Outline
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * M04 reference card. One fixed Android View per visible slot; rebinding never creates a new
 * bitmap or shifts the grid. Full-bleed art, static gradient footer, and a stroke-only focus
 * foreground (no blur, scale or moving layout).
 */
internal class VDesignCardView(
    context: Context,
    val kind: Kind,
    private val loadArtwork: (ImageView, String?) -> Unit,
    private val sourceMark: (String) -> Drawable?,
) : FrameLayout(context) {
    enum class Kind { HERO, STANDARD, AVAILABLE, COMPACT }
    private object Colors {
        const val TEXT = 0xFFF9F7FF.toInt()
        const val DIM = 0xFFE0DDEB.toInt()
        const val VIOLET = 0xFFB18AFF.toInt()
    }

    private fun scale() = min(resources.displayMetrics.widthPixels / 1536f,
        resources.displayMetrics.heightPixels / 864f).coerceAtLeast(0.2f)
    private fun px(v: Int) = (v * scale()).roundToInt().coerceAtLeast(1)
    private fun font(v: Int) = v * scale() *
        resources.configuration.fontScale.coerceIn(0.85f, 1.25f)

    private fun text(size: Int, heavy: Boolean = false) = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, font(size))
        setTextColor(if (heavy) Colors.TEXT else Colors.DIM)
        typeface = Typeface.create(if (heavy) "sans-serif-medium" else "sans-serif",
            if (heavy) Typeface.BOLD else Typeface.NORMAL)
        includeFontPadding = false
        maxLines = if (heavy && kind == Kind.HERO) 2 else 1
        ellipsize = android.text.TextUtils.TruncateAt.END
    }

    private val art = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    private val shade = View(context).apply {
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
            0x0010111D, 0x9910111D.toInt(), 0xEF0B0B17.toInt()))
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    private val title = text(when (kind) {
        Kind.HERO -> 39
        Kind.STANDARD -> 24
        Kind.AVAILABLE -> 24
        Kind.COMPACT -> 19
    }, heavy = true)
    private val episode = text(when (kind) {
        Kind.HERO -> 19
        Kind.STANDARD -> 16
        Kind.AVAILABLE -> 17
        Kind.COMPACT -> 16
    })
    private val brandIcon = ImageView(context).apply {
        scaleType = ImageView.ScaleType.FIT_CENTER
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    private val brand = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(brandIcon, LinearLayout.LayoutParams(px(21), px(21)))
        addView(text(16, true).apply { text = "Nuvio TV" },
            LinearLayout.LayoutParams(-2, -2).apply { marginStart = px(7) })
    }
    private val newBadge = text(15, true).apply {
        text = "Neu"
        setTextColor(0xFF20172F.toInt())
        setPadding(px(12), px(4), px(12), px(4))
        background = GradientDrawable().apply {
            cornerRadius = px(13).toFloat()
            setColor(Colors.VIOLET)
        }
        visibility = View.GONE
    }
    private val progressTrack = FrameLayout(context).apply {
        background = GradientDrawable().apply {
            cornerRadius = px(4).toFloat()
            setColor(0x66D5D5DD)
        }
    }
    private val progressFill = View(context).apply {
        background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(
            0xFF9872F5.toInt(), 0xFFC09BFF.toInt())).apply {
            cornerRadius = px(4).toFloat()
        }
        addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ -> v.pivotX = 0f }
    }
    private val remaining = text(16)
    private val date = text(15)
    private val countdown = text(14, true).apply {
        setPadding(px(10), px(4), px(10), px(4))
        background = GradientDrawable().apply {
            cornerRadius = px(12).toFloat()
            setColor(0xBB55417C.toInt())
            setStroke(px(1), 0x665D4A8F)
        }
    }
    private val metadata = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(date, LinearLayout.LayoutParams(0, -2, 1f))
        addView(countdown, LinearLayout.LayoutParams(-2, -2))
    }
    private val playback = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(progressTrack, LinearLayout.LayoutParams(0, px(8), 1f))
        addView(remaining, LinearLayout.LayoutParams(-2, -2).apply { marginStart = px(13) })
    }
    private val copy = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.BOTTOM
        setPadding(px(if (kind == Kind.HERO) 23 else 16), px(7),
            px(if (kind == Kind.HERO) 24 else 15), px(if (kind == Kind.HERO) 20 else 11))
        addView(title, LinearLayout.LayoutParams(-1, -2))
        addView(episode, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(4) })
        if (kind == Kind.COMPACT) {
            addView(metadata, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(9) })
        } else if (kind == Kind.HERO || kind == Kind.STANDARD) {
            addView(playback, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(14) })
        }
    }

    var media: NuvioMediaItem? = null
        private set
    var onSelect: ((NuvioMediaItem) -> Unit)? = null
    var onFocused: ((VDesignCardView) -> Unit)? = null
    private var artworkUri: String? = null
    private var emptyLabel: TextView? = null

    init {
        clipChildren = true
        clipToPadding = false
        val radius = px(if (kind == Kind.COMPACT) 16 else 19).toFloat()
        background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(
            0xFF333449.toInt(), 0xFF181B30.toInt())).apply { cornerRadius = radius }
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
        clipToOutline = true
        addView(art, LayoutParams(-1, -1))
        addView(shade, LayoutParams(-1, -1))
        addView(copy, LayoutParams(-1, -2, Gravity.BOTTOM))
        if (kind != Kind.COMPACT) {
            addView(brand, LayoutParams(-2, -2, Gravity.TOP or Gravity.START).apply {
                leftMargin = px(17)
                topMargin = px(14)
            })
        }
        if (kind == Kind.AVAILABLE) {
            addView(newBadge, LayoutParams(-2, -2, Gravity.TOP or Gravity.END).apply {
                rightMargin = px(13)
                topMargin = px(11)
            })
        }
        progressTrack.addView(progressFill, LayoutParams(-1, -1))
        isFocusable = false
        isClickable = false
        setOnFocusChangeListener { _, focused ->
            if (focused) onFocused?.invoke(this)
            foreground = if (focused) GradientDrawable().apply {
                cornerRadius = radius
                setColor(Color.TRANSPARENT)
                setStroke(px(3), Colors.VIOLET)
            } else null
            elevation = if (focused) px(7).toFloat() else 0f
        }
        setOnClickListener { media?.let { onSelect?.invoke(it) } }
    }

    fun bind(item: NuvioMediaItem?, now: Instant, zone: ZoneId, locale: Locale) {
        media = item
        val visible = if (item != null) View.VISIBLE else View.INVISIBLE
        title.visibility = visible
        episode.visibility = visible
        brand.visibility = if (item != null && kind != Kind.COMPACT) View.VISIBLE else View.GONE
        isFocusable = item != null
        isClickable = item != null
        isEnabled = item != null
        contentDescription = item?.let { listOfNotNull(it.title, VDesignCardText.episode(it))
            .joinToString(", ") }
        title.text = item?.title.orEmpty()
        episode.text = item?.let(VDesignCardText::episode)
        episode.visibility = if (episode.text.isNullOrBlank()) View.GONE else visible
        if (item != null && kind != Kind.COMPACT) {
            val icon = sourceMark(item.sourcePackage)
            brandIcon.setImageDrawable(icon?.constantState?.newDrawable(resources)?.mutate() ?: icon)
        }
        newBadge.visibility = if (kind == Kind.AVAILABLE && item?.showNewBadge == true)
            View.VISIBLE else View.GONE

        val percent = item?.progressPercent?.takeIf { it in 1..100 }
        playback.visibility = if (percent != null && kind in setOf(Kind.HERO, Kind.STANDARD))
            View.VISIBLE else View.GONE
        progressFill.scaleX = (percent ?: 0) / 100f
        remaining.text = item?.let(VDesignCardText::remaining)
        remaining.visibility = if (remaining.text.isNullOrBlank()) View.GONE else View.VISIBLE

        date.text = item?.let { VDesignCardText.date(it, locale) }
        countdown.text = item?.let { VDesignCardText.countdown(it, now, zone) }
        metadata.visibility = if (item != null && kind == Kind.COMPACT &&
            !date.text.isNullOrBlank()) View.VISIBLE else View.GONE
        countdown.visibility = if (countdown.text.isNullOrBlank()) View.GONE else View.VISIBLE

        val nextArt = item?.let(VDesignCardText::artwork)
        if (nextArt != artworkUri || item == null) {
            artworkUri = nextArt
            art.tag = nextArt
            art.setImageDrawable(null) // no old bitmap after rebind/scroll/permission loss
            if (nextArt != null) loadArtwork(art, nextArt)
        }
    }

    /** Visible only for the first slot of a genuinely empty Nuvio section. */
    fun showEmpty(message: String?) {
        if (message == null) {
            emptyLabel?.visibility = View.GONE
            return
        }
        val label = emptyLabel ?: TextView(context).apply {
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_PX, font(18))
            setTextColor(Colors.DIM)
            includeFontPadding = false
            isFocusable = false
        }.also {
            addView(it, LayoutParams(-1, -2, Gravity.CENTER))
            emptyLabel = it
        }
        label.text = message
        label.visibility = View.VISIBLE
    }

    /** Network recovery must retry unchanged URLs after a previous decode failure. */
    fun retryMissingArtwork() {
        val uri = artworkUri ?: return
        if (media != null && art.tag == uri && art.drawable == null) loadArtwork(art, uri)
    }

    /** Existing LruCache loader notifies all cards waiting for an in-flight identical URI. */
    fun artworkReady(uri: String, bitmap: Bitmap) {
        if (artworkUri == uri && art.tag == uri && media != null) art.setImageBitmap(bitmap)
    }
}
