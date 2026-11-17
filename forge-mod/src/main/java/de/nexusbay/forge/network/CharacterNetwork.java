package de.nexusbay.forge.network;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.CharacterProgressionData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Server-authoritative character profile screen and stat allocation. */
public final class CharacterNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NexusBayForge.MOD_ID, "character"),
            () -> VERSION, VERSION::equals, VERSION::equals);
    private static boolean registered;

    private CharacterNetwork() {}

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, SyncProfile.class, SyncProfile::encode, SyncProfile::decode,
                (m, c) -> c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT, () -> () -> de.nexusbay.forge.client.CharacterScreen.show(
                                m.level, m.xp, m.points, m.strength, m.defense, m.stamina, m.sword, m.fruit))));
        CHANNEL.registerMessage(1, AllocateStat.class, AllocateStat::encode, AllocateStat::decode,
                (m, c) -> c.get().enqueueWork(() -> {
                    ServerPlayer player = c.get().getSender();
                    if (player == null) return;
                    CharacterProgressionData data = CharacterProgressionData.get(player.serverLevel());
                    CharacterProgressionData.Profile updated = data.allocate(player.getUUID(), m.stat, m.points);
                    if (updated == null) {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "Nicht genug Punkte oder Stat-Limit 100 erreicht.")
                                .withStyle(net.minecraft.ChatFormatting.RED));
                        sync(player);
                        return;
                    }
                    sync(player);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "+" + m.points + " " + label(m.stat) + " verteilt.")
                            .withStyle(net.minecraft.ChatFormatting.GREEN));
                }));
    }

    public static void open(ServerPlayer player) { sync(player); }

    public static void allocate(String stat, int points) {
        CHANNEL.sendToServer(new AllocateStat(stat, points));
    }

    private static void sync(ServerPlayer player) {
        CharacterProgressionData.Profile p = CharacterProgressionData.get(player.serverLevel()).get(player.getUUID());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncProfile(p.level(), p.experience(), p.unspentPoints(),
                        p.strength(), p.defense(), p.stamina(), p.sword(), p.fruit()));
    }

    private static String label(String stat) {
        return switch (stat) {
            case "strength" -> "Stärke";
            case "defense" -> "Verteidigung";
            case "stamina" -> "Ausdauer";
            case "sword" -> "Schwert";
            case "fruit" -> "Teufelsfrucht";
            default -> stat;
        };
    }

    private record AllocateStat(String stat, int points) {
        static void encode(AllocateStat m, FriendlyByteBuf b) { b.writeUtf(m.stat, 16); b.writeVarInt(m.points); }
        static AllocateStat decode(FriendlyByteBuf b) { return new AllocateStat(b.readUtf(16), b.readVarInt()); }
    }

    private record SyncProfile(int level, long xp, int points, int strength,
                               int defense, int stamina, int sword, int fruit) {
        static void encode(SyncProfile m, FriendlyByteBuf b) {
            b.writeVarInt(m.level); b.writeVarLong(m.xp); b.writeVarInt(m.points);
            b.writeVarInt(m.strength); b.writeVarInt(m.defense); b.writeVarInt(m.stamina);
            b.writeVarInt(m.sword); b.writeVarInt(m.fruit);
        }
        static SyncProfile decode(FriendlyByteBuf b) {
            return new SyncProfile(b.readVarInt(), b.readVarLong(), b.readVarInt(),
                    b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt());
        }
    }
}
