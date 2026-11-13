package dev.plume

/** Pure policy for typed Nuvio preview programs: future episodes never stream. */
internal object VDesignLaunchPolicy {
    enum class Mode { STREAM, DETAIL, UNAVAILABLE }

    fun mode(section: NuvioSection, contentId: String?, contentType: String?,
        videoId: String?, name: String?, publisherMode: String?): Mode {
        if (contentId.isNullOrBlank() || contentType !in setOf("series", "movie")) return Mode.UNAVAILABLE
        if (section == NuvioSection.UPCOMING) return Mode.DETAIL
        return if (publisherMode == "stream" && !videoId.isNullOrBlank() && !name.isNullOrBlank())
            Mode.STREAM else Mode.DETAIL
    }

    fun safeComponent(sourcePackage: String, componentPackage: String?, componentClass: String?,
        declaredPackage: String?, action: String?, dataScheme: String?, hasSelector: Boolean): Boolean =
        sourcePackage.isNotBlank() && componentPackage == sourcePackage &&
            componentClass == "com.nuvio.tv.MainActivity" &&
            (declaredPackage == null || declaredPackage == sourcePackage) &&
            (action == null || action == "android.intent.action.VIEW") &&
            dataScheme == null && !hasSelector
}
