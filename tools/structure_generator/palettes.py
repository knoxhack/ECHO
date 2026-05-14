"""Themed Echo/Blockworks palettes for POI structure generation."""

from typing import List

CRASH_ZONE_BLOCKS = [
    "echoblockworks:orbital_hull_damaged_hull",
    "echoblockworks:charred_concrete_scorched",
    "echoblockworks:charred_concrete_rebar",
    "echoblockworks:rusted_metal_cracked",
    "echoblockworks:rusted_metal_grate",
    "echoblockworks:rubble_pile",
    "echoblockworks:scattered_debris",
    "echoblockworks:sparking_cable_panel",
    "echoashfallprotocol:drop_pod_hull",
    "echoashfallprotocol:drop_pod_glass",
    "echoashfallprotocol:crash_slag",
    "echoashfallprotocol:power_cable",
]

CITYSCAPE_BLOCKS = [
    "echoblockworks:ashstone_brick",
    "echoblockworks:ashstone_cracked_brick",
    "echoblockworks:ashstone_debris",
    "echoblockworks:charred_concrete_cracked",
    "echoblockworks:charred_concrete_broken",
    "echoblockworks:charred_concrete_rebar",
    "echoblockworks:rusted_metal_panel",
    "echoblockworks:rubble_pile",
    "echoblockworks:scattered_debris",
    "echoashfallprotocol:oil_stained_concrete",
    "echoashfallprotocol:concrete_rubble",
    "echoashfallprotocol:power_cable",
]

RADIATION_BLOCKS = [
    "echoashfallprotocol:radiation_block",
    "echoashfallprotocol:fallout_dust",
    "echoashfallprotocol:contaminated_soil",
    "echoashfallprotocol:toxic_waste_barrel",
    "echoashfallprotocol:nuclear_grass",
    "echoashfallprotocol:uranium_crystal",
    "echoblockworks:reinforced_metal_hazard_stripe",
    "echoblockworks:terminal_panel_warning_panel",
    "echoblockworks:flickering_warning_light",
    "echoblockworks:warning_beacon",
    "echoblockworks:reclamation_glass_green_glass",
    "echoblockworks:reinforced_metal_grate",
    "echoblockworks:rusted_metal_pipe_wall",
]

TOXIC_BLOCKS = [
    "echoashfallprotocol:toxic_puddle",
    "echoashfallprotocol:acidic_sludge",
    "echoashfallprotocol:toxic_waste_barrel",
    "echoashfallprotocol:contaminated_soil",
    "echoashfallprotocol:toxic_moss",
    "echoashfallprotocol:mutated_bush",
    "echoashfallprotocol:ooze_crystal",
    "echoblockworks:reclamation_glass_green_glass",
    "echoblockworks:reclamation_glass_overgrown_glass",
    "echoblockworks:reclamation_glass_irrigation_pipe",
    "echoblockworks:reclamation_glass_hydroponic_panel",
    "echoblockworks:wall_pipe",
    "echoblockworks:steam_vent",
]

INDUSTRIAL_BLOCKS = [
    "echoashfallprotocol:oil_stained_concrete",
    "echoashfallprotocol:concrete_rubble",
    "echoashfallprotocol:rusted_metal_sheet",
    "echoashfallprotocol:item_pipe",
    "echoashfallprotocol:power_cable",
    "echoashfallprotocol:scrap_press",
    "echoashfallprotocol:battery_bank",
    "echoblockworks:reinforced_metal_panel",
    "echoblockworks:reinforced_metal_grate",
    "echoblockworks:reinforced_metal_hazard_stripe",
    "echoblockworks:rusted_metal_pipe_wall",
    "echoblockworks:ceiling_pipe",
    "echoblockworks:steam_vent",
    "echoblockworks:warning_beacon",
    "echoblockworks:terminal_panel_screen",
]

