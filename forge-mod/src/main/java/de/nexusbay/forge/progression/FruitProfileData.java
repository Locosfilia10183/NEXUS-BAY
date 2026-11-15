package de.nexusbay.forge.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent active-fruit ownership and mastery, stored once per world. */
public final class FruitProfileData extends SavedData {
    private static final String DATA_NAME = "nexusbay_fruit_profiles";
    private final Map<UUID, Profile> profiles = new HashMap<>();

    public record Profile(String fruitId, int level, long experience) {
        public Profile {
            level = Math.max(1, Math.min(100, level));
            experience = Math.max(0L, experience);
        }
    }

    public static FruitProfileData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                FruitProfileData::load, FruitProfileData::new, DATA_NAME);
    }

    public static FruitProfileData load(CompoundTag tag) {
        FruitProfileData data = new FruitProfileData();
        CompoundTag entries = tag.getCompound("Profiles");
        for (String key : entries.getAllKeys()) {
            try {
                CompoundTag value = entries.getCompound(key);
                String fruit = value.getString("Fruit");
                if (!fruit.isBlank()) data.profiles.put(UUID.fromString(key),
                        new Profile(fruit, value.getInt("Level"), value.getLong("Experience")));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed profile entries.
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag entries = new CompoundTag();
        profiles.forEach((uuid, profile) -> {
            CompoundTag value = new CompoundTag();
            value.putString("Fruit", profile.fruitId());
            value.putInt("Level", profile.level());
            value.putLong("Experience", profile.experience());
            entries.put(uuid.toString(), value);
        });
        tag.put("Profiles", entries);
        return tag;
    }

    public Profile get(UUID playerId) { return profiles.get(playerId); }

    public void set(UUID playerId, String fruitId) {
        profiles.put(playerId, new Profile(fruitId, 1, 0L));
        setDirty();
    }

    public boolean reset(UUID playerId) {
        if (profiles.remove(playerId) == null) return false;
        setDirty();
        return true;
    }
}
