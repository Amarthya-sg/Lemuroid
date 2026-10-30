package come.asg.lemudroid.lib.saves.migrators

import come.asg.lemudroid.lib.library.CoreID
import come.asg.lemudroid.lib.library.SystemCoreConfig
import come.asg.lemudroid.lib.library.db.entity.Game
import come.asg.lemudroid.lib.storage.DirectoriesManager

interface SavesMigrator {
    fun loadPreviousSaveForGame(
        game: Game,
        directoriesManager: DirectoriesManager,
    ): ByteArray?
}

fun SystemCoreConfig.getSavesMigrator(): SavesMigrator? {
    return when (this.coreID) {
        CoreID.MELONDS -> MelonDsSavesMigrator
        else -> null
    }
}
