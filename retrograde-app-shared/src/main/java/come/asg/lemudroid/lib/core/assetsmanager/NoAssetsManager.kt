package come.asg.lemudroid.lib.core.assetsmanager

import android.content.SharedPreferences
import come.asg.lemudroid.lib.core.CoreUpdater
import come.asg.lemudroid.lib.library.CoreID
import come.asg.lemudroid.lib.storage.DirectoriesManager

class NoAssetsManager : CoreID.AssetsManager {
    override suspend fun clearAssets(directoriesManager: DirectoriesManager) {}

    override suspend fun retrieveAssetsIfNeeded(
        coreUpdaterApi: CoreUpdater.CoreManagerApi,
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences,
    ) {
    }
}
