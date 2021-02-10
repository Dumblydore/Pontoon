package me.mauricee.pontoon.model.video

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.common.BaseBoundaryCallback
import me.mauricee.pontoon.common.PagingState
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import javax.inject.Inject

class SearchBoundaryCallback(private val query: String,
                             private val api: FloatPlaneApi,
                             private val videoDao: VideoDao,
                             private val disposable: CompositeDisposable,
                             private vararg val creators: String) : BaseBoundaryCallback<Video>(), Disposable {

    override fun clearItems(): Completable = videoDao.clearCreatorVideos(*creators)

    override fun noItemsLoaded(): Observable<PagingState> = creators.toObservable()
            .flatMapSingle { api.searchVideos(it, query, 0) }
            .flatMapIterable { it }
            .map { it.toEntity() }
            .toList()
            .map(::cacheVideos)
            .toObservable()

    override fun frontItemLoaded(itemAtFront: Video): Observable<PagingState> = Observable.just(PagingState.Completed)

    override fun endItemLoaded(itemAtEnd: Video): Observable<PagingState> = creators.toObservable()
            .flatMapSingle { api.searchVideos(it, query, videoDao.getNumberOfVideosByCreator(it)) }
            .flatMapIterable { it }
            .map { it.toEntity() }
            .toList()
            .map(::cacheVideos)
            .toObservable()

    private fun cacheVideos(it: List<VideoEntity>): PagingState {
        return when {
            videoDao.insert(it).isNotEmpty() -> PagingState.Fetched
            else -> PagingState.Completed
        }
    }

    class Factory @Inject constructor(private val api: FloatPlaneApi, private val videoDao: VideoDao) {
        fun newInstance(query: String, vararg creator: String) = SearchBoundaryCallback(query, api, videoDao, CompositeDisposable(), *creator)
    }

}