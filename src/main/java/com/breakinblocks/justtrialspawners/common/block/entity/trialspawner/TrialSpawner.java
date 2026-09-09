package com.breakinblocks.justtrialspawners.common.block.entity.trialspawner;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.block.TrialSpawnerBlock;
import com.breakinblocks.justtrialspawners.config.JTSConfig;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Core trial spawner logic - state machine, mob spawning, reward ejection, ominous mode.
 */
public final class TrialSpawner {
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(MAX_MOB_TRACKING_DISTANCE);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;

    private TrialSpawnerConfig normalConfig;
    private TrialSpawnerConfig ominousConfig;
    private final TrialSpawnerData data;
    private final StateAccessor stateAccessor;
    private boolean isOminous;

    public TrialSpawner(StateAccessor stateAccessor) {
        this(new TrialSpawnerConfig(), TrialSpawnerConfig.createOminousDefault(), new TrialSpawnerData(), stateAccessor);
    }

    public TrialSpawner(TrialSpawnerConfig normalConfig, TrialSpawnerConfig ominousConfig,
                         TrialSpawnerData data, StateAccessor stateAccessor) {
        this.normalConfig = normalConfig;
        this.ominousConfig = ominousConfig;
        this.data = data;
        this.stateAccessor = stateAccessor;
    }

    public TrialSpawnerConfig getConfig() {
        return this.isOminous ? this.ominousConfig : this.normalConfig;
    }

    public static boolean isOminousModeEnabled() {
        return JTSConfig.ominousEnabled();
    }

    public TrialSpawnerConfig getNormalConfig() { return this.normalConfig; }
    public TrialSpawnerConfig getOminousConfig() { return this.ominousConfig; }
    public TrialSpawnerData getData() { return this.data; }
    public int getTargetCooldownLength() { return JTSConfig.cooldownTicks(); }
    public int getRequiredPlayerRange() { return JTSConfig.detectionRange(); }

    public TrialSpawnerState getState() { return this.stateAccessor.getState(); }
    public void setState(Level level, TrialSpawnerState state) { this.stateAccessor.setState(level, state); }
    public void markUpdated() { this.stateAccessor.markUpdated(); }

    public boolean isOminous() { return this.isOminous; }

