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

## Global result

| Grid spacing | Land coverage | Water coverage |
|---:|---:|---:|
| 128 blocks | 3.215% | 96.785% |
| 64 blocks | 3.221% | 96.779% |
| 32 blocks | 3.215% | 96.785% |

The measurements are stable across the three grid resolutions. The atlas remains substantially more ocean-heavy than the requested 85–90% water target. The 34 catalog islands cover only about 3.2% of the full rectangular sampling window.

## Regional result (128-block grid)

Each region is sampled within its own declared bounds using only islands assigned to that region. Region bounds overlap, so regional percentages are not additive and must not be averaged to obtain the global result.

| Region | Island entries | Land | Water |
|---|---:|---:|---:|
| East Blue | 7 | 2.268% | 97.732% |
| West Blue | 2 | 1.178% | 98.822% |
| North Blue | 2 | 0.764% | 99.236% |
| South Blue | 2 | 0.877% | 99.123% |
| Grand Line | 15 | 5.663% | 94.337% |
| New World | 6 | 3.105% | 96.895% |

All six declared regions now have island entries. A geometric pairwise check found no overlaps between the 34 catalog design radii; this does not rule out visual proximity or density-field interactions in generated terrain.

## Next design work

1. Define whether the 85–90% water target applies to the entire bounding rectangle or to a defined playable-world boundary. The current rectangle includes large spaces between regions.
2. Add and place more canonical islands and smaller island chains based on a deliberate route/map plan, rather than blindly enlarging every radius.
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
