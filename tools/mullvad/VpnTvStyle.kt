package net.mullvad.mullvadvpn.app

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.widget.EditText
import android.widget.Button

/**
 * Plume's TV-only visual language shared by all Mullvad pages.
 * Presentation only: never caches tunnel/account/settings state and adds no dependencies.
 */
internal object VpnTvStyle {
    const val BACKGROUND = 0xFF121317.toInt()
    const val SURFACE = 0xFF202126.toInt()
    const val RAIL = 0xFF191A1F.toInt()
    const val INK = 0xFFF7F7F8.toInt()
    const val MUTED = 0xFFB8BAC1.toInt()
    const val LIME = 0xFFE7FF80.toInt()
    const val LAVENDER = 0xFFDCB7F7.toInt()
    const val EDGE = 0xFF34363E.toInt()

    private fun dp(context: Context, n: Int) =
        (n * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)

    fun background(context: Context, focused: Boolean, selected: Boolean = false) =
        GradientDrawable().apply {
            cornerRadius = dp(context, 11).toFloat()
            setColor(if (focused) LIME else if (selected) 0xFF353A30.toInt() else SURFACE)
            setStroke(dp(context, 1), if (focused) LIME else EDGE)
        }

    fun paint(button: Button, focused: Boolean, selected: Boolean = false) {
        button.isAllCaps = false
        button.minHeight = 0
        button.minWidth = 0
        button.setTextColor(if (focused) BACKGROUND else INK)
        button.background = background(button.context, focused, selected)
        button.animate().cancel()
        if (android.animation.ValueAnimator.areAnimatorsEnabled()) {
            button.animate().scaleX(if (focused) 1.012f else 1f)
                .scaleY(if (focused) 1.012f else 1f)
                .setDuration(150L).start()
        } else {
            button.scaleX = 1f
            button.scaleY = 1f
        }
    }

    /** Framework focus-state drawable; no extra listeners or keyboard side effects. */
    fun field(edit: EditText) {
        edit.setTextColor(INK)
        edit.setHintTextColor(MUTED)
        edit.minHeight = dp(edit.context, 42)
        edit.setPadding(dp(edit.context, 12), dp(edit.context, 6),
            dp(edit.context, 12), dp(edit.context, 6))
        edit.background = StateListDrawable().apply {
            val focused = GradientDrawable().apply {
                cornerRadius = dp(edit.context, 9).toFloat()
                setColor(0xFF292B31.toInt())
                setStroke(dp(edit.context, 2), LIME)
            }
            val resting = GradientDrawable().apply {
                cornerRadius = dp(edit.context, 9).toFloat()
                setColor(SURFACE)
                setStroke(dp(edit.context, 1), EDGE)
            }
            addState(intArrayOf(android.R.attr.state_focused), focused)
            addState(intArrayOf(), resting)
        }
    }
}
