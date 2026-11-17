package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class WallpaperVideoFilesTest {
    private fun scene(file: String) = VideoScene("Apple Aerial", "City", file, file, "https://example.invalid/$file")

    private fun withDir(block: (File) -> Unit) {
        val dir = Files.createTempDirectory("plume-video-files").toFile()
        try { block(dir) } finally { dir.deleteRecursively() }
    }

    @Test fun ownVideosExcludeCatalogAndKeepFilenameSort() = withDir { dir ->
        listOf("scene.mp4", "z.mp4", "a.MP4", "readme.txt").forEach { File(dir, it).writeText("data") }
        val own = WallpaperVideoFiles.personal(dir, listOf(scene("scene.mp4")))
        assertEquals(listOf("a.MP4", "z.mp4"), own.map { it.name })
    }

    @Test fun pruningKeepsSelectedWallpaperAerialAndPersonalUploads() = withDir { dir ->
        val names = listOf("selected.mp4", "unused.mp4", "aerial.mp4", "my-video.mp4", "other.png")
        names.forEach { File(dir, it).writeText("data") }
        WallpaperVideoFiles.cleanUnused(
            dir, listOf(scene("selected.mp4"), scene("unused.mp4"), scene("aerial.mp4")),
            screensaverFiles = setOf("aerial.mp4"), light = "video:selected.mp4", dark = null,
        )
        assertTrue(File(dir, "selected.mp4").exists())
        assertTrue(File(dir, "aerial.mp4").exists())
        assertTrue(File(dir, "my-video.mp4").exists())
        assertTrue(File(dir, "other.png").exists())
        assertFalse(File(dir, "unused.mp4").exists())
    }

    @Test fun pruningPreservesBothDarkAndLightChoices() = withDir { dir ->
        listOf("light.mp4", "dark.mp4", "unused.mp4").forEach { File(dir, it).writeText("data") }
        WallpaperVideoFiles.cleanUnused(
            dir, listOf(scene("light.mp4"), scene("dark.mp4"), scene("unused.mp4")),
            screensaverFiles = emptySet(), light = "video:light.mp4", dark = "video:dark.mp4",
        )
        assertEquals(setOf("dark.mp4", "light.mp4"), dir.list()!!.toSet())
    }
}
