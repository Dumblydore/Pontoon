package me.mauricee.pontoon.player.player

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.video.Video

interface PlayerContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>
    interface Controls {
        fun toggleFullscreen()
        fun setPlayerExpanded(isExpanded: Boolean)
        fun setVideoRatio(ratio: String)
    }

    sealed class State : EventTracker.State {
        object Paused : State()
        object Playing : State()
        object DownloadStart : State()
        object DownloadFailed : State()
        object Error : State()
        class ShareUrl(val video: Video) : State()
        class Bind(val displayPipIcon: Boolean) : State()
        class Preview(val path: String) : State()
        class PreviewThumbnail(val path: String) : State()
        class Duration(val duration: Long, val formattedDuration: String) : State() {
            override val level: EventTracker.Level
                get() = EventTracker.Level.DEBUG
        }

        class Progress(val progress: Long, val bufferedProgress: Long, val formattedProgress: String) : State() {
            override val level: EventTracker.Level
                get() = EventTracker.Level.DEBUG
        }

        class Quality(val qualityLevel: Player.QualityLevel) : State()
    }

    sealed class Action : EventTracker.Action {
        object PlayPause : Action()
        object SkipForward : Action()
        object SkipBackward : Action()
        object ToggleFullscreen : Action()
        object MinimizePlayer : Action()
        object RequestShare: Action()
        class SeekProgress(val progress: Int) : Action()
        class Download(val quality: Player.QualityLevel) : Action()
        class Quality(val qualityLevel: Player.QualityLevel) : Action()
    }
}
