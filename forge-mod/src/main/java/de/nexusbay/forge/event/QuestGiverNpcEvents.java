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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
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

    /** Gives each island's quest giver a fitting vanilla outfit until custom GeoNPC models are ready. */
    private static VillagerProfession professionForIsland(String islandId) {
        return switch (islandId) {
            case "ShellsTown", "EniesLobby", "ImpelDown", "Marineford" -> VillagerProfession.ARMORER;
            case "Baratie", "WholeCakeIsland" -> VillagerProfession.BUTCHER;
            case "Water7" -> VillagerProfession.TOOLSMITH;
            case "Alabasta", "Loguetown", "Sabaody" -> VillagerProfession.CARTOGRAPHER;
            case "DrumIsland", "FishManIsland", "ReverseMountain" -> VillagerProfession.FISHERMAN;
            case "WanoKuni", "Onigashima", "AmazonLily" -> VillagerProfession.WEAPONSMITH;
            case "Skypiea", "PunkHazard", "LittleGarden" -> VillagerProfession.CLERIC;
            case "SyrupVillage", "FooshaVillage", "OrangeTown", "ArlongPark", "WhiskeyPeak",
                    "Jaya", "ThrillerBark", "Dressrosa", "Zou" -> VillagerProfession.FLETCHER;
            default -> VillagerProfession.LIBRARIAN;
        };
    }

    /** Gives each quest giver a recognizable prop while dedicated GeoNPC models are being built. */
    private static ItemStack heldItemForIsland(String islandId) {
        return switch (islandId) {
            case "ShellsTown", "EniesLobby", "ImpelDown", "Marineford" -> new ItemStack(Items.IRON_SWORD);
            case "Baratie", "WholeCakeIsland" -> new ItemStack(Items.COOKED_BEEF);
            case "Water7" -> new ItemStack(Items.IRON_AXE);
            case "Alabasta", "Loguetown", "Sabaody" -> new ItemStack(Items.FILLED_MAP);
            case "DrumIsland", "FishManIsland", "ReverseMountain" -> new ItemStack(Items.FISHING_ROD);
            case "WanoKuni", "Onigashima", "AmazonLily" -> new ItemStack(Items.DIAMOND_SWORD);
            case "Skypiea", "PunkHazard", "LittleGarden" -> new ItemStack(Items.BLAZE_ROD);
            case "SyrupVillage", "FooshaVillage", "OrangeTown", "ArlongPark", "WhiskeyPeak",
                    "Jaya", "ThrillerBark", "Dressrosa", "Zou" -> new ItemStack(Items.BOW);
            default -> new ItemStack(Items.WRITABLE_BOOK);
        };
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
            if (!level.getEntitiesOfClass(Villager.class, search, e -> e.getTags().contains(tag)).isEmpty()) continue;
            Villager npc = EntityType.VILLAGER.create(level);
            if (npc == null) continue;
            npc.moveTo(island.x() + 0.5D, island.safeY(), island.z() + 0.5D, 180.0F, 0.0F);
            npc.setVillagerData(npc.getVillagerData()
                    .setProfession(professionForIsland(islandId))
                    .setLevel(5));
            npc.setItemSlot(EquipmentSlot.MAINHAND, heldItemForIsland(islandId));
            npc.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
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
                || !(event.getTarget() instanceof Villager npc)
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
