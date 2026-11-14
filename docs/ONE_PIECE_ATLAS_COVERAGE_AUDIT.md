# One Piece atlas coverage audit

## Current geometry estimate

The current density function generates a circular island footprint for each catalog entry. A first-pass area estimate was calculated from the 28 catalog radii and the rectangle spanning the island center coordinates.

- Catalog entries: 28
- Sum of individual circle areas (overlaps counted more than once): approximately 79.2 million square blocks
- Center-coordinate bounding rectangle: 1.9075 billion square blocks
- Naive land-area upper estimate: approximately 4.15%
- Corresponding naive ocean estimate: approximately 95.85%

This is not a measured Minecraft chunk sample. It ignores coastline modulation, circle overlaps, ocean-floor shape, and the actual region bounds. Because overlaps are counted more than once, actual land coverage within this rectangle will be lower than the naive sum suggests.

## Decision

The desired world-scale ocean ratio is 85–90%. The current atlas layout appears more ocean-heavy than that target under the center-bounds rectangle, so do not claim the target is met.

Before changing island radii, define the canonical world sampling window and calculate the union of island footprints (not the sum of circle areas). Then tune island radii and inter-island spacing together. Avoid a global radius multiplier: several nearby locations already overlap and could merge into oversized landmasses.

## Next validation

1. Agree on the actual map sampling window from region bounds.
2. Sample the same deterministic footprint used by the Java density function, including its coastline modulation.
3. Calculate land/ocean ratio using union coverage.
4. Report regional ratios as well as the global ratio.
5. Only tune radii after reviewing the measured map; preserve recognizable sea gaps and canonical island separation.
6. Confirm the ratio from generated chunks in a fresh disposable Forge world. The chunk sample is authoritative; the circle estimate is only a planning diagnostic.

No changes to the live server world or existing saves are part of this audit.
