#!/usr/bin/env python3
"""Check non-empty core assets and ARM64 ELF content in an integrated Mullvad APK."""
import json
import sys
import zipfile

REQUIRED = {
    "AndroidManifest.xml",
    "classes.dex",
    "lib/arm64-v8a/libmullvad_jni.so",
    "assets/relays.json",
}


def check_apk(path: str) -> int:
    with zipfile.ZipFile(path) as archive:
        members = set(archive.namelist())
        missing = REQUIRED - members
        if missing:
            raise ValueError(f"APK is missing mandatory entries: {sorted(missing)}")
        for name in sorted(REQUIRED):
            if archive.getinfo(name).file_size == 0:
                raise ValueError(f"APK has an empty mandatory entry: {name}")
        packaged_abis = {
            name.split("/")[1]
            for name in members
            if name.startswith("lib/") and name.endswith(".so")
        }
        unsupported = packaged_abis - {"arm64-v8a"}
        if unsupported:
            raise ValueError(
                f"APK advertises ABI(s) without the ARM64 Mullvad JNI: {sorted(unsupported)}"
            )
        with archive.open("lib/arm64-v8a/libmullvad_jni.so") as native:
            header = native.read(20)
        # ELF64, little-endian, e_machine = EM_AARCH64 (183).
        if (
            len(header) != 20
            or header[:4] != b"\x7fELF"
            or header[4] != 2
            or header[5] != 1
            or int.from_bytes(header[18:20], "little") != 183
        ):
            raise ValueError("Mullvad JNI is not a 64-bit ARM64 ELF library")
        with archive.open("assets/relays.json") as relays:
            try:
                json.load(relays)
            except (ValueError, UnicodeError) as error:
                raise ValueError("Mullvad relay asset is not valid JSON") from error
        return len(members)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit("usage: check_apk.py <integrated.apk>")
    try:
        count = check_apk(sys.argv[1])
    except (ValueError, OSError, zipfile.BadZipFile) as error:
        sys.exit(str(error))
    print(f"APK structure OK ({count} entries; ARM64 Rust/JNI ELF and JSON relay assets present)")
