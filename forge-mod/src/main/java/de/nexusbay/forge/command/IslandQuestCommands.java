package de.nexusbay.forge.command;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.BeliLedger;
import de.nexusbay.forge.progression.FruitProfileData;
import de.nexusbay.forge.progression.IslandQuestCatalog;
import de.nexusbay.forge.progression.IslandQuestData;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Temporary quest-board commands; island NPC interaction will call the same services. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandQuestCommands {
    private IslandQuestCommands() {}
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("nexus").then(Commands.literal("quest")
            .then(Commands.literal("list").executes(ctx -> {
                var player = ctx.getSource().getPlayerOrException();
                var data = IslandQuestData.get(player.serverLevel());
                String active = data.active(player.getUUID());
                player.sendSystemMessage(Component.literal("=== QUESTS: FOOSHA VILLAGE ==="));
                for (var quest : IslandQuestCatalog.all()) {
                    String state = quest.id().equals(active) ? "AKTIV (" + data.progress(player.getUUID()) + "/" + quest.requiredKills() + ")"
                            : data.completed(player.getUUID(), quest.id()) ? "ABGESCHLOSSEN" : "VERFÜGBAR";
                    player.sendSystemMessage(Component.literal(quest.title() + " — " + state + " | " + quest.description()));
                }
                return 1;
            }))
            .then(Commands.literal("accept").then(Commands.argument("quest", net.minecraft.commands.arguments.StringArgumentType.word()).suggests((ctx, builder) -> {
                for (var quest : IslandQuestCatalog.all()) builder.suggest(quest.id());
                return builder.buildFuture();
            }).executes(ctx -> {
                var player = ctx.getSource().getPlayerOrException();
                String id = net.minecraft.commands.arguments.StringArgumentType.getString(ctx, "quest");
                var quest = IslandQuestCatalog.find(id).orElse(null);
                if (quest == null) { player.sendSystemMessage(Component.literal("Quest nicht gefunden. Nutze /nexus quest list.")); return 0; }
                if (!IslandQuestData.get(player.serverLevel()).accept(player.getUUID(), quest.id())) {
                    player.sendSystemMessage(Component.literal("Du hast bereits eine aktive Quest oder diese Quest abgeschlossen.")); return 0;
                }
                player.sendSystemMessage(Component.literal("Quest angenommen: " + quest.title())); return 1;
            })))
            .then(Commands.literal("claim").executes(ctx -> {
                var player = ctx.getSource().getPlayerOrException();
                var data = IslandQuestData.get(player.serverLevel());
                String id = data.active(player.getUUID());
                var quest = IslandQuestCatalog.find(id).orElse(null);
                if (quest == null || data.progress(player.getUUID()) < quest.requiredKills()) {
                    player.sendSystemMessage(Component.literal("Deine aktive Quest ist noch nicht abgeschlossen.")); return 0;
                }
                if (!data.claim(player.getUUID(), id)) return 0;
                long balance = BeliLedger.get(player.serverLevel()).deposit(player.getUUID(), quest.beliReward());
                var fruitData = FruitProfileData.get(player.serverLevel());
                if (fruitData.get(player.getUUID()) != null && quest.fruitXpReward() > 0) fruitData.addExperience(player.getUUID(), quest.fruitXpReward());
                player.sendSystemMessage(Component.literal("Quest abgeschlossen: " + quest.title() + " | +" + quest.beliReward() + " Beli | Kontostand: " + balance));
                return 1;
            }))
        ));
    }
}
