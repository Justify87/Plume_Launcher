package dev.plume

/**
 * Pure navigation contract for the single persistent Android TV workspace.
 *
 * Focusing a rail destination selects its right-hand content immediately.
 * OK is reserved for actions inside that content (or launching an installed app).
 * The rail must never be replaced by an overlay or a separate in-launcher Activity.
 */
internal object LauncherWorkspaceNavigation {
    enum class Destination { HOME, NUVIO, SMARTTUBE, SPOTIFY, ALL_APPS, SETTINGS, VPN }
    enum class Action { SHOW_CONTENT, LAUNCH_APP, FOCUS_CONTENT, GO_HOME, NO_OP }

    data class State(
        val destination: Destination = Destination.HOME,
        val railFocused: Boolean = true,
        val contentFocusKey: String? = null,
    )

    fun destinationForKey(key: String): Destination = when (key) {
        VDesignHomeView.NUVIO -> Destination.NUVIO
        VDesignHomeView.SMARTTUBE -> Destination.SMARTTUBE
        VDesignHomeView.SPOTIFY -> Destination.SPOTIFY
        VDesignHomeView.ALL_APPS -> Destination.ALL_APPS
        VDesignHomeView.SETTINGS -> Destination.SETTINGS
        VDesignHomeView.VPN -> Destination.VPN
        else -> Destination.HOME
    }

    fun keyForDestination(destination: Destination): String = when (destination) {
        Destination.HOME -> VDesignHomeView.OVERVIEW
        Destination.NUVIO -> VDesignHomeView.NUVIO
        Destination.SMARTTUBE -> VDesignHomeView.SMARTTUBE
        Destination.SPOTIFY -> VDesignHomeView.SPOTIFY
        Destination.ALL_APPS -> VDesignHomeView.ALL_APPS
        Destination.SETTINGS -> VDesignHomeView.SETTINGS
        Destination.VPN -> VDesignHomeView.VPN
    }

    fun focusRail(state: State, destination: Destination): State =
        state.copy(destination = destination, railFocused = true, contentFocusKey = null)

    fun right(state: State, hasFocusableContent: Boolean): State =
        if (hasFocusableContent) state.copy(railFocused = false) else state

    fun back(state: State): State = when {
        !state.railFocused -> state.copy(railFocused = true)
        state.destination != Destination.HOME -> State()
        else -> state
    }

    fun actionOnOk(destination: Destination): Action = when (destination) {
        Destination.NUVIO, Destination.SMARTTUBE, Destination.SPOTIFY -> Action.LAUNCH_APP
        Destination.HOME, Destination.ALL_APPS, Destination.SETTINGS, Destination.VPN ->
            Action.FOCUS_CONTENT
    }

    fun focusAction(old: State, newDestination: Destination): Action =
        if (old.destination == newDestination) Action.NO_OP else Action.SHOW_CONTENT
}

/** Avoid launching a different installed variant of Nuvio than the channel provider. */
internal object NuvioInstallationPolicy {
    fun preferredPackage(installed: Collection<String>, feedOwner: String?): String? {
        if (feedOwner != null && feedOwner in installed &&
            AndroidTvMediaSource.isNuvioPackage(feedOwner)) return feedOwner
        val candidates = installed.filter(AndroidTvMediaSource::isNuvioPackage)
        return candidates.singleOrNull()
    }
}

internal object WorkspaceMediaLaunchPolicy {
    fun valid(owner: String?, componentPackage: String?, declaredPackage: String?,
        hasSelector: Boolean, dataScheme: String?): Boolean {
        if (owner.isNullOrBlank() || (componentPackage == null && declaredPackage == null))
            return false
        if (componentPackage != null && componentPackage != owner) return false
        if (declaredPackage != null && declaredPackage != owner) return false
        if (hasSelector || dataScheme in setOf("file", "content")) return false
        return true
    }
}
