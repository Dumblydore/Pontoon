package me.mauricee.pontoon.ext

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.common.log.loge

class RxHelpers {

    companion object {
        fun <T> applyObservableSchedulers(observeOn: Scheduler = AndroidSchedulers.mainThread()): ObservableTransformer<T, T> {
            return ObservableTransformer { it.subscribeOn(Schedulers.io()).observeOn(observeOn) }
        }

        fun <T> applyFlowableSchedulers(observeOn: Scheduler = AndroidSchedulers.mainThread()): FlowableTransformer<T, T> {
            return FlowableTransformer { it.subscribeOn(Schedulers.io()).observeOn(observeOn) }
        }

        fun <T> applySingleSchedulers(observeOn: Scheduler = AndroidSchedulers.mainThread()): SingleTransformer<T, T> {
            return SingleTransformer { it.subscribeOn(Schedulers.io()).observeOn(observeOn) }
        }

        fun applyCompletableSchedulers(observeOn: Scheduler = AndroidSchedulers.mainThread()): CompletableTransformer {
            return CompletableTransformer { it.subscribeOn(Schedulers.io()).observeOn(observeOn) }
        }

        fun <T> applyOnErrorReturnItem(error: T): ObservableTransformer<T, T> =
                ObservableTransformer {
                    it.doOnError { loge("Error!", it) }.onErrorReturnItem(error)
                }
    }
}

fun <T> Single<T>.ioStream() = this.compose(RxHelpers.applySingleSchedulers())
fun <T> Observable<T>.ioStream() = this.compose(RxHelpers.applyObservableSchedulers())
fun Completable.doOnIo() = this.compose(RxHelpers.applyCompletableSchedulers())


fun <T> T.toObservable() = Observable.just(this)

inline fun <reified T> Single<List<T>>.throwIfEmpty(): Single<List<T>> = flatMap {
    if (it.isEmpty())
        Single.error(IllegalStateException("List<${T::class.simpleName}> is empty!"))
    else
        Single.just(it)

}