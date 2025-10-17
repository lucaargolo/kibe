package io.github.lucaargolo.kibe.utils

import java.util.function.Supplier

abstract class GenericCompendium<T: Any> {

    val entries = mutableMapOf<String, Lazy<T>>()

    protected open fun <E: T> register(string: String, entry: Supplier<E>): Lazy<E> {
        if(entries.containsKey(string)) {
            throw AssertionError("Kibe entry was already registered: $string")
        }
        val lazy = lazy { entry.get() }
        entries[string] = lazy
        return lazy
    }

    abstract fun initialize()

    abstract fun initializeClient()

}