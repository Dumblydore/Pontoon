package me.mauricee.pontoon.main.creator

import androidx.annotation.StringRes
import androidx.paging.PagedList
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository

interface CreatorContract {

    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action {
        class Refresh(val creator: String, val offset: Int = 0) : Action()
        class PlayVideo(val video: Video) : Action()
    }

    sealed class State {
        object Loading : State()
        class DisplayCreator(val creator: UserRepository.Creator) : State()
        class DisplayVideos(val videos: PagedList<Video>) : State()
        class Error(val type: Type = Type.Unknown) : State() {
            enum class Type(@StringRes val msg: Int) {
                Network(R.string.creator_error_noCreator),
                NoVideos(R.string.creator_error_noVideos),
                Unknown(R.string.creator_error_general)
            }
        }
    }
}