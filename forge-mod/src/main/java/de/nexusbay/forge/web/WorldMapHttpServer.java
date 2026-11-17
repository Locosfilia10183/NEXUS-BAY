package de.nexusbay.forge.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.nexusbay.forge.NexusBayForge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

/**
 * Read-only HTTP host for the NEXUS-BAY atlas.
 * Serves the bundled map and a privacy-filtered server snapshot on port 8080.
 * Player coordinates are deliberately never exposed by this public endpoint.
 */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WorldMapHttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("NEXUS-BAY");
    private static final int PORT = 8080;
    private static HttpServer httpServer;
    private static volatile MinecraftServer minecraftServer;

    private WorldMapHttpServer() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        minecraftServer = event.getServer();
        try {
            httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            httpServer.createContext("/", WorldMapHttpServer::handle);
            httpServer.setExecutor(Executors.newFixedThreadPool(2, task -> {
                Thread thread = new Thread(task, "NEXUS-BAY-World-Map");
                thread.setDaemon(true);
                return thread;
            }));
            httpServer.start();
            LOGGER.info("NEXUS-BAY world map available on http://0.0.0.0:{}/", PORT);
        } catch (IOException exception) {
            LOGGER.error("Could not start NEXUS-BAY world map HTTP server on port {}", PORT, exception);
            httpServer = null;
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        minecraftServer = null;
        if (httpServer != null) {
            httpServer.stop(1);
            httpServer = null;
            LOGGER.info("NEXUS-BAY world map HTTP server stopped");
        }
    }

    private static void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "GET");
            send(exchange, 405, "text/plain; charset=utf-8", "Method not allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        exchange.getResponseHeaders().set("Cache-Control", "no-store, max-age=0");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");

        if ("/".equals(path) || "/index.html".equals(path)) {
            try (InputStream page = WorldMapHttpServer.class.getClassLoader().getResourceAsStream("web/index.html")) {
                if (page == null) {
                    send(exchange, 500, "text/plain; charset=utf-8", "World map page is missing from the mod");
                    return;
                }
                send(exchange, 200, "text/html; charset=utf-8", new String(page.readAllBytes(), StandardCharsets.UTF_8));
            }
            return;
        }

        if ("/nexusbay-map.json".equals(path)) {
            MinecraftServer server = minecraftServer;
            if (server == null) {
                send(exchange, 503, "application/json; charset=utf-8", "{\"error\":\"server_stopping\"}");
                return;
            }
            Path snapshot = server.getWorldPath(LevelResource.ROOT).resolve("nexusbay-map.json");
            if (!Files.isRegularFile(snapshot)) {
                send(exchange, 503, "application/json; charset=utf-8", "{\"error\":\"snapshot_not_ready\"}");
                return;
            }
            try {
                JsonObject data = JsonParser.parseString(Files.readString(snapshot, StandardCharsets.UTF_8)).getAsJsonObject();
                data.remove("players");
                data.addProperty("playersVisible", false);
                send(exchange, 200, "application/json; charset=utf-8", data.toString());
            } catch (RuntimeException exception) {
                LOGGER.warn("Could not read NEXUS-BAY map snapshot", exception);
                send(exchange, 500, "application/json; charset=utf-8", "{\"error\":\"snapshot_invalid\"}");
            }
            return;
        }

        send(exchange, 404, "text/plain; charset=utf-8", "Not found");
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
