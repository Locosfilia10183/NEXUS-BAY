package de.nexusbay.forge.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.List;

/**
 * Atlas-driven land mass density for the isolated One Piece world preset.
 * Island coordinates are serialized from island_catalog.json; no live-world
 * data or mutable random state is accessed during terrain sampling.
 */
public record IslandAtlasDensityFunction(List<Island> islands) implements DensityFunction.SimpleFunction {
    public static final MapCodecHolder CODEC_HOLDER = new MapCodecHolder(
            KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance
                    .group(Island.CODEC.listOf().fieldOf("islands").forGetter(IslandAtlasDensityFunction::islands))
                    .apply(instance, IslandAtlasDensityFunction::new)))
    );

    private static final double OCEAN_FLOOR = 38.0;
    private static final double SEA_LEVEL = 63.0;

    public IslandAtlasDensityFunction {
        islands = List.copyOf(islands);
    }

    @Override
    public double compute(FunctionContext context) {
        double x = context.blockX();
        double z = context.blockZ();
        double surface = OCEAN_FLOOR;

        for (Island island : islands) {
            double dx = x - island.x();
            double dz = z - island.z();
            double distanceSquared = dx * dx + dz * dz;
            double radius = island.radius();

            // Small coordinate-based coastline variation keeps silhouettes organic
            // while remaining deterministic across chunk borders and server restarts.
            double coastVariation = Math.sin(x * 0.0061 + z * 0.0037)
                    * Math.cos(z * 0.0053 - x * 0.0029) * radius * 0.025;
            double effectiveRadius = radius + coastVariation;
            if (distanceSquared > effectiveRadius * effectiveRadius) {
                continue;
            }

            double distance = Math.sqrt(distanceSquared);
            double normalized = Math.max(0.0, Math.min(1.0, distance / effectiveRadius));
            double inland = 1.0 - normalized;
            double islandSurface = SEA_LEVEL + 5.0
                    + (island.maxHeight() - (SEA_LEVEL + 5.0)) * Math.pow(inland, 1.45);

            // The highest overlapping island wins, allowing intentional archipelagos.
            surface = Math.max(surface, islandSurface);
        }

        return (surface - context.blockY()) / 16.0;
    }

    @Override
    public double minValue() {
        return (OCEAN_FLOOR - 320.0) / 16.0;
    }

    @Override
    public double maxValue() {
        return (240.0 - (-64.0)) / 16.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC_HOLDER.codec();
    }

    public record Island(String id, int x, int z, int radius, int maxHeight) {
        public static final Codec<Island> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        Codec.STRING.fieldOf("id").forGetter(Island::id),
                        Codec.INT.fieldOf("x").forGetter(Island::x),
                        Codec.INT.fieldOf("z").forGetter(Island::z),
                        Codec.intRange(64, 10000).fieldOf("radius").forGetter(Island::radius),
                        Codec.intRange(64, 300).fieldOf("maxHeight").forGetter(Island::maxHeight)
                )
                .apply(instance, Island::new));
    }

    /** Small wrapper keeps the codec declaration readable and strongly typed. */
    public record MapCodecHolder(KeyDispatchDataCodec<IslandAtlasDensityFunction> codec) {}
}
