# ECHO: Terminal 1.2.0

Status: planned terminal module release for addon-page config editing and expanded zoom options.

## Highlights

- Adds stack-wide config editing from the Terminal Addons page for modules that publish curated config entries through Echo Core.
- Shows server/common config separately from client-local config so players can see what is authoritative on the server and what is saved locally.
- Gates server/common edits behind operator or singleplayer-owner permission while keeping published values viewable to all players.
- Saves client-local Terminal options locally, including the Terminal client options exposed through the config registry.
- Expands Terminal zoom presets to `50%`, `75%`, `85%`, `90%`, `100%`, `110%`, `125%`, and `150%`.

## Compatibility Notes

- Existing saved Terminal zoom values using `ZOOM_85`, `ZOOM_90`, `ZOOM_100`, `ZOOM_110`, or `ZOOM_125` remain compatible.
- `ZOOM_100` remains the default and fallback zoom value.
- Server/common config snapshots are rendered from server-synced values, not from the local client's common config copy.
- This is a terminal module release. A public full-stack GitHub release should be cut separately if all release-module versions and the tag need to align.

## Verification

- `.\gradlew.bat :echoterminal:compileJava`
- `.\gradlew.bat :echoterminal:runGameTestServer --warning-mode all`
- Manual smoke: open Terminal by block and keybind, inspect Addons config sections, verify non-op server config is read-only, verify operator edits save and resync, verify invalid values are rejected, and verify client-local zoom/settings save without server traffic.
