package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.progression.CharacterProgressionData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/** Converts allocated character points into real server-side combat attributes. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class CharacterStatEvents {
    private static final UUID STRENGTH_ID = UUID.fromString("2d5f2e10-8b6e-4f8e-9c21-7c8d1a001001");
    private static final UUID DEFENSE_ID  = UUID.fromString("2d5f2e10-8b6e-4f8e-9c21-7c8d1a001002");
    private static final UUID STAMINA_ID  = UUID.fromString("2d5f2e10-8b6e-4f8e-9c21-7c8d1a001003");
    private static final UUID SWORD_ID    = UUID.fromString("2d5f2e10-8b6e-4f8e-9c21-7c8d1a001004");
    private static final UUID FRUIT_ID    = UUID.fromString("2d5f2e10-8b6e-4f8e-9c21-7c8d1a001005");

    private CharacterStatEvents() {}

    @SubscribeEvent
    public static void applyAllocatedStats(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 20 != 0) return;

        CharacterProgressionData.Profile profile =
                CharacterProgressionData.get(player.serverLevel()).get(player.getUUID());

        set(player, Attributes.ATTACK_DAMAGE, STRENGTH_ID,
                profile.strength() * 0.15D, "NEXUS-BAY Stärke");
        set(player, Attributes.ARMOR, DEFENSE_ID,
                profile.defense() * 0.12D, "NEXUS-BAY Verteidigung");
        set(player, Attributes.MAX_HEALTH, STAMINA_ID,
                profile.stamina() * 0.25D, "NEXUS-BAY Ausdauer");

        double swordBonus = player.getMainHandItem().getItem() instanceof SwordItem
                ? profile.sword() * 0.12D : 0.0D;
        set(player, Attributes.ATTACK_DAMAGE, SWORD_ID, swordBonus, "NEXUS-BAY Schwert");

        var fruitProfile = de.nexusbay.forge.progression.FruitProfileData
                .get(player.serverLevel()).get(player.getUUID());
        double fruitBonus = fruitProfile != null ? profile.fruit() * 0.12D : 0.0D;
        set(player, Attributes.ATTACK_DAMAGE, FRUIT_ID, fruitBonus, "NEXUS-BAY Teufelsfrucht");
    }

    private static void set(ServerPlayer player, Attribute attribute, UUID id,
                            double amount, String name) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount > 0.0D) {
            instance.addTransientModifier(new AttributeModifier(
                    id, name, amount, AttributeModifier.Operation.ADDITION));
        }
    }
}
