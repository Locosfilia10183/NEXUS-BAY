package de.nexusbay.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Builds the main pirate harbor spawn automatically (once) or on demand.
 */
public final class HarborSpawn {

    private final NexusBay plugin;

    public HarborSpawn(NexusBay plugin) {
        this.plugin = plugin;
    }

    /** Called from onEnable – builds once if not marked built. */
    public void tryAutoBuild() {
        if (plugin.getConfig().getBoolean("spawn.built", false)) {
            plugin.getLogger().info("Harbor spawn already built – skip.");
            return;
        }
        World w = Bukkit.getWorld(plugin.getConfig().getString("spawn.world", "world"));
        if (w == null) {
            w = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        }
        if (w == null) {
            plugin.getLogger().warning("No world for harbor spawn.");
            return;
        }
        int cx = plugin.getConfig().getInt("spawn.build-x", 0);
        int cy = plugin.getConfig().getInt("spawn.build-y", 100);
        int cz = plugin.getConfig().getInt("spawn.build-z", 0);

        plugin.getLogger().info("Building NEXUS-BAY harbor spawn at " + cx + "," + cy + "," + cz + " …");
        build(w, cx, cy, cz);
        plugin.getLogger().info("Harbor spawn finished.");
    }

    public void buildAt(Player admin) {
        Location loc = admin.getLocation();
        World w = loc.getWorld();
        if (w == null) return;
        admin.sendMessage("§6§lNEXUS-BAY §8» §eHafen wird gebaut…");
        build(w, loc.getBlockX(), Math.max(62, loc.getBlockY()), loc.getBlockZ());
        admin.teleport(new Location(w,
                plugin.getConfig().getDouble("spawn.x"),
                plugin.getConfig().getDouble("spawn.y"),
                plugin.getConfig().getDouble("spawn.z")));
        admin.sendMessage("§aHafen-Spawn fertig.");
    }

    public void build(World w, int cx, int cy, int cz) {
        // Large plaza
        fillBox(w, cx - 28, cy - 1, cz - 28, cx + 28, cy - 1, cz + 28, Material.STONE_BRICKS);
        fillBox(w, cx - 22, cy, cz - 22, cx + 22, cy, cz + 22, Material.POLISHED_ANDESITE);
        fillBox(w, cx - 10, cy, cz - 10, cx + 10, cy, cz + 10, Material.DARK_OAK_PLANKS);

        // Paths
        fillBox(w, cx - 2, cy, cz - 22, cx + 2, cy, cz + 22, Material.SPRUCE_PLANKS);
        fillBox(w, cx - 22, cy, cz - 2, cx + 22, cy, cz + 2, Material.SPRUCE_PLANKS);

        // Central monument
        for (int y = cy + 1; y <= cy + 10; y++) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    if (Math.abs(dx) + Math.abs(dz) < 2)
                        set(w, cx + dx, y, cz + dz, Material.STONE_BRICKS);
        }
        set(w, cx, cy + 11, cz, Material.SEA_LANTERN);
        set(w, cx, cy + 12, cz, Material.SEA_LANTERN);
        set(w, cx, cy + 13, cz, Material.RED_BANNER);

