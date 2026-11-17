# NEXUS-BAY — Master completion checklist
Last updated: 2026-11-17
Status source of truth for the private NEXUS-BAY One Piece RPG project.

## Project target and non-negotiable rules

- Minecraft 1.20.1, Forge, Java 17; development branch: `forge-development`.
- Private modpack/server for 1–3 players (owner + up to two friends), not a public network.
- Implement the systems as original NEXUS-BAY code and original assets. Do not copy/decompile/package True | Prime Piece code, models, textures, animations, or other protected files.
- The One Piece ocean/island map is considered complete by the project owner. Do not restart world generation; only fix verified defects found during final tests.
- Kaido is considered complete as a main boss/content milestone by the project owner. Keep him exclusive to Onigashima. Remaining model/combat polish is optional and should not block core completion.
- Bosses must never place, break, replace, or terraform blocks.
- The intended game loop is exploration, quests, combat, fruits, swords, eating/drinking, ships and boss progression. No normal mining, pickaxes, axes, or vanilla crafting loop.
- All important rewards, inventory changes, damage, cooldowns, progression, and purchases must be server-authoritative.
- Continue development directly on GitHub; the owner may be using a phone. Do not require manual local edits for normal progress.
- Build after coherent batches. The full in-game test is deliberately reserved for the end; do not interrupt development with repeated test requests.

## Status key
- [x] Existing foundation / milestone reported complete or present in repository.
- [~] Partially implemented; needs completion, integration, polish, or verification.
- [ ] Not yet implemented or not yet confirmed.
- [!] Important final-test / release gate.

## 1. Project foundation and world

- [x] Forge 1.20.1 project, Java 17, GitHub Actions build workflow.
- [x] One Piece ocean-and-island world and island catalog (28 destinations).
- [x] Island map screen and map keybind.
- [x] Island terrain profiles, ocean floor and island placement systems.
- [~] Final worldgen integrity pass: ensure chunk loading does not overwrite player builds or regenerate terrain unexpectedly.
- [ ] Final map accuracy pass: verify every island label, destination coordinate, icon, and teleport landing point.
- [ ] Safe teleport validation for every island and return/spawn route.
- [ ] Spawn/first-join flow and recovery route if a player falls into the ocean or gets stuck.
- [ ] Preserve player-built structures and never destructively regenerate an existing world.

## 2. Existing core systems to retain and finish

- [x] Beli currency ledger and commands.
- [x] Island factions, enemy tiers and elite/commander/calamity variants.
- [x] Island boss base, boss bars, phases, combat triggers and loot foundations.
- [x] Quest catalog, NPC questgivers, quest GUI/network and persistent progress foundation.
- [x] Island-specific merchants, offers, shop GUI/network foundation.
- [x] Starter kit and starter island systems.
- [~] Complete end-to-end integration of quest acceptance, progress, completion, reward, persistence and reconnect behavior.
- [~] Complete end-to-end integration of merchant stock, prices, Beli deduction, inventory capacity, feedback and persistence.
- [ ] Make all NPC interactions clear and reliable (quest, merchant, dialogue, boss/story NPC).
- [ ] Add party/co-op quest credit and reward rules for 1–3 players.
- [ ] Add clear player feedback for currency, rewards, level-ups, unlocks, quest objectives and boss phases.

## 3. Bosses and major characters

- [x] Kaido main boss milestone and Onigashima encounter (owner-confirmed complete).
- [x] Crocodile boss foundation and Alabasta spawn.
- [x] Enel boss foundation and Skypiea spawn.
- [x] Rob Lucci boss foundation and Enies Lobby spawn.
- [~] Kizaru unique model, texture, light sword, animation set and three phase attacks in active development.
- [ ] Finish Kizaru implementation and confirm model/texture/weapon/animation assets are all correctly wired.
- [ ] Add remaining major One Piece villains/bosses selected for the final roster, each with a unique 3D model, proportions, textures, weapon/fruit, moveset, animations, arena, rewards and spawn rules.
- [ ] Add unique boss intro/arrival, combat start, phase transition, hit reaction, defeat/death and despawn behavior.
- [ ] Add attack telegraphs and fair warning windows for every boss ability.
- [ ] Add boss-specific music/sound cues and readable boss UI.
- [ ] Add contribution-based rewards so every active participant gets fair credit.
- [ ] Scale boss health/damage/rewards for solo, duo and trio without trivializing encounters.
- [ ] Enforce boss block protection universally: no block placement, breaking, replacement, explosions or terrain edits by boss AI/attacks.
- [ ] Ensure Kaido can only spawn/fight on Onigashima and cannot be summoned by normal world progression elsewhere.
- [~] Final boss model UV, animation timing, hitbox, scale and multiplayer visual review (final-test phase).

