package de.nexusbay.forge.client;

import de.nexusbay.forge.NexusBayForge;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-side transformation scale and short, synchronized player attack poses. */
@Mod.EventBusSubscriber(modid=NexusBayForge.MOD_ID,value=Dist.CLIENT)
public final class FruitFormClientData {
 private static final Map<UUID,String> FORMS=new ConcurrentHashMap<>();
 private static final Map<UUID,Animation> ANIMATIONS=new ConcurrentHashMap<>();
 private static final ThreadLocal<float[]> ORIGINAL_POSE=new ThreadLocal<>();
 private FruitFormClientData(){}
 private record Animation(String id,long start,int duration){}
 public static void set(UUID playerId,String form,int ticks){
  if(form==null||form.isEmpty()||ticks<=0)FORMS.remove(playerId);
  else FORMS.put(playerId,form);
 }
 public static void animate(UUID playerId,String animation,int duration){
  var level=Minecraft.getInstance().level;
  if(level==null||animation==null||animation.isEmpty()||duration<=0)return;
  ANIMATIONS.put(playerId,new Animation(animation,level.getGameTime(),duration));
 }
 @SubscribeEvent public static void renderPlayer(RenderPlayerEvent.Pre event){
  Player player=event.getEntity();
  String form=FORMS.get(player.getUUID());
  if(form!=null){
   float scale=switch(form){
    case "gear_third" -> 1.28F;
    case "gear_fourth_boundman" -> 1.38F;
    case "gear_fourth_snake_man" -> 1.08F;
    case "gear_fourth_tankman" -> 1.52F;
    case "azure_dragon" -> 1.18F;
    default -> 1.04F;
   };
   event.getPoseStack().scale(scale,scale,scale);
  }
  Animation animation=ANIMATIONS.get(player.getUUID());
  if(animation==null)return;
  var level=Minecraft.getInstance().level;
  if(level==null)return;
  float age=(level.getGameTime()-animation.start())+event.getPartialTick();
  if(age<0||age>=animation.duration()){ANIMATIONS.remove(player.getUUID(),animation);return;}
  PlayerModel<?> model=event.getRenderer().getModel();
  ModelPart right=model.rightArm,left=model.leftArm,body=model.body;
  ORIGINAL_POSE.set(new float[]{right.xRot,right.yRot,right.zRot,left.xRot,left.yRot,left.zRot,body.xRot});
  float t=age/animation.duration();
  float pulse=(float)Math.sin(Math.PI*t);
  switch(animation.id()){
   case "gear_second_punch" -> {right.xRot-=2.2F*pulse;right.zRot-=0.25F*pulse;body.xRot-=0.22F*pulse;}
   case "gear_third_slam" -> {right.xRot=-2.7F*pulse;left.xRot=-2.7F*pulse;body.xRot=0.45F*pulse;}
   case "boundman_slam" -> {right.xRot=-2.5F*pulse;right.zRot=-0.7F*pulse;left.xRot=-1.2F*pulse;body.xRot=0.35F*pulse;}
   case "snakeman_combo" -> {float rapid=(float)Math.sin(age*2.8F);right.xRot=-1.8F*Math.max(0,rapid);left.xRot=-1.8F*Math.max(0,-rapid);body.yRot=0.12F*rapid;}
   case "tankman_bounce" -> {right.xRot=-1.9F*pulse;left.xRot=-1.9F*pulse;right.zRot=-0.65F*pulse;left.zRot=0.65F*pulse;body.xRot=0.6F*pulse;}
   default -> ORIGINAL_POSE.remove();
  }
 }
 @SubscribeEvent public static void restorePlayer(RenderPlayerEvent.Post event){
  float[] pose=ORIGINAL_POSE.get();
  if(pose==null)return;
  PlayerModel<?> model=event.getRenderer().getModel();
  model.rightArm.xRot=pose[0];model.rightArm.yRot=pose[1];model.rightArm.zRot=pose[2];
  model.leftArm.xRot=pose[3];model.leftArm.yRot=pose[4];model.leftArm.zRot=pose[5];
  model.body.xRot=pose[6];
  ORIGINAL_POSE.remove();
 }
}
