## What changed and why

<!-- A cohesive redesign may change Home, Settings and Mullvad together. No feature issue or small-diff requirement. -->

## Design reference and visual QA

- Reference: current user-supplied expressive 1536×864 screenshot / applicable design specification
- Screens changed:
- Screenshot/video evidence, device resolution and font/display scale:
- Known visual differences, if any:

## Navigation and functionality

- D-Pad and focus restore:
- Back/Home/All Apps/System actions:
- Mullvad connection, account/settings, split tunnel and other impacted functions:

## Technology and performance

- Libraries or refactors introduced (allowed when justified):
- APK/memory/startup impact if measurable:
- Android permission/network/security changes (if any):

## Verification

- [ ] JVM tests and Android lint
- [ ] Combined Plume+Mullvad ARM64 CI
- [ ] Actual Android TV/emulator focus and overscan checks (or explicitly pending)
- [ ] Real VPN connection/permissions check if affected (or explicitly pending)
- [ ] CHANGELOG.md updated for user-facing behavior
- [ ] Secrets, signing, data integrity and privacy gates preserved

A passing build is not proof of pixel-perfect design or real-device VPN behavior.
