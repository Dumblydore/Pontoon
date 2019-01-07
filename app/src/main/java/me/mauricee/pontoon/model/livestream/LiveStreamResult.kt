package me.mauricee.pontoon.model.livestream

import io.reactivex.Observable

data class LiveStreamResult(val streamUrl: String, val chat: Observable<ChatSession>)