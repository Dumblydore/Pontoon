package me.mauricee.pontoon.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.toSingle
import me.mauricee.pontoon.Pontoon
import me.mauricee.pontoon.common.NotificationHelper
import me.mauricee.pontoon.ext.doOnIo
import me.mauricee.pontoon.ext.loge
import me.mauricee.pontoon.main.MainActivity
import me.mauricee.pontoon.model.livestream.LiveStreamInfo
import me.mauricee.pontoon.model.livestream.LiveStreamRepository
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.rx.okhttp.asSingle
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class LiveStreamWorker(context: Context, workerParameters: WorkerParameters) : RxWorker(context, workerParameters) {
    @Inject
    lateinit var liveStreamRepository: LiveStreamRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var notificationHelper: NotificationHelper
    @Inject
    lateinit var okHttpClient: OkHttpClient

    init {
        (applicationContext as Pontoon).appComponent.workerComponent().inject(this)
    }

    override fun createWork(): Single<Result> = liveStreamRepository.activeLiveStreams
            .flatMapObservable { it.toObservable() }
            .flatMapCompletable { stream ->
                okHttpClient.newCall(Request.Builder().url(stream.liveStreamMetadata.thumbnail.path).build())
                        .asSingle().map { BitmapFactory.decodeStream(it.body()?.byteStream()) }
                        .flatMapCompletable { buildNotification(stream, it) }
                        .doOnIo()
            }
            .andThen(Result.success().toSingle())
            .doOnError { loge("Error!", it) }
            .onErrorReturnItem(Result.retry())


    private fun buildNotification(info: LiveStreamInfo, thumbnail: Bitmap) = Completable.fromAction {
        notificationHelper.importantNotification(MainActivity.buildIntentForLivestream(applicationContext, info), NotificationHelper.LiveStreamNotificationChannel) {
            it.setContentTitle("${info.creator.name} just went live!")
            it.setSubText(info.liveStreamMetadata.description)
            it.setLargeIcon(thumbnail)
        }
    }

}