package me.mauricee.pontoon.rx.exoplayer

import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.video.VideoListener
import io.reactivex.Observable
import io.reactivex.Observer
import me.mauricee.pontoon.rx.BaseDisposable

fun SimpleExoPlayer.observe(): Observable<VideoEvent> = VideoObservable(this)

class VideoObservable(private val player: SimpleExoPlayer) : Observable<VideoEvent>() {

    override fun subscribeActual(observer: Observer<in VideoEvent>) {
        player.addVideoListener(Listener(observer).also(observer::onSubscribe))
    }

    private inner class Listener(private val observer: Observer<in VideoEvent>) : BaseDisposable(), VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            observer.onNext(VideoEvent.OnVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio))
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            observer.onNext(VideoEvent.OnSurfaceSizeChanged(width, height))
        }

        override fun onRenderedFirstFrame() {
            observer.onNext(VideoEvent.OnRenderedFirstFrame)
        }

        override fun onDisposed() {
            player.removeVideoListener(this)
        }
    }
}

sealed class VideoEvent {
    data class OnVideoSizeChanged(val width: Int, val height: Int, val unappliedRotationDegrees: Int, val pixelWidthHeightRatio: Float) : VideoEvent()
    data class OnSurfaceSizeChanged(val width: Int, val height: Int) : VideoEvent()
    object OnRenderedFirstFrame : VideoEvent()
}