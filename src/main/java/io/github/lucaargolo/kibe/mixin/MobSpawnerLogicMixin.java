package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.block.CursedDirt;
import io.github.lucaargolo.kibe.blockentity.BigTorchBlockEntity;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {

    @Inject(at = @At("HEAD"), method = "isPlayerInRange", cancellable = true)
    private void isPlayerInRange(World world, BlockPos blockPos, CallbackInfoReturnable<Boolean> info) {
        boolean returnValue = world.getBlockState(blockPos.down()).getBlock() instanceof CursedDirt;
        if(returnValue) info.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "serverTick")
    private void serverTickHead(ServerWorld world, BlockPos blockPos, CallbackInfo ci) {
        if(world.getBlockState(blockPos.down()).getBlock() instanceof CursedDirt) {
            BigTorchBlockEntity.Companion.setException(true);
        }
    }

    @Inject(at = @At("TAIL"), method = "serverTick")
    private void serverTickTail(ServerWorld world, BlockPos blockPos, CallbackInfo ci) {
        BigTorchBlockEntity.Companion.setException(false);
    }

}
