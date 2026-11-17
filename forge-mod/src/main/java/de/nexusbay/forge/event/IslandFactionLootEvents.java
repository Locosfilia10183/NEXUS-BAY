package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Adds collectible, faction-themed trophies to island enemy drops. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandFactionLootEvents {
    private IslandFactionLootEvents() {}

    @SubscribeEvent
    public static void onFactionDrop(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Monster defeated)) return;
        String faction = defeated.getPersistentData().getString("nexusbay_faction");
        if (faction.isBlank()) return;
        String rank = defeated.getPersistentData().getString("nexusbay_elite_rank");
        float chance = rank.equals("Katastrophe") ? 1.0F
                : rank.equals("Kommandant") ? 0.95F
                : defeated.getPersistentData().getBoolean("nexusbay_elite") ? 0.85F
                : (faction.equals("onigashima_beasts") ? 0.45F : 0.28F);
        if (defeated.getRandom().nextFloat() >= chance) return;

        ItemStack trophy;
        String name;
        switch (faction) {
            case "foosha_bandits", "reverse_raiders" -> {
                trophy = new ItemStack(Items.LEATHER); name = "Banditen-Trophäe";
            }
            case "shells_marine", "marineford_raiders", "enies_cp9" -> {
                trophy = new ItemStack(Items.IRON_NUGGET); name = "Marine-Abzeichen";
            }
            case "arlong_fishmen", "fishman_raiders" -> {
                trophy = new ItemStack(Items.PRISMARINE_SHARD); name = "Fischmenschen-Schuppe";
            }
            case "syrup_blackcat", "orange_pirates", "baratie_raiders", "loguetown_outlaws",
                 "jaya_bellamy", "sabaody_slavers" -> {
                trophy = new ItemStack(Items.GOLD_NUGGET); name = "Piraten-Kopfgeldmarke";
            }
            case "thriller_zombies", "impel_prisoners" -> {
                trophy = new ItemStack(Items.ROTTEN_FLESH); name = "Schatten-Trophäe";
            }
            case "alabasta_rebels", "whiskey_hunters" -> {
                trophy = new ItemStack(Items.GOLD_INGOT); name = "Wüsten-Kopfgeld";
            }
            case "skypiea_enforcers" -> {
                trophy = new ItemStack(Items.GLOWSTONE_DUST); name = "Himmelskristall";
            }
            case "punk_hazards" -> {
                trophy = new ItemStack(Items.SLIME_BALL); name = "Laborprobe";
            }
            case "wholecake_homies", "dressrosa_dons" -> {
                trophy = new ItemStack(Items.AMETHYST_SHARD); name = "Unterwelt-Juwel";
            }
            case "wano_beasts", "onigashima_beasts" -> {
                trophy = new ItemStack(Items.BLAZE_POWDER); name = "Bestien-Piraten-Marke";
            }
            default -> {
                trophy = new ItemStack(Items.EMERALD); name = "Fraktions-Beute";
            }
        }
        trophy.setHoverName(Component.literal(name));
        if ((faction.equals("onigashima_beasts") || rank.equals("Katastrophe")) && defeated.getRandom().nextFloat() < 0.35F) {
            trophy.setCount(2);
        }
        event.getDrops().add(new ItemEntity(defeated.level(), defeated.getX(), defeated.getY(),
                defeated.getZ(), trophy));

        // Elite enemies can drop rare equipment in addition to their faction trophy.
        if (!rank.isBlank() && defeated.getRandom().nextFloat() < (rank.equals("Katastrophe") ? 0.35F : 0.08F)) {
            net.minecraft.world.item.Item rare = null;
            if (faction.equals("onigashima_beasts") || faction.equals("wano_beasts")) {
                rare = rank.equals("Katastrophe")
                        ? de.nexusbay.forge.registry.ModItems.ONI_CLEAVER.get()
                        : de.nexusbay.forge.registry.ModItems.WANO_SAMURAI_HELMET.get();
            } else if (faction.equals("shells_marine") || faction.equals("marineford_raiders")
                    || faction.equals("enies_cp9")) {
                rare = de.nexusbay.forge.registry.ModItems.SEASTONE_SABER.get();
            }
            if (rare != null) event.getDrops().add(new ItemEntity(defeated.level(), defeated.getX(),
                    defeated.getY(), defeated.getZ(), new ItemStack(rare)));
        }
    }
}
