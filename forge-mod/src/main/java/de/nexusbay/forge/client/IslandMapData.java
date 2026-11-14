package de.nexusbay.forge.client;

import java.util.List;

/**
 * Planned island atlas for the separate One Piece save.
 * Coordinates are canonical map anchors; terrain generation must place land around these anchors.
 */
public final class IslandMapData {
    public record Island(String name, String region, int requiredLevel, int worldX, int worldZ, String theme, int radius) {}

    public static final int MIN_X = -18000;
    public static final int MIN_Z = -16000;
    public static final int MAX_X = 36000;
    public static final int MAX_Z = 19000;

    public static final List<Island> ISLANDS = List.of(
            new Island("Foosha Village", "East Blue", 1, -18000, -9000, "Tropical", 700),
            new Island("Shells Town", "East Blue", 5, -14000, -4000, "Temperate", 650),
            new Island("Orange Town", "East Blue", 8, -9000, -11000, "Temperate", 600),
            new Island("Syrup Village", "East Blue", 12, -6500, -3000, "Forest", 700),
            new Island("Baratie", "East Blue", 16, -11000, 2000, "Ocean Platform", 180),
            new Island("Arlong Park", "East Blue", 20, -18000, 7000, "Tropical Reef", 650),
            new Island("Loguetown", "East Blue", 25, -7000, 9000, "Temperate", 850),
            new Island("Reverse Mountain", "Grand Line", 30, 0, -16000, "Mountain Pass", 1100),
            new Island("Whiskey Peak", "Grand Line", 34, 1500, -11000, "Desert", 700),
            new Island("Little Garden", "Grand Line", 38, -1800, -7000, "Prehistoric Jungle", 1000),
            new Island("Drum Island", "Grand Line", 42, -2500, -2500, "Snowy Mountain", 900),
            new Island("Alabasta", "Grand Line", 48, 2500, -1000, "Desert", 1800),
            new Island("Jaya", "Grand Line", 55, -2200, 3500, "Jungle", 850),
            new Island("Skypiea", "Grand Line", 62, 0, 5000, "Sky Island", 900),
            new Island("Water 7", "Grand Line", 68, 2500, 8500, "Canal City", 850),
            new Island("Enies Lobby", "Grand Line", 72, -2500, 11000, "Fortress", 900),
            new Island("Thriller Bark", "Grand Line", 78, 6000, -5000, "Haunted Forest", 1100),
            new Island("Sabaody Archipelago", "Grand Line", 82, 1000, 14500, "Mangrove", 1000),
            new Island("Amazon Lily", "Grand Line", 85, -6500, 15500, "Jungle", 850),
            new Island("Impel Down", "Grand Line", 88, 7000, 15000, "Underwater Fortress", 600),
            new Island("Marineford", "Grand Line", 92, 11000, 9000, "Fortress", 1100),
            new Island("Fish-Man Island", "Grand Line", 95, 0, 19000, "Underwater", 900),
            new Island("Punk Hazard", "New World", 98, 25000, -10000, "Volcanic / Ice", 1000),
            new Island("Dressrosa", "New World", 100, 30000, -4000, "Mediterranean", 1100),
            new Island("Zou", "New World", 105, 36000, 3000, "Moving Forest", 900),
            new Island("Whole Cake Island", "New World", 110, 28000, 7000, "Fantasy", 1200),
            new Island("Wano kuni", "New World", 120, 34000, 12000, "Mountain Forest", 1500),
            new Island("Onigashima", "New World", 125, 2752, 1472, "Oni Fortress", 650)
    );

    private IslandMapData() {}
}
