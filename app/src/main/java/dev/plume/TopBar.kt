package dev.plume

internal object TopBar {
    fun parse(saved: String?, known: List<String>, default: List<String>, required: String): List<String> {
        val order = saved?.split(',')?.filter { it in known }?.distinct() ?: default
        return if (required in order) order else order + required
    }

    fun slide(order: List<String>, shown: List<String>, key: String, dir: Int): List<String> {
        val to = shown.indexOf(key) + dir
        if (key !in shown || to !in shown.indices) return order
        val other = shown[to]
        return order.map { if (it == key) other else if (it == other) key else it }
    }

    fun addAfter(order: List<String>, key: String, after: String): List<String> {
        val rest = order - key
        val at = rest.indexOf(after)
        return if (at < 0) rest + key else rest.take(at + 1) + key + rest.drop(at + 1)
    }
}
