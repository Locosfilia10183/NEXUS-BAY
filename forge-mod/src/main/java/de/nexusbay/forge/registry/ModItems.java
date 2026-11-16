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

    public static final RegistryObject<Item> MARINE_INSIGNIA = ITEMS.register("marine_insignia", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SKYPIEA_DIAL = ITEMS.register("skypiea_dial", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FISHMAN_PEARL = ITEMS.register("fishman_pearl", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WANO_STEEL = ITEMS.register("wano_steel", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ALCHEMY_REAGENT = ITEMS.register("alchemy_reagent", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BERRY_TONIC = ITEMS.register("berry_tonic", () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationMod(0.6F).build())));

    public static final RegistryObject<Item> LIGHT_SABER = ITEMS.register("light_saber",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.NETHERITE, 6, -2.1F, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SEASTONE_SABER = ITEMS.register("seastone_saber",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.DIAMOND, 4, -2.3F, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ONI_CLEAVER = ITEMS.register("oni_cleaver",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.NETHERITE, 5, -2.7F, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WANO_SAMURAI_HELMET = ITEMS.register("wano_samurai_helmet",
            () -> new net.minecraft.world.item.ArmorItem(net.minecraft.world.item.ArmorMaterials.DIAMOND, net.minecraft.world.item.ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WANO_SAMURAI_CHESTPLATE = ITEMS.register("wano_samurai_chestplate",
            () -> new net.minecraft.world.item.ArmorItem(net.minecraft.world.item.ArmorMaterials.DIAMOND, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WANO_SAMURAI_LEGGINGS = ITEMS.register("wano_samurai_leggings",
            () -> new net.minecraft.world.item.ArmorItem(net.minecraft.world.item.ArmorMaterials.DIAMOND, net.minecraft.world.item.ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WANO_SAMURAI_BOOTS = ITEMS.register("wano_samurai_boots",
            () -> new net.minecraft.world.item.ArmorItem(net.minecraft.world.item.ArmorMaterials.DIAMOND, net.minecraft.world.item.ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> KAIDO_KANABO = ITEMS.register("kaido_kanabo",
            () -> new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.NETHERITE, 7, -3.2F, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AZURE_DRAGON_CORE = ITEMS.register("azure_dragon_core",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CROCODILE_HOOK = ITEMS.register("crocodile_hook",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SAND_SAND_CORE = ITEMS.register("sand_sand_core",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENEL_GOLDEN_STAFF = ITEMS.register("enel_golden_staff",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GORO_GORO_CORE = ITEMS.register("goro_goro_core",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CP0_MASK = ITEMS.register("cp0_mask",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ROKUSHIKI_MANUAL = ITEMS.register("rokushiki_manual",
            () -> new Item(new Item.Properties().stacksTo(1)));

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
