package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class VDesignPosterDecodePolicyTest {
    @Test fun ordinaryPosterKeepsUsefulResolution() {
        assertEquals(2, VDesignPosterDecodePolicy.sampleSize(1920, 1080, 350))
        assertEquals(1, VDesignPosterDecodePolicy.sampleSize(300, 450, 350))
    }

    @Test fun extremelyTallArtworkIsSampledToABoundedDimension() {
        assertEquals(16, VDesignPosterDecodePolicy.sampleSize(300, 30000, 350))
        assertEquals(8, VDesignPosterDecodePolicy.sampleSize(20000, 100, 350))
    }

    @Test fun invalidBoundsAndTinyTargetsDoNotLoopForever() {
        assertEquals(1, VDesignPosterDecodePolicy.sampleSize(0, 1000, 350))
        assertEquals(1, VDesignPosterDecodePolicy.sampleSize(300, -1, 350))
        assertEquals(256, VDesignPosterDecodePolicy.sampleSize(100000, 100000, 0))
    }

    @Test fun inputSizeIsExplicitlyBounded() {
        assertEquals(8_388_608, VDesignPosterDecodePolicy.MAX_BYTES)
    }
}
