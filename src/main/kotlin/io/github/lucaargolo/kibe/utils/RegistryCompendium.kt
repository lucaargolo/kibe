package io.github.lucaargolo.kibe.utils

import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier

open class RegistryCompendium<T: Any>(private val registry: Registry<T>): GenericCompendium<T>() {

    fun get(identifier: Identifier): T? {
        return registry.get(identifier)
    }

    fun getId(entry: T): Identifier? {
        return registry.getId(entry)
    }

    protected open fun <E: T> registerReference(string: String, entry: E): RegistryEntry<T> {
        return registerReference(ModIdentifier.of(string), entry)
    }

    protected open fun <E: T> registerReference(identifier: Identifier, entry: E): RegistryEntry<T> {
        return Registry.registerReference(registry, identifier, entry)
    }

    override fun initialize() {
        map.forEach { (identifier, entry) ->
            Registry.register(registry, identifier, entry)
        }
    }

    override fun initializeClient() { }

}