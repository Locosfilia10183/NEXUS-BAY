# NEXUS-BAY Forge Mod

This directory is an **independent Forge mod project** for the live NEXUS-BAY server (Minecraft 1.20.1 / Forge). It does not replace or alter the existing Paper plugin project in the repository root.

## Target
- Minecraft Java Edition 1.20.1
- Minecraft Forge 47.2.0
- Java 17
- GeckoLib 4 for animated custom entities and items

## Planned systems
1. Custom boss entities, boss bars, phases, attributes and faction minions.
2. Fruit, sword and weapon mastery with persistent player progression.
3. Unlockable skills, cooldowns, server-authoritative combat and effects.
4. GeckoLib models and per-character / per-skill animations.
5. Island encounters, boss loot, rare fruit drops, reset items and keys.

## Important integration rule
Mine Mine no Mi is already installed on the target server. Before adding fruit mechanics, inspect its installed version and API. NEXUS-BAY should integrate with existing fruit ownership and powers where possible, not create conflicting duplicate fruit systems.

## Build
Use JDK 17 and Gradle:
```bash
cd forge-mod
gradle build
```
The output jar is written to `build/libs/`.

This is the initial project foundation. A successful GitHub commit does not mean the mod has already been built, installed, or tested on the live server.
