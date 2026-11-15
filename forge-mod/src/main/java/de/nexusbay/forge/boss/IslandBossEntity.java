package de.nexusbay.forge.boss;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class IslandBossEntity extends Monster implements GeoEntity, BossMob {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossBar;
    private int specialCooldown = 100;
    private int phase = 1;

    protected IslandBossEntity(EntityType<? extends Monster> type, Level level, String title, float health,
                               double damage, double speed, BossEvent.BossBarColor color) {
        super(type, level);
        this.bossBar = new ServerBossEvent(Component.literal(title), color, BossEvent.BossBarOverlay.PROGRESS);
        this.xpReward = 2500;
        this.setCustomName(Component.literal(title));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        this.setHealth(health);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64.0D);
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 180000.0D)
                .add(Attributes.ATTACK_DAMAGE, 90.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
    }

    @Override protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 48.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state ->
                state.setAndContinue(state.isMoving()
                        ? RawAnimation.begin().thenLoop("animation." + animationKey() + ".walk")
                        : RawAnimation.begin().thenLoop("animation." + animationKey() + ".idle"))));
        controllers.add(new AnimationController<>(this, "combat", 0, state -> PlayState.STOP)
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation." + animationKey() + ".special"))
                .triggerableAnim("hit", RawAnimation.begin().thenPlay("animation." + animationKey() + ".hit")));
    }

    protected abstract String animationKey();
    protected abstract void performSpecial(LivingEntity target);

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        float ratio = getHealth() / Math.max(1.0F, getMaxHealth());
        int nextPhase = ratio <= 0.30F ? 3 : ratio <= 0.65F ? 2 : 1;
        if (nextPhase != phase) {
            phase = nextPhase;
            bossBar.setName(Component.literal(getCustomName().getString() + " §e· PHASE " + phase));
            level().playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.5F, 0.8F + phase * 0.12F);
            if (level() instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + getBbHeight() * 0.6D, getZ(), 18, 1.8D, 1.2D, 1.8D, 0.08D);
            }
        }
        bossBar.setProgress(Math.max(0.0F, ratio));
        if (specialCooldown > 0) specialCooldown--;
        LivingEntity target = getTarget();
        if (target != null && target.isAlive() && specialCooldown <= 0 && distanceToSqr(target) <= 24.0D * 24.0D) {
            performSpecial(target);
            specialCooldown = phase == 3 ? 65 : phase == 2 ? 85 : 110;
        }
    }

    protected void damageArea(double radius, float damage, boolean knockback) {
        AABB area = getBoundingBox().inflate(radius, 5.0D, radius);
        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && !(entity instanceof IslandBossEntity) && entity.isAlive())) {
            victim.hurt(damageSources().mobAttack(this), damage);
            if (knockback) {
                Vec3 push = victim.position().subtract(position()).normalize().scale(1.35D);
                victim.push(push.x, 0.45D, push.z);
            }
        }
    }

    protected void strikeTarget(LivingEntity target, float damage) {
        target.hurt(damageSources().mobAttack(this), damage);
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && tickCount % 5 == 0) {
            level().playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.45F, 1.6F);
        }
        return hurt;
    }

    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); bossBar.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bossBar.removePlayer(player); }

    @Override public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide) {
            bossBar.removeAllPlayers();
            level().playSound(null, blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 1.4F, 0.8F);
        }
    }
}