## 4. Devil fruits

- [x] Fruit item registry, fruit case and weighted case table.
- [x] Persistent fruit ownership/mastery profile.
- [x] Fruit skill catalog, unlock levels, cooldowns and server-side combat foundation.
- [x] Fruit HUD/keybind foundation; fruit bound to hotbar slot 1.
- [~] Expand the roster to the full planned One Piece fruit list, not just the current 19 registered fruits.
- [ ] Create a complete fruit master list with canon name, category, rarity, abilities, awakening/form and island/boss source.
- [ ] Implement every planned fruit as an original NEXUS-BAY fruit with a unique moveset and effects; no generic shared attack presented as a unique power.
- [ ] Add fruit-specific 3D/visual effects, sounds, icons, skill animations and transformation visuals.
- [ ] Implement missing fruit transformations and awakenings, including proper model/visual state where applicable.
- [ ] Complete mastery progression, XP curve, skill unlocks, cooldown UI and respec/fruit reset rules.
- [ ] Verify fruit case odds and make all rarity weights sum exactly to 100%.
- [ ] Verify fruit cannot be duplicated, lost on death/relog, moved out of reserved slot incorrectly, or overwritten by another fruit.
- [ ] Add fruit storage/collection and safe fruit replacement rules, including what happens to the old fruit.
- [ ] Add sea weakness/swimming consequences if desired by the design, without soft-locking players.
- [ ] Balance fruit damage and cooldowns against swords, Haki, fighting styles and bosses.

## 5. Character progression and stats

- [x] Persistent character progression data class created.
- [x] Character info and stat allocation commands created.
- [x] Character XP award hook added for enemy defeats.
- [ ] Verify current progression implementation builds and persists correctly.
- [ ] Add character level display and a proper in-game character/stat screen (not command-only).
- [ ] Add keybind and client/server sync for the character screen.
- [ ] Make Strength affect melee damage in a controlled, capped formula.
- [ ] Make Defense affect incoming damage in a controlled, capped formula.
- [ ] Make Stamina affect energy/stamina capacity and recovery.
- [ ] Make Sword affect sword damage/weapon techniques.
- [ ] Make Fruit affect fruit damage/fruit techniques, without double-counting fruit mastery.
- [ ] Add stat point reset/respec with a clear cost/item/rule.
- [ ] Add death, relog, dimension change and multiplayer persistence checks.
- [ ] Add level cap, XP curve and anti-exploit limits for repeated low-level mob farming.

## 6. Haki

- [ ] Observation Haki: activation, mastery, cooldown/energy, threat awareness and dodge/telegraph interaction.
- [ ] Armament Haki: activation, coating visual, weapon/hand damage bonus and defense.
- [ ] Conqueror's Haki: area pressure/stun against weaker enemies, boss resistance and visual aura.
- [ ] Haki progression, unlock requirements, training quests and mastery UI.
- [ ] Distinct animations, particles, sounds and status indicators for each Haki type.
- [ ] Balance Haki with character stats, fruit powers, sword styles and boss phases.
- [ ] Multiplayer synchronization and server-authoritative checks.

## 7. Fighting styles and sword combat

- [ ] Build fighting-style framework with unlock, mastery, moves, cooldowns and UI.
- [ ] Implement Black Leg with unique kicks, combos and animations.
- [ ] Implement Fish-Man Karate with water/impact techniques and unique animations.
- [ ] Implement Rokushiki (Soru, Geppo, Tekkai, Rankyaku, Shigan, Rokuogan) as a complete style.
- [ ] Implement sword-style progression and named techniques.
- [ ] Add dual-wield/three-sword style support where intended, with correct weapon positioning.
- [ ] Give each named sword its own model, texture, stats, effects and special trait.
- [ ] Finish signature weapons and drops for the boss roster.
- [ ] Add weapon durability/repair or an intentionally simplified private-server rule.
- [ ] Balance style damage against fruits and Haki; prevent stacking exploits.

## 8. Bounty, factions and reputation

