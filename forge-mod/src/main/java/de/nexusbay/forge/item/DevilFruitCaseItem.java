package de.nexusbay.forge.item;

import de.nexusbay.forge.progression.DevilFruitCaseTable;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

/** Opens one fruit case on the server and awards exactly one rolled fruit. */
public final class DevilFruitCaseItem extends Item {
    public DevilFruitCaseItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(held);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(held);
        }

        DevilFruitCaseTable.Fruit fruit = DevilFruitCaseTable.open(new Random(level.getRandom().nextLong()));
        var fruitItem = ModItems.FRUIT_ITEMS.get(fruit.id());
        if (fruitItem == null) {
            serverPlayer.sendSystemMessage(Component.literal("Diese Frucht ist momentan nicht verfügbar.")
                    .withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(held);
        }

        ItemStack reward = new ItemStack(fruitItem.get());
        if (!serverPlayer.getInventory().add(reward)) {
            serverPlayer.drop(reward, false);
        }
        held.shrink(1);

        ChatFormatting rarityColor = switch (fruit.rarity()) {
            case COMMON -> ChatFormatting.GRAY;
            case UNCOMMON -> ChatFormatting.GREEN;
            case RARE -> ChatFormatting.BLUE;
            case EPIC -> ChatFormatting.DARK_PURPLE;
            case LEGENDARY -> ChatFormatting.GOLD;
            case MYTHICAL -> ChatFormatting.LIGHT_PURPLE;
        };
        serverPlayer.sendSystemMessage(Component.literal("FRUCHT-KISTE GEÖFFNET: ")
                .append(Component.literal(fruit.displayName()).withStyle(rarityColor, ChatFormatting.BOLD))
                .append(Component.literal(" [" + fruit.rarity().name() + "]")));
        return InteractionResultHolder.consume(held);
    }
}
