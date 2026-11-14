package de.nexusbay.forge.client;

import java.util.List;

/** Canonical Grand Line atlas coordinates. Must match IslandProgression slot layout in the Paper core. */
public final class IslandMapData {
    public record Island(String name, int requiredLevel, int worldX, int worldZ) {}

    public static final int MIN_X = 512;
    public static final int MIN_Z = 512;
    public static final int MAX_X = 2752;
    public static final int MAX_Z = 1472;

    public static final List<Island> ISLANDS = List.of(
            new Island("Foosha Village", 1, 512, 512),
            new Island("Shells Town", 5, 832, 512),
            new Island("Orange Town", 8, 1152, 512),
            new Island("Syrup Village", 12, 1472, 512),
            new Island("Baratie", 16, 1792, 512),
            new Island("Arlong Park", 20, 2112, 512),
            new Island("Loguetown", 25, 2432, 512),
            new Island("Reverse Mountain", 30, 2752, 512),
            new Island("Whiskey Peak", 34, 512, 832),
            new Island("Little Garden", 38, 832, 832),
            new Island("Drum Island", 42, 1152, 832),
            new Island("Alabasta", 48, 1472, 832),
            new Island("Jaya", 55, 1792, 832),
            new Island("Skypiea", 62, 2112, 832),
            new Island("Water 7", 68, 2432, 832),
            new Island("Enies Lobby", 72, 2752, 832),
            new Island("Thriller Bark", 78, 512, 1152),
            new Island("Sabaody Archipelago", 82, 832, 1152),
            new Island("Amazon Lily", 85, 1152, 1152),
            new Island("Impel Down", 88, 1472, 1152),
            new Island("Marineford", 92, 1792, 1152),
            new Island("Fish-Man Island", 95, 2112, 1152),
            new Island("Punk Hazard", 98, 2432, 1152),
            new Island("Dressrosa", 100, 2752, 1152),
            new Island("Zou", 105, 512, 1472),
            new Island("Whole Cake Island", 110, 832, 1472),
            new Island("Wano kuni", 120, 1152, 1472),
            new Island("Onigashima", 125, 1472, 1472)
    );

    private IslandMapData() {}
}
