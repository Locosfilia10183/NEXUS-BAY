package de.nexusbay.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.nexusbay.forge.NexusBayForge;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class IslandMapKeybind {
    public static final KeyMapping OPEN_MAP = new KeyMapping(
            "key.nexusbay.open_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M,
            "key.categories.nexusbay"
    );

    private IslandMapKeybind() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MAP);
    }
}
