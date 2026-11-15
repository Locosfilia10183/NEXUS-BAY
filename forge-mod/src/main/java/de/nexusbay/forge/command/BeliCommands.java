package de.nexusbay.forge.command;

import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.BeliLedger;

/** Player-facing Beli balance, transfer and staff grant commands. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class BeliCommands {
    private BeliCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("beli")
                        .then(Commands.literal("balance")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    long balance = BeliLedger.get(player.serverLevel()).balance(player);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Dein Kontostand: " + balance + " Beli"), false);
                                    return 1;
                                }))
                        .then(Commands.literal("pay")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(1L))
                                                .executes(context -> {
                                                    ServerPlayer sender = context.getSource().getPlayerOrException();
                                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                                    long amount = LongArgumentType.getLong(context, "amount");
                                                    BeliLedger ledger = BeliLedger.get(sender.serverLevel());
                                                    if (!ledger.transfer(sender.getUUID(), target.getUUID(), amount)) {
                                                        context.getSource().sendFailure(Component.literal(
                                                                "Überweisung fehlgeschlagen: Kontostand prüfen."));
                                                        return 0;
                                                    }
                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal(amount + " Beli an "
                                                                    + target.getGameProfile().getName() + " überwiesen."), false);
                                                    target.sendSystemMessage(Component.literal(
                                                            "Du hast " + amount + " Beli erhalten."));
                                                    return 1;
                                                }))))
                        .then(Commands.literal("give")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(1L))
                                                .executes(context -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                                    long amount = LongArgumentType.getLong(context, "amount");
                                                    long balance = BeliLedger.get(target.serverLevel())
                                                            .deposit(target.getUUID(), amount);
                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal(amount + " Beli an "
                                                                    + target.getGameProfile().getName()
                                                                    + " gegeben. Neuer Kontostand: " + balance), true);
                                                    target.sendSystemMessage(Component.literal(
                                                            "Du hast " + amount + " Beli erhalten. Kontostand: " + balance));
                                                    return 1;
                                                }))))
        );
    }
}
