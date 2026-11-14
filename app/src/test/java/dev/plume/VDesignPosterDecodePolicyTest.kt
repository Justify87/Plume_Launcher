package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class VDesignPosterDecodePolicyTest {
    @Test fun ordinaryPosterKeepsUsefulResolution() {
        assertEquals(4, VDesignPosterDecodePolicy.sampleSize(1920, 1080, 350))
        assertEquals(1, VDesignPosterDecodePolicy.sampleSize(300, 450, 350))
    }

    @Test fun extremelyTallArtworkIsSampledToABoundedDimension() {
        assertEquals(16, VDesignPosterDecodePolicy.sampleSize(300, 30000, 350))
        assertEquals(32, VDesignPosterDecodePolicy.sampleSize(20000, 100, 350))
    }

    @Test fun invalidBoundsAndTinyTargetsDoNotLoopForever() {
        assertEquals(1, VDesignPosterDecodePolicy.sampleSize(0, 1000, 350))
        assertEquals(1, VDesignPosterDecodePolicy.sampleSize(300, -1, 350))
        assertEquals(1024, VDesignPosterDecodePolicy.sampleSize(1024, 1024, 0))
    }

    @Test fun inputSizeIsExplicitlyBounded() {
        assertEquals(8_388_608, VDesignPosterDecodePolicy.MAX_BYTES)
    }
}
