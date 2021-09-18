package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.miscellaneous.CursedDirt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {

    @Inject(at = @At("HEAD"), method = "isPlayerInRange", cancellable = true)
    private void isPlayerInRange(World world, BlockPos blockPos, CallbackInfoReturnable<Boolean> info) {
        boolean returnValue = world.getBlockState(blockPos.down()).getBlock() instanceof CursedDirt;
        if(returnValue) info.setReturnValue(true);
    }
}
