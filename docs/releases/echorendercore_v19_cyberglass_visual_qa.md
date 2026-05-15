# ECHO: RenderCore V19 Cyberglass Visual QA Evidence

RenderCore V19 turns the V18 cyberglass adoption into a release-evidence pass. Runtime screen chrome APIs stay stable; the new work is a deterministic QA catalog, evidence export metadata, debug capture commands, and Workbench visibility for the adopted chrome styles.

## Highlights

- Adds a screen chrome QA catalog for Terminal normal/reduced-motion, SignalOS terminal/rack, HoloMap minimap, Index overlay, Lens overlay, and the generic RenderCore cyberglass example.
- Extends creator visual QA exports with screen chrome surface metadata: addon id, surface id, style, label policy, profile id, reduced-motion flag, screenshot path, and QA status/notes.
- Adds `/rendercore debug screenchrome evidence start|capture <surface>|status|export|reset`, writing `rendercore_creator/all/visual_qa/screenchrome.evidence.json`.
- Updates the Creator Workbench report surface with screen QA counts and style chips for `CYBERGLASS`, `TERMINAL`, `HOLOGRAM`, `NEON`, and `MINIMAL`.
- Keeps `RenderCoreScreenVisualHost`, `RenderCoreScreenFrameOptions`, and runtime visual profile schema unchanged.

## QA Matrix

Required surface ids: `echo_terminal`, `echo_terminal_reduced_motion`, `signalos_terminal`, `signalos_rack`, `holomap_minimap`, `index_overlay`, `lens_overlay`, and `rendercore_cyberglass_example`.

Acceptance: each surface has a captured screenshot, the expected chrome style is visible, quiet overlays stay quiet, Terminal reduced-motion avoids motion-heavy glints/chromatic effects, and `screen_chrome_blockers` is empty after export.

## Verification

- `.\gradlew.bat :echorendercore:compileJava`
- `.\gradlew.bat :echoterminal:compileJava :echosignalos:compileJava :echoholomap:compileJava :echoindex:compileJava :echolens:compileJava`
- `.\gradlew.bat buildEchoWorkspace -PechoAddonSet=all`

Manual visual QA still requires opening each listed surface in-game and capturing evidence with the V19 screen chrome command.
