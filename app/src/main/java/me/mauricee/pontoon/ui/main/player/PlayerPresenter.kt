package me.mauricee.pontoon.ui.main.player

import android.support.v4.media.session.PlaybackStateCompat
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.ShareManager
import me.mauricee.pontoon.common.download.DownloadHelper
import me.mauricee.pontoon.common.gestures.GestureEvent
import me.mauricee.pontoon.common.gestures.VideoTouchHandler
import me.mauricee.pontoon.ext.toDuration
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.playback.Player
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PlayerPresenter @Inject constructor(private val player: Player,
                                          private val downloadHelper: DownloadHelper,
                                          private val shareManager: ShareManager,
                                          private val videoTouchHandler: VideoTouchHandler,
                                          private val controls: PlayerContract.Controls,
                                          eventTracker: EventTracker) :
        BasePresenter<PlayerContract.State, PlayerContract.View>(eventTracker), PlayerContract.Presenter {

    override fun onViewAttached(view: PlayerContract.View): Observable<PlayerContract.State> = Observable.merge(listOf(view.actions.doOnNext
    { eventTracker.trackAction(it, view) }.flatMap(::handleActions),
            watchState(), watchProgress(), watchPreview(), watchTimeline(), watchDuration(), watchViewMode(), handleVideoTouchEvents()))
            .startWith(mutableListOf(PlayerContract.State.Bind(player.viewMode != Player.ViewMode.FullScreen)))
            .onErrorReturnItem(PlayerContract.State.Error)

    private fun handleActions(action: PlayerContract.Action): Observable<PlayerContract.State> = when (action) {
        PlayerContract.Action.SkipForward -> stateless { }
        PlayerContract.Action.SkipBackward -> stateless { }
        PlayerContract.Action.MinimizePlayer -> stateless { controls.setPlayerExpanded(false) }
        PlayerContract.Action.ToggleFullscreen -> stateless { controls.toggleFullscreen() }
        is PlayerContract.Action.PlayPause -> stateless { player.playPause() }
        is PlayerContract.Action.Download -> downloadVideo(action.quality)
        is PlayerContract.Action.Quality -> Observable.fromCallable { PlayerContract.State.Quality(player.setQualityIndex(action.quality)!!) }
        is PlayerContract.Action.SeekProgress -> stateless { player.onSeekTo((action.progress * 1000).toLong()) }
        PlayerContract.Action.RequestShare -> stateless { shareManager.shareVideo(player.currentlyPlaying!!.video) }
    }

    private fun watchDuration() = player.duration.map { PlayerContract.State.Duration(it, it.toDuration()) }

    private fun watchProgress() = Observable.combineLatest<Long, Long, PlayerContract.State>(player.progress().distinctUntilChanged(),
            player.bufferedProgress().distinctUntilChanged(), BiFunction { t1, t2 ->
        PlayerContract.State.Progress(t1, t2 / 1000, t1.toDuration())
    })

    private fun watchPreview() = player.previewImage.map(PlayerContract.State::Preview)

    private fun watchState() = player.playbackState.flatMap<PlayerContract.State> {
        when (it) {
            PlaybackStateCompat.STATE_PLAYING -> PlayerContract.State.Playing.toObservable()
            PlaybackStateCompat.STATE_PAUSED -> PlayerContract.State.Paused.toObservable()
            PlaybackStateCompat.STATE_ERROR -> PlayerContract.State.Error.toObservable()
            else -> Observable.empty()
        }
    }

    private fun watchViewMode() = player.viewModes.map {
        when (it) {
            Player.ViewMode.PictureInPicture -> PlayerContract.State.ControlBehavior(areControlsAccepted = false, isFullscreen = false, isExpanded = false)
            Player.ViewMode.FullScreen -> PlayerContract.State.ControlBehavior(true, true, false)
            Player.ViewMode.Expanded -> PlayerContract.State.ControlBehavior(true, false, true)
        }
    }

    private fun watchTimeline() = player.thumbnailTimeline.map(PlayerContract.State::PreviewThumbnail)

    private fun handleVideoTouchEvents() = Observable.merge(videoTouchHandler.events.filter { it is GestureEvent.Click }
            .cast(GestureEvent.Click::class.java).map { PlayerContract.State.ToggleControls(player.viewMode == Player.ViewMode.Expanded) },
            videoTouchHandler.events.filter { it is GestureEvent.Scale }
                    .cast(GestureEvent.Scale::class.java).map { PlayerContract.State.HideControls }
                    .throttleLatest(250, TimeUnit.SECONDS)
    )

    private fun downloadVideo(qualityLevel: Int): Observable<PlayerContract.State> = Observable.empty()
    /* = player.currentlyPlaying!!.video.let { video ->
        downloadHelper.download(video, qualityLevel)
    }.map { if (it) PlayerContract.State.DownloadStart else PlayerContract.State.DownloadFailed }
            .onErrorReturnItem(PlayerContract.State.DownloadFailed).toObservable()*/


}