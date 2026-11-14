package de.nexusbay.forge.command;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.KaidoBossEntity;
import de.nexusbay.forge.registry.ModEntities;

/**
 * Admin summon command for the Kaido encounter.
 * The canonical Onigashima anchor matches the island atlas / Paper island slot.
 */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class KaidoCommands {
    private static final double ONIGASHIMA_X = 1472.5D;
    private static final double ONIGASHIMA_Y = 101.0D;
    private static final double ONIGASHIMA_Z = 1472.5D;

    private KaidoCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("kaido")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("spawn")
                                .executes(context -> spawn(context.getSource().getLevel(),
                                        context.getSource().getPosition().x,
                                        context.getSource().getPosition().y,
                                        context.getSource().getPosition().z,
                                        context.getSource()::sendSuccess))
                                .then(Commands.literal("onigashima")
                                        .executes(context -> spawn(context.getSource().getLevel(),
                                                ONIGASHIMA_X, ONIGASHIMA_Y, ONIGASHIMA_Z,
                                                context.getSource()::sendSuccess))))
        );
    }

    private static int spawn(ServerLevel level, double x, double y, double z,
                             java.util.function.Consumer<java.util.function.Supplier<Component>> feedback) {
        AABB area = new AABB(x - 12, y - 8, z - 12, x + 12, y + 12, z + 12);
        if (!level.getEntitiesOfClass(KaidoBossEntity.class, area).isEmpty()) {
            feedback.accept(() -> Component.literal("Kaido ist in diesem Gebiet bereits anwesend."));
            return 0;
        }
        Entity entity = ModEntities.KAIDO.get().create(level);
        if (!(entity instanceof KaidoBossEntity kaido)) {
            feedback.accept(() -> Component.literal("Kaido konnte nicht erstellt werden."));
            return 0;
        }
        kaido.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        if (!level.addFreshEntity(kaido)) {
            feedback.accept(() -> Component.literal("Kaido konnte nicht in die Welt gesetzt werden."));
            return 0;
        }
        feedback.accept(() -> Component.literal("KAIDO – König der Bestien wurde beschworen!"));
        return Command.SINGLE_SUCCESS;
    }
}
