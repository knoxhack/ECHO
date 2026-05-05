#!/usr/bin/env python3
"""
ECHO: Ashfall Protocol - Advanced POI Structure NBT Generator

Generates the canonical POI structure templates as NBT files for the
target structure-template format using nbtlib.

Usage:
    python tools/structure_generator/generator.py
"""

import sys
import argparse
import hashlib
import json
import shutil
from collections import Counter
from pathlib import Path
from typing import Iterable

sys.path.insert(0, str(Path(__file__).parent))

from nbt_writer import write_structure_nbt
from shapes import SHAPE_REGISTRY
from theme_polish import apply_theme_polish
from detail_pass import (
    INTENTIONAL_FLOATING_TEMPLATE_NAMES,
    apply_grounding_pass,
    apply_detail_pass,
    is_support_sensitive_block,
    is_terrain_blend_block,
)


# Category mapping for directory layout
CATEGORY_MAP = {
    "scrap_pile_small": "crash_zone_wasteland",
    "scrap_pile_medium": "crash_zone_wasteland",
    "wreckage_cluster": "crash_zone_wasteland",
    "ash_covered_ruin": "crash_zone_wasteland",
    "collapsed_building_small": "ruined_cityscape",
    "collapsed_building_tall": "ruined_cityscape",
    "street_barricade": "ruined_cityscape",
    "parking_ruin": "ruined_cityscape",
    "containment_breach": "radiation_zone",
    "waste_barrel_cluster": "radiation_zone",
    "irradiated_vehicle": "radiation_zone",
    "radiation_crater": "radiation_zone",
    "chemical_spill": "toxic_swamp",
    "broken_pipeline": "toxic_swamp",
    "abandoned_shed": "toxic_swamp",
    "toxic_pool_small": "toxic_swamp",
    "conveyor_ruin": "industrial_ruins",
    "storage_yard": "industrial_ruins",
    "crane_wreck": "industrial_ruins",
    "pipe_cluster": "industrial_ruins",
    "frozen_vehicle": "cryogenic_ruins",
    "ice_covered_ruin": "cryogenic_ruins",
    "broken_tank": "cryogenic_ruins",
    "frozen_cache": "cryogenic_ruins",
    "nomad_camp": "ruined_plains",
    "windmill_ruin": "ruined_plains",
    "impact_crater": "ruined_plains",
    "supply_drop": "ruined_plains",
    "debris_field_small": "global",
    "debris_field_large": "global",
    "survivor_cache": "global",
    "radio_relay_small": "global",
    "abandoned_camp": "global",
    "road_wreck": "global",
    # additional structures from template pool audit
    "scavenger_camp": "ruined_plains",
    "bio_lab": "toxic_swamp",
    "military_vault": "ruined_cityscape",
    "data_center_ruin": "ruined_cityscape",
    "reactor_ruin": "radiation_zone",
    "drop_pod": "global",
    "industrial_factory": "industrial_ruins",
    "subway_station": "ruined_cityscape",
    # faction village structures
    "radwarden_outpost/command_bunker": "faction",
    "radwarden_outpost/barracks": "faction",
    "radwarden_outpost/armory": "faction",
    "radwarden_outpost/guard_post": "faction",
    "radwarden_outpost/supply_depot": "faction",
    "radwarden_outpost/street_straight": "faction",
    "radwarden_outpost/street_corner": "faction",
    "radwarden_outpost/street_cross": "faction",
    "radwarden_outpost/wall_section": "faction",
    "radwarden_outpost/wall_corner": "faction",
    "crashbreak_salvage/market_plaza": "faction",
    "crashbreak_salvage/warehouse": "faction",
    "sporebound_sanctum/biodome_hub": "faction",
    "sporebound_sanctum/processing_hut": "faction",
    # Enhanced special structures v2 (big landmarks)
    "bio_facility": "toxic_swamp",
    "bunker_complex": "ruined_cityscape",
    "server_farm": "ruined_cityscape",
    "power_plant_ruin": "radiation_zone",
    # Ruined Plains - Medium structures
    "walled_encampment": "ruined_plains",
    "abandoned_homestead": "ruined_plains",
    "trader_post": "ruined_plains",
    # Ruined Plains - Big structures
    "ruined_outpost": "ruined_plains",
    "settlement_ruins": "ruined_plains",
    # Crash Zone - Medium structures
    "crashbreak_worksite": "crash_zone_wasteland",
    "crash_site_large": "crash_zone_wasteland",
    "radiation_field": "crash_zone_wasteland",
    # Crash Zone - Big structures
    "ship_breaking_yard": "crash_zone_wasteland",
    "containment_facility_ruin": "crash_zone_wasteland",
    # Biome overhaul rare landmarks
    "nexus_pylon": "nexus_scar",
    "floating_obelisk_cluster": "nexus_scar",
    "drop_pod_wreck_large": "crash_zone_wasteland",
    "cargo_module_field": "crash_zone_wasteland",
    "wasteland_bunker_ruin": "ruined_plains",
    "collapsed_tower_large": "ruined_cityscape",
    "corroded_pipe_network": "toxic_swamp",
    "reactor_containment_ruin": "radiation_zone",
    "frozen_lab_large": "cryogenic_ruins",
    "industrial_factory_shell": "industrial_ruins",
    # Full POI polish expansion
    "burned_convoy": "crash_zone_wasteland",
    "cargo_lift_wreck": "crash_zone_wasteland",
    "road_checkpoint": "global",
    "subway_stairwell": "ruined_cityscape",
    "sludge_drain": "toxic_swamp",
    "pipe_pump_house": "toxic_swamp",
    "radiation_beacon_line": "radiation_zone",
    "reactor_gatehouse": "radiation_zone",
    "frozen_comms_tower": "cryogenic_ruins",
    "cryo_tank_field": "cryogenic_ruins",
    "rail_signal_yard": "industrial_ruins",
    "factory_pipe_gate": "industrial_ruins",
}

