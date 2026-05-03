"""
Medium-scale POI generators for Ruined Plains (20-30 block footprint).
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import PLAINS_BLOCKS, pick_from
from .poi_primitives import (
    add_block,
    add_signal_marker,
    add_supply_cluster,
    blob_patch,
    courtyard_layout,
    doorway,
    enforce_guardrails,
    fill,
    hollow_rect,
    lean_to,
    line,
    multi_room_layout,
    ruined_wall,
    scatter,
    scrap_barricade,
    tent_frame,
    watchtower,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_walled_encampment(seed: int) -> BlockList:
    """Medium: 24x24 walled encampment with multiple tents and central fire."""
    rng = _r(seed)
    blocks: BlockList = []

    # Base clearing
    blob_patch(blocks, 12, 12, 11, 10, "minecraft:coarse_dirt")

    # Palisade walls with gaps
    wall_block = pick_from(PLAINS_BLOCKS, seed, 0)
    ruined_wall(blocks, rng, 2, 2, 22, 2, wall_block, min_height=2, max_height=4, gap_chance=0.15)
    ruined_wall(blocks, rng, 2, 2, 2, 22, wall_block, min_height=2, max_height=4, gap_chance=0.15)
    ruined_wall(blocks, rng, 22, 2, 22, 22, wall_block, min_height=2, max_height=4, gap_chance=0.15)
    ruined_wall(blocks, rng, 2, 22, 22, 22, wall_block, min_height=2, max_height=4, gap_chance=0.15)

    # Gate opening
    add_block(blocks, 12, 0, 2, "minecraft:air")
    add_block(blocks, 12, 1, 2, "minecraft:air")

    # Central fire pit
    fill(blocks, 11, 0, 11, 13, 0, 13, "minecraft:stone_bricks")
    add_block(blocks, 12, 1, 12, "minecraft:campfire")

    # Multiple tents around perimeter
    tent_positions = [(5, 5), (18, 5), (5, 18), (18, 18)]
    for tx, tz in tent_positions:
        if rng.random() > 0.2:
            tent_frame(blocks, tx, tz, 5, 4, 3, "minecraft:white_wool")
            add_block(blocks, tx + 2, 0, tz + 2, "minecraft:campfire")

    # Watchtower in corner
    watchtower(blocks, rng, 19, 19, 2, 6, wall_block, "minecraft:oak_planks")

    # Supply/storage areas
    add_supply_cluster(blocks, rng, 8, 15, 4, 3, containers=3,
                       clutter_palette=["minecraft:hay_block", "minecraft:oak_log"])
    add_supply_cluster(blocks, rng, 15, 8, 3, 3, containers=2,
                       clutter_palette=["minecraft:barrel", "minecraft:chest"])

    # Scattered details
    scatter(blocks, rng, 3, 3, 21, 21, 30, ["minecraft:gravel", "minecraft:cobblestone", "minecraft:oak_log"])
    add_signal_marker(blocks, 20, 4, 4)
    add_signal_marker(blocks, 4, 4, 20)

    return enforce_guardrails("walled_encampment", blocks, 20, 20, 8, min_story_nodes=4, min_reward_nodes=2)


def generate_abandoned_homestead(seed: int) -> BlockList:
    """Medium: 22x20 farm house ruins with well, fences, and cellar."""
    rng = _r(seed)
    blocks: BlockList = []

    # Ground preparation
    blob_patch(blocks, 11, 10, 9, 8, "minecraft:coarse_dirt")

    # Farm house (ruined)
    house_x, house_z = 8, 6
    house_w, house_d = 8, 7

    # House foundation
    fill(blocks, house_x, 0, house_z, house_x + house_w, 0, house_z + house_d, "minecraft:cobblestone")

    # Partial walls (ruined)
    for x in range(house_x, house_x + house_w + 1):
        for z in [house_z, house_z + house_d]:
            if rng.random() > 0.4:  # Some walls collapsed
                for y in range(1, 4):
                    add_block(blocks, x, y, z, "minecraft:cobblestone")
    for z in range(house_z, house_z + house_d + 1):
        for x in [house_x, house_x + house_w]:
            if rng.random() > 0.4:
                for y in range(1, 4):
                    add_block(blocks, x, y, z, "minecraft:cobblestone")

    # Doorway
    doorway(blocks, house_x + 4, 1, house_z, "north", width=2, height=2)

    # Interior (what's left)
    add_block(blocks, house_x + 2, 1, house_z + 2, "minecraft:chest")
    add_block(blocks, house_x + 6, 1, house_z + 5, "minecraft:barrel")
    add_block(blocks, house_x + 3, 1, house_z + 5, "minecraft:cauldron")

    # Well in yard
    well_x, well_z = 5, 15
    fill(blocks, well_x, 0, well_z, well_x + 2, 0, well_z + 2, "minecraft:stone_bricks")
    fill(blocks, well_x + 1, 0, well_z + 1, well_x + 1, -2, well_z + 1, "minecraft:water")
    ruined_wall(blocks, rng, well_x - 1, well_z - 1, well_x + 3, well_z - 1, "minecraft:cobblestone", min_height=1, max_height=2, gap_chance=0.3)

    # Broken fence sections
    for fx in range(2, 20, 3):
        if rng.random() > 0.3:
            add_block(blocks, fx, 1, 2, "minecraft:oak_fence")
    for fz in range(2, 18, 3):
        if rng.random() > 0.3:
            add_block(blocks, 2, 1, fz, "minecraft:oak_fence")

    # Cellar entrance (hatch with ladder down)
    cellar_x, cellar_z = house_x + 6, house_z + 2
    add_block(blocks, cellar_x, 1, cellar_z, "minecraft:oak_trapdoor")
    for y in range(1, 4):
        add_block(blocks, cellar_x, -y, cellar_z, "minecraft:ladder")
    # Small cellar room
    fill(blocks, cellar_x - 1, -3, cellar_z - 1, cellar_x + 1, -3, cellar_z + 1, "minecraft:stone_bricks")
    add_block(blocks, cellar_x, -3, cellar_z + 1, "minecraft:chest")

    # Debris and vegetation
    scatter(blocks, rng, 2, 2, 20, 18, 25, ["minecraft:gravel", "minecraft:oak_log", "minecraft:dead_bush"])
    scrap_barricade(blocks, rng, 2, 19, 8, 19, ["minecraft:oak_fence", "minecraft:oak_log"])

    # Additional anchor blocks
    add_block(blocks, house_x + 2, 1, house_z + 4, "minecraft:campfire")
    add_block(blocks, well_x, 2, well_z, "minecraft:cauldron")

    return enforce_guardrails("abandoned_homestead", blocks, 18, 16, 5, min_story_nodes=4, min_reward_nodes=2)


def generate_trader_post(seed: int) -> BlockList:
    """Medium: 20x18 wooden trading post with stalls and storage."""
    rng = _r(seed)
    blocks: BlockList = []

    # Wooden platform base
    fill(blocks, 2, 0, 2, 17, 0, 15, "minecraft:oak_planks")

    # Main trading pavilion (pillared roof)
    pavilion_x, pavilion_z = 4, 4
    pavilion_w, pavilion_d = 8, 6

    # Pillars
    for px in [pavilion_x, pavilion_x + pavilion_w]:
        for pz in [pavilion_z, pavilion_z + pavilion_d]:
            for y in range(1, 5):
                add_block(blocks, px, y, pz, "minecraft:oak_log")

    # Roof
    for x in range(pavilion_x, pavilion_x + pavilion_w + 1):
        for z in range(pavilion_z, pavilion_z + pavilion_d + 1):
            roof_y = 4 - abs(x - (pavilion_x + pavilion_w // 2)) // 2
            add_block(blocks, x, roof_y, z, "minecraft:oak_planks")

    # Trading counters
    for tx in range(pavilion_x + 1, pavilion_x + pavilion_w):
        add_block(blocks, tx, 1, pavilion_z + 1, "echoashfallprotocol:trade_counter")
        add_block(blocks, tx, 1, pavilion_z + pavilion_d - 1, "echoashfallprotocol:trade_counter")

    # Storage shed
    shed_x, shed_z = 14, 10
    fill(blocks, shed_x, 1, shed_z, shed_x + 3, 2, shed_z + 3, "minecraft:oak_planks")
    add_block(blocks, shed_x + 1, 1, shed_z, "minecraft:air")  # Door
    add_block(blocks, shed_x + 2, 1, shed_z + 2, "minecraft:barrel")
    add_block(blocks, shed_x + 1, 1, shed_z + 2, "minecraft:chest")

    # Hitching posts
    for hx in [6, 12]:
        add_block(blocks, hx, 1, 13, "minecraft:oak_fence")
        add_block(blocks, hx, 2, 13, "minecraft:oak_fence")

    # Water trough
    add_block(blocks, 9, 1, 13, "minecraft:cauldron")

    # Scattered wares/debris
    scatter(blocks, rng, 3, 3, 16, 14, 20, ["minecraft:oak_log", "minecraft:barrel", "minecraft:gravel"])
    add_signal_marker(blocks, 16, 4, 4)

    # Additional anchor
    add_block(blocks, 10, 1, 10, "minecraft:campfire")

    return enforce_guardrails("trader_post", blocks, 16, 14, 5, min_story_nodes=3, min_reward_nodes=2)
