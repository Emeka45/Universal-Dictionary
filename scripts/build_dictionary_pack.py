#!/usr/bin/env python3
"""Build the bundled Open Dictionary asset pack from its public GitHub source."""
import shutil
import tarfile
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "app" / "src" / "main" / "assets" / "dictionary"
ARCHIVE = Path("/tmp/open-dictionary.tar.gz")
URL = "https://github.com/mhollingshead/open-dictionary/archive/refs/heads/main.tar.gz"

if OUT.exists():
    shutil.rmtree(OUT)
OUT.mkdir(parents=True)

print("Downloading Open Dictionary (260k+ English entries)...")
urllib.request.urlretrieve(URL, ARCHIVE)

with tarfile.open(ARCHIVE, "r:gz") as archive:
    api_root = next(m.name for m in archive.getmembers() if m.name.endswith("/api") and m.isdir())
    for member in archive.getmembers():
        if not member.name.startswith(api_root + "/") or not member.name.endswith(".json"):
            continue
        relative = Path(member.name[len(api_root) + 1:])
        if len(relative.parts) != 2:
            continue
        target = OUT / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        source = archive.extractfile(member)
        if source:
            target.write_bytes(source.read())

print(f"Bundled dictionary pack created: {OUT}")
