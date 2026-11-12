#!/usr/bin/env bash
# Builds ONE OSS/Prod APK using Mullvad's original app host and original Rust/JNI.
set -euo pipefail
cd "$(dirname "$0")/../.."
python3 tools/mullvad/prepare.py
source_gradle=app/build.gradle.kts
version_code=$(sed -n 's/^[[:space:]]*versionCode = \([0-9][0-9]*\).*$/\1/p' "$source_gradle" | head -n 1)
version_name=$(sed -n 's/^[[:space:]]*versionName = "\([^"]*\)".*$/\1/p' "$source_gradle" | head -n 1)
test -n "$version_code" && test -n "$version_name"
cd .build/plume-mullvad/mullvadvpn-app/android
# Target one ABI for Shield. Expand only after checking the actual device ABI.
./gradlew :app:assembleOssProdDebug \
  "-Pmullvad.app.config.override.versionCode=$version_code" \
  "-Pmullvad.app.config.override.versionName=$version_name" \
  "-Pmullvad.app.build.cargo.targets=arm64" \
  "-Pmullvad.app.build.cargo.cleanBuild=false" \
  "$@"
