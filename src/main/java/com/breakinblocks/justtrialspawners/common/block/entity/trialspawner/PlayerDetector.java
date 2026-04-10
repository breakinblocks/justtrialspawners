package com.breakinblocks.justtrialspawners.common.block.entity.trialspawner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.UUID;

@FunctionalInterface
public interface PlayerDetector {

    PlayerDetector NO_CREATIVE_PLAYERS = (level, pos, range, requireLineOfSight) ->
            level.getPlayers(player ->
                    player.blockPosition().closerThan(pos, range)
                            && !player.isCreative()
                            && !player.isSpectator()
            ).stream()
                    .filter(player -> !requireLineOfSight || inLineOfSight(level, pos.getCenter(), player.getEyePosition()))
                    .map(Entity::getUUID)
                    .toList();

    PlayerDetector INCLUDING_CREATIVE_PLAYERS = (level, pos, range, requireLineOfSight) ->
            level.getPlayers(player ->
                    player.blockPosition().closerThan(pos, range)
                            && !player.isSpectator()
            ).stream()
                    .filter(player -> !requireLineOfSight || inLineOfSight(level, pos.getCenter(), player.getEyePosition()))
                    .map(Entity::getUUID)
                    .toList();

    List<UUID> detect(ServerLevel level, BlockPos pos, double range, boolean requireLineOfSight);

    static boolean inLineOfSight(Level level, Vec3 spawnerPos, Vec3 playerPos) {
        BlockHitResult result = level.clip(
                new ClipContext(playerPos, spawnerPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity) null)
        );
        return result.getBlockPos().equals(BlockPos.containing(spawnerPos)) || result.getType() == HitResult.Type.MISS;
    }
}
