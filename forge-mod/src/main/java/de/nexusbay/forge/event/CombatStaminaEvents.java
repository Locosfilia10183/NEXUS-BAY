package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.network.CombatNetwork;
import de.nexusbay.forge.progression.CharacterProgressionData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Persistent per-player stamina pool used by dodge and guard actions. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class CombatStaminaEvents {
    private static final String CURRENT = "nexusbay_combat_stamina";
    private static final String TICK = "nexusbay_combat_stamina_tick";

    private CombatStaminaEvents() {}

    public static float maximum(ServerPlayer player) {
        int staminaStat = CharacterProgressionData.get(player.serverLevel())
                .get(player.getUUID()).stamina();
        return 100.0F + staminaStat * 5.0F;
    }

    public static float current(ServerPlayer player) {
        if (!player.getPersistentData().contains(CURRENT)) {
            player.getPersistentData().putFloat(CURRENT, maximum(player));
        }
        return Math.min(maximum(player), Math.max(0.0F, player.getPersistentData().getFloat(CURRENT)));
    }

    public static boolean spend(ServerPlayer player, float amount) {
        float value = current(player);
        if (value < amount) return false;
        player.getPersistentData().putFloat(CURRENT, value - amount);
        return true;
    }

    public static void restore(ServerPlayer player, float amount) {
        player.getPersistentData().putFloat(CURRENT, Math.min(maximum(player), current(player) + amount));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        long tick = player.serverLevel().getGameTime();
        float value = current(player);
        if (CombatNetwork.isBlocking(player)) {
            if (tick % 5L == 0L && !spend(player, 1.0F)) {
                player.getPersistentData().putBoolean("nexusbay_blocking", false);
            }
        } else if (tick % 2L == 0L && value < maximum(player)) {
            restore(player, 1.0F);
        }
        player.getPersistentData().putLong(TICK, tick);
    }
}
