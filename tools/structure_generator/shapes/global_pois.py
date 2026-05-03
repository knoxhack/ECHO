"""
Global POI shape generators.
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import GLOBAL_BLOCKS, pick_from
from .poi_primitives import (
    add_block,
    add_signal_marker,
    add_supply_cluster,
    blob_patch,
    enforce_guardrails,
    fill,
    lean_to,
    line,
    ruined_wall,
    scatter,
    scrap_barricade,
    tent_frame,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_debris_field_small(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    blob_patch(blocks, 5, 5, 5, 4, "minecraft:coarse_dirt")
    line(blocks, (0, 0, 5), (10, 0, 5), "minecraft:gravel")
    line(blocks, (5, 0, 0), (5, 0, 10), "minecraft:gravel")

    # Buried cache with a low silhouette and one obvious salvage focus.
    fill(blocks, 4, 0, 4, 6, 0, 6, "minecraft:smooth_stone")
    add_block(blocks, 5, 1, 5, "minecraft:barrel")
    add_block(blocks, 6, 1, 5, "minecraft:chest")
    add_block(blocks, 4, 1, 5, "minecraft:iron_bars")
    add_block(blocks, 5, 1, 4, "minecraft:chain")
    add_signal_marker(blocks, 8, 3, 3)

    scatter(blocks, rng, 1, 1, 9, 9, 10, [
        "minecraft:gravel",
        "minecraft:cobblestone",
        "echoashfallprotocol:rubble",
        pick_from(GLOBAL_BLOCKS, seed, 11),
    ])
    return enforce_guardrails("debris_field_small", blocks, 10, 10, 4, min_story_nodes=2)


def generate_debris_field_large(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    blob_patch(blocks, 7, 6, 7, 5, "minecraft:coarse_dirt")
    for x in range(0, 15):
        for z in range(5, 8):
            add_block(blocks, x, 0, z, "minecraft:smooth_stone" if (x + z) % 4 else "minecraft:cracked_stone_bricks")

    # Broken convoy fragment: two wreck frames, a spilled cache, and a safer path edge.
    fill(blocks, 3, 1, 5, 6, 1, 7, "minecraft:iron_block")
    fill(blocks, 4, 2, 5, 5, 2, 6, "minecraft:light_gray_stained_glass")
    fill(blocks, 9, 1, 4, 12, 1, 6, "minecraft:iron_block")
    add_block(blocks, 10, 2, 4, "minecraft:iron_bars")
    add_block(blocks, 11, 2, 6, "minecraft:iron_bars")
    for px, pz in ((2, 4), (6, 8), (8, 3), (12, 7)):
        add_block(blocks, px, 1, pz, "minecraft:black_wool")

    add_supply_cluster(blocks, rng, 6, 9, 4, 2, containers=3, clutter_palette=[
        "minecraft:gravel",
        "minecraft:cobblestone",
        "echoashfallprotocol:rusted_metal_debris",
    ])
    add_block(blocks, 13, 1, 8, "minecraft:campfire")
    add_signal_marker(blocks, 1, 3, 4)
    scrap_barricade(blocks, rng, 2, 10, 13, 10, ["minecraft:iron_bars", "minecraft:cobblestone", "minecraft:chain"])
    scatter(blocks, rng, 1, 1, 13, 11, 14, [
        "minecraft:gravel",
        "minecraft:cobblestone",
        "echoashfallprotocol:rubble",
        "echoashfallprotocol:rusted_metal_debris",
    ])
    return enforce_guardrails("debris_field_large", blocks, 14, 11, 4, min_story_nodes=3)


def generate_survivor_cache(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    variant = seed % 3

    blob_patch(blocks, 6, 6, 6, 5, "minecraft:coarse_dirt")
    scatter(blocks, rng, 1, 1, 11, 11, 14, ["minecraft:gravel", "minecraft:oak_log", "minecraft:cobblestone"])
    add_signal_marker(blocks, 9, 3, 5)

    if variant == 0:
        lean_to(blocks, 2, 3, 5, 3, "minecraft:white_wool")
        add_supply_cluster(blocks, rng, 3, 4, 3, 2, containers=2, clutter_palette=["minecraft:oak_log", "minecraft:gravel"])
        ruined_wall(blocks, rng, 1, 2, 6, 7, "minecraft:cobblestone", min_height=1, max_height=2, gap_chance=0.6)
    elif variant == 1:
        for x in range(3, 9):
            for z in range(3, 9):
                add_block(blocks, x, 0, z, "minecraft:cobblestone")
                if x in (3, 8) or z in (3, 8):
                    if (x, z) != (5, 8):
                        add_block(blocks, x, 1, z, "minecraft:stone_bricks")
        for x in range(4, 8):
            for z in range(4, 8):
                if not (x == 5 and z == 5):
                    add_block(blocks, x, 2, z, "minecraft:oak_planks")
        add_block(blocks, 5, 1, 5, "minecraft:chest")
        add_block(blocks, 6, 1, 5, "minecraft:barrel")
        add_block(blocks, 4, 1, 6, "minecraft:campfire")
    else:
        tent_frame(blocks, 3, 3, 6, 4, 4, "minecraft:white_wool")
        add_supply_cluster(blocks, rng, 4, 4, 3, 2, containers=2, clutter_palette=["minecraft:oak_log", "minecraft:coarse_dirt"])
        scrap_barricade(blocks, rng, 1, 8, 9, 8, ["minecraft:oak_fence", "minecraft:oak_log", "minecraft:cobblestone"])
        add_block(blocks, 10, 6, 10, "minecraft:barrel")

    add_block(blocks, 2, 0, 8, "minecraft:gravel")
    add_block(blocks, 8, 0, 2, "minecraft:oak_log")
    add_block(blocks, 7, 1, 8, "minecraft:barrel")
    add_block(blocks, 3, 1, 8, "minecraft:campfire")
    add_block(blocks, 8, 1, 7, "minecraft:cauldron")

    return enforce_guardrails("survivor_cache", blocks, 10, 10, 4, min_story_nodes=3)


def generate_radio_relay_small(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    blob_patch(blocks, 6, 6, 5, 5, "minecraft:coarse_dirt")
    for x in range(4, 9):
        for z in range(4, 9):
            add_block(blocks, x, 0, z, "minecraft:smooth_stone")

    mast_height = rng.randint(6, 8)
    for y in range(1, mast_height):
        add_block(blocks, 6, y, 6, "minecraft:iron_bars")
    add_block(blocks, 6, mast_height, 6, "minecraft:lightning_rod")

    line(blocks, (6, mast_height - 1, 6), (9, 2, 8), "minecraft:chain")
    line(blocks, (6, mast_height - 2, 6), (3, 2, 9), "minecraft:chain")
    line(blocks, (6, mast_height - 1, 6), (2, 1, 4), "minecraft:chain")

    for px, pz in ((4, 4), (8, 4), (4, 8), (8, 8)):
        add_block(blocks, px, 1, pz, "minecraft:stone_bricks")
        add_block(blocks, px, 2, pz, "minecraft:oak_fence")

    ruined_wall(blocks, rng, 3, 3, 9, 9, "minecraft:stone_bricks", min_height=1, max_height=3, gap_chance=0.45)
    add_supply_cluster(blocks, rng, 4, 5, 2, 2, containers=1, clutter_palette=["minecraft:gravel", "minecraft:oak_log"])
    scrap_barricade(blocks, rng, 2, 10, 10, 10, ["minecraft:oak_fence", "minecraft:cobblestone", "minecraft:iron_bars"])
    scatter(blocks, rng, 1, 1, 11, 11, 10, ["minecraft:gravel", "minecraft:cobblestone"])

    return enforce_guardrails("radio_relay_small", blocks, 10, 10, 6, min_reward_nodes=1)


def generate_abandoned_camp(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    variant = seed % 3
    blob_patch(blocks, 7, 7, 7, 6, "minecraft:coarse_dirt")
    ruined_wall(blocks, rng, 2, 2, 12, 12, "minecraft:oak_fence", min_height=1, max_height=2, gap_chance=0.35)
    scrap_barricade(blocks, rng, 2, 11, 12, 9, ["minecraft:oak_fence", "minecraft:oak_log", "minecraft:cobblestone"])

    if variant == 0:
        tent_frame(blocks, 3, 3, 5, 4, 4, "minecraft:white_wool")
        lean_to(blocks, 8, 5, 4, 3, "minecraft:oak_planks")
    elif variant == 1:
        tent_frame(blocks, 3, 4, 4, 5, 4, "minecraft:white_wool")
        tent_frame(blocks, 8, 3, 4, 4, 3, "minecraft:white_wool")
        ruined_wall(blocks, rng, 6, 2, 10, 6, "minecraft:cobblestone", min_height=1, max_height=2, gap_chance=0.5)
    else:
        lean_to(blocks, 3, 3, 5, 4, "minecraft:oak_planks")
        lean_to(blocks, 9, 5, 3, 4, "minecraft:white_wool")
        add_signal_marker(blocks, 11, 3, 4)

    add_block(blocks, 7, 0, 7, "minecraft:campfire")
    add_block(blocks, 10, 1, 4, "minecraft:cauldron")
    add_signal_marker(blocks, 11, 10, 4)
    add_block(blocks, 3, 1, 11, "minecraft:iron_bars")
    add_block(blocks, 3, 2, 11, "minecraft:iron_bars")
    add_supply_cluster(blocks, rng, 4, 8, 3, 2, containers=2, clutter_palette=["minecraft:hay_block", "minecraft:oak_log", "minecraft:gravel"])
    add_block(blocks, 10, 1, 9, "minecraft:barrel")
    add_block(blocks, 5, 1, 5, "minecraft:chest")
    scatter(blocks, rng, 2, 2, 12, 12, 16, ["minecraft:gravel", "minecraft:oak_log", "minecraft:cobblestone", "minecraft:coarse_dirt"])

    return enforce_guardrails("abandoned_camp", blocks, 12, 12, 4, min_story_nodes=3)


def generate_road_wreck(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    length = 14
    for x in range(length):
        for z in range(3, 7):
            road = "minecraft:smooth_stone" if z not in (3, 6) else "minecraft:cracked_stone_bricks"
            add_block(blocks, x, 0, z, road)
        if x % 3 == 1:
            add_block(blocks, x, 0, 4, "minecraft:white_concrete")

    fill(blocks, 5, 1, 3, 8, 1, 5, "minecraft:iron_block")
    add_block(blocks, 6, 2, 3, "minecraft:light_gray_stained_glass")
    add_block(blocks, 7, 2, 4, "minecraft:iron_bars")
    for px, pz in ((4, 2), (8, 6), (5, 6), (9, 3)):
        add_block(blocks, px, 1, pz, "minecraft:black_wool")

    add_supply_cluster(blocks, rng, 9, 7, 3, 2, containers=2, clutter_palette=[
        "minecraft:gravel",
        "echoashfallprotocol:rubble",
        "minecraft:iron_bars",
    ])
    add_block(blocks, 2, 1, 7, "minecraft:campfire")
    add_signal_marker(blocks, 12, 8, 4)
    scatter(blocks, rng, 1, 1, 12, 9, 12, ["minecraft:gravel", "minecraft:cobblestone", "echoashfallprotocol:rubble"])
    return enforce_guardrails("road_wreck", blocks, 13, 9, 4, min_story_nodes=3)
