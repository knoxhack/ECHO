from __future__ import annotations

import hashlib
import json
import math
import random
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageOps


WORKSPACE = Path(__file__).resolve().parents[3]
GUI_ROOT = WORKSPACE / "src/main/resources/assets/echoterminal/textures/gui"
THEME_ROOT = GUI_ROOT / "themes"
SOURCE_ROOT = WORKSPACE / "art_sources/echoterminal/theme_overhaul"
SHEET_ROOT = SOURCE_ROOT / "source_sheets"
MENU_ICON_SIZE = (128, 128)
MISSION_ICON_SIZE = (256, 256)
MISSION_SHEET_COLUMNS = 4
MISSION_SHEET_ROWS = 4
MISSION_SHEET_CELLS = MISSION_SHEET_COLUMNS * MISSION_SHEET_ROWS

MENU_PROMPTS = {
    "echo_console": (
        "4x4 transparent-ready sheet of crisp cyan/blue/green ECHO terminal hologram menu glyphs "
        "on a flat #00ff00 chroma key."
    ),
    "nexus_modpack": (
        "4x4 transparent-ready sheet of crisp amber/gold/violet Nexus industrial terminal menu glyphs "
        "on a flat #ff00ff chroma key."
    ),
}

MISSION_PROMPT = (
    "Built-in imagegen 4x4 opaque mission art sheets, grouped by theme and chapter, then sliced "
    "into 256x256 runtime mission icons."
)


@dataclass(frozen=True)
class Palette:
    folder: str
    display: str
    bg: tuple[int, int, int]
    shell: tuple[int, int, int]
    mid: tuple[int, int, int]
    accent: tuple[int, int, int]
    accent2: tuple[int, int, int]
    hi: tuple[int, int, int]
    success: tuple[int, int, int]
    warning: tuple[int, int, int]
    danger: tuple[int, int, int]
    info: tuple[int, int, int]
    violet: tuple[int, int, int]


PALETTES = {
    "echo_console": Palette(
        "echo_console",
        "ECHO Console",
        (2, 7, 12),
        (5, 13, 20),
        (18, 50, 65),
        (102, 232, 255),
        (46, 142, 157),
        (233, 251, 255),
        (146, 247, 166),
        (255, 209, 102),
        (255, 143, 163),
        (159, 209, 255),
        (158, 176, 255),
    ),
    "nexus_modpack": Palette(
        "nexus_modpack",
        "Nexus Modpack",
        (6, 7, 6),
        (11, 14, 13),
        (48, 31, 24),
        (255, 211, 106),
        (146, 125, 73),
        (255, 246, 222),
        (167, 240, 108),
        (255, 200, 90),
        (255, 110, 110),
        (116, 217, 255),
        (166, 108, 255),
    ),
}


CHAPTERS = {
    "minecraft": ("baseline", (143, 227, 107), (184, 135, 74)),
    "echoashfallprotocol": ("ashfall", (255, 154, 90), (255, 110, 110)),
    "echoindustrialnexus": ("industrial", (255, 200, 90), (116, 217, 255)),
    "echonexusprotocol": ("nexus_protocol", (166, 108, 255), (116, 217, 255)),
    "echoorbitalremnants": ("orbital", (116, 217, 255), (166, 108, 255)),
    "echostationfall": ("stationfall", (255, 110, 110), (255, 200, 90)),
    "echoblackboxprotocol": ("blackbox", (198, 168, 255), (141, 132, 117)),
}

BANNERS = [
    "echo_console",
    "nexus_modpack",
    "baseline",
    "ashfall",
    "industrial",
    "nexus_protocol",
    "orbital",
    "stationfall",
    "blackbox",
]

PANELS = [
    "panel_plate",
    "panel_selected",
    "panel_hover",
    "panel_minecraft",
    "panel_echoashfallprotocol",
    "panel_echoindustrialnexus",
    "panel_echonexusprotocol",
    "panel_echoorbitalremnants",
    "panel_echostationfall",
    "panel_echoblackboxprotocol",
]

BORDERS = ["panel_border", "divider_rune", "prompt_ornament"]
BACKGROUNDS = ["terminal_backdrop", "loading_portal"]


