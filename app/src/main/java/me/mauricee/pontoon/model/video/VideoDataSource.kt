package me.mauricee.pontoon.model.video

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.model.user.UserRepository

class VideoDataSource(private val creators: List<UserRepository.Creator>,
                      private val subs: CompositeDisposable,
                      private val floatPlaneApi: FloatPlaneApi) : PositionalDataSource<Video>() {

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        subs += creators.toObservable().flatMap { creator -> loadCreatorVideos(creator, params.startPosition) }
                .sorted(::compareVideos).toList()
                .compose(RxHelpers.applySingleSchedulers())
                .subscribe(callback::onResult)
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        subs += creators.toObservable().flatMap { creator -> loadCreatorVideos(creator, params.requestedStartPosition) }
                .sorted(::compareVideos).toList()
                .compose(RxHelpers.applySingleSchedulers())
                .subscribe { it -> callback.onResult(it, params.requestedStartPosition) }
    }

    private fun loadCreatorVideos(creator: UserRepository.Creator, offset: Int) =
            floatPlaneApi.getVideos(creator.id, offset).flatMap { it.toObservable() }
                    .map { convertVideo(it, creator) }


    private fun convertVideo(video: me.mauricee.pontoon.domain.floatplane.Video, creator: UserRepository.Creator): Video =
            Video(video.guid, video.title, video.description, video.releaseDate, video.duration, creator, video.defaultThumbnail)

    private fun compareVideos(vid1: Video, vid2: Video) = (vid2.releaseDate.epochSecond - vid1.releaseDate.epochSecond).toInt()

    class Factory(private val floatPlaneApi: FloatPlaneApi, private vararg val creators: UserRepository.Creator)
        : DataSource.Factory<Int, Video>(), Disposable {
        private val subs = CompositeDisposable()
        override fun isDisposed(): Boolean = subs.isDisposed
        override fun dispose() = subs.clear()
        override fun create(): DataSource<Int, Video> = VideoDataSource(creators.toList(), subs, floatPlaneApi)
    }
}