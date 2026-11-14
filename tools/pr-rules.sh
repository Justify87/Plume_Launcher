#!/bin/sh
# PR change review: flag dependency, permission and changelog changes for human review.
# The Plume+Mullvad fork intentionally has dependencies and VPN permissions; do not
# reject legitimate integration work solely because a new declaration was added.
set -eu

base=${1:?usage: pr-rules.sh BASE}
summary=${GITHUB_STEP_SUMMARY:-/dev/null}
git rev-parse --verify "$base^{commit}" >/dev/null

# Review both additions and removals in Gradle and manifest declarations.
deps=$(git diff "$base"...HEAD -- '*.gradle.kts' | grep -E '^[+-][^+-].*(implementation|api|compileOnly|runtimeOnly|ksp|kapt|annotationProcessor)[[:space:]]*\(|^[+-][^+-].*id[[:space:]]*\(' || true)
perms=$(git diff "$base"...HEAD -- '*AndroidManifest.xml' | grep -E '^[+-][^+-].*(uses-permission|<permission)' || true)
changed=$(git diff --name-only "$base"...HEAD -- app/src/main tools/mullvad)
notes=$(git diff "$base"...HEAD -- CHANGELOG.md | grep -c '^+- ' || true)

{
    echo "### Plume change review"
    echo "Review hints only: Mullvad integration may legitimately add dependencies or permissions."
    echo "| Review item | Result |"
    echo "|---|---|"
} >> "$summary"

if [ -n "$deps" ]; then
    echo "::warning title=Gradle dependency review::A dependency or plugin declaration changed; review purpose and supply-chain impact."
    printf '%s\n' "$deps"
    echo "| Gradle dependencies/plugins | ⚠️ changed; review required |" >> "$summary"
else
    echo "| Gradle dependencies/plugins | ✅ no declaration changed |" >> "$summary"
fi

if [ -n "$perms" ]; then
    echo "::warning title=Android permission review::A manifest permission declaration changed; review VPN and Android permission implications."
    printf '%s\n' "$perms"
    echo "| Manifest permissions | ⚠️ changed; review required |" >> "$summary"
else
    echo "| Manifest permissions | ✅ no declaration changed |" >> "$summary"
fi

if [ -n "$changed" ] && [ "$notes" -eq 0 ]; then
    echo "::warning title=Changelog review::App or Mullvad integration code changed without a changelog bullet; add one for user-visible changes."
    echo "| CHANGELOG entry | ⚠️ none; add for user-visible changes |" >> "$summary"
else
    echo "| CHANGELOG entry | ✅ |" >> "$summary"
fi
