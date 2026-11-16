package de.nexusbay.forge.registry;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.KaidoBossEntity;
import de.nexusbay.forge.boss.CrocodileBossEntity;
import de.nexusbay.forge.boss.EnelBossEntity;
import de.nexusbay.forge.boss.RobLucciBossEntity;
import de.nexusbay.forge.boss.KizaruBossEntity;
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
                    .of(KaidoBossEntity::new, MobCategory.MONSTER).sized(2.2F, 4.8F)
                    .clientTrackingRange(12).build("kaido"));

    public static final RegistryObject<EntityType<CrocodileBossEntity>> CROCODILE =
            ENTITY_TYPES.register("crocodile", () -> EntityType.Builder
                    .of(CrocodileBossEntity::new, MobCategory.MONSTER).sized(1.2F, 3.2F)
                    .clientTrackingRange(12).build("crocodile"));

    public static final RegistryObject<EntityType<EnelBossEntity>> ENEL =
            ENTITY_TYPES.register("enel", () -> EntityType.Builder
                    .of(EnelBossEntity::new, MobCategory.MONSTER).sized(1.15F, 3.0F)
                    .clientTrackingRange(12).build("enel"));

    public static final RegistryObject<EntityType<RobLucciBossEntity>> ROB_LUCCI =
            ENTITY_TYPES.register("rob_lucci", () -> EntityType.Builder
                    .of(RobLucciBossEntity::new, MobCategory.MONSTER).sized(1.1F, 2.9F)
                    .clientTrackingRange(12).build("rob_lucci"));

    public static final RegistryObject<EntityType<KizaruBossEntity>> KIZARU =
            ENTITY_TYPES.register("kizaru", () -> EntityType.Builder
                    .of(KizaruBossEntity::new, MobCategory.MONSTER).sized(1.25F, 3.15F)
                    .clientTrackingRange(14).build("kizaru"));

    private ModEntities() {}
    public static void register(IEventBus modBus) { ENTITY_TYPES.register(modBus); }
}
