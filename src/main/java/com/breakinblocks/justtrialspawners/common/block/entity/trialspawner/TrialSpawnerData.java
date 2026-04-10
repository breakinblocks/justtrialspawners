package com.breakinblocks.justtrialspawners.common.block.entity.trialspawner;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.registry.ModMobEffects;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * Runtime mutable data for a trial spawner - player tracking, mob tracking, timing.
 */
public class TrialSpawnerData {
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;

    protected final Set<UUID> detectedPlayers = new HashSet<>();
    protected final Set<UUID> currentMobs = new HashSet<>();
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<SpawnData> nextSpawnData = Optional.empty();
    protected Optional<String> ejectingLootTable = Optional.empty();

    @Nullable
    protected Entity displayEntity;
    @Nullable
    private SimpleWeightedRandomList<ItemStack> dispensing;
    protected double spin;
    protected double oSpin;

    public void setNextSpawnData(SpawnData spawnData) {
        this.nextSpawnData = Optional.of(spawnData);
    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
        this.nextSpawnData = Optional.empty();
    }

    public boolean hasMobToSpawn(TrialSpawner spawner, RandomSource random) {
        boolean flag = this.getOrCreateNextSpawnData(spawner, random).getEntityToSpawn().contains("id", 8);
        return flag || !spawner.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig config, int additionalPlayers) {
        return this.totalMobsSpawned >= config.calculateTargetTotalMobs(additionalPlayers);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel level, TrialSpawnerConfig config, int additionalPlayers) {
        return level.getGameTime() >= this.nextMobSpawnsAt
                && this.currentMobs.size() < config.calculateTargetSimultaneousMobs(additionalPlayers);
    }

    public int countAdditionalPlayers(BlockPos pos) {
        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel level, BlockPos pos, TrialSpawner spawner) {
        boolean shouldSkip = (pos.asLong() + level.getGameTime()) % DELAY_BETWEEN_PLAYER_SCANS != 0L;
        if (shouldSkip) return;

        if (spawner.getState().equals(TrialSpawnerState.COOLDOWN) && spawner.isOminous()) return;

        List<UUID> detected = PlayerDetector.NO_CREATIVE_PLAYERS.detect(
                level, pos, spawner.getRequiredPlayerRange(), true);

        // Check for ominous effect conversion
        if (!spawner.isOminous() && !detected.isEmpty()) {
            Optional<Player> ominousPlayer = findPlayerWithOminousEffect(level, detected);
            if (ominousPlayer.isPresent()) {
                Player player = ominousPlayer.get();
                transformBadOmenIntoTrialOmen(player);
                spawner.applyOminous(level, pos);
                return;
            }
        }

        if (spawner.getState().equals(TrialSpawnerState.COOLDOWN)) return;

        boolean wasEmpty = this.detectedPlayers.isEmpty();
        List<UUID> playersToAdd = wasEmpty ? detected :
                PlayerDetector.NO_CREATIVE_PLAYERS.detect(level, pos, spawner.getRequiredPlayerRange(), false);

        if (this.detectedPlayers.addAll(playersToAdd)) {
            this.nextMobSpawnsAt = Math.max(level.getGameTime() + 40L, this.nextMobSpawnsAt);
            int levelEventId = spawner.isOminous() ? 3019 : 3013;
            level.levelEvent(levelEventId, pos, this.detectedPlayers.size());
        }
    }

    private Optional<Player> findPlayerWithOminousEffect(ServerLevel level, List<UUID> playerUUIDs) {
        Player badOmenPlayer = null;

        for (UUID uuid : playerUUIDs) {
            Player player = level.getPlayerByUUID(uuid);
            if (player != null) {
                // Check for Trial Omen first (higher priority)
                if (player.hasEffect(ModMobEffects.TRIAL_OMEN.get())) {
                    return Optional.of(player);
                }
                // Then check for Bad Omen
                if (player.hasEffect(MobEffects.BAD_OMEN)) {
                    badOmenPlayer = player;
                }
            }
        }

        return Optional.ofNullable(badOmenPlayer);
    }

    private static void transformBadOmenIntoTrialOmen(Player player) {
        MobEffectInstance badOmen = player.getEffect(MobEffects.BAD_OMEN);
        if (badOmen != null) {
            int amplifier = badOmen.getAmplifier() + 1;
            int duration = TRIAL_OMEN_PER_BAD_OMEN_LEVEL * amplifier;
            player.removeEffect(MobEffects.BAD_OMEN);
            player.addEffect(new MobEffectInstance(ModMobEffects.TRIAL_OMEN.get(), duration, 0));
        }
    }

