package come.asg.lemudroid.app.tv.game

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import come.asg.lemudroid.R
import come.asg.lemudroid.app.shared.game.BaseGameActivity
import come.asg.lemudroid.app.shared.game.BaseGameScreenViewModel
import come.asg.lemudroid.app.tv.gamemenu.TVGameMenuActivity
import come.asg.lemudroid.common.coroutines.launchOnState
import come.asg.lemudroid.common.coroutines.safeCollect
import come.asg.lemudroid.common.displayToast
import kotlinx.coroutines.flow.filter

class TVGameActivity : BaseGameActivity() {
    override fun getDialogClass() = TVGameMenuActivity::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeFlows()
    }

    @Composable
    override fun GameScreen(viewModel: BaseGameScreenViewModel) {
        TVGameScreen(viewModel)
    }

    private fun initializeFlows() {
        launchOnState(Lifecycle.State.CREATED) {
            initializeShortcutToastFlow()
        }
    }

    private suspend fun initializeShortcutToastFlow() {
        inputDeviceManager
            .getEnabledInputsObservable()
            .filter { it.isEmpty() }
            .safeCollect {
                displayToast(R.string.tv_game_message_missing_gamepad)
            }
    }
}
