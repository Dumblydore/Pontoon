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
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

class VideoBoundaryCallback(private val api: FloatPlaneApi,
                            private val videoDao: VideoDao,
                            private val disposable: CompositeDisposable,
                            private vararg val creators: UserRepository.Creator)
    : StateBoundaryCallback<Video>(), Disposable {


    override fun onZeroItemsLoaded() {
        if (isLoading) return
        isLoading = true
        disposable += creators.toObservable().flatMap { api.getVideos(it.id).flatMapIterable { it } }
                .map(this::convertVideo)
                .toList().doAfterTerminate { isLoading = false }
                .compose(RxHelpers.applySingleSchedulers(Schedulers.io()))
                .subscribe({ it -> videoDao.insert(*it.toTypedArray()) }, { stateRelay.accept(State.ERROR) })
    }

    override fun onItemAtEndLoaded(itemAtEnd: Video) {
        if (isLoading) return
        isLoading = true
        disposable += Observable.fromCallable { videoDao.getNumberOfVideosByCreator(itemAtEnd.creator.id) }
                .flatMap { api.getVideos(itemAtEnd.creator.id, it) }
                .flatMap { it.toObservable().map(this::convertVideo) }
                .toList()
                .doAfterTerminate { isLoading = false }
                .compose(RxHelpers.applySingleSchedulers(Schedulers.io()))
                .subscribe({ it -> videoDao.insert(*it.toTypedArray()) }, { stateRelay.accept(State.ERROR) })
    }

    override fun isDisposed(): Boolean = disposable.isDisposed

    override fun dispose() {
        disposable.dispose()
    }

    private fun convertVideo(video: me.mauricee.pontoon.domain.floatplane.Video) = VideoEntity(video.guid, video.creator, video.description, video.releaseDate, video.duration, video.defaultThumbnail, video.title)


    class Factory @Inject constructor(private val api: FloatPlaneApi, private val videoDao: VideoDao) {

        fun newInstance(vararg creator: UserRepository.Creator): VideoBoundaryCallback = VideoBoundaryCallback(api, videoDao, CompositeDisposable(), *creator)
    }

}