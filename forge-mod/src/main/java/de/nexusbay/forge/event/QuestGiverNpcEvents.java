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
import net.minecraft.world.entity.npc.Villager;
import de.nexusbay.forge.entity.QuestGiverEntity;
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

    private static String IslandQuestCatalogDialogueName(String islandId) {
        return de.nexusbay.forge.progression.QuestDialogueCatalog.find(islandId).name();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 40 != 0 || player.level().dimension() != net.minecraft.world.level.Level.OVERWORLD) return;
        ServerLevel level = player.serverLevel();
        for (String islandId : new String[]{"FooshaVillage", "ShellsTown", "OrangeTown", "SyrupVillage", "Baratie", "ArlongPark", "Loguetown", "ReverseMountain", "WhiskeyPeak", "LittleGarden", "DrumIsland", "Alabasta", "Jaya", "Skypiea", "Water7", "EniesLobby", "ThrillerBark", "Sabaody", "AmazonLily", "ImpelDown", "Marineford", "FishManIsland", "PunkHazard", "Dressrosa", "Zou", "WholeCakeIsland", "WanoKuni", "Onigashima"}) {
            var island = IslandCatalog.find(islandId).orElse(null);
            if (island == null || player.distanceToSqr(island.x() + 0.5D, player.getY(), island.z() + 0.5D) > 128.0D * 128.0D) continue;
            BlockPos pos = new BlockPos(island.x(), island.safeY(), island.z());
            level.getChunkAt(pos);
            String tag = TAG_PREFIX + islandId;
            AABB search = new AABB(pos).inflate(12.0D, 8.0D, 12.0D);
            // Replace quest givers created by older mod versions instead of leaving duplicates behind.
            for (Villager legacyNpc : level.getEntitiesOfClass(Villager.class, search,
                    e -> e.getTags().contains(tag))) {
                legacyNpc.discard();
            }
            if (!level.getEntitiesOfClass(QuestGiverEntity.class, search, e -> e.getTags().contains(tag)).isEmpty()) continue;
            QuestGiverEntity npc = de.nexusbay.forge.registry.ModEntities.QUEST_GIVER.get().create(level);
            if (npc == null) continue;
            npc.moveTo(island.x() + 0.5D, island.safeY(), island.z() + 0.5D, 180.0F, 0.0F);
            npc.setCustomName(Component.literal(IslandQuestCatalogDialogueName(islandId)));
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
                || !(event.getTarget() instanceof QuestGiverEntity npc)
                || !(event.getEntity() instanceof ServerPlayer player)) return;
        String islandId = npc.getTags().stream().filter(tag -> tag.startsWith(TAG_PREFIX))
                .map(tag -> tag.substring(TAG_PREFIX.length())).findFirst().orElse("");
        if (islandId.isBlank()) return;
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if (!npc.isAlive() || npc.level() != player.level()
                || player.distanceToSqr(npc) > 36.0D) {
            player.displayClientMessage(Component.literal("Du bist zu weit vom Questgeber entfernt."), true);
            return;
        }
        long now = player.serverLevel().getGameTime();
        long nextUse = player.getPersistentData().getLong("NexusBayQuestNpcNextUse");
        if (now < nextUse) return;
        player.getPersistentData().putLong("NexusBayQuestNpcNextUse", now + 10L);
        de.nexusbay.forge.network.QuestNetwork.open(player, islandId);
    }
}