ALIAS_PATHS = {
    "bio_lab": ["bio_lab"],
    "data_center_ruin": ["data_center_ruin"],
    "drop_pod": ["drop_pod"],
    "military_vault": ["military_vault"],
    "reactor_ruin": ["reactor_ruin"],
}

LANDMARK_NAMES = {
    "nexus_pylon",
    "floating_obelisk_cluster",
    "drop_pod_wreck_large",
    "cargo_module_field",
    "wasteland_bunker_ruin",
    "collapsed_tower_large",
    "corroded_pipe_network",
    "reactor_containment_ruin",
    "frozen_lab_large",
    "industrial_factory_shell",
}

CURATED_TEMPLATE_NAMES = {
    # The starting pod is guarded by gameplay validation because first-login
    # missions depend on its exact footprint and bed placement.
    "drop_pod",
}

VISUAL_PLACEHOLDER_BLOCKS = {
    "minecraft:bone_block",
    "minecraft:quartz_block",
    "minecraft:quartz_pillar",
    "minecraft:white_wool",
}

FORBIDDEN_GENERATED_BLOCKS = {
    # Namespaced typo that renders as a missing block when it slips into NBT.
    "minecraft:scattered_bones",
    # Generated ash campfires have produced block-entity load warnings in
    # runtime logs; use vanilla campfire or static Ashfall lighting instead.
    "echoashfallprotocol:ash_campfire",
}

CATEGORY_SIGNATURE_BLOCKS = {
    "crash_zone_wasteland": {
        "echoashfallprotocol:cable_bundle",
        "echoashfallprotocol:drop_pod_hull",
        "echoashfallprotocol:power_cable",
        "echoashfallprotocol:rusted_metal_debris",
    },
    "cryogenic_ruins": {
        "echoashfallprotocol:blue_ice_crystal",
        "echoashfallprotocol:frozen_conduit",
        "echoashfallprotocol:thermal_array",
        "minecraft:blue_ice",
        "minecraft:packed_ice",
    },
    "faction": {
        "echoashfallprotocol:bio_processing_station",
        "echoashfallprotocol:map_table",
        "echoashfallprotocol:power_node",
        "echoashfallprotocol:spore_garden",
        "echoashfallprotocol:supply_crate",
        "echoashfallprotocol:trade_counter",
        "echoashfallprotocol:weapon_rack",
    },
    "global": {
        "echoashfallprotocol:rain_collector",
        "echoashfallprotocol:rusted_metal_debris",
        "echoashfallprotocol:supply_crate",
        "minecraft:campfire",
    },
    "industrial_ruins": {
        "echoashfallprotocol:battery_bank",
        "echoashfallprotocol:factory_controller",
        "echoashfallprotocol:item_pipe",
        "echoashfallprotocol:power_cable",
        "echoashfallprotocol:scrap_press",
    },
    "nexus_scar": {
        "echoashfallprotocol:echo_crystal",
        "echoashfallprotocol:energized_fissure",
        "echoashfallprotocol:riftstone",
        "minecraft:crying_obsidian",
    },
    "radiation_zone": {
        "echoashfallprotocol:fallout_dust",
        "echoashfallprotocol:radiation_block",
        "echoashfallprotocol:toxic_waste_barrel",
        "echoashfallprotocol:uranium_crystal",
        "minecraft:redstone_torch",
    },
    "ruined_cityscape": {
        "echoashfallprotocol:concrete_rubble",
        "echoashfallprotocol:oil_stained_concrete",
        "echoashfallprotocol:power_cable",
        "echoashfallprotocol:rebar_block",
        "echoashfallprotocol:signal_scanner",
    },
    "ruined_plains": {
        "echoashfallprotocol:charred_wood_log",
        "echoashfallprotocol:rain_collector",
        "echoashfallprotocol:supply_crate",
        "echoashfallprotocol:wild_berry_bush",
        "minecraft:campfire",
    },
    "toxic_swamp": {
        "echoashfallprotocol:acidic_sludge",
        "echoashfallprotocol:bio_processing_station",
        "echoashfallprotocol:corroded_pipe",
        "echoashfallprotocol:toxic_puddle",
        "echoashfallprotocol:toxic_waste_barrel",
    },
}

