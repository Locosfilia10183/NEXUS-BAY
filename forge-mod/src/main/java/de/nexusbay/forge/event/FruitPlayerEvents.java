package de.nexusbay.forge.event;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.network.FruitNetwork;
import de.nexusbay.forge.progression.FruitProfileData;
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
  if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||p.tickCount%10!=0)return;
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
  if(!(e.getSource().getEntity() instanceof ServerPlayer p))return;
  FruitProfileData.Profile updated=FruitProfileData.get(p.serverLevel()).addExperience(p.getUUID(),25);
  if(updated!=null)FruitNetwork.sync(p);
 }
}