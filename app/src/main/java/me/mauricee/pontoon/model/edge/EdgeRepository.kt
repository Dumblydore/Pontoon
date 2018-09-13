package me.mauricee.pontoon.model.edge

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import javax.inject.Inject

class EdgeRepository @Inject constructor(private val edgeDao: EdgeDao, private val floatPlaneApi: FloatPlaneApi) {

    val downloadHost
        get() = preCache(edgeDao::getDownloadEdgeHost)

    val streamingHost
        get() = preCache(edgeDao::getStreamingEdgeHost)

    private fun cacheEdges(): Completable = floatPlaneApi.edges.flatMapIterable { it.edges }
            .map { EdgeEntity(it.allowStreaming, it.allowDownloads, it.hostname) }
            .toList()
            .flatMapCompletable { Completable.fromCallable { edgeDao.addEdges(it) } }

    private fun <T> preCache(action: () -> Single<T>): Single<T> = Single.fromCallable { edgeDao.size() }
            .flatMapCompletable { if (it > 0) Completable.complete() else cacheEdges() }
            .toSingle { 0 }.flatMap { action() }.subscribeOn(Schedulers.io())
}