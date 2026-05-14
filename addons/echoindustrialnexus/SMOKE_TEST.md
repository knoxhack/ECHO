# ECHO Industrial Nexus Multiblock Smoke Test

Run from the repository root with Java 25:

```powershell
$env:JAVA_HOME='C:\Users\knox\.jdks\temurin-25'
python tools\validate_resources.py
.\gradlew.bat :echomultiblockcore:compileJava --no-daemon --no-configuration-cache
.\gradlew.bat :echorendercore:compileJava --no-daemon --no-configuration-cache
.\gradlew.bat :echoterminal:compileJava :echolens:compileJava :echoholomap:compileJava --no-daemon --no-configuration-cache
.\gradlew.bat :echoindustrialnexus:compileJava --no-daemon --no-configuration-cache
.\gradlew.bat :echologisticsnetwork:compileJava --no-daemon --no-configuration-cache
.\gradlew.bat :echoindustrialnexus:build --no-daemon --no-configuration-cache
.\gradlew.bat :echoindustrialnexus:runGameTestServer --no-daemon --no-configuration-cache
.\gradlew.bat :echoindustrialnexus:runIndustrialClient --no-daemon --no-configuration-cache
```

Manual client checklist:

- Verify the `ECHO: Industrial Nexus` creative tab includes controllers, buses, casings, crates, robotics, tool heads, blueprints, upgrade chips, and Industrial materials.
- Place each controller and sneak-use or use the Factory Diagnostic Tool to confirm incomplete diagnostics are readable.
- Build the Industrial Assembly Line from `data/echoindustrialnexus/echo_multiblocks/industrial_assembly_line.json`.
- Install an Industrial Welder Head into the Robotic Arm Mount.
- Put 4 Refined Plates, 1 Servo Motor, and 1 Industrial Circuit in the Input Depot Crate.
- Right-click the formed Industrial Assembly Line Controller and confirm the Factory Command GUI opens.
- Press 1 on Weld Reinforced Machine Frame and confirm inputs are consumed, the arm animates, progress updates, and a Reinforced Machine Frame appears in the Output Depot Crate.
- Restock enough ingredients for three runs, press 3, and confirm the task queue advances tasks in order.
- Restock enough ingredients for five runs, press 5, and confirm the queue caps at the visible capacity instead of overfilling.
- Press CLEAR with a queued task and confirm the queue clears.
- Create a blocked task, press RETRY, and confirm the blocked reason clears while already-consumed inputs are not duplicated.
- Press REVALIDATE and confirm the structure status remains online.
- Open ECHO Terminal, select Industrial Nexus, confirm Factory Command appears before Missions, press REFRESH, select the Assembly Line, queue a task remotely, toggle Logistics auto-restock, set the target to x1/x3/x5, then clear/retry/revalidate from Terminal.
- With ECHO Lens installed, Deep Scan the Assembly Line Controller, Robotic Arm Mount, Input Depot Crate, and Output Depot Crate; verify status, alert, tool, heat, queue, and safe inventory summaries.
- With ECHO HoloMap installed, sync the map and verify the Assembly Line marker appears on the Multiblocks layer with alert, integrity, and restock summary text.
- With ECHO Logistics Network installed, connect a Logistics network, Drone Delivery Dock, stocked storage, and Smart Storage Label to an Input Depot Crate, then press REQ for a recipe and confirm a courier dispatches to the depot.
- Enable AUTO on the controller, set target x3, select the matching Industrial loadout on an Auto-Restock Station, and confirm Logistics dispatches only when the Input Depot Crate falls below the minimum run threshold.
- Try the same task with the tool head removed and confirm the diagnostic says a robotic tool is missing.
- Remove a required Reinforced Machine Casing and confirm the structure becomes incomplete or damaged.
- Replace the casing, revalidate the controller, and confirm the facility returns online.
- Save and reload the world; formed state should safely persist or revalidate.
- Launch a dedicated GameTest server to catch client-only classloading mistakes.

Known 1.0.0 limitations:

- The controller GUI is functional but uses a hand-drawn first-pass style rather than final art.
- Upgrade chips are craftable and tagged, but only effects already supported by MultiblockCore are active.
- Nexus Furnace Array is a definition and integration hook, not a full Nexus Protocol processing route yet.
- Logistics Network request and auto-restock routing are optional and only appear when ECHO Logistics Network is present; Factory Command shows controls and status, but Logistics still owns route discovery, stock checks, in-flight caps, and delivery.
