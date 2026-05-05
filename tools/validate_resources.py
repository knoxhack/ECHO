#!/usr/bin/env python3
"""Validate ECHO asset, localization, and packet ID references.

This intentionally checks the failure modes that showed up in packaged-client
logs: missing item/block model parents, missing texture references, raw
advancement/entity translation keys, and invalid custom packet namespaces.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path, PurePosixPath
from typing import Any, Iterable

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
MODS = (
    ("echocore", ROOT / "core/echocore/src/main/resources", ROOT / "core/echocore/src/main/java/com/knoxhack/echocore"),
    (
        "echoterminal",
        ROOT / "addons/echoterminal/src/main/resources",
        ROOT / "addons/echoterminal/src/main/java/com/knoxhack/echoterminal",
    ),
    ("echoashfallprotocol", ROOT / "src/main/resources", ROOT / "src/main/java/com/knoxhack/echoashfallprotocol"),
    (
        "echoorbitalremnants",
        ROOT / "addons/echoorbitalremnants/src/main/resources",
        ROOT / "addons/echoorbitalremnants/src/main/java/com/knoxhack/echoorbitalremnants",
    ),
)
SKIPPED_SCAN_DIRS = {".git", ".gradle", "build", "run", "__pycache__"}
TEXT_SCAN_SUFFIXES = {".gradle", ".java", ".json", ".md", ".properties", ".py", ".toml", ".yaml", ".yml"}
RELEASE_POLISH_SCAN_ROOTS = (
    ROOT / "src/main/java",
    ROOT / "src/main/resources",
    ROOT / "core/echocore/src/main",
    ROOT / "addons/echoterminal/src/main",
    ROOT / "addons/echoorbitalremnants/src/main",
    ROOT / ".github",
    ROOT / "README.md",
    ROOT / "GETTING_STARTED.md",
    ROOT / "MODPACK_OVERVIEW.md",
    ROOT / "PROCEDURAL_STRUCTURES.md",
    ROOT / "build.gradle",
    ROOT / "settings.gradle",
    ROOT / "gradle.properties",
)
MOJIBAKE_CHARS = {
    "\u00c2": "U+00C2",
    "\u00c3": "U+00C3",
    "\u00e2": "U+00E2",
    "\ufffd": "U+FFFD",
}
PLACEHOLDER_MARKERS = (
    "stub implementation",
    "legacy placeholder",
    "structure placeholder",
    "placeholder generator",
    "placeholder approach",
    "manually replaced",
    "not implemented yet",
)
STALE_RELEASE_TOKENS = (
    "data/echoashfallprotocol/structures",
    "data\\echoashfallprotocol\\structures",
    "echoashfallprotocol:echo_terminal",
    "DroneMenu",
    "DRONE_MENU",
)
MODID_CONSTANTS = {
    "EchoCore.MODID": "echocore",
    "EchoTerminal.MODID": "echoterminal",
    "EchoAshfallProtocol.MODID": "echoashfallprotocol",
    "EchoOrbitalRemnants.MODID": "echoorbitalremnants",
}
LOW_DETAIL_BLOCK_EXEMPTIONS = {
    "ash_layer",
    "drop_pod_glass",
    "toxic_puddle",
}
ALLOWED_PIXEL_TEXTURE_SIZES = {
    (16, 16),
    (144, 144),
}
REQUIRED_16X16_BLOCK_TEXTURES = {
    "acid_mud",
    "acidic_sludge",
    "ash_bush",
    "ash_layer",
    "ash_stone",
    "ashen_wasteland_dirt",
    "burnt_fern",
    "burnt_wasteland_soil",
    "concrete_chunk",
    "concrete_rubble",
    "contaminated_soil",
    "cracked_asphalt",
    "cracked_earth",
    "crash_slag",
    "cryogenic_fractured_stone",
    "debris_block",
    "deep_ash",
    "fallout_dust",
    "industrial_aggregate",
    "irradiated_cactus",
    "irradiated_crust",
    "mutated_wasteland_grass_block",
    "nexus_cracked_soil",
    "oil_stained_concrete",
    "permafrost",
    "radioactive_sludge",
    "riftstone",
    "rubble",
    "rusted_metal_debris",
    "rusted_metal_sheet",
    "rusty_wheat",
    "scorched_ash",
    "toxic_moss",
    "toxic_puddle",
    "toxic_slagstone",
    "toxic_wasteland_grass_block",
    "twisted_metal",
    "wasteland_dirt",
    "wasteland_grass",
    "wasteland_grass_block",
    "wasteland_reed",
    "wasteland_stone",
    "wasteland_tall_grass",
    "wasteland_trace_rubble",
}
TERMINAL_GUI_TEXTURE_SIZES = {
    (1920, 1080),
    (2048, 1024),
    (1024, 512),
    (512, 256),
}
TERMINAL_REQUIRED_GUI_TEXTURES = {
    "terminal_frame_backdrop.png",
    "overview_protocol_dashboard.png",
    "missions_visual_hero.png",
    "mission_survival.png",
    "mission_crafting.png",
    "mission_tech.png",
    "mission_exploration.png",
    "mission_combat.png",
    "mission_story.png",
    "mission_side_ops.png",
    "status_hazard_scan.png",
    "drone_command_link.png",
    "archives_dossier_wall.png",
    "codex_field_manual.png",
    "world_route_map.png",
    "nexus_core_interface.png",
    "orbital_route_telemetry.png",
    "addons_module_grid.png",
}
ASHFALL_TERMINAL_BACKGROUNDS = {
    "nexus_main_menu.png": (1920, 1080),
    "world_archive.png": (1920, 1080),
    "create_simulation.png": (1920, 1080),
    "multiplayer_uplink.png": (1920, 1080),
    "loading_boot.png": (1920, 1080),
    "terrain_loading.png": (1920, 1080),
}
ASHFALL_FULL_SURFACE_GRASS_BLOCKS = {
    "echoashfallprotocol:wasteland_grass_block",
    "echoashfallprotocol:toxic_wasteland_grass_block",
    "echoashfallprotocol:mutated_wasteland_grass_block",
}


def expected_terminal_gui_size(file_name: str) -> tuple[int, int] | None:
    if file_name == "terminal_frame_backdrop.png":
        return (1920, 1080)
    if file_name.startswith("cards/"):
        return None
    if file_name.startswith("mission_"):
        return (512, 256)
    if "/" not in file_name:
        return (2048, 1024)
    return None


def rel(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def load_json(path: Path, errors: list[str]) -> Any | None:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # noqa: BLE001 - report any malformed JSON.
        errors.append(f"JSON_PARSE {rel(path)}: {exc}")
        return None


def walk(value: Any) -> Iterable[Any]:
    yield value
    if isinstance(value, dict):
        for item in value.values():
            yield from walk(item)
    elif isinstance(value, list):
        for item in value:
            yield from walk(item)


def parse_ref(ref: str, default_namespace: str) -> tuple[str, str] | None:
    if not ref or ref.startswith("#") or ref.startswith("builtin/"):
        return None
    if ":" in ref:
        namespace, path = ref.split(":", 1)
    else:
        namespace, path = default_namespace, ref
    return namespace, path


def collect_resource_set(root: Path, suffix: str) -> set[str]:
    if not root.exists():
        return set()
    return {path.relative_to(root).as_posix().removesuffix(suffix) for path in root.rglob(f"*{suffix}")}


def check_assets(modid: str, resource_root: Path, errors: list[str]) -> None:
    asset_root = resource_root / f"assets/{modid}"
    data_root = resource_root / f"data/{modid}"
    if not asset_root.exists():
        return

    models = collect_resource_set(asset_root / "models", ".json")
    textures = collect_resource_set(asset_root / "textures", ".png")
    lang_path = asset_root / "lang/en_us.json"
    lang = load_json(lang_path, errors) if lang_path.exists() else {}
    if not isinstance(lang, dict):
        lang = {}

    def model_exists(ref: str) -> bool:
        parsed = parse_ref(ref, modid)
        if parsed is None:
            return True
        namespace, path = parsed
        return namespace != modid or path in models

    def texture_exists(ref: str) -> bool:
        parsed = parse_ref(ref, modid)
        if parsed is None:
            return True
        namespace, path = parsed
        return namespace != modid or path in textures

    for path in asset_root.rglob("*.json"):
        data = load_json(path, errors)
        if data is None:
            continue
        path_text = path.as_posix()

        if "/blockstates/" in path_text or "/items/" in path_text or "/models/item/" in path_text:
            for node in walk(data):
                if isinstance(node, dict) and isinstance(node.get("model"), str) and not model_exists(node["model"]):
                    errors.append(f"MISSING_MODEL_REF {rel(path)}: {node['model']}")

        if "/models/" in path_text:
            parent = data.get("parent") if isinstance(data, dict) else None
            if isinstance(parent, str) and not parent.startswith("minecraft:") and not model_exists(parent):
                errors.append(f"MISSING_PARENT_MODEL {rel(path)}: {parent}")
            for node in walk(data):
                textures_obj = node.get("textures") if isinstance(node, dict) else None
                if not isinstance(textures_obj, dict):
                    continue
                for key, texture in textures_obj.items():
                    if isinstance(texture, str) and not texture.startswith("#") and not texture_exists(texture):
                        errors.append(f"MISSING_TEXTURE_REF {rel(path)}: {key} -> {texture}")

    if lang:
        for key, value in list(lang.items()):
            for prefix in ("item", "block", "entity", "effect", "biome"):
                marker = f"{prefix}.EchoAshfallProtocol."
                if key.startswith(marker):
                    alias = f"{prefix}.{modid}." + key[len(marker) :]
                    if alias not in lang:
                        errors.append(f"MISSING_LANG_ALIAS {rel(lang_path)}: {alias} for {key}")
                    elif lang[alias] != value:
                        errors.append(f"MISMATCHED_LANG_ALIAS {rel(lang_path)}: {alias} for {key}")
            if key == "itemGroup.EchoAshfallProtocol" and f"itemGroup.{modid}" not in lang:
                errors.append(f"MISSING_LANG_ALIAS {rel(lang_path)}: itemGroup.{modid}")

    if data_root.exists() and lang:
        for path in data_root.rglob("*.json"):
            data = load_json(path, errors)
            if data is None:
                continue
            for node in walk(data):
                if isinstance(node, dict) and isinstance(node.get("translate"), str):
                    key = node["translate"]
                    if key not in lang:
                        errors.append(f"MISSING_LANG_TRANSLATE {rel(path)}: {key}")


def check_packet_namespaces(modid: str, java_root: Path, errors: list[str]) -> None:
    if not java_root.exists():
        return
    pattern = re.compile(
        r"Identifier\.fromNamespaceAndPath\(\s*(?:\"([^\"]+)\"|([A-Za-z_][A-Za-z0-9_.]*\.MODID))"
    )
    search_root = java_root / "network"
    files = search_root.rglob("*.java") if search_root.exists() else java_root.rglob("*.java")
    for path in files:
        text = path.read_text(encoding="utf-8")
        for match in pattern.finditer(text):
            namespace, modid_constant = match.groups()
            if namespace is not None:
                if namespace != namespace.lower():
                    errors.append(f"UPPERCASE_PACKET_NAMESPACE {rel(path)}: {namespace}")
                if namespace != modid:
                    errors.append(f"WRONG_PACKET_NAMESPACE {rel(path)}: {namespace}, expected {modid}")
                else:
                    errors.append(f"LITERAL_PACKET_NAMESPACE {rel(path)}: use MODID constant instead of {namespace}")
                continue

            expected_namespace = MODID_CONSTANTS.get(modid_constant)
            if expected_namespace != modid:
                errors.append(f"WRONG_PACKET_NAMESPACE_CONSTANT {rel(path)}: {modid_constant}, expected {modid}")


def iter_repo_text_files() -> Iterable[Path]:
    validator_path = Path(__file__).resolve()
    for path in ROOT.rglob("*"):
        if not path.is_file():
            continue
        if path.resolve() == validator_path:
            continue
        relative_parts = path.relative_to(ROOT).parts
        if any(part in SKIPPED_SCAN_DIRS for part in relative_parts):
            continue
        if path.suffix.lower() not in TEXT_SCAN_SUFFIXES:
            continue
        yield path


def iter_release_polish_files() -> Iterable[Path]:
    seen: set[Path] = set()
    for root in RELEASE_POLISH_SCAN_ROOTS:
        if root.is_file():
            files = (root,)
        elif root.is_dir():
            files = root.rglob("*")
        else:
            continue

        for path in files:
            if not path.is_file():
                continue
            try:
                resolved = path.resolve()
            except OSError:
                continue
            if resolved in seen:
                continue
            seen.add(resolved)

            relative_parts = path.relative_to(ROOT).parts
            if any(part in SKIPPED_SCAN_DIRS for part in relative_parts):
                continue
            if path.suffix.lower() not in TEXT_SCAN_SUFFIXES:
                continue
            yield path


def check_uppercase_resource_namespaces(errors: list[str]) -> None:
    path_patterns = {
        "data/EchoAshfallProtocol": "data/echoashfallprotocol",
        "data\\EchoAshfallProtocol": "data\\echoashfallprotocol",
        "assets/EchoAshfallProtocol": "assets/echoashfallprotocol",
        "assets\\EchoAshfallProtocol": "assets\\echoashfallprotocol",
        "data/EchoOrbitalRemnants": "data/echoorbitalremnants",
        "data\\EchoOrbitalRemnants": "data\\echoorbitalremnants",
        "assets/EchoOrbitalRemnants": "assets/echoorbitalremnants",
        "assets\\EchoOrbitalRemnants": "assets\\echoorbitalremnants",
        "data/EchoCore": "data/echocore",
        "data\\EchoCore": "data\\echocore",
        "assets/EchoCore": "assets/echocore",
        "assets\\EchoCore": "assets\\echocore",
        "data/EchoTerminal": "data/echoterminal",
        "data\\EchoTerminal": "data\\echoterminal",
        "assets/EchoTerminal": "assets/echoterminal",
        "assets\\EchoTerminal": "assets\\echoterminal",
    }
    api_pattern = re.compile(
        r"(?:Identifier|ResourceLocation)\.fromNamespaceAndPath\(\s*\"(EchoAshfallProtocol|EchoOrbitalRemnants|EchoCore|EchoTerminal)\""
    )

    for path in iter_repo_text_files():
        text = path.read_text(encoding="utf-8", errors="ignore")
        for bad, expected in path_patterns.items():
            if bad in text:
                errors.append(f"UPPERCASE_RESOURCE_NAMESPACE {rel(path)}: {bad}, use {expected}")
        for match in api_pattern.finditer(text):
            namespace = match.group(1)
            errors.append(f"UPPERCASE_RESOURCE_NAMESPACE_API {rel(path)}: {namespace}")


def first_line_number(text: str, needle: str) -> int:
    index = text.find(needle)
    if index < 0:
        return 0
    return text.count("\n", 0, index) + 1


def check_release_polish_text(errors: list[str]) -> None:
    for path in iter_release_polish_files():
        text = path.read_text(encoding="utf-8", errors="ignore")
        lower_text = text.lower()
        for char, label in MOJIBAKE_CHARS.items():
            if char in text:
                errors.append(f"MOJIBAKE_TEXT {rel(path)}:{first_line_number(text, char)}: {label}")
        for marker in PLACEHOLDER_MARKERS:
            if marker in lower_text:
                errors.append(f"PLACEHOLDER_TEXT {rel(path)}:{first_line_number(lower_text, marker)}: {marker}")
        for token in STALE_RELEASE_TOKENS:
            if token in text:
                errors.append(f"STALE_RELEASE_REFERENCE {rel(path)}:{first_line_number(text, token)}: {token}")


def check_worldgen_resource_polish(errors: list[str]) -> None:
    data_root = ROOT / "src/main/resources/data/echoashfallprotocol"
    noise_settings = load_json(data_root / "worldgen/noise_settings/wasteland_overworld.json", errors)
    if isinstance(noise_settings, dict):
        surface_refs = {node for node in walk(noise_settings.get("surface_rule", {})) if isinstance(node, str)}
        for ref in sorted(ASHFALL_FULL_SURFACE_GRASS_BLOCKS & surface_refs):
            errors.append(f"FULL_SURFACE_GRASS_BLOCK data/echoashfallprotocol/worldgen/noise_settings/wasteland_overworld.json: {ref}")

    placed_root = data_root / "worldgen/placed_feature"
    if not placed_root.exists():
        return
    for path in placed_root.rglob("*.json"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        for node in walk(data):
            if isinstance(node, dict) and node.get("type") == "minecraft:matching_blocks" and "blocks" in node and "offset" not in node:
                errors.append(f"MISSING_GROUND_PREDICATE_OFFSET {rel(path)}")


def check_required_texture_sizes(errors: list[str]) -> None:
    required = {
        ROOT / "src/main/resources/assets/echoashfallprotocol/textures/entity/warden_boss.png": (128, 64),
        ROOT / "src/main/resources/assets/echoashfallprotocol/textures/entity/wasteland_sentinel.png": (128, 64),
    }
    terminal_root = ROOT / "src/main/resources/assets/echoashfallprotocol/textures/gui/terminal"
    for file_name, expected in ASHFALL_TERMINAL_BACKGROUNDS.items():
        required[terminal_root / file_name] = expected

    for path, expected in required.items():
        if not path.exists():
            errors.append(f"MISSING_TEXTURE {rel(path)}")
            continue
        data = path.read_bytes()
        if not data.startswith(b"\x89PNG\r\n\x1a\n") or len(data) < 24:
            errors.append(f"BAD_PNG {rel(path)}")
            continue
        width = int.from_bytes(data[16:20], "big")
        height = int.from_bytes(data[20:24], "big")
        if (width, height) != expected:
            errors.append(f"BAD_TEXTURE_SIZE {rel(path)}: {width}x{height}, expected {expected[0]}x{expected[1]}")


def check_terminal_visual_assets(errors: list[str]) -> None:
    asset_root = ROOT / "addons/echoterminal/src/main/resources/assets/echoterminal/textures/gui/terminal"
    manifest_path = ROOT / "tools/terminal_art_manifest.json"
    manifest = load_json(manifest_path, errors)
    manifest_assets: dict[str, tuple[int, int]] = {}
    manifest_ids: set[str] = set()
    if isinstance(manifest, dict) and isinstance(manifest.get("assets"), list):
        for entry in manifest["assets"]:
            if not isinstance(entry, dict):
                continue
            asset_id = entry.get("id")
            file_name = entry.get("file")
            size = entry.get("size")
            if not isinstance(asset_id, str) or not asset_id:
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_ID {rel(manifest_path)}: {entry!r}")
                continue
            if asset_id in manifest_ids:
                errors.append(f"DUPLICATE_TERMINAL_ART_MANIFEST_ID {asset_id}")
            manifest_ids.add(asset_id)
            if not isinstance(file_name, str) or not file_name:
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_FILE {asset_id}: {file_name!r}")
                continue
            rel_path = PurePosixPath(file_name)
            if file_name != rel_path.as_posix() or rel_path.is_absolute() or ".." in rel_path.parts:
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_PATH {asset_id}: {file_name}")
                continue
            if not isinstance(size, list) or len(size) != 2:
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_SIZE {file_name}: {size!r}")
                continue
            try:
                expected = (int(size[0]), int(size[1]))
            except (TypeError, ValueError):
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_SIZE {file_name}: {size!r}")
                continue
            if expected not in TERMINAL_GUI_TEXTURE_SIZES:
                allowed = " or ".join(f"{w}x{h}" for w, h in sorted(TERMINAL_GUI_TEXTURE_SIZES))
                errors.append(
                    f"UNSUPPORTED_TERMINAL_ART_MANIFEST_SIZE {file_name}: "
                    f"{expected[0]}x{expected[1]}, expected {allowed}"
                )
            bucket_expected = expected_terminal_gui_size(file_name)
            if bucket_expected is not None and expected != bucket_expected:
                errors.append(
                    f"BAD_TERMINAL_ART_MANIFEST_SIZE {file_name}: "
                    f"{expected[0]}x{expected[1]}, expected {bucket_expected[0]}x{bucket_expected[1]}"
                )
            if file_name in manifest_assets:
                errors.append(f"DUPLICATE_TERMINAL_ART_MANIFEST_FILE {file_name}")
                continue
            manifest_assets[file_name] = expected
    else:
        errors.append(f"MISSING_TERMINAL_ART_MANIFEST {rel(manifest_path)}")

    for file_name in sorted(TERMINAL_REQUIRED_GUI_TEXTURES):
        if file_name not in manifest_assets:
            errors.append(f"MISSING_TERMINAL_ART_MANIFEST_ENTRY {file_name}")

    for file_name, expected in manifest_assets.items():
        path = asset_root / PurePosixPath(file_name)
        if not path.exists():
            errors.append(f"MISSING_TERMINAL_GUI_TEXTURE {rel(path)}")
            continue
        try:
            with Image.open(path) as image:
                size = image.size
        except Exception as exc:  # noqa: BLE001 - report corrupt or unreadable assets.
            errors.append(f"BAD_TERMINAL_GUI_TEXTURE {rel(path)}: {exc}")
            continue
        if size != expected:
            errors.append(f"BAD_TERMINAL_GUI_TEXTURE_SIZE {rel(path)}: {size[0]}x{size[1]}, expected {expected[0]}x{expected[1]}")

    if asset_root.exists():
        for path in asset_root.rglob("*.png"):
            file_name = path.relative_to(asset_root).as_posix()
            if file_name not in manifest_assets:
                errors.append(f"UNMANIFESTED_TERMINAL_GUI_TEXTURE {rel(path)}")
                continue
            try:
                with Image.open(path) as image:
                    size = image.size
            except Exception as exc:  # noqa: BLE001
                errors.append(f"BAD_TERMINAL_GUI_TEXTURE {rel(path)}: {exc}")
                continue
            expected = manifest_assets[file_name]
            if size != expected:
                errors.append(f"BAD_TERMINAL_GUI_TEXTURE_SIZE {rel(path)}: {size[0]}x{size[1]}, expected {expected[0]}x{expected[1]}")


def check_pixel_texture_quality(modid: str, resource_root: Path, errors: list[str]) -> None:
    if modid != "echoashfallprotocol":
        return

    asset_root = resource_root / f"assets/{modid}"
    texture_root = asset_root / "textures"
    if not texture_root.exists():
        return

    for folder in ("block", "item"):
        root = texture_root / folder
        if not root.exists():
            continue
        for path in root.rglob("*.png"):
            try:
                with Image.open(path) as image:
                    rgba = image.convert("RGBA")
                    width, height = rgba.size
                    pixel_data = getattr(rgba, "get_flattened_data", rgba.getdata)
                    pixels = list(pixel_data())
            except Exception as exc:  # noqa: BLE001 - report corrupt or unreadable assets.
                errors.append(f"BAD_PNG {rel(path)}: {exc}")
                continue

            if (width, height) not in ALLOWED_PIXEL_TEXTURE_SIZES:
                expected = " or ".join(f"{w}x{h}" for w, h in sorted(ALLOWED_PIXEL_TEXTURE_SIZES))
                errors.append(f"BAD_PIXEL_TEXTURE_SIZE {rel(path)}: {width}x{height}, expected {expected}")

            if folder == "block" and path.stem in REQUIRED_16X16_BLOCK_TEXTURES and (width, height) != (16, 16):
                errors.append(f"BAD_TERRAIN_TEXTURE_SIZE {rel(path)}: {width}x{height}, expected 16x16")

            if modid != "echoashfallprotocol" or folder != "block":
                continue

            opaque_pixels = [pixel for pixel in pixels if pixel[3] >= 250]
            has_transparency = len(opaque_pixels) != len(pixels)
            if has_transparency or path.stem in LOW_DETAIL_BLOCK_EXEMPTIONS:
                continue

            opaque_colors = {pixel[:3] for pixel in opaque_pixels}
            if len(opaque_colors) < 5:
                errors.append(f"LOW_DETAIL_BLOCK_TEXTURE {rel(path)}: {len(opaque_colors)} opaque colors, expected at least 5")


def check_ashfall_block_model_texture_namespaces(errors: list[str]) -> None:
    model_root = ROOT / "src/main/resources/assets/echoashfallprotocol/models/block"
    if not model_root.exists():
        return

    for path in sorted(model_root.rglob("*.json")):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            errors.append(f"BAD_JSON {rel(path)}: {exc}")
            continue

        for node in walk(data):
            if not isinstance(node, dict):
                continue
            textures_obj = node.get("textures")
            if not isinstance(textures_obj, dict):
                continue
            for key, texture in textures_obj.items():
                if isinstance(texture, str) and texture.startswith("minecraft:"):
                    errors.append(f"VANILLA_BLOCK_MODEL_TEXTURE {rel(path)}: {key} -> {texture}")


def main() -> int:
    errors: list[str] = []
    for modid, resources, java_root in MODS:
        check_assets(modid, resources, errors)
        check_packet_namespaces(modid, java_root, errors)
        check_pixel_texture_quality(modid, resources, errors)
    check_uppercase_resource_namespaces(errors)
    check_release_polish_text(errors)
    check_worldgen_resource_polish(errors)
    check_required_texture_sizes(errors)
    check_terminal_visual_assets(errors)
    check_ashfall_block_model_texture_namespaces(errors)

    if errors:
        for error in errors:
            print(error)
        print(f"Resource validation failed with {len(errors)} issue(s).")
        return 1

    print("Resource validation passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
