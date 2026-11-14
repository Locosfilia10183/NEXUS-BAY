# NEXUS-BAY – Gameplay roadmap

## Included in the first core build
- Paper Java server foundation and Geyser/Floodgate cross-play
- Admin permission and 54-slot administration GUI
- Procedural starter island generator with dock, lighthouse and outpost
- Persistent player profile: level, XP and Bay-Coins
- Player-to-player coin transfers
- Systemd service and backup helper

## World generation specification\n- See [Worldgen catalog](WORLDGEN.md) for the canonical island profiles, generation rules and staged implementation plan. The catalog is a design specification; runtime generation is still generic.\n\n## Next gameplay systems
1. Island archipelago generator: multiple biome themes, ports, forts, shipwrecks, caves and hidden treasure rooms.
2. Pirate progression: weapon mastery, Haki branches, crew reputation and prestige.
3. Quest framework: NPC quest givers, daily/weekly contracts, island discovery and boss objectives.
4. Economy: player market, auction house, shops, taxes and anti-dupe transaction ledger.
5. Crews and territory: crew creation, ranks, flags, claims, alliances and wars.
6. Bosses and dungeons: encounter phases, loot tables, keys and repeatable instances.
7. Admin suite: player inspection, moderation, inventory inspection, vanish, teleport, item editor, economy controls and audit log.

## Important technical boundary
The first core plugin creates vanilla-block structures and basic RPG/admin systems. It does not yet contain custom 3D models, a full NPC quest database, devil-fruit abilities, Haki combat, or a complete marketplace. Those are separate systems to implement and test in later releases. Paper plugins are used for Java/Bedrock compatibility; Forge/Fabric-only mods cannot be installed on this Paper server.