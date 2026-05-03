#!/usr/bin/env python3
"""Generate consistent machine texture sheets with GPT Image 2.

The tool discovers machine block textures from block entity registries and
existing model/blockstate JSON, asks GPT Image 2 for 1024x1024 sprite sheets,
then crops each 128x128 cell down to a true 16x16 Minecraft texture.

Default mode is a dry run: it writes manifests and prompts under
build/texture_previews/gpt_machine_textures without calling the API or touching
game assets. Pass --apply to call the Image API and write final textures.
"""

from __future__ import annotations

import argparse
import base64
import hashlib
import json
import math
import os
import random
import re
import textwrap
import urllib.error
import urllib.request
import uuid
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable

from PIL import Image, ImageDraw


REPO_ROOT = Path(__file__).resolve().parents[1]
BUILD_OUT = REPO_ROOT / "build" / "texture_previews" / "gpt_machine_textures"

GRID_SIZE = 8
CELL_SIZE = 128
SHEET_SIZE = GRID_SIZE * CELL_SIZE
FINAL_SIZE = 16
DEFAULT_MODEL = "gpt-image-2-2026-04-21"

FACE_ORDER = ("front", "side", "top", "bottom")
MAPPED_TEXTURE_BLOCKS = {
    "item_pipe",
    "power_cable",
    "reinforced_power_cable",
    "high_voltage_power_cable",
}


@dataclass(frozen=True)
class TextureTarget:
    key: str
    modid: str
    root: Path
    registry: Path

    @property
    def assets(self) -> Path:
        return self.root / "src" / "main" / "resources" / "assets" / self.modid

    @property
    def blockstates(self) -> Path:
        return self.assets / "blockstates"

    @property
    def block_models(self) -> Path:
        return self.assets / "models" / "block"

    @property
    def textures(self) -> Path:
        return self.assets / "textures" / "block"


TARGETS = {
    "ashfall": TextureTarget(
        key="ashfall",
        modid="echoashfallprotocol",
        root=REPO_ROOT,
        registry=REPO_ROOT
        / "src"
        / "main"
        / "java"
        / "com"
        / "knoxhack"
        / "echoashfallprotocol"
        / "registry"
        / "ModBlockEntities.java",
    ),
    "orbital": TextureTarget(
        key="orbital",
        modid="echoorbitalremnants",
        root=REPO_ROOT / "addons" / "echoorbitalremnants",
        registry=REPO_ROOT
        / "addons"
        / "echoorbitalremnants"
        / "src"
        / "main"
        / "java"
        / "com"
        / "knoxhack"
        / "echoorbitalremnants"
        / "registry"
        / "ModBlockEntities.java",
    ),
}


@dataclass(frozen=True)
class TextureJob:
    target: str
    modid: str
    block_id: str
    model_name: str
    state: str
    face: str
    texture_name: str
    texture_path: Path
    source: str

    @property
    def key(self) -> tuple[str, str]:
        return self.target, self.texture_name

    def manifest_row(self) -> dict[str, str]:
        return {
            "target": self.target,
            "modid": self.modid,
            "block": self.block_id,
            "model": self.model_name,
            "state": self.state,
            "face": self.face,
            "texture": self.texture_name,
            "path": rel(self.texture_path),
            "source": self.source,
        }


@dataclass(frozen=True)
class ModelUpdate:
    target: str
    modid: str
    model_name: str
    path: Path
    textures: dict[str, str]

    def manifest_row(self) -> dict[str, Any]:
        return {
            "target": self.target,
            "model": self.model_name,
            "path": rel(self.path),
            "textures": self.textures,
        }


def rel(path: Path) -> str:
    return path.resolve().relative_to(REPO_ROOT.resolve()).as_posix()


def read_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def selected_targets(name: str) -> list[TextureTarget]:
    if name == "all":
        return [TARGETS["ashfall"], TARGETS["orbital"]]
    return [TARGETS[name]]


def registered_machine_blocks(target: TextureTarget) -> list[str]:
    if not target.registry.exists():
        raise FileNotFoundError(f"Missing registry: {target.registry}")
    text = target.registry.read_text(encoding="utf-8", errors="ignore")
    ids = {
        match.group(1).lower()
        for match in re.finditer(r"ModBlocks\.([A-Z0-9_]+)\.get\(\)", text)
    }
    return sorted(ids)


