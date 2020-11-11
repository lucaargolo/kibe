package io.github.lucaargolo.kibe.utils

import net.minecraft.class_5603
import net.minecraft.class_5606
import net.minecraft.class_5607
import net.minecraft.class_5609
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.util.DyeColor

class EntangledRendererHelper(val parent: String) {

    private val runeModelLayers = linkedMapOf<String, EntityModelLayer>()

    init {
        (1..8).forEach { runeId ->
            DyeColor.values().forEach { runeColor ->
                runeModelLayers["rune${runeId}_${runeColor.getName()}"] = EntityModelLayers.register(parent, "rune${runeId}_${runeColor.getName()}")
            }
        }
    }

    val bottomModelLayer: EntityModelLayer = EntityModelLayers.register(parent, "bottom")
    val topModelLayer: EntityModelLayer = EntityModelLayers.register(parent, "top")
    val coreModelLayerGold: EntityModelLayer = EntityModelLayers.register(parent, "core_gold")
    val coreModelLayerDiamond: EntityModelLayer = EntityModelLayers.register(parent, "core_diamond")

    fun getRuneLayer(runeId: Int, runeColor: DyeColor): EntityModelLayer? {
        return runeModelLayers["rune${runeId}_${runeColor.getName()}"]
    }

    fun getEntries(): LinkedHashMap<EntityModelLayer, class_5607> {
        val map = linkedMapOf<EntityModelLayer, class_5607>()
        map[bottomModelLayer] = setupBottomModel()
        map[topModelLayer] = setupTopModel()
        runeModelLayers.forEach { (string, entityModelLayer) ->
            val stringList: List<String> = string.replace("rune", "").split("_")
            val runeId = stringList[0].toInt()
            val runeColor = DyeColor.byName(stringList.subList(1, stringList.size).joinToString("_"), DyeColor.WHITE)
            map[entityModelLayer] = setupRuneModel(runeId, runeColor)
        }
        map[coreModelLayerGold] = setupCoreModel(false)
        map[coreModelLayerDiamond] = setupCoreModel(true)
        return map
    }



    private fun setupBottomModel(): class_5607 {
        val lv = class_5609()
        val lv2 = lv.method_32111()
        val ySize = if(parent == "entangled_chest") 10.0f else 1.0f
        lv2.method_32117("bottom", class_5606.method_32108().method_32101(0, 0).method_32097(1.0f, 0.0f, 1.0f, 14.0f, ySize, 14.0f), class_5603.field_27701)
        return class_5607.method_32110(lv, 64, 64)
    }

