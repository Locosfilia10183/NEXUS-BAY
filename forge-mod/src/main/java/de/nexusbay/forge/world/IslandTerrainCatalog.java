package de.nexusbay.forge.world;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Terrain-generation design profiles for the canonical NEXUS-BAY island atlas.
 *
 * This is generation input data, not a chunk generator by itself. The actual
 * Forge world-generation pipeline must consume these profiles before islands
 * are considered generated in-game.
 */
public final class IslandTerrainCatalog {
    public enum Climate {
        TEMPERATE, TROPICAL, DESERT, POLAR, VOLCANIC, SKY, SWAMP, UNDERWATER, TOXIC, FAIRYTALE
    }

    public enum Terrain {
        COASTAL_VILLAGE, FORTRESS, PORT_TOWN, FOREST_HILLS, OCEAN_RESTAURANT,
        TROPICAL_JUNGLE, GRAND_CITY, MOUNTAIN_PASS, CACTUS_DESERT, GIANT_JUNGLE,
        SNOW_MOUNTAINS, SAND_KINGDOM, JUNGLE_RUINS, SKY_ISLAND, CANAL_CITY,
        JUDICIAL_FORTRESS, HAUNTED_FOREST, ARCHIPELAGO, AMAZON_JUNGLE,
        DEEP_SEA_PRISON, MARINE_FORTRESS, UNDERWATER_DOME, FROZEN_VOLCANO,
        TOY_KINGDOM, MOVING_FOREST, CANDY_KINGDOM, SAMURAI_MOUNTAINS, DEMON_FORTRESS
    }

    public record Profile(
            String islandId,
            Climate climate,
            Terrain terrain,
            int radius,
            int baseHeight,
            int heightVariation,
            String surfaceBlock,
            String subsurfaceBlock,
            String vegetation
    ) {}

    private static final Map<String, Profile> PROFILES = createProfiles();

