package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.IslandFactionCatalog;
import de.nexusbay.forge.world.IslandCatalog;
import de.nexusbay.forge.world.IslandTerrainCatalog;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
            boolean elite = monster.getRandom().nextFloat()
                    < (faction.id().equals("onigashima_beasts") ? 0.22F : 0.08F);
            if (elite) {
                monster.getPersistentData().putBoolean("nexusbay_elite", true);
                int level = faction.recommendedLevel();
                String rank;
                double healthScale;
                double damageScale;
                double speedScale;
                int resistance;
                if (level >= 100 && monster.getRandom().nextFloat() < 0.10F) {
                    rank = "Katastrophe";
                    healthScale = 5.0D;
                    damageScale = 2.5D;
                    speedScale = 1.25D;
                    resistance = 1;
                } else if (level >= 60 && monster.getRandom().nextFloat() < 0.30F) {
                    rank = "Kommandant";
                    healthScale = 3.5D;
                    damageScale = 2.0D;
                    speedScale = 1.20D;
                    resistance = 1;
                } else {
                    rank = "Elite";
                    healthScale = 2.5D;
                    damageScale = 1.6D;
                    speedScale = 1.15D;
                    resistance = 0;
                }
                monster.getPersistentData().putString("nexusbay_elite_rank", rank);
                var health = monster.getAttribute(Attributes.MAX_HEALTH);
                if (health != null) {
                    health.setBaseValue(Math.min(2048.0D, health.getBaseValue() * healthScale));
                    monster.setHealth(monster.getMaxHealth());
                }
                var damage = monster.getAttribute(Attributes.ATTACK_DAMAGE);
                if (damage != null) damage.setBaseValue(damage.getBaseValue() * damageScale);
                var speed = monster.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) speed.setBaseValue(speed.getBaseValue() * speedScale);
                monster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, resistance));
                if (!rank.equals("Elite")) monster.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0));
                if (rank.equals("Katastrophe")) monster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
                String color = rank.equals("Katastrophe") ? "§c" : rank.equals("Kommandant") ? "§5" : "§6";
                monster.setCustomName(Component.literal(color + rank + " §f" + faction.displayName()));
            } else if (!monster.hasCustomName()) {
                monster.setCustomName(Component.literal(faction.displayName()));
            }
            monster.setCustomNameVisible(true);
            return;
        }
    }
}
