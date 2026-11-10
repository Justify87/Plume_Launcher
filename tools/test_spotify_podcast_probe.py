"""Offline unit tests for the M10 read-only Spotify proof of concept."""
import datetime as dt
import unittest
from unittest.mock import patch
import spotify_podcast_probe as p

SHOW = "A" * 22
EPISODE = "B" * 22


class FakeApi:
    def __init__(self, responses):
        self.responses = responses
        self.visits = []

    def get(self, path):
        self.visits.append(path)
        item = self.responses[path]
        if isinstance(item, Exception):
            raise item
        return item


class SpotifyPodcastProbeTest(unittest.TestCase):
    def test_only_explicit_unfinished_position_is_continue(self):
        sample = {"resume_point": {"fully_played": False, "resume_position_ms": 15200},
                  "duration_ms": 1800000}
        self.assertTrue(p.in_progress(sample))
        self.assertFalse(p.in_progress({**sample, "resume_point": None}))
        self.assertFalse(p.in_progress({**sample, "resume_point": {
            "fully_played": True, "resume_position_ms": 15200}}))
        self.assertFalse(p.in_progress({**sample, "resume_point": {
            "fully_played": False, "resume_position_ms": 0}}))
        self.assertFalse(p.in_progress({**sample, "resume_point": {
            "fully_played": False, "resume_position_ms": 1800000}}))
        self.assertFalse(p.in_progress({**sample, "resume_point": {
            "fully_played": False, "resume_position_ms": True}}))

    def test_new_requires_genuine_day_release(self):
        today = dt.date(2026, 11, 11)
        self.assertTrue(p.recently_published({
            "release_date": "2026-11-10", "release_date_precision": "day"}, today))
        self.assertFalse(p.recently_published({
            "release_date": "2026-12-01", "release_date_precision": "day"}, today))
        self.assertFalse(p.recently_published({
            "release_date": "2025-11-10", "release_date_precision": "day"}, today))
        self.assertFalse(p.recently_published({
            "release_date": "2026-11", "release_date_precision": "month"}, today))
        self.assertFalse(p.recently_published({}, today))

    def test_episode_links_require_trusted_id(self):
        self.assertEqual((f"spotify:episode:{EPISODE}",
                          f"https://open.spotify.com/episode/{EPISODE}"), p.links(EPISODE))
        for invalid in (None, "", "episode", "a/../../x",
                        "http://attacker.test", "A" * 23):
            self.assertIsNone(p.links(invalid))

    def test_expired_denied_and_quota_limited_access(self):
        for status in (401, 403, 429):
            result, payload = p.check_one(
                FakeApi({"/me/shows?limit=3": p.SpotifyApiError(status)}),
                "/me/shows?limit=3")
            self.assertEqual("blocked", result.state)
            self.assertIsNone(payload)
            self.assertIn(str(status), result.reason)

    def test_offline_and_malformed_response(self):
        result, payload = p.check_one(
            FakeApi({"/me/shows?limit=3": p.SpotifyOfflineError()}),
            "/me/shows?limit=3")
        self.assertEqual("offline", result.state)
        self.assertIsNone(payload)
        result, payload = p.check_one(
            FakeApi({"/me/shows?limit=3": {"items": None}}),
            "/me/shows?limit=3")
        self.assertEqual("unsupported", result.state)
        self.assertIsNone(payload)

    def test_valid_empty_library_creates_no_fake_progress(self):
        fake = FakeApi({"/me/shows?limit=3": {"items": []},
                        "/me/episodes?limit=10": {"items": []}})
        lines = []
        self.assertTrue(p.run(fake, lines.append, dt.date(2026, 11, 11)))
        self.assertIn("Explicit unfinished resume points in sample: 0", lines)
        self.assertIn("Show episodes endpoint NOT EXERCISED (no saved show in sample)", lines)
        self.assertEqual(2, len(fake.visits))

    def test_valid_metadata_still_needs_shield_test(self):
        fake = FakeApi({
            "/me/shows?limit=3": {"items": [{"show": {"id": SHOW}}]},
            "/me/episodes?limit=10": {"items": []},
            f"/shows/{SHOW}/episodes?limit=10": {"items": [{
                "id": EPISODE, "duration_ms": 100000,
                "resume_point": {"fully_played": False, "resume_position_ms": 24000},
                "release_date": "2026-11-10", "release_date_precision": "day"}]},
        })
        lines = []
        self.assertTrue(p.run(fake, lines.append, dt.date(2026, 11, 11)))
        self.assertIn(f"  spotify:episode:{EPISODE}", lines)
        self.assertIn("Shield app routing and exact-position resume: NOT TESTED by this API probe", lines)

    def test_show_episode_denial_blocks_proof(self):
        fake = FakeApi({
            "/me/shows?limit=3": {"items": [{"show": {"id": SHOW}}]},
            "/me/episodes?limit=10": {"items": []},
            f"/shows/{SHOW}/episodes?limit=10": p.SpotifyApiError(403),
        })
        lines = []
        self.assertFalse(p.run(fake, lines.append))
        self.assertTrue(any("Show episodes (sample): blocked" in x for x in lines))

    def test_no_token_means_no_login_or_request(self):
        with patch.dict("os.environ", {}, clear=True):
            self.assertEqual(2, p.main())

    def test_probe_cannot_access_arbitrary_paths(self):
        api = p.SpotifyApi("temporary-test-token")
        with self.assertRaises(ValueError):
            api.get("/me?x=1")
        with self.assertRaises(ValueError):
            api.get("/shows/../../evil/episodes?limit=10")


if __name__ == "__main__":
    unittest.main()
