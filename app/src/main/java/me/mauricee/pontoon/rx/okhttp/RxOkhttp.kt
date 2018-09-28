package me.mauricee.pontoon.rx.okhttp

import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.Call
import okhttp3.Response

fun Call.toObservable(): Observable<Response> = CallObservable(this)
fun Call.toSingle(): Single<Response> = CallObservable(this).singleOrError()