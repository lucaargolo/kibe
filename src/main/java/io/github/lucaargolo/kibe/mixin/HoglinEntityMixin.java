package io.github.lucaargolo.kibe.mixin;

import com.google.common.collect.Sets;
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;
import java.util.Set;

@Mixin(HoglinEntity.class)
public class HoglinEntityMixin {
    private static final Set<SpawnReason> EXCLUDED_SPAWN_REASONS =
            Sets.immutableEnumSet(SpawnReason.COMMAND, SpawnReason.MOB_SUMMONED, SpawnReason.PATROL, SpawnReason.TRIGGERED);

    @Inject(at = @At("HEAD"), method = "canSpawn", cancellable = true)
    private static void canSpawn(EntityType<HoglinEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> info) {
        if (world instanceof ServerWorldAccess) {
            ServerWorldAccess server = (ServerWorldAccess) world;
            if (BigTorchBlockEntity.Companion.isChunkSuppressed(server.toServerWorld().getRegistryKey(), new ChunkPos(pos))) {
                if(!EXCLUDED_SPAWN_REASONS.contains(spawnReason)) {
                    info.setReturnValue(false);
                }
            }
        }
    }
}
