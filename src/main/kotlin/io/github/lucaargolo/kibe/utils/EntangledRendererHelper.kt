package io.github.lucaargolo.kibe.utils

import net.minecraft.client.model.ModelTransform
import net.minecraft.client.model.ModelPartBuilder
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.model.ModelData
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

    fun getEntries(): LinkedHashMap<EntityModelLayer, TexturedModelData> {
        val map = linkedMapOf<EntityModelLayer, TexturedModelData>()
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



    private fun setupBottomModel(): TexturedModelData {
        val lv = ModelData()
        val lv2 = lv.root
        val ySize = if(parent == "entangled_chest") 10.0f else 1.0f
        lv2.addChild("bottom", ModelPartBuilder.create().uv(0, 0).cuboid(1.0f, 0.0f, 1.0f, 14.0f, ySize, 14.0f), ModelTransform.NONE)
        return TexturedModelData.of(lv, 64, 64)
    }

    private fun setupTopModel(): TexturedModelData {
        val lv = ModelData()
        val lv2 = lv.root
        lv2.addChild("top1", ModelPartBuilder.create().uv(32, 43).cuboid(1F, 14F, 1F, 2F, 1F, 14F), ModelTransform.NONE)
        lv2.addChild("top2", ModelPartBuilder.create().uv(56, 0).cuboid(3F, 14F, 1F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top3", ModelPartBuilder.create().uv(56, 3).cuboid(7F, 14F, 1F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top4", ModelPartBuilder.create().uv(56, 6).cuboid(11F, 14F, 1F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top5", ModelPartBuilder.create().uv(56, 31).cuboid(11F, 14F, 13F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top6", ModelPartBuilder.create().uv(56, 28).cuboid(7F, 14F, 13F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top7", ModelPartBuilder.create().uv(56, 25).cuboid(3F, 14F, 13F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top8", ModelPartBuilder.create().uv(56, 17).cuboid(3F, 14F, 9F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top9", ModelPartBuilder.create().uv(56, 9).cuboid(3F, 14F, 5F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top10", ModelPartBuilder.create().uv(56, 14).cuboid(11F, 14F, 5F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top11", ModelPartBuilder.create().uv(58, 12).cuboid(7F, 14F, 5F, 2F, 1F, 1F), ModelTransform.NONE)
        lv2.addChild("top12", ModelPartBuilder.create().uv(56, 22).cuboid(11F, 14F, 9F, 2F, 1F, 2F), ModelTransform.NONE)
        lv2.addChild("top13", ModelPartBuilder.create().uv(58, 20).cuboid(7F, 14F, 10F, 2F, 1F, 1F), ModelTransform.NONE)
        lv2.addChild("top14", ModelPartBuilder.create().uv(0, 43).cuboid(13F, 14F, 1F, 2F, 1F, 14F), ModelTransform.NONE)
        lv2.addChild("top15", ModelPartBuilder.create().uv(16, 42).cuboid(10F, 14F, 1F, 1F, 1F, 14F), ModelTransform.NONE)
        lv2.addChild("top16", ModelPartBuilder.create().uv(0, 0).cuboid(9F, 14F, 1F, 1F, 1F, 6F), ModelTransform.NONE)
        lv2.addChild("top17", ModelPartBuilder.create().uv(0, 24).cuboid(6F, 14F, 1F, 1F, 1F, 6F), ModelTransform.NONE)
        lv2.addChild("top18", ModelPartBuilder.create().uv(0, 31).cuboid(6F, 14F, 9F, 1F, 1F, 6F), ModelTransform.NONE)
        lv2.addChild("top19", ModelPartBuilder.create().uv(0, 7).cuboid(9F, 14F, 9F, 1F, 1F, 6F), ModelTransform.NONE)
        lv2.addChild("top20", ModelPartBuilder.create().uv(16, 42).cuboid(5F, 14F, 1F, 1F, 1F, 14F), ModelTransform.NONE)
        return TexturedModelData.of(lv, 64, 64)
    }

    private fun setupRuneModel(runeId: Int, runeColor: DyeColor): TexturedModelData {
        val lv = ModelData()
        val lv2 = lv.root

        val runeTextureV = if(runeColor.id >= 8) (runeColor.id-8)*4 else runeColor.id*4
        val runeTextureU = if(runeColor.id >= 8) 8 else 0

        when(runeId) {
            1 -> lv2.addChild("rune1_${runeColor.getName()}", ModelPartBuilder.create().uv(runeTextureU, runeTextureV).cuboid(11f, 13f, 11f, 2f, 2f, 2f), ModelTransform.NONE)
            2 -> lv2.addChild("rune2_${runeColor.getName()}", ModelPartBuilder.create().uv(runeTextureU, runeTextureV).cuboid(7f, 13f, 11f, 2f, 2f, 2f), ModelTransform.NONE)
            3 -> lv2.addChild("rune3_${runeColor.getName()}", ModelPartBuilder.create().uv(runeTextureU, runeTextureV).cuboid(3f, 13f, 11f, 2f, 2f, 2f), ModelTransform.NONE)
            4 -> lv2.addChild("rune4_${runeColor.getName()}", ModelPartBuilder.create().uv(runeTextureU, runeTextureV).cuboid(3f, 13f, 7f, 2f, 2f, 2f), ModelTransform.NONE)
            5 -> lv2.addChild("rune5_${runeColor.getName()}", ModelPartBuilder.create().uv(runeTextureU, runeTextureV).cuboid(3f, 13f, 3f, 2f, 2f, 2f), ModelTransform.NONE)
            6 -> lv2.addChild("rune6_${runeColor.getName()}", ModelPartBuilder.create().uv(runeTextureU, runeTextureV).cuboid(7f, 13f, 3f, 2f, 2f, 2f), ModelTransform.NONE)
            7 -> lv2.addChild("rune7_${runeColor.getName()}", ModelPartBuilder.create().uv(runeTextureU, runeTextureV).cuboid(11f, 13f, 3f, 2f, 2f, 2f), ModelTransform.NONE)
            8 -> lv2.addChild("rune8_${runeColor.getName()}", ModelPartBuilder.create().uv(runeTextureU, runeTextureV).cuboid(11f, 13f, 7f, 2f, 2f, 2f), ModelTransform.NONE)
        }
        return TexturedModelData.of(lv, 32, 32)
    }

    private fun setupCoreModel(diamond: Boolean): TexturedModelData {
        val lv = ModelData()
        val lv2 = lv.root
        val upUv = if(diamond) -10 else 0
        lv2.addChild("core1", ModelPartBuilder.create().uv(58, 50 + upUv).cuboid(9f, 14f, 7f, 1f, 2f, 2f), ModelTransform.NONE)
        lv2.addChild("core2", ModelPartBuilder.create().uv(52, 44 + upUv).cuboid(7f, 14f, 6f, 2f, 2f, 4f), ModelTransform.NONE)
        lv2.addChild("core3", ModelPartBuilder.create().uv(52, 50 + upUv).cuboid(6f, 14f, 7f, 1f, 2f, 2f), ModelTransform.NONE)
        return TexturedModelData.of(lv, 64, 64)
    }
        
    
}