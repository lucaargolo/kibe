package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.item.ItemCompendium;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

    @Shadow @Final RawShapedRecipe raw;
    @Shadow @Final ItemStack result;

    @Inject(at = @At("HEAD"), method = "matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z", cancellable = true)
    private void matchesSmall(CraftingRecipeInput craftingRecipeInput, World world, CallbackInfoReturnable<Boolean> cir) {
        if(result.getItem() == ItemCompendium.INSTANCE.getGLIDER_LEFT_WING() || result.getItem() == ItemCompendium.INSTANCE.getGLIDER_RIGHT_WING()) {
            if(((RawShapedRecipeAccessor) (Object) this.raw).isSymmetrical()) cir.setReturnValue(false);
        }
    }

}
