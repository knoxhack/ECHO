"""
Additional POI variants for the release structure polish pass.

These are deliberately template-scale scenes rather than dungeons: each one
adds a clear silhouette, a small story beat, conservative cache placement, and
biome-specific clutter while staying inside existing Ashfall registries.
"""

from __future__ import annotations

import random

from .poi_primitives import (
    BlockList,
    add_block,
    add_debris_scatter,
    add_hazard_leak,
    add_machine_cluster,
    add_signal_marker,
    add_supply_cluster,
    bridge_section,
    dedupe_blocks,
    elevated_platform,
    enforce_guardrails,
    fill,
    hollow_rect,
    line,
    pipe_run,
    ruined_wall,
    scrap_barricade,
    watchtower,
)


METAL = [
    "echoashfallprotocol:twisted_metal",
    "echoashfallprotocol:rusted_metal_debris",
    "echoashfallprotocol:rusted_metal_sheet",
    "echoashfallprotocol:drop_pod_hull",
]
RUBBLE = [
    "echoashfallprotocol:rubble",
    "echoashfallprotocol:concrete_rubble",
    "echoashfallprotocol:concrete_chunk",
    "minecraft:gravel",
]
ROAD = ["echoashfallprotocol:oil_stained_concrete", "minecraft:blackstone", "minecraft:gravel"]


def _rng(seed: int) -> random.Random:
    return random.Random(seed)


def _base_pad(blocks: BlockList, x1: int, z1: int, x2: int, z2: int, palette: list[str]) -> None:
    for x in range(x1, x2 + 1):
        for z in range(z1, z2 + 1):
            blocks.append((x, 0, z, palette[(x * 31 + z * 17) % len(palette)], None))


def _wrecked_vehicle(blocks: BlockList, rng: random.Random, x: int, z: int, length: int, facing_x: bool) -> None:
    palette = METAL + ["echoashfallprotocol:cable_bundle"]
    if facing_x:
        fill(blocks, x, 1, z, x + length, 2, z + 2, rng.choice(palette))
        line(blocks, (x + 1, 3, z), (x + length - 1, 3, z + 1), "echoashfallprotocol:drop_pod_hull")
        for wx in (x + 1, x + length - 1):
            add_block(blocks, wx, 0, z - 1, "minecraft:blackstone")
            add_block(blocks, wx, 0, z + 3, "minecraft:blackstone")
    else:
        fill(blocks, x, 1, z, x + 2, 2, z + length, rng.choice(palette))
        line(blocks, (x, 3, z + 1), (x + 1, 3, z + length - 1), "echoashfallprotocol:drop_pod_hull")
        for wz in (z + 1, z + length - 1):
            add_block(blocks, x - 1, 0, wz, "minecraft:blackstone")
            add_block(blocks, x + 3, 0, wz, "minecraft:blackstone")


def _pipe_post(blocks: BlockList, x: int, z: int, height: int, block_id: str = "echoashfallprotocol:corroded_pipe") -> None:
    for y in range(1, height + 1):
        add_block(blocks, x, y, z, block_id)
    add_block(blocks, x, height + 1, z, "minecraft:lightning_rod")


def _tank(blocks: BlockList, cx: int, cz: int, radius: int, height: int, wall: str, cap: str) -> None:
    for y in range(1, height + 1):
        for dx in range(-radius, radius + 1):
            for dz in range(-radius, radius + 1):
                edge = abs(dx) == radius or abs(dz) == radius
                if edge and abs(dx) + abs(dz) <= radius * 2:
                    add_block(blocks, cx + dx, y, cz + dz, wall)
    fill(blocks, cx - radius + 1, height + 1, cz - radius + 1, cx + radius - 1, height + 1, cz + radius - 1, cap)


