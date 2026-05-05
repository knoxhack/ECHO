#!/usr/bin/env python3
"""Scan recent ECHO runtime logs for startup crash signatures."""

from __future__ import annotations

import argparse
import sys
import time
from pathlib import Path


CRASH_SIGNATURES = (
    "Crash report saved to",
    "Encountered exception during server tick loop",
    "NoClassDefFoundError",
    "ClassNotFoundException",
    "ExceptionInInitializerError",
    "Could not execute entrypoint",
    "Mod loading error has occurred",
    "Loading errors encountered",
    "Error during pre-loading phase",
    "Mixin apply failed",
)

IGNORED_TEST_WARNINGS = (
    "test hazard provider failure",
    "test route provider failure",
    "test diagnostic provider failure",
    "test profile provider failure",
    "test profile save failure",
    "test ledger provider failure",
    "test ledger save failure",
    "test terminal placement failure",
    "test terminal block state failure",
    "test terminal block check failure",
    "test reward store failure",
    "test reward claim failure",
    "test reward count failure",
    "test nexus path failure",
    "test campaign path failure",
    "test campaign instability failure",
    "test campaign warfront failure",
    "test campaign final failure",
    "test campaign status failure",
    "test intel mirror failure",
    "test terminal action failure",
    "test terminal mission chapter failure",
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--max-age-minutes",
        type=float,
        default=180.0,
        help="Only scan logs and crash reports modified within this many minutes.",
    )
    return parser.parse_args()


def is_recent(path: Path, cutoff: float) -> bool:
    try:
        return path.stat().st_mtime >= cutoff
    except OSError:
        return False


def candidate_files(root: Path, cutoff: float) -> list[Path]:
    files: list[Path] = []
    for directory in root.rglob("*"):
        if not directory.is_dir():
            continue
        parent_name = directory.parent.name.lower()
        name = directory.name.lower()
        if name == "logs" and parent_name.startswith("run"):
            files.extend(path for path in directory.glob("*.log") if is_recent(path, cutoff))
        if name == "crash-reports" and (parent_name.startswith("run") or directory.parent == root):
            files.extend(path for path in directory.glob("*.txt") if is_recent(path, cutoff))
    return sorted(set(files))


def should_ignore(line: str) -> bool:
    lower = line.lower()
    return any(token in lower for token in IGNORED_TEST_WARNINGS)


def scan_file(path: Path) -> list[str]:
    if path.parent.name.lower() == "crash-reports":
        return [f"{path}: fresh crash report present"]
    findings: list[str] = []
    try:
        lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
    except OSError as exc:
        return [f"{path}: could not read file: {exc}"]
    for number, line in enumerate(lines, start=1):
        if should_ignore(line):
            continue
        if any(signature in line for signature in CRASH_SIGNATURES):
            findings.append(f"{path}:{number}: {line.strip()}")
    return findings


def main() -> int:
    args = parse_args()
    root = Path(__file__).resolve().parents[1]
    cutoff = time.time() - max(0.0, args.max_age_minutes) * 60.0
    files = candidate_files(root, cutoff)
    findings: list[str] = []
    for path in files:
        findings.extend(scan_file(path))
    if findings:
        print("Fresh ECHO runtime crash signatures found:")
        for finding in findings:
            print(f" - {finding}")
        return 1
    print(f"ECHO runtime log scan clean ({len(files)} recent file(s) checked).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
