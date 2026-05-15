# ECHO Stack 1.2.0 - RenderCore and Terminal Minor

ECHO Stack `1.2.0` is a minor release for two public capability upgrades: explicit Terminal mission route placement and RenderCore V18 shared screen chrome. It keeps Minecraft, NeoForge, Java, save data, packet formats, recipes, menus, datapack schemas, and third-party dependency versions stable.

## Highlights

- Public stack metadata now targets `1.2.0`; release artifacts are expected to use `*-1.2.0.jar` filenames and the release tag `v1.2.0`.
- Terminal mission providers can publish route placement through `TerminalMissionProvider#routePlacement(...)` and `TerminalMissionRoutePlacement`.
- The Survival Route honors provider phase/order/role placement, excludes hidden route entries, and falls back safely when provider placement fails.
- RenderCore V18 adds `RenderCoreScreenFrameOptions` builder presets plus `RenderCoreScreenChromeStyle` for shared cyberglass, terminal, hologram, neon, and minimal screen chrome.
- Terminal, SignalOS, HoloMap, Index, and Lens now use shared RenderCore screen chrome presets while keeping their own visual-profile accents.

## Compatibility

- Existing Terminal mission providers remain source-compatible because route placement is a default method.
- `RenderCoreScreenFrameOptions` keeps the public six-argument constructor and accessor names while adding builders and presets.
- Addons that call the new Terminal or RenderCore APIs now declare a `1.2.0` minimum optional dependency for those integrations.
- `addons/echorecovery` remains local prototype work outside the public stack and stays on its own prototype version.

## Verification Checklist

- `.\gradlew.bat :echoterminal:compileJava --warning-mode all`
- `.\gradlew.bat :echorendercore:compileJava --warning-mode all`
- `.\gradlew.bat :echoholomap:compileJava --warning-mode all`
- `.\gradlew.bat :echoindex:compileJava --warning-mode all`
- `.\gradlew.bat :echolens:compileJava --warning-mode all`
- `.\gradlew.bat :echosignalos:compileJava --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=all verifyEchoRelease --warning-mode all`

Manual visual QA should cover Terminal, SignalOS terminal/rack screens, HoloMap minimap, Index overlay, Lens overlay, and Terminal reduced-motion chrome.
