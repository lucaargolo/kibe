package io.github.lucaargolo.kibe.utils

import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

open class RegistryCompendium<T: Any>(private val registry: Registry<T>): GenericCompendium<T>() {

    val tags = mutableMapOf<TagKey<T>, TagEntry<T>>()

    fun get(identifier: Identifier): T? {
        return registry.get(identifier)
    }

    fun getId(entry: T): Identifier? {
        return registry.getId(entry)
    }

    protected open fun <E: T> register(string: String, entry: E, vararg tags: TagKey<T>): Lazy<E> {
        return register(ModIdentifier.of(string), entry, *tags)
    }

    protected open fun <E: T> register(identifier: Identifier, entry: E, vararg tags: TagKey<T>): Lazy<E> {
        tags.forEach { tag ->
            this.tags.getOrPut(tag) {
                TagEntry(tag, mutableListOf(), mutableListOf())
            }.values.add(entry)
        }
        return super.register(identifier, entry)
    }

    protected open fun <E: T> registerReference(string: String, entry: E): RegistryEntry<T> {
        return registerReference(ModIdentifier.of(string), entry)
    }

    protected open fun <E: T> registerReference(identifier: Identifier, entry: E): RegistryEntry<T> {
        return Registry.registerReference(registry, identifier, entry)
    }

    protected open fun registerTag(string: String, vararg values: T, children: MutableCollection<TagKey<T>> = mutableListOf()): TagEntry<T> {
        return registerTag(ModIdentifier.of(string), *values, children = children)
    }

    protected open fun registerTag(identifier: Identifier, vararg values: T, children: MutableCollection<TagKey<T>> = mutableListOf()): TagEntry<T> {
        val key = TagKey.of(registry.key, identifier)
        if(tags.containsKey(key)) {
            throw AssertionError("Tag was already registered: $key")
        }
        val entry = TagEntry(key, children, values.toMutableList())
        tags[key] = entry
        return entry
    }

    protected open fun <K: Any> registerAssociatedTag(string: String, vararg associations: Pair<K, T>): AssociatedTagEntry<T, K> {
        return registerAssociatedTag(ModIdentifier.of(string), *associations)
    }

    protected open fun <K: Any> registerAssociatedTag(identifier: Identifier, vararg associations: Pair<K, T>): AssociatedTagEntry<T, K> {
        val key = TagKey.of(registry.key, identifier)
        if(tags.containsKey(key)) {
            throw AssertionError("Tag was already registered: $key")
        }
        val entry = AssociatedTagEntry(key, associations.toMap(mutableMapOf()))
        tags[key] = entry
        return entry
    }

    override fun initialize() {
        entries.forEach { (identifier, entry) ->
            Registry.register(registry, identifier, entry)
        }
    }

    override fun initializeClient() {

    }

    open class TagEntry<T: Any>(val key: TagKey<T>, val children: MutableCollection<TagKey<T>>, val values: MutableCollection<T>)

    class AssociatedTagEntry<T: Any, K: Any>(tag: TagKey<T>, val associations: MutableMap<K, T>): TagEntry<T>(tag, mutableListOf(), associations.values)

}