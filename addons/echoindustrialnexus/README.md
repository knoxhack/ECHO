# ECHO: Industrial Nexus

Production-ready included addon chapter for the ECHO mod family.

Industrial Nexus turns Ashfall survival into midgame infrastructure: Thermal Flux generators, recipe-driven machines, sided automation, item ducts, Flux ducts, NeoForge fluid tanks and pipes, overheating, scrubber safe zones, procedural industrial POIs, Furnace Warden progression, and soft ECHO Terminal missions.

## Status

Production Completion pass.

- Canonical source: `addons/echoindustrialnexus`
- Gradle project: `:echoindustrialnexus`
- Mod id: `echoindustrialnexus`
- Release jar: `addons/echoindustrialnexus/build/libs/echoindustrialnexus-0.1.0.jar`
- Required compile/runtime dependency: `echocore`
- Optional soft integrations: `echoterminal`, Ashfall Protocol, Nexus Protocol, Orbital Remnants, Stationfall, Blackbox Protocol

Industrial Nexus loads and plays without optional sibling chapters. When ECHO Terminal is present, it registers a shared Industrial Nexus chapter, mission provider, archive entries, actions for factory scans, POI hints, reward cache claims, and a terminal recipe provider for Industrial processing data.

## Production Features

- Thermal Flux network: generators, capacitor banks, Flux ducts, machine storage, generation stats, and controller scans.
- Machine gameplay: input, catalyst, output, byproduct, upgrade, sided inventory, recipe duration, TF cost/generation, heat gain/cooling, remote shutdown, warnings, and custom menu/screen sync.
- NeoForge fluid gameplay: registered Industrial fluids, machine fluid capabilities, internal tanks, bucket/cell fallback recipes, and active fluid pipes with tier capacity, transfer rate, filtering, loss, and hazardous leak behavior.
- Terminal Recipe Index integration: `echoindustrialnexus:industrial_processing` JSON is surfaced with item/tag ingredients, catalysts, byproducts, input/output fluids, Thermal Flux cost or generation, heat gain, process duration, and machine categories.
- Overheating: Cool, Warm, Hot, Critical, and Meltdown states with heat sinks, coolant, scrubber cooling, emergency shutdown modules, fire/leak fallout, and Terminal progress recording.
- Industrial Scrubber: Air, Radiation, Blight, Station, and Cooling modes with fallback safe-zone records and soft reflection hooks into sibling systems when they are installed.
- World content: configurable procedural Abandoned Thermal Plants, Rusted Factory Complexes, Geothermal Drill Sites, Reactor Cooling Stations, and Nexus Heat Exchanger Ruins with loot, hazards, schematics, and Warden arena support.
- Boss progression: Furnace Warden activation, phased fight state, participant reward credit, trophy/core drops, and one-time Terminal reward eligibility.

## Build And Validation

From the repository root:

```powershell
python tools\validate_resources.py
.\gradlew.bat :echoindustrialnexus:compileJava --no-daemon --no-configuration-cache
.\gradlew.bat :echoindustrialnexus:build --no-daemon --no-configuration-cache
.\gradlew.bat :echoindustrialnexus:runGameTestServer --no-daemon --no-configuration-cache
.\gradlew.bat :echoindustrialnexus:runIndustrialClient --no-daemon --no-configuration-cache
```

Manual smoke checklist after the client launches:

- Craft a Scrap Dynamo, Copper Flux Duct, Ore Grinder, Scrap Fuel, and Thermal Wrench from survival materials or Industrial POI loot.
- Start the Scrap Dynamo with Scrap Fuel or a lava bucket and confirm Thermal Flux stores in the generator, the lava bucket returns an empty bucket, and the Ore Grinder receives power through a Copper Flux Duct.
- Process iron ore into Iron Dust, retrieve output by shift-use, then break the machine and Smart Duct to confirm machine inventory and duct filter stacks drop once.
- Open a machine GUI and confirm progress, Thermal Flux, heat, fluid bars, warnings, side config, and scrubber mode render.
- Build dirty power, automate filters, and complete the Dense Alloy chain.
- Fill/drain the Fluid Refiner and Water Purifier through cells and fluid pipes.
- Cycle Industrial Scrubber modes and confirm safe-zone progress updates.
- Use ECHO Terminal Industrial Nexus actions: scan factory, view POI hint, claim cache once.
- Open the ECHO Terminal Recipe Index and confirm Industrial machine categories show JSON-driven processing recipes, fluid notes, heat, Thermal Flux, catalysts, and byproducts.
- Generate or locate a procedural POI and verify loot/hazard placement.
- Activate and defeat the Furnace Warden and verify participant reward credit.

## Release Checklist

- Release jar: `addons/echoindustrialnexus/build/libs/echoindustrialnexus-0.1.0.jar`
- Required validation: JSON parse, `python tools\validate_resources.py`, `:echoindustrialnexus:compileJava`, `:echoindustrialnexus:build`, and `:echoindustrialnexus:runGameTestServer`.
- Current RC note: Industrial JSON/resource validation, compile, build, and the shared `runGameTestServer` suite pass in this checkout.
- Optional compat matrix: Terminal, Nexus, Orbital, Stationfall, and Blackbox present/absent paths.
- Ashfall source is not included under `addons`; Industrial keeps soft hooks for `ToxicAirHelper.cleanAirAround` and `RadiationHelper.reduceRadiationAround`.
- Known RC limitation: final manual balance numbers and client UX sign-off still depend on a human playthrough of the full factory-to-Warden route.

## Release Notes

See `CHANGELOG.md` for the production completion pass details.
