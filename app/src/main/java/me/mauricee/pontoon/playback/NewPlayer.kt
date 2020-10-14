package me.mauricee.pontoon.playback

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.common.playback.PlayerFactory
import me.mauricee.pontoon.di.AppScope
import javax.inject.Inject

@AppScope
class NewPlayer @Inject constructor(private val playerFactory: PlayerFactory,
                                    private val mediaSessionConnector: MediaSessionConnector) : Disposable {

    private val subs = CompositeDisposable()
    private var activePlayer: Player? = null

    init {
        subs += playerFactory.playback.doOnNext { activePlayer = it }.subscribe(mediaSessionConnector::setPlayer)
    }

    override fun dispose() = subs.clear()

    override fun isDisposed(): Boolean = false
}