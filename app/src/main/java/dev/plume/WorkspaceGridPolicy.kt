package dev.plume

/**
 * Grid geometry for the actual right-hand viewport, never for the whole display.
 * All units are dp. Keep the number of columns stable while scrolling, and
 * choose a narrower grid on smaller Android TV / overscan configurations.
 */
internal object WorkspaceGridPolicy {
    data class Geometry(val columns: Int, val tileWidth: Int, val gap: Int)

    fun calculate(viewportWidthDp: Int, horizontalPaddingDp: Int = 48,
        desiredColumns: Int = 5, minimumTileWidthDp: Int = 112, gapDp: Int = 11): Geometry {
        val available = (viewportWidthDp - horizontalPaddingDp).coerceAtLeast(minimumTileWidthDp)
        val columns = (available + gapDp)
            .div(minimumTileWidthDp + gapDp)
            .coerceIn(1, desiredColumns)
        val width = ((available - (columns - 1) * gapDp) / columns).coerceAtLeast(1)
        return Geometry(columns, width, gapDp)
    }
}
