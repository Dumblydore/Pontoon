package me.mauricee.pontoon.model.video

import android.net.Uri
import androidx.core.net.toUri
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function4
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.Subscription
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.ext.ioStream
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.edge.EdgeRepository
import me.mauricee.pontoon.model.subscription.SubscriptionDao
import me.mauricee.pontoon.model.subscription.SubscriptionEntity
import me.mauricee.pontoon.model.user.UserRepository
import okhttp3.ResponseBody
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VideoRepository @Inject constructor(private val userRepo: UserRepository,
                                          private val edgeRepo: EdgeRepository,
                                          private val videoDao: VideoDao,
                                          private val floatPlaneApi: FloatPlaneApi,
                                          private val subscriptionDao: SubscriptionDao,
                                          private val searchCallbackFactory: SearchBoundaryCallback.Factory,
                                          private val videoCallbackFactory: VideoBoundaryCallback.Factory,
                                          private val pageListConfig: PagedList.Config) {

    val subscriptions: Observable<List<UserRepository.Creator>> = Observable.mergeArray(subscriptionsFromCache(), subscriptionsFromNetwork())
            .debounce(400, TimeUnit.MILLISECONDS)
            .flatMap { userRepo.getCreators(*it) }
            .compose(RxHelpers.applyObservableSchedulers())

    private fun subscriptionsFromNetwork() = floatPlaneApi.subscriptions.flatMapSingle(this::validateSubscriptions)
            .compose { observer ->
                observer.doOnNext { cacheSubscriptions(it).also { observer.doOnDispose(it::dispose) } }
            }
            .map { it.map { it.creatorId }.toTypedArray() }

    private fun subscriptionsFromCache() = subscriptionDao.getSubscriptions()
            .map { it.map { it.creator }.toTypedArray() }

    fun getSubscriptionFeed(unwatchedOnly: Boolean = false, clean: Boolean): Observable<SubscriptionFeed> = subscriptions.map {
        SubscriptionFeed(it, getVideos(*it.toTypedArray(), unwatchedOnly = unwatchedOnly, refresh = clean))
    }

    fun getVideos(vararg creator: UserRepository.Creator, unwatchedOnly: Boolean = false, refresh: Boolean): VideoResult {
        val callback = videoCallbackFactory.newInstance(*creator)
        val creators = creator.map { it.id }.toTypedArray()
        val factory = if (unwatchedOnly) videoDao.getUnwatchedVideosByCreators(*creators) else
            videoDao.getVideoByCreators(*creators)
        return RxPagedListBuilder(factory.map { vid -> Video(vid, creator.first { it.id == vid.creator }) }, pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .setBoundaryCallback(callback)
                .buildObservable()
                .doOnDispose(callback::dispose)
                .apply {
                    if (refresh) {
                        Completable.fromCallable { videoDao.clearCreatorVideos(*creators) }
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .onErrorComplete().subscribe().also { doOnDispose(it::dispose) }
                    }
                }
                .let { VideoResult(it, callback.state, callback::retry) }
    }

    fun search(query: String, vararg filteredSubs: UserRepository.Creator): VideoResult {
        val callback = searchCallbackFactory.newInstance(query, *filteredSubs)
        return RxPagedListBuilder(videoDao.search(query, *filteredSubs.map { it.id }.toTypedArray())
                .map { vid -> Video(vid, filteredSubs.first { it.id == vid.creator }) }, pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .setBoundaryCallback(callback)
                .buildObservable()
                .doOnDispose(callback::dispose)
                .let { VideoResult(it, callback.state, callback::retry) }
    }

    fun getVideo(video: String): Single<Video> = videoDao.getVideo(video)
            .switchIfEmpty(getVideoInfoFromNetwork(video))
            .flatMap { vid ->
                userRepo.getCreators(vid.creator)
                        .map { it.first() }
                        .map { it ->
                            Video(vid.id, vid.title, vid.description, vid.releaseDate, vid.duration, it, vid.thumbnail, null)
                        }.firstOrError()
            }.ioStream()

    fun getRelatedVideos(video: String): Single<List<Video>> = floatPlaneApi.getRelatedVideos(video).flatMap { videos ->
        videos.map { it.creator }.distinct().toTypedArray().let { userRepo.getCreators(*it) }
                .flatMap { it.toObservable() }
                .flatMap { creator ->
                    videos.toObservable().filter { it.creator == creator.id }.map { Video(it, creator) }
                }
    }.toList()

    fun getDownloadLink(videoId: String, quality: Player.QualityLevel): Single<String> = Observable.combineLatest<ResponseBody, String, String>(
            floatPlaneApi.getVideoUrl(videoId, quality.name.replace("p", "")), edgeRepo.downloadHost.toObservable(),
            BiFunction { t1, t2 ->
                getUrlFromResponse(t2, t1).replace("/chunk.m3u8", "")
            })
            .doOnNext { logd("download url: $it") }
            .singleOrError()

    fun getQualityOfVideo(videoId: String): Observable<Quality> = edgeRepo.streamingHost.flatMapObservable<Quality> { host ->
        Observable.zip(floatPlaneApi.getVideoUrl(videoId, "360").map { getUrlFromResponse(host, it) },
                floatPlaneApi.getVideoUrl(videoId, "480").map { getUrlFromResponse(host, it) },
                floatPlaneApi.getVideoUrl(videoId, "720").map { getUrlFromResponse(host, it) },
                floatPlaneApi.getVideoUrl(videoId, "1080").map { getUrlFromResponse(host, it) },
                Function4 { t1, t2, t3, t4 -> Quality(t1, t2, t3, t4) })
    }

    fun watchHistory(): Observable<PagedList<Video>> = subscriptions.flatMap { creators ->
        videoDao.history().map { vid -> Video(vid, creators.first { it.id == vid.creator }) }
                .let {
                    RxPagedListBuilder(it, pageListConfig)
                            .setFetchScheduler(Schedulers.io())
                            .setNotifyScheduler(AndroidSchedulers.mainThread())
                            .buildObservable()
                }
    }

    fun addToWatchHistory(video: Video) {
        Completable.fromCallable { videoDao.setWatched(Instant.now(), video.id) }.onErrorComplete()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    private fun getVideoInfoFromNetwork(video: String): Single<VideoEntity> = floatPlaneApi.getVideoInfo(video)
            .map { it.toEntity() }.singleOrError()


    //TODO use proper id
    private fun cacheSubscriptions(subscriptions: List<Subscription>) = subscriptions.toObservable()
            .map { SubscriptionEntity(it.creatorId, it.plan.id, it.startDate, it.endDate) }
            .toList()
            .observeOn(Schedulers.io())
            .subscribe { it ->
                subscriptionDao.insert(*it.toTypedArray())
            }


    private fun validateSubscriptions(subscriptions: List<Subscription>) =
            if (subscriptions.isEmpty()) Single.error(NoSubscriptionsException())
            else Single.just(subscriptions)

    private fun getUrlFromResponse(host: String, responseBody: ResponseBody): String {
        val baseUri = responseBody.string().let { it.substring(1, it.length - 1) }.toUri()
        return Uri.Builder().authority(host).scheme(baseUri.scheme).encodedPath(baseUri.path)
                .encodedQuery(baseUri.encodedQuery).build().toString()
    }

    class NoSubscriptionsException : Exception("No subscriptions available")
}

data class Quality(val p360: String, val p480: String, val p720: String, val p1080: String)
data class Video(val id: String, val title: String, val description: String, val releaseDate: Instant,
                 val duration: Long, val creator: UserRepository.Creator, val thumbnail: String, val watched: Instant?) {

    constructor(video: me.mauricee.pontoon.domain.floatplane.Video, creator: UserRepository.Creator) : this(video.guid, video.title, video.description, video.releaseDate, video.duration, creator, video.defaultThumbnail, null)
    constructor(video: VideoEntity, creator: UserRepository.Creator) : this(video.id, video.title, video.description, video.releaseDate, video.duration, creator, video.thumbnail, video.watched)
}

data class Playback(val video: me.mauricee.pontoon.model.video.Video, val quality: Quality)

data class SubscriptionFeed(val subscriptions: List<UserRepository.Creator>, val videos: VideoResult)