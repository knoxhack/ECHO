# ECHO PowerGrid Smoke Test

## Build
1. Run workspace build.
2. Confirm `echopowergrid` compiles without errors.

## Dev Client
1. Launch dev client.
2. Open the **ECHO: PowerGrid** creative tab.
3. Verify all blocks and items are present with names and icons.

## Basic Grid
1. Place a **Scrap Burner Generator**.
2. Place a **Small Battery Bank** adjacent or connect with **Low Voltage Cable**.
3. Place a **Test Power Consumer** on the network.
4. Add burnable fuel to the generator (right-click with coal/charcoal).
5. Confirm generator shows burn time.
6. Wait a few seconds.
7. Confirm battery charges (right-click to inspect).
8. Confirm consumer is powered (right-click to inspect).

## Network Diagnostics
1. Place a **Power Meter** on the network.
2. Right-click it to see generation, demand, stored, and state.
3. Use the **Grid Diagnostic Tool** on the generator (local node info).
4. Sneak-use the diagnostic tool on the generator (network info).

## Cable Disconnect
1. Break a cable between generator and battery.
2. Confirm battery stops charging.
3. Replace the cable.
4. Confirm battery resumes charging.

## Persistence
1. Save and quit the world.
2. Re-enter the world.
3. Confirm generator buffer and battery energy persisted.

## Commands
1. Run `/echo_power inspect` while standing on the battery.
2. Run `/echo_power status`.
3. Run `/echo_power give_test_kit` (creative/op).
4. Run `/echo_power reset_network`.

## Dedicated Server
1. Launch dedicated server.
2. Join with a client.
3. Repeat basic grid steps.
4. Verify no client-only class crashes.

## Acceptance
- All blocks place and break correctly.
- No missing textures (purple/black squares).
- Energy flows from generator -> cable -> battery -> consumer.
- Chat diagnostics show meaningful data.
- Commands execute without errors.
- Save/load preserves energy.
