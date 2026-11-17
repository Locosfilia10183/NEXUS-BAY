package de.nexusbay.forge.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.world.IslandCatalog;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin navigation for the development atlas.
 * Uses /nexus tp <island|boss>; the vanilla /tp command remains untouched.
 */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandTeleportCommands {
    private static final List<String> EXTRA_DESTINATIONS = List.of("Kaido");

    private IslandTeleportCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("nexus")
                .then(Commands.literal("tp")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("destination", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider
                                        .suggest(destinationNames(), builder))
                                .executes(context -> teleport(context.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(context, "destination"))))));
    }

    private static List<String> destinationNames() {
        List<String> names = new ArrayList<>();
        IslandCatalog.all().forEach(island -> names.add(island.id()));
        names.addAll(EXTRA_DESTINATIONS);
        return names;
    }

    private static int teleport(ServerPlayer player, String requested) {
        if (requested.equalsIgnoreCase("Kaido")) {
            return teleportTo(player, 1472, 1472, 100, "Kaidos Arena (Onigashima)");
        }

        var destination = IslandCatalog.find(requested);
        if (destination.isEmpty()) {
            player.sendSystemMessage(Component.literal("Unbekanntes Inselziel: " + requested));
            return 0;
        }

        var island = destination.get();
        return teleportTo(player, island.x(), island.z(), island.safeY(), island.displayName());
    }

    private static int teleportTo(ServerPlayer player, int x, int z, int plannedY, String label) {
        Level level = player.level();
        BlockPos plannedPosition = new BlockPos(x, plannedY, z);
        if (!level.getWorldBorder().isWithinBounds(plannedPosition)) {
            player.sendSystemMessage(Component.literal(
                    "Teleport abgebrochen: " + label + " liegt außerhalb der Weltgrenze."));
            return 0;
        }

        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 2;
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int y = Math.max(minY, Math.min(maxY, Math.max(plannedY, surfaceY + 2)));

        player.teleportTo(x + 0.5D, y, z + 0.5D);
        player.sendSystemMessage(Component.literal("Teleportiert zu " + label
                + " bei X " + x + " / Y " + y + " / Z " + z
                + ". Hinweis: Inselgenerierung ist noch in Entwicklung."));
        return 1;
    }
}
