package dev.plume

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.graphics.drawable.Drawable
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

/**
 * Right-hand TV media column. Ordinary Views, existing SnapScrollView and Classic's card decorator.
 * Slots reserve geometry; only visible and adjacent cards get Views. No provider queries or timers.
 */
class OverviewHomeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    data class Position(val row: Int, val index: Int)

    private class Lane(
        val category: MediaCategory,
        val cards: List<MediaItem>,
        val scroller: SnapScrollView,
        val slots: List<FrameLayout>,
    )

    private var lanes: List<Lane> = emptyList()
    private val mounted = mutableMapOf<View, Position>()
    private data class Bookmark(val category: MediaCategory, val stableId: String, val index: Int)
    private val lastPerPage = mutableMapOf<String, Bookmark>()
    private val lastPerRow = mutableMapOf<String, MutableMap<MediaCategory, Int>>()
    private val scrollPositions = mutableMapOf<String, MutableMap<MediaCategory, Int>>()
    private var page = HomeSidebarView.OVERVIEW
    private var renderedSignature: OverviewRenderSignature? = null

    /** Called when card appearance changes without a change to the media snapshot. */
    fun invalidateContent() { renderedSignature = null }
    private var demoLabel = ""
    private var decorate: (View) -> Unit = {}
    private var tint: (TextView) -> Unit = {}
    private var dispose: (View) -> Unit = {}
    private var selected: (MediaItem) -> Unit = {}
    private var progressArtwork: () -> Drawable = { android.graphics.drawable.ColorDrawable(0) }
    private var artwork: (ImageView, String?) -> Unit = { _, _ -> }
    private var sourceMark: (String?) -> Drawable? = { null }
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
        sections: List<HomeMediaSection>,
        sectionTitle: (MediaCategory) -> String,
        emptyLabel: (MediaCategory) -> String,
        exampleTag: String,
        decorateCard: (View) -> Unit,
        tintLabel: (TextView) -> Unit,
        disposeCard: (View) -> Unit,
        onSelect: (MediaItem) -> Unit,
        makeProgress: () -> Drawable,
        loadArtwork: (ImageView, String?) -> Unit = { _, _ -> },
        sourceMark: (String?) -> Drawable? = { null },
        onScroll: () -> Unit,
    ) {
        val nextSignature = OverviewRenderSignature(pageKey, sections,
            sections.flatMap { listOf(sectionTitle(it.category), emptyLabel(it.category)) }, exampleTag)
        // No new views, poster requests or focus resets for an identical provider snapshot.
        if (nextSignature == renderedSignature) return
        val focusedBefore = positionOf(findFocus())?.let { position ->
            val lane = lanes[position.row]
            Bookmark(lane.category, lane.cards[position.index].stableId, position.index)
        }
        val previousPage = page
        val rememberedScroll = scrollPositions.getOrPut(page) { mutableMapOf() }
        for (lane in lanes) {
            rememberedScroll[lane.category] = lane.scroller.scrollX
            for (slot in lane.slots) unmount(slot)
        }
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
        artwork = loadArtwork
        this.sourceMark = sourceMark
        onScrolled = onScroll

        val assembled = mutableListOf<Lane>()
        for ((section, group) in sections.withIndex()) {
            val cards = group.items
            val heading = title(sectionTitle(group.category), 18f)
            addView(heading, LayoutParams(MATCH_PARENT, dp(24)).apply {
                if (section > 0) topMargin = dp(12)
            })
            if (cards.isEmpty()) {
                addView(title(emptyLabel(group.category), 13f, 0.8f), LayoutParams(MATCH_PARENT, dp(35)))
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
            val lane = Lane(group.category, cards, scroller, slots)
            assembled += lane
            val savedScroll = scrollPositions[pageKey]?.get(group.category) ?: 0
            var restoredScroll = false
            scroller.setOnScrollChangeListener { _, _, _, _, _ ->
                updateWindow(lane)
                onScrolled()
            }
            scroller.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                if (!restoredScroll) {
                    restoredScroll = true
                    if (savedScroll > 0) scroller.scrollTo(savedScroll, 0)
                }
                updateWindow(lane)
            }
            addView(scroller, LayoutParams(MATCH_PARENT, dp(149)))
        }
        lanes = assembled
        renderedSignature = nextSignature
        resizeTiles()
        lanes.forEach(::updateWindow)
        if (focusedBefore != null && previousPage == pageKey) {
            val target = HomePageContent.restoreFocus(visibleSections(), focusedBefore.category,
                focusedBefore.stableId, focusedBefore.index)
            target?.let { focusAt(it.row, it.index)?.requestFocus() }
        }
    }

    /** Map attached focusable card covers back to row/index. Stale Views cannot pass this check. */
    fun positionOf(v: View?): Position? =
        v?.takeIf { it.isAttachedToWindow && it.isShown }?.let { mounted[it] }

    fun counts(): List<Int> = lanes.map { it.cards.size }
    fun lastIndex(row: Int): Int? = lanes.getOrNull(row)?.category?.let { lastPerRow[page]?.get(it) }

    private fun visibleSections() = lanes.map { HomeMediaSection(it.category, it.cards) }

    /** Network has recovered: retry only currently mounted covers that are still missing. */
    internal fun retryArtwork() {
        for (cover in mounted.keys) {
            val image = (cover as? FrameLayout)?.getChildAt(0) as? ImageView ?: continue
            if (image.drawable == null) artwork(image, image.tag as? String)
        }
    }

    /** Fill only cards waiting for this exact image; don't retry unrelated failed URLs. */
    internal fun artworkReady(uri: String, bitmap: Bitmap) {
        for (cover in mounted.keys) {
            val image = (cover as? FrameLayout)?.getChildAt(0) as? ImageView ?: continue
            if (image.tag == uri && image.drawable == null) image.setImageBitmap(bitmap)
        }
    }

    /** Restore a previous media target for this page, or choose the first available row. */
    fun preferredCard(): View? {
        val saved = lastPerPage[page]
        val target = HomePageContent.restoreFocus(visibleSections(), saved?.category,
            saved?.stableId, saved?.index ?: 0)
        return target?.let { focusAt(it.row, it.index) }
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
            tag = item.stableId
            contentDescription = listOfNotNull(
                demoLabel.takeIf { it.isNotBlank() }, item.title, item.sourceLabel ?: item.sourcePackage,
                item.subtitle?.takeIf { it.isNotBlank() },
            ).joinToString(", ")
        }
        decorate(cover)
        val kind = when (item.kind) {
            MediaKind.EPISODE -> context.getString(R.string.demo_episode)
            MediaKind.MOVIE -> context.getString(R.string.demo_movie)
            MediaKind.VIDEO -> context.getString(R.string.demo_video)
            MediaKind.PODCAST -> context.getString(R.string.demo_podcast)
            MediaKind.UNKNOWN -> ""
        }
        if (item.imageUri != null) {
            val image = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(0xFF343A44.toInt())
            }
            cover.addView(image, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            artwork(image, item.imageUri)
        } else if (kind.isNotEmpty()) {
            cover.addView(title(kind, 16f), FrameLayout.LayoutParams(
                MATCH_PARENT, MATCH_PARENT, Gravity.CENTER,
            ).apply { leftMargin = dp(10); rightMargin = dp(10) })
        }
        cover.setOnClickListener { selected(item) }
        column.addView(cover, LayoutParams(MATCH_PARENT, dp(78)))
        column.addView(ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = item.progressPercent ?: 0
            progressDrawable = progressArtwork()
            visibility = if (item.progressPercent != null) View.VISIBLE else View.INVISIBLE
        }, LayoutParams(MATCH_PARENT, dp(3)).apply { topMargin = dp(4) })
        column.addView(title(item.title, 14f), LayoutParams(MATCH_PARENT, dp(19)).apply { topMargin = dp(2) })
        column.addView(title(item.subtitle.orEmpty(), 12f, 0.8f), LayoutParams(MATCH_PARENT, dp(17)))
        val sourceText = listOfNotNull(demoLabel.takeIf { it.isNotBlank() },
            item.sourceLabel ?: item.sourcePackage).joinToString(" · ")
        column.addView(title(sourceText, 11f, 0.72f).apply {
            sourceMark(item.sourcePackage)?.let { mark ->
                mark.setBounds(0, 0, dp(12), dp(12))
                setCompoundDrawablesRelative(mark, null, null, null)
                compoundDrawablePadding = dp(5)
            }
        }, LayoutParams(MATCH_PARENT, dp(16)))
        slot.addView(column, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        val position = Position(row, index)
        mounted[cover] = position
        val focusTreatment = cover.onFocusChangeListener
        cover.setOnFocusChangeListener { v, focused ->
            focusTreatment?.onFocusChange(v, focused)
            if (focused) {
                lastPerPage[page] = Bookmark(lane.category, item.stableId, index)
                lastPerRow.getOrPut(page) { mutableMapOf() }[lane.category] = index
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
