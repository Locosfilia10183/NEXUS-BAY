package de.nexusbay.forge;

import de.nexusbay.forge.registry.ModEntities;
import de.nexusbay.forge.registry.ModItems;
import de.nexusbay.forge.network.FruitNetwork;
import de.nexusbay.forge.network.QuestNetwork;
import de.nexusbay.forge.network.MerchantNetwork;
import de.nexusbay.forge.network.CharacterNetwork;
import de.nexusbay.forge.network.CombatNetwork;
import de.nexusbay.forge.network.CombatStyleNetwork;
import de.nexusbay.forge.network.StarterKitNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NexusBayForge.MOD_ID)
public final class NexusBayForge {
    public static final String MOD_ID = "nexusbay";

    public NexusBayForge() {
        ModEntities.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModItems.register(FMLJavaModLoadingContext.get().getModEventBus());
        FruitNetwork.register();
        QuestNetwork.register();
        MerchantNetwork.register();
        CharacterNetwork.register();
        CombatNetwork.register();
        CombatStyleNetwork.register();
        StarterKitNetwork.register();
    }
}
