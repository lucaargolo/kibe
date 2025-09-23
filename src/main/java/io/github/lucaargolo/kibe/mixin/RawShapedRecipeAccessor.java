package io.github.lucaargolo.kibe.mixin;

import net.minecraft.recipe.RawShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RawShapedRecipe.class)
public interface RawShapedRecipeAccessor {

    @Accessor
    boolean isSymmetrical();

}
