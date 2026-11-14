package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class VDesignCanvasTest {
    @Test fun referenceCanvasPreservesExactSidebarAndHeroAnchors() {
        assertEquals(VDesignCanvas.Placement(0, 0, 218, 864),
            VDesignCanvas.place(1536, 864, VDesignCanvas.Box(0, 0, 218, 864)))
        assertEquals(VDesignCanvas.Placement(255, 74, 651, 262),
            VDesignCanvas.place(1536, 864, VDesignCanvas.Box(255, 74, 651, 262)))
    }

    @Test fun proportionalTvCanvasDoesNotAssumeDpEqualsReferencePixels() {
        assertEquals(VDesignCanvas.Placement(0, 0, 136, 540),
            VDesignCanvas.place(960, 540, VDesignCanvas.Box(0, 0, 218, 864)))
        assertEquals(VDesignCanvas.Placement(159, 46, 407, 164),
            VDesignCanvas.place(960, 540, VDesignCanvas.Box(255, 74, 651, 262)))
    }

    @Test fun fullHdAndInsetTvViewportKeepProportionalReferenceGeometry() {
        assertEquals(VDesignCanvas.Placement(0, 0, 273, 1080),
            VDesignCanvas.place(1920, 1080, VDesignCanvas.Box(0, 0, 218, 864)))
        assertEquals(VDesignCanvas.Placement(319, 93, 814, 328),
            VDesignCanvas.place(1920, 1080, VDesignCanvas.Box(255, 74, 651, 262)))
        assertEquals(VDesignCanvas.Placement(32, 0, 254, 1008),
            VDesignCanvas.place(1856, 1008, VDesignCanvas.Box(0, 0, 218, 864)))
        assertEquals(VDesignCanvas.Placement(330, 86, 760, 306),
            VDesignCanvas.place(1856, 1008, VDesignCanvas.Box(255, 74, 651, 262)))
    }

    @Test fun unusualInsetsOrAspectKeepCanvasUnstretched() {
        assertEquals(VDesignCanvas.Placement(120, 0, 136, 540),
            VDesignCanvas.place(1200, 540, VDesignCanvas.Box(0, 0, 218, 864)))
        assertEquals(VDesignCanvas.Placement(0, 0, 0, 0),
            VDesignCanvas.place(0, 540, VDesignCanvas.Box(0, 0, 218, 864)))
    }
}
