package de.nexusbay.forge.network;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.event.CombatStyleEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Server-authoritative combat style selection. */
public final class CombatStyleNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NexusBayForge.MOD_ID, "combat_style"),
            () -> VERSION, VERSION::equals, VERSION::equals);
    private static boolean registered;

    private CombatStyleNetwork() {}

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, CycleStyleRequest.class,
                CycleStyleRequest::encode, CycleStyleRequest::decode,
                (message, context) -> context.get().enqueueWork(() -> {
                    ServerPlayer player = context.get().getSender();
                    if (player != null && player.isAlive()) CombatStyleEvents.cycle(player);
                }));
    }

    public static void cycleStyle() {
        CHANNEL.sendToServer(new CycleStyleRequest());
    }

    private record CycleStyleRequest() {
        static void encode(CycleStyleRequest message, FriendlyByteBuf buffer) {}
        static CycleStyleRequest decode(FriendlyByteBuf buffer) { return new CycleStyleRequest(); }
    }
}
