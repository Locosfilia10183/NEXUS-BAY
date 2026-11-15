package de.nexusbay.forge.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent active and completed island quests, shared across dimensions. */
public final class IslandQuestData extends SavedData {
    private static final String DATA_NAME = "nexusbay_island_quests";
    private final Map<UUID, String> active = new HashMap<>();
    private final Map<UUID, Integer> progress = new HashMap<>();
    private final Map<UUID, String> completed = new HashMap<>();

    public static IslandQuestData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(IslandQuestData::load, IslandQuestData::new, DATA_NAME);
    }
    public static IslandQuestData load(CompoundTag tag) {
        IslandQuestData data = new IslandQuestData();
        CompoundTag a = tag.getCompound("Active"), p = tag.getCompound("Progress"), c = tag.getCompound("Completed");
        for (String key : a.getAllKeys()) try { data.active.put(UUID.fromString(key), a.getString(key)); } catch (IllegalArgumentException ignored) {}
        for (String key : p.getAllKeys()) try { data.progress.put(UUID.fromString(key), Math.max(0, p.getInt(key))); } catch (IllegalArgumentException ignored) {}
        for (String key : c.getAllKeys()) try { data.completed.put(UUID.fromString(key), c.getString(key)); } catch (IllegalArgumentException ignored) {}
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        CompoundTag a = new CompoundTag(), p = new CompoundTag(), c = new CompoundTag();
        active.forEach((id, value) -> a.putString(id.toString(), value));
        progress.forEach((id, value) -> p.putInt(id.toString(), value));
        completed.forEach((id, value) -> c.putString(id.toString(), value));
        tag.put("Active", a); tag.put("Progress", p); tag.put("Completed", c); return tag;
    }
    public String active(UUID id) { return active.getOrDefault(id, ""); }
    public int progress(UUID id) { return progress.getOrDefault(id, 0); }
    public boolean completed(UUID id, String quest) { return completed.getOrDefault(id, "").equals(quest); }
    public boolean accept(UUID id, String quest) {
        if (!active(id).isBlank() || completed(id, quest)) return false;
        active.put(id, quest); progress.put(id, 0); setDirty(); return true;
    }
    public int addKill(UUID id) {
        if (active(id).isBlank()) return 0;
        int value = progress(id) + 1; progress.put(id, value); setDirty(); return value;
    }
    public boolean claim(UUID id, String quest) {
        if (!active(id).equals(quest)) return false;
        active.remove(id); progress.remove(id); completed.put(id, quest); setDirty(); return true;
    }
}
