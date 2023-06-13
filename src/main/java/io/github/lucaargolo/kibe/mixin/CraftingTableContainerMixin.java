package io.github.lucaargolo.kibe.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingScreenHandler.class)
public interface CraftingTableContainerMixin {

    @Accessor
    RecipeInputInventory getInput();
    @Accessor
    CraftingResultInventory getResult();
    @Accessor
    ScreenHandlerContext getContext();
    @Accessor
    PlayerEntity getPlayer();
}
