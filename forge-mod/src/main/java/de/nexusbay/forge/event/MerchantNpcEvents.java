package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.world.IslandCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/** Creates one island-themed vanilla trading villager when an island is visited. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class MerchantNpcEvents {
    private static final String TAG_PREFIX = "nexusbay_merchant_";
    private static final List<String> ISLANDS = List.of(
            "FooshaVillage", "ShellsTown", "OrangeTown", "SyrupVillage", "Baratie",
            "ArlongPark", "Loguetown", "ReverseMountain", "WhiskeyPeak", "LittleGarden",
            "DrumIsland", "Alabasta", "Jaya", "Skypiea", "Water7", "EniesLobby",
            "ThrillerBark", "Sabaody", "AmazonLily", "ImpelDown", "Marineford",
            "FishManIsland", "PunkHazard", "Dressrosa", "Zou", "WholeCakeIsland",
            "WanoKuni", "Onigashima");

    private MerchantNpcEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 40 != 0
                || player.level().dimension() != net.minecraft.world.level.Level.OVERWORLD) return;

        ServerLevel level = player.serverLevel();
        for (String islandId : ISLANDS) {
            var island = IslandCatalog.find(islandId).orElse(null);
            if (island == null || player.distanceToSqr(island.x() + .5D, player.getY(),
                    island.z() + .5D) > 96.0D * 96.0D) continue;

            BlockPos pos = new BlockPos(island.x() + 4, island.safeY(), island.z() + 4);
            level.getChunkAt(pos);
            String tag = TAG_PREFIX + islandId;
            AABB area = new AABB(pos).inflate(10.0D, 6.0D, 10.0D);
            if (!level.getEntitiesOfClass(Villager.class, area,
                    villager -> villager.getTags().contains(tag)).isEmpty()) continue;

            Villager merchant = EntityType.VILLAGER.create(level);
            if (merchant == null) continue;
            merchant.moveTo(pos.getX() + .5D, pos.getY(), pos.getZ() + .5D, 90.0F, 0.0F);
            merchant.setVillagerData(merchant.getVillagerData()
                    .setType(villagerType(islandId))
                    .setProfession(profession(islandId))
                    .setLevel(2));
            merchant.setCustomName(Component.literal(merchantName(islandId)));
            merchant.setCustomNameVisible(true);
            merchant.setPersistenceRequired();
            merchant.addTag(tag);
            merchant.setPersistenceRequired();
            level.addFreshEntity(merchant);
        }
    }

    @SubscribeEvent
    public static void interact(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND
                || !(event.getTarget() instanceof Villager merchant)
                || !(event.getEntity() instanceof ServerPlayer player)) return;
        String islandId = merchant.getTags().stream().filter(tag -> tag.startsWith(TAG_PREFIX))
                .map(tag -> tag.substring(TAG_PREFIX.length())).findFirst().orElse("");
        if (islandId.isBlank()) return;
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        de.nexusbay.forge.network.MerchantNetwork.open(player, islandId,
                merchant.getCustomName() == null ? "Inselhändler" : merchant.getCustomName().getString());
    }

    private static VillagerProfession profession(String island) {
        return switch (island) {
            case "FooshaVillage", "Baratie", "WholeCakeIsland" -> VillagerProfession.BUTCHER;
            case "ShellsTown", "Marineford", "EniesLobby", "ImpelDown" -> VillagerProfession.ARMORER;
            case "OrangeTown", "SyrupVillage", "Dressrosa" -> VillagerProfession.LEATHERWORKER;
            case "Loguetown", "Water7", "WanoKuni", "Onigashima" -> VillagerProfession.WEAPONSMITH;
            case "DrumIsland", "PunkHazard", "LittleGarden" -> VillagerProfession.CLERIC;
            case "Alabasta", "WhiskeyPeak", "Jaya", "Sabaody" -> VillagerProfession.FLETCHER;
            case "Skypiea", "FishManIsland", "AmazonLily", "Zou" -> VillagerProfession.FISHERMAN;
            default -> VillagerProfession.TOOLSMITH;
        };
    }

    private static VillagerType villagerType(String island) {
        return switch (island) {
            case "Alabasta", "WholeCakeIsland" -> VillagerType.DESERT;
            case "DrumIsland", "PunkHazard" -> VillagerType.SNOW;
            case "WanoKuni", "Zou" -> VillagerType.TAIGA;
            case "Jaya", "Skypiea" -> VillagerType.JUNGLE;
            case "FishManIsland", "Sabaody" -> VillagerType.SWAMP;
            default -> VillagerType.PLAINS;
        };
    }

    private static String merchantName(String island) {
        return switch (island) {
            case "FooshaVillage" -> "Makinos Markt";
            case "ShellsTown", "Marineford" -> "Marine-Ausrüster";
            case "OrangeTown" -> "Buggy-Schwarzmarkt";
            case "SyrupVillage" -> "Dorfhändler";
            case "Baratie" -> "Baratie-Koch";
            case "ArlongPark", "FishManIsland" -> "Händler der Fischmenschen";
            case "Loguetown" -> "Loguetown-Waffenhändler";
            case "DrumIsland" -> "Winter-Apotheke";
            case "Alabasta" -> "Wüstenbasar";
            case "Skypiea" -> "Himmelswaren";
            case "Water7" -> "Galley-La-Ausrüster";
            case "Sabaody" -> "Blasenhändler";
            case "PunkHazard" -> "Labor-Ausrüster";
            case "Dressrosa" -> "Spielzeugmarkt";
            case "WanoKuni", "Onigashima" -> "Wano-Waffenschmied";
            case "WholeCakeIsland" -> "Süßwarenhändler";
            default -> "Händler von " + IslandCatalog.find(island).map(i -> i.displayName()).orElse(island);
        };
    }
}
