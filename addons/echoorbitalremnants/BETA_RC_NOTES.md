# ECHO-7 Release Verification Notes

## Version

- Mod: `1.4.0`
- Minecraft: `26.1.2`
- NeoForge: `26.1.2.29-beta`

## Automated Result

- `.\gradlew.bat :echoorbitalremnants:build`: required from the ECHO workspace root.
- `.\gradlew.bat :echoorbitalremnants:runGameTestServer`: required from the ECHO workspace root.
- Built jar: `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.4.0.jar`.

## Manual Smoke-Test Result

Manual client smoke testing is still required before public upload. This automation pass cannot complete the interactive client checklist, but the release test plan is synced for `1.4.0` and covers clean survival launch, route repairs, creative route traversal, bosses, surveys, faction contracts, machines, return vectors, ECHO-0, Nexus stabilization guidance, and final network sealing.

Manual pass/fail fields to fill before upload:

- Clean survival reaches Low Earth Orbit: not run in this automated pass.
- Creative route pass verifies all five dimensions: not run in this automated pass.
- Machines open and process: covered by GameTests; manual screen pass still recommended.
- Boss bars, drops, and progression rewards fire once: rewards covered by GameTests; visual boss-bar pass still recommended.
- Surveys and Nexus stabilization guidance complete: covered by GameTests; manual route pass still recommended.
- One faction contract completes and final-seal/cooldown double rewards are blocked: covered by GameTests.
- Route return vectors work: covered by GameTests; manual route pass still recommended.

## Remaining Known Issues

- Bespoke animated boss models are deferred until after this release.
- Faction vendors, bases, and long quest chains are deferred until after this release.
- New planets are deferred; this release focuses on the existing Earth-to-Nexus route arc.
- Large multichunk structures are deferred; route terrain uses deterministic compact features for now.

## Release Note

Publish only `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.4.0.jar` after automated checks pass and the manual smoke-test checklist has been completed.
