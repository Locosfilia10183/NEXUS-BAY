package de.nexusbay.forge.command;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.CharacterProgressionData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-authoritative commands for the NEXUS-BAY character progression profile. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class CharacterProgressionCommands {
    private CharacterProgressionCommands() { }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("nexus")
            .then(Commands.literal("character")
                .then(Commands.literal("info").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    var p = CharacterProgressionData.get(player.serverLevel()).get(player.getUUID());
                    player.sendSystemMessage(Component.literal("=== NEXUS-BAY CHARACTER ===")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                    player.sendSystemMessage(Component.literal("Level " + p.level() + " | XP " + p.experience()
                            + " | freie Stat-Punkte " + p.unspentPoints()).withStyle(ChatFormatting.YELLOW));
                    player.sendSystemMessage(Component.literal("Stärke " + p.strength() + " | Verteidigung "
                            + p.defense() + " | Ausdauer " + p.stamina()).withStyle(ChatFormatting.GREEN));
                    player.sendSystemMessage(Component.literal("Schwert " + p.sword() + " | Teufelsfrucht "
                            + p.fruit()).withStyle(ChatFormatting.AQUA));
                    player.sendSystemMessage(Component.literal("Punkte verteilen: /nexus character allocate <strength|defense|stamina|sword|fruit> <1-100>")
                            .withStyle(ChatFormatting.GRAY));
                    return 1;
                }))
                .then(Commands.literal("allocate")
                    .then(Commands.argument("stat", StringArgumentType.word())
                        .then(Commands.argument("points", IntegerArgumentType.integer(1, 100))
                            .executes(context -> {
                                var player = context.getSource().getPlayerOrException();
                                String stat = StringArgumentType.getString(context, "stat");
                                int points = IntegerArgumentType.getInteger(context, "points");
                                var data = CharacterProgressionData.get(player.serverLevel());
                                var updated = data.allocate(player.getUUID(), stat, points);
                                if (updated == null) {
                                    player.sendSystemMessage(Component.literal("Ungültiger Wert, Stat-Limit 100 oder nicht genug freie Punkte.")
                                            .withStyle(ChatFormatting.RED));
                                    return 0;
                                }
                                player.sendSystemMessage(Component.literal("+" + points + " " + stat
                                        + " | verbleibende Punkte: " + updated.unspentPoints())
                                        .withStyle(ChatFormatting.GREEN));
                                return 1;
                            })))))
        );
    }
}
