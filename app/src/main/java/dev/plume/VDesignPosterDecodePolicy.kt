package dev.plume

internal object VDesignPosterDecodePolicy {
    const val MAX_BYTES = 8 * 1024 * 1024
    private const val MAX_DECODED_DIMENSION = 2048

    fun sampleSize(width: Int, height: Int, targetWidth: Int): Int {
        if (width <= 0 || height <= 0) return 1
        val target = targetWidth.coerceAtLeast(1)
        var sample = 1
        while (sample < (1 shl 20) &&
            (width / sample / 2 >= target || height / sample > MAX_DECODED_DIMENSION)) {
            sample *= 2
        }
        return sample
    }
}
