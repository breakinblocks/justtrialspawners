package com.breakinblocks.justtrialspawners.registry;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.entity.BoggedEntity;
import com.breakinblocks.justtrialspawners.common.entity.BreezeEntity;
import com.breakinblocks.justtrialspawners.common.entity.BreezeWindChargeProjectile;
import com.breakinblocks.justtrialspawners.common.entity.WindChargeProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
            ForgeRegistries.ENTITY_TYPES, JustTrialSpawners.MOD_ID);

    public static final RegistryObject<EntityType<BreezeEntity>> BREEZE = ENTITIES.register("breeze",
            () -> EntityType.Builder.of(BreezeEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.77f)
                    .clientTrackingRange(10)
                    .build("breeze"));

    public static final RegistryObject<EntityType<BoggedEntity>> BOGGED = ENTITIES.register("bogged",
            () -> EntityType.Builder.of(BoggedEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.99f)
                    .clientTrackingRange(8)
                    .build("bogged"));

    public static final RegistryObject<EntityType<WindChargeProjectile>> WIND_CHARGE = ENTITIES.register("wind_charge",
            () -> EntityType.Builder.<WindChargeProjectile>of(WindChargeProjectile::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("wind_charge"));

    public static final RegistryObject<EntityType<BreezeWindChargeProjectile>> BREEZE_WIND_CHARGE = ENTITIES.register("breeze_wind_charge",
            () -> EntityType.Builder.<BreezeWindChargeProjectile>of(BreezeWindChargeProjectile::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("breeze_wind_charge"));

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
