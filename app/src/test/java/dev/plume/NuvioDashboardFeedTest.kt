package dev.plume

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** M02 pure-JVM contract; provider access and the real Shield remain separate gates. */
class NuvioDashboardFeedTest {
    private fun record(
        section: NuvioSection,
        providerId: String? = "show_s1e2",
        season: String? = "1",
        episode: String? = "2",
        title: String = "Series",
        sourceOrder: Int = 0,
        watched: Long? = null,
        release: String? = null,
        duration: Long? = null,
        position: Long? = null,
        percent: Int? = null,
        sourcePkg: String = "com.nuvio.tv",
        intent: String? = null,
        description: String? = null,
        image: String? = null,
    ): NuvioFeedEntry {
        val kind = when (section) {
            NuvioSection.IN_PROGRESS -> NuvioChannelKind.IN_PROGRESS
            NuvioSection.AVAILABLE -> NuvioChannelKind.AVAILABLE
            NuvioSection.UPCOMING -> NuvioChannelKind.UPCOMING
        }
        return NuvioFeedEntry(AndroidTvProgram(
            item = MediaItem(
                stableId = "tv:${section.name}:$sourceOrder:${providerId.orEmpty()}",
                sourceId = "tv", sourcePackage = sourcePkg, title = title,
                intentUri = intent, imageUri = image, sourceOrder = sourceOrder,
                engagedAtMillis = watched, durationMillis = duration,
                playbackPositionMillis = position, progressPercent = percent,
            ),
            watchNext = false,
            internalProviderId = providerId,
            seasonNumber = season,
            episodeNumber = episode,
            episodeTitle = "Episode",
            releaseDate = release,
            shortDescription = description,
        ), kind)
    }

    private fun feed(
        progress: List<NuvioFeedEntry> = emptyList(),
        available: List<NuvioFeedEntry> = emptyList(),
        upcoming: List<NuvioFeedEntry> = emptyList(),
    ) = NuvioMediaProjector.project(NuvioProviderFeeds(progress, available, upcoming))

    @Test fun fixedOrderAndHonestEmptySections() {
        val sections = feed().sections()
        assertEquals(listOf(NuvioSection.IN_PROGRESS, NuvioSection.AVAILABLE,
            NuvioSection.UPCOMING), sections.map { it.section })
        assertEquals(listOf(0, 0, 0), sections.map { it.count })
        assertTrue(sections.all { it.items.isEmpty() })
    }

    @Test fun fullVisibleCountsNotMountedThreeOrEightCards() {
        val all = (0 until 15).map {
            record(NuvioSection.AVAILABLE, providerId = "show_${it}_s1e2", sourceOrder = it)
        }
        val result = feed(available = all)
        assertEquals(15, result.sections()[1].count)
        assertEquals(15, result.available.size)
    }

    @Test fun priorityIsWatchTimeThenProviderWeightThenUpcomingCivilDate() {
        val result = feed(
            progress = listOf(
                record(NuvioSection.IN_PROGRESS, "older", watched = 1L, sourceOrder = 0),
                record(NuvioSection.IN_PROGRESS, "newer", watched = 9L, sourceOrder = 2),
                record(NuvioSection.IN_PROGRESS, "tie", watched = 9L, sourceOrder = 1),
            ),
            available = listOf(
                record(NuvioSection.AVAILABLE, "second", sourceOrder = 2, watched = 999999L),
                record(NuvioSection.AVAILABLE, "first", sourceOrder = 0, watched = 1L),
            ),
            upcoming = listOf(
                record(NuvioSection.UPCOMING, "unknown", release = null, sourceOrder = 0),
                record(NuvioSection.UPCOMING, "later", release = "2027-03-01", sourceOrder = 1),
                record(NuvioSection.UPCOMING, "sooner", release = "2026-12-20", sourceOrder = 2),
            ),
        )
        assertEquals(listOf("tie", "newer", "older"),
            result.inProgress.map { it.stableId.substringAfter("content:").substringBefore(":s") }
                .map { it.substringAfter(":") })
        assertEquals(listOf(0, 2), result.available.map { it.providerOrder })
        assertEquals(listOf(LocalDate.of(2026, 12, 20), LocalDate.of(2027, 3, 1), null),
            result.upcoming.map { it.releaseDate })
    }

    @Test fun crossChannelDuplicateStartedWinsAndWatchNextPrefixNormalizes() {
        val result = feed(
            progress = listOf(record(NuvioSection.IN_PROGRESS, "wn_shared_s1e2", watched = 8L)),
            available = listOf(record(NuvioSection.AVAILABLE, "shared_s1e2")),
            upcoming = listOf(record(NuvioSection.UPCOMING, "shared_s1e2")),
        )
        assertEquals(1, result.inProgress.size)
        assertTrue(result.available.isEmpty())
        assertTrue(result.upcoming.isEmpty())
    }

    @Test fun distinctEpisodesAndShowsSurviveEvenWhenTitlesMatch() {
        val result = feed(available = listOf(
            record(NuvioSection.AVAILABLE, "series_s1e1", episode = "1", title = "Pilot"),
            record(NuvioSection.AVAILABLE, "series_s1e2", episode = "2", title = "Pilot"),
            record(NuvioSection.AVAILABLE, "other_s1e1", episode = "1", title = "Pilot"),
            record(NuvioSection.AVAILABLE, "series_s2e1", season = "2", episode = "1", title = "Pilot"),
        ))
        assertEquals(4, result.available.size)
        assertEquals(4, result.available.map { it.stableId }.toSet().size)
    }

