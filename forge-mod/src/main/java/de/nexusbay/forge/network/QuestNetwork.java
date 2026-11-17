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
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NexusBayForge.MOD_ID, "quests"),
            () -> VERSION, VERSION::equals, VERSION::equals);
    private static boolean registered;
    private QuestNetwork() {}

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, OpenQuestScreen.class, OpenQuestScreen::encode, OpenQuestScreen::decode,
                (m, c) -> c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT, () -> () -> de.nexusbay.forge.client.QuestScreen.open(
                                m.islandId, m.islandName, m.npcName, m.greeting, m.activeId,
                                m.progress, m.ids, m.titles, m.descriptions, m.kills, m.beli, m.xp, m.done))));
        CHANNEL.registerMessage(1, QuestAction.class, QuestAction::encode, QuestAction::decode,
                (m, c) -> c.get().enqueueWork(() -> {
                    ServerPlayer player = c.get().getSender();
                    if (player != null) act(player, m.island, m.quest, m.claim);
                }));
    }

    public static void open(ServerPlayer player, String islandId) {
        var island = IslandCatalog.find(islandId).orElse(null);
        if (island == null) return;
        var data = IslandQuestData.get(player.serverLevel());
        var quests = IslandQuestCatalog.all().stream().filter(q -> q.islandId().equals(islandId)).toList();
        int n = quests.size();
        String[] ids = new String[n], titles = new String[n], descriptions = new String[n];
        int[] kills = new int[n]; long[] beli = new long[n], xp = new long[n]; boolean[] done = new boolean[n];
        for (int i = 0; i < n; i++) {
            var q = quests.get(i);
            ids[i] = q.id(); titles[i] = q.title(); descriptions[i] = q.description();
            kills[i] = q.requiredKills(); beli[i] = q.beliReward(); xp[i] = q.fruitXpReward();
            done[i] = data.completed(player.getUUID(), q.id());
        }
        var dialogue = QuestDialogueCatalog.find(islandId);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenQuestScreen(islandId, island.displayName(), dialogue.name(), dialogue.greeting(),
                        data.active(player.getUUID()), data.progress(player.getUUID()),
                        ids, titles, descriptions, kills, beli, xp, done));
    }

    private static void act(ServerPlayer player, String islandId, String questId, boolean claim) {
        var island = IslandCatalog.find(islandId).orElse(null);
        if (island == null) { fail(player, "Diese Insel ist nicht verfügbar."); return; }
        if (player.distanceToSqr(island.x() + .5D, player.getY(), island.z() + .5D) > 144) {
            fail(player, "Du bist zu weit vom Questgeber entfernt."); return;
        }
        var quest = IslandQuestCatalog.find(questId).orElse(null);
        if (quest == null || !quest.islandId().equals(islandId)) {
            fail(player, "Diese Quest gehört nicht zu diesem Questgeber."); return;
        }
        var data = IslandQuestData.get(player.serverLevel());
        if (claim) {
            if (!data.active(player.getUUID()).equals(questId)) {
                fail(player, "Du hast diese Quest gerade nicht aktiv."); return;
            }
            int progress = data.progress(player.getUUID());
            if (progress < quest.requiredKills()) {
                fail(player, "Dir fehlen noch " + (quest.requiredKills() - progress) + " Gegner.");
                return;
            }
            if (!data.claim(player.getUUID(), questId)) {
                fail(player, "Die Questbelohnung konnte nicht abgeholt werden."); return;
            }
            long balance = BeliLedger.get(player.serverLevel()).deposit(player.getUUID(), quest.beliReward());
            var fruits = FruitProfileData.get(player.serverLevel());
            boolean fruitRewarded = fruits.get(player.getUUID()) != null && quest.fruitXpReward() > 0;
            if (fruitRewarded) {
                fruits.addExperience(player.getUUID(), quest.fruitXpReward());
                FruitNetwork.sync(player);
            }
            String reward = "Quest abgeschlossen! +" + quest.beliReward() + " Beli";
            if (fruitRewarded) reward += " | +" + quest.fruitXpReward() + " Frucht-XP";
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    reward + " | Kontostand: " + balance).withStyle(net.minecraft.ChatFormatting.GREEN));
            player.playNotifySound(net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);
        } else {
            if (!data.active(player.getUUID()).isBlank()) {
                fail(player, "Du hast bereits eine aktive Quest. Schließe sie zuerst ab.");
                return;
            }
            if (data.completed(player.getUUID(), questId)) {
                fail(player, "Diese Quest hast du bereits abgeschlossen.");
                return;
            }
            if (!data.accept(player.getUUID(), questId)) {
                fail(player, "Die Quest konnte nicht angenommen werden."); return;
            }
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Quest angenommen: " + quest.title()).withStyle(net.minecraft.ChatFormatting.YELLOW));
            player.playNotifySound(net.minecraft.sounds.SoundEvents.UI_TOAST_IN,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.0F);
        }
        open(player, islandId);
    }

    private static void fail(ServerPlayer player, String message) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message)
                .withStyle(net.minecraft.ChatFormatting.RED));
        player.playNotifySound(net.minecraft.sounds.SoundEvents.VILLAGER_NO,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    public static void accept(String island, String quest) { CHANNEL.sendToServer(new QuestAction(island, quest, false)); }
    public static void claim(String island, String quest) { CHANNEL.sendToServer(new QuestAction(island, quest, true)); }

    private record QuestAction(String island, String quest, boolean claim) {
        static void encode(QuestAction m, FriendlyByteBuf b) { b.writeUtf(m.island, 64); b.writeUtf(m.quest, 64); b.writeBoolean(m.claim); }
        static QuestAction decode(FriendlyByteBuf b) { return new QuestAction(b.readUtf(64), b.readUtf(64), b.readBoolean()); }
    }

    private record OpenQuestScreen(String islandId, String islandName, String npcName, String greeting,
                                   String activeId, int progress, String[] ids, String[] titles,
                                   String[] descriptions, int[] kills, long[] beli, long[] xp, boolean[] done) {
        static void encode(OpenQuestScreen m, FriendlyByteBuf b) {
            b.writeUtf(m.islandId, 64); b.writeUtf(m.islandName, 128); b.writeUtf(m.npcName, 128);
            b.writeUtf(m.greeting, 256); b.writeUtf(m.activeId, 64); b.writeVarInt(m.progress);
            b.writeVarInt(m.ids.length);
            for (int i = 0; i < m.ids.length; i++) {
                b.writeUtf(m.ids[i], 64); b.writeUtf(m.titles[i], 128); b.writeUtf(m.descriptions[i], 256);
                b.writeVarInt(m.kills[i]); b.writeVarLong(m.beli[i]); b.writeVarLong(m.xp[i]); b.writeBoolean(m.done[i]);
            }
        }
        static OpenQuestScreen decode(FriendlyByteBuf b) {
            String island = b.readUtf(64), name = b.readUtf(128), npc = b.readUtf(128);
            String greeting = b.readUtf(256), active = b.readUtf(64);
            int progress = b.readVarInt(), n = Math.min(8, b.readVarInt());
            String[] ids = new String[n], titles = new String[n], desc = new String[n];
            int[] kills = new int[n]; long[] beli = new long[n], xp = new long[n]; boolean[] done = new boolean[n];
            for (int i = 0; i < n; i++) {
                ids[i] = b.readUtf(64); titles[i] = b.readUtf(128); desc[i] = b.readUtf(256);
                kills[i] = b.readVarInt(); beli[i] = b.readVarLong(); xp[i] = b.readVarLong(); done[i] = b.readBoolean();
            }
            return new OpenQuestScreen(island, name, npc, greeting, active, progress, ids, titles, desc, kills, beli, xp, done);
        }
    }
}
