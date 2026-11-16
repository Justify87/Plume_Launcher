#!/usr/bin/env python3
"""Validate release signing configuration before the expensive Android/Mullvad build.

Checks only for the manually configured *persistent* Android release key.
Never generates or substitutes a transient key, and never prints secret values.
"""
from __future__ import annotations

import base64
import binascii
import hashlib
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile

REQUIRED = {
    "KEYSTORE_B64": "PLUME_RELEASE_KEYSTORE_BASE64 (Actions secret)",
    "PLUME_STORE_PASSWORD": "PLUME_RELEASE_KEYSTORE_PASSWORD (Actions secret)",
    "PLUME_KEY_ALIAS": "PLUME_RELEASE_KEY_ALIAS (Actions secret)",
    "PLUME_KEY_PASSWORD": "PLUME_RELEASE_KEY_PASSWORD (Actions secret)",
    "EXPECTED_CERT": "PLUME_RELEASE_CERT_SHA256 (Actions variable)",
}
DOCS = "docs/RELEASING.md"
MAX_KEYSTORE_BYTES = 8 * 1024 * 1024


class SigningPreflightError(Exception):
    """Friendly public diagnostic without disclosing signing material."""


def normalized_fingerprint(value: str) -> str:
    normalized = value.upper().replace(":", "").replace(" ", "").strip()
    if re.fullmatch(r"[0-9A-F]{64}", normalized) is None:
        raise SigningPreflightError(
            "PLUME_RELEASE_CERT_SHA256 must be a 64-hex-digit SHA-256 certificate "
            "fingerprint (colons are optional)."
        )
    return normalized


def validate_configuration(environ: dict[str, str]) -> str:
    missing = [
        display
        for key, display in REQUIRED.items()
        if not environ.get(key, "").strip()
    ]
    if missing:
        raise SigningPreflightError(
            "Missing persistent Android signing configuration:\n  - "
            + "\n  - ".join(missing)
            + f"\nSet these under repository Settings > Secrets and variables > Actions. "
            + f"Follow {DOCS}. Do not upload signing keys to the repository."
        )
    return normalized_fingerprint(environ["EXPECTED_CERT"])


def decode_keystore(value: str) -> bytes:
    try:
        encoded = "".join(value.split()).encode("ascii")
        data = base64.b64decode(encoded, validate=True)
    except (UnicodeError, binascii.Error) as exc:
        raise SigningPreflightError(
            "PLUME_RELEASE_KEYSTORE_BASE64 must contain the base64-encoded keystore file."
        ) from exc
    if not data or len(data) > MAX_KEYSTORE_BYTES:
        raise SigningPreflightError(
            "PLUME_RELEASE_KEYSTORE_BASE64 decoded to an empty or oversized file."
        )
    return data


def validate_keystore(environ: dict[str, str], expected: str) -> None:
    key_bytes = decode_keystore(environ["KEYSTORE_B64"])
    with tempfile.TemporaryDirectory(prefix="plume-release-key-") as work:
        keystore = Path(work) / "release.p12"
        keystore.write_bytes(key_bytes)
        keystore.chmod(0o600)
        env = dict(os.environ)
        env["PLUME_STORE_PASSWORD"] = environ["PLUME_STORE_PASSWORD"]
        env["PLUME_KEY_PASSWORD"] = environ["PLUME_KEY_PASSWORD"]
        common = [
            "-alias", environ["PLUME_KEY_ALIAS"], "-keystore", str(keystore),
            "-storepass:env", "PLUME_STORE_PASSWORD",
        ]
        try:
            cert = subprocess.run(
                ["keytool", "-exportcert", *common],
                stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
                env=env, check=True,
            ).stdout
        except (OSError, subprocess.CalledProcessError) as exc:
            raise SigningPreflightError(
                "Cannot open the release keystore or alias. Check the keystore "
                "base64, store password and key alias."
            ) from exc

        if not cert or hashlib.sha256(cert).hexdigest().upper() != expected:
            raise SigningPreflightError(
                "Signing certificate SHA-256 does not match "
                "PLUME_RELEASE_CERT_SHA256. Do not change keys unintentionally."
            )
        try:
            listing = subprocess.run(
                ["keytool", "-J-Duser.language=en", "-J-Duser.country=US",
                 "-list", "-keystore", str(keystore),
                 "-storepass:env", "PLUME_STORE_PASSWORD"],
                stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
                env=env, check=True,
            ).stdout.decode("utf-8", errors="replace")
        except (OSError, subprocess.CalledProcessError) as exc:
            raise SigningPreflightError(
                "Unable to inspect the keystore type. Check the configured keystore."
            ) from exc
        if re.search(r"(?m)^Keystore type:\s*PKCS12\s*$", listing):
            if environ["PLUME_KEY_PASSWORD"] != environ["PLUME_STORE_PASSWORD"]:
                raise SigningPreflightError(
                    "PKCS12 uses the same entry and keystore password: "
                    "PLUME_RELEASE_KEY_PASSWORD must equal "
                    "PLUME_RELEASE_KEYSTORE_PASSWORD."
                )
        try:
            subprocess.run(
                ["keytool", "-certreq", *common, "-keypass:env",
                 "PLUME_KEY_PASSWORD", "-file", str(Path(work) / "test.csr")],
                stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
                env=env, check=True,
            )
        except (OSError, subprocess.CalledProcessError) as exc:
            raise SigningPreflightError(
                "The release key cannot sign. Check the key alias and "
                "PLUME_RELEASE_KEY_PASSWORD."
            ) from exc


def main() -> int:
    try:
        expected = validate_configuration(os.environ)
        validate_keystore(os.environ, expected)
    except SigningPreflightError as exc:
        for line in str(exc).splitlines():
            print(f"::error::{line}", file=sys.stderr)
        return 1
    print("Release signing preflight OK: persistent keystore, private key and "
          "expected certificate fingerprint verified.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
