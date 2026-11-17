package de.nexusbay.forge.network;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.BeliLedger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

public final class MerchantNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NexusBayForge.MOD_ID, "merchant"), () -> "1", "1"::equals, "1"::equals);
    private static boolean registered;
    private MerchantNetwork() {}
    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, OpenPacket.class, OpenPacket::encode, OpenPacket::decode,
                (m,c)->c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        ()->()->de.nexusbay.forge.client.MerchantScreen.open(m.island,m.name,m.balance,m.ids,m.names,m.prices))));
        CHANNEL.registerMessage(1, BuyPacket.class, BuyPacket::encode, BuyPacket::decode,
                (m,c)->c.get().enqueueWork(()->{ServerPlayer p=c.get().getSender();if(p!=null)buy(p,m.island,m.itemId);}));
    }
    public static void open(ServerPlayer p,String island,String name) {
        List<Offer> offers=offers(island);
        String[] ids=new String[offers.size()],names=new String[offers.size()];long[] prices=new long[offers.size()];
        for(int i=0;i<offers.size();i++){Offer o=offers.get(i);ids[i]=o.id;names[i]=o.name;prices[i]=o.price;}
        CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new OpenPacket(island,name,BeliLedger.get(p.serverLevel()).balance(p),ids,names,prices));
    }
    private static void buy(ServerPlayer p,String island,String itemId) {
        Offer offer=offers(island).stream().filter(o->o.id.equals(itemId)).findFirst().orElse(null);
        if(offer==null)return;
        var ledger=BeliLedger.get(p.serverLevel());
        if(!ledger.withdraw(p.getUUID(),offer.price)){p.sendSystemMessage(net.minecraft.network.chat.Component.literal("Nicht genug Beli!"));open(p,island,merchantName(island));return;}
        var item=net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(NexusBayForge.MOD_ID,itemId));
        if(item==null||item==net.minecraft.world.item.Items.AIR){ledger.deposit(p.getUUID(),offer.price);return;}
        ItemStack stack=new ItemStack(item,1);
        if(!p.getInventory().add(stack))p.drop(stack,false);
        p.sendSystemMessage(net.minecraft.network.chat.Component.literal("Gekauft: "+offer.name+" für "+offer.price+" Beli."));
        open(p,island,merchantName(island));
    }
    private static String merchantName(String id){return switch(id){case "FooshaVillage"->"Makinos Markt";case "Loguetown"->"Loguetown-Waffenhändler";case "Alabasta"->"Wüstenbasar";case "Skypiea"->"Himmelswaren";case "FishManIsland"->"Händler der Fischmenschen";case "WanoKuni","Onigashima"->"Wano-Waffenschmied";case "PunkHazard"->"Labor-Ausrüster";default->"Händler von "+id;};}
    private static List<Offer> offers(String island) {
        return switch(island) {
            case "ShellsTown","Marineford","EniesLobby" -> List.of(new Offer("marine_insignia","Marine-Abzeichen",180),new Offer("seastone_saber","Seestein-Säbel",12500),new Offer("berry_tonic","Beeren-Tonikum",90));
            case "Skypiea","Jaya" -> List.of(new Offer("skypiea_dial","Himmels-Dial",650),new Offer("devil_fruit_case","Versiegelte Fruchtkiste",2600),new Offer("berry_tonic","Beeren-Tonikum",100));
            case "FishManIsland","Sabaody" -> List.of(new Offer("fishman_pearl","Tiefsee-Perle",720),new Offer("devil_fruit_case","Versiegelte Fruchtkiste",2800),new Offer("berry_tonic","Beeren-Tonikum",110));
            case "WanoKuni","Onigashima" -> List.of(new Offer("wano_steel","Wano-Stahl",950),new Offer("oni_cleaver","Oni-Spalter",18000),new Offer("wano_samurai_helmet","Samurai-Helm",8500));
            case "PunkHazard","DrumIsland" -> List.of(new Offer("alchemy_reagent","Alchemie-Reagenz",500),new Offer("berry_tonic","Beeren-Tonikum",80),new Offer("devil_fruit_case","Versiegelte Fruchtkiste",2700));
            default -> List.of(new Offer("berry_tonic","Beeren-Tonikum",75),new Offer("devil_fruit_case","Versiegelte Fruchtkiste",3000),new Offer("three_sword_style_kit","Drei-Schwerter-Stil-Kit",1200));
        };
    }
    public static void buy(String island,String id){CHANNEL.sendToServer(new BuyPacket(island,id));}
    private record Offer(String id,String name,long price){}
    private record BuyPacket(String island,String itemId){static void encode(BuyPacket m,FriendlyByteBuf b){b.writeUtf(m.island,64);b.writeUtf(m.itemId,64);}static BuyPacket decode(FriendlyByteBuf b){return new BuyPacket(b.readUtf(64),b.readUtf(64));}}
    private record OpenPacket(String island,String name,long balance,String[] ids,String[] names,long[] prices){
        static void encode(OpenPacket m,FriendlyByteBuf b){b.writeUtf(m.island,64);b.writeUtf(m.name,128);b.writeVarLong(m.balance);b.writeVarInt(m.ids.length);for(int i=0;i<m.ids.length;i++){b.writeUtf(m.ids[i],64);b.writeUtf(m.names[i],128);b.writeVarLong(m.prices[i]);}}
        static OpenPacket decode(FriendlyByteBuf b){String island=b.readUtf(64),name=b.readUtf(128);long balance=b.readVarLong();int n=Math.min(8,b.readVarInt());String[] ids=new String[n],names=new String[n];long[] prices=new long[n];for(int i=0;i<n;i++){ids[i]=b.readUtf(64);names[i]=b.readUtf(128);prices[i]=b.readVarLong();}return new OpenPacket(island,name,balance,ids,names,prices);}
    }
}
