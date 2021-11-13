package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.miscellaneous.DehumidifierBlockEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

//    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getTopY(Lnet/minecraft/world/Heightmap$Type;II)I"), method = "renderWeather")
//    private int onRenderWeather(ClientWorld clientWorld, Heightmap.Type heightmap, int x, int z) {
//        if(DehumidifierBlockEntity.Companion.isBeingDehumidified(new ChunkPos(new BlockPos(x, 0, z)))) {
//            return 512;
//        }
//        return clientWorld.getTopY(heightmap, x, z);
//    }

//    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getTopPosition(Lnet/minecraft/world/Heightmap$Type;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"), method = "tickRainSplashing")
//    private BlockPos onTickRainSplashing(ClientWorld clientWorld, Heightmap.Type heightmap, BlockPos pos) {
//        if(DehumidifierBlockEntity.Companion.isBeingDehumidified(new ChunkPos(pos))) {
//            return new BlockPos(pos.getX(), 512, pos.getZ());
//        }
//        return clientWorld.getTopPosition(heightmap, pos);
//    }


}
