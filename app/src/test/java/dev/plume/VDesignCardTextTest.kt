package dev.plume

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VDesignCardTextTest {
    private fun item(
        season: Int? = 1, episode: Int? = 2, episodeTitle: String? = "Eine Folge",
        date: LocalDate? = null, duration: Long? = null, remaining: Long? = null,
        artwork: String? = null, poster: String? = null, logo: String? = null,
    ) = NuvioMediaItem(
        stableId = "episode:1", section = NuvioSection.IN_PROGRESS,
        sourcePackage = "com.nuvio.tv", title = "Ein echter Titel", seriesId = "series1",
        seasonNumber = season, episodeNumber = episode, episodeTitle = episodeTitle,
        shortDescription = null, longDescription = null, releaseDate = date,
        artworkUri = artwork, posterUri = poster, logoUri = logo, intentUri = null,
        progressPercent = null, durationMillis = duration, playbackPositionMillis = null,
        remainingMillis = remaining, lastWatchedMillis = null, providerOrder = 0,
        legacyFallback = false,
    )

    @Test fun episodeLabelsOmitUnknownSeasonAndNeverInventFilmEpisodes() {
        assertEquals("S1 · F2  Eine Folge", VDesignCardText.episode(item()))
        assertEquals("F2", VDesignCardText.episode(item(season = null, episodeTitle = null)))
        assertEquals("S1", VDesignCardText.episode(item(episode = null, episodeTitle = null)))
        assertNull(VDesignCardText.episode(item(null, null, null)))
    }

    @Test fun remainingIsShownOnlyWithRealDuration() {
        assertNull(VDesignCardText.remaining(item(remaining = 1_000)))
        assertNull(VDesignCardText.remaining(item(duration = 100_000, remaining = null)))
        assertEquals("2 min verbleibend",
            VDesignCardText.remaining(item(duration = 3_600_000, remaining = 61_000)))
        assertNull(VDesignCardText.remaining(item(duration = 3_600_000, remaining = 0)))
    }

    @Test fun civilReleaseDateAndMissingCountdownAreHonest() {
        val zone = ZoneId.of("Europe/Berlin")
        val now = Instant.parse("2026-11-13T23:15:00Z") // local November 14
        assertEquals("Heute", VDesignCardText.countdown(item(date = LocalDate.of(2026, 11, 14)), now, zone))
        assertEquals("in 1 Tag", VDesignCardText.countdown(item(date = LocalDate.of(2026, 11, 15)), now, zone))
        assertNull(VDesignCardText.countdown(item(date = LocalDate.of(2026, 11, 13)), now, zone))
        assertNull(VDesignCardText.countdown(item(), now, zone))
        assertNull(VDesignCardText.date(item(), Locale.GERMAN))
        assertTrue(VDesignCardText.date(item(date = LocalDate.of(2026, 11, 15)),
            Locale.GERMAN)!!.contains("Nov"))
    }

    @Test fun artworkSelectionRejectsFileSchemesAndUsesPosterFallback() {
        val i = item(artwork = "file:///private/data.jpg",
            poster = " https://example.org/scene.jpg ", logo = "content://provider/logo")
        assertEquals("https://example.org/scene.jpg", VDesignCardText.artwork(i))
        assertEquals("content://provider/logo",
            VDesignCardText.artwork(item(artwork = "javascript:alert(1)", logo = "content://provider/logo")))
        assertNull(VDesignCardText.artwork(item(artwork = "file:///sdcard/private", poster = "ftp://bad")))
        assertNull(VDesignCardText.supportedArtwork("https:///missing-host.jpg"))
        assertNull(VDesignCardText.supportedArtwork("https://user:pass@host.example/image.jpg"))
        assertNull(VDesignCardText.supportedArtwork("content:///missing-authority"))
    }
}
