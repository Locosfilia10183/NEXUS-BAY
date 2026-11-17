package de.nexusbay.forge.network;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.FruitProfileData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
/** Server-authoritative fruit HUD synchronization. */
public final class FruitNetwork {
 private static final String VERSION="1";
 public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(NexusBayForge.MOD_ID,"main"),()->VERSION,VERSION::equals,VERSION::equals);
 private static boolean registered;
 private FruitNetwork(){}
 public static void register(){
  if(registered)return;registered=true;
  CHANNEL.registerMessage(0,SyncFruit.class,SyncFruit::encode,SyncFruit::decode,(m,c)->c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->de.nexusbay.forge.client.FruitHudData.set(m.id,m.level,m.xp))));
  CHANNEL.registerMessage(1,UseFruitSkill.class,UseFruitSkill::encode,UseFruitSkill::decode,(m,c)->c.get().enqueueWork(()->{var player=c.get().getSender();if(player!=null)FruitSkillCombat.activate(player);}));
  CHANNEL.registerMessage(2,CycleFruitSkill.class,CycleFruitSkill::encode,CycleFruitSkill::decode,(m,c)->c.get().enqueueWork(()->{var player=c.get().getSender();if(player!=null)FruitSkillCombat.cycle(player);}));
  CHANNEL.registerMessage(3,SyncFruitForm.class,SyncFruitForm::encode,SyncFruitForm::decode,(m,c)->c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->de.nexusbay.forge.client.FruitFormClientData.set(m.playerId,m.form,m.ticks))));
 }
 public static void sync(net.minecraft.server.level.ServerPlayer player){
  FruitProfileData.Profile p=FruitProfileData.get(player.serverLevel()).get(player.getUUID());
  CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new SyncFruit(p==null?"":p.fruitId(),p==null?0:p.level(),p==null?0:p.experience()));
 }
 public static void syncForm(net.minecraft.server.level.ServerPlayer player,String form,int ticks){CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->player),new SyncFruitForm(player.getUUID(),form,ticks));}
 public static void requestSkill(){CHANNEL.sendToServer(new UseFruitSkill());}
 public static void cycleSkill(){CHANNEL.sendToServer(new CycleFruitSkill());}
 private record UseFruitSkill(){
  static void encode(UseFruitSkill m,FriendlyByteBuf b){}
  static UseFruitSkill decode(FriendlyByteBuf b){return new UseFruitSkill();}
 }
 private record CycleFruitSkill(){
  static void encode(CycleFruitSkill m,FriendlyByteBuf b){}
  static CycleFruitSkill decode(FriendlyByteBuf b){return new CycleFruitSkill();}
 }
 private record SyncFruitForm(java.util.UUID playerId,String form,int ticks){
  static void encode(SyncFruitForm m,FriendlyByteBuf b){b.writeUUID(m.playerId);b.writeUtf(m.form,64);b.writeVarInt(m.ticks);}
  static SyncFruitForm decode(FriendlyByteBuf b){return new SyncFruitForm(b.readUUID(),b.readUtf(64),b.readVarInt());}
 }
 private record SyncFruit(String id,int level,long xp){
  static void encode(SyncFruit m,FriendlyByteBuf b){b.writeUtf(m.id,64);b.writeVarInt(m.level);b.writeVarLong(m.xp);}
  static SyncFruit decode(FriendlyByteBuf b){return new SyncFruit(b.readUtf(64),b.readVarInt(),b.readVarLong());}
 }
}