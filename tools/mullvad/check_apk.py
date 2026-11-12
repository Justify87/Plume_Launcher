#!/usr/bin/env python3
"""Fail when the integrated APK lacks its native VPN or bundled relay list."""
import sys
import zipfile

apk = sys.argv[1] if len(sys.argv) == 2 else None
if not apk:
    sys.exit("usage: check_apk.py <integrated.apk>")
with zipfile.ZipFile(apk) as package:
    members = set(package.namelist())
    required = {
        "AndroidManifest.xml",
        "classes.dex",
        "lib/arm64-v8a/libmullvad_jni.so",
        "assets/relays.json",
    }
    missing = required - members
    if missing:
        sys.exit(f"APK is missing mandatory entries: {sorted(missing)}")
    print(f"APK structure OK ({len(members)} entries; Rust/JNI and relay assets present)")
