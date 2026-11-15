package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Right-click techniques for rare NEXUS-BAY signature weapons. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class SignatureWeaponEvents {
    private static final String COOLDOWN = "nexusbay_kanabo_cooldown";
    private SignatureWeaponEvents() {}

    @SubscribeEvent
    public static void onWeaponUse(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !event.getItemStack().is(ModItems.KAIDO_KANABO.get())
                || !(player.level() instanceof ServerLevel level)) return;
        event.setCanceled(true);
        long now = player.level().getGameTime();
        long readyAt = player.getPersistentData().getLong(COOLDOWN);
        if (now < readyAt) {
            player.displayClientMessage(Component.literal("§cKanabo-Schlag lädt noch: "
                    + Math.max(1, (readyAt - now + 19) / 20) + " s"), true);
            return;
        }
        player.getPersistentData().putLong(COOLDOWN, now + 12 * 20L);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 center = player.position().add(look.scale(3.0D));
        AABB area = new AABB(center, center).inflate(3.2D, 2.0D, 3.2D);
        int hits = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive() && entity.isPickable())) {
            Vec3 toward = target.position().subtract(player.position()).normalize();
            if (look.dot(toward) < 0.15D) continue;
            target.hurt(player.damageSources().playerAttack(player), 16.0F);
            target.knockback(1.15D, player.getX() - target.getX(), player.getZ() - target.getZ());
            hits++;
        }
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5D, center.z,
                3, 1.1D, 0.4D, 1.1D, 0.04D);
        level.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z,
                35, 1.8D, 0.8D, 1.8D, 0.2D);
        player.getCooldowns().addCooldown(ModItems.KAIDO_KANABO.get(), 12 * 20);
        player.displayClientMessage(Component.literal("§6§lKANABO-SCHOCKWELLE §eTreffer: " + hits), true);
    }
}
