package io.github.lucaargolo.kibe.blocks.chunkloader

import io.github.lucaargolo.kibe.CHUNK_MAP_CLICK
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.MaterialColor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos

class ChunkLoaderScreen(be: ChunkLoaderBlockEntity): Screen(TranslatableText("screen.kibe.chunk_loader")) {

    private val chunkPos = ChunkPos(be.pos)
    val world = be.world!!
    val entity = be

    var identifier: Identifier? = null

    private fun createImage() {
        val image = NativeImage(NativeImage.Format.ABGR, 256, 256, false)
        (chunkPos.startX-32..chunkPos.endX+32).forEach {  x ->
            (chunkPos.startZ-32..chunkPos.endZ+32).forEach { z ->
                var color = MaterialColor.WHITE
                var y = 256
                while(y >= 0) {
                    val innerPos = BlockPos(x, y, z)
                    val state = world.getBlockState(innerPos)
                    if(!state.isAir) {
                        //println("block: ${state.block.name} x: $x z: $z")
                        color = state.getTopMaterialColor(world, innerPos)
                        break
                    }
                    else y--
                }
                val red: Int = color.color shr 16 and 0xFF
                val green: Int = color.color shr 8 and 0xFF
                val blue: Int = color.color shr 0 and 0xFF
                val out = (255 shl 24) or (blue shl 16) or (green shl 8) or (red shl 0)
                image.setPixelColor(x-chunkPos.startX+32, z-chunkPos.startZ+32, out)
            }
        }

        val mc = MinecraftClient.getInstance()
        val texture = NativeImageBackedTexture(image)
        identifier = mc.textureManager.registerDynamicTexture("chunk_loader_minimap", texture)
    }

    override fun isPauseScreen() = false

    private val backgroundHeight = 102
    private val backgroundWidth = 94

    var x = 0
    var y = 0

    override fun onClose() {
        super.onClose()
        identifier?.let{ client?.textureManager?.destroyTexture(it) }
    }

    override fun init() {
        super.init()
        x = (width-backgroundWidth)/2
        y = (height-backgroundHeight)/2
    }

    private val texture = Identifier("kibe:textures/gui/chunk_loader.png")

    @Suppress("UNUSED_PARAMETER")
    private fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        textRenderer.draw(matrices, title, (x+47 - textRenderer.getWidth(title) / 2f), y+6f, 4210752)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        drawBackground(matrices, delta, mouseX, mouseY)
        if(identifier == null) createImage()
        identifier?.let {
            val mc = MinecraftClient.getInstance()
            mc.textureManager.bindTexture(it)
            drawTexture(matrices, x+7, y+15, 0, 0, 80, 80)
        }
        entity.enabledChunks.forEach {
            DrawableHelper.fill(matrices, x+7+((it.first+2)*16), y+15+((it.second+2)*16), x+7+((it.first+2)*16)+16, y+15+((it.second+2)*16)+16, -2147418368)
        }
        if(mouseX in (x+7 until x+87) && mouseY in (y+15 until y+95)) {
            val chunkX = (mouseX-(x+7))/16
            val chunkZ = (mouseY-(y+15))/16
            DrawableHelper.fill(matrices, x+7+(chunkX*16), y+15+(chunkZ*16), x+7+(chunkX*16)+16, y+15+(chunkZ*16)+16, -2130706433)
            val tooltip = mutableListOf<Text>()
            tooltip.add(TranslatableText("tooltip.kibe.chunk_at").append(LiteralText("${chunkPos.x+chunkX-2}, ${chunkPos.z+chunkZ-2}")))
            tooltip.add(TranslatableText("tooltip.kibe.forced").append(TranslatableText(if(entity.enabledChunks.contains(Pair(chunkX-2, chunkZ-2))) "tooltip.kibe.enabled" else "tooltip.kibe.disabled")))
            renderTooltip(matrices, tooltip, mouseX, mouseY)
        }
        super.render(matrices, mouseX, mouseY, delta)
        drawForeground(matrices, mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if(mouseX.toInt() in (x+7 until x+87) && mouseY.toInt() in (y+15 until y+95)) {
            val x = ((mouseX.toInt()-(x+7))/16) - 2
            val z = ((mouseY.toInt()-(y+15))/16) - 2
            val passedData = PacketByteBuf(Unpooled.buffer())
            passedData.writeInt(x)
            passedData.writeInt(z)
            passedData.writeBlockPos(entity.pos)
            ClientPlayNetworking.send(CHUNK_MAP_CLICK, passedData)
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

}