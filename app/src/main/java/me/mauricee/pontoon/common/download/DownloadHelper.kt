package me.mauricee.pontoon.common.download

import android.app.DownloadManager
import android.content.Context
import androidx.core.content.getSystemService
import io.reactivex.Single
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.repository.video.VideoRepository
import javax.inject.Inject


class DownloadHelper @Inject constructor(private val videoRepository: VideoRepository,
                                         private val context: Context) {

    private val downloadManager: DownloadManager by lazy { context.getSystemService<DownloadManager>()!! }

    //TODO downloads broken
    fun download(video: Video, quality: String): Single<Boolean> = Single.just(false) /*checkForPermission()
            .flatMap { if (it) attemptToDownload(video, quality) else Single.just(false) }
            .onErrorReturnItem(false)*/

//    private fun checkForPermission() = if (permissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) Single.just(true) else
//        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).map { it.state() == Permission.State.GRANTED }
//
//    private fun attemptToDownload(video: Video, quality: String) = videoRepository.getDownloadLink(video.id, quality).map {
//        val request = DownloadManager.Request(it.toUri())
//        request.setTitle(context.getString(R.string.download_notification, video.entity.title))
//        request.allowScanningByMediaScanner()
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, generateFilename(video, it.toUri()))
//    }.flatMap {
//        Completable.fromAction { downloadManager.enqueue(it) }.andThen(Single.just(true))
//    }

//    private fun generateFilename(video: Video, path: Uri): String =
//            "${video.entity.title.replace("\\W+".toRegex(), "_")}_${path.pathSegments.first { it.contains(".") }}"
//                    .toLowerCase()
}
