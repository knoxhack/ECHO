"""
Canonical Echo/Blockworks block replacements for structure templates.

The generator still has older shape code that describes silhouettes with
vanilla names. This module is the last-mile contract: every non-air vanilla
state is mapped to an Echo stack block before NBT is written or audited.
"""

from __future__ import annotations

from typing import Any, Dict, Iterable, Optional, Tuple, Union


BlockEntry = Union[
    Tuple[int, int, int, str, Optional[Dict[str, str]]],
    Tuple[int, int, int, str, Optional[Dict[str, str]], Optional[Dict[str, Any]]],
]

ECHO_STRUCTURE_CACHE = "echoashfallprotocol:structure_cache"
ECHO_CACHE = "echoashfallprotocol:echo_cache"
ECHO_CRATE = "echoashfallprotocol:echo_crate"
ECHO_CONTAINER_BLOCKS = {ECHO_CACHE, ECHO_CRATE}
ECHO_LOOT_CONTAINER_BLOCKS = {ECHO_STRUCTURE_CACHE, ECHO_CACHE, ECHO_CRATE}

VANILLA_REPLACEMENTS: dict[str, str] = {
    "minecraft:acacia_door": "echoblockworks:rusted_metal_panel",
    "minecraft:acacia_fence": "echoashfallprotocol:dead_wood_log",
    "minecraft:anvil": "echoblockworks:rusted_metal_dark_plate",
    "minecraft:amethyst_block": "echoblockworks:nexus_crystal_glowing_crystal",
    "minecraft:armor_stand": "echoashfallprotocol:weapon_rack",
    "minecraft:bamboo_door": "echoblockworks:rusted_metal_panel",
    "minecraft:barrel": ECHO_CRATE,
    "minecraft:beacon": "echoblockworks:hologram_floor_projector",
    "minecraft:birch_door": "echoblockworks:rusted_metal_panel",
    "minecraft:birch_planks": "echoblockworks:ashstone_smooth",
    "minecraft:black_concrete": "echoblockworks:orbital_hull_black_hull",
    "minecraft:black_wool": "echoblockworks:charred_concrete_scorched",
    "minecraft:blackstone": "echoblockworks:blackbox_vault_dark_alloy",
    "minecraft:blast_furnace": "echoashfallprotocol:scrap_press",
    "minecraft:blue_bed": "echoashfallprotocol:emergency_bunk",
    "minecraft:blue_ice": "echoashfallprotocol:blue_ice_crystal",
    "minecraft:bone_block": "echoashfallprotocol:scattered_bones",
    "minecraft:bookshelf": "echoblockworks:terminal_panel_server_rack",
    "minecraft:brewing_stand": "echoashfallprotocol:contaminant_condenser",
    "minecraft:bricks": "echoblockworks:ashstone_brick",
    "minecraft:brown_wool": "echoblockworks:rusted_metal_dark_plate",
    "minecraft:campfire": "echoblockworks:warning_beacon",
    "minecraft:cartography_table": "echoashfallprotocol:map_table",
    "minecraft:cauldron": "echoashfallprotocol:contaminant_condenser",
    "minecraft:chain": "echoblockworks:hanging_wire",
    "minecraft:cherry_door": "echoblockworks:rusted_metal_panel",
    "minecraft:chest": ECHO_CACHE,
    "minecraft:coal_ore": "echoashfallprotocol:scrap_ore",
    "minecraft:cobble_wall": "echoblockworks:ashstone_raw",
    "minecraft:cobblestone_wall": "echoblockworks:ashstone_raw_wall",
    "minecraft:cobbled_deepslate": "echoblockworks:blackbox_vault_cracked_vault",
    "minecraft:cobblestone": "echoblockworks:ashstone_raw",
    "minecraft:cobblestone_slab": "echoblockworks:ashstone_raw_slab",
    "minecraft:cobblestone_stairs": "echoblockworks:ashstone_raw_stairs",
    "minecraft:cobweb": "echoblockworks:hanging_wire",
    "minecraft:comparator": "echoblockworks:echo_circuit_service_node",
    "minecraft:composter": "echoashfallprotocol:bio_processing_station",
    "minecraft:copper_block": "echoblockworks:rusted_metal_panel",
    "minecraft:coarse_dirt": "echoashfallprotocol:wasteland_dirt",
    "minecraft:crafting_table": "echoashfallprotocol:workshop_block",
    "minecraft:cracked_deepslate_bricks": "echoblockworks:blackbox_vault_cracked_vault",
    "minecraft:cracked_nether_bricks": "echoblockworks:blackbox_vault_cracked_vault",
    "minecraft:cracked_polished_blackstone_bricks": "echoblockworks:blackbox_vault_cracked_vault",
    "minecraft:cracked_stone_bricks": "echoblockworks:ashstone_cracked_brick",
    "minecraft:crying_obsidian": "echoblockworks:nexus_crystal_rift_panel",
    "minecraft:cut_copper": "echoblockworks:rusted_metal_pipe_wall",
    "minecraft:dark_oak_door": "echoblockworks:rusted_metal_panel",
    "minecraft:dark_oak_fence": "echoashfallprotocol:charred_wood_log",
    "minecraft:dark_oak_planks": "echoblockworks:rusted_metal_dark_plate",
    "minecraft:daylight_detector": "echoblockworks:signal_dish_decorative",
    "minecraft:dead_bush": "echoashfallprotocol:ash_bush",
    "minecraft:deepslate": "echoblockworks:blackbox_vault_dark_alloy",
    "minecraft:deepslate_bricks": "echoblockworks:blackbox_vault_vault_wall",
    "minecraft:deepslate_tiles": "echoblockworks:blackbox_vault_archive_panel",
    "minecraft:diorite": "echoblockworks:ashstone_smooth",
    "minecraft:dirt": "echoashfallprotocol:wasteland_dirt",
    "minecraft:dirt_path": "echoashfallprotocol:cracked_asphalt",
    "minecraft:dispenser": "echoblockworks:terminal_panel_warning_panel",
    "minecraft:emerald_block": "echoashfallprotocol:trade_counter",
    "minecraft:end_rod": "echoblockworks:echo_strip_light",
    "minecraft:exposed_copper": "echoblockworks:rusted_metal_riveted",
    "minecraft:fern": "echoashfallprotocol:burnt_fern",
    "minecraft:fire": "echoashfallprotocol:crash_slag",
    "minecraft:flower_pot": "echoblockworks:scattered_debris",
    "minecraft:furnace": "echoashfallprotocol:thermal_burner",
    "minecraft:glass": "echoblockworks:reclamation_glass_framed_glass",
    "minecraft:glass_pane": "echoblockworks:reclamation_glass_framed_glass",
    "minecraft:glow_lichen": "echoblockworks:echo_strip_light",
    "minecraft:glowstone": "echoblockworks:echo_circuit_glowing_circuit",
    "minecraft:grass": "echoashfallprotocol:wasteland_grass",
    "minecraft:grass_block": "echoashfallprotocol:wasteland_grass_block",
    "minecraft:gravel": "echoblockworks:rubble_pile",
    "minecraft:gray_concrete": "echoblockworks:charred_concrete_smooth",
    "minecraft:gray_wool": "echoblockworks:charred_concrete_scorched",
    "minecraft:green_concrete": "echoblockworks:reclamation_glass_hydroponic_panel",
    "minecraft:green_stained_glass": "echoblockworks:reclamation_glass_green_glass",
    "minecraft:green_terracotta": "echoashfallprotocol:toxic_moss",
    "minecraft:green_wool": "echoblockworks:reclamation_glass_hydroponic_panel",
    "minecraft:grindstone": "echoashfallprotocol:hand_recycler",
    "minecraft:hay_block": "echoashfallprotocol:rusty_wheat",
    "minecraft:heavy_weighted_pressure_plate": "echoblockworks:reinforced_metal_grate",
    "minecraft:hopper": "echoashfallprotocol:autofeed_hopper",
    "minecraft:ice": "echoashfallprotocol:cryogenic_fractured_stone",
    "minecraft:iron_bars": "echoblockworks:reinforced_metal_grate",
    "minecraft:iron_block": "echoblockworks:reinforced_metal_panel",
    "minecraft:iron_door": "echoblockworks:reinforced_metal_frame",
    "minecraft:iron_ore": "echoashfallprotocol:scrap_ore",
    "minecraft:iron_trapdoor": "echoblockworks:reinforced_metal_grate",
    "minecraft:jungle_door": "echoblockworks:rusted_metal_panel",
    "minecraft:ladder": "echoblockworks:wall_pipe",
    "minecraft:lantern": "echoblockworks:echo_strip_light",
    "minecraft:lava": "echoashfallprotocol:energized_fissure",
    "minecraft:lectern": "echoblockworks:data_wall",
    "minecraft:light_blue_concrete": "echoblockworks:echo_circuit_circuit_panel",
    "minecraft:light_gray_concrete": "echoblockworks:orbital_hull_white_hull",
    "minecraft:light_gray_stained_glass": "echoblockworks:reclamation_glass_framed_glass",
    "minecraft:light_gray_wool": "echoblockworks:charred_concrete_broken",
    "minecraft:lightning_rod": "echoblockworks:signal_dish_decorative",
    "minecraft:lily_pad": "echoashfallprotocol:toxic_puddle",
    "minecraft:lime_terracotta": "echoashfallprotocol:nuclear_grass",
    "minecraft:lime_wool": "echoblockworks:reclamation_glass_hydroponic_panel",
    "minecraft:magma_block": "echoashfallprotocol:crash_slag",
    "minecraft:mangrove_door": "echoblockworks:rusted_metal_panel",
    "minecraft:mangrove_planks": "echoblockworks:rusted_metal_panel",
    "minecraft:moss_block": "echoashfallprotocol:toxic_moss",
    "minecraft:mossy_cobblestone": "echoblockworks:ashstone_debris",
    "minecraft:mossy_cobblestone_slab": "echoblockworks:ashstone_debris_slab",
    "minecraft:mossy_cobblestone_stairs": "echoblockworks:ashstone_debris_stairs",
    "minecraft:mossy_stone_bricks": "echoblockworks:ashstone_debris",
    "minecraft:mud": "echoashfallprotocol:acid_mud",
    "minecraft:mud_bricks": "echoashfallprotocol:acid_mud",
    "minecraft:muddy_mangrove_roots": "echoashfallprotocol:acid_mud",
    "minecraft:mushroom_stem": "echoashfallprotocol:bio_processing_station",
    "minecraft:mycelium": "echoashfallprotocol:toxic_moss",
    "minecraft:netherrack": "echoashfallprotocol:scorched_ash",
    "minecraft:nether_bricks": "echoblockworks:blackbox_vault_vault_wall",
    "minecraft:oak_door": "echoblockworks:rusted_metal_panel",
    "minecraft:oak_fence": "echoashfallprotocol:dead_wood_log",
    "minecraft:oak_log": "echoashfallprotocol:dead_wood_log",
    "minecraft:oak_planks": "echoblockworks:ashstone_smooth",
    "minecraft:oak_stairs": "echoblockworks:ashstone_smooth_stairs",
    "minecraft:oak_trapdoor": "echoblockworks:rusted_metal_grate",
    "minecraft:oak_wall_sign": "echoblockworks:data_wall",
    "minecraft:observer": "echoblockworks:terminal_panel_screen",
    "minecraft:obsidian": "echoblockworks:blackbox_vault_secure_frame",
    "minecraft:orange_terracotta": "echoblockworks:charred_concrete_scorched",
    "minecraft:orange_wool": "echoblockworks:rusted_metal_hazard_stripe",
    "minecraft:oxidized_copper": "echoblockworks:rusted_metal_pipe_wall",
    "minecraft:packed_ice": "echoashfallprotocol:frozen_conduit",
    "minecraft:polished_andesite": "echoblockworks:charred_concrete_tile",
    "minecraft:polished_andesite_slab": "echoblockworks:charred_concrete_tile_slab",
    "minecraft:polished_andesite_stairs": "echoblockworks:charred_concrete_tile_stairs",
    "minecraft:polished_blackstone": "echoblockworks:blackbox_vault_dark_alloy",
    "minecraft:polished_deepslate": "echoblockworks:blackbox_vault_archive_panel",
    "minecraft:polished_diorite": "echoblockworks:ashstone_smooth",
    "minecraft:podzol": "echoashfallprotocol:contaminated_soil",
    "minecraft:powder_snow": "echoashfallprotocol:permafrost",
    "minecraft:quartz_block": "echoblockworks:orbital_hull_white_hull",
    "minecraft:quartz_pillar": "echoblockworks:orbital_hull_airlock_frame",
    "minecraft:rail": "echoblockworks:reinforced_metal_grate",
    "minecraft:red_banner": "echoblockworks:terminal_panel_warning_panel",
    "minecraft:red_bed": "echoashfallprotocol:emergency_bunk",
    "minecraft:red_mushroom_block": "echoashfallprotocol:spore_garden",
    "minecraft:redstone_block": "echoblockworks:echo_circuit_glowing_circuit",
    "minecraft:redstone_lamp": "echoblockworks:warning_beacon",
    "minecraft:redstone_torch": "echoblockworks:flickering_warning_light",
    "minecraft:sculk": "echoblockworks:echo_circuit_encrypted_circuit",
    "minecraft:sculk_shrieker": "echoblockworks:echo_circuit_warning_circuit",
    "minecraft:sea_lantern": "echoblockworks:echo_strip_light",
    "minecraft:shroomlight": "echoashfallprotocol:ooze_crystal",
    "minecraft:short_grass": "echoashfallprotocol:dry_grass",
    "minecraft:slime_block": "echoashfallprotocol:acidic_sludge",
    "minecraft:smooth_stone": "echoblockworks:charred_concrete_smooth",
    "minecraft:smooth_stone_slab": "echoblockworks:charred_concrete_smooth_slab",
    "minecraft:snow": "echoashfallprotocol:permafrost",
    "minecraft:snow_block": "echoashfallprotocol:permafrost",
    "minecraft:soul_lantern": "echoblockworks:echo_strip_light",
    "minecraft:soul_torch": "echoblockworks:flickering_warning_light",
    "minecraft:soul_wall_torch": "echoblockworks:flickering_warning_light",
    "minecraft:spawner": "echoblockworks:warning_beacon",
    "minecraft:spruce_door": "echoblockworks:rusted_metal_panel",
    "minecraft:spruce_fence": "echoashfallprotocol:dead_wood_log",
    "minecraft:spruce_log": "echoashfallprotocol:charred_wood_log",
    "minecraft:spruce_planks": "echoblockworks:rusted_metal_panel",
    "minecraft:spruce_stairs": "echoblockworks:rusted_metal_panel_stairs",
    "minecraft:stick": "echoashfallprotocol:dead_wood_log",
    "minecraft:stone": "echoashfallprotocol:wasteland_stone",
    "minecraft:stone_brick_stairs": "echoblockworks:ashstone_brick_stairs",
    "minecraft:stone_bricks": "echoblockworks:ashstone_brick",
    "minecraft:stone_button": "echoblockworks:sparking_cable_panel",
    "minecraft:stone_slab": "echoblockworks:ashstone_brick_slab",
    "minecraft:stripped_oak_log": "echoashfallprotocol:dead_wood_log",
    "minecraft:stripped_spruce_log": "echoashfallprotocol:charred_wood_log",
    "minecraft:tall_grass": "echoashfallprotocol:wasteland_tall_grass",
    "minecraft:tinted_glass": "echoblockworks:nexus_crystal_cracked_crystal",
    "minecraft:torch": "echoblockworks:flickering_warning_light",
    "minecraft:trapped_chest": ECHO_CACHE,
    "minecraft:vine": "echoblockworks:hanging_wire",
    "minecraft:water": "echoashfallprotocol:acidic_sludge",
    "minecraft:weathered_oak_planks": "echoashfallprotocol:dead_wood_log",
    "minecraft:white_bed": "echoashfallprotocol:emergency_bunk",
    "minecraft:white_concrete": "echoblockworks:orbital_hull_white_hull",
    "minecraft:white_stained_glass_pane": "echoblockworks:reclamation_glass_framed_glass",
    "minecraft:white_wool": "echoblockworks:orbital_hull_white_hull",
    "minecraft:yellow_concrete": "echoblockworks:charred_concrete_warning_stripe",
    "minecraft:yellow_wool": "echoblockworks:charred_concrete_warning_stripe",
}


