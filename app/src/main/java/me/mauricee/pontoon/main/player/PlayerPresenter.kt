package me.mauricee.pontoon.main.player

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
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.OrientationManager
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.video.VideoRepository
import org.threeten.bp.Duration
import javax.inject.Inject

class PlayerPresenter @Inject constructor(private val player: Player,
                                          private val videoRepository: VideoRepository,
                                          private val downloadManager: DownloadManager,
                                          private val storageRoot: StorageRoot,
                                          private val navigator: MainContract.Navigator,
                                          private val orientationManager: OrientationManager,
                                          eventTracker: EventTracker) :
        BasePresenter<PlayerContract.State, PlayerContract.View>(eventTracker), PlayerContract.Presenter {

    override fun onViewAttached(view: PlayerContract.View): Observable<PlayerContract.State> =
            Observable.merge(listOf(view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap(::handleActions),
                    watchState(), watchProgress(), watchDuration(), watchPreview(), watchTimeline()))
                    .startWith(listOf(PlayerContract.State.Bind(player), PlayerContract.State.Loading, PlayerContract.State.Quality(player.quality)))

    private fun handleActions(action: PlayerContract.Action): Observable<PlayerContract.State> = when (action) {
        PlayerContract.Action.SkipForward -> stateless { }
        PlayerContract.Action.SkipBackward -> stateless { }
        PlayerContract.Action.MinimizePlayer -> stateless {
            player.controlsVisible = false
            navigator.setPlayerExpanded(false)
        }
        PlayerContract.Action.ToggleFullscreen -> stateless { orientationManager.apply { isFullscreen = !isFullscreen } }
        is PlayerContract.Action.PlayPause -> stateless { player.playPause() }
        is PlayerContract.Action.Download -> downloadVideo(action.quality)
        is PlayerContract.Action.Quality -> Observable.fromCallable { player.quality = action.qualityLevel; PlayerContract.State.Quality(action.qualityLevel) }
        is PlayerContract.Action.SeekProgress -> stateless { player.onSeekTo((action.progress * 1000).toLong()) }
    }

    private fun watchProgress() = Observable.combineLatest<Long, Long, PlayerContract.State>(player.progress().distinctUntilChanged(),
            player.bufferedProgress().distinctUntilChanged(), BiFunction { t1, t2 ->
        PlayerContract.State.Progress((t1 / 1000).toInt(), (t2 / 1000).toInt(), formatMillis(t1))
    })

    private fun watchPreview() = player.previewImage.map(PlayerContract.State::Preview)

    private fun watchDuration() = player.duration.map { PlayerContract.State.Duration((it / 1000).toInt(), formatMillis(it)) }

    private fun watchState() = player.playbackState.map {
        when (it) {
            PlaybackStateCompat.STATE_PLAYING -> PlayerContract.State.Playing
            PlaybackStateCompat.STATE_PAUSED -> PlayerContract.State.Paused
            PlaybackStateCompat.STATE_BUFFERING -> PlayerContract.State.Buffering
            PlaybackStateCompat.STATE_CONNECTING -> PlayerContract.State.Loading
            else -> PlayerContract.State.Paused
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