#!/usr/bin/env python3
"""Compatibility wrapper for the canonical POI structure generator.

Older release notes and local muscle memory still mention ``audit_fix.py``.
Keep that entrypoint, but route it through ``generator.py`` so template maps,
validation, and output-root behavior live in exactly one place.
"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path


def main() -> int:
    script_dir = Path(__file__).resolve().parent
    generator = script_dir / "generator.py"

    print("audit_fix.py is deprecated; using generator.py as the canonical POI structure pipeline.")
    generate = subprocess.run([sys.executable, str(generator)], cwd=script_dir.parents[1])
    if generate.returncode != 0:
        return generate.returncode

    return subprocess.run(
        [sys.executable, str(generator), "--check"],
        cwd=script_dir.parents[1],
    ).returncode


if __name__ == "__main__":
    sys.exit(main())