    public void resetAfterBecomingOminous(TrialSpawner spawner, ServerLevel level) {
        // Remove currently tracked mobs
        this.currentMobs.stream().map(level::getEntity).forEach(entity -> {
            if (entity != null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        });

        if (!spawner.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.nextSpawnData = Optional.empty();
        }

        this.totalMobsSpawned = 0;
        this.currentMobs.clear();
        this.nextMobSpawnsAt = level.getGameTime() + spawner.getOminousConfig().ticksBetweenSpawn();
        spawner.markUpdated();
        this.cooldownEndsAt = level.getGameTime() + spawner.getOminousConfig().ticksBetweenItemSpawners();
    }

    public boolean isReadyToOpenShutter(ServerLevel level, float delay, int targetCooldownLength) {
        long shutterOpenTime = this.cooldownEndsAt - targetCooldownLength;
        return (float) level.getGameTime() >= (float) shutterOpenTime + delay;
    }

    public boolean isReadyToEjectItems(ServerLevel level, float interval, int targetCooldownLength) {
        long baseTime = this.cooldownEndsAt - targetCooldownLength;
        return (float) (level.getGameTime() - baseTime) % interval == 0.0F;
    }

    public boolean isCooldownFinished(ServerLevel level) {
        return level.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(TrialSpawner spawner, RandomSource random, EntityType<?> entityType) {
        this.getOrCreateNextSpawnData(spawner, random).getEntityToSpawn()
                .putString("id", EntityType.getKey(entityType).toString());
    }

    protected SpawnData getOrCreateNextSpawnData(TrialSpawner spawner, RandomSource random) {
        if (this.nextSpawnData.isPresent()) {
            return this.nextSpawnData.get();
        }

        SimpleWeightedRandomList<SpawnData> potentials = spawner.getConfig().spawnPotentialsDefinition();
        Optional<SpawnData> optional = potentials.isEmpty()
                ? this.nextSpawnData
                : potentials.getRandom(random).map(WeightedEntry.Wrapper::getData);

        this.nextSpawnData = Optional.of(optional.orElseGet(SpawnData::new));
        spawner.markUpdated();
        return this.nextSpawnData.get();
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(TrialSpawner spawner, Level level, TrialSpawnerState state) {
        if (!state.hasSpinningMob()) return null;

        if (this.displayEntity == null) {
            CompoundTag entityTag = this.getOrCreateNextSpawnData(spawner, level.getRandom()).getEntityToSpawn();
            if (entityTag.contains("id", 8)) {
                this.displayEntity = EntityType.loadEntityRecursive(entityTag, level, Function.identity());
            }
        }
        return this.displayEntity;
    }

    SimpleWeightedRandomList<ItemStack> getDispensingItems(ServerLevel level, TrialSpawnerConfig config, BlockPos pos) {
        if (this.dispensing != null) {
            return this.dispensing;
        }

        LootTable lootTable = level.getServer().getLootData().getLootTable(config.itemsToDropWhenOminous());
        LootParams lootParams = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        long seed = lowResolutionPosition(level, pos);
        ObjectArrayList<ItemStack> items = lootTable.getRandomItems(lootParams, seed);

        if (items.isEmpty()) {
            return SimpleWeightedRandomList.empty();
        }

        SimpleWeightedRandomList.Builder<ItemStack> builder = new SimpleWeightedRandomList.Builder<>();
        for (ItemStack itemStack : items) {
            builder.add(itemStack.copyWithCount(1), itemStack.getCount());
        }

        this.dispensing = builder.build();
        return this.dispensing;
    }

    private static long lowResolutionPosition(ServerLevel level, BlockPos pos) {
        BlockPos blockpos = new BlockPos(
                Mth.floor((float) pos.getX() / 30.0F),
                Mth.floor((float) pos.getY() / 20.0F),
                Mth.floor((float) pos.getZ() / 30.0F));
        return level.getSeed() + blockpos.asLong();
    }

    public double getSpin() { return this.spin; }
    public double getOSpin() { return this.oSpin; }
    public Set<UUID> getDetectedPlayers() { return this.detectedPlayers; }
    public Set<UUID> getCurrentMobs() { return this.currentMobs; }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        ListTag playersTag = new ListTag();
        for (UUID uuid : detectedPlayers) {
            playersTag.add(NbtUtils.createUUID(uuid));
        }
        tag.put("detected_players", playersTag);

        ListTag mobsTag = new ListTag();
        for (UUID uuid : currentMobs) {
            mobsTag.add(NbtUtils.createUUID(uuid));
        }
        tag.put("current_mobs", mobsTag);

        tag.putLong("cooldown_ends_at", cooldownEndsAt);
        tag.putLong("next_mob_spawns_at", nextMobSpawnsAt);
        tag.putInt("total_mobs_spawned", totalMobsSpawned);

        nextSpawnData.ifPresent(data -> {
            CompoundTag spawnTag = new CompoundTag();
            spawnTag.put("entity", data.getEntityToSpawn().copy());
            tag.put("spawn_data", spawnTag);
        });

        ejectingLootTable.ifPresent(table -> tag.putString("ejecting_loot_table", table));

        return tag;
    }

    public static TrialSpawnerData load(CompoundTag tag) {
        TrialSpawnerData data = new TrialSpawnerData();

        if (tag.contains("detected_players")) {
            ListTag playersTag = tag.getList("detected_players", 11); // IntArray type for UUID
            for (int i = 0; i < playersTag.size(); i++) {
                try {
                    data.detectedPlayers.add(NbtUtils.loadUUID(playersTag.get(i)));
                } catch (Exception e) {
                    JustTrialSpawners.LOGGER.warn("Failed to load detected player UUID", e);
                }
            }
        }

        if (tag.contains("current_mobs")) {
            ListTag mobsTag = tag.getList("current_mobs", 11);
            for (int i = 0; i < mobsTag.size(); i++) {
                try {
                    data.currentMobs.add(NbtUtils.loadUUID(mobsTag.get(i)));
                } catch (Exception e) {
                    JustTrialSpawners.LOGGER.warn("Failed to load current mob UUID", e);
                }
            }
        }

        data.cooldownEndsAt = tag.getLong("cooldown_ends_at");
        data.nextMobSpawnsAt = tag.getLong("next_mob_spawns_at");
        data.totalMobsSpawned = tag.getInt("total_mobs_spawned");

        if (tag.contains("spawn_data")) {
            CompoundTag spawnTag = tag.getCompound("spawn_data");
            SpawnData spawnData = new SpawnData();
            if (spawnTag.contains("entity")) {
                spawnData.getEntityToSpawn().merge(spawnTag.getCompound("entity"));
            }
            data.nextSpawnData = Optional.of(spawnData);
        }

        if (tag.contains("ejecting_loot_table")) {
            data.ejectingLootTable = Optional.of(tag.getString("ejecting_loot_table"));
        }

        return data;
    }
}
