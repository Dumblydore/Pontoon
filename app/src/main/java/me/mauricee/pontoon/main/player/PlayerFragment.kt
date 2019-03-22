package me.mauricee.pontoon.main.player

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.google.android.gms.cast.framework.CastButtonFactory
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.SeekBarStartChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarStopChangeEvent
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.layout_player_controls.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.playback.PlayerFactory
import me.mauricee.pontoon.ext.just
import me.mauricee.pontoon.ext.supportActionBar
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.rx.glide.toSingle
import javax.inject.Inject

class PlayerFragment : BaseFragment<PlayerPresenter>(),
        PlayerContract.View {

    @Inject
    lateinit var player: Player
    @Inject
    lateinit var playerControls: PlayerContract.Controls
    @Inject
    lateinit var playerFactory: PlayerFactory

    private val playIconAnimation by lazy { getDrawable(requireContext(), R.drawable.avc_play_to_pause) }
    private val playIcon by lazy { getDrawable(requireContext(), R.drawable.ic_play) }
    private val pauseIconAnimation by lazy { getDrawable(requireContext(), R.drawable.avc_pause_to_play) }
    private val pauseIcon by lazy { getDrawable(requireContext(), R.drawable.ic_pause) }

    private val previewArt by lazy { arguments?.getString(PreviewArtKey) ?: "" }
    private val qualityMenu by lazy { player_controls_toolbar.menu.findItem(R.id.action_quality) }
    private var isSeeking: Boolean = false
    private lateinit var mediaRouteMenuItem: MenuItem

    override val actions: Observable<PlayerContract.Action>
        get() = listOf(player_controls_fullscreen.clicks().map { PlayerContract.Action.ToggleFullscreen },
                player_controls_playPause.clicks().map { PlayerContract.Action.PlayPause },
                player_controls_progress.seekBarChanges
                        .doOnNext { isSeeking = it is SeekBarStartChangeEvent || (isSeeking && it !is SeekBarStopChangeEvent) }
                        .filter { it is SeekBarStopChangeEvent }
                        .cast(SeekBarStopChangeEvent::class.java)
                        .map { PlayerContract.Action.SeekProgress(it.view().progress) },
                RxToolbar.navigationClicks(player_controls_toolbar).map
                { PlayerContract.Action.MinimizePlayer }, itemClicks()
        ).let { Observable.merge(it) }

    override fun getLayoutId(): Int = R.layout.fragment_player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player_controls_toolbar.inflateMenu(R.menu.player_toolbar)
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(requireContext().applicationContext, player_controls_toolbar.menu, R.id.media_route_menu_item)
        player_display.setThumbnail(previewArt)
        supportActionBar?.just {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        subscriptions += playerFactory.playback.subscribe { player_display.player = it }
        subscriptions += player_display.ratio.subscribe(playerControls::setVideoRatio)
        subscriptions += player_display.controlsVisibilityChanged.subscribe {
            if (!it && !isSeeking) {
                player_controls_progress.thumbVisibility = false
                player_controls_progress.isVisible = player.viewMode == Player.ViewMode.Expanded
            }
        }
    }

    override fun updateState(state: PlayerContract.State) {
        when (state) {
            is PlayerContract.State.Bind -> {
                if (state.displayPipIcon) player_controls_toolbar.setNavigationIcon(R.drawable.ic_arrow_down)
            }
            is PlayerContract.State.Playing -> {
                isPlaying(true)
                player_controls_playPause.isVisible = true
                player_display.isVisible = true
            }
            is PlayerContract.State.Paused -> {
                isPlaying(false)
                player_display.isVisible = true
                player_controls_playPause.isVisible = true
            }
            is PlayerContract.State.Progress -> {
                player_controls_position.text = state.formattedProgress
                if (!isSeeking)
                    player_controls_progress.progress = state.progress
                player_controls_progress.bufferedProgress = state.bufferedProgress
            }
            is PlayerContract.State.Preview -> player_display.setThumbnail(state.path)
            is PlayerContract.State.Quality -> {
                when (state.qualityLevel) {
                    Player.QualityLevel.p1080 -> qualityMenu.subMenu.findItem(R.id.action_p1080).isChecked = true
                    Player.QualityLevel.p720 -> qualityMenu.subMenu.findItem(R.id.action_p720).isChecked = true
                    Player.QualityLevel.p480 -> qualityMenu.subMenu.findItem(R.id.action_p480).isChecked = true
                    Player.QualityLevel.p360 -> qualityMenu.subMenu.findItem(R.id.action_p360).isChecked = true
                }
            }
            PlayerContract.State.Error -> {
                player_controls_playPause.isVisible = false
            }
            PlayerContract.State.DownloadStart -> Toast.makeText(requireContext(), R.string.download_start, Toast.LENGTH_LONG).show()
            PlayerContract.State.DownloadFailed -> Toast.makeText(requireContext(), R.string.download_error, Toast.LENGTH_LONG).show()
            is PlayerContract.State.PreviewThumbnail -> {
                subscriptions += GlideApp.with(this).asBitmap().load(state.path)
                        .toSingle().subscribe({ it -> player_controls_progress.timelineBitmap = it },
                                { player_controls_progress.timelineBitmap = null })
            }
            is PlayerContract.State.Duration -> {
                player_controls_duration.text = state.formattedDuration
                player_controls_progress.duration = state.duration
            }
            is PlayerContract.State.ToggleControls -> {
                player_display.controlsVisible = !player_display.controlsVisible
                player_controls_progress.isVisible = player_display.controlsVisible || state.showProgress
                player_controls_progress.thumbVisibility = player_display.controlsVisible && !isSeeking
            }
            is PlayerContract.State.ControlBehavior -> {
                player_display.controlsVisible = false
                player_controls_progress.isVisible = state.isExpanded
                player_controls_progress.acceptTapsFromUser = state.areControlsAccepted
                player_display.isInFullscreen = state.isFullscreen
            }
        }
    }

    private fun isPlaying(isPlaying: Boolean) {
        val isVisible = player_controls_playPause.isVisible
        val currentIcon = when {
            isVisible && isPlaying -> playIconAnimation
            isVisible && !isPlaying -> pauseIconAnimation
            !isVisible && isPlaying -> pauseIcon
            else -> playIcon
        }
        player_controls_playPause.setImageDrawable(currentIcon)
        player_controls_playPause.drawable.startAsAnimatable()
    }

    private fun itemClicks(): Observable<PlayerContract.Action> {
        return RxToolbar.itemClicks(player_controls_toolbar).flatMap<PlayerContract.Action> {
            when (it.itemId) {
                R.id.action_p1080 -> PlayerContract.Action.Quality(Player.QualityLevel.p1080).toObservable()
                R.id.action_p720 -> PlayerContract.Action.Quality(Player.QualityLevel.p720).toObservable()
                R.id.action_p480 -> PlayerContract.Action.Quality(Player.QualityLevel.p480).toObservable()
                R.id.action_p360 -> PlayerContract.Action.Quality(Player.QualityLevel.p360).toObservable()
                R.id.action_download_p1080 -> PlayerContract.Action.Download(Player.QualityLevel.p1080).toObservable()
                R.id.action_download_p720 -> PlayerContract.Action.Download(Player.QualityLevel.p720).toObservable()
                R.id.action_download_p480 -> PlayerContract.Action.Download(Player.QualityLevel.p480).toObservable()
                R.id.action_download_p360 -> PlayerContract.Action.Download(Player.QualityLevel.p360).toObservable()
                R.id.action_share -> PlayerContract.Action.RequestShare.toObservable()
                else -> Observable.empty()
            }
        }
    }

    private fun Drawable.startAsAnimatable() {
        (this as? Animatable)?.start()
    }

    companion object {
        private const val PreviewArtKey = "PreviewArt"
        fun newInstance(previewArt: String): Fragment = PlayerFragment().apply { arguments = bundleOf(PreviewArtKey to previewArt) }
    }
}