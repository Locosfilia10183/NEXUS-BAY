# One Piece atlas-backed terrain generator design

## Status

This document records the implementation path for the next world-generation milestone. It is a design/engineering note, not a claim that atlas islands already generate in Minecraft.

Current baseline:
- Forge 1.20.1 / Forge 47.2.0 / Java 17.
- The isolated `nexusbay:one_piece_ocean` preset currently uses Minecraft's built-in noise generator.
- `island_catalog.json` and `terrain_profiles.json` are planning metadata. The current noise router does not read either file.
- The existing live save and server are out of scope. Testing must use a newly created disposable world.

## Chosen direction: custom density function first

Prefer extending the built-in `minecraft:noise` generator with a custom density-function codec over replacing the entire `ChunkGenerator`.

Why:
- Minecraft's `NoiseBasedChunkGenerator` already implements chunk filling, base columns/heights, carvers, biome creation and surface building.
- A custom `ChunkGenerator` would have to implement or correctly delegate several abstract lifecycle methods, increasing compatibility risk.
- A custom density function can supply atlas-aware terrain density while retaining the vanilla noise-generation pipeline.

Forge 1.20.1 exposes the relevant `DensityFunction` interface and a `Registries.DENSITY_FUNCTION_TYPE` registry. The exact registration and codec wiring must be compiled against the pinned Forge 47.2.0 dependency before the preset is switched over.

## Density model

For each sampled world position:
1. Evaluate the nearest island candidate using its catalog center (X/Z), radius, and shape parameters.
2. Convert horizontal distance to a signed island influence. Positive values represent land mass; negative values represent ocean.
3. Apply the profile's height, coast width, and roughness parameters to shape the vertical density.
4. Combine island influence with a low-amplitude regional noise term so coastlines are not perfect circles.
5. Keep the sea level at Y=63. Ocean columns must remain water-filled; island interiors rise above sea level.
6. Return a stable density result for the same coordinates and world seed. Do not use mutable global random state.

The initial implementation should use a small candidate set (all 28 catalog entries is acceptable at this scale) and a bounded search strategy. Profile values must be validated for finite numbers and sensible ranges before being used.

## Data ownership

The island catalog remains the source of truth for names, regions, centers and radii. Terrain profiles remain the source of truth for shape and surface intent.

Do not silently invent missing West Blue, North Blue or South Blue islands. The validator should continue reporting those empty regions as planning warnings until the atlas is deliberately expanded.

The density-function JSON must receive a deterministic, generated representation of the catalog/profile data (or a single explicitly versioned atlas resource). Avoid hand-maintaining a second, divergent list of island coordinates in Java.

## Surface and biome stages

The first terrain milestone is land/ocean geometry only:
- ocean ratio target: 85–90% across a defined atlas sampling window;
- distinct island silhouettes and meaningful sea gaps;
- no accidental continuous continents between regions;
- stable sea floor and coast transitions.

A later milestone can add per-profile beaches, vegetation, cliffs and landmarks. Regional biome assignment and climate tuning follow after geometry is measurable. NPCs and quests remain blocked until the world map is complete.

## Required verification before enabling the preset

1. Compile the custom density function and codec against Forge 47.2.0 / official 1.20.1 mappings.
2. Add structural tests for codec fields, atlas/profile parity, finite values, and coordinate bounds.
3. Launch a dedicated Forge test instance and create a brand-new disposable world using the One Piece preset.
4. Verify the game decodes the preset without registry/codec errors.
5. Generate a fixed grid of chunks around each atlas region and sample surface heights.
6. Measure water coverage over a documented, repeatable sampling window; target 85–90%.
7. Inspect coastlines and inter-island distances in-game. Record seed, coordinates and screenshots/logs.
8. Only after successful tests, consider making the preset available to players. Never migrate or overwrite the live `world` save.

## API references checked

- Forge 1.20.1 `ChunkGenerator`: abstract generation lifecycle methods and `NoiseBasedChunkGenerator` subclass relationship.
- Forge 1.20.1 `NoiseBasedChunkGenerator`: existing noise implementation and generator settings holder.
- Forge 1.20.1 `DensityFunction`: `compute`, `fillArray`, `mapAll`, bounds and codec contract.
- Forge 1.20.1 registry documentation: registration lifecycle and dynamic datapack registry distinction.

This design is intentionally conservative: no custom generator is considered implemented until the codec compiles, the preset loads, and a fresh test world proves the terrain.
