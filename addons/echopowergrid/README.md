# ECHO: PowerGrid

**Mod ID:** `echopowergrid`  
**Version:** 0.1.0  
**Tagline:** Restore the grid. Power the signal.

## What is ECHO PowerGrid?

ECHO PowerGrid is the shared first-party power generation, storage, cable, substation, overload, brownout, and facility power system for the full ECHO/Ashfall ecosystem. It provides a real playable energy backbone for terminals, signal towers, industrial machines, multiblock facilities, convoy depots, orbital launchpads, agriculture domes, blackbox archives, nexus stabilizers, and armory fabricators.

## Power Unit

**EP** = Echo Power. All internal energy math uses EP.

## Blocks

### Generators
- **Hand Crank Generator** - 5 EP/t, manual/scrap tier, no fuel
- **Scrap Burner Generator** - 40 EP/t, burns fuel, outpost tier
- **Solar Panel** - 10 EP/t during day, outpost tier
- **Creative Power Source** - Infinite EP, admin/testing only

### Storage
- **Small Battery Bank** - 20,000 EP capacity, 100 EP/t in/out, outpost tier

### Cables
- **Low Voltage Cable** - 100 EP/t transfer, scrap/outpost tier
- **Industrial Cable** - 500 EP/t transfer, industrial tier

### Control
- **Outpost Substation** - Network coordinator and monitor
- **Emergency Breaker** - Trips on overload, player-resettable
- **Power Meter** - Shows local network stats on use

### Test
- **Creative Power Sink** - Consumes unlimited EP for testing
- **Test Power Consumer** - 20 EP/t consumer for validation

## Items

- **Copper Coil** - Crafting component
- **Scrap Wire** - Basic wire material
- **Insulated Wire** - Protected wire for cables
- **Power Cell** - Small energy component
- **Battery Core** - Battery crafting core
- **Fuse** - Breaker component
- **Breaker Switch** - Control component
- **Grid Diagnostic Tool** - Right-click to inspect nodes and networks

## Building Your First Grid

1. Craft a **Scrap Burner Generator**.
2. Place a **Small Battery Bank** next to it (or connect with **Low Voltage Cable**).
3. Place a **Test Power Consumer** on the network.
4. Add fuel to the generator.
5. Watch the battery charge and the consumer receive power.
6. Use a **Power Meter** or **Substation** to check network status.

## Brownout and Overload

- **Brownout**: Demand exceeds supply. Consumers receive partial power. Network state becomes `BROWNOUT`.
- **Overload**: Flow exceeds cable/transfer limits. If enabled, breakers trip. Network state becomes `OVERLOADED`.
- **Breaker Reset**: Right-click a tripped Emergency Breaker to restore the circuit.

## FE Compatibility

FE bridge is scaffolded in the config (`enableFeBridge`, `feToEpRatio`, `epToFeRatio`). A full FE adapter is planned for the next pass.

## Commands

- `/echo_power status` - Show grid status at your position
- `/echo_power inspect` - Inspect the power node at your position
- `/echo_power networks` - Summary of loaded networks
- `/echo_power debug_chunk` - Count power nodes in current chunk
- `/echo_power give_test_kit` - Give creative test blocks (op only)
- `/echo_power set_energy <amount>` - Set battery energy (op only)
- `/echo_power reset_network` - Mark network dirty/rebuild (op only)

## Config

See `echopowergrid-common.toml`:
- `general.enabled` - Enable/disable PowerGrid
- `network.maxNetworkSize` - Node cap per network
- `overload` - Overload and breaker behavior
- `brownout` - Brownout thresholds
- `compat` - FE bridge ratios
- `performance` - Tick budgets and update intervals

## Optional Integrations

- **Terminal** - Future power dashboard
- **Lens** - Future power scan data
- **HoloMap** - Future power overlay
- **MultiblockCore** - Future facility power API
- **Industrial Nexus** - Future machine power consumption
- **WorldCore** - Solar weather hooks
- **RuntimeGuard** - Tick budget respect

## Future Roadmap

- Advanced substations with priority routing
- Terminal power dashboard widgets
- Lens scan support for power nodes
- HoloMap power overlay
- FE bridge polish
- MultiblockCore consumer integration
- Industrial Nexus power conversion
- Nexus power quality expansion
- Advanced cable path loss
- Power meter GUI
