#!/bin/sh
# The Plume+Mullvad fork intentionally has dependencies and VPN permissions; do not
set -eu

base=${1:?usage: pr-rules.sh BASE}
summary=${GITHUB_STEP_SUMMARY:-/dev/null}
git rev-parse --verify "$base^{commit}" >/dev/null

deps=$(git diff "$base"...HEAD -- '*.gradle.kts' | grep -E '^[+-][^+-].*(implementation|api|compileOnly|runtimeOnly|ksp|kapt|annotationProcessor)[[:space:]]*\(|^[+-][^+-].*id[[:space:]]*\(' || true)
perms=$(git diff "$base"...HEAD -- '*AndroidManifest.xml' | grep -E '^[+-][^+-].*(uses-permission|<permission)' || true)
changed=$(git diff --name-only "$base"...HEAD -- app/src/main tools/mullvad)
notes=$(git diff "$base"...HEAD -- CHANGELOG.md | grep -c '^+- ' || true)

{
    echo "### Plume change review"
    echo "Review hints only: modern UI libraries and cohesive refactors are allowed. Keep VPN/privacy guardrails."
    echo "| Review item | Result |"
    echo "|---|---|"
} >> "$summary"

if [ -n "$deps" ]; then
    echo "::notice title=Gradle dependency review::UI dependencies are allowed; document size, license and supply-chain tradeoffs."
    printf '%s\n' "$deps"
    echo "| Gradle dependencies/plugins | ℹ️ changed; UI dependencies allowed, review cost |" >> "$summary"
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
