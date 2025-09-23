package io.github.lucaargolo.kibe.utils

import thedarkcolour.kotlinforforge.forge.ObjectHolderDelegate

abstract class GenericCompendium<T: Any> {

    protected open fun <E: T> register(string: String, entry: () -> E): () -> E {
        return entry
    }

    abstract fun initialize()

    abstract fun initializeClient()


}