def model_leaf(value: str, modid: str) -> str | None:
    prefix = f"{modid}:block/"
    if not value.startswith(prefix):
        return None
    return value[len(prefix) :].rsplit("/", 1)[-1]


def collect_model_names(value: object, modid: str, out: set[str]) -> None:
    if isinstance(value, dict):
        model = value.get("model")
        if isinstance(model, str):
            prefix = f"{modid}:block/"
            if model.startswith(prefix):
                out.add(model[len(prefix) :].rsplit("/", 1)[-1])
        for child in value.values():
            collect_model_names(child, modid, out)
    elif isinstance(value, list):
        for child in value:
            collect_model_names(child, modid, out)


def model_names_for_block(target: TextureTarget, block_id: str) -> list[str]:
    blockstate = target.blockstates / f"{block_id}.json"
    if not blockstate.exists():
        return [block_id]
    names: set[str] = set()
    collect_model_names(read_json(blockstate), target.modid, names)
    return sorted(names) or [block_id]


def texture_values(model: dict[str, Any], modid: str) -> list[str]:
    values: list[str] = []

    def collect(value: object) -> None:
        if isinstance(value, dict):
            textures = value.get("textures")
            if isinstance(textures, dict):
                for texture in textures.values():
                    if isinstance(texture, str):
                        leaf = model_leaf(texture, modid)
                        if leaf:
                            values.append(leaf)
            for child in value.values():
                collect(child)
        elif isinstance(value, list):
            for child in value:
                collect(child)

    collect(model)
    return values


def state_from_name(name: str) -> str:
    for state in ("active", "error", "repaired"):
        if name == state or name.endswith(f"_{state}") or f"_{state}_" in name:
            return state
    return "base"


def prompt_words(name: str) -> str:
    return name.replace("_", " ")


def face_hint(face: str, block_id: str) -> str:
    if face == "front":
        return "front face with readable controls, ports, gauges, or machine identity"
    if face == "side":
        return "side face with vents, seams, bolts, ribbing, or service panels"
    if face == "top":
        return "top face with access hatches, caps, intake grilles, or conduit sockets"
    if face == "bottom":
        return "bottom face with underside plating, brackets, feet, and darker grime"
    if "cable" in block_id:
        return "mapped cable material tile that works on cube core and connector rods"
    if "pipe" in block_id:
        return "mapped pipe material tile that works on cube core and connector rods"
    return "mapped machine material tile for a custom block model"


def job_for_texture(
    target: TextureTarget,
    block_id: str,
    model_name: str,
    state: str,
    face: str,
    texture_name: str,
    source: str,
) -> TextureJob:
    return TextureJob(
        target=target.key,
        modid=target.modid,
        block_id=block_id,
        model_name=model_name,
        state=state,
        face=face,
        texture_name=texture_name,
        texture_path=target.textures / f"{texture_name}.png",
        source=source,
    )


def cube_model_jobs(
    target: TextureTarget,
    block_id: str,
    model_name: str,
    model: dict[str, Any],
) -> tuple[list[TextureJob], ModelUpdate | None]:
    parent = str(model.get("parent", ""))
    textures = model.get("textures", {})
    if not isinstance(textures, dict):
        textures = {}
    state = state_from_name(model_name)

    if parent == "minecraft:block/cube_all" or "all" in textures:
        face_textures = {
            "front": f"{target.modid}:block/{model_name}_front",
            "side": f"{target.modid}:block/{model_name}_side",
            "top": f"{target.modid}:block/{model_name}_top",
            "bottom": f"{target.modid}:block/{model_name}_bottom",
        }
        jobs = [
            job_for_texture(
                target,
                block_id,
                model_name,
                state,
                face,
                f"{model_name}_{face}",
                "cube_all_expansion",
            )
            for face in FACE_ORDER
        ]
        update_textures = {
            "particle": face_textures["front"],
            "north": face_textures["front"],
            "south": face_textures["side"],
            "east": face_textures["side"],
            "west": face_textures["side"],
            "up": face_textures["top"],
            "down": face_textures["bottom"],
        }
        update = ModelUpdate(
            target=target.key,
            modid=target.modid,
            model_name=model_name,
            path=target.block_models / f"{model_name}.json",
            textures=update_textures,
        )
        return jobs, update

    face_sources = {
        "front": textures.get("north") or textures.get("particle"),
        "side": textures.get("south") or textures.get("east") or textures.get("west"),
        "top": textures.get("up"),
        "bottom": textures.get("down"),
    }
    jobs: list[TextureJob] = []
    for face in FACE_ORDER:
        texture = face_sources.get(face)
        if not isinstance(texture, str):
            texture_name = f"{model_name}_{face}"
        else:
            texture_name = model_leaf(texture, target.modid) or f"{model_name}_{face}"
        jobs.append(
            job_for_texture(
                target,
                block_id,
                model_name,
                state,
                face,
                texture_name,
                "existing_cube_face",
            )
        )
    return jobs, None


