package de.nexusbay.forge.client;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.network.FruitNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Sends a simple activation request when the fruit skill key is pressed. */
@Mod.EventBusSubscriber(modid=NexusBayForge.MOD_ID,value=Dist.CLIENT)
public final class FruitSkillClientEvents {
 private FruitSkillClientEvents(){}
 @SubscribeEvent public static void clientTick(TickEvent.ClientTickEvent event){
  if(event.phase!=TickEvent.Phase.END||Minecraft.getInstance().player==null)return;
  while(FruitSkillKeybind.USE_SKILL.consumeClick())FruitNetwork.requestSkill();
  while(FruitSkillKeybind.CYCLE_SKILL.consumeClick())FruitNetwork.cycleSkill();
 }
}
