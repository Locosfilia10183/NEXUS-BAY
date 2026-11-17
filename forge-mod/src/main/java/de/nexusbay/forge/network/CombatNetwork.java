package de.nexusbay.forge.network;

import de.nexusbay.forge.NexusBayForge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Server-authoritative combat actions. */
public final class CombatNetwork {
    private static final String VERSION = "1";
    private static final String DODGE_COOLDOWN = "nexusbay_dodge_ready_at";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NexusBayForge.MOD_ID, "combat"),
            () -> VERSION, VERSION::equals, VERSION::equals);
    private static boolean registered;

    private CombatNetwork() {}

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerMessage(0, DodgeRequest.class, DodgeRequest::encode, DodgeRequest::decode,
                (message, context) -> context.get().enqueueWork(() -> {
                    ServerPlayer player = context.get().getSender();
                    if (player == null || !player.isAlive() || player.isPassenger()
                            || player.isFallFlying() || player.isInWater()) return;

                    long now = player.serverLevel().getGameTime();
                    long readyAt = player.getPersistentData().getLong(DODGE_COOLDOWN);
                    if (now < readyAt) return;

                    Vec3 look = player.getLookAngle();
                    Vec3 direction = new Vec3(look.x, 0.0D, look.z);
                    if (direction.lengthSqr() < 1.0E-4D) return;
                    direction = direction.normalize();

                    // A short forward burst; the client cannot choose the force or cooldown.
                    Vec3 current = player.getDeltaMovement();
                    player.setDeltaMovement(direction.x * 1.15D,
                            Math.max(current.y, 0.12D), direction.z * 1.15D);
                    player.hurtMarked = true;
                    player.fallDistance = 0.0F;
                    player.getPersistentData().putLong(DODGE_COOLDOWN, now + 30L);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                            player.getSoundSource(), 0.55F, 1.45F);
                }));
    }

    public static void requestDodge() {
        CHANNEL.sendToServer(new DodgeRequest());
    }

    private record DodgeRequest() {
        static void encode(DodgeRequest message, FriendlyByteBuf buffer) {}
        static DodgeRequest decode(FriendlyByteBuf buffer) { return new DodgeRequest(); }
    }
}
