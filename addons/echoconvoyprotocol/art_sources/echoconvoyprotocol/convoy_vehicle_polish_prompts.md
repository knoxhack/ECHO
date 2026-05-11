# Convoy Vehicle Polish Imagegen Source

Generated with the built-in `image_gen` tool for the convoy vehicle visual polish pass.

Source output copied from:
`C:\Users\knox\.codex\generated_images\019e1405-24c8-7892-b3c8-9d9ea23380ba\ig_03f01a80d3325f68016a01119846d88190b3c89da49dedfa36.png`

Workspace reference:
`art_sources/echoconvoyprotocol/generated_vehicle_polish_texture_reference.png`

Final translated atlas sheet:
`art_sources/echoconvoyprotocol/convoy_vehicle_polish_atlas_sheet.png`

Final in-game textures:

- `assets/echoconvoyprotocol/textures/entity/scrap_bike.png`
- `assets/echoconvoyprotocol/textures/entity/wasteland_rover.png`
- `assets/echoconvoyprotocol/textures/entity/cargo_crawler.png`
- `assets/echoconvoyprotocol/textures/entity/armored_relay_truck.png`

## Built-In Imagegen Prompt

```text
Use case: stylized-concept
Asset type: Minecraft mod entity texture reference sheet
Primary request: Create a single square reference image for four Minecraft NeoForge convoy vehicle UV texture sheets: scrap_bike, wasteland_rover, cargo_crawler, armored_relay_truck. This is reference art for later clean UV atlas translation, not a final in-game atlas.
Scene/backdrop: no environment, flat neutral dark background, no shadows or perspective scene.
Subject: four separate 256x256-style Minecraft pixel-art texture sheet panels arranged in a 2x2 grid. Each panel should visually suggest UV islands for blocky cuboid Java entity model parts. No labels, no readable text.
Style/medium: crisp Minecraft pixel art, clean modded asset sheet, blocky cuboid model materials, sharp edges, no blur, no photorealism, no painterly concept art.
Composition/framing: each panel has organized UV-like rectangles and small repeated detail islands for wheels, treads, body panels, cab glass, cargo, scanners, antennae, dock clamps, shield plates, damage plates, and lights.
Color palette: ECHO/Ashfall dark gunmetal, faded olive, dusty tan/orange, rust brown, black rubber, gray steel, worn wood, cyan ECHO lights, pale yellow headlights, muted red rear lights.
Materials/textures: scratched painted metal, raw steel, rubber tread, reinforced glass, wood slats, leather/fabric bags, cables, bolts, vents, hazard plates.
Details: panel seams, bolts, scratches, dust, chipped paint, rust, tire tread, hazard striping, cyan scanner lenses and status bars. Transparent-looking unused spaces are acceptable, but keep the image square and easy to inspect.
Constraints: no readable text, no watermark, no logo, no characters, no environment, no perspective vehicle render; texture sheet/reference only.
```

## UV And Part Map

- `0-63`: tire/tread rubber, hubs, forks, bumpers, lower running gear.
- `64-127`: chassis, hood, cab, glass, engine blocks, scanner/light modules.
- `128-191`: shields, cargo markers, damage plates, side armor, crates, tie-downs.
- `192-255`: cargo/tarp/fuel tanks, dock clamps, warnings, repeated trim and hazard plating.

The final PNGs are deterministic translated atlases derived from the imagegen reference and model part map. Raw generated art is not used directly as an in-game UV atlas.

## Validation Checklist

- Each final texture is `256x256` ARGB PNG.
- Texture filenames remain unchanged.
- Cyan light zones align with scanner/status/light parts.
- Cargo, shield, dock, and damage UV zones have visible material differences.
- Relay truck has hazard-striping zones; other vehicles avoid overusing hazard markings.

## Screenshot-Driven Visual Repair Pass

Date: 2026-05-10

The first in-game screenshots showed that the compile-safe polish pass still read too much like slab geometry:

- `scrap_bike`: too wide and boxy for a two-wheel vehicle; tires read as rectangular blocks.
- `wasteland_rover`: recognizable, but tires, cab, scanner, and roof hardware still needed stronger shape language.
- `cargo_crawler`: cargo mass and tracks were too tall and slab-like; road wheels did not read clearly.
- `armored_relay_truck`: front face was too flat; relay hardware and six-wheel identity needed more silhouette.

### Revised Implementation Notes

- The Java model now uses segmented wheel assemblies instead of single rectangular tire cubes.
- The cargo crawler track base is lower and split into belt rails, ground pads, top pads, road wheels, slatted bed walls, crates, tarp, and tie-downs.
- The relay truck cab was rebuilt with cheek armor, grille blocks, lower bumper, side suspension blocks, roof scanner bar, dish lip, relay module, and hazard plates.
- The scrap bike was narrowed around its existing passenger attachment point and rebuilt with fork pieces, grips, frame rails, engine cradle, exhaust bands, saddlebags, headlamp, and a smaller cargo roll.

### Final Visual Repair Atlas

`art_sources/echoconvoyprotocol/convoy_vehicle_visual_repair_atlas_sheet.png`

The v2 atlases are deterministic UV-aware pixel sheets. They do not use raw generated image output directly. Broad fallback material bands cover every sampled cube face while key regions provide cleaner islands for:

- `0-63`: segmented rubber tires, road wheels, tracks, hubs, axles, bumpers, lower running gear.
- `64-127`: painted body panels, cab armor, hood plates, windows, grilles, lamps.
- `128-191`: side armor, shield plates, cargo markers, scanner hardware, hazard strips, damage plates.
- `192-255`: crates, tarp, cargo tie-downs, status lights, rear lights, trim islands.

### Revised GPT Image 2 Reference Prompt

```text
Create a Minecraft modded entity texture reference sheet for four repaired convoy vehicles: scrap_bike, wasteland_rover, cargo_crawler, armored_relay_truck. Texture size target: four 256x256 UV-style panels. The texture must support blocky cuboid Java entity models with segmented wheel assemblies, lower crawler treads, slatted cargo bed parts, armored cab panels, relay scanner hardware, dock clamps, shield plates, cargo load markers, damage plates, and ECHO status lights. Style: crisp Minecraft pixel art, clean modded Minecraft asset, readable in-game, no photorealism, no blur, no painterly concept art, no environment background, no readable text. Palette: ECHO/Ashfall dark gunmetal, faded olive, dusty tan paint, rust brown, black rubber, gray steel, worn wood, cyan ECHO lights, pale yellow headlights, muted red rear lights. Materials: scratched painted metal, raw steel, rubber tread, reinforced glass, wood crates, tarp fabric, cables, bolts, vents, hazard plates. Details: fewer random speckles, stronger panel boundaries, readable tire treads, clean glass regions, chipped paint only on edges, dust on horizontal panels, rust at bolts and lower plates. UV layout: broad filled material zones with reusable islands for mirrored wheels and repeated trim, unique islands for front panels, cabs, scanners, cargo, relay core, and light panels. The output is reference art only; final in-game assets must be translated into clean UV atlases at assets/echoconvoyprotocol/textures/entity/<vehicle>.png.
```

### Repair Validation Checklist

- Spawn each vehicle after resource reload and confirm no purple/black missing texture.
- Inspect side, front three-quarter, and rear three-quarter views.
- Confirm wheels read as segmented tires rather than solid black boxes.
- Confirm cargo crawler sits lower and tracks read separately from cargo load.
- Confirm relay truck reads as a six-wheel signal vehicle from the front.
- Confirm mounted rider positions still work without changing entity dimensions or passenger heights.