CRYOGENIC_BLOCKS = [
    "echoashfallprotocol:thermal_array",
    "echoashfallprotocol:research_lab",
    "echoashfallprotocol:contaminant_condenser",
    "echoashfallprotocol:deep_ash",
    "echoashfallprotocol:concrete_chunk",
    "echoashfallprotocol:frozen_conduit",
    "echoashfallprotocol:blue_ice_crystal",
    "echoashfallprotocol:permafrost",
    "echoashfallprotocol:cryogenic_fractured_stone",
    "echoblockworks:orbital_hull_white_hull",
    "echoblockworks:orbital_hull_thermal_tile",
    "echoblockworks:orbital_hull_lit_strip",
    "echoblockworks:reclamation_glass_framed_glass",
]

PLAINS_BLOCKS = [
    "echoashfallprotocol:rain_collector",
    "echoblockworks:warning_beacon",
    "echoashfallprotocol:wild_berry_bush",
    "echoashfallprotocol:mutated_bush",
    "echoashfallprotocol:scattered_bones",
    "echoashfallprotocol:dead_wood_log",
    "echoashfallprotocol:charred_wood_log",
    "echoashfallprotocol:rusty_wheat",
    "echoashfallprotocol:wasteland_dirt",
    "echoblockworks:ashstone_raw",
    "echoblockworks:ashstone_smooth",
    "echoblockworks:rubble_pile",
    "echoblockworks:scattered_debris",
]

GLOBAL_BLOCKS = [
    "echoashfallprotocol:debris_block",
    "echoashfallprotocol:rubble",
    "echoashfallprotocol:concrete_chunk",
    "echoashfallprotocol:rusted_metal_debris",
    "echoashfallprotocol:ash_layer",
    "echoashfallprotocol:rain_collector",
    "echoashfallprotocol:echo_cache",
    "echoashfallprotocol:echo_crate",
    "echoashfallprotocol:structure_cache",
    "echoblockworks:ashstone_raw",
    "echoblockworks:charred_concrete_broken",
    "echoblockworks:rusted_metal_cracked",
    "echoblockworks:rubble_pile",
    "echoblockworks:scattered_debris",
    "echoblockworks:echo_strip_light",
]

RADWARDEN_BLOCKS = [
    "echoashfallprotocol:weapon_rack",
    "echoashfallprotocol:structure_cache",
    "echoashfallprotocol:rusted_metal_sheet",
    "echoashfallprotocol:concrete_rubble",
    "echoashfallprotocol:power_cable",
    "echoashfallprotocol:relay_station",
    "echoblockworks:blackbox_vault_vault_wall",
    "echoblockworks:reinforced_metal_panel",
    "echoblockworks:reinforced_metal_grate",
    "echoblockworks:terminal_panel_warning_panel",
]

CRASHBREAK_BLOCKS = [
    "echoashfallprotocol:trade_counter",
    "echoashfallprotocol:map_table",
    "echoashfallprotocol:structure_cache",
    "echoashfallprotocol:rain_collector",
    "echoashfallprotocol:rubble",
    "echoblockworks:warning_beacon",
    "echoblockworks:charred_concrete_road_plate",
    "echoblockworks:rusted_metal_panel",
    "echoblockworks:wall_pipe",
]

SPOREBOUND_BLOCKS = [
    "echoashfallprotocol:bio_processing_station",
    "echoashfallprotocol:spore_garden",
    "echoashfallprotocol:toxic_puddle",
    "echoashfallprotocol:acidic_sludge",
    "echoashfallprotocol:mutated_bush",
    "echoashfallprotocol:toxic_moss",
    "echoashfallprotocol:ooze_crystal",
    "echoashfallprotocol:nuclear_fungus",
    "echoblockworks:reclamation_glass_overgrown_glass",
    "echoblockworks:reclamation_glass_lit_grow_panel",
]


def pick_from(palette: List[str], seed: int, index: int) -> str:
    """Deterministically pick a block from a palette."""
    return palette[(seed + index * 31) % len(palette)]
