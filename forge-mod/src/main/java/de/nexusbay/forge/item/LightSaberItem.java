package de.nexusbay.forge.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LightSaberItem extends SwordItem {
    private static final double RANGE = 18.0D;
    private static final double WIDTH = 1.35D;
    private static final float BEAM_DAMAGE = 18.0F;
    private static final int COOLDOWN_TICKS = 80;

    public LightSaberItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            Vec3 start = player.getEyePosition();
            Vec3 direction = player.getLookAngle().normalize();
            Vec3 end = start.add(direction.scale(RANGE));
            AABB corridor = new AABB(start, end).inflate(WIDTH, WIDTH, WIDTH);

            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                for (int step = 1; step <= 18; step++) {
                    Vec3 point = start.add(direction.scale(step));
                    server.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                            7, 0.12D, 0.12D, 0.12D, 0.015D);
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                            3, 0.08D, 0.08D, 0.08D, 0.02D);
                }
                server.sendParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0, 0, 0, 0);
            }

            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, corridor,
                    entity -> entity != player && entity.isAlive())) {
                Vec3 toTarget = target.getBoundingBox().getCenter().subtract(start);
                double along = toTarget.dot(direction);
                if (along <= 0.0D || along > RANGE) continue;
                Vec3 nearest = start.add(direction.scale(along));
                if (target.getBoundingBox().distanceToSqr(nearest) <= WIDTH * WIDTH) {
                    target.hurt(level.damageSources().playerAttack(player), BEAM_DAMAGE);
                    target.knockback(0.65D, -direction.x, -direction.z);
                }
            }

            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.PLAYERS, 1.0F, 1.65F);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
