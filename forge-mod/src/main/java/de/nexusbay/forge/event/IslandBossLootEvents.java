package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.CrocodileBossEntity;
import de.nexusbay.forge.boss.EnelBossEntity;
import de.nexusbay.forge.boss.IslandBossEntity;
import de.nexusbay.forge.boss.RobLucciBossEntity;
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

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandBossLootEvents {
    private IslandBossLootEvents() {}

    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof IslandBossEntity boss)) return;
        if (!(boss.level() instanceof net.minecraft.server.level.ServerLevel level)) return;

        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player ? player : null;
        long killerBeli = 0L;
        if (boss instanceof CrocodileBossEntity) {
            killerBeli = 125000L;
            if (killer != null) rewardKiller(killer, level, killerBeli, "CROCODILE BESIEGT",
                    Items.GOLD_INGOT, 24, ModItems.SAND_SAND_CORE.get(), ModItems.CROCODILE_HOOK.get());
        } else if (boss instanceof EnelBossEntity) {
            killerBeli = 175000L;
            if (killer != null) rewardKiller(killer, level, killerBeli, "ENEL BESIEGT",
                    Items.GOLD_INGOT, 32, ModItems.GORO_GORO_CORE.get(), ModItems.ENEL_GOLDEN_STAFF.get());
        } else if (boss instanceof RobLucciBossEntity) {
            killerBeli = 220000L;
            if (killer != null) rewardKiller(killer, level, killerBeli, "ROB LUCCI BESIEGT",
                    Items.GOLD_INGOT, 36, ModItems.CP0_MASK.get(), ModItems.ROKUSHIKI_MANUAL.get());
        } else {
            return;
        }

        // Contributors who dealt at least 1% of the boss's maximum health receive a participation reward.
        for (Map.Entry<UUID, Float> entry : boss.getPlayerDamageContributions().entrySet()) {
            if (entry.getValue() < boss.getMaxHealth() * 0.01F) continue;
            if (killer != null && killer.getUUID().equals(entry.getKey())) continue;
            net.minecraft.world.entity.player.Player onlinePlayer = level.getPlayerByUUID(entry.getKey());
            if (!(onlinePlayer instanceof ServerPlayer participant)) continue;
            long share = killerBeli / 4L;
            BeliLedger.get(level).deposit(participant.getUUID(), share);
            participant.displayClientMessage(Component.literal("§aBOSS-TEILNAHME §7· §e+" + share + " Beli"), false);
            drop(level, participant, new ItemStack(Items.GOLD_INGOT, 6));
        }
    }

    private static void rewardKiller(ServerPlayer player, net.minecraft.server.level.ServerLevel level, long beli,
                                     String title, net.minecraft.world.item.Item gold, int goldCount,
                                     net.minecraft.world.item.Item rare, net.minecraft.world.item.Item material) {
        BeliLedger.get(level).deposit(player.getUUID(), beli);
        player.displayClientMessage(Component.literal("§6§l" + title + " §e+" + beli + " Beli"), false);
        drop(level, player, new ItemStack(gold, goldCount));
        drop(level, player, new ItemStack(rare, 1));
        drop(level, player, new ItemStack(material, 1));
    }

    private static void drop(net.minecraft.server.level.ServerLevel level, ServerPlayer player, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, player.getX(), player.getY() + 0.5D, player.getZ(), stack);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }
}
