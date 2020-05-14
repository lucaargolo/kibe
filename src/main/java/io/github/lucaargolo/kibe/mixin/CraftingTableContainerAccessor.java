package io.github.lucaargolo.kibe.mixin;

import net.minecraft.container.BlockContext;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingTableContainer.class)
public interface CraftingTableContainerAccessor {

    @Accessor
    CraftingInventory getCraftingInv();

    @Accessor
    CraftingResultInventory getResultInv();

    @Accessor
    BlockContext getContext();

    @Accessor
    PlayerEntity getPlayer();
}
