package com.breakinblocks.justtrialspawners.registry;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.block.TrialSpawnerBlock;
import com.breakinblocks.justtrialspawners.common.block.VaultBlock;
import com.breakinblocks.justtrialspawners.common.block.entity.vault.VaultState;
import com.breakinblocks.justtrialspawners.common.item.TrialSpawnerBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            ForgeRegistries.BLOCKS, JustTrialSpawners.MOD_ID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            ForgeRegistries.ITEMS, JustTrialSpawners.MOD_ID);

    public static final RegistryObject<Block> TRIAL_SPAWNER = BLOCKS.register("trial_spawner",
            () -> new TrialSpawnerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(50.0f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(TrialSpawnerBlock.STATE).lightLevel())
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<BlockItem> TRIAL_SPAWNER_ITEM = ITEMS.register("trial_spawner",
            () -> new TrialSpawnerBlockItem(TRIAL_SPAWNER.get(), new Item.Properties()));

    public static final RegistryObject<Block> VAULT = BLOCKS.register("vault",
            () -> new VaultBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(50.0f)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(VaultBlock.STATE).lightLevel())
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<BlockItem> VAULT_ITEM = ITEMS.register("vault",
            () -> new BlockItem(VAULT.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
