package de.nexusbay.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class IslandMapScreen extends Screen {
    private static final int PANEL = 0xE8172334;
    private static final int GOLD = 0xFFFFD36A;
    private static final int SEA = 0xFF102D49;
    private static final int LAND = 0xFF477D58;
    private static final int LOCKED = 0xFF9B5360;

    public IslandMapScreen() { super(Component.literal("NEXUS-BAY • Weltkarte")); }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int w = this.width, h = this.height;
        graphics.fill(0, 0, w, h, 0xD9000814);
        graphics.fill(18, 18, w - 18, h - 18, PANEL);
        graphics.drawString(this.font, "NEXUS-BAY  |  GRAND LINE ATLAS", 32, 29, GOLD, false);
        graphics.drawString(this.font, "M / ESC  -  Karte schließen", w - 190, 29, 0xFFB7C9D9, false);

        int mapLeft = 34, mapTop = 54, mapRight = w - 250, mapBottom = h - 36;
        graphics.fill(mapLeft, mapTop, mapRight, mapBottom, SEA);
        drawSeaGrid(graphics, mapLeft, mapTop, mapRight, mapBottom);
        int level = IslandMapClientEvents.playerLevel(Minecraft.getInstance());
        graphics.drawString(this.font, "Dein Level: " + level, mapLeft + 10, mapTop + 9, 0xFFFFFFFF, false);
        for (IslandMapData.Island island : IslandMapData.ISLANDS) {
            int x = worldToMapX(island.worldX(), mapLeft + 12, mapRight - 12);
            int y = worldToMapY(island.worldZ(), mapTop + 28, mapBottom - 14);
            boolean unlocked = level >= island.requiredLevel();
            graphics.fill(x - 5, y - 5, x + 6, y + 6, unlocked ? GOLD : LOCKED);
            graphics.fill(x - 2, y - 2, x + 3, y + 3, LAND);
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            int px = worldToMapX(minecraft.player.getBlockX(), mapLeft + 12, mapRight - 12);
            int py = worldToMapY(minecraft.player.getBlockZ(), mapTop + 28, mapBottom - 14);
            graphics.fill(px - 4, py - 4, px + 5, py + 5, 0xFFFFFFFF);
            graphics.fill(px - 2, py - 2, px + 3, py + 3, 0xFF36D9FF);
        }

        int listX = mapRight + 14;
        graphics.drawString(this.font, "INSELN & LEVEL", listX, mapTop + 2, GOLD, false);
        int rowY = mapTop + 18;
        int rowHeight = Math.max(12, Math.min(18, (mapBottom - rowY - 8) / IslandMapData.ISLANDS.size()));
        for (IslandMapData.Island island : IslandMapData.ISLANDS) {
            boolean unlocked = level >= island.requiredLevel();
            graphics.drawString(this.font, island.name(), listX, rowY, unlocked ? 0xFFBCE8C4 : 0xFFFFAAB2, false);
            String requirement = "Level " + island.requiredLevel() + (unlocked ? "  ✓" : "  LOCK");
            graphics.drawString(this.font, requirement, listX + 4, rowY + 9, unlocked ? 0xFF8FCB9A : 0xFFE29AA2, false);
            rowY += rowHeight;
        }
        graphics.drawString(this.font, "Cyan = du   Grün = Insel   Rot = gesperrt", 34, h - 27, 0xFFD5E4EF, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static int worldToMapX(int worldX, int left, int right) {
        double t = (worldX - IslandMapData.MIN_X) / (double)(IslandMapData.MAX_X - IslandMapData.MIN_X);
        return left + (int)(Math.max(0.0, Math.min(1.0, t)) * (right - left));
    }
    private static int worldToMapY(int worldZ, int top, int bottom) {
        double t = (worldZ - IslandMapData.MIN_Z) / (double)(IslandMapData.MAX_Z - IslandMapData.MIN_Z);
        return top + (int)(Math.max(0.0, Math.min(1.0, t)) * (bottom - top));
    }

    private static void drawSeaGrid(GuiGraphics g, int left, int top, int right, int bottom) {
        for (int x = left + 24; x < right; x += 32) g.fill(x, top, x + 1, bottom, 0x223E7899);
        for (int y = top + 24; y < bottom; y += 32) g.fill(left, y, right, y + 1, 0x223E7899);
        g.drawString(Minecraft.getInstance().font, "N", right - 14, top + 5, 0xFFB7C9D9, false);
    }

    static void renderMinimap(GuiGraphics g, int screenWidth, int screenHeight,
                              int playerX, int playerZ, int playerLevel) {
        int right = screenWidth - 8, top = 8, width = 148, height = 104;
        g.fill(right - width - 2, top - 2, right + 2, top + height + 2, 0xAA050D18);
        g.fill(right - width, top, right, top + height, 0xDD14283B);
        g.drawString(Minecraft.getInstance().font, "NEXUS-BAY  [M]", right - width + 6, top + 5, GOLD, false);
        int mapLeft = right - width + 6, mapTop = top + 20;
        int mapWidth = width - 12, mapHeight = height - 26;
        for (IslandMapData.Island island : IslandMapData.ISLANDS) {
            int x = worldToMapX(island.worldX(), mapLeft, mapLeft + mapWidth);
            int y = worldToMapY(island.worldZ(), mapTop, mapTop + mapHeight);
            g.fill(x - 1, y - 1, x + 2, y + 2, playerLevel >= island.requiredLevel() ? GOLD : LOCKED);
        }
        int px = worldToMapX(playerX, mapLeft, mapLeft + mapWidth);
        int py = worldToMapY(playerZ, mapTop, mapTop + mapHeight);
        g.fill(px - 3, py - 3, px + 4, py + 4, 0xFFFFFFFF);
        g.fill(px - 1, py - 1, px + 2, py + 2, 0xFF36D9FF);
    }

    @Override public boolean isPauseScreen() { return false; }
}
