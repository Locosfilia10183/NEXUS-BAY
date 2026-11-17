package de.nexusbay.forge.client;

import de.nexusbay.forge.network.CharacterNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Compact character sheet with server-authoritative stat allocation buttons. */
public final class CharacterScreen extends Screen {
    private int level, points, strength, defense, stamina, sword, fruit;
    private long xp;

    private CharacterScreen(int level, long xp, int points, int strength,
                            int defense, int stamina, int sword, int fruit) {
        super(Component.literal("NEXUS-BAY Charakter"));
        update(level, xp, points, strength, defense, stamina, sword, fruit);
    }

    public static void show(int level, long xp, int points, int strength,
                            int defense, int stamina, int sword, int fruit) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CharacterScreen screen) {
            screen.update(level, xp, points, strength, defense, stamina, sword, fruit);
        } else {
            mc.setScreen(new CharacterScreen(level, xp, points, strength, defense, stamina, sword, fruit));
        }
    }

    private void update(int level, long xp, int points, int strength,
                        int defense, int stamina, int sword, int fruit) {
        this.level = level; this.xp = xp; this.points = points;
        this.strength = strength; this.defense = defense; this.stamina = stamina;
        this.sword = sword; this.fruit = fruit;
    }

    @Override protected void init() {
        int left = this.width / 2 - 112;
        int top = 128;
        addStatButton(left, top, "Stärke", "strength", strength);
        addStatButton(left, top + 28, "Verteidigung", "defense", defense);
        addStatButton(left, top + 56, "Ausdauer", "stamina", stamina);
        addStatButton(left + 116, top, "Schwert", "sword", sword);
        addStatButton(left + 116, top + 28, "Teufelsfrucht", "fruit", fruit);
        addRenderableWidget(Button.builder(Component.literal("Schließen"), b -> onClose())
                .bounds(this.width / 2 - 50, this.height - 34, 100, 20).build());
    }

    private void addStatButton(int x, int y, String name, String id, int value) {
        addRenderableWidget(Button.builder(Component.literal("+1 " + name + " (" + value + ")"),
                b -> CharacterNetwork.allocate(id, 1)).bounds(x, y, 112, 20).build());
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int left = this.width / 2 - 124;
        int right = this.width / 2 + 124;
        g.fill(left, 24, right, this.height - 24, 0xEE101B2B);
        g.fill(left, 24, right, 54, 0xFF263C50);
        g.drawCenteredString(font, "NEXUS-BAY • CHARAKTER", this.width / 2, 35, 0xFFFFD36A);
        g.drawCenteredString(font, "Level " + level + "   |   Gesamt-XP: " + xp,
                this.width / 2, 68, 0xFFFFFFFF);
        g.drawCenteredString(font, "Freie Stat-Punkte: " + points,
                this.width / 2, 86, points > 0 ? 0xFF83D69A : 0xFFB8C7D8);
        g.drawCenteredString(font, "Klicke +1, um einen Punkt serverseitig zu verteilen.",
                this.width / 2, 108, 0xFFB8C7D8);
        g.drawCenteredString(font, "Stärke " + strength + "  •  Verteidigung " + defense
                + "  •  Ausdauer " + stamina, this.width / 2, 202, 0xFFE7EDF5);
        g.drawCenteredString(font, "Schwert " + sword + "  •  Teufelsfrucht " + fruit,
                this.width / 2, 216, 0xFFE7EDF5);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public boolean isPauseScreen() { return false; }
}
