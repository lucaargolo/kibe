package io.github.lucaargolo.kibe.mixin;

import com.google.common.collect.ImmutableMap;
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChestEntityRenderer;
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntityRenderer;
import io.github.lucaargolo.kibe.blocks.miscellaneous.RedstoneTimerEntityRenderer;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(EntityModels.class)
public class EntityModelsMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;"), method = "getModels")
    private static ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builderRedirect() {
        ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builder = ImmutableMap.builder();
        builder.putAll(EntangledChestEntityRenderer.Companion.getHelper().getEntries());
        builder.putAll(EntangledTankEntityRenderer.Companion.getHelper().getEntries());
        AtomicInteger integer = new AtomicInteger();
        RedstoneTimerEntityRenderer.Companion.getSelectorModelLayers().forEach(entityModelLayer -> {
            builder.put(entityModelLayer, RedstoneTimerEntityRenderer.Companion.setupSelectorModel(integer.getAndIncrement()));
        });
        return builder;
    }


}