    @Test fun titleAndGenericIntentAndRowIdAloneCannotCrossDedupe() {
        val generic = "intent:#Intent;package=com.nuvio.tv;end"
        val result = feed(available = listOf(
            record(NuvioSection.AVAILABLE, providerId = null, title = "Pilot", intent = generic),
            record(NuvioSection.AVAILABLE, providerId = null, title = "Pilot", intent = generic, sourceOrder = 1),
        ))
        assertEquals(2, result.available.size)
    }

    @Test fun recognizedIntentContentIdIsCrossChannelEvidence() {
        val intent = "intent:#Intent;S.contentId=tt123;S.contentType=series;end"
        val result = feed(
            available = listOf(record(NuvioSection.AVAILABLE, null, intent = intent)),
            upcoming = listOf(record(NuvioSection.UPCOMING, null, intent = intent)),
        )
        assertEquals(1, result.available.size)
        assertTrue(result.upcoming.isEmpty())
    }

    @Test fun percentOnlyPublisherDurationNeverClaimsMinutes() {
        val result = feed(progress = listOf(record(NuvioSection.IN_PROGRESS,
            duration = 100_000L, position = 35_000L, percent = 35)))
        val item = result.inProgress.single()
        assertEquals(35, item.progressPercent)
        assertNull(item.durationMillis)
        assertNull(item.playbackPositionMillis)
        assertNull(item.remainingMillis)
    }

    @Test fun realDurationAndPositionYieldValidProgressAndRemaining() {
        val item = feed(progress = listOf(record(NuvioSection.IN_PROGRESS,
            duration = 2_400_000L, position = 600_000L))).inProgress.single()
        assertEquals(25, item.progressPercent)
        assertEquals(2_400_000L, item.durationMillis)
        assertEquals(600_000L, item.playbackPositionMillis)
        assertEquals(1_800_000L, item.remainingMillis)
    }

    @Test fun unknownAndImpossibleTimingRemainsUnknown() {
        val unknown = feed(progress = listOf(record(NuvioSection.IN_PROGRESS))).inProgress.single()
        assertNull(unknown.progressPercent)
        assertNull(unknown.durationMillis)
        assertNull(unknown.remainingMillis)
        val impossible = feed(progress = listOf(record(NuvioSection.IN_PROGRESS,
            duration = 20_000L, position = 21_000L))).inProgress.single()
        assertNull(impossible.progressPercent)
        assertNull(impossible.remainingMillis)
        assertNull(impossible.playbackPositionMillis)
    }

    @Test fun descriptionIsNotAnAlertFlagAndMissingArtIsNotFilteredOut() {
        val item = feed(available = listOf(record(NuvioSection.AVAILABLE,
            description = "New Episode", image = null, release = "2026-11-12"))).available.single()
        assertFalse(item.showNewBadge)
        assertEquals("New Episode", item.shortDescription)
        assertNull(item.artworkUri)
        assertNull(item.posterUri)
        assertNull(item.logoUri)
    }

    @Test fun strictCivilDateNeverFallsBackToEngagementTimestamp() {
        val entries = listOf(
            record(NuvioSection.UPCOMING, "future", release = "2028-02-29"),
            record(NuvioSection.UPCOMING, "bad", release = "2027-02-29", watched = 1_800_000_000_000L),
            record(NuvioSection.UPCOMING, "isoInstant", release = "2028-02-29T00:00:00Z"),
            record(NuvioSection.UPCOMING, "missing", release = null, watched = 1_800_000_000_000L),
        )
        val items = feed(upcoming = entries).upcoming
        assertEquals(LocalDate.of(2028, 2, 29), items.first().releaseDate)
        assertEquals(3, items.count { it.releaseDate == null })
        assertTrue(items.drop(1).all { it.releaseDate == null })
    }

    @Test fun todayAndTimezoneSwitchAtActualLocalMidnight() {
        val release = "2026-11-14"
        val item = feed(upcoming = listOf(record(NuvioSection.UPCOMING, release = release))).upcoming.single()
        val moment = Instant.parse("2026-11-13T23:30:00Z")
        assertEquals(0L, item.daysUntilRelease(moment, ZoneId.of("Europe/Berlin")))
        assertEquals(1L, item.daysUntilRelease(moment, ZoneId.of("America/Los_Angeles")))
        assertNull(item.daysUntilRelease(Instant.parse("2026-11-15T12:00:00Z"), ZoneId.of("Europe/Berlin")))
        assertEquals(Instant.parse("2026-11-14T23:00:00Z"),
            NuvioMediaProjector.nextLocalDayBoundary(moment, ZoneId.of("Europe/Berlin")))
    }

    @Test fun leapDayAndDstBoundaryDoNotAssume24HourDays() {
        val leap = feed(upcoming = listOf(record(NuvioSection.UPCOMING,
            release = "2028-02-29"))).upcoming.single()
        val now = Instant.parse("2028-02-28T23:30:00Z")
        assertEquals(0L, leap.daysUntilRelease(now, ZoneId.of("Europe/Berlin")))
        assertEquals(1L, leap.daysUntilRelease(now, ZoneId.of("America/Los_Angeles")))
        assertEquals(Instant.parse("2026-03-29T22:00:00Z"),
            NuvioMediaProjector.nextLocalDayBoundary(
                Instant.parse("2026-03-28T23:30:00Z"), ZoneId.of("Europe/Berlin")))
    }

    @Test fun nonNuvioEntriesCannotEnterThreeFeeds() {
        assertTrue(feed(available = listOf(record(NuvioSection.AVAILABLE,
            sourcePkg = "org.smarttube.beta"))).available.isEmpty())
    }
}
