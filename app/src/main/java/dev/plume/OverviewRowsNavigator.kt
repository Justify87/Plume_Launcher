package dev.plume

/** Pure directions inside the M03 media rows. Hidden/empty rows are never focus targets. */
internal object OverviewRowsNavigator {
    enum class Direction { UP, DOWN, LEFT, RIGHT }
    sealed class Target {
        object Stay : Target()
        object Sidebar : Target()
        object Top : Target()
        data class Card(val row: Int, val index: Int) : Target()
    }

    fun next(
        row: Int,
        index: Int,
        counts: List<Int>,
        direction: Direction,
        rememberedOtherIndex: Int? = null,
    ): Target {
        if (row !in counts.indices || counts[row] <= 0 || index !in 0 until counts[row]) return Target.Sidebar
        return when (direction) {
            Direction.LEFT -> if (index == 0) Target.Sidebar else Target.Card(row, index - 1)
            Direction.RIGHT -> if (index + 1 >= counts[row]) Target.Stay else Target.Card(row, index + 1)
            Direction.UP, Direction.DOWN -> {
                val step = if (direction == Direction.UP) -1 else 1
                val nextRow = (row + step).let { start ->
                    if (step < 0) (start downTo 0).firstOrNull { counts[it] > 0 }
                    else (start until counts.size).firstOrNull { counts[it] > 0 }
                }
                if (nextRow == null) {
                    if (step < 0) Target.Top else Target.Stay
                } else {
                    Target.Card(nextRow, (rememberedOtherIndex ?: index).coerceIn(0, counts[nextRow] - 1))
                }
            }
        }
    }
}