def mapped_model_jobs(
    target: TextureTarget,
    block_id: str,
    model_name: str,
    model: dict[str, Any],
) -> list[TextureJob]:
    unique_textures = sorted(set(texture_values(model, target.modid)))
    if not unique_textures:
        return []
    return [
        job_for_texture(
            target,
            block_id,
            model_name,
            state_from_name(model_name),
            "mapped",
            texture_name,
            "mapped_custom_model",
        )
        for texture_name in unique_textures
    ]


def discover_jobs(target: TextureTarget) -> tuple[list[TextureJob], list[ModelUpdate], list[str]]:
    jobs_by_key: dict[tuple[str, str], TextureJob] = {}
    updates_by_path: dict[Path, ModelUpdate] = {}
    warnings: list[str] = []

    for block_id in registered_machine_blocks(target):
        for model_name in model_names_for_block(target, block_id):
            model_path = target.block_models / f"{model_name}.json"
            if not model_path.exists():
                warnings.append(f"{target.key}:{block_id} references missing model {model_name}")
                continue

            model = read_json(model_path)
            if block_id in MAPPED_TEXTURE_BLOCKS:
                model_jobs = mapped_model_jobs(target, block_id, model_name, model)
                update = None
            else:
                model_jobs, update = cube_model_jobs(target, block_id, model_name, model)

            for job in model_jobs:
                jobs_by_key.setdefault(job.key, job)
            if update:
                updates_by_path[update.path] = update

    return sorted(jobs_by_key.values(), key=lambda job: (job.target, job.texture_name)), sorted(
        updates_by_path.values(), key=lambda update: (update.target, update.model_name)
    ), warnings


def chunked(items: list[TextureJob], size: int) -> Iterable[list[TextureJob]]:
    for index in range(0, len(items), size):
        yield items[index : index + size]


def sheet_prompt(sheet_index: int, jobs: list[TextureJob]) -> str:
    rows: list[str] = []
    for index, job in enumerate(jobs):
        row = index // GRID_SIZE + 1
        col = index % GRID_SIZE + 1
        rows.append(
            f"R{row}C{col}: {prompt_words(job.block_id)}; state {job.state}; "
            f"{face_hint(job.face, job.block_id)}; texture id {job.texture_name}."
        )

    return textwrap.dedent(
        f"""
        Create one 1024x1024 sprite sheet for Minecraft Java machine block textures.

        Hard requirements:
        - The sheet is an exact 8 by 8 grid of equal 128x128 cells.
        - No gutters, borders, labels, captions, watermarks, signatures, or UI chrome.
        - Each cell must look like a true 16x16 Minecraft texture enlarged 8x.
        - Every visible mark must align to a 16x16 pixel grid inside its cell.
        - Use hard square pixels only. No blur, antialiasing, gradients, photos, text, or tiny unreadable letters.
        - Keep all cells in one consistent ECHO industrial machine family.
        - Shared style: weathered charcoal frame, worn gray metal plates, dark bevels, subtle rust, compact sci-fi hardware.
        - Keep the named machine identity distinct using small silhouettes, accent colors, vents, gauges, tanks, panels, or ports.
        - Keep sides compatible: front/side/top/bottom for the same machine should feel like the same block.
        - Make all textures opaque and tile-safe at the edges.

        Sheet {sheet_index:03d} cell map:
        {chr(10).join(rows)}

        Empty unused cells, if any, should be neutral dark machine casing in the same style.
        """
    ).strip()


