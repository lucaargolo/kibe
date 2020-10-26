package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {

    @Inject(at = @At("HEAD"), method = "isSpawnDark", cancellable = true)
    private static void isSpawnDark(ServerWorldAccess world, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> info) {
        if(BigTorchBlockEntity.Companion.isChunkSuppressed(world.toServerWorld().getRegistryKey(), new ChunkPos(pos))) {
            info.setReturnValue(false);
        }
    }

}
