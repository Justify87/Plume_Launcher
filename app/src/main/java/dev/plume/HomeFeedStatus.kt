package dev.plume

/** Explain unavailable content without inventing watch history or new releases. */
internal enum class HomeFeedEmptyState {
    NONE, PERMISSION, PROVIDER_ERROR, OFFLINE, EMPTY
}

internal object HomeFeedStatus {
    fun emptyState(
        hasItems: Boolean,
        hasTvPermission: Boolean,
        queryFailed: Boolean,
        youtubeEnabled: Boolean,
        online: Boolean,
    ): HomeFeedEmptyState = when {
        hasItems -> HomeFeedEmptyState.NONE
        !hasTvPermission -> HomeFeedEmptyState.PERMISSION
        queryFailed -> HomeFeedEmptyState.PROVIDER_ERROR
        youtubeEnabled && !online -> HomeFeedEmptyState.OFFLINE
        else -> HomeFeedEmptyState.EMPTY
    }
}
