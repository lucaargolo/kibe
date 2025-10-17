package io.github.lucaargolo.kibe.utils

import net.minecraft.util.Identifier

abstract class GenericCompendium<T: Any> {

    val entries = mutableMapOf<Identifier, T>()

    protected open fun <E: T> register(string: String, entry: E): Lazy<E> {
        return register(ModIdentifier.of(string), entry)
    }

    protected open fun <E: T> register(identifier: Identifier, entry: E): Lazy<E> {
        if(entries.containsKey(identifier)) {
            throw AssertionError("Entry was already registered: $identifier")
        }
        entries[identifier] = entry
        return lazy { entry }
    }

    abstract fun initialize()

    abstract fun initializeClient()

}