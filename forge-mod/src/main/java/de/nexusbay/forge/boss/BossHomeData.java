package de.nexusbay.forge.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent home-boss respawn state, stored in the overworld save. */
public final class BossHomeData extends SavedData {
    private static final String DATA_NAME = "nexusbay_boss_home_state";
    private static final String KAIDO_DEFEATED_AT = "kaido_defeated_at";
    private static final long RESPAWN_DELAY_TICKS = 36_000L; // 30 minutes

    private long kaidoDefeatedAt = -1L;

    public static BossHomeData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                BossHomeData::load, BossHomeData::new, DATA_NAME);
    }

    public static BossHomeData load(CompoundTag tag) {
        BossHomeData data = new BossHomeData();
        data.kaidoDefeatedAt = tag.getLong(KAIDO_DEFEATED_AT);
        if (!tag.contains(KAIDO_DEFEATED_AT)) data.kaidoDefeatedAt = -1L;
        return data;
    }

    public void markKaidoDefeated(long gameTime) {
        kaidoDefeatedAt = gameTime;
        setDirty();
    }

    public boolean canSpawnKaido(long gameTime) {
        return kaidoDefeatedAt < 0L || gameTime - kaidoDefeatedAt >= RESPAWN_DELAY_TICKS;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong(KAIDO_DEFEATED_AT, kaidoDefeatedAt);
        return tag;
    }
}