POI_SIZE_RULES = {
    "landmark": {
        "drop_pod_wreck_large",
        "cargo_module_field",
        "wasteland_bunker_ruin",
        "collapsed_tower_large",
        "corroded_pipe_network",
        "reactor_containment_ruin",
        "frozen_lab_large",
        "industrial_factory_shell",
        "nexus_pylon",
        "floating_obelisk_cluster",
    },
    "big": {
        "bio_facility",
        "containment_facility_ruin",
        "factory_shell_large",
        "large_storage_tank",
        "power_plant_ruin",
        "reactor_gatehouse",
        "subway_station",
        "server_farm",
        "bunker_complex",
        "trader_post",
        "settlement_ruins",
        "settlement_radwarden",
        "outpost_large",
        "sanctuary_garden",
    },
    "medium": {
        "cargo_lift_wreck",
        "crashbreak_worksite",
        "burned_convoy",
        "relay_tower",
        "watchtower_ruin",
        "homestead_ruin",
        "industrial_factory",
        "rail_signal_yard",
        "factory_pipe_gate",
        "toxic_pump",
        "pipe_pump_house",
        "radiation_beacon_line",
        "frozen_comms_tower",
        "cryo_tank_field",
        "sporebound_spore_garden",
        "crashbreak_market",
        "radwarden_watchtower",
    },
}

POI_ROLE_KEYWORDS = {
    "anomaly": ("nexus", "obelisk", "pylon", "rift", "anomaly"),
    "hub": ("settlement", "outpost", "post", "sanctuary", "market", "trader", "village"),
    "hazard": ("reactor", "containment", "toxic", "sludge", "radiation", "crater", "breach", "bio"),
    "industrial": ("factory", "pipe", "pump", "tank", "yard", "gantry", "rail"),
    "cache": ("cache", "supply", "depot", "storage", "cargo"),
    "salvage": ("wreck", "convoy", "worksite", "debris", "scrap"),
    "camp": ("camp", "homestead", "watchtower", "bunker"),
}

HAZARD_BY_CATEGORY = {
    "crash_zone_wasteland": "medium",
    "cryogenic_ruins": "medium",
    "faction": "low",
    "global": "low",
    "industrial_ruins": "medium",
    "nexus_scar": "anomaly",
    "radiation_zone": "high",
    "ruined_cityscape": "low",
    "ruined_plains": "low",
    "toxic_swamp": "high",
}

ROUTE_ROLE_OVERRIDES = {
    "bio_lab": "hazard",
    "data_center_ruin": "cache",
    "military_vault": "cache",
    "reactor_ruin": "hazard",
    "scavenger_camp": "camp",
    "supply_cache": "cache",
    "road_checkpoint": "route",
    "radio_relay": "route",
}

EXPLAINED_OVERHANG_TEMPLATES = {
    "bio_facility": "broken bio-lab roof shell",
    "containment_facility_ruin": "collapsed containment ring",
    "spore_research_hut": "raised swamp hut shell",
    "subway_station": "buried platform canopy",
    "wasteland_bunker_ruin": "sheared bunker roof",
}

QUALITY_CACHE_BLOCKS = {
    "minecraft:barrel",
    "minecraft:chest",
    "minecraft:trapped_chest",
    "echoashfallprotocol:supply_crate",
}

QUALITY_PATH_BLOCKS = {
    "minecraft:coarse_dirt",
    "minecraft:dirt_path",
    "minecraft:gravel",
    "minecraft:moss_block",
    "minecraft:oak_planks",
    "minecraft:smooth_stone",
    "minecraft:snow_block",
    "echoashfallprotocol:acid_mud",
    "echoashfallprotocol:burnt_wasteland_soil",
    "echoashfallprotocol:fallout_dust",
    "echoashfallprotocol:frozen_soil",
    "echoashfallprotocol:permafrost",
    "echoashfallprotocol:oil_stained_concrete",
    "echoashfallprotocol:riftstone",
    "echoashfallprotocol:scorched_ash",
}

QUALITY_HAZARD_BLOCKS = {
    "minecraft:magma_block",
    "minecraft:powder_snow",
    "echoashfallprotocol:acidic_sludge",
    "echoashfallprotocol:energized_fissure",
    "echoashfallprotocol:fallout_dust",
    "echoashfallprotocol:oil_stained_concrete",
    "echoashfallprotocol:radiation_block",
    "echoashfallprotocol:riftstone",
    "echoashfallprotocol:rusted_metal_debris",
    "echoashfallprotocol:toxic_puddle",
    "echoashfallprotocol:toxic_waste_barrel",
}


def infer_poi_size(name: str) -> str:
    for size, names in POI_SIZE_RULES.items():
        if name in names:
            return size
    if name in LANDMARK_NAMES:
        return "landmark"
    if any(token in name for token in ["_large", "_facility", "_complex", "_farm", "_yard", "_outpost", "_settlement", "_encampment"]):
        return "big"
    if any(token in name for token in ["_medium", "_homestead", "_worksite", "_field", "_station", "_post", "_tower"]):
        return "medium"
    return "small"


def infer_route_role(name: str, category: str) -> str:
    if category == "faction":
        return "faction_hub"
    if name in ROUTE_ROLE_OVERRIDES:
        return ROUTE_ROLE_OVERRIDES[name]
    for role, keywords in POI_ROLE_KEYWORDS.items():
        if any(keyword in name for keyword in keywords):
            return role
    if category == "nexus_scar":
        return "anomaly"
    if category in {"toxic_swamp", "radiation_zone"}:
        return "hazard"
    if category == "industrial_ruins":
        return "industrial"
    return "route"