def _unpack(entry: BlockEntry):
    if len(entry) == 6:
        return entry  # type: ignore[return-value]
    x, y, z, block_id, props = entry  # type: ignore[misc]
    return x, y, z, block_id, props, None


def _pack(x: int, y: int, z: int, block_id: str, props, be_nbt):
    if be_nbt:
        return (x, y, z, block_id, props, be_nbt)
    return (x, y, z, block_id, props)


def echo_replacement(block_id: str) -> str:
    if block_id == "minecraft:air":
        return block_id
    if block_id.startswith("minecraft:"):
        return VANILLA_REPLACEMENTS.get(block_id, "echoblockworks:ashstone_raw")
    return block_id


def loot_container_block_entity_id(block_id: str) -> str | None:
    if block_id == ECHO_STRUCTURE_CACHE:
        return "echoashfallprotocol:structure_cache"
    if block_id in ECHO_CONTAINER_BLOCKS:
        return "echoashfallprotocol:echo_container"
    return None


def sanitize_block_state(
    block_id: str,
    props: Optional[Dict[str, str]],
    be_nbt: Optional[Dict[str, Any]],
) -> tuple[str, Optional[Dict[str, str]], Optional[Dict[str, Any]]]:
    replacement = echo_replacement(block_id)
    next_props = props if replacement == block_id else None
    next_nbt = be_nbt
    block_entity_id = loot_container_block_entity_id(replacement)
    if block_entity_id and next_nbt:
        next_nbt = dict(next_nbt)
        next_nbt["id"] = block_entity_id
    return replacement, next_props, next_nbt


def sanitize_blocks(blocks: Iterable[BlockEntry]) -> list[BlockEntry]:
    sanitized: list[BlockEntry] = []
    for entry in blocks:
        x, y, z, block_id, props, be_nbt = _unpack(entry)
        replacement, next_props, next_nbt = sanitize_block_state(block_id, props, be_nbt)
        sanitized.append(_pack(x, y, z, replacement, next_props, next_nbt))
    return sanitized


def vanilla_structure_blocks(block_ids: Iterable[str]) -> list[str]:
    return sorted({block_id for block_id in block_ids if block_id.startswith("minecraft:") and block_id != "minecraft:air"})
