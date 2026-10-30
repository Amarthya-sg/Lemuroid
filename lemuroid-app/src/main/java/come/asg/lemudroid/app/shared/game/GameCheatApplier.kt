package come.asg.lemudroid.app.shared.game

import come.asg.lemudroid.lib.library.db.entity.GameCheat
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.LibretroDroid
import timber.log.Timber

object GameCheatApplier {
    fun apply(
        retroView: GLRetroView?,
        cheats: List<GameCheat>,
        resetEvenIfEmpty: Boolean = false,
    ) {
        if (retroView == null || (cheats.isEmpty() && !resetEvenIfEmpty)) return

        val orderedCheats = cheats.sortedBy(GameCheat::id)
        retroView.queueEvent {
            runCatching { LibretroDroid.resetCheat() }
                .onFailure { Timber.w(it, "Unable to reset cheats for the active core") }
            orderedCheats.forEachIndexed { index, cheat ->
                runCatching {
                    retroView.setCheat(index, cheat.enabled, cheat.code)
                }
                    .onFailure { error ->
                        Timber.w(error, "Active core rejected cheat ${cheat.id}; continuing with remaining cheats")
                    }
            }
        }
    }
}
