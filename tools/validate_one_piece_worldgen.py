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
atlas_density = load_json("worldgen/density_function/one_piece_atlas.json")
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


islands = catalog.get("islands", [])
region_list = catalog.get("regions", [])
region_ids = [item.get("id") for item in region_list]
if len(region_ids) != len(set(region_ids)):
    errors.append("Region catalog contains duplicate IDs")
regions = {item.get("id"): item for item in region_list}
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
warnings = []
for island in islands:
    island_id = island.get("id")
    center = island.get("center", {})
    x, z = center.get("x"), center.get("z")
    radius = island.get("radius")
    if not isinstance(x, int) or not isinstance(z, int):
        errors.append(f"Island {island_id} needs integer x/z coordinates")
        continue
    if not isinstance(center.get("y"), int):
        errors.append(f"Island {island_id} needs an integer planned center y")
    if not isinstance(radius, int) or radius <= 0:
        errors.append(f"Island {island_id} needs a positive radius")
    region = regions.get(island.get("region"))
    if region is None:
        errors.append(f"Island {island_id} references unknown region {island.get('region')}")
    else:
        bounds = region.get("bounds", {})
        if not (bounds.get("minX", x) <= x <= bounds.get("maxX", x)
                and bounds.get("minZ", z) <= z <= bounds.get("maxZ", z)):
            warnings.append(
                f"{island_id} center ({x}, {z}) lies outside region "
                f"{island.get('region')} bounds"
            )

# Empty seas are reported as planning gaps, not auto-filled with invented islands.
for region in region_list:
    region_id = region.get("id")
    if not any(island.get("region") == region_id for island in islands):
        warnings.append(
            f"region {region_id} has no island entries; confirm whether this sea is intentionally empty"
        )

# Overlapping design radii are surfaced for review, not rejected: some locations
# may intentionally form archipelagos or linked island groups.
for index, first in enumerate(islands):
    a = first.get("center", {})
    ar = first.get("radius", 0)
    if not isinstance(a.get("x"), int) or not isinstance(a.get("z"), int):
        continue
    for second in islands[index + 1:]:
        b = second.get("center", {})
        br = second.get("radius", 0)
        if not isinstance(b.get("x"), int) or not isinstance(b.get("z"), int):
            continue
        dx, dz = a["x"] - b["x"], a["z"] - b["z"]
        if dx * dx + dz * dz < (ar + br) * (ar + br):
            warnings.append(
                f"{first.get('id')} and {second.get('id')} design radii overlap"
            )

# Check the density graph recursively so a valid-looking router cannot silently
# stop using the custom island-mass field.
def walk(value):
    if isinstance(value, dict):
        yield value
        for child in value.values():
            yield from walk(child)
    elif isinstance(value, list):
        for child in value:
            yield from walk(child)

for density_key in ("initial_density_without_jaggedness", "final_density"):
    if router.get(density_key) != "nexusbay:one_piece_atlas":
        errors.append(f"{density_key} must reference nexusbay:one_piece_atlas")

atlas_entries = atlas_density.get("islands", [])
atlas_ids = [item.get("id") for item in atlas_entries]
if atlas_density.get("type") != "nexusbay:island_atlas":
    errors.append("Atlas density resource has the wrong custom type")
if len(atlas_ids) != len(set(atlas_ids)):
    errors.append("Atlas density resource contains duplicate island IDs")
if set(atlas_ids) != set(island_ids):
    errors.append(
        "Atlas density/catalog IDs differ; missing="
        + repr(sorted(set(island_ids) - set(atlas_ids)))
        + "; unknown=" + repr(sorted(set(atlas_ids) - set(island_ids)))
    )
profile_by_id = {item.get("islandId"): item for item in profile_list}
catalog_by_id = {item.get("id"): item for item in islands}
for entry in atlas_entries:
    island_id = entry.get("id")
    source = catalog_by_id.get(island_id, {})
    center = source.get("center", {})
    profile = profile_by_id.get(island_id, {})
    if (entry.get("x"), entry.get("z"), entry.get("radius")) != (
        center.get("x"), center.get("z"), source.get("radius")
    ):
        errors.append(f"Atlas density coordinates/radius are stale for {island_id}")
    if entry.get("maxHeight") != profile.get("maxHeight"):
        errors.append(f"Atlas density maxHeight is stale for {island_id}")
    if not 64 <= entry.get("radius", 0) <= 10000:
        errors.append(f"Atlas density radius out of codec range for {island_id}")
    if not 64 <= entry.get("maxHeight", 0) <= 300:
        errors.append(f"Atlas density maxHeight out of codec range for {island_id}")

fluid = settings.get("default_fluid", {})
if fluid.get("Name") != "minecraft:water":
    errors.append("One Piece noise settings must use water as the default fluid")
if settings.get("default_block", {}).get("Name") != "minecraft:stone":
    errors.append("One Piece noise settings must use stone as the default solid block")
if settings.get("noise", {}).get("min_y") != -64 or settings.get("noise", {}).get("height") != 384:
    errors.append("Expected the Minecraft 1.20.1 overworld vertical noise range (-64..319)")

surface = settings.get("surface_rule", {})
if surface.get("type") != "minecraft:sequence":
    errors.append("Surface rule must be a sequence")
else:
    surface_blocks = {
        node.get("then_run", {}).get("result_state", {}).get("Name")
        for node in surface.get("sequence", [])
    }
    surface_blocks.update(
        node.get("result_state", {}).get("Name")
        for node in surface.get("sequence", [])
    )
    if "minecraft:grass_block" not in surface_blocks:
        errors.append("Surface rule is missing the grass_block surface layer")
    if "minecraft:stone" not in surface_blocks:
        errors.append("Surface rule is missing the stone fallback layer")

if errors:
    for error in errors:
        print("FAIL:", error)
    sys.exit(1)

print(
    f"PASS: world preset structure, atlas density resource, and {len(islands)} "
    f"island catalog/profile pairs validated."
)
for warning in warnings:
    print("WARN:", warning)
print(f"INFO: {len(warnings)} atlas layout warning(s) require design review.")
print("NOTE: This does not prove Minecraft can decode the datapack or generate terrain.")
