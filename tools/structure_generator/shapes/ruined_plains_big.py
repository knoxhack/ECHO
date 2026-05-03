"""
Big-scale POI generators for Ruined Plains (35-50 block footprint).
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import PLAINS_BLOCKS, pick_from
from .poi_primitives import (
    add_block,
    add_signal_marker,
    add_supply_cluster,
    blob_patch,
    bridge_section,
    courtyard_layout,
    doorway,
    elevated_platform,
    enforce_guardrails,
    fill,
    hollow_rect,
    lean_to,
    line,
    multi_room_layout,
    pillared_hall,
    ruined_wall,
    scatter,
    scrap_barricade,
    stairwell,
    tent_frame,
    underground_vault,
    watchtower,
    window_opening,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_ruined_outpost(seed: int) -> BlockList:
    """Big: 40x36 multi-building compound with watchtower, barracks, armory, courtyard."""
    rng = _r(seed)
    blocks: BlockList = []

    wall_block = "minecraft:cobblestone"
    floor_block = "minecraft:stone_bricks"

    # Outer walls (compound perimeter)
    for x in range(0, 41):
        for y in range(1, 4):
            if rng.random() > 0.25:  # Some gaps in wall
                add_block(blocks, x, y, 0, wall_block)
                add_block(blocks, x, y, 36, wall_block)
    for z in range(0, 37):
        for y in range(1, 4):
            if rng.random() > 0.25:
                add_block(blocks, 0, y, z, wall_block)
                add_block(blocks, 40, y, z, wall_block)

    # Main gate
    fill(blocks, 18, 1, 0, 22, 3, 0, "minecraft:air")

    # Ground preparation
    fill(blocks, 2, 0, 2, 38, 0, 34, "minecraft:coarse_dirt")

    # === Building 1: Barracks (west side) ===
    barracks_x, barracks_z = 4, 4
    barracks_w, barracks_d = 12, 10

    # Floor
    fill(blocks, barracks_x, 1, barracks_z, barracks_x + barracks_w, 1, barracks_z + barracks_d, floor_block)

    # Walls (partially ruined)
    for x in range(barracks_x, barracks_x + barracks_w + 1):
        for z in [barracks_z, barracks_z + barracks_d]:
            for y in range(2, 5):
                if rng.random() > 0.3:
                    add_block(blocks, x, y, z, wall_block)
    for z in range(barracks_z, barracks_z + barracks_d + 1):
        for x in [barracks_x, barracks_x + barracks_w]:
            for y in range(2, 5):
                if rng.random() > 0.3:
                    add_block(blocks, x, y, z, wall_block)

    # Doorways
    doorway(blocks, barracks_x + 5, 2, barracks_z, "north", width=2, height=2)

    # Bunk beds
    for bx in range(barracks_x + 2, barracks_x + barracks_w - 1, 2):
        add_block(blocks, bx, 2, barracks_z + 2, "minecraft:oak_planks")  # Bed frame
        add_block(blocks, bx, 3, barracks_z + 2, "minecraft:white_wool")  # Mattress
        add_block(blocks, bx, 2, barracks_z + 4, "minecraft:chest")  # Footlocker

    # === Building 2: Armory (east side) ===
    armory_x, armory_z = 24, 4
    armory_w, armory_d = 12, 10

    fill(blocks, armory_x, 1, armory_z, armory_x + armory_w, 1, armory_z + armory_d, floor_block)

    # Reinforced walls
    for x in range(armory_x, armory_x + armory_w + 1):
        for z in [armory_z, armory_z + armory_d]:
            for y in range(2, 5):
                block = "minecraft:iron_block" if rng.random() > 0.7 else wall_block
                add_block(blocks, x, y, z, block)

    # Weapon racks
    for wx in range(armory_x + 2, armory_x + armory_w - 1, 2):
        add_block(blocks, wx, 2, armory_z + 2, "echoashfallprotocol:weapon_rack")
        add_block(blocks, wx, 2, armory_z + armory_d - 2, "echoashfallprotocol:weapon_rack")

    # Storage
    add_supply_cluster(blocks, rng, armory_x + 2, armory_z + 5, 3, 3, containers=4,
                       clutter_palette=["minecraft:barrel", "minecraft:chest"])

    # === Central Courtyard ===
    courtyard_x, courtyard_z = 14, 18
    courtyard_w, courtyard_d = 12, 14

    # Courtyard floor
    fill(blocks, courtyard_x, 1, courtyard_z, courtyard_x + courtyard_w, 1, courtyard_z + courtyard_d, "minecraft:gravel")

    # Central fire
    add_block(blocks, courtyard_x + courtyard_w // 2, 1, courtyard_z + courtyard_d // 2, "minecraft:campfire")

    # Training dummies
    add_block(blocks, courtyard_x + 2, 1, courtyard_z + 3, "minecraft:armor_stand")
    add_block(blocks, courtyard_x + courtyard_w - 2, 1, courtyard_z + 3, "minecraft:armor_stand")

    # === Watchtower (southeast corner) ===
    watchtower(blocks, rng, 34, 26, 2, 10, wall_block, "minecraft:oak_planks")

    # === Command building (north center) ===
    cmd_x, cmd_z = 16, 26
    cmd_w, cmd_d = 8, 8

    # Elevated platform
    elevated_platform(blocks, cmd_x, cmd_z, cmd_w, cmd_d, 2, "minecraft:oak_planks", "minecraft:oak_log")

    # Command room
    for x in range(cmd_x, cmd_x + cmd_w + 1):
        for z in [cmd_z, cmd_z + cmd_d]:
            for y in range(3, 6):
                add_block(blocks, x, y, z, wall_block)
    for z in range(cmd_z, cmd_z + cmd_d + 1):
        for x in [cmd_x, cmd_x + cmd_w]:
            for y in range(3, 6):
                add_block(blocks, x, y, z, wall_block)

    # Map table
    add_block(blocks, cmd_x + 4, 3, cmd_z + 4, "echoashfallprotocol:map_table")

    # Stairs to platform
    for i in range(3):
        add_block(blocks, cmd_x - 1 - i, 2 - i, cmd_z + cmd_d // 2, "minecraft:oak_stairs")

    # === Underground bunker ===
    bunker_cx, bunker_cz = 20, 20
    underground_vault(blocks, rng, bunker_cx, bunker_cz, 6, 6, 3, wall_block, floor_block)

    # Scatter debris
    scatter(blocks, rng, 2, 2, 38, 34, 50, ["minecraft:gravel", "minecraft:cobblestone", "minecraft:oak_log"])

    # Signal markers
    add_signal_marker(blocks, 38, 4, 4)
    add_signal_marker(blocks, 4, 4, 32)

    return enforce_guardrails("ruined_outpost", blocks, 35, 32, 12, min_story_nodes=8, min_reward_nodes=4)


def generate_settlement_ruins(seed: int) -> BlockList:
    """Big: 45x40 settlement with multiple foundations, church ruins, town square, hidden bunker."""
    rng = _r(seed)
    blocks: BlockList = []

    wall_block = "minecraft:cobblestone"
    wood_block = "minecraft:oak_planks"

    # Ground preparation
    fill(blocks, 2, 0, 2, 43, 0, 38, "minecraft:coarse_dirt")

    # === House foundations (scattered ruins) ===
    house_positions = [
        (5, 5, 8, 7),   # Northwest
        (18, 5, 9, 8),  # North center
        (32, 5, 8, 7),  # Northeast
        (5, 20, 7, 8),  # West
        (35, 20, 7, 8), # East
    ]

    for hx, hz, hw, hd in house_positions:
        # Foundation only (mostly destroyed)
        fill(blocks, hx, 1, hz, hx + hw, 1, hz + hd, "minecraft:stone_bricks")

        # Partial walls
        for x in range(hx, hx + hw + 1):
            for z in [hz, hz + hd]:
                if rng.random() > 0.5:
                    for y in range(2, 4):
                        if rng.random() > 0.3:
                            add_block(blocks, x, y, z, wall_block)

        # Chimney remains
        if rng.random() > 0.3:
            chimney_x, chimney_z = hx + hw - 1, hz + 1
            for y in range(1, 6):
                add_block(blocks, chimney_x, y, chimney_z, "minecraft:cobblestone")

    # === Church ruins (large prominent structure) ===
    church_x, church_z = 16, 22
    church_w, church_d = 12, 14

    # Church foundation
    fill(blocks, church_x, 1, church_z, church_x + church_w, 1, church_z + church_d, "minecraft:stone_bricks")

    # Standing walls (more complete than houses)
    for x in range(church_x, church_x + church_w + 1):
        for z in [church_z, church_z + church_d]:
            for y in range(2, 7):
                add_block(blocks, x, y, z, wall_block)

    # Gothic arch windows
    window_opening(blocks, church_x + 3, 3, church_z, 2, 3, "north")
    window_opening(blocks, church_x + 7, 3, church_z, 2, 3, "north")

    # Collapsed roof beams
    for x in range(church_x + 1, church_x + church_w):
        add_block(blocks, x, 6, church_z + church_d // 2, "minecraft:oak_log")

    # Altar area
    add_block(blocks, church_x + church_w // 2, 2, church_z + church_d - 2, "minecraft:cauldron")

    # Bell tower (partially standing)
    tower_x, tower_z = church_x + church_w - 2, church_z + 2
    for y in range(1, 10):
        for dx in [0, 1]:
            for dz in [0, 1]:
                if rng.random() > 0.2 or y < 6:
                    add_block(blocks, tower_x + dx, y, tower_z + dz, wall_block)

    # === Town square ===
    square_x, square_z = 16, 8
    square_w, square_d = 12, 10

    fill(blocks, square_x, 1, square_z, square_x + square_w, 1, square_z + square_d, "minecraft:gravel")

    # Well in center
    well_x, well_z = square_x + square_w // 2, square_z + square_d // 2
    fill(blocks, well_x - 1, 1, well_z - 1, well_x + 1, 1, well_z + 1, "minecraft:cobblestone")
    fill(blocks, well_x, 0, well_z, well_x, -2, well_z, "minecraft:water")
    add_block(blocks, well_x, 1, well_z, "minecraft:oak_fence")

    # Market stalls
    stall_positions = [(square_x + 2, square_z + 2), (square_x + square_w - 3, square_z + 2)]
    for sx, sz in stall_positions:
        lean_to(blocks, sx, sz, 3, 2, wood_block)
        add_block(blocks, sx + 1, 2, sz + 1, "minecraft:barrel")

    # === Hidden bunker ===
    # Entrance disguised near church
    bunker_entrance_x, bunker_entrance_z = church_x + 2, church_z + church_d - 3
    add_block(blocks, bunker_entrance_x, 1, bunker_entrance_z, "minecraft:oak_trapdoor")

    # Underground lab
    vault_cx, vault_cz = underground_vault(blocks, rng, bunker_entrance_x, bunker_entrance_z, 8, 10, 4, wall_block, wood_block)

    # Secret storage in vault
    add_supply_cluster(blocks, rng, vault_cx - 2, vault_cz - 2, 3, 3, containers=3,
                       clutter_palette=["minecraft:chest", "minecraft:barrel"])
    add_block(blocks, vault_cx + 2, -3, vault_cz + 2, "echoashfallprotocol:map_table")

    # === Debris and atmosphere ===
    scatter(blocks, rng, 2, 2, 43, 38, 60, ["minecraft:gravel", "minecraft:cobblestone", "echoashfallprotocol:scattered_bones", "echoashfallprotocol:rubble"])

    # Overgrown vegetation
    for _ in range(30):
        vx = rng.randint(2, 43)
        vz = rng.randint(2, 38)
        add_block(blocks, vx, 1, vz, "minecraft:tall_grass")

    # Signal marker
    add_signal_marker(blocks, 40, 4, 35)

    # Additional anchor
    add_block(blocks, vault_cx, 1, vault_cz + 5, "minecraft:campfire")

    return enforce_guardrails("settlement_ruins", blocks, 40, 36, 10, min_story_nodes=10, min_reward_nodes=3)
