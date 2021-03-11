package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.miscellaneous.Elevator;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract BlockPos getBlockPos();
    @Shadow public World world;
    @Shadow public abstract void teleport(double destX, double destY, double destZ);

    @Shadow private Vec3d pos;

    @Inject(at = @At("HEAD"), method = "setSneaking", cancellable = true)
    private void setSneaking(boolean sneaking, CallbackInfo info) {
        if(sneaking) {
            BlockPos pos = getBlockPos();
            Block block = world.getBlockState(pos.down()).getBlock();
            if (block instanceof Elevator && world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()) {
                pos = pos.down();
                while(pos.getY() > world.getBottomY()) {
                    if(world.getBlockState(pos.down()).getBlock().equals(block) && Elevator.Companion.isElevatorValid(world, pos.down())) {
                        world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.15F + 0.6F);
                        teleport(this.pos.x, pos.down().getY()+1.15, this.pos.z);
                        break;
                    }else{
                        pos = pos.down();
                    }
                }
            }
        }
    }

}
