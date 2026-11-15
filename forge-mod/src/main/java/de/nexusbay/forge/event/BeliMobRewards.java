package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.BeliLedger;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Awards Beli to the player who defeats hostile mobs.
 * Rewards scale gently with max health and include a small random variation.
 */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class BeliMobRewards {
    private BeliMobRewards() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity defeated = event.getEntity();
        if (!(defeated instanceof Monster)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        double health = defeated.getMaxHealth();
        long base = Math.max(3L, Math.min(250L, Math.round(health * 0.35D)));
        long variation = player.getRandom().nextInt((int) Math.max(2L, base / 2L + 1L));
        long reward = base + variation;

        long balance = BeliLedger.get(player.serverLevel()).deposit(player.getUUID(), reward);
        player.displayClientMessage(Component.literal("+" + reward + " Beli  |  Kontostand: " + balance), true);
    }
}
