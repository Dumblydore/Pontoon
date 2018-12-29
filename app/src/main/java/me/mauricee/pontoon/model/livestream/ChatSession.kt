package me.mauricee.pontoon.model.livestream

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.LiveStreamMetadata
import me.mauricee.pontoon.rx.okhttp.WebSocketEvent
import me.mauricee.pontoon.rx.okhttp.openSocket
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import javax.inject.Inject


/*
*  0{"sid":"OOlqhCmP8zFybMsZAAIl","upgrades":[],"pingInterval":25000,"pingTimeout":60000}
*  40
*  420["get",{"method":"get","headers":{},"data":{"channel":"/live/5c13f3c006f1be15e08e05c0","message":null},"url":"/RadioMessage/joinLivestreamRadioFrequency/"}]
*  421["get",{"method":"get","headers":{},"data":{"channel":"/live/5c13f3c006f1be15e08e05c0","message":null},"url":"/RadioMessage/joinLivestreamRadioFrequency/"}]
*  430[{"body":{"success":true},"headers":{},"statusCode":200}]
*  431[{"body":{"success":true},"headers":{},"statusCode":200}]
*  42["radioChatter",{"id":"cjpou9pfb00j4083oa9vvzex3","userGUID":"5a9bbb7910a6f56d7dc0ffa1","username":"GriffinBaxter","channel":"/live/5c13f3c006f1be15e08e05c0","message":"!!!","userType":"Normal","success":true}]
*  422["post",{"method":"post","headers":{},"data":{"channel":"/live/5c13f3c006f1be15e08e05c0","message":"Great stream!"},"url":"/RadioMessage/sendLivestreamRadioChatter/"}]
* */
class ChatSession(private val liveStreamMetadata: LiveStreamMetadata, private val socket: WebSocket, private val events: Observable<WebSocketEvent.Message>) {

    init {
        socket.send("420[\"get\",{\"method\":\"get\",\"headers\":{},\"data\":{\"channel\":\"${liveStreamMetadata.streamPath}\",\"message\":null},\"url\":\"/RadioMessage/joinLivestreamRadioFrequency/\"}]")
    }
    val radioChatter: Observable<String>
        get() = filterFor(RadioChatterId)
    val usersInStream: Single<String>
        get() = send("422[\"get\",{\"method\":\"get\",\"headers\":{},\"data\":{\"channel\":\"/live/${liveStreamMetadata.id}\",\"message\":\"\"},\"url\":\"/RadioMessage/getChatUserList/\"}]", UsersInStreamResponseId)

    fun comment(comment: String) {
        socket.send(comment)
    }

    private fun send(request: String, responseId: String): Single<String> = Completable.fromAction { socket.send(request) }
            .andThen(filterFor(responseId).firstOrError())

    private fun filterFor(responseId: String) = events.map { it.string }.filter { it.contains(responseId) }

    class Builder @Inject constructor(private val client: OkHttpClient, private val accountManagerHelper: AccountManagerHelper) {
        fun startSession(liveStreamMetadata: LiveStreamMetadata): Observable<ChatSession> = Request.Builder().url(chatUrl)
                .addHeader("Cookie", " __cfduid=${accountManagerHelper.cfduid}; sails.sid=${accountManagerHelper.sid}")
                .addHeader("Origin", " https://www.floatplane.com")
                .build().let { client.openSocket(it) }.share().compose { socket ->
                    socket.filter { it is WebSocketEvent.Open }.map {
                        ChatSession(liveStreamMetadata, it.webSocket, socket.filter { a -> a is WebSocketEvent.Message }.cast(WebSocketEvent.Message::class.java))
                    }
                }
    }

    companion object {
        private const val RadioChatterId = "42"
        private const val UsersInStreamResponseId = "432"
        private val chatUrl = "https://chat.floatplane.com/socket.io/?__sails_io_sdk_version=0.13.8&__sails_io_sdk_platform=browser&__sails_io_sdk_language=javascript&EIO=3&transport=websocket"


    }
}