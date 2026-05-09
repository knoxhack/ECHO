#!/usr/bin/env python3
"""Validate high-risk ECHO: Ashfall Protocol gameplay data references.

This is intentionally lightweight: it does not try to emulate Minecraft's full
data loader, but it catches the common regressions that break fresh-world tests:
missing mod item/block IDs, invalid advancement structure references, recipe
ingredient drift, and biome/feature reference mismatches.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any, Iterable


ROOT = Path(__file__).resolve().parents[1]
MODID = "echoashfallprotocol"
KNOWN_NAMESPACES = {MODID, "minecraft"}
JAVA_ROOT = ROOT / "src/main/java/com/knoxhack/echoashfallprotocol"
CORE_JAVA_ROOT = ROOT / "core/echocore/src/main/java/com/knoxhack/echocore"
ORBITAL_JAVA_ROOT = ROOT / "addons/echoorbitalremnants/src/main/java/com/knoxhack/echoorbitalremnants"
TERMINAL_JAVA_ROOT = ROOT / "addons/echoterminal/src/main/java/com/knoxhack/echoterminal"
DATA_ROOT = ROOT / f"src/main/resources/data/{MODID}"
ASSET_ROOT = ROOT / f"src/main/resources/assets/{MODID}"
POD_TEMPLATE_PATHS = (
    DATA_ROOT / "structure/drop_pod.nbt",
    DATA_ROOT / "structure/global/drop_pod.nbt",
)

GENERIC_UNUSED_PLACED_FEATURE_ALLOWLIST = {
    f"{MODID}:acidic_sludge_pools",
    f"{MODID}:ash_bushes",
    f"{MODID}:ash_layer_patches",
    f"{MODID}:bone_scatter",
    f"{MODID}:burnt_ferns",
    f"{MODID}:charred_trees",
    f"{MODID}:charred_trees_bare",
    f"{MODID}:charred_trees_split",
    f"{MODID}:concrete_chunk_scatter",
    f"{MODID}:concrete_rubble_piles",
    f"{MODID}:contaminated_soil_patches",
    f"{MODID}:dead_trees",
    f"{MODID}:dead_trees_fallen",
    f"{MODID}:dead_trees_snapped",
    f"{MODID}:dead_trees_stumps",
    f"{MODID}:debris_pile",
    f"{MODID}:deep_ash_patch",
    f"{MODID}:deep_ash_surface",
    f"{MODID}:dry_grass_patches",
    f"{MODID}:fallout_dust_patches",
    f"{MODID}:irradiated_cactus",
    f"{MODID}:metal_debris_scatter",
    f"{MODID}:mutated_bush_clusters",
    f"{MODID}:mutated_giant_trees",
    f"{MODID}:mutated_gray_trees",
    f"{MODID}:mutated_purple_trees",
    f"{MODID}:mutated_red_trees",
    f"{MODID}:mutated_saplings",
    f"{MODID}:mutated_twisted_trees",
    f"{MODID}:nuclear_fungus",
    f"{MODID}:nuclear_fungus_clusters",
    f"{MODID}:oil_stained_concrete_patches",
    f"{MODID}:radiation_hotspot",
    f"{MODID}:radiation_zones",
    f"{MODID}:rubble_scatter",
    f"{MODID}:rusted_metal_debris",
    f"{MODID}:rusted_metal_debris_scatter",
    f"{MODID}:scattered_bones_dense",
    f"{MODID}:toxic_moss",
    f"{MODID}:toxic_moss_patches",
    f"{MODID}:toxic_puddle_patches",
    f"{MODID}:wasteland_reeds",
}

WORLDGEN_SURFACE_GRASS_BLOCKS = {
    f"{MODID}:wasteland_grass_block",
    f"{MODID}:toxic_wasteland_grass_block",
    f"{MODID}:mutated_wasteland_grass_block",
}

REQUIRED_PLANT_GROUND_BLOCKS = {
    f"{MODID}:wasteland_dirt",
    f"{MODID}:wasteland_grass_block",
    f"{MODID}:ashen_wasteland_dirt",
    f"{MODID}:burnt_wasteland_soil",
    f"{MODID}:contaminated_soil",
    f"{MODID}:toxic_wasteland_grass_block",
    f"{MODID}:mutated_wasteland_grass_block",
    f"{MODID}:irradiated_crust",
    f"{MODID}:nexus_cracked_soil",
    f"{MODID}:cracked_earth",
    f"{MODID}:scorched_ash",
    f"{MODID}:acid_mud",
    f"{MODID}:radioactive_sludge",
    f"{MODID}:permafrost",
}

EXPECTED_STRUCTURE_SET_PLACEMENT = {
    "poi_crash_zone_wasteland": ("CRASH_POI_SPACING", "CRASH_POI_SEPARATION"),
    "poi_ruined_cityscape": ("URBAN_POI_SPACING", "URBAN_POI_SEPARATION"),
    "poi_industrial_ruins": ("URBAN_POI_SPACING", "URBAN_POI_SEPARATION"),
    "poi_radiation_zone": ("MICRO_POI_SPACING", "MICRO_POI_SEPARATION"),
    "poi_toxic_swamp": ("MICRO_POI_SPACING", "MICRO_POI_SEPARATION"),
    "poi_cryogenic_ruins": ("MICRO_POI_SPACING", "MICRO_POI_SEPARATION"),
    "poi_nexus_scar": ("MICRO_POI_SPACING", "MICRO_POI_SEPARATION"),
    "poi_ruined_plains": ("CAMP_SPACING", "CAMP_SEPARATION"),
    "poi_global": ("GLOBAL_POI_SPACING", "GLOBAL_POI_SEPARATION"),
    "major_pois": ("MAJOR_SPACING", "MAJOR_SEPARATION"),
    "landmark_pois": ("LANDMARK_SPACING", "LANDMARK_SEPARATION"),
    "industrial_factories": ("LANDMARK_SPACING", "LANDMARK_SEPARATION"),
}

STARTER_ONLY_TEMPLATE_LOCATIONS = {
    f"{MODID}:global/drop_pod",
}

FORBIDDEN_NATURAL_DROP_POD_TEMPLATE_TOKENS = (
    "drop_pod",
    "drop_pod_wreck",
)

SUBSTRATE_GRINDER_ITEMS = (
    "minecraft:stone",
    "minecraft:cobblestone",
    "minecraft:deepslate",
    "minecraft:cobbled_deepslate",
    f"{MODID}:wasteland_stone",
    f"{MODID}:wasteland_trace_rubble",
    f"{MODID}:rubble",
    f"{MODID}:concrete_rubble",
    f"{MODID}:concrete_chunk",
    f"{MODID}:industrial_aggregate",
    f"{MODID}:oil_stained_concrete",
    f"{MODID}:crash_slag",
    f"{MODID}:ash_stone",
    f"{MODID}:deep_ash",
    f"{MODID}:toxic_slagstone",
    f"{MODID}:irradiated_shale",
    f"{MODID}:cryogenic_fractured_stone",
    f"{MODID}:nexus_cracked_soil",
    f"{MODID}:riftstone",
    f"{MODID}:iron_shard",
    f"{MODID}:copper_shard",
    f"{MODID}:coal_dust",
    f"{MODID}:gold_trace",
    f"{MODID}:gold_cluster",
    f"{MODID}:uranium_shard",
)

SUBSTRATE_GRINDER_BLOCKS = SUBSTRATE_GRINDER_ITEMS[:19]

POD_EXPECTED_SIZE = (20, 10, 20)
POD_EXPECTED_BED_STATES = {
    (5, 3, 10): (
        "minecraft:white_bed",
        (("facing", "south"), ("occupied", "false"), ("part", "foot")),
    ),
    (5, 3, 11): (
        "minecraft:white_bed",
        (("facing", "south"), ("occupied", "false"), ("part", "head")),
    ),
}

POD_VANILLA_STATE_SCHEMAS = {
    "minecraft:air": {},
    "minecraft:barrel": {
        "facing": {"down", "up", "north", "south", "west", "east"},
        "open": {"false", "true"},
    },
    "minecraft:blast_furnace": {},
    "minecraft:campfire": {
        "facing": {"north", "south", "west", "east"},
        "lit": {"false", "true"},
        "signal_fire": {"false", "true"},
        "waterlogged": {"false", "true"},
    },
    "minecraft:chain": {
        "axis": {"x", "y", "z"},
        "waterlogged": {"false", "true"},
    },
    "minecraft:black_concrete": {},
    "minecraft:chest": {
        "facing": {"north", "south", "west", "east"},
        "type": {"single", "left", "right"},
        "waterlogged": {"false", "true"},
    },
    "minecraft:blackstone": {},
    "minecraft:cartography_table": {},
    "minecraft:coarse_dirt": {},
    "minecraft:crafting_table": {},
    "minecraft:crying_obsidian": {},
    "minecraft:cut_copper": {},
    "minecraft:deepslate": {},
    "minecraft:fire": {},
    "minecraft:gravel": {},
    "minecraft:gray_concrete": {},
    "minecraft:grindstone": {
        "face": {"floor", "wall", "ceiling"},
        "facing": {"north", "south", "west", "east"},
    },
    "minecraft:heavy_weighted_pressure_plate": {
        "power": {str(value) for value in range(16)},
    },
    "minecraft:iron_bars": {
        "east": {"false", "true"},
        "north": {"false", "true"},
        "south": {"false", "true"},
        "waterlogged": {"false", "true"},
        "west": {"false", "true"},
    },
    "minecraft:iron_door": {
        "facing": {"north", "south", "west", "east"},
        "half": {"upper", "lower"},
        "hinge": {"left", "right"},
        "open": {"false", "true"},
        "powered": {"false", "true"},
    },
    "minecraft:iron_trapdoor": {
        "facing": {"north", "south", "west", "east"},
        "half": {"top", "bottom"},
        "open": {"false", "true"},
        "powered": {"false", "true"},
        "waterlogged": {"false", "true"},
    },
    "minecraft:lava": {},
    "minecraft:lectern": {
        "facing": {"north", "south", "west", "east"},
        "has_book": {"false", "true"},
        "powered": {"false", "true"},
    },
    "minecraft:light_gray_concrete": {},
    "minecraft:lightning_rod": {},
    "minecraft:magma_block": {},
    "minecraft:netherrack": {},
    "minecraft:oak_stairs": {
        "facing": {"north", "south", "west", "east"},
        "half": {"top", "bottom"},
        "shape": {"straight", "inner_left", "inner_right", "outer_left", "outer_right"},
        "waterlogged": {"false", "true"},
    },
    "minecraft:oak_wall_sign": {
        "facing": {"north", "south", "west", "east"},
        "waterlogged": {"false", "true"},
    },
    "minecraft:polished_andesite": {},
    "minecraft:polished_andesite_slab": {
        "type": {"top", "bottom", "double"},
        "waterlogged": {"false", "true"},
    },
    "minecraft:polished_blackstone": {},
    "minecraft:polished_deepslate": {},
    "minecraft:quartz_block": {},
    "minecraft:quartz_pillar": {"axis": {"x", "y", "z"}},
    "minecraft:redstone_torch": {"lit": {"false", "true"}},
    "minecraft:sea_lantern": {},
    "minecraft:smooth_stone": {},
    "minecraft:stone_button": {
        "face": {"floor", "wall", "ceiling"},
        "facing": {"north", "south", "west", "east"},
        "powered": {"false", "true"},
    },
    "minecraft:stone_slab": {
        "type": {"top", "bottom", "double"},
        "waterlogged": {"false", "true"},
    },
    "minecraft:white_bed": {
        "facing": {"north", "south", "west", "east"},
        "occupied": {"false", "true"},
        "part": {"head", "foot"},
    },
    "minecraft:yellow_concrete": {},
}


REGISTER_PATTERNS = (
    re.compile(r'registerSimpleItem\("([^"]+)"'),
    re.compile(r'register\("([^"]+)"'),
    re.compile(r'registerSpawnEgg\("([^"]+)"'),
    re.compile(r'registerSimpleBlockItem\("([^"]+)"'),
    re.compile(r'registerCustomBlock\("([^"]+)"'),
    re.compile(r'registerSimpleBlock\("([^"]+)"'),
    re.compile(r'registerSimpleProfessionBlock\("([^"]+)"'),
)


def load_json(path: Path, errors: list[str]) -> Any | None:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # noqa: BLE001 - report any malformed JSON.
        errors.append(f"JSON_PARSE {path.relative_to(ROOT)}: {exc}")
        return None


def collect_registered_ids() -> set[str]:
    ids: set[str] = set()
    for source in (
        JAVA_ROOT / "registry/ModItems.java",
        JAVA_ROOT / "registry/ModBlocks.java",
    ):
        if not source.exists():
            continue
        text = source.read_text(encoding="utf-8")
        for pattern in REGISTER_PATTERNS:
            ids.update(f"{MODID}:{match}" for match in pattern.findall(text))
    return ids


def collect_registered_block_ids() -> set[str]:
    ids: set[str] = set()
    source = JAVA_ROOT / "registry/ModBlocks.java"
    if not source.exists():
        return ids

    text = source.read_text(encoding="utf-8")
    for pattern in REGISTER_PATTERNS:
        ids.update(f"{MODID}:{match}" for match in pattern.findall(text))
    return ids


def json_files(path: Path) -> Iterable[Path]:
    if not path.exists():
        return ()
    return path.rglob("*.json")


def walk_strings(value: Any) -> Iterable[str]:
    if isinstance(value, str):
        yield value
    elif isinstance(value, list):
        for item in value:
            yield from walk_strings(item)
    elif isinstance(value, dict):
        for item in value.values():
            yield from walk_strings(item)


def echo_template_pool_locations(errors: list[str]) -> set[str]:
    locations: set[str] = set()

    def collect_locations(value: Any) -> None:
        if isinstance(value, dict):
            location = value.get("location")
            if isinstance(location, str) and location.startswith(f"{MODID}:"):
                locations.add(location)
            for child in value.values():
                collect_locations(child)
        elif isinstance(value, list):
            for child in value:
                collect_locations(child)

    for path in json_files(DATA_ROOT / "worldgen/template_pool"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        collect_locations(data)
    return locations


def structure_template_path(location: str) -> Path:
    path = location.split(":", 1)[1] if ":" in location else location
    return DATA_ROOT / "structure" / f"{path}.nbt"


def nbt_state_key(entry: Any) -> tuple[str, tuple[tuple[str, str], ...]]:
    props = entry.get("Properties", {})
    return (
        str(entry.get("Name", "")),
        tuple(sorted((str(key), str(value)) for key, value in props.items())),
    )


def nbt_pos(value: Any) -> tuple[int, int, int]:
    return tuple(int(part) for part in value)  # type: ignore[return-value]


def check_item_id(ref: str, registered: set[str], errors: list[str], path: Path, context: str) -> None:
    if ref.startswith(f"{MODID}:") and ref not in registered:
        errors.append(f"MISSING_ITEM {path.relative_to(ROOT)} {context}: {ref}")


def tag_values(path: Path, errors: list[str]) -> set[str]:
    data = load_json(path, errors)
    if not isinstance(data, dict):
        return set()
    values = data.get("values", [])
    if not isinstance(values, list):
        errors.append(f"BAD_TAG_VALUES {path.relative_to(ROOT)}: values must be a list")
        return set()
    return {value for value in values if isinstance(value, str)}


def loot_entry_items(value: Any) -> Iterable[str]:
    if isinstance(value, dict):
        if value.get("type") == "minecraft:item" and isinstance(value.get("name"), str):
            yield value["name"]
        for child in value.values():
            yield from loot_entry_items(child)
    elif isinstance(value, list):
        for child in value:
            yield from loot_entry_items(child)


def check_advancements(registered: set[str], structures: set[str], errors: list[str]) -> None:
    for path in json_files(DATA_ROOT / "advancement"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue

        icon = data.get("display", {}).get("icon", {})
        if isinstance(icon, dict):
            ref = icon.get("id") or icon.get("item")
            if isinstance(ref, str):
                check_item_id(ref, registered, errors, path, "display.icon")

        for ref in walk_strings(data.get("criteria", {})):
            if ref.startswith(f"{MODID}:"):
                if ref in structures:
                    continue
                # Criteria strings can reference many registries. For this
                # lightweight check, validate item/block-like IDs against the
                # combined item/block registry and structure IDs separately.
                if ref not in registered and ref not in structures:
                    errors.append(f"MISSING_CRITERIA_REF {path.relative_to(ROOT)}: {ref}")

        for criterion in data.get("criteria", {}).values():
            player = criterion.get("conditions", {}).get("player", {}) if isinstance(criterion, dict) else {}
            location = player.get("location", {}) if isinstance(player, dict) else {}
            if isinstance(location, dict):
                refs = location.get("structures")
                if isinstance(refs, str) and refs.startswith(f"{MODID}:") and refs not in structures:
                    errors.append(f"MISSING_STRUCTURE {path.relative_to(ROOT)}: {refs}")
                elif isinstance(refs, list):
                    errors.append(f"LEGACY_STRUCTURE_LIST {path.relative_to(ROOT)}: use a string or tag ID")


def check_recipes(registered: set[str], errors: list[str]) -> None:
    for path in json_files(DATA_ROOT / "recipe"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        for ref in walk_strings(data.get("ingredients", [])):
            if ref.startswith(f"{MODID}:"):
                check_item_id(ref, registered, errors, path, "ingredient")
        key = data.get("key", {})
        if isinstance(key, dict):
            for ref in walk_strings(key):
                if ref.startswith(f"{MODID}:"):
                    check_item_id(ref, registered, errors, path, "key")
        result = data.get("result", {})
        if isinstance(result, dict):
            ref = result.get("id")
            if isinstance(ref, str):
                check_item_id(ref, registered, errors, path, "result")


def shaped_recipe_counts(recipe_name: str, errors: list[str]) -> dict[str, int]:
    path = DATA_ROOT / "recipe" / f"{recipe_name}.json"
    data = load_json(path, errors)
    if not isinstance(data, dict):
        return {}
    pattern = data.get("pattern", [])
    key = data.get("key", {})
    if not isinstance(pattern, list) or not isinstance(key, dict):
        errors.append(f"BAD_SHAPED_RECIPE {path.relative_to(ROOT)}")
        return {}

    refs: dict[str, str] = {}
    for symbol, ref in key.items():
        if isinstance(symbol, str) and isinstance(ref, str):
            refs[symbol] = ref
        elif isinstance(symbol, str) and isinstance(ref, dict):
            item = ref.get("item") or ref.get("id")
            if isinstance(item, str):
                refs[symbol] = item

    counts: dict[str, int] = {}
    for row in pattern:
        if not isinstance(row, str):
            continue
        for symbol in row:
            if symbol == " ":
                continue
            ref = refs.get(symbol)
            if ref:
                counts[ref] = counts.get(ref, 0) + 1
    return counts


def expect_recipe_count(
    recipe_name: str,
    counts: dict[str, int],
    item_id: str,
    expected: int,
    errors: list[str],
) -> None:
    actual = counts.get(item_id, 0)
    if actual != expected:
        errors.append(
            f"FIRST_HOUR_RECIPE_COST {recipe_name} expected {expected}x {item_id}, found {actual}"
        )


def check_first_hour_stability_data(errors: list[str]) -> None:
    hand_recycler = shaped_recipe_counts("hand_recycler", errors)
    expect_recipe_count("hand_recycler", hand_recycler, f"{MODID}:machine_casing", 1, errors)
    expect_recipe_count("hand_recycler", hand_recycler, f"{MODID}:scrap_metal", 4, errors)
    expect_recipe_count("hand_recycler", hand_recycler, f"{MODID}:scrap_wire", 4, errors)

    micro_generator = shaped_recipe_counts("micro_generator", errors)
    expect_recipe_count("micro_generator", micro_generator, f"{MODID}:machine_casing", 1, errors)
    expect_recipe_count("micro_generator", micro_generator, f"{MODID}:scrap_wire", 3, errors)

    water_purifier = shaped_recipe_counts("water_purifier", errors)
    expect_recipe_count("water_purifier", water_purifier, f"{MODID}:machine_casing", 3, errors)

    mission_registry = read_source("echo/MissionRegistry.java", errors)
    require_source_tokens(
        "First-hour stable-base reward bridge",
        mission_registry,
        (
            "new ItemStack(ModItems.FILTER_CARTRIDGE_BASIC.get(), 2)",
            "new ItemStack(ModItems.DIRTY_WATER_BOTTLE.get(), 2)",
            "three machine casings",
        ),
        errors,
    )

    echo_guide = read_source("echo/EchoGuideManager.java", errors)
    require_source_tokens(
        "No-Terminal server-authoritative turn-in fallback",
        echo_guide,
        (
            "!isTerminalInstalled()",
            "Completing protocol through server fallback",
            "completeMission(player, quest, currentMission)",
        ),
        errors,
    )

    hud = read_source("client/hud/SurvivalHudOverlay.java", errors)
    welcome = read_source("client/screen/WelcomeScreen.java", errors)
    lang = (ASSET_ROOT / "lang/en_us.json").read_text(encoding="utf-8")
    require_source_tokens(
        "No-Terminal first-hour guidance copy",
        hud + welcome + lang,
        (
            "[N] GUIDE",
            "welcome.callout.guide",
            "GUIDANCE ONLINE",
            "Terminal when installed",
        ),
        errors,
    )


def check_substrate_grinder_data(errors: list[str]) -> None:
    item_tag = ROOT / f"src/main/resources/data/{MODID}/tags/item/substrate_grinder_inputs.json"
    block_tag = ROOT / f"src/main/resources/data/{MODID}/tags/block/grindable_substrates.json"
    item_values = tag_values(item_tag, errors)
    block_values = tag_values(block_tag, errors)
    pickaxe_values = tag_values(ROOT / "src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json", errors)
    shovel_values = tag_values(ROOT / "src/main/resources/data/minecraft/tags/block/mineable/shovel.json", errors)
    mineable_values = pickaxe_values | shovel_values

    for ref in SUBSTRATE_GRINDER_ITEMS:
        if ref not in item_values:
            errors.append(f"MISSING_SUBSTRATE_GRINDER_ITEM_TAG {ref}")
    for ref in SUBSTRATE_GRINDER_BLOCKS:
        if ref not in block_values:
            errors.append(f"MISSING_GRINDABLE_SUBSTRATE_BLOCK_TAG {ref}")
        if ref.startswith(f"{MODID}:") and ref not in mineable_values:
            errors.append(f"MISSING_GRINDABLE_SUBSTRATE_MINEABLE_TAG {ref}")

    for ref in SUBSTRATE_GRINDER_BLOCKS:
        if not ref.startswith(f"{MODID}:"):
            continue
        block_name = ref.split(":", 1)[1]
        loot_path = DATA_ROOT / "loot_table/blocks" / f"{block_name}.json"
        data = load_json(loot_path, errors)
        if not isinstance(data, dict):
            errors.append(f"MISSING_SUBSTRATE_LOOT_TABLE {loot_path.relative_to(ROOT)}")
            continue
        entry_items = set(loot_entry_items(data))
        if ref not in entry_items:
            errors.append(f"SUBSTRATE_BLOCK_DOES_NOT_SELF_DROP {loot_path.relative_to(ROOT)}: {ref}")
        extra_direct_items = sorted(item for item in entry_items if item != ref)
        if extra_direct_items:
            errors.append(
                f"SUBSTRATE_BLOCK_HAS_DIRECT_TRACE_DROPS {loot_path.relative_to(ROOT)}: "
                + ", ".join(extra_direct_items)
            )


def check_worldgen(errors: list[str]) -> None:
    structures = {f"{MODID}:{path.stem}" for path in json_files(DATA_ROOT / "worldgen/structure")}
    placed = {f"{MODID}:{path.stem}" for path in json_files(DATA_ROOT / "worldgen/placed_feature")}
    configured = {f"{MODID}:{path.stem}" for path in json_files(DATA_ROOT / "worldgen/configured_feature")}
    used_placed: set[str] = set()
    used_configured: set[str] = set()

    for path in json_files(DATA_ROOT / "worldgen/biome"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        for ref in walk_strings(data.get("features", [])):
            if ref.startswith(f"{MODID}:"):
                used_placed.add(ref)
                if ref not in placed:
                    errors.append(f"MISSING_PLACED_FEATURE {path.relative_to(ROOT)}: {ref}")

    for path in json_files(DATA_ROOT / "neoforge/biome_modifier"):
        data = load_json(path, errors)
        if not isinstance(data, dict) or data.get("type") != "neoforge:add_features":
            continue
        for ref in walk_strings(data.get("features", [])):
            if ref.startswith(f"{MODID}:"):
                used_placed.add(ref)
                if ref not in placed:
                    errors.append(f"MISSING_BIOME_MODIFIER_PLACED_FEATURE {path.relative_to(ROOT)}: {ref}")

    for path in json_files(DATA_ROOT / "worldgen/placed_feature"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        ref = data.get("feature")
        if isinstance(ref, str) and ref.startswith(f"{MODID}:"):
            used_configured.add(ref)
            if ref not in configured:
                errors.append(f"MISSING_CONFIGURED_FEATURE {path.relative_to(ROOT)}: {ref}")

        def check_matching_block_offsets(value: Any) -> None:
            if isinstance(value, dict):
                if value.get("type") == "minecraft:matching_blocks" and "blocks" in value and "offset" not in value:
                    errors.append(f"MISSING_GROUND_PREDICATE_OFFSET {path.relative_to(ROOT)}")
                for child in value.values():
                    check_matching_block_offsets(child)
            elif isinstance(value, list):
                for child in value:
                    check_matching_block_offsets(child)

        check_matching_block_offsets(data)

    for ref in sorted(placed - used_placed - GENERIC_UNUSED_PLACED_FEATURE_ALLOWLIST):
        errors.append(f"UNREFERENCED_PLACED_FEATURE {ref}")

    for ref in sorted(configured - used_configured):
        errors.append(f"UNREFERENCED_CONFIGURED_FEATURE {ref}")

    noise_settings = load_json(DATA_ROOT / "worldgen/noise_settings/wasteland_overworld.json", errors)
    if isinstance(noise_settings, dict):
        surface_refs = set(walk_strings(noise_settings.get("surface_rule", {})))
        for ref in sorted(WORLDGEN_SURFACE_GRASS_BLOCKS & surface_refs):
            errors.append(f"FULL_SURFACE_GRASS_BLOCK {ref} appears in wasteland_overworld surface rules")

    plant_ground = tag_values(ROOT / "src/main/resources/data/minecraft/tags/block/dirt.json", errors)
    for ref in sorted(REQUIRED_PLANT_GROUND_BLOCKS - plant_ground):
        errors.append(f"MISSING_PLANT_GROUND_TAG data/minecraft/tags/block/dirt.json: {ref}")

    balance_source = JAVA_ROOT / "worldgen/WorldgenBalance.java"
    balance_constants: dict[str, int] = {}
    if balance_source.exists():
        text = balance_source.read_text(encoding="utf-8")
        balance_constants = {
            name: int(value)
            for name, value in re.findall(r"public static final int ([A-Z0-9_]+) = ([0-9]+);", text)
        }
    else:
        errors.append(f"MISSING_WORLDGEN_BALANCE_SOURCE {balance_source.relative_to(ROOT)}")

    for path in json_files(DATA_ROOT / "worldgen/structure_set"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        for structure in data.get("structures", []):
            ref = structure.get("structure") if isinstance(structure, dict) else None
            if isinstance(ref, str) and ref.startswith(f"{MODID}:") and ref not in structures:
                errors.append(f"MISSING_STRUCTURE_SET_TARGET {path.relative_to(ROOT)}: {ref}")
        expected = EXPECTED_STRUCTURE_SET_PLACEMENT.get(path.stem)
        if expected is not None:
            spacing_name, separation_name = expected
            spacing = balance_constants.get(spacing_name)
            separation = balance_constants.get(separation_name)
            placement = data.get("placement", {})
            if spacing is None or separation is None:
                errors.append(f"MISSING_WORLDGEN_BALANCE_CONSTANT {spacing_name}/{separation_name}")
            elif isinstance(placement, dict):
                actual = (placement.get("spacing"), placement.get("separation"))
                if actual != (spacing, separation):
                    errors.append(
                        f"STRUCTURE_SET_BALANCE_DRIFT {path.relative_to(ROOT)}: "
                        f"spacing/separation {actual} expected {(spacing, separation)}"
                    )


def check_starter_only_drop_pod_worldgen(errors: list[str]) -> None:
    poi_tag = tag_values(DATA_ROOT / "tags/worldgen/structure/poi_structures.json", errors)
    if f"{MODID}:drop_pod" in poi_tag:
        errors.append("RANDOM_DROP_POD_STRUCTURE_TAG data/echoashfallprotocol/tags/worldgen/structure/poi_structures.json")

    for path in json_files(DATA_ROOT / "worldgen/structure_set"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        for structure in data.get("structures", []):
            ref = structure.get("structure") if isinstance(structure, dict) else None
            if ref == f"{MODID}:drop_pod":
                errors.append(f"RANDOM_DROP_POD_STRUCTURE_SET {path.relative_to(ROOT)}")

    starter_pool = DATA_ROOT / "worldgen/template_pool/drop_pod/start.json"
    for path in json_files(DATA_ROOT / "worldgen/template_pool"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        for ref in walk_strings(data):
            if not isinstance(ref, str) or not ref.startswith(f"{MODID}:"):
                continue
            if path == starter_pool and ref in {f"{MODID}:drop_pod/start", f"{MODID}:global/drop_pod"}:
                continue
            if any(token in ref for token in FORBIDDEN_NATURAL_DROP_POD_TEMPLATE_TOKENS):
                errors.append(f"RANDOM_DROP_POD_TEMPLATE_POOL {path.relative_to(ROOT)}: {ref}")

    procedural = read_source("event/ProceduralStructureHandler.java", errors)
    forbidden_snippets = (
        "Map.entry(StructureType.DROP_POD",
        "SPAWN_CONFIGS.put(StructureType.DROP_POD",
        "case DROP_POD ->",
    )
    for snippet in forbidden_snippets:
        if snippet in procedural:
            errors.append(f"RANDOM_DROP_POD_PROCEDURAL_HANDLER contains {snippet}")


def check_poi_catalog(errors: list[str]) -> None:
    catalog_source = read_source("world/ExplorationPoiCatalog.java", errors)
    site_source = read_source("world/ExplorationSiteRegistry.java", errors)
    terminal_source = read_source("integration/AshfallTerminalIntegration.java", errors)
    procedural_doc = (ROOT / "PROCEDURAL_STRUCTURES.md").read_text(encoding="utf-8")
    structure_readme = (DATA_ROOT / "structure/README.md").read_text(encoding="utf-8")
    readme = (ROOT / "README.md").read_text(encoding="utf-8")
    getting_started = (ROOT / "GETTING_STARTED.md").read_text(encoding="utf-8")
    overview = (ROOT / "MODPACK_OVERVIEW.md").read_text(encoding="utf-8")

    pool_locations = echo_template_pool_locations(errors) - STARTER_ONLY_TEMPLATE_LOCATIONS
    catalog_entries = re.findall(
        r'entry\(\s*"([^"]+)",\s*"[^"]*",\s*"([^"]+)",\s*"[^"]*",\s*List\.of\(',
        catalog_source,
    )
    catalog_locations = {location for location, _profile in catalog_entries}
    site_ids = set(re.findall(r'register\(site\(\s*\n\s*"([a-z0-9_]+)"', site_source))

    for location in sorted(pool_locations - catalog_locations):
        errors.append(f"MISSING_POI_CATALOG_ENTRY {location}")
    for location in sorted(catalog_locations - pool_locations):
        errors.append(f"STALE_POI_CATALOG_ENTRY {location}")
    for location, profile_id in catalog_entries:
        if profile_id not in site_ids:
            errors.append(f"POI_CATALOG_MISSING_PROFILE {location}: {profile_id}")
        if not structure_template_path(location).exists():
            errors.append(f"POI_CATALOG_MISSING_NBT {location}")

    require_source_tokens(
        "POI Atlas terminal surface",
        catalog_source + terminal_source,
        (
            "class ExplorationPoiCatalog",
            "POI Atlas",
            "renderPoiAtlas",
            "poi_field_atlas",
            "ExplorationPoiCatalog.totalTemplateCount()",
        ),
        errors,
    )
    require_source_tokens(
        "POI Atlas docs coverage",
        procedural_doc + structure_readme + readme + getting_started + overview,
        (
            "POI Atlas",
            "POI Field Atlas",
            "template signals",
            "scanner profile",
        ),
        errors,
    )


def check_item_definitions(registered: set[str], errors: list[str]) -> None:
    item_defs = {f"{MODID}:{path.stem}" for path in (ASSET_ROOT / "items").glob("*.json")}
    missing = sorted(ref for ref in registered if ref not in item_defs)
    for ref in missing:
        # Most registered blocks/items should have item definitions on this NeoForge line.
        errors.append(f"MISSING_ITEM_DEFINITION assets/{MODID}/items/{ref.split(':', 1)[1]}.json")


def check_structure_nbt_palettes(registered_blocks: set[str], errors: list[str]) -> None:
    try:
        import nbtlib
    except ImportError as exc:
        errors.append(f"NBT_VALIDATOR_IMPORT nbtlib is required: {exc}")
        return

    for structure_dir_name in ("structure",):
        structure_dir = DATA_ROOT / structure_dir_name
        if not structure_dir.exists():
            continue

        for path in structure_dir.rglob("*.nbt"):
            try:
                nbt = nbtlib.load(path)
            except Exception as exc:  # noqa: BLE001 - keep validation reporting compact.
                errors.append(f"NBT_PARSE {path.relative_to(ROOT)}: {exc}")
                continue

            palette = nbt.get("palette", [])
            for index, entry in enumerate(palette):
                block_id = str(entry.get("Name", ""))
                namespace = block_id.split(":", 1)[0] if ":" in block_id else ""
                if namespace and namespace not in KNOWN_NAMESPACES:
                    errors.append(
                        f"UNEXPECTED_STRUCTURE_BLOCK_NAMESPACE {path.relative_to(ROOT)} palette[{index}]: {block_id}"
                    )
                elif block_id.startswith(f"{MODID}:") and block_id not in registered_blocks:
                    errors.append(
                        f"MISSING_STRUCTURE_BLOCK {path.relative_to(ROOT)} palette[{index}]: {block_id}"
                    )


def validate_mod_block_asset(block_id: str, errors: list[str]) -> None:
    block_name = block_id.split(":", 1)[1]
    blockstate_path = ASSET_ROOT / "blockstates" / f"{block_name}.json"
    if not blockstate_path.exists():
        errors.append(f"MISSING_POD_BLOCKSTATE assets/{MODID}/blockstates/{block_name}.json")
        return

    blockstate = load_json(blockstate_path, errors)
    if not isinstance(blockstate, dict):
        return

    model_refs = set()
    for ref in walk_strings(blockstate):
        if ref.startswith(f"{MODID}:block/"):
            model_refs.add(ref.split(":", 1)[1].removeprefix("block/"))
        elif ref.startswith("block/"):
            model_refs.add(ref.removeprefix("block/"))

    if not model_refs and (ASSET_ROOT / "models/block" / f"{block_name}.json").exists():
        model_refs.add(block_name)

    seen_models: set[str] = set()

    def validate_model(model_name: str) -> None:
        if model_name in seen_models:
            return
        seen_models.add(model_name)

        model_path = ASSET_ROOT / "models/block" / f"{model_name}.json"
        if not model_path.exists():
            errors.append(f"MISSING_POD_BLOCK_MODEL assets/{MODID}/models/block/{model_name}.json")
            return

        model = load_json(model_path, errors)
        if not isinstance(model, dict):
            return

        parent = model.get("parent")
        if isinstance(parent, str):
            if parent.startswith(f"{MODID}:block/"):
                validate_model(parent.split(":", 1)[1].removeprefix("block/"))
            elif parent.startswith("block/"):
                validate_model(parent.removeprefix("block/"))

        textures = model.get("textures", {})
        if not isinstance(textures, dict):
            return
        for texture in textures.values():
            if not isinstance(texture, str) or texture.startswith("#") or texture.startswith("minecraft:"):
                continue
            texture_path = texture.split(":", 1)[1] if ":" in texture else texture
            texture_path = texture_path.removeprefix("block/")
            if not (ASSET_ROOT / "textures/block" / f"{texture_path}.png").exists():
                errors.append(f"MISSING_POD_BLOCK_TEXTURE assets/{MODID}/textures/block/{texture_path}.png")

    for model_ref in model_refs:
        validate_model(model_ref)


def check_starting_drop_pod_templates(registered_blocks: set[str], errors: list[str]) -> None:
    try:
        import nbtlib
    except ImportError as exc:
        errors.append(f"POD_NBT_VALIDATOR_IMPORT nbtlib is required: {exc}")
        return

    reference_signature = None
    pod_custom_blocks: set[str] = set()

    for path in POD_TEMPLATE_PATHS:
        if not path.exists():
            errors.append(f"MISSING_STARTING_POD_TEMPLATE {path.relative_to(ROOT)}")
            continue

        try:
            nbt = nbtlib.load(path)
        except Exception as exc:  # noqa: BLE001 - report malformed templates.
            errors.append(f"POD_NBT_PARSE {path.relative_to(ROOT)}: {exc}")
            continue

        size = tuple(int(part) for part in nbt.get("size", []))
        if size != POD_EXPECTED_SIZE:
            errors.append(f"BAD_STARTING_POD_SIZE {path.relative_to(ROOT)}: {size}")

        palette = nbt.get("palette", [])
        blocks = nbt.get("blocks", [])
        palette_signature = tuple(nbt_state_key(entry) for entry in palette)
        signature = (size, len(blocks), palette_signature)
        if reference_signature is None:
            reference_signature = signature
        elif signature != reference_signature:
            errors.append(f"UNSYNCED_STARTING_POD_TEMPLATE {path.relative_to(ROOT)}")

        for index, (block_id, properties) in enumerate(palette_signature):
            if block_id.startswith(f"{MODID}:"):
                pod_custom_blocks.add(block_id)
                if block_id not in registered_blocks:
                    errors.append(f"MISSING_POD_BLOCK {path.relative_to(ROOT)} palette[{index}]: {block_id}")
            elif block_id.startswith("minecraft:"):
                schema = POD_VANILLA_STATE_SCHEMAS.get(block_id)
                prop_map = dict(properties)
                if schema is None:
                    errors.append(f"UNREVIEWED_POD_VANILLA_BLOCK {path.relative_to(ROOT)} palette[{index}]: {block_id}")
                    continue
                if set(prop_map) != set(schema):
                    errors.append(
                        f"BAD_POD_BLOCK_PROPERTIES {path.relative_to(ROOT)} palette[{index}]: "
                        f"{block_id} has {sorted(prop_map)} expected {sorted(schema)}"
                    )
                    continue
                for prop_name, prop_value in prop_map.items():
                    if prop_value not in schema[prop_name]:
                        errors.append(
                            f"BAD_POD_BLOCK_PROPERTY_VALUE {path.relative_to(ROOT)} palette[{index}]: "
                            f"{block_id}.{prop_name}={prop_value}"
                        )

        blocks_by_pos = {
            nbt_pos(block["pos"]): palette_signature[int(block["state"])]
            for block in blocks
        }
        for pos, expected in POD_EXPECTED_BED_STATES.items():
            actual = blocks_by_pos.get(pos)
            if actual != expected:
                errors.append(
                    f"BAD_STARTING_POD_BED {path.relative_to(ROOT)} pos={pos}: "
                    f"expected {expected}, found {actual}"
                )

    for block_id in sorted(pod_custom_blocks):
        validate_mod_block_asset(block_id, errors)


def read_source(relative_path: str, errors: list[str]) -> str:
    path = JAVA_ROOT / relative_path
    if not path.exists():
        errors.append(f"MISSING_SOURCE_GUARD {path.relative_to(ROOT)}")
        return ""
    return path.read_text(encoding="utf-8")


def read_core_source(relative_path: str, errors: list[str]) -> str:
    path = CORE_JAVA_ROOT / relative_path
    if not path.exists():
        errors.append(f"MISSING_SOURCE_GUARD {path.relative_to(ROOT)}")
        return ""
    return path.read_text(encoding="utf-8")


def read_orbital_source(relative_path: str, errors: list[str]) -> str:
    path = ORBITAL_JAVA_ROOT / relative_path
    if not path.exists():
        errors.append(f"MISSING_SOURCE_GUARD {path.relative_to(ROOT)}")
        return ""
    return path.read_text(encoding="utf-8")


def read_terminal_source(relative_path: str, errors: list[str]) -> str:
    path = TERMINAL_JAVA_ROOT / relative_path
    if not path.exists():
        errors.append(f"MISSING_SOURCE_GUARD {path.relative_to(ROOT)}")
        return ""
    return path.read_text(encoding="utf-8")


def require_source_tokens(label: str, text: str, tokens: Iterable[str], errors: list[str]) -> None:
    for token in tokens:
        if token not in text:
            errors.append(f"MISSING_SOURCE_TOKEN {label}: {token}")


def check_echo_core_faction_source_guards(errors: list[str]) -> None:
    core_sources = "".join(
        read_core_source(f"api/{name}.java", errors)
        for name in (
            "EchoFactionDefinition",
            "EchoFactionStanding",
            "EchoFactionProfile",
            "EchoFactionContract",
            "EchoFactionContractState",
            "EchoFactionAction",
            "EchoFactionActionResult",
            "EchoFactionInteractionSnapshot",
            "EchoFactionActionHandlerService",
            "EchoDialogueTree",
            "EchoNpcRole",
            "EchoFactionPoiAffinity",
            "EchoFactionRegistry",
            "EchoFactionDataService",
            "EchoCoreServices",
        )
    )
    ashfall_factions = read_source("faction/AshfallBiomeFactions.java", errors)
    ashfall_services = read_source("integration/AshfallCoreServices.java", errors)
    ashfall_interactions = read_source("faction/AshfallFactionInteractionHandler.java", errors)
    ashfall_contracts = read_source("faction/AshfallFactionContracts.java", errors)
    ashfall_contract_progression = read_source("faction/AshfallFactionContractProgression.java", errors)
    ashfall_contract_data = read_source("faction/AshfallFactionContractData.java", errors)
    ashfall_services_runtime = read_source("faction/AshfallFactionServices.java", errors)
    ashfall_npc = read_source("entity/faction/FactionNpcEntity.java", errors)
    ashfall_dialogue = read_source("faction/FactionNpcDialogueService.java", errors)
    ashfall_population = read_source("faction/FactionNpcPopulationHandler.java", errors)
    ashfall_map = read_source("faction/AshfallFactionMap.java", errors)
    ashfall_mod_entities = read_source("entity/ModEntities.java", errors)
    ashfall_client = read_source("EchoAshfallProtocolClient.java", errors)
    faction_screen = read_source("client/screen/FactionDialogueScreen.java", errors)
    faction_open_packet = read_source("network/FactionDialogueOpenPacket.java", errors)
    faction_action_packet = read_source("network/FactionNpcActionPacket.java", errors)
    mod_network = read_source("network/ModNetwork.java", errors)
    migration = read_source("data/SaveMigrationHandler.java", errors)
    orbital_factions = read_orbital_source("integration/OrbitalFactions.java", errors)
    orbital_progress = read_orbital_source("progression/EchoTerminalProgress.java", errors)
    orbital_services = read_orbital_source("integration/AshfallCompat.java", errors)
    terminal_tabs = read_terminal_source("client/BuiltinTerminalTabs.java", errors)

    require_source_tokens(
        "Echo Core faction framework",
        core_sources,
        (
            "record EchoFactionDefinition",
            "enum EchoFactionStanding",
            "record EchoFactionProfile",
            "record EchoFactionContract",
            "record EchoFactionContractState",
            "record EchoFactionAction",
            "record EchoFactionActionResult",
            "record EchoFactionInteractionSnapshot",
            "interface EchoFactionActionHandlerService",
            "record EchoDialogueTree",
            "record EchoNpcRole",
            "record EchoFactionPoiAffinity",
            "class EchoFactionRegistry",
            "class EchoFactionDataService",
            "echocore_factions",
            "registerFaction(EchoFactionDefinition definition)",
            "factionProfiles(Player player)",
            "acceptFactionContract",
            "completeFactionContract",
            "factionContractState",
            "syncFactionDataToClient",
            "registerFactionActionHandler",
            "performFactionAction",
            "recordFactionInteraction",
            "contact_count",
            "last_interaction_tick",
            "last_role_id",
        ),
        errors,
    )

    ashfall_ids = (
        "survivor_network",
        "ashland_rangers",
        "dustline_freeholds",
        "metro_archivists",
        "rustworks_union",
        "sporebound_sanctum",
        "crashbreak_salvage",
        "radwarden_compact",
        "thawbound_collective",
        "scarbound_conclave",
    )
    orbital_ids = ("orbital_remnants", "void_salvagers", "nexus_choir")
    for faction_id in ashfall_ids:
        if f'id("{faction_id}")' not in ashfall_factions and f'"{faction_id}"' not in ashfall_factions:
            errors.append(f"MISSING_ASHFALL_CORE_FACTION {MODID}:{faction_id}")
    if 'path + "_field_contract"' not in ashfall_factions + ashfall_contracts:
        errors.append("MISSING_ASHFALL_FACTION_CONTRACT_FACTORY path + \"_field_contract\"")
    for tier in ("_trusted_contract", "_aligned_contract"):
        if tier not in ashfall_contracts:
            errors.append(f"MISSING_ASHFALL_FACTION_CONTRACT_TIER {tier}")
    for faction_id in orbital_ids:
        if f'id("{faction_id}")' not in orbital_factions and f'"{faction_id}"' not in orbital_factions:
            errors.append(f"MISSING_ORBITAL_CORE_FACTION echoorbitalremnants:{faction_id}")

    require_source_tokens(
        "Ashfall faction registration and clean routing",
        ashfall_factions + ashfall_services + migration + ashfall_map
        + ashfall_contracts + ashfall_contract_progression,
        (
            "AshfallBiomeFactions.register()",
            "registerFactionActionHandler",
            "Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID",
            "CURRENT_MIGRATION_VERSION = 2",
            "class AshfallFactionMap",
            "List<Identifier> all()",
            "boolean isAshfall(Identifier factionId)",
            "Identifier resolveFactionId(String value)",
            "Identifier forPoi(String poiId)",
            "Identifier forEntity(String entityId)",
            "EchoCoreServices.factionDefinition",
        ),
        errors,
    )
    require_source_tokens(
        "Playable Ashfall faction NPC interaction",
        ashfall_interactions + ashfall_npc + ashfall_dialogue + ashfall_population
        + ashfall_contracts + ashfall_contract_progression + ashfall_contract_data + ashfall_services_runtime
        + faction_screen + faction_open_packet + faction_action_packet + mod_network
        + ashfall_mod_entities + ashfall_client,
        (
            "class FactionNpcEntity",
            "DATA_FACTION_ID",
            "DATA_ROLE_ID",
            "FactionNpcDialogueService.open",
            "class FactionDialogueScreen",
            "FactionDialogueOpenPacket",
            "FactionNpcActionPacket",
            "FACTION_NPC",
            "FactionNpcRenderer",
            "FactionNpcPopulationHandler",
            "EchoFactionPoiAffinity",
            "LOCAL_POI_HINT",
            "handleFactionNpcAction",
            "handleFactionDialogueOpen",
            "ACCEPT_FACTION_CONTRACT_ACTION",
            "COMPLETE_FACTION_CONTRACT_ACTION",
            "AshfallFactionContractProgression",
            "AshfallFactionContractData",
            "progressLine",
            "lockedReason",
            "ServiceKind",
        ),
        errors,
    )
    require_source_tokens(
        "Orbital faction mirror",
        orbital_factions + orbital_progress + orbital_services,
        (
            "OrbitalFactions.register()",
            "OrbitalFactions.sync(player, this)",
            "Identifier.fromNamespaceAndPath(EchoOrbitalRemnants.MODID",
            "orbitalRemnantStanding()",
            "voidSalvagerStanding()",
            "nexusChoirStanding()",
            "setFactionReputation",
            "markFactionContacted",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal faction atlas",
        terminal_tabs,
        (
            "FactionAtlasTab",
            "FACTION ATLAS",
            "EchoCoreServices.factionProfiles",
            "filterNamespace",
            "NPC Roles",
            "Service State",
            "Last Contact",
            "contactSummary(profile)",
            "POI Affinity",
        ),
        errors,
    )


def check_top_risk_source_guards(errors: list[str]) -> None:
    nexus_rules = read_source("endgame/NexusAccessRules.java", errors)
    nexus_block = read_source("block/NexusCoreBlock.java", errors)
    nexus_choice = read_source("endgame/NexusChoiceService.java", errors)
    terminal_integration = read_source("integration/AshfallTerminalIntegration.java", errors)
    terminal_common_integration = read_source("integration/AshfallTerminalCommonIntegration.java", errors)
    player_tech_tracker = read_source("survival/PlayerTechTracker.java", errors)
    drone = read_source("entity/EchoCompanionDrone.java", errors)
    combat_ai = read_source("entity/drone/DroneCombatAI.java", errors)
    combat_events = read_source("entity/drone/DroneCombatEventHandler.java", errors)

    require_source_tokens(
        "NexusAccessRules guardian gate",
        nexus_rules,
        (
            "BiomeGuardianProfiles.all()",
            "quest.isMissionCompleted(profile.missionId())",
            "quest.getEntityKills(profile.entityId()) >= 1",
            "missingGuardianCount",
            "activatedNodes",
            "NexusWorldData.get(level.getServer().overworld())",
            "worldResolved",
            "statusText",
            "stateLabel",
        ),
        errors,
    )
    require_source_tokens(
        "Nexus shared gate callers",
        nexus_block + nexus_choice,
        ("NexusAccessRules.evaluate",),
        errors,
    )
    if "NexusAccessRules.evaluate" not in nexus_block:
        errors.append("MISSING_NEXUS_BLOCK_GATE NexusCoreBlock must use NexusAccessRules.evaluate")
    if "NexusAccessRules.evaluate" not in nexus_choice:
        errors.append("MISSING_NEXUS_COMMAND_GATE NexusChoiceService must use NexusAccessRules.evaluate")
    discovery_index = nexus_block.find("be.setDiscovered()")
    gate_index = nexus_block.find("NexusAccessRules.evaluate")
    if discovery_index < 0 or gate_index < 0 or discovery_index > gate_index:
        errors.append("MISSING_NEXUS_DISCOVERY_BEFORE_DENIAL NexusCoreBlock must discover before access denial")
    if 'getDescriptionId().contains("glowing_ghoul")' in nexus_choice:
        errors.append("BRITTLE_NEXUS_GHOUL_TARGETING use ModEntities.GLOWING_GHOUL instead of description id text")
    require_source_tokens(
        "Nexus finale polish",
        nexus_choice,
        (
            "access.worldResolved()",
            "PostNexusEventHandler.syncPlayerToWorldChoice(player)",
            "entity.setOwner(owner)",
            "e.getType() == ModEntities.GLOWING_GHOUL.get()",
        ),
        errors,
    )
    require_source_tokens(
        "Nexus terminal integration",
        terminal_integration + terminal_common_integration,
        (
            "private static final Identifier NEXUS",
            "NEXUS_CHOICE = id(\"nexus_choice\")",
            "TerminalTabRegistry.register(new NexusTab())",
            "TerminalActionRegistry.register(NEXUS, NEXUS_CHOICE",
            "NexusChoiceService.applyChoice(player, payload)",
            "context.sendAction(NEXUS, NEXUS_CHOICE",
        ),
        errors,
    )
    require_source_tokens(
        "Nexus login sync",
        player_tech_tracker,
        (
            "serverLevel.getServer().overworld()",
            "PostNexusEventHandler.syncPlayerToWorldChoice(player)",
            "PostNexusData.syncToClient(player)",
        ),
        errors,
    )

    require_source_tokens(
        "Companion drone mode handlers",
        drone,
        (
            "tickScavengeMode",
            "tickPatrolMode",
            "case PATROL -> intelHandler.tickCombatMode",
            "findNearestDroppedItem",
            "findNearestDebrisBlock",
            "findNearestPatrolThreat",
            "hasMarkedTarget",
        ),
        errors,
    )
    require_source_tokens(
        "Drone mark completion",
        combat_ai + combat_events,
        (
            "MobEffects.GLOWING",
            "markCooldown = MARK_COOLDOWN",
            "LivingDamageEvent.Pre",
            "MARK_DAMAGE_MULTIPLIER = 1.25F",
            "drone.hasMarkedTarget(target)",
        ),
        errors,
    )

    stale_drone_menu_sources = sorted(JAVA_ROOT.rglob("*DroneMenu*.java"))
    for source in stale_drone_menu_sources:
        errors.append(f"STALE_DRONE_MENU_SOURCE {source.relative_to(ROOT)}")

    menu_types = read_source("registry/ModMenuTypes.java", errors)
    if "DRONE_MENU" in menu_types or 'register("drone' in menu_types or "DroneMenu" in menu_types:
        errors.append("STALE_DRONE_MENU_REGISTRATION ModMenuTypes should route drones through ECHO terminal only")


def check_environmental_event_source_guards(errors: list[str]) -> None:
    event_type = read_source("event/EnvironmentalEventType.java", errors)
    profiles = read_source("event/EnvironmentalEventProfiles.java", errors)
    data = read_source("event/EnvironmentalEventData.java", errors)
    status = read_source("event/EnvironmentalEventStatus.java", errors)
    handler = read_source("event/EnvironmentalEventHandler.java", errors)
    command = read_source("event/EnvironmentalEventCommandHandler.java", errors)
    packet = read_source("network/EnvironmentalSyncPacket.java", errors)
    client_network = read_source("client/ClientNetworkHandlers.java", errors)
    hud = read_source("client/hud/HudState.java", errors)
    hud_overlay = read_source("client/hud/SurvivalHudOverlay.java", errors)
    terminal = read_source("integration/AshfallTerminalIntegration.java", errors)
    visuals = read_source("client/EnvironmentalVisualController.java", errors)
    client = read_source("EchoAshfallProtocolClient.java", errors)
    config = read_source("Config.java", errors)
    sounds = read_source("registry/ModSounds.java", errors)
    smart_data = read_source("event/SmartEventData.java", errors)
    orbital_suit = read_orbital_source("suit/SuitEvents.java", errors)
    orbital_network = read_orbital_source("network/ModNetworking.java", errors)
    orbital_payload = read_orbital_source("network/OrbitalEventVisualPayload.java", errors)
    orbital_client = read_orbital_source("EchoOrbitalRemnantsClient.java", errors)

    for source in (
        JAVA_ROOT / "event/RadiationStormEvent.java",
        JAVA_ROOT / "event/EndgameWorldEvents.java",
    ):
        if source.exists():
            errors.append(f"STALE_ENVIRONMENTAL_EVENT_SOURCE {source.relative_to(ROOT)}")

    for event in ("RADIATION_STORM", "TOXIC_STORM", "BLACKOUT", "ASH_STORM", "CRYO_FRONT", "NEXUS_SURGE"):
        if event not in event_type:
            errors.append(f"MISSING_ENVIRONMENTAL_EVENT_ENUM {event}")
        if event not in profiles:
            errors.append(f"MISSING_ENVIRONMENTAL_EVENT_PROFILE {event}")

    require_source_tokens(
        "Environmental event saved state",
        data,
        (
            "eventSurvivalCounts",
            "eventSeed",
            "getEventIntensity",
            "getEventPhase",
            "startEvent(EnvironmentalEventType type, long gameTime, int durationTicks, long seed)",
        ),
        errors,
    )
    require_source_tokens(
        "Environmental event sync payload",
        packet + client_network + hud + handler,
        (
            "float intensity",
            "float phase",
            "long seed",
            "int radiationStormsSurvived",
            "int ashStormsSurvived",
            "packet.intensity()",
            "packet.ashStormsSurvived()",
            "data.getEventIntensity()",
            "data.getEventPhase(gameTime)",
            "getEnvEventSurvivalCount",
            "getEnvironmentalEventStatus",
        ),
        errors,
    )
    require_source_tokens(
        "Environmental event presentation contract",
        status + profiles + data,
        (
            "record EnvironmentalEventStatus",
            "fromData(EnvironmentalEventData data, long gameTime)",
            "fromSynced(String typeName",
            "commandAliases()",
            "counterGuidance",
            "survivalImpact",
            "centerWarningSubtitle",
            "weatherLabel",
        ),
        errors,
    )
    require_source_tokens(
        "Environmental event server behavior",
        handler,
        (
            "selectWeightedEvent",
            "EnvironmentalEventProfiles.activeProfiles()",
            "applyAshStormEffects",
            "applyCryoFrontEffects",
            "applyNexusSurgeEffects",
            "MachineGameplayHelper.addNexusSurge",
            "RadiationHelper.addEnvironmentalRadiation",
            "spawnThreatNear",
        ),
        errors,
    )
    require_source_tokens(
        "Environmental event client visuals",
        visuals + client,
        (
            "WEATHER_VISUAL_INTENSITY",
            "WEATHER_PARTICLE_DENSITY",
            "spawnWeatherParticles",
            "renderOverlay",
            "EnvironmentalVisualController.tick()",
            "EnvironmentalVisualController.renderOverlay",
        ),
        errors,
    )
    require_source_tokens(
        "Environmental event HUD and terminal integration",
        hud_overlay + terminal,
        (
            "currentEventStatus()",
            "eventChipLabel",
            "centerWarningTitle",
            "WEATHER EVENT",
            "Weather Event Protocols",
            "weatherCountsLineOne",
        ),
        errors,
    )
    require_source_tokens(
        "Environmental event QA commands and sounds",
        command + sounds,
        (
            "StringArgumentType.word()",
            "EnvironmentalEventProfiles.commandAliases()",
            "startByAlias",
            "qaSummary",
            "ash_storm",
            "cryo_front",
            "nexus_surge",
            "status",
            "RADIATION_STORM = registerSound(\"event.radiation_storm\")",
            "ASH_STORM = registerSound(\"event.ash_storm\")",
            "CRYO_FRONT = registerSound(\"event.cryo_front\")",
            "NEXUS_SURGE = registerSound(\"event.nexus_surge\")",
            "BLACKOUT = registerSound(\"event.blackout\")",
        ),
        errors,
    )
    require_source_tokens(
        "Environmental event config and score clamps",
        config + smart_data,
        (
            "ENABLE_ASH_STORMS",
            "ENABLE_CRYO_FRONTS",
            "ENABLE_NEXUS_SURGES",
            "ORBITAL_EVENT_VISUALS",
            "clampScore",
            "Math.max(0, Math.min(100, value))",
        ),
        errors,
    )
    require_source_tokens(
        "Orbital event visual bridge",
        orbital_suit + orbital_network + orbital_payload + orbital_client,
        (
            "OrbitalEventVisualPayload",
            "sendOrbitalEventVisual",
            "playToClient(OrbitalEventVisualPayload.TYPE",
            "event.register(OrbitalEventVisualPayload.TYPE",
            "eventVisualTicks",
            "eventVisualName",
            "EnvironmentalVisualController",
            "triggerOrbitalPulse",
            "ORBITAL_EVENT_VISUALS",
        ),
        errors,
    )

    lang_json = ASSET_ROOT / "lang/en_us.json"
    lang_data = load_json(lang_json, errors)
    if isinstance(lang_data, dict):
        for key in (
            "configuration.EchoAshfallProtocol.enableAshStorms",
            "configuration.EchoAshfallProtocol.enableCryoFronts",
            "configuration.EchoAshfallProtocol.enableNexusSurges",
            "configuration.EchoAshfallProtocol.weatherVisualIntensity",
            "configuration.EchoAshfallProtocol.weatherParticleDensity",
            "configuration.EchoAshfallProtocol.orbitalEventVisuals",
        ):
            if key not in lang_data:
                errors.append(f"MISSING_ENVIRONMENTAL_LANG {lang_json.relative_to(ROOT)}: {key}")

    sounds_json = ASSET_ROOT / "sounds.json"
    sounds_data = load_json(sounds_json, errors)
    if isinstance(sounds_data, dict):
        for key in (
            "event.radiation_storm",
            "event.toxic_storm",
            "event.ash_storm",
            "event.cryo_front",
            "event.nexus_surge",
            "event.blackout",
        ):
            if key not in sounds_data:
                errors.append(f"MISSING_ENVIRONMENTAL_SOUND {sounds_json.relative_to(ROOT)}: {key}")


def check_warden_arena_source_guards(errors: list[str]) -> None:
    for rel_path in (
        "dimension/prefall_archives.json",
        "dimension_type/prefall_archives.json",
        "recipe/prefall_archives_key.json",
    ):
        if not (DATA_ROOT / rel_path).exists():
            errors.append(f"MISSING_WARDEN_ARCHIVES_DATA data/{MODID}/{rel_path}")

    arena_service = read_source("endgame/PrefallArchivesArenaService.java", errors)
    warden = read_source("entity/boss/WardenBossEntity.java", errors)
    commands = read_source("event/EchoEndgameCommandHandler.java", errors)
    post_nexus = read_source("event/PostNexusEventHandler.java", errors)
    game_tests = read_source("test/ModGameTests.java", errors)

    require_source_tokens(
        "Pre-Fall Archives arena reliability",
        arena_service,
        (
            "ArenaReport",
            "inspectArena",
            "repairArenaShell",
            "cleanupDuplicateWardens",
            "removeAllWardens",
            "activeFight",
            "resolveReturnTarget",
            "safeSpawnPosition",
            "ensureReturnKeystone",
            "keyStack.shrink(1)",
            "keystoneStack.shrink(1)",
            "post.clearArchivesReturnPoint()",
        ),
        errors,
    )

    require_source_tokens(
        "Warden singleton and defender bounds",
        warden,
        (
            "MAX_DEFENDERS",
            "countActiveDefenders",
            "trimAndLeashDefenders",
            "leashToArena",
            "PrefallArchivesArenaService.clampInsideArena",
            "defender.setTarget(target)",
        ),
        errors,
    )

    require_source_tokens(
        "Endgame QA arena reporting",
        commands,
        (
            "arenaSummary",
            "duplicateWardens",
            "activePlayers",
            "returnPointLabel",
            "arenaReady",
            "cleanupDuplicateWardens",
        ),
        errors,
    )

    require_source_tokens(
        "Warden defeat reward-once credit",
        post_nexus,
        (
            "public static int creditWardenDefeat",
            "ModDimensions.isPrefallArchives(level)",
            "isWardenRewardClaimed",
            "setWardenRewardClaimed(true)",
            "commitProgress(candidate, candidateData)",
            "credited++",
        ),
        errors,
    )

    require_source_tokens(
        "Warden arena GameTest coverage",
        game_tests,
        (
            "WARDEN_ARENA_SERVICE",
            "wardenArenaService",
            "spawnWardenIfMissing",
            "cleanupDuplicateWardens",
            "resetArena",
            "removeAllWardens",
        ),
        errors,
    )


def check_guardian_structure_source_guards(errors: list[str]) -> None:
    generator = read_source("worldgen/ProceduralStructureGenerator.java", errors)
    scanner = read_source("world/POIScannerService.java", errors)
    site_data = read_source("world/BiomeGuardianSiteData.java", errors)
    command = read_source("event/StructureGenCommand.java", errors)
    game_tests = read_source("test/ModGameTests.java", errors)
    profiles = read_source("guardian/BiomeGuardianProfiles.java", errors)
    structures_doc = (ROOT / "PROCEDURAL_STRUCTURES.md").read_text(encoding="utf-8")

    boss_paths = sorted(set(re.findall(r'"([a-z_]+)",\s*\n\s*"[^"]+",\s*\n\s*"([a-z_]+)"', profiles)))
    guardian_boss_paths = sorted({boss_path for _biome, boss_path in boss_paths})
    if len(guardian_boss_paths) != 8:
        errors.append(f"BAD_GUARDIAN_PROFILE_COUNT expected 8 active boss paths, found {len(guardian_boss_paths)}")
    for boss_path in guardian_boss_paths:
        if f'Map.entry("{boss_path}"' not in generator:
            errors.append(f"MISSING_GUARDIAN_THEME {boss_path}")

    require_source_tokens(
        "Biome guardian structure reliability",
        generator,
        (
            "GuardianSiteLayout",
            "ensureGuardianSiteLayout",
            "selectGuardianBossRoom",
            "createDedicatedGuardianBossRoom",
            "GUARDIAN_MIN_BOSS_ROOM_SIZE",
            "repairGuardianBossChamber",
            "carveGuardianLayoutConnector",
            "carveGuardianPathCell",
            "guardianAccessCandidates",
            "prepareGuardianSurfacePad",
            "buildGuardianFallbackRoute",
            "GuardianSiteReport",
            "findExistingBiomeBoss",
            "addOrUpdate(profile, guardianEntrance, existing.get().blockPosition())",
            "hasGuardianSiteTheme",
            "guardianSiteLayoutContractValid",
        ),
        errors,
    )
    require_source_tokens(
        "Biome guardian scanner route",
        scanner + site_data,
        (
            "scanActiveGuardianSite",
            "nearestActiveForMission",
            "dedicated boss chamber",
            "follow the marked route",
            "profile.surfaceEntrance()",
        ),
        errors,
    )
    require_source_tokens(
        "Biome guardian QA command",
        command + structures_doc,
        (
            "guardian_sites",
            "sendGuardianGenerationSummary",
            "listGuardianSites",
            "BiomeGuardianSiteData.get(level).allSites()",
            "Scanner mission",
            "/genpoi guardian_sites [guardian]",
        ),
        errors,
    )
    require_source_tokens(
        "Biome guardian GameTest coverage",
        game_tests,
        (
            "hasGuardianSiteTheme",
            "guardianSiteLayoutContractValid",
            "nearestActiveForMission",
            "Nearby duplicate guardian sites should collapse",
        ),
        errors,
    )


def check_terminal_mission_browser_source_guards(errors: list[str]) -> None:
    browser = read_terminal_source("client/mission/TerminalMissionBrowser.java", errors)
    screen = read_terminal_source("client/screen/EchoTerminalScreen.java", errors)
    theme = read_terminal_source("client/screen/TerminalScreenTheme.java", errors)
    event_handler = read_terminal_source("client/TerminalEventHandler.java", errors)
    ui = read_terminal_source("api/TerminalUi.java", errors)
    visual_assets = read_terminal_source("api/TerminalVisualAssets.java", errors)
    tab_api = read_terminal_source("api/TerminalTab.java", errors)
    tab_chrome = read_terminal_source("api/TerminalTabChrome.java", errors)
    provider = read_terminal_source("api/mission/TerminalMissionProvider.java", errors)
    presentation = read_terminal_source("api/mission/TerminalMissionPresentation.java", errors)
    visuals = read_terminal_source("api/mission/TerminalMissionVisuals.java", errors)
    builtin_tabs = read_terminal_source("client/BuiltinTerminalTabs.java", errors)
    ashfall_terminal = read_source("integration/AshfallTerminalIntegration.java", errors)
    ashfall_client = read_source("EchoAshfallProtocolClient.java", errors)
    orbital_terminal = read_orbital_source("integration/OrbitalTerminalIntegration.java", errors)
    mission_ux = read_source("echo/MissionUxSummary.java", errors)
    hud = read_source("client/hud/SurvivalHudOverlay.java", errors)
    guide = read_source("echo/EchoGuideManager.java", errors)
    require_source_tokens(
        "Terminal tactical tab chrome API",
        tab_api + tab_chrome,
        (
            "record TerminalTabChrome",
            "GROUP_PROTOCOL",
            "GROUP_CORE",
            "GROUP_FIELD",
            "GROUP_SYSTEMS",
            "GROUP_NEXUS",
            "GROUP_ORBITAL",
            "GROUP_ENDGAME",
            "GROUP_ADDONS",
            "default TerminalTabChrome chrome",
            "TerminalTabChrome.fromDescriptor",
            "public static TerminalTabChrome of",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal adaptive grouped navigation",
        screen + theme + ashfall_client
        + read_terminal_source("client/screen/TerminalNavigationModel.java", errors)
        + read_terminal_source("client/screen/TerminalClientOptions.java", errors),
        (
            "LayoutProfile",
            "TerminalNavigationModel",
            "SIDEBAR_HUB",
            "drawSidebarNavigation",
            "drawCompactTopNavigation",
            "handleSidebarNavigationClick",
            "handleCompactNavigationClick",
            "selectGroupOffset",
            "rememberedTabId",
            "OVERVIEW_TAB",
            "GLFW_KEY_PAGE_UP",
            "GLFW_KEY_PAGE_DOWN",
            "tabScroll",
            "chrome().group()",
            "groupLabel",
            "1500",
            "820",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal tactical UI primitives",
        ui,
        (
            "sectionHeader",
            "statusPill",
            "miniStatusPill",
            "tabChip",
            "densePanel",
            "dataCard",
            "denseDataCard",
            "commandStrip",
            "compactCommandStrip",
            "emptyState",
            "callout",
            "imagePanel",
            "imageHero",
            "questArtCard",
            "metricRow",
            "denseMetricRow",
            "disabledReasonRow",
            "sidebarGroupChip",
            "sidebarTabChip",
            "pageHeader",
            "shortcutCard",
            "missionLaneHeader",
            "stickyActionBar",
            "compactButton",
            "trim(Font font",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal AAA visual asset kit",
        visual_assets + ui + screen + browser + builtin_tabs + ashfall_terminal + orbital_terminal,
        (
            "class TerminalVisualAssets",
            "TERMINAL_FRAME_BACKDROP",
            "OVERVIEW_PROTOCOL_DASHBOARD",
            "MISSIONS_VISUAL_HERO",
            "MISSION_SURVIVAL",
            "MISSION_CRAFTING",
            "MISSION_TECH",
            "MISSION_EXPLORATION",
            "MISSION_COMBAT",
            "MISSION_STORY",
            "MISSION_SIDE_OPS",
            "STATUS_HAZARD_SCAN",
            "DRONE_COMMAND_LINK",
            "ARCHIVES_DOSSIER_WALL",
            "CODEX_FIELD_MANUAL",
            "WORLD_ROUTE_MAP",
            "NEXUS_CORE_INTERFACE",
            "ORBITAL_ROUTE_TELEMETRY",
            "ADDONS_MODULE_GRID",
            "missionCategoryArt",
            "TerminalUi.imagePanel",
            "TerminalUi.imageHero",
            "TerminalUi.questArtCard",
        ),
        errors,
    )
    require_source_tokens(
        "Ashfall and Orbital terminal chrome alignment",
        ashfall_terminal + orbital_terminal,
        (
            "TerminalTabChrome.of",
            "GROUP_PROTOCOL",
            "GROUP_FIELD",
            "GROUP_SYSTEMS",
            "GROUP_NEXUS",
            "GROUP_ORBITAL",
            "Active protocol dashboard",
            "ECHO-7 route objectives",
            "Optional recon signals",
            "Routes, POIs, signal map",
            "Recovered field records",
            "Intel and recipes index",
            "Systems and hazard scan",
            "Drone command channel",
            "Final path control",
            "ECHO-0 route telemetry",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal mission readability polish",
        browser,
        (
            "TerminalMissionPresentation",
            "provider.presentation",
            "drawBriefingHeader",
            "drawNextStepCallout",
            "compactStatusLabel",
            "drawStickyActions",
            "firstDisabledReason",
            "MissionFilter",
            "drawFilterChips",
            "pendingTreeFocus",
            "focusTreeOnSelection",
            "syncDetailScrollWithSelection",
            "MissionRenderState",
            "buildState",
            "TerminalMissionRole",
            "GUIDED",
            "SIGNAL LEADS",
            "commandSummary",
            "tagLine",
            "RELATED INTEL",
            "MISSION_ROW_HEIGHT = 30",
            "PHASE_ROW_HEIGHT = 22",
            "w >= 820",
            "graphics.outline(rowX, y, rowW",
            "NEXT STEP",
            "REQUIREMENTS",
            "FIELD GUIDE",
            "\"EXPAND\"",
            "\"COMPACT\"",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal mission input API and frame cache",
        browser + screen + tab_api + event_handler + builtin_tabs + ashfall_terminal,
        (
            "ScreenEvent.CharacterTyped.Pre",
            "handleCharTyped",
            "default boolean charTyped",
            "public boolean keyPressed(TerminalRenderContext context, KeyEvent event)",
            "public boolean charTyped(TerminalRenderContext context, CharacterEvent event)",
            "TerminalRenderCache.current().frameId()",
            "cachedStateFrame",
            "cachedState",
            "VISUAL_RPG(\"VISUAL\")",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal additive mission presentation API",
        provider + presentation,
        (
            "default TerminalMissionPresentation presentation",
            "TerminalMissionPresentation.fallback",
            "shortTitle",
            "objectiveSummary",
            "nextStep",
            "routeHint",
            "statusTone",
            "relatedIntelKey",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal additive mission visual API",
        provider + visuals + browser,
        (
            "default TerminalMissionVisuals visuals",
            "TerminalMissionVisuals.fallback",
            "categoryArt",
            "trackType",
            "heroVariant",
            "visualTone",
            "provider.visuals",
            "VISUAL_RPG",
            "MINIMAL_FUTURE",
            "drawMissionModeChips",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal client options and render cache",
        read_terminal_source("client/screen/TerminalClientOptions.java", errors)
        + read_terminal_source("api/TerminalRenderCache.java", errors)
        + screen
        + ui,
        (
            "navigationStyle = NavigationStyle.APP_HUB",
            "missionView = MissionView.VISUAL_QUEST_HUB",
            "visualLevel = VisualLevel.BALANCED",
            "reducedMotion = false",
            "TerminalRenderCache.beginFrame",
            "TerminalRenderCache.current",
            "frameId",
            "wrappedHeightCache",
            "trimCache",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal additive mission role API",
        provider + read_terminal_source("api/mission/TerminalMissionRole.java", errors) + browser + builtin_tabs + ashfall_terminal,
        (
            "enum TerminalMissionRole",
            "default TerminalMissionRole role",
            "TerminalMissionRole.fallback",
            "MAIN",
            "OPTIONAL",
            "REFERENCE",
            "Optional missions are nonblocking",
            "provider.role",
        ),
        errors,
    )
    require_source_tokens(
        "Ashfall shared mission UX summary",
        ashfall_terminal + mission_ux,
        (
            "MissionUxSummary.current",
            "MissionUxSummary.of",
            "summaryColor",
            "TerminalMissionPresentation",
            "summary.nextStep",
            "summary.relatedIntelKey",
            "MissionUxSummary.unlockReason",
            "MissionUxSummary.turnInReason",
        ),
        errors,
    )
    require_source_tokens(
        "Mission HUD and guide copy integration",
        hud + guide,
        (
            "MissionUxSummary.current",
            "MissionUxSummary.of",
            "summary.shortTitle",
            "summary.nextStep",
        ),
        errors,
    )
    require_source_tokens(
        "Terminal shared mission UI helpers",
        ui,
        (
            "filterChip",
            "selected ? 0xFF123241",
            "graphics.centeredText",
        ),
        errors,
    )
    if "private static final class MissionsTab" in ashfall_terminal:
        errors.append("STALE_MISSIONS_TAB_SOURCE active mission UX must stay in TerminalMissionBrowser")
    if "Checklist remains authoritative" in browser + ashfall_terminal:
        errors.append("STALE_MISSION_COPY debug-style checklist copy returned")
    if 'VISUAL_RPG("QUEST")' in browser:
        errors.append("STALE_MISSION_VIEW_LABEL visual mode should not use vague QUEST label")
    if "DAY 3" in screen or "14:32" in screen:
        errors.append("STALE_TERMINAL_FAKE_TIME_METADATA terminal shell should not render fixed fake day/time")
    if "close and reopen after the next server sync" in ashfall_terminal:
        errors.append("STALE_TERMINAL_DEBUG_SYNC_COPY status copy should be player-facing")
    if "private int tabWidth" in screen or "drawTabs(" in screen:
        errors.append("STALE_TERMINAL_SINGLE_ROW_TABS shared shell should use grouped navigation")
    if "drawGroupChips" in screen or "drawTabChips" in screen:
        errors.append("STALE_TERMINAL_TOP_TWO_ROW_NAV shared shell should use sidebar hub or compact carousel")
    require_source_tokens(
        "Terminal dense operator polish",
        screen + browser + ui + builtin_tabs + ashfall_terminal + orbital_terminal,
        (
            "sidebarNavigation",
            "footerTop - contentY - 8",
            "TerminalUi.densePanel",
            "TerminalUi.denseDataCard",
            "TerminalUi.compactButton",
            "TerminalUi.compactCommandStrip",
            "TerminalUi.miniStatusPill",
            "MODE LOCKS",
            "PATH BRIEF",
            "SUIT TELEMETRY",
            "FACTION CONTRACT",
        ),
        errors,
    )


def check_lore_cohesion_source_guards(errors: list[str]) -> None:
    lore_bible = ROOT / "LORE_BIBLE.md"
    if not lore_bible.exists():
        errors.append("MISSING_LORE_BIBLE LORE_BIBLE.md")
        lore_text = ""
    else:
        lore_text = lore_bible.read_text(encoding="utf-8")
    require_source_tokens(
        "ECHO lore canon guide",
        lore_text,
        (
            "Gridfall",
            "ECHO-7",
            "ECHO-0",
            "Nexus Core",
            "Orbital Remnants",
            "Signal Leads",
            "tactical eerie",
        ),
        errors,
    )

    mission_registry = read_source("echo/MissionRegistry.java", errors)
    mission_guides = read_source("echo/MissionGuideRegistry.java", errors)
    mission_ids = set(re.findall(r'new Mission\(\s*"([a-z0-9_]+)"', mission_registry))
    guide_ids = set(re.findall(r'Map\.entry\("([a-z0-9_]+)"', mission_guides))
    if not mission_ids:
        errors.append("NO_MISSION_IDS_FOR_GUIDE_COVERAGE")
    for mission_id in sorted(mission_ids - guide_ids):
        errors.append(f"MISSING_MISSION_GUIDE {mission_id}")

    ashfall_terminal = read_source("integration/AshfallTerminalIntegration.java", errors)
    builtin_terminal = read_terminal_source("client/BuiltinTerminalTabs.java", errors)
    require_source_tokens(
        "Ashfall lore signal leads",
        ashfall_terminal,
        (
            "AshfallSideOpsProvider",
            "ashfall_side_ops",
            "SIGNAL LEADS",
            "Wasteland Region Field Notes",
            "Faction Signal Threads",
            "Optional lore, recon, and world-context objectives",
        ),
        errors,
    )
    if "Terminal API" in builtin_terminal:
        errors.append("PLAYER_FACING_TERMINAL_API_COPY BuiltinTerminalTabs.java")

    for rel_path in (
        "addons/echoorbitalremnants/README.md",
        "addons/echoorbitalremnants/guide.md",
    ):
        path = ROOT / rel_path
        if not path.exists():
            errors.append(f"MISSING_ORBITAL_LORE_DOC {rel_path}")
            continue
        text = path.read_text(encoding="utf-8")
        if "direct required dependency on `echoashfallprotocol`" in text:
            errors.append(f"STALE_ORBITAL_DEPENDENCY_COPY {rel_path}")
        if "now runs as an addon chapter for `echoashfallprotocol`" in text:
            errors.append(f"STALE_ORBITAL_COMPAT_COPY {rel_path}")


def check_clean_ashfall_faction_rebuild_guards(errors: list[str]) -> None:
    """Keep the old 3-faction Ashfall runtime from leaking back in."""

    scan_roots = (
        JAVA_ROOT,
        DATA_ROOT,
        ASSET_ROOT,
        ROOT / "tools",
        ROOT / "README.md",
    )
    suffixes = {".java", ".json", ".md", ".py", ".toml", ".properties", ".txt"}
    allow_path_parts = (
        "tools/validate_gameplay_data.py",
    )
    line_allow_tokens = (
        "Orbital Remnants",
        "Orbital Remnant",
        "Void Salvager",
        "echoorbitalremnants",
        "orbital_remnant",
        "void_salvager",
    )
    legacy_patterns = (
        r"ReputationData\.Faction",
        r"\bReputationData\b",
        r"\bLegacyFactionIds\b",
        r"\bLegacyReputationData\b",
        r"\bLegacyFactionQuestData\b",
        r"\bLEGACY_REMNANT_SOLDIER\b",
        r"\bLEGACY_SALVAGER_TRADER\b",
        r"\bLEGACY_MUTANT_CREATURE\b",
        r"\bFactionQuest(?:Data|Progression|Registry)?\b",
        r"\bVillagerQuestHandler\b",
        r"\bAshfallFactionBridge\b",
        r"\bcontact_remnants\b",
        r"\bcontact_salvagers\b",
        r"\bcontact_mutants\b",
        r"\bearn_remnant_trust\b",
        r"\bmake_salvager_trade\b",
        r"\brecover_mutant_sample\b",
        r"\bremnant_soldier\b",
        r"\bsalvager_trader\b",
        r"\bmutant_creature\b",
        r"\breputation_data\b",
        r"\bfaction_quest_data\b",
        r"\bremnant_village\b",
        r"\bsalvager_village\b",
        r"\bmutant_village\b",
        r"\bremnant_outpost\b",
        r"\bsalvager_trading_post\b",
        r"\bmutant_sanctuary\b",
        r"\bRemnants\b",
        r"\bSalvagers\b",
        r"\bRemnant\b",
        r"\bSalvager\b",
        r"\bMutants\b",
        r"\bMutant Sanctuary\b",
        r"\bContact Mutants\b",
        r"\b3 Factions\b",
    )
    compiled = [re.compile(pattern) for pattern in legacy_patterns]

    def allowed(path: Path, line: str) -> bool:
        rel_path = path.relative_to(ROOT).as_posix()
        if any(part in rel_path for part in allow_path_parts):
            return True
        return any(token in line for token in line_allow_tokens)

    for root in scan_roots:
        if not root.exists():
            continue
        files = [root] if root.is_file() else root.rglob("*")
        for path in files:
            if not path.is_file() or path.suffix not in suffixes:
                continue
            try:
                lines = path.read_text(encoding="utf-8").splitlines()
            except UnicodeDecodeError:
                continue
            for line_number, line in enumerate(lines, start=1):
                if allowed(path, line):
                    continue
                for pattern in compiled:
                    if pattern.search(line):
                        errors.append(
                            f"STALE_ASHFALL_3_FACTION_REFERENCE {path.relative_to(ROOT)}:{line_number}: "
                            f"{pattern.pattern}"
                        )
                        break


def main() -> int:
    errors: list[str] = []
    registered = collect_registered_ids()
    registered_blocks = collect_registered_block_ids()
    structures = {f"{MODID}:{path.stem}" for path in json_files(DATA_ROOT / "worldgen/structure")}

    if not registered:
        errors.append(f"NO_REGISTERED_IDS found under {JAVA_ROOT.relative_to(ROOT)}")
    if not registered_blocks:
        errors.append(f"NO_REGISTERED_BLOCK_IDS found under {JAVA_ROOT.relative_to(ROOT)}")

    check_advancements(registered, structures, errors)
    check_recipes(registered, errors)
    check_first_hour_stability_data(errors)
    check_substrate_grinder_data(errors)
    check_worldgen(errors)
    check_starter_only_drop_pod_worldgen(errors)
    check_poi_catalog(errors)
    check_item_definitions(registered, errors)
    check_structure_nbt_palettes(registered_blocks, errors)
    check_starting_drop_pod_templates(registered_blocks, errors)
    check_echo_core_faction_source_guards(errors)
    check_top_risk_source_guards(errors)
    check_environmental_event_source_guards(errors)
    check_warden_arena_source_guards(errors)
    check_guardian_structure_source_guards(errors)
    check_terminal_mission_browser_source_guards(errors)
    check_lore_cohesion_source_guards(errors)
    check_clean_ashfall_faction_rebuild_guards(errors)

    if errors:
        print("Gameplay data validation failed:")
        for error in errors:
            print(f"  - {error}")
        return 1

    print(
        f"Gameplay data validation passed ({len(registered)} registered item/block ids, "
        f"{len(registered_blocks)} structure palette block ids checked)."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
