package de.nexusbay.forge.registry;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.KaidoBossEntity;
import de.nexusbay.forge.boss.CrocodileBossEntity;
import de.nexusbay.forge.boss.EnelBossEntity;
import de.nexusbay.forge.boss.RobLucciBossEntity;
import de.nexusbay.forge.entity.QuestGiverEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NexusBayForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntityAttributes {
    private ModEntityAttributes() {}
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.KAIDO.get(), KaidoBossEntity.createAttributes().build());
        event.put(ModEntities.CROCODILE.get(), CrocodileBossEntity.createAttributes().build());
        event.put(ModEntities.ENEL.get(), EnelBossEntity.createAttributes().build());
        event.put(ModEntities.ROB_LUCCI.get(), RobLucciBossEntity.createAttributes().build());
        event.put(ModEntities.QUEST_GIVER.get(), QuestGiverEntity.createAttributes().build());
    }
}
