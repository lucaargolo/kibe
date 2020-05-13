package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.miscellaneous.Elevator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract BlockState getBlockState();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "jump", cancellable = true)
    private void jump(CallbackInfo info) {
        BlockPos pos = this.getBlockPos();
        Block block = this.world.getBlockState(pos.down()).getBlock();
        if (block instanceof Elevator) {
            while(pos.getY() < 255) {
                if(world.getBlockState(pos.up()).getBlock().equals(block) && Elevator.Companion.isElevatorValid(world, pos.up())) {
                    world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
                    this.teleport(pos.up().getX()+0.5, pos.up().getY()+1.15, pos.up().getZ()+0.5);
                    break;
                }else{
                    pos = pos.up();
                }
            }
        }
    }

}
