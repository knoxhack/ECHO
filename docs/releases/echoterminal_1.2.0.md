# ECHO: Terminal 1.2.0

Status: planned terminal module release for addon-page config editing, expanded zoom options, and explicit Survival Route placement.

## Highlights

- Adds stack-wide config editing from the Terminal Addons page for modules that publish curated config entries through Echo Core.
- Shows server/common config separately from client-local config so players can see what is authoritative on the server and what is saved locally.
- Gates server/common edits behind operator or singleplayer-owner permission while keeping published values viewable to all players.
- Saves client-local Terminal options locally, including the Terminal client options exposed through the config registry.
- Expands Terminal zoom presets to `50%`, `75%`, `85%`, `90%`, `100%`, `110%`, `125%`, and `150%`.
- Finalizes `TerminalMissionProvider#routePlacement(...)` and `TerminalMissionRoutePlacement` for `main`, `optional`, `reference`, and `hidden` aggregate-route placement.
- Lets the aggregate Survival Route consume explicit placement before authored phase inference, honor hidden records, and sort by explicit phase and route order.
- Keeps the guided route browser focused on do-next missions, ready rewards, side leads, and the full roadmap without shipping dormant filter chip code.

## Compatibility Notes

- Existing saved Terminal zoom values using `ZOOM_85`, `ZOOM_90`, `ZOOM_100`, `ZOOM_110`, or `ZOOM_125` remain compatible.
- `ZOOM_100` remains the default and fallback zoom value.
- Server/common config snapshots are rendered from server-synced values, not from the local client's common config copy.
- Providers that do not publish explicit route placement continue to use authored phase inference.
- The public full-stack GitHub release is tracked separately in `echo_stack_1.2.0_orbital_terminal_polish.md` so release-module versions and the tag stay aligned.

## Verification

- `.\gradlew.bat :echoterminal:compileJava`
- `.\gradlew.bat :echoterminal:build --warning-mode all`
- `.\gradlew.bat :echoterminal:runGameTestServer --warning-mode all`
- Manual smoke: open Terminal by block and keybind, inspect Addons config sections, verify non-op server config is read-only, verify operator edits save and resync, verify invalid values are rejected, and verify client-local zoom/settings save without server traffic.
