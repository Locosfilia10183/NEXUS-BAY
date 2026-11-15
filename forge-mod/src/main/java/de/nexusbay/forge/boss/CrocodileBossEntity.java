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
        Vec3 delta = target.position().subtract(position()).normalize();
        if (phase() == 3) {
            damageArea(10.0D, 135.0F, true);
            level().explode(this, getX(), getY(), getZ(), 3.2F, Level.ExplosionInteraction.NONE);
            return;
        }
        if (distanceToSqr(target) > 8.0D * 8.0D) {
            Vec3 point = target.position().subtract(delta.scale(2.0D));
            teleportTo(point.x, point.y, point.z);
        }
        target.hurt(damageSources().mobAttack(this), phase() == 2 ? 115.0F : 85.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.8D, target.getZ(), 45, 2.2D, 1.0D, 2.2D, 0.08D);
            server.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1.0D, getZ(), 14, 1.2D, 0.8D, 1.2D, 0.0D);
        }
        level().playSound(null, blockPosition(), SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 2.0F, 0.7F);
    }
    private int phase() { return getHealth() / getMaxHealth() <= 0.30F ? 3 : getHealth() / getMaxHealth() <= 0.65F ? 2 : 1; }
}
