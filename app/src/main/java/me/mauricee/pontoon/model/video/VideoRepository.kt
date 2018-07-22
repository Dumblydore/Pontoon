package me.mauricee.pontoon.model.video

import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function4
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.common.CacheValidator
import me.mauricee.pontoon.domain.floatplane.Creator
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.Subscription
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.model.user.UserRepository
import okhttp3.ResponseBody
import javax.inject.Inject
import kotlin.math.log

class VideoRepository @Inject constructor(private val userRespository: UserRepository,
                                          private val videoDao: VideoDao,
                                          private val historyDao: HistoryDao,
                                          private val floatPlaneApi: FloatPlaneApi,
                                          private val pageListConfig: PagedList.Config,
                                          cacheValidatorBuilder: CacheValidator.Factory) {

    private val videoCache = cacheValidatorBuilder.newInstance("VideoCache")

    val subscriptions: Observable<List<UserRepository.Creator>> =
            floatPlaneApi.subscriptions.flatMapSingle(this::validateSubscriptions)
                    .map { it.map { it.creatorId }.toTypedArray() }
                    .flatMap { userRespository.getCreators(*it) }

    fun getVideos(forced: Boolean = false, vararg creators: UserRepository.Creator): Observable<PagedList<Video>> =
            videoCache.check<DataSource.Factory<Int, Video>>({ loadVideosFromCache(*creators) },
                    { VideoDataSource.Factory(floatPlaneApi, *creators) })
                    .let {
                        RxPagedListBuilder(it, pageListConfig)
                                .setFetchScheduler(Schedulers.io())
                                .setNotifyScheduler(AndroidSchedulers.mainThread())
                                .buildObservable()
                    }.compose(RxHelpers.applyObservableSchedulers())

    //lol
    private fun loadVideosFromCache(vararg creators: UserRepository.Creator) =
            videoDao.getVideoByCreators(*creators.map { it.id }.toTypedArray())
                    .map { Video(it, creators.first { it2 -> it.id == it2.id }) }


    fun getVideo(video: String): Single<Video> = videoDao.getVideo(video)
            .switchIfEmpty(getVideoInfoFromNetwork(video))
            .flatMap { vid ->
                userRespository.getCreators(vid.creator)
                        .map { it.first() }
                        .map { it ->
                            Video(vid.id, vid.title, vid.description, vid.releaseDate, vid.duration, it, vid.thumbnail)
                        }.firstOrError()
            }.compose(RxHelpers.applySingleSchedulers())

    fun getRelatedVideos(video: String): Single<List<Video>> = floatPlaneApi.getRelatedVideos(video)
            .doOnNext(::cacheVideos).flatMap { videos ->
                videos.map { it.creator }.distinct().toTypedArray().let { userRespository.getCreators(*it) }
                        .flatMap { it.toObservable() }
                        .flatMap { creator ->
                            videos.toObservable().filter { it.creator == creator.id }.map { Video(it, creator) }
                        }
            }.toList()

    fun getQualityOfVideo(videoId: String): Observable<Quality> = Observable.zip(
            floatPlaneApi.getVideoUrl(videoId, "360").map(::getUrlFromResponse),
            floatPlaneApi.getVideoUrl(videoId, "480").map(::getUrlFromResponse),
            floatPlaneApi.getVideoUrl(videoId, "720").map(::getUrlFromResponse),
            floatPlaneApi.getVideoUrl(videoId, "1080").map(::getUrlFromResponse),
            Function4 { t1, t2, t3, t4 -> Quality(t1, t2, t3, t4) }
    )

    fun addToWatchHistory(video: Video) {
        Completable.fromCallable { historyDao.insert(HistoryEntity(video.id)) }.onErrorComplete()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    fun watchHistory(): Flowable<List<Video>> = historyDao.history().distinctUntilChanged()
            .flatMapSingle { it.toFlowable().flatMapSingle { getVideo(it.videoId) }.toList() }

    private fun getVideoInfoFromNetwork(video: String): Single<VideoEntity> = floatPlaneApi.getVideoInfo(video)
            .map(::VideoEntity).singleOrError()

    private fun cacheVideos(videos: List<me.mauricee.pontoon.domain.floatplane.Video>) {
        videos.toObservable()
                .map(::VideoEntity)
                .toList().map { it.toTypedArray() }
                .flatMapCompletable { Completable.fromCallable { videoDao.insert(*it) } }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe()
    }

    private fun validateSubscriptions(subscriptions: List<Subscription>) =
            if (subscriptions.isEmpty()) Single.error(NoSubscriptionsException())
            else Single.just(subscriptions)

    private fun getUrlFromResponse(responseBody: ResponseBody): String =
            responseBody.string().let { it.substring(1, it.length - 1) }

    class NoSubscriptionsException : Exception("No subscriptions available")
}