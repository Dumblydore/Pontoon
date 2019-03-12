package me.mauricee.pontoon.model.livestream

import androidx.core.text.trimmedLength
import io.reactivex.Maybe
import io.reactivex.Single
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.LiveStreamMetadata
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

    val activeLiveStreams: Single<List<LiveStreamMetadata>>
        get() = floatPlaneApi.subscriptions.flatMapIterable { it }
                .map { it.creatorId }.toList()
                .flatMapObservable { floatPlaneApi.getCreator(*it.toTypedArray()) }
                .flatMapIterable { it }
                .map { it.liveStream }
                .flatMapMaybe(this::getValidLiveStream)
                .toList()

    fun getLiveStreamOf(creatorId: String): Single<LiveStreamMetadata> = floatPlaneApi.getCreator(creatorId)
            .flatMapIterable { it }.firstOrError().map { it.liveStream }

    fun getChatSession(creator: UserRepository.Creator): Single<LiveStreamResult> =
            floatPlaneApi.subscriptions.flatMapIterable { it }
                    .map { it.creatorId }.toList()
                    .flatMapObservable { floatPlaneApi.getCreator(*it.toTypedArray()) }
                    .flatMapIterable { it }
                    .map { it.liveStream }
                    .filter { creator.id == it.owner }
                    .firstOrError().map { LiveStreamResult(it, chatSessionBuilder.startSession(it)) }

    private fun getValidLiveStream(liveStreamMetadata: LiveStreamMetadata): Maybe<LiveStreamMetadata> = Request.Builder()
            .get().url("$LiveStreamHost${liveStreamMetadata.streamPath}/$LiveStreamPlaylist")
            .build().let(client::newCall).asSingle().map { it.body()?.string() ?: "" }
            .doOnSuccess { logd("payload: $it, text length: ${it.length}") }
            .filter { (it.isNotEmpty() && it.trimmedLength() >= 25) || BuildConfig.DEBUG }
            .map { liveStreamMetadata }.onErrorComplete()

    companion object {
        //TODO not sure if this is dynamic.
        private const val LiveStreamHost = "https://cdn1.floatplane.com"
        private const val LiveStreamPlaylist = "playlist.m3u8"
    }


}