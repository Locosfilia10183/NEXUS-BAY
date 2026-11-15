package de.nexusbay.forge.network;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.BeliLedger;
import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.progression.IslandQuestCatalog;
import de.nexusbay.forge.progression.IslandQuestData;
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
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NexusBayForge.MOD_ID, "quests"), () -> VERSION, VERSION::equals, VERSION::equals);
    private static boolean registered;
    private QuestNetwork() {}

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, OpenQuestScreen.class, OpenQuestScreen::encode, OpenQuestScreen::decode,
                (message, context) -> context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> de.nexusbay.forge.client.QuestScreen.open(message.islandId, message.islandName,
                                message.activeId, message.progress, message.questIds, message.titles,
                                message.descriptions, message.kills, message.beli, message.fruitXp,
                                message.completed))));
        CHANNEL.registerMessage(1, QuestAction.class, QuestAction::encode, QuestAction::decode,
                (message, context) -> context.get().enqueueWork(() -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null) handleAction(player, message.islandId, message.questId, message.claim);
                }));
    }

    public static void open(ServerPlayer player, String islandId) {
        var island = IslandCatalog.find(islandId).orElse(null);
        if (island == null) return;
        var data = IslandQuestData.get(player.serverLevel());
        var quests = IslandQuestCatalog.all().stream().filter(q -> q.islandId().equals(islandId)).toList();
        String[] ids = new String[quests.size()], titles = new String[quests.size()], descriptions = new String[quests.size()];
        int[] kills = new int[quests.size()];
        long[] beli = new long[quests.size()], xp = new long[quests.size()];
        boolean[] completed = new boolean[quests.size()];
        for (int i = 0; i < quests.size(); i++) {
            var q = quests.get(i);
            ids[i] = q.id(); titles[i] = q.title(); descriptions[i] = q.description();
            kills[i] = q.requiredKills(); beli[i] = q.beliReward(); xp[i] = q.fruitXpReward();
            completed[i] = data.completed(player.getUUID(), q.id());
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenQuestScreen(islandId, island.displayName(), data.active(player.getUUID()),
                        data.progress(player.getUUID()), ids, titles, descriptions, kills, beli, xp, completed));
    }

    private static void handleAction(ServerPlayer player, String islandId, String questId, boolean claim) {
        var island = IslandCatalog.find(islandId).orElse(null);
        if (island == null || player.distanceToSqr(island.x() + 0.5D, player.getY(), island.z() + 0.5D) > 12.0D * 12.0D) return;
        var quest = IslandQuestCatalog.find(questId).orElse(null);
        if (quest == null || !quest.islandId().equals(islandId)) return;
        var data = IslandQuestData.get(player.serverLevel());
        if (claim) {
            if (!data.active(player.getUUID()).equals(questId) || data.progress(player.getUUID()) < quest.requiredKills()) return;
            if (!data.claim(player.getUUID(), questId)) return;
            long balance = BeliLedger.get(player.serverLevel()).deposit(player.getUUID(), quest.beliReward());
            var fruits = FruitProfileData.get(player.serverLevel());
            if (fruits.get(player.getUUID()) != null && quest.fruitXpReward() > 0) {
                fruits.addExperience(player.getUUID(), quest.fruitXpReward());
                FruitNetwork.sync(player);
            }
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Quest abgeschlossen: +" + quest.beliReward() + " Beli | Kontostand: " + balance));
            if (fruits.get(player.getUUID()) != null && quest.fruitXpReward() > 0)
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("+" + quest.fruitXpReward() + " Frucht-XP"));
        } else {
            if (!data.active(player.getUUID()).isBlank() || data.completed(player.getUUID(), questId)) return;
            if (!data.accept(player.getUUID(), questId)) return;
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Quest angenommen: " + quest.title()));
        }
        open(player, islandId);
    }

    public static void accept(String islandId, String questId) {
        CHANNEL.sendToServer(new QuestAction(islandId, questId, false));
    }
    public static void claim(String islandId, String questId) {
        CHANNEL.sendToServer(new QuestAction(islandId, questId, true));
    }

    private record QuestAction(String islandId, String questId, boolean claim) {
        static void encode(QuestAction m, FriendlyByteBuf b) { b.writeUtf(m.islandId, 64); b.writeUtf(m.questId, 64); b.writeBoolean(m.claim); }
        static QuestAction decode(FriendlyByteBuf b) { return new QuestAction(b.readUtf(64), b.readUtf(64), b.readBoolean()); }
    }

    private record OpenQuestScreen(String islandId, String islandName, String activeId, int progress,
                                   String[] questIds, String[] titles, String[] descriptions, int[] kills,
                                   long[] beli, long[] fruitXp, boolean[] completed) {
        static void encode(OpenQuestScreen m, FriendlyByteBuf b) {
            b.writeUtf(m.islandId, 64); b.writeUtf(m.islandName, 128); b.writeUtf(m.activeId, 64); b.writeVarInt(m.progress);
            b.writeVarInt(m.questIds.length);
            for (int i = 0; i < m.questIds.length; i++) {
                b.writeUtf(m.questIds[i], 64); b.writeUtf(m.titles[i], 128); b.writeUtf(m.descriptions[i], 256);
                b.writeVarInt(m.kills[i]); b.writeVarLong(m.beli[i]); b.writeVarLong(m.fruitXp[i]); b.writeBoolean(m.completed[i]);
            }
        }
        static OpenQuestScreen decode(FriendlyByteBuf b) {
            String islandId=b.readUtf(64), islandName=b.readUtf(128), activeId=b.readUtf(64); int progress=b.readVarInt();
            int n=Math.min(8,b.readVarInt()); String[] ids=new String[n], titles=new String[n], desc=new String[n];
            int[] kills=new int[n]; long[] beli=new long[n], xp=new long[n]; boolean[] done=new boolean[n];
            for(int i=0;i<n;i++){ids[i]=b.readUtf(64);titles[i]=b.readUtf(128);desc[i]=b.readUtf(256);kills[i]=b.readVarInt();beli[i]=b.readVarLong();xp[i]=b.readVarLong();done[i]=b.readBoolean();}
            return new OpenQuestScreen(islandId,islandName,activeId,progress,ids,titles,desc,kills,beli,xp,done);
        }
    }
}
