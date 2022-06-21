package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnRestriction.class)
public class SpawnRestrictionMixin {

    @Inject(at = @At("HEAD"), method = "canSpawn")
    private static <T extends Entity> void headCanSpawn(EntityType<T> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (BigTorchBlockEntity.Companion.isChunkSuppressed(world.toServerWorld().getRegistryKey(), new ChunkPos(pos))) {
            BigTorchBlockEntity.Companion.setTestingThread(Thread.currentThread());
            BigTorchBlockEntity.Companion.setTesting(true);
        }
    }

    @Inject(at = @At("TAIL"), method = "canSpawn")
    private static <T extends Entity> void tailCanSpawn(EntityType<T> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        BigTorchBlockEntity.Companion.setTestingThread(null);
        BigTorchBlockEntity.Companion.setTesting(false);
    }

}