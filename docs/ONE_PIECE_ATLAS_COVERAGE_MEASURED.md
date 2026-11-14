# One Piece Atlas Coverage Measurement

## Method

The atlas footprint was sampled on deterministic grids at 128-, 64-, and 32-block spacing. Each sample tests the union of all island footprints and applies the same coordinate-based coastline modulation as `IslandAtlasDensityFunction`. A sample is counted as land once if any island contains it.

The global sampling window is the bounding rectangle of all declared region bounds:

- X: -22,000 to 42,000
- Z: -22,000 to 42,000
- 128-block grid samples: 172,000
- 64-block grid samples: 687,000
- 32-block grid samples: 2,750,000

This is an atlas footprint estimate, not a Minecraft runtime terrain measurement. It does not model density interpolation, biome placement, vertical terrain, surface rules, or chunk-generation behavior.

## Global result (40 islands)

| Grid spacing | Land coverage | Water coverage |
|---:|---:|---:|
| 128 blocks | 4.122% | 95.878% |
| 64 blocks | 4.127% | 95.873% |
| 32 blocks | 4.121% | 95.879% |

The measurements are stable across the three grid resolutions. The atlas has expanded from 34 to 40 islands, but still falls short of the requested 85–90% water target: the global estimate is approximately 95.9% water.

## Regional result (128-block grid)

Each region is sampled within its own declared bounds using only islands assigned to that region. Region bounds overlap, so regional percentages are not additive and must not be averaged to obtain the global result.

| Region | Island entries | Land | Water |
|---|---:|---:|---:|
| East Blue | 8 | 3.120% | 96.880% |
| West Blue | 3 | 1.934% | 98.066% |
| North Blue | 3 | 1.472% | 98.528% |
| South Blue | 3 | 1.737% | 98.263% |
| Grand Line | 16 | 6.203% | 93.797% |
| New World | 7 | 4.211% | 95.789% |

The six latest additions are Tequila Wolf, Spider Miles, Minion Island, Torino Kingdom, Kuraigana Island, and Elbaf. A geometric pairwise check found no overlaps between the 40 catalog design radii. This does not rule out visual proximity or density-field interactions in generated terrain.

## Next design work

1. Decide whether the 85–90% water target applies to the entire bounding rectangle or to a defined playable-world boundary. The current rectangle includes large spaces between regions.
2. Continue adding canonical islands and smaller island chains based on a deliberate route/map plan, rather than blindly enlarging every radius.
3. Re-run the coverage analysis after each atlas expansion.
4. Validate the result in a disposable fresh Minecraft world before considering the world map complete.

## Reproducibility

Run:

```bash
python3 tools/analyze_one_piece_atlas.py --step 128
python3 tools/analyze_one_piece_atlas.py --step 64
python3 tools/analyze_one_piece_atlas.py --step 32
```

The analysis is deterministic for a given catalog and grid spacing.
