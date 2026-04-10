package com.breakinblocks.justtrialspawners.registry;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.block.entity.TrialSpawnerBlockEntity;
import com.breakinblocks.justtrialspawners.common.block.entity.VaultBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES, JustTrialSpawners.MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<TrialSpawnerBlockEntity>> TRIAL_SPAWNER = BLOCK_ENTITIES.register("trial_spawner",
            () -> BlockEntityType.Builder.of(TrialSpawnerBlockEntity::new, ModBlocks.TRIAL_SPAWNER.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<VaultBlockEntity>> VAULT = BLOCK_ENTITIES.register("vault",
            () -> BlockEntityType.Builder.of(VaultBlockEntity::new, ModBlocks.VAULT.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
