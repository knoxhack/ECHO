#!/usr/bin/env python3
"""Cut in-game texture atlases from Codex imagegen Echo mob boards."""

from __future__ import annotations

import argparse

from pathlib import Path

from generate_echo_mob_rendercore_assets import ROOT, build_manifest, crop_board_textures


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--modid", help="Only cut boards for one mod id.")
    parser.add_argument("--entity", help="Only cut one entity id. Use with --modid when names overlap.")
    parser.add_argument("--existing-only", action="store_true", help="Skip entities whose docs production board has not been generated yet.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    manifest = build_manifest(args)
    count = 0
    for entry in manifest["entities"]:
        if args.modid and entry["modid"] != args.modid:
            continue
        if args.entity and entry["entity"] != args.entity:
            continue
        if args.existing_only and not Path(ROOT / entry["production_board"]).exists():
            continue
        crop_board_textures(entry)
        count += 1
    print(f"Cut Echo mob board textures for {count} entities.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
