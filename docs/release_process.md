# ECHO Release Process

This document defines the version contract and release automation checks for the ECHO stack.

## Version Contract

All public releases must keep three surfaces aligned:

1. **Git tag**: `v<major>.<minor>.<patch>[-<prerelease>.<n>]`
   - Example prerelease: `v1.0.1-beta.1`
2. **Module versions**: every release module (`echocore`, `echoashfallprotocol`, and included addons) resolves to the matching release version for that cut.
3. **GitHub release naming**: the release title/name should match the exact Git tag value.

If these drift, the release is considered invalid.

## CI Release Manifest

The root Gradle task `printReleaseManifest` prints one line per expected module:

```text
<modulePath>|<artifactName>|<resolvedVersion>|<absoluteJarPath>
```

This manifest is persisted by CI as `release-manifest.txt` and uploaded as both:

- workflow artifact (`release-manifest`)
- GitHub release asset (`release-manifest.txt`)

## Expected Module Validation

Expected modules are derived from the same inclusion logic used by `settings.gradle`:

- `echoAddonSet=beta`: beta addon set only
- `echoAddonSet=all`: explicit beta + release addon sets

In all cases, `:echocore` and root `:` (`echoashfallprotocol`) are required.

Release addon sets are intentionally explicit. Local or prototype addon directories under
`addons/` are not part of public release validation until they are added to the declared
beta or release lists in `settings.gradle`, root release artifact selection, and
`tools/validate_resources.py`.

`validateReleaseArtifacts` enforces that every expected module has a built JAR before publishing. If any are missing, the release job fails.

`verifyEchoRelease` is the pure repository release gate. It runs validators, the workspace build, runtime-log scanning, and GameTests without requiring a local CurseForge or launcher profile.

Local modpack-profile checks are explicit:

- `copyEchoJarsToModpack` builds and copies public ECHO jars only when `-PechoModpackModsDir=<mods path>` is supplied.
- `checkEchoModJarSet` verifies exactly one current ECHO jar per expected module only when `-PechoModpackModsDir=<mods path>` is supplied.
- `verifyEchoModpackProfile` runs the local profile jar-set check after jars have been copied.

## Suggested Release Sequence

1. Run resource and gameplay validators.
2. Build and verify release modules.
3. Run `validateReleaseArtifacts`.
4. Run full-stack release verification with `verifyEchoRelease`.
5. For local launcher QA, run `copyEchoJarsToModpack` and `verifyEchoModpackProfile` with an explicit `echoModpackModsDir`.
6. Run the Ashfall fresh-world manual smoke pass and record the result in the matching release note.
7. Generate `release-manifest.txt` via `printReleaseManifest`.
8. Publish only after checks pass.
9. Attach module jars plus `release-manifest.txt` to the GitHub release.
