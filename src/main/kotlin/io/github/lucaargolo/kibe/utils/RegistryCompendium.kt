package io.github.lucaargolo.kibe.utils

import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import java.util.function.Supplier

open class RegistryCompendium<T: Any>(private val registry: Registry<T>): GenericCompendium<T>() {

    val lazyTags = mutableListOf<Lazy<*>>()
    val tags = mutableMapOf<TagKey<T>, TagEntry<T>>()

    fun get(identifier: Identifier): T? {
        return registry.get(identifier)
    }

    fun getId(entry: T): Identifier? {
        return registry.getId(entry)
    }

    protected open fun <E: T> register(string: String, entry: Supplier<E>, vararg tags: TagKey<T>): Lazy<E> {
        return super.register(string, entry).also { lazy ->
            tags.forEach { tag ->
                this.tags.getOrPut(tag) {
                    TagEntry(tag, mutableListOf(), mutableListOf())
                }.values.add(lazy)
            }
        }
    }


    protected open fun <E: T> registerReference(string: String, entry: E): RegistryEntry<T> {
        return Registry.registerReference(registry, ModIdentifier.of(string), entry)
    }

    protected open fun registerTag(string: String, vararg values: String, children: MutableCollection<TagKey<T>> = mutableListOf()): Lazy<TagEntry<T>> {
        return registerTag(ModIdentifier.of(string), *values, children = children)
    }

    protected open fun registerTag(identifier: Identifier, vararg values: String, children: MutableCollection<TagKey<T>> = mutableListOf()): Lazy<TagEntry<T>> {
        return lazy {
            val key = TagKey.of(registry.key, identifier)
            if (tags.containsKey(key)) {
                throw AssertionError("Tag was already registered: $key")
            }
            val entry = TagEntry(key, children, values.mapNotNull(entries::get).toMutableList())
            tags[key] = entry
            return@lazy entry
        }.also { lazyTags.add(it) }
    }

    protected open fun <K: Any> registerAssociatedTag(string: String, vararg associations: Pair<K, String>): Lazy<AssociatedTagEntry<T, K>> {
        return registerAssociatedTag(ModIdentifier.of(string), *associations)
    }

    protected open fun <K: Any> registerAssociatedTag(identifier: Identifier, vararg associations: Pair<K, String>): Lazy<AssociatedTagEntry<T, K>> {
        return lazy {
            val key = TagKey.of(registry.key, identifier)
            if (tags.containsKey(key)) {
                throw AssertionError("Tag was already registered: $key")
            }
            val entry = AssociatedTagEntry(key, associations.mapNotNull { p -> entries[p.second]?.let { Pair(p.first, it) } }.toMap(mutableMapOf()))
            tags[key] = entry
            return@lazy entry
        }.also { lazyTags.add(it) }
    }

    override fun initialize() {
        entries.forEach { (path, entry) ->
            Registry.register(registry, ModIdentifier.of(path), entry.value)
        }
        lazyTags.forEach { tag -> tag.value }
    }

    override fun initializeClient() {

    }

    open class TagEntry<T: Any>(val key: TagKey<T>, val children: MutableCollection<TagKey<T>>, val values: MutableCollection<Lazy<T>>)

    class AssociatedTagEntry<T: Any, K: Any>(tag: TagKey<T>, val associations: MutableMap<K, Lazy<T>>): TagEntry<T>(tag, mutableListOf(), associations.values)

}