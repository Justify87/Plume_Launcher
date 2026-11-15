package dev.plume

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Honest Android-TV preview for non-Nuvio apps, backed only by MediaFeedRepository.
 * No source entry means no placeholder artwork or invented episode metadata.
 */
internal class WorkspaceGenericMediaPane(
    context: Context,
    private val title: String,
    private val loadArtwork: (ImageView, String?) -> Unit,
    private val open: (MediaItem) -> Unit,
) : ScrollView(context) {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt().coerceAtLeast(1)
    private val column = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(27), dp(25), dp(24), dp(24))
        clipChildren = false
    }
    private val artwork = mutableMapOf<String, MutableList<ImageView>>()
    private var lastItems: List<MediaItem>? = null

    init {
        setBackgroundColor(0xFF16171C.toInt())
        isFillViewport = true
        isVerticalScrollBarEnabled = false
        clipChildren = false
        addView(column, LayoutParams(-1, -2))
    }

    private fun heading(value: String, size: Float) = TextView(context).apply {
        text = value
        textSize = size
        setTextColor(0xFFF7F7F8.toInt())
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        includeFontPadding = false
    }

    fun bind(items: List<MediaItem>) {
        if (items == lastItems) return
        lastItems = items.toList()
        column.removeAllViews()
        artwork.clear()
        column.addView(heading(title, 26f))
        if (items.isEmpty()) {
            column.addView(TextView(context).apply {
                text = "Keine freigegebenen Medienkacheln verfügbar"
                textSize = 17f
                setTextColor(0xFFB8BAC1.toInt())
            }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(26) })
            return
        }
        val groups = items.groupBy { it.category }
        for ((category, entries) in groups) {
            val name = when (category) {
                MediaCategory.CONTINUE -> "WEITERSCHAUEN"
                MediaCategory.NEW -> "NEUE INHALTE"
                MediaCategory.RECOMMENDATION -> "EMPFEHLUNGEN"
                MediaCategory.UNKNOWN -> "MEDIEN"
            }
            column.addView(heading(name, 18f),
                LinearLayout.LayoutParams(-1, -2).apply {
                    topMargin = dp(22); bottomMargin = dp(12)
                })
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                clipChildren = false
                clipToPadding = false
            }
            for (item in entries.take(3)) {
                val panel = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    isFocusable = !item.intentUri.isNullOrBlank()
                    isClickable = isFocusable
                    contentDescription = item.title
                    background = GradientDrawable().apply {
                        cornerRadius = dp(16).toFloat()
                        setColor(0xFF202126.toInt())
                    }
                }
                val poster = ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }
                panel.addView(poster, LinearLayout.LayoutParams(-1, dp(126)))
                item.imageUri?.let { uri ->
                    artwork.getOrPut(uri) { mutableListOf() }.add(poster)
                    loadArtwork(poster, uri)
                }
                panel.addView(TextView(context).apply {
                    text = item.title
                    textSize = 16f
                    maxLines = 2
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    setTextColor(0xFFF7F7F8.toInt())
                    setPadding(dp(12), dp(8), dp(12), dp(8))
                }, LinearLayout.LayoutParams(-1, dp(61)))
                panel.setOnClickListener { if (panel.isFocusable) open(item) }
                panel.setOnFocusChangeListener { _, focused ->
                    panel.background = GradientDrawable().apply {
                        cornerRadius = dp(16).toFloat()
                        setColor(0xFF202126.toInt())
                        setStroke(dp(if (focused) 2 else 1),
                            if (focused) 0xFFE7FF80.toInt() else 0xFF34363E.toInt())
                    }
                    panel.animate().cancel()
                    if (ValueAnimator.areAnimatorsEnabled())
                        panel.animate().scaleX(if (focused) 1.03f else 1f)
                            .scaleY(if (focused) 1.03f else 1f).setDuration(145L).start()
                    else {
                        panel.scaleX = 1f
                        panel.scaleY = 1f
                    }
                }
                row.addView(panel, LinearLayout.LayoutParams(dp(226), dp(188)).apply {
                    marginEnd = dp(14)
                })
            }
            column.addView(row)
        }
    }

    fun artworkReady(uri: String, bitmap: Bitmap) {
        artwork[uri]?.forEach { it.setImageBitmap(bitmap) }
    }
}
