package dev.plume

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.graphics.drawable.Drawable
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

/**
 * M03's offline right-hand content column. Ordinary Views, existing SnapScrollView and the same
 * card decorator as Classic. Slots reserve geometry; only visible and adjacent cards get Views.
 * No media-provider calls, bitmap requests, timers or background work.
 */
class OverviewHomeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    data class Position(val row: Int, val index: Int)

    private class Lane(
        val cards: List<DemoMedia.Card>,
        val scroller: SnapScrollView,
        val slots: List<FrameLayout>,
    )

    private var lanes: List<Lane> = emptyList()
    private val mounted = mutableMapOf<View, Position>()
    private val lastPerPage = mutableMapOf<String, Position>()
    private val lastPerRow = mutableMapOf<String, MutableMap<Int, Int>>()
    private var page = HomeSidebarView.OVERVIEW
    private var demoLabel = ""
    private var decorate: (View) -> Unit = {}
    private var tint: (TextView) -> Unit = {}
    private var dispose: (View) -> Unit = {}
    private var selected: (DemoMedia.Card) -> Unit = {}
    private var progressArtwork: () -> Drawable = { android.graphics.drawable.ColorDrawable(0) }
    private var onScrolled: () -> Unit = {}
    private var tileWidth = dp(196)

    init {
        orientation = VERTICAL
        clipChildren = false
        clipToPadding = false
        isFocusable = false
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun title(text: String, size: Float, alphaValue: Float = 1f) = TextView(context).apply {
        this.text = text
        textSize = size
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        alpha = alphaValue
        tint(this)
    }

    internal fun bind(
        pageKey: String,
        rows: List<List<DemoMedia.Card>>,
        continueTitle: String,
        newTitle: String,
        exampleTag: String,
        emptyLabel: String,
        decorateCard: (View) -> Unit,
        tintLabel: (TextView) -> Unit,
        disposeCard: (View) -> Unit,
        onSelect: (DemoMedia.Card) -> Unit,
        makeProgress: () -> Drawable,
        onScroll: () -> Unit,
    ) {
        val focusedBefore = positionOf(findFocus())
        val previousPage = page
        for (lane in lanes) for (slot in lane.slots) unmount(slot)
        removeAllViews()
        lanes = emptyList()
        mounted.clear()
        page = pageKey
        demoLabel = exampleTag
        decorate = decorateCard
        tint = tintLabel
        dispose = disposeCard
        selected = onSelect
        progressArtwork = makeProgress
        onScrolled = onScroll

        val assembled = mutableListOf<Lane>()
        for (section in 0..1) {
            val cards = rows.getOrElse(section) { emptyList() }
            // The common Overview always shows exactly two sections. App previews hide empty ones.
            if (pageKey != HomeSidebarView.OVERVIEW && cards.isEmpty()) continue
            val heading = title(if (section == 0) continueTitle else newTitle, 18f)
            addView(heading, LayoutParams(MATCH_PARENT, dp(24)).apply {
                if (section > 0) topMargin = dp(12)
            })
            if (cards.isEmpty()) {
                addView(title(emptyLabel, 13f, 0.8f), LayoutParams(MATCH_PARENT, dp(35)))
                continue
            }

            val scroller = SnapScrollView(context, null).apply {
                isHorizontalScrollBarEnabled = false
                isFocusable = false
                clipChildren = false
                setPadding(dp(5), dp(6), dp(5), dp(3))
            }
            val row = LinearLayout(context).apply {
                orientation = HORIZONTAL
                clipChildren = false
                clipToPadding = false
            }
            scroller.addView(row, ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
            val slots = cards.indices.map { i ->
                FrameLayout(context).apply {
                    isFocusable = false
                    clipChildren = false
                    clipToPadding = false
                    row.addView(this, LinearLayout.LayoutParams(tileWidth, dp(138)).apply {
                        if (i > 0) marginStart = dp(16)
                    })
                }
            }
            val lane = Lane(cards, scroller, slots)
            assembled += lane
            scroller.setOnScrollChangeListener { _, _, _, _, _ ->
                updateWindow(lane)
                onScrolled()
            }
            scroller.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateWindow(lane) }
            addView(scroller, LayoutParams(MATCH_PARENT, dp(149)))
        }
        lanes = assembled
        resizeTiles()
        lanes.forEach(::updateWindow)
        if (focusedBefore != null && previousPage == pageKey) {
            focusAt(focusedBefore.row, focusedBefore.index)?.requestFocus()
        }
    }

    /** Map attached focusable card covers back to row/index. Stale Views cannot pass this check. */
    fun positionOf(v: View?): Position? =
        v?.takeIf { it.isAttachedToWindow && it.isShown }?.let { mounted[it] }

    fun counts(): List<Int> = lanes.map { it.cards.size }
    fun lastIndex(row: Int): Int? = lastPerRow[page]?.get(row)

    /** Restore a previous media target for this page, or choose the first available row. */
    fun preferredCard(): View? {
        val saved = lastPerPage[page]
        if (saved != null) focusAt(saved.row, saved.index)?.let { return it }
        return lanes.indices.firstNotNullOfOrNull { focusAt(it, 0) }
    }

    /**
     * Mount before requesting focus. SnapScrollView sees full tile-sized slots even when the rest
     * of the row has not been constructed, so Left/Right cannot scroll to half a card.
     */
    fun focusAt(row: Int, index: Int): View? {
        val lane = lanes.getOrNull(row) ?: return null
        if (index !in lane.cards.indices) return null
        val cover = mount(lane, row, index)
        val pitch = tileWidth + dp(16)
        val left = index * pitch
        val right = left + tileWidth
        val start = lane.scroller.scrollX
        val available = (lane.scroller.width - lane.scroller.paddingLeft - lane.scroller.paddingRight).coerceAtLeast(tileWidth)
        if (left < start || right > start + available) {
            lane.scroller.scrollTo(left, 0)
        }
        updateWindow(lane)
        return cover
    }

    private fun mount(lane: Lane, row: Int, index: Int): View {
        val slot = lane.slots[index]
        (slot.getChildAt(0) as? LinearLayout)?.let { existing ->
            return existing.getChildAt(0)
        }
        val item = lane.cards[index]
        val column = LinearLayout(context).apply {
            orientation = VERTICAL
            clipChildren = false
            clipToPadding = false
        }
        val cover = FrameLayout(context).apply {
            tag = item.id
            contentDescription = "$demoLabel: ${item.title}, ${item.source}, ${item.subtitle}"
        }
        decorate(cover)
        val kind = when (item.kind) {
            DemoMedia.Kind.EPISODE -> context.getString(R.string.demo_episode)
            DemoMedia.Kind.MOVIE -> context.getString(R.string.demo_movie)
            DemoMedia.Kind.VIDEO -> context.getString(R.string.demo_video)
            DemoMedia.Kind.PODCAST -> context.getString(R.string.demo_podcast)
        }
        cover.addView(title(kind, 16f), FrameLayout.LayoutParams(
            MATCH_PARENT, MATCH_PARENT, Gravity.CENTER,
        ).apply { leftMargin = dp(10); rightMargin = dp(10) })
        cover.setOnClickListener { selected(item) }
        column.addView(cover, LayoutParams(MATCH_PARENT, dp(78)))
        column.addView(ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = item.progress ?: 0
            progressDrawable = progressArtwork()
            visibility = if (item.progress != null) View.VISIBLE else View.INVISIBLE
        }, LayoutParams(MATCH_PARENT, dp(3)).apply { topMargin = dp(4) })
        column.addView(title(item.title, 14f), LayoutParams(MATCH_PARENT, dp(19)).apply { topMargin = dp(2) })
        column.addView(title(item.subtitle, 12f, 0.8f), LayoutParams(MATCH_PARENT, dp(17)))
        column.addView(title("$demoLabel · ${item.source}", 11f, 0.72f), LayoutParams(MATCH_PARENT, dp(16)))
        slot.addView(column, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        val position = Position(row, index)
        mounted[cover] = position
        val focusTreatment = cover.onFocusChangeListener
        cover.setOnFocusChangeListener { v, focused ->
            focusTreatment?.onFocusChange(v, focused)
            if (focused) {
                lastPerPage[page] = position
                lastPerRow.getOrPut(page) { mutableMapOf() }[row] = index
                updateWindow(lane)
            }
        }
        return cover
    }

    private fun unmount(slot: FrameLayout) {
        val column = slot.getChildAt(0) as? LinearLayout ?: return
        val cover = column.getChildAt(0)
        mounted.remove(cover)
        dispose(cover)
        slot.removeAllViews()
    }

    private fun updateWindow(lane: Lane) {
        val window = DemoMedia.visibleWindow(
            lane.cards.size, lane.scroller.scrollX,
            lane.scroller.width - lane.scroller.paddingLeft - lane.scroller.paddingRight,
            tileWidth + dp(16),
        )
        val row = lanes.indexOf(lane)
        // During bind the lanes collection has not been assigned yet; use the assembled slot list.
        val indexRow = if (row >= 0) row else return
        for ((i, slot) in lane.slots.withIndex()) {
            if (i in window) mount(lane, indexRow, i)
            else if (slot.childCount > 0 && !(slot.getChildAt(0) as LinearLayout).getChildAt(0).isFocused) unmount(slot)
        }
    }

    private fun resizeTiles() {
        val actual = if (width > 0) width else dp(580)
        tileWidth = ((actual - dp(15)) / 2.65f).toInt().coerceIn(dp(130), dp(224))
        for (lane in lanes) lane.slots.forEachIndexed { index, slot ->
            val params = slot.layoutParams as LayoutParams
            if (params.width != tileWidth) {
                params.width = tileWidth
                slot.layoutParams = params
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw) {
            resizeTiles()
            lanes.forEach(::updateWindow)
        }
    }
}
