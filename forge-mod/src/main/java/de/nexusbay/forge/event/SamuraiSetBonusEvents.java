package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Grants a small defensive and offensive bonus while wearing the complete Wano set. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class SamuraiSetBonusEvents {
    private SamuraiSetBonusEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 40 != 0) return;
        var armor = player.getInventory().armor;
        boolean fullSet = armor.get(3).is(ModItems.WANO_SAMURAI_HELMET.get())
                && armor.get(2).is(ModItems.WANO_SAMURAI_CHESTPLATE.get())
                && armor.get(1).is(ModItems.WANO_SAMURAI_LEGGINGS.get())
                && armor.get(0).is(ModItems.WANO_SAMURAI_BOOTS.get());
        if (!fullSet) return;
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0, true, false, true));
        if (player.tickCount % 200 == 0) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6§lWANO-SAMURAI-SET §eSetbonus aktiv: Stärke und Resistenz I"), true);
        }
    }
}
