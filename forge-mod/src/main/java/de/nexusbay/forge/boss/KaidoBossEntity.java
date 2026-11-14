package de.nexusbay.forge.boss;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KaidoBossEntity extends Monster implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.kaido.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.kaido.walk");
    private static final RawAnimation BREATH_ANIM = RawAnimation.begin().thenPlay("animation.kaido.breath");
    private static final RawAnimation SLAM_ANIM = RawAnimation.begin().thenPlay("animation.kaido.slam");

    private static final int NO_ATTACK = 0;
    private static final int BREATH_ATTACK = 1;
    private static final int SLAM_ATTACK = 2;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final double KAIDO_MAX_HEALTH = 60_000_000D;
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("KAIDO – KÖNIG DER BESTIEN"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    );

    private int currentPhase = 1;
    private int breathCooldown = 0;
    private int slamCooldown = 0;
    private int pendingAttack = NO_ATTACK;
    private int attackImpactDelay = 0;
    private int attackLockTicks = 0;

    public KaidoBossEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 5000;
        this.setCustomName(Component.literal("§cKAIDO §6[WELTBOSS · LEVEL 250]"));
        this.setCustomNameVisible(true);
        this.setHealth((float) KAIDO_MAX_HEALTH);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
        controllers.add(new AnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                .triggerableAnim("breath", BREATH_ANIM)
                .triggerableAnim("slam", SLAM_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, KAIDO_MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(3.5F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount == 1) {
            setHealth((float) getMaxHealth());
        }
        if (!level().isClientSide) {
            if (breathCooldown > 0) breathCooldown--;
            if (slamCooldown > 0) slamCooldown--;
            if (tickCount % 5 == 0) {
                float progress = (float) Math.max(0.0D, Math.min(1.0D,
                        getHealth() / getMaxHealth()));
                bossEvent.setProgress(progress);
                updatePhase();
            }

            tickPendingAttack();
            if (pendingAttack == NO_ATTACK) {
                runBossCombat();
            }
        }
    }

    /**
     * Runs the attack wind-up on the server. Damage is applied only when the
     * animation reaches its impact frame, not when the animation is triggered.
     */
    private void tickPendingAttack() {
        if (pendingAttack == NO_ATTACK) return;

        if (attackImpactDelay > 0) {
            attackImpactDelay--;
            if (attackImpactDelay == 0) {
                if (pendingAttack == BREATH_ATTACK) applyBreathDamage();
                else if (pendingAttack == SLAM_ATTACK) applySlamDamage();
            }
        }

        if (attackLockTicks > 0) attackLockTicks--;
        if (attackLockTicks == 0) {
            pendingAttack = NO_ATTACK;
        }
    }

    private void runBossCombat() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;

        double distanceSqr = distanceToSqr(target);
        if (distanceSqr >= 36.0D && distanceSqr <= 576.0D && breathCooldown <= 0) {
            startBreathAttack();
            breathCooldown = currentPhase == 3 ? 65 : 100;
            return;
        }

        if (distanceSqr <= 64.0D && slamCooldown <= 0) {
            startSlamAttack();
            slamCooldown = currentPhase == 3 ? 75 : 110;
        }
    }

    private void startBreathAttack() {
        pendingAttack = BREATH_ATTACK;
        attackImpactDelay = 12; // 0.6 seconds: breath release after the wind-up
        attackLockTicks = 24;   // 1.2-second animation lock
        triggerAnim("attack", "breath");
    }

    private void startSlamAttack() {
        pendingAttack = SLAM_ATTACK;
        attackImpactDelay = 12; // 0.6 seconds: impact at the downward strike
        attackLockTicks = 18;   // 0.9-second animation lock
        triggerAnim("attack", "slam");
    }

    private void applyBreathDamage() {
        Vec3 look = getLookAngle().normalize();
        Vec3 origin = position().add(0.0D, getBbHeight() * 0.65D, 0.0D);
        AABB area = getBoundingBox().expandTowards(look.scale(18.0D)).inflate(3.0D);

        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity.isAlive())) {
            Vec3 direction = victim.position().add(0.0D, victim.getBbHeight() * 0.5D, 0.0D)
                    .subtract(origin).normalize();
            if (look.dot(direction) > 0.72D && victim.distanceToSqr(this) <= 400.0D) {
                victim.hurt(damageSources().mobAttack(this), currentPhase == 3 ? 32.0F : 22.0F);
                victim.setSecondsOnFire(5);
            }
        }
    }

    private void applySlamDamage() {
        double radius = currentPhase == 3 ? 9.0D : 7.0D;
        AABB area = getBoundingBox().inflate(radius, 3.0D, radius);

        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity.isAlive())) {
            victim.hurt(damageSources().mobAttack(this), currentPhase == 3 ? 38.0F : 26.0F);
            Vec3 push = victim.position().subtract(position()).normalize();
            victim.push(push.x * 2.2D, 0.8D, push.z * 2.2D);
            victim.hurtMarked = true;
        }
    }

    private void updatePhase() {
        float healthPercent = getHealth() / getMaxHealth() * 100.0F;
        int nextPhase = healthPercent <= 30.0F ? 3 : healthPercent <= 65.0F ? 2 : 1;
        if (nextPhase != currentPhase) {
            currentPhase = nextPhase;
            String phaseName = switch (currentPhase) {
                case 2 -> "HYBRIDFORM";
                case 3 -> "DRACHENFORM";
                default -> "MENSCHENFORM";
            };
            bossEvent.setName(Component.literal("KAIDO – " + phaseName));
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean damaged = super.hurt(source, amount);
        if (damaged && isDeadOrDying()) bossEvent.setProgress(0.0F);
        return damaged;
    }

    @Override
    public void die(DamageSource source) {
        pendingAttack = NO_ATTACK;
        bossEvent.removeAllPlayers();
        super.die(source);
    }
}
