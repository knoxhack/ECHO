#!/usr/bin/env python3
"""Cut ECHO Logistics source sheets into Minecraft-ready pixel textures."""

from __future__ import annotations

import json
import math
from pathlib import Path
from typing import Iterable

from PIL import Image, ImageDraw


BASE = Path(__file__).resolve().parents[2]
SOURCE_DIR = Path(__file__).resolve().parent
MANIFEST = SOURCE_DIR / "cut_manifest.json"
PREVIEW_DIR = BASE / "build/texture_previews"

RGBA = tuple[int, int, int, int]


def load_manifest() -> dict:
    return json.loads(MANIFEST.read_text(encoding="utf-8"))


def is_key_pixel(pixel: RGBA, key: tuple[int, int, int]) -> bool:
    r, g, b, a = pixel
    if a == 0:
        return True
    kr, kg, kb = key
    return (
        abs(r - kr) <= 50
        and abs(g - kg) <= 70
        and abs(b - kb) <= 50
        and r >= 190
        and b >= 170
        and g <= 110
    )


def chroma_to_alpha(image: Image.Image, key: tuple[int, int, int]) -> Image.Image:
    rgba = image.convert("RGBA")
    pixels = []
    for pixel in flattened_data(rgba):
        if is_key_pixel(pixel, key):
            pixels.append((0, 0, 0, 0))
        else:
            pixels.append((pixel[0], pixel[1], pixel[2], 255))
    rgba.putdata(pixels)
    return rgba


def quantize_opaque(image: Image.Image, colors: int) -> Image.Image:
    rgba = image.convert("RGBA")
    alpha = rgba.getchannel("A")
    rgb = Image.new("RGB", rgba.size, (0, 0, 0))
    rgb.paste(rgba.convert("RGB"), mask=alpha)
    quantized = rgb.quantize(colors=colors, method=Image.Quantize.MEDIANCUT).convert("RGBA")
    quantized.putalpha(alpha.point(lambda a: 255 if a >= 128 else 0))
    return quantized


def fit_item_sprite(crop: Image.Image) -> Image.Image:
    crop = crop.convert("RGBA")
    crop.thumbnail((14, 14), Image.Resampling.NEAREST)
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    out.alpha_composite(crop, ((16 - crop.width) // 2, (16 - crop.height) // 2))
    return quantize_opaque(out, 12)


def make_block_sprite(crop: Image.Image) -> Image.Image:
    block = crop.convert("RGBA").resize((16, 16), Image.Resampling.NEAREST)
    background = Image.new("RGBA", (16, 16), (30, 33, 33, 255))
    background.alpha_composite(block)
    return quantize_opaque(background, 12)


def draw_rect(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], color: RGBA) -> None:
    draw.rectangle(xy, fill=color)


def draw_panel(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], fill: RGBA, palette: dict[str, RGBA]) -> None:
    x0, y0, x1, y1 = xy
    draw_rect(draw, xy, palette["outline"])
    if x1 - x0 <= 1 or y1 - y0 <= 1:
        return
    draw_rect(draw, (x0 + 1, y0 + 1, x1 - 1, y1 - 1), fill)
    draw.line((x0 + 1, y0 + 1, x1 - 1, y0 + 1), fill=palette["light"])
    draw.line((x0 + 1, y0 + 1, x0 + 1, y1 - 1), fill=palette["light"])
    draw.line((x0 + 1, y1 - 1, x1 - 1, y1 - 1), fill=palette["dark"])
    draw.line((x1 - 1, y0 + 2, x1 - 1, y1 - 1), fill=palette["dark"])


def render_drone_atlas() -> Image.Image:
    palette: dict[str, RGBA] = {
        "outline": (20, 24, 25, 255),
        "dark": (42, 46, 47, 255),
        "mid": (78, 84, 82, 255),
        "panel": (103, 112, 107, 255),
        "light": (169, 184, 176, 255),
        "accent": (38, 222, 225, 255),
        "glow": (142, 255, 246, 255),
        "orange": (238, 143, 24, 255),
        "white": (242, 255, 248, 255),
    }
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # UV islands are aligned with CourierDroneModel texture offsets.
    draw_panel(draw, (0, 0, 27, 14), palette["mid"], palette)
    for x in (4, 12, 20):
        draw.line((x, 3, x + 5, 3), fill=palette["light"])
        draw.line((x, 10, x + 5, 10), fill=palette["dark"])
    draw_rect(draw, (9, 5, 19, 8), palette["panel"])
    draw_rect(draw, (11, 6, 17, 7), palette["accent"])
    draw.point((18, 12), fill=palette["orange"])

    draw_panel(draw, (0, 16, 21, 27), palette["panel"], palette)
    draw_rect(draw, (4, 19, 17, 22), palette["accent"])
    draw_rect(draw, (6, 20, 15, 21), palette["glow"])

    draw_panel(draw, (28, 0, 47, 11), palette["panel"], palette)
    draw_rect(draw, (32, 3, 43, 7), palette["accent"])
    draw_rect(draw, (34, 4, 41, 6), palette["glow"])
    draw_panel(draw, (48, 0, 60, 8), palette["glow"], palette)
    draw_rect(draw, (51, 2, 57, 5), palette["white"])

    draw_panel(draw, (0, 24, 18, 33), palette["panel"], palette)
    draw_rect(draw, (4, 27, 14, 30), palette["dark"])
    draw_panel(draw, (20, 24, 35, 31), palette["accent"], palette)
    draw_rect(draw, (23, 26, 32, 29), palette["glow"])
    draw.point((24, 26), fill=palette["white"])
    draw.point((31, 26), fill=palette["white"])

    draw_panel(draw, (36, 24, 51, 33), palette["glow"], palette)
    draw_rect(draw, (40, 27, 47, 30), palette["white"])
    draw.line((38, 31, 49, 31), fill=palette["accent"])

    draw_panel(draw, (40, 32, 51, 43), palette["dark"], palette)
    for x in (43, 46, 49):
        draw.line((x, 35, x, 41), fill=palette["panel"])
    draw_rect(draw, (42, 40, 50, 42), palette["accent"])
    draw_panel(draw, (52, 32, 63, 38), palette["accent"], palette)
    draw_rect(draw, (55, 34, 60, 36), palette["glow"])

    draw_panel(draw, (0, 40, 13, 49), palette["mid"], palette)
    draw.line((3, 43, 11, 47), fill=palette["accent"])
    draw.line((2, 47, 11, 42), fill=palette["dark"])
    draw_panel(draw, (16, 40, 23, 48), palette["dark"], palette)
    draw_rect(draw, (18, 42, 21, 43), palette["accent"])
    draw.point((20, 41), fill=palette["orange"])

    for x, y in ((29, 18), (33, 18), (29, 21), (33, 21), (57, 13), (61, 13), (57, 17), (61, 17)):
        draw_rect(draw, (x, y, x + 1, y + 1), palette["orange"])

    return img


