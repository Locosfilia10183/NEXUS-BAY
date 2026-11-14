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

    public static final RegistryObject<Item> WADO_ICHIMONJI = ITEMS.register("wado_ichimonji",
            () -> new SwordItem(Tiers.DIAMOND, 4, -2.2F, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENMA = ITEMS.register("enma",
            () -> new SwordItem(Tiers.NETHERITE, 5, -2.3F, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SANDAI_KITETSU = ITEMS.register("sandai_kitetsu",
            () -> new SwordItem(Tiers.DIAMOND, 4, -2.4F, new Item.Properties().stacksTo(1)));

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
