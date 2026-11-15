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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public class EnelBossEntity extends IslandBossEntity {
    public EnelBossEntity(EntityType<? extends EnelBossEntity> type, Level level) {
        super(type, level, "§eENEL §7· GOTT VON SKYPIEA · LEVEL 150", 260000.0F, 125.0D, 0.38D, BossEvent.BossBarColor.YELLOW);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, 260000.0D).add(Attributes.ATTACK_DAMAGE, 125.0D).add(Attributes.MOVEMENT_SPEED, 0.38D);
    }
    @Override protected String animationKey() { return "enel"; }
    @Override protected void performSpecial(LivingEntity target) {
        triggerAnim("combat", "special");
        if (phase() == 3) {
            for (int i = 0; i < 5; i++) {
                double angle = (Math.PI * 2.0D * i) / 5.0D;
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
                if (bolt != null) {
                    bolt.moveTo(target.getX() + Math.cos(angle) * 4.0D, target.getY(), target.getZ() + Math.sin(angle) * 4.0D);
                    level().addFreshEntity(bolt);
                }
            }
            damageArea(12.0D, 150.0F, true);
        } else {
            strikeTarget(target, phase() == 2 ? 145.0F : 105.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1));
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
            if (bolt != null) {
                bolt.moveTo(target.getX(), target.getY(), target.getZ());
                level().addFreshEntity(bolt);
            }
        }
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 65, 2.5D, 2.0D, 2.5D, 0.18D);
            server.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.0D);
        }
        level().playSound(null, blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.7F, 1.1F);
    }
    private int phase() { return getHealth() / getMaxHealth() <= 0.30F ? 3 : getHealth() / getMaxHealth() <= 0.65F ? 2 : 1; }
}