def write_sheet_assets(group: dict, key: tuple[int, int, int], item: bool) -> list[Path]:
    source = Image.open(SOURCE_DIR / group["source"]).convert("RGBA")
    written: list[Path] = []
    for sprite in group["sprites"]:
        box = tuple(sprite["box"])
        crop = chroma_to_alpha(source.crop(box), key)
        out_img = fit_item_sprite(crop) if item else make_block_sprite(crop)
        out = BASE / group["output_dir"] / f"{sprite['name']}.png"
        out.parent.mkdir(parents=True, exist_ok=True)
        out_img.save(out)
        written.append(out)
    return written


def write_alpha_source(source_name: str, key: tuple[int, int, int]) -> Path:
    source = Image.open(SOURCE_DIR / source_name).convert("RGBA")
    alpha = chroma_to_alpha(source, key)
    out = SOURCE_DIR / source_name.replace("_source.png", "_source_alpha.png")
    alpha.save(out)
    return out


def alpha_counts(path: Path) -> tuple[int, int, set[int]]:
    with Image.open(path) as image:
        rgba = image.convert("RGBA")
        alphas = [pixel[3] for pixel in flattened_data(rgba)]
        transparent = sum(1 for alpha in alphas if alpha == 0)
        opaque = sum(1 for alpha in alphas if alpha > 0)
        return opaque, transparent, set(alphas)


def validate_texture(path: Path, size: tuple[int, int], transparent_required: bool | None, key: tuple[int, int, int]) -> None:
    with Image.open(path) as image:
        rgba = image.convert("RGBA")
        if rgba.size != size:
            raise ValueError(f"{path} is {rgba.size}, expected {size}")
        pixels = list(flattened_data(rgba))
    opaque, transparent, alphas = alpha_counts(path)
    if opaque == 0:
        raise ValueError(f"{path} is fully transparent")
    if transparent_required is True and transparent == 0:
        raise ValueError(f"{path} must have transparent background")
    if transparent_required is False and transparent > 0:
        raise ValueError(f"{path} must be opaque")
    if any(alpha not in {0, 255} for alpha in alphas):
        raise ValueError(f"{path} contains soft alpha")
    visible_key = sum(1 for pixel in pixels if pixel[3] > 0 and is_key_pixel(pixel, key))
    if visible_key:
        raise ValueError(f"{path} contains {visible_key} visible chroma-key pixels")


def paste_scaled(canvas: Image.Image, image: Image.Image, x: int, y: int, scale: int) -> None:
    canvas.alpha_composite(image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST), (x, y))


def flattened_data(image: Image.Image):
    data = getattr(image, "get_flattened_data", None)
    return data() if data else image.getdata()


def write_preview(paths: Iterable[Path]) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    paths = list(paths)
    scale = 6
    cell = 24 * scale
    cols = 8
    rows = math.ceil(len(paths) / cols)
    sheet = Image.new("RGBA", (cols * cell, rows * cell), (24, 27, 27, 255))
    for index, path in enumerate(paths):
        with Image.open(path) as image:
            x = (index % cols) * cell + 4 * scale
            y = (index // cols) * cell + 4 * scale
            paste_scaled(sheet, image.convert("RGBA"), x, y, scale)
    sheet.save(PREVIEW_DIR / "echologisticsnetwork_texture_sheet.png")


def main() -> int:
    manifest = load_manifest()
    key = tuple(manifest["key"])
    write_alpha_source(manifest["items"]["source"], key)
    write_alpha_source(manifest["blocks"]["source"], key)
    write_alpha_source(manifest["entity"]["source"], key)
    written = []
    written.extend(write_sheet_assets(manifest["items"], key, item=True))
    written.extend(write_sheet_assets(manifest["blocks"], key, item=False))

    entity_out = BASE / manifest["entity"]["output"]
    entity_out.parent.mkdir(parents=True, exist_ok=True)
    render_drone_atlas().save(entity_out)
    written.append(entity_out)

    for path in written:
        if "/textures/item/" in path.as_posix():
            validate_texture(path, (16, 16), True, key)
        elif "/textures/block/" in path.as_posix():
            validate_texture(path, (16, 16), False, key)
        else:
            validate_texture(path, (64, 64), None, key)
    write_preview(written)
    print(f"Wrote {len(written)} logistics texture(s).")
    print(f"Wrote preview to {PREVIEW_DIR / 'echologisticsnetwork_texture_sheet.png'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
