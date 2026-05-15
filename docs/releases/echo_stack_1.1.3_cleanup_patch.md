# ECHO Stack 1.1.3 - Cleanup Patch

ECHO Stack `1.1.3` is a cleanup-only patch after the CyberGlass default and deterministic release-gate work. It does not add gameplay features, runtime APIs, generated art, config migrations, or launch certification claims.

## Highlights

- Public stack metadata and current docs are aligned to `1.1.3`.
- `echoAddonSet=all` continues to use the explicit public beta plus release addon lists introduced in `1.1.2`.
- Local prototype addons, including `addons/echorecovery`, remain excluded from public beta/all lists and release artifacts.
- CyberGlass remains the ThemeCore default/fallback theme, and ThemeCore still makes CyberGlass Terminal's default when Terminal is loaded.
- `verifyEchoRelease` is now the pure repository release gate and no longer requires a local CurseForge or launcher `mods` folder.
- Local modpack-profile checks are explicit through `copyEchoJarsToModpack`, `checkEchoModJarSet`, and `verifyEchoModpackProfile`, all requiring `-PechoModpackModsDir=<mods path>`.

## Compatibility

- Public stack version: `1.1.3`.
- No runtime API changes.
- No consumer hard dependencies.
- No packet, save-data, recipe, mission, route, menu, datapack schema, or user-config migration.
- No runtime image generation and no new CyberGlass or Nexus art.

## Automated Verification

Run deterministic release gates:

```powershell
python tools/validate_resources.py --addon-set beta
python tools/validate_resources.py --addon-set all
.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all
```

Run split-gate checks:

```powershell
.\gradlew.bat -PechoAddonSet=all verifyEchoRelease --warning-mode all
.\gradlew.bat -PechoAddonSet=all checkEchoModJarSet --warning-mode all
.\gradlew.bat -PechoAddonSet=all -PechoModpackModsDir=<mods path> copyEchoJarsToModpack verifyEchoModpackProfile --warning-mode all
```

Current workspace evidence from 2026-05-14:

- Pending.

## Manual Visual Evidence

`1.1.3` does not claim playable launch certification. Manual visual screenshot QA remains pending for a later launch RC or dedicated visual QA pass.

| Check | Result |
| --- | --- |
| Terminal opens to CyberGlass with no saved Terminal theme | Not claimed by this cleanup patch |
| Existing saved Terminal theme is preserved after restart | Not claimed by this cleanup patch |
| Vanilla title/pause/options/inventory/container/hotbar/tooltips/toasts/boss bar use CyberGlass accents safely | Not claimed by this cleanup patch |
| HoloMap, Lens, RenderCore, SignalOS, SoundCore, and Blockworks remain stable with ThemeCore present/absent | Not claimed by this cleanup patch |

## Notes

- `1.1.2` resolved the `1.1.1` dirty-worktree `echorecovery` auto-discovery blocker by making public addon sets explicit.
- `addons/echorecovery` remains local prototype work and is not part of the public `1.1.3` stack.
