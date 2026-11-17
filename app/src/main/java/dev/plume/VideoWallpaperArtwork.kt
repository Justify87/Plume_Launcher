package dev.plume

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.File

/** Worker-thread video frame reads; playback Surface remains owned by VideoWallpaper. */
internal object VideoWallpaperArtwork {
    fun poster(video: File, cacheDir: File, displayWidth: Int, displayHeight: Int): Bitmap? {
        val cache = File(cacheDir, "poster-${video.name}-${video.length()}-${video.lastModified()}.jpg")
        if (cache.isFile) BitmapFactory.decodeFile(cache.path)?.let { return it }
        val r = android.media.MediaMetadataRetriever()
        return try {
            r.setDataSource(video.path)
            val at = android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            val frame = if (Build.VERSION.SDK_INT >= 27) r.getScaledFrameAtTime(0, at, displayWidth, displayHeight)
                else r.getFrameAtTime(0, at)?.let { f -> // Android 8.0 has no scaled read: a 4K frame is 33 MB, shrink it
                    val w = displayWidth
                    if (f.width <= w) f else Bitmap.createScaledBitmap(f, w, maxOf(1, f.height * w / f.width), true).also { f.recycle() }
                }
            frame?.also { f ->
                cacheDir.listFiles { f2 -> f2.name.startsWith("poster-") }?.forEach { it.delete() } 
                cache.outputStream().use { f.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            }
        } catch (e: Exception) {
            null
        } finally {
            r.release()
        }
    }

    fun frames(video: File, w: Int, h: Int): List<Bitmap> {
        val r = android.media.MediaMetadataRetriever()
        return try {
            r.setDataSource(video.path)
            val ms = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val at = android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            (0L..ms step 4000L).take(30).mapNotNull { t ->
                val f = if (Build.VERSION.SDK_INT >= 27) r.getScaledFrameAtTime(t * 1000, at, w * 4, h * 4) else r.getFrameAtTime(t * 1000, at)
                f?.let { Bitmap.createScaledBitmap(it, w, h, true).also { s -> if (s !== it) it.recycle() } }
            }
        } catch (e: Exception) {
            emptyList()
        } finally {
            r.release()
        }
    }

}
