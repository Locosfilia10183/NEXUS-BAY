package de.nexusbay.forge.event;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.network.FruitNetwork;
import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.progression.CharacterProgressionData;
import de.nexusbay.forge.progression.FruitSkillCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Monster;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Keeps slot 1 bound to the active fruit and awards mastery for combat. */
@Mod.EventBusSubscriber(modid=NexusBayForge.MOD_ID)
public final class FruitPlayerEvents {
 private FruitPlayerEvents(){}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)FruitNetwork.sync(p);}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;
  int formTicks=p.getPersistentData().getInt("NexusFruitFormTicks");
  if(formTicks>0){
   p.getPersistentData().putInt("NexusFruitFormTicks",formTicks-1);
   if(p.tickCount%5==0){
    String form=p.getPersistentData().getString("NexusFruitForm");
    var particle=switch(form){
     case "azure_dragon" -> net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH;
     case "gear_third","gear_fourth_boundman","gear_fourth_tankman" -> net.minecraft.core.particles.ParticleTypes.CLOUD;
     case "gear_fourth_snake_man" -> net.minecraft.core.particles.ParticleTypes.CRIT;
     default -> net.minecraft.core.particles.ParticleTypes.FLAME;
    };
    p.serverLevel().sendParticles(particle,p.getX(),p.getY()+1.0D,p.getZ(),form.startsWith("gear_fourth")?18:"gear_third".equals(form)?16:8,0.65D,0.8D,0.65D,0.025D);
   }
   if(formTicks==1){
    String endedForm=p.getPersistentData().getString("NexusFruitForm");
    p.getPersistentData().remove("NexusFruitForm");
    if("gear_third".equals(endedForm)||"gear_fourth_tankman".equals(endedForm))p.removeEffect(net.minecraft.world.effect.MobEffects.HEALTH_BOOST);
    p.sendSystemMessage(Component.literal("Deine Verwandlung ist beendet."));
   }
  }
  if(p.tickCount%10!=0)return;
  FruitProfileData.Profile profile=FruitProfileData.get(p.serverLevel()).get(p.getUUID());if(profile==null)return;
  var item=ModItems.FRUIT_ITEMS.get(profile.fruitId());if(item==null)return;
  ItemStack slot=p.getInventory().getItem(0);
  if(!slot.is(item.get())){
   ItemStack fruitStack=ItemStack.EMPTY;
   for(int i=1;i<p.getInventory().getContainerSize();i++){
    ItemStack candidate=p.getInventory().getItem(i);
    if(candidate.is(item.get())){fruitStack=candidate;break;}
   }
   if(!slot.isEmpty()){
    ItemStack displaced=slot.copy();
    if(!p.getInventory().add(displaced))p.drop(displaced,false);
   }
   if(!fruitStack.isEmpty()){
    fruitStack=p.getInventory().removeItem(p.getInventory().findSlotMatchingItem(fruitStack),1);
   }else fruitStack=new ItemStack(item.get());
   p.getInventory().setItem(0,fruitStack);p.getInventory().setChanged();
  }
 }
 @SubscribeEvent public static void kill(LivingDeathEvent e){
  if(!(e.getEntity() instanceof Monster monster)||!(e.getSource().getEntity() instanceof ServerPlayer p))return;
  long characterReward=Math.max(8L,Math.min(80L,8L+(long)(monster.getMaxHealth()/5.0F)));
  var characterBefore=CharacterProgressionData.get(p.serverLevel()).get(p.getUUID());
  var characterAfter=CharacterProgressionData.get(p.serverLevel()).addExperience(p.getUUID(),characterReward);
  p.sendSystemMessage(Component.literal("+"+characterReward+" Charakter-XP").withStyle(ChatFormatting.YELLOW));
  if(characterAfter.level()>characterBefore.level()){
   p.sendSystemMessage(Component.literal("Charakter-Level gestiegen: "+characterBefore.level()+" → "+characterAfter.level()+" | +"+((characterAfter.level()-characterBefore.level())*3)+" Stat-Punkte").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
  }
  FruitProfileData data=FruitProfileData.get(p.serverLevel());
  FruitProfileData.Profile before=data.get(p.getUUID());
  if(before==null)return;
  long reward=Math.max(10L,Math.min(100L,10L+(long)(monster.getMaxHealth()/4.0F)));
  FruitProfileData.Profile updated=data.addExperience(p.getUUID(),reward);
  if(updated==null)return;
  p.sendSystemMessage(Component.literal("+"+reward+" Frucht-Meisterschafts-XP").withStyle(ChatFormatting.LIGHT_PURPLE));
  if(updated.level()>before.level()){
   p.sendSystemMessage(Component.literal("Teufelsfrucht-Level gestiegen: "+before.level()+" → "+updated.level()).withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD));
   var unlocked=FruitSkillCatalog.forFruit(updated.fruitId()).stream()
    .filter(skill->skill.requiredLevel()>before.level()&&skill.requiredLevel()<=updated.level()).toList();
   for(var skill:unlocked)p.sendSystemMessage(Component.literal("Neue Fähigkeit freigeschaltet: "+skill.name()).withStyle(ChatFormatting.GREEN));
  }
  FruitNetwork.sync(p);
 }
}