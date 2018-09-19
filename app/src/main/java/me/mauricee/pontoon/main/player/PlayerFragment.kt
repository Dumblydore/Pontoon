package me.mauricee.pontoon.main.player

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isVisible
import androidx.transition.TransitionInflater
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.SeekBarStartChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarStopChangeEvent
import com.jakewharton.rxbinding2.widget.changeEvents
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.layout_player_controls.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.main.Player

class PlayerFragment : BaseFragment<PlayerPresenter>(),
        PlayerContract.View, Player.ControlView {

    private val playIcon by lazy { getDrawable(requireContext(), R.drawable.ic_play) }
    private val pauseIcon by lazy { getDrawable(requireContext(), R.drawable.ic_pause) }
    private val previewArt by lazy { arguments!!.getString(PreviewArtKey) }
    private val qualityMenu by lazy { player_controls_toolbar.menu.findItem(R.id.action_quality) }
    private var isSeeking: Boolean = false

    override val actions: Observable<PlayerContract.Action>
        get() = listOf(player_controls_fullscreen.clicks().map { PlayerContract.Action.ToggleFullscreen },
                player_controls_playPause.clicks().map { PlayerContract.Action.PlayPause },
                player_controls_progress.changeEvents()
                        .doOnNext { isSeeking = it is SeekBarStartChangeEvent || (isSeeking && it !is SeekBarStopChangeEvent) }
                        .flatMap {
                            when (it) {
                                is SeekBarStopChangeEvent -> PlayerContract.Action.SeekProgress(it.view().progress).toObservable()
                                else -> Observable.empty()
                            }
                        }, RxToolbar.navigationClicks(player_controls_toolbar).map
        { PlayerContract.Action.MinimizePlayer }, itemClicks()
        ).let { Observable.merge(it) }

    override fun getLayoutId(): Int = R.layout.fragment_player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlideApp.with(this).load(previewArt).placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(player_preview)
        player_controls_toolbar.inflateMenu(R.menu.player_toolbar)

    }

    override fun updateState(state: PlayerContract.State) {
        when (state) {
            is PlayerContract.State.Bind -> {
                state.player.bindToView(player_display)
                state.player.controller = this
            }
            is PlayerContract.State.Playing -> {
                player_controls_playPause.setImageDrawable(pauseIcon)
                player_controls_playPause.isVisible = true
                player_controls_loading.isVisible = false
                player_display.isVisible = true
            }
            is PlayerContract.State.Paused -> {
                player_controls_playPause.setImageDrawable(playIcon)
                player_display.isVisible = true
                player_controls_loading.isVisible = false
                player_controls_playPause.isVisible = true
            }
            is PlayerContract.State.Loading -> {
                player_display.isVisible = false
                player_controls_loading.isVisible = true
                player_controls_playPause.isVisible = false
                player_controls_progress.secondaryProgress = 0
            }
            is PlayerContract.State.Buffering -> {
                player_controls_loading.isVisible = true
                player_controls_playPause.isVisible = false
            }
            is PlayerContract.State.Duration -> {
                player_controls_duration.text = state.formattedDuration
                player_controls_progress.max = state.duration
            }
            is PlayerContract.State.Progress -> {
                player_controls_position.text = state.formattedProgress
                if (isSeeking)
                    player_controls_progress.progress = state.progress
                player_controls_progress.secondaryProgress = state.bufferedProgress
            }
            is PlayerContract.State.Preview -> GlideApp.with(this).load(state.path)
                    .placeholder(R.drawable.ic_default_thumbnail).error(R.drawable.ic_default_thumbnail)
                    .transition(DrawableTransitionOptions.withCrossFade()).into(player_preview)
            is PlayerContract.State.Quality -> {
                when (state.qualityLevel) {
                    Player.QualityLevel.p1080 -> qualityMenu.subMenu.findItem(R.id.action_p1080).isChecked = true
                    Player.QualityLevel.p720 -> qualityMenu.subMenu.findItem(R.id.action_p720).isChecked = true
                    Player.QualityLevel.p480 -> qualityMenu.subMenu.findItem(R.id.action_p480).isChecked = true
                    Player.QualityLevel.p360 -> qualityMenu.subMenu.findItem(R.id.action_p360).isChecked = true
                }
            }
        }
    }

    override fun controlsVisible(isVisible: Boolean) {
        player_controls.isVisible = isVisible
    }

    private fun itemClicks(): Observable<PlayerContract.Action> {
        return RxToolbar.itemClicks(player_controls_toolbar).map<PlayerContract.Action> {
            when (it.itemId) {
                R.id.action_p1080 -> PlayerContract.Action.Quality(Player.QualityLevel.p1080)
                R.id.action_p720 -> PlayerContract.Action.Quality(Player.QualityLevel.p720)
                R.id.action_p480 -> PlayerContract.Action.Quality(Player.QualityLevel.p480)
                R.id.action_p360 -> PlayerContract.Action.Quality(Player.QualityLevel.p360)
                R.id.action_share -> throw RuntimeException("Not handled yet.")
                else -> throw RuntimeException("Not handled yet.")
            }
        }
    }

    companion object {
        private const val PreviewArtKey = "PreviewArt"
        fun newInstance(previewArt: String) = PlayerFragment().apply { arguments = Bundle().apply { putString(PreviewArtKey, previewArt) } }
    }
}