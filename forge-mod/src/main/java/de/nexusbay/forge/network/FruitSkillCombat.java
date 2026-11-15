package de.nexusbay.forge.network;

import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.progression.FruitSkillCatalog;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative first combat implementation for unlocked fruit skills. */
public final class FruitSkillCombat {
 private FruitSkillCombat(){}
 public static void activate(ServerPlayer player){
  FruitProfileData.Profile profile=FruitProfileData.get(player.serverLevel()).get(player.getUUID());
  if(profile==null)return;
  var skills=FruitSkillCatalog.unlocked(profile.fruitId(),profile.level());
  if(skills.isEmpty())return;
  var skill=skills.get(skills.size()-1);
  var item=ModItems.FRUIT_ITEMS.get(profile.fruitId());
  if(item==null||player.getCooldowns().isOnCooldown(item.get()))return;
  player.getCooldowns().addCooldown(item.get(),skill.cooldownTicks());
  Vec3 look=player.getLookAngle().normalize();
  AABB area=player.getBoundingBox().expandTowards(look.scale(6.0D)).inflate(1.6D);
  float damage=Math.min(30.0F,6.0F+profile.level()*0.25F);
  int hits=0;
  for(LivingEntity target:player.serverLevel().getEntitiesOfClass(LivingEntity.class,area,e->e!=player&&e.isAlive()&&!e.isAlliedTo(player))){
   if(target.position().distanceTo(player.position())>7.0D)continue;
   target.hurt(player.damageSources().playerAttack(player),damage);
   switch(profile.fruitId()){
    case "mera_mera" -> target.setSecondsOnFire(4);
    case "goro_goro" -> {target.knockback(1.2D,player.getX()-target.getX(),player.getZ()-target.getZ());target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,50,1));}
    case "gomu_gomu" -> target.knockback(1.5D,player.getX()-target.getX(),player.getZ()-target.getZ());
    default -> {}
   }
   hits++;
  }
  player.serverLevel().sendParticles(
   profile.fruitId().equals("mera_mera")?net.minecraft.core.particles.ParticleTypes.FLAME:
    profile.fruitId().equals("goro_goro")?net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK:net.minecraft.core.particles.ParticleTypes.CRIT,
   player.getX()+look.x*2,player.getY()+1.0D+look.y,player.getZ()+look.z*2,24,0.8D,0.6D,0.8D,0.08D);
  player.sendSystemMessage(net.minecraft.network.chat.Component.literal(skill.name()+"! Treffer: "+hits));
 }
}
