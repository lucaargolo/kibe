package io.github.lucaargolo.kibe.compat

import io.github.lucaargolo.kibe.item.CoolerBlockItem
import top.theillusivec4.curios.api.CuriosApi
import top.theillusivec4.curios.api.type.capability.ICurioItem

class CurioCoolerBlockItem(settings: Settings) : CoolerBlockItem(settings), ICurioItem {

    init {
        CuriosApi.registerCurio(this, this)
    }

}

