package de.nexusbay.forge.network;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.BeliLedger;
import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.progression.IslandQuestCatalog;
import de.nexusbay.forge.progression.IslandQuestData;
import de.nexusbay.forge.progression.QuestDialogueCatalog;
import de.nexusbay.forge.world.IslandCatalog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class QuestNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(NexusBayForge.MOD_ID, "quests"), () -> VERSION, VERSION::equals, VERSION::equals);
    private static boolean registered;
    private QuestNetwork() {}
    public static void register() {
        if (registered) return; registered = true;
        CHANNEL.registerMessage(0, OpenQuestScreen.class, OpenQuestScreen::encode, OpenQuestScreen::decode,
            (m,c)->c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->de.nexusbay.forge.client.QuestScreen.open(m.islandId,m.islandName,m.npcName,m.greeting,m.activeId,m.progress,m.ids,m.titles,m.descriptions,m.kills,m.beli,m.xp,m.done))));
        CHANNEL.registerMessage(1, QuestAction.class, QuestAction::encode, QuestAction::decode,
            (m,c)->c.get().enqueueWork(()->{ServerPlayer p=c.get().getSender();if(p!=null)act(p,m.island,m.quest,m.claim);}));
    }
    public static void open(ServerPlayer p,String islandId){
        var island=IslandCatalog.find(islandId).orElse(null);if(island==null)return;
        var data=IslandQuestData.get(p.serverLevel());var qs=IslandQuestCatalog.all().stream().filter(q->q.islandId().equals(islandId)).toList();
        int n=qs.size();String[] ids=new String[n],titles=new String[n],desc=new String[n];int[] kills=new int[n];long[] beli=new long[n],xp=new long[n];boolean[] done=new boolean[n];
        for(int i=0;i<n;i++){var q=qs.get(i);ids[i]=q.id();titles[i]=q.title();desc[i]=q.description();kills[i]=q.requiredKills();beli[i]=q.beliReward();xp[i]=q.fruitXpReward();done[i]=data.completed(p.getUUID(),q.id());}
        var dialogue=QuestDialogueCatalog.find(islandId);
        CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new OpenQuestScreen(islandId,island.displayName(),dialogue.name(),dialogue.greeting(),data.active(p.getUUID()),data.progress(p.getUUID()),ids,titles,desc,kills,beli,xp,done));
    }
    private static void act(ServerPlayer p,String islandId,String questId,boolean claim){
        var island=IslandCatalog.find(islandId).orElse(null);if(island==null||p.distanceToSqr(island.x()+.5D,p.getY(),island.z()+.5D)>144)return;
        var q=IslandQuestCatalog.find(questId).orElse(null);if(q==null||!q.islandId().equals(islandId))return;
        var d=IslandQuestData.get(p.serverLevel());
        if(claim){
            if(!d.active(p.getUUID()).equals(questId)||d.progress(p.getUUID())<q.requiredKills()||!d.claim(p.getUUID(),questId))return;
            long balance=BeliLedger.get(p.serverLevel()).deposit(p.getUUID(),q.beliReward());
            var fruits=FruitProfileData.get(p.serverLevel());
            if(fruits.get(p.getUUID())!=null&&q.fruitXpReward()>0){fruits.addExperience(p.getUUID(),q.fruitXpReward());FruitNetwork.sync(p);}
            p.sendSystemMessage(net.minecraft.network.chat.Component.literal("Quest abgeschlossen: +"+q.beliReward()+" Beli | Kontostand: "+balance));
        }else{
            if(!d.active(p.getUUID()).isBlank()||d.completed(p.getUUID(),questId)||!d.accept(p.getUUID(),questId))return;
            p.sendSystemMessage(net.minecraft.network.chat.Component.literal("Quest angenommen: "+q.title()));
        }
        open(p,islandId);
    }
    public static void accept(String island,String quest){CHANNEL.sendToServer(new QuestAction(island,quest,false));}
    public static void claim(String island,String quest){CHANNEL.sendToServer(new QuestAction(island,quest,true));}
    private record QuestAction(String island,String quest,boolean claim){
        static void encode(QuestAction m,FriendlyByteBuf b){b.writeUtf(m.island,64);b.writeUtf(m.quest,64);b.writeBoolean(m.claim);}
        static QuestAction decode(FriendlyByteBuf b){return new QuestAction(b.readUtf(64),b.readUtf(64),b.readBoolean());}
    }
    private record OpenQuestScreen(String islandId,String islandName,String npcName,String greeting,String activeId,int progress,String[] ids,String[] titles,String[] descriptions,int[] kills,long[] beli,long[] xp,boolean[] done){
        static void encode(OpenQuestScreen m,FriendlyByteBuf b){b.writeUtf(m.islandId,64);b.writeUtf(m.islandName,128);b.writeUtf(m.npcName,128);b.writeUtf(m.greeting,256);b.writeUtf(m.activeId,64);b.writeVarInt(m.progress);b.writeVarInt(m.ids.length);for(int i=0;i<m.ids.length;i++){b.writeUtf(m.ids[i],64);b.writeUtf(m.titles[i],128);b.writeUtf(m.descriptions[i],256);b.writeVarInt(m.kills[i]);b.writeVarLong(m.beli[i]);b.writeVarLong(m.xp[i]);b.writeBoolean(m.done[i]);}}
        static OpenQuestScreen decode(FriendlyByteBuf b){String island=b.readUtf(64),name=b.readUtf(128),npc=b.readUtf(128),greeting=b.readUtf(256),active=b.readUtf(64);int progress=b.readVarInt(),n=Math.min(8,b.readVarInt());String[] ids=new String[n],titles=new String[n],desc=new String[n];int[] kills=new int[n];long[] beli=new long[n],xp=new long[n];boolean[] done=new boolean[n];for(int i=0;i<n;i++){ids[i]=b.readUtf(64);titles[i]=b.readUtf(128);desc[i]=b.readUtf(256);kills[i]=b.readVarInt();beli[i]=b.readVarLong();xp[i]=b.readVarLong();done[i]=b.readBoolean();}return new OpenQuestScreen(island,name,npc,greeting,active,progress,ids,titles,desc,kills,beli,xp,done);}
    }
}
