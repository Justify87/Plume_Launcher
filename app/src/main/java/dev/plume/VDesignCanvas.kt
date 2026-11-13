package dev.plume

import kotlin.math.min
import kotlin.math.roundToInt

/** M03: reference coordinates are visual pixels in the immutable 1536x864 design, not Android dp. */
internal object VDesignCanvas {
    const val WIDTH = 1536
    const val HEIGHT = 864
    const val SIDEBAR_WIDTH = 218

    data class Box(val x: Int, val y: Int, val width: Int, val height: Int)
    data class Placement(val x: Int, val y: Int, val width: Int, val height: Int)

    /** Fit without stretching: Android has already applied decor/window insets to this host. */
    fun place(viewWidth: Int, viewHeight: Int, box: Box): Placement {
        if (viewWidth <= 0 || viewHeight <= 0) return Placement(0, 0, 0, 0)
        val scale = min(viewWidth.toFloat() / WIDTH, viewHeight.toFloat() / HEIGHT)
        val xOffset = (viewWidth - WIDTH * scale) / 2f
        val yOffset = (viewHeight - HEIGHT * scale) / 2f
        return Placement(
            (xOffset + box.x * scale).roundToInt(),
            (yOffset + box.y * scale).roundToInt(),
            (box.width * scale).roundToInt().coerceAtLeast(1),
            (box.height * scale).roundToInt().coerceAtLeast(1),
        )
    }
}
