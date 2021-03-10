package io.github.lucaargolo.kibe.utils

import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry
import me.shedaniel.autoconfig.serializer.PartitioningSerializer.GlobalData
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment

@Config(name = "kibe")
class ModConfig: GlobalData() {

    @ConfigEntry.Category("miscellaneous")
    @ConfigEntry.Gui.TransitiveObject()
    var miscellaneousModule: MiscellaneousModule = MiscellaneousModule()
    @ConfigEntry.Category("chunk_loader")
    @ConfigEntry.Gui.TransitiveObject()
    var chunkLoaderModule: ChunkLoaderModule = ChunkLoaderModule()

    @Config(name = "miscellaneous")
    class MiscellaneousModule: ConfigData {
        @Comment("Should fluid changing inside tanks update the block's luminance.")
        var tanksChangeLights: Boolean = true
        @Comment("How many mobs can the Cursed Dirt spawn in a chunk.")
        var cursedDirtMobCap: Int = 25
    }

    @Config(name = "chunk_loader")
    class ChunkLoaderModule: ConfigData {
        @Comment("If enabled chunk loaders will only work if it's owner is on the server")
        var checkForPlayer: Boolean = false
        @Comment("Limits how many active chunk loaders players have. (Set to -1 for infinite)")
        var maxPerPlayer: Int = -1
        @Comment("How many seconds before the chunk loader disables itself after it's owner left the server. (Set to -1 to disable)")
        var maxOfflineTime: Long = -1
    }

}