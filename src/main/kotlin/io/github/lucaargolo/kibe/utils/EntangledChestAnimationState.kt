package io.github.lucaargolo.kibe.utils

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.apache.commons.lang3.tuple.MutablePair

object EntangledChestAnimationState {

    const val MAX = 10
    private val DEFAULT = MutablePair(0f, false)

    private val map = mutableMapOf<Pair<String, String>, MutablePair<Float, Boolean>>()

    fun state(pair: Pair<String, String>): Float {
        return map.getOrDefault(pair, DEFAULT).left
    }

    fun update(opened: Set<Pair<String, String>>) {
        map.forEach { (key, pair) ->
            if(!opened.contains(key)) {
                pair.setRight(false)
            }
        }
        opened.forEach { code ->
            if (!map.containsKey(code)) {
                map.put(code, MutablePair(0f, true))
            }
        }

    }

    fun initializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register{ client ->
            tick()
        }
    }

    private fun tick() {
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val pair = iterator.next().value
            if(pair.right && pair.left < MAX) {
                pair.left++
            }else if(!pair.right) {
                if(pair.left > 0) {
                    pair.left--
                }else{
                    iterator.remove()
                }
            }
        }
    }

}