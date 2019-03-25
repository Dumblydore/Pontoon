package me.mauricee.pontoon.model.livestream

import androidx.core.text.trimmedLength
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.domain.floatplane.Creator
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.LiveStreamMetadata
import me.mauricee.pontoon.ext.doOnIo
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.rx.okhttp.asSingle
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class LiveStreamRepository @Inject constructor(private val client: OkHttpClient,
                                               private val creatorRepository: UserRepository,
                                               private val floatPlaneApi: FloatPlaneApi,
                                               private val chatSessionBuilder: ChatSession.Builder) {

    val activeLiveStreams: Single<List<LiveStreamInfo>>
        get() = getLiveStreamMetaData().flatMapMaybe { creators ->
            creatorRepository.getCreators(*creators.map { it.id }.toTypedArray())
                    .flatMapIterable { it }
                    .map { Pair(creators.first { t2 -> it.id == t2.id }.liveStream, it) }
                    .flatMapSingle { liveStream ->
                        checkIfLive(liveStream.first).map { LiveStreamInfo(liveStream.first, liveStream.second, it) }
                    }.firstElement()
        }.toList()

    fun getLiveStreamOf(creatorId: String): Single<LiveStreamInfo> = Observable.zip<Creator, UserRepository.Creator, LiveStreamInfo>(floatPlaneApi.getCreator(creatorId).map { it.first() },
            creatorRepository.getCreators(creatorId).map { it.first() }, BiFunction { t1: Creator, t2: UserRepository.Creator -> LiveStreamInfo(t1.liveStream, t2, false) })
            .singleOrError()

    fun getChatSession(creator: UserRepository.Creator): Observable<ChatSession> =
            floatPlaneApi.subscriptions.flatMapIterable { it }
                    .map { it.creatorId }.toList()
                    .flatMapObservable { floatPlaneApi.getCreator(*it.toTypedArray()) }
                    .flatMapIterable { it }
                    .map { it.liveStream }
                    .filter { creator.id == it.owner }
                    .firstOrError().flatMapObservable { chatSessionBuilder.startSession(it) }

    private fun checkIfLive(liveStreamMetadata: LiveStreamMetadata): Single<Boolean> = Request.Builder()
            .get().url("$LiveStreamHost${liveStreamMetadata.streamPath}/$LiveStreamPlaylist")
            .build().let(client::newCall).asSingle().map { it.body()?.string() ?: "" }
            .doOnSuccess { logd("payload: $it, text length: ${it.length}") }
            .map { (it.isNotEmpty() && it.trimmedLength() >= 25) || BuildConfig.DEBUG }
            .onErrorReturnItem(false)
            .doOnError { logd("error", it) }
            .doOnIo()


    private fun getLiveStreamMetaData() = floatPlaneApi.subscriptions.flatMapIterable { it }
            .map { it.creatorId }.toList()
            .flatMapObservable { floatPlaneApi.getCreator(*it.toTypedArray()) }

    companion object {
        //TODO not sure if this is dynamic.
        private const val LiveStreamHost = "https://cdn1.floatplane.com"
        private const val LiveStreamPlaylist = "playlist.m3u8"
    }


}

data class LiveStreamInfo(val liveStreamMetadata: LiveStreamMetadata, val creator: UserRepository.Creator, val isLive: Boolean)