"""
Big-scale POI generators for Crash Zone Wasteland (35-50 block footprint).
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import CRASH_ZONE_BLOCKS, pick_from
from .poi_primitives import (
    add_block,
    add_signal_marker,
    add_supply_cluster,
    blob_patch,
    bridge_section,
    courtyard_layout,
    elevated_platform,
    enforce_guardrails,
    fill,
    line,
    multi_room_layout,
    pillared_hall,
    ruined_wall,
    scatter,
    scrap_barricade,
    stairwell,
    underground_vault,
    watchtower,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_ship_breaking_yard(seed: int) -> BlockList:
    """Big: 42x38 ship breaking yard with multiple hulls, cutting stations, cranes, worker shelters."""
    rng = _r(seed)
    blocks: BlockList = []

    metal_block = "echoashfallprotocol:rusted_metal_debris"
    concrete = "minecraft:smooth_stone"

    # Yard surface
    fill(blocks, 2, 0, 2, 40, 0, 36, "minecraft:coarse_dirt")

    # === Hull 1: Large ship section (west) ===
    hull1_x, hull1_z = 4, 6
    hull1_w, hull1_d = 14, 28

    # Hull framework (partial)
    for x in range(hull1_x, hull1_x + hull1_w):
        for z in [hull1_z, hull1_z + hull1_d - 1]:
            add_block(blocks, x, 1, z, metal_block)
            if rng.random() > 0.5:
                add_block(blocks, x, 2, z, metal_block)
    for z in range(hull1_z, hull1_z + hull1_d):
        for x in [hull1_x, hull1_x + hull1_w - 1]:
            add_block(blocks, x, 1, z, metal_block)
            if rng.random() > 0.3:
                add_block(blocks, x, 2, z, metal_block)

    # Interior decks (cut open)
    for x in range(hull1_x + 2, hull1_x + hull1_w - 2, 4):
        for z in range(hull1_z + 2, hull1_z + hull1_d - 2):
            if rng.random() > 0.4:
                add_block(blocks, x, 1, z, "echoashfallprotocol:rusted_metal_sheet")

    # === Hull 2: Smaller vessel (northeast) ===
    hull2_x, hull2_z = 22, 4
    for x in range(hull2_x, hull2_x + 10):
        for z in range(hull2_z, hull2_z + 12):
            if x in [hull2_x, hull2_x + 9] or z in [hull2_z, hull2_z + 11]:
                add_block(blocks, x, 1, z, metal_block)
                if rng.random() > 0.6:
                    add_block(blocks, x, 2, z, metal_block)

    # === Main Crane (center) ===
    crane_base_x, crane_base_z = 20, 22
    # Crane tower
    for y in range(1, 12):
        add_block(blocks, crane_base_x, y, crane_base_z, metal_block)
        add_block(blocks, crane_base_x + 1, y, crane_base_z, metal_block)
        add_block(blocks, crane_base_x, y, crane_base_z + 1, metal_block)
        add_block(blocks, crane_base_x + 1, y, crane_base_z + 1, metal_block)

    # Crane arm (extends to both hulls)
    for x in range(hull1_x + 4, crane_base_x):
        add_block(blocks, x, 10, crane_base_z, metal_block)
    for x in range(crane_base_x + 2, hull2_x + 8):
        add_block(blocks, x, 10, crane_base_z, metal_block)

    # Crane cables
    line(blocks, (crane_base_x - 5, 10, crane_base_z), (crane_base_x - 5, 2, crane_base_z), "minecraft:chain")
    line(blocks, (crane_base_x + 6, 10, crane_base_z), (crane_base_x + 6, 2, crane_base_z), "minecraft:chain")

    # === Cutting Stations ===
    stations = [(18, 28), (28, 28), (18, 32)]
    for sx, sz in stations:
        elevated_platform(blocks, sx, sz, 3, 3, 2, "minecraft:stone_bricks", "minecraft:stone_bricks")
        add_block(blocks, sx + 1, 3, sz + 1, "minecraft:anvil")  # Cutting station
        add_block(blocks, sx, 3, sz + 1, "minecraft:barrel")  # Tools

    # === Worker Shelters ===
    for shelter_x, shelter_z in [(32, 26), (36, 30)]:
        fill(blocks, shelter_x, 1, shelter_z, shelter_x + 3, 2, shelter_z + 2, "echoashfallprotocol:rusted_metal_sheet")
        add_block(blocks, shelter_x + 1, 1, shelter_z, "minecraft:air")  # Door
        add_block(blocks, shelter_x + 2, 1, shelter_z + 1, "minecraft:chest")

    # === Scrap Piles (organized by type) ===
    # Metal scrap mountain
    for _ in range(40):
        sx = rng.randint(32, 38)
        sz = rng.randint(6, 16)
        height = rng.randint(1, 4)
        for y in range(1, height + 1):
            add_block(blocks, sx, y, sz, metal_block)

    # Cable/electrical pile
    for _ in range(20):
        cx = rng.randint(25, 30)
        cz = rng.randint(18, 24)
        add_block(blocks, cx, 1, cz, "minecraft:iron_bars")
        if rng.random() > 0.5:
            line(blocks, (cx, 1, cz), (cx + rng.randint(-2, 2), 1, cz + rng.randint(-2, 2)), "minecraft:iron_bars")

    # Scattered debris
    scatter(blocks, rng, 3, 3, 39, 35, 60, [
        "echoashfallprotocol:rusted_metal_debris",
        "echoashfallprotocol:concrete_rubble",
        "minecraft:iron_bars",
        "minecraft:chain",
    ])

    add_signal_marker(blocks, 38, 5, 5)

    return enforce_guardrails("ship_breaking_yard", blocks, 36, 34, 12, min_story_nodes=8, min_reward_nodes=4)


def generate_containment_facility_ruin(seed: int) -> BlockList:
    """Big: 48x44 containment facility with broken fences, quarantine buildings, monitoring station, lab entrance."""
    rng = _r(seed)
    blocks: BlockList = []

    wall_block = "minecraft:stone_bricks"
    fence_block = "minecraft:iron_bars"

    # Outer fence (perimeter - partially broken)
    fence_coords = [
        (4, 4, 44, 4),  # North
        (4, 4, 4, 40),  # West
        (44, 4, 44, 40),  # East
        (4, 40, 44, 40),  # South
    ]

    for x1, z1, x2, z2 in fence_coords:
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for z in range(min(z1, z2), max(z1, z2) + 1):
                if (x, z) in [(x1, z1), (x1, z2), (x2, z1), (x2, z2)] or x in [x1, x2] or z in [z1, z2]:
                    # Gap in fence (breach)
                    if rng.random() > 0.15:
                        for y in range(1, 3):
                            add_block(blocks, x, y, z, fence_block)

    # Fence gate (main entrance)
    add_block(blocks, 24, 1, 4, "minecraft:air")
    add_block(blocks, 24, 2, 4, "minecraft:air")

    # === Quarantine Building 1 (northwest) ===
    q1_x, q1_z = 8, 8
    q1_w, q1_d = 12, 10

    fill(blocks, q1_x, 0, q1_z, q1_x + q1_w, 0, q1_z + q1_d, "minecraft:smooth_stone")

    for x in range(q1_x, q1_x + q1_w + 1):
        for z in [q1_z, q1_z + q1_d]:
            for y in range(1, 4):
                if rng.random() > 0.3:  # Damaged
                    add_block(blocks, x, y, z, wall_block)
    for z in range(q1_z, q1_z + q1_d + 1):
        for x in [q1_x, q1_x + q1_w]:
            for y in range(1, 4):
                if rng.random() > 0.3:
                    add_block(blocks, x, y, z, wall_block)

    # Containment cells inside
    rooms = multi_room_layout(blocks, rng, q1_x + 2, q1_z + 2, q1_w - 4, q1_d - 4,
                               wall_block, "minecraft:smooth_stone", room_count=2)
    for rx, rz, rx2, rz2 in rooms:
        add_block(blocks, (rx + rx2) // 2, 1, (rz + rz2) // 2, "echoashfallprotocol:bio_processing_station")

    # === Quarantine Building 2 (northeast) ===
    q2_x, q2_z = 30, 8
    fill(blocks, q2_x, 0, q2_z, q2_x + 10, 0, q2_z + 10, "minecraft:smooth_stone")

    # This one is more damaged
    for x in range(q2_x, q2_x + 10):
        for z in [q2_z, q2_z + 10]:
            for y in range(1, 3):
                if rng.random() > 0.5:
                    add_block(blocks, x, y, z, wall_block)

    # Toxic spill from this building
    for _ in range(15):
        tx = rng.randint(q2_x - 2, q2_x + 12)
        tz = rng.randint(q2_z + 10, q2_z + 18)
        add_block(blocks, tx, 0, tz, "echoashfallprotocol:toxic_puddle")

    # === Monitoring Station (center) ===
    station_x, station_z = 22, 22

    # Elevated platform
    elevated_platform(blocks, station_x - 2, station_z - 2, 8, 8, 3, "minecraft:stone_bricks", "minecraft:stone_bricks")

    # Control room
    for x in range(station_x - 1, station_x + 3):
        for z in [station_z - 1, station_z + 3]:
            for y in range(4, 7):
                add_block(blocks, x, y, z, wall_block)
    for z in range(station_z - 1, station_z + 4):
        for x in [station_x - 1, station_x + 3]:
            for y in range(4, 7):
                add_block(blocks, x, y, z, wall_block)

    # Monitoring equipment
    add_block(blocks, station_x, 4, station_z, "minecraft:observer")
    add_block(blocks, station_x + 1, 4, station_z, "echoashfallprotocol:map_table")
    add_block(blocks, station_x, 4, station_z + 2, "minecraft:chest")

    # Watchtower
    watchtower(blocks, rng, station_x + 4, station_z + 4, 7, 8, wall_block, "minecraft:stone_bricks")

    # === Underground Lab Entrance (south) ===
    lab_x, lab_z = 24, 32

    # Bunker entrance structure
    fill(blocks, lab_x - 2, 1, lab_z, lab_x + 2, 3, lab_z + 3, wall_block)
    add_block(blocks, lab_x, 1, lab_z, "minecraft:air")  # Doorway

    # Stairs down
    for i in range(4):
        add_block(blocks, lab_x, 1 - i, lab_z + 1 + i, "minecraft:stone_bricks")

    # Underground lab
    vault_cx, vault_cz = underground_vault(blocks, rng, lab_x, lab_z + 4, 10, 8, 4, wall_block, "minecraft:smooth_stone")

    # Lab equipment in vault
    add_block(blocks, vault_cx - 3, -4, vault_cz - 2, "echoashfallprotocol:bio_processing_station")
    add_block(blocks, vault_cx + 2, -4, vault_cz + 2, "echoashfallprotocol:bio_processing_station")
    add_supply_cluster(blocks, rng, vault_cx - 2, vault_cz, 3, 3, containers=3)

    # Contamination effects
    for _ in range(30):
        cx = rng.randint(6, 42)
        cz = rng.randint(6, 38)
        add_block(blocks, cx, 1, cz, "echoashfallprotocol:nuclear_fungus")

    # Warning signs/barriers
    for _ in range(8):
        wx = rng.randint(5, 43)
        wz = rng.randint(5, 39)
        add_block(blocks, wx, 2, wz, "minecraft:observer")  # Warning sign

    return enforce_guardrails("containment_facility_ruin", blocks, 40, 36, 14, min_story_nodes=10, min_reward_nodes=4)
