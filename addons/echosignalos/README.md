# SignalOS

SignalOS is a standalone Echo-compatible computer tech addon for NeoForge. It provides a full-screen desktop shell, utility apps, networked computer blocks, portable data drives, and a defensive bridge into Echo Core state.

SignalOS is not a replacement for `echoterminal`. It owns the computer OS fantasy while continuing to expose legacy-compatible mission, archive, reward, and diagnostic surfaces inside the new shell.

## Current V1 Scope

SignalOS currently ships:

- `signalos:terminal` as the base access point and `signalos:workstation` as the stronger access tier.
- `signalos:server_rack`, `signalos:network_relay`, and `signalos:data_drive` for computer-network gameplay.
- A desktop shell with an app launcher, status bar, active app view, notifications, settings surface, and shared visual tokens.
- Built-in apps: Home, Files, Notes, Logs, Network Monitor, Settings, Data Vault, Echo Link, Missions, Archives, Rewards, and Diagnostics.
- Server-owned network discovery around owned terminals/workstations, including linked racks, relays, drives, and data records.
- Persistent player preferences, operator notes, archive read state, mission claimed state, and pending terminal reward counts.
- Java registration APIs and datapack JSON loading for apps, data records, drive templates, chapters, missions, and archives.
- Optional Echo Core integration through `EchoCoreServices` for module reports, diagnostics, route records, and platform summaries.
- A soft KubeJS-friendly bridge through `Java.loadClass`, without a hard KubeJS runtime dependency.

Current limitations:

- V1 is a single-active-app desktop, not a draggable multi-window manager.
- Notes support server-side create/clear actions but do not yet expose full freeform text entry widgets.
- Drive templates are loaded as data definitions; custom generation or addon code can apply them to drive stacks.
- Custom app entries can appear in the launcher, but only known built-in app `type` values have rich client renderers in V1.
- The block/item models intentionally use simple placeholder geometry until production art is added.

## Java API

```java
SignalOsApi.registerApp(SignalOsApp.builder("example:field_files")
        .title("Field Files")
        .type("files")
        .summary("Browse recovered field records.")
        .order(25)
        .accentColor(0x66E8FF)
        .build());

SignalOsApi.registerDataProvider(new SignalOsDataProvider() {
    @Override
    public Identifier id() {
        return SignalOsApi.id("example:cache_records");
    }

    @Override
    public List<SignalOsDataRecord> records(Player player) {
        return List.of(SignalOsDataRecord.of(
                "example:records/cache_note",
                "Cache Note",
                "record",
                "Example Module",
                "A server-synced record visible in Files and Data Vault.",
                10));
    }
});

SignalOsApi.registerComputerPeripheral(new SignalOsPeripheralProvider() {
    @Override
    public Identifier id() {
        return SignalOsApi.id("example:beacons");
    }

    @Override
    public List<SignalOsPeripheralProvider.Peripheral> peripherals(Player player) {
        return List.of(new SignalOsPeripheralProvider.Peripheral(
                SignalOsApi.id("example:peripherals/beacon"),
                "relay",
                "Beacon Peripheral",
                "ONLINE",
                player.blockPosition(),
                1));
    }
});
```

Legacy terminal content remains supported:

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

- `data/<namespace>/signalos/apps/*.json`
- `data/<namespace>/signalos/data_records/*.json`
- `data/<namespace>/signalos/drive_templates/*.json`
- `data/<namespace>/signalos/chapters/*.json`
- `data/<namespace>/signalos/missions/*.json`
- `data/<namespace>/signalos/archives/*.json`

KubeJS packs can put the same files under the KubeJS `data/` folder. See [DATA_FORMATS.md](DATA_FORMATS.md) for field-level examples.

SignalOS intentionally ships a `kubejs.classfilter.txt` soft bridge instead of `kubejs.plugins.txt`; KubeJS' addon guidance reserves `kubejs.plugins.txt` for compile-time KubeJS plugin classes.

## Computer Gameplay

- Terminals and workstations anchor an operator network when owned by the player.
- Server racks store installed data drives and expose their records to Files, Logs, Data Vault, and Echo Link views.
- Network relays increase the discovered network footprint by participating in the scan.
- Data drives carry portable `SignalOsDriveData` components with small text-oriented records.
- Player persistent data stores settings, notes, recently exposed records, mission claims, archive read state, and terminal reward inbox counts.

Network identity is server-owned and derived from dimension, anchor position, and owner. SignalOS works without rich Echo addons, then surfaces more records when Echo Core providers return module, diagnostic, route, discovery, faction, or profile data.

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

- Keep `echoterminal` imports and content separate; SignalOS integrates through Echo Core and shared service contracts.
- Keep Echo integration optional and defensive.
- Keep custom app renderers behind explicit app types or client-side renderer registration rather than assuming every JSON app has a rich surface.
