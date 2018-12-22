package me.mauricee.pontoon.model.video

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

private typealias VideoPojo = me.mauricee.pontoon.domain.floatplane.Video

class VideoBoundaryCallback(private val api: FloatPlaneApi,
                            private val videoDao: VideoDao,
                            private val disposable: CompositeDisposable,
                            private vararg val creators: UserRepository.Creator)
    : StateBoundaryCallback<Video>(), Disposable {

    private var isLoading = false

    init {
        onZeroItemsLoaded()
    }

    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()
        if (isLoading) return
        isLoading = true
        stateRelay.accept(State.LOADING)
        disposable += creators.toObservable().flatMap { api.getVideos(it.id).flatMapIterable { it } }
                .sorted(this::sortVideos)
                .map{it.toEntity()}.toList()
                .compose(RxHelpers.applySingleSchedulers(Schedulers.io()))
                .subscribe({ it -> cacheVideos(it) }, { stateRelay.accept(State.ERROR) })
    }

    override fun onItemAtEndLoaded(itemAtEnd: Video) {
        super.onItemAtEndLoaded(itemAtEnd)
        if (isLoading) return
        isLoading = true
        stateRelay.accept(State.LOADING)
        disposable += creators.toObservable().flatMap {
            api.getVideos(it.id, videoDao.getNumberOfVideosByCreator(it.id))
        }.flatMapIterable { it }
                .sorted(this::sortVideos)
                .map{it.toEntity()}.toList()
                .compose(RxHelpers.applySingleSchedulers(Schedulers.io()))
                .subscribe({ it -> cacheVideos(it) }, { stateRelay.accept(State.ERROR) })
    }

    private fun cacheVideos(it: MutableList<VideoEntity>) {
        when {
            videoDao.cacheVideos(*it.toTypedArray()).isNotEmpty() -> stateRelay.accept(State.FETCHED)
            else -> stateRelay.accept(State.FINISHED)
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
        fun newInstance(vararg creator: UserRepository.Creator): VideoBoundaryCallback = VideoBoundaryCallback(api, videoDao, CompositeDisposable(), *creator)
    }

}