package de.nexusbay.forge.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent server-side Beli balances.
 *
 * Stored in the overworld SavedData so balances survive logout and server
 * restarts, and are shared consistently across dimensions.
 */
public final class BeliLedger extends SavedData {
    private static final String DATA_NAME = "nexusbay_beli";
    private final Map<UUID, Long> balances = new HashMap<>();

    public static BeliLedger get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                BeliLedger::load,
                BeliLedger::new,
                DATA_NAME
        );
    }

    public static BeliLedger load(CompoundTag tag) {
        BeliLedger ledger = new BeliLedger();
        CompoundTag entries = tag.getCompound("Balances");
        for (String key : entries.getAllKeys()) {
            try {
                long amount = Math.max(0L, entries.getLong(key));
                ledger.balances.put(UUID.fromString(key), amount);
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed UUID entries rather than preventing world load.
            }
        }
        return ledger;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag entries = new CompoundTag();
        balances.forEach((uuid, amount) -> entries.putLong(uuid.toString(), Math.max(0L, amount)));
        tag.put("Balances", entries);
        return tag;
    }

    public long balance(UUID playerId) {
        return playerId == null ? 0L : balances.getOrDefault(playerId, 0L);
    }

    public long balance(ServerPlayer player) {
        return player == null ? 0L : balance(player.getUUID());
    }

    public long deposit(UUID playerId, long amount) {
        if (playerId == null || amount <= 0L) return balance(playerId);
        long current = balance(playerId);
        long updated = current > Long.MAX_VALUE - amount ? Long.MAX_VALUE : current + amount;
        if (updated != current) {
            balances.put(playerId, updated);
            setDirty();
        }
        return updated;
    }

    public boolean withdraw(UUID playerId, long amount) {
        if (playerId == null || amount < 0L) return false;
        if (amount == 0L) return true;
        long current = balance(playerId);
        if (current < amount) return false;
        balances.put(playerId, current - amount);
        setDirty();
        return true;
    }

    public boolean transfer(UUID from, UUID to, long amount) {
        if (from == null || to == null || amount <= 0L || from.equals(to)
                || balance(from) < amount) return false;
        long recipient = balance(to);
        if (recipient > Long.MAX_VALUE - amount) return false;
        balances.put(from, balance(from) - amount);
        balances.put(to, recipient + amount);
        setDirty();
        return true;
    }
}
}
