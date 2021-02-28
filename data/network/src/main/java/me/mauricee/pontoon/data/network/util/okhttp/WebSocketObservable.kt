package me.mauricee.pontoon.data.network.util.okhttp

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.*
import okio.ByteString
import java.util.concurrent.atomic.AtomicBoolean

fun OkHttpClient.observeWebSocket(request: Request): Observable<WebSocketEvent> {
    return WebSocketObservable(request, this)
}

class WebSocketObservable(private val request: Request, private val client: OkHttpClient) : Observable<WebSocketEvent>() {

    override fun subscribeActual(observer: Observer<in WebSocketEvent>) {
        val listener = Listener(observer).also(observer::onSubscribe)
        client.newWebSocket(request, listener)
    }

    private inner class Listener(private val observer: Observer<in WebSocketEvent>) : WebSocketListener(), Disposable {
        private val isDisposed = AtomicBoolean(false)
        private var webSocket: WebSocket? = null
        override fun onOpen(webSocket: WebSocket, response: Response) {
            this.webSocket = webSocket
            observer.onNext(WebSocketEvent.OnOpen(response))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            observer.onNext(WebSocketEvent.OnMessageAsString(text))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            observer.onNext(WebSocketEvent.OnMessageAsByteString(bytes))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            observer.onNext(WebSocketEvent.OnClosing(code, reason))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            observer.onNext(WebSocketEvent.OnClosed(code, reason))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            observer.onNext(WebSocketEvent.OnFailure(t, response))
        }

        override fun dispose() {
            webSocket?.close(1000, null)
            isDisposed.set(true)
        }

        override fun isDisposed(): Boolean = isDisposed.get()
    }
}

sealed class WebSocketEvent {
    data class OnOpen(val response: Response) : WebSocketEvent()
    data class OnMessageAsString(val text: String) : WebSocketEvent()
    data class OnMessageAsByteString(val bytes: ByteString) : WebSocketEvent()
    data class OnClosing(val code: Int, val reason: String) : WebSocketEvent()
    data class OnClosed(val code: Int, val reason: String) : WebSocketEvent()
    data class OnFailure(val t: Throwable, val response: Response?) : WebSocketEvent()
}