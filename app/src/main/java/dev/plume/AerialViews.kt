package dev.plume

import android.content.Context
import android.media.MediaPlayer
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File

/**
 * The Aerial Views screensaver: Apple's aerial scenes, from the same list and the same downloaded files as the
 * video wallpaper (Plume's wallpaper folder), so a scene is downloaded once and serves both. The dream never
 * downloads; it plays the scenes already on disk. Scene list adapted from the Aerial Views project (GPL-3.0).
 */
internal const val AERIAL_SCREENSAVER = "aerial"
internal const val APPLE_AERIAL = "Apple Aerial"

internal fun aerialScenes(c: Context) = videoScenes(c).filter { it.source == APPLE_AERIAL }

internal fun aerialFiles(c: Context): List<File> {
    val dir = c.getExternalFilesDir("wallpapers") ?: return emptyList()
    return aerialScenes(c).map { File(dir, it.file) }.filter { it.isFile }
}

internal class AerialShow(context: Context, files: List<File>) : SurfaceHolder.Callback {
    private val files = files.shuffled()
    private var index = 0
    private var player: MediaPlayer? = null
    private var ready = false
    private var failures = 0 
    val view = SurfaceView(context).also { it.holder.addCallback(this) }

    override fun surfaceCreated(holder: SurfaceHolder) { ready = true; play() }
    override fun surfaceDestroyed(holder: SurfaceHolder) { ready = false; release() }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    fun step(by: Int) {
        index = (index + by).mod(files.size)
        play()
    }

    private fun play() {
        release()
        if (!ready) return
        try {
            player = MediaPlayer().also { p ->
                p.setOnPreparedListener { failures = 0; it.isLooping = files.size == 1; it.setVolume(0f, 0f); it.start() }
                p.setOnCompletionListener { step(1) }
                p.setOnErrorListener { _, _, _ -> view.post { skipBroken() }; true }
                p.setDataSource(files[index].path)
                p.setDisplay(view.holder)
                p.prepareAsync()
            }
        } catch (_: Exception) {
            skipBroken()
        }
    }

    private fun skipBroken() {
        release()
        if (++failures < files.size) step(1)
    }

    fun release() { player?.release(); player = null }
}
