package de.nexusbay.forge.client;

import de.nexusbay.forge.world.IslandCatalog;

import java.util.List;

/** Client atlas view backed by the canonical server-side destination catalogue. */
public final class IslandMapData {
    public record Island(String name, int requiredLevel, int worldX, int worldZ) {}

    public static final int MIN_X = 512;
    public static final int MIN_Z = 512;
    public static final int MAX_X = 2752;
    public static final int MAX_Z = 1472;

    public static final List<Island> ISLANDS = IslandCatalog.all().stream()
            .map(destination -> new Island(destination.displayName(),
                    destination.requiredLevel(), destination.x(), destination.z()))
            .toList();

    private IslandMapData() {}
}
