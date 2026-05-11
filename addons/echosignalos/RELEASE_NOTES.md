# SignalOS Standalone Computer OS Notes

SignalOS has evolved from a terminal-framework MVP into a standalone Echo-compatible computer OS addon. This pass keeps the existing mission/archive/reward/diagnostic APIs, but routes them through a desktop shell with real computer blocks, network identity, data records, and optional Echo Core state.

## Highlights

- Upgrades `signalos:terminal` into the base SignalOS access point.
- Adds `signalos:workstation`, `signalos:server_rack`, `signalos:network_relay`, and `signalos:data_drive`.
- Adds a desktop shell with app launcher, status bar, active app panel, notifications, settings, and shared theme tokens.
- Adds built-in apps: Home, Files, Notes, Logs, Network Monitor, Settings, Data Vault, Echo Link, Missions, Archives, Rewards, and Diagnostics.
- Adds Java APIs for `SignalOsApp`, `SignalOsDataProvider`, and `SignalOsPeripheralProvider`.
- Loads datapack JSON from:
  - `data/<namespace>/signalos/apps/*.json`
  - `data/<namespace>/signalos/data_records/*.json`
  - `data/<namespace>/signalos/drive_templates/*.json`
  - existing chapter, mission, and archive paths.
- Syncs network state, access tier, linked block counts, and data records to the client terminal state packet.
- Persists player notes and preferences, and persists drive records with the new data-drive component.
- Bridges to Echo Core defensively through module reports, platform summaries, diagnostics, and route records where available.

## Requirements

- Minecraft `26.1.2`
- NeoForge `26.1.2.29-beta` or newer in the `26.1.x` range
- Java `25+`
- `echocore` `1.1.0+`

## Verified In This Pass

- `:echosignalos:build --warning-mode all` passes.
- `:echosignalos:runGameTestServer --warning-mode all` returned successfully.
- Added GameTests for app registration and duplicate rejection, data-drive component flow, workstation access validity, and computer-network discovery.
- Existing `signalosexample` chapter/mission/archive content remains compatible through the legacy app views.

## Known Limitations

- V1 uses one active app at a time, not draggable multi-window management.
- Notes can be created and cleared from the app surface, but full typed editing is still a follow-up.
- Data Vault exposes and archives records conceptually through the current record browser; deeper import/export workflows can build on the drive component model.
- JSON apps without a known app `type` are launcher metadata until a custom renderer path is added.
- Computer block art is placeholder-quality and should be replaced before a production content pass.

## Manual Smoke Checklist

1. Launch `:signalosexample:runExampleClient`.
2. Create or open a local test world.
3. Place `signalos:terminal` and open it by interacting with the block.
4. Confirm the launcher shows Home, Files, Notes, Logs, Network Monitor, Settings, Data Vault, Echo Link, Missions, Archives, Rewards, and Diagnostics.
5. Place a workstation, server rack, network relay, and data drive; confirm Network Monitor updates after reopening the terminal.
6. Open Files, Logs, Data Vault, and Echo Link and confirm records render without overlap.
7. Create a note, reopen the terminal, and confirm the note appears.
8. Open Settings and change theme, UI scale, and access mode.
9. Confirm Missions, Archives, Rewards, and Diagnostics still expose existing `signalosexample` content.
10. Run `/reload` and confirm JSON app/data/legacy content remains present afterward.

## Build Commands

```powershell
.\gradlew.bat :echosignalos:build --warning-mode all
.\gradlew.bat :signalosexample:build --warning-mode all
.\gradlew.bat :echosignalos:runGameTestServer --warning-mode all
python tools\validate_resources.py --addon-set beta
```
