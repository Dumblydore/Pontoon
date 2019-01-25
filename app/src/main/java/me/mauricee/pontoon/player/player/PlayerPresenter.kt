package me.mauricee.pontoon.player.player

import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.gms.cast.framework.SessionManager
import com.novoda.downloadmanager.Batch
import com.novoda.downloadmanager.DownloadBatchIdCreator
import com.novoda.downloadmanager.DownloadManager
import com.novoda.downloadmanager.StorageRoot
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.playback.PlaybackLocation
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.ext.loge
import me.mauricee.pontoon.ext.toDuration
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.rx.cast.SessionEvent
import me.mauricee.pontoon.rx.cast.events
import javax.inject.Inject

class PlayerPresenter @Inject constructor(private val player: Player,
                                          private val videoRepository: VideoRepository,
                                          private val downloadManager: DownloadManager,
                                          private val storageRoot: StorageRoot,
                                          private val controls: PlayerContract.Controls,
                                          private val castSessionManager: SessionManager,
                                          eventTracker: EventTracker) :
        BasePresenter<PlayerContract.State, PlayerContract.View>(eventTracker), PlayerContract.Presenter {

    override fun onViewAttached(view: PlayerContract.View): Observable<PlayerContract.State> =
            Observable.merge(listOf(view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap(::handleActions),
                    watchState(), watchProgress(), watchDuration(), watchPreview(), watchTimeline(), watchCastSession()))
                    .startWith(mutableListOf<PlayerContract.State>(PlayerContract.State.Quality(player.quality))
                            .also { if (!player.isActive()) it += PlayerContract.State.Loading })
                    .doOnError { loge("PlayerPresenter error!", it) }
                    .onErrorReturnItem(PlayerContract.State.Error)

    private fun handleActions(action: PlayerContract.Action): Observable<PlayerContract.State> = when (action) {
        PlayerContract.Action.SkipForward -> stateless { }
        PlayerContract.Action.SkipBackward -> stateless { }
        PlayerContract.Action.MinimizePlayer -> stateless {
            if (player.viewMode != Player.ViewMode.FullScreen) {
                player.controlsVisible = false
                controls.setPlayerExpanded(false)
            }
        }
        PlayerContract.Action.ToggleFullscreen -> stateless { controls.toggleFullscreen() }
        is PlayerContract.Action.PlayPause -> stateless { player.playPause() }
        is PlayerContract.Action.Download -> downloadVideo(action.quality)
        is PlayerContract.Action.Quality -> Observable.fromCallable { player.quality = action.qualityLevel; PlayerContract.State.Quality(action.qualityLevel) }
        is PlayerContract.Action.SeekProgress -> stateless { player.onSeekTo((action.progress * 1000).toLong()) }
        PlayerContract.Action.RequestShare -> PlayerContract.State.ShareUrl(player.currentlyPlaying!!.video).toObservable()
    }

    private fun watchCastSession() = castSessionManager.events().filter {
        it is SessionEvent.Starting || it is SessionEvent.Ending
    }.map { PlayerContract.State.Loading }

    private fun watchDuration() = player.duration.map { PlayerContract.State.Duration(it, it.toDuration()) }

    private fun watchProgress() = Observable.combineLatest<Long, Long, PlayerContract.State>(player.progress().distinctUntilChanged(),
            player.bufferedProgress().distinctUntilChanged(), BiFunction { t1, t2 ->
        PlayerContract.State.Progress(t1, t2 / 1000, t1.toDuration())
    })

    private fun watchPreview() = player.previewImage.map(PlayerContract.State::Preview)

    private fun watchState() = Observable.combineLatest<PlaybackLocation, Int, PlayerContract.State>(
            player.playbackLocation.doOnNext { logd("playbackLocation fired!") },
            player.playbackState.doOnNext { logd("playbackState fired!") }, BiFunction { location, state ->
        when (state) {
            PlaybackStateCompat.STATE_PLAYING -> PlayerContract.State.Playing(location)
            PlaybackStateCompat.STATE_PAUSED -> PlayerContract.State.Paused(location)
            PlaybackStateCompat.STATE_BUFFERING -> PlayerContract.State.Buffering(location)
            PlaybackStateCompat.STATE_CONNECTING -> PlayerContract.State.Loading
            PlaybackStateCompat.STATE_ERROR -> PlayerContract.State.Error
            else -> PlayerContract.State.Paused(location)
        }
    })

    private fun watchTimeline() = player.thumbnailTimeline.map(PlayerContract.State::PreviewThumbnail)

    private fun downloadVideo(qualityLevel: Player.QualityLevel) = player.currentlyPlaying!!.video.let { video ->
        videoRepository.getDownloadLink(video.id, qualityLevel).map {
            Batch.with(storageRoot, DownloadBatchIdCreator.createSanitizedFrom(video.id), video.title)
                    .downloadFrom(it).apply()
        }.flatMapObservable {
            Observable.fromCallable<PlayerContract.State> {
                downloadManager.download(it.build())
                PlayerContract.State.DownloadStart
            }
        }.doOnError { loge("Error downloading.", it) }
                .onErrorReturnItem(PlayerContract.State.DownloadFailed)
    }

}