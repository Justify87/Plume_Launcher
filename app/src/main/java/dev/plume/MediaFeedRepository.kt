package dev.plume

import java.util.Collections

/** Pure, Android-free selection rules for both shared Overview rows and individual app pages. */
internal object MediaFeedRules {
    /**
     * Android TV Watch Next: 0=continue, 1=next, 2=new, 3=watchlist.
     * An app preview channel without a type does not imply that an item was started.
     */
    fun classify(watchNextType: Long?, playbackPositionMillis: Long? = null, continueChannel: Boolean = false): MediaCategory =
        when (watchNextType) {
            0L -> MediaCategory.CONTINUE
            2L -> MediaCategory.NEW
            1L, 3L -> MediaCategory.RECOMMENDATION
            else -> if (continueChannel || (playbackPositionMillis ?: 0L) >= 1000L)
                MediaCategory.CONTINUE else MediaCategory.UNKNOWN
        }

    /**
     * Prefer a provider content ID, then its intent, then the index inside this particular source.
     * Last fallback stays stable only while source order stays stable. Do not dedupe by title.
     * Length-prefix every component so separators inside IDs and intents cannot collide.
     */
    fun stableId(source: String, pkg: String?, contentId: String?, intentUri: String?, index: Int): String {
        require(index >= 0 && source.isNotBlank())
        fun part(value: String) = "${value.length}:$value"
        val local = when {
            !contentId.isNullOrBlank() -> "content:$contentId"
            !intentUri.isNullOrBlank() -> "intent:$intentUri"
            else -> "row:$index"
        }
        return "media:${part(source)}:${part(pkg.orEmpty())}:${part(local)}"
    }

    /** Match the same app and content/intent; never collapse distinct series on their title alone. */
    fun dedupe(items: List<MediaItem>): List<MediaItem> {
        fun part(s: String) = "${s.length}:$s"
        val seen = HashSet<String>()
        return items.filter { item ->
            val owner = item.sourcePackage?.takeIf { it.isNotBlank() } ?: "unknown:${item.stableId}"
            val key = when {
                !item.contentId.isNullOrBlank() -> "content:${part(owner)}:${part(item.contentId)}"
                !item.intentUri.isNullOrBlank() -> "intent:${part(owner)}:${part(item.intentUri)}"
                else -> "row:${part(item.stableId)}"
            }
            seen.add(key)
        }
    }

    /** Some TV providers report seconds in a millisecond column. Keep unknown values null. */
    fun timestampMillis(value: Long?): Long? = when {
        value == null || value <= 0 -> null
        value < 100_000_000_000L -> value * 1000L
        else -> value
    }

    private fun categoryOrder(category: MediaCategory) = when (category) {
        MediaCategory.CONTINUE -> 0
        MediaCategory.NEW -> 1
        MediaCategory.RECOMMENDATION -> 2
        MediaCategory.UNKNOWN -> 3
    }

    /**
     * Recent rows first, ties by provider order and stable ID. Each app gets up to [perApp] front
     * places before its overflow. Apply this separately to each category so a very active provider
     * does not bury another app, while single-app pages retain every item.
     */
    fun sort(items: List<MediaItem>, perApp: Int = 3): List<MediaItem> {
        require(perApp > 0)
        val ordered = items.sortedWith(
            compareBy<MediaItem> { categoryOrder(it.category) }
                .thenByDescending { timestampMillis(it.engagedAtMillis ?: it.publishedAtMillis) ?: 0L }
                .thenBy { it.sourceOrder }
                .thenBy { it.stableId },
        )
        return ordered.groupBy { it.category }.entries
            .sortedBy { categoryOrder(it.key) }
            .flatMap { (_, categoryItems) ->
                val seen = HashMap<String, Int>()
                val (front, rest) = categoryItems.partition { item ->
                    val owner = item.sourcePackage?.takeIf { it.isNotBlank() } ?: "source:${item.sourceId}"
                    seen.merge(owner, 1, Int::plus)!! <= perApp
                }
                front + rest
            }
    }
}

/**
 * An in-memory snapshot of independent sources. There is no querying, caching of images, service,
 * polling, or Android dependency here. Call [replaceSource] on the existing TV query executor.
 */
internal class MediaFeedRepository {
    private val lock = Any()
    private val sources = LinkedHashMap<String, List<MediaItem>>()

    fun replaceSource(sourceId: String, items: List<MediaItem>) {
        require(sourceId.isNotBlank())
        require(items.all { it.sourceId == sourceId })
        // Copy both collection layers so no caller can change a published snapshot later.
        val copy = items.map { it.copy(packageAliases = it.packageAliases.toSet()) }
        synchronized(lock) {
            if (copy.isEmpty()) sources.remove(sourceId) else sources[sourceId] = copy
        }
    }

    /** Null filters mean all sources, apps, or categories. Caller receives an unmodifiable list. */
    fun snapshot(
        sourceId: String? = null,
        packageName: String? = null,
        category: MediaCategory? = null,
    ): List<MediaItem> {
        val selected = synchronized(lock) {
            sources.entries.asSequence()
                .filter { sourceId == null || it.key == sourceId }
                .flatMap { it.value.asSequence() }
                .filter { category == null || it.category == category }
                .filter { packageName == null || packageName == it.sourcePackage || packageName in it.packageAliases }
                .toList()
        }
        return Collections.unmodifiableList(MediaFeedRules.sort(MediaFeedRules.dedupe(selected)))
    }
}
