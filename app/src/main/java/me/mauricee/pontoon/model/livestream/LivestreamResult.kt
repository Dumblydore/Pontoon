package me.mauricee.pontoon.model.livestream

import io.reactivex.Observable

data class LivestreamResult(val streamUrl: String, val chat: Observable<ChatSession>)