def generate_burned_convoy(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 27, 17, ["echoashfallprotocol:scorched_ash", "minecraft:blackstone", "minecraft:gravel"])
    fill(blocks, 2, 0, 7, 25, 0, 10, "echoashfallprotocol:oil_stained_concrete")
    _wrecked_vehicle(blocks, rng, 4, 5, 6, True)
    _wrecked_vehicle(blocks, rng, 13, 9, 7, True)
    _wrecked_vehicle(blocks, rng, 20, 3, 5, False)
    add_hazard_leak(blocks, rng, 10, 9, 2, "echoashfallprotocol:toxic_puddle", "echoashfallprotocol:scorched_ash")
    for x, z in [(3, 11), (8, 4), (18, 12), (24, 8)]:
        add_block(blocks, x, 1, z, "minecraft:campfire", {"lit": "true", "facing": "north", "signal_fire": "false", "waterlogged": "false"})
    add_supply_cluster(blocks, rng, 16, 4, 4, 3, containers=2, clutter_palette=METAL)
    add_debris_scatter(blocks, rng, 0, 0, 27, 17, 22, METAL + RUBBLE, y=1)
    return enforce_guardrails("burned_convoy", blocks, 24, 16, 4, min_anchor_blocks=4, min_clutter_blocks=22)


def generate_cargo_lift_wreck(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 29, 23, ["echoashfallprotocol:scorched_ash", "minecraft:gravel", "minecraft:blackstone"])
    fill(blocks, 7, 0, 5, 22, 0, 17, "echoashfallprotocol:oil_stained_concrete")
    for x, z in [(7, 5), (22, 5), (7, 17), (22, 17)]:
        for y in range(1, 8):
            add_block(blocks, x, y, z, "echoashfallprotocol:twisted_metal")
    bridge_section(blocks, 7, 5, 22, 5, 8, "echoashfallprotocol:drop_pod_hull", "minecraft:iron_bars")
    bridge_section(blocks, 22, 5, 22, 17, 7, "echoashfallprotocol:drop_pod_hull", "minecraft:iron_bars")
    line(blocks, (8, 7, 6), (18, 2, 15), "minecraft:chain")
    line(blocks, (10, 6, 6), (20, 1, 16), "echoashfallprotocol:cable_bundle")
    for cx, cz in [(10, 9), (15, 13), (20, 10)]:
        fill(blocks, cx, 1, cz, cx + 2, 2, cz + 3, "echoashfallprotocol:supply_crate")
        add_block(blocks, cx + 1, 3, cz + 1, rng.choice(METAL))
    add_machine_cluster(blocks, rng, 11, 16, ["echoashfallprotocol:scrap_press", "echoashfallprotocol:battery_bank", "echoashfallprotocol:power_node"])
    add_supply_cluster(blocks, rng, 3, 18, 4, 3, containers=2, clutter_palette=METAL)
    add_debris_scatter(blocks, rng, 1, 1, 28, 22, 26, METAL + RUBBLE, y=1)
    return enforce_guardrails("cargo_lift_wreck", blocks, 28, 22, 9, min_anchor_blocks=6, min_clutter_blocks=24)


def generate_road_checkpoint(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 25, 21, ["echoashfallprotocol:ash_layer", "minecraft:gravel", "echoashfallprotocol:rubble"])
    fill(blocks, 0, 0, 8, 25, 0, 13, "echoashfallprotocol:oil_stained_concrete")
    for x in range(1, 25, 3):
        add_block(blocks, x, 1, 7, "minecraft:iron_bars")
        add_block(blocks, x, 1, 14, "minecraft:iron_bars")
    scrap_barricade(blocks, rng, 6, 8, 15, 13, METAL + RUBBLE)
    watchtower(blocks, rng, 20, 4, 1, 5, "echoashfallprotocol:rusted_metal_sheet", "minecraft:smooth_stone")
    hollow_rect(blocks, 3, 3, 10, 7, 1, 3, "echoashfallprotocol:concrete_rubble")
    add_block(blocks, 6, 1, 5, "echoashfallprotocol:relay_station")
    add_signal_marker(blocks, 12, 15, height=5)
    add_supply_cluster(blocks, rng, 4, 15, 5, 3, containers=2, clutter_palette=RUBBLE)
    add_debris_scatter(blocks, rng, 0, 0, 25, 21, 20, METAL + RUBBLE, y=1)
    return enforce_guardrails("road_checkpoint", blocks, 24, 20, 7, min_anchor_blocks=5, min_clutter_blocks=20)


