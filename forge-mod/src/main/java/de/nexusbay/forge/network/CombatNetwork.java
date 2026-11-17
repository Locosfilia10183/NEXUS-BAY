package de.nexusbay.forge.network;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.event.CombatStaminaEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Server-authoritative combat actions. */
public final class CombatNetwork {
    private static final String VERSION = "1";
    private static final String DODGE_COOLDOWN = "nexusbay_dodge_ready_at";
    private static final String BLOCKING = "nexusbay_blocking";
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
                            || player.isFallFlying() || player.isInWater()
                            || isBlocking(player) || !CombatStaminaEvents.spend(player, 20.0F)) return;
                    long now = player.serverLevel().getGameTime();
                    if (now < player.getPersistentData().getLong(DODGE_COOLDOWN)) {
                        CombatStaminaEvents.restore(player, 20.0F);
                        return;
                    }
                    Vec3 look = player.getLookAngle();
                    Vec3 direction = new Vec3(look.x, 0.0D, look.z);
                    if (direction.lengthSqr() < 1.0E-4D) {
                        CombatStaminaEvents.restore(player, 20.0F);
                        return;
                    }
                    direction = direction.normalize();
                    Vec3 current = player.getDeltaMovement();
                    player.setDeltaMovement(direction.x * 1.15D,
                            Math.max(current.y, 0.12D), direction.z * 1.15D);
                    player.hurtMarked = true;
                    player.fallDistance = 0.0F;
                    player.getPersistentData().putLong(DODGE_COOLDOWN, now + 30L);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                            player.getSoundSource(), 0.55F, 1.45F);
                }));
        CHANNEL.registerMessage(1, BlockRequest.class, BlockRequest::encode, BlockRequest::decode,
                (message, context) -> context.get().enqueueWork(() -> {
                    ServerPlayer player = context.get().getSender();
                    if (player == null) return;
                    boolean enabled = message.enabled && player.isAlive()
                            && !player.isPassenger() && !player.isFallFlying() && !player.isSwimming()
                            && CombatStaminaEvents.current(player) > 0.0F;
                    player.getPersistentData().putBoolean(BLOCKING, enabled);
                    if (enabled) {
                        player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK,
                                player.getSoundSource(), 0.35F, 1.35F);
                    }
                }));
    }

    public static void requestDodge() { CHANNEL.sendToServer(new DodgeRequest()); }
    public static void setBlocking(boolean enabled) { CHANNEL.sendToServer(new BlockRequest(enabled)); }
    public static boolean isBlocking(ServerPlayer player) {
        return player.getPersistentData().getBoolean(BLOCKING);
    }

    private record DodgeRequest() {
        static void encode(DodgeRequest message, FriendlyByteBuf buffer) {}
        static DodgeRequest decode(FriendlyByteBuf buffer) { return new DodgeRequest(); }
    }
    private record BlockRequest(boolean enabled) {
        static void encode(BlockRequest message, FriendlyByteBuf buffer) { buffer.writeBoolean(message.enabled); }
        static BlockRequest decode(FriendlyByteBuf buffer) { return new BlockRequest(buffer.readBoolean()); }
    }
}
