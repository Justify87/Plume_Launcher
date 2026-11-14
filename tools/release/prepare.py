#!/usr/bin/env python3
"""Prepare a tagged Plume release from the current main checkout.

The workflow runs this only in its temporary checkout, builds and signs the APK,
then pushes the resulting release commit if the remote main SHA has not changed.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
VERSION = re.compile(r"^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(?:-(beta|rc)([1-9][0-9]*))?$")
NAME_LINE = re.compile(r'(?m)^(\s*versionName\s*=\s*")([^"]+)(".*)$')
CODE_LINE = re.compile(r"(?m)^([ \t]*versionCode[ \t]*=[ \t]*)([0-9]+)([ \t]*)$")
UNRELEASED = re.compile(r"(?m)^## Unreleased[ \t]*$")
HEADING = re.compile(r"(?m)^## [^\n]+$")


def version_key(name: str) -> tuple[int, ...]:
    match = VERSION.fullmatch(name)
    if match is None:
        raise ValueError("Use a version such as 1.8.0, 1.8.0-beta1 or 1.8.0-rc1")
    major, minor, patch, stage, number = match.groups()
    # Stable is after beta and rc at the same numeric version.
    kind = {"beta": 0, "rc": 1, None: 2}[stage]
    return int(major), int(minor), int(patch), kind, int(number or 0)


def read_version(build: str) -> tuple[str, int]:
    names = NAME_LINE.findall(build)
    codes = CODE_LINE.findall(build)
    if len(names) != 1 or len(codes) != 1:
        raise ValueError("Expected exactly one versionName and versionCode in app/build.gradle.kts")
    return names[0][1], int(codes[0][1])


def prepare(root: Path, version: str, notes_output: Path | None = None) -> int:
    requested = version_key(version)
    build_path = root / "app/build.gradle.kts"
    changelog_path = root / "CHANGELOG.md"
    build = build_path.read_text(encoding="utf-8")
    current_name, current_code = read_version(build)
    if requested < version_key(current_name):
        raise ValueError(f"New version {version} must not be lower than {current_name}")
    if current_code < 1 or current_code >= 2_100_000_000:
        raise ValueError("Invalid/out-of-range Android versionCode")

    changelog = changelog_path.read_text(encoding="utf-8")
    if requested == version_key(current_name):
        # Supported for main branches protected by required PR checks: prepare
        # version/changelog in a reviewed PR, then publish this exact commit.
        # Also permits recovering an interrupted publish after the main push.
        heading = re.search(rf"(?m)^## {re.escape(version)}[ \t]*$", changelog)
        if heading is None:
            raise ValueError(f"Source is already v{version}, but changelog heading is missing")
        next_heading = HEADING.search(changelog, heading.end())
        notes = changelog[heading.end():next_heading.start() if next_heading else None].strip()
        if not any(line.startswith("- ") and line[2:].strip() for line in notes.splitlines()):
            raise ValueError(f"Release section ## {version} needs at least one bullet")
        if notes_output is not None:
            notes_output.parent.mkdir(parents=True, exist_ok=True)
            notes_output.write_text(f"# Plume Launcher Advanced v{version}\n\n{notes}\n", encoding="utf-8")
        return current_code
    markers = list(UNRELEASED.finditer(changelog))
    if len(markers) != 1:
        raise ValueError("CHANGELOG.md must contain exactly one ## Unreleased section")
    start = markers[0].end()
    next_heading = HEADING.search(changelog, start)
    if next_heading is None:
        raise ValueError("Missing previous version heading after ## Unreleased")
    notes = changelog[start:next_heading.start()].strip()
    if not any(line.startswith("- ") and line[2:].strip() for line in notes.splitlines()):
        raise ValueError("Unreleased changelog must have at least one release-note bullet")
    if re.search(rf"(?m)^## {re.escape(version)}[ \t]*$", changelog):
        raise ValueError(f"Release heading ## {version} already exists")

    # Preserve all Unreleased notes under the new version, keep a fresh empty
    # Unreleased section, and preserve every older release unchanged.
    updated_changelog = (
        changelog[:markers[0].start()]
        + f"## Unreleased\n\n## {version}\n\n{notes}\n\n"
        + changelog[next_heading.start():]
    )
    next_code = current_code + 1
    updated_build, count_name = NAME_LINE.subn(
        lambda m: f'{m.group(1)}{version}{m.group(3)}', build
    )
    updated_build, count_code = CODE_LINE.subn(
        lambda m: f'{m.group(1)}{next_code}{m.group(3)}', updated_build
    )
    if count_name != 1 or count_code != 1:
        raise ValueError("Could not uniquely update both Gradle version fields")

    if notes_output is not None:
        notes_output.parent.mkdir(parents=True, exist_ok=True)
        notes_output.write_text(f"# Plume Launcher Advanced v{version}\n\n{notes}\n", encoding="utf-8")

    build_path.write_text(updated_build, encoding="utf-8")
    changelog_path.write_text(updated_changelog, encoding="utf-8")
    return next_code


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--version", required=True)
    parser.add_argument("--notes-output", type=Path)
    args = parser.parse_args()
    code = prepare(ROOT, args.version, args.notes_output)
    print(f"Prepared Plume Launcher Advanced v{args.version} (Android versionCode {code})")


if __name__ == "__main__":
    main()
