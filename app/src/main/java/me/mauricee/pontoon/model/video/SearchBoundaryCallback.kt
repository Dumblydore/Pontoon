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

class SearchBoundaryCallback(private val query: String,
                             private val api: FloatPlaneApi,
                             private val videoDao: VideoDao,
                             private val disposable: CompositeDisposable,
                             private vararg val creators: UserRepository.Creator)
    : StateBoundaryCallback<Video>(), Disposable {

    private var isLoading = false

    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()
        if (isLoading) return
        isLoading = true
        stateRelay.accept(State.LOADING)
        disposable += creators.toObservable().flatMap { api.getVideos(it.id).flatMapIterable { it } }
                .filter { it.title.contains(query, true) }
                .map(this::convertVideo).toList()
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
                .filter { it.title.contains(query, true) }
                .map(this::convertVideo).toList()
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

    override fun isDisposed(): Boolean = disposable.isDisposed

    override fun dispose() {
        disposable.dispose()
    }

    private fun convertVideo(video: me.mauricee.pontoon.domain.floatplane.Video) = VideoEntity(video.guid, video.creator, video.description, video.releaseDate, video.duration, video.defaultThumbnail, video.title)


    class Factory @Inject constructor(private val api: FloatPlaneApi, private val videoDao: VideoDao) {
        fun newInstance(query: String, vararg creator: UserRepository.Creator) = SearchBoundaryCallback(query, api, videoDao, CompositeDisposable(), *creator)
    }

}