    public void applyOminous(ServerLevel level, BlockPos pos) {
        if (!isOminousModeEnabled()) return;
        level.setBlock(pos, level.getBlockState(pos).setValue(TrialSpawnerBlock.OMINOUS, true), 3);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, level);
    }

    public void removeOminous(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, level.getBlockState(pos).setValue(TrialSpawnerBlock.OMINOUS, false), 3);
        this.isOminous = false;
    }

    public boolean canSpawnInLevel(Level level) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
    }

    public void tickServer(ServerLevel level, BlockPos pos, boolean ominous) {
        if (ominous && !isOminousModeEnabled()) {
            this.removeOminous(level, pos);
            ominous = false;
        }
        this.isOminous = ominous;
        TrialSpawnerState currentState = this.getState();

        // Prune dead/distant mobs
        if (this.data.currentMobs.removeIf(uuid -> shouldMobBeUntracked(level, pos, uuid))) {
            this.data.nextMobSpawnsAt = level.getGameTime() + this.getConfig().ticksBetweenSpawn();
        }

        TrialSpawnerState nextState = tickAndGetNext(pos, level, currentState);
        if (nextState != currentState) {
            this.setState(level, nextState);
        }
    }

    private TrialSpawnerState tickAndGetNext(BlockPos pos, ServerLevel level, TrialSpawnerState currentState) {
        return switch (currentState) {
            case INACTIVE -> {
                if (this.data.getOrCreateDisplayEntity(this, level, TrialSpawnerState.WAITING_FOR_PLAYERS) == null) {
                    yield TrialSpawnerState.INACTIVE;
                }
                yield TrialSpawnerState.WAITING_FOR_PLAYERS;
            }

            case WAITING_FOR_PLAYERS -> {
                if (!canSpawnInLevel(level)) {
                    this.data.reset();
                    yield TrialSpawnerState.WAITING_FOR_PLAYERS;
                }
                if (!this.data.hasMobToSpawn(this, level.random)) {
                    yield TrialSpawnerState.INACTIVE;
                }
                this.data.tryDetectPlayers(level, pos, this);
                yield this.data.detectedPlayers.isEmpty()
                        ? TrialSpawnerState.WAITING_FOR_PLAYERS
                        : TrialSpawnerState.ACTIVE;
            }

            case ACTIVE -> {
                if (!canSpawnInLevel(level)) {
                    this.data.reset();
                    yield TrialSpawnerState.WAITING_FOR_PLAYERS;
                }
                if (!this.data.hasMobToSpawn(this, level.random)) {
                    yield TrialSpawnerState.INACTIVE;
                }

                int additionalPlayers = this.data.countAdditionalPlayers(pos);
                this.data.tryDetectPlayers(level, pos, this);

                if (this.isOminous) {
                    spawnOminousItem(level, pos);
                }

                if (this.data.hasFinishedSpawningAllMobs(this.getConfig(), additionalPlayers)) {
                    if (this.data.haveAllCurrentMobsDied()) {
                        this.data.cooldownEndsAt = level.getGameTime() + this.getTargetCooldownLength();
                        this.data.totalMobsSpawned = 0;
                        this.data.nextMobSpawnsAt = 0L;
                        yield TrialSpawnerState.WAITING_FOR_REWARD_EJECTION;
                    }
                } else if (this.data.isReadyToSpawnNextMob(level, this.getConfig(), additionalPlayers)) {
                    spawnMob(level, pos).ifPresent(uuid -> {
                        this.data.currentMobs.add(uuid);
                        this.data.totalMobsSpawned++;
                        this.data.nextMobSpawnsAt = level.getGameTime() + this.getConfig().ticksBetweenSpawn();
                        this.getConfig().spawnPotentialsDefinition().getRandom(level.getRandom()).ifPresent(entry -> {
                            this.data.nextSpawnData = Optional.of(entry.getData());
                            this.markUpdated();
                        });
                    });
                }
                yield TrialSpawnerState.ACTIVE;
            }

            case WAITING_FOR_REWARD_EJECTION -> {
                if (this.data.isReadyToOpenShutter(level, TrialSpawnerState.DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB, this.getTargetCooldownLength())) {
                    level.playSound(null, pos, ModSounds.TRIAL_SPAWNER_OPEN_SHUTTER.get(), SoundSource.BLOCKS);
                    yield TrialSpawnerState.EJECTING_REWARD;
                }
                yield TrialSpawnerState.WAITING_FOR_REWARD_EJECTION;
            }

            case EJECTING_REWARD -> {
                if (!this.data.isReadyToEjectItems(level, TrialSpawnerState.TIME_BETWEEN_EACH_EJECTION, this.getTargetCooldownLength())) {
                    yield TrialSpawnerState.EJECTING_REWARD;
                }
                if (this.data.detectedPlayers.isEmpty()) {
                    level.playSound(null, pos, ModSounds.TRIAL_SPAWNER_CLOSE_SHUTTER.get(), SoundSource.BLOCKS);
                    this.data.ejectingLootTable = Optional.empty();
                    yield TrialSpawnerState.COOLDOWN;
                }

                if (this.data.ejectingLootTable.isEmpty()) {
                    List<ResourceLocation> tables = this.getConfig().lootTablesToEject();
                    if (!tables.isEmpty()) {
                        this.data.ejectingLootTable = Optional.of(
                                tables.get(level.getRandom().nextInt(tables.size())).toString());
                    }
                }

                this.data.ejectingLootTable.ifPresent(tableStr ->
                        ejectReward(level, pos, new ResourceLocation(tableStr)));
                this.data.detectedPlayers.remove(this.data.detectedPlayers.iterator().next());
                yield TrialSpawnerState.EJECTING_REWARD;
            }

            case COOLDOWN -> {
                this.data.tryDetectPlayers(level, pos, this);
                if (!this.data.detectedPlayers.isEmpty()) {
                    this.data.totalMobsSpawned = 0;
                    this.data.nextMobSpawnsAt = 0L;
                    yield TrialSpawnerState.ACTIVE;
                }
                if (this.data.isCooldownFinished(level)) {
                    this.removeOminous(level, pos);
                    this.data.reset();
                    yield TrialSpawnerState.WAITING_FOR_PLAYERS;
                }
                yield TrialSpawnerState.COOLDOWN;
            }
        };
    }

    public Optional<UUID> spawnMob(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        SpawnData spawnData = this.data.getOrCreateNextSpawnData(this, random);
        CompoundTag entityTag = spawnData.getEntityToSpawn();

        Optional<EntityType<?>> optionalType = EntityType.by(entityTag);
        if (optionalType.isEmpty()) {
            JustTrialSpawners.LOGGER.warn("Trial spawner at {} failed to resolve entity type from tag: {}", pos, entityTag);
            return Optional.empty();
        }

        EntityType<?> entityType = optionalType.get();
        int spawnRange = this.getConfig().spawnRange();

        double x = pos.getX() + (random.nextDouble() - random.nextDouble()) * spawnRange + 0.5;
        double y = pos.getY() + random.nextInt(3) - 1;
        double z = pos.getZ() + (random.nextDouble() - random.nextDouble()) * spawnRange + 0.5;

        if (!level.noCollision(entityType.getAABB(x, y, z))) return Optional.empty();

        Vec3 spawnPos = new Vec3(x, y, z);
        if (!inLineOfSight(level, pos.getCenter(), spawnPos)) return Optional.empty();

        // Trial spawners ignore vanilla spawn rules (light level, etc.) - they spawn mobs unconditionally
        Entity entity = EntityType.loadEntityRecursive(entityTag, level, e -> {
            e.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
            return e;
        });

        if (entity == null) return Optional.empty();

        if (entity instanceof Mob mob) {
            if (!mob.checkSpawnObstruction(level)) return Optional.empty();

            boolean isSimpleSpawn = spawnData.getEntityToSpawn().size() == 1
                    && spawnData.getEntityToSpawn().contains("id", 8);
            if (isSimpleSpawn) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
                        MobSpawnType.SPAWNER, null, null);
            }
            mob.setPersistenceRequired();
        }

        if (!level.tryAddFreshEntityWithPassengers(entity)) return Optional.empty();

        level.levelEvent(3011, pos, 0);
        level.levelEvent(3012, BlockPos.containing(spawnPos), 0);

        return Optional.of(entity.getUUID());
    }

    public void ejectReward(ServerLevel level, BlockPos pos, ResourceLocation lootTableId) {
        LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);
        LootParams lootParams = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        List<ItemStack> items = lootTable.getRandomItems(lootParams);

        if (!items.isEmpty()) {
            for (ItemStack item : items) {
                net.minecraft.core.dispenser.DefaultDispenseItemBehavior.spawnItem(
                        level, item, 2, Direction.UP,
                        Vec3.atBottomCenterOf(pos).add(0, 1.2, 0));
            }
            level.levelEvent(3014, pos, 0);
        }
    }

    private void spawnOminousItem(ServerLevel level, BlockPos pos) {
        SimpleWeightedRandomList<ItemStack> dispensingItems = this.data.getDispensingItems(level, this.getConfig(), pos);
        Optional<ItemStack> optionalItem = dispensingItems.getRandomValue(level.random);
        if (optionalItem.isEmpty() || optionalItem.get().isEmpty()) return;

        if (level.getGameTime() < this.data.cooldownEndsAt) return;

        calculatePositionToSpawnItem(level, pos).ifPresent(spawnPos -> {
            ItemStack itemStack = optionalItem.get().copy();
            net.minecraft.world.entity.item.ItemEntity itemEntity =
                    new net.minecraft.world.entity.item.ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, itemStack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);

            float pitch = (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F;
            level.playSound(null, BlockPos.containing(spawnPos), ModSounds.TRIAL_SPAWNER_EJECT_ITEM.get(),
                    SoundSource.BLOCKS, 1.0F, pitch);
            this.data.cooldownEndsAt = level.getGameTime() + this.getConfig().ticksBetweenItemSpawners();
        });
    }

    private Optional<Vec3> calculatePositionToSpawnItem(ServerLevel level, BlockPos pos) {
        List<net.minecraft.world.entity.player.Player> players = this.data.detectedPlayers.stream()
                .map(level::getPlayerByUUID)
                .filter(java.util.Objects::nonNull)
                .filter(p -> !p.isCreative() && !p.isSpectator() && p.isAlive()
                        && p.distanceToSqr(pos.getCenter()) <= (double) Mth.square(this.getRequiredPlayerRange()))
                .toList();

        if (players.isEmpty()) return Optional.empty();

        Entity target = selectEntityToSpawnItemAbove(players, this.data.currentMobs, pos, level);
        if (target == null) return Optional.empty();

        return calculatePositionAbove(target, level);
    }

    private static Optional<Vec3> calculatePositionAbove(Entity entity, ServerLevel level) {
        Vec3 entityPos = entity.position();
        Vec3 abovePos = entityPos.add(0, (double)(entity.getBbHeight() + 2.0F + (float)level.random.nextInt(4)), 0);
        BlockHitResult hitResult = level.clip(new ClipContext(entityPos, abovePos,
                ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (Entity) null));
        Vec3 spawnPos = hitResult.getBlockPos().getCenter().add(0, -1.0, 0);
        BlockPos blockPos = BlockPos.containing(spawnPos);
        return level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty()
                ? Optional.of(spawnPos) : Optional.empty();
    }

    @javax.annotation.Nullable
    private static Entity selectEntityToSpawnItemAbove(
            List<net.minecraft.world.entity.player.Player> players, Set<UUID> trackedMobs,
            BlockPos pos, ServerLevel level) {
        List<Entity> aliveMobs = trackedMobs.stream()
                .map(level::getEntity)
                .filter(java.util.Objects::nonNull)
                .filter(e -> e.isAlive() && e.distanceToSqr(pos.getCenter()) <= (double) Mth.square(MAX_MOB_TRACKING_DISTANCE))
                .toList();

        boolean preferMobs = level.random.nextBoolean();
        List<? extends Entity> candidates = preferMobs ? aliveMobs : players;
        if (candidates.isEmpty()) {
            candidates = preferMobs ? players : aliveMobs;
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(level.random.nextInt(candidates.size()));
    }

    public void tickClient(Level level, BlockPos pos, boolean ominous) {
        TrialSpawnerState state = this.getState();
        state.emitParticles(level, pos, ominous);

        if (state.hasSpinningMob()) {
            double countdown = Math.max(0L, this.data.nextMobSpawnsAt - level.getGameTime());
            this.data.oSpin = this.data.spin;
            this.data.spin = (this.data.spin + state.spinningMobSpeed() / (countdown + 200.0)) % 360.0;
        }

        if (state.isCapableOfSpawning()) {
            RandomSource random = level.getRandom();
            if (random.nextFloat() <= SPAWNING_AMBIENT_SOUND_CHANCE) {
                level.playLocalSound(pos, ominous ? ModSounds.TRIAL_SPAWNER_AMBIENT_OMINOUS.get()
                        : ModSounds.TRIAL_SPAWNER_AMBIENT.get(), SoundSource.BLOCKS,
                        random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
            }
        }
    }

    private static boolean shouldMobBeUntracked(ServerLevel level, BlockPos pos, UUID uuid) {
        Entity entity = level.getEntity(uuid);
        return entity == null
                || !entity.isAlive()
                || !entity.level().dimension().equals(level.dimension())
                || entity.blockPosition().distSqr(pos) > MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(Level level, Vec3 from, Vec3 to) {
        BlockHitResult result = level.clip(
                new ClipContext(to, from, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (Entity) null));
        return result.getBlockPos().equals(BlockPos.containing(from)) || result.getType() == HitResult.Type.MISS;
    }

    public static void addSpawnParticles(Level level, BlockPos pos, RandomSource random, SimpleParticleType type) {
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
            level.addParticle(type, x, y, z, 0, 0, 0);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("normal_config", normalConfig.save());
        tag.put("ominous_config", ominousConfig.save());
        tag.put("data", data.save());
        tag.putInt("target_cooldown_length", getTargetCooldownLength());
        tag.putInt("required_player_range", getRequiredPlayerRange());
        tag.putBoolean("is_ominous", isOminous);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("normal_config")) {
            this.normalConfig = TrialSpawnerConfig.load(tag.getCompound("normal_config"));
        }
        if (tag.contains("ominous_config")) {
            this.ominousConfig = TrialSpawnerConfig.load(tag.getCompound("ominous_config"));
        }
        if (tag.contains("data")) {
            TrialSpawnerData loaded = TrialSpawnerData.load(tag.getCompound("data"));
            this.data.detectedPlayers.clear();
            this.data.detectedPlayers.addAll(loaded.detectedPlayers);
            this.data.currentMobs.clear();
            this.data.currentMobs.addAll(loaded.currentMobs);
            this.data.cooldownEndsAt = loaded.cooldownEndsAt;
            this.data.nextMobSpawnsAt = loaded.nextMobSpawnsAt;
            this.data.totalMobsSpawned = loaded.totalMobsSpawned;
            this.data.nextSpawnData = loaded.nextSpawnData;
            this.data.ejectingLootTable = loaded.ejectingLootTable;
        }
        this.isOminous = tag.getBoolean("is_ominous");
    }

    public interface StateAccessor {
        void setState(Level level, TrialSpawnerState state);
        TrialSpawnerState getState();
        void markUpdated();
    }
}
