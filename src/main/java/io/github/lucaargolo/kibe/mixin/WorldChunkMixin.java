package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.utils.SyncableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V"), method = "method_31716", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void onBlockEntityUpdate(BlockPos pos, BlockEntityType<?> type, NbtCompound nbtCompound, CallbackInfo info, BlockEntity blockEntity) {
        if(blockEntity instanceof SyncableBlockEntity) {
            ((SyncableBlockEntity) blockEntity).readClientNbt(nbtCompound);
            info.cancel();
        }
    }

}
