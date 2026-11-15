package dev.plume

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Transitional in-workspace Mullvad entry while moving upstream Mullvad's
 * Activity-owned repository controls into a real embedded panel. Never
 * invents a connected state or performs tunnel management outside Mullvad.
 */
internal class WorkspaceVpnEntryPane(
    context: Context,
    openMullvad: () -> Unit,
) : LinearLayout(context) {
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt().coerceAtLeast(1)

    init {
        orientation = VERTICAL
        setPadding(dp(30), dp(27), dp(30), dp(22))
        setBackgroundColor(0xFF16171C.toInt())
        addView(TextView(context).apply {
            text = "Mullvad VPN"
            textSize = 26f
            setTextColor(0xFFF7F7F8.toInt())
        })
        addView(TextView(context).apply {
            text = "Tunnelverwaltung und Verbindungsstatus kommen ausschließlich vom originalen Mullvad-Dienst."
            textSize = 16f
            setTextColor(0xFFB8BAC1.toInt())
        }, LayoutParams(-1, -2).apply { topMargin = dp(14) })
        val button = TextView(context).apply {
            text = "VPN-Verwaltung öffnen"
            textSize = 18f
            gravity = Gravity.CENTER
            isFocusable = true
            isClickable = true
            setPadding(dp(24), dp(13), dp(24), dp(13))
            fun paint(focused: Boolean) {
                background = GradientDrawable().apply {
                    cornerRadius = dp(14).toFloat()
                    setColor(if (focused) 0xFFE7FF80.toInt() else 0xFF202126.toInt())
                }
                setTextColor(if (focused) 0xFF121317.toInt() else 0xFFF7F7F8.toInt())
                animate().cancel()
                if (!ValueAnimator.areAnimatorsEnabled()) {
                    scaleX = 1f
                    scaleY = 1f
                } else animate().scaleX(if (focused) 1.025f else 1f)
                    .scaleY(if (focused) 1.025f else 1f).setDuration(150L).start()
            }
            paint(false)
            setOnFocusChangeListener { _, focused -> paint(focused) }
            setOnClickListener { openMullvad() }
        }
        addView(button, LayoutParams(dp(270), -2).apply { topMargin = dp(28) })
    }
}
