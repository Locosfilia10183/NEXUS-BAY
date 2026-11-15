package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.IslandFactionCatalog;
import de.nexusbay.forge.world.IslandCatalog;
import de.nexusbay.forge.world.IslandTerrainCatalog;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Marks naturally spawned hostile mobs with the faction controlling their island. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandFactionEvents {
    private static final String FACTION_KEY = "nexusbay_faction";
    private static final String ISLAND_KEY = "nexusbay_faction_island";
    private IslandFactionEvents() {}

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!(event.getEntity() instanceof Monster monster) || monster.level().isClientSide) return;
        if (monster.getPersistentData().contains(ISLAND_KEY)) return;
        int x=monster.blockPosition().getX(), z=monster.blockPosition().getZ();
        for (var island : IslandCatalog.all()) {
            var profile=IslandTerrainCatalog.find(island.id()).orElse(null);
            var faction=IslandFactionCatalog.find(island.id());
            if(profile==null||faction==null)continue;
            double dx=x-island.x(), dz=z-island.z();
            double radius=profile.radius()+12.0D;
            if(dx*dx+dz*dz>radius*radius)continue;
            monster.getPersistentData().putString(FACTION_KEY,faction.id());
            monster.getPersistentData().putString(ISLAND_KEY,island.id());
            if(!monster.hasCustomName()){
                monster.setCustomName(Component.literal(faction.displayName()));
                monster.setCustomNameVisible(true);
            }
            return;
        }
    }
}
