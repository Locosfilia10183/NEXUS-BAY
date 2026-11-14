#!/usr/bin/env python3
"""Estimate One Piece atlas land/water coverage with the Java coastline formula.

Samples cell centres in the union bounding rectangle of all declared region bounds.
This is a deterministic design estimate, not a Minecraft noise/biome/runtime test.
"""
import argparse
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "forge-mod/src/main/resources/data/nexusbay/islands/island_catalog.json"
TARGET_MIN = 85.0
TARGET_MAX = 90.0
SEA_LEVEL = 63.0


def load_atlas():
    data = json.loads(CATALOG.read_text(encoding="utf-8"))
    regions = data["regions"]
    islands = []
    for item in data["islands"]:
        center = item["center"]
        islands.append({
            "id": item["id"], "region": item["region"],
            "x": center["x"], "z": center["z"],
            "radius": item["radius"],
        })
    return regions, islands


def is_land(x, z, islands):
    for island in islands:
        dx = x - island["x"]
        dz = z - island["z"]
        radius = island["radius"]
        coast = math.sin(x * 0.0061 + z * 0.0037) * math.cos(
            z * 0.0053 - x * 0.0029
        ) * radius * 0.025
        effective_radius = radius + coast
        if dx * dx + dz * dz <= effective_radius * effective_radius:
            return True
    return False


def measure(bounds, islands, step):
    min_x, max_x = bounds["minX"], bounds["maxX"]
    min_z, max_z = bounds["minZ"], bounds["maxZ"]
    total = land = 0
    z = min_z + step / 2.0
    while z < max_z:
        x = min_x + step / 2.0
        while x < max_x:
            total += 1
            if is_land(x, z, islands):
                land += 1
            x += step
        z += step
    land_pct = 100.0 * land / total if total else 0.0
    return total, land, land_pct, 100.0 - land_pct


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--step", type=int, default=128,
                        help="grid spacing in blocks (default: 128)")
    args = parser.parse_args()
    if args.step < 1:
        parser.error("--step must be positive")

    regions, islands = load_atlas()
    bounds = {
        "minX": min(r["bounds"]["minX"] for r in regions),
        "maxX": max(r["bounds"]["maxX"] for r in regions),
        "minZ": min(r["bounds"]["minZ"] for r in regions),
        "maxZ": max(r["bounds"]["maxZ"] for r in regions),
    }
    total, land, land_pct, water_pct = measure(bounds, islands, args.step)
    print("ONE PIECE ATLAS COVERAGE (deterministic grid estimate)")
    print(f"Sampling window: X {bounds['minX']}..{bounds['maxX']}, "
          f"Z {bounds['minZ']}..{bounds['maxZ']}")
    print(f"Grid spacing: {args.step} blocks; samples: {total:,}")
    print(f"Land: {land:,} ({land_pct:.3f}%)")
    print(f"Water: {water_pct:.3f}%")
    if TARGET_MIN <= water_pct <= TARGET_MAX:
        print(f"TARGET: PASS ({TARGET_MIN:.0f}-{TARGET_MAX:.0f}% water)")
    else:
        print(f"TARGET: OUTSIDE {TARGET_MIN:.0f}-{TARGET_MAX:.0f}% water range")

    print("\nREGIONAL COVERAGE (each declared region measured in its own bounds)")
    for region in regions:
        region_islands = [i for i in islands if i["region"] == region["id"]]
        count, region_land, region_land_pct, region_water_pct = measure(
            region["bounds"], region_islands, args.step
        )
        print(f"- {region['name']}: {len(region_islands)} islands; "
              f"land {region_land_pct:.3f}%; water {region_water_pct:.3f}%; "
              f"samples {count:,}")
    print("\nNOTE: Regions overlap geographically; regional percentages are not "
          "additive. The global figure counts the union of land footprints once.")
    print("NOTE: This mirrors the atlas footprint/coastline test only. It does not "
          "model vertical density interpolation, terrain profiles, surface rules, "
          "biomes, or Minecraft runtime generation.")


if __name__ == "__main__":
    main()
