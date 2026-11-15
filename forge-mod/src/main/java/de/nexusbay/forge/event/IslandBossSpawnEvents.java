package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.CrocodileBossEntity;
import de.nexusbay.forge.boss.EnelBossEntity;
import de.nexusbay.forge.boss.RobLucciBossEntity;
import de.nexusbay.forge.registry.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandBossSpawnEvents {
    private IslandBossSpawnEvents() {}
    @SubscribeEvent public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level) || level.dimension() != net.minecraft.world.level.Level.OVERWORLD) return;
        if (player.tickCount % 40 != 0) return;
        trySpawnCrocodile(player, level);
        trySpawnEnel(player, level);
        trySpawnLucci(player, level);
    }
    private static void trySpawnCrocodile(ServerPlayer player, ServerLevel level) {
        trySpawn(player, level, 1472.5D, 82.0D, 832.5D, 78.0D, ModEntities.CROCODILE.get(),
                CrocodileBossEntity.class, "§6CROCODILE §7ist in Alabasta erschienen!");
    }
    private static void trySpawnEnel(ServerPlayer player, ServerLevel level) {
        trySpawn(player, level, 2112.5D, 182.0D, 832.5D, 78.0D, ModEntities.ENEL.get(),
                EnelBossEntity.class, "§eENEL §7ist auf Skypiea erschienen!");
    }
    private static void trySpawnLucci(ServerPlayer player, ServerLevel level) {
        trySpawn(player, level, 2752.5D, 82.0D, 832.5D, 78.0D, ModEntities.ROB_LUCCI.get(),
                RobLucciBossEntity.class, "§cROB LUCCI §7hat Enies Lobby betreten!");
    }
    private static <T extends Entity> void trySpawn(ServerPlayer player, ServerLevel level,
            double x, double y, double z, double triggerRadius, net.minecraft.world.entity.EntityType<T> type,
            Class<T> bossClass, String message) {
        double dx = player.getX() - x, dz = player.getZ() - z;
        if (dx * dx + dz * dz > triggerRadius * triggerRadius) return;
        AABB area = new AABB(x - 96, y - 48, z - 96, x + 96, y + 48, z + 96);
        if (!level.getEntitiesOfClass(bossClass, area).isEmpty()) return;
        T boss = type.create(level);
        if (boss == null) return;
        boss.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        if (level.addFreshEntity(boss)) {
            player.displayClientMessage(Component.literal(message), false);
        }
    }
}
