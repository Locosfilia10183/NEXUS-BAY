package de.nexusbay.forge.command;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.network.FruitNetwork;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Player command for the fruit reset mechanic. */
@Mod.EventBusSubscriber(modid=NexusBayForge.MOD_ID)
public final class FruitCommands {
 private FruitCommands(){}
 @SubscribeEvent public static void register(RegisterCommandsEvent event){
  event.getDispatcher().register(Commands.literal("nexus").then(Commands.literal("fruit").then(Commands.literal("reset").executes(context->{
   var player=context.getSource().getPlayerOrException();
   FruitProfileData data=FruitProfileData.get(player.serverLevel());
   if(!data.reset(player.getUUID())){player.sendSystemMessage(Component.literal("Du hast aktuell keine gespeicherte Teufelsfrucht zum Zurücksetzen."));return 0;}
   ItemStack current=player.getInventory().getItem(0);
   if(current.getItem() instanceof de.nexusbay.forge.item.DevilFruitItem)player.getInventory().setItem(0,ItemStack.EMPTY);
   player.getInventory().setChanged();FruitNetwork.sync(player);
   player.sendSystemMessage(Component.literal("Deine Teufelsfrucht wurde zurückgesetzt. Dein nächster Frucht-Start beginnt auf Level 1."));
   player.kill();return 1;
  }))));
 }
}