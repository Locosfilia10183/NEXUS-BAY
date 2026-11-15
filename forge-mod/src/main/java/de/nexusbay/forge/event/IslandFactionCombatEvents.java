package de.nexusbay.forge.event;

import de.nexusbay.forge.NexusBayForge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Gives island faction enemies recognizable combat traits without replacing vanilla AI. */
@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID)
public final class IslandFactionCombatEvents {
    private IslandFactionCombatEvents() {}

    @SubscribeEvent
    public static void onFactionAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Monster attacker)
                || !(event.getEntity() instanceof ServerPlayer victim)) return;
        String faction=attacker.getPersistentData().getString("nexusbay_faction");
        if(faction.isBlank())return;
        event.setAmount(event.getAmount()*1.12F);
        if(attacker.tickCount%80!=0)return;
        switch(faction){
            case "arlong_fishmen","fishman_raiders" ->
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0));
            case "syrup_blackcat","thriller_zombies","impel_prisoners" ->
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80,0));
            case "alabasta_rebels","whiskey_hunters","jaya_bellamy" ->
                victim.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,80,0));
            case "skypiea_enforcers","enies_cp9","marineford_raiders" ->
                victim.addEffect(new MobEffectInstance(MobEffects.GLOWING,100,0));
            case "punk_hazards","onigashima_beasts","wano_beasts" ->
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,1));
            case "wholecake_homies","dressrosa_dons","orange_pirates" ->
                victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION,60,0));
            default -> victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0));
        }
    }
}
