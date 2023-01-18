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
        //The xp drain speed multiplier
        var xpDrainSpeedMultiplier = 1.0
        //The xp drain speed multiplier
        var xpShowerSpeedMultiplier = 1.0
        //The range of the magnet
        var magnetRange = 8.0
        //If glider has any durability
        var gliderUnbreakable: Boolean = true
        //Glider durability in ticks (36000 = 30 minutes)
        var gliderDurability: Int = 36000
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