def write_dry_run_outputs(
    jobs: list[TextureJob],
    updates: list[ModelUpdate],
    warnings: list[str],
    target_name: str,
) -> list[dict[str, Any]]:
    out_dir = BUILD_OUT / "dry_run"
    out_dir.mkdir(parents=True, exist_ok=True)
    sheets: list[dict[str, Any]] = []

    for sheet_index, sheet_jobs in enumerate(chunked(jobs, GRID_SIZE * GRID_SIZE), start=1):
        prompt = sheet_prompt(sheet_index, sheet_jobs)
        prompt_path = out_dir / f"{target_name}_sheet_{sheet_index:03d}_prompt.txt"
        prompt_path.write_text(prompt + "\n", encoding="utf-8")
        sheets.append(
            {
                "index": sheet_index,
                "prompt": rel(prompt_path),
                "jobs": [job.manifest_row() for job in sheet_jobs],
            }
        )

    manifest = {
        "mode": "dry-run",
        "target": target_name,
        "cellSize": CELL_SIZE,
        "gridSize": GRID_SIZE,
        "finalSize": FINAL_SIZE,
        "jobs": [job.manifest_row() for job in jobs],
        "modelUpdates": [update.manifest_row() for update in updates],
        "sheets": sheets,
        "warnings": warnings,
    }
    write_json(out_dir / f"{target_name}_manifest.json", manifest)
    return sheets


def call_openai_image_api(
    prompt: str,
    model: str,
    quality: str,
    reference_sheet: Path | None,
) -> bytes:
    if not os.environ.get("OPENAI_API_KEY"):
        raise RuntimeError("OPENAI_API_KEY is required for --apply.")

    try:
        from openai import OpenAI
    except ImportError:
        return call_openai_image_api_http(prompt, model, quality, reference_sheet)

    client = OpenAI()
    if reference_sheet:
        with reference_sheet.open("rb") as image_file:
            result = client.images.edit(
                model=model,
                image=[image_file],
                prompt=prompt,
                size=f"{SHEET_SIZE}x{SHEET_SIZE}",
                quality=quality,
            )
    else:
        result = client.images.generate(
            model=model,
            prompt=prompt,
            size=f"{SHEET_SIZE}x{SHEET_SIZE}",
            quality=quality,
        )

    image_data = result.data[0].b64_json
    if not image_data:
        raise RuntimeError("Image API response did not include b64_json image data.")
    return base64.b64decode(image_data)


def openai_headers(content_type: str) -> dict[str, str]:
    headers = {
        "Authorization": f"Bearer {os.environ['OPENAI_API_KEY']}",
        "Content-Type": content_type,
    }
    organization = os.environ.get("OPENAI_ORG_ID") or os.environ.get("OPENAI_ORGANIZATION")
    project = os.environ.get("OPENAI_PROJECT_ID") or os.environ.get("OPENAI_PROJECT")
    if organization:
        headers["OpenAI-Organization"] = organization
    if project:
        headers["OpenAI-Project"] = project
    return headers


def read_api_response(request: urllib.request.Request) -> dict[str, Any]:
    try:
        with urllib.request.urlopen(request, timeout=300) as response:
            body = response.read().decode("utf-8")
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"OpenAI Image API returned HTTP {exc.code}: {body}") from exc
    except urllib.error.URLError as exc:
        raise RuntimeError(f"OpenAI Image API request failed: {exc}") from exc
    return json.loads(body)


def decode_image_response(payload: dict[str, Any]) -> bytes:
    data = payload.get("data")
    if not isinstance(data, list) or not data:
        raise RuntimeError(f"Image API response did not include data: {payload}")
    image_data = data[0].get("b64_json") if isinstance(data[0], dict) else None
    if not image_data:
        raise RuntimeError(f"Image API response did not include b64_json image data: {payload}")
    return base64.b64decode(image_data)


def call_openai_image_api_http(
    prompt: str,
    model: str,
    quality: str,
    reference_sheet: Path | None,
) -> bytes:
    if reference_sheet:
        return call_openai_image_edit_http(prompt, model, quality, reference_sheet)

    body = json.dumps(
        {
            "model": model,
            "prompt": prompt,
            "size": f"{SHEET_SIZE}x{SHEET_SIZE}",
            "quality": quality,
            "output_format": "png",
        }
    ).encode("utf-8")
    request = urllib.request.Request(
        "https://api.openai.com/v1/images/generations",
        data=body,
        headers=openai_headers("application/json"),
        method="POST",
    )
    return decode_image_response(read_api_response(request))


