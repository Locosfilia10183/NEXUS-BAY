package de.nexusbay.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.network.CombatStyleNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** C cycles the selected combat style. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CombatStyleKeybind {
    public static final KeyMapping CYCLE_STYLE = new KeyMapping(
            "key.nexusbay.cycle_combat_style", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C, "key.categories.nexusbay");

    private CombatStyleKeybind() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(CYCLE_STYLE);
    }

    @Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ClientTickHandler {
        private ClientTickHandler() {}
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            while (CYCLE_STYLE.consumeClick()) {
                if (net.minecraft.client.Minecraft.getInstance().player != null) {
                    CombatStyleNetwork.cycleStyle();
                }
            }
        }
    }
}
