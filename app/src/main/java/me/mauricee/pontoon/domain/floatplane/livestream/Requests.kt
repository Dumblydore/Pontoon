package me.mauricee.pontoon.domain.floatplane.livestream

enum class RequestPaths(val channel: Int, val path: String) {
    ChatList(422, "/RadioMessage/getChatUserList/"),
    Comment(424,"/RadioMessage/sendLivestreamRadioChatter/")
}