def multipart_field(boundary: str, name: str, value: str) -> bytes:
    return (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="{name}"\r\n\r\n'
        f"{value}\r\n"
    ).encode("utf-8")


def multipart_file(boundary: str, name: str, path: Path) -> bytes:
    return (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="{name}"; filename="{path.name}"\r\n'
        "Content-Type: image/png\r\n\r\n"
    ).encode("utf-8") + path.read_bytes() + b"\r\n"


def call_openai_image_edit_http(prompt: str, model: str, quality: str, reference_sheet: Path) -> bytes:
    boundary = f"----echo-machine-textures-{uuid.uuid4().hex}"
    body = b"".join(
        [
            multipart_field(boundary, "model", model),
            multipart_field(boundary, "prompt", prompt),
            multipart_field(boundary, "size", f"{SHEET_SIZE}x{SHEET_SIZE}"),
            multipart_field(boundary, "quality", quality),
            multipart_field(boundary, "output_format", "png"),
            multipart_file(boundary, "image", reference_sheet),
            f"--{boundary}--\r\n".encode("utf-8"),
        ]
    )
    request = urllib.request.Request(
        "https://api.openai.com/v1/images/edits",
        data=body,
        headers=openai_headers(f"multipart/form-data; boundary={boundary}"),
        method="POST",
    )
    return decode_image_response(read_api_response(request))


def quantize_opaque(tile: Image.Image, colors: int = 8) -> Image.Image:
    rgba = tile.convert("RGBA")
    opaque = Image.new("RGBA", rgba.size, (20, 22, 24, 255))
    opaque.alpha_composite(rgba)
    quantized = opaque.convert("RGB").quantize(colors=colors).convert("RGBA")
    alpha = Image.new("L", quantized.size, 255)
    quantized.putalpha(alpha)
    return quantized


def stable_rng(key: str) -> random.Random:
    seed = int(hashlib.sha256(key.encode("utf-8")).hexdigest()[:16], 16)
    return random.Random(seed)


def clamp(value: int) -> int:
    return max(0, min(255, value))


def adjust(color: tuple[int, int, int], amount: int) -> tuple[int, int, int]:
    return tuple(clamp(channel + amount) for channel in color)


def accent_for(job: TextureJob) -> tuple[int, int, int]:
    accents = [
        (91, 196, 255),
        (126, 222, 112),
        (236, 177, 80),
        (230, 96, 84),
        (177, 134, 255),
        (105, 216, 202),
        (238, 213, 86),
        (216, 133, 79),
    ]
    rng = stable_rng(f"accent:{job.target}:{job.block_id}")
    accent = accents[rng.randrange(len(accents))]
    if job.state == "active":
        return adjust(accent, 30)
    if job.state == "error":
        return (228, 72, 54)
    return accent


def rect(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], color: tuple[int, int, int]) -> None:
    draw.rectangle(xy, fill=color + (255,))


