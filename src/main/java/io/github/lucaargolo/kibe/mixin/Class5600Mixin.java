package io.github.lucaargolo.kibe.mixin;

import com.google.common.collect.ImmutableMap;
import io.github.lucaargolo.kibe.blocks.entangledchest.EntangledChestEntityRenderer;
import io.github.lucaargolo.kibe.blocks.entangledtank.EntangledTankEntityRenderer;
import io.github.lucaargolo.kibe.blocks.miscellaneous.RedstoneTimerEntity;
import io.github.lucaargolo.kibe.blocks.miscellaneous.RedstoneTimerEntityRenderer;
import net.minecraft.class_5600;
import net.minecraft.class_5607;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(class_5600.class)
public class Class5600Mixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;"), method = "method_32073")
    private static ImmutableMap.Builder<EntityModelLayer, class_5607> builderRedirect() {
        ImmutableMap.Builder<EntityModelLayer, class_5607> builder = ImmutableMap.builder();
        builder.putAll(EntangledChestEntityRenderer.Companion.getHelper().getEntries());
        builder.putAll(EntangledTankEntityRenderer.Companion.getHelper().getEntries());
        AtomicInteger integer = new AtomicInteger();
        RedstoneTimerEntityRenderer.Companion.getSelectorModelLayers().forEach(entityModelLayer -> {
            builder.put(entityModelLayer, RedstoneTimerEntityRenderer.Companion.setupSelectorModel(integer.getAndIncrement()));
        });
        return builder;
    }


}
