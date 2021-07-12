package io.github.lucaargolo.kibe.compat

import io.github.lucaargolo.kibe.items.entangledbucket.EntangledBucketBakedModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashConstructor
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.api.enums.ConstructorMode
import net.oskarstrom.dashloader.model.DashModel

@DashObject(EntangledBucketBakedModel::class)
class DashEntangledBucketBakedModel @DashConstructor(ConstructorMode.EMPTY) constructor() : DashModel {

    override fun toUndash(registry: DashRegistry): BakedModel = EntangledBucketBakedModel()

    override fun getStage(): Int = 3

}