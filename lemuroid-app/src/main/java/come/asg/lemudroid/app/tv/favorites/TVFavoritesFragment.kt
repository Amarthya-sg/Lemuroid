package come.asg.lemudroid.app.tv.favorites

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import come.asg.lemudroid.R
import come.asg.lemudroid.app.shared.GameInteractor
import come.asg.lemudroid.app.tv.shared.GamePresenter
import come.asg.lemudroid.common.coroutines.launchOnState
import come.asg.lemudroid.lib.library.db.RetrogradeDatabase
import come.asg.lemudroid.lib.library.db.entity.Game
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TVFavoritesFragment : VerticalGridSupportFragment() {
    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    init {
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = 4
        setGridPresenter(gridPresenter)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val factory = TVFavoritesViewModel.Factory(retrogradeDb)
        val favoritesViewModel = ViewModelProvider(this, factory)[TVFavoritesViewModel::class.java]

        val cardSize = resources.getDimensionPixelSize(come.asg.lemudroid.lib.R.dimen.card_size)
        val pagingAdapter =
            PagingDataAdapter(
                GamePresenter(cardSize, gameInteractor),
                Game.DIFF_CALLBACK,
            )

        this.adapter = pagingAdapter

        launchOnState(Lifecycle.State.RESUMED) {
            favoritesViewModel.favorites
                .collect { pagingAdapter.submitData(lifecycle, it) }
        }

        onItemViewClickedListener =
            OnItemViewClickedListener { _, item, _, _ ->
                when (item) {
                    is Game -> gameInteractor.onGamePlay(item)
                }
            }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    @dagger.Module
    class Module
}
