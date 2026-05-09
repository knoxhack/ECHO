# ECHO: Agriculture Reclamation 0.1.1 Draft Changelog

## Crop Utility Outputs

- Added Bio-Reactor utility routing for harvested non-food crops:
  - `Medicinal Aloe` produces Bio-Gel and can grant `echoashfallprotocol:bandage` when Ashfall Protocol is loaded.
  - `Signal Fungus` produces increased Bio-Gel.
  - `Cryo Moss` produces Purification Enzyme.
  - `Nexus Orchid` produces Gene Sample and can grant `echonexusprotocol:nexus_gel` when Nexus Protocol is loaded.
- Added Compost Recycler utility routing:
  - `Filter Reed` produces increased Soil Nutrient Mix and can grant `echoashfallprotocol:plant_fiber` when Ashfall Protocol is loaded.
  - `Cryo Moss` and `Signal Fungus` produce increased Soil Nutrient Mix.
- Added standalone fallback recipes:
  - `Filter Reed` crafts into paper.
  - `Medicinal Aloe` plus Bio-Gel crafts into Purification Enzyme.
- Added produce tooltips describing crop utility roles.

## Scope Notes

- Optional Ashfall and Nexus outputs use runtime registry lookup only.
- No greenhouse rule, Terminal layout, Core milestone, biome restoration, crop roster, machine block, or public API changes are included in this slice.
