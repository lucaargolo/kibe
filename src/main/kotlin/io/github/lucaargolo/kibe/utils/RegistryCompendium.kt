package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.KibeMod
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull

open class RegistryCompendium<T: Any>(private val registry: Registry<T>): GenericCompendium<T>() {

    val DEFERRED = DeferredRegister.create(registry, KibeMod.MOD_ID)

    fun get(identifier: Identifier): T? {
        return registry.get(identifier)
    }

    fun getId(entry: T): Identifier? {
        return registry.getKey(entry).getOrNull()?.value
    }

    override fun <E : T> register(string: String, entry: Supplier<E>): DeferredHolder<T, E> {
        return DEFERRED.register(string, entry)
    }

    override fun initialize() {
        DEFERRED.register(MOD_BUS)
    }

    override fun initializeClient() { }

}