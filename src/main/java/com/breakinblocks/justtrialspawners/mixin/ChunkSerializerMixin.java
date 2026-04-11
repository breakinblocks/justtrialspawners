package com.breakinblocks.justtrialspawners.mixin;

import com.breakinblocks.justtrialspawners.migration.TrialsMigrationHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    @Inject(method = "read", at = @At("HEAD"))
    private static void jts_rewriteTrialsNbtBeforeDeserialization(
            ServerLevel level,
            PoiManager poi,
            ChunkPos chunkPos,
            CompoundTag tag,
            CallbackInfoReturnable<ProtoChunk> cir) {
        TrialsMigrationHandler.rewriteChunkNbt(tag);
    }
}
