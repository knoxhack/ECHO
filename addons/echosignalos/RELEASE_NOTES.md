# SignalOS 0.2.0 Release Notes

SignalOS 0.2.0 is the next minor feature release for the standalone Echo-compatible computer OS addon. This pass keeps the existing Minecraft, NeoForge, Java, and Echo Core dependency targets unchanged while adding editable notes, a real server-rack drive workflow, and extensible custom app rendering.

## Highlights

- Bumps `mod_version` to `0.2.0`.
- Adds editable Notes behavior in the terminal with a selected note list, title/body drafts, Save, New, Delete, and Clear actions.
- Persists notes in the current player-data layout with caps of 64 notes, 80-character titles, and 2000-character bodies.
- Updates the `SAVE_NOTE` terminal action to accept JSON payloads like `{ "id": "...", "title": "...", "body": "..." }` while preserving the old newline payload path.
- Adds a server-rack menu and screen opened by empty-hand right-click.
- Preserves drive-item right-click insertion and sneak empty-hand drive ejection.
- Adds four rack drive slots, player inventory transfer, selected-drive details, drive records, network records, template selection, copy, remove, apply-template, clear, and rename actions.
- Validates server-side rack actions against the open rack menu, rack block position, selected slot, held drive component, network snapshot records, and loaded drive templates.
- Extends custom app JSON with `view: "records"`, `recordTypes`, `recordSources`, `includeArchived`, and `emptyText`.
- Adds the client-only Java renderer API: `SignalOsAppRenderer`, `SignalOsAppRenderContext`, and `SignalOsAppRenderers.register(type, renderer)`.
- Resolves terminal app rendering in order: built-in type, registered Java renderer, config record view, then unsupported metadata view.
- Extends `SignalOsDriveData` with label, add/replace, remove, clear, and template-merge helpers, with a 64-record cap enforced for player rack actions.

## Requirements

- Minecraft `26.1.2`
- NeoForge `26.1.2.29-beta` or newer in the `26.1.x` range
- Java `25+`
- `echocore` `1.1.0+`

## Verified In This Pass

- `:echosignalos:build --warning-mode all` passes.
- `:echosignalos:runGameTestServer --warning-mode all` passes.
- Added GameTests for note create/update/delete/clear, JSON note payload compatibility, rack menu validity, rack quick-move drive restrictions, drive template application, copying network records to a drive, removing drive records, and custom app JSON parsing/filter behavior.
- Existing app registration, duplicate rejection, data-drive component flow, workstation access validity, and computer-network discovery tests remain covered.

## Manual Smoke Checklist

1. Launch `:signalosexample:runExampleClient`.
2. Create or open a local test world.
3. Place `signalos:terminal` and open it by interacting with the block.
4. Open Notes, create or edit a note, save it, reopen the terminal, and confirm the note persists.
5. Place a workstation, server rack, network relay, and data drive.
6. Right-click the rack with the drive item to insert it, then empty-hand right-click the rack to open the rack screen.
7. Move drives between rack/player inventory, apply a template, copy a network record, remove a drive record, rename the drive, and clear the drive.
8. Sneak empty-hand right-click the rack and confirm a drive ejects.
9. Register a JSON app with `view: "records"` and confirm filtered records render with the configured empty text when no records match.
10. Register a Java renderer type and confirm render, click, key, and character hooks are called.
11. Open Files, Logs, Network Monitor, Settings, Data Vault, Echo Link, Missions, Archives, Rewards, and Diagnostics and confirm existing content remains reachable.
12. Run `/reload` and confirm JSON app/data/legacy content remains present afterward.

## Build Commands

```powershell
.\gradlew.bat :echosignalos:build --warning-mode all
.\gradlew.bat :signalosexample:build --warning-mode all
.\gradlew.bat :echosignalos:runGameTestServer --warning-mode all
python tools\validate_resources.py --addon-set beta
```

## Notes

- SignalOS still uses one active app at a time rather than draggable multi-window management.
- Computer block and item models remain placeholder-quality for this feature release.
- Custom app rendering should continue to use explicit app types or `view: "records"` rather than assuming every unknown JSON app has a rich surface.
