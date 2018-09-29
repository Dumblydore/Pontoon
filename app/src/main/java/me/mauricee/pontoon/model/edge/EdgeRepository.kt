package me.mauricee.pontoon.model.edge

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.rx.okhttp.toSingle
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class EdgeRepository @Inject constructor(private val edgeDao: EdgeDao,
                                         private val floatPlaneApi: FloatPlaneApi,
                                         private val client: OkHttpClient) {

    val downloadHost
        get() = preCache { edgeDao.getDownloadEdgeHosts().flatMap(::getAvailableHosts) }

    val streamingHost
        get() = preCache { edgeDao.getStreamingEdgeHosts().flatMap(::getAvailableHosts) }

    private fun cacheEdges(): Completable = floatPlaneApi.edges.flatMapIterable { it.edges }
            .map { EdgeEntity(it.allowStreaming, it.allowDownload, it.hostname) }
            .toList()
            .flatMapCompletable { Completable.fromCallable { edgeDao.addEdges(it) } }

    private fun <T> preCache(action: () -> Single<T>): Single<T> = Single.fromCallable(edgeDao::size)
            .flatMapCompletable { if (it > 0) Completable.complete() else cacheEdges() }
            .toSingle { 0 }.flatMap { action() }.subscribeOn(Schedulers.io())

    private fun getAvailableHosts(hosts: List<String>): Single<String> = hosts.map(::makeCall)
            .let { Observable.amb(it) }
            .firstOrError()

    private fun makeCall(host: String) = host.toObservable()
            .map { "https://${it.replaceFirst(".", "-query.")}" }
            .flatMapSingle { client.newCall(Request.Builder().get().url(it).build()).toSingle() }
            .map { host }
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext(Observable.empty())
}
