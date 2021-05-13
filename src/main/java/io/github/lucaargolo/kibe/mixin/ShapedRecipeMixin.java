package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.items.ItemCompendiumKt;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

    @Shadow public abstract ItemStack getOutput();

    @Inject(at = @At("HEAD"), method = "matchesPattern", cancellable = true)
    private void matchesSmall(CraftingInventory inv, int offsetX, int offsetY, boolean bl, CallbackInfoReturnable<Boolean> info) {
        if(getOutput().getItem() == ItemCompendiumKt.getGLIDER_LEFT_WING() || getOutput().getItem() == ItemCompendiumKt.getGLIDER_RIGHT_WING()) {
            if(bl) info.setReturnValue(false);
        }
    }

}
