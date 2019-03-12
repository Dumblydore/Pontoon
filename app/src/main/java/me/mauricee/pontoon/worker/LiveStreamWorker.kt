package me.mauricee.pontoon.worker

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.toSingle
import me.mauricee.pontoon.Pontoon
import me.mauricee.pontoon.common.NotificationHelper
import me.mauricee.pontoon.domain.floatplane.LiveStreamMetadata
import me.mauricee.pontoon.ext.loge
import me.mauricee.pontoon.main.MainActivity
import me.mauricee.pontoon.model.livestream.LiveStreamRepository
import okhttp3.OkHttpClient
import javax.inject.Inject

class LiveStreamWorker(context: Context, workerParameters: WorkerParameters) : RxWorker(context, workerParameters) {
    @Inject
    lateinit var liveStreamRepository: LiveStreamRepository
    @Inject
    lateinit var notificationHelper: NotificationHelper
    @Inject
    lateinit var okHttpClient: OkHttpClient

    init {
        (applicationContext as Pontoon).appComponent.workerComponent().inject(this)
    }

    override fun createWork(): Single<Result> = liveStreamRepository.activeLiveStreams
            .flatMapObservable { it.toObservable() }
            .flatMapCompletable { metadata ->
//                okHttpClient.newCall(Request.Builder().url(metadata.thumbnail.path).build())
//                        .asSingle().map { BitmapFactory.decodeStream(it.body()?.byteStream()) }
//                        .flatMapCompletable { buildNotification(metadata, it) }
                buildNotification(metadata)
            }
            .andThen(Result.success().toSingle())
            .doOnError { loge("Error!", it) }
            .onErrorReturnItem(Result.retry())

    private fun buildNotification(metaData: LiveStreamMetadata) = Completable.fromAction {
        notificationHelper.importantNotification(MainActivity.buildIntentForLivestream(applicationContext, metaData), NotificationHelper.LiveStreamNotificationChannel) {
            it.setContentTitle("${metaData.owner} just went live!")
            it.setSubText(metaData.description)
//            it.setLargeIcon(thumbnail)
        }
    }

}