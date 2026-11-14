package de.nexusbay.core;

import java.util.Locale;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Seed-like deterministic island profiles for the first NEXUS-BAY worldgen batch.
 * Generation is intentionally bounded to one island footprint. Large-scale async
 * chunk scheduling is a follow-up optimization; do not call this for huge maps.
 */
public final class IslandService {
    private final NexusBay plugin;

    private enum Profile { DRUM, ALABASTA, SKYPIA, WATER7, SABAODY, STARTER }

    public IslandService(NexusBay plugin) { this.plugin = plugin; }

    public boolean create(String name) {
        ConfigurationSection all = plugin.getConfig().getConfigurationSection("islands");
        if (all == null) all = plugin.getConfig().createSection("islands");
        String key = name.toLowerCase(Locale.ROOT);
        if (all.contains(key)) return false;

        World world = Bukkit.getWorld("world");
        if (world == null) return false;

        Profile profile = profileFor(name);
        int spacing = plugin.getConfig().getInt("settings.island-spacing", 320);
        int index = all.getKeys(false).size();
        int x = (index % 8) * spacing;
        int z = (index / 8) * spacing;
        int y = profile == Profile.SKYPIA ? 160 : 100;

        buildIsland(world, x, y, z, profile);
        all.set(key + ".name", name);
        all.set(key + ".profile", profile.name().toLowerCase(Locale.ROOT));
        all.set(key + ".world", world.getName());
        all.set(key + ".x", x + 0.5);
        all.set(key + ".y", y + 3);
        all.set(key + ".z", z + 0.5);
        plugin.saveConfig();
        return true;
    }

    public boolean visit(org.bukkit.entity.Player player, String name) {
        String key = name.toLowerCase(Locale.ROOT);
        if (!plugin.getConfig().contains("islands." + key)) return false;
        String worldName = plugin.getConfig().getString("islands." + key + ".world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;
        double x = plugin.getConfig().getDouble("islands." + key + ".x");
        double y = plugin.getConfig().getDouble("islands." + key + ".y");
        double z = plugin.getConfig().getDouble("islands." + key + ".z");
        player.teleport(new Location(world, x, y, z));
        return true;
    }

    private Profile profileFor(String name) {
        String id = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return switch (id) {
            case "drum", "drumisland" -> Profile.DRUM;
            case "alabasta", "arabasta" -> Profile.ALABASTA;
            case "skypia", "skypiea" -> Profile.SKYPIA;
            case "water7", "waterseven" -> Profile.WATER7;
            case "sabaody", "sabaodyarchipelago", "sabaodyarchipel" -> Profile.SABAODY;
            default -> Profile.STARTER;
        };
    }

    private void buildIsland(World world, int cx, int cy, int cz, Profile profile) {
        int radius = plugin.getConfig().getInt("settings.island-radius", 42);
        if (profile == Profile.SKYPIA) {
            buildSkyTerrain(world, cx, cy, cz, radius);
            buildSkyLandmarks(world, cx, cy, cz, radius);
            return;
        }

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                double edge = radius * (0.84 + 0.08 * Math.sin((x + cz) * 0.19) + 0.08 * Math.cos((z + cx) * 0.17));
                if (distance > edge) continue;

                int top = surfaceHeight(profile, x, z, cy, radius);
                int depth = Math.max(4, (int) ((1.0 - distance / radius) * 10) + 4);
                Material surface = surfaceMaterial(profile, x, z, top, cy);
                Material filler = fillerMaterial(profile);
                for (int d = 0; d <= depth; d++) {
                    Material material = d == 0 ? surface : d < depth - 1 ? filler : Material.STONE;
                    world.getBlockAt(cx + x, top - d, cz + z).setType(material, false);
                }
                decorateSurface(world, cx + x, top + 1, cz + z, profile, x, z, distance, radius);
            }
        }

