package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.miscellaneous.HeaterBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class BiomeMixin {

    @Inject(at = @At("HEAD"), method = "canSetIce(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Z)Z", cancellable = true)
    private void canSetIce(WorldView world, BlockPos pos, boolean doWaterCheck, CallbackInfoReturnable<Boolean> cir) {
        if(world instanceof ServerWorld) {
            if(HeaterBlockEntity.Companion.isBeingHeated((ServerWorld) world, new ChunkPos(pos))) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "canSetSnow", cancellable = true)
    private void canSetSnow(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(world instanceof ServerWorld) {
            if(HeaterBlockEntity.Companion.isBeingHeated((ServerWorld) world, new ChunkPos(pos))) {
                cir.setReturnValue(false);
            }
        }
    }

}
