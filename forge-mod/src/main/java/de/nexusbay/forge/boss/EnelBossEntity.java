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

public class EnelBossEntity extends IslandBossEntity {
    private int attackPattern;

    public EnelBossEntity(EntityType<? extends EnelBossEntity> type, Level level) {
        super(type, level, "§eENEL §7· GOTT VON SKYPIEA · LEVEL 150", 260000.0F, 125.0D, 0.38D, BossEvent.BossBarColor.YELLOW);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, 260000.0D)
                .add(Attributes.ATTACK_DAMAGE, 125.0D).add(Attributes.MOVEMENT_SPEED, 0.38D);
    }

    @Override protected String animationKey() { return "enel"; }

    @Override protected void performSpecial(LivingEntity target) {
        triggerAnim("combat", "special");
        int currentPhase = phase();
        int attack = attackPattern++ % (currentPhase == 3 ? 4 : 3);

        if (currentPhase == 3 && attack == 3) {
            raigoBurst(target);
        } else if (attack == 0) {
            elThor(target, currentPhase);
        } else if (attack == 1) {
            lightningCage(target);
        } else {
            lightningRush(target, currentPhase);
        }
    }

    private void elThor(LivingEntity target, int currentPhase) {
        strike(target.getX(), target.getY(), target.getZ());
        target.hurt(damageSources().mobAttack(this), currentPhase == 3 ? 165.0F : currentPhase == 2 ? 145.0F : 105.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 45, 1));
        particles(target, 75, 2.2D);
    }

    private void lightningCage(LivingEntity target) {
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2.0D * i / 8.0D;
            strike(target.getX() + Math.cos(angle) * 3.5D, target.getY(), target.getZ() + Math.sin(angle) * 3.5D);
        }
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 90, 4));
        target.hurt(damageSources().mobAttack(this), 80.0F);
        particles(target, 110, 4.0D);
        level().playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.0F, 0.75F);
    }

    private void lightningRush(LivingEntity target, int currentPhase) {
        teleportTo(target.getX() - 1.5D, target.getY(), target.getZ() - 1.5D);
        target.hurt(damageSources().mobAttack(this), currentPhase == 3 ? 145.0F : 100.0F);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, 1));
        strike(target.getX(), target.getY(), target.getZ());
        particles(target, 55, 1.6D);
    }

    private void raigoBurst(LivingEntity target) {
        AABB area = new AABB(target.blockPosition()).inflate(12.0D, 5.0D, 12.0D);
        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity.isAlive())) {
            victim.hurt(damageSources().mobAttack(this), 150.0F);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        }
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * 2.0D * i / 12.0D;
            strike(target.getX() + Math.cos(angle) * 6.0D, target.getY(), target.getZ() + Math.sin(angle) * 6.0D);
        }
        particles(target, 180, 6.0D);
        level().playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 3.0F, 0.55F);
    }

    private void strike(double x, double y, double z) {
        net.minecraft.world.entity.LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
        if (bolt != null) {
            bolt.moveTo(x, y, z);
            level().addFreshEntity(bolt);
        }
    }

    private void particles(LivingEntity target, int count, double spread) {
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D,
                    target.getZ(), count, spread, 2.0D, spread, 0.18D);
            server.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.0D,
                    target.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.0D);
        }
        level().playSound(null, blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.HOSTILE, 1.7F, 1.1F);
    }

    private int phase() {
        float ratio = getHealth() / Math.max(1.0F, getMaxHealth());
        return ratio <= 0.30F ? 3 : ratio <= 0.65F ? 2 : 1;
    }
}