        buildOceanRing(world, cx, cy, cz, radius);
        switch (profile) {
            case DRUM -> buildDrumLandmarks(world, cx, cy, cz, radius);
            case ALABASTA -> buildAlabastaLandmarks(world, cx, cy, cz, radius);
            case WATER7 -> buildWater7Landmarks(world, cx, cy, cz, radius);
            case SABAODY -> buildSabaodyLandmarks(world, cx, cy, cz, radius);
            default -> buildStarterLandmarks(world, cx, cy, cz, radius);
        }
    }

    private int surfaceHeight(Profile profile, int x, int z, int cy, int radius) {
        double distance = Math.sqrt(x * x + z * z);
        double rolling = Math.sin(x * 0.16) * 2 + Math.cos(z * 0.13) * 2;
        return switch (profile) {
            case DRUM -> {
                double peakA = Math.max(0, 1.0 - Math.sqrt((x + 12) * (x + 12) + (z + 5) * (z + 5)) / (radius * 0.55));
                double peakB = Math.max(0, 1.0 - Math.sqrt((x - 14) * (x - 14) + (z - 9) * (z - 9)) / (radius * 0.42));
                yield cy + (int) (rolling + peakA * 42 + peakB * 28);
            }
            case ALABASTA -> cy + (int) (rolling * 0.5 + Math.sin((x + z) * 0.09) * 4 + Math.cos((x - z) * 0.07) * 3);
            case WATER7 -> cy + (int) (Math.sin(x * 0.11) + Math.cos(z * 0.12));
            case SABAODY -> cy + (int) (rolling + Math.sin(x * 0.08) * 2);
            default -> cy + (int) rolling;
        };
    }

    private Material surfaceMaterial(Profile profile, int x, int z, int top, int cy) {
        return switch (profile) {
            case DRUM -> top > cy + 23 ? Material.SNOW_BLOCK : top > cy + 12 ? Material.STONE : Material.SNOW_BLOCK;
            case ALABASTA -> Math.floorMod(x * 19 + z * 23, 11) == 0 ? Material.RED_SAND : Material.SAND;
            case WATER7 -> Material.STONE_BRICKS;
            case SABAODY -> Math.floorMod(x * 13 - z * 7, 9) == 0 ? Material.COARSE_DIRT : Material.GRASS_BLOCK;
            default -> Material.GRASS_BLOCK;
        };
    }

    private Material fillerMaterial(Profile profile) {
        return switch (profile) {
            case DRUM -> Material.STONE;
            case ALABASTA -> Material.SANDSTONE;
            case WATER7 -> Material.STONE_BRICKS;
            case SABAODY -> Material.DIRT;
            default -> Material.DIRT;
        };
    }

    private void decorateSurface(World world, int x, int y, int z, Profile profile, int ox, int oz, double distance, int radius) {
        if (distance > radius * 0.78) return;
        switch (profile) {
            case DRUM -> {
                if (Math.floorMod(ox * 31 + oz * 17, 61) == 0) world.getBlockAt(x, y, z).setType(Material.SPRUCE_SAPLING, false);
            }
            case ALABASTA -> {
                if (Math.floorMod(ox * 29 + oz * 13, 181) == 0) {
                    for (int h = 0; h < 4; h++) world.getBlockAt(x, y + h, z).setType(Material.OAK_LOG, false);
                        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
                            if (Math.abs(dx) + Math.abs(dz) < 4) world.getBlockAt(x + dx, y + 4, z + dz).setType(Material.OAK_LEAVES, false);
                }
            }
            case SABAODY -> {
                if (Math.floorMod(ox * 17 + oz * 31, 43) == 0) world.getBlockAt(x, y, z).setType(Material.OAK_LEAVES, false);
            }
            default -> { }
        }
    }

    private void buildOceanRing(World world, int cx, int cy, int cz, int radius) {
        int sea = cy - 10;
        for (int x = -(radius + 24); x <= radius + 24; x++) {
            for (int z = -(radius + 24); z <= radius + 24; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance > radius - 2 && distance <= radius + 24) {
                    for (int y = sea - 2; y <= sea; y++) world.getBlockAt(cx + x, y, cz + z).setType(Material.WATER, false);
                }
            }
        }
    }

    private void buildStarterLandmarks(World w, int cx, int cy, int cz, int radius) {
        buildDock(w, cx, cy, cz, radius);
        buildTower(w, cx - radius / 3, cy, cz - radius / 3, Material.COBBLESTONE, 12);
        w.getBlockAt(cx, cy + 1, cz).setType(Material.CHEST, false);
    }

    private void buildDock(World w, int cx, int cy, int cz, int radius) {
        int dockZ = cz + radius / 2;
        for (int x = -5; x <= 5; x++) for (int z = 0; z < 12; z++)
            w.getBlockAt(cx + x, cy - 1, dockZ + z).setType(Material.OAK_PLANKS, false);
        for (int x = -5; x <= 5; x += 5) for (int z = 0; z < 12; z += 3)
            w.getBlockAt(cx + x, cy, dockZ + z).setType(Material.OAK_FENCE, false);
    }

    private void buildTower(World w, int x, int y, int z, Material material, int height) {
        for (int dy = 0; dy <= height; dy++) {
            Material block = dy == height ? Material.SEA_LANTERN : material;
            w.getBlockAt(x, y + dy, z).setType(block, false);
            if (dy < height - 1) {
                w.getBlockAt(x + 1, y + dy, z).setType(block, false);
                w.getBlockAt(x - 1, y + dy, z).setType(block, false);
                w.getBlockAt(x, y + dy, z + 1).setType(block, false);
                w.getBlockAt(x, y + dy, z - 1).setType(block, false);
            }
        }
    }

    private void buildDrumLandmarks(World w, int cx, int cy, int cz, int radius) {
        buildDock(w, cx, cy, cz, radius);
        int baseX = cx - 12, baseZ = cz - 8;
        for (int layer = 0; layer < 5; layer++) {
            int y = cy + 20 + layer * 3;
            int half = 8 - layer;
            for (int x = -half; x <= half; x++) for (int z = -half; z <= half; z++)
                if (Math.abs(x) == half || Math.abs(z) == half || layer == 0)
                    w.getBlockAt(baseX + x, y, baseZ + z).setType(Material.SNOW_BLOCK, false);
        }
        for (int y = cy + 21; y < cy + 34; y++) {
            w.getBlockAt(baseX, y, baseZ).setType(Material.SPRUCE_LOG, false);
            w.getBlockAt(baseX + 1, y, baseZ).setType(Material.SPRUCE_LOG, false);
        }
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
            w.getBlockAt(baseX + x, cy + 35, baseZ + z).setType(Material.SNOW_BLOCK, false);
    }

    private void buildAlabastaLandmarks(World w, int cx, int cy, int cz, int radius) {
        buildDock(w, cx, cy, cz, radius);
        int px = cx + 8, pz = cz - 8, py = cy + 1;
        for (int x = -10; x <= 10; x++) for (int z = -8; z <= 8; z++)
            w.getBlockAt(px + x, py, pz + z).setType(Material.SMOOTH_SANDSTONE, false);
        for (int y = py + 1; y <= py + 9; y++) {
            for (int x = -8; x <= 8; x++) {
                w.getBlockAt(px + x, y, pz - 6).setType(Material.SANDSTONE, false);
                w.getBlockAt(px + x, y, pz + 6).setType(Material.SANDSTONE, false);
            }
            for (int z = -6; z <= 6; z++) {
                w.getBlockAt(px - 8, y, pz + z).setType(Material.SANDSTONE, false);
                w.getBlockAt(px + 8, y, pz + z).setType(Material.SANDSTONE, false);
            }
        }
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
            w.getBlockAt(px + x, py + 10, pz + z).setType(Material.SMOOTH_SANDSTONE, false);
        w.getBlockAt(cx - 18, cy + 1, cz + 4).setType(Material.WATER, false);
    }

    private void buildWater7Landmarks(World w, int cx, int cy, int cz, int radius) {
        buildDock(w, cx, cy, cz, radius);
        // A cross-shaped canal with stone quay walls and a walkable city grid.
        for (int offset = -radius + 8; offset <= radius - 8; offset++) {
            for (int width = -3; width <= 3; width++) {
                w.getBlockAt(cx + width, cy, cz + offset).setType(Material.WATER, false);
                w.getBlockAt(cx + width, cy + 1, cz + offset).setType(Material.AIR, false);
                w.getBlockAt(cx + offset, cy, cz + width).setType(Material.WATER, false);
                w.getBlockAt(cx + offset, cy + 1, cz + width).setType(Material.AIR, false);
            }
        }
        for (int x = -radius + 10; x <= radius - 10; x += 14) {
            for (int z = -radius + 10; z <= radius - 10; z += 14) {
                if (Math.abs(x) < 9 || Math.abs(z) < 9) continue;
                buildHouse(w, cx + x, cy + 1, cz + z, 7, 6, Material.BRICKS);
            }
        }
        buildTower(w, cx - 20, cy + 1, cz - 20, Material.BRICKS, 15);
    }

    private void buildHouse(World w, int x, int y, int z, int width, int height, Material wall) {
        for (int dx = 0; dx < width; dx++) for (int dz = 0; dz < width; dz++) {
            boolean edge = dx == 0 || dz == 0 || dx == width - 1 || dz == width - 1;
            for (int dy = 0; dy < height; dy++) {
                if (edge && dy != 2) w.getBlockAt(x + dx, y + dy, z + dz).setType(wall, false);
            }
            w.getBlockAt(x + dx, y + height, z + dz).setType(Material.DARK_OAK_PLANKS, false);
        }
        w.getBlockAt(x + width / 2, y + 1, z).setType(Material.AIR, false);
    }

    private void buildSabaodyLandmarks(World w, int cx, int cy, int cz, int radius) {
        buildDock(w, cx, cy, cz, radius);
        int[][] groves = {{-18, -15}, {17, -15}, {-18, 16}, {17, 16}, {0, 0}};
        for (int[] grove : groves) {
            int x = cx + grove[0], z = cz + grove[1];
            int height = grove[0] == 0 ? 25 : 18;
            for (int y = cy; y < cy + height; y++) {
                w.getBlockAt(x, y, z).setType(Material.OAK_LOG, false);
                if (y % 4 == 0) w.getBlockAt(x + 1, y, z).setType(Material.OAK_LOG, false);
            }
            int crownY = cy + height;
            for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++)
                if (dx * dx + dz * dz <= 28)
                    for (int dy = -2; dy <= 2; dy++)
                        w.getBlockAt(x + dx, crownY + dy, z + dz).setType(Material.OAK_LEAVES, false);
        }
        for (int x = -12; x <= 12; x++) {
            w.getBlockAt(cx + x, cy + 2, cz - 4).setType(Material.OAK_PLANKS, false);
            w.getBlockAt(cx + x, cy + 2, cz + 4).setType(Material.OAK_PLANKS, false);
        }
    }

    private void buildSkyTerrain(World w, int cx, int cy, int cz, int radius) {
        // Floating landmass with a stepped underside and a broad safe top.
        for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
            double distance = Math.sqrt(x * x + z * z);
            double edge = radius * (0.86 + 0.08 * Math.sin(x * 0.2) + 0.06 * Math.cos(z * 0.17));
            if (distance > edge) continue;
            int top = cy + (int) (Math.sin(x * 0.12) * 2 + Math.cos(z * 0.11) * 2);
            int depth = Math.max(5, (int) ((1 - distance / radius) * 13) + 5);
            for (int d = 0; d <= depth; d++) {
                Material material = d == 0 ? Material.GRASS_BLOCK : d < depth - 2 ? Material.DIRT : Material.STONE;
                w.getBlockAt(cx + x, top - d, cz + z).setType(material, false);
            }
        }
        // Small satellite cloud-islands and connecting bridges.
        for (int i = 0; i < 3; i++) {
            int sx = cx + (i - 1) * 27, sz = cz + (i % 2 == 0 ? -29 : 28);
            for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++)
                if (x * x + z * z <= 42) w.getBlockAt(sx + x, cy - 2, sz + z).setType(Material.QUARTZ_BLOCK, false);
            for (int step = 0; step <= 12; step++)
                w.getBlockAt(cx + (sx - cx) * step / 12, cy, cz + (sz - cz) * step / 12).setType(Material.QUARTZ_BLOCK, false);
        }
    }

    private void buildSkyLandmarks(World w, int cx, int cy, int cz, int radius) {
        for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++)
            w.getBlockAt(cx + x, cy + 1, cz + z).setType(Material.QUARTZ_BLOCK, false);
        buildTower(w, cx, cy + 2, cz, Material.QUARTZ_BLOCK, 18);
        for (int x = -5; x <= 5; x++) for (int z = -5; z <= 5; z++)
            if (Math.abs(x) == 5 || Math.abs(z) == 5)
                w.getBlockAt(cx + x, cy + 2, cz + z).setType(Material.GLOWSTONE, false);
    }
}
