package de.nexusbay.forge.client;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.KAIDO.get(), KaidoRenderer::new);
        event.registerEntityRenderer(ModEntities.CROCODILE.get(), CrocodileRenderer::new);
        event.registerEntityRenderer(ModEntities.ENEL.get(), EnelRenderer::new);
        event.registerEntityRenderer(ModEntities.ROB_LUCCI.get(), RobLucciRenderer::new);
    }
}
