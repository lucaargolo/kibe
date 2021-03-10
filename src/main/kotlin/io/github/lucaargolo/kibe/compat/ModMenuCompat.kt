package io.github.lucaargolo.kibe.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import io.github.lucaargolo.kibe.utils.ModConfig

import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.gui.screen.Screen

class ModMenuCompat : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { screen: Screen? -> AutoConfig.getConfigScreen(ModConfig::class.java, screen).get() }
    }

}