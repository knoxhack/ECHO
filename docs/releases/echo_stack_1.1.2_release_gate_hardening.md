# ECHO Stack 1.1.2 - Release Gate Hardening

ECHO Stack `1.1.2` is a patch-only release-readiness pass after the CyberGlass default rollout. It keeps gameplay, saves, packets, schemas, ThemeCore runtime behavior, and consumer dependencies stable while making public release gates deterministic.

## Highlights

- `echoAddonSet=all` now resolves to the explicit public release stack instead of every buildable directory under `addons`.
- Local or prototype addon directories, including `addons/echorecovery`, are excluded until deliberately added to the public release addon list.
- CyberGlass remains the ThemeCore default/fallback theme, and ThemeCore still makes CyberGlass Terminal's default when Terminal is loaded.
- Existing saved Terminal theme selections are preserved.
- Vanilla UI safety remains unchanged: no slot movement, no widget movement, unknown-screen protection, and text-contrast preservation stay enabled.

## Compatibility

- Public stack version: `1.1.2`.
- No runtime API changes.
- No consumer hard dependencies.
- No packet, save-data, recipe, mission, route, menu, or datapack schema migration.
- No runtime image generation and no new CyberGlass or Nexus art.

## Automated Verification

Run CyberGlass gates:

```powershell
python tools/echo-themeforge/themeforge.py validate --theme cyberglass --strict
python tools/echo-themeforge/themeforge.py report --theme cyberglass
```

Run focused CyberGlass/default-on gates:

```powershell
.\gradlew.bat :echothemecore:build :echoterminal:build :echosignalos:build :echoholomap:build :echolens:build :echorendercore:build :echosoundcore:build :echoblockworks:build
.\gradlew.bat :echothemecore:runGameTestServer :echoterminal:runGameTestServer
```

Run deterministic release gates:

```powershell
.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=beta validateReleaseArtifacts printReleaseManifest --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all
```

Current workspace evidence from 2026-05-14:

- `python tools/echo-themeforge/themeforge.py validate --theme cyberglass --strict` passed.
- `python tools/echo-themeforge/themeforge.py report --theme cyberglass` passed.
- Direct PNG signature/dimension validation over `cyberglass.json` texture references passed: 50 PNG references checked, 0 missing, 0 invalid.
- Focused CyberGlass/default-on build passed for ThemeCore, Terminal, SignalOS, HoloMap, Lens, RenderCore, SoundCore, and Blockworks.
- `.\gradlew.bat :echothemecore:runGameTestServer :echoterminal:runGameTestServer` passed.
- `.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all` passed.
- `.\gradlew.bat -PechoAddonSet=beta validateReleaseArtifacts printReleaseManifest --warning-mode all` passed and emitted `1.1.2` release artifacts.
- `.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all` passed using the explicit public addon list.
- `.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all` passed and emitted `1.1.2` release artifacts for the explicit beta plus release addon lists.
- The beta/all settings and release manifests were rechecked after correction; `addons/echorecovery` is not included in the public addon lists or emitted release artifacts.

## Manual Visual Evidence

Record final visual screenshot evidence before publishing:

| Check | Result |
| --- | --- |
| Terminal opens to CyberGlass with no saved Terminal theme | Automated Terminal game test passed; visual screenshot evidence still required before publishing |
| Existing saved Terminal theme is preserved after restart | Automated Terminal game test passed; visual screenshot evidence still required before publishing |
| Title, pause, options, inventory, container, hotbar, tooltip, toast, and boss-bar accents render without slot/widget movement | Automated ThemeCore safety/default tests passed; visual screenshot evidence still required before publishing |
| HoloMap, Lens, RenderCore, SignalOS, SoundCore, and Blockworks remain stable with ThemeCore present | Focused module builds passed; visual smoke evidence still required before publishing |
| HoloMap, Lens, RenderCore, SignalOS, SoundCore, and Blockworks keep fallback visuals/sounds with ThemeCore absent | Focused module builds passed; fallback visual/sound smoke evidence still required before publishing |
| Dedicated server startup does not load client-only ThemeCore classes | `verifyCommonServerSafe` checks ran during focused/release builds and passed |

## Notes

- `addons/echorecovery` is local prototype work and is not part of the public `1.1.2` stack.
- Nexus generated assets remain backlog unless a packaged runtime theme JSON references them.
