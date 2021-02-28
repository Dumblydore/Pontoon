package me.mauricee.pontoon.data.network.creator.livestream

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import me.mauricee.pontoon.data.network.util.okhttp.WebSocketEvent
import me.mauricee.pontoon.data.network.util.okhttp.observeWebSocket
import okhttp3.OkHttpClient
import okhttp3.Request

class ChatWebSocketSession(private val client: OkHttpClient) {
    fun connect(): Flowable<WebSocketEvent> = Observable.defer {
        val request = Request.Builder()
                .url("wss://www.floatplane.com/socket.io/?__sails_io_sdk_version=0.13.8&__sails_io_sdk_platform=browser&__sails_io_sdk_language=javascript&EIO=3&transport=websocket")
                .build()
        client.observeWebSocket(request)
    }/*.map<ChatEvent> {
        ChatEvent.Other
    }*/.toFlowable(BackpressureStrategy.LATEST)
}

sealed class ChatEvent {
    object Other : ChatEvent()
}