package io.github.lucaargolo.kibe.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;

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
