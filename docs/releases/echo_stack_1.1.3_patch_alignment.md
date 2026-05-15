# ECHO Stack 1.1.3 - Patch Version Alignment

ECHO Stack `1.1.3` is a metadata and documentation-only patch alignment after the previous release-gate hardening pass. It keeps gameplay, saves, packets, schemas, commands, dependency minimums, Minecraft, NeoForge, Java, and public APIs stable while aligning the declared public stack version.

## Highlights

- Root Ashfall, ECHO Core, and every active public addon in the `echoAddonSet=all` stack now declare `1.1.3`.
- Hardcoded mod metadata files for non-templated addons now report `version="1.1.3"`.
- Build-truth docs, release artifact examples, command-center seed data, ThemeCore visible/logged version strings, and the Ashfall welcome version text now point at `1.1.3`.
- Historical release notes, prototype addon versions, dependency locks, ARCANA seed data, and dependency `versionRange` floors are intentionally unchanged.

## Compatibility

- Public stack version: `1.1.3`.
- Release tag and GitHub release name: `v1.1.3`.
- No runtime API changes.
- No packet, save-data, recipe, mission, route, menu, or datapack schema migration.
- No new gameplay content or runtime behavior changes are part of this patch.

## Automated Verification

Run targeted version checks:

```powershell
rg -n "mod_version=1\.1\.2" . -g "gradle.properties"
rg -n "version=`"1\.1\.2`"" . -g "neoforge.mods.toml" -g "!**/build/**"
rg -n "1\.1\.2" . -g "!**/build/**" -g "!**/.gradle/**" -g "!**/package-lock.json" -g "!**/run/**"
```

Run command-center seed checks:

```powershell
npm.cmd test
```

Run release gates:

```powershell
.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=beta validateReleaseArtifacts printReleaseManifest --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all
```

## Current Workspace Evidence

- Targeted current-version metadata checks passed after the final restore: no active `gradle.properties` files reported `mod_version=1.1.2` or `mod_version=1.2.0`, and no source `neoforge.mods.toml` files reported hardcoded `version="1.1.2"` or `version="1.2.0"`.
- Current public docs, release artifact examples, ThemeCore strings, and Ashfall welcome text were checked for stale `1.1.2`/`1.2.0` references after the patch restore.
- `npm.cmd test` in `addons/echomodpackcommandcenter` passed with 40/40 tests.
- `python tools\validate_resources.py --addon-set beta` passed.
- `python tools\validate_resources.py --addon-set all` passed after restoring a concurrent `echoarmory` version drift.
- `.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all` was attempted, but a concurrent Gradle process stopped the shared daemon before project tasks ran; rerun the Gradle gates once the workspace is quiet.
