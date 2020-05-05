package io.github.lucaargolo.kibe.entangled

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class EntangledChestScreen(container: EntangledChestContainer?, inventory: PlayerInventory?, title: Text?): AbstractInventoryScreen<EntangledChestContainer>(container, inventory, title) {

    override fun drawBackground(delta: Float, mouseX: Int, mouseY: Int) {

    }

}