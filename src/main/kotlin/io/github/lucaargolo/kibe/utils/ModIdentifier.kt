package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.KibeMod
import net.minecraft.util.Identifier

object ModIdentifier {

    fun of(path: String): Identifier = Identifier.of(KibeMod.MOD_ID, path)

}