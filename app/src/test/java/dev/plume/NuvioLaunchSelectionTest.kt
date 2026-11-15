package dev.plume

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NuvioLaunchSelectionTest {
    @Test fun prefersProductionOverLoggedOutDebugVariantRegardlessOfOrder() {
        val packages = listOf("com.nuviodebug.com", "com.nuvio.tv")
        assertEquals("com.nuvio.tv", NuvioLaunchSelection.select(packages, null))
        assertEquals("com.nuvio.tv", NuvioLaunchSelection.select(packages.reversed(), null))
    }
    @Test fun explicitUserChoiceIsRespectedButNeverLaunchesUninstalledVariant() {
        val packages = listOf("com.nuviodebug.com", "com.nuvio.tv")
        assertEquals("com.nuviodebug.com",
            NuvioLaunchSelection.select(packages, "com.nuviodebug.com"))
        assertEquals("com.nuvio.tv",
            NuvioLaunchSelection.select(packages, "com.nuvio.app"))
    }
    @Test fun emptyOrUntrustedInstalledPackagesNeverClaimNuvio() {
        assertNull(NuvioLaunchSelection.select(emptyList(), null))
        assertNull(NuvioLaunchSelection.select(listOf("com.evil.nuvio.tv"), "com.evil.nuvio.tv"))
        assertEquals("com.nuvio.app",
            NuvioLaunchSelection.select(listOf("com.nuvio.app"), null))
    }
}
