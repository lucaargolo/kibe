package io.github.lucaargolo.kibe.blocks.vacuum

import io.github.lucaargolo.kibe.blocks.entangled.EntangledChestContainer
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class VacuumHopperScreen(container: VacuumHopperContainer, inventory: PlayerInventory, title: Text): AbstractInventoryScreen<VacuumHopperContainer>(container, inventory, title) {

    override fun drawBackground(delta: Float, mouseX: Int, mouseY: Int) {

    }

}
