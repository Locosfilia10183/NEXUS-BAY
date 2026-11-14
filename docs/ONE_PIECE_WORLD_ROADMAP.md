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
1. Implement a dedicated world preset/dimension configuration for a new save, not a mutation of existing world settings.
2. Establish ocean-heavy biome/terrain distribution and verify island spacing with seed-based tests.
3. Add structure placement pools for ports, Marine bases, pirate outposts, and ruins.
4. Add region progression and discovery/map metadata.
5. Test new-world creation, spawn safety, chunk borders, structure spacing, server restart, and compatibility with Mine Mine no Mi.

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
- Initial active implementation: Kaido at Onigashima (anchor X=2752, Y=101, Z=1472). Other catalog entries remain planned until their individual boss implementations are ready.
