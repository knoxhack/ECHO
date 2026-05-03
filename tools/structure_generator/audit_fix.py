#!/usr/bin/env python3
"""
POI Structure Audit & Fix Script

Scans template pools for missing NBT files and auto-generates them.
Also validates existing NBT files for correctness.

Usage:
    python tools/structure_generator/audit_fix.py
"""

import json
import hashlib
import sys
from pathlib import Path
from typing import Iterable

sys.path.insert(0, str(Path(__file__).parent))

from nbt_writer import write_structure_nbt
from shapes import SHAPE_REGISTRY

ALIAS_PATHS = {
    "bio_lab": ["bio_lab"],
    "data_center_ruin": ["data_center_ruin"],
    "drop_pod": ["drop_pod"],
    "military_vault": ["military_vault"],
    "reactor_ruin": ["reactor_ruin"],
}


def get_structure_dirs(base_path: Path) -> list[Path]:
    return [
        base_path / "structure",
        base_path / "structures",
    ]


def get_template_pool_refs(base_path: Path) -> set:
    """Extract all structure references from template pool JSONs."""
    pool_dir = base_path / "worldgen" / "template_pool"
    refs = set()
    
    for f in pool_dir.rglob("*.json"):
        try:
            data = json.loads(f.read_text())
            for elem in data.get("elements", []):
                loc = elem.get("element", {}).get("location", "")
                if loc.startswith("echoashfallprotocol:"):
                    path = loc.replace("echoashfallprotocol:", "")
                    refs.add((path, f.name))
        except Exception as e:
            print(f"  WARN: Failed to parse {f}: {e}")
    
    return refs


def get_existing_nbt(struct_dir: Path) -> set:
    """Get all existing NBT files."""
    existing = set()
    for f in struct_dir.rglob("*.nbt"):
        rel = f.relative_to(struct_dir).with_suffix("")
        existing.add(str(rel).replace("\\", "/"))
    return existing


def validate_nbt(nbt_path: Path) -> tuple:
    """Validate an NBT file, returns (is_valid, error_message)."""
    try:
        import nbtlib
        nbt = nbtlib.load(str(nbt_path))
        
        # Check required tags
        required = ["DataVersion", "size", "palette", "blocks", "entities"]
        for tag in required:
            if tag not in nbt:
                return False, f"Missing required tag: {tag}"
        
        # Check DataVersion
        if nbt["DataVersion"] != 4189:
            return False, f"Wrong DataVersion: {nbt['DataVersion']} (expected 4189)"

        # Modern structure templates store size/pos as int lists, not int arrays.
        if len(nbt["size"]) != 3:
            return False, f"Invalid size length: {len(nbt['size'])}"
        
        # Check palette indices
        palette_len = len(nbt["palette"])
        for block in nbt["blocks"]:
            if len(block["pos"]) != 3:
                return False, f"Invalid block pos length: {len(block['pos'])}"
            if block["state"] >= palette_len:
                return False, f"Invalid state index: {block['state']} >= {palette_len}"
        
        return True, f"OK ({len(nbt['blocks'])} blocks, {palette_len} palette entries)"
    except Exception as e:
        return False, f"Load error: {e}"


def iter_output_files(output_roots: Iterable[Path], category: str, struct_name: str) -> Iterable[Path]:
    for base_path in output_roots:
        if category == "global":
            yield base_path / "global" / f"{struct_name}.nbt"
        else:
            yield base_path / "biomes" / category / f"{struct_name}.nbt"
        for alias_path in ALIAS_PATHS.get(struct_name, []):
            yield base_path / f"{alias_path}.nbt"


def generate_missing(struct_name: str, category: str, output_roots: Iterable[Path]) -> bool:
    """Generate a missing structure NBT file."""
    if struct_name not in SHAPE_REGISTRY:
        return False

    seed = int.from_bytes(hashlib.sha256(struct_name.encode("utf-8")).digest()[:4], "big") & 0x7FFFFFFF
    blocks = SHAPE_REGISTRY[struct_name](seed)

    if not blocks:
        return False

    for output_file in iter_output_files(output_roots, category, struct_name):
        write_structure_nbt(blocks, output_file)
    return True


