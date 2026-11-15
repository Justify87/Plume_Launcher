package dev.plume

import kotlin.math.abs

/**
 * Spatial Android-TV D-pad targeting for the asymmetrical reference composition:
 * left rail; two progress columns; three vertical episodes; one upcoming strip.
 * The data model never determines adjacency; the reference boxes do.
 */
internal object VDesignFocusNavigator {
    enum class Direction { UP, DOWN, LEFT, RIGHT }
    data class Cell(val section: NuvioSection, val slot: Int, val box: VDesignCanvas.Box) {
        val row: Int get() = when (section) {
            NuvioSection.IN_PROGRESS -> 0
            NuvioSection.AVAILABLE -> 1
            NuvioSection.UPCOMING -> 2
        }
        val centerX: Int get() = box.x + box.width / 2
        val centerY: Int get() = box.y + box.height / 2
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
    private fun axisGap(aStart: Int, aEnd: Int, bStart: Int, bEnd: Int) = when {
        aEnd < bStart -> bStart - aEnd
        bEnd < aStart -> aStart - bEnd
        else -> 0
    }
    fun next(current: Cell, live: List<Cell>, direction: Direction, sidebarKey: String): Target {
        if (current !in live) return Target.Sidebar(sidebarKey)
        val a = current.box
        val candidates = live.filter { it != current && when (direction) {
            Direction.LEFT -> it.box.x + it.box.width <= a.x + 4
            Direction.RIGHT -> it.box.x >= a.x + a.width - 4
            Direction.UP -> it.box.y + it.box.height <= a.y + 4
            Direction.DOWN -> it.box.y >= a.y + a.height - 4
        } }
        if (candidates.isEmpty()) return if (direction == Direction.LEFT)
            Target.Sidebar(sidebarKey) else Target.Stay
        val closest = candidates.minWithOrNull(compareBy<Cell> { candidate ->
            val b = candidate.box
            val transverse = when (direction) {
                Direction.LEFT, Direction.RIGHT -> axisGap(a.y, a.y + a.height, b.y, b.y + b.height)
                Direction.UP, Direction.DOWN -> axisGap(a.x, a.x + a.width, b.x, b.x + b.width)
            }
            transverse * 10 + when (direction) {
                Direction.LEFT -> a.x - (b.x + b.width)
                Direction.RIGHT -> b.x - (a.x + a.width)
                Direction.UP -> a.y - (b.y + b.height)
                Direction.DOWN -> b.y - (a.y + a.height)
            }
        }.thenBy {
            if (direction == Direction.LEFT || direction == Direction.RIGHT)
                abs(current.centerY - it.centerY)
            else abs(current.centerX - it.centerX)
        }.thenBy { it.slot }) ?: return Target.Stay
        return Target.Card(closest.section, closest.slot)
    }
}
