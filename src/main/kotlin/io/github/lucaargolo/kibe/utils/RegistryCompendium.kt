package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.KibeMod
import net.minecraft.util.Identifier
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.IForgeRegistry
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.ObjectHolderDelegate
import thedarkcolour.kotlinforforge.forge.registerObject

open class RegistryCompendium<T: Any>(private val registry: IForgeRegistry<T>): GenericCompendium<T>() {

    val DEFERRED = DeferredRegister.create(registry, KibeMod.MOD_ID)

    fun get(identifier: Identifier): T? {
        return registry.getValue(identifier)
    }

    fun getId(entry: T): Identifier? {
        return registry.getKey(entry)
    }

    override fun <E : T> register(string: String, entry: () -> E): ObjectHolderDelegate<E> {
        return DEFERRED.registerObject(string, entry)
    }

    override fun initialize() {
        DEFERRED.register(MOD_BUS)
    }

    override fun initializeClient() { }

}