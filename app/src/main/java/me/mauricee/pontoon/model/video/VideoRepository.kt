package me.mauricee.pontoon.model.video

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import me.mauricee.pontoon.domain.floatplane.ContentType
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.doOnIo
import me.mauricee.pontoon.ext.getAndFetch
import me.mauricee.pontoon.model.PagedModel
import me.mauricee.pontoon.model.edge.EdgeRepository
import okhttp3.ResponseBody
import javax.inject.Inject

class VideoRepository @Inject constructor(private val videoStore: StoreRoom<Video, String>,
                                          private val relatedVideoStore: StoreRoom<List<Video>, String>,
                                          private val edgeRepo: EdgeRepository,
                                          private val videoDao: VideoDao,
                                          private val floatPlaneApi: FloatPlaneApi,
                                          private val searchCallbackFactory: SearchBoundaryCallback.Factory,
                                          private val videoCallbackFactory: VideoBoundaryCallback.Factory,
                                          private val pageListConfig: PagedList.Config) {


    fun getVideos(unwatchedOnly: Boolean, clean: Boolean, vararg creatorIds: String): PagedModel<Video> {
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
                .let {
                    PagedModel(it, callback.state, callback::refresh, callback::retry)
                }
    }

    fun getVideo(videoId: String): Observable<Video> = videoStore.getAndFetch(videoId)

    fun getRelatedVideos(video: String): Observable<List<Video>> = relatedVideoStore.get(video)

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

    fun getStream(videoId: String): Single<List<Stream>> = floatPlaneApi.getVideoContent(videoId, ContentType.vod).map { content ->
        content.resource.data.qualityLevels.map { level ->
            val uri = content.resource.uri.replace("{qualityLevels}", level.name)
                    .replace("{qualityLevelParams.token}", content.resource.data.qualityLevelParams[level.name]?.token
                            ?: "")
            Stream(level.label, level.order, level.width, level.height, "${content.cdn}$uri")
        }
    }

    //TODO
    fun getDownloadLink(videoId: String, qualityIndex: Int): Single<String> = Single.never()

    /*Observable.combineLatest<ResponseBody, String, String>(
            floatPlaneApi.getVideoUrl(videoId, quality.name.replace("p", "")), edgeRepo.downloadHost.toObservable(),
            BiFunction { t1, t2 ->
                getUrlFromResponse(t2, t1).replace("/chunk.m3u8", "")
            })
            .singleOrError()*/

    fun watchHistory(): Observable<PagedList<Video>> = videoDao.history().let {
        RxPagedListBuilder(it, pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .buildObservable()
    }

    fun addToWatchHistory(videoId: String): Completable = videoDao.setWatched(videoId).doOnIo()

    private fun getUrlFromResponse(host: String, responseBody: ResponseBody): String {
        val baseUri = responseBody.string().let { it.substring(1, it.length - 1) }.toUri()
        return Uri.Builder().authority(host).scheme(baseUri.scheme).encodedPath(baseUri.path)
                .encodedQuery(baseUri.encodedQuery).build().toString()
    }

    class NoSubscriptionsException : Exception("No subscriptions available")
}

@Parcelize
data class Stream(val name: String,
                  val ordinal: Int,
                  val width: Int, val height: Int,
                  val url: String) : Parcelable

data class Playback(val video: Video, val streams: List<Stream>)

operator fun List<Stream>.get(value: String): Stream? = firstOrNull { it.name == value }