def stable_int(value: str) -> int:
    return int(hashlib.sha256(value.encode("utf-8")).hexdigest()[:16], 16)


def rng_for(value: str) -> random.Random:
    return random.Random(stable_int(value))


def rgba(rgb: tuple[int, int, int], alpha: int = 255) -> tuple[int, int, int, int]:
    return rgb[0], rgb[1], rgb[2], alpha


def blend(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return tuple(max(0, min(255, round(a[i] * (1.0 - t) + b[i] * t))) for i in range(3))


def ensure_parent(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)


def safe_rel(path: Path) -> str:
    return path.as_posix().replace("\\", "/")


def load_motifs(folder: str) -> list[Image.Image]:
    sheet = SHEET_ROOT / f"{folder}_icons_alpha.png"
    if not sheet.exists():
        return []
    image = Image.open(sheet).convert("RGBA")
    motifs: list[Image.Image] = []
    cols = 4
    rows = 4
    cell_w = image.width // cols
    cell_h = image.height // rows
    for row in range(rows):
        for col in range(cols):
            crop = image.crop((col * cell_w, row * cell_h, (col + 1) * cell_w, (row + 1) * cell_h))
            alpha = crop.getchannel("A")
            box = alpha.point(lambda px: 255 if px > 18 else 0).getbbox()
            if box:
                motifs.append(crop.crop(box))
    return motifs


def tint_alpha(mask: Image.Image, color: tuple[int, int, int], alpha: int) -> Image.Image:
    if mask.mode != "L":
        mask = mask.getchannel("A")
    mask = mask.point(lambda px: min(alpha, int(px * alpha / 255)))
    out = Image.new("RGBA", mask.size, rgba(color, 0))
    out.putalpha(mask)
    return out


def overlay_lines(image: Image.Image, palette: Palette, key: str, heavy: bool) -> None:
    draw = ImageDraw.Draw(image, "RGBA")
    w, h = image.size
    rnd = rng_for(key)
    step_y = max(4, h // (48 if heavy else 36))
    step_x = max(12, w // (36 if heavy else 24))
    for y in range(rnd.randrange(step_y), h, step_y):
        alpha = 18 if heavy else 12
        draw.line((0, y, w, y), fill=rgba(palette.accent, alpha), width=1)
    for x in range(rnd.randrange(step_x), w, step_x):
        alpha = 12 if heavy else 8
        draw.line((x, 0, x, h), fill=rgba(palette.accent2, alpha), width=1)
    for _ in range(7 if heavy else 4):
        x0 = rnd.randrange(-w // 4, w)
        y0 = rnd.randrange(0, h)
        length = rnd.randrange(max(24, w // 8), max(32, w // 3))
        color = palette.info if rnd.randrange(3) == 0 else palette.accent
        draw.line((x0, y0, x0 + length, y0 + rnd.randrange(-h // 8, h // 8 + 1)),
                  fill=rgba(color, rnd.randrange(22, 50)), width=rnd.randrange(1, 3))


def add_vignette(image: Image.Image, palette: Palette, strength: int = 92) -> None:
    w, h = image.size
    sw = 96
    sh = max(2, round(sw * h / max(1, w)))
    mask = Image.new("L", (sw, sh), 0)
    pixels = mask.load()
    cx = (sw - 1) / 2.0
    cy = (sh - 1) / 2.0
    max_distance = math.sqrt(cx * cx + cy * cy)
    for y in range(sh):
        for x in range(sw):
            distance = math.sqrt((x - cx) ** 2 + (y - cy) ** 2) / max_distance
            alpha = int(max(0.0, min(1.0, (distance - 0.30) / 0.70)) * strength)
            pixels[x, y] = alpha
    mask = mask.resize((w, h), Image.Resampling.BICUBIC)
    edge = Image.new("RGBA", (w, h), rgba(palette.bg, 0))
    edge.putalpha(mask)
    image.alpha_composite(edge)
    glow_mask = ImageOps.invert(mask).point(lambda px: min(32, px // 8))
    glow = Image.new("RGBA", (w, h), rgba(blend(palette.bg, palette.accent2, 0.35), 0))
    glow.putalpha(glow_mask)
    image.alpha_composite(glow)


def themed_opaque(source: Path, palette: Palette, rel: Path) -> Image.Image:
    base = Image.open(source).convert("RGBA")
    w, h = base.size
    gray = ImageOps.grayscale(base)
    gray = ImageEnhance.Contrast(gray).enhance(1.24)
    colorized = ImageOps.colorize(gray, black=palette.bg, white=palette.hi)
    toned = Image.blend(Image.new("RGB", (w, h), palette.shell), colorized, 0.78).convert("RGBA")
    toned.putalpha(base.getchannel("A"))
    glaze = Image.new("RGBA", (w, h), rgba(blend(palette.bg, palette.mid, 0.35), 88))
    toned.alpha_composite(glaze)
    overlay_lines(toned, palette, safe_rel(rel), heavy=w >= 256 and h >= 128)
    draw = ImageDraw.Draw(toned, "RGBA")
    rnd = rng_for(safe_rel(rel) + palette.folder)
    for _ in range(6):
        x = rnd.randrange(0, max(1, w))
        y = rnd.randrange(0, max(1, h))
        rw = rnd.randrange(max(6, w // 16), max(7, max(8, w // 4)))
        rh = rnd.randrange(max(4, h // 24), max(5, max(6, h // 8)))
        color = palette.accent if rnd.randrange(2) == 0 else palette.warning
        draw.rectangle((x, y, min(w, x + rw), min(h, y + rh)), outline=rgba(color, rnd.randrange(22, 54)), width=1)
    if w >= 256 and h >= 128:
        add_vignette(toned, palette)
    return toned


def polygon_points(cx: float, cy: float, radius: float, sides: int, rotation: float) -> list[tuple[float, float]]:
    return [
        (cx + math.cos(rotation + i * math.tau / sides) * radius,
         cy + math.sin(rotation + i * math.tau / sides) * radius)
        for i in range(sides)
    ]


def draw_keyword(draw: ImageDraw.ImageDraw, name: str, cx: int, cy: int, r: int, palette: Palette) -> None:
    key = name.lower()
    stroke = rgba(palette.hi, 232)
    accent = rgba(palette.accent, 230)
    secondary = rgba(palette.accent2, 210)
    if "lock" in key or "blocker" in key:
        draw.arc((cx - r // 2, cy - r, cx + r // 2, cy), 180, 360, fill=accent, width=max(2, r // 9))
        draw.rounded_rectangle((cx - r // 2, cy - r // 8, cx + r // 2, cy + r // 2),
                               radius=max(2, r // 10), outline=stroke, width=max(2, r // 10))
        draw.line((cx, cy + r // 10, cx, cy + r // 3), fill=secondary, width=max(2, r // 10))
    elif "claim" in key or "reward" in key or "cache" in key:
        draw.polygon([(cx, cy - r), (cx + r, cy - r // 4), (cx + r // 2, cy + r), (cx - r // 2, cy + r),
                      (cx - r, cy - r // 4)], outline=accent, fill=rgba(palette.warning, 32))
        draw.line((cx - r // 2, cy + r // 8, cx - r // 8, cy + r // 2, cx + r // 2, cy - r // 3),
                  fill=stroke, width=max(2, r // 9))
    elif "scan" in key or "view" in key or "vitals" in key:
        draw.ellipse((cx - r, cy - r // 2, cx + r, cy + r // 2), outline=accent, width=max(2, r // 10))
        draw.ellipse((cx - r // 3, cy - r // 3, cx + r // 3, cy + r // 3), outline=stroke, width=max(2, r // 10))
        draw.line((cx - r, cy - r, cx - r // 2, cy - r), fill=secondary, width=max(2, r // 12))
        draw.line((cx + r // 2, cy + r, cx + r, cy + r), fill=secondary, width=max(2, r // 12))
    elif "route" in key or "orbital" in key or "world" in key:
        draw.ellipse((cx - r, cy - r // 2, cx + r, cy + r // 2), outline=secondary, width=max(2, r // 12))
        draw.arc((cx - r, cy - r, cx + r, cy + r), 210, 30, fill=accent, width=max(2, r // 10))
        draw.ellipse((cx - r // 5, cy - r // 5, cx + r // 5, cy + r // 5), fill=stroke)
    elif "combat" in key or "hazard" in key or "warning" in key:
        draw.polygon([(cx, cy - r), (cx + r, cy + r), (cx - r, cy + r)], outline=rgba(palette.danger, 232),
                     fill=rgba(palette.danger, 30))
        draw.line((cx, cy - r // 2, cx, cy + r // 4), fill=stroke, width=max(2, r // 8))
        draw.rectangle((cx - r // 10, cy + r // 2, cx + r // 10, cy + r * 2 // 3), fill=stroke)
    else:
        sides = 6 if stable_int(key) % 2 == 0 else 4
        draw.polygon(polygon_points(cx, cy, r, sides, math.radians(30)), outline=accent, fill=rgba(palette.mid, 44))
        draw.line((cx - r // 2, cy, cx + r // 2, cy), fill=stroke, width=max(2, r // 10))
        draw.line((cx, cy - r // 2, cx, cy + r // 2), fill=secondary, width=max(2, r // 12))


def generated_icon(size: tuple[int, int], palette: Palette, name: str, motifs: list[Image.Image],
                   chapter_hint: str | None = None) -> Image.Image:
    w, h = size
    out = Image.new("RGBA", size, (0, 0, 0, 0))
    glow = Image.new("RGBA", size, (0, 0, 0, 0))
    draw_glow = ImageDraw.Draw(glow, "RGBA")
    draw = ImageDraw.Draw(out, "RGBA")
    rnd = rng_for(name + palette.folder)
    cx, cy = w // 2, h // 2
    r = max(7, min(w, h) * 34 // 100)
    chapter = CHAPTERS.get(chapter_hint or "")
    accent = chapter[1] if chapter else palette.accent
    secondary = chapter[2] if chapter else palette.accent2
    draw_glow.ellipse((cx - r - 4, cy - r - 4, cx + r + 4, cy + r + 4), outline=rgba(accent, 88), width=max(3, r // 5))
    out.alpha_composite(glow.filter(ImageFilter.GaussianBlur(max(2, min(w, h) // 18))))
    if motifs:
        motif = motifs[stable_int(name) % len(motifs)]
        motif_size = max(12, min(w, h) * 58 // 100)
        motif = motif.resize((motif_size, motif_size), Image.Resampling.LANCZOS)
        mask = motif.getchannel("A").filter(ImageFilter.GaussianBlur(0.3))
        tinted = tint_alpha(mask, accent, 120)
        out.alpha_composite(tinted, (cx - motif_size // 2, cy - motif_size // 2))
    outer_sides = [4, 5, 6, 8][rnd.randrange(4)]
    draw.polygon(polygon_points(cx, cy, r + max(4, r // 6), outer_sides, rnd.random() * math.tau),
                 outline=rgba(secondary, 168), fill=rgba(palette.bg, 8))
    draw_keyword(draw, name, cx, cy, r, palette)
    for _ in range(3):
        angle = rnd.random() * math.tau
        x0 = cx + math.cos(angle) * (r + 3)
        y0 = cy + math.sin(angle) * (r + 3)
        x1 = cx + math.cos(angle) * (r + 10)
        y1 = cy + math.sin(angle) * (r + 10)
        draw.line((x0, y0, x1, y1), fill=rgba(accent, 170), width=max(1, min(w, h) // 48))
    return out


def chapter_colors(palette: Palette, chapter_hint: str | None) -> tuple[tuple[int, int, int], tuple[int, int, int]]:
    chapter = CHAPTERS.get(chapter_hint or "")
    if chapter:
        return chapter[1], chapter[2]
    return palette.accent, palette.accent2


def mission_icon_records() -> list[tuple[Path, Path, str]]:
    root = GUI_ROOT / "mission_icons"
    if not root.exists():
        return []
    records: list[tuple[Path, Path, str]] = []
    for source in sorted(root.rglob("*.png")):
        rel = source.relative_to(GUI_ROOT)
        if len(rel.parts) < 3:
            continue
        records.append((source, rel, rel.parts[1]))
    return records


def mission_source_sheets(
        folder: str,
        records: list[tuple[Path, Path, str]]) -> tuple[dict[str, dict], dict[str, tuple[Path, int, int]]]:
    grouped: dict[str, list[tuple[Path, Path, str]]] = {}
    for source, rel, chapter in records:
        grouped.setdefault(chapter, []).append((source, rel, chapter))

    sheet_manifest: dict[str, dict] = {}
    index: dict[str, tuple[Path, int, int]] = {}
    for chapter, items in sorted(grouped.items()):
        pages: dict[str, dict] = {}
        cells: dict[str, dict] = {}
        for i, (_, rel, _) in enumerate(items):
            page = i // MISSION_SHEET_CELLS
            page_index = i % MISSION_SHEET_CELLS
            col = page_index % MISSION_SHEET_COLUMNS
            row = page_index // MISSION_SHEET_COLUMNS
            sheet_path = SHEET_ROOT / "mission_tiles" / folder / f"{chapter}_p{page + 1:02d}.png"
            rel_key = safe_rel(rel)
            pages.setdefault(f"p{page + 1:02d}", {
                "sheet": safe_rel(sheet_path.relative_to(SOURCE_ROOT)),
                "cell_size": MISSION_ICON_SIZE[0],
                "columns": MISSION_SHEET_COLUMNS,
                "rows": MISSION_SHEET_ROWS,
                "cells": {},
            })
            pages[f"p{page + 1:02d}"]["cells"][rel_key] = [col, row]
            cells[rel_key] = {"page": f"p{page + 1:02d}", "cell": [col, row]}
            index[rel_key] = (sheet_path, col, row)
        sheet_manifest[chapter] = {
            "cell_size": MISSION_ICON_SIZE[0],
            "columns": MISSION_SHEET_COLUMNS,
            "rows": MISSION_SHEET_ROWS,
            "pages": pages,
            "page_count": len(pages),
            "count": len(cells),
            "cells": cells,
        }
    return sheet_manifest, index


def mission_tile_from_sheet(
        rel: Path,
        index: dict[str, tuple[Path, int, int]],
        cache: dict[Path, Image.Image]) -> Image.Image | None:
    rel_key = safe_rel(rel)
    entry = index.get(rel_key)
    if entry is None:
        raise FileNotFoundError(f"No imagegen mission sheet mapping for {rel_key}")
    sheet_path, col, row = entry
    sheet = cache.get(sheet_path)
    if sheet is None:
        if not sheet_path.exists():
            raise FileNotFoundError(f"Missing imagegen mission sheet: {safe_rel(sheet_path.relative_to(WORKSPACE))}")
        sheet = Image.open(sheet_path).convert("RGBA")
        expected_size = (MISSION_SHEET_COLUMNS * MISSION_ICON_SIZE[0], MISSION_SHEET_ROWS * MISSION_ICON_SIZE[1])
        if sheet.size != expected_size:
            raise ValueError(f"{safe_rel(sheet_path.relative_to(WORKSPACE))} must be {expected_size}, got {sheet.size}")
        cache[sheet_path] = sheet
    x = col * MISSION_ICON_SIZE[0]
    y = row * MISSION_ICON_SIZE[1]
    return sheet.crop((x, y, x + MISSION_ICON_SIZE[0], y + MISSION_ICON_SIZE[1]))


def generated_panel(size: tuple[int, int], palette: Palette, name: str) -> Image.Image:
    w, h = size
    image = Image.new("RGBA", size, rgba(palette.shell, 220))
    draw = ImageDraw.Draw(image, "RGBA")
    rnd = rng_for(name + palette.folder)
    draw.rectangle((0, 0, w, h), fill=rgba(blend(palette.bg, palette.mid, 0.18), 230))
    draw.line((0, 1, w, 1), fill=rgba(palette.accent, 115), width=1)
    draw.line((0, h - 2, w, h - 2), fill=rgba(palette.accent2, 95), width=1)
    for _ in range(16):
        x = rnd.randrange(0, max(1, w))
        length = rnd.randrange(16, max(17, w // 7))
        color = palette.accent if rnd.randrange(3) else palette.warning
        draw.line((x, rnd.randrange(0, max(1, h)), min(w, x + length), rnd.randrange(0, max(1, h))),
                  fill=rgba(color, rnd.randrange(34, 80)), width=1)
    return image


def generated_banner(size: tuple[int, int], palette: Palette, name: str, motifs: list[Image.Image]) -> Image.Image:
    w, h = size
    image = Image.new("RGBA", size, rgba(palette.bg, 255))
    draw = ImageDraw.Draw(image, "RGBA")
    rnd = rng_for(name + palette.folder)
    for y in range(h):
        t = y / max(1, h - 1)
        color = blend(blend(palette.bg, palette.shell, 0.35), palette.mid, t * 0.45)
        draw.line((0, y, w, y), fill=rgba(color, 255))
    stripe = CHAPTERS.get(next((k for k, v in CHAPTERS.items() if v[0] == name), ""), None)
    accent = stripe[1] if stripe else palette.accent
    secondary = stripe[2] if stripe else palette.accent2
    for _ in range(12):
        x = rnd.randrange(-w // 5, w)
        y = rnd.randrange(0, h)
        draw.polygon([(x, y), (x + rnd.randrange(w // 8, w // 3), max(0, y - rnd.randrange(5, h // 2 + 1))),
                      (x + rnd.randrange(w // 6, w // 2), min(h, y + rnd.randrange(5, h // 2 + 1)))],
                     outline=rgba(accent, rnd.randrange(32, 90)), fill=rgba(secondary, rnd.randrange(8, 28)))
    if motifs:
        motif = motifs[stable_int(name) % len(motifs)]
        mh = max(24, h * 5 // 6)
        mw = max(24, int(motif.width * mh / max(1, motif.height)))
        motif = motif.resize((mw, mh), Image.Resampling.LANCZOS)
        tinted = tint_alpha(motif.getchannel("A"), accent, 78)
        image.alpha_composite(tinted, (w - mw - max(8, w // 24), (h - mh) // 2))
    overlay_lines(image, palette, "banner/" + name, True)
    draw.rectangle((0, 0, w - 1, h - 1), outline=rgba(accent, 110), width=max(1, h // 26))
    return image


def generated_border(size: tuple[int, int], palette: Palette, name: str) -> Image.Image:
    w, h = size
    image = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(image, "RGBA")
    rnd = rng_for(name + palette.folder)
    line_y = h // 2
    draw.line((0, line_y, w, line_y), fill=rgba(palette.accent2, 138), width=max(1, h // 8))
    draw.line((0, max(0, line_y - 2), w, max(0, line_y - 2)), fill=rgba(palette.accent, 76), width=1)
    ticks = max(8, min(44, w // 36))
    for i in range(ticks):
        x = i * w // ticks + rnd.randrange(-4, 5)
        draw.line((x, max(0, line_y - h // 3), x + rnd.randrange(8, 24), min(h - 1, line_y + h // 3)),
                  fill=rgba(palette.warning if i % 5 == 0 else palette.accent, 110), width=1)
    if "prompt" in name:
        r = max(6, min(w, h) // 4)
        draw.polygon(polygon_points(w // 2, h // 2, r, 6, math.radians(30)), outline=rgba(palette.accent, 180))
    return image


def chrome_dimensions() -> dict[str, tuple[int, int]]:
    defaults: dict[str, tuple[int, int]] = {
        "backgrounds/terminal_backdrop.png": (1920, 80),
        "backgrounds/loading_portal.png": (1024, 24),
        "borders/panel_border.png": (1024, 12),
        "borders/divider_rune.png": (1024, 12),
        "borders/prompt_ornament.png": (128, 32),
    }
    for banner in BANNERS:
        defaults[f"banners/{banner}.png"] = (1024, 84)
    for panel in PANELS:
        defaults[f"panels/{panel}.png"] = (1024, 12)
    nexus = THEME_ROOT / "nexus_modpack"
    if nexus.exists():
        for path in nexus.rglob("*.png"):
            rel = safe_rel(path.relative_to(nexus))
            if rel.startswith(("backgrounds/", "banners/", "borders/", "panels/")):
                with Image.open(path) as image:
                    defaults[rel] = image.size
    return defaults


def flat_icon_names() -> list[str]:
    names = set()
    for folder in ("echo_console", "nexus_modpack"):
        icon_dir = THEME_ROOT / folder / "icons"
        if icon_dir.exists():
            names.update(path.stem for path in icon_dir.glob("*.png"))
    defaults = [
        "fallback_unknown",
        "fallback_state",
        "fallback_action",
        "fallback_page",
        "theme_brand",
        "theme_settings",
        "theme_cycle",
        "action_claim",
        "action_turn_in",
        "action_view",
        "action_scan",
        "action_open",
        "action_continue",
        "action_review",
        "action_resolve",
        "action_settings",
        "action_theme_cycle",
        "state_locked",
        "state_active",
        "state_needed",
        "state_open",
        "state_available",
        "state_online",
        "state_claimable",
        "state_complete",
        "state_completed",
        "state_warning",
        "state_blocker",
        "state_empty",
        "state_unknown",
        "reward_cache",
        "reward_inbox",
    ]
    for default in defaults:
        names.add(default)
    for group in [
            "command", "chapters", "intel", "system",
            "protocol", "core", "field", "systems", "nexus", "endgame", "orbital", "addons"]:
        names.add(f"group_{group}")
    for page in ["command_deck", "survival_route", "baseline", "mission_graph", "what_now", "vitals",
                 "route_records", "factions", "reward_inbox", "field_archive", "chapter_guide",
                 "interface_settings"]:
        names.add(f"page_{page}")
    for category in ["survival", "crafting", "tech", "exploration", "combat", "story", "side_ops", "hazard"]:
        names.add(f"mission_category_{category}")
    for key, (short, _, _) in CHAPTERS.items():
        names.add(f"chapter_{key}")
        names.add(f"chapter_{short}")
    return sorted(names)


def export_theme(folder: str, palette: Palette) -> dict[str, object]:
    theme_dir = THEME_ROOT / folder
    motifs = load_motifs(folder)
    mission_records = mission_icon_records()
    mission_sheets, mission_index = mission_source_sheets(folder, mission_records)
    mission_sheet_cache: dict[Path, Image.Image] = {}
    chrome_dims = chrome_dimensions()
    counts = {
        "mirrored_base_textures": 0,
        "flat_cards": 0,
        "chrome": 0,
        "semantic_icons": 0,
        "mission_icons": 0,
        "mission_source_sheets": len(mission_sheets),
    }
    base_files = [
        path for path in GUI_ROOT.rglob("*.png")
        if "themes" not in path.relative_to(GUI_ROOT).parts
    ]
    for source in sorted(base_files):
        rel = source.relative_to(GUI_ROOT)
        dest = theme_dir / rel
        ensure_parent(dest)
        rel_text = safe_rel(rel)
        if rel_text.startswith("mission_icons/"):
            out = mission_tile_from_sheet(rel, mission_index, mission_sheet_cache)
            counts["mission_icons"] += 1
        elif rel_text.startswith("icons/"):
            out = generated_icon(MENU_ICON_SIZE, palette, rel.stem, motifs)
        else:
            out = themed_opaque(source, palette, rel)
        out.save(dest)
        counts["mirrored_base_textures"] += 1
        if rel_text.startswith("terminal/cards/"):
            flat_dest = theme_dir / "cards" / rel.name
            ensure_parent(flat_dest)
            out.save(flat_dest)
            counts["flat_cards"] += 1

    for rel, size in sorted(chrome_dims.items()):
        section, filename = rel.split("/", 1)
        name = Path(filename).stem
        dest = theme_dir / rel
        ensure_parent(dest)
        if section == "backgrounds":
            source = GUI_ROOT / "terminal" / ("terminal_frame_backdrop.png" if name == "terminal_backdrop" else "missions_visual_hero.png")
            out = themed_opaque(source, palette, Path(rel)).resize(size, Image.Resampling.LANCZOS)
        elif section == "banners":
            out = generated_banner(size, palette, name, motifs)
        elif section == "panels":
            out = generated_panel(size, palette, name)
        else:
            out = generated_border(size, palette, name)
        out.save(dest)
        counts["chrome"] += 1

    for name in flat_icon_names():
        dest = theme_dir / "icons" / f"{name}.png"
        ensure_parent(dest)
        chapter_hint = None
        for chapter_key, (short, _, _) in CHAPTERS.items():
            if name.endswith(chapter_key) or name.endswith(short):
                chapter_hint = chapter_key
                break
        generated_icon(MENU_ICON_SIZE, palette, name, motifs, chapter_hint).save(dest)
        counts["semantic_icons"] += 1

    counts["_mission_sheets"] = mission_sheets
    return counts


def validate_menu_icons(folder: str) -> dict[str, int]:
    theme_dir = THEME_ROOT / folder
    checked = 0
    transparent = 0
    size_checked = 0
    correct_size = 0
    for path in sorted((theme_dir / "icons").rglob("*.png")):
        with Image.open(path).convert("RGBA") as image:
            checked += 1
            size_checked += 1
            if image.size == MENU_ICON_SIZE:
                correct_size += 1
            corners = [
                image.getpixel((0, 0))[3],
                image.getpixel((image.width - 1, 0))[3],
                image.getpixel((0, image.height - 1))[3],
                image.getpixel((image.width - 1, image.height - 1))[3],
            ]
            if all(alpha == 0 for alpha in corners):
                transparent += 1
    return {
        "menu_checked": checked,
        "menu_transparent_corners": transparent,
        "menu_size_checked": size_checked,
        "menu_size_128": correct_size,
    }


def validate_mission_icons(folder: str) -> dict[str, int]:
    theme_dir = THEME_ROOT / folder
    checked = 0
    correct_size = 0
    opaque = 0
    background_tiles = 0
    mission_dir = theme_dir / "mission_icons"
    for path in sorted(mission_dir.rglob("*.png")):
        with Image.open(path).convert("RGBA") as image:
            checked += 1
            if image.size == MISSION_ICON_SIZE:
                correct_size += 1
            alpha = image.getchannel("A")
            extrema = alpha.getextrema()
            if extrema == (255, 255):
                opaque += 1
            channel_extrema = image.convert("RGB").getextrema()
            if any(hi - lo >= 18 for lo, hi in channel_extrema):
                background_tiles += 1
    return {
        "mission_checked": checked,
        "mission_size_256": correct_size,
        "mission_opaque_tiles": opaque,
        "mission_non_empty_background": background_tiles,
    }


def write_manifest(results: dict[str, dict[str, object]], sheet_maps: dict[str, dict[str, dict]]) -> None:
    manifest = {
        "workflow": "Built-in imagegen chroma-key menu sheets plus local alpha removal; built-in imagegen mission sheets and deterministic slicing/export.",
        "runtime_assets_root": "src/main/resources/assets/echoterminal/textures/gui/themes",
        "runtime_export_sizes": {
            "menu_icons": f"{MENU_ICON_SIZE[0]}x{MENU_ICON_SIZE[1]}",
            "mission_icons": f"{MISSION_ICON_SIZE[0]}x{MISSION_ICON_SIZE[1]}",
        },
        "source_sheets": {
            "echo_console": {
                "menu": {
                    "chroma_key": "#00ff00",
                    "chroma_sheet": "source_sheets/echo_console_icons_chroma.png",
                    "alpha_sheet": "source_sheets/echo_console_icons_alpha.png",
                    "prompt": MENU_PROMPTS["echo_console"],
                },
                "mission_tiles": sheet_maps.get("echo_console", {}),
            },
            "nexus_modpack": {
                "menu": {
                    "chroma_key": "#ff00ff",
                    "chroma_sheet": "source_sheets/nexus_modpack_icons_chroma.png",
                    "alpha_sheet": "source_sheets/nexus_modpack_icons_alpha.png",
                    "prompt": MENU_PROMPTS["nexus_modpack"],
                },
                "mission_tiles": sheet_maps.get("nexus_modpack", {}),
            },
        },
        "prompts": {
            "echo_console": MENU_PROMPTS["echo_console"],
            "nexus_modpack": MENU_PROMPTS["nexus_modpack"],
            "mission_tiles": MISSION_PROMPT,
        },
        "themes": results,
        "generated_at": datetime.now(timezone.utc).isoformat(),
    }
    path = SOURCE_ROOT / "manifest.json"
    path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    results: dict[str, dict[str, object]] = {}
    sheet_maps: dict[str, dict[str, dict]] = {}
    for folder, palette in PALETTES.items():
        counts = export_theme(folder, palette)
        sheet_maps[folder] = counts.pop("_mission_sheets", {})
        counts.update(validate_menu_icons(folder))
        counts.update(validate_mission_icons(folder))
        results[folder] = counts
    write_manifest(results, sheet_maps)
    print(json.dumps(results, indent=2))


if __name__ == "__main__":
    main()
