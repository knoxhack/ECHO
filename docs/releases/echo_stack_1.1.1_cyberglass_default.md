# ECHO Stack 1.1.1 - CyberGlass Default

ECHO Stack `1.1.1` is the CyberGlass default certification patch. It keeps gameplay, save data, packets, mission schemas, route schemas, and menu contracts stable while making `echothemecore:cyberglass` the documented default visual identity for fresh installs and optional UI consumers.

## Highlights

- CyberGlass is the default and fallback ThemeCore theme for fresh configs.
- ThemeCore registers CyberGlass as the Terminal default when Terminal is loaded, while preserving existing valid Terminal theme selections.
- CyberGlass generated assets are treated as the release-gated theme asset set; Nexus generated assets remain future-theme backlog unless a packaged runtime reference is missing.
- Vanilla UI theming remains safety-first: no slot movement, no widget movement, no recipe/menu behavior changes, and text contrast preservation stays enabled.
- Terminal, SignalOS, HoloMap, Lens, RenderCore, SoundCore, and Blockworks keep optional ThemeCore behavior and vanilla/fallback behavior when ThemeCore is absent.

## Compatibility

- Public stack version: `1.1.1`.
- No packet, save-data, recipe JSON, mission, route, loadout, menu, or datapack schema migration is included.
- No new runtime ThemeCore API is included.
- No consumer gains a hard ThemeCore dependency.
- No runtime image generation is included; generated CyberGlass PNGs are packaged resources.

## Automated Verification

Run the CyberGlass asset gate:

```powershell
python tools/echo-themeforge/themeforge.py validate --theme cyberglass --strict
python tools/echo-themeforge/themeforge.py report --theme cyberglass
```

Run the focused module gate:

```powershell
.\gradlew.bat :echothemecore:build :echoterminal:build :echosignalos:build :echoholomap:build :echolens:build :echorendercore:build :echosoundcore:build :echoblockworks:build
.\gradlew.bat :echothemecore:runGameTestServer :echoterminal:runGameTestServer
```

Run the stack release gate from a clean CyberGlass-only worktree:

```powershell
.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all
```

Current workspace evidence from 2026-05-14:

- `validate --theme cyberglass --strict` passed.
- `report --theme cyberglass` passed with every CyberGlass generated asset group complete.
- Direct PNG signature/dimension validation checked 50 `cyberglass.json` texture refs with 0 missing and 0 invalid PNGs.
- Focused builds for ThemeCore, Terminal, SignalOS, HoloMap, Lens, RenderCore, SoundCore, and Blockworks passed.
- ThemeCore and Terminal game-test servers passed.
- Supplemental `-PechoAddonSet=beta` checks passed for `validateEchoResources buildEchoWorkspace` and `validateReleaseArtifacts printReleaseManifest` after aligning HoloMap/Lens with the public stack set.
- The `-PechoAddonSet=all` release gates were blocked in this dirty workspace because the untracked `addons/echorecovery` addon was auto-discovered and did not compile against the current Minecraft/NeoForge API. This was resolved in `1.1.2` by making the public addon sets explicit; `1.1.3` carries that behavior forward.

## Manual Visual Evidence

Record the result before publishing:

| Check | Result |
| --- | --- |
| Terminal opens to CyberGlass with no saved Terminal theme | Pending |
| Existing saved Terminal theme is preserved after restart | Pending |
| Title, pause, options, inventory, container, creative, hotbar, tooltip, toast, and boss-bar accents render without slot/widget movement | Pending |
| HoloMap, Lens, RenderCore, SignalOS, SoundCore, and Blockworks use CyberGlass when ThemeCore is present | Pending |
| HoloMap, Lens, RenderCore, SignalOS, SoundCore, and Blockworks keep fallback visuals/sounds when ThemeCore is absent | Pending |
| Dedicated server startup does not load client-only ThemeCore classes | Pending |

## Notes

- `addons/echorecovery` is not part of this CyberGlass 1.1.1 release branch.
- Global ThemeForge reports may still list missing generated Nexus assets; that is not a CyberGlass 1.1.1 blocker unless a packaged runtime reference is missing.
