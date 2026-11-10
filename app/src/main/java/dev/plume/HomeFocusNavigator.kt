package dev.plume

/**
 * Pure D-pad decisions for the optional Overview layout. The Activity translates targets into
 * actual Views, so focus is never tracked by a second mutable zone/index state.
 */
internal object HomeFocusNavigator {
    enum class Zone { TOP, SIDEBAR, CONTENT }
    enum class Direction { UP, DOWN, LEFT, RIGHT }

    sealed class Target {
        object Stay : Target()
        object Framework : Target()
        object Top : Target()
        data class Sidebar(val index: Int) : Target()
        object Content : Target()
    }

    fun next(zone: Zone, direction: Direction, index: Int, count: Int, contentAvailable: Boolean): Target {
        val selected = index.coerceIn(0, (count - 1).coerceAtLeast(0))
        return when (zone) {
            Zone.TOP -> when (direction) {
                Direction.UP -> Target.Stay
                Direction.DOWN -> if (count > 0) Target.Sidebar(selected) else Target.Stay
                Direction.LEFT, Direction.RIGHT -> Target.Framework
            }
            Zone.SIDEBAR -> {
                if (count == 0) return Target.Stay
                when (direction) {
                    Direction.UP -> if (selected == 0) Target.Top else Target.Sidebar(selected - 1)
                    Direction.DOWN -> if (selected == count - 1) Target.Stay else Target.Sidebar(selected + 1)
                    Direction.LEFT -> Target.Stay
                    Direction.RIGHT -> if (contentAvailable) Target.Content else Target.Stay
                }
            }
            Zone.CONTENT -> when (direction) {
                Direction.LEFT -> if (count > 0) Target.Sidebar(selected) else Target.Stay
                Direction.UP -> Target.Top
                Direction.DOWN, Direction.RIGHT -> Target.Stay
            }
        }
    }
}
