package me.mauricee.pontoon.ui.main.player.playback

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.video.Stream

interface PlayerContract {

    @Parcelize
    data class Arguments(val videoId: String) : Parcelable {
        companion object {
            const val Key = "PlayerContract.Args"
        }
    }

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
        object HideControls : State() {
            override val level: EventTracker.Level
                get() = EventTracker.Level.DEBUG
        }

        data class ToggleControls(val showProgress: Boolean) : State()
        data class ControlBehavior(val areControlsAccepted: Boolean, val isFullscreen: Boolean, val isExpanded: Boolean) : State()
        data class Bind(val displayPipIcon: Boolean) : State()
        data class Preview(val path: String) : State()
        data class PreviewThumbnail(val path: String) : State()
        data class Duration(val duration: Long, val formattedDuration: String) : State() {
            override val level: EventTracker.Level
                get() = EventTracker.Level.DEBUG
        }

        data class Progress(val progress: Long, val bufferedProgress: Long, val formattedProgress: String) : State() {
            override val level: EventTracker.Level
                get() = EventTracker.Level.DEBUG
        }

        data class DisplayQualityOptions(val options: List<Stream>) : State()
        data class Quality(val qualityLevel: Stream) : State()
    }

    sealed class Action : EventTracker.Action {
        object PlayPause : Action()
        object SkipForward : Action()
        object SkipBackward : Action()
        object MinimizePlayer : Action()
        object ToggleFullscreen : Action()
        object RequestShare : Action()
        class SeekProgress(val progress: Int) : Action()
        class Download(val quality: Int) : Action()
        class Quality(val quality: Int) : Action()
    }
}
