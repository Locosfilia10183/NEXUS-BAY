package de.nexusbay.forge.world;

import java.util.Optional;

/**
 * Deterministic island shape sampler shared by chunk-generation code.
 *
 * It never edits chunks itself. A future Forge ChunkGenerator/feature pipeline
 * can sample this class for each column and place the returned surface and
 * subsurface materials. The same seed and coordinates always produce the same
 * island outline.
 */
public final class IslandTerrainSampler {
    public record ColumnSample(
            String islandId,
            boolean land,
            int surfaceY,
            double normalizedDistance,
            String surfaceBlock,
            String subsurfaceBlock,
            String vegetation
    ) {
        public static ColumnSample ocean() {
            return new ColumnSample("", false, IslandCatalog.OCEAN_FLOOR,
                    Double.POSITIVE_INFINITY, "minecraft:gravel", "minecraft:stone", "none");
        }
    }

    public static ColumnSample sample(int blockX, int blockZ, long worldSeed) {
        IslandCatalog.Destination nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (IslandCatalog.Destination island : IslandCatalog.all()) {
            double dx = blockX - island.x();
            double dz = blockZ - island.z();
            double distance = Math.sqrt(dx * dx + dz * dz);
            Optional<IslandTerrainCatalog.Profile> candidate = IslandTerrainCatalog.find(island.id());
            if (candidate.isEmpty()) continue;
            double reach = candidate.get().radius() + 24.0D;
            if (distance <= reach && distance < nearestDistance) {
                nearest = island;
                nearestDistance = distance;
            }
        }

        if (nearest == null) return ColumnSample.ocean();

        IslandTerrainCatalog.Profile profile = IslandTerrainCatalog.find(nearest.id()).orElseThrow();
        double angle = Math.atan2(blockZ - nearest.z(), blockX - nearest.x());
        double edgeNoise = noise(blockX, blockZ, worldSeed);
        double coastRadius = profile.radius() * (0.82D + 0.10D * Math.sin(angle * 3.0D)
                + 0.07D * Math.cos(angle * 5.0D) + 0.035D * edgeNoise);
        double normalizedDistance = nearestDistance / coastRadius;

        if (normalizedDistance > 1.0D) return ColumnSample.ocean();

        double inland = 1.0D - normalizedDistance;
        double broadHill = Math.max(0.0D, Math.sin((blockX + worldSeed * 0.001D) * 0.035D)
                * Math.cos((blockZ - worldSeed * 0.001D) * 0.031D));
        double detail = noise(blockX * 2, blockZ * 2, worldSeed ^ 0x5DEECE66DL);
        double height = profile.baseHeight()
                + inland * profile.heightVariation() * (0.35D + 0.65D * broadHill)
                + detail * Math.min(8.0D, profile.heightVariation() * 0.18D);

        if (profile.climate() == IslandTerrainCatalog.Climate.UNDERWATER) {
            height = Math.min(profile.baseHeight(), height);
        }

        int surfaceY = Math.max(-60, Math.min(300, (int) Math.round(height)));
        return new ColumnSample(profile.islandId(), true, surfaceY, normalizedDistance,
                profile.surfaceBlock(), profile.subsurfaceBlock(), profile.vegetation());
    }

    private static double noise(int x, int z, long seed) {
        long value = seed;
        value ^= (long) x * 0x9E3779B97F4A7C15L;
        value = Long.rotateLeft(value, 27);
        value ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return ((value >>> 11) * 0x1.0p-53) * 2.0D - 1.0D;
    }

    private IslandTerrainSampler() {}
}
