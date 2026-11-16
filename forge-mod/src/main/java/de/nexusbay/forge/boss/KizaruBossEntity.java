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

    private int lightParryCooldown = 0;

    @Override
    public void tick() {
        super.tick();
        if (lightParryCooldown > 0) lightParryCooldown--;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        boolean directMelee = attacker != null && source.getDirectEntity() == attacker
                && attacker != this && !(attacker instanceof IslandBossEntity);
        if (!level().isClientSide && directMelee && lightParryCooldown <= 0
                && getHealth() / Math.max(1.0F, getMaxHealth()) < 0.90F
                && random.nextFloat() < 0.28F) {
            lightParryCooldown = 70;
            triggerAnim("combat", "sword_slash");
            boolean survived = super.hurt(source, Math.max(1.0F, amount * 0.18F));
            attacker.hurt(damageSources().mobAttack(this), 38.0F);
            Vec3 away = attacker.position().subtract(position()).normalize();
            attacker.knockback(1.15D, -away.x, -away.z);
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                Vec3 center = position().add(0.0D, getBbHeight() * 0.65D, 0.0D);
                server.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z,
                        24, 0.55D, 0.7D, 0.55D, 0.035D);
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z,
                        18, 0.45D, 0.55D, 0.45D, 0.06D);
                Vec3 clash = attacker.getBoundingBox().getCenter();
                server.sendParticles(ParticleTypes.FLASH, clash.x, clash.y, clash.z, 1, 0, 0, 0, 0);
            }
            level().playSound(null, blockPosition(), SoundEvents.SHIELD_BLOCK,
                    SoundSource.HOSTILE, 1.2F, 1.75F);
            return survived;
        }
        return super.hurt(source, amount);
    }

    @Override protected void performSpecial(LivingEntity target) {
        int phase = phase();
        if (phase == 1) {
            triggerAnim("combat", "light_speed");
            // Yata no Kagami: three rapid light-speed passes leave afterimages around the target.
            Vec3 direction = target.position().subtract(position()).normalize();
            if (direction.lengthSqr() < 0.001D) direction = new Vec3(0.0D, 0.0D, 1.0D);
            Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
            Vec3 center = target.position();
            Vec3[] passes = new Vec3[] {
                    center.add(side.scale(3.2D)).subtract(direction.scale(1.8D)),
                    center.subtract(side.scale(3.2D)).subtract(direction.scale(1.8D)),
                    center.subtract(direction.scale(2.4D))
            };
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                Vec3 origin = position();
                for (int i = 1; i <= 18; i++) {
                    Vec3 point = origin.lerp(passes[0], i / 18.0D);
                    server.sendParticles(ParticleTypes.END_ROD, point.x, point.y + 1.0D, point.z, 6, 0.1D, 0.12D, 0.1D, 0.01D);
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y + 1.15D, point.z, 3, 0.15D, 0.18D, 0.15D, 0.02D);
                }
                for (Vec3 pass : passes) {
                    server.sendParticles(ParticleTypes.FIREWORK, pass.x, pass.y + 1.0D, pass.z, 12, 0.35D, 0.5D, 0.35D, 0.02D);
                    server.sendParticles(ParticleTypes.END_ROD, pass.x, pass.y + 1.0D, pass.z, 18, 0.25D, 0.4D, 0.25D, 0.02D);
                }
            }
            for (Vec3 pass : passes) {
                teleportTo(pass.x, pass.y, pass.z);
                target.hurt(damageSources().mobAttack(this), 48.0F);
                target.knockback(0.45D, getX() - target.getX(), getZ() - target.getZ());
            }
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1.0D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 1.0D, getZ(), 28, 0.45D, 0.5D, 0.45D, 0.08D);
            }
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
            triggerAnim("combat", "ultimate");
            // Yasakani no Magatama: a radial barrage of light projectiles around the marked target.
            Vec3 center = target.position();
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                // Three expanding rings announce Kizaru's final light detonation.
                for (int ring = 1; ring <= 3; ring++) {
                    double radius = ring * 4.5D;
                    for (int i = 0; i < 40; i++) {
                        double angle = Math.PI * 2.0D * i / 40.0D;
                        double x = center.x + Math.cos(angle) * radius;
                        double z = center.z + Math.sin(angle) * radius;
                        server.sendParticles(ParticleTypes.END_ROD, x, center.y + 0.15D * ring, z,
                                3, 0.08D, 0.12D, 0.08D, 0.015D);
                        if (i % 2 == 0) {
                            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, center.y + 0.15D * ring, z,
                                    2, 0.06D, 0.1D, 0.06D, 0.025D);
                        }
                    }
                }
                // A towering column marks the center of the explosion.
                for (int height = 0; height <= 10; height++) {
                    double y = center.y + height * 0.8D;
                    server.sendParticles(ParticleTypes.END_ROD, center.x, y, center.z,
                            10, 0.45D, 0.18D, 0.45D, 0.015D);
                    server.sendParticles(ParticleTypes.FIREWORK, center.x, y, center.z,
                            4, 0.3D, 0.15D, 0.3D, 0.0D);
                }
                server.sendParticles(ParticleTypes.FLASH, center.x, center.y + 2.0D, center.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 1.0D, center.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            AABB blast = new AABB(center.x - 14, center.y - 5, center.z - 14, center.x + 14, center.y + 11, center.z + 14);
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, blast,
                    entity -> entity != this && !(entity instanceof IslandBossEntity) && entity.isAlive())) {
                double horizontalDistance = victim.position().subtract(center).horizontalDistance();
                if (horizontalDistance <= 14.0D) {
                    victim.hurt(damageSources().mobAttack(this), 235.0F);
                    Vec3 push = victim.position().subtract(center).normalize();
                    victim.knockback(1.8D, -push.x, -push.z);
                    victim.push(0.0D, 0.55D, 0.0D);
                }
            }
        }
        level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, phase == 3 ? 1.6F : 1.25F);
    }

    private int phase() {
        float ratio = getHealth() / Math.max(1.0F, getMaxHealth());
        return ratio <= 0.30F ? 3 : ratio <= 0.65F ? 2 : 1;
    }
}
