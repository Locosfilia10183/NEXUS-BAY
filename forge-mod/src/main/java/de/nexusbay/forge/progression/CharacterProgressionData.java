package de.nexusbay.forge.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent character level and allocatable combat stats, independent from fruit mastery. */
public final class CharacterProgressionData extends SavedData {
    private static final String DATA_NAME = "nexusbay_character_progression";
    private final Map<UUID, Profile> profiles = new HashMap<>();

    public record Profile(int level, long experience, int unspentPoints,
                          int strength, int defense, int stamina, int sword, int fruit) {
        public Profile {
            level = Math.max(1, Math.min(200, level));
            experience = Math.max(0L, experience);
            unspentPoints = Math.max(0, unspentPoints);
            strength = clampStat(strength);
            defense = clampStat(defense);
            stamina = clampStat(stamina);
            sword = clampStat(sword);
            fruit = clampStat(fruit);
        }
        private static int clampStat(int value) { return Math.max(0, Math.min(100, value)); }
        public int totalAllocated() { return strength + defense + stamina + sword + fruit; }
    }

    public static CharacterProgressionData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(CharacterProgressionData::load, CharacterProgressionData::new, DATA_NAME);
    }

    public static CharacterProgressionData load(CompoundTag tag) {
        CharacterProgressionData data = new CharacterProgressionData();
        CompoundTag entries = tag.getCompound("Profiles");
        for (String key : entries.getAllKeys()) {
            try {
                CompoundTag v = entries.getCompound(key);
                data.profiles.put(UUID.fromString(key), new Profile(
                        v.getInt("Level"), v.getLong("Experience"), v.getInt("Unspent"),
                        v.getInt("Strength"), v.getInt("Defense"), v.getInt("Stamina"),
                        v.getInt("Sword"), v.getInt("Fruit")));
            } catch (IllegalArgumentException ignored) { }
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        CompoundTag entries = new CompoundTag();
        profiles.forEach((id, p) -> {
            CompoundTag v = new CompoundTag();
            v.putInt("Level", p.level()); v.putLong("Experience", p.experience());
            v.putInt("Unspent", p.unspentPoints()); v.putInt("Strength", p.strength());
            v.putInt("Defense", p.defense()); v.putInt("Stamina", p.stamina());
            v.putInt("Sword", p.sword()); v.putInt("Fruit", p.fruit());
            entries.put(id.toString(), v);
        });
        tag.put("Profiles", entries);
        return tag;
    }

    public Profile get(UUID id) {
        return profiles.computeIfAbsent(id, ignored -> {
            setDirty();
            return new Profile(1, 0L, 0, 0, 0, 0, 0, 0);
        });
    }

    public Profile addExperience(UUID id, long amount) {
        if (amount <= 0) return get(id);
        Profile old = get(id);
        long xp = old.experience() > Long.MAX_VALUE - amount ? Long.MAX_VALUE : old.experience() + amount;
        int level = old.level();
        int points = old.unspentPoints();
        while (level < 200 && xp >= 250L * level * level) {
            level++;
            points += 3;
        }
        Profile next = new Profile(level, xp, points, old.strength(), old.defense(),
                old.stamina(), old.sword(), old.fruit());
        profiles.put(id, next);
        setDirty();
        return next;
    }

    public Profile allocate(UUID id, String stat, int points) {
        if (points < 1 || points > 100) return null;
        Profile old = get(id);
        if (old.unspentPoints() < points) return null;
        int strength = old.strength(), defense = old.defense(), stamina = old.stamina();
        int sword = old.sword(), fruit = old.fruit();
        switch (stat.toLowerCase(java.util.Locale.ROOT)) {
            case "strength" -> strength += points;
            case "defense" -> defense += points;
            case "stamina" -> stamina += points;
            case "sword" -> sword += points;
            case "fruit" -> fruit += points;
            default -> { return null; }
        }
        if (strength > 100 || defense > 100 || stamina > 100 || sword > 100 || fruit > 100) return null;
        Profile next = new Profile(old.level(), old.experience(), old.unspentPoints() - points,
                strength, defense, stamina, sword, fruit);
        profiles.put(id, next);
        setDirty();
        return next;
    }
}
