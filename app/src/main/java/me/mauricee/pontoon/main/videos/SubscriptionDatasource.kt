package me.mauricee.pontoon.main.videos

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import io.reactivex.disposables.CompositeDisposable
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository

class SubscriptionDatasource(private val videoRepository: VideoRepository,
                             private val subs: CompositeDisposable) : PositionalDataSource<Video>() {
    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
//        subs += videoRepository.getSubscriptionFeed(params.startPosition)
//                .compose(RxHelpers.applyObservableSchedulers())
//                .subscribe(callback::onResult) { callback.onResult(emptyList()) }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
//        subs += videoRepository.getSubscriptionFeed(params.requestedStartPosition)
//                .compose(RxHelpers.applyObservableSchedulers())
//                .subscribe({ callback.onResult(it, params.requestedStartPosition) }
//                        , { callback.onResult(emptyList(), params.requestedStartPosition) })
    }

    class Factory(private val videoRepository: VideoRepository,
                  private val subs: CompositeDisposable) : DataSource.Factory<Int, Video>() {
        override fun create(): DataSource<Int, Video> = SubscriptionDatasource(videoRepository, subs)
    }
}