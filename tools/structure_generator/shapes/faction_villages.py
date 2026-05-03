"""
Faction Village shape generators for wasteland-themed villages.
Generates Remnant, Salvager, and Mutant village structures.
"""

import random
from typing import List, Tuple, Optional, Dict

from .poi_primitives import (
    add_block,
    fill,
    line,
    blob_patch,
    scatter,
    ruined_wall,
    enforce_guardrails,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


# === REMNANT VILLAGE STRUCTURES ===

def generate_remnant_command_bunker(seed: int) -> BlockList:
    """Central command bunker for Remnant villages."""
    rng = _r(seed)
    blocks: BlockList = []

    # Concrete foundation
    fill(blocks, 0, 0, 0, 15, 1, 15, "minecraft:smooth_stone")

    # Bunker walls (reinforced concrete)
    fill(blocks, 1, 1, 1, 14, 5, 14, "minecraft:gray_concrete")
    fill(blocks, 2, 1, 2, 13, 5, 13, "minecraft:air")  # Interior

    # Reinforced corners
    for x, z in [(1, 1), (1, 14), (14, 1), (14, 14)]:
        fill(blocks, x, 1, z, x, 6, z, "minecraft:deepslate_bricks")

    # Command table (weapon rack profession block)
    add_block(blocks, 7, 1, 7, "echoashfallprotocol:weapon_rack")

    # Anchor blocks (campfires for visibility)
    add_block(blocks, 5, 0, 5, "minecraft:campfire")
    add_block(blocks, 9, 0, 9, "minecraft:campfire")
    add_block(blocks, 5, 0, 9, "minecraft:campfire")

    # Supply crates around the room (clutter)
    add_block(blocks, 3, 1, 3, "echoashfallprotocol:supply_crate")
    add_block(blocks, 12, 1, 3, "echoashfallprotocol:supply_crate")
    add_block(blocks, 3, 1, 12, "echoashfallprotocol:supply_crate")
    add_block(blocks, 12, 1, 12, "echoashfallprotocol:supply_crate")

    # Additional clutter barrels
    add_block(blocks, 5, 1, 2, "minecraft:barrel")
    add_block(blocks, 9, 1, 2, "minecraft:barrel")
    add_block(blocks, 5, 1, 13, "minecraft:barrel")
    add_block(blocks, 9, 1, 13, "minecraft:barrel")
    add_block(blocks, 2, 1, 7, "minecraft:chest")
    add_block(blocks, 13, 1, 7, "minecraft:chest")

    # Gravel clutter around exterior (20 blocks at y=0)
    scatter(blocks, rng, 0, 0, 15, 15, 20, ["minecraft:gravel", "minecraft:coarse_dirt"])

    # Entrance
    fill(blocks, 7, 1, 14, 8, 3, 14, "minecraft:air")
    fill(blocks, 7, 4, 14, 8, 4, 14, "minecraft:iron_bars")

    # Lighting
    add_block(blocks, 4, 4, 4, "minecraft:redstone_lamp")
    add_block(blocks, 11, 4, 4, "minecraft:redstone_lamp")
    add_block(blocks, 4, 4, 11, "minecraft:redstone_lamp")
    add_block(blocks, 11, 4, 11, "minecraft:redstone_lamp")

    return enforce_guardrails("remnant_command_bunker", blocks, 16, 16, 7)


def generate_remnant_barracks(seed: int) -> BlockList:
    """Soldier sleeping quarters."""
    rng = _r(seed)
    blocks: BlockList = []

    # Floor
    fill(blocks, 0, 0, 0, 11, 0, 11, "minecraft:smooth_stone")

    # Walls
    fill(blocks, 0, 1, 0, 11, 4, 0, "minecraft:gray_concrete")
    fill(blocks, 0, 1, 11, 11, 4, 11, "minecraft:gray_concrete")
    fill(blocks, 0, 1, 1, 0, 4, 10, "minecraft:gray_concrete")
    fill(blocks, 11, 1, 1, 11, 4, 10, "minecraft:gray_concrete")

    # Roof
    fill(blocks, 0, 5, 0, 11, 5, 11, "minecraft:smooth_stone_slab")

    # Beds (bunk style) - two rows
    for x in range(2, 10, 2):
        add_block(blocks, x, 1, 2, "minecraft:red_bed")
        add_block(blocks, x, 1, 9, "minecraft:blue_bed")

    # Weapon rack for soldier profession
    add_block(blocks, 5, 1, 5, "echoashfallprotocol:weapon_rack")

    # Anchor blocks
    add_block(blocks, 2, 0, 5, "minecraft:campfire")
    add_block(blocks, 9, 0, 5, "minecraft:campfire")
    add_block(blocks, 5, 0, 2, "minecraft:cauldron")

    # Storage (clutter)
    add_block(blocks, 1, 1, 1, "minecraft:chest")
    add_block(blocks, 10, 1, 1, "minecraft:chest")
    add_block(blocks, 1, 1, 10, "minecraft:chest")
    add_block(blocks, 10, 1, 10, "minecraft:chest")
    add_block(blocks, 3, 1, 3, "minecraft:barrel")
    add_block(blocks, 8, 1, 3, "minecraft:barrel")
    add_block(blocks, 3, 1, 8, "minecraft:barrel")
    add_block(blocks, 8, 1, 8, "minecraft:barrel")

    return enforce_guardrails("remnant_barracks", blocks, 12, 12, 6)


def generate_remnant_armory(seed: int) -> BlockList:
    """Weapon and equipment storage."""
    rng = _r(seed)
    blocks: BlockList = []

    # Foundation
    fill(blocks, 0, 0, 0, 9, 0, 9, "minecraft:smooth_stone")

    # Walls with reinforcement
    fill(blocks, 0, 1, 0, 9, 4, 9, "minecraft:gray_concrete")
    fill(blocks, 1, 1, 1, 8, 4, 8, "minecraft:air")

    # Iron bars for secure storage
    fill(blocks, 1, 3, 1, 8, 3, 8, "minecraft:iron_bars")

    # Weapon racks (profession blocks)
    add_block(blocks, 2, 1, 2, "echoashfallprotocol:weapon_rack")
    add_block(blocks, 7, 1, 2, "echoashfallprotocol:weapon_rack")
    add_block(blocks, 2, 1, 7, "echoashfallprotocol:weapon_rack")
    add_block(blocks, 7, 1, 7, "echoashfallprotocol:weapon_rack")

    # Anchor blocks
    add_block(blocks, 1, 0, 1, "minecraft:campfire")
    add_block(blocks, 8, 0, 1, "minecraft:campfire")
    add_block(blocks, 4, 0, 8, "minecraft:cauldron")

    # Supply crates and clutter
    add_block(blocks, 4, 1, 4, "echoashfallprotocol:supply_crate")
    add_block(blocks, 5, 1, 5, "echoashfallprotocol:supply_crate")
    add_block(blocks, 2, 1, 5, "minecraft:barrel")
    add_block(blocks, 7, 1, 5, "minecraft:barrel")
    add_block(blocks, 4, 1, 2, "minecraft:chest")
    add_block(blocks, 5, 1, 7, "minecraft:chest")
    add_block(blocks, 2, 1, 2, "minecraft:barrel")
    add_block(blocks, 7, 1, 7, "minecraft:barrel")
    add_block(blocks, 1, 1, 4, "minecraft:chest")
    add_block(blocks, 8, 1, 4, "minecraft:chest")

    return enforce_guardrails("remnant_armory", blocks, 10, 10, 5)


def generate_remnant_guard_post(seed: int) -> BlockList:
    """Compact sentry post used as a Remnant house-pool filler."""
    blocks: BlockList = []

    fill(blocks, 0, 0, 0, 7, 0, 7, "minecraft:smooth_stone")
    fill(blocks, 1, 1, 1, 6, 3, 6, "minecraft:gray_concrete")
    fill(blocks, 2, 1, 2, 5, 3, 5, "minecraft:air")

    # Watch platform and protected firing slits.
    fill(blocks, 1, 4, 1, 6, 4, 6, "minecraft:smooth_stone_slab")
    fill(blocks, 1, 5, 1, 6, 5, 1, "minecraft:iron_bars")
    fill(blocks, 1, 5, 6, 6, 5, 6, "minecraft:iron_bars")
    fill(blocks, 1, 5, 2, 1, 5, 5, "minecraft:iron_bars")
    fill(blocks, 6, 5, 2, 6, 5, 5, "minecraft:iron_bars")

    fill(blocks, 3, 1, 6, 4, 3, 6, "minecraft:air")
    add_block(blocks, 3, 1, 3, "echoashfallprotocol:weapon_rack")
    add_block(blocks, 4, 1, 3, "minecraft:barrel")
    add_block(blocks, 2, 1, 3, "minecraft:chest")
    add_block(blocks, 5, 1, 3, "minecraft:barrel")
    add_block(blocks, 2, 1, 4, "minecraft:barrel")
    add_block(blocks, 5, 1, 4, "minecraft:chest")
    add_block(blocks, 2, 2, 2, "minecraft:chain")
    add_block(blocks, 5, 2, 2, "minecraft:chain")
    add_block(blocks, 2, 2, 5, "minecraft:chain")
    add_block(blocks, 5, 2, 5, "minecraft:chain")
    add_block(blocks, 2, 1, 5, "minecraft:ladder", {"facing": "north"})

    add_block(blocks, 0, 0, 0, "minecraft:campfire")
    add_block(blocks, 7, 0, 7, "minecraft:campfire")
    add_block(blocks, 3, 5, 3, "minecraft:lantern")

    return enforce_guardrails("remnant_guard_post", blocks, 8, 8, 6)


def generate_remnant_supply_depot(seed: int) -> BlockList:
    """Fortified storage hut with supply-crate job-site coverage."""
    rng = _r(seed)
    blocks: BlockList = []

    fill(blocks, 0, 0, 0, 11, 0, 9, "minecraft:smooth_stone")
    fill(blocks, 0, 1, 0, 11, 4, 9, "minecraft:gray_concrete")
    fill(blocks, 1, 1, 1, 10, 4, 8, "minecraft:air")
    fill(blocks, 0, 5, 0, 11, 5, 9, "minecraft:smooth_stone_slab")

    fill(blocks, 5, 1, 9, 6, 3, 9, "minecraft:air")
    for x, z in [(2, 2), (5, 2), (8, 2), (2, 6), (5, 6), (8, 6)]:
        add_block(blocks, x, 1, z, "echoashfallprotocol:supply_crate")
    for x, z in [(3, 3), (7, 3), (3, 7), (7, 7), (9, 5)]:
        add_block(blocks, x, 1, z, "minecraft:barrel")

    add_block(blocks, 1, 1, 1, "minecraft:chest")
    add_block(blocks, 10, 1, 8, "minecraft:chest")
    scatter(blocks, rng, 0, 0, 11, 9, 10, ["minecraft:gravel", "minecraft:coarse_dirt"])

    add_block(blocks, 1, 0, 1, "minecraft:campfire")
    add_block(blocks, 10, 0, 1, "minecraft:campfire")
    add_block(blocks, 5, 0, 8, "minecraft:cauldron")

    return enforce_guardrails("remnant_supply_depot", blocks, 12, 10, 6)


def generate_remnant_street_straight(seed: int) -> BlockList:
    """Straight street segment."""
    blocks: BlockList = []

    # Full area base
    fill(blocks, 0, -1, 0, 15, -1, 15, "minecraft:coarse_dirt")

    # Gravel path
    fill(blocks, 0, 0, 4, 15, 0, 11, "minecraft:gravel")

    # Pavement edges
    line(blocks, (0, 0, 2), (15, 0, 2), "minecraft:cobblestone")
    line(blocks, (0, 0, 13), (15, 0, 13), "minecraft:cobblestone")

    # Street lamps
    for x in [4, 11]:
        add_block(blocks, x, 1, 2, "minecraft:cobblestone_wall")
        add_block(blocks, x, 2, 2, "minecraft:cobblestone_wall")
        add_block(blocks, x, 3, 2, "minecraft:lantern")
        add_block(blocks, x, 1, 13, "minecraft:cobblestone_wall")
        add_block(blocks, x, 2, 13, "minecraft:cobblestone_wall")
        add_block(blocks, x, 3, 13, "minecraft:lantern")

    # Anchor blocks (campfires for street junctions)
    add_block(blocks, 0, 0, 0, "minecraft:campfire")
    add_block(blocks, 15, 0, 15, "minecraft:campfire")
    add_block(blocks, 7, 0, 7, "minecraft:cauldron")
    add_block(blocks, 14, 0, 3, "minecraft:barrel")

    return enforce_guardrails("remnant_street_straight", blocks, 16, 16, 4)


def generate_remnant_street_corner(seed: int) -> BlockList:
    """Corner street segment."""
    blocks: BlockList = []

    # Full area base
    fill(blocks, 0, -1, 0, 15, -1, 15, "minecraft:coarse_dirt")

    # Gravel path (L-shaped)
    fill(blocks, 0, 0, 4, 15, 0, 11, "minecraft:gravel")  # Horizontal
    fill(blocks, 4, 0, 11, 11, 0, 15, "minecraft:gravel")  # Vertical extension

    # Pavement borders
    line(blocks, (0, 0, 2), (15, 0, 2), "minecraft:cobblestone")
    line(blocks, (0, 0, 13), (13, 0, 13), "minecraft:cobblestone")
    line(blocks, (13, 0, 13), (13, 0, 15), "minecraft:cobblestone")

    # Street lamps
    add_block(blocks, 4, 1, 2, "minecraft:cobblestone_wall")
    add_block(blocks, 4, 2, 2, "minecraft:cobblestone_wall")
    add_block(blocks, 4, 3, 2, "minecraft:lantern")
    add_block(blocks, 13, 1, 13, "minecraft:cobblestone_wall")
    add_block(blocks, 13, 2, 13, "minecraft:cobblestone_wall")
    add_block(blocks, 13, 3, 13, "minecraft:lantern")

    # Anchor blocks
    add_block(blocks, 0, 0, 0, "minecraft:campfire")
    add_block(blocks, 15, 0, 15, "minecraft:campfire")
    add_block(blocks, 7, 0, 7, "minecraft:cauldron")
    add_block(blocks, 2, 0, 14, "minecraft:chest")

    return enforce_guardrails("remnant_street_corner", blocks, 16, 16, 4)


def generate_remnant_street_cross(seed: int) -> BlockList:
    """Four-way Remnant street junction."""
    blocks: BlockList = []

    fill(blocks, 0, -1, 0, 15, -1, 15, "minecraft:coarse_dirt")
    fill(blocks, 0, 0, 4, 15, 0, 11, "minecraft:gravel")
    fill(blocks, 4, 0, 0, 11, 0, 15, "minecraft:gravel")

    line(blocks, (0, 0, 2), (15, 0, 2), "minecraft:cobblestone")
    line(blocks, (0, 0, 13), (15, 0, 13), "minecraft:cobblestone")
    line(blocks, (2, 0, 0), (2, 0, 15), "minecraft:cobblestone")
    line(blocks, (13, 0, 0), (13, 0, 15), "minecraft:cobblestone")

    for x, z in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        add_block(blocks, x, 1, z, "minecraft:cobblestone_wall")
        add_block(blocks, x, 2, z, "minecraft:cobblestone_wall")
        add_block(blocks, x, 3, z, "minecraft:lantern")

    add_block(blocks, 7, 0, 7, "minecraft:cauldron")
    add_block(blocks, 7, 0, 2, "minecraft:barrel")
    add_block(blocks, 0, 0, 0, "minecraft:campfire")
    add_block(blocks, 15, 0, 15, "minecraft:campfire")

    return enforce_guardrails("remnant_street_cross", blocks, 16, 16, 4)


def generate_remnant_wall_section(seed: int) -> BlockList:
    """Village perimeter wall section."""
    blocks: BlockList = []

    # Full area base
    fill(blocks, 0, -1, 0, 15, -1, 15, "minecraft:coarse_dirt")

    # Wall
    fill(blocks, 0, 0, 0, 0, 3, 15, "minecraft:gray_concrete")
    fill(blocks, 15, 0, 0, 15, 3, 15, "minecraft:gray_concrete")

    # Crenellations
    for z in range(0, 16, 2):
        add_block(blocks, 0, 4, z, "minecraft:gray_concrete")
        add_block(blocks, 15, 4, z, "minecraft:gray_concrete")

    # Anchor blocks at wall ends
    add_block(blocks, 0, 0, 0, "minecraft:campfire")
    add_block(blocks, 15, 0, 15, "minecraft:campfire")
    add_block(blocks, 0, 0, 15, "minecraft:campfire")
    add_block(blocks, 15, 0, 0, "minecraft:campfire")
    add_block(blocks, 7, 0, 7, "minecraft:barrel")

    return enforce_guardrails("remnant_wall_section", blocks, 16, 16, 5)


def generate_remnant_wall_corner(seed: int) -> BlockList:
    """L-shaped perimeter wall terminator/corner for Remnant villages."""
    blocks: BlockList = []

    fill(blocks, 0, -1, 0, 15, -1, 15, "minecraft:coarse_dirt")
    fill(blocks, 0, 0, 0, 15, 3, 0, "minecraft:gray_concrete")
    fill(blocks, 0, 0, 0, 0, 3, 15, "minecraft:gray_concrete")

    # Reinforced corner tower.
    fill(blocks, 0, 0, 0, 3, 5, 3, "minecraft:deepslate_bricks")
    fill(blocks, 1, 1, 1, 2, 4, 2, "minecraft:air")
    add_block(blocks, 1, 1, 2, "minecraft:ladder", {"facing": "north"})
    add_block(blocks, 1, 5, 1, "minecraft:lantern")

    for x in range(0, 16, 2):
        add_block(blocks, x, 4, 0, "minecraft:gray_concrete")
    for z in range(0, 16, 2):
        add_block(blocks, 0, 4, z, "minecraft:gray_concrete")

    add_block(blocks, 0, 0, 0, "minecraft:campfire")
    add_block(blocks, 15, 0, 0, "minecraft:campfire")
    add_block(blocks, 0, 0, 15, "minecraft:campfire")
    add_block(blocks, 4, 0, 4, "minecraft:chest")

    return enforce_guardrails("remnant_wall_corner", blocks, 16, 16, 6)


# === SALVAGER VILLAGE STRUCTURES ===

def generate_salvager_market_plaza(seed: int) -> BlockList:
    """Central marketplace for Salvager villages."""
    rng = _r(seed)
    blocks: BlockList = []

    # Plaza floor (mixed materials)
    blob_patch(blocks, 8, 0, 8, 8, "minecraft:oak_planks")
    scatter(blocks, rng, 2, 2, 14, 14, 20, ["minecraft:oak_planks", "minecraft:spruce_planks", "minecraft:stone_bricks"])

    # Market stalls
    for x, z in [(3, 3), (3, 12), (12, 3), (12, 12)]:
        add_block(blocks, x, 1, z, "minecraft:barrel")
        add_block(blocks, x, 2, z, "minecraft:oak_fence")
        fill(blocks, x - 1, 3, z - 1, x + 1, 3, z + 1, "minecraft:white_wool")

    # Central trade counter (profession block)
    add_block(blocks, 8, 1, 8, "echoashfallprotocol:trade_counter")

    # Map table for scout profession
    add_block(blocks, 8, 1, 10, "echoashfallprotocol:map_table")

    # Anchor blocks
    add_block(blocks, 4, 0, 4, "minecraft:campfire")
    add_block(blocks, 12, 0, 4, "minecraft:campfire")
    add_block(blocks, 8, 0, 12, "minecraft:campfire")

    # Decorative elements
    add_block(blocks, 5, 1, 8, "minecraft:flower_pot")
    add_block(blocks, 11, 1, 8, "minecraft:flower_pot")

    return enforce_guardrails("salvager_market_plaza", blocks, 16, 16, 4)


def generate_salvager_warehouse(seed: int) -> BlockList:
    """Storage building with trade counter."""
    rng = _r(seed)
    blocks: BlockList = []

    # Floor
    fill(blocks, 0, 0, 0, 13, 0, 13, "minecraft:oak_planks")

    # Walls
    fill(blocks, 0, 1, 0, 13, 4, 0, "minecraft:spruce_planks")
    fill(blocks, 0, 1, 13, 13, 4, 13, "minecraft:spruce_planks")
    fill(blocks, 0, 1, 1, 0, 4, 12, "minecraft:spruce_planks")
    fill(blocks, 13, 1, 1, 13, 4, 12, "minecraft:spruce_planks")

    # Roof
    fill(blocks, 0, 5, 0, 13, 5, 13, "minecraft:oak_stairs")

    # Interior - storage area
    for x in range(2, 12, 3):
        for z in range(2, 12, 3):
            add_block(blocks, x, 1, z, "minecraft:barrel")

    # Trade counter for merchant profession
    add_block(blocks, 6, 1, 4, "echoashfallprotocol:trade_counter")

    # Anchor blocks
    add_block(blocks, 2, 0, 2, "minecraft:campfire")
    add_block(blocks, 11, 0, 2, "minecraft:campfire")
    add_block(blocks, 6, 0, 11, "minecraft:cauldron")

    # Entrance
    fill(blocks, 6, 1, 13, 7, 3, 13, "minecraft:air")

    return enforce_guardrails("salvager_warehouse", blocks, 14, 14, 6)


# === MUTANT VILLAGE STRUCTURES ===

def generate_mutant_biodome_hub(seed: int) -> BlockList:
    """Central biodome for Mutant villages."""
    rng = _r(seed)
    blocks: BlockList = []

    # Organic floor (mycelium/moss)
    blob_patch(blocks, 8, 0, 8, 8, "minecraft:mycelium")
    scatter(blocks, rng, 1, 1, 14, 14, 15, ["minecraft:moss_block", "minecraft:vine", "minecraft:grass"])

    # Glowing mushroom pillars
    for x, z in [(4, 4), (4, 12), (12, 4), (12, 12)]:
        fill(blocks, x, 1, z, x, 4, z, "minecraft:mushroom_stem")
        add_block(blocks, x, 5, z, "minecraft:shroomlight")

    # Bio processing station (profession block)
    add_block(blocks, 8, 1, 8, "echoashfallprotocol:bio_processing_station")

    # Spore garden for elder profession
    add_block(blocks, 8, 1, 6, "echoashfallprotocol:spore_garden")

    # Anchor blocks (using magma blocks for mutant theme)
    add_block(blocks, 4, 0, 4, "minecraft:magma_block")
    add_block(blocks, 12, 0, 4, "minecraft:magma_block")
    add_block(blocks, 8, 0, 12, "minecraft:magma_block")

    # Water feature
    fill(blocks, 6, 0, 10, 10, 0, 14, "minecraft:water")

    return enforce_guardrails("mutant_biodome_hub", blocks, 16, 16, 6)


def generate_mutant_processing_hut(seed: int) -> BlockList:
    """Small bio-processing workshop."""
    rng = _r(seed)
    blocks: BlockList = []

    # Organic base
    fill(blocks, 0, 0, 0, 9, 0, 9, "minecraft:moss_block")

    # Living walls (mushroom blocks)
    fill(blocks, 0, 1, 0, 9, 3, 0, "minecraft:red_mushroom_block")
    fill(blocks, 0, 1, 9, 9, 3, 9, "minecraft:red_mushroom_block")
    fill(blocks, 0, 1, 1, 0, 3, 8, "minecraft:red_mushroom_block")
    fill(blocks, 9, 1, 1, 9, 3, 8, "minecraft:red_mushroom_block")

    # Clear interior
    fill(blocks, 1, 1, 1, 8, 3, 8, "minecraft:air")

    # Processing stations
    add_block(blocks, 3, 1, 3, "echoashfallprotocol:bio_processing_station")
    add_block(blocks, 6, 1, 6, "echoashfallprotocol:bio_processing_station")

    # Spore garden
    add_block(blocks, 4, 1, 7, "echoashfallprotocol:spore_garden")

    # Anchor blocks
    add_block(blocks, 1, 0, 1, "minecraft:magma_block")
    add_block(blocks, 8, 0, 1, "minecraft:magma_block")
    add_block(blocks, 4, 0, 8, "minecraft:cauldron")

    # Organic lighting
    add_block(blocks, 2, 3, 2, "minecraft:glow_lichen")
    add_block(blocks, 7, 3, 7, "minecraft:glow_lichen")

    return enforce_guardrails("mutant_processing_hut", blocks, 10, 10, 4)


# Export all generators
FACTION_GENERATORS = {
    # Remnant structures
    "remnant_outpost/command_bunker": generate_remnant_command_bunker,
    "remnant_outpost/barracks": generate_remnant_barracks,
    "remnant_outpost/armory": generate_remnant_armory,
    "remnant_outpost/guard_post": generate_remnant_guard_post,
    "remnant_outpost/supply_depot": generate_remnant_supply_depot,
    "remnant_outpost/street_straight": generate_remnant_street_straight,
    "remnant_outpost/street_corner": generate_remnant_street_corner,
    "remnant_outpost/street_cross": generate_remnant_street_cross,
    "remnant_outpost/wall_section": generate_remnant_wall_section,
    "remnant_outpost/wall_corner": generate_remnant_wall_corner,
    # Salvager structures
    "salvager_post/market_plaza": generate_salvager_market_plaza,
    "salvager_post/warehouse": generate_salvager_warehouse,
    # Mutant structures
    "mutant_sanctuary/biodome_hub": generate_mutant_biodome_hub,
    "mutant_sanctuary/processing_hut": generate_mutant_processing_hut,
}
