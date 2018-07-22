package me.mauricee.pontoon.main.player

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.main.details.DetailsContract

interface PlayerContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State {
        object Paused : State()
        object Playing : State()
        object Loading : State()
        class Bind(val player: Player) : State()
        class Preview(val path: String) : State()
        class Duration(val duration: String) : State()
        class Progress(val progress: String) : State()
    }

    sealed class Action {
        object PlayPause : Action()
        object SkipForward: Action()
        object SkipBackward: Action()
        object ToggleFullscreen: Action()
        object MinimizePlayer: Action()
        class Quality(val type: Player.QualityLevel):  Action()
    }
}