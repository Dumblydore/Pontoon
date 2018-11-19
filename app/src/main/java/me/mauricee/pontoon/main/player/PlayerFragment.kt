package me.mauricee.pontoon.main.player

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.transition.TransitionInflater
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
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
                player_controls_error.isVisible = false
                player_controls_progress.bufferedProgress = 0
            }
            is PlayerContract.State.Buffering -> {
                player_controls_loading.isVisible = true
                player_controls_playPause.isVisible = false
                player_controls_error.isVisible = false
            }
            is PlayerContract.State.Duration -> {
                player_controls_duration.text = state.formattedDuration
                player_controls_progress.duration = state.duration
            }
            is PlayerContract.State.Progress -> {
                player_controls_position.text = state.formattedProgress
                if (!isSeeking)
                    player_controls_progress.progress = state.progress
                player_controls_progress.bufferedProgress = state.bufferedProgress
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
            PlayerContract.State.Error -> {
                player_preview.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
                player_controls_loading.isVisible = false
                player_controls_playPause.isVisible = false
                player_controls_error.isVisible = true
            }
            PlayerContract.State.DownloadStart -> Toast.makeText(requireContext(), R.string.download_start, Toast.LENGTH_LONG).show()
            PlayerContract.State.DownloadFailed -> Toast.makeText(requireContext(), R.string.download_error, Toast.LENGTH_LONG).show()
            is PlayerContract.State.PreviewThumbnail -> {
                subscriptions += GlideApp.with(this).asBitmap().load(state.path)
                        .toSingle().subscribe{it -> player_controls_progress.timelineBitmap = it}
            }
        }
    }

    override fun controlsVisible(isVisible: Boolean) {
        player_controls.isVisible = isVisible
        player_controls_progress.thumbVisibility = isVisible
        player_controls_progress.acceptTapsFromUser = isVisible
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