def infer_loot_tier(size: str, route_role: str, category: str) -> str:
    if category == "faction":
        return "faction"
    if size == "landmark" or route_role in {"anomaly", "hazard"}:
        return "landmark" if size == "landmark" else "high"
    if size == "big":
        return "high"
    if size == "medium" or route_role in {"cache", "hub", "faction_hub"}:
        return "medium"
    return "low"


def infer_hazard_tier(name: str, category: str, route_role: str) -> str:
    hazard = HAZARD_BY_CATEGORY.get(category, "low")
    if route_role in {"hazard", "anomaly"}:
        hazard = "anomaly" if route_role == "anomaly" else "high"
    if any(token in name for token in ["reactor", "containment", "sludge", "toxic", "radiation", "rift"]):
        hazard = "high"
    return hazard


def poi_profile(name: str, category: str, static: bool = False) -> dict[str, object]:
    size = infer_poi_size(name)
    route_role = infer_route_role(name, category)
    loot_tier = infer_loot_tier(size, route_role, category)
    hazard_tier = infer_hazard_tier(name, category, route_role)
    return {
        "name": name,
        "category": category,
        "size": size,
        "route_role": route_role,
        "loot_tier": loot_tier,
        "hazard_tier": hazard_tier,
        "landmark": size == "landmark" or name in LANDMARK_NAMES,
        "faction": category == "faction",
        "static": static,
        "required_signatures": sorted(CATEGORY_SIGNATURE_BLOCKS.get(category, set())),
    }


def stable_seed(name: str) -> int:
    digest = hashlib.sha256(name.encode("utf-8")).digest()
    return int.from_bytes(digest[:4], "big") & 0x7FFFFFFF


def get_output_roots(project_root: Path) -> list[Path]:
    """Return all structure template roots we keep in sync.

    Minecraft's resource loader in this repo is consuming the singular
    ``data/.../structure`` tree at runtime, while some older tools still look in
    ``data/.../structures``. We write both so regenerated templates remain
    compatible and the runtime copy is always valid.
    """
    data_root = project_root / "src" / "main" / "resources" / "data" / "echoashfallprotocol"
    return [
        data_root / "structure",
        data_root / "structures",
    ]


def iter_output_files(output_roots: Iterable[Path], category: str, name: str) -> Iterable[Path]:
    for base_path in output_roots:
        if category == "global":
            yield base_path / "global" / f"{name}.nbt"
        elif category == "faction":
            # Faction structures use path separators for subdirectories
            yield base_path / "faction" / f"{name}.nbt"
        else:
            yield base_path / "biomes" / category / f"{name}.nbt"
        for alias_path in ALIAS_PATHS.get(name, []):
            yield base_path / f"{alias_path}.nbt"


def category_name_from_ref(ref: str) -> tuple[str | None, str]:
    parts = ref.replace("\\", "/").split("/")
    if len(parts) >= 3 and parts[0] == "biomes":
        return parts[1], "/".join(parts[2:])
    if len(parts) >= 2 and parts[0] == "faction":
        return "faction", "/".join(parts[1:])
    if len(parts) >= 2 and parts[0] == "global":
        return "global", "/".join(parts[1:])
    name = "/".join(parts)
    return CATEGORY_MAP.get(name), name


def is_generated_ref(ref: str, selected_set: set[str]) -> bool:
    _, name = category_name_from_ref(ref)
    return name in selected_set


def parse_args(argv: list[str]) -> argparse.Namespace:
    categories = sorted(set(CATEGORY_MAP.values()))
    parser = argparse.ArgumentParser(description="Generate Ashfall POI structure templates.")
    parser.add_argument("--dry-run", action="store_true", help="Report selected templates without writing NBT files.")
    parser.add_argument("--check", action="store_true", help="Validate template-pool references and generated NBT files.")
    parser.add_argument("--category", choices=["all", *categories], default="all", help="Generate or check one category.")
    parser.add_argument(
        "--target",
        action="append",
        default=[],
        help="Limit to one structure name. Can be supplied multiple times.",
    )
    return parser.parse_args(argv)


def selected_shape_names(args: argparse.Namespace) -> list[str]:
    requested = set(args.target)
    names = []
    for name in SHAPE_REGISTRY:
        category = CATEGORY_MAP.get(name)
        if category is None:
            continue
        if not requested and name in CURATED_TEMPLATE_NAMES:
            continue
        if args.category != "all" and category != args.category:
            continue
        if requested and name not in requested:
            continue
        names.append(name)
    return names


def get_template_pool_refs(project_root: Path) -> set[str]:
    pool_dir = project_root / "src" / "main" / "resources" / "data" / "echoashfallprotocol" / "worldgen" / "template_pool"
    refs: set[str] = set()
    for path in pool_dir.rglob("*.json"):
        data = json.loads(path.read_text(encoding="utf-8"))
        for element in data.get("elements", []):
            location = element.get("element", {}).get("location", "")
            if location.startswith("echoashfallprotocol:"):
                refs.add(location.removeprefix("echoashfallprotocol:"))
    return refs


def _palette_names(nbt) -> list[str]:
    names: list[str] = []
    for entry in nbt["palette"]:
        names.append(str(entry["Name"]))
    return names


