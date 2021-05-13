package io.github.lucaargolo.kibe.utils

class ModConfig {

    var miscellaneousModule: MiscellaneousModule = MiscellaneousModule()
    var chunkLoaderModule: ChunkLoaderModule = ChunkLoaderModule()

    class MiscellaneousModule {
        var tanksChangeLights: Boolean = true
        var cursedDirtMobCap: Int = 25
    }

    class ChunkLoaderModule {
        var checkForPlayer: Boolean = false
        var maxPerPlayer: Int = -1
        var maxOfflineTime: Long = -1
    }

}