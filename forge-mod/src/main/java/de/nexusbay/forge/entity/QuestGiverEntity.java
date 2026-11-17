package de.nexusbay.forge.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Dedicated GeckoLib character used by island quest-giver stations. */
public final class QuestGiverEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Boolean> TALKING =
            SynchedEntityData.defineId(QuestGiverEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(QuestGiverEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.quest_giver.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.quest_giver.walk");
    private static final RawAnimation TALK = RawAnimation.begin().thenPlay("animation.quest_giver.talk");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int talkingTicks;

    public QuestGiverEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TALKING, false);
        this.entityData.define(VARIANT, 0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("NexusBayQuestGiverVariant", getVariant());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("NexusBayQuestGiverVariant"));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.talkingTicks > 0 && --this.talkingTicks == 0) {
            this.entityData.set(TALKING, false);
        }
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, Math.floorMod(variant, 4));
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    /** Starts a short, client-synchronised greeting/interaction gesture. */
    public void startTalking() {
        if (this.level().isClientSide) return;
        this.talkingTicks = 24;
        this.entityData.set(TALKING, true);
    }

    private boolean isTalking() {
        return this.entityData.get(TALKING);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 3, state -> {
            if (isTalking()) return state.setAndContinue(TALK);
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
