package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Test

class VDesignCanvasTest {
    @Test fun newReferenceCanvasHasNarrowRailAndHero() {
        assertEquals(98, VDesignCanvas.SIDEBAR_WIDTH)
        assertEquals(VDesignCanvas.Placement(0, 0, 98, 864),
            VDesignCanvas.place(1536, 864, VDesignCanvas.Box(0, 0, 98, 864)))
        assertEquals(VDesignCanvas.Placement(108, 145, 655, 365),
            VDesignCanvas.place(1536, 864, VDesignCanvas.Box(108, 145, 655, 365)))
    }

    @Test fun proportionalTvCanvasDoesNotAssumeDpEqualsReferencePixels() {
        assertEquals(VDesignCanvas.Placement(0, 0, 61, 540),
            VDesignCanvas.place(960, 540, VDesignCanvas.Box(0, 0, 98, 864)))
        assertEquals(VDesignCanvas.Placement(68, 91, 409, 228),
            VDesignCanvas.place(960, 540, VDesignCanvas.Box(108, 145, 655, 365)))
    }

    @Test fun fullHdAndInsetTvViewportStayUnstretched() {
        assertEquals(VDesignCanvas.Placement(0, 0, 123, 1080),
            VDesignCanvas.place(1920, 1080, VDesignCanvas.Box(0, 0, 98, 864)))
        assertEquals(VDesignCanvas.Placement(135, 181, 819, 456),
            VDesignCanvas.place(1920, 1080, VDesignCanvas.Box(108, 145, 655, 365)))
        assertEquals(VDesignCanvas.Placement(32, 0, 114, 1008),
            VDesignCanvas.place(1856, 1008, VDesignCanvas.Box(0, 0, 98, 864)))
        assertEquals(VDesignCanvas.Placement(158, 169, 764, 426),
            VDesignCanvas.place(1856, 1008, VDesignCanvas.Box(108, 145, 655, 365)))
    }

    @Test fun typographyAndGeometryShareTheInsetViewportScale() {
        assertEquals(0.625f, VDesignCanvas.scale(960, 540), .0001f)
        assertEquals(1.25f, VDesignCanvas.scale(1920, 1080), .0001f)
        assertEquals(1.1666666f, VDesignCanvas.scale(1856, 1008), .0001f)
        assertEquals(0f, VDesignCanvas.scale(1920, 0), .0001f)
    }

    @Test fun unusualInsetsKeepCanvasCentered() {
        assertEquals(VDesignCanvas.Placement(120, 0, 61, 540),
            VDesignCanvas.place(1200, 540, VDesignCanvas.Box(0, 0, 98, 864)))
        assertEquals(VDesignCanvas.Placement(0, 0, 0, 0),
            VDesignCanvas.place(0, 540, VDesignCanvas.Box(0, 0, 98, 864)))
    }
}
