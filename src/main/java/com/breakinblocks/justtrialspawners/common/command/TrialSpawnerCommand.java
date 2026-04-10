package com.breakinblocks.justtrialspawners.common.command;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.block.TrialSpawnerBlock;
import com.breakinblocks.justtrialspawners.common.block.entity.TrialSpawnerBlockEntity;
import com.breakinblocks.justtrialspawners.common.block.entity.trialspawner.TrialSpawnerConfig;
import com.breakinblocks.justtrialspawners.integration.FTBLibraryIntegration;
import com.breakinblocks.justtrialspawners.registry.ModBlocks;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import java.util.Optional;

/**
 * OP-only /trialspawner command for configuring trial spawners.
 *
 * Subcommands:
 *   /trialspawner edit <pos>           - Opens FTB Library NBT editor (requires FTB Library)
 *   /trialspawner give <entity_type>   - Gives a pre-configured trial spawner item
 *   /trialspawner addmob <pos> <entity_type> [weight] - Add a mob to spawn potentials
 *   /trialspawner setconfig <pos> <key> <value> - Set a config value
 *   /trialspawner info <pos>           - Show current spawner configuration
 */
public class TrialSpawnerCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("trialspawner")
                .requires(source -> source.hasPermission(2))

                // /trialspawner edit - edit held trial spawner item, or /trialspawner edit <pos> for placed block
                .then(Commands.literal("edit")
                        .executes(TrialSpawnerCommand::editHeldSpawner)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(TrialSpawnerCommand::editSpawner)))

                // /trialspawner give <entity_type> [totalMobs] [simultaneousMobs] [ticksBetweenSpawn]
                .then(Commands.literal("give")
                        .then(Commands.argument("entity_type", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> {
                                    BuiltInRegistries.ENTITY_TYPE.keySet().forEach(rl -> builder.suggest(rl.toString()));
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> giveSpawner(ctx, 6.0f, 2.0f, 40))
                                .then(Commands.argument("total_mobs", FloatArgumentType.floatArg(1, 100))
                                        .executes(ctx -> giveSpawner(ctx,
                                                FloatArgumentType.getFloat(ctx, "total_mobs"), 2.0f, 40))
                                        .then(Commands.argument("simultaneous_mobs", FloatArgumentType.floatArg(1, 50))
                                                .executes(ctx -> giveSpawner(ctx,
                                                        FloatArgumentType.getFloat(ctx, "total_mobs"),
                                                        FloatArgumentType.getFloat(ctx, "simultaneous_mobs"), 40))
                                                .then(Commands.argument("ticks_between_spawn", IntegerArgumentType.integer(1, 1200))
                                                        .executes(ctx -> giveSpawner(ctx,
                                                                FloatArgumentType.getFloat(ctx, "total_mobs"),
                                                                FloatArgumentType.getFloat(ctx, "simultaneous_mobs"),
                                                                IntegerArgumentType.getInteger(ctx, "ticks_between_spawn"))))))))

                // /trialspawner addmob <pos> <entity_type> [weight]
                .then(Commands.literal("addmob")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("entity_type", ResourceLocationArgument.id())
                                        .suggests((ctx, builder) -> {
                                            BuiltInRegistries.ENTITY_TYPE.keySet().forEach(rl -> builder.suggest(rl.toString()));
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> addMob(ctx, 1))
                                        .then(Commands.argument("weight", IntegerArgumentType.integer(1, 1000))
                                                .executes(ctx -> addMob(ctx, IntegerArgumentType.getInteger(ctx, "weight")))))))

                // /trialspawner setconfig <pos> <key> <value>
                .then(Commands.literal("setconfig")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for (String k : new String[]{"spawn_range", "total_mobs", "simultaneous_mobs",
                                                    "total_mobs_added_per_player", "simultaneous_mobs_added_per_player",
                                                    "ticks_between_spawn"}) {
                                                builder.suggest(k);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                .executes(TrialSpawnerCommand::setConfig)))))

                // /trialspawner info <pos>
                .then(Commands.literal("info")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(TrialSpawnerCommand::showInfo)))

                // /trialspawner clearmobs <pos>
                .then(Commands.literal("clearmobs")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(TrialSpawnerCommand::clearMobs)))
        );
    }

    // ========== Edit via FTB Library ==========

    private static int editHeldSpawner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (!ModList.get().isLoaded("ftblibrary")) {
            ctx.getSource().sendFailure(Component.literal("FTB Library is required for the NBT editor."));
            return 0;
        }

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem blockItem) || blockItem.getBlock() != ModBlocks.TRIAL_SPAWNER.get()) {
            ctx.getSource().sendFailure(Component.literal("You must hold a trial spawner item, or use /trialspawner edit <pos> for a placed block."));
            return 0;
        }

        FTBLibraryIntegration.openItemNBTEditor(player, held);
        ctx.getSource().sendSuccess(() -> Component.literal("Opened trial spawner item editor"), true);
        return 1;
    }

    private static int editSpawner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = ctx.getSource().getLevel();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrialSpawnerBlockEntity spawnerBE)) {
            ctx.getSource().sendFailure(Component.literal("Block at " + pos.toShortString() + " is not a trial spawner"));
            return 0;
        }

        if (!ModList.get().isLoaded("ftblibrary")) {
            ctx.getSource().sendFailure(Component.literal("FTB Library is required for the NBT editor. Use /trialspawner addmob and /trialspawner setconfig instead."));
            return 0;
        }

        FTBLibraryIntegration.openNBTEditor(player, spawnerBE, pos);
        ctx.getSource().sendSuccess(() -> Component.literal("Opened trial spawner editor at " + pos.toShortString()), true);
        return 1;
    }

    // ========== Give Pre-Configured Spawner ==========

    private static int giveSpawner(CommandContext<CommandSourceStack> ctx, float totalMobs, float simultaneous, int ticksBetween) throws CommandSyntaxException {
        ResourceLocation entityId = ResourceLocationArgument.getId(ctx, "entity_type");
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        // Validate entity type
        Optional<EntityType<?>> entityType = EntityType.byString(entityId.toString());
        if (entityType.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Unknown entity type: " + entityId));
            return 0;
        }

        // Build the spawner item with NBT
        ItemStack spawnerItem = new ItemStack(ModBlocks.TRIAL_SPAWNER_ITEM.get());

        CompoundTag blockEntityTag = new CompoundTag();
        CompoundTag spawnerTag = new CompoundTag();

        // Normal config
        CompoundTag configTag = new CompoundTag();
        configTag.putFloat("total_mobs", totalMobs);
        configTag.putFloat("simultaneous_mobs", simultaneous);
        configTag.putInt("ticks_between_spawn", ticksBetween);

        // Spawn potentials with the specified entity
        ListTag potentials = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putInt("weight", 1);
        CompoundTag dataTag = new CompoundTag();
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("id", entityId.toString());
        dataTag.put("entity", entityTag);
        entry.put("data", dataTag);
        potentials.add(entry);
        configTag.put("spawn_potentials", potentials);

        spawnerTag.put("normal_config", configTag);
        spawnerTag.put("ominous_config", configTag.copy());
        spawnerTag.put("data", new CompoundTag());
        spawnerTag.putBoolean("is_ominous", false);
        spawnerTag.putInt("target_cooldown_length", 36000);
        spawnerTag.putInt("required_player_range", 14);

        blockEntityTag.put("trial_spawner", spawnerTag);

        spawnerItem.getOrCreateTag().put("BlockEntityTag", blockEntityTag);

        // Give to player
        if (!player.getInventory().add(spawnerItem)) {
            player.drop(spawnerItem, false);
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Gave trial spawner configured with " + entityId +
                        " (total=" + totalMobs + ", simultaneous=" + simultaneous +
                        ", interval=" + ticksBetween + "t)"), true);
        return 1;
    }

    // ========== Add Mob to Existing Spawner ==========

    private static int addMob(CommandContext<CommandSourceStack> ctx, int weight) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        ResourceLocation entityId = ResourceLocationArgument.getId(ctx, "entity_type");
        ServerLevel level = ctx.getSource().getLevel();

        // Validate entity type
        Optional<EntityType<?>> entityType = EntityType.byString(entityId.toString());
        if (entityType.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Unknown entity type: " + entityId));
            return 0;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrialSpawnerBlockEntity spawnerBE)) {
            ctx.getSource().sendFailure(Component.literal("Block at " + pos.toShortString() + " is not a trial spawner"));
            return 0;
        }

        // Add to spawn potentials by saving, modifying, and reloading
        CompoundTag tag = new CompoundTag();
        tag.merge(spawnerBE.saveWithoutMetadata());

        CompoundTag spawnerTag = tag.getCompound("trial_spawner");
        CompoundTag configTag = spawnerTag.contains("normal_config")
                ? spawnerTag.getCompound("normal_config") : new CompoundTag();

        ListTag potentials = configTag.contains("spawn_potentials")
                ? configTag.getList("spawn_potentials", 10) : new ListTag();

        CompoundTag entry = new CompoundTag();
        entry.putInt("weight", weight);
        CompoundTag dataTag = new CompoundTag();
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("id", entityId.toString());
        dataTag.put("entity", entityTag);
        entry.put("data", dataTag);
        potentials.add(entry);

        configTag.put("spawn_potentials", potentials);
        spawnerTag.put("normal_config", configTag);

        // Also add to ominous config
        CompoundTag ominousConfig = spawnerTag.contains("ominous_config")
                ? spawnerTag.getCompound("ominous_config") : new CompoundTag();
        ListTag ominousPotentials = ominousConfig.contains("spawn_potentials")
                ? ominousConfig.getList("spawn_potentials", 10) : new ListTag();
        ominousPotentials.add(entry.copy());
        ominousConfig.put("spawn_potentials", ominousPotentials);
        spawnerTag.put("ominous_config", ominousConfig);

        tag.put("trial_spawner", spawnerTag);
        spawnerBE.load(tag);
        spawnerBE.setChanged();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Added " + entityId + " (weight " + weight + ") to trial spawner at " + pos.toShortString()), true);
        return 1;
    }

    // ========== Set Config Value ==========

    private static int setConfig(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        String key = StringArgumentType.getString(ctx, "key");
        String valueStr = StringArgumentType.getString(ctx, "value");
        ServerLevel level = ctx.getSource().getLevel();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrialSpawnerBlockEntity spawnerBE)) {
            ctx.getSource().sendFailure(Component.literal("Block at " + pos.toShortString() + " is not a trial spawner"));
            return 0;
        }

        CompoundTag tag = new CompoundTag();
        tag.merge(spawnerBE.saveWithoutMetadata());
        CompoundTag spawnerTag = tag.getCompound("trial_spawner");
        CompoundTag configTag = spawnerTag.contains("normal_config")
                ? spawnerTag.getCompound("normal_config") : new CompoundTag();

        try {
            switch (key) {
                case "spawn_range" -> configTag.putInt(key, Integer.parseInt(valueStr));
                case "total_mobs", "simultaneous_mobs", "total_mobs_added_per_player",
                     "simultaneous_mobs_added_per_player" -> configTag.putFloat(key, Float.parseFloat(valueStr));
                case "ticks_between_spawn" -> configTag.putInt(key, Integer.parseInt(valueStr));
                default -> {
                    ctx.getSource().sendFailure(Component.literal("Unknown config key: " + key +
                            ". Valid keys: spawn_range, total_mobs, simultaneous_mobs, total_mobs_added_per_player, " +
                            "simultaneous_mobs_added_per_player, ticks_between_spawn"));
                    return 0;
                }
            }
        } catch (NumberFormatException e) {
            ctx.getSource().sendFailure(Component.literal("Invalid value '" + valueStr + "' for key '" + key + "'"));
            return 0;
        }

        spawnerTag.put("normal_config", configTag);
        tag.put("trial_spawner", spawnerTag);
        spawnerBE.load(tag);
        spawnerBE.setChanged();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);

        ctx.getSource().sendSuccess(() -> Component.literal("Set " + key + " = " + valueStr + " on trial spawner at " + pos.toShortString()), true);
        return 1;
    }

    // ========== Show Info ==========

    private static int showInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        ServerLevel level = ctx.getSource().getLevel();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrialSpawnerBlockEntity spawnerBE)) {
            ctx.getSource().sendFailure(Component.literal("Block at " + pos.toShortString() + " is not a trial spawner"));
            return 0;
        }

        CompoundTag tag = new CompoundTag();
        tag.merge(spawnerBE.saveWithoutMetadata());
        CompoundTag spawnerTag = tag.getCompound("trial_spawner");
        CompoundTag configTag = spawnerTag.getCompound("normal_config");

        StringBuilder info = new StringBuilder();
        info.append("Trial Spawner at ").append(pos.toShortString()).append(":\n");
        info.append("  State: ").append(spawnerBE.getState().getSerializedName()).append("\n");
        info.append("  Ominous: ").append(spawnerTag.getBoolean("is_ominous")).append("\n");
        info.append("  Spawn Range: ").append(configTag.getInt("spawn_range")).append("\n");
        info.append("  Total Mobs: ").append(configTag.getFloat("total_mobs")).append("\n");
        info.append("  Simultaneous Mobs: ").append(configTag.getFloat("simultaneous_mobs")).append("\n");
        info.append("  Ticks Between Spawn: ").append(configTag.getInt("ticks_between_spawn")).append("\n");

        if (configTag.contains("spawn_potentials")) {
            ListTag potentials = configTag.getList("spawn_potentials", 10);
            info.append("  Spawn Potentials (").append(potentials.size()).append("):\n");
            for (int i = 0; i < potentials.size(); i++) {
                CompoundTag entry = potentials.getCompound(i);
                int weight = entry.getInt("weight");
                String id = entry.getCompound("data").getCompound("entity").getString("id");
                info.append("    - ").append(id).append(" (weight ").append(weight).append(")\n");
            }
        } else {
            info.append("  Spawn Potentials: none (set with /trialspawner addmob)\n");
        }

        ctx.getSource().sendSuccess(() -> Component.literal(info.toString()), false);
        return 1;
    }

    // ========== Clear Mobs ==========

    private static int clearMobs(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        ServerLevel level = ctx.getSource().getLevel();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TrialSpawnerBlockEntity spawnerBE)) {
            ctx.getSource().sendFailure(Component.literal("Block at " + pos.toShortString() + " is not a trial spawner"));
            return 0;
        }

        CompoundTag tag = new CompoundTag();
        tag.merge(spawnerBE.saveWithoutMetadata());
        CompoundTag spawnerTag = tag.getCompound("trial_spawner");

        CompoundTag normalConfig = spawnerTag.getCompound("normal_config");
        normalConfig.put("spawn_potentials", new ListTag());
        spawnerTag.put("normal_config", normalConfig);

        CompoundTag ominousConfig = spawnerTag.getCompound("ominous_config");
        ominousConfig.put("spawn_potentials", new ListTag());
        spawnerTag.put("ominous_config", ominousConfig);

        tag.put("trial_spawner", spawnerTag);
        spawnerBE.load(tag);
        spawnerBE.setChanged();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);

        ctx.getSource().sendSuccess(() -> Component.literal("Cleared all spawn potentials from trial spawner at " + pos.toShortString()), true);
        return 1;
    }
}
