# ECHO-7 Release Verification Notes

## Version

- Mod: `1.5.0`
- Minecraft: `26.1.2`
- NeoForge: `26.1.2.29-beta`
- Stack: ECHO Core `1.0.0`, ECHO Terminal `1.0.0`, Ashfall Protocol `1.0.0`, and the current full-stack release addons when testing the complete workspace.

## Automated Result

Status: PASS after strict playability verification on 2026-05-10 with Java 25 / `JAVA_HOME` configured.

- `.\gradlew.bat --no-daemon :echoorbitalremnants:build --console=plain -PechoAddonSet=beta`: PASS.
- `.\gradlew.bat --no-daemon validateEchoResources --console=plain -PechoAddonSet=beta -PechoPythonExecutable="python"`: PASS.
- `python tools\validate_gameplay_data.py`: PASS; 252 registered item/block ids and 118 structure palette block ids checked.
- `.\gradlew.bat --no-daemon :echoorbitalremnants:runGameTestServer --console=plain --stacktrace -PechoAddonSet=beta`: PASS; 44 required Orbital GameTests are registered, and the harness reported all 45 required tests passed.
- Built jar confirmed: `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.5.0.jar`.
- Primary redirected Gradle output confirmed: `%LOCALAPPDATA%\EchoGradleBuild\Echo\echoorbitalremnants\libs\echoorbitalremnants-1.5.0.jar`.

## Manual Smoke-Test Result

Status: PENDING USER PLAYTHROUGH.

Manual client smoke testing is still required before public upload. This automation pass cannot complete the interactive client checklist, but the release test plan is synced for `1.5.0` and covers clean survival launch, shared Terminal surfaces, route records, diagnostics, hazard telemetry, Faction Atlas standings, support caches, route repairs, creative route traversal, major encounters, surveys, faction contracts, machines, return vectors, ECHO-0, Nexus stabilization guidance, and final network sealing.

Release promise: Orbital Remnants `1.5.0` is a polished route survival chapter with deterministic route hubs, compact faction contracts, and support/barter kiosks. Full NPC vendor settlements, bespoke animated encounters, long quest chains, and large structure frameworks remain explicitly deferred.

Manual pass/fail fields to fill before upload:

- Clean survival reaches Low Earth Orbit: not run in this automated pass.
- Creative route pass verifies Orbit, Moon, Mars, Europa, Saturn, Titan, and Nexus: not run in this automated pass.
- Machines open and process: covered by GameTests; manual screen pass still recommended.
- Encounter bars, drops, and progression rewards fire once: rewards covered by GameTests; visual encounter-bar pass still recommended.
- Surveys and Nexus stabilization guidance complete: covered by GameTests; manual route pass still recommended.
- Three faction contracts complete and final-seal/cooldown double rewards are blocked: covered by GameTests.
- Route return vectors work: covered by GameTests; manual route pass still recommended.
- Shared terminal route records, What Now diagnostics, Vitals telemetry, Faction Atlas entries, and Reward Inbox support caches match standalone ECHO-7 state: manual full-stack pass recommended.
- Route vessel and rocket handoffs provide clear blocked/success feedback in chat and action bar: covered by GameTests; manual feel pass still recommended.

## Remaining Known Issues

- Bespoke animated encounter models are deferred until after this release.
- Full NPC faction vendors, faction bases, and long quest chains are deferred; this release includes support/barter kiosks, relay hubs, ECHO Core faction standing, terminal atlas visibility, and compact contracts.
- Additional planets beyond Saturn Ring Graveyard and Titan Methane Shelf are deferred; this release focuses on the expanded Earth-to-Nexus route arc.
- Large multichunk structures are deferred; route terrain uses deterministic compact features for now.

## Release Note

Publish only `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.5.0.jar` after automated checks pass and the manual smoke-test checklist has been completed. The addon-local jar is synced from the redirected Gradle output during `:echoorbitalremnants:build`.
