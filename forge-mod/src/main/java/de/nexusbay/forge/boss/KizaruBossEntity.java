package de.nexusbay.forge.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class KizaruBossEntity extends IslandBossEntity {
    public KizaruBossEntity(EntityType<? extends KizaruBossEntity> type, Level level) {
        super(type, level, "§eADMIRAL KIZARU §7· LICHTMENSCH · LEVEL 180",
                380000.0F, 145.0D, 0.36D, BossEvent.BossBarColor.YELLOW);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, 380000.0D)
                .add(Attributes.ATTACK_DAMAGE, 145.0D).add(Attributes.MOVEMENT_SPEED, 0.36D);
    }

    @Override protected String animationKey() { return "kizaru"; }

    @Override protected void performSpecial(LivingEntity target) {
        int phase = phase();
        if (phase == 1) {
            triggerAnim("combat", "light_speed");
            // Yata no Kagami: a visible light path, then Kizaru reappears beside the target.
            Vec3 direction = target.position().subtract(position()).normalize();
            Vec3 destination = target.position().subtract(direction.scale(2.4D));
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                for (int i = 1; i <= 14; i++) {
                    Vec3 point = position().lerp(destination, i / 14.0D);
                    server.sendParticles(ParticleTypes.END_ROD, point.x, point.y + 1.0D, point.z, 7, 0.12D, 0.12D, 0.12D, 0.01D);
                    server.sendParticles(ParticleTypes.FIREWORK, point.x, point.y + 1.0D, point.z, 2, 0.08D, 0.08D, 0.08D, 0.0D);
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y + 1.15D, point.z, 3, 0.18D, 0.2D, 0.18D, 0.02D);
                }
            }
            teleportTo(destination.x, destination.y, destination.z);
            target.hurt(damageSources().mobAttack(this), 125.0F);
            target.knockback(1.0D, getX() - target.getX(), getZ() - target.getZ());
        } else if (phase == 2) {
            triggerAnim("combat", "sword_slash");
            // Ama no Murakumo: a long blade of light sweeps through a broad frontal arc.
            Vec3 forward = target.position().subtract(position()).normalize();
            AABB lane = getBoundingBox().expandTowards(forward.scale(20.0D)).inflate(4.0D, 2.0D, 4.0D);
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, lane,
                    entity -> entity != this && !(entity instanceof IslandBossEntity) && entity.isAlive())) {
                Vec3 offset = victim.position().subtract(position());
                if (offset.length() <= 20.0D && offset.normalize().dot(forward) > 0.68D) {
                    victim.hurt(damageSources().mobAttack(this), 165.0F);
                    victim.knockback(1.25D, -forward.x, -forward.z);
                }
            }
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
                // Bright curved blade trail: a wide golden-white crescent in front of Kizaru.
                for (int arc = 0; arc <= 24; arc++) {
                    double angle = -1.05D + (2.10D * arc / 24.0D);
                    Vec3 point = position().add(forward.scale(7.0D + Math.cos(angle) * 5.0D))
                            .add(side.scale(Math.sin(angle) * 5.0D));
                    double y = getY() + 1.35D + Math.sin(angle) * 1.2D;
                    server.sendParticles(ParticleTypes.END_ROD, point.x, y, point.z, 5, 0.06D, 0.08D, 0.06D, 0.01D);
                    server.sendParticles(ParticleTypes.FIREWORK, point.x, y, point.z, 2, 0.04D, 0.05D, 0.04D, 0.0D);
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, y, point.z, 3, 0.09D, 0.1D, 0.09D, 0.02D);
                }
                for (int step = 1; step <= 10; step++) {
                    Vec3 point = position().add(forward.scale(step * 2.0D));
                    server.sendParticles(ParticleTypes.END_ROD, point.x, point.y + 1.0D, point.z, 12, 0.3D, 0.4D, 0.3D, 0.02D);
                    server.sendParticles(ParticleTypes.SWEEP_ATTACK, point.x, point.y + 1.0D, point.z, 3, 0.25D, 0.25D, 0.25D, 0.0D);
                }
            }
        } else {
            triggerAnim("combat", "special");
            // Yasakani no Magatama: a radial barrage of light projectiles around the marked target.
            Vec3 center = target.position();
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                for (int i = 0; i < 24; i++) {
                    double angle = Math.PI * 2.0D * i / 24.0D;
                    double x = center.x + Math.cos(angle) * 7.0D;
                    double z = center.z + Math.sin(angle) * 7.0D;
                    server.sendParticles(ParticleTypes.END_ROD, x, center.y + 1.0D, z, 5, 0.16D, 0.16D, 0.16D, 0.03D);
                    server.sendParticles(ParticleTypes.FIREWORK, x, center.y + 1.0D, z, 2, 0.08D, 0.08D, 0.08D, 0.0D);
                    // Each light bead leaves a visible trail toward the impact center.
                    for (int trail = 1; trail <= 5; trail++) {
                        double t = trail / 6.0D;
                        double tx = x + (center.x - x) * t;
                        double tz = z + (center.z - z) * t;
                        server.sendParticles(ParticleTypes.END_ROD, tx, center.y + 1.0D, tz, 2, 0.05D, 0.08D, 0.05D, 0.01D);
                    }
                }
            }
            AABB blast = new AABB(center.x - 9, center.y - 4, center.z - 9, center.x + 9, center.y + 5, center.z + 9);
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, blast,
                    entity -> entity != this && !(entity instanceof IslandBossEntity) && entity.isAlive())) {
                victim.hurt(damageSources().mobAttack(this), 185.0F);
            }
        }
        level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, phase == 3 ? 1.6F : 1.25F);
    }

    private int phase() {
        float ratio = getHealth() / Math.max(1.0F, getMaxHealth());
        return ratio <= 0.30F ? 3 : ratio <= 0.65F ? 2 : 1;
    }
}