def draw_local_texture(job: TextureJob) -> Image.Image:
    """Create a deterministic true-16x16 fallback texture for immediate local use."""
    rng = stable_rng(f"local:{job.target}:{job.texture_name}:{job.face}:{job.state}")
    accent = accent_for(job)
    deep = (24, 28, 30)
    shadow = (45, 52, 54)
    plate = (91, 99, 96)
    mid = (119, 128, 122)
    light = (173, 178, 167)
    rust = (148, 87, 49)
    glow = adjust(accent, 24)

    img = Image.new("RGBA", (FINAL_SIZE, FINAL_SIZE), deep + (255,))
    draw = ImageDraw.Draw(img, "RGBA")

    # Shared casing silhouette so every generated cell stays in one machine family.
    rect(draw, (1, 1, 14, 14), shadow)
    rect(draw, (2, 2, 13, 13), plate)
    rect(draw, (2, 2, 13, 3), light)
    rect(draw, (2, 2, 3, 13), light)
    rect(draw, (12, 4, 13, 13), deep)
    rect(draw, (4, 12, 13, 13), deep)
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        rect(draw, (x, y, x, y), mid)

    face = job.face
    block = job.block_id
    if face == "mapped":
        if "cable" in block:
            rect(draw, (0, 7, 15, 9), deep)
            rect(draw, (0, 8, 15, 8), accent)
            rect(draw, (5, 5, 10, 11), shadow)
            rect(draw, (6, 6, 9, 10), mid)
            rect(draw, (7, 7, 8, 9), glow)
        elif "pipe" in block:
            rect(draw, (0, 6, 15, 10), deep)
            rect(draw, (0, 7, 15, 9), mid)
            rect(draw, (5, 4, 10, 12), shadow)
            rect(draw, (6, 5, 9, 11), plate)
            rect(draw, (7, 6, 8, 10), accent)
        return quantize_opaque(img)

    if face == "bottom":
        rect(draw, (4, 4, 11, 11), deep)
        rect(draw, (5, 5, 10, 10), shadow)
        for x in (5, 8, 11):
            rect(draw, (x, 4, x, 11), mid)
        rect(draw, (12, 12, 12, 12), rust)
    elif face == "side":
        for y in (4, 7, 10):
            rect(draw, (4, y, 11, y + 1), deep)
            rect(draw, (5, y, 10, y), mid if y != 7 else accent)
        if rng.random() < 0.5:
            rect(draw, (12, 5, 12, 10), rust)
    elif face == "top":
        rect(draw, (4, 4, 11, 11), deep)
        rect(draw, (5, 5, 10, 10), shadow)
        if any(word in block for word in ("battery", "power", "core", "capacitor", "synthesizer")):
            rect(draw, (6, 6, 9, 9), accent)
            rect(draw, (7, 7, 8, 8), glow)
        else:
            rect(draw, (5, 7, 10, 8), mid)
            rect(draw, (7, 5, 8, 10), mid)
            rect(draw, (7, 7, 8, 8), accent)
    else:
        if any(word in block for word in ("scrubber", "purifier", "condenser")):
            rect(draw, (5, 4, 10, 11), deep)
            rect(draw, (6, 5, 9, 10), accent)
            rect(draw, (6, 7, 9, 8), shadow)
        elif any(word in block for word in ("hopper", "press", "grinder", "recycler", "miner")):
            rect(draw, (4, 5, 11, 10), deep)
            rect(draw, (5, 6, 10, 9), mid)
            rect(draw, (6, 7, 9, 8), shadow)
            rect(draw, (7, 4, 8, 4), accent)
        elif any(word in block for word in ("battery", "power", "generator", "dynamo")):
            rect(draw, (4, 4, 11, 11), deep)
            rect(draw, (5, 5, 10, 10), shadow)
            rect(draw, (6, 6, 9, 9), accent)
            rect(draw, (7, 7, 8, 8), glow)
        else:
            rect(draw, (4, 4, 11, 11), shadow)
            rect(draw, (5, 5, 10, 6), light)
            rect(draw, (5, 9, 10, 10), deep)
            rect(draw, (7, 7, 8, 8), accent)

    # Stable identity marks: small enough to remain Minecraft-like, different enough per machine.
    for _ in range(3):
        x = rng.randrange(3, 13)
        y = rng.randrange(3, 13)
        color = accent if rng.random() < 0.45 else (rust if rng.random() < 0.35 else deep)
        rect(draw, (x, y, x, y), color)

    return quantize_opaque(img)


def updates_with_available_textures(updates: list[ModelUpdate]) -> list[ModelUpdate]:
    available: list[ModelUpdate] = []
    for update in updates:
        target = TARGETS[update.target]
        texture_names = {
            value.split(":", 1)[1].split("/", 1)[1]
            for value in update.textures.values()
            if isinstance(value, str) and value.startswith(f"{target.modid}:block/")
        }
        if all((target.textures / f"{name}.png").exists() for name in texture_names):
            available.append(update)
    return available


