package de.nexusbay.forge.client;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.DevilFruitCaseTable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Compact fruit HUD, visible while hotbar slot 1 is selected. */
@Mod.EventBusSubscriber(modid=NexusBayForge.MOD_ID,value=Dist.CLIENT)
public final class FruitHud {
 private FruitHud(){}
 @SubscribeEvent public static void render(RenderGuiOverlayEvent.Post e){
  Minecraft mc=Minecraft.getInstance();
  if(mc.player==null||mc.options.hideGui||mc.player.getInventory().selected!=0||FruitHudData.fruitId.isBlank())return;
  String name=DevilFruitCaseTable.all().stream().filter(f->f.id().equals(FruitHudData.fruitId)).map(DevilFruitCaseTable.Fruit::displayName).findFirst().orElse(FruitHudData.fruitId);
  var g=e.getGuiGraphics();int x=10,y=e.getWindow().getGuiScaledHeight()/2-34;
  g.fill(x,y,x+166,y+50,0xC0101422);g.fill(x,y,x+3,y+50,0xFF9B55E8);
  g.drawString(mc.font,Component.literal(name).withStyle(ChatFormatting.LIGHT_PURPLE,ChatFormatting.BOLD),x+9,y+7,0xFFFFFF,false);
  g.drawString(mc.font,"Frucht-Level: "+FruitHudData.level,x+9,y+21,0xFFFFFF,false);
  long next=FruitHudData.level>=100?0:100L*FruitHudData.level*FruitHudData.level;
  g.drawString(mc.font,FruitHudData.level>=100?"Meisterschaft MAX":"Fortschritt: "+FruitHudData.experience+" / "+next+" XP",x+9,y+35,0xD8CBEA,false);
 }
}