#!/usr/bin/env python3
"""Deterministic pixel texture generator for ECHO: Ashfall Protocol.

This intentionally avoids resizing or filtering. Every gameplay item/block
texture is drawn on a tiny grid with hard pixels and a small palette.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import random
import re
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Iterable

from PIL import Image, ImageDraw


BASE = Path(__file__).resolve().parents[1]
ASSETS = BASE / "src/main/resources/assets/echoashfallprotocol"
JAVA = BASE / "src/main/java/com/knoxhack/echoashfallprotocol"
TEXTURES = ASSETS / "textures"
BUILD_OUT = BASE / "build/texture_previews"
MODID = "echoashfallprotocol"


RGBA = tuple[int, int, int, int]


@dataclass(frozen=True)
class TextureSpec:
    name: str
    type: str
    path: str
    width: int
    height: int
    transparent: bool
    category: str
    family: str
    machine_role: str
    item_role: str
    summary: str
    palette: list[str]
    placement_notes: str
    mistakes_to_avoid: str
    prompt: str


PALETTES: dict[str, list[str]] = {
    "ash": ["#d2d0c6", "#9c9a91", "#66665f", "#3d3d3a", "#222423", "#6b5542"],
    "charred": ["#b4afa4", "#75726c", "#454542", "#262725", "#151817", "#8a4b28"],
    "dirt": ["#b8885a", "#8b6544", "#634732", "#3f3026", "#a79a78", "#59633a"],
    "toxic_dirt": ["#99845b", "#706349", "#4d4a36", "#2e3328", "#9bd94b", "#5d2d78", "#2a201f"],
    "stone": ["#c0c0b7", "#8c918d", "#686d6b", "#434846", "#292e2d", "#8a7a63"],
    "concrete": ["#c4c1b6", "#959790", "#6f7471", "#4b514f", "#2d3332", "#8b6a4c"],
    "metal": ["#d8d8cf", "#aaaea7", "#6f7775", "#444c4c", "#232a2b", "#9b5a2f", "#d18a43"],
    "machine": ["#d6d3c7", "#9ca29d", "#68706f", "#394243", "#1f282a", "#6aa84f", "#66cbd1"],
    "active_machine": ["#d8d7cc", "#a0a8a2", "#636e6d", "#283235", "#151d21", "#9ceb44", "#53d6e8"],
    "error_machine": ["#d7d2c8", "#9f9f98", "#686c68", "#383f40", "#1d2426", "#d94b3f", "#f29a4a"],
    "plant": ["#c7b56c", "#8f8b4e", "#58653c", "#303c2d", "#9fd35b", "#684c33"],
    "toxic_plant": ["#d4ee74", "#9dca4d", "#5d8037", "#31442b", "#b24de0", "#243125"],
    "nuclear": ["#efff8b", "#c5ef55", "#7fb43a", "#3f6b2d", "#263526", "#8ad7ff"],
    "nexus": ["#ddd2ff", "#a983dd", "#7150a9", "#392b61", "#1d1732", "#51d3ff", "#f2d45c"],
    "cryo": ["#e9fbff", "#aadce5", "#6da8bf", "#3f6178", "#263449", "#ffffff"],
    "water": ["#d9f3ff", "#8bcce8", "#4b90b8", "#2d5775", "#1d344c", "#77e35d"],
    "rust": ["#d0b07a", "#9f6f3f", "#754025", "#472c23", "#242729", "#c95f2e"],
    "scrap": ["#d8d8d0", "#9aa09e", "#69706e", "#3b4242", "#202626", "#a45b31", "#db9440"],
    "organic": ["#d6a2a2", "#a85e66", "#733b46", "#3d242e", "#8cc653", "#d7e66c"],
    "paper": ["#f0e2bd", "#c9b27e", "#8f7144", "#4e3828", "#6e9a4d", "#4da3b8"],
    "default": ["#d0cbc0", "#9c9b93", "#696f6b", "#3e4644", "#202626", "#7fa44d"],
}


MACHINE_NAMES = {
    "atmospheric_scrubber",
    "autofeed_hopper",
    "battery_bank",
    "bio_processing_station",
    "contaminant_condenser",
    "crystalline_synthesizer",
    "deep_core_miner",
    "echo_terminal",
    "echo_terminal_block",
    "energy_meter",
    "factory_controller",
    "field_med_bay",
    "filter_workbench",
    "hand_recycler",
    "isotope_refiner",
    "item_pipe",
    "load_distributor",
    "map_table",
    "micro_generator",
    "nexus_capacitor",
    "nexus_core",
    "ore_grinder",
    "power_cable",
    "power_node",
    "radiation_cleanser",
    "rain_collector",
    "relay_station",
    "research_lab",
    "scrap_dynamo",
    "scrap_press",
    "signal_scanner",
    "spore_garden",
    "supply_crate",
    "thermal_array",
    "thermal_burner",
    "toxic_waste_barrel",
    "trade_counter",
    "water_purifier",
    "weapon_rack",
    "workshop_block",
}


MULTISIDE_MACHINE_MODEL_NAMES = {
    "energy_meter",
    "load_distributor",
    "nexus_capacitor",
    "scrap_dynamo",
    "scrap_dynamo_active",
    "toxic_waste_barrel",
}


MACHINE_ACCENTS = {
    "atmospheric_scrubber": "#75d6c8",
    "autofeed_hopper": "#d4a457",
    "battery_bank": "#a6ef4a",
    "bio_processing_station": "#9bd968",
    "contaminant_condenser": "#7cd957",
    "crystalline_synthesizer": "#75d7ff",
    "deep_core_miner": "#e0b15a",
    "echo_terminal": "#5cc8ff",
    "echo_terminal_block": "#5cc8ff",
    "energy_meter": "#70d9ff",
    "factory_controller": "#f2c75c",
    "field_med_bay": "#f06565",
    "filter_workbench": "#92d06f",
    "hand_recycler": "#d2a05a",
    "isotope_refiner": "#b8f24b",
    "item_pipe": "#c9b47a",
    "load_distributor": "#83d8ff",
    "map_table": "#d0b26b",
    "micro_generator": "#75d7ff",
    "nexus_capacitor": "#b681ff",
    "nexus_core": "#b27dff",
    "ore_grinder": "#c6c0ad",
    "power_cable": "#f1d64b",
    "power_node": "#f4dc55",
    "radiation_cleanser": "#b8f24b",
    "rain_collector": "#69c8ef",
    "relay_station": "#69c8ef",
    "research_lab": "#7fd7ff",
    "scrap_dynamo": "#f2b84b",
    "scrap_press": "#d8974e",
    "signal_scanner": "#65d4ff",
    "spore_garden": "#b06ad8",
    "supply_crate": "#c79252",
    "thermal_array": "#f2b84b",
    "thermal_burner": "#ec743b",
    "toxic_waste_barrel": "#9ee84a",
    "trade_counter": "#d1ab62",
    "water_purifier": "#5fc2f2",
    "weapon_rack": "#c6a064",
    "workshop_block": "#c7b478",
}


DRONE_ENTITY_TEXTURES = {
    "echo_companion_drone",
    "echo_drone",
    "scout_drone",
}


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
)


RESOURCE_WORDS = (
    "scrap",
    "shard",
    "chunk",
    "dust",
    "trace",
    "cluster",
    "crystal",
    "gem",
    "ore",
    "iron",
    "gold",
    "copper",
    "alloy",
    "uranium",
    "coal",
    "ash",
    "metal",
    "plastic",
)


def rel(path: Path) -> str:
    return path.relative_to(BASE).as_posix()


def stable_rng(key: str) -> random.Random:
    digest = hashlib.sha256(key.encode("utf-8")).digest()
    return random.Random(int.from_bytes(digest[:8], "big"))


def hex_to_rgba(value: str, alpha: int = 255) -> RGBA:
    value = value.strip().lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha)


def palette_rgba(spec: TextureSpec) -> list[RGBA]:
    return [hex_to_rgba(c) for c in spec.palette]


def transparent_image(width: int, height: int) -> Image.Image:
    return Image.new("RGBA", (width, height), (0, 0, 0, 0))


def filled_image(width: int, height: int, color: RGBA) -> Image.Image:
    return Image.new("RGBA", (width, height), color)


def put(img: Image.Image, x: int, y: int, color: RGBA) -> None:
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def rect(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], color: RGBA) -> None:
    draw.rectangle(xy, fill=color)


def line(img: Image.Image, points: Iterable[tuple[int, int]], color: RGBA) -> None:
    pts = list(points)
    if len(pts) < 2:
        return
    draw = ImageDraw.Draw(img)
    draw.line(pts, fill=color, width=1)


def draw_poly(draw: ImageDraw.ImageDraw, points: list[tuple[int, int]], color: RGBA) -> None:
    draw.polygon(points, fill=color)


def safe_palette(category: str) -> list[str]:
    return PALETTES.get(category, PALETTES["default"])


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


def category_for(name: str, tex_type: str) -> str:
    n = name.lower()
    base = clean_base_name(n)
    if tex_type == "gui":
        return "machine"
    if tex_type == "armor":
        return "nexus" if "nexus" in n else "metal"
    if tex_type == "entity":
        if any(w in n for w in ("toxic", "slime", "mutant", "crawler")):
            return "toxic_plant"
        if any(w in n for w in ("ash", "wraith", "ghoul", "zombie")):
            return "charred"
        if any(w in n for w in ("drone", "echo", "scout")):
            return "active_machine"
        if any(w in n for w in ("nexus", "warden")):
            return "nexus"
        return "organic"
    if base in MACHINE_NAMES or any(w in n for w in ("machine", "generator", "scrubber", "condenser", "terminal", "controller", "purifier", "refiner", "grinder", "press", "scanner", "relay", "battery", "pipe", "cable", "array", "burner", "workbench", "med_bay", "meter", "distributor", "capacitor", "dynamo", "barrel")):
        if "error" in n:
            return "error_machine"
        if "active" in n or "repaired" in n:
            return "active_machine"
        return "machine"
    if "nexus" in n:
        return "nexus"
    if any(w in n for w in ("cryo", "ice", "frost")):
        return "cryo"
    if any(w in n for w in ("toxic", "acid", "sludge", "puddle", "contaminated", "irradiated", "uranium", "rad_", "radiation")):
        return "toxic_dirt" if tex_type == "block" else "nuclear"
    if any(w in n for w in ("nuclear", "glowing")):
        return "nuclear"
    if any(w in n for w in ("ash", "fallout", "dust")):
        return "ash"
    if any(w in n for w in ("burnt", "charred", "coal", "deep_ash")):
        return "charred"
    if any(w in n for w in ("rust", "copper")):
        return "rust"
    if tex_type == "block" and any(w in n for w in ("drop_pod", "hull")):
        return "metal"
    if any(w in n for w in ("stone", "shale", "slag", "aggregate", "concrete", "rubble", "crust", "debris")):
        return "concrete" if any(w in n for w in ("concrete", "rubble", "aggregate")) else "stone"
    if any(w in n for w in ("dirt", "soil", "wasteland")) and tex_type == "block":
        return "dirt"
    if any(w in n for w in PLANT_WORDS):
        return "toxic_plant" if any(w in n for w in ("toxic", "mutated", "nuclear", "irradiated")) else "plant"
    if any(w in n for w in ("water", "bottle", "vial", "stim", "away")):
        return "water"
    if any(w in n for w in ("scrap", "metal", "iron", "alloy", "blade", "hammer", "knife", "spear", "casing", "shard", "wire")):
        return "scrap"
    if any(w in n for w in ("circuit", "energy", "power", "scanner", "schematic", "data_log", "archives", "cell")):
        return "active_machine"
    if any(w in n for w in ("hide", "bone", "tissue", "meat", "ration", "berry")):
        return "organic"
    if any(w in n for w in ("paper", "log", "archive")):
        return "paper"
    return "default"


def texture_kind_from_path(path: Path) -> str:
    parts = path.relative_to(TEXTURES).parts
    if parts[0] == "models" and len(parts) > 1 and parts[1] == "armor":
        return "armor"
    return parts[0]


def png_dimensions(path: Path) -> tuple[int, int]:
    with Image.open(path) as img:
        return img.size


def block_transparency(name: str) -> bool:
    n = name.lower()
    return is_cutout_plant(n) or any(w in n for w in ("glass", "layer", "puddle"))


def is_cutout_plant(name: str) -> bool:
    n = name.lower()
    if n.endswith("_grass_block") or "wasteland_grass_block" in n:
        return False
    if "leaves" in n:
        return False
    return any(w in n for w in PLANT_WORDS)


def is_leaf_block(name: str) -> bool:
    return "leaves" in name.lower()


def machine_base(name: str) -> str:
    return clean_base_name(name.lower())


def machine_role_for(name: str) -> str:
    base = machine_base(name)
    roles = {
        "atmospheric_scrubber": "scrubber",
        "autofeed_hopper": "hopper",
        "battery_bank": "battery",
        "bio_processing_station": "bio_lab",
        "contaminant_condenser": "condenser",
        "crystalline_synthesizer": "synthesizer",
        "deep_core_miner": "miner",
        "echo_terminal": "terminal",
        "echo_terminal_block": "terminal_block",
        "energy_meter": "meter",
        "factory_controller": "controller",
        "field_med_bay": "med",
        "filter_workbench": "filter_bench",
        "hand_recycler": "recycler",
        "isotope_refiner": "refiner",
        "item_pipe": "pipe",
        "load_distributor": "distributor",
        "map_table": "map_table",
        "micro_generator": "generator",
        "nexus_capacitor": "capacitor",
        "nexus_core": "reactor",
        "ore_grinder": "grinder",
        "power_cable": "cable",
        "power_node": "power_node",
        "radiation_cleanser": "cleanser",
        "rain_collector": "collector",
        "relay_station": "relay",
        "research_lab": "lab",
        "scrap_dynamo": "generator",
        "scrap_press": "press",
        "signal_scanner": "scanner",
        "spore_garden": "bio_pod",
        "supply_crate": "crate",
        "thermal_array": "array",
        "thermal_burner": "burner",
        "toxic_waste_barrel": "barrel",
        "trade_counter": "counter",
        "water_purifier": "purifier",
        "weapon_rack": "rack",
        "workshop_block": "workbench",
    }
    if base in roles:
        return roles[base]
    if "pipe" in base:
        return "pipe"
    if "cable" in base:
        return "cable"
    if "battery" in base:
        return "battery"
    if "terminal" in base:
        return "terminal"
    return "panel"


def item_role_for(name: str) -> str:
    n = name.lower()
    if n.endswith("_spawn_egg"):
        return "spawn_egg"
    if n.startswith("data_log"):
        return "data_log"
    if "filter_cartridge" in n:
        return "filter"
    if "battery" in n or n in {"energy_cell", "power_cell"}:
        return "battery"
    if any(w in n for w in ("bottle", "vial", "stim", "rad_away")):
        return "bottle"
    if any(w in n for w in ("blade", "hammer", "knife", "spear", "annihilator")):
        return "tool"
    if any(w in n for w in ("helmet", "chestplate", "leggings", "boots", "gas_mask")):
        return "armor_item"
    if any(w in n for w in ("circuit", "scanner", "schematic", "upgrade", "cell")):
        return "tech"
    if any(w in n for w in ("bandage", "ration", "warmer", "liner", "membrane")):
        return "survival"
    if any(w in n for w in ("fiber", "rope", "wire")):
        return "fiber"
    if any(w in n for w in ("bone", "hide", "tissue", "berry", "meat")):
        return "organic"
    if any(w in n for w in RESOURCE_WORDS):
        return "resource"
    return "material"


def texture_family_for(name: str, tex_type: str, category: str) -> str:
    n = name.lower()
    if tex_type == "item":
        return item_role_for(n)
    if tex_type != "block":
        return tex_type
    if is_leaf_block(n):
        return "foliage"
    if is_cutout_plant(n):
        return "plant"
    if category in {"machine", "active_machine", "error_machine"}:
        role = machine_role_for(n)
        if role == "pipe":
            return "pipe"
        if role == "cable":
            return "cable"
        return "machine"
    if category in {"dirt", "toxic_dirt", "stone", "concrete", "ash", "charred", "rust", "scrap", "metal", "nuclear", "cryo", "nexus"}:
        return "terrain"
    if any(w in n for w in ("glass", "puddle", "sludge")):
        return "liquid_glass"
    return "block"


def summary_for(name: str, tex_type: str, category: str) -> str:
    words = name.replace("_", " ")
    if tex_type == "item":
        if "spawn_egg" in name:
            return f"Vanilla-friendly spawn egg icon for {words.replace(' spawn egg', '')}."
        if name.startswith("data_log"):
            return "Tiny recovered data-log tablet with a unique archive stripe and status pixel."
        if item_role_for(name) == "battery":
            return f"Clean sci-fi battery/cell silhouette for {words}."
        if item_role_for(name) == "filter":
            return f"Readable filter cartridge item for {words}."
        if item_role_for(name) == "resource":
            return f"Distinct salvaged resource silhouette for {words}."
        return f"Readable 16x16 item silhouette for {words}."
    if tex_type == "block":
        if category in {"machine", "active_machine", "error_machine"}:
            return f"Clean sci-fi {machine_role_for(name).replace('_', ' ')} machine tile for {words}, rebuilt at 16x16."
        if block_transparency(name):
            return f"Transparent cutout block texture for {words}."
        return f"Tileable 16x16 material block texture for {words}."
    if tex_type == "entity":
        return f"Blocky Minecraft entity UV sheet for {words}, preserving required dimensions."
    if tex_type == "armor":
        return f"Pixel armor UV layer for {words}, preserving required 64x32 layout."
    return f"Pixel-styled GUI atlas for {words}, preserving required screen dimensions."


def placement_for(tex_type: str, category: str) -> str:
    if tex_type == "item":
        return "Center the silhouette, leave transparent breathing room, highlight top-left, shadow bottom-right."
    if tex_type == "block":
        if category in {"machine", "active_machine", "error_machine"}:
            return "Use a clean graphite/silver 16x16 shell, one role-specific face feature, and readable status accents."
        return "Use repeating 2x2 and 3x2 clusters with no large center symbol; keep borders tile-friendly."
    if tex_type == "entity":
        return "Draw hard-edged UV islands with larger color regions and small material accents."
    if tex_type == "armor":
        return "Draw transparent UV islands with clear helmet/chest/limb plates and restrained highlights."
    return "Draw a dark panel atlas with slots and borders aligned to a 2-pixel grid."


def prompt_for(spec_name: str, tex_type: str, category: str) -> str:
    return (
        "Create a Minecraft Java pixel-art texture.\n\n"
        f"Texture name:\n{spec_name}\n\n"
        f"Texture type:\n{tex_type}\n\n"
        "Theme:\nPOST-APOCALYPTIC / TOXIC / SCRAP / FUTURISTIC / VANILLA-FRIENDLY\n\n"
        f"Visual description:\n{summary_for(spec_name, tex_type, category)}\n\n"
        "Pixel-art rules:\n"
        "- crisp hard pixels\n"
        "- no anti-aliasing\n"
        "- no blur\n"
        "- no smooth gradients\n"
        "- limited palette\n"
        "- vanilla Minecraft style\n"
        "- readable silhouette or material identity\n"
        "- top-left lighting\n"
        "- darker bottom-right shading\n\n"
        "Quality target:\nLooks like it belongs in a polished Minecraft NeoForge modpack."
    )


def make_spec(path: Path, forced_type: str | None = None, width: int | None = None, height: int | None = None) -> TextureSpec:
    tex_type = forced_type or texture_kind_from_path(path)
    name = path.stem
    if width is None or height is None:
        if path.exists():
            width, height = png_dimensions(path)
        else:
            width, height = (16, 16)
    if tex_type in {"item", "block"}:
        width = height = 16
    elif tex_type == "entity" and name in DRONE_ENTITY_TEXTURES:
        width = height = 64
    category = category_for(name, tex_type)
    family = texture_family_for(name, tex_type, category)
    machine_role = machine_role_for(name) if tex_type == "block" and category in {"machine", "active_machine", "error_machine"} else ""
    item_role = item_role_for(name) if tex_type == "item" else ""
    transparent = tex_type == "item" or tex_type in {"entity", "armor"} or (tex_type == "block" and block_transparency(name))
    pal = safe_palette(category)
    return TextureSpec(
        name=name,
        type=tex_type,
        path=rel(path),
        width=width,
        height=height,
        transparent=transparent,
        category=category,
        family=family,
        machine_role=machine_role,
        item_role=item_role,
        summary=summary_for(name, tex_type, category),
        palette=pal,
        placement_notes=placement_for(tex_type, category),
        mistakes_to_avoid="Avoid blur, anti-aliasing, smooth gradients, noisy single-pixel static, and edge-crowded shapes.",
        prompt=prompt_for(name, tex_type, category),
    )


def parse_registered_items() -> set[str]:
    path = JAVA / "registry/ModItems.java"
    if not path.exists():
        return set()
    text = path.read_text(encoding="utf-8", errors="ignore")
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
    text = path.read_text(encoding="utf-8", errors="ignore")
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
    item_refs: set[str] = set()
    block_refs: set[str] = set()
    for model in (ASSETS / "models").rglob("*.json"):
        try:
            data = json.loads(model.read_text(encoding="utf-8", errors="ignore"))
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
            match = re.match(r'echoashfallprotocol:(item|block)/([a-z0-9_/-]+)$', value)
            if not match:
                continue
            kind, name = match.groups()
            leaf = name.rsplit("/", 1)[-1]
            if kind == "item":
                item_refs.add(leaf)
            else:
                block_refs.add(leaf)
    return item_refs, block_refs


def multiside_machine_texture_names() -> set[str]:
    names: set[str] = set()
    for model_name in MULTISIDE_MACHINE_MODEL_NAMES:
        names.add(model_name)
        for suffix in ("front", "side", "top", "bottom"):
            names.add(f"{model_name}_{suffix}")
    return names


def build_manifest() -> list[TextureSpec]:
    registered_items = parse_registered_items()
    registered_blocks = parse_registered_blocks()
    paths: dict[str, Path] = {}
    for path in TEXTURES.rglob("*.png"):
        parts = path.relative_to(TEXTURES).parts
        if parts[0] == "item" and path.stem in registered_blocks and path.stem not in registered_items:
            continue
        paths[rel(path)] = path

    item_refs, block_refs = referenced_textures()
    item_names = {
        p.stem
        for p in (TEXTURES / "item").glob("*.png")
        if p.stem not in registered_blocks or p.stem in registered_items
    }
    block_names = {p.stem for p in (TEXTURES / "block").glob("*.png")}
    item_names.update(item_refs - registered_blocks)
    item_names.update(registered_items)
    block_names.update(block_refs)
    block_names.update(registered_blocks)
    block_names.update(multiside_machine_texture_names())

    for name in sorted(item_names):
        path = TEXTURES / "item" / f"{name}.png"
        paths[rel(path)] = path
    for name in sorted(block_names):
        path = TEXTURES / "block" / f"{name}.png"
        paths[rel(path)] = path

    specs: list[TextureSpec] = []
    for path in sorted(paths.values(), key=lambda p: rel(p)):
        tex_type = texture_kind_from_path(path)
        if tex_type in {"item", "block"}:
            specs.append(make_spec(path, width=16, height=16))
        elif tex_type in {"entity", "gui", "armor"}:
            width, height = png_dimensions(path) if path.exists() else ((64, 32) if tex_type != "gui" else (256, 166))
            specs.append(make_spec(path, width=width, height=height))
    return specs

def accent_for_machine(name: str) -> RGBA:
    base = machine_base(name)
    return hex_to_rgba(MACHINE_ACCENTS.get(base, "#79c7d8"))


def machine_style(name: str) -> str:
    role = machine_role_for(name)
    if role in {"purifier", "collector"}:
        return "fluid"
    if role in {"burner", "array", "generator"}:
        return "power"
    if role in {"battery", "power_node", "cable"}:
        return "energy"
    if role in {"scrubber", "filter_bench", "condenser", "cleanser"}:
        return "filter"
    if role in {"press", "grinder", "miner", "hopper", "recycler"}:
        return "mechanical"
    if role in {"relay", "scanner", "terminal", "terminal_block", "controller"}:
        return "screen"
    if role in {"bio_pod", "bio_lab"}:
        return "bio"
    if role == "pipe":
        return "pipe"
    if role == "barrel":
        return "barrel"
    if role in {"crate", "counter", "map_table", "rack", "workbench", "lab", "med"}:
        return "utility"
    if role in {"reactor", "synthesizer", "refiner"}:
        return "reactor"
    return "panel"


def draw_tiny_gear(img: Image.Image, cx: int, cy: int, color: RGBA, dark: RGBA) -> None:
    for x, y in [(cx, cy - 2), (cx + 2, cy), (cx, cy + 2), (cx - 2, cy)]:
        put(img, x, y, dark)
    for x, y in [(cx - 1, cy - 1), (cx, cy - 1), (cx + 1, cy - 1), (cx - 1, cy), (cx + 1, cy), (cx - 1, cy + 1), (cx, cy + 1), (cx + 1, cy + 1)]:
        put(img, x, y, color)
    put(img, cx, cy, dark)


def draw_tiny_drop(img: Image.Image, x: int, y: int, color: RGBA, light: RGBA) -> None:
    draw = ImageDraw.Draw(img)
    draw_poly(draw, [(x + 2, y), (x + 4, y + 3), (x + 3, y + 6), (x, y + 6), (x - 1, y + 3)], color)
    put(img, x + 1, y + 2, light)


def draw_cluster_tile(img: Image.Image, spec: TextureSpec) -> None:
    rng = stable_rng(f"block:{spec.name}")
    n = spec.name.lower()
    draw = ImageDraw.Draw(img)

    if spec.category == "toxic_dirt":
        palette = ["#5c5942", "#706848", "#8a7a4f", "#41432f", "#292f26", "#8dbf4c", "#6b4778", "#b4dd62"]
        material = "dirt"
    elif spec.category == "dirt":
        palette = ["#695f4f", "#7d705b", "#9a8668", "#4c453b", "#302c27", "#6a7449", "#9b895f", "#62536b"]
        material = "dirt"
    elif spec.category == "ash":
        palette = ["#6b6d67", "#81827a", "#9c9c90", "#50544f", "#333835", "#756a59", "#aaa99c", "#454943"]
        material = "ash"
    elif spec.category == "charred":
        palette = ["#424440", "#5a5a52", "#747166", "#2e3330", "#191d1c", "#8e4e2e", "#a86b3a", "#666a60"]
        material = "ash"
    elif spec.category == "concrete":
        palette = ["#687069", "#7f8379", "#9f9a89", "#505852", "#2e3633", "#8a684b", "#b7ad91", "#4d5650"]
        material = "stone"
        if "oil" in n:
            palette = ["#555b55", "#686d66", "#85867c", "#3f4742", "#252c2a", "#3b2e24", "#9d8f70", "#2f3734"]
    elif spec.category == "stone":
        palette = ["#626a65", "#788078", "#969486", "#48524e", "#2a3230", "#737b5c", "#aaa28c", "#3c4542"]
        material = "stone"
        if any(w in n for w in ("cryo", "fractured")):
            palette = ["#5e8494", "#75a7b7", "#a8d6de", "#3f6372", "#263848", "#d8fbff", "#ffffff", "#467a94"]
            material = "cryo"
        elif any(w in n for w in ("irradiated", "radiation")):
            palette = ["#62694b", "#78825a", "#969b70", "#444d39", "#283026", "#a7d850", "#d9ff78", "#33402e"]
            material = "toxic_stone"
        elif "slag" in n:
            palette = ["#5f5950", "#756c5f", "#968271", "#433f3a", "#282927", "#a15c35", "#c77943", "#504b43"]
    elif spec.category == "cryo":
        palette = ["#5e8494", "#75a7b7", "#a8d6de", "#3f6372", "#263848", "#d8fbff", "#ffffff", "#467a94"]
        material = "cryo"
    elif spec.category == "rust":
        palette = ["#704225", "#8b5330", "#b1693a", "#4b2d21", "#282321", "#c98547", "#2f3333", "#a3522e"]
        material = "metal"
    elif spec.category in {"metal", "scrap"}:
        palette = ["#68716d", "#7f8782", "#a3a69a", "#4a5552", "#2b3432", "#ad7944", "#d39a53", "#5d6461"]
        material = "metal"
    elif spec.category == "nexus":
        palette = ["#443765", "#5b4783", "#8466b7", "#2d2544", "#191526", "#5dcfe8", "#b685ff", "#efd668"]
        material = "nexus"
    elif spec.category == "nuclear":
        palette = ["#596645", "#758651", "#9dbb63", "#3f4b35", "#263024", "#b8f24b", "#eaff85", "#71c958"]
        material = "toxic_stone"
    else:
        palette = ["#69706a", "#7d847b", "#999b8f", "#4d5752", "#2e3633", "#806f53", "#a9a48d", "#56605a"]
        material = "stone"

    c = [hex_to_rgba(value) for value in palette]
    img.paste(c[0], (0, 0, 16, 16))

    def color(index: int) -> RGBA:
        return c[min(index, len(c) - 1)]

    def tile_rect(x: int, y: int, w: int, h: int, value: int) -> None:
        fill = color(value)
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                put(img, xx % 16, yy % 16, fill)

    def clustered(values: list[tuple[int, int, int, int, int]]) -> None:
        for x, y, w, h, value in values:
            dx = rng.choice([-1, 0, 0, 1])
            dy = rng.choice([-1, 0, 0, 1])
            tile_rect(x + dx, y + dy, w, h, value)

    def add_corner_light() -> None:
        tile_rect(1, 1, 2, 1, 2)
        tile_rect(13, 14, 2, 1, 4)

    if material == "dirt":
        clustered(
            [
                (0, 1, 3, 2, 1),
                (4, 0, 3, 3, 2),
                (9, 2, 4, 2, 3),
                (14, 4, 4, 3, 1),
                (1, 8, 4, 3, 3),
                (6, 7, 3, 2, 1),
                (10, 10, 4, 3, 2),
                (3, 13, 3, 3, 4),
                (12, 14, 4, 2, 1),
            ]
        )
        tile_rect(4, 6, 2, 2, 6)
        tile_rect(11, 6, 2, 2, 4)
        line(img, [(0, 15), (2, 15)], color(3))
        line(img, [(3, 0), (5, 0)], color(3))
        line(img, [(8, 12), (11, 12), (11, 13)], color(4))
        if "grass_block" in n:
            grass = color(5)
            grass_light = color(7)
            rect(draw, (0, 0, 15, 1), grass)
            tile_rect(2, 2, 3, 1, 5)
            tile_rect(9, 2, 4, 1, 5)
            tile_rect(5, 0, 2, 1, 7)
            tile_rect(13, 1, 3, 2, 7)
            put(img, 1, 0, grass_light)
            put(img, 10, 1, grass_light)
        if spec.category == "toxic_dirt":
            tile_rect(5, 9, 2, 2, 5)
            tile_rect(13, 7, 2, 2, 6)
            line(img, [(6, 10), (8, 10), (9, 11)], color(7))
    elif material in {"stone", "toxic_stone", "cryo", "nexus"}:
        clustered(
            [
                (0, 0, 4, 3, 1),
                (5, 1, 3, 3, 2),
                (10, 0, 5, 2, 3),
                (13, 4, 4, 3, 1),
                (2, 5, 5, 3, 3),
                (8, 6, 3, 2, 1),
                (0, 10, 4, 4, 4),
                (5, 11, 4, 3, 1),
                (11, 10, 5, 4, 2),
                (7, 14, 3, 3, 3),
            ]
        )
        line(img, [(2, 9), (5, 9), (5, 10), (7, 10)], color(4))
        line(img, [(10, 4), (12, 4), (12, 6)], color(4))
        line(img, [(14, 12), (15, 12)], color(4))
        line(img, [(0, 12), (1, 13)], color(4))
        if material == "toxic_stone":
            tile_rect(6, 13, 2, 2, 5)
            tile_rect(13, 8, 2, 2, 6)
            line(img, [(6, 14), (8, 14), (9, 13)], color(6))
        elif material == "cryo":
            line(img, [(3, 13), (5, 10), (8, 9), (11, 6)], color(5))
            line(img, [(4, 13), (6, 11), (9, 10)], color(6))
            tile_rect(12, 2, 2, 2, 5)
        elif material == "nexus":
            line(img, [(1, 13), (4, 11), (7, 11), (10, 8)], color(5))
            tile_rect(10, 8, 2, 2, 6)
            tile_rect(3, 3, 2, 2, 7)
    elif material == "ash":
        clustered(
            [
                (0, 1, 5, 2, 1),
                (6, 2, 4, 2, 2),
                (12, 0, 4, 3, 3),
                (1, 6, 4, 2, 3),
                (5, 8, 5, 2, 1),
                (11, 7, 4, 3, 2),
                (0, 12, 5, 3, 4),
                (7, 13, 5, 2, 1),
                (13, 12, 4, 3, 3),
            ]
        )
        for y, value in ((4, 3), (10, 7), (15, 4)):
            line(img, [(0, y), (4, y), (6, y + (1 if y < 15 else 0)), (12, y), (15, y)], color(value))
        if spec.category == "charred":
            tile_rect(3, 11, 2, 2, 5)
            tile_rect(4, 11, 2, 1, 6)
    elif material == "metal":
        img.paste(color(1), (0, 0, 16, 16))
        rect(draw, (0, 0, 15, 1), color(2))
        rect(draw, (0, 0, 1, 15), color(2))
        rect(draw, (14, 2, 15, 15), color(4))
        rect(draw, (2, 14, 15, 15), color(4))
        line(img, [(0, 7), (15, 7)], color(3))
        line(img, [(7, 0), (7, 15)], color(3))
        clustered(
            [
                (2, 2, 4, 3, 0),
                (9, 2, 4, 2, 3),
                (2, 9, 4, 3, 3),
                (10, 10, 3, 3, 0),
                (14, 5, 3, 2, 5),
            ]
        )
        for x, y in [(3, 3), (12, 3), (3, 12), (12, 12)]:
            tile_rect(x, y, 2, 1, 2)
        if spec.category == "rust":
            tile_rect(5, 5, 3, 2, 5)
            tile_rect(11, 9, 3, 2, 7)
            line(img, [(6, 6), (6, 9), (5, 10)], color(5))
    else:
        clustered(
            [
                (0, 1, 4, 2, 1),
                (5, 1, 3, 3, 2),
                (10, 3, 4, 2, 3),
                (1, 7, 4, 3, 3),
                (7, 8, 3, 2, 1),
                (12, 10, 4, 3, 2),
                (3, 13, 4, 2, 4),
            ]
        )

    add_corner_light()


def draw_pod_hull_block(img: Image.Image, spec: TextureSpec) -> None:
    draw = ImageDraw.Draw(img)
    base = hex_to_rgba("#5c665f")
    mid = hex_to_rgba("#747c72")
    light = hex_to_rgba("#9a9d8f")
    dark = hex_to_rgba("#424c49")
    deep = hex_to_rgba("#273230")
    rust = hex_to_rgba("#a36a3c")
    seal = hex_to_rgba("#303b39")
    img.paste(base, (0, 0, 16, 16))
    rect(draw, (0, 0, 15, 1), light)
    rect(draw, (0, 0, 1, 15), light)
    rect(draw, (14, 2, 15, 15), deep)
    rect(draw, (2, 14, 15, 15), deep)
    line(img, [(0, 7), (15, 7)], dark)
    line(img, [(7, 0), (7, 15)], dark)
    rect(draw, (2, 2, 5, 4), mid)
    rect(draw, (9, 2, 13, 4), dark)
    rect(draw, (2, 9, 5, 12), dark)
    rect(draw, (10, 10, 13, 12), mid)
    rect(draw, (3, 3, 4, 3), light)
    rect(draw, (11, 3, 12, 3), seal)
    rect(draw, (3, 11, 4, 11), seal)
    rect(draw, (11, 11, 12, 11), light)
    rect(draw, (14, 5, 15, 6), rust)
    rect(draw, (5, 14, 6, 15), rust)
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        put(img, x, y, seal)


def draw_pod_glass_block(img: Image.Image, spec: TextureSpec) -> None:
    draw = ImageDraw.Draw(img)
    img.paste((0, 0, 0, 0), (0, 0, 16, 16))
    edge = (103, 124, 128, 150)
    pane = (74, 103, 112, 76)
    pane_dark = (36, 50, 56, 95)
    shine = (199, 229, 232, 170)
    mid = (124, 154, 158, 115)
    shadow = (25, 34, 39, 135)
    rect(draw, (0, 0, 15, 15), edge)
    rect(draw, (1, 1, 14, 14), pane_dark)
    rect(draw, (2, 2, 13, 13), pane)
    rect(draw, (1, 1, 5, 2), shine)
    rect(draw, (1, 1, 2, 6), shine)
    rect(draw, (12, 10, 14, 14), shadow)
    line(img, [(4, 12), (12, 4)], mid)
    line(img, [(5, 13), (13, 5)], shadow)
    line(img, [(3, 5), (6, 2)], shine)
    rect(draw, (7, 1, 8, 14), edge)
    rect(draw, (1, 7, 14, 8), edge)
    put(img, 4, 4, shine)
    put(img, 11, 11, shadow)


def draw_scattered_bones_block(img: Image.Image, spec: TextureSpec) -> None:
    draw = ImageDraw.Draw(img)
    base = hex_to_rgba("#8b8678")
    mid = hex_to_rgba("#b8ad91")
    light = hex_to_rgba("#d2c6a6")
    dark = hex_to_rgba("#5b554b")
    deep = hex_to_rgba("#342f2a")
    img.paste(base, (0, 0, 16, 16))
    for xy in ((2, 3, 7, 4), (9, 9, 14, 10), (5, 12, 7, 14)):
        rect(draw, xy, mid)
    for x, y in ((3, 3), (6, 4), (10, 9), (13, 10), (6, 12)):
        put(img, x, y, light)
    line(img, [(1, 7), (5, 7), (6, 8)], dark)
    line(img, [(8, 2), (10, 4), (13, 4)], dark)
    line(img, [(9, 13), (12, 13), (14, 14)], deep)
    rect(draw, (0, 0, 15, 0), hex_to_rgba("#9a9078"))
    rect(draw, (0, 15, 15, 15), deep)


def draw_concrete_like_block(img: Image.Image, spec: TextureSpec) -> None:
    draw = ImageDraw.Draw(img)
    n = spec.name.lower()
    profiles: dict[str, tuple[tuple[str, str, str, str, str, str], list[tuple[tuple[int, int, int, int], int]], list[list[tuple[int, int]]]]] = {
        "debris_block": (
            ("#74796f", "#83887d", "#a2a193", "#596059", "#333b37", "#765640"),
            [((1, 2, 3, 3), 2), ((7, 1, 8, 2), 1), ((11, 5, 13, 6), 3), ((3, 10, 5, 11), 5), ((9, 13, 11, 14), 1)],
            [[(1, 13), (4, 13), (4, 14)], [(10, 8), (12, 8), (12, 9)]],
        ),
        "concrete_rubble": (
            ("#6d736d", "#7f847b", "#99998e", "#525a55", "#303735", "#8a7155"),
            [((2, 1, 4, 1), 2), ((6, 5, 7, 6), 1), ((12, 2, 13, 4), 3), ((1, 9, 3, 10), 3), ((8, 12, 10, 12), 5)],
            [[(5, 14), (7, 14), (7, 15)], [(11, 7), (13, 7)]],
        ),
        "concrete_chunk": (
            ("#7a7f76", "#8c8f84", "#aaa79a", "#5a615b", "#353d39", "#6d6654"),
            [((0, 3, 2, 4), 1), ((5, 2, 6, 3), 2), ((10, 5, 12, 6), 3), ((4, 11, 6, 13), 3), ((12, 12, 14, 13), 1)],
            [[(2, 8), (4, 8), (4, 9)], [(9, 1), (9, 3), (10, 3)]],
        ),
        "rubble": (
            ("#696d67", "#7d7d73", "#969181", "#4d5550", "#2c3431", "#7a5f45"),
            [((1, 4, 3, 5), 5), ((6, 1, 8, 2), 1), ((11, 8, 13, 9), 3), ((5, 12, 7, 14), 3), ((13, 2, 14, 3), 2)],
            [[(1, 11), (3, 11), (3, 12)], [(9, 6), (10, 6), (10, 7)]],
        ),
        "wasteland_trace_rubble": (
            ("#70766d", "#83877a", "#9d9b8d", "#535c55", "#323a36", "#6d7450"),
            [((2, 2, 4, 3), 2), ((8, 3, 9, 4), 5), ((12, 7, 14, 8), 3), ((3, 12, 5, 13), 1), ((10, 13, 11, 14), 5)],
            [[(4, 8), (6, 8), (6, 9)], [(12, 1), (12, 3)]],
        ),
        "industrial_aggregate": (
            ("#686f68", "#7a8178", "#9c998b", "#4c5650", "#2b3431", "#8b8063"),
            [((1, 1, 3, 2), 2), ((6, 6, 8, 7), 1), ((11, 2, 13, 3), 4), ((2, 11, 4, 12), 3), ((10, 10, 12, 11), 5)],
            [[(5, 14), (7, 14)], [(13, 6), (14, 6), (14, 7)]],
        ),
    }
    palette, marks, cracks = profiles.get(n, profiles["debris_block"])
    base, mid, light, dark, deep, warm = [hex_to_rgba(c) for c in palette]
    img.paste(base, (0, 0, 16, 16))
    colors = [base, mid, light, dark, deep, warm]
    for xy, color_index in marks:
        rect(draw, xy, colors[color_index])
    for points in cracks:
        line(img, points, deep)
    put(img, 1, 1, light)
    put(img, 14, 14, deep)


def draw_dirt_like_block(img: Image.Image, spec: TextureSpec) -> None:
    draw = ImageDraw.Draw(img)
    n = spec.name.lower()
    profiles: dict[str, tuple[tuple[str, str, str, str, str, str, str, str], list[tuple[tuple[int, int, int, int], int]], list[tuple[int, int, int]]]] = {
        "wasteland_dirt": (
            ("#71543a", "#84603f", "#9d7650", "#533b2b", "#342820", "#637044", "#7e8d4a", "#584060"),
            [((1, 2, 2, 3), 2), ((5, 5, 7, 6), 1), ((11, 2, 12, 3), 3), ((2, 10, 4, 11), 3), ((9, 12, 11, 13), 1), ((13, 8, 14, 8), 2)],
            [],
        ),
        "ashen_wasteland_dirt": (
            ("#675d50", "#776a58", "#978668", "#4b4339", "#302b27", "#596343", "#718345", "#594563"),
            [((1, 1, 3, 2), 2), ((6, 4, 7, 5), 1), ((11, 6, 13, 7), 3), ((3, 12, 5, 13), 1), ((9, 1, 10, 2), 4), ((13, 13, 14, 14), 2)],
            [],
        ),
        "burnt_wasteland_soil": (
            ("#5b4030", "#6e4b34", "#8a603f", "#3d2c24", "#241f1d", "#50583a", "#6f7b3e", "#52385f"),
            [((2, 2, 4, 3), 2), ((7, 5, 8, 6), 1), ((12, 2, 13, 3), 4), ((1, 11, 3, 12), 3), ((9, 12, 10, 14), 1), ((12, 9, 14, 9), 3)],
            [],
        ),
        "wasteland_grass_block": (
            ("#5d653e", "#6b7446", "#879055", "#454d36", "#2e3328", "#7c8e4d", "#9aaa58", "#5a4660"),
            [((1, 1, 3, 2), 5), ((6, 4, 7, 5), 1), ((11, 2, 13, 3), 3), ((2, 10, 4, 11), 3), ((8, 12, 10, 13), 1), ((13, 8, 14, 9), 2)],
            [(5, 1, 5), (12, 13, 5)],
        ),
        "mutated_wasteland_grass_block": (
            ("#64633e", "#766f46", "#918351", "#464835", "#2d3328", "#6c8746", "#93b84f", "#724581"),
            [((1, 2, 2, 3), 5), ((5, 4, 7, 5), 1), ((11, 1, 13, 2), 7), ((3, 10, 4, 11), 3), ((8, 13, 10, 14), 1), ((13, 7, 14, 8), 2)],
            [(6, 8, 6), (2, 13, 5)],
        ),
        "toxic_wasteland_grass_block": (
            ("#555840", "#666447", "#807851", "#3e4231", "#2b3027", "#6f8546", "#91ba4e", "#673f75"),
            [((2, 1, 3, 2), 5), ((6, 5, 7, 6), 1), ((10, 2, 12, 3), 3), ((1, 10, 3, 11), 3), ((8, 12, 10, 13), 1), ((13, 7, 14, 8), 7)],
            [(6, 8, 6), (12, 13, 6)],
        ),
        "contaminated_soil": (
            ("#55533c", "#696149", "#837351", "#3e402f", "#292f27", "#647946", "#88ad49", "#634070"),
            [((1, 2, 3, 3), 2), ((6, 5, 7, 6), 1), ((11, 2, 12, 3), 3), ((2, 10, 4, 11), 3), ((9, 13, 11, 14), 1), ((13, 8, 14, 8), 7)],
            [(6, 7, 6)],
        ),
        "irradiated_crust": (
            ("#4f5139", "#626044", "#7d744f", "#393f2e", "#252d25", "#657f43", "#91bd4b", "#6e4380"),
            [((2, 1, 4, 2), 2), ((7, 4, 8, 5), 1), ((11, 7, 13, 8), 3), ((2, 12, 4, 13), 3), ((9, 1, 10, 2), 6), ((13, 13, 14, 14), 7)],
            [(5, 9, 6), (10, 11, 7)],
        ),
    }
    palette, marks, accents = profiles.get(n, profiles["wasteland_dirt"])
    base, mid, light, dark, deep, grass, toxic, purple = [hex_to_rgba(c) for c in palette]
    img.paste(base, (0, 0, 16, 16))
    colors = [base, mid, light, dark, deep, grass, toxic, purple]
    for xy, color_index in marks:
        rect(draw, xy, colors[color_index])
    for x, y, color_index in accents:
        put(img, x, y, colors[color_index])
    if "grass_block" in n:
        rect(draw, (0, 0, 15, 0), grass)
        rect(draw, (2, 1, 4, 1), grass)
        rect(draw, (10, 1, 13, 1), grass)
    put(img, 14, 14, deep)


def draw_rusted_panel_block(img: Image.Image, spec: TextureSpec) -> None:
    draw = ImageDraw.Draw(img)
    base = hex_to_rgba("#743f26")
    mid = hex_to_rgba("#8d522f")
    light = hex_to_rgba("#b36c3c")
    dark = hex_to_rgba("#4b2d22")
    deep = hex_to_rgba("#282321")
    img.paste(base, (0, 0, 16, 16))
    if spec.name.lower().endswith("debris"):
        rect(draw, (0, 0, 15, 1), mid)
        rect(draw, (0, 0, 1, 15), mid)
        rect(draw, (2, 3, 6, 5), dark)
        rect(draw, (8, 2, 13, 4), mid)
        rect(draw, (10, 8, 14, 11), dark)
        rect(draw, (1, 11, 5, 14), mid)
        line(img, [(0, 8), (4, 8), (5, 9), (11, 9), (12, 10), (15, 10)], deep)
        line(img, [(8, 0), (8, 5), (9, 5), (9, 15)], dark)
        for x, y in [(3, 4), (12, 3), (2, 12), (11, 9)]:
            put(img, x, y, light)
    else:
        rect(draw, (0, 0, 15, 1), mid)
        rect(draw, (0, 0, 1, 15), mid)
        rect(draw, (15, 2, 15, 15), deep)
        rect(draw, (2, 15, 15, 15), deep)
        line(img, [(0, 7), (15, 7)], dark)
        rect(draw, (2, 2, 5, 5), mid)
        rect(draw, (9, 2, 13, 4), base)
        rect(draw, (2, 9, 6, 13), dark)
        rect(draw, (10, 10, 13, 13), mid)
        for x, y in [(3, 2), (12, 3), (5, 12), (11, 11)]:
            put(img, x, y, light)
    put(img, 14, 14, deep)


def draw_nexus_core_block(img: Image.Image, spec: TextureSpec) -> None:
    draw = ImageDraw.Draw(img)
    n = spec.name.lower()
    active = "_active" in n or n.endswith("_active")
    base = hex_to_rgba("#34284e")
    plate = hex_to_rgba("#493a6d")
    light = hex_to_rgba("#7e67ab")
    deep = hex_to_rgba("#191429")
    shadow = hex_to_rgba("#271f40")
    cyan = hex_to_rgba("#62d2eb" if active else "#4aa9c3")
    glow = hex_to_rgba("#b7f4ff" if active else "#8fd8e5")
    img.paste(deep, (0, 0, 16, 16))
    rect(draw, (1, 1, 14, 14), shadow)
    rect(draw, (2, 2, 13, 13), plate)
    rect(draw, (2, 2, 13, 3), light)
    rect(draw, (2, 2, 3, 13), light)
    rect(draw, (12, 4, 13, 13), deep)
    rect(draw, (4, 12, 13, 13), deep)

    if n.endswith("_bottom"):
        rect(draw, (4, 4, 11, 11), deep)
        for x in (5, 8, 11):
            rect(draw, (x, 4, x, 11), shadow)
        rect(draw, (5, 5, 10, 6), plate)
        put(img, 7, 9, cyan)
        put(img, 10, 9, cyan if active else shadow)
    elif n.endswith("_side"):
        rect(draw, (3, 5, 12, 6), deep)
        rect(draw, (3, 9, 12, 10), deep)
        rect(draw, (4, 5, 10, 5), cyan)
        rect(draw, (6, 9, 12, 9), cyan if active else plate)
        rect(draw, (11, 4, 12, 7), glow if active else light)
        put(img, 4, 10, glow)
    elif n.endswith("_top"):
        rect(draw, (4, 4, 11, 11), deep)
        rect(draw, (5, 5, 10, 10), plate)
        rect(draw, (6, 6, 9, 9), cyan)
        rect(draw, (7, 7, 8, 8), deep)
        put(img, 5, 5, glow)
        put(img, 10, 10, glow if active else shadow)
    elif n in {"nexus_core", "nexus_core_active"}:
        rect(draw, (3, 3, 12, 12), deep)
        draw_poly(draw, [(8, 4), (12, 8), (8, 12), (4, 8)], plate)
        draw_poly(draw, [(8, 5), (11, 8), (8, 11), (5, 8)], cyan)
        rect(draw, (7, 7, 8, 8), glow if active else shadow)
        put(img, 4, 4, light)
        put(img, 11, 11, glow if active else deep)
    else:
        rect(draw, (4, 3, 11, 12), deep)
        rect(draw, (5, 4, 10, 11), cyan)
        rect(draw, (6, 5, 9, 10), shadow)
        rect(draw, (7, 6, 8, 9), glow)
        line(img, [(3, 8), (5, 8)], cyan)
        line(img, [(10, 8), (12, 8)], cyan)
        if active:
            put(img, 6, 4, glow)
            put(img, 9, 11, glow)


def draw_named_block(img: Image.Image, spec: TextureSpec) -> bool:
    n = spec.name.lower()
    if n.startswith("nexus_core"):
        draw_nexus_core_block(img, spec)
        return True
    if n == "drop_pod_hull":
        draw_pod_hull_block(img, spec)
        return True
    if n == "drop_pod_glass":
        draw_pod_glass_block(img, spec)
        return True
    if n == "scattered_bones":
        draw_scattered_bones_block(img, spec)
        return True
    if n in {"debris_block", "concrete_rubble", "concrete_chunk", "rubble", "wasteland_trace_rubble", "industrial_aggregate"}:
        draw_cluster_tile(img, spec)
        return True
    if n in {"wasteland_dirt", "wasteland_grass_block", "ashen_wasteland_dirt", "burnt_wasteland_soil", "contaminated_soil", "toxic_wasteland_grass_block", "mutated_wasteland_grass_block", "irradiated_crust"}:
        draw_cluster_tile(img, spec)
        return True
    if n in {"rusted_metal_sheet", "rusted_metal_debris"}:
        draw_rusted_panel_block(img, spec)
        return True
    return False


def draw_machine_block(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    light, mid, plate, shadow, deep = colors[0], colors[1], colors[2], colors[3], colors[4]
    accent = accent_for_machine(spec.name)
    glow = colors[min(6, len(colors) - 1)]
    alert = colors[5]
    status = alert if "error" in spec.name else (glow if "active" in spec.name or "repaired" in spec.name else accent)
    role = spec.machine_role or machine_role_for(spec.name)
    n = spec.name

    img.paste(deep, (0, 0, 16, 16))
    rect(draw, (1, 1, 14, 14), shadow)
    rect(draw, (2, 2, 13, 13), plate)
    rect(draw, (2, 2, 13, 3), light)
    rect(draw, (2, 2, 3, 13), light)
    rect(draw, (12, 4, 13, 13), deep)
    rect(draw, (4, 12, 13, 13), deep)
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        put(img, x, y, mid)
    rect(draw, (3, 3, 5, 3), light)
    rect(draw, (3, 3, 3, 5), light)
    rect(draw, (10, 12, 12, 12), deep)
    rect(draw, (12, 10, 12, 12), deep)
    for x, y in [(4, 4), (11, 4), (4, 11), (11, 11)]:
        put(img, x, y, shadow)

    face = "front"
    if n.endswith("_top"):
        face = "top"
    elif n.endswith("_bottom"):
        face = "bottom"
    elif n.endswith("_side"):
        face = "side"
    elif n.endswith("_core"):
        face = "core"

    def mark_machine_identity() -> None:
        marker = stable_rng(f"machine-id:{machine_base(spec.name)}")
        x = 2 + marker.randrange(0, 3)
        y = 11 + marker.randrange(0, 3)
        rect(draw, (x, y, x + 1, y), accent)
        x = 10 + marker.randrange(0, 3)
        y = 2 + marker.randrange(0, 3)
        rect(draw, (x, y, x + 1, y), light)
        if marker.random() < 0.65:
            x = 4 + marker.randrange(0, 7)
            y = 4 + marker.randrange(0, 7)
            rect(draw, (x, y, x + 1, y), status)

    if role in {"pipe", "cable"}:
        if role == "pipe":
            rect(draw, (0, 6, 15, 10), deep)
            rect(draw, (0, 7, 15, 9), mid)
            rect(draw, (5, 4, 10, 12), deep)
            rect(draw, (6, 5, 9, 11), plate)
            rect(draw, (7, 6, 8, 10), status)
        else:
            if "high_voltage" in n:
                rect(draw, (0, 6, 15, 7), deep)
                rect(draw, (0, 9, 15, 10), deep)
                rect(draw, (0, 7, 15, 7), status)
                rect(draw, (0, 9, 15, 9), status)
                rect(draw, (6, 4, 9, 12), shadow)
                rect(draw, (7, 5, 8, 11), plate)
                draw_poly(draw, [(8, 5), (6, 8), (8, 8), (7, 11), (10, 7), (8, 7)], glow)
            elif "reinforced" in n:
                rect(draw, (0, 7, 15, 9), deep)
                rect(draw, (0, 8, 15, 8), status)
                rect(draw, (3, 4, 12, 12), shadow)
                rect(draw, (4, 5, 11, 11), plate)
                rect(draw, (5, 6, 10, 10), deep)
                rect(draw, (6, 7, 9, 9), mid)
                for x, y in [(3, 4), (12, 4), (3, 12), (12, 12)]:
                    put(img, x, y, light)
            else:
                rect(draw, (0, 7, 15, 9), deep)
                rect(draw, (0, 8, 15, 8), status)
                rect(draw, (4, 5, 11, 11), shadow)
                rect(draw, (6, 6, 9, 10), plate)
                put(img, 7, 8, glow)
        mark_machine_identity()
        return

    if face == "bottom":
        rect(draw, (4, 4, 11, 11), deep)
        rect(draw, (5, 5, 10, 10), shadow)
        for x in (5, 8, 11):
            rect(draw, (x, 4, x, 11), mid)
        put(img, 12, 12, status)
        mark_machine_identity()
        return

    if face == "side":
        if role in {"battery", "power_node", "generator", "meter", "distributor", "capacitor"}:
            for y in (4, 7, 10):
                rect(draw, (4, y, 11, y + 1), deep)
                rect(draw, (5, y, 10, y), status)
            if role == "distributor":
                rect(draw, (7, 3, 8, 12), shadow)
                rect(draw, (4, 7, 11, 8), status)
            elif role == "capacitor":
                rect(draw, (5, 4, 10, 11), deep)
                rect(draw, (6, 5, 9, 10), status)
                rect(draw, (7, 6, 8, 9), glow)
            elif role == "meter":
                rect(draw, (5, 5, 10, 9), shadow)
                rect(draw, (6, 6, 9, 7), status)
        elif role in {"purifier", "collector", "barrel", "condenser"}:
            rect(draw, (5, 3, 10, 12), deep)
            rect(draw, (6, 4, 9, 11), status)
            rect(draw, (5, 6, 10, 7), shadow)
        elif role in {"press", "grinder", "miner", "hopper", "recycler"}:
            rect(draw, (4, 5, 11, 10), deep)
            rect(draw, (5, 6, 10, 9), mid)
            rect(draw, (6, 7, 9, 8), shadow)
        else:
            rect(draw, (4, 4, 11, 11), shadow)
            rect(draw, (5, 5, 10, 6), light)
            rect(draw, (5, 9, 10, 10), deep)
            put(img, 12, 4, status)
        mark_machine_identity()
        return

    if face == "top":
        if role in {"reactor", "synthesizer", "refiner", "power_node", "capacitor", "distributor", "meter"}:
            rect(draw, (4, 4, 11, 11), deep)
            rect(draw, (5, 5, 10, 10), status)
            rect(draw, (6, 6, 9, 9), shadow)
            if role == "synthesizer":
                line(img, [(5, 10), (10, 5)], glow)
            elif role == "refiner":
                rect(draw, (7, 5, 8, 10), glow)
            elif role == "power_node":
                rect(draw, (6, 7, 9, 8), glow)
            elif role == "capacitor":
                draw_poly(draw, [(8, 5), (11, 8), (8, 11), (5, 8)], glow)
            elif role == "distributor":
                rect(draw, (7, 4, 8, 11), glow)
                rect(draw, (4, 7, 11, 8), glow)
            elif role == "meter":
                rect(draw, (6, 5, 10, 7), light)
                put(img, 9, 9, glow)
            else:
                rect(draw, (7, 7, 8, 8), glow)
        elif role in {"battery", "generator", "array"}:
            for x in (4, 7, 10):
                rect(draw, (x, 4, x + 1, 11), deep)
                rect(draw, (x, 5, x + 1, 9), status)
        elif role in {"purifier", "collector", "scrubber", "condenser", "cleanser", "filter_bench"}:
            rect(draw, (4, 3, 11, 12), deep)
            rect(draw, (5, 4, 10, 11), status)
            rect(draw, (6, 5, 9, 7), light)
        elif role in {"press", "grinder", "miner"}:
            draw_tiny_gear(img, 8, 8, mid, deep)
            put(img, 8, 5, status)
        else:
            rect(draw, (4, 4, 11, 11), mid)
            rect(draw, (5, 5, 10, 6), light)
            put(img, 8, 8, status)
        mark_machine_identity()
        return

    if role == "meter":
        rect(draw, (3, 3, 12, 8), deep)
        rect(draw, (4, 4, 11, 7), shadow)
        rect(draw, (5, 5, 10, 5), status)
        line(img, [(5, 7), (8, 5), (11, 7)], glow)
        rect(draw, (4, 10, 11, 12), deep)
        rect(draw, (5, 11, 6, 11), status)
        rect(draw, (8, 11, 9, 11), light)
        rect(draw, (11, 11, 11, 11), alert)
    elif role == "distributor":
        rect(draw, (7, 2, 8, 13), deep)
        rect(draw, (3, 7, 12, 8), deep)
        rect(draw, (7, 3, 8, 12), status)
        rect(draw, (4, 7, 11, 8), status)
        rect(draw, (5, 4, 6, 5), shadow)
        rect(draw, (10, 10, 11, 11), shadow)
        put(img, 8, 8, glow)
    elif role == "capacitor":
        rect(draw, (4, 3, 11, 12), deep)
        rect(draw, (5, 4, 10, 11), shadow)
        draw_poly(draw, [(8, 4), (11, 7), (10, 11), (6, 11), (5, 7)], status)
        rect(draw, (7, 6, 8, 9), glow)
        put(img, 5, 5, light)
        put(img, 10, 10, alert)
    elif role == "relay":
        rect(draw, (4, 3, 11, 8), deep)
        rect(draw, (5, 4, 10, 7), status)
        line(img, [(8, 3), (8, 1)], deep)
        put(img, 7, 2, glow)
        put(img, 9, 2, glow)
        rect(draw, (5, 11, 11, 12), shadow)
    elif role == "scanner":
        rect(draw, (4, 3, 11, 10), deep)
        rect(draw, (5, 4, 10, 9), shadow)
        line(img, [(5, 8), (10, 4)], status)
        put(img, 8, 6, glow)
        rect(draw, (5, 12, 10, 12), mid)
    elif role == "terminal":
        rect(draw, (4, 3, 11, 8), deep)
        rect(draw, (5, 4, 10, 7), status)
        rect(draw, (5, 10, 11, 11), shadow)
        put(img, 6, 11, glow)
        put(img, 10, 11, light)
    elif role == "terminal_block":
        rect(draw, (4, 4, 11, 11), deep)
        rect(draw, (5, 5, 10, 10), shadow)
        rect(draw, (6, 6, 9, 7), status)
        rect(draw, (7, 9, 8, 10), glow)
    elif role == "controller":
        rect(draw, (3, 3, 12, 9), deep)
        rect(draw, (4, 4, 11, 8), status)
        rect(draw, (5, 11, 6, 12), glow)
        rect(draw, (9, 11, 10, 12), alert)
    elif role in {"purifier", "collector"}:
        rect(draw, (4, 3, 7, 12), deep)
        rect(draw, (5, 4, 6, 11), status)
        draw_tiny_drop(img, 11, 4, status, light)
        rect(draw, (9, 12, 12, 12), mid)
    elif role == "battery":
        for x in (4, 7, 10):
            rect(draw, (x, 3, x + 1, 12), deep)
            rect(draw, (x, 4, x + 1, 10), status)
        rect(draw, (4, 12, 11, 12), shadow)
    elif role == "scrubber":
        rect(draw, (4, 3, 11, 11), deep)
        for y in (4, 6, 8, 10):
            rect(draw, (5, y, 10, y), mid if y % 4 else light)
        put(img, 12, 4, status)
    elif role == "condenser":
        rect(draw, (4, 3, 11, 11), deep)
        rect(draw, (5, 4, 10, 5), status)
        for x in (5, 7, 9):
            rect(draw, (x, 6, x + 1, 10), mid)
        put(img, 11, 11, glow)
    elif role == "cleanser":
        rect(draw, (4, 3, 11, 11), deep)
        rect(draw, (5, 5, 10, 9), shadow)
        rect(draw, (7, 4, 8, 10), status)
        rect(draw, (5, 6, 10, 7), status)
        put(img, 11, 10, glow)
    elif role == "filter_bench":
        rect(draw, (4, 3, 11, 9), deep)
        for x in (5, 7, 9):
            rect(draw, (x, 4, x, 8), mid)
        rect(draw, (5, 11, 10, 12), status)
    elif role == "press":
        rect(draw, (4, 3, 11, 5), deep)
        rect(draw, (4, 10, 11, 12), deep)
        rect(draw, (5, 6, 10, 9), shadow)
        rect(draw, (6, 7, 9, 8), status)
    elif role == "grinder":
        draw_tiny_gear(img, 6, 6, mid, deep)
        draw_tiny_gear(img, 10, 10, status, shadow)
    elif role == "miner":
        rect(draw, (5, 3, 10, 6), deep)
        draw_poly(draw, [(5, 7), (10, 7), (8, 13)], status)
        line(img, [(8, 8), (8, 12)], deep)
    elif role == "hopper":
        draw_poly(draw, [(4, 3), (12, 3), (10, 8), (9, 12), (7, 12), (6, 8)], deep)
        rect(draw, (6, 4, 10, 6), mid)
        rect(draw, (7, 9, 8, 12), status)
    elif role == "burner":
        rect(draw, (4, 3, 11, 8), deep)
        draw_poly(draw, [(6, 11), (8, 7), (10, 11)], status)
        rect(draw, (5, 5, 10, 6), glow)
    elif role == "generator":
        rect(draw, (4, 3, 11, 11), deep)
        rect(draw, (5, 4, 10, 6), status)
        draw_tiny_gear(img, 8, 10, mid, deep)
    elif role == "array":
        rect(draw, (4, 3, 11, 8), deep)
        for x in (5, 7, 9):
            rect(draw, (x, 4, x, 10), status)
    elif role == "power_node":
        rect(draw, (4, 3, 11, 12), deep)
        draw_poly(draw, [(8, 4), (11, 7), (8, 11), (5, 7)], status)
        rect(draw, (7, 7, 8, 8), glow)
    elif role == "synthesizer":
        rect(draw, (4, 3, 11, 12), deep)
        rect(draw, (5, 4, 10, 11), shadow)
        line(img, [(5, 10), (10, 5)], status)
        line(img, [(5, 5), (10, 10)], glow)
    elif role == "refiner":
        rect(draw, (4, 3, 11, 12), deep)
        for x in (5, 8):
            rect(draw, (x, 4, x + 1, 11), status)
        rect(draw, (9, 6, 10, 8), glow)
    elif role == "reactor":
        rect(draw, (4, 3, 11, 12), deep)
        rect(draw, (5, 4, 10, 11), status)
        rect(draw, (6, 5, 9, 10), shadow)
        rect(draw, (7, 6, 8, 9), glow)
    elif role in {"bio_lab", "bio_pod"}:
        rect(draw, (5, 3, 10, 12), deep)
        rect(draw, (6, 4, 9, 11), status)
        put(img, 7, 6, glow)
        put(img, 9, 9, alert)
    elif role == "barrel":
        rect(draw, (5, 2, 10, 13), deep)
        rect(draw, (6, 3, 9, 12), status)
        rect(draw, (5, 5, 10, 6), shadow)
        rect(draw, (5, 10, 10, 11), shadow)
    elif role == "crate":
        rect(draw, (4, 4, 11, 11), shadow)
        rect(draw, (5, 5, 10, 10), accent)
        line(img, [(5, 5), (10, 10)], deep)
        line(img, [(10, 5), (5, 10)], deep)
    elif role == "map_table":
        rect(draw, (3, 4, 12, 11), deep)
        rect(draw, (4, 5, 11, 10), accent)
        line(img, [(5, 7), (8, 6), (11, 8)], shadow)
    elif role == "rack":
        for x in (4, 8, 12):
            rect(draw, (x, 3, x, 12), deep)
        rect(draw, (5, 6, 11, 7), accent)
        rect(draw, (5, 10, 11, 10), mid)
    elif role == "med":
        rect(draw, (4, 4, 11, 11), deep)
        rect(draw, (5, 5, 10, 10), plate)
        rect(draw, (7, 5, 8, 10), status)
        rect(draw, (5, 7, 10, 8), status)
    elif role == "lab":
        rect(draw, (5, 3, 10, 12), deep)
        rect(draw, (6, 4, 9, 7), light)
        rect(draw, (6, 8, 9, 11), status)
        put(img, 11, 5, glow)
    elif role == "counter":
        rect(draw, (3, 8, 12, 11), deep)
        rect(draw, (4, 5, 11, 7), mid)
        rect(draw, (8, 5, 11, 6), status)
        put(img, 5, 10, light)
    elif role == "workbench":
        rect(draw, (3, 8, 12, 11), deep)
        rect(draw, (4, 5, 11, 7), mid)
        line(img, [(5, 6), (8, 4), (11, 7)], status)
        put(img, 10, 10, light)
    elif role == "recycler":
        rect(draw, (3, 8, 12, 11), deep)
        rect(draw, (4, 5, 11, 7), mid)
        draw_tiny_gear(img, 8, 6, status, deep)
    else:
        rect(draw, (4, 3, 11, 8), deep)
        rect(draw, (5, 4, 10, 7), status)
        rect(draw, (5, 10, 11, 11), shadow)

    mark_machine_identity()


def draw_plant_block(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    stem = colors[min(2, len(colors) - 1)]
    mid = colors[1]
    light = colors[0]
    dark = colors[min(4, len(colors) - 1)]
    accent = colors[min(5, len(colors) - 1)]
    toxic = any(w in spec.name for w in ("toxic", "mutated", "nuclear", "irradiated"))

    if "cactus" in spec.name:
        rect(draw, (7, 3, 9, 14), mid)
        rect(draw, (7, 3, 7, 14), light)
        rect(draw, (9, 4, 9, 14), dark)
        for y in (4, 7, 11):
            put(img, 6, y, light)
        return

    if "reed" in spec.name:
        for x, top in [(5, 7), (8, 4), (11, 9)]:
            line(img, [(x, 15), (x, top)], stem)
            put(img, x + 1, top + 2, dark if "wasteland" in spec.name else mid)
            put(img, x, top, light)
        if toxic:
            put(img, 9, 10, accent)
        return

    if "fungus" in spec.name:
        rect(draw, (7, 9, 8, 14), stem)
        rect(draw, (5, 7, 10, 8), mid)
        rect(draw, (6, 6, 9, 6), light)
        put(img, 4, 7, dark)
        put(img, 11, 7, dark)
        put(img, 8, 6, accent)
        return

    if "fern" in spec.name:
        line(img, [(8, 15), (8, 6)], stem)
        for y, left, right in [(8, 5, 11), (10, 4, 12), (12, 6, 10), (14, 7, 9)]:
            line(img, [(8, y), (left, y - 1)], mid)
            if y != 14:
                line(img, [(8, y), (right, y - 1)], mid)
        put(img, 8, 6, light)
        return

    if "bush" in spec.name or "leaves" in spec.name:
        rect(draw, (3, 11, 5, 12), mid)
        rect(draw, (6, 8, 9, 10), light)
        rect(draw, (10, 11, 12, 12), dark)
        rect(draw, (5, 13, 11, 13), stem)
        put(img, 10, 8, accent if toxic else mid)
        rng = stable_rng(f"plant-id:{spec.name}")
        put(img, rng.choice([4, 7, 11]), rng.choice([8, 10, 12]), accent if rng.random() < 0.6 else light)
        return

    if "bottom" in spec.name:
        blade_sets = [
            [(4, 15), (4, 11), (5, 8)],
            [(8, 15), (8, 10), (9, 7)],
            [(12, 15), (11, 11), (12, 8)],
        ]
    elif "top" in spec.name or "tall_grass" in spec.name:
        blade_sets = [
            [(4, 15), (5, 10), (4, 5), (5, 2)],
            [(8, 15), (8, 9), (9, 4), (9, 1)],
            [(12, 15), (11, 11), (12, 7), (12, 4)],
        ]
    else:
        blade_sets = [
            [(4, 15), (4, 11), (5, 7)],
            [(8, 15), (8, 10), (9, 5)],
            [(12, 15), (11, 11), (12, 8)],
        ]
    for pts in blade_sets:
        line(img, pts, stem)
        for x, y in pts[1::2]:
            put(img, x, y, mid)
    for x, y in [(5, 8), (9, 6), (12, 9)]:
        put(img, x, y, light)
    if toxic:
        put(img, 6, 12, accent)
        put(img, 11, 7, accent)
    rng = stable_rng(f"plant-id:{spec.name}")
    x = rng.choice([3, 6, 9, 12])
    y = rng.choice([6, 9, 12])
    put(img, x, y, light if rng.random() < 0.55 else mid)
    if rng.random() < 0.45:
        put(img, max(0, x - 1), min(15, y + 1), (0, 0, 0, 0))


def draw_leaf_block(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    if "purple" in spec.name:
        base = hex_to_rgba("#4a3a5e")
        mid = hex_to_rgba("#5f4a79")
        light = hex_to_rgba("#8c6ac0")
        dark = hex_to_rgba("#2b2634")
        accent = hex_to_rgba("#a8d85c")
    elif "gray" in spec.name or "ash" in spec.name:
        base = hex_to_rgba("#5b6057")
        mid = hex_to_rgba("#73786d")
        light = hex_to_rgba("#a4aa99")
        dark = hex_to_rgba("#343932")
        accent = hex_to_rgba("#7ca35a")
    else:
        base = hex_to_rgba("#4f6a3d")
        mid = hex_to_rgba("#638348")
        light = hex_to_rgba("#93b867")
        dark = hex_to_rgba("#2f402d")
        accent = hex_to_rgba("#b06ad8") if "mutated" in spec.name else hex_to_rgba("#9fd35b")
    img.paste(base, (0, 0, 16, 16))
    clusters = [
        (1, 1, 3, 2, light),
        (5, 0, 8, 2, mid),
        (11, 2, 14, 3, dark),
        (2, 5, 5, 7, mid),
        (7, 5, 10, 8, light),
        (12, 7, 15, 9, mid),
        (0, 10, 3, 12, dark),
        (5, 11, 8, 14, mid),
        (10, 12, 13, 15, light),
    ]
    for x1, y1, x2, y2, color in clusters:
        rect(draw, (x1, y1, x2, y2), color)
    for x, y in [(4, 4), (9, 3), (13, 11), (6, 15)]:
        put(img, x, y, dark)
    if "mutated" in spec.name:
        put(img, 6, 6, accent)
        put(img, 11, 9, accent)


def draw_liquid_or_glass_block(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    if "glass" in spec.name:
        edge = (*colors[2][:3], 150)
        shine = (*colors[0][:3], 180)
        rect(draw, (0, 0, 15, 15), edge)
        rect(draw, (2, 2, 13, 13), (0, 0, 0, 35))
        line(img, [(2, 3), (5, 1), (9, 1)], shine)
        line(img, [(5, 13), (12, 6)], (*colors[1][:3], 120))
    elif "puddle" in spec.name:
        base = (*colors[2][:3], 205)
        img.paste((0, 0, 0, 0), (0, 0, 16, 16))
        draw_poly(draw, [(1, 7), (4, 4), (10, 4), (14, 7), (13, 12), (7, 14), (2, 12)], base)
        draw_poly(draw, [(3, 6), (7, 5), (11, 6), (10, 8), (5, 8)], (*colors[0][:3], 230))
        put(img, 12, 11, colors[min(4, len(colors) - 1)])
    else:
        img.paste(colors[2], (0, 0, 16, 16))
        for y in (2, 5, 8, 12):
            line(img, [(0, y), (3, y - 1), (7, y), (12, y - 1), (15, y)], colors[1])
        rect(draw, (3, 3, 5, 4), colors[0])
        rect(draw, (10, 10, 12, 11), colors[min(4, len(colors) - 1)])
        rect(draw, (6, 13, 7, 14), colors[min(5, len(colors) - 1)])
        rect(draw, (13, 4, 14, 5), colors[min(6, len(colors) - 1)])


def draw_block(spec: TextureSpec) -> Image.Image:
    img = transparent_image(16, 16) if spec.transparent else filled_image(16, 16, palette_rgba(spec)[1])
    if draw_named_block(img, spec):
        return img
    if is_cutout_plant(spec.name):
        draw_plant_block(img, spec)
    elif is_leaf_block(spec.name):
        draw_leaf_block(img, spec)
    elif any(w in spec.name for w in ("glass", "puddle", "sludge")):
        draw_liquid_or_glass_block(img, spec)
    elif spec.category in {"machine", "active_machine", "error_machine"}:
        draw_machine_block(img, spec)
    elif "log" in spec.name:
        draw_log_block(img, spec)
    else:
        draw_cluster_tile(img, spec)
    return img


def draw_log_block(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    if spec.name.endswith("_top"):
        img.paste(colors[2], (0, 0, 16, 16))
        rect(draw, (2, 2, 13, 13), colors[1])
        rect(draw, (4, 4, 11, 11), colors[2])
        rect(draw, (6, 6, 9, 9), colors[3])
        put(img, 5, 5, colors[0])
        put(img, 10, 10, colors[4])
    else:
        img.paste(colors[2], (0, 0, 16, 16))
        for x in (1, 4, 8, 12):
            rect(draw, (x, 0, min(15, x + 1), 15), colors[3])
        rect(draw, (0, 0, 15, 1), colors[0])
        rect(draw, (0, 14, 15, 15), colors[4])
        for y in (4, 9, 13):
            line(img, [(2, y), (7, y + 1), (13, y)], colors[1])


def draw_item_outline(draw: ImageDraw.ImageDraw, points: list[tuple[int, int]], color: RGBA) -> None:
    draw.polygon(points, fill=color)


def draw_spawn_egg(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    rng = stable_rng(spec.name)
    draw = ImageDraw.Draw(img)
    base_idx = rng.randrange(1, min(len(colors), 4))
    base = colors[base_idx]
    shade = colors[min(4, len(colors) - 1)]
    light = colors[0]
    accent = colors[min(5, len(colors) - 1)]
    outline = colors[min(4, len(colors) - 1)]
    shape = [(6, 1), (9, 1), (12, 4), (13, 8), (12, 12), (9, 15), (5, 15), (2, 12), (2, 7), (3, 4)]
    draw_item_outline(draw, shape, outline)
    inner = [(6, 2), (9, 2), (11, 5), (12, 8), (11, 12), (8, 14), (5, 14), (3, 11), (3, 7), (4, 4)]
    draw_item_outline(draw, inner, base)
    rect(draw, (5, 3, 7, 5), light)
    rect(draw, (9, 11, 11, 13), shade)
    n = spec.name
    if any(w in n for w in ("drone", "echo", "sentinel")):
        rect(draw, (6, 6, 9, 7), accent)
        put(img, 8, 9, colors[min(6, len(colors) - 1)])
        put(img, 10, 10, accent)
    elif any(w in n for w in ("boss", "matriarch", "colossus", "behemoth", "warlord", "overseer", "avatar")):
        rect(draw, (5, 6, 10, 6), accent)
        rect(draw, (5, 9, 10, 9), accent)
        put(img, 8, 11, colors[min(6, len(colors) - 1)])
    elif any(w in n for w in ("toxic", "slime", "crawler", "mutant", "irradiated")):
        for x, y in [(5, 6), (9, 5), (10, 9), (6, 11)]:
            rect(draw, (x, y, x + 1, y + 1), accent)
    elif any(w in n for w in ("ash", "wraith", "ghoul", "zombie")):
        for x, y in [(5, 7), (8, 5), (10, 10), (7, 12)]:
            put(img, x, y, accent)
    else:
        for x, y in [(5, 7), (9, 6), (7, 10), (10, 11)]:
            put(img, x + rng.randrange(0, 2), y, accent)


def draw_cartridge(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    metal = PALETTES["metal"]
    m = [hex_to_rgba(c) for c in metal]
    body = colors[2]
    glow = colors[min(5, len(colors) - 1)]
    outline = m[4]
    rect(draw, (5, 2, 10, 14), outline)
    rect(draw, (4, 3, 11, 5), outline)
    rect(draw, (5, 3, 10, 5), m[1])
    rect(draw, (5, 6, 10, 12), body)
    rect(draw, (5, 6, 6, 12), colors[0])
    rect(draw, (9, 7, 10, 12), colors[3])
    rect(draw, (4, 12, 11, 14), outline)
    rect(draw, (5, 12, 10, 13), m[2])
    if "advanced" in spec.name:
        rect(draw, (7, 7, 8, 10), glow)
    elif "elite" in spec.name:
        rect(draw, (6, 7, 9, 10), glow)
        put(img, 7, 6, colors[0])
    else:
        put(img, 8, 11, glow)


def draw_bottle(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    outline = colors[min(4, len(colors) - 1)]
    liquid = colors[2]
    cap = PALETTES["metal"]
    capc = hex_to_rgba(cap[3])
    rect(draw, (7, 1, 9, 3), capc)
    rect(draw, (5, 4, 11, 14), outline)
    rect(draw, (6, 5, 10, 13), (*liquid[:3], 210))
    rect(draw, (6, 5, 7, 7), colors[0])
    rect(draw, (9, 10, 10, 13), colors[3])
    if "dirty" in spec.name or "mutagen" in spec.name or "rad" in spec.name:
        put(img, 8, 10, colors[min(5, len(colors) - 1)])
        put(img, 9, 8, colors[min(5, len(colors) - 1)])


def draw_battery_item(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    outline = colors[min(4, len(colors) - 1)]
    shell = colors[2]
    cap = colors[1]
    light = colors[0]
    status = colors[min(6, len(colors) - 1)]
    accent = colors[min(5, len(colors) - 1)]
    if spec.name == "power_cell":
        rect(draw, (3, 6, 12, 10), outline)
        rect(draw, (4, 7, 11, 9), shell)
        rect(draw, (12, 7, 13, 9), cap)
        rect(draw, (5, 7, 8, 9), status)
        put(img, 5, 7, light)
        return
    if "advanced" in spec.name:
        rect(draw, (4, 3, 11, 13), outline)
        rect(draw, (5, 4, 10, 12), shell)
        rect(draw, (5, 4, 10, 5), cap)
        rect(draw, (6, 7, 9, 8), status)
        rect(draw, (6, 10, 9, 10), status)
        rect(draw, (5, 6, 5, 10), light)
        put(img, 10, 12, accent)
        return
    if "elite" in spec.name:
        rect(draw, (4, 2, 11, 14), outline)
        rect(draw, (5, 3, 10, 13), shell)
        rect(draw, (3, 5, 4, 11), outline)
        rect(draw, (11, 5, 12, 11), outline)
        for y in (5, 7, 9):
            rect(draw, (6, y, 9, y), status)
        rect(draw, (6, 3, 9, 4), light)
        put(img, 10, 12, accent)
        return
    rect(draw, (5, 2, 10, 14), outline)
    rect(draw, (6, 3, 9, 13), shell)
    rect(draw, (6, 3, 9, 4), cap)
    rect(draw, (6, 12, 9, 13), cap)
    bars = 2 if "energy" in spec.name else 1
    for i in range(bars):
        y = 6 + i * 2
        rect(draw, (7, y, 8, y), status)
    rect(draw, (6, 5, 6, 10), light)
    put(img, 9, 11, accent)


def draw_data_log_item(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    outline = colors[min(4, len(colors) - 1)]
    rng = stable_rng(spec.name)
    accent_options = ["#5cc8ff", "#9ceb44", "#d68cff", "#f2c75c", "#e46b55"]
    accent = hex_to_rgba(accent_options[rng.randrange(len(accent_options))])
    rect(draw, (3, 4, 12, 12), outline)
    rect(draw, (4, 5, 11, 11), hex_to_rgba("#31404a"))
    rect(draw, (4, 5, 5, 11), accent)
    line_y = 6 + rng.randrange(0, 3)
    rect(draw, (7, line_y, 10, line_y), colors[0])
    rect(draw, (7, 9, 11, 9), colors[1])
    if rng.random() < 0.5:
        rect(draw, (9, 10, 10, 10), accent)
    else:
        put(img, 10, 10, accent)


def draw_tool(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    metal = colors[1]
    light = colors[0]
    dark = colors[min(4, len(colors) - 1)]
    handle = hex_to_rgba("#5a3a25")
    if "hammer" in spec.name:
        rect(draw, (8, 2, 13, 6), dark)
        rect(draw, (7, 3, 12, 5), metal)
        line(img, [(7, 6), (2, 13)], handle)
        line(img, [(8, 6), (3, 14)], dark)
        put(img, 8, 3, light)
    elif "spear" in spec.name:
        line(img, [(3, 14), (11, 5)], handle)
        line(img, [(4, 14), (12, 5)], dark)
        draw_poly(draw, [(11, 1), (14, 4), (10, 6)], metal)
        put(img, 12, 3, light)
    else:
        line(img, [(3, 13), (12, 4)], dark)
        line(img, [(4, 13), (13, 4)], metal)
        line(img, [(5, 12), (12, 5)], light)
        rect(draw, (2, 12, 4, 14), handle)
        if "nexus" in spec.name:
            put(img, 11, 5, colors[min(5, len(colors) - 1)])


def draw_armor_item(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    outline = colors[min(4, len(colors) - 1)]
    plate = colors[2]
    light = colors[0]
    shadow = colors[3]
    if "helmet" in spec.name or "gas_mask" in spec.name:
        rect(draw, (4, 3, 11, 11), outline)
        rect(draw, (5, 4, 10, 10), plate)
        rect(draw, (5, 5, 10, 6), shadow if "gas_mask" in spec.name else light)
        if "gas_mask" in spec.name:
            rect(draw, (3, 8, 5, 11), outline)
            rect(draw, (10, 8, 12, 11), outline)
            put(img, 8, 10, colors[min(5, len(colors) - 1)])
    elif "chestplate" in spec.name:
        draw_poly(draw, [(4, 3), (11, 3), (13, 7), (11, 14), (4, 14), (2, 7)], outline)
        draw_poly(draw, [(5, 4), (10, 4), (11, 7), (10, 13), (5, 13), (4, 7)], plate)
        rect(draw, (5, 4, 10, 5), light)
    elif "leggings" in spec.name:
        rect(draw, (4, 3, 11, 14), outline)
        rect(draw, (5, 4, 7, 13), plate)
        rect(draw, (9, 4, 10, 13), plate)
        rect(draw, (5, 4, 10, 5), light)
    else:
        rect(draw, (4, 7, 7, 13), outline)
        rect(draw, (9, 7, 12, 13), outline)
        rect(draw, (5, 8, 7, 12), plate)
        rect(draw, (9, 8, 11, 12), plate)
        put(img, 5, 8, light)


def draw_material_item(img: Image.Image, spec: TextureSpec) -> None:
    colors = palette_rgba(spec)
    rng = stable_rng(spec.name)
    draw = ImageDraw.Draw(img)
    n = spec.name
    outline = colors[min(4, len(colors) - 1)]
    if "bandage" in n:
        rect(draw, (3, 6, 12, 10), outline)
        rect(draw, (4, 7, 11, 9), hex_to_rgba("#d9d2c2"))
        rect(draw, (7, 6, 8, 10), hex_to_rgba("#b8aa96"))
        put(img, 5, 8, hex_to_rgba("#f0e7d2"))
        put(img, 10, 8, hex_to_rgba("#8f806f"))
    elif "filtration_membrane" in n:
        rect(draw, (4, 3, 11, 12), outline)
        rect(draw, (5, 4, 10, 11), hex_to_rgba("#9ca29d"))
        for x in (6, 8, 10):
            line(img, [(x, 5), (x, 10)], hex_to_rgba("#394243"))
        for y in (6, 8, 10):
            line(img, [(5, y), (10, y)], hex_to_rgba("#d6d3c7"))
    elif "hand_warmer" in n:
        rect(draw, (4, 5, 11, 12), outline)
        rect(draw, (5, 6, 10, 11), hex_to_rgba("#8f7144"))
        rect(draw, (5, 6, 10, 7), hex_to_rgba("#d0b26b"))
        put(img, 8, 9, hex_to_rgba("#ec743b"))
    elif "thermal_liner" in n:
        draw_poly(draw, [(3, 6), (9, 3), (13, 6), (11, 12), (5, 13)], outline)
        draw_poly(draw, [(5, 7), (9, 5), (11, 7), (10, 11), (6, 11)], hex_to_rgba("#c9b27e"))
        line(img, [(6, 8), (10, 10)], hex_to_rgba("#ec743b"))
    elif "scout_drone_item" in n:
        rect(draw, (5, 5, 10, 10), outline)
        rect(draw, (6, 6, 9, 9), hex_to_rgba("#68706f"))
        rect(draw, (7, 7, 8, 8), hex_to_rgba("#65d4ff"))
        rect(draw, (2, 6, 4, 8), outline)
        rect(draw, (11, 6, 13, 8), outline)
        put(img, 3, 7, hex_to_rgba("#a0a8a2"))
        put(img, 12, 7, hex_to_rgba("#a0a8a2"))
    elif "rain_collector" in n:
        rect(draw, (4, 4, 11, 12), outline)
        rect(draw, (5, 5, 10, 11), hex_to_rgba("#68706f"))
        rect(draw, (5, 4, 10, 5), hex_to_rgba("#d6d3c7"))
        rect(draw, (6, 8, 9, 10), hex_to_rgba("#5fc2f2"))
        draw_tiny_drop(img, 8, 2, hex_to_rgba("#5fc2f2"), hex_to_rgba("#d9f3ff"))
    elif "prefall_archives_key" in n or n == "return_keystone":
        line(img, [(4, 11), (10, 5)], outline)
        line(img, [(5, 11), (11, 5)], hex_to_rgba("#d0b26b"))
        rect(draw, (9, 3, 12, 6), outline)
        rect(draw, (10, 4, 11, 5), hex_to_rgba("#f0e2bd"))
        put(img, 7, 9, hex_to_rgba("#5cc8ff"))
    elif "fiber_rope" in n:
        for off in (0, 1, 2):
            line(img, [(4 + off, 12), (6 + off, 8), (5 + off, 5), (9 + off, 3), (12, 6), (10, 10)], outline if off == 0 else hex_to_rgba("#8f7144"))
    elif "plant_fiber" in n:
        for x, top in [(4, 6), (7, 3), (10, 5), (12, 8)]:
            line(img, [(x, 14), (x + 1, top)], hex_to_rgba("#58653c"))
            put(img, x + 1, top, hex_to_rgba("#c7b56c"))
    elif "gold_trace" in n:
        for x, y in [(5, 8), (8, 6), (10, 11)]:
            rect(draw, (x, y, x + 1, y + 1), outline)
            put(img, x, y, hex_to_rgba("#f2d45c"))
            put(img, x + 1, y + 1, hex_to_rgba("#8f7144"))
    elif "gold_cluster" in n:
        draw_poly(draw, [(4, 10), (6, 6), (10, 5), (13, 8), (11, 12), (6, 13)], outline)
        draw_poly(draw, [(6, 10), (7, 7), (10, 6), (11, 8), (10, 11), (7, 12)], hex_to_rgba("#d0b26b"))
        put(img, 8, 7, hex_to_rgba("#f2d45c"))
        put(img, 11, 9, hex_to_rgba("#8f7144"))
    elif any(w in n for w in ("gem_fragment", "crystal_dust")):
        draw_poly(draw, [(7, 2), (11, 6), (9, 12), (4, 13), (3, 8)], outline)
        draw_poly(draw, [(7, 3), (10, 6), (8, 11), (5, 12), (4, 8)], hex_to_rgba("#75d7ff"))
        line(img, [(5, 8), (8, 4)], hex_to_rgba("#e9fbff"))
    elif "dense_alloy_chunk" in n:
        draw_poly(draw, [(4, 9), (6, 5), (11, 4), (13, 8), (10, 13), (5, 12)], outline)
        draw_poly(draw, [(6, 9), (7, 6), (10, 5), (11, 8), (9, 11), (6, 11)], hex_to_rgba("#9aa09e"))
        rect(draw, (7, 7, 10, 8), hex_to_rgba("#d8d8d0"))
    elif "scrap_iron_bundle" in n:
        rect(draw, (3, 6, 12, 11), outline)
        for y, c in [(6, "#9aa09e"), (8, "#69706e"), (10, "#9aa09e")]:
            rect(draw, (4, y, 11, y), hex_to_rgba(c))
        line(img, [(5, 5), (5, 12)], hex_to_rgba("#202626"))
        line(img, [(10, 5), (10, 12)], hex_to_rgba("#202626"))
    elif "iron_shard" in n:
        draw_poly(draw, [(4, 12), (6, 5), (12, 3), (11, 7), (8, 13)], outline)
        draw_poly(draw, [(6, 11), (7, 6), (10, 5), (10, 7), (8, 11)], hex_to_rgba("#9aa09e"))
        put(img, 7, 7, hex_to_rgba("#d8d8d0"))
    elif "scrap_metal" in n:
        draw_poly(draw, [(3, 10), (5, 5), (9, 7), (12, 4), (13, 10), (8, 13)], outline)
        draw_poly(draw, [(5, 10), (6, 7), (9, 8), (11, 6), (11, 10), (8, 12)], hex_to_rgba("#69706e"))
        put(img, 7, 8, hex_to_rgba("#d8d8d0"))
        put(img, 10, 10, hex_to_rgba("#a45b31"))
    elif "data_log" in n or "archives" in n:
        accent_options = ["#5cc8ff", "#9ceb44", "#d68cff", "#f2c75c", "#e46b55"]
        accent = hex_to_rgba(accent_options[stable_rng(n).randrange(len(accent_options))])
        rect(draw, (3, 4, 12, 12), outline)
        rect(draw, (4, 5, 11, 11), hex_to_rgba("#31404a"))
        rect(draw, (4, 5, 5, 11), accent)
        rect(draw, (7, 6, 10, 6), colors[0])
        rect(draw, (7, 8, 11, 8), colors[1])
        put(img, 10, 10, accent)
    elif any(w in n for w in ("circuit", "scanner", "schematic", "upgrade")):
        rect(draw, (3, 4, 12, 12), outline)
        rect(draw, (4, 5, 11, 11), colors[2])
        if "scanner" in n:
            rect(draw, (5, 6, 10, 8), colors[min(5, len(colors) - 1)])
            line(img, [(6, 10), (10, 10)], colors[0])
        elif "schematic" in n:
            rect(draw, (5, 6, 10, 10), hex_to_rgba("#f0e2bd"))
            line(img, [(6, 7), (10, 7)], colors[3])
            put(img, 6, 9, colors[min(5, len(colors) - 1)])
        elif "upgrade" in n:
            if "speed" in n:
                rect(draw, (5, 6, 10, 10), hex_to_rgba("#31404a"))
                draw_poly(draw, [(6, 7), (9, 7), (9, 6), (11, 8), (9, 10), (9, 9), (6, 9)], hex_to_rgba("#53d6e8"))
                put(img, 5, 10, hex_to_rgba("#9ceb44"))
            elif "overclock" in n:
                rect(draw, (5, 6, 10, 10), hex_to_rgba("#3b3434"))
                draw_poly(draw, [(8, 5), (6, 8), (8, 8), (7, 11), (10, 7), (8, 7)], hex_to_rgba("#f29a4a"))
                put(img, 10, 10, hex_to_rgba("#d94b3f"))
            elif "efficiency" in n:
                rect(draw, (5, 6, 10, 10), hex_to_rgba("#344232"))
                line(img, [(5, 9), (7, 11), (11, 6)], hex_to_rgba("#9ceb44"))
                rect(draw, (9, 9, 10, 10), hex_to_rgba("#53d6e8"))
            else:
                rect(draw, (5, 6, 10, 9), colors[1])
                put(img, 5, 10, colors[min(5, len(colors) - 1)])
                put(img, 8, 10, colors[min(6, len(colors) - 1)])
        else:
            rect(draw, (5, 6, 10, 9), colors[1])
            put(img, 5, 10, colors[min(5, len(colors) - 1)])
            put(img, 8, 10, colors[min(6, len(colors) - 1)])
    elif any(w in n for w in ("cell", "energy", "power")):
        if "power_cell" in n:
            rect(draw, (3, 6, 12, 10), outline)
            rect(draw, (4, 7, 11, 9), colors[2])
            rect(draw, (5, 7, 7, 9), colors[min(5, len(colors) - 1)])
        else:
            rect(draw, (5, 2, 10, 13), outline)
            rect(draw, (6, 3, 9, 12), colors[2])
            rect(draw, (6, 3, 9, 5), colors[0])
            put(img, 8, 8, colors[min(5, len(colors) - 1)])
            put(img, 8, 9, colors[min(6, len(colors) - 1)])
    elif any(w in n for w in ("dust", "ash")):
        for _ in range(11):
            x, y = rng.randrange(4, 12), rng.randrange(6, 13)
            rect(draw, (x, y, min(15, x + rng.randrange(1, 3)), y), colors[rng.randrange(1, min(4, len(colors)))])
        rect(draw, (4, 12, 11, 13), colors[min(3, len(colors) - 1)])
    elif any(w in n for w in ("wire", "rope", "fiber")):
        line(img, [(3, 12), (6, 8), (4, 5), (8, 3), (12, 6), (10, 10), (13, 13)], outline)
        line(img, [(4, 12), (7, 8), (5, 5), (8, 4), (11, 6), (9, 10), (12, 13)], colors[1])
    elif any(w in n for w in ("bone", "hide", "tissue", "ration", "berry")):
        draw_poly(draw, [(4, 5), (9, 3), (12, 6), (10, 11), (5, 13), (3, 9)], outline)
        draw_poly(draw, [(5, 6), (9, 4), (11, 7), (9, 10), (5, 12), (4, 9)], colors[1])
        if "berry" in n:
            rect(draw, (6, 6, 9, 9), hex_to_rgba("#9b303a"))
            put(img, 7, 6, hex_to_rgba("#d86b70"))
        elif "bone" in n:
            rect(draw, (4, 7, 11, 9), hex_to_rgba("#d8c9a7"))
            put(img, 5, 6, hex_to_rgba("#f0e2bd"))
            put(img, 10, 10, hex_to_rgba("#8f7144"))
        elif "ration" in n:
            rect(draw, (5, 5, 10, 11), hex_to_rgba("#8f7144"))
            rect(draw, (5, 5, 10, 6), hex_to_rgba("#d0b26b"))
    else:
        # jagged shard/chunk silhouette
        if any(w in n for w in ("wire", "plastic")):
            draw_poly(draw, [(4, 9), (8, 4), (12, 6), (9, 12), (5, 13)], outline)
            draw_poly(draw, [(5, 9), (8, 5), (11, 7), (8, 11), (6, 12)], colors[2])
        else:
            draw_poly(draw, [(3, 11), (6, 5), (11, 2), (13, 5), (10, 12), (5, 14)], outline)
            draw_poly(draw, [(5, 11), (7, 6), (10, 4), (11, 6), (9, 11), (6, 13)], colors[2])
        line(img, [(6, 10), (9, 5), (10, 5)], colors[0])
        put(img, 10, 11, colors[min(5, len(colors) - 1)])


def add_item_identity(img: Image.Image, spec: TextureSpec) -> None:
    """Break template sameness with one deliberate material fleck."""
    colors = palette_rgba(spec)
    rng = stable_rng(f"item-id:{spec.name}")
    opaque: list[tuple[int, int]] = []
    for y in range(img.height):
        for x in range(img.width):
            if img.getpixel((x, y))[3] != 0:
                opaque.append((x, y))
    if not opaque:
        return
    x, y = opaque[rng.randrange(len(opaque))]
    accent_idx = min(len(colors) - 1, 5 if rng.random() < 0.6 else 0)
    put(img, x, y, colors[accent_idx])


def draw_item(spec: TextureSpec) -> Image.Image:
    img = transparent_image(16, 16)
    n = spec.name
    if "spawn_egg" in n:
        draw_spawn_egg(img, spec)
    elif spec.item_role == "data_log":
        draw_data_log_item(img, spec)
    elif "filter_cartridge" in n:
        draw_cartridge(img, spec)
    elif spec.item_role == "battery":
        draw_battery_item(img, spec)
    elif any(w in n for w in ("bottle", "vial", "stim", "rad_away")):
        draw_bottle(img, spec)
    elif any(w in n for w in ("blade", "hammer", "knife", "spear", "annihilator")):
        draw_tool(img, spec)
    elif any(w in n for w in ("helmet", "chestplate", "leggings", "boots", "gas_mask")):
        draw_armor_item(img, spec)
    else:
        draw_material_item(img, spec)
    add_item_identity(img, spec)
    return img


def drone_variant_palette(name: str) -> dict[str, RGBA]:
    if name == "echo_drone":
        return {
            "outline": (16, 15, 15, 255),
            "dark": (34, 34, 32, 255),
            "mid": (57, 55, 50, 255),
            "panel": (79, 71, 61, 255),
            "light": (135, 109, 88, 255),
            "accent": (255, 83, 43, 255),
            "glow": (255, 176, 47, 255),
            "warn": (255, 111, 15, 255),
            "white": (255, 234, 169, 255),
        }
    if name == "scout_drone":
        return {
            "outline": (14, 22, 18, 255),
            "dark": (34, 45, 38, 255),
            "mid": (58, 75, 64, 255),
            "panel": (76, 99, 80, 255),
            "light": (138, 164, 132, 255),
            "accent": (82, 236, 104, 255),
            "glow": (188, 255, 154, 255),
            "warn": (244, 169, 39, 255),
            "white": (243, 255, 221, 255),
        }
    return {
        "outline": (13, 20, 22, 255),
        "dark": (35, 48, 51, 255),
        "mid": (67, 86, 88, 255),
        "panel": (94, 116, 116, 255),
        "light": (168, 192, 188, 255),
        "accent": (36, 231, 238, 255),
        "glow": (169, 255, 255, 255),
        "warn": (255, 163, 33, 255),
        "white": (249, 255, 255, 255),
    }


def drone_tint(color: RGBA, amount: float) -> RGBA:
    if amount >= 1.0:
        return tuple(min(255, int(c + (255 - c) * (amount - 1.0))) for c in color[:3]) + (color[3],)
    return tuple(max(0, int(c * amount)) for c in color[:3]) + (color[3],)


def draw_drone_panel(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int],
                     fill: RGBA, palette: dict[str, RGBA]) -> None:
    x0, y0, x1, y1 = xy
    rect(draw, xy, palette["outline"])
    if x1 - x0 <= 1 or y1 - y0 <= 1:
        return
    rect(draw, (x0 + 1, y0 + 1, x1 - 1, y1 - 1), fill)
    draw.line((x0 + 1, y0 + 1, x1 - 1, y0 + 1), fill=drone_tint(palette["light"], 1.05))
    draw.line((x0 + 1, y0 + 1, x0 + 1, y1 - 1), fill=drone_tint(palette["light"], 0.92))
    draw.line((x0 + 1, y1 - 1, x1 - 1, y1 - 1), fill=palette["dark"])
    draw.line((x1 - 1, y0 + 2, x1 - 1, y1 - 1), fill=palette["dark"])
    if x1 - x0 > 7 and y1 - y0 > 5:
        rect(draw, (x0 + 3, y0 + 3, x1 - 3, y1 - 3), drone_tint(fill, 0.82))
        draw.line((x0 + 4, y0 + 3, x1 - 4, y0 + 3), fill=drone_tint(fill, 1.2))
        draw.line((x0 + 4, y1 - 3, x1 - 4, y1 - 3), fill=palette["outline"])


def draw_drone_vents(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int],
                     palette: dict[str, RGBA], horizontal: bool = True) -> None:
    x0, y0, x1, y1 = xy
    if horizontal:
        for y in range(y0 + 2, y1 - 1, 3):
            draw.line((x0 + 2, y, x1 - 2, y), fill=palette["outline"])
            draw.line((x0 + 2, y + 1, x1 - 3, y + 1), fill=palette["dark"])
    else:
        for x in range(x0 + 2, x1 - 1, 3):
            draw.line((x, y0 + 2, x, y1 - 2), fill=palette["outline"])
            draw.line((x + 1, y0 + 2, x + 1, y1 - 3), fill=palette["dark"])


def draw_drone_display(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int],
                       palette: dict[str, RGBA], hostile: bool = False) -> None:
    x0, y0, x1, y1 = xy
    draw_drone_panel(draw, xy, palette["dark"], palette)
    rect(draw, (x0 + 2, y0 + 2, x1 - 2, y1 - 2), drone_tint(palette["accent"], 0.38))
    rect(draw, (x0 + 3, y0 + 3, x1 - 3, y1 - 3), drone_tint(palette["accent"], 0.75))
    cx = (x0 + x1) // 2
    cy = (y0 + y1) // 2
    if hostile:
        rect(draw, (x0 + 4, cy - 1, cx - 1, cy), palette["glow"])
        rect(draw, (cx + 1, cy - 1, x1 - 4, cy), palette["glow"])
        draw.line((x0 + 5, cy + 2, x1 - 5, cy + 2), fill=palette["warn"])
    else:
        rect(draw, (cx - 1, cy - 1, cx + 1, cy + 1), palette["glow"])
        draw.line((cx - 4, cy, cx + 4, cy), fill=palette["glow"])
        draw.line((cx, cy - 3, cx, cy + 3), fill=palette["glow"])
        draw.point((cx - 2, cy - 2), fill=palette["white"])
    for corner in ((x0 + 2, y0 + 2), (x1 - 2, y0 + 2), (x0 + 2, y1 - 2), (x1 - 2, y1 - 2)):
        draw.point(corner, fill=palette["white"])


def draw_drone_core(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int],
                    palette: dict[str, RGBA]) -> None:
    x0, y0, x1, y1 = xy
    draw_drone_panel(draw, xy, palette["dark"], palette)
    cx = (x0 + x1) // 2
    cy = (y0 + y1) // 2
    rect(draw, (cx - 2, cy - 2, cx + 2, cy + 2), palette["accent"])
    rect(draw, (cx - 1, cy - 1, cx + 1, cy + 1), palette["glow"])
    draw.line((x0 + 3, cy, x1 - 3, cy), fill=drone_tint(palette["accent"], 0.7))
    draw.line((cx, y0 + 2, cx, y1 - 2), fill=drone_tint(palette["accent"], 0.7))


def draw_drone_chip_marks(draw: ImageDraw.ImageDraw, palette: dict[str, RGBA],
                          rng: random.Random, regions: Iterable[tuple[int, int, int, int]]) -> None:
    region_list = list(regions)
    for _ in range(26):
        x0, y0, x1, y1 = rng.choice(region_list)
        if x1 - x0 < 3 or y1 - y0 < 3:
            continue
        x = rng.randrange(x0 + 2, x1)
        y = rng.randrange(y0 + 2, y1)
        color = palette["light"] if rng.random() < 0.48 else palette["warn"]
        rect(draw, (x, y, min(x1 - 1, x + 1), y), color)


def draw_drone_entity(spec: TextureSpec) -> Image.Image:
    img = transparent_image(64, 64)
    draw = ImageDraw.Draw(img)
    colors = drone_variant_palette(spec.name)
    rng = stable_rng(f"drone-entity:{spec.name}")

    chassis = (0, 0, 33, 10)
    top_plate = (0, 12, 29, 20)
    top_module = (30, 12, 45, 20)
    front_panel = (0, 24, 20, 31)
    front_display = (22, 24, 35, 31)
    rear_panel = (0, 34, 20, 40)
    rear_display = (22, 34, 35, 40)
    side_engine = (36, 0, 55, 14)
    engine_nozzle = (48, 16, 63, 23)
    hover_shell = (40, 32, 51, 43)
    hover_glow = (52, 32, 63, 38)
    bottom_panel = (20, 44, 43, 52)
    bottom_display = (44, 44, 62, 50)
    wing = (0, 48, 18, 63)
    wing_tip = (16, 48, 27, 57)
    antenna_post = (56, 0, 60, 8)
    antenna_tip = (56, 8, 62, 14)

    draw_drone_panel(draw, chassis, colors["mid"], colors)
    draw_drone_vents(draw, (4, 2, 15, 8), colors, horizontal=True)
    draw_drone_vents(draw, (18, 2, 30, 8), colors, horizontal=True)
    rect(draw, (2, 2, 3, 3), colors["light"])
    rect(draw, (29, 7, 31, 8), colors["warn"])
    rect(draw, (16, 4, 18, 6), colors["accent"])

    draw_drone_panel(draw, top_plate, colors["panel"], colors)
    draw_drone_vents(draw, (5, 15, 23, 18), colors, horizontal=False)
    rect(draw, (2, 14, 4, 15), colors["warn"])
    rect(draw, (25, 17, 27, 18), colors["warn"])
    draw_drone_core(draw, top_module, colors)

    draw_drone_panel(draw, front_panel, colors["dark"], colors)
    draw.line((3, 29, 17, 29), fill=colors["warn"])
    draw.line((5, 26, 15, 26), fill=colors["panel"])
    draw_drone_display(draw, front_display, colors, hostile=spec.name == "echo_drone")

    draw_drone_panel(draw, rear_panel, colors["dark"], colors)
    draw_drone_vents(draw, (3, 36, 17, 38), colors, horizontal=True)
    draw_drone_display(draw, rear_display, colors, hostile=False)

    draw_drone_panel(draw, side_engine, colors["panel"], colors)
    draw_drone_vents(draw, (39, 3, 45, 11), colors, horizontal=False)
    rect(draw, (48, 4, 53, 7), drone_tint(colors["accent"], 0.5))
    rect(draw, (49, 5, 52, 6), colors["glow"])
    rect(draw, (38, 11, 40, 12), colors["warn"])

    draw_drone_panel(draw, engine_nozzle, colors["dark"], colors)
    draw.line((51, 19, 60, 19), fill=colors["accent"])
    draw.line((51, 20, 60, 20), fill=colors["glow"])

    draw_drone_panel(draw, hover_shell, colors["dark"], colors)
    draw.rectangle((43, 35, 48, 40), outline=colors["accent"], fill=drone_tint(colors["accent"], 0.35))
    rect(draw, (44, 36, 47, 39), colors["glow"])
    draw_drone_core(draw, hover_glow, colors)

    draw_drone_panel(draw, bottom_panel, colors["dark"], colors)
    draw.line((23, 47, 40, 47), fill=colors["accent"])
    for x in (25, 30, 35):
        rect(draw, (x, 46, x + 2, 48), colors["glow"])
    draw_drone_display(draw, bottom_display, colors, hostile=spec.name == "echo_drone")

    draw_drone_panel(draw, wing, colors["mid"], colors)
    draw.line((3, 51, 15, 60), fill=colors["outline"])
    draw.line((4, 52, 16, 60), fill=colors["dark"])
    draw.line((5, 53, 16, 58), fill=colors["accent"])
    draw_drone_panel(draw, wing_tip, colors["dark"], colors)
    draw.line((19, 52, 25, 52), fill=colors["glow"])

    draw_drone_panel(draw, antenna_post, colors["dark"], colors)
    draw_drone_panel(draw, antenna_tip, colors["dark"], colors)
    rect(draw, (59, 8, 61, 10), colors["warn"])

    draw_drone_chip_marks(
        draw,
        colors,
        rng,
        (chassis, top_plate, front_panel, rear_panel, side_engine, bottom_panel, wing),
    )
    return img


def draw_entity(spec: TextureSpec) -> Image.Image:
    if spec.name in DRONE_ENTITY_TEXTURES:
        return draw_drone_entity(spec)

    img = transparent_image(spec.width, spec.height)
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    w, h = spec.width, spec.height
    outline = colors[min(4, len(colors) - 1)]
    mid = colors[2]
    light = colors[0]
    shadow = colors[3]
    accent = colors[min(5, len(colors) - 1)]

    # Blocky UV islands arranged close to vanilla humanoid sheet conventions.
    rect(draw, (8, 8, 15, 15), outline)
    rect(draw, (9, 9, 14, 14), mid)
    rect(draw, (9, 9, 12, 10), light)
    rect(draw, (13, 12, 14, 14), shadow)
    rect(draw, (20, 20, 27, 31), outline)
    rect(draw, (21, 21, 26, 30), mid)
    rect(draw, (21, 21, 26, 22), light)
    for x0 in (44, 36):
        rect(draw, (x0, 20, x0 + 3, 31), outline)
        rect(draw, (x0 + 1, 21, x0 + 2, 30), mid)
    for x0 in (4, 12):
        rect(draw, (x0, 20, x0 + 3, 31), outline)
        rect(draw, (x0 + 1, 21, x0 + 2, 30), mid)
    if w == 64 and h == 64:
        rect(draw, (32, 8, 47, 15), (*accent[:3], 175))
        rect(draw, (20, 36, 35, 47), (*shadow[:3], 180))
        rect(draw, (36, 52, 47, 63), (*mid[:3], 175))

    rng = stable_rng(f"entity:{spec.name}")
    for _ in range(22):
        x = rng.randrange(0, w)
        y = rng.randrange(0, h)
        if rng.random() < 0.65:
            rect(draw, (x, y, min(w - 1, x + 1), min(h - 1, y + 1)), colors[rng.randrange(1, min(5, len(colors)))])
    if any(wd in spec.name for wd in ("drone", "echo", "scout")):
        rect(draw, (24, 8, 31, 15), accent)
        rect(draw, (26, 10, 29, 13), colors[min(6, len(colors) - 1)])
    return img


def draw_armor_layer(spec: TextureSpec) -> Image.Image:
    img = transparent_image(spec.width, spec.height)
    colors = palette_rgba(spec)
    draw = ImageDraw.Draw(img)
    outline = colors[min(4, len(colors) - 1)]
    plate = colors[2]
    light = colors[0]
    shadow = colors[3]
    accent = colors[min(5, len(colors) - 1)]
    islands = [
        (8, 8, 15, 15),
        (20, 20, 27, 31),
        (36, 20, 43, 31),
        (44, 20, 51, 31),
        (4, 20, 11, 31),
        (12, 20, 19, 31),
    ]
    for x1, y1, x2, y2 in islands:
        rect(draw, (x1, y1, x2, y2), outline)
        rect(draw, (x1 + 1, y1 + 1, x2 - 1, y2 - 1), plate)
        rect(draw, (x1 + 1, y1 + 1, x2 - 1, y1 + 2), light)
        rect(draw, (x2 - 1, y1 + 3, x2 - 1, y2 - 1), shadow)
        put(img, x1 + 2, min(y2 - 2, y1 + 5), accent)
    return img


def draw_gui(spec: TextureSpec) -> Image.Image:
    colors = palette_rgba(spec)
    img = filled_image(spec.width, spec.height, colors[3])
    draw = ImageDraw.Draw(img)
    bg = colors[2]
    light = colors[0]
    shadow = colors[4]
    accent = colors[min(5, len(colors) - 1)]
    rect(draw, (0, 0, spec.width - 1, spec.height - 1), shadow)
    rect(draw, (3, 3, spec.width - 4, spec.height - 4), bg)
    rect(draw, (3, 3, spec.width - 4, 5), light)
    rect(draw, (3, 3, 5, spec.height - 4), light)
    rect(draw, (spec.width - 6, 6, spec.width - 4, spec.height - 4), shadow)
    rect(draw, (6, spec.height - 6, spec.width - 4, spec.height - 4), shadow)

    # Inventory-ish slots and machine panel zones.
    for row in range(3):
        for col in range(9):
            x = 8 + col * 18
            y = spec.height - 62 + row * 18
            rect(draw, (x, y, x + 15, y + 15), colors[4])
            rect(draw, (x + 1, y + 1, x + 14, y + 14), colors[1])
    rect(draw, (22, 18, 89, 57), colors[4])
    rect(draw, (24, 20, 87, 55), colors[2])
    rect(draw, (104, 20, 151, 55), colors[4])
    rect(draw, (106, 22, 149, 53), colors[1])
    for x in range(110, 146, 6):
        rect(draw, (x, 28, x + 2, 47), accent if x % 12 == 0 else colors[3])
    rect(draw, (170, 22, 230, 54), colors[4])
    rect(draw, (172, 24, 228, 52), colors[2])
    rect(draw, (177, 29, 223, 34), accent)
    return img


def render_texture(spec: TextureSpec) -> Image.Image:
    if spec.type == "item":
        return draw_item(spec)
    if spec.type == "block":
        return draw_block(spec)
    if spec.type == "entity":
        return draw_entity(spec)
    if spec.type == "armor":
        return draw_armor_layer(spec)
    if spec.type == "gui":
        return draw_gui(spec)
    raise ValueError(f"Unsupported texture type: {spec.type}")


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def update_multiside_machine_block_models() -> None:
    for name in sorted(MULTISIDE_MACHINE_MODEL_NAMES):
        write_json(
            ASSETS / "models/block" / f"{name}.json",
            {
                "parent": "minecraft:block/cube",
                "textures": {
                    "particle": f"{MODID}:block/{name}_front",
                    "north": f"{MODID}:block/{name}_front",
                    "south": f"{MODID}:block/{name}_side",
                    "east": f"{MODID}:block/{name}_side",
                    "west": f"{MODID}:block/{name}_side",
                    "up": f"{MODID}:block/{name}_top",
                    "down": f"{MODID}:block/{name}_bottom",
                },
            },
        )


def update_registered_item_models(item_names: Iterable[str]) -> None:
    for name in sorted(item_names):
        write_json(
            ASSETS / "items" / f"{name}.json",
            {"model": {"type": "minecraft:model", "model": f"{MODID}:item/{name}"}},
        )
        write_json(
            ASSETS / "models/item" / f"{name}.json",
            {
                "parent": "minecraft:item/generated",
                "textures": {"layer0": f"{MODID}:item/{name}"},
            },
        )


def block_item_model_name(name: str) -> str:
    if name == "ash_layer":
        return "ash_layer_height2"
    if name in {"item_pipe", "power_cable", "high_voltage_power_cable", "reinforced_power_cable"}:
        return f"{name}_core"
    if name.endswith("_tall_grass"):
        return f"{name}_bottom"
    return name


def update_registered_block_item_models(block_names: Iterable[str]) -> None:
    for name in sorted(block_names):
        model_name = block_item_model_name(name)
        write_json(
            ASSETS / "items" / f"{name}.json",
            {"model": {"type": "minecraft:model", "model": f"{MODID}:block/{model_name}"}},
        )
        write_json(
            ASSETS / "models/item" / f"{name}.json",
            {"parent": f"{MODID}:block/{model_name}"},
        )


def cleanup_block_item_sprite_artifacts(block_names: Iterable[str], regular_item_names: Iterable[str]) -> None:
    regular = set(regular_item_names)
    for name in sorted(set(block_names) - regular):
        stale_texture = TEXTURES / "item" / f"{name}.png"
        if stale_texture.exists():
            stale_texture.unlink()


def write_manifest(specs: list[TextureSpec]) -> None:
    BUILD_OUT.mkdir(parents=True, exist_ok=True)
    write_json(BUILD_OUT / "texture_manifest.generated.json", [asdict(s) for s in specs])


def write_catalog(specs: list[TextureSpec]) -> None:
    BUILD_OUT.mkdir(parents=True, exist_ok=True)
    lines = ["# ECHO: Ashfall Protocol Texture Catalog", ""]
    for spec in specs:
        if spec.type not in {"item", "block"}:
            continue
        lines.extend(
            [
                f"## {spec.name}",
                f"1. Texture Name: `{spec.name}`",
                f"2. Type: `{spec.type}`",
                f"3. 16x16 Design Summary: {spec.summary}",
                f"4. Color Palette: {', '.join(spec.palette)}",
                f"5. Pixel Placement Notes: {spec.placement_notes}",
                f"6. Common Mistakes to Avoid: {spec.mistakes_to_avoid}",
                "7. Final Image Generation Prompt:",
                "",
                "```text",
                spec.prompt,
                "```",
                "",
            ]
        )
    (BUILD_OUT / "texture_catalog.md").write_text("\n".join(lines), encoding="utf-8")


def paste_scaled(canvas: Image.Image, image: Image.Image, x: int, y: int, scale: int) -> None:
    resized = image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST)
    canvas.alpha_composite(resized, (x, y))


def preview_family(spec: TextureSpec) -> str:
    if spec.type == "item":
        if spec.item_role in {"resource", "organic", "fiber"}:
            return "item_resources"
        if spec.item_role in {"filter", "battery", "bottle", "data_log", "tech", "survival", "tool", "armor_item"}:
            return "item_utility"
        return "items"
    if spec.type != "block":
        return spec.type
    if spec.family == "plant":
        return "plants"
    if spec.family in {"machine", "pipe", "cable"}:
        return "machines"
    if spec.family == "terrain":
        return "terrain"
    return "misc_blocks"


def write_item_preview(specs: list[TextureSpec], out_name: str) -> None:
    if not specs:
        return
    scale = 4
    cols = 16
    cell = 40
    rows = math.ceil(len(specs) / cols)
    sheet = filled_image(cols * cell, rows * cell, (28, 31, 31, 255))
    for i, spec in enumerate(specs):
        img = Image.open(BASE / spec.path).convert("RGBA")
        try:
            x = (i % cols) * cell + 2
            y = (i // cols) * cell + 2
            paste_scaled(sheet, img, x, y, scale)
        finally:
            img.close()
    sheet.save(BUILD_OUT / out_name)


def write_block_preview(specs: list[TextureSpec], out_name: str, cols: int = 8) -> None:
    if not specs:
        return
    scale = 3
    cell = 16 * scale * 3
    rows = math.ceil(len(specs) / cols)
    sheet = filled_image(cols * cell, rows * cell, (24, 26, 26, 255))
    for i, spec in enumerate(specs):
        img = Image.open(BASE / spec.path).convert("RGBA")
        try:
            tile = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
            for ty in range(3):
                for tx in range(3):
                    tile.alpha_composite(img, (tx * 16, ty * 16))
            x = (i % cols) * cell
            y = (i // cols) * cell
            paste_scaled(sheet, tile, x, y, scale)
        finally:
            img.close()
    sheet.save(BUILD_OUT / out_name)


def write_block_contact_sheet(specs: list[TextureSpec], out_name: str, cols: int = 16) -> None:
    if not specs:
        return
    scale = 4
    cell = 24 * scale
    rows = math.ceil(len(specs) / cols)
    sheet = filled_image(cols * cell, rows * cell, (24, 26, 26, 255))
    for i, spec in enumerate(specs):
        img = Image.open(BASE / spec.path).convert("RGBA")
        try:
            x = (i % cols) * cell + 4 * scale
            y = (i // cols) * cell + 4 * scale
            paste_scaled(sheet, img, x, y, scale)
        finally:
            img.close()
    sheet.save(BUILD_OUT / out_name)


def write_previews(specs: list[TextureSpec]) -> None:
    BUILD_OUT.mkdir(parents=True, exist_ok=True)
    item_specs = [s for s in specs if s.type == "item"]
    block_specs = [s for s in specs if s.type == "block"]

    write_item_preview(item_specs, "items_inventory_sheet.png")
    write_block_contact_sheet(block_specs, "blocks_1x_contact_sheet.png", cols=16)
    write_block_preview(block_specs, "blocks_3x3_tiling_sheet.png", cols=12)

    grouped: dict[str, list[TextureSpec]] = {}
    for spec in specs:
        grouped.setdefault(preview_family(spec), []).append(spec)
    write_item_preview(grouped.get("item_resources", []), "items_resources_sheet.png")
    write_item_preview(grouped.get("item_utility", []), "items_utility_sheet.png")
    write_block_preview(grouped.get("terrain", []), "blocks_terrain_3x3_sheet.png")
    write_block_preview(grouped.get("plants", []), "blocks_plants_3x3_sheet.png")
    write_block_preview(grouped.get("machines", []), "blocks_machines_3x3_sheet.png")
    write_block_preview(grouped.get("misc_blocks", []), "blocks_misc_3x3_sheet.png")


def generate(dry_run: bool = False, targets: set[str] | None = None) -> list[TextureSpec]:
    if not dry_run and not targets:
        update_multiside_machine_block_models()
    all_specs = build_manifest()
    specs = all_specs
    if targets:
        available = {spec.name for spec in all_specs}
        missing = sorted(targets - available)
        if missing:
            raise SystemExit(f"Unknown texture target(s): {', '.join(missing)}")
        specs = [spec for spec in all_specs if spec.name in targets]
    item_names = parse_registered_items()
    block_names = parse_registered_blocks()
    if not dry_run and not targets:
        update_registered_item_models(item_names - block_names)
        update_registered_block_item_models(block_names)
        cleanup_block_item_sprite_artifacts(block_names, item_names)
    for spec in specs:
        out = BASE / spec.path
        if dry_run:
            continue
        out.parent.mkdir(parents=True, exist_ok=True)
        img = render_texture(spec)
        img.save(out)
    if not dry_run and not targets:
        write_manifest(specs)
        write_catalog(specs)
        write_previews(specs)
    return specs


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dry-run", action="store_true", help="Only build and print the manifest summary.")
    parser.add_argument(
        "--target",
        action="append",
        default=[],
        help="Generate one texture by name. Can be supplied multiple times.",
    )
    args = parser.parse_args()
    specs = generate(dry_run=args.dry_run, targets=set(args.target) if args.target else None)
    counts: dict[str, int] = {}
    for spec in specs:
        counts[spec.type] = counts.get(spec.type, 0) + 1
    print("Generated texture manifest:")
    for key in sorted(counts):
        print(f"  {key}: {counts[key]}")
    if not args.dry_run and not args.target:
        print(f"Wrote previews and catalog to {BUILD_OUT}")
    elif not args.dry_run:
        print(f"Wrote {len(specs)} targeted texture(s).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
