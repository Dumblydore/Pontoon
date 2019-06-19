package me.mauricee.pontoon.model.edge

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.doOnIo
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.rx.okhttp.asSingle
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class EdgeRepository @Inject constructor(private val edgeStore: StoreRoom<List<String>, EdgeDao.Persistor.EdgeType>,
                                         private val client: OkHttpClient) {

    val downloadHost: Single<String>
        get() = edgeStore.get(EdgeDao.Persistor.EdgeType.Download).firstOrError().flatMap(::getAvailableHosts)

    val streamingHost: Single<String>
        get() = edgeStore.get(EdgeDao.Persistor.EdgeType.Streaming).firstOrError().flatMap(::getAvailableHosts)

    fun refresh(): Completable = Completable.fromAction { edgeStore.clear() }

    private fun getAvailableHosts(hosts: List<String>): Single<String> = hosts.map(::makeCall)
            .let { Observable.amb(it) }
            .firstOrError()

    private fun makeCall(host: String) = host.toObservable()
            .map { "https://${it.replaceFirst(".", "-query.")}" }
            .flatMapSingle { client.newCall(Request.Builder().get().url(it).build()).asSingle() }
            .map { host }
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext(Observable.empty())
}
