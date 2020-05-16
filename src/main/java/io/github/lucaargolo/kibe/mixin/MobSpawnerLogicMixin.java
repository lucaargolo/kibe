package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.miscellaneous.InfectedDirt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {

    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract BlockPos getPos();

    @Inject(at = @At("HEAD"), method = "isPlayerInRange", cancellable = true)
    private void isPlayerInRange(CallbackInfoReturnable<Boolean> info) {
        boolean returnValue = this.getWorld().getBlockState(this.getPos().down()).getBlock() instanceof InfectedDirt;
        if(returnValue) info.setReturnValue(true);
    }
}