        // Lantern ring
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI / 8;
            int lx = cx + (int) (Math.cos(a) * 16);
            int lz = cz + (int) (Math.sin(a) * 16);
            set(w, lx, cy + 1, lz, Material.OAK_FENCE);
            set(w, lx, cy + 2, lz, Material.LANTERN);
        }

        // Market stalls (4 corners of inner plaza)
        buildStall(w, cx - 14, cy, cz - 14);
        buildStall(w, cx + 8, cy, cz - 14);
        buildStall(w, cx - 14, cy, cz + 8);
        buildStall(w, cx + 8, cy, cz + 8);

        buildDock(w, cx, cy, cz - 36, true);
        buildDock(w, cx, cy, cz + 28, false);
        buildDock(w, cx - 36, cy, cz, false); // west-ish platform extension

        buildTavern(w, cx - 34, cy, cz - 8);
        buildTavern(w, cx + 24, cy, cz + 6); // second tavern / warehouse look

        buildLighthouse(w, cx + 40, cy, cz - 20);
        buildLighthouse(w, cx - 42, cy, cz + 18);

        buildTreasureChamber(w, cx + 12, cy, cz + 12);
        buildTreasureChamber(w, cx - 16, cy, cz + 14);

        // Small ship hull on north dock
        buildShipHull(w, cx - 3, cy, cz - 48);

        w.setSpawnLocation(cx, cy + 1, cz);
        plugin.getConfig().set("spawn.world", w.getName());
        plugin.getConfig().set("spawn.x", cx + 0.5);
        plugin.getConfig().set("spawn.y", cy + 1.0);
        plugin.getConfig().set("spawn.z", cz + 0.5);
        plugin.getConfig().set("spawn.built", true);
        plugin.getConfig().set("spawn.build-x", cx);
        plugin.getConfig().set("spawn.build-y", cy);
        plugin.getConfig().set("spawn.build-z", cz);
        plugin.saveConfig();
    }

    private void buildStall(World w, int ox, int cy, int oz) {
        fillBox(w, ox, cy, oz, ox + 4, cy, oz + 4, Material.OAK_PLANKS);
        for (int x = ox; x <= ox + 4; x += 4)
            for (int z = oz; z <= oz + 4; z += 4) {
                set(w, x, cy + 1, z, Material.OAK_FENCE);
                set(w, x, cy + 2, z, Material.OAK_FENCE);
            }
        fillBox(w, ox, cy + 3, oz, ox + 4, cy + 3, oz + 4, Material.WHITE_WOOL);
        set(w, ox + 2, cy + 1, oz + 2, Material.BARREL);
        set(w, ox + 1, cy + 1, oz + 2, Material.CHEST);
    }

    private void buildDock(World w, int cx, int cy, int zStart, boolean north) {
        int dir = north ? -1 : 1;
        for (int z = 0; z < 18; z++) {
            for (int x = -7; x <= 7; x++) {
                set(w, cx + x, cy - 1, zStart + z * dir, Material.OAK_PLANKS);
            }
            if (z % 3 == 0) {
                set(w, cx - 7, cy, zStart + z * dir, Material.OAK_FENCE);
                set(w, cx + 7, cy, zStart + z * dir, Material.OAK_FENCE);
                set(w, cx - 7, cy + 1, zStart + z * dir, Material.TORCH);
                set(w, cx + 7, cy + 1, zStart + z * dir, Material.TORCH);
            }
        }
        for (int x = -7; x <= 7; x += 7) {
            for (int z = 0; z < 18; z += 4) {
                for (int y = cy - 2; y >= cy - 7; y--)
                    set(w, cx + x, y, zStart + z * dir, Material.OAK_LOG);
            }
        }
    }

    private void buildShipHull(World w, int ox, int cy, int oz) {
        for (int z = 0; z < 14; z++) {
            int width = z < 3 || z > 11 ? 2 : 4;
            for (int x = -width; x <= width; x++) {
                set(w, ox + x, cy, oz - z, Material.SPRUCE_PLANKS);
                if (Math.abs(x) == width) {
                    set(w, ox + x, cy + 1, oz - z, Material.SPRUCE_PLANKS);
                    set(w, ox + x, cy + 2, oz - z, Material.SPRUCE_FENCE);
                }
            }
        }
        // Mast
        for (int y = cy + 1; y <= cy + 10; y++)
            set(w, ox, y, oz - 6, Material.OAK_LOG);
        set(w, ox, cy + 11, oz - 6, Material.WHITE_WOOL);
        set(w, ox, cy + 12, oz - 6, Material.RED_BANNER);
    }

    private void buildTavern(World w, int ox, int cy, int oz) {
        fillBox(w, ox, cy, oz, ox + 12, cy, oz + 9, Material.SPRUCE_PLANKS);
        for (int y = cy + 1; y <= cy + 4; y++) {
            for (int x = ox; x <= ox + 12; x++) {
                set(w, x, y, oz, Material.SPRUCE_PLANKS);
                set(w, x, y, oz + 9, Material.SPRUCE_PLANKS);
            }
            for (int z = oz; z <= oz + 9; z++) {
                set(w, ox, y, z, Material.SPRUCE_PLANKS);
                set(w, ox + 12, y, z, Material.SPRUCE_PLANKS);
            }
        }
        set(w, ox + 6, cy + 1, oz, Material.AIR);
        set(w, ox + 6, cy + 2, oz, Material.AIR);
        fillBox(w, ox - 1, cy + 5, oz - 1, ox + 13, cy + 5, oz + 10, Material.DARK_OAK_SLAB);
        set(w, ox + 2, cy + 1, oz + 3, Material.BARREL);
        set(w, ox + 3, cy + 1, oz + 3, Material.BARREL);
        set(w, ox + 10, cy + 1, oz + 7, Material.CRAFTING_TABLE);
        set(w, ox + 9, cy + 1, oz + 7, Material.FURNACE);
        set(w, ox + 6, cy + 1, oz + 5, Material.SPRUCE_STAIRS);
        set(w, ox + 2, cy + 3, oz + 5, Material.LANTERN);
        set(w, ox + 10, cy + 3, oz + 5, Material.LANTERN);
        set(w, ox + 6, cy + 1, oz - 1, Material.OAK_FENCE);
        set(w, ox + 6, cy + 2, oz - 1, Material.OAK_SIGN);
    }

    private void buildLighthouse(World w, int ox, int cy, int oz) {
        int height = 24;
        for (int y = cy; y < cy + height; y++) {
            Material ring = (y / 3) % 2 == 0 ? Material.RED_CONCRETE : Material.WHITE_CONCRETE;
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2)
                        set(w, ox + dx, y, oz + dz, ring);
        }
        fillBox(w, ox - 2, cy + height, oz - 2, ox + 2, cy + height, oz + 2, Material.STONE_BRICKS);
        set(w, ox, cy + height + 1, oz, Material.SEA_LANTERN);
        set(w, ox, cy + height + 2, oz, Material.SEA_LANTERN);
        set(w, ox, cy + height + 3, oz, Material.LANTERN);
        set(w, ox, cy + 1, oz - 2, Material.AIR);
        set(w, ox, cy + 2, oz - 2, Material.AIR);
    }

    private void buildTreasureChamber(World w, int ox, int cy, int oz) {
        for (int i = 0; i < 5; i++) {
            set(w, ox, cy - i, oz + i, Material.STONE_BRICK_STAIRS);
            set(w, ox + 1, cy - i, oz + i, Material.STONE_BRICK_STAIRS);
        }
        int by = cy - 5;
        fillBox(w, ox - 3, by, oz + 4, ox + 4, by, oz + 11, Material.STONE_BRICKS);
        fillBox(w, ox - 2, by + 1, oz + 5, ox + 3, by + 3, oz + 10, Material.AIR);
        set(w, ox, by + 1, oz + 8, Material.GOLD_BLOCK);
        set(w, ox + 1, by + 1, oz + 8, Material.CHEST);
        set(w, ox - 1, by + 1, oz + 8, Material.BARREL);
        set(w, ox + 1, by + 1, oz + 7, Material.DIAMOND_BLOCK);
        set(w, ox, by + 3, oz + 7, Material.LANTERN);
    }

    private void fillBox(World w, int x1, int y1, int z1, int x2, int y2, int z2, Material m) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++)
                    set(w, x, y, z, m);
    }

    private void set(World w, int x, int y, int z, Material m) {
        w.getBlockAt(x, y, z).setType(m, false);
    }
}
