package de.nexusbay.forge.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
        int phase = phase();
        if (phase == 3) {
            // Raigo-style storm: several visible strikes around the marked target.
            for (int i = 0; i < 7; i++) {
                double angle = (Math.PI * 2.0D * i) / 7.0D;
                summonLightning(target.getX() + Math.cos(angle) * 5.0D, target.getY(), target.getZ() + Math.sin(angle) * 5.0D);
            }
            damageArea(12.0D, 150.0F, true);
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 120, 5.0D, 2.5D, 5.0D, 0.25D);
                server.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 4, 1.0D, 1.0D, 1.0D, 0.0D);
            }
        } else if (phase == 2) {
            // El Thor: a heavy targeted strike followed by a short stun.
            strikeTarget(target, 145.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 75, 2));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            summonLightning(target.getX(), target.getY(), target.getZ());
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 75, 1.8D, 1.6D, 1.8D, 0.2D);
            }
        } else {
            // Mantra-guided chain: Enel jumps between nearby opponents with rapid strikes.
            AABB range = target.getBoundingBox().inflate(10.0D, 4.0D, 10.0D);
            java.util.List<LivingEntity> victims = level().getEntitiesOfClass(LivingEntity.class, range,
                    entity -> entity != this && !(entity instanceof IslandBossEntity) && entity.isAlive());
            victims.sort(java.util.Comparator.comparingDouble(this::distanceToSqr));
            int hits = Math.min(3, victims.size());
            for (int i = 0; i < hits; i++) {
                LivingEntity victim = victims.get(i);
                strikeTarget(victim, i == 0 ? 105.0F : 65.0F);
                summonLightning(victim.getX(), victim.getY(), victim.getZ());
                if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, victim.getX(), victim.getY() + 1.0D, victim.getZ(), 28, 0.8D, 1.0D, 0.8D, 0.12D);
                }
            }
        }
        level().playSound(null, blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.7F, phase == 3 ? 0.75F : 1.1F);
    }

    private void summonLightning(double x, double y, double z) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
        if (bolt != null) {
            bolt.moveTo(x, y, z);
            bolt.setVisualOnly(true);
            level().addFreshEntity(bolt);
        }
    }

    private int phase() {
        float ratio = getHealth() / Math.max(1.0F, getMaxHealth());
        return ratio <= 0.30F ? 3 : ratio <= 0.65F ? 2 : 1;
    }
}
