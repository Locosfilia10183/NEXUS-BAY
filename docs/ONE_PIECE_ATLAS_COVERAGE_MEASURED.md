# One Piece Atlas Coverage Measurement

## Method

The atlas footprint was sampled on a deterministic 128-block grid. Each sample tests the union of all island footprints and applies the same coordinate-based coastline modulation as `IslandAtlasDensityFunction`. A sample is counted as land once if any island contains it.

The global sampling window is the bounding rectangle of all declared region bounds:

- X: -22,000 to 42,000
- Z: -22,000 to 42,000
- Grid samples: 172,000

This is an atlas footprint estimate, not a Minecraft runtime terrain measurement. It does not model density interpolation, biome placement, vertical terrain, surface rules, or chunk-generation behavior.

## Global result

| Metric | Result |
|---|---:|
| Land samples | 4,831 |
| Land coverage | 2.809% |
| Water coverage | 97.191% |
| Desired water coverage | 85–90% |
| Status | Above target water share |

The current atlas is substantially more ocean-heavy than the requested 85–90% water target. Do not blindly enlarge every island: several island footprints overlap, and some named seas have no island entries at all.

## Regional result

Each region is sampled within its own declared bounds using only islands assigned to that region. Region bounds overlap, so regional percentages are not additive and must not be averaged to obtain the global result.

| Region | Island entries | Land | Water |
|---|---:|---:|---:|
| East Blue | 7 | 2.268% | 97.732% |
| West Blue | 0 | 0.000% | 100.000% |
| North Blue | 0 | 0.000% | 100.000% |
| South Blue | 0 | 0.000% | 100.000% |
| Grand Line | 15 | 5.663% | 94.337% |
| New World | 6 | 3.105% | 96.895% |

## Design decisions required before radius tuning

1. Define canonical island placement for West Blue, North Blue, and South Blue. These currently contain no island entries.
2. Decide whether the global 85–90% water target applies to the entire bounding rectangle or to a defined playable-world boundary. The current rectangle includes large spaces between regions.
3. Review archipelago overlaps before changing radii.
4. Re-run at finer grid spacing (64 and 32 blocks) after the region plan is finalized.
5. Validate the result in a disposable fresh Minecraft world before considering the world map complete.

## Reproducibility

Run:

```bash
python3 tools/analyze_one_piece_atlas.py --step 128
python3 tools/analyze_one_piece_atlas.py --step 64
python3 tools/analyze_one_piece_atlas.py --step 32
```

The analysis is deterministic for a given catalog and grid spacing.
