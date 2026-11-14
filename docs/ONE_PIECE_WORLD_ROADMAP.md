# NEXUS-BAY One Piece World Roadmap

Target runtime: Minecraft 1.20.1 Forge 47.x, Java 17, GeckoLib 4.4.9.

## Non-negotiable safety
- Never overwrite, rename, or delete the existing server world.
- The One Piece world must be a separate world/save, created only after a tested generator build.
- Keep all work on feature/one-piece-ocean-world-ships-kaido until CI and in-game tests pass.
- Do not deploy experimental world-generation code to the live server.

## World-generation goals
- Ocean-dominant overworld target: approximately 85-90% ocean at the large-map scale.
- Large island groups separated by meaningful ocean distances; small islets and reefs between major islands.
- Distinct regions inspired by the Four Blues, Grand Line, and New World, with original names/lore where needed.
- Island biome palette: tropical beaches/jungle, temperate forest, volcanic, desert, snowy, swamp, and rocky highlands.
- Generated landmarks: ports, villages, Marine bases, pirate hideouts, ruins, and boss arenas.
- Ocean travel must be meaningful; avoid placing every island within short swimming distance.

## Generator implementation sequence
1. [x] Add an isolated `nexusbay:one_piece_ocean` world-preset scaffold at `data/nexusbay/worldgen/world_preset/one_piece_ocean.json`; it does not alter the existing save.
2. [x] Add a first experimental custom noise settings file and low-frequency island-mass noise; connect the One Piece preset to it.
3. [x] Add a CI structural validator for the One Piece preset, required noise-router fields, atlas coordinates, and all 28 catalog/profile ID pairs (`tools/validate_one_piece_worldgen.py`). This catches malformed JSON and inconsistent design data, but does not replace Minecraft runtime validation.
4. [ ] Validate the noise-settings registry and density functions in an actual Forge 1.20.1 client/server.
5. [ ] Tune island frequency and land ratio against multiple seeds until the large-map target is approximately 85-90% ocean. Current land ratio is NOT measured or guaranteed.
6. [x] Switch from a fixed Deep Ocean biome to the vanilla multi-noise climate biome source and restore temperature, vegetation, continentalness, erosion, depth, and ridge climate signals in the custom router. This is a first biome variety pass, not yet a custom One Piece region climate map.
7. [x] Define a canonical island atlas with planned coordinates, region, level gate, terrain theme, and island radius in `data/nexusbay/islands/island_catalog.json`; sync the client map markers to the atlas.
8. [ ] Make the terrain generator produce landmasses at the atlas anchors and preserve large ocean gaps. The atlas currently describes planned locations; it does not itself create terrain.
9. [x] Define deterministic terrain design profiles for all 28 atlas islands in `data/nexusbay/islands/terrain_profiles.json` (shape, maximum height, surface palette, vegetation, shoreline material, and landmark anchors). These are design data only; no terrain is generated from them yet.
10. [ ] Implement a tested generator that consumes the atlas anchors and these profiles to create real island landmasses.
10. [ ] Test the map in a fresh dedicated save.
12. [x] Align the Kaido home anchor with the planned Onigashima location in the New World atlas (X=36500, Z=14000); Y=90 remains provisional until the island surface exists.
13. Add structure placement pools for ports, villages, Marine bases, pirate outposts, and ruins.
14. Add region progression and discovery/map metadata.
15. Test new-world creation, spawn safety, chunk borders, structure spacing, server restart, and compatibility with Mine Mine no Mi.

## Gameplay sequence after the world map
- Finish and validate the world map, ocean, island landmasses, and island-specific terrain before adding gameplay loops.
- Then add NPC placement and island-specific quest boards/dialogue, with quest chains, objectives, rewards, and level recommendations.
- Quests must reference stable island/NPC IDs from the island catalog so later map and NPC changes do not break quest progress.
- Boss encounters, faction reputation, shops, and island unlocks build on that foundation; do not start quest implementation before the islands exist.

## Ships gameplay goals
- Large custom ship entity with visible hull, deck, mast, sails, wheel, and collision bounds.
- Board/leave interaction; one player pilots at the wheel.
- Server-authoritative steering and throttle; client sends bounded input packets.
- Sail state controls speed; acceleration, turning, drag, and reverse/stop behavior.
- Ship movement must work on water and avoid uncontrolled land movement.
- Storage and crew/cannon systems are later milestones, not prerequisites for the first sailing prototype.
- Initial ship prototype should be a single stable vessel with a clear spawn/craft path, then add ship classes and upgrades.

## Kaido visual milestone
- Keep model geometry, renderer bounds/shadow, and collision dimensions in agreement for human, hybrid, and dragon forms.
- Validate all three phases in a real client; a green compile is not visual validation.
- Keep transformations within the same boss entity and synchronize phase changes to clients.

## Acceptance checklist
- [ ] Forge build succeeds on CI.
- [ ] Kaido human/hybrid/dragon visual scale and collision are tested in-game.
- [ ] New One Piece world can be created without touching the existing world folder.
- [ ] Ocean ratio and island spacing are checked across multiple seeds.
- [ ] Ship can be boarded, piloted, turned, stopped, and dismounted in multiplayer.
- [ ] Server restart preserves ship/world state without corruption.

## Boss home-island rules
- Bosses are world inhabitants, not admin-summoned encounters: no player-facing spawn command.
- Each implemented boss has one canonical home island and home region.
- Spawn a boss automatically when its home chunk is naturally loaded and no living instance is present.
- Mark boss entities persistent so chunk unloads and server restarts do not despawn them.
- Never force-load distant island chunks only to keep a boss active.
- Keep boss respawn policy separate from initial spawning so defeat rewards cannot be farmed through instant respawns.
- A boss is added to the active home-spawn roster only after its own entity, model, renderer, AI, and island anchor exist.
- Initial active implementation: Kaido at the planned Onigashima atlas anchor (X=36500, Y=90, Z=14000); Y is provisional until terrain generation is validated. Other catalog entries remain planned until their individual boss implementations are ready.
