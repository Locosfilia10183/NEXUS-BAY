package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.network.StarterKitNetwork;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** One-time starter equipment, granted only after explicit menu acceptance. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class StarterIslandKit {
    private static final String GRANTED_KEY = "nexusbay_starter_kit_granted";

    private StarterIslandKit() {}

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        CompoundTag oldData = event.getOriginal().getPersistentData();
        CompoundTag newData = event.getEntity().getPersistentData();
        CompoundTag oldSaved = oldData.getCompound(Player.PERSISTED_NBT_TAG);
        if (oldSaved.getBoolean(GRANTED_KEY) || oldData.getBoolean(GRANTED_KEY)) {
            CompoundTag newSaved = newData.getCompound(Player.PERSISTED_NBT_TAG);
            newSaved.putBoolean(GRANTED_KEY, true);
            newData.put(Player.PERSISTED_NBT_TAG, newSaved);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag persistent = player.getPersistentData();
        CompoundTag saved = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (saved.getBoolean(GRANTED_KEY)) return;

        // Existing players who received the old automatic kit must not receive it twice.
        if (persistent.getBoolean(GRANTED_KEY)) {
            saved.putBoolean(GRANTED_KEY, true);
            persistent.put(Player.PERSISTED_NBT_TAG, saved);
            return;
        }

        StarterKitNetwork.open(player);
    }

    /** Called only by the server packet after the player presses the accept button. */
    public static void accept(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag saved = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (saved.getBoolean(GRANTED_KEY) || persistent.getBoolean(GRANTED_KEY)) return;

        // Persist before granting, preventing duplicate kits on reconnect or respawn.
        saved.putBoolean(GRANTED_KEY, true);
        persistent.put(Player.PERSISTED_NBT_TAG, saved);

        give(player, new ItemStack(ModItems.THREE_SWORD_STYLE_KIT.get()));
        give(player, new ItemStack(ModItems.WADO_ICHIMONJI.get()));
        give(player, new ItemStack(ModItems.SANDai_KITETSU.get()));
        give(player, new ItemStack(ModItems.YUBASHIRI.get()));
        give(player, new ItemStack(ModItems.DEVIL_FRUIT_CASE.get()));

        player.sendSystemMessage(Component.literal("Starterpaket angenommen! Viel Erfolg in NEXUS-BAY!")
                .withStyle(net.minecraft.ChatFormatting.GREEN));
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
}
