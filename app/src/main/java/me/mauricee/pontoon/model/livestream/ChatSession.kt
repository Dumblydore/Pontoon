package me.mauricee.pontoon.model.livestream

import okhttp3.OkHttpClient
import okhttp3.Request


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
class ChatSession(private val client: OkHttpClient) {

    init {
        Request.Builder()

//client.openSocket()
    }

    companion object {
        private val chatUrl = "https://chat.floatplane.com/socket.io/?__sails_io_sdk_version=0.13.8&__sails_io_sdk_platform=browser&__sails_io_sdk_language=javascript&EIO=3&transport=websocket"
    }

}