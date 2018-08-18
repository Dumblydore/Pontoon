package me.mauricee.pontoon.main.player

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.EventTracker
import me.mauricee.pontoon.main.Player

interface PlayerContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State {
        object Paused : State()
        object Playing : State()
        object Loading : State()
        object Buffering : State()
        class Bind(val player: Player) : State()
        class Preview(val path: String) : State()
        class Duration(val duration: String) : State()
        class Progress(val progress: String) : State()
        class Quality(val qualityLevel: Player.QualityLevel) : State()
    }

    sealed class Action : EventTracker.Action {
        object PlayPause : Action()
        object SkipForward : Action()
        object SkipBackward : Action()
        object ToggleFullscreen : Action()
        object MinimizePlayer : Action()
        class Quality(val level: Player.QualityLevel) : Action()
    }
}