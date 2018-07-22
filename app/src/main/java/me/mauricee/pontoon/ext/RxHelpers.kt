package me.mauricee.pontoon.ext

import androidx.core.widget.NestedScrollView
import com.jakewharton.rxbinding2.view.ViewScrollChangeEvent
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.main.videos.VideoContract

class RxHelpers {

    companion object {
        fun <T> applyObservableSchedulers(observeOn: Scheduler = AndroidSchedulers.mainThread()): ObservableTransformer<T, T> {
            return ObservableTransformer { it.subscribeOn(Schedulers.io()).observeOn(observeOn) }
        }

        fun <T> applyFlowableSchedulers(observeOn: Scheduler = AndroidSchedulers.mainThread()): FlowableTransformer<T, T> {
            return FlowableTransformer { it.subscribeOn(Schedulers.io()).observeOn(observeOn) }
        }

        fun <T> applySingleSchedulers(observeOn: Scheduler = AndroidSchedulers.mainThread()): SingleTransformer<T, T> {
            return SingleTransformer{ it.subscribeOn(Schedulers.io()).observeOn(observeOn) }
        }


    }
}

fun <T> T.toObservable() = Observable.just(this)!!

