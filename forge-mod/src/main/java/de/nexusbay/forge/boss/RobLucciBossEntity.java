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

public class RobLucciBossEntity extends IslandBossEntity {
    public RobLucciBossEntity(EntityType<? extends RobLucciBossEntity> type, Level level) {
        super(type, level, "§cROB LUCCI §7· CP0 · LEVEL 135", 210000.0F, 118.0D, 0.39D, BossEvent.BossBarColor.RED);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, 210000.0D)
                .add(Attributes.ATTACK_DAMAGE, 118.0D).add(Attributes.MOVEMENT_SPEED, 0.39D);
    }

    @Override protected String animationKey() { return "lucci"; }

    @Override protected void performSpecial(LivingEntity target) {
        triggerAnim("combat", "special");
        int phase = phase();
        Vec3 forward = target.position().subtract(position()).normalize();
        if (phase == 1) {
            // Shigan: an explosive, extremely fast finger thrust.
            target.hurt(damageSources().mobAttack(this), 145.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 35, 1));
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 35, 0.45D, 0.7D, 0.45D, 0.2D);
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.7D, 0.6D, 0.7D, 0.0D);
            }
            level().playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.8F, 0.65F);
            return;
        }

        if (phase == 2) {
            // Rankyaku: a compressed air blade cuts a long, narrow lane.
            AABB lane = getBoundingBox().expandTowards(forward.scale(24.0D)).inflate(2.2D, 1.4D, 2.2D);
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, lane,
                    entity -> entity != this && !(entity instanceof IslandBossEntity) && entity.isAlive())) {
                Vec3 offset = victim.position().subtract(position());
                if (offset.length() <= 24.0D && offset.normalize().dot(forward) > 0.88D) {
                    victim.hurt(damageSources().mobAttack(this), 132.0F);
                    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 45, 1));
                }
            }
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                for (int step = 1; step <= 12; step++) {
                    Vec3 point = position().add(forward.scale(step * 2.0D));
                    server.sendParticles(ParticleTypes.SWEEP_ATTACK, point.x, point.y + 1.0D, point.z, 7, 0.45D, 0.5D, 0.45D, 0.0D);
                    server.sendParticles(ParticleTypes.CLOUD, point.x, point.y + 0.5D, point.z, 5, 0.3D, 0.25D, 0.3D, 0.02D);
                }
            }
            level().playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.0F, 0.55F);
            return;
        }

        // Rokuogan: Lucci's final six-king shockwave detonates around him.
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.3D, getZ(), 28, 2.4D, 1.0D, 2.4D, 0.12D);
            server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.6D, getZ(), 90, 4.5D, 0.7D, 4.5D, 0.08D);
        }
        damageArea(8.5D, 190.0F, true);
        level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.2F, 0.7F);
    }

    private int phase() {
        float ratio = getHealth() / Math.max(1.0F, getMaxHealth());
        return ratio <= 0.30F ? 3 : ratio <= 0.65F ? 2 : 1;
    }
}
