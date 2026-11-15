package dev.plume

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
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
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The four reference card families share a single bitmap cache and the original Nuvio intents.
 * No mockup art or sample series names are bundled. A slot is focusable only with real media.
 */
internal class VDesignCardView(
    context: Context,
    val kind: Kind,
    private val loadArtwork: (ImageView, String?) -> Unit,
    @Suppress("UNUSED_PARAMETER") private val sourceMark: (String) -> Drawable?,
) : FrameLayout(context) {
    enum class Kind { HERO, STANDARD, AVAILABLE, COMPACT }

    private object Colors {
        const val WHITE = 0xFFF8F8FA.toInt()
        const val MUTED = 0xFFD5D5D7.toInt()
        const val INK = 0xFF111217.toInt()
        const val LIME = 0xFFE7FF80.toInt()
        const val LAVENDER = 0xFFDCB7F7.toInt()
        const val PEACH = 0xFFFFCEB6.toInt()
        const val SURFACE = 0xFF25272B.toInt()
    }
    private fun scale() = min(resources.displayMetrics.widthPixels / 1536f,
        resources.displayMetrics.heightPixels / 864f).coerceAtLeast(0.2f)
    private fun px(v: Int) = (v * scale()).roundToInt().coerceAtLeast(1)
    private fun font(v: Int) = v * scale() *
        resources.configuration.fontScale.coerceIn(0.85f, 1.25f)
    private fun bg(color: Int, radius: Int) = GradientDrawable().apply {
        cornerRadius = px(radius).toFloat()
        setColor(color)
    }
    private fun label(size: Int, bold: Boolean = false, darkInk: Boolean = false) = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, font(size))
        setTextColor(if (darkInk) Colors.INK else if (bold) Colors.WHITE else Colors.MUTED)
        typeface = Typeface.create(if (bold) "sans-serif-medium" else "sans-serif",
            if (bold) Typeface.BOLD else Typeface.NORMAL)
        includeFontPadding = false
        maxLines = if (kind == Kind.HERO && bold) 2 else 1
        ellipsize = android.text.TextUtils.TruncateAt.END
        gravity = Gravity.CENTER_VERTICAL
    }

    private val art = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    private val title = label(
        when (kind) { Kind.HERO -> 51; Kind.STANDARD -> 25; Kind.AVAILABLE -> 25; Kind.COMPACT -> 22 },
        bold = true, darkInk = kind == Kind.HERO)
    private val episode = label(
        when (kind) { Kind.HERO -> 23; Kind.STANDARD -> 19; Kind.AVAILABLE -> 19; Kind.COMPACT -> 17 },
        darkInk = kind == Kind.HERO)
    private val percentText = label(18, darkInk = kind == Kind.HERO)
    private val progressTrack = FrameLayout(context).apply {
        background = bg(if (kind == Kind.HERO) 0x6690947B else 0x778E9399, 6)
    }
    private val progressFill = View(context).apply {
        background = bg(Colors.LIME, 6)
        addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ -> view.pivotX = 0f }
    }
    private val playback = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(progressTrack, LinearLayout.LayoutParams(0, px(10), 1f))
        addView(percentText, LinearLayout.LayoutParams(-2, -2).apply { marginStart = px(15) })
    }
    private val heroButton = label(25, true).apply {
        text = "▶  Fortsetzen"
        gravity = Gravity.CENTER
        background = bg(Colors.INK, 20)
        setPadding(px(22), px(10), px(22), px(10))
        visibility = if (kind == Kind.HERO) View.VISIBLE else View.GONE
    }
    private val dateBadge = label(17, true, darkInk = true).apply {
        setPadding(px(18), px(8), px(18), px(8))
        gravity = Gravity.CENTER
        background = bg(Colors.LIME, 22)
    }
    private val countdown = label(15)
    private val newBadge = label(16, true, darkInk = true).apply {
        text = "Neu"
        gravity = Gravity.CENTER
        setPadding(px(13), px(5), px(13), px(5))
        background = bg(Colors.LAVENDER, 15)
    }
    private var artworkUri: String? = null
    private var emptyLabel: TextView? = null
    var media: NuvioMediaItem? = null
        private set
    var onSelect: ((NuvioMediaItem) -> Unit)? = null
    var onFocused: ((VDesignCardView) -> Unit)? = null

    private fun roundImage(image: View, radius: Int) {
        image.background = bg(Colors.SURFACE, radius)
        image.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, px(radius).toFloat())
            }
        }
        image.clipToOutline = true
    }

    init {
        clipChildren = false
        clipToPadding = false
        progressTrack.addView(progressFill, FrameLayout.LayoutParams(-1, -1))
        when (kind) {
            Kind.HERO -> {
                roundImage(this, 28)
                background = bg(Colors.SURFACE, 28)
                addView(art, LayoutParams(-1, -1))
                // The photographed subject stays to the right; the distinctive pale curved
                // editorial panel is drawn rather than baked into a static hero image.
                addView(object : View(context) {
                    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    private val path = Path()
                    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
                        super.onSizeChanged(w, h, oldw, oldh)
                        if (w > 0 && h > 0) paint.shader = LinearGradient(
                            0f, 0f, w * .35f, h.toFloat(),
                            intArrayOf(0xFFE2E7BF.toInt(), 0xFFCFD9A4.toInt(), 0xFFB2C076.toInt()),
                            floatArrayOf(0f, .55f, 1f), Shader.TileMode.CLAMP)
                    }
                    override fun onDraw(canvas: Canvas) {
                        val w = width.toFloat()
                        val h = height.toFloat()
                        path.reset()
                        path.moveTo(0f, 0f)
                        path.lineTo(w * .62f, 0f)
                        path.cubicTo(w * .45f, h * .27f, w * .45f, h * .70f, w * .70f, h)
                        path.lineTo(0f, h)
                        path.close()
                        canvas.drawPath(path, paint)
                    }
                }, LayoutParams(-1, -1))
                val content = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(px(39), px(32), px(6), px(27))
                    addView(title, LinearLayout.LayoutParams(-1, -2))
                    addView(episode, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(8) })
                    addView(heroButton, LinearLayout.LayoutParams(-2, -2).apply { topMargin = px(27) })
                    addView(playback, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(22) })
                }
                addView(content, LayoutParams(px(414), -1, Gravity.START))
            }
            Kind.STANDARD -> {
                roundImage(this, 22)
                background = bg(Colors.SURFACE, 22)
                addView(art, LayoutParams(-1, -1))
                addView(View(context).apply {
                    background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(0x0013171C, 0x3313171C, 0xED111419.toInt()))
                }, LayoutParams(-1, -1))
                val content = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.BOTTOM
                    setPadding(px(23), 0, px(19), px(12))
                    addView(title)
                    addView(episode, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(3) })
                    addView(playback, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(8) })
                }
                addView(content, LayoutParams(-1, -1))
            }
            Kind.AVAILABLE -> {
                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    addView(art, LinearLayout.LayoutParams(px(150), px(98)))
                    val details = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(px(22), 0, 0, 0)
                        addView(title)
                        val meta = LinearLayout(context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            addView(episode, LinearLayout.LayoutParams(-2, -2))
                            addView(newBadge, LinearLayout.LayoutParams(-2, -2).apply {
                                marginStart = px(13)
                            })
                        }
                        addView(meta, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(10) })
                    }
                    addView(details, LinearLayout.LayoutParams(0, -1, 1f))
                }
                roundImage(art, 18)
                addView(row, LayoutParams(-1, -1))
                addView(View(context).apply { setBackgroundColor(0xFF414247.toInt()) },
                    LayoutParams(-1, px(1), Gravity.BOTTOM))
            }
            Kind.COMPACT -> {
                roundImage(art, 20)
                addView(art, LayoutParams(-1, px(149), Gravity.TOP))
                addView(dateBadge, LayoutParams(-2, -2, Gravity.TOP or Gravity.START))
                val captions = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(px(7), 0, px(3), 0)
                    addView(title)
                    val details = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        addView(episode, LinearLayout.LayoutParams(-2, -2))
                        addView(View(context).apply { background = bg(Colors.LIME, 99) },
                            LinearLayout.LayoutParams(px(8), px(8)).apply {
                                marginStart = px(10); marginEnd = px(8)
                            })
                        addView(countdown)
                    }
                    addView(details, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(5) })
                }
                addView(captions, LayoutParams(-1, -2, Gravity.TOP).apply { topMargin = px(157) })
            }
        }
        isFocusable = false
        isClickable = false
        setOnFocusChangeListener { _, focused ->
            if (focused) onFocused?.invoke(this)
            foreground = if (focused) GradientDrawable().apply {
                cornerRadius = px(if (kind == Kind.COMPACT) 18 else 23).toFloat()
                setColor(Color.TRANSPARENT)
                setStroke(px(2), Colors.LIME)
            } else null
            animate().cancel()
            animate().scaleX(if (focused) 1.015f else 1f)
                .scaleY(if (focused) 1.015f else 1f)
                .setDuration(155).start()
            elevation = if (focused) px(5).toFloat() else 0f
        }
        setOnClickListener { media?.let { onSelect?.invoke(it) } }
    }

    /** Reflow type to the *actual* canvas viewport, not the physical display metrics. */
    fun updateViewportScale(viewportScale: Float) {
        if (viewportScale <= 0f) return
        val fontScale = resources.configuration.fontScale.coerceIn(.85f, 1.25f)
        fun adjust(view: TextView, sourcePx: Int) =
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, sourcePx * viewportScale * fontScale)
        adjust(title, when (kind) {
            Kind.HERO -> 51; Kind.STANDARD -> 25; Kind.AVAILABLE -> 25; Kind.COMPACT -> 22
        })
        adjust(episode, when (kind) {
            Kind.HERO -> 23; Kind.STANDARD -> 19; Kind.AVAILABLE -> 19; Kind.COMPACT -> 17
        })
        adjust(percentText, 18)
        adjust(heroButton, 25)
        adjust(dateBadge, 17)
        adjust(newBadge, 16)
        adjust(countdown, 15)
        emptyLabel?.let { adjust(it, 18) }
    }

    /** Reference alternates lime, lavender and peach date chips across six visible tiles. */
    fun setUpcomingAccent(slot: Int) {
        if (kind != Kind.COMPACT) return
        dateBadge.background = bg(when (slot % 3) {
            1 -> Colors.LAVENDER
            2 -> Colors.PEACH
            else -> Colors.LIME
        }, 22)
    }

    fun bind(item: NuvioMediaItem?, now: Instant, zone: ZoneId, locale: Locale) {
        media = item
        isFocusable = item != null
        isClickable = item != null
        isEnabled = item != null
        val visible = if (item == null) View.INVISIBLE else View.VISIBLE
        title.text = item?.title.orEmpty()
        title.visibility = visible
        episode.text = item?.let(VDesignCardText::episode)
        episode.visibility = if (item == null || episode.text.isNullOrBlank()) View.GONE else View.VISIBLE
        contentDescription = item?.let { listOfNotNull(it.title, VDesignCardText.episode(it))
            .joinToString(", ") }
        val percent = item?.progressPercent?.takeIf { it in 1..100 }
        playback.visibility = if (percent != null) View.VISIBLE else View.GONE
        progressFill.scaleX = (percent ?: 0) / 100f
        percentText.text = percent?.let { "$it %" }
        heroButton.visibility = if (kind == Kind.HERO && item != null) View.VISIBLE else View.GONE
        newBadge.visibility = if (kind == Kind.AVAILABLE && item?.showNewBadge == true) View.VISIBLE else View.GONE
        dateBadge.text = item?.releaseDate?.format(DateTimeFormatter.ofPattern("EE d", locale))
            ?.uppercase(locale).orEmpty()
        dateBadge.visibility = if (kind == Kind.COMPACT && item?.releaseDate != null)
            View.VISIBLE else View.GONE
        countdown.text = item?.let { VDesignCardText.countdown(it, now, zone) }
        countdown.visibility = if (countdown.text.isNullOrBlank()) View.GONE else View.VISIBLE
        val next = item?.let(VDesignCardText::artwork)
        if (artworkUri != next || item == null) {
            artworkUri = next
            art.tag = next
            art.setImageDrawable(null)
            if (next != null) loadArtwork(art, next)
        }
    }

    fun showEmpty(message: String?) {
        if (message == null) {
            emptyLabel?.visibility = View.GONE
            return
        }
        val v = emptyLabel ?: label(18).apply {
            gravity = Gravity.CENTER
            setTextColor(Colors.MUTED)
            background = bg(Colors.SURFACE, 15)
        }.also { addView(it, LayoutParams(-1, px(50), Gravity.CENTER)); emptyLabel = it }
        v.text = message
        v.visibility = View.VISIBLE
    }

    fun retryMissingArtwork() {
        val uri = artworkUri ?: return
        if (media != null && art.tag == uri && art.drawable == null) loadArtwork(art, uri)
    }
    fun artworkReady(uri: String, bitmap: Bitmap) {
        if (artworkUri == uri && art.tag == uri && media != null) art.setImageBitmap(bitmap)
    }
}
