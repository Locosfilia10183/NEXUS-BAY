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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/** Spawns Marine officers and provides persistent, server-authoritative enlistment. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class MarineOfficerEvents {
    private static final String TAG_PREFIX = "nexusbay_marine_officer_";
    private static final List<String> MARINE_ISLANDS =
            List.of("ShellsTown", "Loguetown", "EniesLobby", "Marineford");

    private MarineOfficerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 40 != 0
                || player.level().dimension() != net.minecraft.world.level.Level.OVERWORLD) return;

        ServerLevel level = player.serverLevel();
        for (String islandId : MARINE_ISLANDS) {
            var island = IslandCatalog.find(islandId).orElse(null);
            if (island == null || player.distanceToSqr(island.x() + .5D, player.getY(),
                    island.z() + .5D) > 96.0D * 96.0D) continue;

            BlockPos pos = new BlockPos(island.x() - 7, island.safeY(), island.z() + 5);
            level.getChunkAt(pos);
            String tag = TAG_PREFIX + islandId;
            AABB area = new AABB(pos).inflate(12.0D, 7.0D, 12.0D);
            if (!level.getEntitiesOfClass(Villager.class, area,
                    villager -> villager.getTags().contains(tag)).isEmpty()) continue;

            Villager officer = EntityType.VILLAGER.create(level);
            if (officer == null) continue;
            officer.moveTo(pos.getX() + .5D, pos.getY(), pos.getZ() + .5D, 0.0F, 0.0F);
            officer.setVillagerData(officer.getVillagerData()
                    .setProfession(VillagerProfession.ARMORER).setLevel(3));
            officer.setCustomName(Component.literal(officerName(islandId)));
            officer.setCustomNameVisible(true);
            officer.setNoAi(true);
            officer.setInvulnerable(true);
            officer.setPersistenceRequired();
            officer.addTag(tag);
            level.addFreshEntity(officer);
        }
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND
                || !(event.getTarget() instanceof Villager officer)
                || !(event.getEntity() instanceof ServerPlayer player)) return;

        String islandId = officer.getTags().stream().filter(tag -> tag.startsWith(TAG_PREFIX))
                .map(tag -> tag.substring(TAG_PREFIX.length())).findFirst().orElse("");
        if (islandId.isBlank()) return;

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if (!officer.isAlive() || officer.level() != player.level()
                || player.distanceToSqr(officer) > 36.0D) {
            player.displayClientMessage(Component.literal("Du bist zu weit vom Marine-Offizier entfernt."), true);
            return;
        }

        if (player.getPersistentData().getBoolean("nexusbay_marine_member")) {
            player.sendSystemMessage(Component.literal("Marine-Rekrut: Deine Einheit erwartet weitere Befehle.").withStyle(net.minecraft.ChatFormatting.BLUE));
            return;
        }

        player.getPersistentData().putBoolean("nexusbay_marine_member", true);
        player.getPersistentData().putString("nexusbay_faction", "marine");
        player.getPersistentData().putString("nexusbay_marine_rank", "Rekrut");
        player.getPersistentData().putString("nexusbay_marine_origin", islandId);
        player.sendSystemMessage(Component.literal("§9[MARINE] §fDu bist jetzt Rekrut der Marine!"));
        player.sendSystemMessage(Component.literal("§7Deine Zugehörigkeit bleibt nach Logout und Serverneustart gespeichert."));
        player.playNotifySound(net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static String officerName(String islandId) {
        return switch (islandId) {
            case "ShellsTown" -> "Marine-Rekrutierer";
            case "Loguetown" -> "Marine-Unteroffizier";
            case "EniesLobby" -> "Marine-Offizier";
            case "Marineford" -> "Marine-Kommandant";
            default -> "Marine-Offizier";
        };
    }
}
