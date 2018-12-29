package me.mauricee.pontoon.rx.okhttp

import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

fun Call.asObservable(): Observable<Response> = CallObservable(this)
fun Call.asSingle(): Single<Response> = CallObservable(this).singleOrError()
fun OkHttpClient.openSocket(request: Request): Observable<WebSocketEvent> = WebSocketObservable(request, this)