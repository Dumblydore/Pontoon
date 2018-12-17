package me.mauricee.pontoon.model.livestream

import io.reactivex.Observable
import me.mauricee.pontoon.domain.floatplane.livestream.RadioChatter

data class LivestreamResult(val streamUrl: String, val chat: Observable<RadioChatter>)