def generate_subway_stairwell(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 23, 27, ["echoashfallprotocol:concrete_rubble", "minecraft:stone", "minecraft:gravel"])
    fill(blocks, 0, 0, 11, 23, 0, 16, "echoashfallprotocol:oil_stained_concrete")
    hollow_rect(blocks, 7, 7, 16, 20, 1, 4, "echoashfallprotocol:rebar_block")
    for step in range(8):
        fill(blocks, 9, 1 + step // 2, 18 - step, 14, 1 + step // 2, 18 - step, "minecraft:stone_bricks")
    fill(blocks, 8, 1, 8, 15, 1, 13, "minecraft:blackstone")
    for x in range(8, 16, 2):
        add_block(blocks, x, 2, 8, "minecraft:iron_bars")
        add_block(blocks, x, 3, 20, "echoashfallprotocol:shattered_glass")
    pipe_run(blocks, (5, 3, 9), (19, 3, 9), "echoashfallprotocol:power_cable", "echoashfallprotocol:concrete_rubble")
    add_machine_cluster(blocks, rng, 4, 20, ["echoashfallprotocol:signal_scanner", "echoashfallprotocol:battery_bank", "echoashfallprotocol:field_med_bay"])
    add_supply_cluster(blocks, rng, 17, 17, 4, 3, containers=2, clutter_palette=RUBBLE)
    add_debris_scatter(blocks, rng, 1, 1, 22, 26, 24, RUBBLE + METAL, y=1)
    return enforce_guardrails("subway_stairwell", blocks, 22, 26, 5, min_anchor_blocks=4, min_clutter_blocks=22)


def generate_sludge_drain(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 23, 23, ["echoashfallprotocol:acid_mud", "echoashfallprotocol:toxic_puddle", "echoashfallprotocol:contaminated_soil"])
    for r in range(2, 8):
        for x in range(12 - r, 12 + r + 1):
            for z in range(12 - r, 12 + r + 1):
                if abs(abs(x - 12) + abs(z - 12) - r) <= 1:
                    add_block(blocks, x, 1, z, "echoashfallprotocol:corroded_pipe")
    add_hazard_leak(blocks, rng, 12, 12, 5, "echoashfallprotocol:acidic_sludge", "echoashfallprotocol:acid_mud")
    for angle in [(12, 4), (12, 20), (4, 12), (20, 12)]:
        pipe_run(blocks, (12, 2, 12), (angle[0], 2, angle[1]), "echoashfallprotocol:corroded_pipe", "echoashfallprotocol:concrete_rubble")
    elevated_platform(blocks, 2, 3, 5, 4, 2, "minecraft:oak_planks", "echoashfallprotocol:dead_wood_log")
    add_block(blocks, 4, 3, 5, "echoashfallprotocol:bio_processing_station")
    add_supply_cluster(blocks, rng, 16, 3, 4, 4, containers=2, clutter_palette=["echoashfallprotocol:toxic_waste_barrel", "echoashfallprotocol:toxic_moss"])
    add_debris_scatter(blocks, rng, 1, 1, 22, 22, 20, ["echoashfallprotocol:ooze_crystal", "echoashfallprotocol:toxic_waste_barrel", "echoashfallprotocol:toxic_moss"], y=1)
    return enforce_guardrails("sludge_drain", blocks, 22, 22, 4, min_anchor_blocks=5, min_clutter_blocks=20)


def generate_pipe_pump_house(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 25, 21, ["echoashfallprotocol:acid_mud", "echoashfallprotocol:contaminated_soil", "minecraft:mud"])
    hollow_rect(blocks, 5, 5, 15, 14, 1, 4, "echoashfallprotocol:rusted_metal_sheet")
    fill(blocks, 6, 0, 6, 14, 0, 13, "echoashfallprotocol:oil_stained_concrete")
    fill(blocks, 7, 5, 7, 13, 5, 12, "minecraft:dark_oak_planks")
    pipe_run(blocks, (1, 2, 10), (24, 2, 10), "echoashfallprotocol:corroded_pipe", "echoashfallprotocol:concrete_rubble")
    pipe_run(blocks, (11, 2, 1), (11, 2, 20), "echoashfallprotocol:item_pipe", "echoashfallprotocol:concrete_rubble")
    add_machine_cluster(blocks, rng, 8, 8, ["echoashfallprotocol:contaminant_condenser", "echoashfallprotocol:bio_processing_station", "echoashfallprotocol:battery_bank"])
    _pipe_post(blocks, 3, 3, 5)
    _pipe_post(blocks, 22, 18, 4)
    add_hazard_leak(blocks, rng, 18, 15, 3, "echoashfallprotocol:acidic_sludge", "echoashfallprotocol:toxic_puddle")
    add_supply_cluster(blocks, rng, 17, 4, 4, 4, containers=2, clutter_palette=["echoashfallprotocol:toxic_waste_barrel", "echoashfallprotocol:corroded_pipe"])
    add_debris_scatter(blocks, rng, 1, 1, 24, 20, 18, ["echoashfallprotocol:ooze_crystal", "echoashfallprotocol:toxic_moss", "echoashfallprotocol:toxic_waste_barrel"], y=1)
    return enforce_guardrails("pipe_pump_house", blocks, 24, 20, 6, min_anchor_blocks=5, min_clutter_blocks=20)


def generate_radiation_beacon_line(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 31, 15, ["echoashfallprotocol:fallout_dust", "echoashfallprotocol:contaminated_soil", "minecraft:gravel"])
    fill(blocks, 1, 0, 6, 30, 0, 9, "echoashfallprotocol:concrete_rubble")
    for x in range(4, 30, 6):
        for y in range(1, 6):
            add_block(blocks, x, y, 7, "echoashfallprotocol:rusted_metal_sheet")
        add_block(blocks, x, 6, 7, "minecraft:redstone_torch", {"lit": "true"})
        add_block(blocks, x + 1, 1, 7, "echoashfallprotocol:radiation_block")
        add_block(blocks, x - 1, 1, 7, "echoashfallprotocol:uranium_crystal")
    for z in (3, 12):
        for x in range(2, 31, 4):
            add_block(blocks, x, 1, z, "minecraft:iron_bars")
    add_hazard_leak(blocks, rng, 12, 4, 2, "echoashfallprotocol:radioactive_sludge", "echoashfallprotocol:fallout_dust")
    add_hazard_leak(blocks, rng, 24, 11, 2, "echoashfallprotocol:radioactive_sludge", "echoashfallprotocol:fallout_dust")
    add_supply_cluster(blocks, rng, 2, 10, 5, 3, containers=2, clutter_palette=["echoashfallprotocol:toxic_waste_barrel", "echoashfallprotocol:concrete_rubble"])
    add_debris_scatter(blocks, rng, 0, 0, 31, 15, 20, ["echoashfallprotocol:toxic_waste_barrel", "echoashfallprotocol:concrete_rubble", "echoashfallprotocol:rusted_metal_debris"], y=1)
    return enforce_guardrails("radiation_beacon_line", blocks, 30, 14, 7, min_anchor_blocks=8, min_clutter_blocks=20)


def generate_reactor_gatehouse(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 31, 25, ["echoashfallprotocol:fallout_dust", "minecraft:gravel", "echoashfallprotocol:contaminated_soil"])
    fill(blocks, 0, 0, 11, 31, 0, 14, "echoashfallprotocol:oil_stained_concrete")
    hollow_rect(blocks, 7, 5, 24, 20, 1, 5, "echoashfallprotocol:concrete_rubble")
    for z in range(6, 20):
        if z in (12, 13):
            continue
        for y in range(1, 6):
            add_block(blocks, 15, y, z, "echoashfallprotocol:rebar_block")
            add_block(blocks, 16, y, z, "echoashfallprotocol:rebar_block")
    for x in (8, 23):
        for z in (6, 19):
            for y in range(1, 8):
                add_block(blocks, x, y, z, "echoashfallprotocol:concrete_rubble")
            add_block(blocks, x, 8, z, "echoashfallprotocol:radiation_block")
    add_machine_cluster(blocks, rng, 10, 9, ["echoashfallprotocol:contaminant_condenser", "echoashfallprotocol:relay_station", "echoashfallprotocol:battery_bank"])
    add_hazard_leak(blocks, rng, 20, 16, 3, "echoashfallprotocol:radioactive_sludge", "echoashfallprotocol:fallout_dust")
    add_supply_cluster(blocks, rng, 18, 7, 4, 4, containers=2, clutter_palette=["echoashfallprotocol:toxic_waste_barrel", "echoashfallprotocol:rusted_metal_debris"])
    add_debris_scatter(blocks, rng, 1, 1, 30, 24, 26, RUBBLE + ["echoashfallprotocol:toxic_waste_barrel", "echoashfallprotocol:uranium_crystal"], y=1)
    return enforce_guardrails("reactor_gatehouse", blocks, 30, 24, 9, min_anchor_blocks=8, min_clutter_blocks=24)


def generate_frozen_comms_tower(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 25, 25, ["echoashfallprotocol:permafrost", "minecraft:snow_block", "minecraft:packed_ice"])
    hollow_rect(blocks, 4, 15, 12, 22, 1, 4, "minecraft:polished_diorite")
    fill(blocks, 5, 0, 16, 11, 0, 21, "echoashfallprotocol:frozen_conduit")
    for y in range(1, 16):
        add_block(blocks, 18, y, 12, "echoashfallprotocol:frozen_conduit")
        if y % 3 == 0:
            add_block(blocks, 17, y, 12, "minecraft:iron_bars")
            add_block(blocks, 19, y, 12, "minecraft:iron_bars")
            add_block(blocks, 18, y, 11, "minecraft:iron_bars")
            add_block(blocks, 18, y, 13, "minecraft:iron_bars")
    add_block(blocks, 18, 16, 12, "minecraft:lightning_rod")
    line(blocks, (18, 11, 12), (8, 5, 18), "echoashfallprotocol:power_cable")
    add_machine_cluster(blocks, rng, 6, 18, ["echoashfallprotocol:relay_station", "echoashfallprotocol:thermal_array", "echoashfallprotocol:battery_bank"])
    add_supply_cluster(blocks, rng, 14, 18, 4, 3, containers=2, clutter_palette=["echoashfallprotocol:blue_ice_crystal", "minecraft:ice"])
    add_debris_scatter(blocks, rng, 1, 1, 24, 24, 24, ["echoashfallprotocol:blue_ice_crystal", "echoashfallprotocol:frozen_conduit", "minecraft:snow"], y=1)
    return enforce_guardrails("frozen_comms_tower", blocks, 24, 24, 17, min_anchor_blocks=5, min_clutter_blocks=20)


def generate_cryo_tank_field(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 27, 21, ["echoashfallprotocol:permafrost", "minecraft:ice", "minecraft:snow_block"])
    fill(blocks, 4, 0, 4, 23, 0, 17, "echoashfallprotocol:frozen_conduit")
    for cx, cz in [(8, 8), (14, 7), (20, 9), (10, 15), (18, 15)]:
        _tank(blocks, cx, cz, 2, 4, "minecraft:packed_ice", "echoashfallprotocol:blue_ice_crystal")
        add_block(blocks, cx, 1, cz, "echoashfallprotocol:frozen_conduit")
        add_block(blocks, cx, 5, cz, "minecraft:chain")
    pipe_run(blocks, (8, 2, 8), (20, 2, 9), "echoashfallprotocol:frozen_conduit", "minecraft:packed_ice")
    pipe_run(blocks, (10, 2, 15), (18, 2, 15), "echoashfallprotocol:power_cable", "minecraft:packed_ice")
    add_machine_cluster(blocks, rng, 4, 17, ["echoashfallprotocol:thermal_array", "echoashfallprotocol:research_lab", "echoashfallprotocol:contaminant_condenser"])
    add_supply_cluster(blocks, rng, 20, 2, 4, 3, containers=2, clutter_palette=["echoashfallprotocol:blue_ice_crystal", "minecraft:ice"])
    add_debris_scatter(blocks, rng, 1, 1, 26, 20, 20, ["echoashfallprotocol:blue_ice_crystal", "minecraft:snow", "echoashfallprotocol:frozen_conduit"], y=1)
    return enforce_guardrails("cryo_tank_field", blocks, 26, 20, 6, min_anchor_blocks=5, min_clutter_blocks=20)


def generate_rail_signal_yard(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 31, 23, ["echoashfallprotocol:oil_stained_concrete", "minecraft:gravel", "echoashfallprotocol:rusted_metal_debris"])
    for z in (6, 11, 16):
        for x in range(1, 31):
            add_block(blocks, x, 0, z, "minecraft:iron_bars")
            if x % 3 == 0:
                add_block(blocks, x, 0, z + 1, "minecraft:oak_planks")
    watchtower(blocks, rng, 25, 5, 1, 6, "echoashfallprotocol:rusted_metal_sheet", "echoashfallprotocol:oil_stained_concrete")
    add_signal_marker(blocks, 8, 3, height=6)
    add_signal_marker(blocks, 17, 19, height=5)
    fill(blocks, 4, 1, 13, 12, 2, 18, "echoashfallprotocol:twisted_metal")
    add_machine_cluster(blocks, rng, 15, 7, ["echoashfallprotocol:scrap_press", "echoashfallprotocol:factory_controller", "echoashfallprotocol:power_node"])
    add_supply_cluster(blocks, rng, 2, 2, 5, 3, containers=2, clutter_palette=METAL)
    add_debris_scatter(blocks, rng, 1, 1, 30, 22, 26, METAL + RUBBLE + ["echoashfallprotocol:item_pipe"], y=1)
    return enforce_guardrails("rail_signal_yard", blocks, 30, 22, 8, min_anchor_blocks=6, min_clutter_blocks=24)


def generate_factory_pipe_gate(seed: int) -> BlockList:
    rng = _rng(seed)
    blocks: BlockList = []
    _base_pad(blocks, 0, 0, 31, 25, ["echoashfallprotocol:oil_stained_concrete", "echoashfallprotocol:concrete_rubble", "minecraft:gravel"])
    for x in (8, 23):
        for y in range(1, 9):
            add_block(blocks, x, y, 12, "echoashfallprotocol:rusted_metal_sheet")
        add_block(blocks, x, 9, 12, "minecraft:lightning_rod")
    for z in (8, 16):
        pipe_run(blocks, (4, 5, z), (27, 5, z), "echoashfallprotocol:item_pipe", "echoashfallprotocol:concrete_rubble", support_spacing=4)
    bridge_section(blocks, 8, 12, 23, 12, 8, "echoashfallprotocol:rusted_metal_sheet", "minecraft:iron_bars")
    hollow_rect(blocks, 2, 3, 10, 9, 1, 4, "echoashfallprotocol:concrete_rubble")
    hollow_rect(blocks, 21, 16, 29, 22, 1, 4, "echoashfallprotocol:concrete_rubble")
    add_machine_cluster(blocks, rng, 4, 6, ["echoashfallprotocol:factory_controller", "echoashfallprotocol:scrap_press", "echoashfallprotocol:battery_bank"])
    add_machine_cluster(blocks, rng, 23, 19, ["echoashfallprotocol:power_node", "echoashfallprotocol:scrap_press", "echoashfallprotocol:item_pipe"])
    add_supply_cluster(blocks, rng, 13, 18, 5, 4, containers=2, clutter_palette=METAL)
    add_debris_scatter(blocks, rng, 1, 1, 30, 24, 28, METAL + RUBBLE + ["echoashfallprotocol:power_cable"], y=1)
    return enforce_guardrails("factory_pipe_gate", blocks, 30, 24, 10, min_anchor_blocks=6, min_clutter_blocks=24)
