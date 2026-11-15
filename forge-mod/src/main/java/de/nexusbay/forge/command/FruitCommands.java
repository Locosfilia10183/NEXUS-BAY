package de.nexusbay.forge.command;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.network.FruitNetwork;
import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.progression.FruitSkillCatalog;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Player commands for inspecting and resetting fruit mastery. */
@Mod.EventBusSubscriber(modid=NexusBayForge.MOD_ID)
public final class FruitCommands {
 private FruitCommands(){}
 @SubscribeEvent public static void register(RegisterCommandsEvent event){
  event.getDispatcher().register(Commands.literal("nexus").then(Commands.literal("fruit")
   .then(Commands.literal("info").executes(context->{
    var p=context.getSource().getPlayerOrException();var profile=FruitProfileData.get(p.serverLevel()).get(p.getUUID());
    if(profile==null){p.sendSystemMessage(Component.literal("Du besitzt aktuell keine Teufelsfrucht."));return 0;}
    String fruitName=de.nexusbay.forge.progression.DevilFruitCaseTable.all().stream().filter(f->f.id().equals(profile.fruitId())).map(de.nexusbay.forge.progression.DevilFruitCaseTable.Fruit::displayName).findFirst().orElse(profile.fruitId());
    p.sendSystemMessage(Component.literal("Teufelsfrucht: "+fruitName+" | Level "+profile.level()+" | "+profile.experience()+" XP").withStyle(ChatFormatting.LIGHT_PURPLE));
    var skills=FruitSkillCatalog.unlocked(profile.fruitId(),profile.level());
    p.sendSystemMessage(Component.literal("Freigeschaltete Fähigkeiten: "+(skills.isEmpty()?"Keine":String.join(", ",skills.stream().map(FruitSkillCatalog.Skill::name).toList()))));
    var next=FruitSkillCatalog.next(profile.fruitId(),profile.level());
    p.sendSystemMessage(Component.literal(FruitSkillCatalog.forFruit(profile.fruitId()).isEmpty()?"Für diese Frucht sind die Fähigkeiten noch in Entwicklung.":next==null?"Alle bekannten Fähigkeiten freigeschaltet!":"Nächste Fähigkeit: "+next.name()+" – Level "+next.requiredLevel()));
    return 1;
   }))
   .then(Commands.literal("skills").executes(context->{
    var p=context.getSource().getPlayerOrException();var profile=FruitProfileData.get(p.serverLevel()).get(p.getUUID());
    if(profile==null){p.sendSystemMessage(Component.literal("Du besitzt aktuell keine Teufelsfrucht."));return 0;}
    var unlocked=FruitSkillCatalog.unlocked(profile.fruitId(),profile.level());
    p.sendSystemMessage(Component.literal("=== DEINE FRUCHT-SKILLS ===").withStyle(ChatFormatting.LIGHT_PURPLE,ChatFormatting.BOLD));
    for(int i=0;i<unlocked.size();i++){
     var skill=unlocked.get(i);p.sendSystemMessage(Component.literal((i+1)+". "+skill.name()+" | Cooldown: "+(skill.cooldownTicks()/20.0)+"s").withStyle(ChatFormatting.GREEN));
    }
    var next=FruitSkillCatalog.next(profile.fruitId(),profile.level());
    if(next!=null)p.sendSystemMessage(Component.literal("Gesperrt: "+next.name()+" – benötigt Level "+next.requiredLevel()).withStyle(ChatFormatting.GRAY));
    p.sendSystemMessage(Component.literal("G = Fähigkeit einsetzen | H = Fähigkeit wechseln").withStyle(ChatFormatting.YELLOW));
    return 1;
   }))
   .then(Commands.literal("reset").executes(context->{
    var player=context.getSource().getPlayerOrException();FruitProfileData data=FruitProfileData.get(player.serverLevel());
    var oldProfile=data.get(player.getUUID());
    if(oldProfile==null||!data.reset(player.getUUID())){player.sendSystemMessage(Component.literal("Du hast aktuell keine gespeicherte Teufelsfrucht zum Zurücksetzen."));return 0;}
    var fruitItem=de.nexusbay.forge.registry.ModItems.FRUIT_ITEMS.get(oldProfile.fruitId());
    if(fruitItem!=null){for(int i=0;i<player.getInventory().getContainerSize();i++){ItemStack stack=player.getInventory().getItem(i);if(stack.is(fruitItem.get()))player.getInventory().setItem(i,ItemStack.EMPTY);}}
    player.getPersistentData().remove("NexusFruitSkillIndex");
    player.getPersistentData().remove("NexusFruitForm");
    player.getPersistentData().remove("NexusFruitFormTicks");
    player.getInventory().setChanged();FruitNetwork.sync(player);
    player.sendSystemMessage(Component.literal("Deine Teufelsfrucht wurde zurückgesetzt. Dein nächster Frucht-Start beginnt auf Level 1."));
    player.kill();return 1;
   }))));
 }
}
