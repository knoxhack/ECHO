#!/usr/bin/env python3
"""Validate generated ECHO mob RenderCore resource coverage."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
MANIFEST_PATH = ROOT / "tools/echo_mob_rendercore_manifest.json"
EXPECTED_ENTITY_COUNT = 80
REQUIRED_VISUAL_FIELDS = {
    "schema_version",
    "base_texture",
    "glow_texture",
    "damaged_overlay_texture",
    "corrupted_overlay_texture",
    "active_overlay_texture",
    "animation_profile",
    "particle_profile",
    "state_animations",
    "anchors",
}
FAMILY_ENUMS = {
    "humanoid": "HUMANOID",
    "survivor_npc": "SURVIVOR_NPC",
    "station_suit": "STATION_SUIT",
    "wraith": "WRAITH",
    "drone": "DRONE",
    "quadruped": "QUADRUPED",
    "crawler": "CRAWLER",
    "slime": "SLIME",
    "heavy_boss": "HEAVY_BOSS",
    "industrial_construct": "INDUSTRIAL_CONSTRUCT",
    "rocket": "ROCKET",
}
JAVA_ROOTS = {
    "echoashfallprotocol": ROOT / "src/main/java",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, default=MANIFEST_PATH)
    parser.add_argument(
        "--require-all-boards",
        action="store_true",
        help="Fail when manifest entries do not yet have promoted docs production boards.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    errors: list[str] = []
    pending_boards: list[str] = []
    manifest = load_json(args.manifest, errors)
    if not isinstance(manifest, dict):
        report(errors)
        return 1

    entities = manifest.get("entities")
    if not isinstance(entities, list):
        errors.append("MANIFEST_MISSING_ENTITIES")
        report(errors)
        return 1
    if len(entities) != EXPECTED_ENTITY_COUNT:
        errors.append(f"BAD_ENTITY_COUNT {len(entities)}, expected {EXPECTED_ENTITY_COUNT}")

    seen: set[tuple[str, str]] = set()
    profile_ids: set[str] = set()
    for index, entry in enumerate(entities):
        if not isinstance(entry, dict):
            errors.append(f"BAD_ENTITY_ENTRY {index}")
            continue
        modid = string(entry, "modid")
        entity = string(entry, "entity")
        key = (modid, entity)
        if key in seen:
            errors.append(f"DUPLICATE_ENTITY {modid}:{entity}")
        seen.add(key)

        profile_id = entry.get("rendercore", {}).get("profile_id") if isinstance(entry.get("rendercore"), dict) else ""
        if not isinstance(profile_id, str) or not profile_id:
            errors.append(f"MISSING_PROFILE_ID {modid}:{entity}")
        elif profile_id in profile_ids:
            errors.append(f"DUPLICATE_PROFILE_ID {profile_id}")
        else:
            profile_ids.add(profile_id)

        validate_textures(entry, errors)
        validate_production_board(entry, errors, pending_boards, args.require_all_boards)
        validate_rendercore(entry, errors)
        validate_renderer_contract(entry, errors)

    validate_source_art(manifest, errors)
    validate_model_blueprints(manifest, errors)
    report(errors, pending_boards)
    return 1 if errors else 0


def validate_textures(entry: dict[str, Any], errors: list[str]) -> None:
    modid = string(entry, "modid")
    entity = string(entry, "entity")
    model = entry.get("model") if isinstance(entry.get("model"), dict) else {}
    expected_size = tuple(model.get("texture_size", []))
    if len(expected_size) != 2:
        errors.append(f"MISSING_TEXTURE_SIZE {modid}:{entity}")
        expected_size = None
    textures = entry.get("textures") if isinstance(entry.get("textures"), dict) else {}
    expected_base_suffix = f"assets/{modid}/textures/entity/rendercore_echo_mobs/{entity}.png"
    base_path = textures.get("base")
    if not isinstance(base_path, str) or not base_path.endswith(expected_base_suffix):
        errors.append(f"BAD_BASE_TEXTURE_PATH {modid}:{entity}: {base_path!r}, expected suffix {expected_base_suffix!r}")
    for key in ("base", "glow", "active_overlay", "damage_overlay", "corrupted_overlay"):
        rel_path = textures.get(key)
        if not isinstance(rel_path, str):
            errors.append(f"MISSING_TEXTURE_PATH {modid}:{entity}:{key}")
            continue
        path = ROOT / rel_path
        if not path.exists():
            errors.append(f"MISSING_TEXTURE {rel(path)}")
            continue
        try:
            with Image.open(path) as image:
                rgba = image.convert("RGBA")
                size = rgba.size
                pixel_data = getattr(rgba, "get_flattened_data", rgba.getdata)
                pixels = list(pixel_data())
        except Exception as exc:  # noqa: BLE001
            errors.append(f"BAD_TEXTURE {rel(path)}: {exc}")
            continue
        if expected_size is not None and size != expected_size:
            errors.append(f"BAD_TEXTURE_SIZE {rel(path)}: {size[0]}x{size[1]}, expected {expected_size[0]}x{expected_size[1]}")
        opaque = sum(1 for pixel in pixels if pixel[3] > 0)
        if opaque == 0:
            errors.append(f"EMPTY_TEXTURE {rel(path)}")
        if key != "base":
            bright = sum(1 for r, g, b, a in pixels if a > 0 and max(r, g, b) > 90)
            if bright == 0:
                errors.append(f"LOW_SIGNAL_OVERLAY {rel(path)}")


def validate_production_board(
    entry: dict[str, Any],
    errors: list[str],
    pending_boards: list[str],
    require_all_boards: bool,
) -> None:
    modid = string(entry, "modid")
    entity = string(entry, "entity")
    board_rel = entry.get("production_board")
    prompt_rel = entry.get("board_prompt")
    source = entry.get("texture_source") if isinstance(entry.get("texture_source"), dict) else {}
    if source.get("mode") != "codex_gpt_imagegen_production_board_crop":
        errors.append(f"BAD_TEXTURE_SOURCE_MODE {modid}:{entity}: {source.get('mode')!r}")
    if not isinstance(board_rel, str):
        errors.append(f"MISSING_PRODUCTION_BOARD_PATH {modid}:{entity}")
    else:
        board_path = ROOT / board_rel
        if not board_path.exists():
            message = f"MISSING_PRODUCTION_BOARD {rel(board_path)}"
            if require_all_boards:
                errors.append(message)
            else:
                pending_boards.append(f"{modid}:{entity}")
        else:
            try:
                with Image.open(board_path) as image:
                    width, height = image.size
                    mode = image.mode
            except Exception as exc:  # noqa: BLE001
                errors.append(f"BAD_PRODUCTION_BOARD {rel(board_path)}: {exc}")
            else:
                if width < 900 or height < 900:
                    errors.append(f"PRODUCTION_BOARD_TOO_SMALL {rel(board_path)}: {width}x{height}")
                if mode not in {"RGB", "RGBA"}:
                    errors.append(f"BAD_PRODUCTION_BOARD_MODE {rel(board_path)}: {mode}")
    if not isinstance(prompt_rel, str):
        errors.append(f"MISSING_BOARD_PROMPT_PATH {modid}:{entity}")
    elif not (ROOT / prompt_rel).exists():
        errors.append(f"MISSING_BOARD_PROMPT {rel(ROOT / prompt_rel)}")


def validate_rendercore(entry: dict[str, Any], errors: list[str]) -> None:
    modid = string(entry, "modid")
    entity = string(entry, "entity")
    rendercore = entry.get("rendercore") if isinstance(entry.get("rendercore"), dict) else {}
    profile_id = rendercore.get("profile_id", "")
    visual = load_json_path(rendercore.get("visual_profile"), errors)
    animations = load_json_path(rendercore.get("animation_profile"), errors)
    particles = load_json_path(rendercore.get("particle_profile"), errors)
    if not isinstance(visual, dict) or not isinstance(animations, dict) or not isinstance(particles, dict):
        return

    missing = sorted(REQUIRED_VISUAL_FIELDS - set(visual))
    if missing:
        errors.append(f"VISUAL_MISSING_FIELDS {profile_id}: {', '.join(missing)}")
    if visual.get("schema_version") != 11:
        errors.append(f"BAD_VISUAL_SCHEMA {profile_id}: {visual.get('schema_version')!r}")
    if visual.get("animation_profile") != profile_id:
        errors.append(f"BAD_ANIMATION_REF {profile_id}: {visual.get('animation_profile')!r}")
    if visual.get("particle_profile") != profile_id:
        errors.append(f"BAD_PARTICLE_REF {profile_id}: {visual.get('particle_profile')!r}")

    state_animations = visual.get("state_animations")
    animation_clips = animations.get("animations")
    if isinstance(state_animations, dict) and isinstance(animation_clips, dict):
        for state, clip in state_animations.items():
            if clip not in animation_clips:
                errors.append(f"MISSING_ANIMATION_CLIP {profile_id}:{state}->{clip}")
    else:
        errors.append(f"BAD_ANIMATION_SHAPE {profile_id}")

    anchors = visual.get("anchors")
    emitters = particles.get("emitters")
    if isinstance(anchors, dict) and isinstance(emitters, dict):
        for emitter_id, emitter in emitters.items():
            if isinstance(emitter, dict):
                anchor = emitter.get("anchor", "")
                if anchor and anchor not in anchors:
                    errors.append(f"MISSING_PARTICLE_ANCHOR {profile_id}:{emitter_id}->{anchor}")
                if emitter.get("rate", 0) > 0.2:
                    errors.append(f"PARTICLE_RATE_TOO_HIGH {profile_id}:{emitter_id} {emitter.get('rate')}")
    else:
        errors.append(f"BAD_PARTICLE_SHAPE {profile_id}")

    textures = entry.get("textures") if isinstance(entry.get("textures"), dict) else {}
    expected_refs = {
        "base_texture": texture_ref(modid, textures.get("base")),
        "glow_texture": texture_ref(modid, textures.get("glow")),
        "active_overlay_texture": texture_ref(modid, textures.get("active_overlay")),
        "damaged_overlay_texture": texture_ref(modid, textures.get("damage_overlay")),
        "corrupted_overlay_texture": texture_ref(modid, textures.get("corrupted_overlay")),
    }
    for key, expected in expected_refs.items():
        if expected and visual.get(key) != expected:
            errors.append(f"BAD_TEXTURE_REF {profile_id}:{key}: {visual.get(key)!r}, expected {expected!r}")

    if not string(entry, "family"):
        errors.append(f"MISSING_FAMILY {modid}:{entity}")
    if not string(entry, "fallback_renderer"):
        errors.append(f"MISSING_FALLBACK_RENDERER {modid}:{entity}")


def validate_renderer_contract(entry: dict[str, Any], errors: list[str]) -> None:
    modid = string(entry, "modid")
    entity = string(entry, "entity")
    family = string(entry, "family")
    root = java_root(modid)
    if root is None or not root.exists():
        errors.append(f"MISSING_JAVA_ROOT {modid}:{entity}")
        return
    java = module_java_text(root)
    if family == "vehicle":
        validate_vehicle_renderer_contract(modid, entity, java, errors)
        return
    if family == "rocket":
        validate_rocket_renderer_contract(modid, entity, java, errors)
        return
    enum_name = FAMILY_ENUMS.get(family)
    if not enum_name:
        errors.append(f"UNKNOWN_RENDERER_FAMILY {modid}:{entity}:{family}")
        return
    renderer_spec = f'renderer("{entity}", EchoMobFamily.{enum_name}'
    count = java.count(renderer_spec)
    if count == 0:
        errors.append(f"MISSING_RENDERER_SPEC {modid}:{entity}: expected {renderer_spec}")
    elif modid != "echoconvoyprotocol" and count < 2:
        errors.append(f"MISSING_RENDERCORE_RENDERER_SPEC {modid}:{entity}: found {count}, expected fallback and RenderCore specs")
    generated_texture = f'textures/entity/rendercore_echo_mobs/{entity}.png'
    if generated_texture not in java and "EchoMobFamilyRenderer" not in java:
        errors.append(f"MISSING_GENERATED_TEXTURE_USAGE {modid}:{entity}")


def validate_vehicle_renderer_contract(modid: str, entity: str, java: str, errors: list[str]) -> None:
    if modid != "echoconvoyprotocol":
        errors.append(f"UNSUPPORTED_VEHICLE_RENDERER_MODULE {modid}:{entity}")
        return
    generated_texture = f'textures/entity/rendercore_echo_mobs/" + kinds[i].getSerializedName() + ".png"'
    if generated_texture not in java:
        errors.append(f"MISSING_VEHICLE_GENERATED_TEXTURE_USAGE {modid}:{entity}")
    if f'"echo_mobs/" + kinds[i].getSerializedName()' not in java:
        errors.append(f"MISSING_VEHICLE_RENDERCORE_PROFILE_USAGE {modid}:{entity}")


def validate_rocket_renderer_contract(modid: str, entity: str, java: str, errors: list[str]) -> None:
    generated_texture = f"textures/entity/rendercore_echo_mobs/{entity}.png"
    profile_id = f"echo_mobs/{entity}"
    if generated_texture not in java:
        errors.append(f"MISSING_ROCKET_GENERATED_TEXTURE_USAGE {modid}:{entity}")
    if profile_id not in java:
        errors.append(f"MISSING_ROCKET_RENDERCORE_PROFILE_USAGE {modid}:{entity}")


def validate_source_art(manifest: dict[str, Any], errors: list[str]) -> None:
    source_art = manifest.get("source_art") if isinstance(manifest.get("source_art"), dict) else {}
    for key in ("prompt_template", "entity_prompt_root", "board_root"):
        rel_path = source_art.get(key)
        if not isinstance(rel_path, str):
            errors.append(f"MISSING_SOURCE_ART {key}")
            continue
        path = ROOT / rel_path
        if not path.exists():
            errors.append(f"MISSING_SOURCE_ART_FILE {rel(path)}")


def validate_model_blueprints(manifest: dict[str, Any], errors: list[str]) -> None:
    families = manifest.get("families") if isinstance(manifest.get("families"), dict) else {}
    blueprints = manifest.get("model_blueprints") if isinstance(manifest.get("model_blueprints"), dict) else {}
    for family, family_data in families.items():
        rel_path = blueprints.get(family)
        if not isinstance(rel_path, str):
            errors.append(f"MISSING_MODEL_BLUEPRINT {family}")
            continue
        data = load_json(ROOT / rel_path, errors)
        if not isinstance(data, dict):
            continue
        if data.get("family") != family:
            errors.append(f"BAD_MODEL_BLUEPRINT_FAMILY {rel_path}: {data.get('family')!r}")
        expected_parts = set(family_data.get("named_parts", [])) if isinstance(family_data, dict) else set()
        actual_parts = {
            part.get("name")
            for part in data.get("parts", [])
            if isinstance(part, dict) and isinstance(part.get("name"), str)
        }
        missing = sorted(expected_parts - actual_parts)
        if missing:
            errors.append(f"MODEL_BLUEPRINT_MISSING_PARTS {family}: {', '.join(missing)}")
        if not isinstance(data.get("anchors"), dict) or not data["anchors"]:
            errors.append(f"MODEL_BLUEPRINT_MISSING_ANCHORS {family}")


def load_json_path(value: Any, errors: list[str]) -> Any | None:
    if not isinstance(value, str):
        errors.append(f"BAD_JSON_PATH {value!r}")
        return None
    return load_json(ROOT / value, errors)


def load_json(path: Path, errors: list[str]) -> Any | None:
    if not path.exists():
        errors.append(f"MISSING_JSON {rel(path)}")
        return None
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # noqa: BLE001
        errors.append(f"BAD_JSON {rel(path)}: {exc}")
        return None


def texture_ref(modid: str, rel_path: Any) -> str:
    if not isinstance(rel_path, str):
        return ""
    marker = f"assets/{modid}/"
    if marker not in rel_path:
        return ""
    texture = rel_path.split(marker, 1)[1]
    return f"{modid}:{texture}"


def string(value: dict[str, Any], key: str) -> str:
    result = value.get(key, "")
    return result if isinstance(result, str) else ""


def java_root(modid: str) -> Path | None:
    if modid in JAVA_ROOTS:
        return JAVA_ROOTS[modid]
    return ROOT / "addons" / modid / "src/main/java"


def module_java_text(root: Path) -> str:
    chunks: list[str] = []
    for path in sorted(root.rglob("*.java")):
        try:
            chunks.append(path.read_text(encoding="utf-8"))
        except UnicodeDecodeError:
            chunks.append(path.read_text(encoding="utf-8", errors="ignore"))
    return "\n".join(chunks)


def report(errors: list[str], pending_boards: list[str] | None = None) -> None:
    if errors:
        for error in errors:
            print(error)
        print(f"Echo mob RenderCore validation failed with {len(errors)} issue(s).")
    else:
        if pending_boards:
            print(f"Pending production boards: {len(pending_boards)}")
        print("Echo mob RenderCore validation passed.")


def rel(path: Path) -> str:
    try:
        return path.resolve().relative_to(ROOT).as_posix()
    except ValueError:
        return str(path)


if __name__ == "__main__":
    sys.exit(main())
