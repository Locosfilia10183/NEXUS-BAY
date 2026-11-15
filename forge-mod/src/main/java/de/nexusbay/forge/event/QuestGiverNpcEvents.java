package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.BeliLedger;
import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.progression.IslandQuestCatalog;
import de.nexusbay.forge.progression.IslandQuestData;
import de.nexusbay.forge.world.IslandCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Spawns island-specific quest givers only when a player visits their island. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class QuestGiverNpcEvents {
    private static final String TAG_PREFIX = "nexusbay_questgiver_";
    private QuestGiverNpcEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 40 != 0 || player.level().dimension() != net.minecraft.world.level.Level.OVERWORLD) return;
        ServerLevel level = player.serverLevel();
        for (String islandId : new String[]{"FooshaVillage", "ShellsTown", "OrangeTown", "SyrupVillage", "Baratie", "ArlongPark", "Loguetown", "ReverseMountain", "WhiskeyPeak", "LittleGarden", "DrumIsland", "Alabasta", "Jaya", "Skypiea", "Water7", "EniesLobby", "ThrillerBark", "Sabaody", "AmazonLily"}) {
            var island = IslandCatalog.find(islandId).orElse(null);
            if (island == null || player.distanceToSqr(island.x() + 0.5D, player.getY(), island.z() + 0.5D) > 128.0D * 128.0D) continue;
            BlockPos pos = new BlockPos(island.x(), island.safeY(), island.z());
            level.getChunkAt(pos);
            String tag = TAG_PREFIX + islandId;
            AABB search = new AABB(pos).inflate(12.0D, 8.0D, 12.0D);
            if (!level.getEntitiesOfClass(Villager.class, search, e -> e.getTags().contains(tag)).isEmpty()) continue;
            Villager npc = EntityType.VILLAGER.create(level);
            if (npc == null) continue;
            npc.moveTo(island.x() + 0.5D, island.safeY(), island.z() + 0.5D, 180.0F, 0.0F);
            npc.setCustomName(Component.literal("Questgeber von " + island.displayName()));
            npc.setCustomNameVisible(true);
            npc.setNoAi(true);
            npc.setInvulnerable(true);
            npc.setSilent(true);
            npc.setGlowingTag(true);
            npc.setPersistenceRequired();
            npc.addTag(tag);
            level.addFreshEntity(npc);
        }
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND
                || !(event.getTarget() instanceof Villager npc)
                || !(event.getEntity() instanceof ServerPlayer player)) return;
        String islandId = npc.getTags().stream().filter(tag -> tag.startsWith(TAG_PREFIX))
                .map(tag -> tag.substring(TAG_PREFIX.length())).findFirst().orElse("");
        if (islandId.isBlank()) return;
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        var data = IslandQuestData.get(player.serverLevel());
        String activeId = data.active(player.getUUID());
        if (!activeId.isBlank()) {
            var active = IslandQuestCatalog.find(activeId).orElse(null);
            if (active == null) {
                player.sendSystemMessage(Component.literal("Dein Questfortschritt konnte nicht zugeordnet werden."));
                return;
            }
            if (!active.islandId().equals(islandId)) {
                player.sendSystemMessage(Component.literal("Du hast noch eine aktive Aufgabe auf " + IslandCatalog.find(active.islandId()).map(IslandCatalog.Destination::displayName).orElse(active.islandId()) + "."));
                return;
            }
            int progress = data.progress(player.getUUID());
            if (progress < active.requiredKills()) {
                player.sendSystemMessage(Component.literal(active.title() + " — " + active.description()));
                player.sendSystemMessage(Component.literal("Fortschritt: " + progress + "/" + active.requiredKills()));
                return;
            }
            if (!data.claim(player.getUUID(), active.id())) return;
            long balance = BeliLedger.get(player.serverLevel()).deposit(player.getUUID(), active.beliReward());
            var fruits = FruitProfileData.get(player.serverLevel());
            if (fruits.get(player.getUUID()) != null && active.fruitXpReward() > 0) {
                fruits.addExperience(player.getUUID(), active.fruitXpReward());
                de.nexusbay.forge.network.FruitNetwork.sync(player);
            }
            player.sendSystemMessage(Component.literal("Quest abgeschlossen: " + active.title()));
            player.sendSystemMessage(Component.literal("Belohnung: +" + active.beliReward() + " Beli | Kontostand: " + balance));
            if (active.fruitXpReward() > 0 && fruits.get(player.getUUID()) != null)
                player.sendSystemMessage(Component.literal("+" + active.fruitXpReward() + " Frucht-Meisterschafts-XP"));
            player.sendSystemMessage(Component.literal("Sprich erneut mit mir für deine nächste Aufgabe."));
            return;
        }
        var next = IslandQuestCatalog.all().stream()
                .filter(q -> q.islandId().equals(islandId))
                .filter(q -> !data.completed(player.getUUID(), q.id()))
                .findFirst().orElse(null);
        if (next == null) {
            player.sendSystemMessage(Component.literal("Du hast alle Aufgaben auf dieser Insel abgeschlossen. Gute Arbeit!"));
            return;
        }
        if (!data.accept(player.getUUID(), next.id())) return;
        player.sendSystemMessage(Component.literal("Ich habe eine Aufgabe für dich: " + next.title()));
        player.sendSystemMessage(Component.literal(next.description()));
        player.sendSystemMessage(Component.literal("Belohnung: " + next.beliReward() + " Beli"));
        player.sendSystemMessage(Component.literal("Besiege die Gegner und sprich danach wieder mit mir."));
    }
}
