package de.nexusbay.forge.item;
import de.nexusbay.forge.network.FruitNetwork;
import de.nexusbay.forge.progression.FruitProfileData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
/** Edible fruit that activates persistent player fruit ownership. */
public final class DevilFruitItem extends Item {
 private final String fruitId,displayName;
 public DevilFruitItem(String id,String name){super(new Item.Properties().stacksTo(1));fruitId=id;displayName=name;}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
  ItemStack held=player.getItemInHand(hand); if(level.isClientSide)return InteractionResultHolder.success(held);
  if(!(player instanceof ServerPlayer p))return InteractionResultHolder.pass(held);
  FruitProfileData data=FruitProfileData.get(p.serverLevel());
  if(data.get(p.getUUID())!=null){p.sendSystemMessage(Component.literal("Du besitzt bereits eine Teufelsfrucht. Nutze zuerst /nexus fruit reset.").withStyle(ChatFormatting.RED));return InteractionResultHolder.fail(held);}
  data.set(p.getUUID(),fruitId);held.shrink(1);FruitNetwork.sync(p);
  p.sendSystemMessage(Component.literal("Du hast die "+displayName+" gegessen! Frucht-Level: 1").withStyle(ChatFormatting.LIGHT_PURPLE,ChatFormatting.BOLD));
  return InteractionResultHolder.consume(held);
 }
}