- [ ] Persistent player bounty value and wanted display/poster.
- [ ] Bounty rewards for pirate/hostile encounters and boss achievements.
- [ ] Marine/Pirate faction selection or reputation progression.
- [ ] Reputation consequences: hostile patrols, friendly NPC access, discounts, quests and faction rewards.
- [ ] Marine patrols, pirate crews, captains and elite encounters around appropriate islands.
- [ ] Wanted rank thresholds and rewards tuned for a 1–3 player world.
- [ ] Co-op bounty credit and anti-farm rules.

## 9. Ships and sea gameplay

- [ ] Custom ship entities with reliable movement and collision.
- [ ] Helm interaction and passenger/crew seats for up to three players.
- [ ] Ship ownership, storage, health/durability and respawn/recovery rules.
- [ ] Sail speed, upgrades and ship classes.
- [ ] Cannons and ship-to-ship combat with original models, projectiles, effects and sounds.
- [ ] Sea encounters: patrol ships, pirate crews, marine ships, captains and rare events.
- [ ] Island discovery, navigation markers and route guidance.
- [ ] Sea danger and rewards balanced for solo/duo/trio.
- [ ] Bosses and ship attacks must not damage or modify terrain blocks.

## 10. Chests, loot, economy and items

- [x] Beli balance/payment foundation and mob reward foundation.
- [x] Boss loot foundations and island-specific merchant offer foundation.
- [ ] Add wood/silver/gold/rare treasure chest tiers with original models/textures.
- [ ] Add server-authoritative weighted loot tables and anti-duplicate/opening checks.
- [ ] Add island-specific materials, crafting-free upgrade tokens and collectibles.
- [ ] Add boss keys, rare weapon drops, fruit cases and cosmetics to suitable loot tables.
- [ ] Add shop purchase validation, inventory-full handling and clear transaction messages.
- [ ] Add a complete item registry audit: every item has a name, icon/model, texture, description and intended use.
- [ ] Remove unused placeholder items or finish their function.
- [ ] Because the game avoids vanilla crafting/mining, provide NPC, quest, boss and chest paths for all progression materials.

## 11. NPCs, quests and story progression

- [x] Questgiver and merchant NPC foundations.
- [x] Island quest catalog and dialogue catalog foundations.
- [ ] Finish quest chains per island with prerequisites, multiple stages and clear rewards.
- [ ] Add story progression from East Blue through later seas/islands.
- [ ] Add boss unlock requirements and story triggers.
- [ ] Add NPC dialogue options, accept/decline/turn-in and quest journal.
- [ ] Add repeatable/daily-style quests only where useful; avoid grind for a private small group.
- [ ] Add party sharing and reconnect persistence.
- [ ] Add secret quests, hidden areas and collectible lore rewards.
- [ ] Make NPC spawning deterministic and prevent duplicate NPC piles.

## 12. Inventory, HUD and menus

- [ ] Create custom One Piece RPG inventory layout.
- [ ] Add character stats, fruit skills, Haki, fighting style, bounty, Beli and quest panels.
- [ ] Improve fruit HUD with mastery, selected skill, cooldown and transformation state.
- [ ] Add sword/weapon skill HUD and cooldown indicators.
- [ ] Add ship status/navigation HUD.
- [ ] Add boss objective/phase UI and clear attack warning indicators.
- [ ] Add consistent NEXUS-BAY visual language, icons, fonts, tooltips and localization.
- [ ] Ensure menus work at common resolutions and with GUI scale changes.
- [ ] Keep essential interactions usable with keyboard and controller/mobile-compatible layouts where feasible.

## 13. Audio, visual quality and accessibility

- [ ] Add original sound design for fruits, Haki, swords, ships, NPCs, bosses and UI.
- [ ] Finish unique textures and UVs for every character, boss, weapon, fruit and ship.
- [ ] Ensure every major character has unique proportions/model, not recolored vanilla entities.
- [ ] Finish unique idle, walk, attack, special, hit, transformation and death animations for each major character.
- [ ] Add readable telegraphs and effect intensity settings.
- [ ] Add options to reduce particles/shake/flash for performance and comfort.
- [~] German and English names added for all currently registered items and all 19 registered fruits; remaining NPC/UI/quest text and missing-key audit still open.

## 14. Private server and multiplayer reliability

