package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.vacuum.VacuumHopperEntity;
import io.github.lucaargolo.kibe.items.ItemCompendiumKt;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow public abstract ItemStack getStack();

    @Shadow public abstract void setStack(ItemStack stack);

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {
        //Oh shit, theres a vacuum hopper here, i am being S U C C
        for(BlockEntity blockEntity : world.blockEntities) {
            if(blockEntity instanceof VacuumHopperEntity) {
                BlockPos pos = blockEntity.getPos();
                Vec3d vecPos = new Vec3d(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
                double distance = this.getPos().distanceTo(vecPos);
                if(distance < 8.0) {
                    if(distance < 1.0) {
                        this.setStack(((VacuumHopperEntity) blockEntity).addStack(this.getStack()));
                    }
                    Vec3d vel = this.getPos().reverseSubtract(vecPos).normalize().multiply(0.1);
                    this.addVelocity(vel.x, vel.y, vel.z);
                }
            }
        }
        //Oh shit, theres a magnet here, i am being P U L L E D
        for(PlayerEntity playerEntity : world.getPlayers()) {
            double distance = this.getPos().distanceTo(playerEntity.getPos());
            if(distance < 8.0) {
                ItemStack magnet = new ItemStack(ItemCompendiumKt.getMAGNET());
                if(playerEntity.inventory.contains(magnet)) {
                    List<DefaultedList<ItemStack>> combinedInventory = ((PlayerInventoryMixin) playerEntity.inventory).getCombinedInventory();

                    for (List<ItemStack> list : combinedInventory) {
                        for (ItemStack itemStack : list) {
                            if (itemStack.getItem().equals(ItemCompendiumKt.getMAGNET()) && itemStack.hasTag() && itemStack.getTag().contains("enabled") && itemStack.getTag().getBoolean("enabled")) {
                                Vec3d vel = this.getPos().reverseSubtract(playerEntity.getPos()).normalize().multiply(0.1);
                                this.addVelocity(vel.x, vel.y, vel.z);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

}
