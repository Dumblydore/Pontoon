package me.mauricee.pontoon.model.video

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.RxHelpers
import javax.inject.Inject

private typealias VideoPojo = me.mauricee.pontoon.domain.floatplane.VideoJson

class VideoBoundaryCallback(private val api: FloatPlaneApi,
                            private val videoDao: VideoDao,
                            private val disposable: CompositeDisposable,
                            private vararg val creatorIds: String)
    : StateBoundaryCallback<Video>(), Disposable {

    private var isLoading = false

    init {
        onZeroItemsLoaded()
    }

    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()
        if (isLoading) return
        isLoading = true
        stateRelay.accept(State.Loading)
        disposable += creatorIds.toObservable().flatMap { api.getVideos(it).flatMapIterable { it } }
                .sorted(this::sortVideos)
                .map { it.toEntity() }.toList()
                .compose(RxHelpers.applySingleSchedulers(Schedulers.io()))
                .subscribe(this::cacheVideos) { stateRelay.accept(State.Error) }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Video) {
        super.onItemAtEndLoaded(itemAtEnd)
        if (isLoading) return
        isLoading = true
        stateRelay.accept(State.Loading)
        disposable += creatorIds.toObservable().flatMap {
            api.getVideos(it, videoDao.getNumberOfVideosByCreator(it))
        }.flatMapIterable { it }
                .sorted(this::sortVideos)
                .map { it.toEntity() }.toList()
                .compose(RxHelpers.applySingleSchedulers(Schedulers.io()))
                .subscribe(this::cacheVideos) { stateRelay.accept(State.Error) }
    }

    private fun cacheVideos(it: MutableList<VideoEntity>) {
        when {
            videoDao.insert(it).isNotEmpty() -> stateRelay.accept(State.Fetched)
            else -> stateRelay.accept(State.Finished)
        }
        isLoading = false
    }

    private fun sortVideos(video1: VideoPojo, video2: VideoPojo) =
            (video2.releaseDate.epochSecond - video1.releaseDate.epochSecond).toInt()

    override fun isDisposed(): Boolean = disposable.isDisposed

    override fun dispose() {
        disposable.dispose()
    }


    class Factory @Inject constructor(private val api: FloatPlaneApi, private val videoDao: VideoDao) {
        fun newInstance(vararg creator: String): VideoBoundaryCallback = VideoBoundaryCallback(api, videoDao, CompositeDisposable(), *creator)
    }

}