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
        if (island == null || player.distanceToSqr(island.x() + 0.5D, player.getY(), island.z() + 0.5D) > 180.0D * 180.0D) return;
        int count = data.addKill(player.getUUID());
        player.sendSystemMessage(Component.literal("Quest: " + quest.title() + " — " + Math.min(count, quest.requiredKills()) + "/" + quest.requiredKills()));
        if (count == quest.requiredKills()) player.sendSystemMessage(Component.literal("Questziel erreicht! Kehre zum Questgeber auf dieser Insel zurück."));
    }
}
