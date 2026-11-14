package de.nexusbay.core;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Builds a large pirate harbor spawn around a center point.
 * Admin: /bayspawn build
 */
public final class HarborSpawn {

    private final NexusBay plugin;

    public HarborSpawn(NexusBay plugin) {
        this.plugin = plugin;
    }

    public void buildAt(Player admin) {
        Location loc = admin.getLocation();
        World w = loc.getWorld();
        if (w == null) return;
        int cx = loc.getBlockX();
        int cy = Math.max(62, loc.getBlockY());
        int cz = loc.getBlockZ();

        admin.sendMessage("§6§lNEXUS-BAY §8» §eHafen-Spawn wird gebaut… (kann kurz dauern)");

        // Platform / plaza
        fillBox(w, cx - 24, cy - 1, cz - 24, cx + 24, cy - 1, cz + 24, Material.STONE_BRICKS);
        fillBox(w, cx - 18, cy, cz - 18, cx + 18, cy, cz + 18, Material.POLISHED_ANDESITE);
        fillBox(w, cx - 8, cy, cz - 8, cx + 8, cy, cz + 8, Material.DARK_OAK_PLANKS);

        // Central monument
        for (int y = cy + 1; y <= cy + 8; y++) {
            set(w, cx, y, cz, Material.STONE_BRICKS);
            set(w, cx + 1, y, cz, Material.STONE_BRICKS);
            set(w, cx - 1, y, cz, Material.STONE_BRICKS);
            set(w, cx, y, cz + 1, Material.STONE_BRICKS);
            set(w, cx, y, cz - 1, Material.STONE_BRICKS);
        }
        set(w, cx, cy + 9, cz, Material.SEA_LANTERN);
        set(w, cx, cy + 10, cz, Material.RED_BANNER);

        // Ring of lanterns
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            int lx = cx + (int) (Math.cos(a) * 14);
            int lz = cz + (int) (Math.sin(a) * 14);
            set(w, lx, cy + 1, lz, Material.OAK_FENCE);
            set(w, lx, cy + 2, lz, Material.LANTERN);
        }

        // North dock
        buildDock(w, cx, cy, cz - 30, true);
        // South dock
        buildDock(w, cx, cy, cz + 22, false);

        // West tavern
        buildTavern(w, cx - 28, cy, cz - 6);

        // East lighthouse
        buildLighthouse(w, cx + 32, cy, cz);

        // Treasure chamber under plaza (entrance)
        buildTreasureChamber(w, cx + 10, cy, cz + 10);

        // Set world spawn + config
        w.setSpawnLocation(cx, cy + 1, cz);
        plugin.getConfig().set("spawn.world", w.getName());
        plugin.getConfig().set("spawn.x", cx + 0.5);
        plugin.getConfig().set("spawn.y", cy + 1);
        plugin.getConfig().set("spawn.z", cz + 0.5);
        plugin.getConfig().set("spawn.built", true);
        plugin.saveConfig();

        admin.teleport(new Location(w, cx + 0.5, cy + 1, cz + 0.5));
        admin.sendMessage("§aHafen-Spawn fertig. §7Welt-Spawn gesetzt.");
        admin.sendMessage("§7Enthalten: Plaza, Docks, Taverne, Leuchtturm, Schatzkammer-Eingang");
    }

    private void buildDock(World w, int cx, int cy, int zStart, boolean north) {
        int dir = north ? -1 : 1;
        for (int z = 0; z < 16; z++) {
            for (int x = -6; x <= 6; x++) {
                set(w, cx + x, cy - 1, zStart + z * dir, Material.OAK_PLANKS);
            }
            if (z % 3 == 0) {
                set(w, cx - 6, cy, zStart + z * dir, Material.OAK_FENCE);
                set(w, cx + 6, cy, zStart + z * dir, Material.OAK_FENCE);
                set(w, cx - 6, cy + 1, zStart + z * dir, Material.TORCH);
                set(w, cx + 6, cy + 1, zStart + z * dir, Material.TORCH);
            }
        }
        // Pillars into water
        for (int x = -6; x <= 6; x += 6) {
            for (int z = 0; z < 16; z += 4) {
                for (int y = cy - 2; y >= cy - 6; y--) {
                    set(w, cx + x, y, zStart + z * dir, Material.OAK_LOG);
                }
            }
        }
    }

    private void buildTavern(World w, int ox, int cy, int oz) {
        // Floor & walls
        fillBox(w, ox, cy, oz, ox + 10, cy, oz + 8, Material.SPRUCE_PLANKS);
        for (int y = cy + 1; y <= cy + 4; y++) {
            for (int x = ox; x <= ox + 10; x++) {
                set(w, x, y, oz, Material.SPRUCE_PLANKS);
                set(w, x, y, oz + 8, Material.SPRUCE_PLANKS);
            }
            for (int z = oz; z <= oz + 8; z++) {
                set(w, ox, y, z, Material.SPRUCE_PLANKS);
                set(w, ox + 10, y, z, Material.SPRUCE_PLANKS);
            }
        }
        // Door hole
        set(w, ox + 5, cy + 1, oz, Material.AIR);
        set(w, ox + 5, cy + 2, oz, Material.AIR);
        // Roof
        fillBox(w, ox - 1, cy + 5, oz - 1, ox + 11, cy + 5, oz + 9, Material.DARK_OAK_SLAB);
        // Interior
        set(w, ox + 2, cy + 1, oz + 2, Material.BARREL);
        set(w, ox + 3, cy + 1, oz + 2, Material.BARREL);
        set(w, ox + 8, cy + 1, oz + 6, Material.CRAFTING_TABLE);
        set(w, ox + 7, cy + 1, oz + 6, Material.FURNACE);
        set(w, ox + 5, cy + 1, oz + 4, Material.SPRUCE_STAIRS);
        set(w, ox + 1, cy + 3, oz + 4, Material.LANTERN);
        set(w, ox + 9, cy + 3, oz + 4, Material.LANTERN);
        // Sign post outside
        set(w, ox + 5, cy + 1, oz - 1, Material.OAK_FENCE);
        set(w, ox + 5, cy + 2, oz - 1, Material.OAK_SIGN);
    }

    private void buildLighthouse(World w, int ox, int cy, int oz) {
        int height = 22;
        for (int y = cy; y < cy + height; y++) {
            Material ring = (y / 3) % 2 == 0 ? Material.RED_CONCRETE : Material.WHITE_CONCRETE;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                        set(w, ox + dx, y, oz + dz, ring);
                    }
                }
            }
        }
        // Light room
        fillBox(w, ox - 2, cy + height, oz - 2, ox + 2, cy + height, oz + 2, Material.STONE_BRICKS);
        set(w, ox, cy + height + 1, oz, Material.SEA_LANTERN);
        set(w, ox, cy + height + 2, oz, Material.SEA_LANTERN);
        set(w, ox, cy + height + 3, oz, Material.LANTERN);
        // Door
        set(w, ox, cy + 1, oz - 2, Material.AIR);
        set(w, ox, cy + 2, oz - 2, Material.AIR);
    }

    private void buildTreasureChamber(World w, int ox, int cy, int oz) {
        // Stairs down
        for (int i = 0; i < 5; i++) {
            set(w, ox, cy - i, oz + i, Material.STONE_BRICK_STAIRS);
            set(w, ox + 1, cy - i, oz + i, Material.STONE_BRICK_STAIRS);
        }
        // Room
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
        Block b = w.getBlockAt(x, y, z);
        b.setType(m, false);
    }
}
