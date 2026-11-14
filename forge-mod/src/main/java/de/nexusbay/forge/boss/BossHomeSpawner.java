package de.nexusbay.forge.boss;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Automatically maintains the Kaido encounter at his home island.
 * The home chunk is never force-loaded: the boss is created when the island
 * chunk is naturally loaded by a player. Entity persistence keeps him saved
 * across chunk unloads and server restarts.
 */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class BossHomeSpawner {
    private static final int CHECK_INTERVAL_TICKS = 100;
    private static final BlockPos KAIDO_HOME = new BlockPos(2752, 101, 1472);
    private static final double HOME_RADIUS = 48.0D;
    private static long lastCheck = 0L;

    private BossHomeSpawner() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime - lastCheck < CHECK_INTERVAL_TICKS) return;
        lastCheck = gameTime;

        ServerLevel level = event.getServer().overworld();
        // Do not load or force-load a remote island chunk just to create a boss.
        if (!level.hasChunkAt(KAIDO_HOME)) return;

        AABB homeArea = new AABB(
                KAIDO_HOME.getX() - HOME_RADIUS, KAIDO_HOME.getY() - 32.0D,
                KAIDO_HOME.getZ() - HOME_RADIUS,
                KAIDO_HOME.getX() + HOME_RADIUS, KAIDO_HOME.getY() + 64.0D,
                KAIDO_HOME.getZ() + HOME_RADIUS
        );
        if (!level.getEntitiesOfClass(KaidoBossEntity.class, homeArea).isEmpty()) return;

        Entity created = ModEntities.KAIDO.get().create(level);
        if (!(created instanceof KaidoBossEntity kaido)) return;

        kaido.moveTo(KAIDO_HOME.getX() + 0.5D, KAIDO_HOME.getY(),
                KAIDO_HOME.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        kaido.setPersistenceRequired();
        kaido.setArenaBounds(KAIDO_HOME.getX() + 0.5D, KAIDO_HOME.getZ() + 0.5D, 42.0D);
        level.addFreshEntity(kaido);
    }
}
