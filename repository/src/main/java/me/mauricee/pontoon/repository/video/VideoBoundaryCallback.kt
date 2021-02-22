package me.mauricee.pontoon.repository.video

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.common.BaseBoundaryCallback
import me.mauricee.pontoon.common.PagingState
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import javax.inject.Inject

class VideoBoundaryCallback(private val api: FloatPlaneApi,
                            private val videoDao: VideoDao,
                            private vararg val creatorIds: String) : BaseBoundaryCallback<Video>() {

    override fun clearItems(): Completable = videoDao.clearCreatorVideos(*creatorIds)

    override fun noItemsLoaded(): Observable<PagingState> = Observable.fromArray(*creatorIds)
            .flatMapSingle(api::getVideos)
            .flatMap { it.toObservable() }
            .map { it.toEntity() }
            .toList().toObservable()
            .map {
                if (videoDao.insert(it).isEmpty()) PagingState.Completed
                else PagingState.Fetched
            }

    override fun frontItemLoaded(itemAtFront: Video): Observable<PagingState> = noItemsLoaded()

    override fun endItemLoaded(itemAtEnd: Video): Observable<PagingState> = Observable.fromArray(*creatorIds)
            .flatMapSingle { api.getVideos(it, videoDao.getNumberOfVideosByCreator(it)) }
            .flatMap { it.toObservable() }
            .map { it.toEntity() }
            .toList().toObservable()
            .map {
                if (videoDao.insert(it).isEmpty()) PagingState.Completed
                else PagingState.Fetched
            }

    class Factory @Inject constructor(private val api: FloatPlaneApi, private val videoDao: VideoDao) {
        fun newInstance(vararg creator: String): VideoBoundaryCallback = VideoBoundaryCallback(api, videoDao, *creator)
    }
}