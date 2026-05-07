#!/usr/bin/env python3
"""Generate missing addon item/block textures and wire resource JSONs.

This pass targets addon assets that still point at vanilla Minecraft textures
or models. Existing addon PNGs are preserved.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import random
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Any

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ADDONS = ROOT / "addons"
OUT = ROOT / "tmp_generated_addon_textures"
RGBA = tuple[int, int, int, int]


@dataclass(frozen=True)
class Target:
    namespace: str
    kind: str
    name: str
    asset_root: str
    source: str
    page: int
    cell: int
    prompt: str


PALETTES: dict[str, list[str]] = {
    "blackbox": ["#f2f0e8", "#aeb2ae", "#6c7270", "#343b3d", "#151a1e", "#7d51cf", "#55d7ff"],
    "industrial": ["#ded9c8", "#a9a99e", "#6d7471", "#3b4444", "#1f2728", "#bf6f32", "#f0c84b"],
    "nexus": ["#e8dbff", "#b28ce7", "#7350aa", "#3d2a63", "#1a142c", "#45d0ff", "#e658b6"],
    "orbital": ["#e9fbff", "#b7d8e4", "#708da5", "#3c5168", "#1d2938", "#ff8c55", "#8ce6ff"],
    "terminal": ["#d8f2e4", "#89b8a0", "#4f776d", "#293f42", "#121b21", "#69d0ff", "#94e45c"],
    "paper": ["#f4e4bd", "#caaa70", "#85633f", "#483629", "#201d1b", "#60a6c7", "#81bd65"],
    "fluid": ["#eaffff", "#9edce9", "#5798ba", "#2d5572", "#162a42", "#7ce056", "#ca6be6"],
    "hazard": ["#f8e585", "#d7a640", "#9b6030", "#5a3428", "#1f1d1b", "#9ee64f", "#d94b3f"],
}


def stable_rng(key: str) -> random.Random:
    digest = hashlib.sha256(key.encode("utf-8")).digest()
    return random.Random(int.from_bytes(digest[:8], "big"))


def hex_rgba(value: str, alpha: int = 255) -> RGBA:
    value = value.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha)


def read_json(path: Path) -> Any | None:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        return None


def write_json(path: Path, data: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def parse_ref(ref: str, default_namespace: str) -> tuple[str, str] | None:
    if not ref or ref.startswith("#"):
        return None
    if ":" in ref:
        namespace, name = ref.split(":", 1)
    else:
        namespace, name = default_namespace, ref
    return namespace, name


def iter_asset_roots() -> list[Path]:
    roots: list[Path] = []
    if not ADDONS.exists():
        return roots
    for addon in sorted(path for path in ADDONS.iterdir() if path.is_dir()):
        assets = addon / "src/main/resources/assets"
        if not assets.exists():
            continue
        roots.extend(sorted(path for path in assets.iterdir() if path.is_dir()))
    return roots


def texture_exists(asset_root: Path, texture_ref: str, namespace: str) -> bool:
    parsed = parse_ref(texture_ref, namespace)
    if parsed is None:
        return True
    ref_ns, ref_path = parsed
    if ref_ns != namespace:
        return False
    return (asset_root / "textures" / f"{ref_path}.png").exists()


def local_item_model_needs_texture(asset_root: Path, namespace: str, model_name: str) -> bool:
    model_path = asset_root / "models/item" / f"{model_name}.json"
    data = read_json(model_path)
    if not isinstance(data, dict):
        return True
    textures = data.get("textures")
    if not isinstance(textures, dict) or not textures:
        return True
    has_own_named_texture = False
    expected_ref = f"item/{model_name}"
    for value in textures.values():
        if not isinstance(value, str) or value.startswith("#"):
            continue
        parsed = parse_ref(value, namespace)
        if parsed is None:
            continue
        ref_ns, ref_path = parsed
        if ref_ns != namespace:
            return True
        if ref_path == expected_ref:
            has_own_named_texture = True
        if not (asset_root / "textures" / f"{ref_path}.png").exists():
            return True
    return not has_own_named_texture


def block_model_needs_texture(asset_root: Path, namespace: str, model_path: Path) -> bool:
    data = read_json(model_path)
    if not isinstance(data, dict):
        return False
    textures = data.get("textures")
    if not isinstance(textures, dict) or not textures:
        return True
    own = 0
    for value in textures.values():
        if not isinstance(value, str) or value.startswith("#"):
            continue
        parsed = parse_ref(value, namespace)
        if parsed is None:
            continue
        ref_ns, _ = parsed
        if ref_ns != namespace:
            return True
        own += 1
        if not texture_exists(asset_root, value, namespace):
            return True
    return own == 0


def theme_for(namespace: str, name: str) -> str:
    n = f"{namespace}_{name}".lower()
    if any(word in n for word in ("directive", "record", "schematic", "map", "tablet", "log")):
        return "paper"
    if any(word in n for word in ("fluid", "water", "coolant", "solvent", "sludge", "tar", "cell", "canister")):
        return "fluid"
    if any(word in n for word in ("rad", "toxic", "uranium", "hazard", "warning", "corrupt")):
        return "hazard"
    if "blackbox" in namespace or "blackbox" in n:
        return "blackbox"
    if "industrial" in namespace:
        return "industrial"
    if "nexus" in namespace:
        return "nexus"
    if "orbital" in namespace:
        return "orbital"
    if "terminal" in namespace:
        return "terminal"
    return "industrial"


def colors(namespace: str, name: str) -> list[RGBA]:
    return [hex_rgba(value) for value in PALETTES[theme_for(namespace, name)]]


def rect(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], color: RGBA) -> None:
    draw.rectangle(xy, fill=color)


def put(img: Image.Image, x: int, y: int, color: RGBA) -> None:
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def line(draw: ImageDraw.ImageDraw, points: list[tuple[int, int]], color: RGBA) -> None:
    draw.line(points, fill=color, width=1)


def poly(draw: ImageDraw.ImageDraw, points: list[tuple[int, int]], color: RGBA) -> None:
    draw.polygon(points, fill=color)


def draw_item(target: Target) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    c = colors(target.namespace, target.name)
    n = target.name
    rng = stable_rng(f"item:{target.namespace}:{n}")
    outline = c[4]

    if any(word in n for word in ("key", "access")):
        rect(draw, (3, 7, 10, 9), outline)
        rect(draw, (4, 8, 10, 8), c[1])
        rect(draw, (10, 6, 13, 10), outline)
        rect(draw, (11, 7, 12, 9), c[5])
        put(img, 5, 7, c[0])
        put(img, 6, 10, c[6])
    elif any(word in n for word in ("directive", "record", "schematic", "protocol", "tablet", "log")):
        rect(draw, (4, 3, 11, 13), outline)
        rect(draw, (5, 4, 10, 12), c[1])
        rect(draw, (5, 4, 10, 5), c[0])
        line(draw, [(6, 7), (9, 7)], c[3])
        line(draw, [(6, 9), (10, 9)], c[3])
        put(img, 9, 11, c[5])
    elif any(word in n for word in ("core", "matrix", "chip", "circuit", "module", "motor", "servo", "coil", "capacitor")):
        rect(draw, (3, 4, 12, 12), outline)
        rect(draw, (4, 5, 11, 11), c[2])
        rect(draw, (5, 6, 10, 9), c[3])
        put(img, 6, 7, c[5])
        put(img, 9, 8, c[6])
        line(draw, [(4, 4), (6, 2), (9, 2), (12, 5)], c[0])
    elif any(word in n for word in ("fluid", "water", "coolant", "solvent", "sludge", "tar", "cell", "canister", "battery")):
        rect(draw, (5, 2, 10, 13), outline)
        rect(draw, (6, 3, 9, 12), c[2])
        rect(draw, (6, 3, 9, 5), c[0])
        rect(draw, (6, 8, 9, 12), c[5])
        put(img, 8, 9, c[6])
    elif any(word in n for word in ("ingot", "plate", "metal", "alloy", "ferrite", "scrap", "slag")):
        poly(draw, [(3, 10), (6, 5), (11, 4), (13, 7), (10, 12), (5, 13)], outline)
        poly(draw, [(5, 10), (7, 6), (10, 5), (11, 7), (9, 11), (6, 12)], c[2])
        line(draw, [(6, 9), (9, 6), (10, 6)], c[0])
        put(img, 10, 10, c[5])
    elif any(word in n for word in ("dust", "powder")):
        for _ in range(14):
            x = rng.randrange(4, 12)
            y = rng.randrange(6, 14)
            put(img, x, y, c[rng.randrange(1, 5)])
        rect(draw, (4, 12, 11, 13), c[3])
        put(img, 7, 10, c[5])
    elif any(word in n for word in ("wire", "pipe", "duct", "cable")):
        line(draw, [(3, 12), (6, 8), (5, 5), (8, 3), (12, 6), (10, 10), (13, 13)], outline)
        line(draw, [(4, 12), (7, 8), (6, 5), (8, 4), (11, 6), (9, 10), (12, 13)], c[1])
        put(img, 8, 4, c[5])
    elif any(word in n for word in ("wrench", "magnet", "tool", "hammer", "cutter")):
        line(draw, [(4, 12), (7, 9), (10, 6), (12, 3)], outline)
        line(draw, [(5, 12), (8, 9), (11, 6)], c[1])
        rect(draw, (10, 3, 13, 5), c[2])
        put(img, 11, 4, c[0])
    else:
        poly(draw, [(3, 11), (6, 4), (10, 2), (13, 5), (11, 11), (6, 14)], outline)
        poly(draw, [(5, 11), (7, 5), (10, 4), (11, 6), (9, 11), (7, 12)], c[2])
        line(draw, [(6, 10), (9, 5), (10, 5)], c[0])
        put(img, 10, 10, c[5])

    # A stable identity pixel helps similar resources avoid exact duplicates.
    opaque = [(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[3] > 0]
    if opaque:
        x, y = opaque[rng.randrange(len(opaque))]
        put(img, x, y, c[6 if rng.random() < 0.5 else 5])
    return img


def draw_block(target: Target) -> Image.Image:
    n = target.name
    c = colors(target.namespace, n)
    rng = stable_rng(f"block:{target.namespace}:{n}")
    transparent = any(word in n for word in ("glass", "grate", "vent", "fan"))
    base_alpha = 150 if "glass" in n else 255
    img = Image.new("RGBA", (16, 16), (c[3][0], c[3][1], c[3][2], base_alpha))
    draw = ImageDraw.Draw(img)

    machine = any(
        word in n
        for word in (
            "machine",
            "terminal",
            "decoder",
            "assembler",
            "panel",
            "controller",
            "kiln",
            "monitor",
            "fan",
            "duct",
            "pipe",
            "bank",
            "recycler",
            "scrubber",
            "refiner",
            "pump",
            "furnace",
            "hatch",
            "grinder",
            "gauge",
            "capacitor",
            "array",
            "purifier",
            "engine",
            "extractor",
            "projector",
            "stabilizer",
        )
    )
    monolith = "monolith" in n or "blackbox" in n or "core" in n

    if transparent:
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        for y in range(16):
            for x in range(16):
                if x in {0, 15} or y in {0, 15} or (x + y) % 5 == 0:
                    put(img, x, y, (c[1][0], c[1][1], c[1][2], 150))
        line(draw, [(2, 12), (12, 2)], (c[0][0], c[0][1], c[0][2], 170))
        return img

    if machine or monolith:
        rect(draw, (0, 0, 15, 15), c[4])
        rect(draw, (1, 1, 14, 14), c[2])
        rect(draw, (1, 1, 14, 2), c[0])
        rect(draw, (1, 1, 2, 14), c[1])
        rect(draw, (13, 3, 14, 14), c[3])
        rect(draw, (3, 5, 12, 11), c[4])
        rect(draw, (4, 6, 11, 10), c[3])
        if any(word in n for word in ("pipe", "duct")):
            rect(draw, (0, 6, 15, 9), c[4])
            rect(draw, (1, 7, 14, 8), c[1])
            for x in (3, 8, 13):
                rect(draw, (x, 5, x + 1, 10), c[3])
        elif any(word in n for word in ("fan", "vent")):
            rect(draw, (4, 4, 11, 11), c[4])
            line(draw, [(8, 5), (8, 11)], c[1])
            line(draw, [(5, 8), (11, 8)], c[1])
            put(img, 8, 8, c[5])
        elif any(word in n for word in ("light", "lamp")):
            rect(draw, (5, 5, 10, 10), c[5])
            rect(draw, (6, 6, 9, 9), c[0])
        else:
            put(img, 5, 7, c[5])
            put(img, 10, 8, c[6])
            line(draw, [(4, 12), (12, 12)], c[1])
    else:
        for y in range(16):
            for x in range(16):
                idx = 2 + ((x * 3 + y * 5 + rng.randrange(3)) % 3)
                if rng.random() < 0.22:
                    idx = rng.randrange(0, 5)
                put(img, x, y, c[idx])
        for _ in range(18):
            x = rng.randrange(16)
            y = rng.randrange(16)
            put(img, x, y, c[rng.randrange(0, min(len(c), 6))])
        if any(word in n for word in ("stripe", "hazard", "warning")):
            for x in range(-8, 20, 5):
                line(draw, [(x, 15), (x + 15, 0)], c[5])
        if any(word in n for word in ("ore", "crystal", "ferrite")):
            for _ in range(7):
                x = rng.randrange(2, 14)
                y = rng.randrange(2, 14)
                put(img, x, y, c[5])
                put(img, min(15, x + 1), y, c[0])
    return img


def prompt_for(namespace: str, kind: str, name: str) -> str:
    readable = name.replace("_", " ")
    return (
        f"Create a Minecraft-style 16x16 {kind} texture for {namespace}:{name} ({readable}). "
        "Use crisp hard pixels, no anti-aliasing, no blur, a small palette, top-left lighting, "
        "bottom-right shadow, and a readable vanilla-friendly silhouette or tile."
    )


def discover_targets() -> tuple[list[Target], list[Target]]:
    items: list[tuple[str, str, Path, Path]] = []
    blocks: list[tuple[str, str, Path, Path]] = []

    for asset_root in iter_asset_roots():
        namespace = asset_root.name
        for item_path in sorted((asset_root / "items").glob("*.json")):
            data = read_json(item_path)
            if not isinstance(data, dict):
                continue
            model = data.get("model")
            model_id = model.get("model") if isinstance(model, dict) else None
            if not isinstance(model_id, str):
                continue
            target_name: str | None = None
            if model_id.startswith("minecraft:item/") or model_id.startswith("minecraft:block/"):
                target_name = item_path.stem
            elif model_id.startswith(f"{namespace}:item/"):
                local_name = model_id.split("/", 1)[1]
                if local_item_model_needs_texture(asset_root, namespace, local_name):
                    target_name = local_name
            if target_name is not None:
                items.append((namespace, target_name, asset_root, item_path))

        for model_path in sorted((asset_root / "models/block").glob("*.json")):
            if block_model_needs_texture(asset_root, namespace, model_path):
                blocks.append((namespace, model_path.stem, asset_root, model_path))

    def finalize(raw: list[tuple[str, str, Path, Path]], kind: str) -> list[Target]:
        result: list[Target] = []
        grouped_index: dict[str, int] = {}
        for namespace, name, asset_root, source in raw:
            key = f"{namespace}:{kind}"
            index = grouped_index.get(key, 0)
            grouped_index[key] = index + 1
            result.append(
                Target(
                    namespace=namespace,
                    kind=kind,
                    name=name,
                    asset_root=asset_root.relative_to(ROOT).as_posix(),
                    source=source.relative_to(ROOT).as_posix(),
                    page=index // 64,
                    cell=index % 64,
                    prompt=prompt_for(namespace, kind, name),
                )
            )
        return result

    return finalize(items, "item"), finalize(blocks, "block")


def update_item_resources(target: Target) -> None:
    asset_root = ROOT / target.asset_root
    texture_path = asset_root / "textures/item" / f"{target.name}.png"
    if not texture_path.exists():
        texture_path.parent.mkdir(parents=True, exist_ok=True)
        draw_item(target).save(texture_path)
    write_json(
        asset_root / "models/item" / f"{target.name}.json",
        {"parent": "minecraft:item/generated", "textures": {"layer0": f"{target.namespace}:item/{target.name}"}},
    )
    write_json(
        asset_root / "items" / f"{target.name}.json",
        {"model": {"type": "minecraft:model", "model": f"{target.namespace}:item/{target.name}"}},
    )


def update_block_resources(target: Target) -> None:
    asset_root = ROOT / target.asset_root
    texture_path = asset_root / "textures/block" / f"{target.name}.png"
    if not texture_path.exists():
        texture_path.parent.mkdir(parents=True, exist_ok=True)
        draw_block(target).save(texture_path)
    model_path = asset_root / "models/block" / f"{target.name}.json"
    data = read_json(model_path)
    if not isinstance(data, dict):
        return
    textures = data.get("textures")
    if not isinstance(textures, dict):
        textures = {}
        data["textures"] = textures
    if not textures:
        textures["all"] = f"{target.namespace}:block/{target.name}"
    else:
        for key, value in list(textures.items()):
            if isinstance(value, str) and not value.startswith("#"):
                textures[key] = f"{target.namespace}:block/{target.name}"
    write_json(model_path, data)


def write_sheet(targets: list[Target], namespace: str, kind: str, page: int) -> None:
    page_targets = [target for target in targets if target.namespace == namespace and target.page == page]
    if not page_targets:
        return
    cell = 144
    sheet = Image.new("RGBA", (8 * cell, 8 * cell), (255, 0, 255, 255) if kind == "item" else (31, 35, 34, 255))
    for target in page_targets:
        icon = draw_item(target) if kind == "item" else draw_block(target)
        scaled = icon.resize((96, 96), Image.Resampling.NEAREST)
        x = (target.cell % 8) * cell + 24
        y = (target.cell // 8) * cell + 24
        sheet.alpha_composite(scaled, (x, y))
    out = OUT / "sheets" / f"{namespace}_{kind}_page_{page + 1}.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(out)


def validate_targets(targets: list[Target]) -> list[str]:
    errors: list[str] = []
    for target in targets:
        asset_root = ROOT / target.asset_root
        texture = asset_root / "textures" / target.kind / f"{target.name}.png"
        if not texture.exists():
            errors.append(f"Missing generated texture: {texture.relative_to(ROOT).as_posix()}")
            continue
        with Image.open(texture) as image:
            rgba = image.convert("RGBA")
            pixel_data = getattr(rgba, "get_flattened_data", rgba.getdata)
            pixels = list(pixel_data())
            if rgba.size != (16, 16):
                errors.append(f"Bad texture size: {texture.relative_to(ROOT).as_posix()} is {rgba.size}")
            if target.kind == "item":
                corners = [rgba.getpixel((0, 0)), rgba.getpixel((15, 0)), rgba.getpixel((0, 15)), rgba.getpixel((15, 15))]
                if not all(pixel[3] == 0 for pixel in corners):
                    errors.append(f"Item corners are not transparent: {texture.relative_to(ROOT).as_posix()}")
                if all(pixel[3] == 0 for pixel in pixels):
                    errors.append(f"Item is fully transparent: {texture.relative_to(ROOT).as_posix()}")
            else:
                if all(pixel[3] == 0 for pixel in pixels):
                    errors.append(f"Block is fully transparent: {texture.relative_to(ROOT).as_posix()}")
    return errors


def audit_resource_refs(targets: list[Target]) -> list[str]:
    errors: list[str] = []
    for target in targets:
        asset_root = ROOT / target.asset_root
        if target.kind == "item":
            item_def = read_json(asset_root / "items" / f"{target.name}.json")
            model_id = item_def.get("model", {}).get("model") if isinstance(item_def, dict) else None
            if model_id != f"{target.namespace}:item/{target.name}":
                errors.append(f"Item definition not rewired: {target.namespace}:{target.name} -> {model_id}")
            model = read_json(asset_root / "models/item" / f"{target.name}.json")
            layer = model.get("textures", {}).get("layer0") if isinstance(model, dict) else None
            if layer != f"{target.namespace}:item/{target.name}":
                errors.append(f"Item model texture not rewired: {target.namespace}:{target.name} -> {layer}")
        else:
            model = read_json(asset_root / "models/block" / f"{target.name}.json")
            textures = model.get("textures", {}) if isinstance(model, dict) else {}
            if not isinstance(textures, dict):
                errors.append(f"Block model textures invalid: {target.namespace}:{target.name}")
                continue
            for key, value in textures.items():
                if isinstance(value, str) and value.startswith("minecraft:"):
                    errors.append(f"Block model still uses vanilla texture: {target.namespace}:{target.name} {key} -> {value}")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    item_targets, block_targets = discover_targets()
    targets = item_targets + block_targets
    if args.dry_run:
        print(f"item targets: {len(item_targets)}")
        print(f"block targets: {len(block_targets)}")
        for target in targets:
            print(f"{target.namespace}\t{target.kind}\t{target.name}\tpage={target.page + 1}\tcell={target.cell}")
        return 0

    for target in item_targets:
        update_item_resources(target)
    for target in block_targets:
        update_block_resources(target)

    for kind, kind_targets in (("item", item_targets), ("block", block_targets)):
        namespaces = sorted({target.namespace for target in kind_targets})
        for namespace in namespaces:
            pages = sorted({target.page for target in kind_targets if target.namespace == namespace})
            for page in pages:
                write_sheet(kind_targets, namespace, kind, page)

    OUT.mkdir(parents=True, exist_ok=True)
    write_json(OUT / "missing_addon_texture_manifest.json", [asdict(target) for target in targets])

    errors = validate_targets(targets) + audit_resource_refs(targets)
    if errors:
        for error in errors:
            print(error)
        return 1

    print(f"generated item textures: {len(item_targets)}")
    print(f"generated block textures: {len(block_targets)}")
    print(f"wrote manifest: {(OUT / 'missing_addon_texture_manifest.json').relative_to(ROOT).as_posix()}")
    print(f"wrote sheets: {(OUT / 'sheets').relative_to(ROOT).as_posix()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
