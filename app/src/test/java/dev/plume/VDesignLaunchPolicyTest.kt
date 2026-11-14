package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VDesignLaunchPolicyTest {
    private val p = VDesignLaunchPolicy
    @Test fun episodePlaybackAndDetailFallback() {
        assertEquals(VDesignLaunchPolicy.Mode.STREAM, p.mode(NuvioSection.IN_PROGRESS, "show", "series", "vid1", "Show", "stream"))
        assertEquals(VDesignLaunchPolicy.Mode.STREAM, p.mode(NuvioSection.AVAILABLE, "show", "series", "vid2", "Show", "stream"))
        assertEquals(VDesignLaunchPolicy.Mode.DETAIL, p.mode(NuvioSection.AVAILABLE, "show", "series", "", "Show", "stream"))
        assertEquals(VDesignLaunchPolicy.Mode.DETAIL, p.mode(NuvioSection.IN_PROGRESS, "film", "movie", "vid", "Film", "detail"))
    }
    @Test fun upcomingNeverAutoplays() {
        assertEquals(VDesignLaunchPolicy.Mode.DETAIL, p.mode(NuvioSection.UPCOMING, "show", "series", "video", "Show", "stream"))
        assertEquals(VDesignLaunchPolicy.Mode.UNAVAILABLE, p.mode(NuvioSection.UPCOMING, null, "series", "video", "Show", "stream"))
    }
    @Test fun explicitComponentNoUntrustedSchemesOrSelectors() {
        fun safe(compPkg: String? = "com.nuvio.tv", cls: String? = "com.nuvio.tv.MainActivity",
            declared: String? = null, action: String? = "android.intent.action.VIEW",
            scheme: String? = null, selector: Boolean = false) =
            p.safeComponent("com.nuvio.tv", compPkg, cls, declared, action, scheme, selector)
        assertTrue(safe())
        assertFalse(safe(compPkg = "evil.app"))
        assertFalse(safe(cls = "evil.app.MainActivity"))
        assertFalse(safe(declared = "evil.app"))
        assertFalse(safe(action = "android.intent.action.SEND"))
        assertFalse(safe(scheme = "file"))
        assertFalse(safe(selector = true))
    }
}
