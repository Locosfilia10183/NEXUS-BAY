package de.nexusbay.forge.network;

import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.event.CombatStaminaEvents;
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
  if(player.getInventory().selected!=0)return;
  FruitProfileData.Profile profile=FruitProfileData.get(player.serverLevel()).get(player.getUUID());
  if(profile==null)return;
  int count=FruitSkillCatalog.unlocked(profile.fruitId(),profile.level()).size();
  if(count<2){player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Noch keine weitere Fähigkeit freigeschaltet."));return;}
  int next=Math.floorMod(player.getPersistentData().getInt("NexusFruitSkillIndex")+1,count);
  player.getPersistentData().putInt("NexusFruitSkillIndex",next);
  player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Ausgewählte Fähigkeit: "+FruitSkillCatalog.unlocked(profile.fruitId(),profile.level()).get(next).name()));
 }
 public static void activate(ServerPlayer player){
  if(player.getInventory().selected!=0)return;
  FruitProfileData.Profile profile=FruitProfileData.get(player.serverLevel()).get(player.getUUID());
  if(profile==null)return;
  var skills=FruitSkillCatalog.unlocked(profile.fruitId(),profile.level());
  if(skills.isEmpty())return;
  int selected=Math.floorMod(player.getPersistentData().getInt("NexusFruitSkillIndex"),skills.size());
  var skill=skills.get(selected);
  var item=ModItems.FRUIT_ITEMS.get(profile.fruitId());
  if(item==null)return;
  String cooldownKey="NexusSkillReady_"+skill.id().replaceAll("[^A-Za-z0-9_]","_");
  long now=player.serverLevel().getGameTime();
  long readyAt=player.getPersistentData().getLong(cooldownKey);
  if(now<readyAt){
   long seconds=(readyAt-now+19L)/20L;
   player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Fähigkeit lädt noch: "+seconds+"s").withStyle(net.minecraft.ChatFormatting.GRAY));
   return;
  }
  boolean ultimate=switch(skill.id()){
   case "gear_second","gear_third","gear_fourth_boundman","gear_fourth_snake_man","gear_fourth_tankman","raigo","flame_emperor","azure_dragon","Homies Sturm","Yasakani","Magmaeruption","Eiszeit","Fadenkäfig","ROOM","Drachenbrüllen" -> true;
   default -> false;
  };
  float staminaCost=ultimate?35.0F:15.0F;
  if(!CombatStaminaEvents.spend(player,staminaCost)){
   player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
    "Nicht genug Ausdauer! Benötigt: "+(int)staminaCost).withStyle(net.minecraft.ChatFormatting.RED));
   return;
  }
  player.getPersistentData().putLong(cooldownKey,now+skill.cooldownTicks());\n  FruitNetwork.syncAnimation(player, switch(skill.id()){\n   case "gear_second" -> "gear_second_punch";\n   case "gear_third" -> "gear_third_slam";\n   case "gear_fourth_boundman" -> "boundman_slam";\n   case "gear_fourth_snake_man" -> "snakeman_combo";\n   case "gear_fourth_tankman" -> "tankman_bounce";\n   default -> "";\n  },14);\n
  Vec3 look=player.getLookAngle().normalize();
  boolean beam=switch(skill.id()){case "pistol","lightning_bolt","fire_fist","dragon_roar","Yasakani","Lichttritt","Eislanze","Magmaschlag" -> true;default -> false;};
  boolean areaBurst=switch(skill.id()){case "gatling","raigo","flame_emperor","azure_dragon","Homies Sturm","Magmaeruption","Eiszeit","Fadenkäfig","Bombenfeld","Körperregen" -> true;default -> false;};
  double reach=beam?18.0D:ultimate?10.0D:6.0D;
  double radius=areaBurst?5.0D:beam?1.25D:ultimate?3.5D:1.6D;
  AABB area=player.getBoundingBox().expandTowards(look.scale(reach)).inflate(radius);
  float damage=Math.min(45.0F,6.0F+profile.level()*0.30F+(ultimate?12.0F:0.0F));
  if(skill.id().equals("gear_second")){
   player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,240,2));
   player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,240,1));
   player.getPersistentData().putInt("NexusFruitFormTicks",240);
   player.getPersistentData().putString("NexusFruitForm","gear_second");
   FruitNetwork.syncForm(player,"gear_second",240);
   player.sendSystemMessage(net.minecraft.network.chat.Component.literal("GEAR SECOND!").withStyle(net.minecraft.ChatFormatting.RED,net.minecraft.ChatFormatting.BOLD));
  }
  if(skill.id().equals("gear_third")){
   player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,300,2));
   player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,300,1));
   player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST,300,2));
   player.getPersistentData().putInt("NexusFruitFormTicks",300);
   player.getPersistentData().putString("NexusFruitForm","gear_third");
   FruitNetwork.syncForm(player,"gear_third",300);
   player.sendSystemMessage(net.minecraft.network.chat.Component.literal("GEAR THIRD – RIESENFORM!").withStyle(net.minecraft.ChatFormatting.GOLD,net.minecraft.ChatFormatting.BOLD));
  }
  if(skill.id().equals("gear_fourth_boundman")){
   player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,360,2));
   player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,360,3));
   player.addEffect(new MobEffectInstance(MobEffects.JUMP,360,1));
   player.getPersistentData().putInt("NexusFruitFormTicks",360);
   player.getPersistentData().putString("NexusFruitForm","gear_fourth_boundman");
   FruitNetwork.syncForm(player,"gear_fourth_boundman",360);
   player.sendSystemMessage(net.minecraft.network.chat.Component.literal("GEAR FOURTH: BOUNDMAN!").withStyle(net.minecraft.ChatFormatting.RED,net.minecraft.ChatFormatting.BOLD));
  }
  if(skill.id().equals("gear_fourth_snake_man")){
   player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,300,4));
   player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,300,2));
   player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,300,1));
   player.getPersistentData().putInt("NexusFruitFormTicks",300);
   player.getPersistentData().putString("NexusFruitForm","gear_fourth_snake_man");
   FruitNetwork.syncForm(player,"gear_fourth_snake_man",300);
   player.sendSystemMessage(net.minecraft.network.chat.Component.literal("GEAR FOURTH: SNAKEMAN!").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE,net.minecraft.ChatFormatting.BOLD));
  }
  if(skill.id().equals("gear_fourth_tankman")){
   player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,400,3));
   player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,400,3));
   player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST,400,3));
   player.getPersistentData().putInt("NexusFruitFormTicks",400);
   player.getPersistentData().putString("NexusFruitForm","gear_fourth_tankman");
   FruitNetwork.syncForm(player,"gear_fourth_tankman",400);
   player.sendSystemMessage(net.minecraft.network.chat.Component.literal("GEAR FOURTH: TANKMAN!").withStyle(net.minecraft.ChatFormatting.GOLD,net.minecraft.ChatFormatting.BOLD));
  }
  if(skill.id().equals("azure_dragon")){
   player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,400,1));
   player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,400,2));
   player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,400,1));
   player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,400,0));
   player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,400,0));
   player.getPersistentData().putInt("NexusFruitFormTicks",400);
   player.getPersistentData().putString("NexusFruitForm","azure_dragon");
   FruitNetwork.syncForm(player,"azure_dragon",400);
   player.sendSystemMessage(net.minecraft.network.chat.Component.literal("HIMMELSDRACHEN-FORM AKTIV!").withStyle(net.minecraft.ChatFormatting.AQUA,net.minecraft.ChatFormatting.BOLD));
  }
  if(skill.id().equals("Klingenhaut")||skill.id().equals("Schwergewicht"))player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,160,1));
  if(skill.id().equals("ROOM")||skill.id().equals("Seelenraub"))player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,120,1));
  if(skill.id().equals("Feuerfliegen")||skill.id().equals("firefly"))player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,100,1));
  if(skill.id().equals("Wachsfessel")||skill.id().equals("Fadenkäfig")||skill.id().equals("Eisenkäfig"))player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,0));
  if(skill.id().equals("Gleitsturm")){player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,120,2));player.addEffect(new MobEffectInstance(MobEffects.JUMP,120,1));}
  if(skill.id().equals("Flammenkaiser")||skill.id().equals("Magmaeruption")||skill.id().equals("Raigo"))player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,100,1));
  if(skill.id().equals("Frostseele")||skill.id().equals("Eiszeit"))player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,120,1));
  if(skill.id().equals("lightning_travel")||skill.id().equals("Lichttritt")||skill.id().equals("Sprungfeder")){
   player.setDeltaMovement(player.getDeltaMovement().add(look.scale(1.35D)));
   player.hurtMarked=true;
  }
  int hits=0;
  for(LivingEntity target:player.serverLevel().getEntitiesOfClass(LivingEntity.class,area,e->e!=player&&e.isAlive()&&!e.isAlliedTo(player))){
   Vec3 offset=target.position().add(0,target.getBbHeight()*0.5D,0).subtract(player.getEyePosition());
   double along=offset.dot(look);
   if(along<0.0D||along>reach)continue;
   double side=offset.subtract(look.scale(along)).length();
   if(side>radius||(!beam&&!areaBurst&&side>2.0D))continue;
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
  if(beam){for(int step=2;step<=18;step+=2){Vec3 point=player.getEyePosition().add(look.scale(step));player.serverLevel().sendParticles(particle,point.x,point.y,point.z,10,0.35D,0.35D,0.35D,0.02D);}}
  else player.serverLevel().sendParticles(particle,player.getX()+look.x*2,player.getY()+1.0D+look.y,player.getZ()+look.z*2,areaBurst?120:ultimate?90:32,areaBurst?2.8D:ultimate?1.8D:0.8D,areaBurst?2.0D:ultimate?1.2D:0.6D,areaBurst?2.8D:ultimate?1.8D:0.8D,0.08D);
  player.sendSystemMessage(net.minecraft.network.chat.Component.literal(skill.name()+"! Treffer: "+hits));
 }
}
