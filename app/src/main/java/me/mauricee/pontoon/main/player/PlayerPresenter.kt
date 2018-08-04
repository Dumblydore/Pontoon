package me.mauricee.pontoon.main.player

import android.support.v4.media.session.PlaybackStateCompat
import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.OrientationManager
import me.mauricee.pontoon.main.Player
import org.threeten.bp.Duration
import javax.inject.Inject

class PlayerPresenter @Inject constructor(private val player: Player,
                                          private val navigator: MainContract.Navigator,
                                          private val orientationManager: OrientationManager) :
        BasePresenter<PlayerContract.State, PlayerContract.View>(), PlayerContract.Presenter {

    override fun onViewAttached(view: PlayerContract.View): Observable<PlayerContract.State> =
            Observable.merge(listOf(view.actions.flatMap(::handleActions),
                    watchState(), watchProgress(), watchDuration(), watchPreview()))
                    .startWith(listOf(PlayerContract.State.Bind(player), PlayerContract.State.Loading))

    private fun handleActions(action: PlayerContract.Action): Observable<PlayerContract.State> = when (action) {
        is PlayerContract.Action.PlayPause -> stateless { player.playPause() }
        PlayerContract.Action.SkipForward -> stateless { }
        PlayerContract.Action.SkipBackward -> stateless { }
        PlayerContract.Action.MinimizePlayer -> stateless {
            player.controlsVisible = false
            navigator.setPlayerExpanded(false)
        }
        PlayerContract.Action.ToggleFullscreen -> stateless { orientationManager.apply { isFullscreen = !isFullscreen } }
        is PlayerContract.Action.Quality -> stateless { player.quality = action.level }
    }

    private fun watchProgress() = player.progress().distinctUntilChanged().map { PlayerContract.State.Progress(formatMillis(it)) }

    private fun watchPreview() = player.previewImage.map { PlayerContract.State.Preview(it) }

    private fun watchDuration() = player.duration.map { PlayerContract.State.Duration(formatMillis(it)) }

    private fun watchState() = player.playbackState.map {
        when (it) {
            PlaybackStateCompat.STATE_PLAYING -> PlayerContract.State.Playing
            PlaybackStateCompat.STATE_PAUSED -> PlayerContract.State.Paused
            PlaybackStateCompat.STATE_BUFFERING -> PlayerContract.State.Buffering
            PlaybackStateCompat.STATE_CONNECTING -> PlayerContract.State.Loading
            else -> PlayerContract.State.Paused
        }
    }

    private fun formatMillis(ms: Long) = Duration.ofMillis(ms).let {
        val seconds = it.seconds
        val absSeconds = Math.abs(seconds)
        val positive = String.format(
                "%02d:%02d",
                absSeconds % 3600 / 60,
                absSeconds % 60)
        if (seconds < 0) "-$positive" else positive
    }

}