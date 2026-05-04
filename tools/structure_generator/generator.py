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
from detail_pass import apply_detail_pass


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
    "remnant_outpost/command_bunker": "faction",
    "remnant_outpost/barracks": "faction",
    "remnant_outpost/armory": "faction",
    "remnant_outpost/guard_post": "faction",
    "remnant_outpost/supply_depot": "faction",
    "remnant_outpost/street_straight": "faction",
    "remnant_outpost/street_corner": "faction",
    "remnant_outpost/street_cross": "faction",
    "remnant_outpost/wall_section": "faction",
    "remnant_outpost/wall_corner": "faction",
    "salvager_post/market_plaza": "faction",
    "salvager_post/warehouse": "faction",
    "mutant_sanctuary/biodome_hub": "faction",
    "mutant_sanctuary/processing_hut": "faction",
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
    "salvager_worksite": "crash_zone_wasteland",
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


def validate_visual_nbt(
    path: Path,
    nbt,
    errors: list[str],
    warnings: list[str],
    category: str | None = None,
    name: str | None = None,
) -> None:
    palette = _palette_names(nbt)
    blocks = nbt["blocks"]
    names: list[str] = []
    bottom: list[tuple[int, int, str]] = []
    for block in blocks:
        state = int(block["state"])
        block_name = palette[state] if 0 <= state < len(palette) else "<bad-state>"
        names.append(block_name)
        pos = [int(v) for v in block["pos"]]
        if len(pos) == 3 and pos[1] == 0:
            bottom.append((pos[0], pos[2], block_name))

    forbidden = sorted(set(names) & FORBIDDEN_GENERATED_BLOCKS)
    if forbidden:
        errors.append(f"VISUAL_FORBIDDEN_BLOCK {path}: {', '.join(forbidden)}")

    if name not in CURATED_TEMPLATE_NAMES:
        unique_blocks = set(names)
        if len(names) < 24:
            errors.append(f"VISUAL_UNDER_DETAILED {path}: only {len(names)} blocks")
        if len(names) >= 64 and len(unique_blocks) < 4:
            errors.append(f"VISUAL_LOW_VARIETY {path}: only {len(unique_blocks)} block types")

        signature_blocks = CATEGORY_SIGNATURE_BLOCKS.get(category or "")
        if signature_blocks:
            signature_count = sum(1 for block_name in names if block_name in signature_blocks)
            required = 1 if category == "global" else 2
            if signature_count < required:
                errors.append(
                    f"VISUAL_LOW_SIGNATURE {path}: {signature_count}/{required} "
                    f"{category} signature blocks"
                )

    is_cryo = "cryogenic" in str(path).replace("\\", "/")
    if not is_cryo:
        bright_count = sum(1 for name in names if name in VISUAL_PLACEHOLDER_BLOCKS)
        if bright_count > max(12, len(names) // 12):
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

    validate_visual_nbt(path, nbt, errors, warnings, category, name)


def check_outputs(project_root: Path, selected_names: list[str], output_roots: list[Path]) -> int:
    errors: list[str] = []
    warnings: list[str] = []
    refs = get_template_pool_refs(project_root)
    selected_set = set(selected_names)

    for root in output_roots:
        for ref in sorted(refs):
            path = root / f"{ref}.nbt"
            if not path.exists():
                errors.append(f"MISSING_TEMPLATE {path}")

    for name in selected_names:
        category = CATEGORY_MAP[name]
        for path in iter_output_files(output_roots, category, name):
            if path.exists():
                validate_nbt_file(path, errors, warnings, name in LANDMARK_NAMES, category, name)
            else:
                errors.append(f"MISSING_GENERATED {path}")

    print(f"Checked {len(refs)} template-pool refs and {len(selected_set)} selected generators")
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
        blocks = generator(seed)
        if name != "drop_pod":
            blocks = apply_theme_polish(name, category, blocks, seed)

        # Determine structure size for detail pass
        size = "small"
        if any(x in name for x in ["_large", "_facility", "_complex", "_farm", "_yard", "_outpost", "_settlement", "_encampment"]):
            size = "big"
        elif any(x in name for x in ["_medium", "_homestead", "_worksite", "_field", "_station", "_post"]):
            size = "medium"

        # Apply detail pass for enhanced structures (skip drop_pod)
        if name != "drop_pod":
            blocks = apply_detail_pass(blocks, category, category, seed, structure_size=size, name=name)

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

    mirrored = sync_referenced_templates(project_root, output_roots)

    print(f"\n{'=' * 50}")
    print(f"  Templates: {generated}  |  Files written: {files_written}  |  Mirrored: {mirrored}  |  Skipped: {skipped}")
    print(f"{'=' * 50}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
