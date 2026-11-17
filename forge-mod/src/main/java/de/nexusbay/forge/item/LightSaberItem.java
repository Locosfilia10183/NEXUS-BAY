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
    private static final double RANGE = 22.0D;
    private static final double WIDTH = 1.65D;
    private static final float BEAM_DAMAGE = 26.0F;
    private static final int COOLDOWN_TICKS = 100;

    public LightSaberItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (!attacker.level().isClientSide && attacker.level() instanceof net.minecraft.server.level.ServerLevel server) {
            Vec3 point = target.getBoundingBox().getCenter();
            server.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                    12, 0.22D, 0.28D, 0.22D, 0.025D);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                    8, 0.2D, 0.25D, 0.2D, 0.035D);
        }
        return hit;
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
                Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
                // A brief circular charge gathers light around the emitter before release.
                for (int ring = 0; ring < 16; ring++) {
                    double angle = Math.PI * 2.0D * ring / 16.0D;
                    double x = player.getX() + Math.cos(angle) * 0.8D;
                    double z = player.getZ() + Math.sin(angle) * 0.8D;
                    server.sendParticles(ParticleTypes.END_ROD, x, player.getY() + 1.0D, z,
                            2, 0.025D, 0.08D, 0.025D, 0.01D);
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, player.getY() + 1.0D, z,
                            1, 0.04D, 0.06D, 0.04D, 0.02D);
                }
                for (int step = 1; step <= 22; step++) {
                    Vec3 point = start.add(direction.scale(step));
                    server.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                            8, 0.1D, 0.1D, 0.1D, 0.02D);
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                            4, 0.08D, 0.08D, 0.08D, 0.025D);
                }
                // A crescent-shaped burst marks the end of the released light beam.
                for (int arc = 0; arc <= 16; arc++) {
                    double angle = -1.1D + 2.2D * arc / 16.0D;
                    Vec3 point = end.add(side.scale(Math.sin(angle) * 1.8D))
                            .add(0.0D, Math.cos(angle) * 1.2D, 0.0D);
                    server.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                            4, 0.05D, 0.05D, 0.05D, 0.01D);
                    server.sendParticles(ParticleTypes.FIREWORK, point.x, point.y, point.z,
                            2, 0.04D, 0.04D, 0.04D, 0.0D);
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
