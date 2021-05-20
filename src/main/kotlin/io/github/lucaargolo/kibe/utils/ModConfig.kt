package io.github.lucaargolo.kibe.utils

class ModConfig {

    var miscellaneousModule: MiscellaneousModule = MiscellaneousModule()
    var chunkLoaderModule: ChunkLoaderModule = ChunkLoaderModule()

    class MiscellaneousModule {
        //Should fluid changing inside tanks update the block's luminance.
        var tanksChangeLights: Boolean = true
        //How many mobs can the Cursed Dirt spawn in a chunk.
        var cursedDirtMobCap: Int = 25
        //What mobs aren't allowed to spawn in cursed dirt
        var cursedDirtBlacklist: ArrayList<String> = arrayListOf()
    }

    class ChunkLoaderModule {
        //If enabled chunk loaders will only work if it's owner is on the server
        var checkForPlayer: Boolean = false
        //Limits how many active chunk loaders players have. (Set to -1 for infinite)
        var maxPerPlayer: Int = -1
        //How many seconds before the chunk loader disables itself after it's owner left the server. (Set to -1 to disable)
        var maxOfflineTime: Long = -1
    }

}