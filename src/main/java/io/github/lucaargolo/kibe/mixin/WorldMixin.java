package io.github.lucaargolo.kibe.mixin;

import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(World.class)
public interface WorldMixin {

    @Accessor @Final
    List<BlockEntityTickInvoker> getBlockEntityTickers();

}