def _support_summary(items: list[tuple[tuple[int, int, int], str]], limit: int = 8) -> str:
    counts = Counter(block_name for _, block_name in items)
    return ", ".join(f"{block}={count}" for block, count in counts.most_common(limit))


def _has_side_or_top_attachment(
    pos: tuple[int, int, int],
    block_name: str,
    positions: dict[tuple[int, int, int], str],
) -> bool:
    x, y, z = pos
    attachment_tokens = ("chain", "fence", "iron_bars", "ladder", "slab", "stairs", "vine")
    attachment_blocks = {"minecraft:lantern", "minecraft:redstone_torch", "minecraft:torch"}
    if block_name not in attachment_blocks and not any(token in block_name for token in attachment_tokens):
        return False
    for neighbor in (
        (x, y + 1, z),
        (x - 1, y, z),
        (x + 1, y, z),
        (x, y, z - 1),
        (x, y, z + 1),
    ):
        if neighbor in positions:
            return True
    return False


def _is_architectural_overhang(block_name: str) -> bool:
    return any(
        token in block_name
        for token in (
            "bars",
            "bricks",
            "chain",
            "concrete",
            "fence",
            "glass",
            "hull",
            "log",
            "obsidian",
            "planks",
            "slab",
            "stairs",
            "stone",
            "terracotta",
            "wool",
        )
    )


