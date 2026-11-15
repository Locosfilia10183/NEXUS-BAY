package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** One-time starter equipment grant for each player profile. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class StarterIslandKit {
    private static final String GRANTED_KEY = "nexusbay_starter_kit_granted";

    private StarterIslandKit() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getPersistentData().getBoolean(GRANTED_KEY)) return;

        give(player, new ItemStack(ModItems.THREE_SWORD_STYLE_KIT.get()));
        give(player, new ItemStack(ModItems.WADO_ICHIMONJI.get()));
        give(player, new ItemStack(ModItems.SANDai_KITETSU.get()));
        give(player, new ItemStack(ModItems.YUBASHIRI.get()));
        give(player, new ItemStack(ModItems.DEVIL_FRUIT_CASE.get()));

        player.getPersistentData().putBoolean(GRANTED_KEY, true);
        player.sendSystemMessage(Component.literal(
                "Willkommen in NEXUS-BAY! Dein Starterpaket mit drei Schwertern und einer Teufelsfrucht-Kiste wartet auf dich."));
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
}
