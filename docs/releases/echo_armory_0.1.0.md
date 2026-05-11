# ECHO: Armory 0.1.0 Release Package

Status: packaged first playable release. Armory build, resource validation, gameplay-data validation, GameTests, full workspace build, release artifact validation, and release manifest generation are passing.

## Artifact

- Jar: `C:\Users\knox\AppData\Local\EchoGradleBuild\Echo\echoarmory\libs\echoarmory-0.1.0.jar`
- Jar size: 217,136 bytes
- Release zip: `C:\Github\Echo\addons\echoarmory\build\release\echoarmory-0.1.0-release.zip`
- Release zip size: 179,691 bytes
- Version: `0.1.0`
- Mod id: `echoarmory`

## Package Contents

- `echoarmory-0.1.0.jar`
- `CHANGELOG.md`
- `RELEASE_NOTES.md`
- `release-manifest.txt`

## Verification Commands

- `.\gradlew.bat --no-configuration-cache :echoarmory:build`
- `.\gradlew.bat validateEchoResources`
- `.\gradlew.bat validateEchoGameplayData`
- `.\gradlew.bat --no-configuration-cache :echoarmory:runGameTestServer`
- `.\gradlew.bat buildEchoWorkspace`
- `.\gradlew.bat --no-configuration-cache validateReleaseArtifacts printReleaseManifest`

## Verification Result

- Armory build: passed
- Armory GameTest server: passed, 11 required tests
- Full workspace build: passed
- Resource validation: passed
- Gameplay data validation: passed
- Release artifact validation: passed with `--no-configuration-cache`
- Release manifest generation: passed with `--no-configuration-cache`

## Release Highlights

- Completed the first survival loop from Armory Alloy Plate to module-installed gear.
- Fixed broken `veil_shield` resource IDs and added recipe/model reference validation.
- Hardened station and Terminal actions against item duplication, silent item loss, free recharge, and post-craft deletion.
- Added selected Terminal rows for loadout, augment, and boss context.
- Added data-driven synergy activation and validation for bundled Armory gameplay definitions.
- Added smoke-test and acquisition-path docs for release QA.

## Non-Armory Ecosystem Notes

- Armory verification currently requires `--no-configuration-cache` for GameTests and release manifest tasks.
- Orbital Remnants had a mapped API drift in `OrbitalMachineBlock` that blocked Armory verification; it was updated to the current removal hook.
- No remaining non-Armory blocker was observed in the final validation set.

## Manifest Line

```text
:echoarmory|echoarmory|0.1.0|C:\Users\knox\AppData\Local\EchoGradleBuild\Echo\echoarmory\libs\echoarmory-0.1.0.jar
```
