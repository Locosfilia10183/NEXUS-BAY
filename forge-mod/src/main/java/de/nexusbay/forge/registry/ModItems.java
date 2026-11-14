package de.nexusbay.forge.registry;

import de.nexusbay.forge.NexusBayForge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NexusBayForge.MOD_ID);

    /**
     * One weapon item representing Zoro's complete three-sword Santoryu set:
     * Wado Ichimonji, Enma and Sandai Kitetsu.
     */
    public static final RegistryObject<Item> SANTORYU = ITEMS.register("santoryu",
            () -> new SwordItem(Tiers.NETHERITE, 8, -2.4F, new Item.Properties().stacksTo(1)));

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
