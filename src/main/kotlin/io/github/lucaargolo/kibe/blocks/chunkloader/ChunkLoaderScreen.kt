package io.github.lucaargolo.kibe.blocks.chunkloader

import io.github.lucaargolo.kibe.CHUNK_MAP_CLICK
import io.github.lucaargolo.kibe.CHUNK_PLAYER_CHECK
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.MapColor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.network.PacketByteBuf

import net.minecraft.text.Text

import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos

class ChunkLoaderScreen(be: ChunkLoaderBlockEntity): Screen(Text.translatable("screen.kibe.chunk_loader")) {

    private val chunkPos = ChunkPos(be.pos)
    val world = be.world!!
    val entity = be

    var identifier: Identifier? = null
    var notOwner = false

    private fun createImage() {
        val image = NativeImage(NativeImage.Format.RGBA, 256, 256, false)
        (chunkPos.startX-32..chunkPos.endX+32).forEach {  x ->
            (chunkPos.startZ-32..chunkPos.endZ+32).forEach { z ->
                var color = MapColor.WHITE
                var y = 256
                while(y >= 0) {
                    val innerPos = BlockPos(x, y, z)
                    val state = world.getBlockState(innerPos)
                    if(!state.isAir) {
                        //println("block: ${state.block.name} x: $x z: $z")
                        color = state.getMapColor(world, innerPos)
                        break
                    }
                    else y--
                }
                val red: Int = color.color shr 16 and 0xFF
                val green: Int = color.color shr 8 and 0xFF
                val blue: Int = color.color shr 0 and 0xFF
                val out = (255 shl 24) or (blue shl 16) or (green shl 8) or (red shl 0)
                image.setColor(x-chunkPos.startX+32, z-chunkPos.startZ+32, out)
            }
        }

        val mc = MinecraftClient.getInstance()
        val texture = NativeImageBackedTexture(image)
        identifier = mc.textureManager.registerDynamicTexture("chunk_loader_minimap", texture)
    }

    override fun shouldPause() = false

    private val backgroundHeight = 116
    private val backgroundWidth = 94

    var x = 0
    var y = 0

    override fun close() {
        super.close()
        identifier?.let{ client?.textureManager?.destroyTexture(it) }
    }

    override fun init() {
        super.init()
        x = (width-backgroundWidth)/2
        y = (height-backgroundHeight)/2
    }

    private val texture = Identifier("kibe:textures/gui/chunk_loader.png")

    @Suppress("UNUSED_PARAMETER")
    private fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.drawText(textRenderer, title, x+47 - textRenderer.getWidth(title) / 2, y+6, 4210752, false)
        val toggle = Text.translatable("tooltip.kibe.check_for_owner")
        context.matrices.push()
        context.matrices.scale(0.5f, 0.5f, 0.5f)
        context.drawText(textRenderer, toggle, (x+16)*2f, (y+20f)*2f, 4210752)
        context.matrices.pop()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(texture, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        drawBackground(context, delta, mouseX, mouseY)
        if(identifier == null) createImage()
        identifier?.let {
            context.drawTexture(it, x+7, y+28, 0, 0, 80, 80)
        }
        entity.enabledChunks.forEach {
            context.fill(x+7+((it.first+2)*16), y+28+((it.second+2)*16), x+7+((it.first+2)*16)+16, y+28+((it.second+2)*16)+16, -2147418368)
        }
        if(entity.checkForOwner) {
            RenderSystem.setShaderTexture(0, texture)
            drawTexture(matrices, x+7, y+18, 94, 0, 7, 7)
        }
        if(mouseX in (x+7 until x+87) && mouseY in (y+28 until y+109)) {
            val chunkX = (mouseX-(x+7))/16
            val chunkZ = (mouseY-(y+28))/16
            context.fill(x+7+(chunkX*16), y+28+(chunkZ*16), x+7+(chunkX*16)+16, y+28+(chunkZ*16)+16, -2130706433)
            val tooltip = mutableListOf<Text>()
            tooltip.add(Text.translatable("tooltip.kibe.chunk_at").append(Text.literal("${chunkPos.x+chunkX-2}, ${chunkPos.z+chunkZ-2}")))
            tooltip.add(Text.translatable("tooltip.kibe.forced").append(Text.translatable(if(entity.enabledChunks.contains(Pair(chunkX-2, chunkZ-2))) "tooltip.kibe.enabled" else "tooltip.kibe.disabled")))
            context.drawTooltip(textRenderer, tooltip, mouseX, mouseY)
        }else if(mouseX in (x+7 until x+14) && mouseY in (y+18 until y+25)) {
            DrawableHelper.fill(matrices, x+7, y+18, x+14, y+25, -2130706433)
            val tooltip = mutableListOf<Text>()
            tooltip.add(Text.translatable("tooltip.kibe.check_for_owner_status", if (entity.checkForOwner) Text.translatable("tooltip.kibe.enabled") else Text.translatable("tooltip.kibe.disabled")))
            if(notOwner) {
                tooltip.add(Text.translatable("tooltip.kibe.lore.not_owner"))
            }else {
                tooltip.add(Text.translatable("tooltip.kibe.lore.check_for_owner"))
            }
            renderTooltip(matrices, tooltip, mouseX, mouseY)
        }else{
            notOwner = false
        }
        super.render(context, mouseX, mouseY, delta)
        drawForeground(context, mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if(mouseX.toInt() in (x+7 until x+87) && mouseY.toInt() in (y+28 until y+109)) {
            val x = ((mouseX.toInt()-(x+7))/16) - 2
            val z = ((mouseY.toInt()-(y+28))/16) - 2
            val passedData = PacketByteBuf(Unpooled.buffer())
            passedData.writeInt(x)
            passedData.writeInt(z)
            passedData.writeBlockPos(entity.pos)
            ClientPlayNetworking.send(CHUNK_MAP_CLICK, passedData)
            return true
        }
        if(mouseX.toInt() in (x+7 until x+14) && mouseY.toInt() in (y+18 until y+25)) {
            val passedData = PacketByteBuf(Unpooled.buffer())
            passedData.writeBlockPos(entity.pos)
            if(client?.player?.uuidAsString != entity.ownerUUID) {
                notOwner = true
            }else{
                ClientPlayNetworking.send(CHUNK_PLAYER_CHECK, passedData)
            }
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

}