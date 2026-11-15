package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Right-click techniques for rare NEXUS-BAY signature weapons. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class SignatureWeaponEvents {
    private SignatureWeaponEvents() {}

    @SubscribeEvent
    public static void onWeaponUse(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) return;
        var stack = event.getItemStack();
        if (stack.is(ModItems.KAIDO_KANABO.get())) {
            event.setCanceled(true);
            if (!ready(player, "nexusbay_kanabo_cooldown", 12, "Kanabo")) return;
            shockwave(player, level, 16.0F, 3.2D, ParticleTypes.CRIT);
            player.getCooldowns().addCooldown(ModItems.KAIDO_KANABO.get(), 240);
        } else if (stack.is(ModItems.CROCODILE_HOOK.get())) {
            event.setCanceled(true);
            if (!ready(player, "nexusbay_hook_cooldown", 8, "Sandhaken")) return;
            Vec3 look = player.getLookAngle().normalize();
            AABB scan = player.getBoundingBox().inflate(16.0D, 7.0D, 16.0D);
            LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, scan,
                    entity -> entity != player && entity.isAlive()
                            && look.dot(entity.position().subtract(player.position()).normalize()) > 0.72D)
                    .stream().min(java.util.Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
            if (target != null && player.distanceToSqr(target) <= 18.0D * 18.0D) {
                target.hurt(player.damageSources().playerAttack(player), 24.0F);
                Vec3 pull = player.position().subtract(target.position()).normalize().scale(1.8D);
                target.push(pull.x, 0.55D, pull.z);
                target.hurtMarked = true;
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D,
                        target.getZ(), 24, 0.6D, 0.8D, 0.6D, 0.12D);
                player.displayClientMessage(Component.literal("§6§lSANDHOOK §eGegner herangezogen!"), true);
            } else {
                player.displayClientMessage(Component.literal("§7Kein Gegner in Reichweite."), true);
            }
            player.getCooldowns().addCooldown(ModItems.CROCODILE_HOOK.get(), 160);
        } else if (stack.is(ModItems.ENEL_GOLDEN_STAFF.get())) {
            event.setCanceled(true);
            if (!ready(player, "nexusbay_staff_cooldown", 10, "Donnerstab")) return;
            Vec3 look = player.getLookAngle().normalize();
            AABB scan = player.getBoundingBox().inflate(22.0D, 10.0D, 22.0D);
            var targets = level.getEntitiesOfClass(LivingEntity.class, scan,
                    entity -> entity != player && entity.isAlive()
                            && look.dot(entity.position().subtract(player.position()).normalize()) > 0.65D)
                    .stream().filter(entity -> player.distanceToSqr(entity) <= 24.0D * 24.0D)
                    .sorted(java.util.Comparator.comparingDouble(player::distanceToSqr)).limit(4).toList();
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), 20.0F);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(target.getX(), target.getY(), target.getZ());
                    bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D,
                        target.getZ(), 18, 0.5D, 0.8D, 0.5D, 0.15D);
            }
            level.sendParticles(ParticleTypes.FLASH, player.getX() + look.x * 3.0D,
                    player.getY() + 1.2D + look.y * 3.0D, player.getZ() + look.z * 3.0D,
                    2, 0.3D, 0.3D, 0.3D, 0.0D);
            player.displayClientMessage(Component.literal("§e§lEL THOR §fBlitztreffer: " + targets.size()), true);
            player.getCooldowns().addCooldown(ModItems.ENEL_GOLDEN_STAFF.get(), 200);
        }
    }

    private static boolean ready(ServerPlayer player, String key, int seconds, String weapon) {
        long now = player.level().getGameTime();
        long readyAt = player.getPersistentData().getLong(key);
        if (now < readyAt) {
            player.displayClientMessage(Component.literal("§c" + weapon + " lädt noch: "
                    + Math.max(1, (readyAt - now + 19) / 20) + " s"), true);
            return false;
        }
        player.getPersistentData().putLong(key, now + seconds * 20L);
        return true;
    }

    private static void shockwave(ServerPlayer player, ServerLevel level, float damage,
                                  double radius, net.minecraft.core.particles.ParticleOptions particle) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 center = player.position().add(look.scale(3.0D));
        AABB area = new AABB(center, center).inflate(radius, 2.0D, radius);
        int hits = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive() && entity.isPickable())) {
            Vec3 toward = target.position().subtract(player.position()).normalize();
            if (look.dot(toward) < 0.15D) continue;
            target.hurt(player.damageSources().playerAttack(player), damage);
            target.knockback(1.15D, player.getX() - target.getX(), player.getZ() - target.getZ());
            hits++;
        }
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5D, center.z,
                3, 1.1D, 0.4D, 1.1D, 0.04D);
        level.sendParticles(particle, center.x, center.y, center.z, 35,
                1.8D, 0.8D, 1.8D, 0.2D);
        player.displayClientMessage(Component.literal("§6§lKANABO-SCHOCKWELLE §eTreffer: " + hits), true);
    }
}
