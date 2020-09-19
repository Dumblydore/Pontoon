package me.mauricee.pontoon.model.video

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.RxHelpers
import javax.inject.Inject

class SearchBoundaryCallback(private val query: String,
                             private val api: FloatPlaneApi,
                             private val videoDao: VideoDao,
                             private val disposable: CompositeDisposable,
                             private vararg val creators: String)
    : StateBoundaryCallback<Video>(), Disposable {

    private var isLoading = false

    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()
        if (isLoading) return
        isLoading = true
        stateRelay.accept(State.Loading)
        disposable += pullUntilHit().flatMapIterable { it }
                .map { it.toEntity() }.toList()
                .compose(RxHelpers.applySingleSchedulers(Schedulers.io()))
                .subscribe({ it -> cacheVideos(it) }, { stateRelay.accept(State.Error) })
    }

    override fun onItemAtEndLoaded(itemAtEnd: Video) {
        super.onItemAtEndLoaded(itemAtEnd)
        if (isLoading) return
        isLoading = true
        stateRelay.accept(State.Loading)
        disposable += pullUntilHit().flatMapIterable { it }
                .map { it.toEntity() }.toList()
                .compose(RxHelpers.applySingleSchedulers(Schedulers.io()))
                .subscribe({ it -> cacheVideos(it) }, { stateRelay.accept(State.Error) })
    }


    private fun pullUntilHit(startAt: Int = 0): Observable<List<me.mauricee.pontoon.domain.floatplane.VideoJson>> = creators.toObservable()
            .flatMap { api.getVideos(it, videoDao.getNumberOfVideosByCreator(it)) }
            .flatMapIterable { it }
            .toList().flatMapObservable { videos ->
                if (videos.any { it.title.contains(query, ignoreCase = true) }) Observable.just(videos)
                else pullUntilHit(videos.size + startAt)
            }.flatMapIterable { it }.toList().toObservable()

    private fun cacheVideos(it: MutableList<VideoEntity>) {
        when {
            videoDao.insert(it).isNotEmpty() -> stateRelay.accept(State.Fetched)
            else -> stateRelay.accept(State.Finished)
        }
        isLoading = false
    }

    override fun isDisposed(): Boolean = disposable.isDisposed

    override fun dispose() {
        disposable.dispose()
    }


    class Factory @Inject constructor(private val api: FloatPlaneApi, private val videoDao: VideoDao) {
        fun newInstance(query: String, vararg creator: String) = SearchBoundaryCallback(query, api, videoDao, CompositeDisposable(), *creator)
    }

}