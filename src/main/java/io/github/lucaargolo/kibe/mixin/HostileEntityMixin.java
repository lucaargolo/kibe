package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.KibeModKt;
import io.github.lucaargolo.kibe.blocks.bigtorch.BigTorchBlockEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {

    @Inject(at = @At("HEAD"), method = "isSpawnDark", cancellable = true)
    private static void isSpawnDark(ServerWorldAccess world, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> info) {
        LinkedHashMap<WorldAccess, List<BigTorchBlockEntity>> bigTorchMap = KibeModKt.getBIG_TORCH_MAP();
        List<BigTorchBlockEntity> list = bigTorchMap.get(world);
        if(list != null) {
            list.forEach(bigTorchBlockEntity -> {
                int radius = bigTorchBlockEntity.getChunkRadius();
                ChunkPos torchChunk = new ChunkPos(bigTorchBlockEntity.getPos());
                ChunkPos spawnChunk = new ChunkPos(pos);
                int chunkDistance = MathHelper.ceil(MathHelper.sqrt(Math.pow(torchChunk.x - spawnChunk.x, 2) + Math.pow(torchChunk.z - spawnChunk.z, 2)));
                if(chunkDistance < radius) {
//                    bigTorchBlockEntity.setSuppressedSpawns(bigTorchBlockEntity.getSuppressedSpawns()+1);
//                    bigTorchBlockEntity.markDirty();
//                    bigTorchBlockEntity.sync();
                    info.setReturnValue(false);
                }
            });
        }
    }

}
