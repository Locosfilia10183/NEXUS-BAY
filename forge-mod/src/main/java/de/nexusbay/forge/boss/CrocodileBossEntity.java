package de.nexusbay.forge.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class CrocodileBossEntity extends IslandBossEntity {
    public CrocodileBossEntity(EntityType<? extends CrocodileBossEntity> type, Level level) {
        super(type, level, "§6CROCODILE §7· WÜSTENKÖNIG · LEVEL 120", 180000.0F, 105.0D, 0.31D, BossEvent.BossBarColor.YELLOW);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, 180000.0D).add(Attributes.ATTACK_DAMAGE, 105.0D);
    }
    @Override protected String animationKey() { return "crocodile"; }

    @Override protected void performSpecial(LivingEntity target) {
        triggerAnim("combat", "special");
        int phase = phase();
        if (phase == 3) {
            // Grounded sandstorm: a warning ring, then a wide damaging burst.
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5D, getZ(), 130, 7.0D, 1.0D, 7.0D, 0.12D);
                server.sendParticles(ParticleTypes.ASH, getX(), getY() + 1.0D, getZ(), 75, 5.0D, 1.5D, 5.0D, 0.04D);
            }
            damageArea(10.0D, 135.0F, true);
            level().explode(this, getX(), getY(), getZ(), 3.2F, Level.ExplosionInteraction.NONE);
            return;
        }

        Vec3 forward = target.position().subtract(position()).normalize();
        if (phase == 1) {
            // Desert Spada: a narrow sand blade travels in a straight line.
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                for (int step = 1; step <= 9; step++) {
                    Vec3 point = position().add(forward.scale(step * 2.0D));
                    server.sendParticles(ParticleTypes.SWEEP_ATTACK, point.x, point.y + 1.0D, point.z, 5, 0.6D, 0.7D, 0.6D, 0.0D);
                    server.sendParticles(ParticleTypes.CLOUD, point.x, point.y + 0.4D, point.z, 9, 0.55D, 0.35D, 0.55D, 0.03D);
                }
            }
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().expandTowards(forward.scale(18.0D)).inflate(2.0D, 1.5D, 2.0D),
                    entity -> entity != this && !(entity instanceof IslandBossEntity) && entity.isAlive())) {
                Vec3 offset = victim.position().subtract(position());
                if (offset.length() <= 18.0D && offset.normalize().dot(forward) > 0.82D) {
                    victim.hurt(damageSources().mobAttack(this), 85.0F);
                    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                }
            }
        } else {
            // Ground Secco: Crocodile closes the gap and drains the target's movement.
            if (distanceToSqr(target) > 8.0D * 8.0D) {
                Vec3 point = target.position().subtract(forward.scale(2.0D));
                teleportTo(point.x, point.y, point.z);
            }
            target.hurt(damageSources().mobAttack(this), 115.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.8D, target.getZ(), 55, 2.2D, 1.0D, 2.2D, 0.08D);
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1.0D, getZ(), 18, 1.2D, 0.8D, 1.2D, 0.0D);
            }
        }
        level().playSound(null, blockPosition(), SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 2.0F, phase == 1 ? 0.7F : 0.55F);
    }

    private int phase() {
        float ratio = getHealth() / Math.max(1.0F, getMaxHealth());
        return ratio <= 0.30F ? 3 : ratio <= 0.65F ? 2 : 1;
    }
}
