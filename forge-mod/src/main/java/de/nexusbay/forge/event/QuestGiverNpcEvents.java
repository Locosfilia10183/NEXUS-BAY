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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Spawns the Foosha quest giver and handles all quest dialogue through NPC interaction. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class QuestGiverNpcEvents {
    private static final String NPC_TAG = "nexusbay_foosha_quest_giver";
    private QuestGiverNpcEvents() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(ServerLevel.OVERWORLD)) return;
        var island = IslandCatalog.find("FooshaVillage").orElse(null);
        if (island == null) return;
        BlockPos pos = new BlockPos(island.x(), island.safeY(), island.z());
        AABB search = new AABB(pos).inflate(12.0D, 8.0D, 12.0D);
        if (!level.getEntitiesOfClass(Villager.class, search, e -> e.getTags().contains(NPC_TAG)).isEmpty()) return;
        Villager npc = EntityType.VILLAGER.create(level);
        if (npc == null) return;
        npc.moveTo(island.x() + 0.5D, island.safeY(), island.z() + 0.5D, 180.0F, 0.0F);
        npc.setCustomName(Component.literal("Questgeber von Foosha"));
        npc.setCustomNameVisible(true);
        npc.setPersistenceRequired();
        npc.addTag(NPC_TAG);
        level.addFreshEntity(npc);
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (!(target instanceof Villager npc) || !npc.getTags().contains(NPC_TAG)
                || !(event.getEntity() instanceof ServerPlayer player)) return;
        event.setCanceled(true);
        var data = IslandQuestData.get(player.serverLevel());
        String activeId = data.active(player.getUUID());
        if (!activeId.isBlank()) {
            var active = IslandQuestCatalog.find(activeId).orElse(null);
            if (active == null) {
                player.sendSystemMessage(Component.literal("Dein Questfortschritt ist ungültig. Bitte sprich erneut mit mir."));
                return;
            }
            int progress = data.progress(player.getUUID());
            if (progress < active.requiredKills()) {
                player.sendSystemMessage(Component.literal("Questgeber: " + active.title()));
                player.sendSystemMessage(Component.literal(active.description()));
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
                .filter(q -> q.islandId().equals("FooshaVillage"))
                .filter(q -> !data.completed(player.getUUID(), q.id()))
                .findFirst().orElse(null);
        if (next == null) {
            player.sendSystemMessage(Component.literal("Questgeber: Du hast alle Aufgaben von Foosha abgeschlossen. Gute Arbeit!"));
            return;
        }
        if (!data.accept(player.getUUID(), next.id())) return;
        player.sendSystemMessage(Component.literal("Questgeber von Foosha: Ich habe eine Aufgabe für dich!"));
        player.sendSystemMessage(Component.literal(next.title()));
        player.sendSystemMessage(Component.literal(next.description()));
        player.sendSystemMessage(Component.literal("Belohnung: " + next.beliReward() + " Beli"));
        player.sendSystemMessage(Component.literal("Besiege die Gegner und sprich danach wieder mit mir."));
    }
}