    private fun setupTopModel(): class_5607 {
        val lv = class_5609()
        val lv2 = lv.method_32111()
        lv2.method_32117("top1", class_5606.method_32108().method_32101(32, 43).method_32097(1F, 14F, 1F, 2F, 1F, 14F), class_5603.field_27701)
        lv2.method_32117("top2", class_5606.method_32108().method_32101(56, 0).method_32097(3F, 14F, 1F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top3", class_5606.method_32108().method_32101(56, 3).method_32097(7F, 14F, 1F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top4", class_5606.method_32108().method_32101(56, 6).method_32097(11F, 14F, 1F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top5", class_5606.method_32108().method_32101(56, 31).method_32097(11F, 14F, 13F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top6", class_5606.method_32108().method_32101(56, 28).method_32097(7F, 14F, 13F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top7", class_5606.method_32108().method_32101(56, 25).method_32097(3F, 14F, 13F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top8", class_5606.method_32108().method_32101(56, 17).method_32097(3F, 14F, 9F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top9", class_5606.method_32108().method_32101(56, 9).method_32097(3F, 14F, 5F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top10", class_5606.method_32108().method_32101(56, 14).method_32097(11F, 14F, 5F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top11", class_5606.method_32108().method_32101(58, 12).method_32097(7F, 14F, 5F, 2F, 1F, 1F), class_5603.field_27701)
        lv2.method_32117("top12", class_5606.method_32108().method_32101(56, 22).method_32097(11F, 14F, 9F, 2F, 1F, 2F), class_5603.field_27701)
        lv2.method_32117("top13", class_5606.method_32108().method_32101(58, 20).method_32097(7F, 14F, 10F, 2F, 1F, 1F), class_5603.field_27701)
        lv2.method_32117("top14", class_5606.method_32108().method_32101(0, 43).method_32097(13F, 14F, 1F, 2F, 1F, 14F), class_5603.field_27701)
        lv2.method_32117("top15", class_5606.method_32108().method_32101(16, 42).method_32097(10F, 14F, 1F, 1F, 1F, 14F), class_5603.field_27701)
        lv2.method_32117("top16", class_5606.method_32108().method_32101(0, 0).method_32097(9F, 14F, 1F, 1F, 1F, 6F), class_5603.field_27701)
        lv2.method_32117("top17", class_5606.method_32108().method_32101(0, 24).method_32097(6F, 14F, 1F, 1F, 1F, 6F), class_5603.field_27701)
        lv2.method_32117("top18", class_5606.method_32108().method_32101(0, 31).method_32097(6F, 14F, 9F, 1F, 1F, 6F), class_5603.field_27701)
        lv2.method_32117("top19", class_5606.method_32108().method_32101(0, 7).method_32097(9F, 14F, 9F, 1F, 1F, 6F), class_5603.field_27701)
        lv2.method_32117("top20", class_5606.method_32108().method_32101(16, 42).method_32097(5F, 14F, 1F, 1F, 1F, 14F), class_5603.field_27701)
        return class_5607.method_32110(lv, 64, 64)
    }

    private fun setupRuneModel(runeId: Int, runeColor: DyeColor): class_5607 {
        val lv = class_5609()
        val lv2 = lv.method_32111()

        val runeTextureV = if(runeColor.id >= 8) (runeColor.id-8)*4 else runeColor.id*4
        val runeTextureU = if(runeColor.id >= 8) 8 else 0

        when(runeId) {
            1 -> lv2.method_32117("rune1_${runeColor.getName()}", class_5606.method_32108().method_32101(runeTextureU, runeTextureV).method_32097(11f, 13f, 11f, 2f, 2f, 2f), class_5603.field_27701)
            2 -> lv2.method_32117("rune2_${runeColor.getName()}", class_5606.method_32108().method_32101(runeTextureU, runeTextureV).method_32097(7f, 13f, 11f, 2f, 2f, 2f), class_5603.field_27701)
            3 -> lv2.method_32117("rune3_${runeColor.getName()}", class_5606.method_32108().method_32101(runeTextureU, runeTextureV).method_32097(3f, 13f, 11f, 2f, 2f, 2f), class_5603.field_27701)
            4 -> lv2.method_32117("rune4_${runeColor.getName()}", class_5606.method_32108().method_32101(runeTextureU, runeTextureV).method_32097(3f, 13f, 7f, 2f, 2f, 2f), class_5603.field_27701)
            5 -> lv2.method_32117("rune5_${runeColor.getName()}", class_5606.method_32108().method_32101(runeTextureU, runeTextureV).method_32097(3f, 13f, 3f, 2f, 2f, 2f), class_5603.field_27701)
            6 -> lv2.method_32117("rune6_${runeColor.getName()}", class_5606.method_32108().method_32101(runeTextureU, runeTextureV).method_32097(7f, 13f, 3f, 2f, 2f, 2f), class_5603.field_27701)
            7 -> lv2.method_32117("rune7_${runeColor.getName()}", class_5606.method_32108().method_32101(runeTextureU, runeTextureV).method_32097(11f, 13f, 3f, 2f, 2f, 2f), class_5603.field_27701)
            8 -> lv2.method_32117("rune8_${runeColor.getName()}", class_5606.method_32108().method_32101(runeTextureU, runeTextureV).method_32097(11f, 13f, 7f, 2f, 2f, 2f), class_5603.field_27701)
        }
        return class_5607.method_32110(lv, 32, 32)
    }

    private fun setupCoreModel(diamond: Boolean): class_5607 {
        val lv = class_5609()
        val lv2 = lv.method_32111()
        val upUv = if(diamond) -10 else 0
        lv2.method_32117("core1", class_5606.method_32108().method_32101(58, 50 + upUv).method_32097(9f, 14f, 7f, 1f, 2f, 2f), class_5603.field_27701)
        lv2.method_32117("core2", class_5606.method_32108().method_32101(52, 44 + upUv).method_32097(7f, 14f, 6f, 2f, 2f, 4f), class_5603.field_27701)
        lv2.method_32117("core3", class_5606.method_32108().method_32101(52, 50 + upUv).method_32097(6f, 14f, 7f, 1f, 2f, 2f), class_5603.field_27701)
        return class_5607.method_32110(lv, 64, 64)
    }
        
    
}