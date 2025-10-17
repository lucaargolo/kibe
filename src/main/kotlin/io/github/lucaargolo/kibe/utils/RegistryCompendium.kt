package io.github.lucaargolo.kibe.utils

import io.github.lucaargolo.kibe.KibeMod
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull

open class RegistryCompendium<T: Any>(private val registry: Registry<T>): GenericCompendium<T>() {

    val DEFERRED = DeferredRegister.create(registry, KibeMod.MOD_ID)

    val entries = mutableMapOf<String, DeferredHolder<T, out T>>()
    val tags = mutableMapOf<TagKey<T>, TagEntry<T>>()

    fun get(identifier: Identifier): T? {
        return registry.get(identifier)
    }

    fun getId(entry: T): Identifier? {
        return registry.getKey(entry).getOrNull()?.value
    }

    override fun <E : T> register(string: String, entry: Supplier<E>): DeferredHolder<T, E> {
        val holder = DEFERRED.register(string, entry)
        entries[string] = holder
        return holder
    }

    protected open fun <E : T> register(string: String, entry: Supplier<E>, vararg tags: TagKey<T>): DeferredHolder<T, E> {
        return register(string, entry).also { lazy ->
            tags.forEach { tag ->
                this.tags.getOrPut(tag) {
                    TagEntry(tag, mutableListOf(), mutableListOf())
                }.values.add(lazy)
            }
        }
    }

    protected open fun registerTag(string: String, vararg values: String, children: MutableCollection<TagKey<T>> = mutableListOf()): TagEntry<T> {
        return registerTag(ModIdentifier.of(string), *values, children = children)
    }

    protected open fun registerTag(identifier: Identifier, vararg values: String, children: MutableCollection<TagKey<T>> = mutableListOf()): TagEntry<T> {
        val key = TagKey.of(registry.key, identifier)
        if(tags.containsKey(key)) {
            throw AssertionError("Tag was already registered: $key")
        }
        val entry = TagEntry(key, children, values.mapNotNull(entries::get).toMutableList())
        tags[key] = entry
        return entry
    }

    protected open fun <K: Any> registerAssociatedTag(string: String, vararg associations: Pair<K, String>): AssociatedTagEntry<T, K> {
        return registerAssociatedTag(ModIdentifier.of(string), *associations)
    }

    protected open fun <K: Any> registerAssociatedTag(identifier: Identifier, vararg associations: Pair<K, String>): AssociatedTagEntry<T, K> {
        val key = TagKey.of(registry.key, identifier)
        if(tags.containsKey(key)) {
            throw AssertionError("Tag was already registered: $key")
        }
        val entry = AssociatedTagEntry(key, associations.mapNotNull{ p -> entries[p.second]?.let { Pair(p.first, it) } }.toMap(mutableMapOf()))
        tags[key] = entry
        return entry
    }

    override fun initialize() {
        DEFERRED.register(MOD_BUS)
    }

    override fun initializeClient() { }

    open class TagEntry<T: Any>(val key: TagKey<T>, val children: MutableCollection<TagKey<T>>, val values: MutableCollection<DeferredHolder<T, out T>>)

    class AssociatedTagEntry<T: Any, K: Any>(tag: TagKey<T>, val associations: MutableMap<K, DeferredHolder<T, out T>>): TagEntry<T>(tag, mutableListOf(), associations.values)

}