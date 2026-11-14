package de.nexusbay.forge.registry;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.KaidoBossEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NexusBayForge.MOD_ID);

    public static final RegistryObject<EntityType<KaidoBossEntity>> KAIDO =
            ENTITY_TYPES.register("kaido", () -> EntityType.Builder
                    .of(KaidoBossEntity::new, MobCategory.MONSTER)
                    .sized(2.2F, 4.8F)
                    .clientTrackingRange(12)
                    .build("kaido"));

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