def main() -> int:
    print("=" * 60)
    print("  POI Structure Audit & Fix")
    print("=" * 60)
    
    script_dir = Path(__file__).parent.resolve()
    project_root = script_dir.parent.parent.resolve()
    base_path = project_root / "src" / "main" / "resources" / "data" / "echoashfallprotocol"
    structure_dirs = get_structure_dirs(base_path)
    runtime_struct_dir = structure_dirs[0]
    
    # Get references and existing files
    print("\n[1/3] Scanning template pools...")
    refs = get_template_pool_refs(base_path)
    print(f"      Found {len(refs)} structure references")

    print("\n[2/3] Checking existing NBT files...")
    existing = get_existing_nbt(runtime_struct_dir)
    print(f"      Found {len(existing)} runtime NBT files")
    
    # Find missing
    ref_paths = {r[0] for r in refs}
    missing_paths = ref_paths - existing
    
    # Map paths to categories
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
        "scavenger_camp": "ruined_plains",
        "debris_field_small": "global",
        "debris_field_large": "global",
        "survivor_cache": "global",
        "radio_relay_small": "global",
        "abandoned_camp": "global",
        "road_wreck": "global",
        "bio_lab": "toxic_swamp",
        "military_vault": "ruined_cityscape",
        "data_center_ruin": "ruined_cityscape",
        "reactor_ruin": "radiation_zone",
        "drop_pod": "global",
        "industrial_factory": "industrial_ruins",
        "subway_station": "ruined_cityscape",
    }
    
    # Report missing
    print(f"\n[3/3] Coverage analysis:")
    print(f"      Missing structures: {len(missing_paths)}")
    
    generated = 0
    failed = 0
    
    if missing_paths:
        print("\n      Generating missing structures...")
        for path in sorted(missing_paths):
            struct_name = Path(path).name
            # Infer category from path
            parts = path.split("/")
            if len(parts) >= 2 and parts[0] == "biomes":
                category = parts[1]
            else:
                category = "global"
            
            # Override with known mapping if available
            category = CATEGORY_MAP.get(struct_name, category)
            
            if struct_name in SHAPE_REGISTRY:
                if generate_missing(struct_name, category, structure_dirs):
                    print(f"        [GEN] {path}")
                    generated += 1
                else:
                    print(f"        [FAIL] {path} - generator returned empty")
                    failed += 1
            else:
                print(f"        [SKIP] {path} - no generator registered")
                failed += 1
    
    # Validate all NBT files
    print("\n[4/3] Validating NBT files...")
    valid_count = 0
    error_count = 0
    
    for f in sorted(runtime_struct_dir.rglob("*.nbt")):
        rel = f.relative_to(runtime_struct_dir)
        is_valid, msg = validate_nbt(f)
        status = "[PASS]" if is_valid else "[FAIL]"
        print(f"        {status} {rel}: {msg}")
        if is_valid:
            valid_count += 1
        else:
            error_count += 1
    
    # Summary
    print(f"\n{'=' * 60}")
    print("  SUMMARY:")
    print(f"    Template pool references: {len(refs)}")
    print(f"    NBT files generated: {generated}")
    print(f"    NBT files valid: {valid_count}")
    if error_count:
        print(f"    Validation errors: {error_count}")
    if failed:
        print(f"    Generation failures: {failed}")
    
    total_nbt = len(list(runtime_struct_dir.rglob("*.nbt")))
    missing_after = len(missing_paths) - generated
    
    if missing_after <= 0 and error_count == 0:
        print(f"\n    Status: ALL OK - All structures present and valid!")
        return 0
    else:
        print(f"\n    Status: ISSUES FOUND - {missing_after} missing, {error_count} invalid")
        return 1


if __name__ == "__main__":
    sys.exit(main())
