package dev.plume

import android.content.Context
import java.io.File

internal object ShowsWall {
    const val CHOICE = "shows"

    fun file(c: Context) = File(c.filesDir, "shows.jpg")

    fun wide(thumbnail: Boolean, ratio: Int) = thumbnail || ratio in 0..2

    /** What the picture itself must be: at least 480 px wide, between 1.2:1 and 2.5:1. Checked on the bytes, as the ratio an app gives can be wrong. */
    fun fits(w: Int, h: Int) = w >= 480 && h > 0 && w * 10 >= h * 12 && w * 10 <= h * 25

    fun order(keys: List<String>, current: String?): List<String> {
        val at = keys.indexOf(current)
        return if (at < 0) keys else keys.drop(at + 1) + keys.take(at + 1)
    }

    /** How long a picture that can never be used (not found, not wide, too big) is left alone: 6 hours. */
    const val DEAD_MS = 6 * 3_600_000L

    const val NETWORK_MS = 5 * 60_000L

    fun skipped(failedUntil: Map<String, Long>, key: String, now: Long) = (failedUntil[key] ?: 0L) > now
}
