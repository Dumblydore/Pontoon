package me.mauricee.pontoon.player.player

import android.support.v4.media.session.PlaybackStateCompat
import com.novoda.downloadmanager.Batch
import com.novoda.downloadmanager.DownloadBatchIdCreator
import com.novoda.downloadmanager.DownloadManager
import com.novoda.downloadmanager.StorageRoot
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ext.loge
import me.mauricee.pontoon.ext.toDuration
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class PlayerPresenter @Inject constructor(private val player: Player,
                                          private val videoRepository: VideoRepository,
                                          private val downloadManager: DownloadManager,
                                          private val storageRoot: StorageRoot,
                                          private val controls: PlayerContract.Controls,
                                          eventTracker: EventTracker) :
        BasePresenter<PlayerContract.State, PlayerContract.View>(eventTracker), PlayerContract.Presenter {

    override fun onViewAttached(view: PlayerContract.View): Observable<PlayerContract.State> =
            Observable.merge(listOf(view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap(::handleActions),
                    watchState(), watchProgress(), watchPreview(), watchTimeline(), watchDuration()))
                    .startWith(mutableListOf(PlayerContract.State.Bind(player.viewMode != Player.ViewMode.FullScreen),
                            PlayerContract.State.Quality(player.quality)))
                    .onErrorReturnItem(PlayerContract.State.Error)

    private fun handleActions(action: PlayerContract.Action): Observable<PlayerContract.State> = when (action) {
        PlayerContract.Action.SkipForward -> stateless { }
        PlayerContract.Action.SkipBackward -> stateless { }
        PlayerContract.Action.MinimizePlayer -> stateless {
            player.controlsVisible = false
            controls.setPlayerExpanded(false)
        }
        PlayerContract.Action.ToggleFullscreen -> stateless { controls.toggleFullscreen() }
        is PlayerContract.Action.PlayPause -> stateless { player.playPause() }
        is PlayerContract.Action.Download -> downloadVideo(action.quality)
        is PlayerContract.Action.Quality -> Observable.fromCallable { player.quality = action.qualityLevel; PlayerContract.State.Quality(action.qualityLevel) }
        is PlayerContract.Action.SeekProgress -> stateless { player.onSeekTo((action.progress * 1000).toLong()) }
        PlayerContract.Action.RequestShare -> PlayerContract.State.ShareUrl(player.currentlyPlaying!!.video).toObservable()
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