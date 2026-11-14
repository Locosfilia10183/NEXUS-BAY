package de.nexusbay.forge.boss;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
    private static final RawAnimation KANABO_ANIM = RawAnimation.begin().thenPlay("animation.kaido.kanabo");
    private static final RawAnimation HYBRID_TRANSFORM_ANIM = RawAnimation.begin().thenPlay("animation.kaido.hybrid_transform");
    private static final RawAnimation DRAGON_TRANSFORM_ANIM = RawAnimation.begin().thenPlay("animation.kaido.dragon_transform");
    private static final RawAnimation DRAGON_IDLE_ANIM = RawAnimation.begin().thenLoop("animation.kaido.dragon_idle");
    private static final RawAnimation DRAGON_MOVE_ANIM = RawAnimation.begin().thenLoop("animation.kaido.dragon_move");
    private static final RawAnimation DRAGON_BREATH_ANIM = RawAnimation.begin().thenPlay("animation.kaido.dragon_breath");
    private static final RawAnimation HIT_REACT_ANIM = RawAnimation.begin().thenPlay("animation.kaido.hit_react");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("animation.kaido.death");
    private static final RawAnimation RAGNAROK_ANIM = RawAnimation.begin().thenPlay("animation.kaido.ragnarok");
    private static final RawAnimation THUNDER_BAGUA_ANIM = RawAnimation.begin().thenPlay("animation.kaido.thunder_bagua");
    private static final RawAnimation BLAST_BREATH_ANIM = RawAnimation.begin().thenPlay("animation.kaido.blast_breath");
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(KaidoBossEntity.class, EntityDataSerializers.INT);

    private static final int NO_ATTACK = 0;
    private static final int BREATH_ATTACK = 1;
    private static final int SLAM_ATTACK = 2;
    private static final int RAGNAROK_ATTACK = 3;
    private static final int THUNDER_BAGUA_ATTACK = 4;
    private static final int BLAST_BREATH_ATTACK = 5;

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
    private int ragnarokCooldown = 0;
    private int thunderBaguaCooldown = 0;
    private int blastBreathCooldown = 0;
    private int pendingAttack = NO_ATTACK;
    private int attackImpactDelay = 0;
    private int attackLockTicks = 0;
    private boolean dragonFlying = false;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, 1);
    }

    public int getCurrentPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_PHASE.equals(accessor)) {
            refreshDimensions();
        }
    }

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
            if (getCurrentPhase() == 3) {
                if (state.isMoving()) return state.setAndContinue(DRAGON_MOVE_ANIM);
                return state.setAndContinue(DRAGON_IDLE_ANIM);
            }
            if (state.isMoving()) return state.setAndContinue(WALK_ANIM);
            return state.setAndContinue(IDLE_ANIM);
        }));
        controllers.add(new AnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                .triggerableAnim("breath", BREATH_ANIM)
                .triggerableAnim("slam", SLAM_ANIM)
                .triggerableAnim("kanabo", KANABO_ANIM)
                .triggerableAnim("hybrid_transform", HYBRID_TRANSFORM_ANIM)
                .triggerableAnim("dragon_transform", DRAGON_TRANSFORM_ANIM)
                .triggerableAnim("dragon_breath", DRAGON_BREATH_ANIM)
                .triggerableAnim("hit_react", HIT_REACT_ANIM)
                .triggerableAnim("death", DEATH_ANIM)
                .triggerableAnim("ragnarok", RAGNAROK_ANIM)
                .triggerableAnim("thunder_bagua", THUNDER_BAGUA_ANIM)
                .triggerableAnim("blast_breath", BLAST_BREATH_ANIM));
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
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !level().isClientSide && pendingAttack == NO_ATTACK) {
            triggerAnim("attack", "kanabo");
        }
        return hit;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        // Collision dimensions follow the active form instead of applying one
        // oversized hitbox to every form.
        return switch (getCurrentPhase()) {
            case 2 -> EntityDimensions.scalable(2.2F, 4.8F);
            case 3 -> EntityDimensions.scalable(5.2F, 7.5F);
            default -> EntityDimensions.scalable(1.35F, 3.2F);
        };
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
            if (ragnarokCooldown > 0) ragnarokCooldown--;
            if (thunderBaguaCooldown > 0) thunderBaguaCooldown--;
            if (blastBreathCooldown > 0) blastBreathCooldown--;
            if (tickCount % 5 == 0) {
                float progress = (float) Math.max(0.0D, Math.min(1.0D,
                        getHealth() / getMaxHealth()));
                bossEvent.setProgress(progress);
                updatePhase();
            }

            tickDragonFlight();
            tickConquerorsHakiAura();
            tickPendingAttack();
            if (pendingAttack == NO_ATTACK) {
                runBossCombat();
            }
        }
    }

    /**
     * In dragon form Kaido takes to the air when a target is far away,
     * tracks it from above, and returns to normal gravity near the target.
     */
    private void tickDragonFlight() {
        LivingEntity target = getTarget();
        if (getCurrentPhase() != 3 || target == null || !target.isAlive()) {
            if (dragonFlying) {
                dragonFlying = false;
                setNoGravity(false);
            }
            return;
        }

        double distanceSqr = distanceToSqr(target);
        if (!dragonFlying && distanceSqr > 144.0D) {
            dragonFlying = true;
            setNoGravity(true);
            getNavigation().stop();
        } else if (dragonFlying && distanceSqr < 49.0D) {
            dragonFlying = false;
            setNoGravity(false);
            return;
        }

        if (!dragonFlying) return;

        getNavigation().stop();
        Vec3 destination = target.position().add(0.0D, 5.0D, 0.0D);
        Vec3 difference = destination.subtract(position());
        Vec3 desiredVelocity = difference.normalize().scale(0.22D);
        Vec3 currentVelocity = getDeltaMovement();
        setDeltaMovement(
                currentVelocity.x * 0.72D + desiredVelocity.x * 0.28D,
                currentVelocity.y * 0.72D + desiredVelocity.y * 0.28D,
                currentVelocity.z * 0.72D + desiredVelocity.z * 0.28D
        );
        hasImpulse = true;

        if (tickCount % 4 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    getX(), getY() + 0.4D, getZ(), 3, 0.8D, 0.25D, 0.8D, 0.01D);
        }
    }

    /**
     * Server-side visual aura for Kaido's advanced forms.
     * The particles are cosmetic and do not apply extra damage.
     */
    private void tickConquerorsHakiAura() {
        int phase = getCurrentPhase();
        if (phase < 2 || tickCount % 5 != 0 || !(level() instanceof ServerLevel serverLevel)) return;

        double radius = phase == 3 ? 2.8D : 1.9D;
        double height = getBbHeight();
        for (int i = 0; i < (phase == 3 ? 12 : 8); i++) {
            double angle = (Math.PI * 2.0D * i) / (phase == 3 ? 12.0D : 8.0D)
                    + tickCount * 0.08D;
            double x = getX() + Math.cos(angle) * radius;
            double z = getZ() + Math.sin(angle) * radius;
            double y = getY() + 0.35D + (i % 4) * (height / 4.0D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    x, y, z, 2, 0.12D, 0.22D, 0.12D, 0.015D);
        }

        if (phase == 3) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    getX(), getY() + height * 0.55D, getZ(),
                    2, 1.0D, height * 0.25D, 1.0D, 0.005D);
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
                else if (pendingAttack == RAGNAROK_ATTACK) applyRagnarokDamage();
                else if (pendingAttack == THUNDER_BAGUA_ATTACK) applyThunderBaguaDamage();
                else if (pendingAttack == BLAST_BREATH_ATTACK) applyBlastBreathDamage();
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
        if (currentPhase == 3 && distanceSqr >= 400.0D && distanceSqr <= 1600.0D
                && blastBreathCooldown <= 0) {
            startBlastBreathAttack();
            blastBreathCooldown = 400;
            return;
        }
        if (currentPhase >= 2 && distanceSqr >= 25.0D && distanceSqr <= 225.0D
                && thunderBaguaCooldown <= 0) {
            startThunderBaguaAttack();
            thunderBaguaCooldown = 300;
            return;
        }
        if (currentPhase == 2 && distanceSqr >= 16.0D && distanceSqr <= 100.0D
                && ragnarokCooldown <= 0) {
            startRagnarokAttack();
            ragnarokCooldown = 240;
            return;
        }
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
        triggerAnim("attack", currentPhase == 3 ? "dragon_breath" : "breath");
    }

    private void startSlamAttack() {
        pendingAttack = SLAM_ATTACK;
        attackImpactDelay = 12; // 0.6 seconds: impact at the downward strike
        attackLockTicks = 18;   // 0.9-second animation lock
        triggerAnim("attack", "slam");
    }

    private void startRagnarokAttack() {
        pendingAttack = RAGNAROK_ATTACK;
        attackImpactDelay = 24; // 1.2-second charge-up before the overhead strike
        attackLockTicks = 36;   // 1.8-second recovery window
        triggerAnim("attack", "ragnarok");
    }

    private void startThunderBaguaAttack() {
        pendingAttack = THUNDER_BAGUA_ATTACK;
        attackImpactDelay = 16; // 0.8-second wind-up before the lightning-fast strike
        attackLockTicks = 30;   // 1.5-second total attack window
        triggerAnim("attack", "thunder_bagua");
    }

    private void startBlastBreathAttack() {
        pendingAttack = BLAST_BREATH_ATTACK;
        attackImpactDelay = 24; // 1.2-second dragon breath charge-up
        attackLockTicks = 40;   // 2-second attack and recovery window
        triggerAnim("attack", "blast_breath");
    }

    private void applyBreathDamage() {
        Vec3 look = getLookAngle().normalize();
        Vec3 origin = position().add(0.0D, getBbHeight() * 0.65D, 0.0D);
        double range = currentPhase == 3 ? 26.0D : 18.0D;
        double coneDot = currentPhase == 3 ? 0.66D : 0.72D;
        AABB area = getBoundingBox().expandTowards(look.scale(range)).inflate(currentPhase == 3 ? 4.0D : 3.0D);

        if (level() instanceof ServerLevel serverLevel) {
            // A visible, segmented flame stream follows the boss's facing direction.
            for (double distance = 2.0D; distance <= range; distance += 2.0D) {
                Vec3 point = origin.add(look.scale(distance));
                double spread = currentPhase == 3 ? 0.65D + distance * 0.055D : 0.35D;
                serverLevel.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z,
                        currentPhase == 3 ? 12 : 7, spread, spread * 0.65D, spread, 0.015D);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, point.x, point.y, point.z,
                        currentPhase == 3 ? 4 : 2, spread * 0.7D, spread * 0.5D, spread * 0.7D, 0.005D);
                if (currentPhase == 3 && distance % 4.0D == 2.0D) {
                    serverLevel.sendParticles(ParticleTypes.LAVA, point.x, point.y, point.z,
                            2, spread * 0.35D, spread * 0.3D, spread * 0.35D, 0.0D);
                }
            }
        }

        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity.isAlive())) {
            Vec3 direction = victim.position().add(0.0D, victim.getBbHeight() * 0.5D, 0.0D)
                    .subtract(origin).normalize();
            if (look.dot(direction) > coneDot && victim.distanceToSqr(this) <= range * range) {
                victim.hurt(damageSources().mobAttack(this), currentPhase == 3 ? 45.0F : 22.0F);
                victim.setSecondsOnFire(currentPhase == 3 ? 8 : 5);
            }
        }
    }

    private void applyBlastBreathDamage() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;

        Vec3 origin = position().add(0.0D, getBbHeight() * 0.68D, 0.0D);
        Vec3 direction = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                .subtract(origin).normalize();
        double range = 40.0D;
        double impactRadius = 5.0D;
        Vec3 endpoint = origin.add(direction.scale(range));

        if (level() instanceof ServerLevel serverLevel) {
            // A continuous-looking fire beam with smoke around its core.
            for (double distance = 2.0D; distance <= range; distance += 1.5D) {
                Vec3 point = origin.add(direction.scale(distance));
                double spread = 0.45D + distance * 0.035D;
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        point.x, point.y, point.z, 14, spread, spread, spread, 0.025D);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        point.x, point.y, point.z, 5, spread * 0.8D, spread * 0.8D,
                        spread * 0.8D, 0.008D);
                if (distance % 6.0D < 1.5D) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                            point.x, point.y, point.z, 3, spread * 0.35D,
                            spread * 0.35D, spread * 0.35D, 0.0D);
                }
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    endpoint.x, endpoint.y, endpoint.z, 2, 1.0D, 0.8D, 1.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    endpoint.x, endpoint.y, endpoint.z, 90, 2.2D, 1.6D, 2.2D, 0.12D);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    endpoint.x, endpoint.y, endpoint.z, 35, 2.0D, 1.4D, 2.0D, 0.035D);
        }

        AABB searchArea = new AABB(
                Math.min(origin.x, endpoint.x) - impactRadius,
                Math.min(origin.y, endpoint.y) - impactRadius,
                Math.min(origin.z, endpoint.z) - impactRadius,
                Math.max(origin.x, endpoint.x) + impactRadius,
                Math.max(origin.y, endpoint.y) + impactRadius,
                Math.max(origin.z, endpoint.z) + impactRadius
        );
        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, searchArea,
                entity -> entity != this && entity.isAlive())) {
            Vec3 victimPoint = victim.position().add(0.0D, victim.getBbHeight() * 0.5D, 0.0D);
            Vec3 relative = victimPoint.subtract(origin);
            double alongBeam = relative.dot(direction);
            if (alongBeam < 0.0D || alongBeam > range) continue;
            Vec3 closestPoint = origin.add(direction.scale(alongBeam));
            double distanceToBeam = victimPoint.distanceTo(closestPoint);
            if (distanceToBeam <= impactRadius) {
                victim.hurt(damageSources().mobAttack(this), 75.0F);
                victim.setSecondsOnFire(10);
                Vec3 knockback = victim.position().subtract(endpoint).normalize();
                victim.push(knockback.x * 1.6D, 0.85D, knockback.z * 1.6D);
                victim.hurtMarked = true;
            }
        }
    }

    private void applyThunderBaguaDamage() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;

        Vec3 strikePoint = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        AABB impactArea = new AABB(
                strikePoint.x - 3.5D, strikePoint.y - 3.0D, strikePoint.z - 3.5D,
                strikePoint.x + 3.5D, strikePoint.y + 3.0D, strikePoint.z + 3.5D
        );

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    strikePoint.x, strikePoint.y, strikePoint.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            for (int i = 0; i < 32; i++) {
                double angle = (Math.PI * 2.0D * i) / 32.0D;
                double radius = 1.0D + (i % 4) * 0.55D;
                double x = strikePoint.x + Math.cos(angle) * radius;
                double z = strikePoint.z + Math.sin(angle) * radius;
                double y = strikePoint.y + ((i % 5) - 2) * 0.45D;
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        x, y, z, 5, 0.18D, 0.28D, 0.18D, 0.08D);
            }
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    strikePoint.x, strikePoint.y, strikePoint.z,
                    18, 1.2D, 0.8D, 1.2D, 0.18D);
        }

        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, impactArea,
                entity -> entity != this && entity.isAlive())) {
            victim.hurt(damageSources().mobAttack(this), 55.0F);
            Vec3 knockback = victim.position().subtract(position()).normalize();
            victim.push(knockback.x * 2.8D, 0.75D, knockback.z * 2.8D);
            victim.hurtMarked = true;
        }
    }

    private void applyRagnarokDamage() {
        double radius = 11.0D;
        AABB impactArea = getBoundingBox().inflate(radius, 4.0D, radius);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    getX(), getY() + 0.5D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            // Expanding ground shockwave; particles are visual only, damage is applied once below.
            for (int ring = 1; ring <= 4; ring++) {
                double ringRadius = ring * 2.5D;
                for (int point = 0; point < 24; point++) {
                    double angle = (Math.PI * 2.0D * point) / 24.0D;
                    double x = getX() + Math.cos(angle) * ringRadius;
                    double z = getZ() + Math.sin(angle) * ringRadius;
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            x, getY() + 0.15D, z, 2, 0.25D, 0.12D, 0.25D, 0.04D);
                    if (ring == 4 && point % 3 == 0) {
                        serverLevel.sendParticles(ParticleTypes.CRIT,
                                x, getY() + 0.5D, z, 4, 0.35D, 0.3D, 0.35D, 0.15D);
                    }
                }
            }
        }

        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, impactArea,
                entity -> entity != this && entity.isAlive())) {
            double distance = victim.distanceTo(this);
            if (distance <= radius) {
                victim.hurt(damageSources().mobAttack(this), 65.0F);
                Vec3 push = victim.position().subtract(position()).normalize();
                double strength = Math.max(0.8D, (radius - distance) * 0.35D);
                victim.push(push.x * strength, 0.9D + strength * 0.25D, push.z * strength);
                victim.hurtMarked = true;
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
            int previousPhase = currentPhase;
            currentPhase = nextPhase;
            this.entityData.set(DATA_PHASE, nextPhase);
            refreshDimensions();
            if (nextPhase == 2) triggerAnim("attack", "hybrid_transform");
            else if (nextPhase == 3) triggerAnim("attack", "dragon_transform");
            playTransformationBurst(previousPhase, nextPhase);
            String phaseName = switch (currentPhase) {
                case 2 -> "HYBRIDFORM";
                case 3 -> "DRACHENFORM";
                default -> "MENSCHENFORM";
            };
            bossEvent.setName(Component.literal("KAIDO – " + phaseName));
        }
    }

    /**
     * One-time transformation burst when Kaido changes form.
     * This is visual only; it does not deal damage or alter terrain.
     */
    private void playTransformationBurst(int previousPhase, int nextPhase) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        double height = getBbHeight();
        double radius = nextPhase == 3 ? 5.5D : nextPhase == 2 ? 3.2D : 1.8D;
        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                getX(), getY() + height * 0.45D, getZ(),
                nextPhase == 3 ? 8 : 4, radius * 0.35D, height * 0.25D,
                radius * 0.35D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                getX(), getY() + height * 0.35D, getZ(),
                nextPhase == 3 ? 55 : 28, radius * 0.6D, height * 0.22D,
                radius * 0.6D, 0.12D);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                getX(), getY() + height * 0.55D, getZ(),
                nextPhase == 3 ? 90 : 45, radius, height * 0.45D,
                radius, 0.16D);
        serverLevel.sendParticles(ParticleTypes.FLAME,
                getX(), getY() + height * 0.2D, getZ(),
                nextPhase == 3 ? 35 : 16, radius * 0.5D, height * 0.18D,
                radius * 0.5D, 0.04D);

        // A rising spiral marks the transformation and makes its scale readable.
        for (int i = 0; i < 18; i++) {
            double angle = (Math.PI * 2.0D * i) / 18.0D;
            double x = getX() + Math.cos(angle) * radius;
            double z = getZ() + Math.sin(angle) * radius;
            double y = getY() + (height * i / 18.0D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    x, y, z, 3, 0.12D, 0.18D, 0.12D, 0.035D);
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
        if (damaged && !level().isClientSide) {
            if (isDeadOrDying()) {
                bossEvent.setProgress(0.0F);
            } else if (pendingAttack == NO_ATTACK) {
                triggerAnim("attack", "hit_react");
            }
        }
        return damaged;
    }

    @Override
    public void die(DamageSource source) {
        pendingAttack = NO_ATTACK;
        attackImpactDelay = 0;
        attackLockTicks = 0;
        dragonFlying = false;
        setNoGravity(false);
        if (!level().isClientSide) triggerAnim("attack", "death");
        bossEvent.removeAllPlayers();
        super.die(source);
    }
}
