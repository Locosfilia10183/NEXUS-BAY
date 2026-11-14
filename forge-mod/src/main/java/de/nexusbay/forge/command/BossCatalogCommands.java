package de.nexusbay.forge.command;

import com.mojang.brigadier.Command;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.BossCatalog;
import de.nexusbay.forge.boss.BossDefinition;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;

/** Read-only player command for the planned boss roster. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class BossCatalogCommands {
    private BossCatalogCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("nexusbay")
                .then(Commands.literal("bosses")
                        .executes(context -> {
                            var source = context.getSource();
                            source.sendSuccess(() -> Component.literal("§6§lNEXUS-BAY – BOSS-ROSTER"), false);
                            BossCatalog.all().values().stream()
                                    .sorted(Comparator.comparingInt(BossDefinition::level))
                                    .forEach(boss -> source.sendSuccess(() -> Component.literal(
                                            "§c• §f" + boss.displayName()
                                                    + " §7| Lv. " + boss.level()
                                                    + " | §e" + boss.rank()
                                                    + " §8| Insel: " + boss.islandId()), false));
                            source.sendSuccess(() -> Component.literal(
                                    "§7Hinweis: Die Liste ist der Boss-Plan. Nicht jeder Eintrag ist bereits spawnbar."), false);
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
