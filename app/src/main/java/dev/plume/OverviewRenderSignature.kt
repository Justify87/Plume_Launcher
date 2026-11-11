package dev.plume

/**
 * Identity of the current media layout. A provider refresh often returns exactly the same data;
 * never tear down focused rows just because the Activity requested another projection.
 * Titles are part of the signature so language/label updates can refresh the visible headings.
 */
internal data class OverviewRenderSignature(
    val pageKey: String,
    val sections: List<HomeMediaSection>,
    val labels: List<String>,
    val demoTag: String,
)
