# ECHO: Convoy Protocol

Convoy Protocol adds the ruined-Earth travel layer for ECHO: field vehicles, fuel, cargo, roadside route contracts, checkpoint gating, and FIELD > Convoy Routes terminal support.

## Build Environment

This workspace currently expects JDK 25. In PowerShell:

```powershell
$env:JAVA_HOME='C:\Users\knox\AppData\Local\EchoToolchains\jdk25\jdk-25.0.3+9'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## V1 Smoke Test

1. Craft a Vehicle Workbench, Fuel Still, Battery Charging Pad, Vehicle Dock, Cargo Anchor, Field Repair Station, Convoy Beacon, and Roadside Signal Marker.
2. Use the Fuel Still to process charcoal into Fuel Canisters and the Battery Charging Pad to process redstone into Battery Cells.
3. Use the Vehicle Workbench to process a Vehicle Frame, two Scrap Tires, and a Fuel Canister into a Scrap Bike Kit.
4. Deploy the Scrap Bike Kit, right-click the Scrap Bike to claim/ride it, and verify fuel drains while driving.
5. Sneak/right-click the vehicle with cargo to load one stack item; sneak/right-click with an empty hand to unload the first cargo stack.
6. Damage the vehicle, place a Field Repair Station nearby, insert a Convoy Repair Kit, and verify vehicle damage decreases.
7. Park the vehicle near a Convoy Beacon and activate the Northern Route.
8. Park near a Roadside Signal Marker and complete the active route.
9. Open ECHO Terminal with `echoterminal` installed and use FIELD > Convoy Routes to scan, start, complete, and claim route rewards.

## Release Verification

Run from the workspace root after any patch:

```powershell
python tools\validate_resources.py --addon-set all
python tools\validate_gameplay_data.py
.\gradlew.bat :echoconvoyprotocol:build --no-daemon
.\gradlew.bat buildEchoWorkspace --no-daemon
.\gradlew.bat :echoconvoyprotocol:runGameTestServer --no-daemon
python tools\check_echo_runtime_logs.py --max-age-minutes 180
```

## Balance Notes

- Scrap Bike is intentionally cheap and fast, but has low cargo, low durability, and no battery buffer.
- Rover and Relay Truck have battery support for longer routes.
- Cargo Crawler trades speed for a large cargo bay.
- Rewards are tracked per player and route, so completed route payouts cannot be claimed twice.
- Roadside corridor structures are small jigsaw POIs: signal marker, repair pullout, and cargo checkpoint.
