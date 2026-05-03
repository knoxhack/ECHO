"""
Medium-scale POI generators for Crash Zone Wasteland (20-30 block footprint).
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import CRASH_ZONE_BLOCKS, pick_from
from .poi_primitives import (
    add_block,
    add_signal_marker,
    add_supply_cluster,
    blob_patch,
    collapsed_section,
    elevated_platform,
    enforce_guardrails,
    fill,
    lean_to,
    line,
    ruined_wall,
    scatter,
    scrap_barricade,
    watchtower,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_salvager_worksite(seed: int) -> BlockList:
    """Medium: 26x24 crane structure, scrap piles, work benches, tool shed."""
    rng = _r(seed)
    blocks: BlockList = []

    metal_block = "echoashfallprotocol:rusted_metal_debris"
    concrete = "minecraft:smooth_stone"

    # Work yard surface
    fill(blocks, 2, 0, 2, 23, 0, 21, "minecraft:coarse_dirt")

    # === Crane Structure (north side) ===
    crane_x = 5
    # Crane base/platform
    fill(blocks, crane_x, 1, 3, crane_x + 8, 1, 6, concrete)

    # Crane tower
    for y in range(1, 10):
        add_block(blocks, crane_x + 4, y, 4, metal_block)
        add_block(blocks, crane_x + 4, y, 5, metal_block)
        add_block(blocks, crane_x + 5, y, 4, metal_block)
        add_block(blocks, crane_x + 5, y, 5, metal_block)

    # Crane arm (extending south)
    for z in range(6, 18):
        add_block(blocks, crane_x + 4, 8, z, metal_block)
        add_block(blocks, crane_x + 5, 8, z, metal_block)

    # Crane hook/cable
    hook_z = rng.randint(10, 16)
    line(blocks, (crane_x + 4, 8, hook_z), (crane_x + 4, 2, hook_z), "minecraft:chain")
    add_block(blocks, crane_x + 4, 1, hook_z, "minecraft:anvil")  # Heavy load

    # === Scrap Piles (organized by type) ===
    # Metal scrap pile
    for _ in range(25):
        sx = rng.randint(14, 22)
        sz = rng.randint(3, 8)
        add_block(blocks, sx, 1, sz, metal_block)
        if rng.random() > 0.5:
            add_block(blocks, sx, 2, sz, metal_block)

    # Concrete rubble pile
    for _ in range(20):
        sx = rng.randint(14, 22)
        sz = rng.randint(10, 15)
        add_block(blocks, sx, 1, sz, "echoashfallprotocol:concrete_rubble")
        if rng.random() > 0.6:
            add_block(blocks, sx, 2, sz, "echoashfallprotocol:concrete_rubble")

    # === Work Benches (cutting stations) ===
    for wx in [8, 12]:
        for wz in [12, 16]:
            add_block(blocks, wx, 1, wz, "minecraft:anvil")
            add_block(blocks, wx + 1, 1, wz, "minecraft:barrel")
            add_block(blocks, wx - 1, 1, wz, "minecraft:chest")

    # === Tool Shed ===
    shed_x, shed_z = 18, 18
    fill(blocks, shed_x, 1, shed_z, shed_x + 4, 2, shed_z + 3, "echoashfallprotocol:rusted_metal_sheet")
    add_block(blocks, shed_x + 2, 1, shed_z, "minecraft:air")  # Door
    add_block(blocks, shed_x + 1, 1, shed_z + 2, "minecraft:chest")
    add_block(blocks, shed_x + 3, 1, shed_z + 2, "minecraft:barrel")

    # Scattered tools/debris
    scatter(blocks, rng, 3, 3, 21, 20, 35, [
        "echoashfallprotocol:rusted_metal_debris",
        "echoashfallprotocol:concrete_rubble",
        "minecraft:iron_bars",
        "minecraft:chain",
    ])

    add_signal_marker(blocks, 22, 4, 4)

    return enforce_guardrails("salvager_worksite", blocks, 22, 20, 10, min_story_nodes=5, min_reward_nodes=3)


def generate_crash_site_large(seed: int) -> BlockList:
    """Medium: 28x28 impact crater with scattered wreckage, burned area, emergency supplies."""
    rng = _r(seed)
    blocks: BlockList = []

    # Impact crater (depression)
    crater_cx, crater_cz = 14, 14
    crater_radius = 8

    for x in range(crater_cx - crater_radius, crater_cx + crater_radius + 1):
        for z in range(crater_cz - crater_radius, crater_cz + crater_radius + 1):
            dist = ((x - crater_cx) ** 2 + (z - crater_cz) ** 2) ** 0.5
            if dist <= crater_radius:
                # Crater depth based on distance from center
                depth = max(0, int((crater_radius - dist) / 2))
                for y in range(-depth, 1):
                    if dist < crater_radius * 0.3:
                        add_block(blocks, x, y, z, "minecraft:magma_block")
                    elif dist < crater_radius * 0.6:
                        add_block(blocks, x, y, z, "minecraft:stone")
                    else:
                        add_block(blocks, x, y, z, "minecraft:gravel")

    # === Wreckage Fragments (scattered around crater) ===
    wreckage_positions = [
        (5, 5), (22, 6), (6, 22), (23, 23), (10, 4), (20, 20)
    ]

    for wx, wz in wreckage_positions:
        # Wreck frame
        for dx in range(3):
            for dz in range(3):
                if rng.random() > 0.3:
                    add_block(blocks, wx + dx, 1, wz + dz, "echoashfallprotocol:rusted_metal_debris")
                    if rng.random() > 0.5:
                        add_block(blocks, wx + dx, 2, wz + dz, "echoashfallprotocol:rusted_metal_sheet")

    # Large hull section (dominant wreck)
    hull_x, hull_z = 18, 8
    for x in range(hull_x, hull_x + 6):
        for z in range(hull_z, hull_z + 8):
            if rng.random() > 0.2:
                add_block(blocks, x, 1, z, "echoashfallprotocol:rusted_metal_debris")
                if rng.random() > 0.6:
                    add_block(blocks, x, 2, z, "echoashfallprotocol:rusted_metal_sheet")
                    if rng.random() > 0.4:
                        add_block(blocks, x, 3, z, "echoashfallprotocol:rusted_metal_sheet")

    # === Burned Area (south side) ===
    for x in range(8, 20):
        for z in range(20, 26):
            if rng.random() > 0.3:
                add_block(blocks, x, 1, z, "minecraft:black_wool")  # Charred ground
                add_block(blocks, x, 2, z, "minecraft:air")  # Scorch

    # === Emergency Supplies (survival cache) ===
    cache_x, cache_z = 4, 4
    # Supply drop container
    fill(blocks, cache_x, 1, cache_z, cache_x + 2, 1, cache_z + 2, "minecraft:smooth_stone")
    add_block(blocks, cache_x + 1, 2, cache_x + 1, "minecraft:chest")
    add_block(blocks, cache_x, 2, cache_z, "minecraft:barrel")
    add_block(blocks, cache_x + 2, 2, cache_z + 2, "minecraft:barrel")

    # Emergency beacon
    add_signal_marker(blocks, cache_x + 1, 5, cache_z + 1)

    # Additional supply container
    add_block(blocks, cache_x + 2, 2, cache_z, "minecraft:chest")

    # Scattered survival gear
    scatter(blocks, rng, 2, 2, 6, 6, 8, ["minecraft:white_wool", "minecraft:barrel", "minecraft:oak_log"])

    # Debris field
    scatter(blocks, rng, 0, 0, 27, 27, 50, [
        "echoashfallprotocol:rusted_metal_debris",
        "echoashfallprotocol:concrete_rubble",
        "minecraft:gravel",
    ])

    return enforce_guardrails("crash_site_large", blocks, 26, 26, 6, min_story_nodes=6, min_reward_nodes=3)


def generate_radiation_field(seed: int) -> BlockList:
    """Medium: 22x22 radiation barrels in pattern, warning signs, contaminated soil."""
    rng = _r(seed)
    blocks: BlockList = []

    # Contaminated ground
    fill(blocks, 2, 0, 2, 21, 0, 21, "echoashfallprotocol:contaminated_soil")

    # === Barrel Storage Grid ===
    # Central storage area with organized barrels
    storage_x, storage_z = 6, 6

    for row in range(4):
        for col in range(4):
            bx = storage_x + row * 3
            bz = storage_z + col * 3

            # Stack of barrels
            height = rng.randint(1, 3)
            for y in range(1, height + 1):
                add_block(blocks, bx, y, bz, "echoashfallprotocol:toxic_waste_barrel")

            # Spilled barrels
            if rng.random() > 0.6:
                add_block(blocks, bx + 1, 0, bz, "echoashfallprotocol:toxic_waste_barrel")
                # Leak effect
                add_block(blocks, bx + 1, 0, bz + 1, "echoashfallprotocol:acidic_sludge")

    # === Leak Pools ===
    for _ in range(8):
        lx = rng.randint(4, 19)
        lz = rng.randint(4, 19)
        # Small puddle
        add_block(blocks, lx, 0, lz, "echoashfallprotocol:toxic_puddle")
        if rng.random() > 0.5:
            add_block(blocks, lx + 1, 0, lz, "echoashfallprotocol:acidic_sludge")
        if rng.random() > 0.5:
            add_block(blocks, lx, 0, lz + 1, "echoashfallprotocol:acidic_sludge")

    # === Warning Barriers ===
    # Fence with warning tape (represented by iron bars)
    for x in range(2, 22, 3):
        add_block(blocks, x, 1, 2, "minecraft:iron_bars")
        add_block(blocks, x, 1, 21, "minecraft:iron_bars")
    for z in range(2, 22, 3):
        add_block(blocks, 2, 1, z, "minecraft:iron_bars")
        add_block(blocks, 21, 1, z, "minecraft:iron_bars")

    # Warning signs (represented by signs on fence posts)
    for pos in [(8, 2), (15, 2), (2, 11), (21, 11)]:
        add_block(blocks, pos[0], 2, pos[1], "minecraft:observer")  # Sign substitute

    # === Monitoring Station ===
    station_x, station_z = 18, 18
    elevated_platform(blocks, station_x, station_z, 3, 3, 2, "minecraft:stone_bricks", "minecraft:stone_bricks")
    add_block(blocks, station_x + 1, 3, station_z + 1, "minecraft:observer")  # Monitor
    add_block(blocks, station_x + 2, 3, station_x + 1, "minecraft:chest")  # Samples

    # Scattered warning signs/debris
    scatter(blocks, rng, 3, 3, 20, 20, 15, [
        "echoashfallprotocol:fallout_dust",
        "echoashfallprotocol:contaminated_soil",
        "minecraft:iron_bars",
    ])

    # Radioactive vegetation
    for _ in range(10):
        vx = rng.randint(4, 19)
        vz = rng.randint(4, 19)
        add_block(blocks, vx, 1, vz, "echoashfallprotocol:nuclear_fungus")

    # Additional supply cache
    add_block(blocks, 5, 1, 18, "minecraft:barrel")

    return enforce_guardrails("radiation_field", blocks, 20, 20, 4, min_story_nodes=4, min_reward_nodes=2)
