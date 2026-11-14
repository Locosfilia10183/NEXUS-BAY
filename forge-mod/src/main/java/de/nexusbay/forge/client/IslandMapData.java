package de.nexusbay.forge.client;

import java.util.List;

/** Static Grand Line progression atlas. Coordinates are normalized to the world-map panel. */
public final class IslandMapData {
    public record Island(String name, int requiredLevel, float x, float y) {}

    public static final List<Island> ISLANDS = List.of(
            new Island("Foosha Village", 1, .10F, .77F),
            new Island("Shells Town", 5, .17F, .66F),
            new Island("Orange Town", 8, .24F, .80F),
            new Island("Syrup Village", 12, .29F, .62F),
            new Island("Baratie", 16, .36F, .76F),
            new Island("Arlong Park", 20, .42F, .61F),
            new Island("Loguetown", 25, .48F, .78F),
            new Island("Reverse Mountain", 30, .53F, .48F),
            new Island("Whiskey Peak", 34, .60F, .62F),
            new Island("Little Garden", 38, .66F, .42F),
            new Island("Drum Island", 42, .72F, .72F),
            new Island("Alabasta", 48, .77F, .49F),
            new Island("Jaya", 55, .82F, .30F),
            new Island("Skypiea", 62, .88F, .17F),
            new Island("Water 7", 68, .71F, .20F),
            new Island("Enies Lobby", 72, .63F, .12F),
            new Island("Thriller Bark", 78, .52F, .28F),
            new Island("Sabaody Archipelago", 82, .42F, .16F),
            new Island("Amazon Lily", 85, .32F, .31F),
            new Island("Impel Down", 88, .23F, .18F),
            new Island("Marineford", 92, .15F, .36F),
            new Island("Fish-Man Island", 95, .08F, .51F),
            new Island("Punk Hazard", 98, .17F, .12F),
            new Island("Dressrosa", 100, .28F, .49F),
            new Island("Zou", 105, .38F, .91F),
            new Island("Whole Cake Island", 110, .57F, .89F),
            new Island("Wano kuni", 120, .76F, .91F),
            new Island("Onigashima", 125, .91F, .72F)
    );

    private IslandMapData() {}
}