    private static Map<String, Profile> createProfiles() {
        Map<String, Profile> profiles = new LinkedHashMap<>();
        add(profiles, "FooshaVillage", Climate.TEMPERATE, Terrain.COASTAL_VILLAGE, 72, 80, 9, "minecraft:grass_block", "minecraft:dirt", "oak_village");
        add(profiles, "ShellsTown", Climate.TEMPERATE, Terrain.FORTRESS, 68, 82, 12, "minecraft:grass_block", "minecraft:stone", "sparse_oak");
        add(profiles, "OrangeTown", Climate.TEMPERATE, Terrain.PORT_TOWN, 66, 78, 7, "minecraft:grass_block", "minecraft:dirt", "small_orchard");
        add(profiles, "SyrupVillage", Climate.TEMPERATE, Terrain.FOREST_HILLS, 74, 88, 24, "minecraft:grass_block", "minecraft:dirt", "dense_oak");
        add(profiles, "Baratie", Climate.TEMPERATE, Terrain.OCEAN_RESTAURANT, 48, 76, 4, "minecraft:stone_bricks", "minecraft:stone", "none");
        add(profiles, "ArlongPark", Climate.TROPICAL, Terrain.TROPICAL_JUNGLE, 76, 82, 13, "minecraft:grass_block", "minecraft:dirt", "palm_jungle");
        add(profiles, "Loguetown", Climate.TEMPERATE, Terrain.GRAND_CITY, 78, 84, 8, "minecraft:stone_bricks", "minecraft:stone", "city_greenery");
        add(profiles, "ReverseMountain", Climate.TEMPERATE, Terrain.MOUNTAIN_PASS, 84, 112, 48, "minecraft:stone", "minecraft:stone", "mountain_sparse");
        add(profiles, "WhiskeyPeak", Climate.DESERT, Terrain.CACTUS_DESERT, 72, 82, 10, "minecraft:sand", "minecraft:sandstone", "cactus");
        add(profiles, "LittleGarden", Climate.TROPICAL, Terrain.GIANT_JUNGLE, 88, 94, 32, "minecraft:grass_block", "minecraft:dirt", "giant_ferns");
        add(profiles, "DrumIsland", Climate.POLAR, Terrain.SNOW_MOUNTAINS, 82, 112, 52, "minecraft:snow_block", "minecraft:stone", "spruce_snow");
        add(profiles, "Alabasta", Climate.DESERT, Terrain.SAND_KINGDOM, 92, 84, 14, "minecraft:sand", "minecraft:sandstone", "desert_oasis");
        add(profiles, "Jaya", Climate.TROPICAL, Terrain.JUNGLE_RUINS, 78, 92, 28, "minecraft:grass_block", "minecraft:coarse_dirt", "jungle_ruins");
        add(profiles, "Skypiea", Climate.SKY, Terrain.SKY_ISLAND, 76, 180, 34, "minecraft:grass_block", "minecraft:dirt", "sky_garden");
        add(profiles, "Water7", Climate.TEMPERATE, Terrain.CANAL_CITY, 82, 78, 8, "minecraft:stone_bricks", "minecraft:stone", "canal_city");
        add(profiles, "EniesLobby", Climate.TEMPERATE, Terrain.JUDICIAL_FORTRESS, 74, 86, 10, "minecraft:quartz_block", "minecraft:stone", "fortress_gardens");
        add(profiles, "ThrillerBark", Climate.SWAMP, Terrain.HAUNTED_FOREST, 86, 82, 16, "minecraft:podzol", "minecraft:coarse_dirt", "dark_forest");
        add(profiles, "Sabaody", Climate.TROPICAL, Terrain.ARCHIPELAGO, 90, 82, 12, "minecraft:grass_block", "minecraft:dirt", "mangrove_grove");
        add(profiles, "AmazonLily", Climate.TROPICAL, Terrain.AMAZON_JUNGLE, 78, 94, 26, "minecraft:grass_block", "minecraft:dirt", "dense_jungle");
        add(profiles, "ImpelDown", Climate.TEMPERATE, Terrain.DEEP_SEA_PRISON, 66, 44, 8, "minecraft:deepslate_bricks", "minecraft:deepslate", "none");
        add(profiles, "Marineford", Climate.TEMPERATE, Terrain.MARINE_FORTRESS, 82, 86, 12, "minecraft:stone_bricks", "minecraft:stone", "fortress_gardens");
        add(profiles, "FishManIsland", Climate.UNDERWATER, Terrain.UNDERWATER_DOME, 72, 30, 8, "minecraft:prismarine", "minecraft:dark_prismarine", "coral");
        add(profiles, "PunkHazard", Climate.TOXIC, Terrain.FROZEN_VOLCANO, 86, 96, 42, "minecraft:basalt", "minecraft:blackstone", "ash_and_ice");
        add(profiles, "Dressrosa", Climate.TEMPERATE, Terrain.TOY_KINGDOM, 82, 84, 16, "minecraft:grass_block", "minecraft:dirt", "flower_gardens");
        add(profiles, "Zou", Climate.TROPICAL, Terrain.MOVING_FOREST, 78, 120, 30, "minecraft:grass_block", "minecraft:dirt", "giant_forest");
        add(profiles, "WholeCakeIsland", Climate.FAIRYTALE, Terrain.CANDY_KINGDOM, 84, 86, 18, "minecraft:pink_concrete", "minecraft:terracotta", "candy_groves");
        add(profiles, "WanoKuni", Climate.TEMPERATE, Terrain.SAMURAI_MOUNTAINS, 90, 104, 36, "minecraft:grass_block", "minecraft:stone", "cherry_and_bamboo");
        add(profiles, "Onigashima", Climate.VOLCANIC, Terrain.DEMON_FORTRESS, 72, 100, 28, "minecraft:deepslate", "minecraft:blackstone", "dead_trees");
        return Map.copyOf(profiles);
    }

    private static void add(Map<String, Profile> target, String id, Climate climate, Terrain terrain,
                            int radius, int baseHeight, int heightVariation,
                            String surface, String subsurface, String vegetation) {
        target.put(id, new Profile(id, climate, terrain, radius, baseHeight,
                heightVariation, surface, subsurface, vegetation));
    }

    public static Optional<Profile> find(String islandId) {
        return Optional.ofNullable(PROFILES.get(islandId));
    }

    public static Map<String, Profile> all() {
        return PROFILES;
    }

    private IslandTerrainCatalog() {}
}
