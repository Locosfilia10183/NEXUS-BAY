package de.nexusbay.forge.world;

import de.nexusbay.forge.NexusBayForge;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.world.ChunkDataEvent;
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
                touched = true;
            }
        }

        chunkData.putBoolean(GENERATED_KEY, true);
        if (touched) chunk.setUnsaved(true);
    }

    private static BlockState block(String id, BlockState fallback) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) return fallback;
        var block = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(key);
        return block == null ? fallback : block.defaultBlockState();
    }
}
