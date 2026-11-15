package de.nexusbay.forge.registry;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.item.DevilFruitCaseItem;
import de.nexusbay.forge.item.DevilFruitItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NexusBayForge.MOD_ID);

    public static final RegistryObject<Item> DEVIL_FRUIT_CASE =
            ITEMS.register("devil_fruit_case", () -> new DevilFruitCaseItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> THREE_SWORD_STYLE_KIT =
            ITEMS.register("three_sword_style_kit", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WADO_ICHIMONJI =
            ITEMS.register("wado_ichimonji", () -> new Item(new Item.Properties().stacksTo(1).durability(1800)));
    public static final RegistryObject<Item> SANDai_KITETSU =
            ITEMS.register("sandai_kitetsu", () -> new Item(new Item.Properties().stacksTo(1).durability(1800)));
    public static final RegistryObject<Item> YUBASHIRI =
            ITEMS.register("yubashiri", () -> new Item(new Item.Properties().stacksTo(1).durability(1800)));

    public static final Map<String, RegistryObject<Item>> FRUIT_ITEMS = createFruitItems();

    private static Map<String, RegistryObject<Item>> createFruitItems() {
        Map<String, RegistryObject<Item>> items = new LinkedHashMap<>();
        for (var fruit : de.nexusbay.forge.progression.DevilFruitCaseTable.all()) {
            String itemId = fruit.id() + "_fruit";
            items.put(fruit.id(), ITEMS.register(itemId,
                    () -> new DevilFruitItem(fruit.id(), fruit.displayName())));
        }
        return Map.copyOf(items);
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private ModItems() {}
}
