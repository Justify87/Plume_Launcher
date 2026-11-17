package dev.plume

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.LruCache
import android.widget.ImageView
import java.io.InputStream
import java.util.Collections
import java.util.concurrent.Executors

/**
 * Owns the existing poster pool, inflight URI set and cache together.
 * View updates remain on the launcher's UI handler, guarded by the original URI tag.
 */
internal class MediaPosterLoader(
    private val ui: Handler,
    private val openStream: (String) -> InputStream,
    private val targetSizePx: () -> Int,
    private val isActive: () -> Boolean,
    private val artworkReady: (String, Bitmap) -> Unit,
) {
    private val io = Executors.newFixedThreadPool(3)
    private val loading = Collections.synchronizedSet(HashSet<String>())
    private val posters = object : LruCache<String, Bitmap>(6 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    fun load(view: ImageView, uri: String?) {
        if (uri == null) return
        posters.get(uri)?.let { view.setImageBitmap(it); return }
        view.tag = uri
        if (!loading.add(uri)) return
        io.execute {
            val bitmap = try {
                decode(uri)
            } catch (error: Exception) {
                android.util.Log.w("TvHome", "poster failed: $uri ($error)")
                null
            } finally {
                loading.remove(uri)
            } ?: return@execute
            posters.put(uri, bitmap)
            ui.post {
                if (isActive()) {
                    if (view.tag == uri) view.setImageBitmap(bitmap)
                    artworkReady(uri, bitmap)
                }
            }
        }
    }

    fun evictAll() = posters.evictAll()

    fun shutdownNow() {
        io.shutdownNow()
    }

    private fun decode(uri: String): Bitmap? {
        val bytes = openStream(uri).use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(16 * 1024)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read == 0) continue
                total += read
                if (total > VDesignPosterDecodePolicy.MAX_BYTES) return null
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val sample = VDesignPosterDecodePolicy.sampleSize(
            bounds.outWidth, bounds.outHeight, targetSizePx())
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }
}
