package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.network.CombatNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Applies guard mitigation only to attacks coming from in front of the player. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class CombatGuardEvents {
    private CombatGuardEvents() {}

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !CombatNetwork.isBlocking(player)) return;
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || attacker == player) return;

        Vec3 towardAttacker = attacker.position().subtract(player.position());
        Vec3 facing = player.getLookAngle();
        Vec3 horizontalAttack = new Vec3(towardAttacker.x, 0.0D, towardAttacker.z);
        Vec3 horizontalFacing = new Vec3(facing.x, 0.0D, facing.z);
        if (horizontalAttack.lengthSqr() < 1.0E-4D || horizontalFacing.lengthSqr() < 1.0E-4D) return;
        if (horizontalFacing.normalize().dot(horizontalAttack.normalize()) < 0.15D) return;

        event.setAmount(event.getAmount() * 0.45F);
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.SHIELD_BLOCK,
                player.getSoundSource(), 0.45F, 0.9F);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().putBoolean("nexusbay_blocking", false);
        }
    }
}
