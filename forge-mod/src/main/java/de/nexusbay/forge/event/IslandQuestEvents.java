package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.IslandQuestCatalog;
import de.nexusbay.forge.progression.IslandQuestData;
import de.nexusbay.forge.world.IslandCatalog;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Advances active kill quests when a player defeats a hostile monster. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandQuestEvents {
    private IslandQuestEvents() {}
    @SubscribeEvent public static void onKill(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Monster) || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        var data = IslandQuestData.get(player.serverLevel());
        String id = data.active(player.getUUID());
        var quest = IslandQuestCatalog.find(id).orElse(null);
        if (quest == null) return;
        var island = IslandCatalog.find(quest.islandId()).orElse(null);
        if (island == null) return;

        // A faction-tagged enemy must belong to this island. Untagged hostile
        // mobs can still count, but only when they are physically on the island.
        String factionIsland = event.getEntity().getPersistentData().getString("nexusbay_faction_island");
        if (!factionIsland.isBlank() && !quest.islandId().equals(factionIsland)) return;
        double islandX = island.x() + 0.5D;
        double islandZ = island.z() + 0.5D;
        double islandRadiusSq = (double) IslandCatalog.ISLAND_RADIUS * IslandCatalog.ISLAND_RADIUS;
        if (event.getEntity().distanceToSqr(islandX, event.getEntity().getY(), islandZ) > islandRadiusSq) return;
        if (player.distanceToSqr(islandX, player.getY(), islandZ) > 180.0D * 180.0D) return;
        int count = data.addKill(player.getUUID());
        player.displayClientMessage(Component.literal("Quest: " + quest.title() + " — " + Math.min(count, quest.requiredKills()) + "/" + quest.requiredKills()), true);
        if (count == quest.requiredKills()) player.sendSystemMessage(Component.literal("Questziel erreicht! Kehre zum Questgeber auf dieser Insel zurück."));
    }
}
