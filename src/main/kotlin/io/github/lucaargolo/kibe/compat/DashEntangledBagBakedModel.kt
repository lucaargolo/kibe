package io.github.lucaargolo.kibe.compat

import io.github.lucaargolo.kibe.items.entangledbag.EntangledBagBakedModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashConstructor
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.api.enums.ConstructorMode
import net.oskarstrom.dashloader.model.DashModel

@DashObject(EntangledBagBakedModel::class)
class DashEntangledBagBakedModel @DashConstructor(ConstructorMode.EMPTY) constructor() : DashModel {

    override fun toUndash(registry: DashRegistry): BakedModel = EntangledBagBakedModel()

    override fun getStage(): Int = 3

}