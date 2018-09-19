package me.mauricee.pontoon.model.edge

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
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
            .map { EdgeEntity(it.allowStreaming, it.allowDownloads, it.hostname) }
            .toList()
            .flatMapCompletable { Completable.fromCallable { edgeDao.addEdges(it) } }

    private fun <T> preCache(action: () -> Single<T>): Single<T> = Single.fromCallable { edgeDao.size() }
            .flatMapCompletable { if (it > 0) Completable.complete() else cacheEdges() }
            .toSingle { 0 }.flatMap { action() }.subscribeOn(Schedulers.io())

    private fun getAvailableHosts(hosts: List<String>): Single<String> {
        val subs = CompositeDisposable()
        return Single.create<String> { emitter ->
            hosts.map(this::makeCall).forEach {
                subs += it.subscribe { url ->
                    if (url.isNotBlank()) {
                        subs.clear()
                        emitter.onSuccess(url)
                    }
                }
            }
        }.doOnDispose(subs::dispose)
    }

    private fun makeCall(host: String) = host.toSingle()
            .map { "https://${it.replaceFirst(".", "-query.")}" }
            .flatMap { client.newCall(Request.Builder().get().url(it).build()).toSingle() }
            .map { host }
            .onErrorReturn { "" }
            .subscribeOn(Schedulers.io())
}
