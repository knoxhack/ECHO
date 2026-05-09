# SignalOS

SignalOS is a reusable NeoForge terminal framework for mission, archive, reward, diagnostics, and chapter-based modpack UIs.

## Current MVP Scope

SignalOS currently ships:

- `signalos:terminal` block/item, server menu, keybind open path, and a dark tactical terminal screen.
- Built-in pages for missions, archives, reward inbox, and diagnostics.
- Server-authoritative mission completion checks, claimed mission state, archive read state, and terminal reward inbox storage.
- Java registration APIs and datapack JSON loading for simple chapters, missions, and archives.
- A soft KubeJS-friendly bridge through `Java.loadClass`, without a hard KubeJS runtime dependency.

Current limitations:

- `echocore` `1.1.0+` is required for this MVP.
- `TerminalTheme` is available as API data but is not rendered by the screen yet.
- Custom `TerminalPage` entries provide tab metadata only unless their type is one of `missions`, `archives`, `rewards`, or `diagnostics`.
- Mission graph, POI maps, faction atlas, JEI/GameStages/FTB/Patchouli bridges, and native KubeJS plugin events are not part of this module yet.
- The terminal block/item model intentionally uses a vanilla-textured model until production art is added.

## Java API

```java
SignalOsApi.registerChapter(TerminalChapter.builder("example:field_ops")
        .title("Field Ops")
        .section("progress")
        .page("missions")
        .page("archives")
        .build());

SignalOsApi.registerMission(TerminalMission.builder("example:secure_cache")
        .chapter("example:field_ops")
        .title("Secure the Cache")
        .description("Find shelter and recover the field cache.")
        .objective("Find shelter")
        .completionAdvancement("minecraft:story/root")
        .reward("minecraft:bread", 4)
        .build());
```

## JSON Content

Datapacks can place content in:

- `data/<namespace>/signalos/chapters/*.json`
- `data/<namespace>/signalos/missions/*.json`
- `data/<namespace>/signalos/archives/*.json`

KubeJS packs can put the same files under the KubeJS `data/` folder. SignalOS also exposes `com.knoxhack.signalos.kubejs.SignalOSEvents` and `SignalOSKubeBridge` for script-side registration through `Java.loadClass`.

SignalOS intentionally ships a `kubejs.classfilter.txt` soft bridge instead of `kubejs.plugins.txt`; KubeJS' addon guidance reserves `kubejs.plugins.txt` for compile-time KubeJS plugin classes.

### Chapter JSON

```json
{
  "title": "Field Ops",
  "section": "progress",
  "order": 10,
  "accentColor": 6737151,
  "pages": ["missions", "archives", "rewards", "diagnostics"]
}
```

Supported page types are `missions`, `archives`, `rewards`, and `diagnostics`. Unknown page types can be registered as metadata, but the MVP screen will show them as unsupported.

### Mission JSON

```json
{
  "chapter": "example:field_ops",
  "title": "Secure the Cache",
  "description": "Find shelter and recover the field cache.",
  "objectives": ["Find shelter"],
  "order": 0,
  "icon": "minecraft:chest",
  "completionAdvancement": "minecraft:story/root",
  "rewardClaim": true,
  "displayRewards": [
    {"item": "minecraft:bread", "count": 4}
  ]
}
```

`completionAdvancement` is optional. If it is omitted, the mission is treated as complete by the current MVP completion resolver. `displayRewards` entries are always shown in mission detail; when `rewardClaim` is true, valid reward stacks are also stored server-side after the mission is claimed.

### Archive JSON

```json
{
  "chapter": "example:field_ops",
  "title": "Field Ops Brief",
  "group": "Field Ops",
  "status": "OPEN",
  "order": 0,
  "lines": [
    "This record is loaded from datapack JSON."
  ]
}
```

Archive records can also set `"locked": true`; locked records are visible but cannot be marked read by the built-in archive action.

## KubeJS Example

For reloadable pack content, prefer JSON in the KubeJS `data/` folder. Use the soft bridge when a script needs to assemble content procedurally.

```js
const SignalOSEvents = Java.loadClass('com.knoxhack.signalos.kubejs.SignalOSEvents')

ServerEvents.loaded(event => {
  SignalOSEvents.content(event => {
    event.clear()
    event.chapter('signalosexample:field_ops')
    .title('Field Ops')
    .section('progress')
    .page('missions')
    .page('archives')
    .register()

    event.mission('signalosexample:secure_cache')
    .chapter('signalosexample:field_ops')
    .title('Secure the Cache')
    .description('Find shelter and recover the field cache.')
    .objective('Find shelter')
    .completionAdvancement('minecraft:story/root')
    .reward('minecraft:bread', 4)
    .register()
  })
})
```

## Build And Release Checks

From the workspace root:

```powershell
.\gradlew.bat :echosignalos:build --warning-mode all
.\gradlew.bat :signalosexample:build --warning-mode all
.\gradlew.bat :echosignalos:runGameTestServer --warning-mode all
python tools\validate_resources.py --addon-set beta
```

Release safety notes:

- Keep `echoterminal` imports and content separate; SignalOS is not a migration of the existing ECHO Terminal module.
- Keep KubeJS optional until a real plugin event is added.
- Do not publish docs that imply theme rendering or custom page renderers are active before those layers exist.
