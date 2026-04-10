package com.breakinblocks.justtrialspawners.common.block;

import com.breakinblocks.justtrialspawners.common.block.entity.TrialSpawnerBlockEntity;
import com.breakinblocks.justtrialspawners.common.block.entity.trialspawner.TrialSpawnerState;
import com.breakinblocks.justtrialspawners.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class TrialSpawnerBlock extends BaseEntityBlock {
    public static final EnumProperty<TrialSpawnerState> STATE = EnumProperty.create("trial_spawner_state", TrialSpawnerState.class);
    public static final BooleanProperty OMINOUS = BooleanProperty.create("ominous");

    public TrialSpawnerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STATE, TrialSpawnerState.INACTIVE)
                .setValue(OMINOUS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE, OMINOUS);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof SpawnEggItem spawnEgg) {
            if (!level.isClientSide) {
                EntityType<?> entityType = spawnEgg.getType(stack.getTag());
                ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                if (entityId != null) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof TrialSpawnerBlockEntity spawnerBE) {
                        // Set the spawn data to the egg's entity type
                        CompoundTag entityTag = new CompoundTag();
                        entityTag.putString("id", entityId.toString());
                        SpawnData spawnData = new SpawnData();
                        spawnData.getEntityToSpawn().merge(entityTag);
                        spawnerBE.getTrialSpawner().getData().setNextSpawnData(spawnData);
                        spawnerBE.markUpdated();
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrialSpawnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level instanceof ServerLevel serverLevel) {
            return createTickerHelper(type, ModBlockEntities.TRIAL_SPAWNER.get(),
                    (lvl, pos, blockState, blockEntity) ->
                            blockEntity.getTrialSpawner().tickServer(
                                    serverLevel, pos,
                                    blockState.getValue(OMINOUS)));
        } else {
            return createTickerHelper(type, ModBlockEntities.TRIAL_SPAWNER.get(),
                    (lvl, pos, blockState, blockEntity) ->
                            blockEntity.getTrialSpawner().tickClient(
                                    lvl, pos,
                                    blockState.getValue(OMINOUS)));
        }
    }
}
