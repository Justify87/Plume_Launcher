package dev.plume

import kotlin.math.abs

/** Deterministic, Android-free D-pad policy for V-Design's four visual bands. */
internal object VDesignFocusNavigator {
    enum class Direction { UP, DOWN, LEFT, RIGHT }
    data class Cell(val section: NuvioSection, val slot: Int, val box: VDesignCanvas.Box) {
        val row: Int get() = when (section) {
            NuvioSection.IN_PROGRESS -> 0
            NuvioSection.AVAILABLE -> 1
            NuvioSection.UPCOMING -> if (slot < 4) 2 else 3
        }
        val centerX: Int get() = box.x + box.width / 2
    }
    sealed class Target {
        object Stay : Target()
        data class Sidebar(val key: String) : Target()
        data class Card(val section: NuvioSection, val slot: Int) : Target()
    }

    fun fromSidebar(key: String, keys: List<String>, direction: Direction, preferred: Cell?): Target {
        val at = keys.indexOf(key)
        if (at < 0) return Target.Stay
        return when (direction) {
            Direction.UP -> keys.getOrNull(at - 1)?.let { Target.Sidebar(it) } ?: Target.Stay
            Direction.DOWN -> keys.getOrNull(at + 1)?.let { Target.Sidebar(it) } ?: Target.Stay
            Direction.LEFT -> Target.Stay
            Direction.RIGHT -> preferred?.let { Target.Card(it.section, it.slot) } ?: Target.Stay
        }
    }

    fun next(current: Cell, live: List<Cell>, direction: Direction, sidebarKey: String): Target {
        if (current !in live) return Target.Sidebar(sidebarKey)
        val candidates = when (direction) {
            Direction.LEFT -> live.filter { it.row == current.row && it.centerX < current.centerX }
            Direction.RIGHT -> live.filter { it.row == current.row && it.centerX > current.centerX }
            Direction.UP -> {
                val row = live.filter { it.row < current.row }.maxOfOrNull { it.row }
                live.filter { it.row == row }
            }
            Direction.DOWN -> {
                val row = live.filter { it.row > current.row }.minOfOrNull { it.row }
                live.filter { it.row == row }
            }
        }
        if (candidates.isEmpty()) return if (direction == Direction.LEFT)
            Target.Sidebar(sidebarKey) else Target.Stay
        val selected = when (direction) {
            Direction.LEFT -> candidates.maxByOrNull { it.centerX }
            Direction.RIGHT -> candidates.minByOrNull { it.centerX }
            Direction.UP, Direction.DOWN -> candidates.minWithOrNull(
                compareBy<Cell> { horizontalGap(current.centerX, it.box) }
                    .thenBy { abs(current.centerX - it.centerX) }
                    .thenBy { it.slot }
            )
        } ?: return Target.Stay
        return Target.Card(selected.section, selected.slot)
    }

    private fun horizontalGap(x: Int, box: VDesignCanvas.Box): Int = when {
        x < box.x -> box.x - x
        x > box.x + box.width -> x - box.x - box.width
        else -> 0
    }
}
