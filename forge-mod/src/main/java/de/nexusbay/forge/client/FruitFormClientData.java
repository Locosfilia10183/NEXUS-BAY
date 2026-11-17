package de.nexusbay.forge.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import de.nexusbay.forge.NexusBayForge;

/** Client-side visual scale for server-authoritative Devil Fruit forms. */
@Mod.EventBusSubscriber(modid=NexusBayForge.MOD_ID,value=Dist.CLIENT)
public final class FruitFormClientData {
 private static final Map<UUID,String> FORMS=new ConcurrentHashMap<>();
 private FruitFormClientData(){}
 public static void set(UUID playerId,String form,int ticks){
  if(form==null||form.isEmpty()||ticks<=0)FORMS.remove(playerId);
  else FORMS.put(playerId,form);
 }
 @SubscribeEvent public static void renderPlayer(RenderPlayerEvent.Pre event){
  Player player=event.getEntity();
  String form=FORMS.get(player.getUUID());
  if(form==null)return;
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
}