def apply_local_generation(
    jobs: list[TextureJob],
    updates: list[ModelUpdate],
    target_name: str,
    offset: int,
    limit: int | None,
) -> None:
    selected_jobs = jobs[offset : offset + limit] if limit is not None else jobs[offset:]
    for job in selected_jobs:
        job.texture_path.parent.mkdir(parents=True, exist_ok=True)
        draw_local_texture(job).save(job.texture_path)

    safe_updates = updates_with_available_textures(updates)
    write_model_updates(safe_updates)

    out_dir = BUILD_OUT / "local"
    out_dir.mkdir(parents=True, exist_ok=True)
    last_index = offset + len(selected_jobs) - 1 if selected_jobs else offset
    batch_name = f"{target_name}_{offset:04d}_{last_index:04d}"
    write_contact_sheet(selected_jobs, out_dir / f"{batch_name}_machine_contact_sheet.png")
    write_json(
        out_dir / f"{batch_name}_manifest.json",
        {
            "mode": "local",
            "target": target_name,
            "offset": offset,
            "limit": limit,
            "jobs": [job.manifest_row() for job in selected_jobs],
            "modelUpdates": [update.manifest_row() for update in safe_updates],
        },
    )


def crop_sheet(sheet_path: Path, jobs: list[TextureJob]) -> None:
    with Image.open(sheet_path) as image:
        sheet = image.convert("RGBA")

    if sheet.size != (SHEET_SIZE, SHEET_SIZE):
        raise ValueError(f"{sheet_path} is {sheet.width}x{sheet.height}, expected {SHEET_SIZE}x{SHEET_SIZE}")

    for index, job in enumerate(jobs):
        col = index % GRID_SIZE
        row = index // GRID_SIZE
        x0 = col * CELL_SIZE
        y0 = row * CELL_SIZE
        cell = sheet.crop((x0, y0, x0 + CELL_SIZE, y0 + CELL_SIZE))
        tile = cell.resize((FINAL_SIZE, FINAL_SIZE), Image.Resampling.NEAREST)
        tile = quantize_opaque(tile)
        job.texture_path.parent.mkdir(parents=True, exist_ok=True)
        tile.save(job.texture_path)


def write_model_updates(updates: list[ModelUpdate]) -> None:
    for update in updates:
        write_json(
            update.path,
            {
                "parent": "minecraft:block/cube",
                "textures": update.textures,
            },
        )


