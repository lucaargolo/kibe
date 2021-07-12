package io.github.lucaargolo.kibe.compat

import io.activej.serializer.annotations.Deserialize
import io.activej.serializer.annotations.Serialize
import io.github.lucaargolo.kibe.blocks.tank.TankCustomModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.model.DashModel

@DashObject(TankCustomModel::class)
class DashTankCustomModel : DashModel {

    var spriteList: IntArray @Serialize(order = 0) get

    constructor(tankCustomModel: TankCustomModel, registry: DashRegistry) {
        this.spriteList = tankCustomModel.spriteList.map(registry::createSpritePointer).toIntArray()
    }

    constructor(@Deserialize("spriteList") spriteList: IntArray) {
        this.spriteList = spriteList
    }

    override fun toUndash(registry: DashRegistry): BakedModel {
        val tankCustomModel = TankCustomModel()
        this.spriteList.forEach { tankCustomModel.spriteList.add(registry.getSprite(it)) }
        return tankCustomModel
    }

    override fun getStage(): Int = 3
}