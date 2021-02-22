package me.mauricee.pontoon.repository.video

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.data.local.video.VideoDao
import me.mauricee.pontoon.data.local.video.VideoEntity
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.repository.util.paging.BaseBoundaryCallback
import me.mauricee.pontoon.repository.util.paging.PagingState
import javax.inject.Inject

class SearchBoundaryCallback(private val query: String,
                             private val api: FloatPlaneApi,
                             private val videoDao: VideoDao,
                             private vararg val creators: String) : BaseBoundaryCallback<Video>() {

    override fun clearItems(): Completable = videoDao.clearCreatorVideos(*creators)

    override fun noItemsLoaded(): Single<PagingState> = creators.toObservable()
            .flatMapSingle { api.searchVideos(it, query, 0) }
            .flatMapIterable { it }
            .map { it.toEntity() }
            .toList()
            .map(::cacheVideos)


    override fun frontItemLoaded(itemAtFront: Video): Single<PagingState> = Single.just(PagingState.Completed)

    override fun endItemLoaded(itemAtEnd: Video): Single<PagingState> = creators.toObservable()
            .flatMapSingle { api.searchVideos(it, query, videoDao.getNumberOfVideosByCreator(it)) }
            .flatMapIterable { it }
            .map { it.toEntity() }
            .toList()
            .map(::cacheVideos)

    private fun cacheVideos(it: List<VideoEntity>): PagingState {
        return when {
            videoDao.insert(it).isNotEmpty() -> PagingState.Fetched
            else -> PagingState.Completed
        }
    }

    class Factory @Inject constructor(private val api: FloatPlaneApi, private val videoDao: VideoDao) {
        fun newInstance(query: String, vararg creator: String) = SearchBoundaryCallback(query, api, videoDao, *creator)
    }

}