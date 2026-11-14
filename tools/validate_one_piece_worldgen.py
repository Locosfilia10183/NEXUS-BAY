#!/usr/bin/env python3
"""Validate NEXUS-BAY One Piece worldgen JSON and island atlas data.

This is a structural data check, not a Minecraft runtime/world-generation test.
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "forge-mod/src/main/resources/data/nexusbay"

def load_json(relative):
    path = DATA / relative
    try:
        with path.open(encoding="utf-8") as handle:
            return json.load(handle)
    except Exception as exc:
        raise SystemExit(f"FAIL: {path.relative_to(ROOT)}: {exc}")

preset = load_json("worldgen/world_preset/one_piece_ocean.json")
settings = load_json("worldgen/noise_settings/one_piece_ocean.json")
noise = load_json("worldgen/noise/island_mass.json")
catalog = load_json("islands/island_catalog.json")
profiles = load_json("islands/terrain_profiles.json")

errors = []
dimensions = preset.get("dimensions", {})
overworld = dimensions.get("minecraft:overworld", {})
generator = overworld.get("generator", {})
if generator.get("settings") != "nexusbay:one_piece_ocean":
    errors.append("One Piece preset does not reference nexusbay:one_piece_ocean")
if generator.get("type") != "minecraft:noise":
    errors.append("One Piece overworld generator must be minecraft:noise")
if generator.get("biome_source", {}).get("preset") != "minecraft:overworld":
    errors.append("Expected the vanilla multi-noise overworld biome preset")

router = settings.get("noise_router", {})
required_router = {
    "barrier", "fluid_level_floodedness", "fluid_level_spread", "lava",
    "temperature", "vegetation", "continents", "erosion", "depth", "ridges",
    "initial_density_without_jaggedness", "final_density",
    "vein_toggle", "vein_ridged", "vein_gap",
}
missing = sorted(required_router - router.keys())
if missing:
    errors.append("Noise router missing: " + ", ".join(missing))
if settings.get("sea_level") != 63:
    errors.append("Expected sea level 63")
if noise.get("firstOctave") != -4 or not noise.get("amplitudes"):
    errors.append("Island mass noise parameters are incomplete")

islands = catalog.get("islands", [])
profile_list = profiles.get("profiles", [])
island_ids = [item.get("id") for item in islands]
profile_ids = [item.get("islandId") for item in profile_list]
if len(island_ids) != len(set(island_ids)):
    errors.append("Island catalog contains duplicate IDs")
if len(profile_ids) != len(set(profile_ids)):
    errors.append("Terrain profiles contain duplicate island IDs")
if set(island_ids) != set(profile_ids):
    errors.append(
        "Catalog/profile IDs differ; missing profiles="
        + repr(sorted(set(island_ids) - set(profile_ids)))
        + "; unknown profiles=" + repr(sorted(set(profile_ids) - set(island_ids)))
    )
for island in islands:
    center = island.get("center", {})
    if not isinstance(center.get("x"), int) or not isinstance(center.get("z"), int):
        errors.append(f"Island {island.get('id')} needs integer x/z coordinates")
    if not isinstance(island.get("radius"), int) or island["radius"] <= 0:
        errors.append(f"Island {island.get('id')} needs a positive radius")

if errors:
    for error in errors:
        print("FAIL:", error)
    sys.exit(1)

print(
    f"PASS: world preset structure, noise router, and {len(islands)} "
    f"island catalog/profile pairs validated."
)
print("NOTE: This does not prove Minecraft can decode the datapack or generate terrain.")
