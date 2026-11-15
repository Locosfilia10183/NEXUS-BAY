package de.nexusbay.forge.command;

import com.mojang.brigadier.Command;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.CrocodileBossEntity;
import de.nexusbay.forge.boss.EnelBossEntity;
import de.nexusbay.forge.registry.ModEntities;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandBossCommands {
    private IslandBossCommands() {}
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("nexusboss")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("crocodile").executes(ctx -> spawnCrocodile(ctx.getSource().getLevel(), ctx.getSource()::sendSuccess)))
                .then(Commands.literal("enel").executes(ctx -> spawnEnel(ctx.getSource().getLevel(), ctx.getSource()::sendSuccess))));
    }
    public static int spawnCrocodile(ServerLevel level, java.util.function.BiConsumer<java.util.function.Supplier<Component>, Boolean> feedback) {
        return spawn(level, 1472.5D, 82.0D, 832.5D, ModEntities.CROCODILE.get(), CrocodileBossEntity.class, "CROCODILE", feedback);
    }
    public static int spawnEnel(ServerLevel level, java.util.function.BiConsumer<java.util.function.Supplier<Component>, Boolean> feedback) {
        return spawn(level, 2112.5D, 182.0D, 832.5D, ModEntities.ENEL.get(), EnelBossEntity.class, "ENEL", feedback);
    }
    private static <T extends Entity> int spawn(ServerLevel level, double x, double y, double z,
            net.minecraft.world.entity.EntityType<T> type, Class<T> bossClass, String name,
            java.util.function.BiConsumer<java.util.function.Supplier<Component>, Boolean> feedback) {
        AABB area = new AABB(x - 48, y - 24, z - 48, x + 48, y + 24, z + 48);
        if (!level.getEntitiesOfClass(bossClass, area).isEmpty()) {
            feedback.accept(() -> Component.literal(name + " ist bereits auf seiner Insel."), false);
            return 0;
        }
        T boss = type.create(level);
        if (boss == null) {
            feedback.accept(() -> Component.literal(name + " konnte nicht erstellt werden."), false);
            return 0;
        }
        boss.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        if (!level.addFreshEntity(boss)) {
            feedback.accept(() -> Component.literal(name + " konnte nicht gespawnt werden."), false);
            return 0;
        }
        feedback.accept(() -> Component.literal(name + " ist erschienen!"), true);
        return Command.SINGLE_SUCCESS;
    }
}
