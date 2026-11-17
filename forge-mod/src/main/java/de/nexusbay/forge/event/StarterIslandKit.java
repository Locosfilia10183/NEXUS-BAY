package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** One-time starter equipment grant for each player profile. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class StarterIslandKit {
    private static final String GRANTED_KEY = "nexusbay_starter_kit_granted";

    private StarterIslandKit() {}

    /**
     * Keep the one-time grant flag when Forge creates a replacement player
     * entity after death. This prevents players receiving the kit again
     * simply by respawning.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (event.getOriginal().getPersistentData().getBoolean(GRANTED_KEY)) {
            event.getEntity().getPersistentData().putBoolean(GRANTED_KEY, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // PlayerPersisted survives player recreation on death and prevents
        // a second kit after respawn or reconnect.
        CompoundTag persistent = player.getPersistentData();
        CompoundTag saved = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (saved.getBoolean(GRANTED_KEY)) return;
        // Migrate the legacy flag for players who already received a kit.
        if (persistent.getBoolean(GRANTED_KEY)) {
            saved.putBoolean(GRANTED_KEY, true);
            persistent.put(Player.PERSISTED_NBT_TAG, saved);
            return;
        }

        // Record the grant before inserting items to avoid repeated grants.
        saved.putBoolean(GRANTED_KEY, true);
        persistent.put(Player.PERSISTED_NBT_TAG, saved);

        give(player, new ItemStack(ModItems.THREE_SWORD_STYLE_KIT.get()));
        give(player, new ItemStack(ModItems.WADO_ICHIMONJI.get()));
        give(player, new ItemStack(ModItems.SANDai_KITETSU.get()));
        give(player, new ItemStack(ModItems.YUBASHIRI.get()));
        give(player, new ItemStack(ModItems.DEVIL_FRUIT_CASE.get()));

        player.sendSystemMessage(Component.literal(
                "Willkommen in NEXUS-BAY! Dein Starterpaket mit drei Schwertern und einer Teufelsfrucht-Kiste wartet auf dich."));
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
}
