package me.mauricee.pontoon.main.player

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isVisible
import androidx.transition.TransitionInflater
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.layout_player_controls.*
import kotlinx.android.synthetic.main.layout_player_controls.view.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.isPortrait
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.main.MainActivity

class PlayerFragment : BaseFragment<PlayerPresenter>(),
        PlayerContract.View, Player.ControlView {

    private val playIcon by lazy { getDrawable(requireContext(), R.drawable.ic_media_play_dark) }
    private val pauseIcon by lazy { getDrawable(requireContext(), R.drawable.ic_media_pause_dark) }
    private val previewArt by lazy { arguments!!.getString(PreviewArtKey) }

    override val actions: Observable<PlayerContract.Action>
        get() = Observable.merge(player_controls_fullscreen.clicks().map { PlayerContract.Action.ToggleFullscreen },
                player_controls_playPause.clicks().map { PlayerContract.Action.PlayPause },
                RxToolbar.navigationClicks(player_controls_toolbar).map { PlayerContract.Action.MinimizePlayer },
                itemClicks()
        )

    override fun getLayoutId(): Int = R.layout.fragment_player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlideApp.with(this).load(previewArt).into(player_preview)
        view.player_controls_toolbar.inflateMenu(R.menu.player_toolbar)
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
            }
            is PlayerContract.State.Duration -> {
                player_controls_duration.text = state.duration
            }
            is PlayerContract.State.Progress -> {
                player_controls_position.text = state.progress
            }
            is PlayerContract.State.Preview -> GlideApp.with(this).load(state.path)
                    .transition(DrawableTransitionOptions.withCrossFade()).into(player_preview)
        }
    }

    override fun controlsVisible(isVisible: Boolean) {
        player_controls.isVisible = isVisible
    }

    private fun itemClicks(): Observable<PlayerContract.Action> {
        return RxToolbar.itemClicks(player_controls_toolbar).flatMap<PlayerContract.Action> {
            when (it.itemId) {
                R.id.action_p1080 -> PlayerContract.Action.Quality(Player.QualityLevel.p1080).toObservable()
                R.id.action_p720 -> PlayerContract.Action.Quality(Player.QualityLevel.p720).toObservable()
                R.id.action_p480 -> PlayerContract.Action.Quality(Player.QualityLevel.p480).toObservable()
                R.id.action_p360 -> PlayerContract.Action.Quality(Player.QualityLevel.p360).toObservable()
                R.id.action_share -> Observable.empty()
                else -> Observable.empty()
            }
        }
    }

    companion object {
        private const val PreviewArtKey = "PreviewArt"
        fun newInstance(previewArt: String) = PlayerFragment().apply { arguments = Bundle().apply { putString(PreviewArtKey, previewArt) } }
    }
}