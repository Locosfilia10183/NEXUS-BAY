package de.nexusbay.forge.progression;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
/** Persistent active-fruit ownership and mastery, stored in overworld SavedData. */
public final class FruitProfileData extends SavedData {
 private static final String DATA_NAME="nexusbay_fruit_profiles";
 private final Map<UUID,Profile> profiles=new HashMap<>();
 public record Profile(String fruitId,int level,long experience) {
  public Profile { level=Math.max(1,Math.min(100,level)); experience=Math.max(0L,experience); }
 }
 public static FruitProfileData get(ServerLevel level) { return level.getServer().overworld().getDataStorage().computeIfAbsent(FruitProfileData::load,FruitProfileData::new,DATA_NAME); }
 public static FruitProfileData load(CompoundTag tag) {
  FruitProfileData data=new FruitProfileData(); CompoundTag entries=tag.getCompound("Profiles");
  for(String key:entries.getAllKeys()) try { CompoundTag v=entries.getCompound(key); String fruit=v.getString("Fruit"); if(!fruit.isBlank()) data.profiles.put(UUID.fromString(key),new Profile(fruit,v.getInt("Level"),v.getLong("Experience"))); } catch(IllegalArgumentException ignored) {}
  return data;
 }
 @Override public CompoundTag save(CompoundTag tag) {
  CompoundTag entries=new CompoundTag(); profiles.forEach((id,p)->{ CompoundTag v=new CompoundTag(); v.putString("Fruit",p.fruitId()); v.putInt("Level",p.level()); v.putLong("Experience",p.experience()); entries.put(id.toString(),v); }); tag.put("Profiles",entries); return tag;
 }
 public Profile get(UUID id){return profiles.get(id);}
 public void set(UUID id,String fruit){profiles.put(id,new Profile(fruit,1,0));setDirty();}
 public boolean reset(UUID id){if(profiles.remove(id)==null)return false;setDirty();return true;}
 public Profile addExperience(UUID id,long amount){
  Profile old=profiles.get(id); if(old==null||amount<=0)return old;
  long xp=old.experience()>Long.MAX_VALUE-amount?Long.MAX_VALUE:old.experience()+amount;
  int level=old.level(); while(level<100 && xp>=100L*level*level)level++;
  Profile next=new Profile(old.fruitId(),level,xp);profiles.put(id,next);setDirty();return next;
 }
}