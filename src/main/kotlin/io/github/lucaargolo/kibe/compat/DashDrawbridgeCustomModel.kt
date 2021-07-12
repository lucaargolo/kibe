package io.github.lucaargolo.kibe.compat

import io.activej.serializer.annotations.Deserialize
import io.activej.serializer.annotations.Serialize
import io.github.lucaargolo.kibe.blocks.drawbridge.DrawbridgeCustomModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.model.DashModel

@DashObject(DrawbridgeCustomModel::class)
class DashDrawbridgeCustomModel : DashModel{

    var modelList: IntArray @Serialize(order = 0) get
    var spriteList: IntArray @Serialize(order = 1) get

    constructor(drawbridgeCustomModel: DrawbridgeCustomModel, registry: DashRegistry) {
        this.modelList = drawbridgeCustomModel.modelList.map(registry::createModelPointer).toIntArray()
        this.spriteList = drawbridgeCustomModel.spriteList.map(registry::createSpritePointer).toIntArray()
    }

    constructor(@Deserialize("modelList") modelList: IntArray, @Deserialize("spriteList") spriteList: IntArray) {
        this.modelList = modelList
        this.spriteList = spriteList
    }

    override fun toUndash(registry: DashRegistry): BakedModel {
        val drawbridgeCustomModel = DrawbridgeCustomModel()
        drawbridgeCustomModel.modelTransformation = registry.getModel(modelList[0]).transformation
        this.modelList.forEach { drawbridgeCustomModel.modelList.add(registry.getModel(it)) }
        this.spriteList.forEach { drawbridgeCustomModel.spriteList.add(registry.getSprite(it)) }
        return drawbridgeCustomModel
    }

    override fun getStage(): Int = 3
}