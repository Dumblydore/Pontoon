package me.mauricee.pontoon.rx.okhttp

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.*
import okio.ByteString
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean


internal class WebsocketObservable(private val request: Request, private val okHttpClient: OkHttpClient) : Observable<WebSocketEvent>() {
    override fun subscribeActual(observer: Observer<in WebSocketEvent>) {
        okHttpClient.newWebSocket(request, Listener(okHttpClient, observer))
    }

    private class Listener(private val okHttpClient: OkHttpClient, private val observer: Observer<in WebSocketEvent>) : WebSocketListener(), Disposable {

        init {
            observer.onSubscribe(this)
        }

        private val isDisposed = AtomicBoolean(false)

        override fun isDisposed(): Boolean = isDisposed.get()

        override fun dispose() {
            okHttpClient.dispatcher().executorService().shutdown();
            isDisposed.set(true)
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            if (!isDisposed()) {
                observer.onNext(WebSocketEvent.Open(webSocket, response))
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            if (!isDisposed()) {
                observer.onError(t)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            if (!isDisposed()) {
                observer.onNext(WebSocketEvent.Message(webSocket, text))
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            if (!isDisposed()) {
                observer.onNext(WebSocketEvent.Message(webSocket, bytes.string(Charset.defaultCharset())))
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            if (!isDisposed()) {
                observer.onComplete()
            }
        }
    }
}

sealed class WebSocketEvent(val webSocket: WebSocket) {
    class Open(webSocket: WebSocket, val response: Response) : WebSocketEvent(webSocket)
    class Message(webSocket: WebSocket, val string: String) : WebSocketEvent(webSocket)
}