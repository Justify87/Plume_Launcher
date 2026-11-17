package dev.plume

import android.content.ContentResolver
import android.database.Cursor
import android.media.tv.TvContract
import android.util.Log

/** Owns only the TV channel query and cursor lifetime; the Activity owns refresh timing. */
internal object TvPreviewChannelSource {
    fun read(
        resolver: ContentResolver,
        launcherPackage: String,
        reportFailure: Boolean = false,
        onQueryFailure: () -> Unit = {},
    ): List<TvPreviewChannel> = try {
        val cursor = resolver.query(TvContract.Channels.CONTENT_URI, null, null, null, null)
        if (cursor == null && reportFailure) Log.w("Plume", "TV channels provider returned null")
        cursor?.use { extract(it, launcherPackage, reportFailure) } ?: emptyList()
    } catch (e: Exception) {
        if (reportFailure) {
            onQueryFailure()
            Log.w("Plume", "TV channel discovery unavailable", e)
        }
        emptyList()
    }

    private fun extract(c: Cursor, launcherPackage: String, reportFailure: Boolean): List<TvPreviewChannel> {
        val out = ArrayList<TvPreviewChannel>()
        val name = c.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)
        val pkg = c.getColumnIndex(TvContract.Channels.COLUMN_PACKAGE_NAME)
        val id = c.getColumnIndex(TvContract.Channels._ID)
        val provider = c.getColumnIndex(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_ID)
        val browsable = c.getColumnIndex(TvContract.Channels.COLUMN_BROWSABLE)
        if (name >= 0 && pkg >= 0 && id >= 0) {
            while (c.moveToNext()) {
                val p = if (c.isNull(pkg)) null else c.getString(pkg)
                val n = if (c.isNull(name)) null else c.getString(name)
                if (p.isNullOrBlank() || n == null || p == launcherPackage || c.isNull(id)) continue
                out += TvPreviewChannel(
                    c.getLong(id), p, n,
                    if (provider < 0 || c.isNull(provider)) null else c.getString(provider),
                    if (browsable < 0 || c.isNull(browsable)) null else c.getInt(browsable) == 1,
                )
            }
        } else if (reportFailure) {
            Log.w("Plume", "TV channels provider missing required columns")
        }
        return out
    }
}
