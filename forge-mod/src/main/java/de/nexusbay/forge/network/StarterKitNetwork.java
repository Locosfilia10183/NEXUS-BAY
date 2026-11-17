package de.nexusbay.forge.network;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.event.StarterIslandKit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** First-join welcome screen and server-authoritative starter kit acceptance. */
public final class StarterKitNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NexusBayForge.MOD_ID, "starter_kit"),
            () -> VERSION, VERSION::equals, VERSION::equals);
    private static boolean registered;

    private StarterKitNetwork() {}

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, OpenMenu.class, OpenMenu::encode, OpenMenu::decode,
                (m, c) -> c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT, () -> () -> de.nexusbay.forge.client.StarterKitScreen.open())));
        CHANNEL.registerMessage(1, AcceptKit.class, AcceptKit::encode, AcceptKit::decode,
                (m, c) -> c.get().enqueueWork(() -> {
                    ServerPlayer player = c.get().getSender();
                    if (player != null) StarterIslandKit.accept(player);
                }));
    }

    public static void open(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenMenu());
    }

    public static void accept() {
        CHANNEL.sendToServer(new AcceptKit());
    }

    private record OpenMenu() {
        static void encode(OpenMenu m, FriendlyByteBuf b) {}
        static OpenMenu decode(FriendlyByteBuf b) { return new OpenMenu(); }
    }

    private record AcceptKit() {
        static void encode(AcceptKit m, FriendlyByteBuf b) {}
        static AcceptKit decode(FriendlyByteBuf b) { return new AcceptKit(); }
    }
}
