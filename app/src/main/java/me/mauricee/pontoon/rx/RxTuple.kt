package me.mauricee.pontoon.rx

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3


object RxTuple {

    fun <T, R> zipAsPair(t1: Maybe<T>, t2: Maybe<R>): Maybe<Pair<T, R>> = Maybe.zip(t1, t2, BiFunction(::Pair))

    fun <T, R> zipAsPair(t1: Single<T>, t2: Single<R>): Single<Pair<T, R>> = Single.zip(t1, t2, BiFunction(::Pair))

    fun <T, R, V> zipAsTriple(t1: Single<T>, t2: Single<R>, t3: Single<V>): Single<Triple<T, R, V>> = Single.zip(t1, t2, t3, Function3(::Triple))

    fun <T, R> combineLatestAsPair(t1: Observable<T>, t2: Observable<R>): Observable<Pair<T, R>> =
            Observable.combineLatest<T, R, Pair<T, R>>(t1, t2, BiFunction(::Pair))

    fun <T, R> combineLatestAsPair(t1: Flowable<T>, t2: Flowable<R>): Flowable<Pair<T, R>> =
            Flowable.combineLatest<T, R, Pair<T, R>>(t1, t2, BiFunction(::Pair))

    fun <T, R, V> combineLatestAsTriple(t1: Observable<T>, t2: Observable<R>, t3: Observable<V>): Observable<Triple<T, R, V>> =
            Observable.combineLatest<T, R, V, Triple<T, R, V>>(t1, t2, t3, Function3(::Triple))

    fun <T, R, V, X> combineLatestAsQuad(t1: Observable<T>, t2: Observable<R>, t3: Observable<V>, t4: Observable<X>): Observable<Quad<T, R, V, X>> =
            Observable.combineLatest<T, R, V, X, Quad<T, R, V, X>>(t1, t2, t3, t4, ::Quad)

}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)