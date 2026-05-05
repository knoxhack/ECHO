"""
Themed block palettes for POI structure generation.
Palettes intentionally mix vanilla structure blocks with ECHO: Ashfall Protocol
signature blocks. Vanilla keeps silhouettes readable; custom blocks carry
the modpack identity and hazard/story cues.
"""

from typing import List

CRASH_ZONE_BLOCKS = [
    "echoashfallprotocol:debris_block",
    "echoashfallprotocol:drop_pod_hull",
    "echoashfallprotocol:rubble",
    "echoashfallprotocol:concrete_chunk",
    "echoashfallprotocol:rusted_metal_debris",
    "echoashfallprotocol:ash_layer",
    "minecraft:iron_block",
    "minecraft:coarse_dirt",
    "minecraft:gravel",
    "minecraft:cobblestone",
    "minecraft:anvil",
    "minecraft:iron_bars",
    "minecraft:magma_block",
    "minecraft:orange_terracotta",
    "minecraft:smooth_stone",
    "minecraft:chain",
]

CITYSCAPE_BLOCKS = [
    "echoashfallprotocol:oil_stained_concrete",
    "echoashfallprotocol:concrete_rubble",
    "echoashfallprotocol:rusted_metal_sheet",
    "echoashfallprotocol:rubble",
    "echoashfallprotocol:concrete_chunk",
    "minecraft:stone_bricks",
    "minecraft:cracked_stone_bricks",
    "minecraft:cobblestone",
    "minecraft:mossy_cobblestone",
    "minecraft:oak_planks",
    "minecraft:oak_log",
    "minecraft:glass_pane",
    "minecraft:iron_bars",
    "minecraft:bricks",
    "minecraft:smooth_stone",
]

RADIATION_BLOCKS = [
    "echoashfallprotocol:radiation_block",
    "echoashfallprotocol:fallout_dust",
    "echoashfallprotocol:contaminated_soil",
    "echoashfallprotocol:toxic_waste_barrel",
    "echoashfallprotocol:nuclear_grass",
    "minecraft:iron_block",
    "minecraft:green_concrete",
    "minecraft:lime_terracotta",
    "minecraft:glowstone",
    "minecraft:iron_bars",
    "minecraft:smooth_stone",
    "minecraft:cauldron",
    "minecraft:barrel",
    "minecraft:glass",
    "minecraft:magma_block",
]

TOXIC_BLOCKS = [
    "echoashfallprotocol:toxic_puddle",
    "echoashfallprotocol:acidic_sludge",
    "echoashfallprotocol:toxic_waste_barrel",
    "echoashfallprotocol:contaminated_soil",
    "echoashfallprotocol:toxic_moss",
    "echoashfallprotocol:mutated_bush",
    "minecraft:slime_block",
    "minecraft:green_stained_glass",
    "minecraft:green_terracotta",
    "minecraft:oak_planks",
    "minecraft:oak_fence",
    "minecraft:cauldron",
    "minecraft:barrel",
    "minecraft:mossy_cobblestone",
    "minecraft:water",
    "minecraft:coarse_dirt",
]

INDUSTRIAL_BLOCKS = [
    "echoashfallprotocol:oil_stained_concrete",
    "echoashfallprotocol:concrete_rubble",
    "echoashfallprotocol:rusted_metal_sheet",
    "echoashfallprotocol:item_pipe",
    "echoashfallprotocol:power_cable",
    "echoashfallprotocol:scrap_press",
    "echoashfallprotocol:battery_bank",
    "minecraft:iron_block",
    "minecraft:smooth_stone",
    "minecraft:stone_bricks",
    "minecraft:hopper",
    "minecraft:iron_bars",
    "minecraft:chain",
    "minecraft:barrel",
    "minecraft:cauldron",
    "minecraft:anvil",
    "minecraft:observer",
]

CRYOGENIC_BLOCKS = [
    "echoashfallprotocol:thermal_array",
    "echoashfallprotocol:research_lab",
    "echoashfallprotocol:contaminant_condenser",
    "echoashfallprotocol:deep_ash",
    "echoashfallprotocol:concrete_chunk",
    "minecraft:packed_ice",
    "minecraft:blue_ice",
    "minecraft:iron_block",
    "minecraft:glass",
    "minecraft:smooth_stone",
    "minecraft:stone_bricks",
    "minecraft:snow_block",
    "minecraft:white_wool",
    "minecraft:oak_planks",
    "minecraft:barrel",
]

PLAINS_BLOCKS = [
    "echoashfallprotocol:rain_collector",
    "minecraft:campfire",
    "echoashfallprotocol:wild_berry_bush",
    "echoashfallprotocol:mutated_bush",
    "echoashfallprotocol:scattered_bones",
    "echoashfallprotocol:dead_wood_log",
    "echoashfallprotocol:charred_wood_log",
    "minecraft:white_wool",
    "minecraft:hay_block",
    "minecraft:oak_planks",
    "minecraft:oak_log",
    "minecraft:oak_fence",
    "minecraft:campfire",
    "minecraft:cobblestone",
    "minecraft:stone_bricks",
    "minecraft:dirt",
    "minecraft:coarse_dirt",
]

GLOBAL_BLOCKS = [
    "echoashfallprotocol:debris_block",
    "echoashfallprotocol:rubble",
    "echoashfallprotocol:concrete_chunk",
    "echoashfallprotocol:rusted_metal_debris",
    "echoashfallprotocol:ash_layer",
    "echoashfallprotocol:rain_collector",
    "minecraft:oak_planks",
    "minecraft:cobblestone",
    "minecraft:gravel",
    "minecraft:coarse_dirt",
    "minecraft:stone_bricks",
    "minecraft:iron_bars",
    "minecraft:barrel",
    "minecraft:chest",
    "minecraft:white_wool",
    "minecraft:oak_fence",
]

RADWARDEN_BLOCKS = [
    "echoashfallprotocol:weapon_rack",
    "echoashfallprotocol:supply_crate",
    "echoashfallprotocol:rusted_metal_sheet",
    "echoashfallprotocol:concrete_rubble",
    "echoashfallprotocol:power_cable",
    "echoashfallprotocol:relay_station",
    "minecraft:gray_concrete",
    "minecraft:deepslate_bricks",
    "minecraft:iron_bars",
    "minecraft:smooth_stone",
]

CRASHBREAK_BLOCKS = [
    "echoashfallprotocol:trade_counter",
    "echoashfallprotocol:map_table",
    "echoashfallprotocol:supply_crate",
    "echoashfallprotocol:rain_collector",
    "minecraft:campfire",
    "echoashfallprotocol:rubble",
    "minecraft:barrel",
    "minecraft:oak_planks",
    "minecraft:spruce_planks",
    "minecraft:campfire",
]

SPOREBOUND_BLOCKS = [
    "echoashfallprotocol:bio_processing_station",
    "echoashfallprotocol:spore_garden",
    "echoashfallprotocol:toxic_puddle",
    "echoashfallprotocol:acidic_sludge",
    "echoashfallprotocol:mutated_bush",
    "echoashfallprotocol:toxic_moss",
    "minecraft:moss_block",
    "minecraft:mycelium",
    "minecraft:magma_block",
    "minecraft:shroomlight",
]


def pick_from(palette: List[str], seed: int, index: int) -> str:
    """Deterministically pick a block from a palette."""
    return palette[(seed + index * 31) % len(palette)]
