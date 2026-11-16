package de.nexusbay.forge.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.world.IslandCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Automatic, read-only world-map snapshot writer.
 * Runs every 20 seconds while the server is online. It only inspects chunks
 * that are already loaded and never requests chunk loads or edits world data.
 */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class WorldMapExportCommands {
    private static final long EXPORT_INTERVAL_TICKS = 400L;
    private static final int SAMPLE_STEP = 16;
    private static final int ISLAND_SCAN_RADIUS = 96;
    private static long lastExportTick = Long.MIN_VALUE;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("NEXUS-BAY");

    private WorldMapExportCommands() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        long tick = overworld.getGameTime();
        if (tick - lastExportTick < EXPORT_INTERVAL_TICKS) return;
        lastExportTick = tick;
        writeSnapshot(server, overworld);
    }

    private static void writeSnapshot(MinecraftServer server, ServerLevel level) {
        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);
        root.addProperty("generatedAt", Instant.now().toString());
        root.addProperty("dimension", Level.OVERWORLD.location().toString());

        JsonArray islands = new JsonArray();
        for (IslandCatalog.Destination island : IslandCatalog.all()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", island.id());
            entry.addProperty("name", island.displayName());
            entry.addProperty("x", island.x());
            entry.addProperty("z", island.z());
            entry.addProperty("requiredLevel", island.requiredLevel());
            islands.add(entry);
        }
        root.add("islands", islands);

        JsonArray terrain = new JsonArray();
        int loadedSamples = 0;
        int skippedSamples = 0;
        for (IslandCatalog.Destination island : IslandCatalog.all()) {
            for (int x = island.x() - ISLAND_SCAN_RADIUS; x <= island.x() + ISLAND_SCAN_RADIUS; x += SAMPLE_STEP) {
                for (int z = island.z() - ISLAND_SCAN_RADIUS; z <= island.z() + ISLAND_SCAN_RADIUS; z += SAMPLE_STEP) {
                    int chunkX = x >> 4;
                    int chunkZ = z >> 4;
                    LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                    if (chunk == null) {
                        skippedSamples++;
                        continue;
                    }

                    int localX = x - (chunkX << 4);
                    int localZ = z - (chunkZ << 4);
                    int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, localX, localZ);
                    if (surfaceY <= level.getMinBuildHeight()) {
                        skippedSamples++;
                        continue;
                    }

                    BlockPos surfacePos = new BlockPos(x, surfaceY - 1, z);
                    String blockId = BuiltInRegistries.BLOCK.getKey(
                            chunk.getBlockState(surfacePos).getBlock()).toString();
                    JsonObject point = new JsonObject();
                    point.addProperty("islandId", island.id());
                    point.addProperty("x", x);
                    point.addProperty("z", z);
                    point.addProperty("y", surfaceY - 1);
                    point.addProperty("block", blockId);
                    terrain.add(point);
                    loadedSamples++;
                }
            }
        }
        root.add("terrain", terrain);

        JsonArray players = new JsonArray();
        for (ServerPlayer player : level.players()) {
            JsonObject marker = new JsonObject();
            marker.addProperty("name", player.getGameProfile().getName());
            marker.addProperty("x", player.getX());
            marker.addProperty("y", player.getY());
            marker.addProperty("z", player.getZ());
            players.add(marker);
        }
        root.add("players", players);
        root.addProperty("loadedTerrainSamples", loadedSamples);
        root.addProperty("unloadedTerrainSamplesSkipped", skippedSamples);

        Path output = server.getWorldPath(LevelResource.ROOT).resolve("nexusbay-map.json");
        try {
            Files.createDirectories(output.getParent());
            Path temporary = output.resolveSibling("nexusbay-map.json.tmp");
            Files.writeString(temporary, root.toString(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, output, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, output, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LOGGER.error("NEXUS-BAY map snapshot could not be written", exception);
        }
    }
}
