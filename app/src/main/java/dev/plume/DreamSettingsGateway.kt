package dev.plume

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

/** The existing Dream component and system-setting read/write boundary. */
internal class DreamSettingsGateway(private val context: Context) {
    private val resolver get() = context.contentResolver

    fun galleryComponent(): String = ComponentName(context, GalleryDream::class.java).flattenToString()

    fun currentComponent(): String = Settings.Secure.getString(resolver, "screensaver_components").orEmpty()

    fun enableGallery() {
        Settings.Secure.putString(resolver, "screensaver_components", galleryComponent())
        Settings.Secure.putInt(resolver, "screensaver_enabled", 1)
    }

    fun restoreComponent(previous: String) {
        Settings.Secure.putString(resolver, "screensaver_components", previous)
    }

    fun read(key: String): String? =
        if (key == Settings.System.SCREEN_OFF_TIMEOUT) Settings.System.getString(resolver, key)
        else Settings.Secure.getString(resolver, key)

    fun write(key: String, value: String?): Boolean =
        if (key == Settings.System.SCREEN_OFF_TIMEOUT) Settings.System.putString(resolver, key, value)
        else Settings.Secure.putString(resolver, key, value)
}
