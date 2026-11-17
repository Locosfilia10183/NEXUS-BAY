package de.nexusbay.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.network.CombatNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Client input for the server-authoritative combat dodge. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CombatDodgeKeybind {
    public static final KeyMapping DODGE = new KeyMapping(
            "key.nexusbay.dodge", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R,
            "key.categories.nexusbay");

    private CombatDodgeKeybind() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(DODGE);
    }

    @Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ClientTickHandler {
        private ClientTickHandler() {}

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            while (DODGE.consumeClick()) {
                CombatNetwork.requestDodge();
            }
        }
    }
}
