package de.nexusbay.forge.world;

import de.nexusbay.forge.NexusBayForge;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Applies the NEXUS-BAY island sampler to newly loaded full overworld chunks.
 * Generated columns are stamped in chunk NBT to avoid rebuilding them on reload.
 */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandTerrainChunkEvents {
    private static final String GENERATED_KEY = "nexusbay_island_terrain_v1";
    private static final int SEA_LEVEL = 63;

    private IslandTerrainChunkEvents() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(ServerLevel.OVERWORLD)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        if (chunk.getStatus().getChunkType() != net.minecraft.world.level.chunk.ChunkStatus.ChunkType.LEVELCHUNK) return;
        CompoundTag chunkData = event.getData();
        if (chunkData.getBoolean(GENERATED_KEY)) return;

        ChunkPos chunkPos = chunk.getPos();
        long seed = level.getSeed();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean touched = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = chunkPos.getMinBlockX() + dx;
                int z = chunkPos.getMinBlockZ() + dz;
                IslandTerrainSampler.ColumnSample sample = IslandTerrainSampler.sample(x, z, seed);
                if (!sample.land()) continue;

                int surfaceY = sample.surfaceY();
                BlockState surface = block(sample.surfaceBlock(), Blocks.GRASS_BLOCK.defaultBlockState());
                BlockState subsurface = block(sample.subsurfaceBlock(), Blocks.DIRT.defaultBlockState());

                for (int y = level.getMinBuildHeight(); y <= Math.max(SEA_LEVEL, surfaceY); y++) {
                    pos.set(x, y, z);
                    BlockState state;
                    if (y > surfaceY) {
                        state = y <= SEA_LEVEL ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    } else if (y == surfaceY) {
                        state = surface;
                    } else if (y >= surfaceY - 4) {
                        state = subsurface;
                    } else {
                        state = Blocks.STONE.defaultBlockState();
                    }
                    chunk.setBlockState(pos, state, false);
                }
                if (sample.normalizedDistance() < 0.82D
                        && vegetationRoll(x, z, seed) < vegetationChance(sample.vegetation())) {
                    placeVegetation(chunk, x, surfaceY + 1, z, sample.vegetation(), seed);
                    touched = true;
                }
                touched = true;
            }
        }

        chunkData.putBoolean(GENERATED_KEY, true);
        if (touched) chunk.setUnsaved(true);
    }


    private static double vegetationChance(String type) {
        return switch (type) {
            case "none" -> 0.0D;
            case "cactus", "desert_oasis" -> 0.035D;
            case "sparse_oak", "mountain_sparse" -> 0.055D;
            case "cherry_and_bamboo", "dense_jungle", "giant_forest", "giant_ferns" -> 0.19D;
            default -> 0.12D;
        };
    }

    private static double vegetationRoll(int x, int z, long seed) {
        long value = seed ^ ((long) x * 0x9E3779B97F4A7C15L) ^ ((long) z * 0xC2B2AE3D27D4EB4FL);
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (value >>> 11) * 0x1.0p-53;
    }

    private static void placeVegetation(LevelChunk chunk, int x, int y, int z, String type, long seed) {
        if (y < chunk.getMinBuildHeight() || y + 6 >= chunk.getMaxBuildHeight()) return;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        if (!chunk.getBlockState(pos.below()).isSolid()) return;
        if (!chunk.getBlockState(pos).isAir()) return;

        if (type.equals("cactus")) {
            int height = 1 + (int) (vegetationRoll(x + 7, z - 3, seed) * 3.0D);
            for (int i = 0; i < height; i++) {
                pos.set(x, y + i, z);
                if (!chunk.getBlockState(pos).isAir()) break;
                chunk.setBlockState(pos, Blocks.CACTUS.defaultBlockState(), false);
            }
            return;
        }

        if (type.equals("none") || type.equals("canal_city") || type.equals("ocean_restaurant")) return;
        boolean jungle = type.contains("jungle") || type.contains("palm") || type.contains("fern");
        boolean spruce = type.contains("spruce") || type.contains("snow");
        boolean cherry = type.contains("cherry") || type.contains("candy");
        var log = jungle ? Blocks.JUNGLE_LOG : spruce ? Blocks.SPRUCE_LOG
                : cherry ? Blocks.CHERRY_LOG : Blocks.OAK_LOG;
        var leaves = jungle ? Blocks.JUNGLE_LEAVES : spruce ? Blocks.SPRUCE_LEAVES
                : cherry ? Blocks.CHERRY_LEAVES : Blocks.OAK_LEAVES;
        int trunkHeight = 3 + (int) (vegetationRoll(x, z + 11, seed) * 3.0D);
        for (int i = 0; i < trunkHeight; i++) {
            pos.set(x, y + i, z);
            if (!chunk.getBlockState(pos).isAir()) return;
            chunk.setBlockState(pos, log.defaultBlockState(), false);
        }
        int crownY = y + trunkHeight - 1;
        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                if (Math.abs(ox) == 2 && Math.abs(oz) == 2) continue;
                pos.set(x + ox, crownY, z + oz);
                if (pos.getX() >> 4 != chunk.getPos().x || pos.getZ() >> 4 != chunk.getPos().z) continue;
                if (chunk.getBlockState(pos).isAir()) {
                    chunk.setBlockState(pos, leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), false);
                }
            }
        }
        pos.set(x, crownY + 1, z);
        if (chunk.getBlockState(pos).isAir()) {
            chunk.setBlockState(pos, leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), false);
        }
    }

    private static BlockState block(String id, BlockState fallback) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) return fallback;
        var block = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(key);
        return block == null ? fallback : block.defaultBlockState();
    }
}
