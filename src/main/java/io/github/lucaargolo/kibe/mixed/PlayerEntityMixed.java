package io.github.lucaargolo.kibe.mixed;

import kotlin.Pair;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface PlayerEntityMixed {

    List<Pair<ItemStack, Long>> getKibe_activeRingsList();

}