def write_contact_sheet(jobs: list[TextureJob], output: Path) -> None:
    if not jobs:
        return
    scale = 4
    tile_px = FINAL_SIZE * scale
    label_px = 28
    cols = 8
    rows = math.ceil(len(jobs) / cols)
    sheet = Image.new("RGBA", (cols * tile_px, rows * (tile_px + label_px)), (22, 24, 27, 255))
    draw = ImageDraw.Draw(sheet)

    for index, job in enumerate(jobs):
        if not job.texture_path.exists():
            continue
        x = (index % cols) * tile_px
        y = (index // cols) * (tile_px + label_px)
        with Image.open(job.texture_path) as image:
            preview = image.convert("RGBA").resize((tile_px, tile_px), Image.Resampling.NEAREST)
        sheet.alpha_composite(preview, (x, y))
        label = f"{job.block_id[:11]}\n{job.face}:{job.state[:5]}"
        draw.multiline_text((x + 2, y + tile_px + 2), label, fill=(222, 226, 222, 255), spacing=1)

    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(output)


def apply_generation(
    jobs: list[TextureJob],
    updates: list[ModelUpdate],
    target_name: str,
    model: str,
    quality: str,
    reference_sheet: Path | None,
) -> None:
    out_dir = BUILD_OUT / "applied"
    sheet_dir = out_dir / "sheets"
    sheet_dir.mkdir(parents=True, exist_ok=True)

    sheets: list[dict[str, Any]] = []
    for sheet_index, sheet_jobs in enumerate(chunked(jobs, GRID_SIZE * GRID_SIZE), start=1):
        prompt = sheet_prompt(sheet_index, sheet_jobs)
        prompt_path = out_dir / f"{target_name}_sheet_{sheet_index:03d}_prompt.txt"
        sheet_path = sheet_dir / f"{target_name}_sheet_{sheet_index:03d}.png"
        prompt_path.write_text(prompt + "\n", encoding="utf-8")
        sheet_bytes = call_openai_image_api(prompt, model, quality, reference_sheet)
        sheet_path.write_bytes(sheet_bytes)
        crop_sheet(sheet_path, sheet_jobs)
        sheets.append(
            {
                "index": sheet_index,
                "prompt": rel(prompt_path),
                "sourceSheet": rel(sheet_path),
                "jobs": [job.manifest_row() for job in sheet_jobs],
            }
        )

    write_model_updates(updates)
    write_contact_sheet(jobs, out_dir / f"{target_name}_machine_contact_sheet.png")

    manifest = {
        "mode": "applied",
        "target": target_name,
        "model": model,
        "quality": quality,
        "jobs": [job.manifest_row() for job in jobs],
        "modelUpdates": [update.manifest_row() for update in updates],
        "sheets": sheets,
    }
    write_json(out_dir / f"{target_name}_manifest.json", manifest)


def build_manifest(target_name: str) -> tuple[list[TextureJob], list[ModelUpdate], list[str]]:
    all_jobs: list[TextureJob] = []
    all_updates: list[ModelUpdate] = []
    all_warnings: list[str] = []

    for target in selected_targets(target_name):
        jobs, updates, warnings = discover_jobs(target)
        all_jobs.extend(jobs)
        all_updates.extend(updates)
        all_warnings.extend(warnings)

    jobs_by_key: dict[tuple[str, str], TextureJob] = {}
    for job in all_jobs:
        jobs_by_key.setdefault(job.key, job)

    updates_by_path = {update.path: update for update in all_updates}
    return (
        sorted(jobs_by_key.values(), key=lambda job: (job.target, job.texture_name)),
        sorted(updates_by_path.values(), key=lambda update: (update.target, update.model_name)),
        sorted(all_warnings),
    )


def print_summary(
    mode: str,
    target_name: str,
    jobs: list[TextureJob],
    updates: list[ModelUpdate],
    warnings: list[str],
    sheets: int,
) -> None:
    by_target: dict[str, int] = {}
    by_face: dict[str, int] = {}
    for job in jobs:
        by_target[job.target] = by_target.get(job.target, 0) + 1
        by_face[job.face] = by_face.get(job.face, 0) + 1

    print("GPT machine texture pipeline")
    print(f"Mode:          {mode}")
    print(f"Target:        {target_name}")
    print(f"Texture jobs:  {len(jobs)}")
    print(f"Sheet calls:   {sheets}")
    print(f"Model updates: {len(updates)}")
    print(f"By target:     {json.dumps(by_target, sort_keys=True)}")
    print(f"By face:       {json.dumps(by_face, sort_keys=True)}")
    if warnings:
        print("Warnings:")
        for warning in warnings:
            print(f"  - {warning}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--target", choices=("ashfall", "orbital", "all"), default="all")
    parser.add_argument("--quality", choices=("low", "medium", "high"), default="medium")
    parser.add_argument("--model", default=DEFAULT_MODEL)
    parser.add_argument("--reference-sheet", type=Path, help="Optional prior sheet for GPT Image edits.")

    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--dry-run", action="store_true", help="Build manifests and prompts only.")
    mode.add_argument("--apply", action="store_true", help="Call the Image API and write textures/model JSON.")
    mode.add_argument("--local", action="store_true", help="Write deterministic local fallback textures without API calls.")
    parser.add_argument("--offset", type=int, default=0, help="Skip the first N texture jobs for local generation.")
    parser.add_argument("--limit", type=int, help="Only process the first N texture jobs in manifest order.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    jobs, updates, warnings = build_manifest(args.target)
    sheet_count = math.ceil(len(jobs) / (GRID_SIZE * GRID_SIZE))

    if args.apply:
        apply_generation(
            jobs=jobs,
            updates=updates,
            target_name=args.target,
            model=args.model,
            quality=args.quality,
            reference_sheet=args.reference_sheet,
        )
        print_summary("apply", args.target, jobs, updates, warnings, sheet_count)
    elif args.local:
        apply_local_generation(
            jobs=jobs,
            updates=updates,
            target_name=args.target,
            offset=args.offset,
            limit=args.limit,
        )
        processed = jobs[args.offset : args.offset + args.limit] if args.limit is not None else jobs[args.offset:]
        print_summary("local", args.target, processed, updates_with_available_textures(updates), warnings, math.ceil(len(processed) / (GRID_SIZE * GRID_SIZE)))
    else:
        write_dry_run_outputs(jobs, updates, warnings, args.target)
        print_summary("dry-run", args.target, jobs, updates, warnings, sheet_count)
        print(f"Dry-run files: {rel(BUILD_OUT / 'dry_run')}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
