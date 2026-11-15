package dev.plume

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal object Bing {
    const val CHOICE = "bing"

    private const val WIDTH = 1920
    private const val HEIGHT = 1080

    @Volatile
    var onChanged: (() -> Unit)? = null

    private val main = Handler(Looper.getMainLooper())

    private val worker = ThreadPoolExecutor(0, 1, 5, TimeUnit.SECONDS, LinkedBlockingQueue())
    private val inFlight = AtomicBoolean(false)
    private val lock = Any() // a download finishing and a wallpaper being dropped never overlap

    fun file(c: Context) = File(c.filesDir, "bing.jpg")

    private fun used(prefs: SharedPreferences) = prefs.getString("wp_light", null) == CHOICE || prefs.getString("wp_dark", null) == CHOICE

    /**
     * Looks up the picture if a wallpaper uses it and the picture has not changed today: a minute apart while there is
     * no picture yet, an hour apart otherwise. Bing changes the picture at a set time for each country, not at local
     * midnight, so a lookup that finds yesterday's picture does not end the day. [force] looks it up now (the picture
     * was just chosen).
     */
    fun refresh(c: Context, prefs: SharedPreferences, force: Boolean = false) {
        if (!used(prefs)) return
        val today = LocalDate.now().toEpochDay()
        val now = System.currentTimeMillis()
        val retry = if (file(c).exists()) 3_600_000L else 60_000L
        if (!force && (prefs.getLong("bing_day", 0) == today || now - prefs.getLong("bing_try", 0) < retry)) return
        prefs.edit().putLong("bing_try", now).apply()
        if (!inFlight.compareAndSet(false, true)) return
        worker.execute {
            try {
                val token = download(c, prefs.getString("bing_hash", null))
                synchronized(lock) {
                    if (!used(prefs)) file(c).delete() 
                    else if (token != null) { 
                        prefs.edit().putLong("bing_day", today).putString("bing_hash", token).apply()
                        main.post { onChanged?.invoke() }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("Plume", "Bing picture failed ($e)") 
            } finally {
                inFlight.set(false)
            }
        }
    }

    fun dropIfUnused(c: Context, prefs: SharedPreferences) = synchronized(lock) {
        if (used(prefs)) return@synchronized
        file(c).delete()
        prefs.edit().remove("bing_hash").remove("bing_day").remove("bing_try").apply()
        if (!inFlight.get()) File(c.filesDir, "bing.part").delete()
    }

    private fun download(c: Context, have: String?): String? {
        val l = Locale.getDefault()
        val market = if (l.country.isEmpty()) "en-US" else "${l.language}-${l.country}"
        val image = JSONObject(connect("https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&uhd=1&uhdwidth=$WIDTH&uhdheight=$HEIGHT&mkt=$market")
            .inputStream.use { it.readBytes().decodeToString() }).getJSONArray("images").getJSONObject(0)
        val path = image.getString("url")
        if (!path.startsWith("/")) throw IOException("unexpected picture address") 
        val token = image.getString("hsh")
        if (token == have && file(c).exists()) return null
        val part = File(c.filesDir, "bing.part")
        try {
            val conn = connect("https://www.bing.com$path")
            val copied = conn.inputStream.use { input -> part.outputStream().use { input.copyTo(it) } }
            if (conn.contentEncoding == null && conn.contentLengthLong.let { it >= 0 && it != copied }) throw IOException("cut short")
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(part.path, bounds)
            if (bounds.outWidth <= 0 || !part.renameTo(file(c))) throw IOException("not a picture")
        } finally {
            part.delete() 
        }
        return token
    }

    private fun connect(url: String) = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 8000
        readTimeout = 20000
    }
}
