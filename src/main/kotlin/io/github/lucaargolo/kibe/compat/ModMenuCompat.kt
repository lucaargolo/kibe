package io.github.lucaargolo.kibe.compat

import io.github.lucaargolo.kibe.utils.ModConfig
import io.github.prospector.modmenu.api.ConfigScreenFactory
import io.github.prospector.modmenu.api.ModMenuApi
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import net.minecraft.client.gui.screen.Screen

class ModMenuCompat : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { screen: Screen? -> AutoConfig.getConfigScreen(ModConfig::class.java, screen).get() }
    }

}