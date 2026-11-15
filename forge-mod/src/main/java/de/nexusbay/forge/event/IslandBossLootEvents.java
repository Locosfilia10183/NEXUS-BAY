package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.CrocodileBossEntity;
import de.nexusbay.forge.boss.EnelBossEntity;
import de.nexusbay.forge.progression.BeliLedger;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandBossLootEvents {
    private IslandBossLootEvents() {}
    @SubscribeEvent public static void onBossDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        if (event.getEntity() instanceof CrocodileBossEntity) {
            reward(player, level, 125000L, "CROCODILE BESIEGT", Items.GOLD_INGOT, 24,
                    ModItems.SAND_SAND_CORE.get(), 1, ModItems.CROCODILE_HOOK.get(), 1);
        } else if (event.getEntity() instanceof EnelBossEntity) {
            reward(player, level, 175000L, "ENEL BESIEGT", Items.GOLD_INGOT, 32,
                    ModItems.GORO_GORO_CORE.get(), 1, ModItems.ENEL_GOLDEN_STAFF.get(), 1);
        }
    }
    private static void reward(ServerPlayer player, net.minecraft.server.level.ServerLevel level, long beli,
                               String title, net.minecraft.world.item.Item gold, int goldCount,
                               net.minecraft.world.item.Item rare, int rareCount,
                               net.minecraft.world.item.Item material, int materialCount) {
        BeliLedger.get(level).deposit(player.getUUID(), beli);
        player.displayClientMessage(Component.literal("§6§l" + title + " §e+" + beli + " Beli"), false);
        drop(level, player, new ItemStack(gold, goldCount));
        drop(level, player, new ItemStack(rare, rareCount));
        drop(level, player, new ItemStack(material, materialCount));
    }
    private static void drop(net.minecraft.server.level.ServerLevel level, ServerPlayer player, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, player.getX(), player.getY() + 0.5D, player.getZ(), stack);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }
}
