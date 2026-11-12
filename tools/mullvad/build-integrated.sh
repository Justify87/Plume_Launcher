#!/usr/bin/env bash
# Build one Plume+Mullvad APK using Mullvad's pinned Android build container.
# Matches upstream's /build, /cargo-target, cargo-registry and Gradle cache layout.
set -euo pipefail
cd "$(dirname "$0")/../.."
repo_root="$PWD"
python3 tools/mullvad/prepare.py
version_code=$(sed -n 's/^[[:space:]]*versionCode = \([0-9][0-9]*\).*$/\1/p' app/build.gradle.kts | head -n 1)
version_name=$(sed -n 's/^[[:space:]]*versionName = "\([^"]*\)".*$/\1/p' app/build.gradle.kts | head -n 1)
test -n "$version_code" && test -n "$version_name"
staged="$repo_root/.build/plume-mullvad/mullvadvpn-app"
image=$(tr -d '\r\n' < "$staged/building/android-container-image.txt")
if [[ ! "$image" =~ ^ghcr\.io/mullvad/mullvadvpn-app-build-android@sha256:[[:xdigit:]]{64}$ ]]; then
  echo "Unexpected original Mullvad build image: $image" >&2
  exit 1
fi
runner="${CONTAINER_RUNNER:-docker}"
command -v "$runner" >/dev/null || { echo "$runner is required" >&2; exit 1; }
echo "Building in pinned Mullvad Android image $image"
export GRADLE_OPTS='-Dorg.gradle.jvmargs=-Xmx3g -Dkotlin.daemon.jvm.options=-Xmx2g'
"$runner" run --rm \
  -v "$staged:/build:Z" \
  -v plume-mullvad-cargo-target:/cargo-target:Z \
  -v plume-mullvad-cargo-registry:/root/.cargo/registry:Z \
  -v plume-mullvad-gradle-cache:/root/.gradle:Z \
  -e GRADLE_OPTS -e CARGO_BUILD_JOBS=2 -w /build "$image" \
  ./android/gradlew -p android --console plain --stacktrace --no-daemon \
  :app:assembleOssProdDebug \
  "-Pmullvad.app.config.override.versionCode=$version_code" \
  "-Pmullvad.app.config.override.versionName=$version_name" \
  "-Pmullvad.app.build.cargo.targets=arm64" \
  "-Pmullvad.app.build.cargo.cleanBuild=false" "$@"
