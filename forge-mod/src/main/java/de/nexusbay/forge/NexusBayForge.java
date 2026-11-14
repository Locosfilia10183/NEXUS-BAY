package de.nexusbay.forge;

import de.nexusbay.forge.registry.ModEntities;
import de.nexusbay.forge.registry.ModItems;
import de.nexusbay.forge.registry.ModWorldgen;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NexusBayForge.MOD_ID)
public final class NexusBayForge {
    public static final String MOD_ID = "nexusbay";

    public NexusBayForge() {
        ModEntities.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModItems.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModWorldgen.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