- [ ] Set private-server defaults for 1–3 players.
- [ ] Add party/coop grouping, shared boss eligibility and reward split.
- [ ] Test disconnect/reconnect during quests, boss fights, ships and transformations.
- [ ] Ensure persistent data is per-player and does not leak between players.
- [ ] Verify server-authoritative inventory, combat, cooldown, XP, Beli and shop operations.
- [ ] Check chunk loading, entity despawn, boss respawn and NPC duplication.
- [ ] Add config for difficulty, rewards, boss respawn and solo/duo/trio scaling.
- [ ] Add backup/restore instructions for the private world before release.

## 15. Performance, compatibility and release

- [ ] Review entity/particle counts and avoid per-tick expensive scans.
- [ ] Review network packet size and update frequency.
- [ ] Verify all required GeckoLib/other dependencies are declared and versions are pinned.
- [ ] Verify dedicated server startup and client startup.
- [ ] Verify clean install, update, existing-world upgrade and config defaults.
- [ ] Verify no accidental dependency on True | Prime Piece or its assets.
- [ ] Add version number, changelog, installation guide and private modpack manifest.
- [ ] Produce final Forge JAR and private modpack export.
- [ ] Confirm server and clients use matching mod versions.
- [ ] Create final backup before updating the private world.

## 16. Final test plan — deliberately at the end

- [!] Fresh single-player world: first join, starter kit, spawn, map and all island teleports.
- [!] Visit all 28 islands; check terrain, NPCs, quests, merchants, factions and safe landing.
- [!] Verify no normal mining/pickaxe/axe/crafting loop is required for progression.
- [!] Verify eating/drinking, fruit case, fruit ownership, fruit slot 1, fruit skills, transformations and mastery.
- [!] Verify character XP, stat points, stat effects, Haki and fighting styles.
- [!] Verify swords, weapons, durability and signature abilities.
- [!] Verify Beli, shops, chests, quest rewards and loot without duplication.
- [!] Verify ships, sailing, passengers, cannons and sea encounters.
- [!] Fight every boss, including Kaido only on Onigashima; test all phases, attacks, telegraphs, death, drops and respawn.
- [!] Confirm bosses never place, break, replace or modify blocks.
- [!] Run a three-player session: shared quests, boss credit, rewards, disconnect/reconnect and persistence.
- [!] Run a long session and restart: save integrity, memory/particle performance, entity counts and no corrupted world.
- [!] Only after all checks pass: package release, back up world and deploy to private server.

## Recommended build order — easy first, difficult last

### Batch 1 — Quick completion and cleanup
1. [~] Add localization for registered items/fruits (done); finish descriptions, NPC/UI/quest strings and missing icons/models.
2. Audit item/fruit registry and remove or label placeholders.
3. Finish quest/merchant feedback and inventory-full handling.
4. Add basic character profile display and sync.
5. Add basic quest journal and HUD indicators.
6. Update docs and run CI after each coherent batch.

### Batch 2 — Existing systems completion
7. Finish character progression: stat effects, respec, level curve and UI.
8. Finish fruit catalog, unlocks, storage/replacement and cooldown UI.
9. Finish quest chains, boss unlock gates and co-op rewards.
10. Finish Beli shops, treasure chests and island loot.
11. Finish boss roster integration and Kizaru.

### Batch 3 — Medium gameplay systems
12. Implement Haki types and mastery.
13. Implement Rokushiki and other fighting styles.
14. Implement bounty and faction reputation.
15. Implement sword mastery and named weapon techniques.
16. Add sea encounters, patrols and captain events.

### Batch 4 — Large systems
17. Build ship entities, sailing, crew seats, cannons and ship battles.
18. Build full fruit transformations/awakenings and unique ability presentation.
19. Finish custom inventory, complete HUD and integrated RPG menus.
20. Add remaining boss characters, story arcs and advanced NPC sequences.

### Batch 5 — Polish and final testing
21. Complete all character/weapon/ship art, UVs, animations and audio.
22. Optimize performance and multiplayer synchronization.
23. Run the complete final test plan above.
24. Fix all release blockers, produce final JAR/modpack and back up the world.

## Immediate next task

Continue with the easiest concrete work first: inspect and finish the existing character progression implementation (build correctness, stat effects, XP/level rules and UI foundation), then do the small localization/item/quest/merchant cleanup batch. Do not start a large ship or awakening system before the easy completion batch is done.
