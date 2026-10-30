package come.asg.lemudroid.app.mobile.feature.game

import androidx.compose.runtime.Composable
import come.asg.lemudroid.app.mobile.feature.gamemenu.GameMenuActivity
import come.asg.lemudroid.app.shared.game.BaseGameActivity
import come.asg.lemudroid.app.shared.game.BaseGameScreenViewModel

class GameActivity : BaseGameActivity() {
    @Composable
    override fun GameScreen(viewModel: BaseGameScreenViewModel) {
        MobileGameScreen(viewModel)
    }

    override fun getDialogClass() = GameMenuActivity::class.java
}
