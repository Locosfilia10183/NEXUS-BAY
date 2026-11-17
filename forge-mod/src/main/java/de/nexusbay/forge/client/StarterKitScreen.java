package de.nexusbay.forge.client;

import de.nexusbay.forge.network.StarterKitNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** First-join starter kit offer. Items are only granted after server confirmation. */
public final class StarterKitScreen extends Screen {
    private StarterKitScreen() {
        super(Component.literal("Willkommen in NEXUS-BAY"));
    }

    public static void open() {
        net.minecraft.client.Minecraft.getInstance().setScreen(new StarterKitScreen());
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        addRenderableWidget(Button.builder(
                Component.literal("STARTERPAKET ANNEHMEN"),
                button -> {
                    StarterKitNetwork.accept();
                    onClose();
                }).bounds(center - 105, this.height / 2 + 72, 210, 24).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int center = this.width / 2;
        int top = this.height / 2 - 100;
        graphics.fill(center - 150, top, center + 150, top + 190, 0xEE101B2B);
        graphics.fill(center - 150, top, center + 150, top + 34, 0xFF263C50);
        graphics.drawCenteredString(font, "WILLKOMMEN IN NEXUS-BAY",
                center, top + 12, 0xFFFFD36A);
        graphics.drawCenteredString(font, "Dein Abenteuer beginnt jetzt.",
                center, top + 48, 0xFFFFFFFF);
        graphics.drawCenteredString(font, "DEIN STARTERPAKET",
                center, top + 76, 0xFFFFD36A);
        graphics.drawCenteredString(font, "• Drei-Schwerter-Stil-Kit",
                center, top + 96, 0xFFE7EDF5);
        graphics.drawCenteredString(font, "• Wado Ichimonji",
                center, top + 111, 0xFFE7EDF5);
        graphics.drawCenteredString(font, "• Sandai Kitetsu",
                center, top + 126, 0xFFE7EDF5);
        graphics.drawCenteredString(font, "• Yubashiri",
                center, top + 141, 0xFFE7EDF5);
        graphics.drawCenteredString(font, "• Teufelsfrucht-Kiste",
                center, top + 156, 0xFFE7EDF5);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
