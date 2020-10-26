package io.github.lucaargolo.kibe.utils

import me.sargunvohra.mcmods.autoconfig1u.ConfigData
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry
import me.sargunvohra.mcmods.autoconfig1u.serializer.PartitioningSerializer.GlobalData
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment

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