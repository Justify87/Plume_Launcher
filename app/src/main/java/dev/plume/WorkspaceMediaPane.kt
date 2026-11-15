package dev.plume

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/** Source-backed, lightweight TV application preview inside the common workspace. */
internal class WorkspaceMediaPane(
    context: Context,
    private val appName: String,
    private val loadArtwork: (ImageView, String?) -> Unit,
    private val onSelect: (NuvioMediaItem) -> Unit,
) : ScrollView(context) {
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt().coerceAtLeast(1)
    private val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(27), dp(25), dp(24), dp(24))
        clipChildren = false
        clipToPadding = false
    }
    private val cards = mutableListOf<VDesignCardView>()
    private var lastFeed: NuvioDashboardFeed? = null

    init {
        isFillViewport = true
        isVerticalScrollBarEnabled = false
        setBackgroundColor(0xFF16171C.toInt())
        clipChildren = false
        clipToPadding = false
        addView(content, LayoutParams(-1, -2))
    }

    private fun title(text: String, size: Float): TextView = TextView(context).apply {
        this.text = text
        textSize = size
        typeface = android.graphics.Typeface.create("sans-serif-medium",
            android.graphics.Typeface.BOLD)
        setTextColor(0xFFF7F7F8.toInt())
        includeFontPadding = false
    }

    /** Rebuild only when real provider data is rebound; no fake preview thumbnails. */
    fun bind(feed: NuvioDashboardFeed) {
        if (lastFeed == feed) return
        val oldFocus = cards.firstOrNull { it.isFocused }?.media?.stableId
        lastFeed = feed
        content.removeAllViews()
        cards.clear()
        content.addView(title(appName, 26f))
        val sections = listOf(
            "WEITERSCHAUEN" to feed.inProgress,
            "VERFÜGBARE FOLGEN" to feed.available,
            "DEMNÄCHST" to feed.upcoming,
        )
        var found = false
        for ((name, items) in sections) {
            if (items.isEmpty()) continue
            found = true
            content.addView(title(name, 18f), LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(22); bottomMargin = dp(12)
            })
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
                clipChildren = false
                clipToPadding = false
            }
            for (item in items.take(3)) {
                val card = VDesignCardView(context, VDesignCardView.Kind.STANDARD,
                    loadArtwork = loadArtwork, sourceMark = { _: String -> null })
                card.bind(item, Instant.now(), ZoneId.systemDefault(), Locale.getDefault())
                card.onSelect = onSelect
                row.addView(card, LinearLayout.LayoutParams(dp(220), dp(148)).apply {
                    marginEnd = dp(14)
                })
                cards += card
            }
            content.addView(row)
        }
        if (oldFocus != null) cards.firstOrNull { it.media?.stableId == oldFocus }?.requestFocus()
        if (!found) {
            content.addView(TextView(context).apply {
                text = "Keine freigegebenen Medienkacheln verfügbar"
                textSize = 17f
                setTextColor(0xFFB8BAC1.toInt())
            }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(26) })
        }
    }

    fun artworkReady(uri: String, bitmap: Bitmap) {
        cards.forEach { it.artworkReady(uri, bitmap) }
    }
}
