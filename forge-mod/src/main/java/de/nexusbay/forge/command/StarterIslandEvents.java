package de.nexusbay.forge.command;

import com.mojang.brigadier.Command;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class StarterIslandEvents {
    private static final String STARTED = "nexusbay_starter_started";
    private static final String KILLS = "nexusbay_starter_soldier_kills";
    private static final int REQUIRED_KILLS = 5;
    private static final String STARTER_SWORDS_GIVEN = "nexusbay_starter_swords_given";

    @SubscribeEvent
    public static void onFirstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getPersistentData().getBoolean(STARTER_SWORDS_GIVEN)) return;

        giveOrDrop(player, new ItemStack(ModItems.SANTORYU.get()));
        player.getPersistentData().putBoolean(STARTER_SWORDS_GIVEN, true);
        player.sendSystemMessage(Component.literal("§6NEXUS-BAY §f– Willkommen, Pirat!"));
        player.sendSystemMessage(Component.literal("§eDu hast Zoros kompletten Dreischwerter-Stil erhalten: Wado Ichimonji, Enma und Sandai Kitetsu in einer Waffe."));
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }


    private StarterIslandEvents() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("nexusbay")
                .then(Commands.literal("starter")
                        .executes(context -> start(context.getSource().getPlayerOrException()))));
    }

    private static int start(ServerPlayer player) {
        if (player.getPersistentData().getBoolean(STARTED)) {
            player.sendSystemMessage(Component.literal("Dein Starter-Auftrag läuft bereits. Besiege 5 Soldaten!"));
            return 0;
        }

        player.getInventory().add(new ItemStack(ModItems.SANTORYU.get()));
        player.getPersistentData().putBoolean(STARTED, true);
        player.getPersistentData().putInt(KILLS, 0);

        Vec3 look = player.getLookAngle().normalize();
        Vec3 center = player.position().add(look.scale(9.0D));
        for (int i = 0; i < REQUIRED_KILLS; i++) {
            double angle = (Math.PI * 2.0D * i) / REQUIRED_KILLS;
            double x = center.x + Math.cos(angle) * 4.0D;
            double z = center.z + Math.sin(angle) * 4.0D;
            Zombie soldier = EntityType.ZOMBIE.create(player.serverLevel());
            if (soldier == null) continue;
            soldier.moveTo(x, player.getY(), z, player.getRandom().nextFloat() * 360.0F, 0.0F);
            soldier.setCustomName(Component.literal("Starterinsel-Soldat"));
            soldier.setCustomNameVisible(true);
            soldier.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            soldier.setPersistenceRequired();
            player.serverLevel().addFreshEntity(soldier);
        }

        player.sendSystemMessage(Component.literal("§6STARTERINSEL §f– Besiege die 5 Soldaten!"));
        player.sendSystemMessage(Component.literal("§eDu hast Wado Ichimonji, Enma und Sandai Kitetsu erhalten."));
        player.sendSystemMessage(Component.literal("§7Auftrag: 0/5 Soldaten besiegt."));
        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public static void onSoldierKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!event.getEntity().hasCustomName()
                || !Component.literal("Starterinsel-Soldat").equals(event.getEntity().getCustomName())) return;
        if (!player.getPersistentData().getBoolean(STARTED)) return;

        int kills = Math.min(REQUIRED_KILLS,
                player.getPersistentData().getInt(KILLS) + 1);
        player.getPersistentData().putInt(KILLS, kills);
        player.sendSystemMessage(Component.literal("§eStarter-Auftrag: " + kills + "/" + REQUIRED_KILLS + " Soldaten besiegt."));
        if (kills == REQUIRED_KILLS) {
            player.sendSystemMessage(Component.literal("§aStarter-Auftrag abgeschlossen! Du bist bereit für deine Reise."));
            player.getPersistentData().putBoolean(STARTED, false);
        }
    }
}
