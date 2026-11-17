# NEXUS-BAY — Prime Piece feature parity plan

This document tracks a clean-room implementation plan. NEXUS-BAY will not bundle, decompile, copy, or redistribute True | Prime Piece code, assets, models, textures, animations, or other protected files. The reference mod is used only to identify gameplay categories; all implementation and assets here are original NEXUS-BAY work.

## Reference feature categories

The True | Prime Piece CurseForge description documents:
- Devil Fruit powers and progression
- Player XP and leveling
- Allocatable player stat points (opened with P in the reference mod)
- Bounty progression
- Ship encounters, captains, chests, and loot
- Haki
- Fighting styles
- Pirate/Marine combat encounters and bosses

The latest 1.20.1 file is Forge. Its page lists GeckoLib, Player Animator, Phekui, Kleiders Custom Renderer, and Photon as dependencies for that mod. These are not automatically dependencies for NEXUS-BAY; add libraries only when our own code needs them.

## Existing NEXUS-BAY foundations

- Devil fruit item registry and weighted fruit-case table
- Persistent fruit ownership/mastery and unlock catalog
- Server-authoritative fruit skill combat and client keybind/HUD
- Beli ledger and island-specific economy
- Island factions, enemy progression, bosses, quests, merchants, and world map
- GeckoLib boss models and animation resources

## Implementation phases

### A. Character progression
- Persistent character profile separate from fruit mastery
- XP, level, stat points, and respec
- Strength, Defense, Stamina, Sword, and Fruit mastery attributes
- Server-side validation and multiplayer synchronization
- A compact character/stat screen and keybind

### B. Haki
- Observation, Armament, and Conqueror's Haki
- Haki mastery, stamina/energy drain, cooldowns, toggles, and visual tells
- Armament damage/defense and weapon coating
- Observation dodge/awareness and enemy telegraphs
- Conqueror's pressure against weaker enemies, with boss resistance

### C. Fighting styles
- Original NEXUS-BAY implementations of Black Leg, Fish-Man Karate, Rokushiki, sword styles, and other One Piece-inspired styles
- Separate mastery tracks, move sets, animations, cooldowns, and unlock quests

### D. Bounty and factions
- Player bounty, rank thresholds, wanted status, and reward scaling
- Marine/Pirate faction reputation and consequences
- Captain encounters, patrols, elite crews, and boss contracts

### E. Ships and sea exploration
- Own ship entities and helm/crew controls
- Sailing, durability, upgrades, cannons, and ship combat
- Sea encounters and island discovery rewards

### F. Chests and loot
- Wood, silver, and gold chest tiers
- Server-authoritative weighted rewards, including Beli, gear, materials, and fruit cases
- Captain drops and island-specific loot tables

## Design constraints
- Minecraft Forge 1.20.1 and Java 17 compatibility.
- No copied Prime Piece implementation or assets.
- All combat, inventory rewards, progression, and cooldown checks are server-authoritative.
- Designed for a private 1–3 player world: co-op rewards, boss scaling, and configurable difficulty.
- Keep the existing NEXUS-BAY island/quest/boss systems integrated rather than duplicating them.
- Build after each coherent feature batch; full in-game testing remains for the agreed final test phase.

## Current next implementation
Start with Phase A: a persistent character progression profile and server-side stat allocation, then expose it through a dedicated NEXUS-BAY character screen.
