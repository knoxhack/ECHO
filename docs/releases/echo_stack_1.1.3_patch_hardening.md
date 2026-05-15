# ECHO Stack 1.1.3 - Patch Version Hardening

ECHO Stack `1.1.3` is a patch-only version alignment pass after the `1.1.2` release-gate hardening work. It keeps gameplay, saves, packets, schemas, dependency ranges, Minecraft, NeoForge, Java, and public APIs stable while moving the declared public stack version forward.

## Highlights

- Root Ashfall, ECHO Core, and every public addon included by `echoAddonSet=all` declare `1.1.3`.
- Non-templated shipped `META-INF/neoforge.mods.toml` files now report `version="1.1.3"`.
- Build-truth docs, release artifact examples, command-center seed data, and ThemeCore visible/logged version text now reference `1.1.3`.
- HoloMap optional dependency metadata includes reasons for Terminal, RenderCore, RuntimeGuard, and MissionCore integrations.
- Historical release notes, prototype addon versions, dependency locks, and dependency `versionRange` floors are intentionally unchanged.

## Compatibility

- Public stack version: `1.1.3`.
- Intended release tag and GitHub release name: `v1.1.3`.
- No runtime API changes.
- No packet, save-data, recipe, mission, route, menu, or datapack schema migration.
- No new gameplay content or runtime behavior changes are part of this patch.

## Automated Verification

Run targeted HoloMap compilation:

```powershell
.\gradlew.bat :echoholomap:compileJava --warning-mode all
```

Run all-stack release gates:

```powershell
.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all
```

Run the full release verification only after the Ashfall modpack destination is configured:

```powershell
.\gradlew.bat -PechoAddonSet=all verifyEchoRelease --warning-mode all
```

## Current Workspace Evidence

- Targeted current-version metadata checks passed for public `gradle.properties`, source `neoforge.mods.toml`, command-center seed data, and release-facing docs.
- `.\gradlew.bat :echoholomap:compileJava --warning-mode all` passed.
- `.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all` passed.
- `.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all` passed; the local build output contains 32 current public `1.1.3` jars.
- Full `verifyEchoRelease` was not run because no `-PechoModpackModsDir=<mods path>` value is configured in this workspace.
