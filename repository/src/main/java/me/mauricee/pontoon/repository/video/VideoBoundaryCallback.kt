package me.mauricee.pontoon.repository.video

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.data.local.video.VideoCreatorJoin
import me.mauricee.pontoon.data.local.video.VideoDao
import me.mauricee.pontoon.repository.util.paging.BaseBoundaryCallback
import me.mauricee.pontoon.repository.util.paging.PagingState
import me.mauricee.pontoon.data.network.FloatPlaneApi
import javax.inject.Inject

class VideoBoundaryCallback(private val api: FloatPlaneApi,
                            private val videoDao: VideoDao,
                            private vararg val creatorIds: String) : BaseBoundaryCallback<Video>() {

    override fun clearItems(): Completable = videoDao.clearCreatorVideos(*creatorIds)

    override fun noItemsLoaded(): Single<PagingState> = Observable.fromArray(*creatorIds)
            .flatMapSingle(api::getVideos)
            .flatMap { it.toObservable() }
            .map { it.toEntity() }
            .toList()
            .map {
                if (videoDao.insert(it).isEmpty()) PagingState.Completed
                else PagingState.Fetched
            }

    override fun frontItemLoaded(itemAtFront: Video): Single<PagingState> = noItemsLoaded()

    override fun endItemLoaded(itemAtEnd: Video): Single<PagingState> = Observable.fromArray(*creatorIds)
            .flatMapSingle { api.getVideos(it, videoDao.getNumberOfVideosByCreator(it)) }
            .flatMap { it.toObservable() }
            .map { it.toEntity() }
            .toList()
            .map {
                if (videoDao.insert(it).isEmpty()) PagingState.Completed
                else PagingState.Fetched
            }

    class Factory @Inject constructor(private val api: FloatPlaneApi, private val videoDao: VideoDao) {
        fun newInstance(vararg creator: String): VideoBoundaryCallback = VideoBoundaryCallback(api, videoDao, *creator)
    }
}