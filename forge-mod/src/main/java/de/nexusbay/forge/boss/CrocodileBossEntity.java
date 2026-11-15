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

public class CrocodileBossEntity extends IslandBossEntity {
    private int attackPattern;

    public CrocodileBossEntity(EntityType<? extends CrocodileBossEntity> type, Level level) {
        super(type, level, "§6CROCODILE §7· WÜSTENKÖNIG · LEVEL 120", 180000.0F, 105.0D, 0.31D, BossEvent.BossBarColor.YELLOW);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, 180000.0D).add(Attributes.ATTACK_DAMAGE, 105.0D);
    }

    @Override protected String animationKey() { return "crocodile"; }

    @Override protected void performSpecial(LivingEntity target) {
        triggerAnim("combat", "special");
        int currentPhase = phase();
        int attack = attackPattern++ % (currentPhase == 3 ? 4 : 3);

        if (currentPhase == 3 && attack == 3) {
            sandstorm(target);
        } else if (attack == 0) {
            desertHook(target, currentPhase);
        } else if (attack == 1) {
            sandPrison(target);
        } else {
            groundEruption(target, currentPhase);
        }
    }

    private void desertHook(LivingEntity target, int currentPhase) {
        Vec3 pull = position().subtract(target.position()).normalize().scale(1.15D);
        target.push(pull.x, 0.22D, pull.z);
        target.hurt(damageSources().mobAttack(this), currentPhase == 3 ? 135.0F : currentPhase == 2 ? 115.0F : 85.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 90, 2));
        particles(target, ParticleTypes.SWEEP_ATTACK, 24, 1.0D);
        level().playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.8F, 0.65F);
    }

    private void sandPrison(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 5));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
        target.hurt(damageSources().mobAttack(this), 65.0F);
        particles(target, ParticleTypes.BLOCK, 70, 1.35D);
        level().playSound(null, target.blockPosition(), SoundEvents.SAND_PLACE, SoundSource.HOSTILE, 2.2F, 0.55F);
    }

    private void groundEruption(LivingEntity target, int currentPhase) {
        target.hurt(damageSources().mobAttack(this), currentPhase == 3 ? 125.0F : 90.0F);
        target.push(0.0D, 0.9D, 0.0D);
        damageArea(4.5D, currentPhase == 3 ? 85.0F : 55.0F, true);
        particles(target, ParticleTypes.CLOUD, 55, 2.8D);
        level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.8F, 0.7F);
    }

    private void sandstorm(LivingEntity target) {
        AABB storm = new AABB(target.blockPosition()).inflate(8.0D, 3.0D, 8.0D);
        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, storm,
                entity -> entity != this && entity.isAlive())) {
            victim.hurt(damageSources().mobAttack(this), 95.0F);
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1));
        }
        particles(target, ParticleTypes.CLOUD, 150, 5.5D);
        level().playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 2.0F, 0.55F);
    }

    private void particles(LivingEntity target, net.minecraft.core.particles.ParticleOptions particle, int count, double spread) {
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(particle, target.getX(), target.getY() + 0.8D, target.getZ(),
                    count, spread, 1.2D, spread, 0.08D);
        }
    }

    private int phase() {
        float ratio = getHealth() / Math.max(1.0F, getMaxHealth());
        return ratio <= 0.30F ? 3 : ratio <= 0.65F ? 2 : 1;
    }
}
