package de.nexusbay.forge.world;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Canonical world atlas for NEXUS-BAY Forge 1.20.1.
 *
 * These are planned generation anchors, not proof that terrain already exists.
 * World generation, spawn placement, map markers and teleport destinations must
 * all consume this one catalogue to prevent coordinate drift.
 */
public final class IslandCatalog {
    public static final int SEA_LEVEL = 63;
    public static final int OCEAN_FLOOR = -32;
    public static final int ISLAND_SPACING = 320;
    public static final int ISLAND_RADIUS = 72;

    public record Destination(
            String id,
            String displayName,
            int x,
            int z,
            int safeY,
            int requiredLevel,
            boolean starter
    ) {
        public String normalizedId() {
            return id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        }
    }

    private static final List<Destination> ISLANDS = List.of(
            new Destination("FooshaVillage", "Foosha Village", 512, 512, 80, 1, true),
            new Destination("ShellsTown", "Shells Town", 832, 512, 80, 5, false),
            new Destination("OrangeTown", "Orange Town", 1152, 512, 80, 8, false),
            new Destination("SyrupVillage", "Syrup Village", 1472, 512, 80, 12, false),
            new Destination("Baratie", "Baratie", 1792, 512, 80, 16, false),
            new Destination("ArlongPark", "Arlong Park", 2112, 512, 80, 20, false),
            new Destination("Loguetown", "Loguetown", 2432, 512, 80, 25, false),
            new Destination("ReverseMountain", "Reverse Mountain", 2752, 512, 80, 30, false),
            new Destination("WhiskeyPeak", "Whiskey Peak", 512, 832, 80, 34, false),
            new Destination("LittleGarden", "Little Garden", 832, 832, 80, 38, false),
            new Destination("DrumIsland", "Drum Island", 1152, 832, 100, 42, false),
            new Destination("Alabasta", "Alabasta", 1472, 832, 80, 48, false),
            new Destination("Jaya", "Jaya", 1792, 832, 80, 55, false),
            new Destination("Skypiea", "Skypiea", 2112, 832, 180, 62, false),
            new Destination("Water7", "Water 7", 2432, 832, 80, 68, false),
            new Destination("EniesLobby", "Enies Lobby", 2752, 832, 80, 72, false),
            new Destination("ThrillerBark", "Thriller Bark", 512, 1152, 80, 78, false),
            new Destination("Sabaody", "Sabaody Archipelago", 832, 1152, 80, 82, false),
            new Destination("AmazonLily", "Amazon Lily", 1152, 1152, 80, 85, false),
            new Destination("ImpelDown", "Impel Down", 1472, 1152, 45, 88, false),
            new Destination("Marineford", "Marineford", 1792, 1152, 80, 92, false),
            new Destination("FishManIsland", "Fish-Man Island", 2112, 1152, 30, 95, false),
            new Destination("PunkHazard", "Punk Hazard", 2432, 1152, 80, 98, false),
            new Destination("Dressrosa", "Dressrosa", 2752, 1152, 80, 100, false),
            new Destination("Zou", "Zou", 512, 1472, 120, 105, false),
            new Destination("WholeCakeIsland", "Whole Cake Island", 832, 1472, 80, 110, false),
            new Destination("WanoKuni", "Wano Kuni", 1152, 1472, 100, 120, false),
            new Destination("Onigashima", "Onigashima", 1472, 1472, 100, 125, false)
    );

    public static List<Destination> all() {
        return ISLANDS;
    }

    public static Optional<Destination> find(String input) {
        if (input == null || input.isBlank()) return Optional.empty();
        String normalized = input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return ISLANDS.stream().filter(island ->
                island.normalizedId().equals(normalized)
                        || island.displayName().toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]", "").equals(normalized)
        ).findFirst();
    }

    private IslandCatalog() {}
}
