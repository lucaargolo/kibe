package io.github.lucaargolo.kibe.mixin;

import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PersistentStateManager.class)
public interface PersistentStateManagerAccessor {

    @Accessor
    Map<String, PersistentState> getLoadedStates();

}
