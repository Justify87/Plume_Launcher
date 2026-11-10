package dev.plume

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * A small vertical D-pad rail. Its rows are ordinary Android Views and are styled by the
 * Activity's existing Plume card/glass focus treatment; no parallel launcher/app model.
 */
class HomeSidebarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ScrollView(context, attrs) {

    companion object {
        const val OVERVIEW = "sidebar:overview"
        const val ALL_APPS = "sidebar:all_apps"
    }

    data class Item(val key: String, val label: String, val icon: Drawable)

    private val column = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        clipChildren = false
        clipToPadding = false
    }
    private val rows = mutableListOf<View>()

    init {
        isFocusable = false
        isVerticalScrollBarEnabled = false
        clipChildren = false
        clipToPadding = false
        addView(column, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    fun bind(
        items: List<Item>,
        ink: Int,
        decorate: (View) -> Unit,
        onFocused: (String) -> Unit,
        onOpen: (String) -> Unit,
        onDispose: (View) -> Unit,
    ) {
        rows.forEach(onDispose)
        column.removeAllViews()
        rows.clear()
        for (item in items) {
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), 0, dp(12), 0)
                tag = item.key
                contentDescription = item.label
            }
            row.addView(ImageView(context).apply {
                setImageDrawable(item.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }, LinearLayout.LayoutParams(dp(30), dp(30)))
            row.addView(TextView(context).apply {
                text = item.label
                textSize = 16f
                setTextColor(ink)
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER_VERTICAL
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                marginStart = dp(12)
            })
            decorate(row)
            val original = row.onFocusChangeListener
            row.setOnFocusChangeListener { v, focused ->
                original?.onFocusChange(v, focused)
                if (focused) onFocused(item.key)
            }
            row.setOnClickListener { onOpen(item.key) }
            column.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply {
                bottomMargin = dp(8)
            })
            rows += row
        }
    }

    val itemCount: Int get() = rows.size
    fun viewAt(index: Int): View? = rows.getOrNull(index)
    fun viewFor(key: String): View? = rows.firstOrNull { it.tag == key }
    fun indexOf(view: View): Int = rows.indexOf(view)
    fun focusedKey(): String? = rows.firstOrNull { it.isFocused }?.tag as? String
}
