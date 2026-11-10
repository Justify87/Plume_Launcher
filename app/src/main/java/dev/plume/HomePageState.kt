package dev.plume

/**
 * The page shown on the right is separate from whichever View currently owns D-pad focus.
 * Only installed, visible sidebar apps can become app pages.
 */
internal sealed class HomePageState {
    object Overview : HomePageState()
    object AllApps : HomePageState()
    data class App(val packageName: String) : HomePageState() {
        init { require(packageName.isNotBlank()) }
    }

    /** Back returns from an app preview only; the launcher handles Home separately. */
    fun backTarget(): HomePageState? = if (this is App) Overview else null

    val key: String get() = when (this) {
        Overview -> HomeSidebarView.OVERVIEW
        AllApps -> HomeSidebarView.ALL_APPS
        is App -> packageName
    }

    companion object {
        fun fromSidebar(key: String, visiblePackages: Set<String>): HomePageState = when {
            key == HomeSidebarView.OVERVIEW -> Overview
            key == HomeSidebarView.ALL_APPS -> AllApps
            key in visiblePackages -> App(key)
            else -> Overview // An app can disappear while the launcher is open.
        }
    }
}

internal data class HomeMediaSection(val category: MediaCategory, val items: List<MediaItem>)

/** Projection only: the shared repository remains the sole owner of all TV media records. */
internal object HomePageContent {
    fun sections(feed: MediaFeedRepository, page: HomePageState): List<HomeMediaSection> {
        if (page == HomePageState.AllApps) return emptyList()
        val pkg = (page as? HomePageState.App)?.packageName
        val items = feed.snapshot(sourceId = "tv", packageName = pkg)
        return MediaCategory.values().mapNotNull { category ->
            val group = items.filter { it.category == category }
            if (group.isEmpty()) null else HomeMediaSection(category, group)
        }
    }

    data class FocusTarget(val row: Int, val index: Int)

    /** A stable item wins; if gone, choose its former category and nearest surviving card. */
    fun restoreFocus(
        sections: List<HomeMediaSection>,
        category: MediaCategory?,
        stableId: String?,
        index: Int,
    ): FocusTarget? {
        if (stableId != null) {
            for ((row, section) in sections.withIndex()) {
                val found = section.items.indexOfFirst { it.stableId == stableId }
                if (found >= 0) return FocusTarget(row, found)
            }
        }
        if (category != null) {
            val row = sections.indexOfFirst { it.category == category && it.items.isNotEmpty() }
            if (row >= 0) return FocusTarget(row, index.coerceIn(0, sections[row].items.lastIndex))
        }
        val first = sections.indexOfFirst { it.items.isNotEmpty() }
        return if (first >= 0) FocusTarget(first, 0) else null
    }
}
