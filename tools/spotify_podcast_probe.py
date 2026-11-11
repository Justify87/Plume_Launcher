#!/usr/bin/env python3
"""Read-only Spotify podcast feasibility probe. No OAuth implementation or stored tokens.

Run with a temporary user token (scopes: user-library-read user-read-playback-position):
    SPOTIFY_ACCESS_TOKEN=... python3 tools/spotify_podcast_probe.py
Never commit or share the token.
"""
import datetime as dt
import json
import os
import re
import sys
import urllib.error
import urllib.request
from dataclasses import dataclass
from typing import Any

SPOTIFY_ID = re.compile(r"[A-Za-z0-9]{22}\Z")
MAX_BODY = 256 * 1024
BASE = "https://api.spotify.com/v1"


@dataclass(frozen=True)
class ProbeResult:
    state: str
    reason: str


class SpotifyApiError(Exception):
    def __init__(self, status: int):
        self.status = status
        super().__init__(f"HTTP {status}")


class SpotifyOfflineError(Exception):
    pass


def episode_id(value: Any) -> str | None:
    return value if isinstance(value, str) and SPOTIFY_ID.fullmatch(value) else None


def links(value: Any) -> tuple[str, str] | None:
    identifier = episode_id(value)
    if identifier is None:
        return None
    return f"spotify:episode:{identifier}", f"https://open.spotify.com/episode/{identifier}"


def in_progress(episode: Any) -> bool:
    """Only an explicit positive, unfinished resume point qualifies."""
    if not isinstance(episode, dict):
        return False
    resume = episode.get("resume_point")
    if not isinstance(resume, dict) or resume.get("fully_played") is not False:
        return False
    position = resume.get("resume_position_ms")
    duration = episode.get("duration_ms")
    if type(position) is not int or position <= 0:
        return False
    return not (type(duration) is int and duration > 0 and position >= duration)


def recently_published(episode: Any, today: dt.date, window_days: int = 30) -> bool:
    """Recent release is NOT proof of being unheard."""
    if not isinstance(episode, dict) or episode.get("release_date_precision") != "day":
        return False
    try:
        published = dt.date.fromisoformat(episode["release_date"])
    except (KeyError, TypeError, ValueError):
        return False
    return 0 <= (today - published).days <= window_days


class _NoRedirects(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


class SpotifyApi:
    """Small, bounded read-only client; never follows a redirect with a bearer token."""
    def __init__(self, token: str):
        if not token.strip():
            raise ValueError("Missing temporary user access token")
        self.token = token
        self.opener = urllib.request.build_opener(_NoRedirects)

    def get(self, path: str) -> dict:
        if not (path in ("/me/shows?limit=3", "/me/episodes?limit=10") or
                re.fullmatch(r"/shows/[A-Za-z0-9]{22}/episodes\?limit=10", path)):
            raise ValueError("Probe endpoint not allowlisted")
        request = urllib.request.Request(BASE + path, headers={
            "Authorization": "Bearer " + self.token,
            "Accept": "application/json",
            "User-Agent": "Plume-M10-readonly-probe",
        })
        try:
            with self.opener.open(request, timeout=7) as response:
                if response.status != 200:
                    raise SpotifyApiError(response.status)
                data = response.read(MAX_BODY + 1)
                if len(data) > MAX_BODY:
                    raise ValueError("Spotify response exceeds probe limit")
        except urllib.error.HTTPError as exc:
            raise SpotifyApiError(exc.code) from None
        except (urllib.error.URLError, TimeoutError, OSError):
            raise SpotifyOfflineError() from None
        result = json.loads(data)
        if not isinstance(result, dict):
            raise ValueError("Unexpected Spotify response format")
        return result


def check_one(api: Any, path: str) -> tuple[ProbeResult, dict | None]:
    try:
        payload = api.get(path)
        if not isinstance(payload.get("items"), list):
            return ProbeResult("unsupported", "No items array"), None
        return ProbeResult("ok", f"{len(payload['items'])} entries (limited sample)"), payload
    except SpotifyApiError as exc:
        meanings = {401: "Expired/invalid authorization",
                    403: "Access denied (scope/allowlist/app restrictions)",
                    429: "Rate/quota limit reached"}
        return ProbeResult("blocked", f"HTTP {exc.status}: {meanings.get(exc.status, 'API unavailable')}"), None
    except SpotifyOfflineError:
        return ProbeResult("offline", "Network unavailable or timed out"), None
    except (ValueError, json.JSONDecodeError):
        return ProbeResult("unsupported", "Invalid or unsupported API response"), None


def run(api: Any, out=print, today: dt.date | None = None) -> bool:
    today = today or dt.datetime.now(dt.timezone.utc).date()
    shows_status, shows = check_one(api, "/me/shows?limit=3")
    saved_status, saved = check_one(api, "/me/episodes?limit=10")
    out(f"Saved shows: {shows_status.state} — {shows_status.reason}")
    out(f"Saved episodes: {saved_status.state} — {saved_status.reason}")
    if shows is None or saved is None:
        out("STOP: No reliable full-access proof; do not enable a Spotify feed.")
        return False
    show_ids = [identifier for item in shows["items"] if isinstance(item, dict)
                if isinstance(item.get("show"), dict)
                if (identifier := episode_id(item["show"].get("id"))) is not None]
    episodes: list[dict] = []
    for item in saved["items"]:
        if isinstance(item, dict) and isinstance(item.get("episode"), dict):
            episodes.append(item["episode"])
    for identifier in show_ids:
        status, payload = check_one(api, f"/shows/{identifier}/episodes?limit=10")
        out(f"Show episodes (sample): {status.state} — {status.reason}")
        if payload is None:
            return False
        episodes.extend(item for item in payload["items"] if isinstance(item, dict))

    deduped = {item["id"]: item for item in episodes if episode_id(item.get("id")) is not None}
    started = [item for item in deduped.values() if in_progress(item)]
    fresh = [item for item in deduped.values() if recently_published(item, today)]
    out(f"Explicit unfinished resume points in sample: {len(started)}")
    out(f"Episodes with publication date in last 30 days: {len(fresh)} (NOT proof of unheard status)")
    out("Listening chronology: unavailable from Recently Played for podcasts")
    chosen = next(iter(started or fresh), None)
    if chosen is not None:
        uri, https = links(chosen["id"])
        out("Deep-link candidates for MANUAL Shield verification (not playback proof):")
        out(f"  {uri}")
        out(f"  {https}")
    else:
        out("Deep-link test needs a real episode ID; none in the sample")
    if not show_ids:
        out("Show episodes endpoint NOT EXERCISED (no saved show in sample)")
    out("Shield app routing and exact-position resume: NOT TESTED by this API probe")
    out("This tests a limited API sample, NOT full M10 feature approval")
    return True


def main() -> int:
    token = os.environ.get("SPOTIFY_ACCESS_TOKEN", "")
    if not token:
        print("No SPOTIFY_ACCESS_TOKEN set; no network request made.")
        print("Supply a temporary user token with user-library-read and user-read-playback-position.")
        return 2
    return 0 if run(SpotifyApi(token)) else 1


if __name__ == "__main__":
    sys.exit(main())
