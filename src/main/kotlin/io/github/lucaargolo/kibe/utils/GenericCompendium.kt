package io.github.lucaargolo.kibe.utils

import java.util.function.Supplier

abstract class GenericCompendium<T: Any> {

    protected open fun <E: T> register(string: String, entry: Supplier<E>): Supplier<E> {
        return entry
    }

    abstract fun initialize()

    abstract fun initializeClient()


}