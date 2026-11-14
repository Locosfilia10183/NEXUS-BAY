# NEXUS-BAY Forge Roadmap

## Phase 0 — Project separation
- [x] Create a dedicated Forge project directory and development branch.
- [x] Target the live server's Minecraft 1.20.1 / Forge generation.
- [ ] Build with JDK 17 and verify dependency resolution.

## Phase 1 — Combat foundation
- [ ] Register custom boss entity types and server-side boss bars.
- [ ] Implement boss attributes, target selection, attack states and phases.
- [ ] Add faction minions and island encounter configuration.
- [ ] Add server-authoritative combat abilities and cooldowns.

## Phase 2 — Progression
- [ ] Persistent player profile and mastery XP.
- [ ] Fruit / sword / weapon mastery trees.
- [ ] Skill unlock requirements and respec rules.
- [ ] Loot tables for unique weapons, fruit drops, reset items and keys.

## Phase 3 — Models and animation
- [ ] GeckoLib animated boss models and animation controllers.
- [ ] Unique idle, locomotion, attack, hit, transformation and death animations.
- [ ] Animated player ability actions and weapon-specific attack sequences.
- [ ] Client/server synchronization and multiplayer testing.

## Phase 4 — Islands and content
- [ ] Island definitions, spawn zones, structures and faction rosters.
- [ ] Boss arenas, progression gates, quests and secret chambers.
- [ ] Balance passes and performance testing with multiple players.

## Compatibility gate
The live server uses Mine Mine no Mi 0.11.5. Inspect its actual installed API and data behavior before implementing fruit ownership, fruit reset, or duplicate skill systems. Never replace the live world or overwrite its mods during development.
