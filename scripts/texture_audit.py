#!/usr/bin/env python3
"""Texture audit for ECHO texture assets.

Validates resource references, dimensions, alpha expectations, and palette
limits for the deterministic pixel-art texture pass.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

from PIL import Image


BASE = Path(__file__).resolve().parents[1]
ASSETS = BASE / "src/main/resources/assets/echoashfallprotocol"
TEXTURES = ASSETS / "textures"
JAVA = BASE / "src/main/java/com/knoxhack/echoashfallprotocol"
MODID = "echoashfallprotocol"
ORBITAL_ASSETS = BASE / "addons/echoorbitalremnants/src/main/resources/assets/echoorbitalremnants"
ORBITAL_TEXTURES = ORBITAL_ASSETS / "textures"
ORBITAL_MODID = "echoorbitalremnants"
EXTRA_ASSET_ROOTS = (
    ("echoagriculturereclamation", BASE / "addons/echoagriculturereclamation/src/main/resources/assets/echoagriculturereclamation"),
    ("echoblackboxprotocol", BASE / "addons/echoblackboxprotocol/src/main/resources/assets/echoblackboxprotocol"),
    ("echoconvoyprotocol", BASE / "addons/echoconvoyprotocol/src/main/resources/assets/echoconvoyprotocol"),
    ("echoindustrialnexus", BASE / "addons/echoindustrialnexus/src/main/resources/assets/echoindustrialnexus"),
    ("echologisticsnetwork", BASE / "addons/echologisticsnetwork/src/main/resources/assets/echologisticsnetwork"),
    ("echonexusprotocol", BASE / "addons/echonexusprotocol/src/main/resources/assets/echonexusprotocol"),
    ("signalos", BASE / "addons/echosignalos/src/main/resources/assets/signalos"),
    ("echostationfall", BASE / "addons/echostationfall/src/main/resources/assets/echostationfall"),
    ("echoterminal", BASE / "addons/echoterminal/src/main/resources/assets/echoterminal"),
)


PLANT_WORDS = (
    "grass",
    "fern",
    "bush",
    "sapling",
    "reed",
    "wheat",
    "cactus",
    "leaves",
    "fungus",
    "crop",
    "orchid",
    "beans",
    "aloe",
    "berry",
    "berries",
)

MACHINE_WORDS = (
    "machine",
    "generator",
    "scrubber",
    "condenser",
    "terminal",
    "controller",
    "purifier",
    "refiner",
    "grinder",
    "press",
    "scanner",
    "relay",
    "battery",
    "pipe",
    "cable",
    "array",
    "burner",
    "workbench",
    "med_bay",
    "bank",
    "station",
    "node",
    "collector",
    "garden",
    "crate",
    "table",
    "rack",
    "lab",
    "hopper",
    "synthesizer",
    "miner",
    "recycler",
    "counter",
    "casing",
    "core",
    "barrel",
    "bench",
    "furnace",
    "med",
    "workshop",
)


def rel(path: Path) -> str:
    return path.relative_to(BASE).as_posix()


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="ignore")


def referenced_textures_for(assets: Path, modid: str) -> tuple[set[str], set[str]]:
    item_refs: set[str] = set()
    block_refs: set[str] = set()
    for model in (assets / "models").rglob("*.json"):
        try:
            data = json.loads(read_text(model))
        except json.JSONDecodeError:
            continue
        texture_values: list[str] = []

        def collect_textures(value: object) -> None:
            if isinstance(value, dict):
                textures = value.get("textures")
                if isinstance(textures, dict):
                    texture_values.extend(v for v in textures.values() if isinstance(v, str))
                for child in value.values():
                    collect_textures(child)
            elif isinstance(value, list):
                for child in value:
                    collect_textures(child)

        collect_textures(data)
        for value in texture_values:
            match = re.match(rf'{re.escape(modid)}:(item|block)/([a-z0-9_/-]+)$', value)
            if not match:
                continue
            kind, name = match.groups()
            leaf = name.rsplit("/", 1)[-1]
            if kind == "item":
                item_refs.add(leaf)
            else:
                block_refs.add(leaf)
    return item_refs, block_refs


def parse_registered_items() -> set[str]:
    path = JAVA / "registry/ModItems.java"
    if not path.exists():
        return set()
    text = read_text(path)
    patterns = [
        r'registerSimpleItem\("([a-z0-9_]+)"',
        r'register\("([a-z0-9_]+)"',
        r'ITEMS\.register\("([a-z0-9_]+)"',
        r'registerSpawnEgg\("([a-z0-9_]+)"',
    ]
    ids: set[str] = set()
    for pattern in patterns:
        ids.update(re.findall(pattern, text))
    return ids


def parse_registered_blocks() -> set[str]:
    path = JAVA / "registry/ModBlocks.java"
    if not path.exists():
        return set()
    text = read_text(path)
    patterns = [
        r'registerCustomBlock\("([a-z0-9_]+)"',
        r'BLOCKS\.registerSimpleBlock\("([a-z0-9_]+)"',
        r'BLOCKS\.register\("([a-z0-9_]+)"',
    ]
    ids: set[str] = set()
    for pattern in patterns:
        ids.update(re.findall(pattern, text))
    return ids


def referenced_textures() -> tuple[set[str], set[str]]:
    return referenced_textures_for(ASSETS, MODID)


def item_definition_models() -> set[str]:
    models: set[str] = set()
    for path in (ASSETS / "items").glob("*.json"):
        try:
            data = json.loads(read_text(path))
        except json.JSONDecodeError:
            continue
        model = data.get("model", {})
        model_id = model.get("model")
        if isinstance(model_id, str) and model_id.startswith(f"{MODID}:item/"):
            models.add(model_id.split("/", 1)[1])
    return models


def item_definition_model_id(name: str) -> str | None:
    path = ASSETS / "items" / f"{name}.json"
    if not path.exists():
        return None
    try:
        data = json.loads(read_text(path))
    except json.JSONDecodeError:
        return None
    model = data.get("model", {})
    model_id = model.get("model")
    return model_id if isinstance(model_id, str) else None


def expected_block_item_model(name: str) -> str:
    if name == "ash_layer":
        return f"{MODID}:block/ash_layer_height2"
    if name in {"item_pipe", "power_cable", "high_voltage_power_cable", "reinforced_power_cable"}:
        return f"{MODID}:block/{name}_core"
    if name.endswith("_tall_grass"):
        return f"{MODID}:block/{name}_bottom"
    return f"{MODID}:block/{name}"


def opaque_colors(path: Path) -> set[tuple[int, int, int, int]]:
    with Image.open(path) as img:
        rgba = img.convert("RGBA")
        raw = rgba.tobytes()
    return {
        (raw[i], raw[i + 1], raw[i + 2], raw[i + 3])
        for i in range(0, len(raw), 4)
        if raw[i + 3] != 0
    }


def alpha_stats(path: Path) -> tuple[int, int]:
    with Image.open(path) as img:
        rgba = img.convert("RGBA")
        raw = rgba.tobytes()
    pixels = len(raw) // 4
    transparent = sum(1 for i in range(3, len(raw), 4) if raw[i] == 0)
    return pixels - transparent, transparent


def visible_chroma_key_pixels(path: Path) -> int:
    with Image.open(path) as img:
        rgba = img.convert("RGBA")
        raw = rgba.tobytes()
    return sum(
        1
        for i in range(0, len(raw), 4)
        if raw[i + 3] > 0
        and raw[i] >= 220
        and raw[i + 1] <= 70
        and raw[i + 2] >= 220
        and abs(raw[i] - raw[i + 2]) <= 60
        and raw[i] - raw[i + 1] >= 150
        and raw[i + 2] - raw[i + 1] >= 150
    )


def is_square_texture_size(width: int, height: int) -> bool:
    return width == height and width >= 16 and width % 16 == 0


def is_transparent_block(name: str) -> bool:
    if name.endswith("_grass_block") or "wasteland_grass_block" in name:
        return False
    if "leaves" in name:
        return False
    return any(w in name for w in PLANT_WORDS) or any(w in name for w in ("glass", "layer", "puddle"))


def clean_base_name(name: str) -> str:
    for suffix in (
        "_active_bottom",
        "_active_front",
        "_active_side",
        "_active_top",
        "_error_bottom",
        "_error_front",
        "_error_side",
        "_error_top",
        "_repaired_bottom",
        "_repaired_front",
        "_repaired_side",
        "_repaired_top",
        "_active",
        "_error",
        "_repaired",
        "_bottom",
        "_front",
        "_side",
        "_top",
        "_core",
    ):
        if name.endswith(suffix):
            return name[: -len(suffix)]
    return name


def is_machine_name(name: str) -> bool:
    return any(w in name for w in MACHINE_WORDS)


def machine_face(name: str) -> str:
    for face in ("bottom", "side", "top"):
        if name.endswith(f"_{face}") or any(name.endswith(f"_{state}_{face}") for state in ("active", "error", "repaired")):
            return face
    return "front"


def visual_family(name: str) -> str:
    if name.endswith("_spawn_egg"):
        return "spawn_egg"
    if name.startswith("data_log_"):
        return "data_log"
    if name.startswith("contaminated_") or name in {"uranium_shard"}:
        return "contaminated_resource"
    if any(w in name for w in ("bottle", "vial", "stim_pack", "rad_away")):
        return "bottle"
    if any(w in name for w in ("knife", "blade", "spear", "hammer", "annihilator")):
        return "weapon"
    if any(w in name for w in ("grass", "fern", "bush", "leaves", "reed", "fungus")):
        return "plant"
    if any(w in name for w in ("bone", "hide", "tissue")):
        return "organic"
    if any(w in name for w in ("schematic_fragment", "archives_key")):
        return "document"
    return ""


def pixel_bytes(path: Path) -> bytes:
    with Image.open(path) as img:
        return img.convert("RGBA").tobytes()


def visual_distance(a: bytes, b: bytes) -> int:
    if len(a) != len(b):
        return 10_000
    distance = 0
    for i in range(0, len(a), 4):
        if a[i : i + 4] != b[i : i + 4]:
            distance += 1
    return distance


def center_symbol_score(path: Path) -> bool:
    with Image.open(path) as img:
        rgba = img.convert("RGBA")
        px = rgba.load()
        center: dict[tuple[int, int, int, int], int] = {}
        border: dict[tuple[int, int, int, int], int] = {}
        for y in range(16):
            for x in range(16):
                color = px[x, y]
                if color[3] == 0:
                    continue
                if 5 <= x <= 10 and 5 <= y <= 10:
                    center[color] = center.get(color, 0) + 1
                if x in {0, 1, 14, 15} or y in {0, 1, 14, 15}:
                    border[color] = border.get(color, 0) + 1
    if not center:
        return False
    color, count = max(center.items(), key=lambda kv: kv[1])
    return count >= 24 and border.get(color, 0) <= 2


def add_aesthetic_warnings(texture_files: list[Path], warnings: list[str]) -> None:
    gameplay: list[Path] = []
    for path in texture_files:
        if path.parent.name not in {"item", "block"}:
            continue
        with Image.open(path) as img:
            if img.size == (16, 16):
                gameplay.append(path)

    signatures: dict[bytes, list[Path]] = {}
    samples: list[tuple[Path, bytes]] = []
    for path in gameplay:
        raw = pixel_bytes(path)
        signatures.setdefault(raw, []).append(path)
        samples.append((path, raw))

    for matches in signatures.values():
        if len(matches) > 1:
            bases = {clean_base_name(p.stem) for p in matches}
            if len(bases) == 1:
                continue
            names = ", ".join(rel(p) for p in matches[:5])
            warnings.append(f"Exact duplicate texture pixels: {names}")

    near_duplicate_count = 0
    for i, (a_path, a_raw) in enumerate(samples):
        a_base = clean_base_name(a_path.stem)
        for b_path, b_raw in samples[i + 1 :]:
            if a_path.parent != b_path.parent:
                continue
            if a_base == clean_base_name(b_path.stem):
                continue
            if visual_family(a_path.stem) and visual_family(a_path.stem) == visual_family(b_path.stem):
                continue
            if (
                is_machine_name(a_path.stem)
                and is_machine_name(b_path.stem)
                and machine_face(a_path.stem) != "front"
                and machine_face(b_path.stem) != "front"
            ):
                continue
            if visual_distance(a_raw, b_raw) <= 8:
                warnings.append(f"Near-duplicate 16x16 texture pair: {rel(a_path)} <-> {rel(b_path)}")
                near_duplicate_count += 1
                if near_duplicate_count >= 40:
                    warnings.append("Near-duplicate scan stopped after 40 findings.")
                    break
        if near_duplicate_count >= 40:
            break

    for path in texture_files:
        if path.parent.name != "block":
            continue
        name = path.stem
        if not is_transparent_block(name):
            continue
        opaque, _ = alpha_stats(path)
        if "cactus" in name:
            threshold = 96
        elif any(w in name for w in ("bush", "leaves", "fungus")):
            threshold = 64
        else:
            threshold = 48
        if any(w in name for w in PLANT_WORDS) and opaque > threshold:
            warnings.append(f"Plant cutout may be too dense/noisy ({opaque} opaque px): {rel(path)}")

    for path in texture_files:
        if path.parent.name != "block":
            continue
        name = path.stem
        if is_transparent_block(name) or is_machine_name(name):
            continue
        with Image.open(path) as img:
            if img.size != (16, 16):
                continue
        if center_symbol_score(path):
            warnings.append(f"Block tile has a strong center symbol: {rel(path)}")


def audit_orbital_assets(errors: list[str], warnings: list[str]) -> tuple[list[Path], set[str], set[str]]:
    item_refs, block_refs = referenced_textures_for(ORBITAL_ASSETS, ORBITAL_MODID)
    item_textures = {p.stem for p in (ORBITAL_TEXTURES / "item").glob("*.png")}
    block_textures = {p.stem for p in (ORBITAL_TEXTURES / "block").glob("*.png")}

    for name in sorted(item_refs - item_textures):
        errors.append(f"Orbital referenced item texture missing: textures/item/{name}.png")
    for name in sorted(block_refs - block_textures):
        errors.append(f"Orbital referenced block texture missing: textures/block/{name}.png")

    texture_files = list(ORBITAL_TEXTURES.rglob("*.png"))
    for path in texture_files:
        rel_path = rel(path)
        parts = path.relative_to(ORBITAL_TEXTURES).parts
        folder = parts[0]
        with Image.open(path) as img:
            width, height = img.size

        if folder == "item":
            if not is_square_texture_size(width, height):
                errors.append(
                    f"Orbital item texture must be a square 16px multiple: {rel_path} is {width}x{height}"
                )
            chroma_pixels = visible_chroma_key_pixels(path)
            if chroma_pixels > 128:
                errors.append(f"Orbital item texture contains visible chroma-key magenta: {rel_path}")
            opaque, transparent = alpha_stats(path)
            if transparent == 0:
                errors.append(f"Orbital item texture has no transparent background: {rel_path}")
            if opaque == 0:
                errors.append(f"Orbital item texture is fully transparent: {rel_path}")
        elif folder == "block":
            if not is_square_texture_size(width, height):
                errors.append(
                    f"Orbital block texture must be a square 16px multiple: {rel_path} is {width}x{height}"
                )
            chroma_pixels = visible_chroma_key_pixels(path)
            if chroma_pixels > 128:
                errors.append(f"Orbital block texture contains visible chroma-key magenta: {rel_path}")
            opaque, transparent = alpha_stats(path)
            if opaque == 0:
                errors.append(f"Orbital block texture is fully transparent: {rel_path}")
            if not is_transparent_block(path.stem) and transparent > 0:
                warnings.append(f"Orbital solid block texture contains transparent pixels: {rel_path}")
        elif folder == "entity":
            if (width, height) not in {(64, 32), (64, 64), (128, 64)}:
                errors.append(f"Orbital entity texture must remain 64x32, 64x64, or 128x64: {rel_path} is {width}x{height}")
        elif folder in {"gui", "fluid", "particle"}:
            continue
        else:
            warnings.append(f"Unexpected Orbital texture folder: {rel_path}")

    return texture_files, item_refs, block_refs


def vanilla_texture_refs_for(assets: Path) -> list[tuple[Path, str]]:
    refs: list[tuple[Path, str]] = []
    for model in (assets / "models").rglob("*.json"):
        try:
            data = json.loads(read_text(model))
        except json.JSONDecodeError:
            continue
        texture_values: list[str] = []

        def collect_textures(value: object) -> None:
            if isinstance(value, dict):
                textures = value.get("textures")
                if isinstance(textures, dict):
                    texture_values.extend(v for v in textures.values() if isinstance(v, str))
                for child in value.values():
                    collect_textures(child)
            elif isinstance(value, list):
                for child in value:
                    collect_textures(child)

        collect_textures(data)
        for value in texture_values:
            if value.startswith("minecraft:"):
                refs.append((model, value))
    return refs


def audit_generic_addon_assets(
    modid: str,
    assets: Path,
    errors: list[str],
    warnings: list[str],
) -> tuple[list[Path], set[str], set[str]]:
    if not assets.exists():
        return [], set(), set()

    textures_root = assets / "textures"
    item_refs, block_refs = referenced_textures_for(assets, modid)
    item_textures = {p.stem for p in (textures_root / "item").glob("*.png")}
    block_textures = {p.stem for p in (textures_root / "block").glob("*.png")}

    for model, texture in vanilla_texture_refs_for(assets):
        errors.append(f"{modid} vanilla texture ref remains: {rel(model)} -> {texture}")
    for name in sorted(item_refs - item_textures):
        errors.append(f"{modid} referenced item texture missing: textures/item/{name}.png")
    for name in sorted(block_refs - block_textures):
        errors.append(f"{modid} referenced block texture missing: textures/block/{name}.png")

    texture_files = list(textures_root.rglob("*.png")) if textures_root.exists() else []
    for path in texture_files:
        rel_path = rel(path)
        parts = path.relative_to(textures_root).parts
        folder = parts[0] if parts else ""
        with Image.open(path) as img:
            width, height = img.size

        if folder == "item":
            if not is_square_texture_size(width, height):
                errors.append(f"{modid} item texture must be a square 16px multiple: {rel_path} is {width}x{height}")
            chroma_pixels = visible_chroma_key_pixels(path)
            if chroma_pixels > 128:
                errors.append(f"{modid} item texture contains visible chroma-key magenta: {rel_path}")
            opaque, transparent = alpha_stats(path)
            if transparent == 0:
                errors.append(f"{modid} item texture has no transparent background: {rel_path}")
            if opaque == 0:
                errors.append(f"{modid} item texture is fully transparent: {rel_path}")
        elif folder == "block":
            if not is_square_texture_size(width, height):
                errors.append(f"{modid} block texture must be a square 16px multiple: {rel_path} is {width}x{height}")
            chroma_pixels = visible_chroma_key_pixels(path)
            if chroma_pixels > 128:
                errors.append(f"{modid} block texture contains visible chroma-key magenta: {rel_path}")
            opaque, transparent = alpha_stats(path)
            if opaque == 0:
                errors.append(f"{modid} block texture is fully transparent: {rel_path}")
            if not is_transparent_block(path.stem) and transparent > 0:
                warnings.append(f"{modid} solid block texture contains transparent pixels: {rel_path}")
        elif folder in {"entity", "gui", "fluid", "particle"}:
            continue
        else:
            warnings.append(f"Unexpected {modid} texture folder: {rel_path}")

    return texture_files, item_refs, block_refs


def audit() -> int:
    errors: list[str] = []
    warnings: list[str] = []

    item_refs, block_refs = referenced_textures()
    item_textures = {p.stem for p in (TEXTURES / "item").glob("*.png")}
    block_textures = {p.stem for p in (TEXTURES / "block").glob("*.png")}

    missing_item_refs = sorted(item_refs - item_textures)
    missing_block_refs = sorted(block_refs - block_textures)
    for name in missing_item_refs:
        errors.append(f"Referenced item texture missing: textures/item/{name}.png")
    for name in missing_block_refs:
        errors.append(f"Referenced block texture missing: textures/block/{name}.png")

    registered_items = parse_registered_items()
    missing_registered_items = sorted(registered_items - item_textures)
    for name in missing_registered_items:
        warnings.append(f"Registered item lacks unique item texture: textures/item/{name}.png")

    registered_item_models = item_definition_models()
    model_files = {p.stem for p in (ASSETS / "models/item").glob("*.json")}
    for name in sorted(registered_item_models - model_files):
        errors.append(f"Item definition references missing model: models/item/{name}.json")

    registered_blocks = parse_registered_blocks()
    stale_block_item_sprites = sorted((registered_blocks - registered_items) & item_textures)
    for name in stale_block_item_sprites:
        warnings.append(f"Block item still has stale flat item sprite: textures/item/{name}.png")
    missing_registered_blocks = sorted(registered_blocks - block_textures)
    for name in missing_registered_blocks:
        warnings.append(f"Registered block has no same-name block PNG; may be model-composed: {name}")
    for name in sorted(registered_blocks):
        actual = item_definition_model_id(name)
        expected = expected_block_item_model(name)
        if actual is None:
            errors.append(f"Registered block item definition missing or invalid: assets/echoashfallprotocol/items/{name}.json")
        elif actual != expected:
            errors.append(f"Block item definition points at wrong model: {name} -> {actual}, expected {expected}")

    texture_files = list(TEXTURES.rglob("*.png"))
    for path in texture_files:
        rel_path = rel(path)
        parts = path.relative_to(TEXTURES).parts
        folder = parts[0]
        if folder == "models" and len(parts) > 1 and parts[1] == "armor":
            folder = "armor"
        with Image.open(path) as img:
            width, height = img.size

        if folder == "item":
            if not is_square_texture_size(width, height):
                errors.append(f"Item texture must be a square 16px multiple: {rel_path} is {width}x{height}")
            chroma_pixels = visible_chroma_key_pixels(path)
            if chroma_pixels > 128:
                errors.append(f"Item texture contains visible chroma-key magenta: {rel_path}")
            opaque, transparent = alpha_stats(path)
            if transparent == 0:
                errors.append(f"Item texture has no transparent background: {rel_path}")
            colors = len(opaque_colors(path))
            if colors > 8:
                warnings.append(f"Item texture exceeds 8 opaque colors ({colors}): {rel_path}")
            if opaque == 0:
                errors.append(f"Item texture is fully transparent: {rel_path}")
        elif folder == "block":
            if not is_square_texture_size(width, height):
                errors.append(f"Block texture must be a square 16px multiple: {rel_path} is {width}x{height}")
            chroma_pixels = visible_chroma_key_pixels(path)
            if chroma_pixels > 128:
                errors.append(f"Block texture contains visible chroma-key magenta: {rel_path}")
            opaque, transparent = alpha_stats(path)
            if not is_transparent_block(path.stem) and transparent > 0:
                warnings.append(f"Solid block texture contains transparent pixels: {rel_path}")
            if is_transparent_block(path.stem) and opaque == 0:
                errors.append(f"Transparent-intended block is fully empty: {rel_path}")
            colors = len(opaque_colors(path))
            if colors > 8:
                warnings.append(f"Block texture exceeds 8 opaque colors ({colors}): {rel_path}")
        elif folder == "entity":
            if (width, height) not in {(64, 32), (64, 64), (128, 64)}:
                errors.append(f"Entity texture must remain 64x32, 64x64, or 128x64: {rel_path} is {width}x{height}")
        elif folder == "armor":
            if (width, height) != (64, 32):
                errors.append(f"Armor layer must remain 64x32: {rel_path} is {width}x{height}")
        elif folder == "gui":
            continue

    orbital_texture_files, orbital_item_refs, orbital_block_refs = audit_orbital_assets(errors, warnings)
    extra_texture_files: list[Path] = []
    extra_item_ref_count = 0
    extra_block_ref_count = 0
    for modid, asset_root in EXTRA_ASSET_ROOTS:
        addon_texture_files, addon_item_refs, addon_block_refs = audit_generic_addon_assets(
            modid,
            asset_root,
            errors,
            warnings,
        )
        extra_texture_files.extend(addon_texture_files)
        extra_item_ref_count += len(addon_item_refs)
        extra_block_ref_count += len(addon_block_refs)

    add_aesthetic_warnings(texture_files + orbital_texture_files + extra_texture_files, warnings)

    expected_outputs = [
        BASE / "build/texture_previews/texture_manifest.generated.json",
        BASE / "build/texture_previews/texture_catalog.md",
        BASE / "build/texture_previews/all_textures_sheet.png",
        BASE / "build/texture_previews/echoashfallprotocol_armor_sheet.png",
        BASE / "build/texture_previews/echoashfallprotocol_block_sheet.png",
        BASE / "build/texture_previews/echoashfallprotocol_entity_sheet.png",
        BASE / "build/texture_previews/echoashfallprotocol_gui_sheet.png",
        BASE / "build/texture_previews/echoashfallprotocol_item_sheet.png",
        BASE / "build/texture_previews/echoorbitalremnants_block_sheet.png",
        BASE / "build/texture_previews/echoorbitalremnants_item_sheet.png",
    ]
    for output in expected_outputs:
        if not output.exists():
            warnings.append(f"Generated preview/catalog output not found: {rel(output)}")

    print("=" * 70)
    print("TEXTURE AUDIT - ECHO")
    print("=" * 70)
    print(f"Texture PNGs:             {len(texture_files) + len(orbital_texture_files) + len(extra_texture_files)}")
    print(f"Ashfall texture PNGs:     {len(texture_files)}")
    print(f"Orbital texture PNGs:     {len(orbital_texture_files)}")
    print(f"Addon texture PNGs:       {len(extra_texture_files)}")
    print(f"Referenced item textures: {len(item_refs) + len(orbital_item_refs) + extra_item_ref_count}")
    print(f"Referenced block textures:{len(block_refs) + len(orbital_block_refs) + extra_block_ref_count}")
    print(f"Registered items:         {len(registered_items)}")
    print(f"Registered blocks:        {len(registered_blocks)}")
    print(f"Errors:                   {len(errors)}")
    print(f"Warnings:                 {len(warnings)}")

    if errors:
        print("\nERRORS")
        for error in errors[:200]:
            print(f"  - {error}")
        if len(errors) > 200:
            print(f"  ... and {len(errors) - 200} more")

    if warnings:
        print("\nWARNINGS")
        for warning in warnings[:200]:
            print(f"  - {warning}")
        if len(warnings) > 200:
            print(f"  ... and {len(warnings) - 200} more")

    if errors:
        return 1
    print("\nAll hard texture checks passed.")
    return 0


if __name__ == "__main__":
    sys.exit(audit())
