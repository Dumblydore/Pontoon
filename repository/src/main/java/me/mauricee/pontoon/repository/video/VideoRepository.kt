package me.mauricee.pontoon.repository.video

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.dropbox.android.external.store4.Store
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.parcelize.Parcelize
import me.mauricee.pontoon.data.local.video.VideoCreatorJoin
import me.mauricee.pontoon.data.local.video.VideoDao
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.data.network.video.ContentType
import me.mauricee.pontoon.repository.PagedModel
import me.mauricee.pontoon.repository.util.store.getAndFetch
import okhttp3.ResponseBody
import javax.inject.Inject

class VideoRepository @Inject constructor(private val videoStore: Store<String, VideoCreatorJoin>,
                                          private val relatedVideoStore: Store<String, List<VideoCreatorJoin>>,
                                          private val videoDao: VideoDao,
                                          private val floatPlaneApi: FloatPlaneApi,
                                          private val searchCallbackFactory: SearchBoundaryCallback.Factory,
                                          private val videoCallbackFactory: VideoBoundaryCallback.Factory,
                                          private val pageListConfig: PagedList.Config) {


    fun getVideos(unwatchedOnly: Boolean, vararg creatorIds: String): PagedModel<Video> {
        val callback = videoCallbackFactory.newInstance(*creatorIds)
        val factory = (if (unwatchedOnly) videoDao.getUnwatchedVideosByCreators(*creatorIds) else
            videoDao.getVideoByCreators(*creatorIds)).map { it.toModel() }
        return RxPagedListBuilder(factory, pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .setBoundaryCallback(callback)
                .buildFlowable(BackpressureStrategy.LATEST)
                .doOnTerminate(callback::dispose)
                .let {
                    PagedModel(it,
                            callback.pagingState.toFlowable(BackpressureStrategy.LATEST),
                            callback::refresh)
                }
    }

    fun getVideo(videoId: String): Flowable<Video> = videoStore.getAndFetch(videoId)
            .map(VideoCreatorJoin::toModel)

    fun getRelatedVideos(video: String): Flowable<List<Video>> = relatedVideoStore.getAndFetch(video)
            .map { it.map(VideoCreatorJoin::toModel) }

    fun search(query: String, vararg filteredSubs: String): PagedModel<Video> {
        val callback = searchCallbackFactory.newInstance(query, *filteredSubs)
        return RxPagedListBuilder(videoDao.search("%$query%", *filteredSubs).map(VideoCreatorJoin::toModel), pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .setBoundaryCallback(callback)
                .buildFlowable(BackpressureStrategy.LATEST)
                .doOnTerminate(callback::dispose)
                .let { PagedModel(it, callback.pagingState.toFlowable(BackpressureStrategy.LATEST), callback::refresh) }
    }

    fun getStream(videoId: String): Single<List<Stream>> = floatPlaneApi.getVideoContent(videoId, ContentType.vod).map { content ->
        content.resource.data.qualityLevels.map { level ->
            val uri = content.resource.uri.replace("{qualityLevels}", level.name)
                    .replace("{qualityLevelParams.token}", content.resource.data.qualityLevelParams[level.name]?.token
                            ?: "")
            Stream(level.label, level.order, level.width, level.height, "${content.cdn}$uri")
        }
    }

    fun watchHistory(): Flowable<PagedList<Video>> = videoDao.history().let {
        RxPagedListBuilder(it.map(VideoCreatorJoin::toModel), pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .buildFlowable(BackpressureStrategy.LATEST)
    }

    fun addToWatchHistory(videoId: String): Completable = videoDao.setWatched(videoId)

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

data class Playback(val video: VideoCreatorJoin, val streams: List<Stream>)

operator fun List<Stream>.get(value: String): Stream? = firstOrNull { it.name == value }