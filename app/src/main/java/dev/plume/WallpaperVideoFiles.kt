package dev.plume

import java.io.File

/** Lists and prunes wallpaper video files without owning a Surface or an upload server. */
internal object WallpaperVideoFiles {
    fun personal(dir: File?, scenes: List<VideoScene>): List<File> =
        personal(dir?.listFiles { file -> file.extension.equals("mp4", true) }
            ?.sortedBy { it.name }.orEmpty(), scenes)

    fun personal(files: List<File>, scenes: List<VideoScene>): List<File> =
        files.filterNot { file -> scenes.any { it.file == file.name } }

    fun cleanUnused(
        dir: File,
        scenes: List<VideoScene>,
        screensaverFiles: Set<String>,
        light: String?,
        dark: String?,
    ) {
        val catalogFiles = scenes.map { it.file }.toSet() - screensaverFiles
        dir.listFiles { file -> file.extension.equals("mp4", true) }?.forEach { file ->
            val choice = "video:${file.name}"
            if (file.name in catalogFiles && choice != light && choice != dark) {
                file.delete()
            }
        }
    }
}
