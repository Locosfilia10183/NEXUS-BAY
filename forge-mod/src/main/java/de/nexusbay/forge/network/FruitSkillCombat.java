package de.nexusbay.forge.network;

import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.progression.FruitSkillCatalog;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative fruit skill combat with fruit- and skill-specific effects. */
public final class FruitSkillCombat {
 private FruitSkillCombat(){}
 public static void cycle(ServerPlayer player){
  FruitProfileData.Profile profile=FruitProfileData.get(player.serverLevel()).get(player.getUUID());
  if(profile==null)return;
  int count=FruitSkillCatalog.unlocked(profile.fruitId(),profile.level()).size();
  if(count<2){player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Noch keine weitere Fähigkeit freigeschaltet."));return;}
  int next=Math.floorMod(player.getPersistentData().getInt("NexusFruitSkillIndex")+1,count);
  player.getPersistentData().putInt("NexusFruitSkillIndex",next);
  player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Ausgewählte Fähigkeit: "+FruitSkillCatalog.unlocked(profile.fruitId(),profile.level()).get(next).name()));
 }
 public static void activate(ServerPlayer player){
  FruitProfileData.Profile profile=FruitProfileData.get(player.serverLevel()).get(player.getUUID());
  if(profile==null)return;
  var skills=FruitSkillCatalog.unlocked(profile.fruitId(),profile.level());
  if(skills.isEmpty())return;
  int selected=Math.floorMod(player.getPersistentData().getInt("NexusFruitSkillIndex"),skills.size());
  var skill=skills.get(selected);
  var item=ModItems.FRUIT_ITEMS.get(profile.fruitId());
  if(item==null||player.getCooldowns().isOnCooldown(item.get()))return;
  player.getCooldowns().addCooldown(item.get(),skill.cooldownTicks());
  Vec3 look=player.getLookAngle().normalize();
  boolean ultimate=switch(skill.id()){
   case "gear_second","raigo","flame_emperor","azure_dragon","Homies Sturm","Yasakani","Magmaeruption","Eiszeit","Fadenkäfig","ROOM","Drachenbrüllen" -> true;
   default -> false;
  };
  double reach=ultimate?9.0D:6.0D;
  double radius=ultimate?4.5D:1.6D;
  AABB area=player.getBoundingBox().expandTowards(look.scale(reach)).inflate(radius);
  float damage=Math.min(45.0F,6.0F+profile.level()*0.30F+(ultimate?12.0F:0.0F));
  if(skill.id().equals("gear_second")||skill.id().equals("azure_dragon"))player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,240,1));
  if(skill.id().equals("Klingenhaut")||skill.id().equals("Schwergewicht"))player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,160,1));
  if(skill.id().equals("ROOM")||skill.id().equals("Seelenraub"))player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,120,1));
  if(skill.id().equals("lightning_travel")||skill.id().equals("Lichttritt")||skill.id().equals("Sprungfeder")){
   player.setDeltaMovement(player.getDeltaMovement().add(look.scale(1.35D)));
   player.hurtMarked=true;
  }
  int hits=0;
  for(LivingEntity target:player.serverLevel().getEntitiesOfClass(LivingEntity.class,area,e->e!=player&&e.isAlive()&&!e.isAlliedTo(player))){
   if(target.position().distanceTo(player.position())>reach+radius)continue;
   target.hurt(player.damageSources().playerAttack(player),damage);
   switch(profile.fruitId()){
    case "mera_mera","magu_magu","uo_uo_seiryu" -> target.setSecondsOnFire(ultimate?8:4);
    case "goro_goro","pika_pika" -> {target.knockback(ultimate?1.8D:1.2D,player.getX()-target.getX(),player.getZ()-target.getZ());target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,1));}
    case "gomu_gomu","bane_bane","supa_supa","bara_bara","bomu_bomu" -> target.knockback(ultimate?2.0D:1.3D,player.getX()-target.getX(),player.getZ()-target.getZ());
    case "hie_hie","yomi_yomi" -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,ultimate?180:100,ultimate?3:1));
    case "ori_ori","doru_doru","ito_ito" -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,160,2));
    case "kilo_kilo" -> {target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,120,3));target.knockback(0.5D,player.getX()-target.getX(),player.getZ()-target.getZ());}
    case "sube_sube" -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,1));
    case "ope_ope" -> {target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,140,2));target.addEffect(new MobEffectInstance(MobEffects.GLOWING,100,0));}
    case "soru_soru" -> target.addEffect(new MobEffectInstance(MobEffects.WITHER,ultimate?100:50,1));
    default -> {}
   }
   hits++;
  }
  var particle=switch(profile.fruitId()){
   case "mera_mera","magu_magu" -> ParticleTypes.FLAME;
   case "goro_goro","pika_pika" -> ParticleTypes.ELECTRIC_SPARK;
   case "hie_hie","yomi_yomi" -> ParticleTypes.SNOWFLAKE;
   case "uo_uo_seiryu" -> ParticleTypes.DRAGON_BREATH;
   case "doru_doru","ori_ori","ito_ito" -> ParticleTypes.WAX_ON;
   default -> ParticleTypes.CRIT;
  };
  player.serverLevel().sendParticles(particle,player.getX()+look.x*2,player.getY()+1.0D+look.y,player.getZ()+look.z*2,ultimate?90:32,ultimate?1.8D:0.8D,ultimate?1.2D:0.6D,ultimate?1.8D:0.8D,0.08D);
  player.sendSystemMessage(net.minecraft.network.chat.Component.literal(skill.name()+"! Treffer: "+hits));
 }
}
