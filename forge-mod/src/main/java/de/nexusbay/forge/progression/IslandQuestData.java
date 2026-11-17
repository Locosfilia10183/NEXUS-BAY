package de.nexusbay.forge.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/** Persistent active and completed island quests, shared across dimensions. */
public final class IslandQuestData extends SavedData {
    private static final String DATA_NAME = "nexusbay_island_quests";
    private final Map<UUID, String> active = new HashMap<>();
    private final Map<UUID, Integer> progress = new HashMap<>();
    private final Map<UUID, Set<String>> completed = new HashMap<>();

    public static IslandQuestData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(IslandQuestData::load, IslandQuestData::new, DATA_NAME);
    }
    public static IslandQuestData load(CompoundTag tag) {
        IslandQuestData data = new IslandQuestData();
        CompoundTag a = tag.getCompound("Active"), p = tag.getCompound("Progress"), c = tag.getCompound("Completed");
        for (String key : a.getAllKeys()) try { data.active.put(UUID.fromString(key), a.getString(key)); } catch (IllegalArgumentException ignored) {}
        for (String key : p.getAllKeys()) try { data.progress.put(UUID.fromString(key), Math.max(0, p.getInt(key))); } catch (IllegalArgumentException ignored) {}
        for (String key : c.getAllKeys()) try { Set<String> values = new HashSet<>(); String raw = c.getString(key); if (!raw.isBlank()) for (String value : raw.split(",")) if (!value.isBlank()) values.add(value); data.completed.put(UUID.fromString(key), values); } catch (IllegalArgumentException ignored) {}
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        CompoundTag a = new CompoundTag(), p = new CompoundTag(), c = new CompoundTag();
        active.forEach((id, value) -> a.putString(id.toString(), value));
        progress.forEach((id, value) -> p.putInt(id.toString(), value));
        completed.forEach((id, values) -> c.putString(id.toString(), String.join(",", values)));
        tag.put("Active", a); tag.put("Progress", p); tag.put("Completed", c); return tag;
    }
    public String active(UUID id) { return active.getOrDefault(id, ""); }
    public int progress(UUID id) { return progress.getOrDefault(id, 0); }
    public boolean completed(UUID id, String quest) {
        return id != null && quest != null && !quest.isBlank()
                && completed.getOrDefault(id, Set.of()).contains(quest.trim());
    }

    public boolean accept(UUID id, String quest) {
        if (id == null || quest == null || quest.isBlank()) return false;
        String questId = quest.trim();
        if (!active(id).isBlank() || completed(id, questId)) return false;
        active.put(id, questId);
        progress.put(id, 0);
        setDirty();
        return true;
    }

    public int addKill(UUID id) {
        if (id == null || active(id).isBlank()) return 0;
        // Saturate instead of wrapping to a negative value after integer overflow.
        int current = progress(id);
        int value = current == Integer.MAX_VALUE ? Integer.MAX_VALUE : current + 1;
        if (value != current) {
            progress.put(id, value);
            setDirty();
        }
        return value;
    }

    public boolean claim(UUID id, String quest) {
        if (id == null || quest == null || quest.isBlank()) return false;
        String questId = quest.trim();
        if (!active(id).equals(questId)) return false;
        active.remove(id);
        progress.remove(id);
        completed.computeIfAbsent(id, ignored -> new HashSet<>()).add(questId);
        setDirty();
        return true;
    }
}
