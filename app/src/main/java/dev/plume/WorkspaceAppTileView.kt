package dev.plume

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

internal class WorkspaceAppTileView(
    context: Context,
    val packageName: String,
    label: String,
    icon: Drawable,
    originalColors: Boolean,
    launch: () -> Unit,
    longPress: (View) -> Unit,
) : LinearLayout(context) {
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt().coerceAtLeast(1)
    private val artwork = ImageView(context).apply {
        setImageDrawable(icon.constantState?.newDrawable(resources)?.mutate() ?: icon.mutate())
        scaleType = ImageView.ScaleType.FIT_CENTER
        if (!originalColors) setColorFilter(0xFFF7F7F8.toInt())
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(12), dp(13), dp(12), dp(9))
        clipChildren = false
        clipToPadding = false
        tag = packageName
        contentDescription = label
        isFocusable = true
        isClickable = true
        addView(artwork, LayoutParams(dp(58), dp(58)))
        addView(TextView(context).apply {
            text = label
            textSize = 15f
            setTextColor(0xFFF7F7F8.toInt())
            gravity = Gravity.CENTER
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            includeFontPadding = false
        }, LayoutParams(-1, dp(43)).apply { topMargin = dp(8) })
        fun paint(focused: Boolean) {
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(if (focused) 0xFF303329.toInt() else 0xFF202126.toInt())
                setStroke(dp(if (focused) 2 else 1),
                    if (focused) 0xFFE7FF80.toInt() else 0xFF34363E.toInt())
            }
            animate().cancel()
            if (ValueAnimator.areAnimatorsEnabled()) {
                animate().scaleX(if (focused) 1.045f else 1f)
                    .scaleY(if (focused) 1.045f else 1f)
                    .setDuration(145L).start()
            } else {
                scaleX = 1f
                scaleY = 1f
            }
        }
        paint(false)
        setOnFocusChangeListener { _, focused -> paint(focused) }
        setOnClickListener { launch() }
        setOnLongClickListener { longPress(this); true }
    }
}
