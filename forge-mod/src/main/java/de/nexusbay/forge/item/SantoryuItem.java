package de.nexusbay.forge.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * A single weapon item representing Zoro's complete Santoryu style.
 * A successful hit releases a three-cut follow-through against nearby enemies.
 */
public class SantoryuItem extends SwordItem {
    public SantoryuItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(net.minecraft.world.item.ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        Level level = attacker.level();
        if (level.isClientSide) return result;

        Vec3 look = attacker.getLookAngle().normalize();
        Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);
        if (flatLook.lengthSqr() < 1.0E-5D) flatLook = new Vec3(0.0D, 0.0D, 1.0D);
        flatLook = flatLook.normalize();

        AABB area = target.getBoundingBox().inflate(3.25D, 1.25D, 3.25D);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != attacker && entity != target && entity.isAlive());

        for (LivingEntity victim : nearby) {
            Vec3 offset = victim.position().subtract(attacker.position());
            Vec3 flatOffset = new Vec3(offset.x, 0.0D, offset.z);
            double distance = flatOffset.length();
            if (distance > 3.5D || distance < 0.15D) continue;
            double dot = flatLook.dot(flatOffset.normalize());
            if (dot < 0.35D) continue;

            victim.hurt(attacker.damageSources().mobAttack(attacker), 4.0F);
            victim.knockback(0.45D, -flatLook.x, -flatLook.z);
        }

        if (level instanceof ServerLevel serverLevel) {
            Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
            for (int slash = -1; slash <= 1; slash++) {
                Vec3 side = new Vec3(-flatLook.z, 0.0D, flatLook.x).scale(slash * 0.55D);
                Vec3 point = center.add(flatLook.scale(0.7D)).add(side);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        point.x, point.y, point.z, 2, 0.18D, 0.12D, 0.18D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        point.x, point.y, point.z, 5, 0.22D, 0.18D, 0.22D, 0.12D);
            }
        }
        return result;
    }
}
