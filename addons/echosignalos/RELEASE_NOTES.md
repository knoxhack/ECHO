# SignalOS 0.1.0 RC Notes

SignalOS 0.1.0 is the first reusable terminal-framework release candidate extracted from the ECHO stack. It is an MVP library for NeoForge modpacks and addons that want an in-game terminal with chapters, missions, archives, a reward inbox, and diagnostics.

## Highlights

- Adds the `signalos:terminal` block and item.
- Provides a client terminal screen with chapter navigation and built-in pages for missions, archives, rewards, and diagnostics.
- Supports Java registration for chapters, pages, missions, archive records, themes, actions, and diagnostics providers.
- Loads simple datapack JSON content from:
  - `data/<namespace>/signalos/chapters/*.json`
  - `data/<namespace>/signalos/missions/*.json`
  - `data/<namespace>/signalos/archives/*.json`
- Tracks mission claimed state, archive read state, and pending terminal reward counts through server-to-client sync.
- Stores and claims mission rewards through the linked SignalOS terminal block entity.
- Includes `signalosexample`, a separate example addon with Java content, JSON content, and KubeJS-friendly script examples.

## Requirements

- Minecraft `26.1.2`
- NeoForge `26.1.2.29-beta` or newer in the `26.1.x` range
- Java `25+`
- `echocore` `1.1.0+`

## Verified For This RC

- `:echosignalos:build` passes.
- `:signalosexample:build` passes.
- Beta resource validation passes.
- SignalOS GameTests pass: 13 required tests.
- Example client launch reaches Minecraft client initialization with `echocore`, `signalos`, and `signalosexample` loaded.
- Example client launch registers SignalOS example content during common setup.
- Dedicated example server loads combined core/example JSON content: 2 chapters, 2 missions, and 2 archive records.

## Known Limitations

- Terminal art is still a vanilla-textured model, not final production art.
- `TerminalTheme` is API data only; theme rendering is not implemented yet.
- Custom `TerminalPage` entries provide tab metadata only unless the page type is one of `missions`, `archives`, `rewards`, or `diagnostics`.
- KubeJS support is a soft `Java.loadClass` bridge, not a native KubeJS plugin event.
- Mission graph, POI route map, faction atlas, JEI/GameStages/FTB/Patchouli bridges, and terminal modes are not included in this RC.
- `/reload` was not fully verified in an interactive client session during this RC pass; server startup JSON loading was verified, and the reload command should receive one more manual check before publishing.

## Manual Smoke Checklist Before Publishing

1. Launch `:signalosexample:runExampleClient`.
2. Create or open a local test world.
3. Place `signalos:terminal` and open it by interacting with the block.
4. Press the SignalOS keybind and confirm the terminal opens remotely.
5. Confirm chapters include SignalOS core content and `signalosexample` Java/JSON content.
6. Confirm Missions, Archives, Rewards, and Diagnostics tabs render without overlap.
7. Complete `minecraft:story/root`, claim the example mission reward, then claim the terminal reward inbox.
8. Open the example archive and confirm it changes from unread to read.
9. Run `/reload` and confirm JSON content remains present afterward.

## Build Commands

```powershell
.\gradlew.bat :echosignalos:build --warning-mode all
.\gradlew.bat :signalosexample:build --warning-mode all
.\gradlew.bat :echosignalos:runGameTestServer --warning-mode all
python tools\validate_resources.py --addon-set beta
```
