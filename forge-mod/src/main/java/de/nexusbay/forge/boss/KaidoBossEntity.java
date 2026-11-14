package de.nexusbay.forge.boss;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class KaidoBossEntity extends Monster implements GeoEntity, BossMob {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.kaido.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.kaido.walk");
    private static final RawAnimation BREATH_ANIM = RawAnimation.begin().thenPlay("animation.kaido.breath");
    private static final RawAnimation SLAM_ANIM = RawAnimation.begin().thenPlay("animation.kaido.slam");
    private static final RawAnimation KANABO_ANIM = RawAnimation.begin().thenPlay("animation.kaido.kanabo");
    private static final RawAnimation DRUNKEN_SWAY_ANIM = RawAnimation.begin().thenPlay("animation.kaido.drunken_sway");
    private static final RawAnimation DRUNKEN_STRIKE_ANIM = RawAnimation.begin().thenPlay("animation.kaido.drunken_strike");
    private static final RawAnimation KANABO_COMBO_ANIM = RawAnimation.begin().thenPlay("animation.kaido.kanabo_combo");
    private static final RawAnimation HYBRID_TRANSFORM_ANIM = RawAnimation.begin().thenPlay("animation.kaido.hybrid_transform");
    private static final RawAnimation DRAGON_TRANSFORM_ANIM = RawAnimation.begin().thenPlay("animation.kaido.dragon_transform");
    private static final RawAnimation DRAGON_IDLE_ANIM = RawAnimation.begin().thenLoop("animation.kaido.dragon_idle");
    private static final RawAnimation DRAGON_MOVE_ANIM = RawAnimation.begin().thenLoop("animation.kaido.dragon_move");
    private static final RawAnimation DRAGON_FLY_ANIM = RawAnimation.begin().thenLoop("animation.kaido.dragon_fly");
    private static final RawAnimation DRAGON_TAKEOFF_ANIM = RawAnimation.begin().thenPlay("animation.kaido.dragon_takeoff");
    private static final RawAnimation DRAGON_LAND_ANIM = RawAnimation.begin().thenPlay("animation.kaido.dragon_land");
    private static final RawAnimation DRAGON_SKYFALL_ANIM = RawAnimation.begin().thenPlay("animation.kaido.dragon_skyfall");
    private static final RawAnimation DRAGON_TWISTER_ANIM = RawAnimation.begin().thenPlay("animation.kaido.dragon_twister");
    private static final RawAnimation DRAGON_BREATH_ANIM = RawAnimation.begin().thenPlay("animation.kaido.dragon_breath");
    private static final RawAnimation HIT_REACT_ANIM = RawAnimation.begin().thenPlay("animation.kaido.hit_react");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("animation.kaido.death");
    private static final RawAnimation RAGNAROK_ANIM = RawAnimation.begin().thenPlay("animation.kaido.ragnarok");
    private static final RawAnimation THUNDER_BAGUA_ANIM = RawAnimation.begin().thenPlay("animation.kaido.thunder_bagua");
    private static final RawAnimation BLAST_BREATH_ANIM = RawAnimation.begin().thenPlay("animation.kaido.blast_breath");
    private static final RawAnimation CONQUERORS_HAKI_ANIM = RawAnimation.begin().thenPlay("animation.kaido.conquerors_haki_burst");
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(KaidoBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DRAGON_FLYING =
            SynchedEntityData.defineId(KaidoBossEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int NO_ATTACK = 0;
    private static final int BREATH_ATTACK = 1;
    private static final int SLAM_ATTACK = 2;
    private static final int RAGNAROK_ATTACK = 3;
    private static final int THUNDER_BAGUA_ATTACK = 4;
    private static final int BLAST_BREATH_ATTACK = 5;
    private static final int DRAGON_SKYFALL_ATTACK = 6;
    private static final int DRAGON_TWISTER_ATTACK = 7;
    private static final int CONQUERORS_HAKI_ATTACK = 8;

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
    private int skyfallCooldown = 0;
    private int dragonTwisterCooldown = 0;
    private int conquerorsHakiCooldown = 0;
    private Vec3 skyfallTarget = Vec3.ZERO;
    private int pendingAttack = NO_ATTACK;
    private int attackImpactDelay = 0;
    private int attackLockTicks = 0;
    private boolean dragonFlying = false;
    private int aerialComboStep = 0;
    private int kanaboComboStep = 0;
    private boolean arenaBound = false;
    private double arenaCenterX;
    private double arenaCenterZ;
    private double arenaRadius = 42.0D;
    private final Set<UUID> arenaAnnouncedPlayers = new HashSet<>();
    private final Set<UUID> arenaParticipants = new HashSet<>();

    public void setArenaBounds(double centerX, double centerZ, double radius) {
        this.arenaBound = true;
        this.arenaCenterX = centerX;
        this.arenaCenterZ = centerZ;
        this.arenaRadius = Math.max(12.0D, radius);
    }

    private void tickArenaBoundary() {
        if (!arenaBound) return;
        if (level() instanceof ServerLevel serverLevel && tickCount % 10 == 0) {
            AABB noticeArea = new AABB(arenaCenterX - arenaRadius, getY() - 64.0D,
                    arenaCenterZ - arenaRadius, arenaCenterX + arenaRadius, getY() + 64.0D,
                    arenaCenterZ + arenaRadius);
            for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, noticeArea)) {
                double playerDx = player.getX() - arenaCenterX;
                double playerDz = player.getZ() - arenaCenterZ;
                if (playerDx * playerDx + playerDz * playerDz <= arenaRadius * arenaRadius) {
                    arenaParticipants.add(player.getUUID());
                    if (arenaAnnouncedPlayers.add(player.getUUID())) {
                        player.displayClientMessage(Component.literal("§4§lONIGASHIMA §c– Du hast Kaidos Gebiet betreten! Bereite dich auf den Kampf vor."), false);
                        player.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 0.85F, 0.72F);
                    }
                }
            }
        }
        double dx = getX() - arenaCenterX;
        double dz = getZ() - arenaCenterZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= arenaRadius) return;
        double nx = dx / Math.max(0.001D, distance);
        double nz = dz / Math.max(0.001D, distance);
        getNavigation().stop();
        setDeltaMovement(-nx * 0.42D, Math.max(-0.15D, getDeltaMovement().y), -nz * 0.42D);
        hasImpulse = true;
        if (distance > arenaRadius + 12.0D) {
            setPos(arenaCenterX + nx * (arenaRadius - 2.0D), getY(),
                    arenaCenterZ + nz * (arenaRadius - 2.0D));
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, 1);
        this.entityData.define(DATA_DRAGON_FLYING, false);
    }

    public int getCurrentPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    public boolean isDragonFlying() {
        return this.entityData.get(DATA_DRAGON_FLYING);
    }

    private void setDragonFlying(boolean flying) {
        dragonFlying = flying;
        this.entityData.set(DATA_DRAGON_FLYING, flying);
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
                if (isDragonFlying()) return state.setAndContinue(DRAGON_FLY_ANIM);
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
                .triggerableAnim("drunken_sway", DRUNKEN_SWAY_ANIM)
                .triggerableAnim("drunken_strike", DRUNKEN_STRIKE_ANIM)
                .triggerableAnim("kanabo_combo", KANABO_COMBO_ANIM)
                .triggerableAnim("hybrid_transform", HYBRID_TRANSFORM_ANIM)
                .triggerableAnim("dragon_transform", DRAGON_TRANSFORM_ANIM)
                .triggerableAnim("dragon_breath", DRAGON_BREATH_ANIM)
                .triggerableAnim("hit_react", HIT_REACT_ANIM)
                .triggerableAnim("death", DEATH_ANIM)
                .triggerableAnim("ragnarok", RAGNAROK_ANIM)
                .triggerableAnim("thunder_bagua", THUNDER_BAGUA_ANIM)
                .triggerableAnim("blast_breath", BLAST_BREATH_ANIM)
                .triggerableAnim("dragon_takeoff", DRAGON_TAKEOFF_ANIM)
                .triggerableAnim("dragon_land", DRAGON_LAND_ANIM)
                .triggerableAnim("dragon_skyfall", DRAGON_SKYFALL_ANIM)
                .triggerableAnim("dragon_twister", DRAGON_TWISTER_ANIM)
                .triggerableAnim("conquerors_haki_burst", CONQUERORS_HAKI_ANIM));
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
            kanaboComboStep = (kanaboComboStep + 1) % 4;
            // Kaido's drunken style deliberately changes rhythm between strikes.
            if (getCurrentPhase() >= 2 && kanaboComboStep == 0) {
                triggerAnim("attack", "drunken_sway");
            } else if (getCurrentPhase() >= 2 && kanaboComboStep == 2) {
                triggerAnim("attack", "drunken_strike");
                if (target instanceof LivingEntity victim && victim.isAlive()) {
                    victim.hurt(damageSources().mobAttack(this), 16.0F);
                    Vec3 push = victim.position().subtract(position()).normalize();
                    victim.push(push.x * 0.9D, 0.45D, push.z * 0.9D);
                    victim.hurtMarked = true;
                }
            } else if (kanaboComboStep == 3) {
                triggerAnim("attack", "kanabo_combo");
            } else {
                triggerAnim("attack", "kanabo");
            }
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
            if (skyfallCooldown > 0) skyfallCooldown--;
            if (dragonTwisterCooldown > 0) dragonTwisterCooldown--;
            if (conquerorsHakiCooldown > 0) conquerorsHakiCooldown--;
            if (tickCount % 5 == 0) {
                float progress = (float) Math.max(0.0D, Math.min(1.0D,
                        getHealth() / getMaxHealth()));
                bossEvent.setProgress(progress);
                updatePhase();
            }

            tickArenaBoundary();
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
                setDragonFlying(false);
                setNoGravity(false);
                triggerAnim("attack", "dragon_land");
            }
            return;
        }

        double distanceSqr = distanceToSqr(target);
        if (!dragonFlying && distanceSqr > 144.0D) {
            setDragonFlying(true);
            setNoGravity(true);
            getNavigation().stop();
            triggerAnim("attack", "dragon_takeoff");
        } else if (dragonFlying && distanceSqr < 49.0D && pendingAttack != DRAGON_SKYFALL_ATTACK) {
            setDragonFlying(false);
            setNoGravity(false);
            triggerAnim("attack", "dragon_land");
            return;
        }

        if (!dragonFlying) return;

        getNavigation().stop();

        // During Dragon Twister Kaido hovers in place while a visible vortex
        // builds around his body. This prevents the flight steering from
        // dragging the spinning animation away from its attack position.
        if (pendingAttack == DRAGON_TWISTER_ATTACK) {
            Vec3 velocity = getDeltaMovement();
            setDeltaMovement(velocity.x * 0.55D, velocity.y * 0.55D, velocity.z * 0.55D);
            hasImpulse = true;
            if (tickCount % 2 == 0 && level() instanceof ServerLevel serverLevel) {
                double angle = tickCount * 0.42D;
                for (int i = 0; i < 3; i++) {
                    double swirlAngle = angle + i * (Math.PI * 2.0D / 3.0D);
                    double radius = 2.0D + (i % 2) * 0.65D;
                    double x = getX() + Math.cos(swirlAngle) * radius;
                    double z = getZ() + Math.sin(swirlAngle) * radius;
                    double y = getY() + 1.0D + i * 1.1D;
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            x, y, z, 5, 0.25D, 0.35D, 0.25D, 0.035D);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            x, y, z, 2, 0.12D, 0.2D, 0.12D, 0.04D);
                }
            }
            return;
        }

        Vec3 destination = pendingAttack == DRAGON_SKYFALL_ATTACK
                ? skyfallTarget : target.position().add(0.0D, 5.0D, 0.0D);
        Vec3 difference = destination.subtract(position());
        Vec3 desiredVelocity = difference.normalize().scale(
                pendingAttack == DRAGON_SKYFALL_ATTACK ? 0.82D : 0.22D);
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
                else if (pendingAttack == DRAGON_SKYFALL_ATTACK) applyDragonSkyfallDamage();
                else if (pendingAttack == DRAGON_TWISTER_ATTACK) applyDragonTwisterDamage();
                else if (pendingAttack == CONQUERORS_HAKI_ATTACK) applyConquerorsHakiBurstDamage();
            }
        }

        if (attackLockTicks > 0) attackLockTicks--;
        if (attackLockTicks == 0) {
            pendingAttack = NO_ATTACK;
        }
    }


    /**
     * Gives nearby players a readable warning before a signature attack.
     * The warning ring is visual only and never changes blocks.
     */
    private void announceAttackWarning(String message, Vec3 center, double radius) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        double alertRadius = radius + 18.0D;
        AABB alertArea = new AABB(center.x - alertRadius, center.y - 12.0D, center.z - alertRadius,
                center.x + alertRadius, center.y + 18.0D, center.z + alertRadius);
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, alertArea)) {
            if (player.distanceToSqr(center) <= alertRadius * alertRadius) {
                player.displayClientMessage(Component.literal("§c§l⚠ " + message), false);
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS, SoundSource.HOSTILE, 0.8F, 0.65F);
            }
        }
        for (int point = 0; point < 32; point++) {
            double angle = Math.PI * 2.0D * point / 32.0D;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    x, center.y + 0.18D, z, 3, 0.12D, 0.08D, 0.12D, 0.025D);
        }
    }

    private void runBossCombat() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;

        // Keep the boss facing its opponent before choosing a directional attack.
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distanceSqr = distanceToSqr(target);

        // Kaido unleashes Conqueror's Haki as a close-to-mid range pressure burst.
        // The cooldown prevents the aura attack from crowding out his signature moves.
        if (currentPhase >= 2 && distanceSqr >= 16.0D && distanceSqr <= 196.0D
                && conquerorsHakiCooldown <= 0) {
            startConquerorsHakiBurst();
            conquerorsHakiCooldown = currentPhase == 3 ? 420 : 360;
            return;
        }

        // In dragon form Kaido alternates his aerial finishers instead of
        // repeatedly choosing the first available move in the same order.
        if (currentPhase == 3 && isDragonFlying()
                && distanceSqr >= 49.0D && distanceSqr <= 256.0D) {
            boolean preferTwister = aerialComboStep % 2 == 0;
            if ((preferTwister && dragonTwisterCooldown <= 0) || skyfallCooldown > 0) {
                if (dragonTwisterCooldown <= 0) {
                    startDragonTwisterAttack();
                    dragonTwisterCooldown = 420;
                    aerialComboStep++;
                    return;
                }
            }
            if (skyfallCooldown <= 0) {
                startDragonSkyfallAttack(target);
                skyfallCooldown = 360;
                aerialComboStep++;
                return;
            }
            if (dragonTwisterCooldown <= 0) {
                startDragonTwisterAttack();
                dragonTwisterCooldown = 420;
                aerialComboStep++;
                return;
            }
        }
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
        announceAttackWarning("DRACHENATEM – RAUS AUS DER FEUERLINIE!", position(), 4.0D);
        triggerAnim("attack", currentPhase == 3 ? "dragon_breath" : "breath");
    }

    private void startSlamAttack() {
        pendingAttack = SLAM_ATTACK;
        attackImpactDelay = 12; // 0.6 seconds: impact at the downward strike
        attackLockTicks = 18;   // 0.9-second animation lock
        announceAttackWarning("KANABO-SCHMETTERSCHLAG – ABSTAND HALTEN!", position(), 7.0D);
        triggerAnim("attack", "slam");
    }

    private void startRagnarokAttack() {
        pendingAttack = RAGNAROK_ATTACK;
        attackImpactDelay = 24; // 1.2-second charge-up before the overhead strike
        attackLockTicks = 36;   // 1.8-second recovery window
        announceAttackWarning("RAGNAROK – FLÄCHENANGRIFF! SOFORT AUSWEICHEN!", position(), 11.0D);
        triggerAnim("attack", "ragnarok");
    }

    private void startThunderBaguaAttack() {
        pendingAttack = THUNDER_BAGUA_ATTACK;
        attackImpactDelay = 16; // 0.8-second wind-up before the lightning-fast strike
        attackLockTicks = 30;   // 1.5-second total attack window
        LivingEntity target = getTarget();
        if (target != null) announceAttackWarning("THUNDER BAGUA – KAIDO STÜRMT DEIN ZIEL!", target.position(), 3.5D);
        triggerAnim("attack", "thunder_bagua");
    }

    private void startBlastBreathAttack() {
        pendingAttack = BLAST_BREATH_ATTACK;
        attackImpactDelay = 24; // 1.2-second dragon breath charge-up
        attackLockTicks = 40;   // 2-second attack and recovery window
        LivingEntity target = getTarget();
        if (target != null) announceAttackWarning("BLAST BREATH – VERLASSE DIE FEUERBAHN!", target.position(), 5.0D);
        triggerAnim("attack", "blast_breath");
    }

    private void startDragonSkyfallAttack(LivingEntity target) {
        skyfallTarget = target.position();
        pendingAttack = DRAGON_SKYFALL_ATTACK;
        attackImpactDelay = 18; // dive wind-up and descent
        attackLockTicks = 42;   // impact plus recovery
        announceAttackWarning("DRAGON SKYFALL – EINSCHLAGSPUNKT VERLASSEN!", skyfallTarget, 8.5D);
        triggerAnim("attack", "dragon_skyfall");
    }

    private void startConquerorsHakiBurst() {
        pendingAttack = CONQUERORS_HAKI_ATTACK;
        attackImpactDelay = 22; // 1.1-second pressure build-up before the burst
        attackLockTicks = 38;   // recovery window after the shockwave
        getNavigation().stop();
        announceAttackWarning("KÖNIGSHAKI – DER DRUCKWELLENRING KOMMT!", position(),
                currentPhase == 3 ? 15.0D : 11.0D);
        triggerAnim("attack", "conquerors_haki_burst");
    }

    private void applyConquerorsHakiBurstDamage() {
        double radius = currentPhase == 3 ? 15.0D : 11.0D;
        float damage = currentPhase == 3 ? 82.0F : 58.0F;
        AABB area = getBoundingBox().inflate(radius, 5.0D, radius);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    getX(), getY() + getBbHeight() * 0.55D, getZ(), 1,
                    0.4D, 0.4D, 0.4D, 0.0D);
            for (int ring = 1; ring <= 4; ring++) {
                double ringRadius = radius * ring / 4.0D;
                for (int point = 0; point < 32; point++) {
                    double angle = Math.PI * 2.0D * point / 32.0D + tickCount * 0.025D;
                    double x = getX() + Math.cos(angle) * ringRadius;
                    double z = getZ() + Math.sin(angle) * ringRadius;
                    double y = getY() + 0.25D + (point % 4) * 0.45D;
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            x, y, z, 3, 0.18D, 0.35D, 0.18D, 0.06D);
                    if (point % 2 == 0) {
                        serverLevel.sendParticles(ParticleTypes.CRIT,
                                x, y + 0.35D, z, 2, 0.12D, 0.25D, 0.12D, 0.08D);
                    }
                }
            }
        }

        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity.isAlive())) {
            double distance = victim.position().distanceTo(position());
            if (distance <= radius) {
                victim.hurt(damageSources().mobAttack(this), damage);
                Vec3 away = victim.position().subtract(position()).normalize();
                double strength = Math.max(0.8D, (radius - distance) * 0.24D);
                victim.push(away.x * strength, 0.65D + strength * 0.22D, away.z * strength);
                victim.hurtMarked = true;
            }
        }
    }

    private void startDragonTwisterAttack() {
        pendingAttack = DRAGON_TWISTER_ATTACK;
        attackImpactDelay = 20;
        attackLockTicks = 44;
        announceAttackWarning("DRAGON TWISTER – WIRBELSTURM-BEREICH VERLASSEN!", position(), 12.0D);
        triggerAnim("attack", "dragon_twister");
    }

    private void applyDragonTwisterDamage() {
        double radius = 12.0D;
        AABB area = getBoundingBox().inflate(radius, 7.0D, radius);
        if (level() instanceof ServerLevel serverLevel) {
            for (int ring = 1; ring <= 5; ring++) {
                double ringRadius = ring * 2.2D;
                for (int point = 0; point < 32; point++) {
                    double angle = (Math.PI * 2.0D * point) / 32.0D + tickCount * 0.12D;
                    double x = getX() + Math.cos(angle) * ringRadius;
                    double z = getZ() + Math.sin(angle) * ringRadius;
                    double y = getY() + 1.0D + (point % 8) * 0.55D;
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            x, y, z, 3, 0.35D, 0.45D, 0.35D, 0.08D);
                    if (point % 2 == 0) {
                        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                x, y, z, 2, 0.2D, 0.35D, 0.2D, 0.08D);
                    }
                }
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    getX(), getY() + 1.0D, getZ(), 12, 3.0D, 1.2D, 3.0D, 0.12D);
        }

        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity.isAlive())) {
            double distance = victim.distanceTo(this);
            if (distance <= radius) {
                victim.hurt(damageSources().mobAttack(this), 48.0F);
                Vec3 away = victim.position().subtract(position()).normalize();
                double force = Math.max(1.0D, (radius - distance) * 0.22D);
                victim.push(away.x * force, 1.1D + force * 0.25D, away.z * force);
                victim.hurtMarked = true;
            }
        }
    }

    private void applyDragonSkyfallDamage() {
        double radius = 8.5D;
        Vec3 impact = skyfallTarget;
        AABB impactArea = new AABB(
                impact.x - radius, impact.y - 3.0D, impact.z - radius,
                impact.x + radius, impact.y + 4.0D, impact.z + radius
        );

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    impact.x, impact.y + 0.4D, impact.z, 2, 1.0D, 0.4D, 1.0D, 0.0D);
            for (int i = 0; i < 48; i++) {
                double angle = (Math.PI * 2.0D * i) / 48.0D;
                double ringRadius = radius * (0.35D + (i % 4) * 0.16D);
                double x = impact.x + Math.cos(angle) * ringRadius;
                double z = impact.z + Math.sin(angle) * ringRadius;
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        x, impact.y + 0.15D, z, 3, 0.3D, 0.18D, 0.3D, 0.08D);
                if (i % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            x, impact.y + 0.7D, z, 3, 0.2D, 0.6D, 0.2D, 0.12D);
                }
            }
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    impact.x, impact.y + 0.5D, impact.z,
                    65, 2.8D, 0.8D, 2.8D, 0.08D);
        }

        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, impactArea,
                entity -> entity != this && entity.isAlive())) {
            double distance = victim.position().distanceTo(impact);
            if (distance <= radius) {
                victim.hurt(damageSources().mobAttack(this), 85.0F);
                double strength = Math.max(0.7D, (radius - distance) * 0.32D);
                Vec3 push = victim.position().subtract(impact).normalize();
                victim.push(push.x * strength, 1.0D + strength * 0.35D, push.z * strength);
                victim.hurtMarked = true;
            }
        }

        setDragonFlying(false);
        setNoGravity(false);
        setDeltaMovement(getDeltaMovement().x, Math.max(0.0D, getDeltaMovement().y), getDeltaMovement().z);
        hasImpulse = true;
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

    /**
     * Temporary server-side reward implementation using renamed vanilla items.
     * The dedicated NEXUS-BAY item registry can replace these placeholders later.
     */
    private void dropKaidoRewards() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        // Every player who entered the Onigashima arena receives a participation reward.
        for (UUID participantId : arenaParticipants) {
            ServerPlayer participant = serverLevel.getServer().getPlayerList().getPlayer(participantId);
            if (participant != null) {
                giveParticipationReward(participant);
                participant.displayClientMessage(
                        Component.literal("§6§lKAIDO BESIEGT! §eDeine Teilnahme-Belohnung wurde ausgegeben."),
                        false);
            }
        }

        // The killer receives the original world-boss loot roll and guaranteed trophy.
        LivingEntity killer = getKillCredit();
        if (killer == null) return;

        dropNamedReward(serverLevel, Items.PRISMARINE_SHARD,
                "§bKaidos Drachenschuppe", 1 + random.nextInt(4), 0.65D);
        dropNamedReward(serverLevel, Items.BLAZE_ROD,
                "§6Kanabo-Splitter", 1, 0.25D);
        dropNamedReward(serverLevel, Items.TRIPWIRE_HOOK,
                "§eWeltboss-Schlüssel", 1, 0.015D);
        dropNamedReward(serverLevel, Items.CHORUS_FRUIT,
                "§dAzurblauer Drachenfrucht-Kern", 1, 0.005D);

        ItemStack trophy = new ItemStack(Items.NETHER_STAR);
        trophy.setHoverName(Component.literal("§cTrophäe: Kaido, König der Bestien"));
        spawnAtLocation(trophy);
    }

    private void giveParticipationReward(ServerPlayer player) {
        ItemStack scales = new ItemStack(Items.PRISMARINE_SHARD, 2);
        scales.setHoverName(Component.literal("§bKaidos Drachenschuppe"));
        ItemStack fragment = new ItemStack(Items.BLAZE_ROD);
        fragment.setHoverName(Component.literal("§6Kanabo-Splitter"));
        giveOrDrop(player, scales);
        giveOrDrop(player, fragment);
    }

    private void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private void dropNamedReward(ServerLevel serverLevel, net.minecraft.world.item.Item item,
                                 String name, int count, double chance) {
        if (random.nextDouble() > chance) return;
        ItemStack stack = new ItemStack(item, count);
        stack.setHoverName(Component.literal(name));
        spawnAtLocation(stack);
    }

    /**
     * Server-side cinematic death burst. All effects are particles only:
     * the island terrain is never modified by Kaido's death.
     */
    private void playDeathSequence() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        double height = getBbHeight();
        double radius = getCurrentPhase() == 3 ? 8.0D : 4.5D;
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                getX(), getY() + height * 0.45D, getZ(), 3,
                radius * 0.25D, height * 0.2D, radius * 0.25D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.FLAME,
                getX(), getY() + height * 0.4D, getZ(), 120,
                radius * 0.55D, height * 0.35D, radius * 0.55D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                getX(), getY() + height * 0.55D, getZ(), 90,
                radius * 0.5D, height * 0.4D, radius * 0.5D, 0.035D);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                getX(), getY() + height * 0.5D, getZ(), 100,
                radius * 0.65D, height * 0.45D, radius * 0.65D, 0.12D);

        // A broad, expanding-looking ring marks the defeat without block damage.
        for (int point = 0; point < 64; point++) {
            double angle = Math.PI * 2.0D * point / 64.0D;
            double x = getX() + Math.cos(angle) * radius;
            double z = getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    x, getY() + 0.15D, z, 4, 0.25D, 0.12D, 0.25D, 0.045D);
            if (point % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        x, getY() + 0.7D, z, 3, 0.18D, 0.3D, 0.18D, 0.08D);
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        pendingAttack = NO_ATTACK;
        attackImpactDelay = 0;
        attackLockTicks = 0;
        dragonFlying = false;
        setNoGravity(false);
        if (!level().isClientSide) {
            triggerAnim("attack", "death");
            playDeathSequence();
            dropKaidoRewards();
        }
        bossEvent.removeAllPlayers();
        super.die(source);
    }
}