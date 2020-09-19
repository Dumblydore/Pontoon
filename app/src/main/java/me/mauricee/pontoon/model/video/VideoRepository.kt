package me.mauricee.pontoon.model.video

import android.net.Uri
import androidx.core.net.toUri
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.doOnIo
import me.mauricee.pontoon.ext.getAndFetch
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.edge.EdgeRepository
import me.mauricee.pontoon.model.user.UserRepository
import okhttp3.ResponseBody
import org.threeten.bp.Instant
import javax.inject.Inject

class VideoRepository @Inject constructor(private val videoStore: StoreRoom<Video, String>,
                                          private val relatedVideoStore: StoreRoom<List<Video>, String>,
                                          private val edgeRepo: EdgeRepository,
                                          private val videoDao: VideoDao,
                                          private val floatPlaneApi: FloatPlaneApi,
                                          private val searchCallbackFactory: SearchBoundaryCallback.Factory,
                                          private val videoCallbackFactory: VideoBoundaryCallback.Factory,
                                          private val pageListConfig: PagedList.Config) {

    fun getVideos(unwatchedOnly: Boolean, clean: Boolean, vararg creatorIds: String): VideoResult {
        val callback = videoCallbackFactory.newInstance(*creatorIds)
        val factory = if (unwatchedOnly) videoDao.getUnwatchedVideosByCreators(*creatorIds) else
            videoDao.getVideoByCreators(*creatorIds)
        return RxPagedListBuilder(factory, pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .setBoundaryCallback(callback)
                .buildObservable()
                .doOnDispose(callback::dispose)
                .doOnTerminate(callback::dispose)
                .apply {
                    if (clean) {
                        Completable.fromCallable { videoDao.clearCreatorVideos(*creatorIds) }
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .onErrorComplete().subscribe().also { doOnDispose(it::dispose) }
                    }
                }
                .let { VideoResult(it, callback.state, callback::retry) }
    }

    fun getVideo(videoId: String): Observable<Video> = videoStore.getAndFetch(videoId)

    fun getRelatedVideos(video: String): Observable<List<Video>> = relatedVideoStore.getAndFetch(video)

    fun search(query: String, vararg filteredSubs: String): VideoResult {
        val callback = searchCallbackFactory.newInstance(query, *filteredSubs)
        return RxPagedListBuilder(videoDao.search("%$query%", *filteredSubs), pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .setBoundaryCallback(callback)
                .buildObservable()
                .doOnDispose(callback::dispose)
                .let { VideoResult(it, callback.state, callback::retry) }
    }

    fun getDownloadLink(videoId: String, quality: Player.QualityLevel): Single<String> = Observable.combineLatest<ResponseBody, String, String>(
            floatPlaneApi.getVideoUrl(videoId, quality.name.replace("p", "")), edgeRepo.downloadHost.toObservable(),
            BiFunction { t1, t2 ->
                getUrlFromResponse(t2, t1).replace("/chunk.m3u8", "")
            })
            .singleOrError()

    fun getQualityOfVideo(videoId: String): Observable<Quality> = edgeRepo.streamingHost.flatMapObservable<Quality> { host ->
        Observable.zip(floatPlaneApi.getVideoUrl(videoId, "360").map { getUrlFromResponse(host, it) },
                floatPlaneApi.getVideoUrl(videoId, "480").map { getUrlFromResponse(host, it) },
                floatPlaneApi.getVideoUrl(videoId, "720").map { getUrlFromResponse(host, it) },
                floatPlaneApi.getVideoUrl(videoId, "1080").map { getUrlFromResponse(host, it) },
                Function4 { t1, t2, t3, t4 -> Quality(t1, t2, t3, t4) })
    }

    fun watchHistory(): Observable<PagedList<Video>> = videoDao.history().let {
        RxPagedListBuilder(it, pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .buildObservable()
    }

    fun addToWatchHistory(video: Video) {
        Completable.fromCallable { videoDao.setWatched(Instant.now(), video.id) }
                .onErrorComplete().doOnIo().subscribe()
    }

    private fun getUrlFromResponse(host: String, responseBody: ResponseBody): String {
        val baseUri = responseBody.string().let { it.substring(1, it.length - 1) }.toUri()
        return Uri.Builder().authority(host).scheme(baseUri.scheme).encodedPath(baseUri.path)
                .encodedQuery(baseUri.encodedQuery).build().toString()
    }

    class NoSubscriptionsException : Exception("No subscriptions available")
}

data class Quality(val p360: String, val p480: String, val p720: String, val p1080: String)

data class Playback(val video: Video, val quality: Quality)