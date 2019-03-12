package me.mauricee.pontoon.model.livestream

import io.reactivex.Observable
import me.mauricee.pontoon.domain.floatplane.LiveStreamMetadata

data class LiveStreamResult(val liveStreamMetadata: LiveStreamMetadata, val chat: Observable<ChatSession>)