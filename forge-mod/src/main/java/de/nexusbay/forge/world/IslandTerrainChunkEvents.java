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
                if (!sample.land()) {
                    applyOceanColumn(chunk, pos, x, z, level.getMinBuildHeight(), seed);
                    touched = true;
                    continue;
                }

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
                if (sample.normalizedDistance() < 0.9D
                        && vegetationRoll(x + 31, z - 19, seed) < 0.018D) {
                    placeRockOutcrop(chunk, x, surfaceY + 1, z, sample.surfaceBlock(), seed);
                    touched = true;
                }
                if (sample.normalizedDistance() < 0.72D
                        && vegetationRoll(x + 101, z - 73, seed) < landmarkChance(sample.islandId())) {
                    placeIslandLandmark(chunk, x, surfaceY + 1, z, sample.islandId(), seed);
                    touched = true;
                }
                touched = true;
            }
        }

        chunkData.putBoolean(GENERATED_KEY, true);
        if (touched) chunk.setUnsaved(true);
    }





    private static void applyOceanColumn(LevelChunk chunk, BlockPos.MutableBlockPos pos,
                                         int x, int z, int minBuildHeight, long seed) {
        int floorY = IslandCatalog.OCEAN_FLOOR;
        for (int y = minBuildHeight; y <= IslandCatalog.SEA_LEVEL; y++) {
            pos.set(x, y, z);
            BlockState state;
            if (y < floorY - 1) {
                state = Blocks.STONE.defaultBlockState();
            } else if (y <= floorY) {
                state = Blocks.GRAVEL.defaultBlockState();
            } else {
                state = Blocks.WATER.defaultBlockState();
            }
            chunk.setBlockState(pos, state, false);
        }

        // Sparse kelp on the seabed gives shallow ocean areas a little life.
        if (vegetationRoll(x + 211, z - 137, seed) < 0.012D) {
            pos.set(x, floorY + 1, z);
            if (chunk.getBlockState(pos).getBlock() == Blocks.WATER) {
                int height = 1 + (int) (vegetationRoll(x - 89, z + 53, seed) * 4.0D);
                for (int i = 0; i < height; i++) {
                    pos.set(x, floorY + 1 + i, z);
                    if (chunk.getBlockState(pos).getBlock() != Blocks.WATER) break;
                    chunk.setBlockState(pos, Blocks.KELP.defaultBlockState(), false);
                }
            }
        }
    }

    private static double landmarkChance(String islandId) {
        return switch (islandId) {
            case "Jaya", "DrumIsland", "Alabasta", "PunkHazard",
                    "Skypiea", "EniesLobby", "ThrillerBark", "Sabaody",
                    "Marineford", "WanoKuni", "Onigashima" -> 0.0009D;
            default -> 0.0D;
        };
    }

    private static void placeIslandLandmark(LevelChunk chunk, int x, int y, int z, String islandId, long seed) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        if (x < chunk.getPos().getMinBlockX() + 3 || x > chunk.getPos().getMaxBlockX() - 3
                || z < chunk.getPos().getMinBlockZ() + 3 || z > chunk.getPos().getMaxBlockZ() - 3) return;

        if (islandId.equals("Alabasta")) {
            for (int ox = -2; ox <= 2; ox++) {
                for (int oz = -2; oz <= 2; oz++) {
                    if (ox * ox + oz * oz > 5) continue;
                    pos.set(x + ox, y - 1, z + oz);
                    chunk.setBlockState(pos, Blocks.SANDSTONE.defaultBlockState(), false);
                    pos.set(x + ox, y, z + oz);
                    chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                }
            }
            pos.set(x, y, z);
            chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
            return;
        }

        if (islandId.equals("DrumIsland")) {
            int height = 5 + (int) (vegetationRoll(x + 47, z + 29, seed) * 8.0D);
            for (int i = 0; i < height; i++) {
                int width = Math.max(1, 3 - i / 3);
                for (int ox = -width; ox <= width; ox++) {
                    for (int oz = -width; oz <= width; oz++) {
                        if (Math.abs(ox) + Math.abs(oz) > width + 1) continue;
                        pos.set(x + ox, y + i, z + oz);
                        if (pos.getY() < chunk.getMaxBuildHeight()) {
                            chunk.setBlockState(pos, Blocks.PACKED_ICE.defaultBlockState(), false);
                        }
                    }
                }
            }
            return;
        }

        if (islandId.equals("Skypiea")) {
            for (int ox = -2; ox <= 2; ox++) {
                for (int oz = -2; oz <= 2; oz++) {
                    if (Math.abs(ox) + Math.abs(oz) > 3) continue;
                    pos.set(x + ox, y - 1, z + oz);
                    chunk.setBlockState(pos, Blocks.QUARTZ_BRICKS.defaultBlockState(), false);
                }
            }
            for (int i = 0; i < 5; i++) {
                pos.set(x, y + i, z);
                if (pos.getY() < chunk.getMaxBuildHeight())
                    chunk.setBlockState(pos, Blocks.QUARTZ_PILLAR.defaultBlockState(), false);
            }
            return;
        }

        if (islandId.equals("EniesLobby") || islandId.equals("Marineford")) {
            BlockState fortress = islandId.equals("EniesLobby")
                    ? Blocks.QUARTZ_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
            for (int side : new int[]{-2, 2}) {
                for (int i = 0; i < 6; i++) {
                    pos.set(x + side, y + i, z);
                    if (pos.getY() < chunk.getMaxBuildHeight())
                        chunk.setBlockState(pos, fortress, false);
                }
                pos.set(x + side, y + 6, z);
                if (pos.getY() < chunk.getMaxBuildHeight())
                    chunk.setBlockState(pos, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), false);
            }
            for (int ox = -2; ox <= 2; ox++) {
                pos.set(x + ox, y + 3, z);
                if (pos.getY() < chunk.getMaxBuildHeight())
                    chunk.setBlockState(pos, fortress, false);
            }
            return;
        }

        if (islandId.equals("ThrillerBark")) {
            for (int ox = -2; ox <= 2; ox += 2) {
                for (int oz = -2; oz <= 2; oz += 2) {
                    int height = 3 + (int) (vegetationRoll(x + ox, z + oz, seed) * 5.0D);
                    for (int i = 0; i < height; i++) {
                        pos.set(x + ox, y + i, z + oz);
                        if (pos.getY() < chunk.getMaxBuildHeight())
                            chunk.setBlockState(pos, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), false);
                    }
                }
            }
            return;
        }

        if (islandId.equals("Sabaody")) {
            int height = 7 + (int) (vegetationRoll(x + 61, z - 17, seed) * 8.0D);
            for (int i = 0; i < height; i++) {
                pos.set(x, y + i, z);
                if (pos.getY() < chunk.getMaxBuildHeight())
                    chunk.setBlockState(pos, Blocks.MANGROVE_LOG.defaultBlockState(), false);
            }
            for (int ox = -2; ox <= 2; ox++) {
                for (int oz = -2; oz <= 2; oz++) {
                    if (Math.abs(ox) + Math.abs(oz) > 3) continue;
                    pos.set(x + ox, y + height - 1, z + oz);
                    if (pos.getY() < chunk.getMaxBuildHeight())
                        chunk.setBlockState(pos, Blocks.MANGROVE_LEAVES.defaultBlockState()
                                .setValue(LeavesBlock.PERSISTENT, true), false);
                }
            }
            return;
        }

        if (islandId.equals("WanoKuni") || islandId.equals("Onigashima")) {
            BlockState castle = islandId.equals("WanoKuni")
                    ? Blocks.DEEPSLATE_TILES.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            for (int ox = -2; ox <= 2; ox++) {
                for (int oz = -2; oz <= 2; oz++) {
                    if (Math.abs(ox) == 2 || Math.abs(oz) == 2) {
                        for (int i = 0; i < 5; i++) {
                            pos.set(x + ox, y + i, z + oz);
                            if (pos.getY() < chunk.getMaxBuildHeight())
                                chunk.setBlockState(pos, castle, false);
                        }
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                pos.set(x, y + 5 + i, z);
                if (pos.getY() < chunk.getMaxBuildHeight())
                    chunk.setBlockState(pos, Blocks.DARK_OAK_LOG.defaultBlockState(), false);
            }
            return;
        }

        if (islandId.equals("PunkHazard")) {
            for (int ox = -2; ox <= 2; ox++) {
                for (int oz = -2; oz <= 2; oz++) {
                    if (ox * ox + oz * oz > 5) continue;
                    pos.set(x + ox, y, z + oz);
                    chunk.setBlockState(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), false);
                    if (ox == 0 && oz == 0) {
                        pos.set(x, y + 1, z);
                        chunk.setBlockState(pos, Blocks.LAVA.defaultBlockState(), false);
                    }
                }
            }
            return;
        }

        BlockState ruin = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        for (int pillar = 0; pillar < 3; pillar++) {
            int px = x + (pillar - 1) * 2;
            int height = 2 + (int) (vegetationRoll(px, z + pillar, seed) * 3.0D);
            for (int i = 0; i < height; i++) {
                pos.set(px, y + i, z);
                if (pos.getY() < chunk.getMaxBuildHeight()) chunk.setBlockState(pos, ruin, false);
            }
        }
        for (int ox = -2; ox <= 2; ox++) {
            pos.set(x + ox, y + 3, z);
            if (pos.getY() < chunk.getMaxBuildHeight()) chunk.setBlockState(pos, ruin, false);
        }
    }

    private static void placeRockOutcrop(LevelChunk chunk, int x, int y, int z, String surfaceId, long seed) {
        BlockState rock = surfaceId.contains("sand") ? Blocks.SANDSTONE.defaultBlockState()
                : surfaceId.contains("snow") ? Blocks.PACKED_ICE.defaultBlockState()
                : surfaceId.contains("basalt") || surfaceId.contains("deepslate")
                ? Blocks.BASALT.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
        int radius = 1 + (int) (vegetationRoll(x - 13, z + 23, seed) * 2.0D);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int ox = -radius; ox <= radius; ox++) {
            for (int oz = -radius; oz <= radius; oz++) {
                int px = x + ox;
                int pz = z + oz;
                if ((px >> 4) != chunk.getPos().x || (pz >> 4) != chunk.getPos().z) continue;
                double distance = Math.sqrt(ox * ox + oz * oz);
                if (distance > radius + 0.15D) continue;
                int height = distance < 0.75D ? 2 : 1;
                for (int dy = 0; dy < height; dy++) {
                    pos.set(px, y + dy, pz);
                    if (pos.getY() >= chunk.getMaxBuildHeight()) continue;
                    if (chunk.getBlockState(pos).isAir()) {
                        chunk.setBlockState(pos, rock, false);
                    }
                }
            }
        }
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
