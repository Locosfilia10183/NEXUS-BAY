# NEXUS-BAY Forge Roadmap

## Phase 0 — Project separation
- [x] Create a dedicated Forge project directory and development branch.
- [x] Target the live server's Minecraft 1.20.1 / Forge generation.
- [x] Build with JDK 17 and verify dependency resolution in GitHub Actions.

## Phase 1 — Combat foundation
- [x] Register custom boss entity types and server-side boss bars.
- [x] Implement boss attributes, target selection, attack states and phases.
- [ ] Add faction minions and island encounter configuration.
- [x] Add server-authoritative combat abilities and cooldowns.

## Phase 2 — Progression
- [ ] Persistent player profile and mastery XP.
- [ ] Fruit / sword / weapon mastery trees.
- [ ] Skill unlock requirements and respec rules.
- [ ] Loot tables for unique weapons, fruit drops, reset items and keys.

## Phase 3 — Models and animation
- [x] GeckoLib Kaido blockout model, texture atlas and renderer foundation.
- [x] Unique Kaido idle, locomotion, breath and slam animation clips.
- [x] Server attack triggers with tick-based impact timing.
- [x] Kaido kanabo melee swing animation trigger and weapon-bone motion.
- [ ] Hit, death, kanabo combo and transformation animations.
- [x] First-pass hybrid / dragon form geometry selection synchronized with phase transitions.
- [x] Hybrid / dragon transition clips and dragon idle / movement clips.
- [ ] Refine form geometry, texture UVs, dragon flight and in-game scale.
- [ ] Client/server multiplayer visual verification.

## Phase 4 — Islands and content
- [ ] Island definitions, spawn zones, structures and faction rosters.
- [ ] Boss arenas, progression gates, quests and secret chambers.
- [ ] Balance passes and performance testing with multiple players.

## Compatibility gate
The live server uses Mine Mine no Mi 0.11.5. Inspect its actual installed API and data behavior before implementing fruit ownership, fruit reset, or duplicate skill systems. Never replace the live world or overwrite its mods during development.
