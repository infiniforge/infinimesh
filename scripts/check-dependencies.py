#!/usr/bin/env python3
"""Verify this app's library source revision and submodule gitlink."""
from pathlib import Path
import json
import subprocess

root = Path(__file__).resolve().parents[1]
lock = json.loads((root / "dependencies.lock.json").read_text())
for name, pin in lock["repositories"].items():
    relative = f"{lock['directory']}/{name}"
    directory = root / relative
    head = subprocess.check_output(["git", "-C", str(directory), "rev-parse", "HEAD"], text=True).strip()
    if head != pin["commit"]:
        raise SystemExit(f"{name}: checkout {head} differs from locked source {pin['commit']}")
    gitlink = subprocess.check_output(["git", "-C", str(root), "ls-files", "--stage", relative], text=True).split()
    if len(gitlink) != 4 or gitlink[0] != "160000" or gitlink[1] != pin["commit"]:
        raise SystemExit(f"{name}: app gitlink differs from dependencies.lock.json")
    url = subprocess.check_output(["git", "-C", str(root), "config", "-f", ".gitmodules", "--get", f"submodule.{relative}.url"], text=True).strip()
    if url != pin["url"]:
        raise SystemExit(f"{name}: submodule URL differs from source lock")
    dirty = subprocess.check_output(["git", "-C", str(directory), "status", "--porcelain", "--untracked-files=normal"], text=True).strip()
    if dirty:
        raise SystemExit(f"{name}: commit library changes and update the app source pin before building")
print("Library source pins verified.")
