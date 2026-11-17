package de.nexusbay.forge.client;

import de.nexusbay.forge.NexusBayForge;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID, value = Dist.CLIENT)
public final class IslandMapClientEvents {
    private IslandMapClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        while (IslandMapKeybind.OPEN_MAP.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new IslandMapScreen());
            } else if (minecraft.screen instanceof IslandMapScreen) {
                minecraft.setScreen(null);
            }
        }
    }

    @SubscribeEvent
    public static void renderMinimap(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) return;
        IslandMapScreen.renderMinimap(event.getGuiGraphics(),
                event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(),
                minecraft.player.getBlockX(), minecraft.player.getBlockZ(), playerLevel(minecraft));
    }

    static int playerLevel(Minecraft minecraft) {
        if (minecraft.player == null) return 1;
        var scoreboard = minecraft.player.getScoreboard();
        for (String name : new String[]{"level", "Level", "nexus_level"}) {
            var objective = scoreboard.getObjective(name);
            if (objective != null) {
                var score = scoreboard.getOrCreatePlayerScore(minecraft.player.getScoreboardName(), objective);
                return Math.max(1, score.getScore());
            }
        }
        return 1;
    }
}