def validate_support_audit(
    path: Path,
    palette: list[str],
    blocks,
    errors: list[str],
    warnings: list[str],
    landmark: bool,
    name: str | None = None,
) -> None:
    positions: dict[tuple[int, int, int], str] = {}
    for block in blocks:
        state = int(block["state"])
        block_name = palette[state] if 0 <= state < len(palette) else "<bad-state>"
        if block_name == "minecraft:air":
            continue
        pos = [int(v) for v in block["pos"]]
        if len(pos) == 3:
            positions[(pos[0], pos[1], pos[2])] = block_name

    unsupported: list[tuple[tuple[int, int, int], str]] = []
    support_sensitive: list[tuple[tuple[int, int, int], str]] = []
    low_mass: list[tuple[tuple[int, int, int], str]] = []
    for (x, y, z), block_name in positions.items():
        if y <= 0:
            continue
        if (x, y - 1, z) in positions:
            continue
        if _has_side_or_top_attachment((x, y, z), block_name, positions):
            continue
        item = ((x, y, z), block_name)
        unsupported.append(item)
        if is_support_sensitive_block(block_name):
            support_sensitive.append(item)
        if y <= 3:
            low_mass.append(item)

    if support_sensitive:
        errors.append(
            f"VISUAL_UNSUPPORTED_DETAIL {path}: {len(support_sensitive)} support-sensitive blocks "
            f"({_support_summary(support_sensitive)})"
        )

    if not positions:
        return

    intentional_float = name in INTENTIONAL_FLOATING_TEMPLATE_NAMES
    if intentional_float:
        if low_mass:
            warnings.append(
                f"VISUAL_LOW_FLOATING_MASS {path}: {len(low_mass)} low unsupported blocks "
                f"({_support_summary(low_mass)})"
            )
        return

    unsupported_ratio = len(unsupported) / max(1, len(positions))
    error_ratio = 0.32 if landmark else 0.22
    warn_ratio = 0.22 if landmark else 0.14
    non_architectural = [
        item
        for item in unsupported
        if not _is_architectural_overhang(item[1])
    ]
    non_arch_limit = max(24, int(len(positions) * 0.08))
    if len(positions) >= 64 and unsupported_ratio > error_ratio:
        if len(non_architectural) > non_arch_limit:
            errors.append(
                f"VISUAL_EXCESS_UNSUPPORTED {path}: {len(unsupported)}/{len(positions)} "
                f"({unsupported_ratio:.0%}; {_support_summary(unsupported)})"
            )
        elif name not in EXPLAINED_OVERHANG_TEMPLATES:
            warnings.append(
                f"VISUAL_EXCESS_ARCH_OVERHANG {path}: {len(unsupported)}/{len(positions)} "
                f"({unsupported_ratio:.0%}; {_support_summary(unsupported)})"
            )
    elif len(positions) >= 64 and unsupported_ratio > warn_ratio and name not in EXPLAINED_OVERHANG_TEMPLATES:
        warnings.append(
            f"VISUAL_EXCESS_UNSUPPORTED {path}: {len(unsupported)}/{len(positions)} "
            f"({unsupported_ratio:.0%}; {_support_summary(unsupported)})"
        )

    low_limit = max(12, len(positions) // 28)
    if len(low_mass) > low_limit:
        errors.append(
            f"VISUAL_LOW_UNSUPPORTED_MASS {path}: {len(low_mass)} low unsupported blocks "
            f"({_support_summary(low_mass)})"
        )


def validate_poi_quality_nbt(
    path: Path,
    block_names: list[str],
    bottom: list[tuple[int, int, str]],
    errors: list[str],
    warnings: list[str],
    category: str,
    name: str,
) -> None:
    profile = poi_profile(name, category)
    size = str(profile["size"])
    route_role = str(profile["route_role"])
    loot_tier = str(profile["loot_tier"])
    hazard_tier = str(profile["hazard_tier"])

    cache_count = sum(1 for block_name in block_names if block_name in QUALITY_CACHE_BLOCKS)
    path_count = sum(1 for block_name in block_names if block_name in QUALITY_PATH_BLOCKS)
    hazard_count = sum(1 for block_name in block_names if block_name in QUALITY_HAZARD_BLOCKS)
    signature_count = sum(
        1 for block_name in block_names if block_name in CATEGORY_SIGNATURE_BLOCKS.get(category, set())
    )

    min_cache = {
        "low": 1,
        "medium": 1,
        "high": 2,
        "landmark": 2,
        "faction": 2,
    }.get(loot_tier, 1)
    if cache_count < min_cache:
        errors.append(f"POI_LOW_CACHE {path}: {cache_count}/{min_cache} sheltered cache blocks for {loot_tier}")

    min_path = {
        "small": 3,
        "medium": 6,
        "big": 8,
        "landmark": 10,
    }.get(size, 4)
    if path_count < min_path:
        errors.append(f"POI_WEAK_ROUTE {path}: {path_count}/{min_path} path/approach blocks for {size} {route_role}")

    min_hazard = {
        "none": 0,
        "low": 0,
        "medium": 2,
        "high": 5,
        "anomaly": 6,
    }.get(hazard_tier, 0)
    if hazard_count < min_hazard:
        errors.append(f"POI_WEAK_HAZARD_READ {path}: {hazard_count}/{min_hazard} hazard markers for {hazard_tier}")

    min_signature = 1 if category == "global" else 2
    if size in {"big", "landmark"}:
        min_signature += 1
    if signature_count < min_signature:
        errors.append(f"POI_WEAK_SIGNATURE {path}: {signature_count}/{min_signature} category signature blocks")

    if category == "faction":
        service_blocks = {
            "echoashfallprotocol:map_table",
            "echoashfallprotocol:trade_counter",
            "echoashfallprotocol:weapon_rack",
            "echoashfallprotocol:power_node",
            "echoashfallprotocol:bio_processing_station",
            "echoashfallprotocol:spore_garden",
        }
        service_count = sum(1 for block_name in block_names if block_name in service_blocks)
        if service_count < 3:
            errors.append(f"POI_FACTION_WEAK_SERVICE_PAD {path}: {service_count}/3 service or role blocks")

    if bottom and size in {"big", "landmark"}:
        xs = [x for x, _, _ in bottom]
        zs = [z for _, z, _ in bottom]
        width = max(xs) - min(xs) + 1
        depth = max(zs) - min(zs) + 1
        if width >= 10 and depth >= 10:
            edge_path = sum(
                1
                for x, z, block_name in bottom
                if block_name in QUALITY_PATH_BLOCKS
                and (x <= min(xs) + 1 or x >= max(xs) - 1 or z <= min(zs) + 1 or z >= max(zs) - 1)
            )
            if edge_path < 2:
                errors.append(f"POI_NO_READABLE_ENTRANCE {path}: no path blocks touch the outer approach edge")


def validate_visual_nbt(
    path: Path,
    nbt,
    errors: list[str],
    warnings: list[str],
    landmark: bool,
    category: str | None = None,
    name: str | None = None,
) -> None:
    palette = _palette_names(nbt)
    blocks = nbt["blocks"]
    block_names: list[str] = []
    bottom: list[tuple[int, int, str]] = []
    for block in blocks:
        state = int(block["state"])
        block_name = palette[state] if 0 <= state < len(palette) else "<bad-state>"
        block_names.append(block_name)
        pos = [int(v) for v in block["pos"]]
        if len(pos) == 3 and pos[1] == 0:
            bottom.append((pos[0], pos[2], block_name))

    forbidden = sorted(set(block_names) & FORBIDDEN_GENERATED_BLOCKS)
    if forbidden:
        errors.append(f"VISUAL_FORBIDDEN_BLOCK {path}: {', '.join(forbidden)}")

    if name in CURATED_TEMPLATE_NAMES:
        return

    if name not in CURATED_TEMPLATE_NAMES:
        unique_blocks = set(block_names)
        if len(block_names) < 24:
            errors.append(f"VISUAL_UNDER_DETAILED {path}: only {len(block_names)} blocks")
        if len(block_names) >= 64 and len(unique_blocks) < 4:
            errors.append(f"VISUAL_LOW_VARIETY {path}: only {len(unique_blocks)} block types")

        signature_blocks = CATEGORY_SIGNATURE_BLOCKS.get(category or "")
        if signature_blocks:
            signature_count = sum(1 for block_name in block_names if block_name in signature_blocks)
            required = 1 if category == "global" else 2
            if signature_count < required:
                errors.append(
                    f"VISUAL_LOW_SIGNATURE {path}: {signature_count}/{required} "
                    f"{category} signature blocks"
                )
        validate_support_audit(path, palette, blocks, errors, warnings, landmark, name)
        if category and name:
            validate_poi_quality_nbt(path, block_names, bottom, errors, warnings, category, name)

    is_cryo = "cryogenic" in str(path).replace("\\", "/")
    if not is_cryo:
        bright_count = sum(1 for block_name in block_names if block_name in VISUAL_PLACEHOLDER_BLOCKS)
        if bright_count > max(12, len(block_names) // 12):
            warnings.append(f"VISUAL_BRIGHT_PLACEHOLDER {path}: {bright_count} bright white/quartz/wool blocks")

    if bottom:
        xs = [x for x, _, _ in bottom]
        zs = [z for _, z, _ in bottom]
        area = (max(xs) - min(xs) + 1) * (max(zs) - min(zs) + 1)
        if area >= 96:
            counts = Counter(name for _, _, name in bottom)
            dominant, count = counts.most_common(1)[0]
            coverage = len(bottom) / area
            dominance = count / max(1, len(bottom))
            if coverage > 0.96 and dominance > 0.90:
                errors.append(
                    f"VISUAL_RECTANGULAR_FOOTPRINT {path}: {dominant} covers {dominance:.0%} "
                    f"of a {len(bottom)}/{area} bottom layer"
                )
            elif coverage > 0.88 and dominance > 0.82:
                warnings.append(
                    f"VISUAL_RECTANGULAR_FOOTPRINT {path}: {dominant} covers {dominance:.0%} "
                    f"of a {len(bottom)}/{area} bottom layer"
                )

        terrain_bottom = [(x, z, block_name) for x, z, block_name in bottom if is_terrain_blend_block(block_name)]
        if len(terrain_bottom) >= 96:
            txs = [x for x, _, _ in terrain_bottom]
            tzs = [z for _, z, _ in terrain_bottom]
            terrain_area = (max(txs) - min(txs) + 1) * (max(tzs) - min(tzs) + 1)
            if terrain_area >= 96:
                terrain_counts = Counter(block_name for _, _, block_name in terrain_bottom)
                dominant_terrain, terrain_count = terrain_counts.most_common(1)[0]
                terrain_coverage = len(terrain_bottom) / terrain_area
                terrain_dominance = terrain_count / max(1, len(terrain_bottom))
                if terrain_coverage > 0.92 and terrain_dominance > 0.78:
                    errors.append(
                        f"VISUAL_FLAT_TERRAIN_APRON {path}: {dominant_terrain} covers "
                        f"{terrain_dominance:.0%} of a {len(terrain_bottom)}/{terrain_area} terrain apron"
                    )
                elif terrain_coverage > 0.84 and terrain_dominance > 0.70:
                    warnings.append(
                        f"VISUAL_FLAT_TERRAIN_APRON {path}: {dominant_terrain} covers "
                        f"{terrain_dominance:.0%} of a {len(terrain_bottom)}/{terrain_area} terrain apron"
                    )


def validate_nbt_file(
    path: Path,
    errors: list[str],
    warnings: list[str],
    landmark: bool,
    category: str | None = None,
    name: str | None = None,
) -> None:
    try:
        import nbtlib

        nbt = nbtlib.load(str(path))
    except Exception as exc:  # noqa: BLE001 - report precise template failures.
        errors.append(f"BAD_NBT {path}: {exc}")
        return

    for key in ("DataVersion", "size", "palette", "blocks", "entities"):
        if key not in nbt:
            errors.append(f"BAD_NBT {path}: missing {key}")
            return

    block_count = len(nbt["blocks"])
    if block_count <= 0:
        errors.append(f"EMPTY_NBT {path}")

    size = [int(v) for v in nbt["size"]]
    if len(size) != 3:
        errors.append(f"BAD_NBT_SIZE {path}: {size}")
    elif landmark and (size[0] > 80 or size[1] > 80 or size[2] > 80):
        errors.append(f"LANDMARK_TOO_LARGE {path}: {size[0]}x{size[1]}x{size[2]}")

    validate_visual_nbt(path, nbt, errors, warnings, landmark, category, name)


def check_outputs(project_root: Path, selected_names: list[str], output_roots: list[Path]) -> int:
    errors: list[str] = []
    warnings: list[str] = []
    refs = get_template_pool_refs(project_root)
    selected_set = set(selected_names)
    validated_paths: set[Path] = set()

    for root in output_roots:
        for ref in sorted(refs):
            path = root / f"{ref}.nbt"
            if not path.exists():
                errors.append(f"MISSING_TEMPLATE {path}")
                continue
            category, ref_name = category_name_from_ref(ref)
            validate_nbt_file(path, errors, warnings, ref_name in LANDMARK_NAMES, category, ref_name)
            validated_paths.add(path.resolve())

    for name in selected_names:
        category = CATEGORY_MAP[name]
        for path in iter_output_files(output_roots, category, name):
            if path.resolve() in validated_paths:
                continue
            if path.exists():
                validate_nbt_file(path, errors, warnings, name in LANDMARK_NAMES, category, name)
            else:
                errors.append(f"MISSING_GENERATED {path}")

    category_counts = Counter()
    for ref in refs:
        category, _ = category_name_from_ref(ref)
        if category:
            category_counts[category] += 1

    print(f"Checked {len(refs)} template-pool refs and {len(selected_set)} selected generators")
    print(
        "POI audit coverage: "
        + ", ".join(f"{category}={count}" for category, count in sorted(category_counts.items()))
    )
    if errors or warnings:
        issue_counts = Counter(message.split()[0] for message in errors + warnings)
        print("POI audit findings: " + ", ".join(f"{code}={count}" for code, count in sorted(issue_counts.items())))
    if warnings:
        print("Visual warnings:")
        for warning in warnings[:80]:
            print(warning)
        if len(warnings) > 80:
            print(f"... and {len(warnings) - 80} more visual warnings")
    if errors:
        print("\n".join(errors))
        return 1
    print("Structure check passed.")
    return 0


def load_structure_blocks(path: Path) -> list[tuple[int, int, int, str, dict[str, str] | None]]:
    import nbtlib

    nbt = nbtlib.load(str(path))
    palette: list[tuple[str, dict[str, str] | None]] = []
    for entry in nbt["palette"]:
        props = None
        if "Properties" in entry:
            props = {str(key): str(value) for key, value in entry["Properties"].items()}
        palette.append((str(entry["Name"]), props))

    blocks: list[tuple[int, int, int, str, dict[str, str] | None]] = []
    for block in nbt["blocks"]:
        if "nbt" in block:
            raise ValueError(f"{path} has block-entity NBT; static grounding repair must preserve it explicitly")
        state = int(block["state"])
        block_name, props = palette[state]
        pos = [int(v) for v in block["pos"]]
        blocks.append((pos[0], pos[1], pos[2], block_name, props))
    return blocks


def repair_referenced_static_templates(
    refs: set[str],
    selected_names: list[str],
    output_roots: list[Path],
) -> int:
    """Apply the canonical grounding repair to referenced hand-maintained POIs.

    Static templates are already part of the audit surface, but they do not have
    source shape definitions. Keep the repair pass idempotent so repeated
    generator runs do not grow approach paths outward forever.
    """
    selected_set = set(selected_names)
    repaired = 0
    for root in output_roots:
        for ref in sorted(refs):
            category, name = category_name_from_ref(ref)
            if name in CURATED_TEMPLATE_NAMES or is_generated_ref(ref, selected_set):
                continue
            if category is None:
                continue
            path = root / f"{ref}.nbt"
            if not path.exists():
                continue
            blocks = load_structure_blocks(path)
            seed = stable_seed(ref)
            grounded = apply_grounding_pass(blocks, category, seed, name, soften_outline=False)
            write_structure_nbt(grounded, path)
            print(f"  Repaired static: {path} ({len(grounded)} blocks)")
            repaired += 1
    return repaired


def sync_referenced_templates(project_root: Path, output_roots: list[Path]) -> int:
    """Mirror any hand-maintained referenced templates into legacy output roots."""
    if len(output_roots) < 2:
        return 0
    primary_root, *mirror_roots = output_roots
    copied = 0
    for ref in sorted(get_template_pool_refs(project_root)):
        source = primary_root / f"{ref}.nbt"
        if not source.exists():
            continue
        for mirror_root in mirror_roots:
            target = mirror_root / f"{ref}.nbt"
            if target.exists():
                continue
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, target)
            print(f"  Mirrored: {target}")
            copied += 1
    return copied


def main() -> int:
    args = parse_args(sys.argv[1:])
    script_dir = Path(__file__).parent.resolve()
    project_root = script_dir.parent.parent.resolve()
    output_roots = get_output_roots(project_root)
    selected_names = selected_shape_names(args)

    if not output_roots[0].parent.exists():
        print(f"ERROR: Project resources path not found: {output_roots[0].parent}")
        return 1

    if args.check:
        return check_outputs(project_root, selected_names, output_roots)

    if args.dry_run:
        categories = {}
        for name in selected_names:
            categories[CATEGORY_MAP[name]] = categories.get(CATEGORY_MAP[name], 0) + 1
        print(f"Dry run: {len(selected_names)} templates selected")
        for category, count in sorted(categories.items()):
            print(f"  {category}: {count}")
        return 0

    generated = 0
    skipped = 0
    files_written = 0

    for name in selected_names:
        generator = SHAPE_REGISTRY[name]
        category = CATEGORY_MAP[name]
        # Seed each template uniquely so re-runs are deterministic per template
        seed = stable_seed(name)
        profile = poi_profile(name, category)
        blocks = generator(seed)
        if name != "drop_pod":
            blocks = apply_theme_polish(name, category, blocks, seed)

        # Apply detail pass for enhanced structures (skip drop_pod)
        if name != "drop_pod":
            blocks = apply_detail_pass(
                blocks,
                category,
                category,
                seed,
                structure_size=str(profile["size"]),
                name=name,
                profile=profile,
            )

        if not blocks:
            print(f"  SKIP (empty): {name}")
            skipped += 1
            continue

        output_files = list(iter_output_files(output_roots, category, name))
        for output_file in output_files:
            write_structure_nbt(blocks, output_file)
            print(f"  Generated: {output_file} ({len(blocks)} blocks)")
            files_written += 1
        generated += 1

    static_repaired = 0
    if args.category == "all" and not args.target:
        static_repaired = repair_referenced_static_templates(
            get_template_pool_refs(project_root),
            selected_names,
            output_roots,
        )

    mirrored = sync_referenced_templates(project_root, output_roots)

    print(f"\n{'=' * 50}")
    print(
        f"  Templates: {generated}  |  Files written: {files_written}  |  "
        f"Static repaired: {static_repaired}  |  Mirrored: {mirrored}  |  Skipped: {skipped}"
    )
    print(f"{'=' * 50}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
