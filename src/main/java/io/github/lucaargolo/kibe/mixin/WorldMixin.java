package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.miscellaneous.DehumidifierBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {

    @Shadow @Final
    public boolean isClient;

    @Inject(at = @At("HEAD"), method = "hasRain", cancellable = true)
    private void hasRain(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(isClient) {
            if(DehumidifierBlockEntity.Companion.isBeingDehumidified(new ChunkPos(pos))) {
                cir.setReturnValue(false);
            }
        }
    }

}
